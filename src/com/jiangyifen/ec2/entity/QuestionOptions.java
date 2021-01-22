package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@Table(name = "ec2_question_options")
public class QuestionOptions {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_question_options")
	@SequenceGenerator(name = "ec2_question_options", sequenceName = "seq_ec2_question_options_id", allocationSize = 1)
	private Long id; 
	
	@Column
 	private String name;
	
	@Column
	private int ordernumber;
	
	@Column
	@Size(min = 0, max = 10)
	private String type;
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Question.class)//cascade=CascadeType.REMOVE
	private Question question;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Question.class)
	private Question skip;

	/**主键 */
	public Long getId() {
		return id;
	}

	/**主键 */
	public void setId(Long id) {
		this.id = id;
	}

	/**问卷选项名称 */
	public String getName() {
		return name;
	}

	/**问卷名称 */
	public void setName(String name) {
		this.name = name;
	}

	/**创建时间 */
	public Date getCreateTime() {
		return createTime;
	}

	/**创建时间 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**所属问题 */
	public Question getQuestion() {
		return question;
	}

	/**所属问题 */
	public void setQuestion(Question question) {
		this.question = question;
	}
	
	/**选项跳转 */
	public Question getSkip() {
		return skip;
	}
	
	/**选项跳转 */
	public void setSkip(Question skip) {
		this.skip = skip;
	}
	
	/**选项排序 */
	public int getOrdernumber() {
		return ordernumber;
	}
	
	/**选项排序 */
	public void setOrdernumber(int ordernumber) {
		this.ordernumber = ordernumber;
	}

	/**
	 * 选项类型 
	 * TT Text true
	 * TF Text false
	 * 
	 * */
	public String getType() {
		return type;
	}
	
	/**选项类型 */
	public void setType(String type) {
		this.type = type;
	}
	
}
