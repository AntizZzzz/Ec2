package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vaadin.addon.customfield.CustomField;

import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.csr.workarea.marketingtask.MyMarketingTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.questionnairetask.QuestionnaireTaskTabView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 	呼叫组件，可以用于存放到表格中
 * @author jrh
 */
@SuppressWarnings("serial")
public class DialComponentToTable extends CustomField {
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private Table sourceTable;			// 发起呼叫的表格
	private Object selectedItemId;		// 表格中的选中行
	private Set<Telephone> telephones; 	// 电话Set集合
	
	private String exten;				// 当前用户所使用的分机
	private boolean isEncryptMobile = true;	// 电话号码默认加密
	
	private DialService dialService;			// 拨打电话的服务类
	private TelephoneService telephoneService;	// 电话号码服务类
	private VerticalLayout sourceTableView;
	
	public DialComponentToTable(Table sourceTable, Object selectedItemId, 
			Set<Telephone> telephones, User loginUser, ArrayList<String> ownBusinessModels, VerticalLayout sourceTableView) {
		this.sourceTable = sourceTable;
		this.selectedItemId = selectedItemId;
		this.telephones = telephones;
		this.sourceTableView = sourceTableView;
		
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		exten = ShareData.userToExten.get(loginUser.getId());
		dialService = SpringContextHolder.getBean("dialService");
		telephoneService = SpringContextHolder.getBean("telephoneService");

		// 创建主要的组件
		HorizontalLayout phoneCallLayout = new HorizontalLayout();
		setCompositionRoot(phoneCallLayout);
		
		List<String> localNums = new ArrayList<String>();
		List<String> remoteNums = new ArrayList<String>();
		for(Telephone telephone : telephones) {
			String localNum = telephone.getNumber();
			while(localNum.startsWith("0")) {
				localNum = localNum.substring(1);
			}
			localNums.add(localNum);
			
			String remoteNum = telephone.getNumber();
			if(!remoteNum.startsWith("0")) {
				remoteNum = "0" + remoteNum;
			} 
			remoteNums.add(remoteNum);
		}
		
		if(telephones.size() == 1) {
			phoneCallLayout.addComponent( createDialButton(localNums.get(0), loginUser.getId()) );
			phoneCallLayout.addComponent( createDialButton(remoteNums.get(0), loginUser.getId()) );
		} else if(telephones.size() >= 1) {
			phoneCallLayout.addComponent( createDialMenu(localNums, loginUser.getId()) );
			phoneCallLayout.addComponent( createDialMenu(remoteNums, loginUser.getId()) );
		}
	}

	
	/**
	 * 如果客户只有一个电话号码，则创建一个呼叫按钮
	 */
	private Button createDialButton(final String connectedNum, final Long userId) {
		Button dialButton = new Button();
		dialButton.setWidth("95px");
		dialButton.setStyleName("borderless");
		dialButton.setStyleName(BaseTheme.BUTTON_LINK);
		dialButton.setIcon(ResourceDataCsr.dial_12_ico);
			
		String caption = isEncryptMobile ? telephoneService.encryptMobileNo(connectedNum) : connectedNum;
		dialButton.setCaption(caption);
		
		dialButton.addListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				VerticalLayout currentTab = ShareData.csrToCurrentTab.get(userId);
				if(currentTab.getClass() != sourceTableView.getClass()) {
					try {
						Method method = sourceTableView.getClass().getMethod("changeCurrentTab");
						method.invoke(sourceTableView);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
				sourceTable.select(selectedItemId);
				
				try {	// JRH 2014-06-06 解决弹屏无法获取当前任务，导致空指针异常
					if(sourceTableView.getClass() == MyMarketingTaskTabView.class || 
							sourceTableView.getClass() == QuestionnaireTaskTabView.class) {
						Method method = sourceTableView.getClass().getMethod("echoPopupWindowInfo");
						method.invoke(sourceTableView);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
				dialService.dial(exten, connectedNum);
			}
		});
		
		return dialButton;
	}

	/**
	 * 如果客户有多个电话，则生成呼叫菜单
	 */
	private MenuBar createDialMenu(List<String> connectedNums, final Long userId) {
		MenuBar menubar = new MenuBar();
		menubar.setWidth("100px");
		menubar.addStyleName("mybackground");
		
		String caption = isEncryptMobile ? telephoneService.encryptMobileNo(connectedNums.get(0)) : connectedNums.get(0);
		MenuItem dialAction = menubar.addItem(caption, ResourceDataCsr.dial_12_ico, null);
		dialAction.setStyleName("mypadding");
		
		Command dialCommand = new Command() {
			public void menuSelected(MenuItem selectedItem) {
				VerticalLayout currentTab = ShareData.csrToCurrentTab.get(userId);
				if(currentTab.getClass() != sourceTableView.getClass()) {
					try {
						Method method = sourceTableView.getClass().getMethod("changeCurrentTab");
						method.invoke(sourceTableView);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
// TODO 这样其实并不好
				// 如果是电话号码需要加密，那么则选择菜单的样式名称进行呼叫，不然直接取菜单的Text 进行呼叫
				String connectedLineNum = isEncryptMobile ? selectedItem.getStyleName() : selectedItem.getText();
				sourceTable.select(selectedItemId);
				
				try {	// JRH 2014-06-06 解决弹屏无法获取当前任务，导致空指针异常
					if(sourceTableView.getClass() == MyMarketingTaskTabView.class || 
							sourceTableView.getClass() == QuestionnaireTaskTabView.class) {
						Method method = sourceTableView.getClass().getMethod("echoPopupWindowInfo");
						method.invoke(sourceTableView);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
				dialService.dial(exten, connectedLineNum);
			}
		};
		
		for(String connectedNum : connectedNums) {
			// 如果是电话号码需要加密，那么则将呼叫的号码存入菜单的样式中，不然存入菜单的Text 中
			String encryptMobile = isEncryptMobile ? telephoneService.encryptMobileNo(connectedNum) : connectedNum;
			MenuItem menuItem = dialAction.addItem(encryptMobile, ResourceDataCsr.dial_12_ico, dialCommand);
			menuItem.setStyleName(connectedNum);
		}
		
		return menubar;
	}
	
	@Override
	public Set<Telephone> getValue() {
		return telephones;
	}
	
	@Override
	public Class<?> getType() {
		return Telephone.class;
	}
	
}
