package com.jiangyifen.ec2.entity;

public enum TableKeywordDefault {
	//姓名、性别、生日、电话为必含字段，其余可选
	NAME("姓名", 0), SEX("性别", 1), BIRTHDAY("生日",2),COMPANY("公司",4),PHONE("电话",3),ADDRESS("地址",5);
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private TableKeywordDefault(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (TableKeywordDefault keyword: TableKeywordDefault.values()) {
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