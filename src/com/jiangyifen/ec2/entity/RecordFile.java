package com.jiangyifen.ec2.entity;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.utils.Config;

/**
 * 2013-06-13
 * 		录音文件实体
 * @author jrh
 *
 */
@Entity
@Table(name = "ec2_record_file")
public class RecordFile {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "record_file")
	@SequenceGenerator(name = "record_file", sequenceName = "seq_ec2_record_file_id", allocationSize = 1)
	private Long id;

	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long userId;

	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long customerId;
	
	@Size(min = 0,max = 512)
	@Column(columnDefinition="character varying(512) NOT NULL", nullable=false)
	private String downloadPath;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;
	
	@Size(min = 0, max = 1024)
	@Column(columnDefinition="character varying(1024)")
	private String description;
	
	// 跟客户做的文件对象的关联关系
	@ManyToMany(mappedBy = "recordFiles", fetch = FetchType.LAZY, targetEntity = CustomerQuestionnaire.class)
	private Set<CustomerQuestionnaire> customerQuestionnaires = new HashSet<CustomerQuestionnaire>();

	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	/**
	 * 获取下来录音的http 路径
	 * @return
	 */
	public String getDownloadPath() {
		return Config.props.getProperty(Config.REC_DIR_PATH)+downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<CustomerQuestionnaire> getCustomerQuestionnaires() {
		return customerQuestionnaires;
	}

	public void setCustomerQuestionnaires(
			Set<CustomerQuestionnaire> customerQuestionnaires) {
		this.customerQuestionnaires = customerQuestionnaires;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	/**
	 * 获取录音文件的原始地址：数据库中对应的地址
	 * lxy
	 * @return
	 */
	public String findOriginalDownloadPath(){
		return downloadPath;
	}
}
