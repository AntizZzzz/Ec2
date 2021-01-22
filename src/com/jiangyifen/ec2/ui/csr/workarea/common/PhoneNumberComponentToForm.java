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
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 只用于显示号码，不能直接点击拨打电话（如管理员界面使用）
 * 	电话号码显示组件 ，可以用于存放到Form 表单中
 * CustomerBaseInfoView 使用
 * @author jrh
 */
@SuppressWarnings("serial")
public class PhoneNumberComponentToForm extends CustomField {
	// 号码加密权限
	private final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	// 坐席:发短信的权限
	private static final String CSR_SMS_MANAGEMENT_MESSAGE_SEND_MANAGE = "csr_sms_management&message_send_manage";
	// 管理员：发短信的权限
	private final String MGR_PHONE_MESSAGE_MANAGEMENT_SEND_MANAGEMENT = "phone_message_management&send_management";

	private Button sendMessage;									// 发送短信按钮
	private SendSingleMessageWindow sendSingleMessageWindow;	// 发短信的窗口
	
	private boolean isEncryptMobile;							// 电话号码是否需要加密
	private ArrayList<String> ownBusinessModels;				// 当前登陆用户目前使用的角色类型下，所拥有的权限
	private CustomerResource customer;							// 当前在操作的客户对象
	private Set<Telephone> telephones; 							// 电话Set集合
	private User loginUser;										// 当前登陆用户
	private RoleType roleType;									// 调用该模块的用户使用的角色类型
	
	private TelephoneService telephoneService;
	
	/**
	 * 创建给单个电话发送短信的界面【该界面可以让话务员管理员角色通用】
	 * @param loginUser			当前登陆用户
	 * @param customer			当前查看的客户对象
	 * @param ownBusinessModels	当前登陆用户目前使用的角色类型下，所拥有的权限
	 */
	public PhoneNumberComponentToForm(User loginUser, RoleType roleType, CustomerResource customer, ArrayList<String> ownBusinessModels) {
		this.loginUser = loginUser;
		this.roleType = roleType;
		this.customer = customer;
		this.ownBusinessModels = ownBusinessModels;
		
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		telephones = customer.getTelephones();

		telephoneService = SpringContextHolder.getBean("telephoneService");

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		setCompositionRoot(mainLayout);
		
		// 创建主要的组件
		HorizontalLayout phoneCallLayout = new HorizontalLayout();
		phoneCallLayout.setSpacing(true);
		mainLayout.addComponent(phoneCallLayout);
		
		// 根据电话号码的个数创建呼叫组件
		if(telephones != null && telephones.size() == 1) {
			createDialButton(phoneCallLayout);
		} if(telephones != null && telephones.size() > 1) {
			createDialMenu(phoneCallLayout);
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
	 * 如果客户只有一个电话号码，则创建一个Label
	 */
	private void createDialButton(HorizontalLayout layout) {
		Label numberLabel = new Label();
		numberLabel.setWidth("-1px");
		layout.addComponent(numberLabel);

		for(Telephone telephone : telephones) {	// 只会循环一次
			String telephoneNum = telephone.getNumber();
			String caption = isEncryptMobile ? telephoneService.encryptMobileNo(telephoneNum) : telephoneNum;
			numberLabel.setCaption(caption);
		}
	}

	/**
	 * 如果客户有多个电话，则生成号码显示菜单
	 */
	private void createDialMenu(HorizontalLayout layout) {
		MenuBar menubar = new MenuBar();
		menubar.addStyleName("nobackground");
		menubar.setAutoOpen(true);
		layout.addComponent(menubar);

		MenuItem dialAction = menubar.addItem("", null, null);
		dialAction.setStyleName("mypadding");
		
		int index = 0;
		for(Telephone telephone : telephones) {
			String telephoneNum = telephone.getNumber();
			// 如果是电话号码需要加密，那么则将呼叫的号码存入菜单的样式中，不然存入菜单的Text 中
			String encryptMobile = isEncryptMobile ? telephoneService.encryptMobileNo(telephoneNum) : telephoneNum;
			if(index == 0) {
				dialAction.setText(encryptMobile);
				index++;
			}
			dialAction.addItem(encryptMobile, null, null);
		}
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
				sendSingleMessageWindow.updateWindowComponents(customer);
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
	
	@Override
	public Set<Telephone> getValue() {
		return telephones;
	}
	
	@Override
	public Class<?> getType() {
		return Telephone.class;
	}
}
