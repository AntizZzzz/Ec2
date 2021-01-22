package com.jiangyifen.ec2.entity.enumtype;

/**
 * 导入导出的状态管理
 * @author Administrator
 *
 */
public enum QcMgr {
	QUALIFIED("合格", 1),NOTQUALIFIED("不合格", 0);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private QcMgr(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (QcMgr qcMgr : QcMgr.values()) {
			if (qcMgr.getIndex() == index) {
				return qcMgr.name;
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
