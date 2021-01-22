package com.jiangyifen.ec2.ui.mgr.messagetemplatemanage;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.SmsPhoneNumber;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.SmsPhoneNumberType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.SendMessageManage;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AddContactsWindow extends Window implements ClickListener {

	// 标签和输入框组件
	private Label label;
	private TextField nameField;
	private TextField phoneNumberField;

	// 按钮组件
	private Button save;
	private Button cancel;
	//持有上层组件引用
	private SendMessageManage sendMessageManage;

	private CommonService commonService;

	public AddContactsWindow(SendMessageManage sendMessageManage) {
		this.commonService = SpringContextHolder.getBean("commonService");
		this.sendMessageManage = sendMessageManage;
		this.setCaption("添加联系人");
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
		Label titelLabel = new Label("联系人姓名:");
		titelLabel.setWidth("80px");
		layoutTitle.addComponent(titelLabel);
		
		nameField= new TextField();
		nameField.setNullRepresentation("");
		nameField.setInputPrompt("联系人姓名");
		layoutTitle.addComponent(nameField);
		windowContent.addComponent(layoutTitle);
		

		HorizontalLayout layout = new HorizontalLayout();
		label = new Label("电话号码:");
		label.setWidth("80px");
		layout.addComponent(label);

		phoneNumberField = new TextField();
		phoneNumberField.setRequired(true);
		phoneNumberField.setNullRepresentation("");
		phoneNumberField.setInputPrompt("电话号码");
		layout.addComponent(phoneNumberField);

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
		nameField.setValue(null);
		phoneNumberField.setValue(null);
	}

	//执行添加操作
	private void executeSave() {
		SmsPhoneNumber smsPhoneNumber=new SmsPhoneNumber();
		User loginUser = SpringContextHolder.getLoginUser();
		String name = (String) nameField.getValue();
		String phoneNumber=(String)phoneNumberField.getValue();
		if(StringUtils.isEmpty(phoneNumber)){
			this.showNotification("电话号码不能为空！");
			return;
		}
		if(!StringUtils.isNumeric(phoneNumber)){
			this.showNotification("电话号码 必须由数字组成");
			return;
		}
		
		smsPhoneNumber.setName(name);
		smsPhoneNumber.setPhoneNumber(phoneNumber);
		smsPhoneNumber.setTime(new Date());
		smsPhoneNumber.setSmsPhoneNumberType(SmsPhoneNumberType.CONTACTS);
		smsPhoneNumber.setDomain(loginUser.getDomain());
		try {
			commonService.save(smsPhoneNumber);
			this.getParent().removeWindow(this);
			sendMessageManage.update();
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("添加失败");
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
