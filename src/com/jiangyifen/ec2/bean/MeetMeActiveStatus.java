package com.jiangyifen.ec2.bean;

/**
 * @author chb
 *
 */
public class MeetMeActiveStatus {
	//会议室号码
	private String confNum;
	//会议室人数
	private String parties;
	//会议室活跃时间
	private String activity;
	
	public String getConfNum() {
		return confNum;
	}
	public void setConfNum(String confNum) {
		this.confNum = confNum;
	}
	public String getParties() {
		return parties;
	}
	public void setParties(String parties) {
		this.parties = parties;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}

	
}