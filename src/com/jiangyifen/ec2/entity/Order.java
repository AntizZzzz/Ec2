package com.jiangyifen.ec2.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.DiliverStatus;
import com.jiangyifen.ec2.entity.enumtype.PayStatus;
import com.jiangyifen.ec2.entity.enumtype.QualityStatus;

@Entity
@Table(name = "ec2_order")
public class Order { // 订单

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order")
	@SequenceGenerator(name = "order", sequenceName = "seq_ec2_order_id", allocationSize = 1)
	private Long id;

	//============订货人信息=============//
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String customerPhoneNumber; // 订货人联系电话

	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String customerName; // 订货人姓名

	private Double postMoney; //邮费信息
	
	/*
	 * 省、直辖市、自治区
	 */
	private String province;
	
	/*
	 * 市
	 */
	private String city;
	
	/*
	 * 县、区
	 */
	private String county;

	
	/*
	 * 街道地址
	 */
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String street;
	
	/*
	 * jrh 订单备注 
	 */
	@Size(min = 0, max = 1024)
	@Column(columnDefinition="character varying(1024)")
	private String note;

	// 项目编号
	@Column(columnDefinition="bigint")
	private Long projectId;		// jrh 项目编号

	//项目名字
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String projectName;
	
	private Double totalPrice; 	// 订购商品的价格

	//============订单信息=============//
	@Enumerated
	private PayStatus payStatus; // 订单的支付状态信息

	@Enumerated
	private DiliverStatus diliverStatus; // 发货状态信息
	
	@Enumerated
	private QualityStatus qualityStatus; // 质检状态信息

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date generateDate; // 下订单的时间
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date qualityDate; // 质检的时间
	
	private Long csrUserId; //下订单CSR 

	private Long mgrUserId; //质检的Mgr 
	
	//=================对应关系=================//
	//订单1-->n详单
	@OneToMany(fetch = FetchType.LAZY, targetEntity = Orderdetails.class, mappedBy="order")
	private Set<Orderdetails> orderdetails = new HashSet<Orderdetails>();
	
	@ManyToOne(targetEntity = CustomerResource.class,fetch=FetchType.LAZY) 
	private CustomerResource customerResource; //多个详单对应一个客户
	
	//域的概念 批次n-->1域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	// ================= getter 和 setter 方法=================//
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Double getPostMoney() {
		return postMoney;
	}

	public void setPostMoney(Double postMoney) {
		this.postMoney = postMoney;
	}


	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public PayStatus getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(PayStatus payStatus) {
		this.payStatus = payStatus;
	}

	public DiliverStatus getDiliverStatus() {
		return diliverStatus;
	}

	public void setDiliverStatus(DiliverStatus diliverStatus) {
		this.diliverStatus = diliverStatus;
	}


	public Date getGenerateDate() {
		return generateDate;
	}

	public void setGenerateDate(Date generateDate) {
		this.generateDate = generateDate;
	}

	
	public String getCustomerPhoneNumber() {
		return customerPhoneNumber;
	}

	public void setCustomerPhoneNumber(String customerPhoneNumber) {
		this.customerPhoneNumber = customerPhoneNumber;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public QualityStatus getQualityStatus() {
		return qualityStatus;
	}

	public void setQualityStatus(QualityStatus qualityStatus) {
		this.qualityStatus = qualityStatus;
	}

	public Date getQualityDate() {
		return qualityDate;
	}

	public void setQualityDate(Date qualityDate) {
		this.qualityDate = qualityDate;
	}

	public Long getCsrUserId() {
		return csrUserId;
	}

	public void setCsrUserId(Long csrUserId) {
		this.csrUserId = csrUserId;
	}

	public Long getMgrUserId() {
		return mgrUserId;
	}

	public void setMgrUserId(Long mgrUserId) {
		this.mgrUserId = mgrUserId;
	}

	public Set<Orderdetails> getOrderdetails() {
		return orderdetails;
	}

	public void setOrderdetails(Set<Orderdetails> orderdetails) {
		this.orderdetails = orderdetails;
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
