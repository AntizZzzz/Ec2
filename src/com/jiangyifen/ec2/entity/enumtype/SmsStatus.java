package com.jiangyifen.ec2.entity.enumtype;


public enum SmsStatus {
	SUCCESS("发送成功", 0),UNKNOWN("尚未收到回执", 1),FAILED("发送失败", 2);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private SmsStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (SmsStatus smsStatus : SmsStatus.values()) {
			if (smsStatus.getIndex() == index) {
				return smsStatus.name;
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

}