package com.jiangyifen.ec2.fastagi;

import java.util.Date;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 监控并完善通道生命周期的信息
 * 	比如什么时候发起呼叫，什么时候振铃，什么时候建立通话等
 *
 * @author jrh
 *  2013-9-22
 */
public class SuperviseChannelLifeCycleInfo extends BaseAgiScript {
	
	public void service(AgiRequest request, AgiChannel agiChannel) throws AgiException {
		// 这里一定是主叫的信息
		String callerChannel = request.getChannel();
		String destlinenum = request.getParameter("destlinenum");		// 被叫号码
		
		ChannelLifeCycle channelLifeCycle = ShareData.channelToChannelLifeCycle.get(callerChannel);
		if(channelLifeCycle == null) {
			channelLifeCycle = new ChannelLifeCycle(callerChannel);
			channelLifeCycle.setSelfUniqueid(request.getUniqueId());
			ShareData.channelToChannelLifeCycle.put(callerChannel, channelLifeCycle);
		}

		channelLifeCycle.setDestlinenum(destlinenum);
		channelLifeCycle.setOriginateDialTime(new Date());	// 这样设置时间，存在一个隐患，如果服务器时间设置的不同步，将会出现问题
	}
	
}