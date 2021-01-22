package com.jiangyifen.ec2.bean;

import java.io.Serializable;

/**
 * @Description 描述：记录呼入、或者群呼时使用的外线信息，已经客户电话信息
 *
 * @author  JRH
 * @date    2014年7月11日 下午7:53:10
 * @version v1.0.0
 */
public class IncomingDialInfo implements Serializable {

	private static final long serialVersionUID = 6190195609596972915L;

	private String callerNumber;		// 主叫号码，客户的手机号
	
	private String vasOutline;			// 被叫外线号码
	
	private String dialType;			// 呼叫类型【呼入、自动外呼】，incoming, autodial
	
	private boolean isAutoDial;			// 是否为自动外呼
	
	private Long taskId;				// 任务编号【只有是自动外呼时，才有值】
	
	private Long projectId;				// 项目编号【只有是自动外呼时，才有值】

	public String getCallerNumber() {
		return callerNumber;
	}

	public void setCallerNumber(String callerNumber) {
		this.callerNumber = callerNumber;
	}

	public String getVasOutline() {
		return vasOutline;
	}

	public void setVasOutline(String vasOutline) {
		this.vasOutline = vasOutline;
	}

	public String getDialType() {
		return dialType;
	}

	public void setDialType(String dialType) {
		this.dialType = dialType;
	}

	public boolean isAutoDial() {
		return isAutoDial;
	}

	public void setAutoDial(boolean isAutoDial) {
		this.isAutoDial = isAutoDial;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@Override
	public String toString() {
		return "IncomingDialInfo [callerNumber=" + callerNumber + ", vasOutline=" + vasOutline + ", dialType=" + dialType + ", isAutoDial=" + isAutoDial + ", taskId=" + taskId + ", projectId=" + projectId + "]";
	}

	
}
