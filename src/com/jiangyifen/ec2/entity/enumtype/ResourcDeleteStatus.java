package com.jiangyifen.ec2.entity.enumtype;

/**
 * 执行删除状态
 * @author LXY
 *
 */
public enum ResourcDeleteStatus {
	
	NO_NULL("无删除任务", 0),START_EXECUTE("开始执行", 1),COMPLETE_SUCCESS_EXECITE("上次执行完成-成功", 2),COMPLETE_ERROR_EXECITE("上次执行完成-失败", 3);
	
	// 成员变量
	private String name;
	private int index;

	// 构造方法
	private ResourcDeleteStatus(String name, int index) {
		this.name = name;
		this.index = index;
	}

	// 普通方法
	public static String getName(int index) {
		for (ResourcDeleteStatus batchStatus : ResourcDeleteStatus.values()) {
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
