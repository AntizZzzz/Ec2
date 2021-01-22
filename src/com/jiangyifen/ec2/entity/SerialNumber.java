package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_serial_number")
public class SerialNumber {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_serial_number")
	@SequenceGenerator(name = "ec2_serial_number", sequenceName = "seq_ec2_serial_number_id", allocationSize = 1)
	private Long id;

	/**
	 * 序列号格式：yyyy-MMdd-xxxx-xxxx (xxxx表示随机的四位数)
	 */
	@Size(min = 0, max = 255)
	@Column(columnDefinition = "character varying(255)")
	private String serialNumber;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}
