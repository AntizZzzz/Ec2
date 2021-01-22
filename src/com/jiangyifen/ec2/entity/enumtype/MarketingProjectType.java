package com.jiangyifen.ec2.entity.enumtype;

/**
 * 项目类型
 * @author jrh
 *
 */
public enum MarketingProjectType {
	
	MARKETING("营销", 0), QUESTIONNAIRE("问卷", 1), CALL_BACK("回访", 2);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private MarketingProjectType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (MarketingProjectType rt : MarketingProjectType.values()) {
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
