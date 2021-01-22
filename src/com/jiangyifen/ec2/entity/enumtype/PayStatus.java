package com.jiangyifen.ec2.entity.enumtype;

/**
 * 订单的状态信息
 */
public enum PayStatus {
	PAYED("已支付", 0),NOTPAYED("未支付", 1);

	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private PayStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (PayStatus payStatus : PayStatus.values()) {
			if (payStatus .getIndex() == index) {
				return payStatus .name;
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