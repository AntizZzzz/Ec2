package com.jiangyifen.ec2.bean;

public enum SipConfigType {
	exten("分机", 0), sip_outline("SIP外线", 1), gateway_outline("网关外线", 2);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private SipConfigType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (SipConfigType rt : SipConfigType.values()) {
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
