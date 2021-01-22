package com.jiangyifen.ec2.ami;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.QueueEntryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.entity.MissCallLog;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MissCallLogService;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 监听HangupEvent事件 
 * @author chb
 */
public class HangupEventListener extends AbstractManagerEventListener {
	// 日志工具
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 监听HangupEvent触发的事件
	 */
	@Override
	protected void handleEvent(HangupEvent event) {
		String channel = event.getChannel();
		String sippeerName = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
		Long userId = ShareData.extenToUser.get(sippeerName);
		
		// 根据通道号码检查当前通道是不是外线对应的通道，如果是，则获取外线对应的域的编号
		Long domainId = null;
		for(Long did : ShareData.domainToOutlines.keySet()) {
			if(ShareData.domainToOutlines.get(did).contains(sippeerName)) {
				domainId = did;
				break;
			}
		}

		// jrh 新增 2013-11-21 呼入客户与其对应的请求AgiRequest 从内存中移除
		if(domainId != null) {
			ConcurrentHashMap<String, IncomingDialInfo> incomingCallerToDialInfos = ShareData.domainToIncomingDialInfoMap.get(domainId);
			if(incomingCallerToDialInfos != null) {
				synchronized (incomingCallerToDialInfos) {
					if(event.getCallerIdNum() != null) {
						incomingCallerToDialInfos.remove(event.getCallerIdNum());
					}
				}
			}
		}

//------------------------------------------------------------------------------------------------------------------
//		System.out.println("-------------------------------- hangup event --------------------------------");
//		System.out.println("hangup event---->"+event);
//		System.out.println("--------------------------------              --------------------------------");
////		BridgetimeInfo bridgetimeInfo = ShareData.channelToBridgetime.get(channel);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		System.out.println("hangup event new date--->"+sdf.format(new Date()));
////		System.out.println("hangup event bridgetimeInfo-->"+bridgetimeInfo);
//		System.out.println("hangup event channel-->"+channel);
//		System.out.println();
//------------------------------------------------------------------------------------------------------------------
//TODO
		this.loggerInfoForAsteriskChannelRemant(event);
		
		this.checkAndCreateMissCallLog(event, channel, sippeerName);

		// 从peernamdAndChannel删数据
		Set<String> channelSet = ShareData.peernameAndChannels.get(sippeerName);
		if (channelSet != null) {
			this.removeChannel(channelSet, channel);
		}
		
		this.removeAttributesInChannelSession(ShareData.channelAndChannelSession,channel);

		this.updateCsrUiStatus(sippeerName, userId);
		
		// jrh 移除队列排队人员  2013-1--15 
		this.removeQueueWaiters(channel);

		// jrh Map<主叫号码，ChannelLifeCycle>【用于电话漏接日志的创建】 清理主叫对应的内存
		if(event.getCallerIdNum() != null) {
			synchronized (ShareData.callerNumToChannelLifeCycle) {
				ShareData.callerNumToChannelLifeCycle.remove(event.getCallerIdNum());
			}
		}
	}

	/**
	 * 更新坐席界面组件信息
	 * @param sippeerName
	 * @param userId
	 */
	private void updateCsrUiStatus(String sippeerName, Long userId) {
		// 挂断后修改组件的信息，只有在用户在线的情况下才有更新组件的必要
		if(userId != null) {
			// 挂断后修改Csr 用户状态栏中 通话状态的图标
			CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(userId);
			if(csrStatusBar != null) {
				if (ShareData.peernameAndChannels.get(sippeerName) != null 
						&& ShareData.peernameAndChannels.get(sippeerName).size() > 0) {
					csrStatusBar.updateCallStatus(1); 		// 通话状态
				} else {
					csrStatusBar.updateCallStatus(0); 		// 通话状态
				}
				
				// 挂断后修改CsrStatus 中的挂断组件
				csrStatusBar.updateHangupMenuComponents();
			}
		}
	}

