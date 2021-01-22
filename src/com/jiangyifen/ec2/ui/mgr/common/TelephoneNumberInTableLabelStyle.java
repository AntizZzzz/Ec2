package com.jiangyifen.ec2.ui.mgr.common;

import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * 用于作为管理员界面下的表格中的电话号码的显示组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class TelephoneNumberInTableLabelStyle extends VerticalLayout {

	/**
	 * 传过来的是一个电话号码Telephone 对象集合，以及是否需要加密显示
	 * @param telephones		电话对象集合
	 * @param isEncryptMobile	是否需要加密显示
	 */
	public TelephoneNumberInTableLabelStyle(Set<Telephone> telephones, boolean isEncryptMobile) {
		this.setWidth("-1px");
		if(telephones == null || telephones.size() == 0) {
			return;
		}
		
		TelephoneService telephoneService = SpringContextHolder.getBean("telephoneService");
		
		String numbers = "";
		StringBuffer numberBf = new StringBuffer();

		if(isEncryptMobile == true) {
			for(Telephone tp : telephones) {
				numberBf.append(telephoneService.encryptMobileNo(tp.getNumber()));
				numberBf.append(",");
			}
			numbers = numberBf.toString();
			if(numbers.endsWith(",")) {
				numbers = numbers.substring(0, numbers.length() -1); 
			}
		} else {
			for(Telephone tp : telephones) {
				numberBf.append(tp.getNumber());
				numberBf.append(",");
			}
			
			numbers = numberBf.toString();
			if(numbers.endsWith(",")) {
				numbers = numbers.substring(0, numbers.length() -1); 
			}
		}
		
		Label phoneLabel = new Label(numbers);
		phoneLabel.setWidth("-1px");
		this.addComponent(phoneLabel);
	}
	
	/**
	 * 传过来的是一个电话号码 对象集合，以及是否需要加密显示
	 * @param telephones		电话字符串集合
	 * @param isEncryptMobile	是否需要加密显示
	 */
	public TelephoneNumberInTableLabelStyle(List<String> telephones, boolean isEncryptMobile) {
		this.setWidth("-1px");
		if(telephones == null || telephones.size() == 0) {
			return;
		}
		
		TelephoneService telephoneService = SpringContextHolder.getBean("telephoneService");
		
		String numbers = "";
		StringBuffer numberBf = new StringBuffer();
		
		if(isEncryptMobile == true) {
			for(String tp : telephones) {
				numberBf.append(telephoneService.encryptMobileNo(tp));
				numberBf.append(",");
			}
			numbers = numberBf.toString();
			if(numbers.endsWith(",")) {
				numbers = numbers.substring(0, numbers.length() -1); 
			}
		} else {
			for(String tp : telephones) {
				numberBf.append(tp);
				numberBf.append(",");
			}
			
			numbers = numberBf.toString();
			if(numbers.endsWith(",")) {
				numbers = numbers.substring(0, numbers.length() -1); 
			}
		}
		
		Label phoneLabel = new Label(numbers);
		phoneLabel.setWidth("-1px");
		this.addComponent(phoneLabel);
	}
	
	/**
	 * 传过来的是一个电话号码，以及是否需要加密显示
	 * @param telephone			电话号码
	 * @param isEncryptMobile	是否需要加密显示
	 */
	public TelephoneNumberInTableLabelStyle(String telephone, boolean isEncryptMobile) {
		this.setWidth("-1px");
		if(telephone == null || "".equals(telephone)) {
			return;
		}
		
		TelephoneService telephoneService = SpringContextHolder.getBean("telephoneService");
		
		String number = telephone;
		if(isEncryptMobile == true) {
			number = telephoneService.encryptMobileNo(telephone);
		} 
		
		Label phoneLabel = new Label(number);
		phoneLabel.setWidth("-1px");
		this.addComponent(phoneLabel);
	}
	
	
}
