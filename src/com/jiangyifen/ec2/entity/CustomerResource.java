package com.jiangyifen.ec2.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "ec2_customer_resource")
public class CustomerResource {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_resource")
	@SequenceGenerator(name = "customer_resource", sequenceName = "seq_ec2_customer_resource_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String name;
		
	@Size(min = 0, max = 16)
	@Column(columnDefinition="character varying(16)")
	private String sex;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String birthday;
	
	//是谁导入的这批资源
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User owner;
	
	private Integer count=0; //让资源的初始拨打次数是0
	
	// 资源最后拨打时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date lastDialDate;	

	// 资源的过期日期
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date expireDate;	

	// 资源导入时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date importDate;	
	
	//客户编号
	@Column(columnDefinition="bigint")
	private Long service_record_status_id;		// 目前是根据坐席最近一次联系时，在呼叫弹屏中创建客服记录时选定  20150107 JRH-ADD 
	
	@Size(min = 1,max = 255)
	@Column(columnDefinition="character varying(255)")
	private String customer_source;		// 客户来源【导入时确定，不能修改】  20150107 JRH-ADD 暂时没有使用

	@Column(columnDefinition= "TEXT")
	private String note;	// 描述信息   20150107 JRH-ADD 暂时没有使用
	
	public CustomerResource() {
	}
	
	//=================对应关系=================//
	
	// 客户经理  	资源与CSR 的对应关系
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User accountManager;

	// 资源等级
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerLevel.class)
	private CustomerLevel customerLevel;
	
	// 资源与项目的对应关系的中间表，N--N，这里的项目都是该资源成功购买过东西的项目
	@OneToMany(fetch = FetchType.LAZY, targetEntity = ProjectCustomer.class, mappedBy="customerResource")
	private Set<ProjectCustomer> projectCustomers = new HashSet<ProjectCustomer>();

	//客户1-->n地址
	@OneToMany(fetch = FetchType.LAZY, targetEntity = Address.class, mappedBy="customerResource")
	private Set<Address> addresses = new HashSet<Address>();
	
	//客户1-->1地址 默认地址
	@OneToOne(fetch = FetchType.LAZY, targetEntity = Address.class)
	private Address defaultAddress;
	
	//客户N-->1 公司
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Company.class)
	private Company company;
	
	//客户电话 1--> N
	//程序需要将电话设置为可以为null，不为空由导数据进行控制
	@OneToMany(fetch = FetchType.LAZY, targetEntity = Telephone.class, mappedBy="customerResource")
	private Set<Telephone> telephones = new HashSet<Telephone>();
	
	//批次n-->n客户资源  一个批次对应多条客户资源数据
	@ManyToMany(fetch=FetchType.LAZY, targetEntity = CustomerResourceBatch.class)
	private Set<CustomerResourceBatch> customerResourceBatches = new HashSet<CustomerResourceBatch>();
	
	//域的概念 批次n-->1域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	//================= getter 和  setter 方法=================//

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSex() {
		return sex;
	}

	// jrh 	
	public Date getBirthday() {
		Date date=null;
		if(birthday != null && !"".equals(birthday.trim())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try {
				date= dateFormat.parse(birthday);
			} catch (ParseException e) {
				Logger logger = LoggerFactory.getLogger(this.getClass());
				logger.warn("生日解析错误");
			}
		}
		return date;
	}
	
	//chb 数据导入与显示
	public String getBirthdayStr() {
		return birthday;
	}

	public Date getImportDate() {
		return importDate;
	}

	public Set<ProjectCustomer> getProjectCustomers() {
		return projectCustomers;
	}

	public Set<Address> getAddresses() {
		return addresses;
	}

	public Address getDefaultAddress() {
		return defaultAddress;
	}

	public Company getCompany() {
		return company;
	}
	
	public Set<Telephone> getTelephones() {
		return telephones;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}
	
	/**
	 * 该方法用于Csr 在Form 表单中做客户信息编辑
	 * @param birthday
	 */
	public void setBirthday(Date birthday) {
		if(birthday != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			this.birthday = dateFormat.format(birthday);
		} else {
			this.birthday = null;
		}
	}
	
	//chb 数据导入与显示
	public void setBirthdayStr(String birthday) {
		this.birthday = birthday;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}

	public void setProjectCustomers(Set<ProjectCustomer> projectCustomers) {
		this.projectCustomers = projectCustomers;
	}

	public void setAddresses(Set<Address> addresses) {
		this.addresses = addresses;
	}

	public void setDefaultAddress(Address defaultAddress) {
		this.defaultAddress = defaultAddress;
	}

	public void setTelephones(Set<Telephone> telephones) {
		this.telephones = telephones;
	}
	
	public Set<CustomerResourceBatch> getCustomerResourceBatches() {
		return customerResourceBatches;
	}

	public void setCustomerResourceBatches(
			Set<CustomerResourceBatch> customerResourceBatches) {
		this.customerResourceBatches = customerResourceBatches;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public User getAccountManager() {
		return accountManager;
	}

	public void setAccountManager(User accountManager) {
		this.accountManager = accountManager;
	}
	
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Date getLastDialDate() {
		return lastDialDate;
	}

	public void setLastDialDate(Date lastDialDate) {
		this.lastDialDate = lastDialDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public CustomerLevel getCustomerLevel() {
		return customerLevel;
	}

	public void setCustomerLevel(CustomerLevel customerLevel) {
		this.customerLevel = customerLevel;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public Long getService_record_status_id() {
		return service_record_status_id;
	}

	public void setService_record_status_id(Long service_record_status_id) {
		this.service_record_status_id = service_record_status_id;
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

}
