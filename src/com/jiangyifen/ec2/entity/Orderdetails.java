package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_orderdetails")
public class Orderdetails { //订单详情
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderdetails")
	@SequenceGenerator(name = "orderdetails", sequenceName = "seq_ec2_orderdetails_id", allocationSize = 1)
	private Long id; // 商品Id

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String commodityName;	// 商品名称

	private Integer orderNum;	//订购数量

	private Double salePrice; 	// 商品售出价
	
	private Double subTotalPrice; 	// 价格小计
	
	//=================对应关系=================//
	@ManyToOne(targetEntity = Order.class,fetch=FetchType.LAZY) 
	private Order order; //多个详单对应一个订单
	
	@OneToOne(targetEntity = Commodity.class,fetch=FetchType.LAZY)
	private Commodity commodity; //详单对应商品

	
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

	public String getCommodityName() {
		return commodityName;
	}

	public void setCommodityName(String commodityName) {
		this.commodityName = commodityName;
	}

	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public Double getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(Double salePrice) {
		this.salePrice = salePrice;
	}

	public Double getSubTotalPrice() {
		return subTotalPrice;
	}

	public void setSubTotalPrice(Double subTotalPrice) {
		this.subTotalPrice = subTotalPrice;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Commodity getCommodity() {
		return commodity;
	}

	public void setCommodity(Commodity commodity) {
		this.commodity = commodity;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
