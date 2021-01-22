package com.jiangyifen.ec2.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

//对界面权限控制
@Entity
@Table(name = "ec2_businessmodel", uniqueConstraints={@UniqueConstraint(columnNames={"application", "module", "function"})})
public class BusinessModel {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "business_model")
	@SequenceGenerator(name = "business_model", sequenceName = "seq_ec2_business_model_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String application;	// 其实就是对应一种角色的类型，目前类型只有三种 admin、manager、csr
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String module;	// 模块
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String function;	// 功能
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String description;	// 描述信息，用于指定个字段的中文名称
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = BusinessModel.class)
	private BusinessModel parent; // 上级部门
	
	@OneToMany(fetch=FetchType.LAZY,mappedBy="parent",targetEntity = BusinessModel.class)
	private Set<BusinessModel> childs; //子模块
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}
	
	public String getModule() {
		return module;
	}
	
	public void setModule(String module) {
		this.module = module;
	}
	
	public String getFunction() {
		return function;
	}
	
	public void setFunction(String function) {
		this.function = function;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BusinessModel getParent() {
		return parent;
	}

	public void setParent(BusinessModel parent) {
		this.parent = parent;
	}
	
	public Set<BusinessModel> getChilds() {
		return childs;
	}

	public void setChilds(Set<BusinessModel> childs) {
		this.childs = childs;
	}

	@Transient
	public boolean hasFunction() {
		if(function != null) {
			return true;
		}
		return false;
	}
	
	@Transient
	public boolean hasModule() {
		if(module != null) {
			return true;
		}
		return false;
	}

	@Override
	public String toString(){
		if(function != null) {
			return module+"&"+function;
		}
		return module;
	}

}
