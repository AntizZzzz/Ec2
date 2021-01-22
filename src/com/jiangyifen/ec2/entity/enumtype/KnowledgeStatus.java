package com.jiangyifen.ec2.entity.enumtype;

/**
 * 知识发布状态信息
 * @author Administrator
 *
 */
public enum KnowledgeStatus {
	PUBLISHED("已发布", 0),UNPUBLISHED("未发布", 1);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private KnowledgeStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (KnowledgeStatus knowledgeStatus : KnowledgeStatus.values()) {
			if (knowledgeStatus.getIndex() == index) {
				return knowledgeStatus.name;
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
