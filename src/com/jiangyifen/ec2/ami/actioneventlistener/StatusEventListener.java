package com.jiangyifen.ec2.ami.actioneventlistener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.StatusCompleteEvent;
import org.asteriskjava.manager.event.StatusEvent;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 注意该Listener 监听的Event 是串行执行的
 * 	即：只有一个分机的NewStateEvent 执行完后，才执行下一个分机的NewStateEvent 
 *
 */
public class StatusEventListener extends AbstractManagerEventListener {
	
	// 为防止连接中断，不能发送Action，所以记录一个上次监听到的StatusCompleteEvent事件的时间
	private Long lastStatusCompleteEventReceiveTime = 0L;
	//存储一组StatusEventMap，用来进行定时比对
	private Map<String,StatusEvent> statusEventMap=new ConcurrentHashMap<String, StatusEvent>();
	
	/**
	 * 监听StatusAction触发的事件<p>
	 * 先存储在statusEventMap中，直到StatusCompleteEvent发生时进行处理
	 */
	@Override
	protected void handleEvent(StatusEvent event) {
		String channel = event.getChannel();
		statusEventMap.put(channel, event); 
	}
	
	/**
	 * 监听StatusAction触发的事件，起到校验 ShareData.channelAndChannelSession的作用,只是起到了校验的作用,并没有newStatusEvent中接收到的状态准确
	 */
	@Override
	protected void handleEvent(StatusCompleteEvent event) {
		ShareData.statusEvents=new ArrayList<StatusEvent>(statusEventMap.values());
		
		//重新创建新的Map，将原来的清空，以提高在处理的过程中处理的准确性，此Map只是在同一线程不用Concurrent
		Map<String,StatusEvent> statusEventMapClone=new HashMap<String, StatusEvent>(statusEventMap);
		statusEventMap.clear();
		
		//将新的Event放入到旧的channelAndChannelSession中
		for(String newChannel:statusEventMapClone.keySet()){
			StatusEvent statusEvent=statusEventMapClone.get(newChannel);
			ChannelSession channelSession=ShareData.channelAndChannelSession.get(newChannel);
			if(channelSession==null){
				channelSession=new ChannelSession();
				ShareData.channelAndChannelSession.put(newChannel, channelSession);
			}
			channelSession.setBridgedChannel(statusEvent.getBridgedChannel());
			channelSession.setCallerIdNum(statusEvent.getCallerIdNum());
			channelSession.setStatus(statusEvent.getChannelStateDesc());
			channelSession.setSeconds(statusEvent.getSeconds());
			channelSession.setChannelUniqueId(statusEvent.getUniqueId());
		}
		
		//从新旧总体中将新的移除，计算（并不操作）多余的所有Channels
		Set<String> oldAndNewChannels = ShareData.channelAndChannelSession.keySet();
		Set<String> oldAndNewChannelsClone=new HashSet<String>(oldAndNewChannels);
		oldAndNewChannelsClone.removeAll(statusEventMapClone.keySet());
		Set<String> toRemoveChannels=oldAndNewChannelsClone;//new HashSet();

		//从新旧总体中移除多余的channels
		for(String toRemoveChannel:toRemoveChannels){
			ShareData.channelAndChannelSession.remove(toRemoveChannel);
		}
		
//		//此时获取里一直StatusEvent对象，用这组对象进行正确性比对
//		Set<String> statusChannelSet=new HashSet<String>(statusEventMapClone.keySet());
//		Set<String> leftStatusChannelSet=new HashSet<String>(statusChannelSet);
//		Set<String> newStatusChannelSet=new HashSet<String>(ShareData.channelAndChannelSession.keySet());
//		
//		//剩余StatusEvent多出来的Channel
//		leftStatusChannelSet.removeAll(newStatusChannelSet);
//		//剩余newStatusEvent多出来的Channel
//		newStatusChannelSet.removeAll(statusChannelSet);
//		
//		//对于StatusEvent多出来的Channel进行添加
//		for(String statusChannel:leftStatusChannelSet){
//			ChannelSession channelSession=new ChannelSession();
//			channelSession.setBridgedChannel(statusEventMapClone.get(statusChannel).getBridgedChannel());
//			channelSession.setCallerIdNum(statusEventMapClone.get(statusChannel).getCallerIdNum());
//			channelSession.setStatus(statusEventMapClone.get(statusChannel).getChannelStateDesc());
//			ShareData.channelAndChannelSession.put(statusChannel, channelSession);
//		}
//		//对于newStatusEvent多出来的Channel进行移除
//		for(String newStatusChannel:newStatusChannelSet){
//			ShareData.channelAndChannelSession.remove(newStatusChannel);
//		}
		
		// 记录监听到事件的时间
		lastStatusCompleteEventReceiveTime = System.currentTimeMillis();
	}

	
	

	// //////////////////// Get and Set ///////////////////////////////////
	public Long getLastStatusCompleteEventReceiveTime() {
		return lastStatusCompleteEventReceiveTime;
	}

	public void setLastStatusCompleteEventReceiveTime(
			Long lastStatusCompleteEventReceiveTime) {
		this.lastStatusCompleteEventReceiveTime = lastStatusCompleteEventReceiveTime;
	}
}
