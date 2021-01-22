package com.jiangyifen.ec2.bean;

public enum DayOfWeek {
	sun("星期日", 1), mon("星期一", 2), tue("星期二", 3), wen("星期三", 4), thu("星期四", 5), fri("星期五", 6), sat("星期六", 7);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private DayOfWeek(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (DayOfWeek rt : DayOfWeek.values()) {
			if (rt.getIndex() == index) {
				return rt.name;
			}
		}
		return null;
	}

	// get set 方法
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
		return name;
	}
	
}
