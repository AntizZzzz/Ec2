package com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo;

import com.jiangyifen.ec2.entity.MemoryMethod;

/**
 * 主要用来接收内存数据的传输，以方便执行查询操作
 * @author JHT
 */
public class MemoryParameter {

	private MemoryMethod memoryMethod;						// 内存方法对象
	private String showType;								// 显示方式
	private String parameter;								// 输入的参数
	
	public String getShowType() {
		return showType;
	}
	public void setShowType(String showType) {
		this.showType = showType;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public MemoryMethod getMemoryMethod() {
		return memoryMethod;
	}
	public void setMemoryMethod(MemoryMethod memoryMethod) {
		this.memoryMethod = memoryMethod;
	}
	
	@Override
	public String toString() {
		return "MemoryParameter [memoryMethodVo=" + memoryMethod + ", showType=" + showType + ", parameter=" + parameter + "]";
	}
	
	
}
