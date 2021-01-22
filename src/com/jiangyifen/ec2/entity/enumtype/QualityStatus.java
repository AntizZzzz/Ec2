package com.jiangyifen.ec2.entity.enumtype;

/**
 * 订单的状态信息
 */
public enum QualityStatus {
	CONFIRMING("待审核订单", 0),CONFIRMED("已审核订单", 1),FINISHED("已完成订单", 2),CANCELED("已取消订单", 3);

	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private QualityStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (QualityStatus qualityStatus : QualityStatus.values()) {
			if (qualityStatus.getIndex() == index) {
				return qualityStatus.name;
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