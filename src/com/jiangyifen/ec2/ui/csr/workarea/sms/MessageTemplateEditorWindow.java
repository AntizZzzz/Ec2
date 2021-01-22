package com.jiangyifen.ec2.ui.csr.workarea.sms;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 消息模板编辑器
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class MessageTemplateEditorWindow extends Window implements ClickListener {
	
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"title", "content"};
	
	private Notification success_notification;					// 成功过提示信息
	private Notification warning_notification;					// 错误警告提示信息
	
	private Form templateForm;									// 模板短信表单
	private TextField title_tf;									// 标题输入框
	private TextArea content_ta;								// 内容输入框
	
	private Button save_button;									// 保存按钮
	private Button cancel_button;								// 取消按钮
	
	private CsrMessageTemplateView messageTemplateView;			// 短信模板Tab页组件
	
	private boolean isAddNewTemplate;							// 短信模板Tab 页的操作是否为添加新模板操作
	private MessageTemplate messageTemplate;					// 当前短信对象
	private MessageTemplateService messageTemplateService;		// 短信模板服务类
	
	public MessageTemplateEditorWindow(CsrMessageTemplateView messageTemplateView) {
		this.center();
		this.setImmediate(true);
		this.setResizable(false);
		this.setClosable(true);
		this.messageTemplateView = messageTemplateView;

		messageTemplateService = SpringContextHolder.getBean("messageTemplateService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);

		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		// 创建Form 下的各Field 组件
		createMainFieldComponents();

		// 创建Form 表单
		createFormComponent(windowContent);
	}

	/**
	 * 创建Form 下的各Field 组件
	 */
	private void createMainFieldComponents() {
		title_tf = new TextField();
		title_tf.setRequired(true);
		title_tf.setCaption("短信标题：");
		title_tf.setNullRepresentation("");
		title_tf.setWidth("350px");
		
		content_ta = new TextArea();
		content_ta.setRows(3);
		content_ta.setCaption("短信内容：");
		content_ta.setRequired(true);
		content_ta.setNullRepresentation("");
		content_ta.setWidth("350px");
	}

	/**
	 * 创建Form 表单, 并放入windowContent 
	 * @param windowContent
	 */
	private void createFormComponent(VerticalLayout windowContent) {
		templateForm = new Form();
		templateForm.setValidationVisibleOnCommit(true);
		templateForm.setValidationVisible(false);
		templateForm.setInvalidCommitted(false);
		templateForm.setWriteThrough(false);
		templateForm.setImmediate(true);
		templateForm.setFormFieldFactory(new MyFieldFactory());
		templateForm.setFooter(creatFormFooterComponents());
		windowContent.addComponent(templateForm);
	}
	
	/**
	 *  创建操作按钮
	 */
	private HorizontalLayout creatFormFooterComponents() {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
		operator_l.setWidth("100%");
	
		save_button = new Button("保存", this);
		save_button.setStyleName("default");
		operator_l.addComponent(save_button);
		
		cancel_button = new Button("取消", this);
		operator_l.addComponent(cancel_button);
		
		return operator_l;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_button) {
			executeSave();
		} else if(source == cancel_button) {
			templateForm.discard();
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 执行保存
	 */
	private void executeSave() {
		String title = StringUtils.trimToEmpty((String) title_tf.getValue());
		if("".equals(title)) {
			warning_notification.setCaption("短信模板标题不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		String content = StringUtils.trimToEmpty((String) content_ta.getValue());
		if("".equals(content)) {
			warning_notification.setCaption("短信模板内容不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		templateForm.commit();
		try {
			if(isAddNewTemplate) {
				messageTemplateService.save(messageTemplate);
			} else {
				messageTemplateService.update(messageTemplate);
			}
		} catch (Exception e) {
			logger.error("话务员更新信息模板出现异常---> "+e.getMessage(), e);
			warning_notification.setCaption("保存失败，请检查信息后重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		// 更新短信模板显示界面的信息
		messageTemplateView.refreshTable(isAddNewTemplate);
		
		success_notification.setCaption("保存信息成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
		this.getApplication().getMainWindow().removeWindow(this);
	}
	
	/**
	 * 自定义Form 域创建类
	 */
	private class MyFieldFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("title".equals(propertyId)){
				return title_tf;
			} else if("content".equals(propertyId)){
				return content_ta;
			}
			return null;
		}
	}

	/**
	 * 更新Form 的数据源
	 * @param messageTemplate
	 */
	public void updateFormDataSource(MessageTemplate messageTemplate, boolean isAddNewTemplate) {
		this.messageTemplate = messageTemplate;
		this.isAddNewTemplate = isAddNewTemplate;
		templateForm.setItemDataSource(new BeanItem<MessageTemplate>(messageTemplate), Arrays.asList(VISIBLE_PROPERTIES));
	}
	
}
