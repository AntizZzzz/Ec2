package com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo;

/**
 * 数据表信息视图对象
 * @author JHT
 */
public class TableInfoVo implements Comparable<TableInfoVo>{
	private String relName;					// 数据表名称
	private long  dataNum = -1;				// 数据表大小 byte
	private long seqMax = 0;				// 当前序列的值
	private long tableCount = -1;				// 获取当前表的Count
	
	public String getRelName() {
		return relName;
	}
	public void setRelName(String relName) {
		this.relName = relName;
	}
	public long getDataNum() {
		return dataNum;
	}
	public void setDataNum(long dataNum) {
		this.dataNum = dataNum;
	}
	public long getSeqMax() {
		return seqMax;
	}
	public void setSeqMax(long seqMax) {
		this.seqMax = seqMax;
	}
	public long getTableCount() {
		return tableCount;
	}
	public void setTableCount(long tableCount) {
		this.tableCount = tableCount;
	}
	
	public TableInfoVo() {
		super();
	}
	
	public TableInfoVo(String relName, long dataNum, long seqMax, long tableCount) {
		super();
		this.relName = relName;
		this.dataNum = dataNum;
		this.seqMax = seqMax;
		this.tableCount = tableCount;
	}
	
	@Override
	public String toString() {
		return "TableInfoVo [relName=" + relName + ", dataNum=" + dataNum + ", seqMax=" + seqMax + ", tableCount=" + tableCount + "]";
	}
	
	@Override
	public int compareTo(TableInfoVo o) {
		TableInfoVo tbVo = o;
		return (this.seqMax < tbVo.getSeqMax() ? -1 :(this.seqMax == tbVo.getSeqMax() ? 0 : 1));
	}
	
}
