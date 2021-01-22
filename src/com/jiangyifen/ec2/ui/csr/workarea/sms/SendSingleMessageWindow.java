package com.jiangyifen.ec2.ui.csr.workarea.sms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.sms.SmsUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 只能给一个客户发短信的短信界面   (该类，基本能满足各种只给一个客户或一个手机号发短信的场景)
 * 	此处可以给改窗口传递一个客户对象，或者一个指定的手机号码，进行发送短信
 * 	如果客户有多个手机号码，则发给每个手机都发一条短信
 *  
 *  如果客户有多个号码，但其中存在固话，或者都是固话，则判断，并且不给固话发
 *  当前简单判断方法，电话号去掉前面的0，然后判断是以1 开头,长度在{5,11}位的号码
 *  
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class SendSingleMessageWindow extends Window implements ClickListener, ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息
	
	// 当传递过来的是一个指定的客户对象时，sendType_og 组件才可以看见
	private ComboBox template_cb;									// 短信模板选择框
	private TextField phoneNo1_tf;									// 收信人电话
	private TextArea content_ta;									// 短信内容

	private Button send_bt;											// 发送按钮
	private Button cancel_bt;										// 取消按钮

	private User loginUser;											// 当前登陆用户
	private String exten;											// 当前登陆用户使用的分机
	private Domain domain;											// 当前登陆用户所属域
	private RoleType roleType;										// 调用该模块的用户使用的角色类型
	private boolean isEncryptMobile = true;							// 电话号码是否需要加密
	private MessageTemplate nearestSelectedTemplate;				// 最近选中的模板
	private BeanItemContainer<MessageTemplate> templateContainer;	// 存放短信模板的容器
	
	private HashMap<String, String> phoneNosMap;					// 加密后的号码与原始号码对应项HashMap<加密号码，未加密号码>

	private MessageTemplateService messageTemplateService;			// 模板短信服务类
	private TelephoneService telephoneService;						// 电话号码服务类
	
	/**
	 * 创建给单个电话发送短信的界面【该界面可以让话务员管理员角色通用】
	 * @param loginUser			当前登陆用户
	 * @param roleType			调用该模块的用户使用的角色类型
	 * @param ownBusinessModels	当前登陆用户目前使用的角色类型下，所拥有的权限
	 */
	public SendSingleMessageWindow(User loginUser, RoleType roleType, ArrayList<String> ownBusinessModels) {
		this.center();
		this.setModal(true);
		this.setImmediate(true);
		this.setResizable(false);
		this.setCaption("发送短信");
		this.loginUser = loginUser;
		this.roleType = roleType;
		
		domain = loginUser.getDomain();
		exten = ShareData.userToExten.get(loginUser.getId());

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		phoneNosMap = new HashMap<String, String>();
		
		templateContainer = new BeanItemContainer<MessageTemplate>(MessageTemplate.class);
		
		messageTemplateService = SpringContextHolder.getBean("messageTemplateService");
		telephoneService = SpringContextHolder.getBean("telephoneService");

		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		// 创建主要Field 等相关组件（用于填写信息）
		createFieldComponents(windowContent);
		
		// 创建操作按钮组件
		createOperatorButtons(windowContent);
	}
	
	/**
	 * 创建主要Field 等相关组件（用于填写信息）
	 * @param windowContent
	 */
	private void createFieldComponents(VerticalLayout windowContent) {
		// 短信模板
		HorizontalLayout template_l = new HorizontalLayout();
		template_l.setSpacing(true);
		windowContent.addComponent(template_l);
		
		Label templateCaption = new Label("使用模板：");
		templateCaption.setWidth("-1px");
		template_l.addComponent(templateCaption);
		
		template_cb = new ComboBox();
		template_cb.setWidth("350px");
		template_cb.setImmediate(true);
		template_cb.addListener(this);
		template_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		template_cb.setItemCaptionPropertyId("title");
		template_cb.setContainerDataSource(templateContainer);
		template_l.addComponent(template_cb);
		
		// 接收电话号码集合
		Label phoneNoCaption1_l = new Label("接收电话：");
		phoneNoCaption1_l.setWidth("-1px");

		HorizontalLayout phoneNo_l1 = new HorizontalLayout();
		phoneNo_l1.setSpacing(true);
		phoneNo_l1.addComponent(phoneNoCaption1_l);
		phoneNo_l1.setComponentAlignment(phoneNoCaption1_l, Alignment.MIDDLE_LEFT);
		windowContent.addComponent(phoneNo_l1);
		
		phoneNo1_tf = new TextField();
		phoneNo1_tf.setRequired(true);
		phoneNo1_tf.setNullRepresentation("");
		phoneNo1_tf.setWidth("350px");
		phoneNo_l1.addComponent(phoneNo1_tf);
		
		HorizontalLayout content_l = new HorizontalLayout();
		content_l.setSpacing(true);
		windowContent.addComponent(content_l);
		
		Label contentCaption_l = new Label("短信内容：");
		contentCaption_l.setWidth("-1px");
		content_l.addComponent(contentCaption_l);
		content_l.setComponentAlignment(contentCaption_l, Alignment.MIDDLE_LEFT);
		
		content_ta = new TextArea();
		content_ta.setRows(3);
		content_ta.setRequired(true);
		content_ta.setDescription("<B>短信内容不能为空!</B>");
		content_ta.setNullRepresentation("");
		content_ta.setWidth("350px");
		content_l.addComponent(content_ta);
	}

	/**
	 * 创建操作按钮组件
	 * @param windowContent
	 */
	private void createOperatorButtons(VerticalLayout windowContent) {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
		windowContent.addComponent(operator_l);

		send_bt = new Button("发送", this);
		send_bt.setStyleName("default");
		operator_l.addComponent(send_bt);
		
		cancel_bt = new Button("取消", this);
		operator_l.addComponent(cancel_bt);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == template_cb) {
			nearestSelectedTemplate = (MessageTemplate) template_cb.getValue();
			if(nearestSelectedTemplate == null) {
				content_ta.setValue("");
			} else {
				content_ta.setValue(nearestSelectedTemplate.getContent());
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == send_bt) {
			try {
				executeSend();
			} catch (Exception e) {
				logger.error("jrh 话务员执行发送短信，出现异常 -----> "+e.getMessage(), e);
				warning_notification.setCaption("短信发送失败！！！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
			}
		} else if(source == cancel_bt) {
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 执行发送短信
	 */
	private void executeSend() {
		String content = StringUtils.trimToEmpty((String) content_ta.getValue()).trim();
		if("".equals(content)) {
			warning_notification.setCaption("短信内容不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		List<String> reciverPhoneNos = new ArrayList<String>();
		String phoneNosStr = StringUtils.trimToEmpty((String)phoneNo1_tf.getValue());
		
		// 将用户输入的手机号按中英文逗号切分处理
		ArrayList<String> numbers = new ArrayList<String>();
		for(String number1 : phoneNosStr.split(",")) {	// 按英文逗号切割
			if(number1.contains("，")) {
				for(String number2 : number1.split("，")) {	// 按中文逗号切割
					numbers.add(number2);
				}
			} else {
				numbers.add(number1);
			}
		}
		
		// 切分后，判断号码是否合法，取出合法的电话号码
		for(String phoneNo : numbers) {
			// 如果现在是加密显示号码，则判断当前值是否在加密号码集合中存在
			if(isEncryptMobile) {
				if(phoneNosMap.keySet().contains(phoneNo)) {
					phoneNo = phoneNosMap.get(phoneNo);
				} 
			} 
			
			// 去掉首位的 0
			if(phoneNo.startsWith("0")) {
				phoneNo = phoneNo.substring(1);
			}
			
			// 检验号码是否不是正确的手机号，不是，则继续
			if(!phoneNo.startsWith("1") || !phoneNo.matches("\\d{5,11}")) {
				continue;
			}
			// 检查是否已经包含在内了，不包含，则加入
			if(!reciverPhoneNos.contains(phoneNo)) {
				reciverPhoneNos.add(phoneNo);
			}
		}
		
		if(reciverPhoneNos.size() == 0) {
			warning_notification.setCaption("您没有输入正确的电话号码，请核查后重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}

		// 发送短信
		String result = SmsUtil.sendSMS(loginUser, exten, domain.getId(), reciverPhoneNos, content);
		if(!result.contains("成功")) {
			warning_notification.setCaption("短信发送失败！！！原因："+result);
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}

		success_notification.setCaption("短信发送成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
		this.getApplication().getMainWindow().removeWindow(this);
	}
	
	/**
	 * 更新短信模板的容器
	 */
	private void updateTempates() {
		templateContainer.removeAllItems();
		List<MessageTemplate> templates = new ArrayList<MessageTemplate>();
		// 话务员只能使用自己建的模板，管理员可以是用所有模板
		if(roleType.equals(RoleType.csr)) {
			templates = messageTemplateService.getMessagesByCreator(loginUser);
			templates.addAll(messageTemplateService.getAllByType(MessageTemplateType.system, domain.getId()));
		} else {
			templates = messageTemplateService.getAllByDomain(domain);
		}
		templateContainer.addAll(templates);
//		TODO 目前实现有点麻烦，以后再做
//		// 回显信息
//		if(nearestSelectedTemplate != null) {
//			for(MessageTemplate template : templates) {
//				if(nearestSelectedTemplate.getId().equals(template.getId())) {
//					template_cb.setValue(template); 
//					break;
//				}
//			}
//		}
	}
	
	/**
	 * 传入一个'指定的客户对象'，更新发送短信的窗口
	 * 	使用这种刷新方法的时机：希望使用'客户的手机号'作为接收短息的号码时调用
	 * @param customerResource
	 */
	public void updateWindowComponents(CustomerResource customerResource) {
		phoneNosMap.clear();
		StringBuffer phoneNosSB = new StringBuffer();
		for(Telephone telephone : customerResource.getTelephones()) {
			String originalNum = telephone.getNumber();
			if(originalNum.startsWith("0")) {			// 去掉首位的 0
				originalNum = originalNum.substring(1);
			}
			if(!originalNum.startsWith("1") || originalNum.length() > 12) {			// 检验号码是否不是正确的手机号，不是，则继续
				continue;
			}

			if(isEncryptMobile) {	// 判断电话号码是否要加密
				String encrypted = telephoneService.encryptMobileNo(originalNum);
				if(phoneNosMap.keySet().contains(encrypted)) {	// 检查是否已经加入相同的号码
					continue;
				}
				phoneNosMap.put(encrypted, originalNum);
				phoneNosSB.append(encrypted);
			} else {
				if(phoneNosMap.keySet().contains(originalNum)) {	// 这种情况基本不会发生
					continue;
				}
				phoneNosMap.put(originalNum, originalNum);
				phoneNosSB.append(originalNum);
			}
			phoneNosSB.append(",");
		}
		
		// 设置初始值
		if(phoneNosMap.size() > 0) {
			String phoneNosStr = phoneNosSB.toString();
			phoneNosStr = phoneNosStr.substring(0, phoneNosStr.length()-1);
			phoneNo1_tf.setValue(phoneNosStr);
		} else {
			phoneNo1_tf.setValue("");
		}
		content_ta.setValue("");
		// 更新短信模板的容器
		updateTempates();
	}
	
	/**
	 * 传入一个'指定的电话号码'，更新发送短信的窗口
	 * 	使用这种刷新方法的时机：希望使用一个'指定的手机号'作为接收短息的号码时调用
	 * @param specifiedPhoneNo 指定的手机号
	 */
	public void updateWindowComponents(String specifiedPhoneNo) {
		phoneNosMap.clear();
		if(isEncryptMobile) {	// 判断电话号码是否要加密
			String encrypted = telephoneService.encryptMobileNo(specifiedPhoneNo);
			phoneNosMap.put(encrypted, specifiedPhoneNo);
			phoneNo1_tf.setValue(encrypted);
		} else {
			phoneNosMap.put(specifiedPhoneNo, specifiedPhoneNo);
			phoneNo1_tf.setValue(specifiedPhoneNo);
		}
		content_ta.setValue("");
		// 更新短信模板的容器
		updateTempates();
	}
	
	/**
	 * 不传人任何值，更新发送短信的窗口
	 * 	使用这种刷新方法的时机：希望弹出一个可以输电话号和短信内容的窗口进行发短信
	 */
	public void updateWindowComponents() {
		phoneNo1_tf.setValue("");
		content_ta.setValue("");
		// 更新短信模板的容器
		updateTempates();
	}

}
