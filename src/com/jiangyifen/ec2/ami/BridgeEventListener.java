package com.jiangyifen.ec2.ami;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.BridgeEvent;
import org.asteriskjava.manager.event.QueueEntryEvent;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.WuRuiShareData;
import com.jiangyifen.ec2.servlet.http.common.pojo.BridgeVo;
import com.jiangyifen.ec2.utils.ExternalInterface;
import com.jiangyifen.ec2.utils.HttpIfaceUtil;
import com.jiangyifen.ec2.utils.LoggerUtil;
/**
 * 监听BridgeEvent事件(Link 和 Unlink) 
 * 	
 * 	jrh 注意： 对于处理内部呼叫和外转外的情况之外，其他呼叫的情形，当两路通道建立通话后，每隔5分钟还会调用一次BridgeEvent事件，所以需要判断一下
 * @author chb
 */
public class BridgeEventListener extends AbstractManagerEventListener {
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	/**
	 * 监听Bridged事件<p>
	 * 自动外呼的Bridge和拨打分机和外线的Bridge
	 */
	@Override
	protected void handleEvent(BridgeEvent event) {
		if(BridgeEvent.BRIDGE_STATE_LINK.equals(event.getBridgeState())){
			String recoreFilename;

			//两个uniqueid
			String uniqueId1 = event.getUniqueId1();
			String uniqueId2 = event.getUniqueId2();
			String callerIdnum1=event.getCallerId1();
			String callerIdnum2=event.getCallerId2();  // 呼入时asterisk-java可能是有bug
			
			String recordfileName1=ShareData.recordFileName.get(uniqueId1);
			String recordfileName2=ShareData.recordFileName.get(uniqueId2);
			
			//两个Channel
			String channel1=event.getChannel1();
			String channel2=event.getChannel2();
			// jrh
			String caller1 = channel1.substring(channel1.indexOf("/")+1, channel1.indexOf("-"));
			String caller2 = channel2.substring(channel2.indexOf("/")+1, channel2.indexOf("-"));
			
//------------------------------------------------------------------------------------------------------------------
//			TODO jrh
//			System.err.println("-------------------------com.jiangyifen.ec2.ami.BridgeEventListener -------");
//			System.err.println(callerIdnum1 +"<-----------BridgeEvent -- ----------------> "+ callerIdnum2);
//			System.err.println("BridgeEvent----->"+event);
//			System.err.println();
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//			System.err.println("BridgeEvent -- new date--->"+sdf.format(new Date()));
//			System.err.println();
//			System.err.println("BridgeEvent -- event.getDateReceived()---->"+sdf.format(event.getDateReceived()));
//			System.err.println();
//------------------------------------------------------------------------------------------------------------------
			
			String dateStr=sdf.format(new Date());

			if(recordfileName1==null){
				// 这里如果呼叫的是内部分机, 会判断的不准确
				// if(caller1 != null && caller1.length() == 6) {	// 表示呼出
				if(caller1 != null && caller1.length() == 5) {	// TODO 武睿定制开发, 分机号改为五位数
					BridgeVo bridgeVo = new BridgeVo();
					bridgeVo.setDestination("outgoing");
					bridgeVo.setExten(caller1);
					bridgeVo.setOutline(caller2);
					bridgeVo.setPhoneNumber(callerIdnum2);
					bridgeVo.setCreateTime((new Date()).getTime());
					WuRuiShareData.csrToBridgeVoMap.put(caller1, bridgeVo);
				// } else if(caller1 != null && caller1.length() > 6) {
				} else if(caller1 != null && caller1.length() > 5) {	// TODO 武睿定制开发, 分机号改为五位数
					BridgeVo bridgeVo = new BridgeVo();
					bridgeVo.setDestination("incoming");
					bridgeVo.setExten(caller2);
					bridgeVo.setOutline(caller1);
					bridgeVo.setPhoneNumber(callerIdnum1);
					bridgeVo.setCreateTime((new Date()).getTime());
					WuRuiShareData.csrToBridgeVoMap.put(caller2, bridgeVo);
				}
				
				/*System.out.println("=====================================");
				System.out.println(caller1);
				System.out.println(caller2);
				System.out.println(uniqueId1);
				System.out.println(uniqueId2);
				System.out.println(callerIdnum1);
				System.out.println(callerIdnum2);
				System.out.println(recordfileName1);
				System.out.println(recordfileName2);
				System.out.println(channel1);
				System.out.println(channel2);
				System.out.println("=====================================");
				System.err.println("===========================recordfileName 11111111111 =========================");*/
				// jinht 是否开启被叫接起之后推送信息, 这里只传送一个主叫号码, 因为在呼入的时候, 抓取不到被叫的接起分机号码
				if("true".equals(ExternalInterface.LISTENER_BRIDGE_EVENT_IS_OPEN)) {
					final String calling = callerIdnum1;
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(2000);		// 延迟 2s 推送 CDR 话单信息
							} catch (Exception e) { }
							
							HttpIfaceUtil.doPostRequest(ExternalInterface.LISTENER_BRIDGE_EVENT_URL, "calling="+calling);
						}
					}).start();
				}
				//uniqueId2 为主叫
				recoreFilename=new StringBuilder().append(dateStr).append("-").append(callerIdnum2).append("-").append(callerIdnum1).append("-").append(uniqueId2).append(".wav").toString();
				ShareData.recordFileName.put(uniqueId2, recoreFilename);
			}else if(recordfileName2==null){ //说明是呼入
				System.err.println("===========================recordfileName 222222222 =========================");
				String extenNumber= event.getChannel2().substring(event.getChannel2().indexOf("/")+1, event.getChannel2().indexOf("-"));
				// if(extenNumber.length()==6){
				if(extenNumber.length()==5){	// 武睿定制开发, 分机号改为了五位数
					callerIdnum2=extenNumber;
				}
				//uniqueId1 为主叫
				recoreFilename=new StringBuilder().append(dateStr).append("-").append(callerIdnum1).append("-").append(callerIdnum2).append("-").append(uniqueId1).append(".wav").toString();
				ShareData.recordFileName.put(uniqueId1, recoreFilename);
			}else{
				LoggerUtil.logWarn(this, "录音文件没有初始化文件名！！！");
			}

			//******************** 在CDR中存储channel的Bridge时间 ****************************//
