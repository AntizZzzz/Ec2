package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * 电话号码归属地
 * @author chb
 */

@Entity
@Table(name = "ec2_mobileloc")
public class MobileLoc {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mobileloc")
	@SequenceGenerator(name = "mobileloc", sequenceName = "seq_ec2_mobileloc_id", allocationSize = 1)
	private long id; //归属地实体的ID
	
	/*
	 * 手机号码
	 */
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String mobileNumber;

	/*
	 * 手机归属地
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String mobileArea;
	
	/*
	 * 手机卡类型
	 */
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String mobileType;
	
	/*
	 * 区号
	 */
	@Size(min = 0, max = 16)
	@Column(columnDefinition="character varying(16)")
	private String areaCode;
	
	/*
	 * 邮编
	 */
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String postCode;

	/*=============== Getter and Setter ============= */
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getMobileArea() {
		return mobileArea;
	}

	public void setMobileArea(String mobileArea) {
		this.mobileArea = mobileArea;
	}

	public String getMobileType() {
		return mobileType;
	}

	public void setMobileType(String mobileType) {
		this.mobileType = mobileType;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	@Override
	public String toString() {
		return "MobileLoc [id=" + id + ", mobileNumber=" + mobileNumber
				+ ", mobileArea=" + mobileArea + ", mobileType=" + mobileType
				+ ", areaCode=" + areaCode + ", postCode=" + postCode + "]";
	}
	
	
}
