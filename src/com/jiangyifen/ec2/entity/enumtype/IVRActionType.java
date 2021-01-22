package com.jiangyifen.ec2.entity.enumtype;

/**
 * 
 * @Description 描述：IVRAction 的类型定义
 * 
 * @author  jrh
 * @date    2014年2月19日 下午8:57:54
 * @version v1.0.0
 */
public enum IVRActionType { 
	
	toExten("呼叫分机", 0),toQueue("呼入队列", 1),toMobile("转呼手机", 2),
	toPlayback("只播语音", 3),toRead("播语音，待按键", 4),toReadForAgi("播语音，特殊处理", 5),
	toVoicemail("语音留言", 9);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private IVRActionType(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (IVRActionType actionType : IVRActionType.values()) {
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
