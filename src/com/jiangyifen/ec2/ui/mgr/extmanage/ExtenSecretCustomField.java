package com.jiangyifen.ec2.ui.mgr.extmanage;

import org.vaadin.addon.customfield.CustomField;

import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 自定义分机密码组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class ExtenSecretCustomField extends CustomField {
	
	private VerticalLayout secretLayout;
	
	private OptionGroup stypeOption;
	private TextField specifiySecret;

	public ExtenSecretCustomField() {
		this.setCaption("密码设置：");
		this.setRequired(true);
		this.setRequiredError("密码不能为空！");
		
		secretLayout = new VerticalLayout();
		secretLayout.setSpacing(true);
		this.setCompositionRoot(secretLayout);
		
		stypeOption = new OptionGroup();
		stypeOption.addItem("随机生成");
		stypeOption.addItem("指定密码");
		stypeOption.setValue("指定密码");
		stypeOption.setImmediate(true);
		stypeOption.setStyleName("twocol200");
		secretLayout.addComponent(stypeOption);
		stypeOption.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				String stype = (String) stypeOption.getValue();
				if("随机生成".equals(stype)) {
					specifiySecret.setValue("");
					specifiySecret.setReadOnly(true);
					specifiySecret.setVisible(false);
				} else {
					specifiySecret.setVisible(true);
					specifiySecret.setReadOnly(false);
				}
			}
		});
		
		specifiySecret = new TextField();
		specifiySecret.setNullRepresentation("");
		specifiySecret.setNullSettingAllowed(true);
		specifiySecret.setImmediate(true);
		secretLayout.addComponent(specifiySecret);
	}
	
	@Override
	public Class<?> getType() {
		return String.class;
	}
	
	@Override
	public Object getValue() {
		return specifiySecret.getValue().toString().trim();
	}
	
	/**
	 * 设置自定义的组件的验证属性
	 */
	@Override
	public boolean isValid() {
		if(specifiySecret.isReadOnly()) {
			return true;
		} 
		
		String secret = specifiySecret.getValue().toString().trim();
		if(secret.matches("\\w{1,80}")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 设置自定义的组件 的宽度
	 * @param width
	 */
	@Override
	public void setWidth(String width) {
		specifiySecret.setWidth(width);
	}
}
