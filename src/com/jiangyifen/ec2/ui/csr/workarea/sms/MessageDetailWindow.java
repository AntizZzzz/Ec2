package com.jiangyifen.ec2.ui.csr.workarea.sms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Message;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageType;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.sms.SmsUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

/**
 * 短信详细内容查看窗口
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class MessageDetailWindow extends Window implements ClickListener, CloseListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 号码加密权限
	private final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"phoneNumber", "time", "content", "messageType"};

	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息
	
	private Form messageForm;										// 短信表单
	private TextField phoneNo_tf;									// 收信人电话
	private PopupDateField time_pdf;								// 发生时间
	private TextArea content_ta;									// 短信内容
	private ComboBox messageType_cb;								// 发送状态
	
	private Button editAndSend_button;								// 编辑按钮
	private Button sendAgain_button;								// 重发按钮
	private Button cancel_button;									// 取消按钮
	
	private CsrHistoryMessageView historyMessageView;				// 历史发送短信显示界面

	private User loginUser;											// 当前登陆用户
	private String exten;											// 当前登陆用户使用的分机
	private Domain domain;											// 当前登陆用户所属域
	private ArrayList<String> ownBusinessModels;					// 当前用户拥有的权限
	
	private TelephoneService telephoneService;

	public MessageDetailWindow(CsrHistoryMessageView historyMessageView) {
		this.center();
		this.setResizable(false);
		this.setImmediate(true);
		this.setCaption("短信详情");
		this.addListener((CloseListener)this);

		this.historyMessageView = historyMessageView;

		loginUser = SpringContextHolder.getLoginUser();
		exten = SpringContextHolder.getExten();
		domain = SpringContextHolder.getDomain();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
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
		
		// 创建表单中的主要组件
		createFieldComponents();
		
		// 创建Form 表单, 并放入windowContent 
		createFormComponent(windowContent);
	}
	
	/**
	 *  创建表单中的主要组件
	 */
	private void createFieldComponents() {
		phoneNo_tf = new TextField("接收电话：");
		phoneNo_tf.setMaxLength(11);
		phoneNo_tf.setNullRepresentation("");
		phoneNo_tf.setWidth("350px");
		
		time_pdf = new PopupDateField("发送时间：");
		time_pdf.setImmediate(true);
		time_pdf.setWidth("350px");
		time_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
		time_pdf.setParseErrorMessage("时间格式不合法");
		time_pdf.setResolution(PopupDateField.RESOLUTION_SEC);
		
		content_ta = new TextArea("短信内容：");
		content_ta.setRows(3);
		content_ta.setDescription("<B>短信内容不能为空!</B>");
		content_ta.setNullRepresentation("");
		content_ta.setWidth("350px");
		
		BeanItemContainer<MessageType> typeContainer = new BeanItemContainer<MessageType>(MessageType.class);
		typeContainer.addBean(MessageType.SUCCESS);
		typeContainer.addBean(MessageType.FAILED);
		typeContainer.addBean(MessageType.UNKNOWN);
		
		messageType_cb = new ComboBox("短信状态：");
		messageType_cb.setWidth("350px");
		messageType_cb.setNullSelectionAllowed(false);
		messageType_cb.setContainerDataSource(typeContainer);
		messageType_cb.setItemCaptionPropertyId("name");
	}

	/**
	 * 创建Form 表单, 并放入windowContent 
	 * @param windowContent
	 */
	private void createFormComponent(VerticalLayout windowContent) {
		messageForm = new Form();
		messageForm.setValidationVisibleOnCommit(true);
		messageForm.setValidationVisible(false);
		messageForm.setInvalidCommitted(false);
		messageForm.setWriteThrough(false);
		messageForm.setImmediate(true);
		messageForm.setFormFieldFactory(new MyFieldFactory());
		messageForm.setFooter(creatFormFooterComponents());
		windowContent.addComponent(messageForm);
	}
	
	/**
	 *  创建操作按钮
	 */
	private HorizontalLayout creatFormFooterComponents() {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
		operator_l.setWidth("100%");

		editAndSend_button = new Button("编辑并重发", this);
		operator_l.addComponent(editAndSend_button);
		
		cancel_button = new Button("取消", this);
		operator_l.addComponent(cancel_button);
		
		sendAgain_button = new Button("重发", this);
		sendAgain_button.setStyleName("default");
		operator_l.addComponent(sendAgain_button);
		
		// 更新按钮的可视属性
		updateButtonsVisible(true);
		return operator_l;
	}
	
	/**
	 * 更新按钮的可视属性
	 * @param visible
	 */
	private void updateButtonsVisible(boolean visible) {
		editAndSend_button.setVisible(visible);
		cancel_button.setVisible(!visible);
		sendAgain_button.setVisible(!visible);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == editAndSend_button) {
			messageForm.setReadOnly(false);
			time_pdf.setVisible(false);
			messageType_cb.setVisible(false);
			phoneNo_tf.setReadOnly(true);
			updateButtonsVisible(false);
		} else if(source == sendAgain_button) {
			try {
				executeSend();
			} catch (Exception e) {
				logger.error("话务员执行重发短信，出现异常 -----> "+e.getMessage(), e);
			}
		} else if(source == cancel_button) {
			time_pdf.setVisible(true);
			messageType_cb.setVisible(true);
			messageForm.discard();
			if(ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET)) {
				String encryptNumber = telephoneService.encryptMobileNo((String) phoneNo_tf.getData());
				phoneNo_tf.setReadOnly(false);
				phoneNo_tf.setValue(encryptNumber);
			}
			messageForm.setReadOnly(true);
			updateButtonsVisible(true);
		}
	}

	/**
	 * 执行发送短信
	 */
	private void executeSend() {
		//检查输入是否合法
		String phoneNo = (String) phoneNo_tf.getData();

		String content = StringUtils.trimToEmpty((String) content_ta.getValue()).trim();
		if("".equals(content.trim())) {
			warning_notification.setCaption("短信内容不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}

		// 发送短信
		List<String> phoneNos = new ArrayList<String>();
		phoneNos.add(phoneNo);
		String result = SmsUtil.sendSMS(loginUser, exten, domain.getId(), phoneNos, content);
		if(!"成功".equals(result)) {
			warning_notification.setCaption("短信发送失败！！！原因："+result);
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		// 更新历史短信界面到第一页
		historyMessageView.refreshTable(true);
		
		success_notification.setCaption("短信发送成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
		this.getApplication().getMainWindow().removeWindow(this);
	}

	@Override
	public void windowClose(CloseEvent e) {
		// 窗口关闭时，恢复组件的可视属性
		phoneNo_tf.setVisible(true);
		time_pdf.setVisible(true);
		content_ta.setVisible(true);
		messageType_cb.setVisible(true);
		updateButtonsVisible(true);
	}

	/**
	 * 自定义Form 域创建类
	 */
	private class MyFieldFactory extends DefaultFieldFactory {
		@Override  
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("phoneNumber".equals(propertyId)){
				return phoneNo_tf;
			} else if("time".equals(propertyId)){
				return time_pdf;
			} else if("content".equals(propertyId)){
				return content_ta;
			} else if("messageType".equals(propertyId)){
				return messageType_cb;
			}
			return null;
		}
	}
	
	/**
	 * 更新Form 的数据源，并判断是否为需要重发，如果是，则直接进入编辑状态
	 * @param message		短信对象
	 * @param isSendAgain	是否是需要再次发送
	 */
	public void updateFormDataSource(Message message, boolean isSendAgain) {
		messageForm.setReadOnly(false);
		messageForm.setItemDataSource(new BeanItem<Message>(message), Arrays.asList(VISIBLE_PROPERTIES));
		phoneNo_tf.setData(message.getPhoneNumber());
		if(ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET)) {
			String encryptNumber = telephoneService.encryptMobileNo(message.getPhoneNumber());
			phoneNo_tf.setValue(encryptNumber);
		}
		if(isSendAgain) {
			editAndSend_button.click();
		} else {
			messageForm.setReadOnly(true);
		}
	}
	
}
