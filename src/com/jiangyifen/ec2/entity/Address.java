package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_address")
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address")
	@SequenceGenerator(name = "address", sequenceName = "seq_ec2_address_id", allocationSize = 1)
	private Long id;

	/*
	 * 收货人姓名
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String name = "";
	
	/*
	 * 省、直辖市、自治区
	 */
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Province.class)
	private Province province;
	
	/*
	 * 市
	 */
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = City.class)
	private City city;
	
	/*
	 * 县、区
	 */
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = County.class)
	private County county;

	
	/*
	 * 街道地址
	 */
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String street = "";

	/*
	 * 邮政编码
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String postCode = "";

	/*
	 * 电话号码
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String mobile = "";

	public Address() {
	}
	// =================对应关系=================//

	// 地址 n-->1 客户资源
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	private CustomerResource customerResource;

	// ================= getter 和 setter 方法=================//

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		if(street == null) {
			return "";
		}
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	public Province getProvince() {
		return province;
	}

	public void setProvince(Province province) {
		this.province = province;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public County getCounty() {
		return county;
	}

	public void setCounty(County county) {
		this.county = county;
	}

	public String toString() {
		return street;
	}
	
	public String toLoggerString() {
		return "Address [id=" + id + ", name=" + name + ", province="
				+ province + ", city=" + city + ", county=" + county
				+ ", street=" + street + ", postCode=" + postCode + ", mobile="
				+ mobile + "]";
	}
}

