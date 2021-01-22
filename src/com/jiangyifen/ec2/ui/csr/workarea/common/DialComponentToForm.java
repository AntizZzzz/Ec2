package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.util.ArrayList;
import java.util.Set;

import org.vaadin.addon.customfield.CustomField;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MobileLoc;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.csr.workarea.sms.SendSingleMessageWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 	呼叫组件，可以用于存放到Form 表单中（话务员的表单使用）
 * CustomerBaseInfoView 使用
 * @author jrh
 */
@SuppressWarnings("serial")
public class DialComponentToForm extends CustomField {

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	// 坐席:发短信的权限
	private static final String CSR_SMS_MANAGEMENT_MESSAGE_SEND_MANAGE = "csr_sms_management&message_send_manage";
	// 管理员：发短信的权限
	private final String MGR_PHONE_MESSAGE_MANAGEMENT_SEND_MANAGEMENT = "phone_message_management&send_management";
	
	private VerticalLayout mainLayout;				// 主布局管理器
	private Notification notification;				// 提示信息

	private Button addMore;							// 添加更多联系方式的按钮
	private AddPhoneWindow addPhoneWindow;			// 添加联系方式的子窗口

	private Button sendMessage;						// 发送短信按钮
	private SendSingleMessageWindow sendSingleMessageWindow;	// 发短信的窗口
	
	private CustomerBaseInfoView customerBaseInfoView;			// 客户详细信息显示表
	
	private String exten;							// 当前用户所使用的分机
	private User loginUser;							// 当前登陆用户
	private RoleType roleType;						// 调用该模块的用户使用的角色类型
	private boolean isEncryptMobile = true;			// 电话号码是否需要加密
	private ArrayList<String> ownBusinessModels;	// 当前登陆用户目前使用的角色类型下，所拥有的权限
	private Set<Telephone> telephones; 				// 电话Set集合
	private CustomerResource customerResource;		// 客户资源对象
	
	private DialService dialService;				// 拨打电话的服务类
	private TelephoneService telephoneService;		// 电话服务类
	
