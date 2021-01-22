package com.jiangyifen.ec2.ami;

import java.util.HashSet;
import java.util.Set;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
/**
 * 监听NewChannelEvent事件 
 * @author chb
 */
public class NewChannelEventListener extends AbstractManagerEventListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 监听NewChannelEvent触发的事件
	 */
	@Override
	protected void handleEvent(NewChannelEvent event) {
		String channel = event.getChannel();
		String sippeerName = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
		// 从peernamdAndChannel删数据
		
		Set<String> channelSet = ShareData.peernameAndChannels.get(sippeerName);
		if (channelSet == null) {
			channelSet=new HashSet<String>();
			ShareData.peernameAndChannels.put(sippeerName, channelSet);
		}
		channelSet.add(channel);

		loggerInfoForAsteriskChannelRemant(event);
		
//------------------------------------------------------------------------------------------------------------------
//		System.out.println();
//		System.out.println("--------------------------------");
//		System.out.println("New Channel Event ===>  "+event);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		System.out.println("New Channel Event channel--->  "+ channel);
//		System.out.println("New Channel Event event.getDateReceived()--->  "+ sdf.format(event.getDateReceived()));
//------------------------------------------------------------------------------------------------------------------
		
		// jrh 保存通道的生命周期
		if(!channel.startsWith("AsyncGoto/SIP/")) {	// 如果是转接，被转接通道会生成一个新的通道 如：AsyncGoto/SIP/88860847041-00000016
			ChannelLifeCycle channelLifeCycle = ShareData.channelToChannelLifeCycle.get(channel);
			if(channelLifeCycle == null) {
				channelLifeCycle = new ChannelLifeCycle(channel);
				channelLifeCycle.setSelfUniqueid(event.getUniqueId());
				ShareData.channelToChannelLifeCycle.put(channel, channelLifeCycle);
			}
			channelLifeCycle.setDownStateTime(event.getDateReceived());
		}
	}

	/**
	 * 用于定位asterisk channel 残留的问题
	 * @param event
	 */
	private void loggerInfoForAsteriskChannelRemant(NewChannelEvent event) {
		if(GlobalVariable.mac_asterisk_channel_remnant.equals(GlobalData.MAC_ADDRESS)) {
			StringBuffer strBf = new StringBuffer();
			strBf.append("jrh check channel remnant--> ");
			strBf.append("NewChannelEventListener - ");
			strBf.append("JrhAddByNewChannel : ");
			strBf.append(" channel=");
			strBf.append(event.getChannel());
			strBf.append(", calleridname=");
			strBf.append(event.getCallerIdName());
			strBf.append(", calleridnum=");
			strBf.append(event.getCallerIdNum());
			strBf.append(", exten=");
			strBf.append(event.getExten());
			strBf.append(", channelstatedesc=");
			strBf.append(event.getChannelStateDesc());
			strBf.append(", uniqueid=");
			strBf.append(event.getUniqueId());
			strBf.append(", context=");
			strBf.append(event.getContext());
			logger.warn(strBf.toString());
		}
	}
	
}
