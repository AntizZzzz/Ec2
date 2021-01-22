package com.jiangyifen.ec2.entity.enumtype;

/**
 * 批次启用和停用等状态信息
 * @author chb
 *
 */
public enum BatchStatus {
	USEABLE("可用", 0),UNUSEABLE("不可用", 1);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private BatchStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (BatchStatus batchStatus : BatchStatus.values()) {
			if (batchStatus.getIndex() == index) {
				return batchStatus.name;
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
