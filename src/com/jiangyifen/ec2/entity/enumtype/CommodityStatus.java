package com.jiangyifen.ec2.entity.enumtype;

/**
 * 商品的状态信息
 * @author Administrator
 *
 */
public enum CommodityStatus {
	ONSALE("正在销售", 0),UNDERCARRIAGE("下架", 1);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private CommodityStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (CommodityStatus commodityStatus : CommodityStatus.values()) {
			if (commodityStatus.getIndex() == index) {
				return commodityStatus.name;
			}
		}
		return null;
	}

	// ================= getter 和 setter 方法=================//
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}
