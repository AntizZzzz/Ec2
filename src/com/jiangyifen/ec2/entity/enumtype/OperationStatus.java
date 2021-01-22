package com.jiangyifen.ec2.entity.enumtype;

/**
 * 导入导出的状态管理
 * @author Administrator
 *
 */
public enum OperationStatus {
	EXPORT("导出", 1),IMPORT("导入", 0);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private OperationStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (OperationStatus operationStatus : OperationStatus.values()) {
			if (operationStatus.getIndex() == index) {
				return operationStatus.name;
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
