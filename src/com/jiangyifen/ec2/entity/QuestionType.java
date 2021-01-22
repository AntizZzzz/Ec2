package com.jiangyifen.ec2.entity;

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

//TODO lxy	1305
/**
* 问卷调查
* @author lxy
*
*/
@Entity
@Table(name = "ec2_question_type")
public class QuestionType {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_question_type")
	@SequenceGenerator(name = "ec2_question_type", sequenceName = "seq_ec2_question_type_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 20)
	@Column(columnDefinition="character varying(20) NOT NULL unique", unique = true, nullable = false)
	private String name;
	
	@Size(min = 0, max = 20)
	@Column(columnDefinition="character varying(20)")
	private String value;
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;
	
	/**
	 * 	主键 1; "单选";"radio"
	 * 	2; "多选";"checkbox"
	 * 	3; "开放";"open"
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 	主键 1; "单选";"radio"
	 * 	2; "多选";"checkbox"
	 * 	3; "开放";"open"
	 * @return
	 */
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	 
	
}
