package com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo;

/**
 * 显示内存数据大小的VO类
 * @author JHT
 * @date 2014-10-16 下午5:09:01
 */
public class MemoryMethodCount {

	private String name;				// 内存名称
	private String type;				// 内存类型
	private int count;					// 内存大小
	private String sunName;				// 子名称(Map<Long, List<String>，这里的字名称就是指List<String>)
	private String sunCount;			// 子大小
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String getSunName() {
		return sunName;
	}
	public void setSunName(String sunName) {
		this.sunName = sunName;
	}
	public String getSunCount() {
		return sunCount;
	}
	public void setSunCount(String sunCount) {
		this.sunCount = sunCount;
	}
	
}
