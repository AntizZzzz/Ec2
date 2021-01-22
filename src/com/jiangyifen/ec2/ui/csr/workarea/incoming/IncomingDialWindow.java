package com.jiangyifen.ec2.ui.csr.workarea.incoming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

/**
 * 呼入窗口
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class IncomingDialWindow extends Window implements CloseListener, CloseHandler {
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	// 弹屏后是否需要将分机置忙
	private final static String PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW = "pasue_exten_after_csr_popup_calling_window";
	private final static String CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW = "create_afterCall_log_after_csr_popup_calling_window";	// 是否需要创建话后处理日志

	private TabSheet tabSheet;
	private IncomingDialTabView incomingDialTabView;
	
	private boolean isInMainWindow = false;							// 描述该窗口是否已经在主界面显示了，默认为没有
	private boolean isEncryptMobile = true; 						// 默认的情况下，只显示客户的详细信息中的电话和手机号使用加密模式
	private Map<String, IncomingDialTabView> callerNumToTabView;	// 呼入的电话号对应的tab页，用于避免一路电话又多个tab页的情况

	private User loginUser;
	private Long domainId;
	private Boolean ispauseExtenPopupWindow = false;				// 弹屏是否需要置忙
	private Boolean iscreateCallAfterLog = false;					// 弹屏是否需要创建话后处理记录
	private TelephoneService telephoneService;

	public IncomingDialWindow(User loginUser) {
		this.center();
		this.setImmediate(true);
		this.setResizable(false);
		this.setStyleName("opaque");
		this.addListener((CloseListener)this);
		this.loginUser = loginUser;
		
		domainId = loginUser.getDomain().getId();
		
		telephoneService = SpringContextHolder.getBean("telephoneService");

		// 获取当前登陆话务员所有权限
		ArrayList<String> ownBusinessModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			}
		}
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		callerNumToTabView = new HashMap<String, IncomingDialTabView>();
		ShareData.csrToIncomingDialWindow.put(loginUser.getId(), this);
		
		tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		tabSheet.setImmediate(true);
		tabSheet.setCloseHandler(this);
		this.addComponent(tabSheet);
	}

	@Override
	public void attach() {
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		WebBrowser webBrowser = context.getBrowser();
		int width = webBrowser.getScreenWidth();
		int height = webBrowser.getScreenHeight();
		if(width >= 1366) {
			this.setWidth("1190px");
		} else {
			this.setWidth("940px");
		}

		if(height > 768) {
			this.setHeight("660px");
		} else if(height == 768) {
			this.setHeight("550px");
		} else {
			this.setHeight("500px");
		}
	}
	
	/**
	 * 显示呼入弹屏窗口
	 * @param customerResource		呼入的客户对象
	 * @param ringingExtenChannelSession	被叫分机振铃时的通道信息
	 */
	public void showIncomingDialWindow(CustomerResource customerResource, ChannelSession ringingExtenChannelSession) {
		// 主叫号码
		String callerNum = ringingExtenChannelSession.getConnectedlinenum();
		incomingDialTabView = new IncomingDialTabView(loginUser, tabSheet);
		incomingDialTabView.setRingingExtenChannelSession(ringingExtenChannelSession);		
		incomingDialTabView.echoInformations(customerResource);
		incomingDialTabView.setData(callerNum);
		
		// 根据是否要加密电话，来显示呼入号码
		String caption = isEncryptMobile ? telephoneService.encryptMobileNo(callerNum)+"呼入" : callerNum+"呼入";
		Tab tab = tabSheet.addTab(incomingDialTabView, caption, ResourceDataCsr.call_in_16_ico);
		tab.setClosable(true);
		callerNumToTabView.put(callerNum, incomingDialTabView);

		if(isInMainWindow == false) {
			isInMainWindow = true;
			Application app = ShareData.userToApp.get(loginUser.getId());
			app.getMainWindow().removeWindow(this);
			app.getMainWindow().addWindow(this);
		}
		
		// 自行以的加载方法
		incomingDialTabView.myAttach();

		// jrh  2013-12-17 弹屏就置忙队列成员      ---------------  开始
		ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(domainId);
		ispauseExtenPopupWindow = false;
		if(domainConfigs != null) {
			ispauseExtenPopupWindow = domainConfigs.get(PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW);
			if(ispauseExtenPopupWindow == null) {
				ispauseExtenPopupWindow = false;
			} else if(ispauseExtenPopupWindow) {
				iscreateCallAfterLog = domainConfigs.get(CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW);
				iscreateCallAfterLog = (iscreateCallAfterLog == null) ? true : iscreateCallAfterLog;
				
				CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(loginUser.getId());
				if(csrStatusBar != null) {
					csrStatusBar.executeInAfterCallHandle(iscreateCallAfterLog);
				}
			}
		}	//	------------- 结束
	}

	@Override
	public void windowClose(CloseEvent e) {
		isInMainWindow = false;
		callerNumToTabView.clear();
		tabSheet.removeAllComponents();

		// jrh  2013-12-17 关闭弹屏时将置闲队列成员
		if(ispauseExtenPopupWindow) {
			CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(loginUser.getId());
			if(csrStatusBar != null) {
				csrStatusBar.executeInAfterCallHandle(iscreateCallAfterLog);
			}
		}
	}

	/**
	 * 当Tab 页关闭时，对数据进行清理
	 */
	@Override
	public void onTabClose(TabSheet tabsheet, Component tabContent) {
		IncomingDialTabView incomingDialTabView = (IncomingDialTabView) tabContent;
		incomingDialTabView.clearComponentsValue();
		String callerNum = (String) incomingDialTabView.getData();
		callerNumToTabView.remove(callerNum);
		
		// 如果当前就只有一个Tab,则连同窗口都关闭
		int tabCount = tabsheet.getComponentCount();
		if(tabCount == 1) {
			this.getParent().removeWindow(this);
		} else {
			tabsheet.removeComponent(tabContent);
		}
	}
	
	public TabSheet getTabSheet() {
		return tabSheet;
	}

	public void setTabSheet(TabSheet tabSheet) {
		this.tabSheet = tabSheet;
	}

	/**
	 * 获取主叫方对应的呼叫弹屏窗口中的Tab 页
	 * @return
	 */
	public Map<String, IncomingDialTabView> getCallerNumToTabView() {
		return callerNumToTabView;
	}

	public void setCallerNumToTabView(Map<String, IncomingDialTabView> callerNumToTabView) {
		this.callerNumToTabView = callerNumToTabView;
	}

}
