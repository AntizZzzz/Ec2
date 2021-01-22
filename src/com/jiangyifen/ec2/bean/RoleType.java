package com.jiangyifen.ec2.bean;

public enum RoleType {
	csr("座席", 0), manager("主管", 1), admin("高级管理员", 2);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private RoleType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (RoleType rt : RoleType.values()) {
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
