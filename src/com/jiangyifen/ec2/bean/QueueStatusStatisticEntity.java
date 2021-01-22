package com.jiangyifen.ec2.bean;

public class QueueStatusStatisticEntity {
	
	private String queueName;
	public static final String QUEUENAME_HEADER ="队列名";
	
	private Integer inQueueCount;
	public static final String INQUEUECOUNT_HEADER ="排队中";

	private Long maxWaitLen;
	public static final String MAXWAITLEN_HEADER = "最大排队人数";
	
	private Integer callingCount;
	public static final String CALLINGCOUNT_HEADER ="通话中";
	
	private Integer memberAmount;
	public static final String MEMBERAMOUT_HEADER = "成员总数";
	
	private Integer pauseCount;
	public static final String PAUSECOUNT_HEADER = "置忙人数";
	
	private Integer loginAmount;
	public static final String LOGINAMOUNT_HEADER = "在线人数";
	
	
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Integer getInQueueCount() {
		return inQueueCount;
	}

	public void setInQueueCount(Integer inQueueCount) {
		this.inQueueCount = inQueueCount;
	}

	public Long getMaxWaitLen() {
		return maxWaitLen;
	}

	public void setMaxWaitLen(Long maxWaitLen) {
		this.maxWaitLen = maxWaitLen;
	}

	public Integer getCallingCount() {
		return callingCount;
	}

	public void setCallingCount(Integer callingCount) {
		this.callingCount = callingCount;
	}

	public Integer getMemberAmount() {
		return memberAmount;
	}

	public void setMemberAmount(Integer memberAmount) {
		this.memberAmount = memberAmount;
	}

	public Integer getPauseCount() {
		return pauseCount;
	}

	public void setPauseCount(Integer pauseCount) {
		this.pauseCount = pauseCount;
	}

	public Integer getLoginAmount() {
		return loginAmount;
	}

	public void setLoginAmount(Integer loginAmount) {
		this.loginAmount = loginAmount;
	}

	@Override
	public String toString() {
		return "QueueStatusStatisticEntity [queueName=" + queueName
				+ ", inQueueCount=" + inQueueCount + ", maxWaitLen="
				+ maxWaitLen + ", callingCount=" + callingCount
				+ ", memberAmount=" + memberAmount + ", pauseCount="
				+ pauseCount + ", loginAmount=" + loginAmount + "]";
	}

}
