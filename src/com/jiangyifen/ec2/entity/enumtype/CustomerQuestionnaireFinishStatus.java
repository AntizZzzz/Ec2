package com.jiangyifen.ec2.entity.enumtype;

/**
 * 客户完成问卷的状态
 * @author jrh
 *
 */
public enum CustomerQuestionnaireFinishStatus {
	
	UN_STARTED("尚未开始", 0), PART_FINISHED("部分完成", 1), FINISHED("已完成", 2);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private CustomerQuestionnaireFinishStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (CustomerQuestionnaireFinishStatus cqfs : CustomerQuestionnaireFinishStatus.values()) {
			if (cqfs.getIndex() == index) {
				return cqfs.name;
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
