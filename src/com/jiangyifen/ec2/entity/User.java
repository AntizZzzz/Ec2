package com.jiangyifen.ec2.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_user")
public class User implements Serializable {
	
	private static final long serialVersionUID = 2241269261465281447L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user")
	@SequenceGenerator(name = "user", sequenceName = "seq_ec2_user_id", allocationSize = 1)
	private Long id;	// 用户ID

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String realName;	// 真实姓名

	@Size(min = 0, max = 64)
	@Column(nullable = false,unique = true,columnDefinition="character varying(64) NOT NULL unique")
	private String username;	// 账号名称
	
	@Size(min = 0,max = 32)
	@Column(nullable = false,columnDefinition="character varying(32) NOT NULL")
	private String password;	// 登录密码
	
	private Integer age;	// 用户年龄

	@Size(min = 1, max = 16)
	@Column(columnDefinition="character varying(16)")
	private String gender;	// 用户性别
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String phoneNumber;	// 用户电话
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String empNo;	// 工号
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String emailAddress;	// 用户邮箱

	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date registedDate;	// 注册日期
	
    @NotNull
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = Role.class)
	@JoinTable(name = "ec2_user_role_link",
				joinColumns = @JoinColumn(name = "user_id"), 
				inverseJoinColumns = @JoinColumn(name = "role_id")
			)
	private Set<Role> roles = new HashSet<Role>();	// 用户角色
	
    @NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Department.class)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;	//所属部门
    
    @NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	//所属公司
	
	public User() {	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmpNo() {
		return empNo;
	}

	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Date getRegistedDate() {
		return registedDate;
	}

	public void setRegistedDate(Date registedDate) {
		this.registedDate = registedDate;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getMigrateCsr(){
		if(realName == null) {
			return empNo +"-  -"+ department;
		}
		return empNo+"-"+realName+"-"+department;
	}
	
	/** 如果没有部门，则显示员工工号和员工姓名 */
	public String getMigrateCsr1(){
		if(realName == null && department == null) {
			return empNo;
		}
		if(realName == null) {
			return empNo +"-  -"+ department;
		}
		if(department == null) {
			return empNo + "-"+realName;
		}
		return empNo+"-"+realName+"-"+department;
	}
	
	@Override
	public String toString() {
		return username;
	}

	public String toLoggerString() {
		return "User [realName=" + realName + ", username=" + username
				+ ", department=" + department + ", domain=" + domain + "]";
	}
	
}