	/**
	 * jrh 判断通道信息，并及时创建电话漏接记录
	 * 	 创建漏接记录需要满足的条件：
	 * 		1：呼叫方向为：呼入
	 * 		2：被叫对象是：分机
	 * 		3：被叫分机没有接通电话
	 * @param event
	 * @param channel
	 * @param sippeerName
	 */
	private void checkAndCreateMissCallLog(HangupEvent event, String channel, String sippeerName) {
		try {
			ChannelLifeCycle destChannelLifeCycle = ShareData.channelToChannelLifeCycle.get(channel);
			if(destChannelLifeCycle == null) return;
			
			synchronized(destChannelLifeCycle) {
				// 第一：判断是否为被叫，第二：判断是否已经接通。 创建漏接记录需要满足的条件：没接通，是被叫，还有被叫的对象是分机
				// 第三：destChannelLifeCycle.getUpStateTime() == null，因为如果是分机被邀请到会议室的时候，是没有bridge 的时间的，但是当分机进入会议室时，会有up 的时间 
				if(destChannelLifeCycle.getRingingStateTime() != null && destChannelLifeCycle.getBridgedTime() == null && destChannelLifeCycle.getUpStateTime() == null) {
					Long domainId = null;
					for(Long did : ShareData.domainToExts.keySet()) {		// 检查当前通道对应的被叫是不是分机，如果是才需要做下一步处理
						if(ShareData.domainToExts.get(did).contains(sippeerName)) {
							domainId = did; break;
						}
					}

					String srcNum = (event.getConnectedlinenum() == null) ? destChannelLifeCycle.getConnectedlinenum() : event.getConnectedlinenum();
					if(sippeerName.equals(srcNum)) {	// 如果是分机呼叫自己没接，则不算做漏接电话【这种情况发生在坐席界面点播呼叫，分机振铃后没接】
						return;
					}
					
					if(domainId != null) {		// 如果被叫是分机，创建记录
						if(srcNum != null) {	// 如果主叫号码不为空，则通过主叫号码查询主叫号码对应通道的信息
							ChannelLifeCycle srcChannelLifeCycle = ShareData.callerNumToChannelLifeCycle.get(srcNum);
							if(srcChannelLifeCycle != null) {
								destChannelLifeCycle.setConnectedChannel(srcChannelLifeCycle.getSelfChannel());
								destChannelLifeCycle.setConnectedUniqueid(srcChannelLifeCycle.getSelfUniqueid());
							}
						}

						String srcName = (event.getConnectedlinename() == null) ? destChannelLifeCycle.getConnectedlinename() : event.getConnectedlinename();
						Date ringingTime = destChannelLifeCycle.getRingingStateTime();
						Date hangupTime = event.getDateReceived();
						Long ringingDuration = (hangupTime.getTime() - ringingTime.getTime() + 500) / 1000;		// 计算振铃时长
						Long destUserId = ShareData.extenToUser.get(sippeerName);
						Long destUserDeptId = (destUserId != null) ? ShareData.userToDepartment.get(destUserId) : null;
						Long srcUserId = (srcNum != null) ? ShareData.extenToUser.get(srcNum) : null;
						Long srcUserDeptId = (srcUserId != null) ? ShareData.userToDepartment.get(srcUserId) : null;
						
						// 创建电话漏接记录
						MissCallLog missCallLog = new MissCallLog();
						missCallLog.setSrcChannel(destChannelLifeCycle.getConnectedChannel());
						missCallLog.setSrcUniqueId(destChannelLifeCycle.getConnectedUniqueid());
						missCallLog.setSrcNum(srcNum);
						missCallLog.setSrcName(srcName);
						missCallLog.setDestChannel(event.getChannel());
						missCallLog.setDestUniqueId(event.getUniqueId());
						missCallLog.setDestName(sippeerName);
						missCallLog.setDestNum(sippeerName);
						missCallLog.setRingingDuration(ringingDuration);
						missCallLog.setRingingStateTime(ringingTime);
						missCallLog.setHangupTime(hangupTime);
						missCallLog.setChannelCreateTime(destChannelLifeCycle.getDownStateTime());
						missCallLog.setDestUserId(destUserId);
						missCallLog.setDestUserDeptId(destUserDeptId);
						missCallLog.setSrcUserId(srcUserId);
						missCallLog.setSrcUserDeptId(srcUserDeptId);
						missCallLog.setDomainId(domainId);
						
						MissCallLogService missCallLogService = SpringContextHolder.getBean("missCallLogService");
						missCallLogService.saveMissCallLog(missCallLog);
					}

					// 清理内存
					ShareData.channelToChannelLifeCycle.remove(channel);
				} else if(destChannelLifeCycle.getOriginateDialTime() == null && destChannelLifeCycle.getRingStateTime() == null && destChannelLifeCycle.getRingingStateTime() == null 
						&& destChannelLifeCycle.getBridgedTime() == null && destChannelLifeCycle.getUpStateTime() == null) {	// 如果是呼出，并且是被叫通道，并且被叫没有接通电话，则直接从内存中移除
					// 清理内存
					ShareData.channelToChannelLifeCycle.remove(channel);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("jrh 呼入电话挂断时，创建电话漏接记录出现异常--->"+e.getMessage(), e);
		}
	}

	/**
	 * 线程安全地移除一个Channel
	 * @param channelSet
	 * @param channel
	 */
	private void removeChannel(Set<String> channelSet, String channel) {
		synchronized (channelSet) {
			channelSet.remove(channel);
		}
	}
	
	/**
	 * 移除Channel的ChannelSession
	 * @param channelAndChannelSession
	 * @param channel
	 * @param channelSession
	 * @param event
	 */
	private void removeAttributesInChannelSession(Map<String, ChannelSession> channelAndChannelSession, String channel) {
		synchronized (channelAndChannelSession) {
			channelAndChannelSession.remove(channel);
		}
	}

	/**
	 * jrh  2013-1--15 
	 *	 检查当前通道是否是为某个队列中的等待成员，如果是，则从队列中移除
	 * @param channel	当前挂断的通道
	 */
	private void removeQueueWaiters(String channel) {
		for(String queue : AutoDialHolder.queueToWaiters.keySet()) {
			List<QueueEntryEvent> waiters = AutoDialHolder.queueToWaiters.get(queue);
			if(waiters != null) {
				QueueEntryEvent needRemovedWaiter = null;
				for(QueueEntryEvent waiter : waiters) {
					if(channel.equals(waiter.getChannel())) {
						needRemovedWaiter = waiter;
						break;
					}
				}
				synchronized (waiters) {
					if(needRemovedWaiter != null) {
						waiters.remove(needRemovedWaiter);
					}
				}
			}
		}
	}

	/**
	 * 用于定位asterisk channel 残留的问题
	 * @param event
	 */
	private void loggerInfoForAsteriskChannelRemant(HangupEvent event) {
		if(GlobalVariable.mac_asterisk_channel_remnant.equals(GlobalData.MAC_ADDRESS)) {
			StringBuffer strBf = new StringBuffer();
			strBf.append("jrh check channel remnant--> ");
			strBf.append("HangupEventListener     - ");
			strBf.append("JrhRemoveByHangUp  : ");
			strBf.append(" channel=");
			strBf.append(event.getChannel());
			strBf.append(", calleridname=");
			strBf.append(event.getCallerIdName());
			strBf.append(", calleridnum=");
			strBf.append(event.getCallerIdNum());
			strBf.append(", connectedlinename=");
			strBf.append(event.getConnectedlinename());
			strBf.append(", connectedlinenum=");
			strBf.append(event.getConnectedlinenum());
			strBf.append(", uniqueid=");
			strBf.append(event.getUniqueId());
			LOGGER.warn(strBf.toString());
		}
	}
	
}
