package com.jiangyifen.ec2.entity.enumtype;

/**
 * 电话号码更新方式配置
 * @author chb
 *
 */
public enum MobileLocUpdateIfaceType {
	BAIDU("百度", 0),WANGYI("网易", 1),SHOUJIZAIXIAN("手机在线",2),IP138("ip138",3);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private MobileLocUpdateIfaceType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (MobileLocUpdateIfaceType mobileLocUpdateIface : MobileLocUpdateIfaceType.values()) {
			if (mobileLocUpdateIface.getIndex() == index) {
				return mobileLocUpdateIface.name;
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
