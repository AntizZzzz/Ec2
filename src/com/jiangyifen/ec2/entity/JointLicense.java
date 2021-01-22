package com.jiangyifen.ec2.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

/**
 * @Description 描述：对接许可证，只有拥有许可的第三方系统，方可调用呼叫中心系统的接口
 *
 * @author  JRH
 * @date    2014年8月7日 下午3:31:47
 */
@Entity
@Table(name = "ec2_joint_license")
public class JointLicense implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "joint_license")
	@SequenceGenerator(name = "joint_license", sequenceName = "seq_ec2_joint_license_id", allocationSize = 1)
	private Long id;

	@Size(min = 0, max = 255)
	@Column(nullable = false, unique = true,columnDefinition="character varying(255) NOT NULL unique")
	private String accessId;	// 访问编号

	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String accessKey;	// 访问密钥
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String description;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;

	@Column(columnDefinition="bigint")
	private Long domainId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "JointLicense [id=" + id + ", accessId=" + accessId + ", accessKey=" + accessKey + ", description=" + description + ", createDate=" + createDate + ", domainId=" + domainId + "]";
	}

}
