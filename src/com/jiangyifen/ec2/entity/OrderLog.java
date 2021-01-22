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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_order_log")
public class OrderLog { // 订单日志

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_log")
	@SequenceGenerator(name = "order_log", sequenceName = "ec2_dst_order_log_id", allocationSize = 1)
	private Long id; // 订单日志Id

	//============操作人信息===============//
	private Long userId;  //用户的Id值
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String userName; // 操作订单的人用户名

	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String realName; // 操作订单的人真实姓名
	
	//============操作时间===============//
	// 什么时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date operateDate; // 操作时间

	//============操作的订单===============//
	private Long orderId;

	// 改变订单状态的操作
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String action;

	//=================对应关系=================//
	//域的概念 批次n-->1域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
		
	// ================= getter 和 setter 方法=================//
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Date getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(Date operateDate) {
		this.operateDate = operateDate;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
