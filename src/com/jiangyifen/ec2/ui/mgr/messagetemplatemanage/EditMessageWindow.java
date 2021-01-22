package com.jiangyifen.ec2.ui.mgr.messagetemplatemanage;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MessageTemplateManage;
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

@SuppressWarnings("serial")
public class EditMessageWindow extends Window implements ClickListener {

	private static final Object[] MESSAGE_COL_ORDER = { "user","type", "title","content" };
	private static final String[] MESSAGE_COL_HEADER = { "创建人", "短信类型","短信标题","短信内容" };

	// 持有上层组件引用
	private MessageTemplateManage messageTemplateManage;

	// form组件
	private Form form;
	// 按钮组件
	private Button save;
	private Button cancel;
	// 当前选择的短信
	private MessageTemplate message;

	private MessageTemplateService messageTemplateService;

	public EditMessageWindow(MessageTemplateManage messageTemplateManage) {
		this.messageTemplateManage = messageTemplateManage;
		this.messageTemplateService = SpringContextHolder.getBean("messageTemplateService");
		this.center();
		this.setModal(true);
		this.setResizable(false);

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// From输出
		form = new Form();
		form.setValidationVisible(false);
		form.setValidationVisibleOnCommit(true);
		form.setInvalidCommitted(false);
		form.setWriteThrough(false);
		form.setImmediate(true);
		form.addStyleName("chb");
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(buildButtonsLayout());
		windowContent.addComponent(form);

	}

	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存");
		save.setStyleName("default");
		save.addListener(this);
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);

		return buttonsLayout;
	}

	private class MyFieldFactory extends DefaultFieldFactory {

		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if ("user".equals(propertyId)) {
				TextField user = new TextField();
				user.setReadOnly(true);
				return user;
			} else if ("title".equals(propertyId)) {
				TextField title = new TextField();
				title.setRequired(true);
				title.setRequiredError("短信标题不能为空");
				return title;
			} else if ("content".equals(propertyId)) {
				TextArea content = new TextArea();
				content.setRequired(true);
				content.setRequiredError("短信内容不能为空");
				return content;
			} else if ("type".equals(propertyId)) {
				TextField type = new TextField();
				type.setReadOnly(true);
				return type;
			} else if ("domain".equals(propertyId)) {
				TextField domain = new TextField();
				domain.setReadOnly(true);
				return domain;

			}
			return null;
		}

	}

	@SuppressWarnings("static-access")
	@Override
	public void attach() {
		super.attach();
		this.setCaption("编辑短信");
		message = this.messageTemplateManage.getSelectMessage();
		form.setItemDataSource(new BeanItem<MessageTemplate>(message),
				Arrays.asList(this.MESSAGE_COL_ORDER));
		for (int i = 0; i < this.MESSAGE_COL_ORDER.length; i++) {
			form.getField(this.MESSAGE_COL_ORDER[i]).setCaption(
					this.MESSAGE_COL_HEADER[i]);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == save) {
			executeSave();
		} else if (event.getButton() == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}

	}

	private void executeSave() {
		try {
			String title= StringUtils.trimToEmpty((String)form.getField("title").getValue());
			String content = StringUtils.trimToEmpty((String) form.getField("content").getValue());
			try {
				form.commit();
			} catch (Exception e) {
				return;
			}
			if("".endsWith(content)||"".equals(title)){
				this.getApplication().getMainWindow().showNotification("短信标题和内容都不能为空！");
				return;
			}
			message = messageTemplateService.update(message);
			messageTemplateManage.getTable().refreshRowCache();
			this.getApplication().getMainWindow().showNotification("编辑成功！");
			this.getParent().removeWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存短信失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
		}

	}

}
