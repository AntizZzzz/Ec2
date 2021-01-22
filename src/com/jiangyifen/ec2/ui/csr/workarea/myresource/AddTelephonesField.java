package com.jiangyifen.ec2.ui.csr.workarea.myresource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.addon.customfield.CustomField;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 自定义电话编辑组件，可以一次性添加多个号码
 * 	号码的添加个数有自己决定
 * @author jrh
 */
@SuppressWarnings("serial")
public class AddTelephonesField extends CustomField {

	private Notification notification;		// 提示信息

	private Domain domain;
	private Set<Telephone> telephones; 		// 电话Set集合
	private List<TextField> phoneFields;	// 存放所有手机文本框的集合
	
	public AddTelephonesField(final String width, Domain domain) {
		this.domain = domain;
		this.setRequired(true);
		this.setRequiredError("电话不能为空！");

		telephones = new HashSet<Telephone>();
		phoneFields = new ArrayList<TextField>();
		
		notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建主要的组件
		final VerticalLayout phoneMainLayout = new VerticalLayout();
		phoneMainLayout.setSpacing(true);
		setCompositionRoot(phoneMainLayout); 
		
		HorizontalLayout phoneCallLayout = new HorizontalLayout();
		phoneCallLayout.setSpacing(true);
		phoneMainLayout.addComponent(phoneCallLayout);
		
		phoneCallLayout.addComponent(createPhoneField(width));
		
		Button addMore = new Button();
		addMore.setStyleName(BaseTheme.BUTTON_LINK);
		addMore.setIcon(ResourceDataCsr.add_16_ico);
		phoneCallLayout.addComponent(addMore);
		
		addMore.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				phoneMainLayout.addComponent(createPhoneField(width));
			}
		});
		
	}
	
	private TextField createPhoneField(String width) {
		final TextField phoneField = new TextField();
		phoneField.setNullRepresentation("");
		phoneField.setNullSettingAllowed(true);
		phoneField.setImmediate(true);
		phoneField.setWidth(width);
		phoneField.setInputPrompt("请输入联系电话");
		phoneField.addValidator(new RegexpValidator("(\\d?)+", "电话号只能由数字组成！"));
		phoneField.setValidationVisible(false);
		phoneFields.add(phoneField);
		
		phoneField.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				if(!phoneField.isValid()) {
					notification.setCaption("电话号只能由<B>数字</B>组成！");
					phoneField.getApplication().getMainWindow().showNotification(notification);
				}
			}
		});
		
		return phoneField;
	}

	@Override
	public Set<Telephone> getValue() {
		telephones.clear();
		for(TextField phoneField : phoneFields) {
			String phoneNum = phoneField.getValue().toString().trim();
			if(!"".equals(phoneNum)) {
				Telephone telephone = new Telephone();
				telephone.setNumber(phoneNum);
				telephone.setDomain(domain);
				telephones.add(telephone);
			} 
		}
		
		// 当一次性添加多个号码时，可能会添加重复号码，所以要去重
		if(telephones.size() > 1) {
			Map<String, Telephone> phoneNums = new HashMap<String, Telephone>();
			for(Telephone tele : telephones) {
				phoneNums.put(tele.getNumber(), tele);
			}
			telephones.clear();
			telephones.addAll(phoneNums.values());
		}
		return telephones;
	}
	
	/**
	 * 设置自定义的组件的验证属性
	 */
	@Override
	public boolean isValid() {
		boolean valid = true;
		for(TextField phoneField : phoneFields) {
			if(!phoneField.isValid()) {
				valid = false;
				break;
			}
		}
		return valid;
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		for(TextField phoneField : phoneFields) {
			phoneField.setReadOnly(readOnly);
		}
	}
	
	@Override
	public Class<?> getType() {
		return Telephone.class;
	}

}
