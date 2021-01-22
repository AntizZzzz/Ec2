package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 内存数据信息实体类
 * 
 * @author JHT
 */
@Entity
@Table(name = "ec2_memory_method")
public class MemoryMethod {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memory_method")
	@SequenceGenerator(name = "memory_method", sequenceName = "seq_ec2_memory_method_id", allocationSize = 1)
	private Long id;
	
	/**
	 * 存放数据的Map的名称(现在主要在ShareData里面)
	 */
	@Column
	private String name;
	
	/**
	 * Map类型(例如：Map<String,Long>)
	 */
	@Column
	private String type;
	
	/**
	 * 方法需要传入的参数(也就是Map里面的Key)
	 */
	@Column
	private String parameter;
	
	/**
	 * 查询数据的方法名称
	 */
	@Column
	private String methodName;
	
	/**
	 * 方法名字的缩写
	 */
	@Column
	private String abbrName;
	
	/**
	 * 显示key时，前面的提示信息
	 */
	@Column
	private String keyPrompt;
	
	/**
	 * 显示value时，前面的提示信息
	 */
	@Column
	private String valuePrompt;
	
	/**
	 * 单选时的描述信息
	 */
	@Column
	private String singlePrompt;
	
	/**
	 * 介绍信息
	 */
	@Column
	private String description;
	
	/**
	 * 简单的示例(输入参数时的提示信息)
	 */
	@Column
	private String simpleDemo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/** 存放数据的Map的名称(现在主要在ShareData里面) */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** Map类型(例如：Map<String,Long>) */
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/** 方法需要传入的参数(也就是Map里面的Key) */
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	/** 查询数据的方法名称 */
	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/** 显示key时，前面的提示信息 */
	public String getKeyPrompt() {
		return keyPrompt;
	}

	public void setKeyPrompt(String keyPrompt) {
		this.keyPrompt = keyPrompt;
	}

	/** 显示value时，前面的提示信息 */
	public String getValuePrompt() {
		return valuePrompt;
	}

	public void setValuePrompt(String valuePrompt) {
		this.valuePrompt = valuePrompt;
	}

	/** 介绍信息 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/** 简单的示例(输入参数时的提示信息) */
	public String getSimpleDemo() {
		return simpleDemo;
	}

	/**
	 * 方法名字的缩写
	 */
	public String getAbbrName() {
		return abbrName;
	}

	public void setAbbrName(String abbrName) {
		this.abbrName = abbrName;
	}

	public void setSimpleDemo(String simpleDemo) {
		this.simpleDemo = simpleDemo;
	}

	/** 单选时的描述信息 */
	public String getSinglePrompt() {
		return singlePrompt;
	}

	public void setSinglePrompt(String singlePrompt) {
		this.singlePrompt = singlePrompt;
	}

	
	@Override
	public String toString() {
		return "MemoryMethod [id=" + id + ", name=" + name + ", type=" + type 
				+ ", parameter=" + parameter + ", methodName=" + methodName 
				+ ", abbrName=" + abbrName + ", keyPrompt=" + keyPrompt 
				+ ", valuePrompt=" + valuePrompt + ", singlePrompt=" 
				+ singlePrompt + ", description=" + description 
				+ ", simpleDemo=" + simpleDemo + "]";
	}

	

}
