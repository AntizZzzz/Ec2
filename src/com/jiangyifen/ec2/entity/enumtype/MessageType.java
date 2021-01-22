package com.jiangyifen.ec2.entity.enumtype;

/**
 * 消息发送后的状态
 * @author jrh
 *  2013-7-4
 */
public enum MessageType {
	SUCCESS("发送成功", 0),UNKNOWN("尚未收到回执", 1),FAILED("发送失败", 2);
//	SEND("已发送", 0), RECEIVED("接收" ,1), DRAFT("草稿", 2);
	
	private String name;
	private int index;
	
	private MessageType(String name, int index){
		this.name = name;
		this.index = index;
	}
	
	// 普通方法
	public static String getName(int index) {
		for (MessageType roleType : MessageType.values()) {
			if (roleType.getIndex() == index) {
				return roleType.name;
			}
		}
		return null;
	}

	//======================  Setter and Getter ==============================//
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
