package com.jiangyifen.ec2.bean;

public enum CdrDirection {
	//CDR中的呼叫方向
	e2e("内部", 0), e2o("呼出", 1),o2e("呼入",2),o2o("外转外",3);
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private CdrDirection(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (CdrDirection keyword: CdrDirection.values()) {
			if (keyword.getIndex() == index) {
				return keyword.name;
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