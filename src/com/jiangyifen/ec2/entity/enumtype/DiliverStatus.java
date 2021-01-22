package com.jiangyifen.ec2.entity.enumtype;

/**
 * 订单的状态信息
 */
public enum DiliverStatus {
	NOTDILIVERED("未发货", 0),DILIVERED("已发货", 1),RECEIVEED("已收货", 2);

	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private DiliverStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (DiliverStatus diliverStatus : DiliverStatus.values()) {
			if (diliverStatus .getIndex() == index) {
				return diliverStatus .name;
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