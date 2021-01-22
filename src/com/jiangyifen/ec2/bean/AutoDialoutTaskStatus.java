package com.jiangyifen.ec2.bean;

public enum AutoDialoutTaskStatus {
	NEW("新建", 0), RUNNING("进行中", 1), PAUSE("暂停",2),OVER("结束",3);
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private AutoDialoutTaskStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (AutoDialoutTaskStatus mps : AutoDialoutTaskStatus.values()) {
			if (mps.getIndex() == index) {
				return mps.name;
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