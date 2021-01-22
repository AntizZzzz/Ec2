package com.jiangyifen.ec2.ui.mgr;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.LoginLayout;
import com.jiangyifen.ec2.ui.csr.toolbar.LoginUserInfoView;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutMonitor;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemInfo;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrTabSheet;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Manager的主窗口的 头部
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrHeader extends HorizontalLayout implements Button.ClickListener, CloseListener{
	
	//	右侧的Button组件
	private Button closeMultiTab_bt;				// 一键关闭所有Tab页
	private Button quit;
	private Label loginUserLabel;
	private User loginUser;							// 当前登陆用户
	
	// 用户个人信息编辑
	private Button currentUser;						// 登陆的当前用户
	private Button swithToCsrView_bt;				// 切换至坐席界面，如果当前用户拥有管理员角色
	
	private Window loginUserWindow;					// 登陆用户信息编辑子窗口
	private LoginUserInfoView loginUserInfoView;	// 当前登录用户信息显示界面
	private LoginLayout loginLayout;				// 登陆界面
	
	public MgrHeader(LoginLayout loginLayout) {
		this.setWidth("100%");
		this.setMargin(true, true, false, true);

		this.loginLayout = loginLayout;
		this.loginUser = SpringContextHolder.getLoginUser();
		
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setSpacing(true);
		this.addComponent(logoLayout);
		
		Embedded logoIcon = null;
		if("F0:1F:AF:D8:D8:62".equals(GlobalData.MAC_ADDRESS)) {	// 如果是中道的服务器，则使用中道自己的图标
			logoIcon = new Embedded(null, ResourceDataCsr.logo_32_zhongdao);
		} else {	// 否则使用共同的图标
			logoIcon = new Embedded(null, ResourceDataCsr.logo_ico);
		}
		logoLayout.addComponent(logoIcon);
		logoLayout.setComponentAlignment(logoIcon, Alignment.MIDDLE_LEFT);

		if(!"F0:1F:AF:D8:D8:62".equals(GlobalData.MAC_ADDRESS)) {	// 如果不是中道的服务器，则加上附加信息
			Label companyLabel = new Label("<font size = 4 color='blue'><B>&nbsp;"+SystemInfo.getSystemTitle()+"</B></font><br/>", Label.CONTENT_XHTML);
			logoLayout.addComponent(companyLabel);
			logoLayout.setComponentAlignment(companyLabel, Alignment.MIDDLE_LEFT);
		}
		
		HorizontalLayout rightLayout = new HorizontalLayout();
		rightLayout.setSpacing(true);
		this.addComponent(rightLayout);
		this.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
		
		User loginUser = SpringContextHolder.getLoginUser();
		loginUserLabel = new Label("<B>用户名：</B>"+loginUser.getUsername(), Label.CONTENT_XHTML);
		rightLayout.addComponent(loginUserLabel);
		rightLayout.setComponentAlignment(loginUserLabel, Alignment.MIDDLE_RIGHT);
		
		// 创建登陆用户按钮、及其信息显示子窗口
		HorizontalLayout userInfoLayout=createUserInfoComponent();
		rightLayout.addComponent(userInfoLayout);
		
		closeMultiTab_bt = new Button("一键关闭窗口", this);
		closeMultiTab_bt.setStyleName(BaseTheme.BUTTON_LINK);
		closeMultiTab_bt.setIcon(ResourceDataMgr.close_all_tab_24);
		rightLayout.addComponent(closeMultiTab_bt);
		
		quit = new Button("注 销", this);
		quit.setStyleName(BaseTheme.BUTTON_LINK);
		quit.setIcon(ResourceDataCsr.quit_ico);
		rightLayout.addComponent(quit);
	}

	/**
	 * 创建登陆用户按钮、即其信息显示子窗口
	 */
	private HorizontalLayout createUserInfoComponent() {
		HorizontalLayout userInfoLayout = new HorizontalLayout();
		currentUser = new Button("我的账户");
		currentUser.addListener(this);
		currentUser.setHtmlContentAllowed(true);
		currentUser.setIcon(ResourceDataCsr.user_24_ico);
		currentUser.setStyleName(BaseTheme.BUTTON_LINK);
		userInfoLayout.addComponent(currentUser);
		
		loginUserWindow = new Window("我的个人信息");
		loginUserWindow.setWidth("280px");
		loginUserWindow.setHeight("440px");
		loginUserWindow.setResizable(false);
		loginUserWindow.addListener(this);
		loginUserWindow.center();
		
		loginUserInfoView = new LoginUserInfoView();
		loginUserInfoView.setLoginUserWindow(loginUserWindow);
		loginUserWindow.setContent(loginUserInfoView);

		// 检验当前用户是否持有管理员角色
		boolean isCsr = false;
		for(Role role : loginUser.getRoles()) {
			if(role.getType().getIndex() == RoleType.csr.getIndex()) {
				isCsr = true;
				break;
			}
		}

		if(isCsr) {	// 如果持有管理员角色，就添加切换按钮
			swithToCsrView_bt = new Button("转至坐席界面", this);
			swithToCsrView_bt.setIcon(ResourceDataCsr.goto_24_ico);
			swithToCsrView_bt.setStyleName(BaseTheme.BUTTON_LINK);
			userInfoLayout.addComponent(swithToCsrView_bt);
		}
		
		return userInfoLayout;
	}
	
	/**
	 * 添加子窗口，先删除子窗口，在添加它
	 * @param subWindow 需要添加的子窗口
	 */
	private void handleSubWindow(Window subWindow) {
		this.getApplication().getMainWindow().removeWindow(subWindow);
		this.getApplication().getMainWindow().addWindow(subWindow);
	}
	
	/**
	 *监听按钮单击事件 
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton() == quit){
			// 退出时清理内存
			logoutClearCache();
			
			// 清理页面存储在内存中的数据
			this.getWindow().getApplication().close();
		}else if(event.getButton() == currentUser) {
			handleSubWindow(loginUserWindow);
		}else if(event.getButton() == swithToCsrView_bt) {	// jrh 切换至坐席界面
			// 退出时清理内存
			logoutClearCache();
			
			// 切换至坐席界面
			loginLayout.switchAccountView(RoleType.csr);
		}else if(event.getButton() == closeMultiTab_bt) {	// jrh 一键关闭窗口
			/*************************************************************/
			/**         存在隐患，如果同一个管理员在不同的电脑上同时登录，将会出现问题                   **/ 
			/**    现象：先登录系统的人，点关闭按钮时，关闭的将是后登录的管理员的界面的Tab页         **/ 
			/**    原因：内存中mgrToTabSheet<userId, MgrTabSheet>被替换导致            **/ 
			/*************************************************************/
			MgrTabSheet tabSheet = ShareData.mgrToTabSheet.get(loginUser.getId());
			if(tabSheet != null) {
				tabSheet.removeAllComponents();
			}
		}
	}

	/**
	 * 退出系统或切换至坐席界面时，清理内存
	 */
	private void logoutClearCache() {
		/***********************管理员处理*************************/
		//如果超时的是管理员，则对管理员的自动外呼组件进行移除
		ShareData.mgrToAutoDialout.remove(loginUser.getId());

		AutoDialoutMonitor autodialMonitor=ShareData.mgrToAutoDialoutMonitor.get(loginUser.getId());
		if(autodialMonitor!=null){
			autodialMonitor.stopThread();
		}
		ShareData.mgrToAutoDialoutMonitor.remove(loginUser.getId());
		
		// 停止监控线程
		/*************************************************************/
		/**         存在隐患，如果同一个管理员在不同的电脑上同时登录，将会出现问题                   **/ 
		/**    现象：先登录系统的人，点关闭按钮时，关闭的将是后登录的管理员的界面的Tab页         **/ 
		/**    原因：内存中mgrToTabSheet<userId, MgrTabSheet>被替换导致            **/ 
		/*************************************************************/
		MgrTabSheet tabSheet = ShareData.mgrToTabSheet.get(loginUser.getId());
		if(tabSheet != null) {
			tabSheet.stopSupperviceThread(null, "onTabChange");
		}
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		Window window = e.getWindow();
		if(window == loginUserWindow) {
			Button cancel = loginUserInfoView.getCancel();
			if(cancel != null) {
				cancel.click();
			}
		}
	}
}
