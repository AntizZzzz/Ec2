package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.CommodityStatus;

@Entity
@Table(name = "ec2_commodity")
public class Commodity { //商品
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "commodity")
	@SequenceGenerator(name = "commodity", sequenceName = "seq_ec2_commodity_id", allocationSize = 1)
	private Long id; // 商品Id

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String commodityName;	// 商品名称
	
	@Size(min = 0, max = 512)
	@Column(columnDefinition="character varying(512)")
	private String description;	// 商品描述
	
	@Enumerated
	private CommodityStatus commodityStatus; // 商品是销售还是下架等状态信息
	
	private Double commodityPrice; //商品对应一个价格

	@Column(columnDefinition="bigint")
	private long stockQty;	// 商品库存 

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

	public String getCommodityName() {
		return commodityName;
	}

	public void setCommodityName(String commodityName) {
		this.commodityName = commodityName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CommodityStatus getCommodityStatus() {
		return commodityStatus;
	}

	public void setCommodityStatus(CommodityStatus commodityStatus) {
		this.commodityStatus = commodityStatus;
	}

	public Double getCommodityPrice() {
		return commodityPrice;
	}

	public void setCommodityPrice(Double commodityPrice) {
		this.commodityPrice = commodityPrice;
	}

	public long getStockQty() {
		return stockQty;
	}

	public void setStockQty(long stockQty) {
		this.stockQty = stockQty;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return "Commodity [id=" + id + ", commodityName=" + commodityName
				+ ", description=" + description + ", commodityStatus="
				+ commodityStatus + ", commodityPrice=" + commodityPrice
				+ ", stockQty=" + stockQty + ", domain=" + domain + "]";
	}

}
