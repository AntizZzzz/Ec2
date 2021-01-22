package com.jiangyifen.ec2.ui.mgr.messagetemplatemanage;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MessageTemplateManage;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AddMessageWindow extends Window implements ClickListener {

	// 标签和输入框组件
	private Label label;
	private TextField titleField;
	private TextArea contentArea;

	// 按钮组件
	private Button save;
	private Button cancel;
	//持有上层组件引用
	private MessageTemplateManage messageTemplateManage;

	private MessageTemplateService messageTemplateService;

	public AddMessageWindow(MessageTemplateManage messageTemplateManage) {
		this.messageTemplateService = SpringContextHolder.getBean("messageTemplateService");
		this.messageTemplateManage = messageTemplateManage;
		this.setCaption("添加短信模板");
		this.center();
		this.setModal(true);
		this.setResizable(false);

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);

		HorizontalLayout layoutTitle = new HorizontalLayout();
		Label titelLabel = new Label("短信标题:");
		titelLabel.setWidth("80px");
		layoutTitle.addComponent(titelLabel);
		
		titleField= new TextField();
		titleField.setRequired(true);
		titleField.setNullRepresentation("");
		titleField.setInputPrompt("模板标题");
		layoutTitle.addComponent(titleField);
		windowContent.addComponent(layoutTitle);
		

		HorizontalLayout layout = new HorizontalLayout();
		label = new Label("短信内容:");
		label.setWidth("80px");
		layout.addComponent(label);

		contentArea = new TextArea();
		contentArea.setRequired(true);
		contentArea.setValue("");
		contentArea.setNullRepresentation("");
		contentArea.setInputPrompt("请输入短信内容");
		layout.addComponent(contentArea);

		windowContent.addComponent(layout);

		windowContent.addComponent(buildButtonsLayout());

	}

	// 创建按钮组件
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存");
		save.setStyleName("default");
		save.addListener((ClickListener) this);
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener((ClickListener) this);
		buttonsLayout.addComponent(cancel);
		return buttonsLayout;
	}

	

	@Override
	public void attach() {
		super.attach();
		titleField.setValue("");
		contentArea.setValue("");
	}

	//执行添加操作
	private void executeSave() {
		MessageTemplate message = new MessageTemplate();
		User loginUser = SpringContextHolder.getLoginUser();
		String content = StringUtils.trimToEmpty((String) contentArea.getValue());
		String title= StringUtils.trimToEmpty((String)titleField.getValue());
		if("".endsWith(content)||"".equals(title)){
			this.showNotification("短信标题和内容都不能为空！");
			return;
		}
		
		message.setUser(loginUser);
		message.setTitle(title);
		message.setContent(content);
		message.setType(MessageTemplateType.system);
		message.setDomain(loginUser.getDomain());
		try {
			messageTemplateService.save(message);
			this.getApplication().getMainWindow().showNotification("添加成功！");
			this.getParent().removeWindow(this);
			messageTemplateManage.updateTable();
			messageTemplateManage.getTable().setValue(null);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("添加失败！", Notification.TYPE_WARNING_MESSAGE);
		}
	}
	
	//按钮点击事件监听器
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == save) {
			executeSave();
		} else if (event.getButton() == cancel) {
			this.getParent().removeWindow(this);
		}

	}

}
