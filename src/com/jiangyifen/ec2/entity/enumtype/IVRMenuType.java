package com.jiangyifen.ec2.entity.enumtype;

/**
 * 
 * @Description 描述：IVRMenuType 的类型定义
 * 
 * @author  jrh
 * @date    2014年2月24日 上午9:46:38
 * @version v1.0.0
 */
public enum IVRMenuType { 
	
	customize("用户自定义IVR", 0),template("模板IVR", 1);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private IVRMenuType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (IVRMenuType actionType : IVRMenuType.values()) {
			if (actionType.getIndex() == index) {
				return actionType.name;
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
