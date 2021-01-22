package com.jiangyifen.ec2.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity
@Table(name = "ec2_department")
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department")
	@SequenceGenerator(name = "department", sequenceName = "seq_ec2_department_id", allocationSize = 1)
	private Long id; // 部门ID

	@NotNull
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String name; // 部门名称

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String description; // 部门描述

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Department.class)
	private Department parent; // 上级部门

	@OneToMany(mappedBy = "department", fetch = FetchType.LAZY, targetEntity = User.class)
	private Set<User> users = new HashSet<User>(); // 本部职员

	@ManyToMany(mappedBy = "departments", fetch = FetchType.LAZY, targetEntity = Role.class)
	private Set<Role> roles = new HashSet<Role>(); // 本部所拥有的角色

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable = false)
	private Domain domain; // 所属公司

	public Department() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Department(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Department getParent() {
		return parent;
	}

	public void setParent(Department parent) {
		this.parent = parent;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
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
