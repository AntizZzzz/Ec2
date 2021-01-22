package com.jiangyifen.ec2.ui.mgr.messagetemplatemanage;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.SmsPhoneNumber;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.SendMessageManage;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class EditContactsWindow extends Window implements ClickListener {
	private static final Object[] COL_ORDER = {"name", "phoneNumber"};
	private static final String[] COL_HEADER = { "姓名", "电话" };

	// 持有上层组件引用
	private SendMessageManage sendMessageManage;

	// form组件
	private Form form;
	// 按钮组件
	private Button save;
	private Button cancel;
	// 当前选择的短信
	private SmsPhoneNumber smsPhoneNumber;

	private TextField phoneNumberField;
	
	private CommonService commonService;

	public EditContactsWindow(SendMessageManage sendMessageManage) {
		this.sendMessageManage = sendMessageManage;
		this.commonService = SpringContextHolder.getBean("commonService");
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
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
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
			if ("name".equals(propertyId)) {
				TextField name= new TextField();
				return name;
			} else if ("phoneNumber".equals(propertyId)) {
				phoneNumberField = new TextField();
				phoneNumberField.setRequired(true);
				phoneNumberField.setRequiredError(" 电话号码不能为空");
				return phoneNumberField;
			}
			return null;
		}

	}

	@SuppressWarnings("static-access")
	@Override
	public void attach() {
		super.attach();
		this.setCaption("编辑联系人");
		smsPhoneNumber = this.sendMessageManage.getSelect();
		form.setItemDataSource(new BeanItem<SmsPhoneNumber>(smsPhoneNumber),
				Arrays.asList(this.COL_ORDER));
		for (int i = 0; i < this.COL_ORDER.length; i++) {
			form.getField(this.COL_ORDER[i]).setCaption(
					this.COL_HEADER[i]);
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
		String phoneNumber=(String)phoneNumberField.getValue();
		if(!StringUtils.isNumeric(phoneNumber)){
			this.showNotification("电话号码 必须由数字组成");
			return;
		}
		
		try {
			form.commit();
			smsPhoneNumber = (SmsPhoneNumber)commonService.update(smsPhoneNumber);
			this.getParent().removeWindow(this);
			sendMessageManage.update();
		} catch (Exception e) {
			e.printStackTrace();
//			this.getApplication().getMainWindow().showNotification("保存短信失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
		}

	}

}