	/**
	 * @param telephones			电话机和
	 * @param hasEditAuthority		是否有编辑客户信息的权限
	 * @param loginUser				调用该类的用户对象
	 * @param roleType				调用该模块的用户所使用的角色类型
	 * @param ownBusinessModels		当前登陆用户目前使用的角色类型下，所拥有的权限
	 * @param customerBaseInfoView	客户基本信息显示界面
	 */
	public DialComponentToForm(Set<Telephone> telephones, boolean hasEditAuthority, 
			User loginUser, RoleType roleType, ArrayList<String> ownBusinessModels, CustomerBaseInfoView customerBaseInfoView) {
		this.telephones = telephones;
		this.loginUser = loginUser;
		this.roleType = roleType;
		this.ownBusinessModels = ownBusinessModels;
		this.customerBaseInfoView = customerBaseInfoView;

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		exten = ShareData.userToExten.get(loginUser.getId());
		dialService = SpringContextHolder.getBean("dialService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		
		// 创建主要的组件
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		setCompositionRoot(mainLayout);
		
		HorizontalLayout phoneCallLayout = new HorizontalLayout();
		phoneCallLayout.setSpacing(true);
		mainLayout.addComponent(phoneCallLayout);
		
		// 根据电话号码的个数创建呼叫组件
		if(telephones != null && telephones.size() == 1) {
			createDialButton(phoneCallLayout, "localDial");
			
			Label placeholder = new Label("&nbsp", Label.CONTENT_XHTML);
			placeholder.setWidth("-1px");
			phoneCallLayout.addComponent(placeholder);
			
			createDialButton(phoneCallLayout, "remoteDial");
			// 看客户有没有修改客户信息的权限，如果有，则创建添加号码按钮
			if(hasEditAuthority) {
				createAddPhoneComponent(phoneCallLayout);
			}
		} else if(telephones != null && telephones.size() > 1) {
			createDialMenu(phoneCallLayout, "localDial");
			
			Label placeholder = new Label("&nbsp", Label.CONTENT_XHTML);
			placeholder.setWidth("-1px");
			phoneCallLayout.addComponent(placeholder);
			
			createDialMenu(phoneCallLayout, "remoteDial");
			if(hasEditAuthority) {
				createAddPhoneComponent(phoneCallLayout);
			}
		}

		// 号码下方再添加一个显示行【发送短信按钮、显示号码归属地标签】
		HorizontalLayout tp_lo = new HorizontalLayout();
		tp_lo.setSpacing(true);
		mainLayout.addComponent(tp_lo);
		
		// TODO add 2013-06-28 创建发送短信组件
		if((roleType.equals(RoleType.csr) && ownBusinessModels.contains(CSR_SMS_MANAGEMENT_MESSAGE_SEND_MANAGE)) 
				|| (roleType.equals(RoleType.manager) && ownBusinessModels.contains(MGR_PHONE_MESSAGE_MANAGEMENT_SEND_MANAGEMENT))) {
			createSendMessageComponents(tp_lo);
		}

		// 显示号码归属地
		if(telephones != null) {
			Label mobileLoc_lb = createMobileLoc(telephones);
			tp_lo.addComponent(mobileLoc_lb);
			tp_lo.setComponentAlignment(mobileLoc_lb, Alignment.BOTTOM_LEFT);
		}
	}

	/**
	 * 如果客户只有一个电话号码，则创建一个呼叫按钮
	 */
	private void createDialButton(HorizontalLayout layout, final String dialType) {
		Button dialButton = new Button();
		dialButton.setStyleName("borderless");
		dialButton.setStyleName(BaseTheme.BUTTON_LINK);
		dialButton.setIcon(ResourceDataCsr.dial_12_ico);
		layout.addComponent(dialButton);
	
		for(Telephone telephone : telephones) {	// 只会循环一次
			String telephoneNum = telephone.getNumber();
			if("localDial".equals(dialType)) {	// 本地呼叫
				while(telephoneNum.startsWith("0")) {
					telephoneNum = telephoneNum.substring(1);
				}
			} else {							// 长途呼叫
				if(!telephoneNum.startsWith("0")) {
					telephoneNum = "0" + telephoneNum;
				}
			}
			
			String caption = isEncryptMobile ? telephoneService.encryptMobileNo(telephoneNum) : telephoneNum;
			dialButton.setCaption(caption);
		}
		
		dialButton.addListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				for(Telephone telephone : telephones) {	// 只会循环一次
					String connectedNum = telephone.getNumber();
					if("localDial".equals(dialType)) {	// 本地呼叫
						while(connectedNum.startsWith("0")) {
							connectedNum = connectedNum.substring(1);
						}
						dialService.dial(exten, connectedNum);
					} else {							// 长途呼叫
						if(!connectedNum.startsWith("0")) {
							connectedNum = "0" + connectedNum;
						}
						dialService.dial(exten, connectedNum);
					}
				}
			}
		});
	}

	/**
	 * 如果客户有多个电话，则生成呼叫菜单
	 */
	private void createDialMenu(HorizontalLayout layout, final String dialType) {
		MenuBar menubar = new MenuBar();
		menubar.addStyleName("nobackground");
		menubar.setAutoOpen(true);
		layout.addComponent(menubar);
		
		String caption = "localDial".equals(dialType) ? "本地呼叫" : "长途呼叫";
		MenuItem dialAction = menubar.addItem(caption, ResourceDataCsr.dial_12_ico, null);
		dialAction.setStyleName("mypadding");
		
		Command dialCommand = new Command() {
			public void menuSelected(MenuItem selectedItem) {
// TODO 这样其实很不好，以后要是有好的方案，再改
				// 如果是电话号码需要加密，那么则选择菜单的样式名称进行呼叫，不然直接取菜单的Text 进行呼叫
				String connectedLineNum = isEncryptMobile ? selectedItem.getStyleName() : selectedItem.getText();
				dialService.dial(exten, connectedLineNum);
			}
		};
			
		if("localDial".equals(dialType)) {		// 本地呼叫
			for(Telephone telephone : telephones) {
				String connectedNum = telephone.getNumber();
				while(connectedNum.startsWith("0")) {
					connectedNum = connectedNum.substring(1);
				}
				// 如果是电话号码需要加密，那么则将呼叫的号码存入菜单的样式中，不然存入菜单的Text 中
				String encryptMobile = isEncryptMobile ? telephoneService.encryptMobileNo(connectedNum) : connectedNum;
				MenuItem menuItem = dialAction.addItem(encryptMobile, ResourceDataCsr.dial_12_ico, dialCommand);
				menuItem.setStyleName(connectedNum);
			}
		} else if("remoteDial".equals(dialType)) {	// 长途呼叫
			for(Telephone telephone : telephones) {
				String connectedNum = telephone.getNumber();
				if(!connectedNum.startsWith("0")) {
					connectedNum = "0" + connectedNum;
				} 
				// 如果是电话号码需要加密，那么则将呼叫的号码存入菜单的样式中，不然存入菜单的Text 中
				String encryptMobile = isEncryptMobile ? telephoneService.encryptMobileNo(connectedNum) : connectedNum;
				MenuItem menuItem = dialAction.addItem(encryptMobile, ResourceDataCsr.dial_12_ico, dialCommand);
				menuItem.setStyleName(connectedNum);
			}
		}
	}

	/**
	 * 
	 * @param phoneCallLayout
	 */
	private void createAddPhoneComponent(final HorizontalLayout phoneCallLayout) {
		addMore = new Button("添加");
		addMore.addStyleName(BaseTheme.BUTTON_LINK);
		addMore.addStyleName("borderless");
		addMore.setIcon(ResourceDataCsr.add_16_ico);
		phoneCallLayout.addComponent(addMore);
		
		addMore.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				addPhoneWindow = new AddPhoneWindow(customerResource, DialComponentToForm.this);
				addMore.getApplication().getMainWindow().addWindow(addPhoneWindow);
			}
		});
	}

	/**
	 * 创建发送短信组件
	 * @param phoneCallLayout
	 */
	private void createSendMessageComponents(HorizontalLayout phoneCallLayout) {
		sendMessage = new Button("短信");
		sendMessage.addStyleName(BaseTheme.BUTTON_LINK);
		sendMessage.addStyleName("borderless");
		sendMessage.setIcon(ResourceDataCsr.phone_message_send_16_ico);
		phoneCallLayout.addComponent(sendMessage);
		
		sendMessage.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if(sendSingleMessageWindow == null) {
					sendSingleMessageWindow = new SendSingleMessageWindow(loginUser, roleType, ownBusinessModels);
				}
				sendMessage.getApplication().getMainWindow().removeWindow(sendSingleMessageWindow);
				sendMessage.getApplication().getMainWindow().addWindow(sendSingleMessageWindow);
				sendSingleMessageWindow.updateWindowComponents(customerResource);
			}
		});
	}

	/**
	 * 创建显示电话号码归属地的组件
	 * @param telephones
	 * @return
	 */
	private Label createMobileLoc(Set<Telephone> telephones) {
		String mobileLocStr = "号码归属地：未知";
		for(Telephone phone : telephones) {	// 根据电话号码查归属地
			MobileLoc mobileLoc = MobileLocUtil.getMobileAreaCode(phone.getNumber());
			if(mobileLoc != null) {
				mobileLocStr = "号码归属地："+mobileLoc.getMobileArea();
				break;
			}
		}
		
		Label mobileLoc_lb = new Label(mobileLocStr);
		mobileLoc_lb.setWidth("-1px");
		
		return mobileLoc_lb;
	}
	
	/**
	 * 在添加号码后，更新Form 表单的信息
	 * @param customerResource
	 * @param existedCustomer   	如果这个客户之前不存在，则添加号码后将Form 设置成编辑状态
	 */
	public void updateFormPhoneField(CustomerResource customerResource, boolean existedCustomer) {
		customerBaseInfoView.echoCustomerBaseInfo(customerResource);
		customerBaseInfoView.getCustomerBaseInfoEditorForm().setReadOnly(existedCustomer);
	}
	
	/**
	 * 设置资源信息 
	 * @param customerResource
	 */
	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
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
