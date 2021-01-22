package com.jiangyifen.ec2.ui.csr.workarea.myresource;

import org.vaadin.addon.customfield.CustomField;

import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.TextField;

/**
 * 自定义公司组件，可以放到Form 表单中使用，返回类型为Company.class
 * 	可以直接获取到公司对象
 * @author jrh
 */
@SuppressWarnings("serial")
public class AddCompanyField extends CustomField {
	
	private Company company;
	private TextField companyField;
    
    public AddCompanyField() {
    	Domain domain = SpringContextHolder.getDomain();
    	company = new Company();
    	company.setDomain(domain);
    	
		// 创建主要的组件
    	companyField = new TextField();
		companyField.setNullRepresentation("");
		companyField.setNullSettingAllowed(true);
		companyField.setImmediate(true);
		companyField.setInputPrompt("请输入公司名称");
		companyField.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,255}$", "电话号只能由不大于255位字符组成！"));
		companyField.setValidationVisible(false);
		
		setCompositionRoot(companyField);
    }

	
	@Override
	public Company getValue() {
		String name = companyField.getValue().toString().trim();
		if("".endsWith(name)) {
			return null;
		}
		
		company.setName(name);
		return company;
	}
	
	/**
	 * 设置自定义的companyField 的宽度
	 * @param width
	 */
	@Override
	public void setWidth(String width) {
		companyField.setWidth(width);
	}
	
	@Override
	public void setReadOnly(boolean readOnly) {
		companyField.setReadOnly(readOnly);
	}

	@Override
	public Class<?> getType() {
		return Company.class;
	}
}
