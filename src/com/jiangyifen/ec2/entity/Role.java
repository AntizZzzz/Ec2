package com.jiangyifen.ec2.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.RoleType;

@Entity
@Table(name = "ec2_role")
public class Role  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role")
	@SequenceGenerator(name = "role", sequenceName = "seq_ec2_role_id", allocationSize = 1)
	private Long id;
	
	@NotNull
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String name;	// 角色名称
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String description;	// 角色的描述信息
	
	//角色n-->1角色类型 
	@NotNull
	@Enumerated
	private RoleType type;	// 角色类型  admin manager csr
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable = false)
	private Domain domain;	//所属域
	
	@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY, targetEntity = User.class)
	private Set<User> users = new HashSet<User>();
	
	@ManyToMany(fetch = FetchType.EAGER, targetEntity = Department.class)
	@JoinTable(name = "ec2_role_department_link",
			joinColumns = @JoinColumn(name = "role_id"), 
			inverseJoinColumns = @JoinColumn(name = "department_id")
		)
	private Set<Department> departments = new HashSet<Department>();
	
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = BusinessModel.class)
	@JoinTable(name = "ec2_role_businessmodel_link",
				joinColumns = @JoinColumn(name = "role_id"), 
				inverseJoinColumns = @JoinColumn(name = "businessmodel_id")
			)
	private Set<BusinessModel> businessModels = new HashSet<BusinessModel>();	// 角色所拥有的功能模块

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

	public RoleType getType() {
		return type;
	}

	public void setType(RoleType type) {
		this.type = type;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	/**
	 * 不建议使用，获取的不是实时的数据库数据，要重启
	 * @return
	 */
	public Set<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(Set<Department> departments) {
		this.departments = departments;
	}

	public Set<BusinessModel> getBusinessModels() {
		return businessModels;
	}

	public void setBusinessModels(Set<BusinessModel> businessModels) {
		this.businessModels = businessModels;
	}
	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return name;
	}
}
