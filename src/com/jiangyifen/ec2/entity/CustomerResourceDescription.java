package com.jiangyifen.ec2.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_customer_resource_description")
public class CustomerResourceDescription {
//对资源信息的描述(如：爱好 篮球，身高 180，等)
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_resource_description")
	@SequenceGenerator(name = "customer_resource_description", sequenceName = "seq_ec2_customer_resource_description_id", allocationSize = 1)
	private Long id; 
	
	//描述信息key值（如：爱好）
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String key;
	
	//描述信息Value值（如： 篮球）
	@Column(columnDefinition= "TEXT")
	private String value;
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;		// 创建时间
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdateDate;	// 最后一次修改时间
	
	//=================对应关系=================//
	
	//描述n-->1资源  一条客户信息资源可能有多个描述信息
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	private CustomerResource customerResource;

	//域的概念 批次1-->n域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	//================= getter 和  setter 方法=================//
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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

	public Date getCreateDate() {
		if(createDate == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				return sdf.parse("1970-01-01");
			} catch (ParseException e) {
				
			}
		}
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getLastUpdateDate() {
		if(lastUpdateDate == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try {
				return sdf.parse("1970-01-01");
			} catch (ParseException e) {
				
			}
		}
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String toLoggerString() {
		return "CustomerResourceDescription [id=" + id + ", key=" + key
				+ ", value=" + value + ", customerResource=" + customerResource
				+ ", domain=" + domain + "]";
	}

	// jrh 导出信息时有用的
	@Override
	public String toString() {
		return "[" + key + ": " + value + "] ";
	}
	
}