//			if(!ShareData.channelToBridgetime.containsKey(channel1)) {	// jrh 对于处理内部呼叫和外转外的情况之外，其他呼叫的情形，当两路通道建立通话后，每隔5分钟还会调用一次BridgeEvent事件，所以需要判断一下
//				BridgetimeInfo bridgetimeInfo1=new BridgetimeInfo(channel1, event.getDateReceived());
//				ShareData.channelToBridgetime.put(channel1, bridgetimeInfo1);
//			}
//			if(!ShareData.channelToBridgetime.containsKey(channel2)) {
//				BridgetimeInfo bridgetimeInfo2=new BridgetimeInfo(channel2, event.getDateReceived());
//				ShareData.channelToBridgetime.put(channel2, bridgetimeInfo2);
//			}
			
			// 更新通道的生命周期信息
			this.updateChannelLifeCycleInfo(event, uniqueId1, uniqueId2, channel1, channel2);

			//******************** 维护ChannelSession ****************************//
			//向ChannelSession1中存储信息
			ChannelSession channelSession1=ShareData.channelAndChannelSession.get(channel1);
			if(channelSession1==null){
				channelSession1=new ChannelSession();
				ShareData.channelAndChannelSession.put(channel1,channelSession1);
			}
			channelSession1.setBridgedChannel(channel2);
			channelSession1.setConnectedlinenum(callerIdnum2);
			channelSession1.setBridged(true);
			channelSession1.setChannelUniqueId(uniqueId1);
			channelSession1.setBridgedUniqueId(uniqueId2);
			
			// jrh
			Long csrId2 = ShareData.extenToUser.get(caller2);
			if(csrId2 != null); {
				channelSession1.setBridgedUserId(csrId2);
			}
	
			//向ChannelSession2中存储信息
			ChannelSession channelSession2=ShareData.channelAndChannelSession.get(channel2);
			if(channelSession2==null){
				channelSession2=new ChannelSession();
				ShareData.channelAndChannelSession.put(channel2,channelSession2);
			}
			channelSession2.setBridgedChannel(channel1);
			channelSession2.setConnectedlinenum(callerIdnum1);
			channelSession2.setBridged(true);
			channelSession2.setChannelUniqueId(uniqueId2);
			channelSession2.setBridgedUniqueId(uniqueId1);
			
			// jrh
			Long csrId1 = ShareData.extenToUser.get(caller1);
			if(csrId1 != null); {
				channelSession2.setBridgedUserId(csrId1);
			}
			
			// jrh 将以接通的人从排队队列中移除
			for(String queue : AutoDialHolder.queueToWaiters.keySet()) {
				List<QueueEntryEvent> waiters = AutoDialHolder.queueToWaiters.get(queue);
				if(waiters != null) {
					ArrayList<QueueEntryEvent> needRemovedWaiters = new ArrayList<QueueEntryEvent>();
					for(QueueEntryEvent waiter : waiters) {
						if(channel1.equals(waiter.getChannel()) || channel2.equals(waiter.getChannel())) {
							needRemovedWaiters.add(waiter);
						}
					}
					synchronized (waiters) {
						waiters.removeAll(needRemovedWaiters);
					}
				}
			}
		}
	}
	
	/**
	 * jrh 更新通道的生命周期信息
	 *   
	 * @param event			bridgeevent 
	 * @param uniqueId1		唯一标示
	 * @param uniqueId2
	 * @param channel1		建立通话的通道之一
	 * @param channel2
	 */
	private void updateChannelLifeCycleInfo(BridgeEvent event, String uniqueId1, 
			String uniqueId2, String channel1, String channel2) {
		if(!channel1.startsWith("AsyncGoto/SIP/")) {	// 如果是转接，被转接通道会生成一个新的通道 如：AsyncGoto/SIP/88860847041-00000016
			ChannelLifeCycle channelLifeCycle1 = ShareData.channelToChannelLifeCycle.get(channel1);
			if(channelLifeCycle1 == null) {
				channelLifeCycle1 = new ChannelLifeCycle(channel1);
				channelLifeCycle1.setBridgedChannel(channel2);
				channelLifeCycle1.setBridgedUniqueid(uniqueId2);
				channelLifeCycle1.setBridgedTime(event.getDateReceived());
				ShareData.channelToChannelLifeCycle.put(channel1, channelLifeCycle1);
			} else if(!channel2.equals(channelLifeCycle1.getBridgedChannel())) {	// 对于处理内部呼叫和外转外的情况之外，其他呼叫的情形，当两路通道建立通话后，每隔5分钟还会调用一次BridgeEvent事件，所以需要判断一下当前的联系对象是否发生变化
				channelLifeCycle1.setBridgedChannel(channel2);
				channelLifeCycle1.setBridgedUniqueid(uniqueId2);
				channelLifeCycle1.setBridgedTime(event.getDateReceived());
			}

			String connectedlinenum = channelLifeCycle1.getConnectedlinenum();
			if(connectedlinenum == null) {
				channelLifeCycle1.setConnectedlinenum(event.getCallerId2());
			}
		}
		
		if(!channel2.startsWith("AsyncGoto/SIP/")) {	// 如果是转接，被转接通道会生成一个新的通道 如：AsyncGoto/SIP/88860847041-00000016
			ChannelLifeCycle channelLifeCycle2 = ShareData.channelToChannelLifeCycle.get(channel2);
			if(channelLifeCycle2 == null) {
				channelLifeCycle2 = new ChannelLifeCycle(channel2);
				channelLifeCycle2.setBridgedChannel(channel1);
				channelLifeCycle2.setBridgedUniqueid(uniqueId1);
				channelLifeCycle2.setBridgedTime(event.getDateReceived());
				ShareData.channelToChannelLifeCycle.put(channel2, channelLifeCycle2);
			} else if(!channel1.equals(channelLifeCycle2.getBridgedChannel())) {	// 对于处理内部呼叫和外转外的情况之外，其他呼叫的情形，当两路通道建立通话后，每隔5分钟还会调用一次BridgeEvent事件，所以需要判断一下当前的联系对象是否发生变化
				channelLifeCycle2.setBridgedChannel(channel1);
				channelLifeCycle2.setBridgedUniqueid(uniqueId1);
				channelLifeCycle2.setBridgedTime(event.getDateReceived());
			}

			String connectedlinenum = channelLifeCycle2.getConnectedlinenum();
			if(connectedlinenum == null) {
				channelLifeCycle2.setConnectedlinenum(event.getCallerId1());
			}
		}
	}

}
