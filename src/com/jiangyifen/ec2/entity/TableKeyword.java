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
@Table(name = "ec2_tablekeyword")
public class TableKeyword {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tablekeyword")
	@SequenceGenerator(name = "tablekeyword", sequenceName = "seq_ec2_tablekeyword_id", allocationSize = 1)
	private Long id;
	
	/*
	 * 识别Excel表格的列名
	 */
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64)")
	private String columnName;
	
	/*
	 * 识别Excel表格的列名的描述信息
	 */
	/*
	 * 识别Excel表格的列名
	 */
	@Size(min = 0, max = 256)
	@Column(columnDefinition="character varying(256)")
	private String description;
	
	//=================对应关系=================//
	//域的概念 批次n-->1域
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

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
