package com.jiangyifen.ec2.bean;

/**
 * 
 * @Description 描述：第一个进入会议室的人对应通道的信息
 * 
 * @author  jrh
 * @date    2013年12月23日 下午6:03:00
 * @version v1.0.0
 */
public class MeettingRoomFirstJoinMemberInfo {
	
	private String channel;
	
	private String uniqueId;
	
	private Long originatorId;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public Long getOriginatorId() {
		return originatorId;
	}

	public void setOriginatorId(Long originatorId) {
		this.originatorId = originatorId;
	}

	@Override
	public String toString() {
		return "MeettingRoomFirstJoinMemberInfo [channel=" + channel
				+ ", uniqueId=" + uniqueId + ", originatorId=" + originatorId
				+ "]";
	}
	
}
