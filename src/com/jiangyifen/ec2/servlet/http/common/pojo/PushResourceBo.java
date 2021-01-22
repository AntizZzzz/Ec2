package com.jiangyifen.ec2.servlet.http.common.pojo;

import java.util.List;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Telephone;

/**
 * 推送客户资源时，用来解析 JSON 的 POJO 类
 *
 * @author jinht
 *
 * @date 2015-6-23 上午11:26:32 
 *
 */
public class PushResourceBo {

	private String name;						// 客户名称
	private String sex;						// 客户性别
	private String birthday;					// 客户性别
	private String customer_source;			// 客户来源
	private String note;						// 客户描述
	
	private List<Telephone> telephones;		// 客户手机号
	private List<Address> addresses;			// 客户地址
	private List<CustomerResourceDescription>	descs;	// 客户描述信息
	
	private String customerResourceBatch;		// 批次名称
	private String companyName;				// 公司名称
	private String domainId;					// 所在域

	
//	getter and setter method
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getCustomer_source() {
		return customer_source;
	}

	public void setCustomer_source(String customer_source) {
		this.customer_source = customer_source;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<Telephone> getTelephones() {
		return telephones;
	}

	public void setTelephones(List<Telephone> telephones) {
		this.telephones = telephones;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<CustomerResourceDescription> getDescs() {
		return descs;
	}

	public void setDescs(List<CustomerResourceDescription> descs) {
		this.descs = descs;
	}

	public String getCustomerResourceBatch() {
		return customerResourceBatch;
	}

	public void setCustomerResourceBatch(String customerResourceBatch) {
		this.customerResourceBatch = customerResourceBatch;
	}
	
	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@Override
	public String toString() {
		return "PushResourceBo [name=" + name + ", sex=" + sex + ", birthday="
				+ birthday + ", customer_source=" + customer_source + ", note="
				+ note + ", telephones=" + telephones + ", addresses="
				+ addresses + ", descriptions=" + descs
				+ ", customerResourceBatch=" + customerResourceBatch + ", companyName=" + companyName + ", domainId=" + domainId + "]";
	}
	
}
