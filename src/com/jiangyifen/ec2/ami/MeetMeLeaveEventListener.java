package com.jiangyifen.ec2.ami;

import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.MeetMeLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MeettingDetailRecordService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：
 * 
 * @author  jrh
 * @date    2013年12月23日 上午11:42:33
 * @version v1.0.0
 */
public class MeetMeLeaveEventListener extends AbstractManagerEventListener {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 监听NewChannelEvent触发的事件
	 */
	@Override
	protected void handleEvent(MeetMeLeaveEvent event) {
		// 从peernamdAndChannel删数据
		String meettingRoom = event.getMeetMe();
		String channel = event.getChannel();

		// 首先根据会议室名称，到内存中查找其对应的所有会议室成员的租户
		ConcurrentHashMap<String, MeettingDetailRecord> meettingRecordMap = ShareData.meetingToMemberRecords.get(meettingRoom);
		if(meettingRecordMap == null) {
			logger.error("jrh 在内存中找不到会议室成员的记录信息！ShareData.meetingToMemberRecords.get("+meettingRoom+")");
			return;
		}
		
		// 到内存中查找当前通道对应的会议室成员的相关信息
		MeettingDetailRecord record = meettingRecordMap.get(channel);
		if(record == null) {
			logger.error("jrh 在内存中找不到会议室成员的记录信息！ConcurrentHashMap<String, MeettingDetailRecord> meettingRecordMap.get("+channel+")");
			return;
		}
		
		// 清理内存
		meettingRecordMap.remove(channel);
		if(meettingRecordMap.size() == 0) {
			ShareData.meetingToMemberRecords.remove(meettingRoom);
			ShareData.meettingToFirstJoinMemberMap.remove(meettingRoom);
		}
		
		// 将记录持久化到数据库
		MeettingDetailRecordService meettingDetailRecordService = SpringContextHolder.getBean("meettingDetailRecordService");
		record.setDuration(event.getDuration());
		record.setLeaveDate(event.getDateReceived());
		meettingDetailRecordService.saveMeettingDetailRecord(record);
		
//-------------------------------------------------------------------
//		System.out.println(record);
//		System.out.println(event.toString());
//		System.out.println("callerIdName---->"+event.getCallerIdName());
//		System.out.println("callerIdNum---->"+event.getCallerIdNum());
//		System.out.println("channel---->"+event.getChannel());
//		System.out.println("meettingRoom---->"+event.getMeetMe());
//		System.out.println("uniqueId---->"+event.getUniqueId());
//		System.out.println("memberIndex---->"+event.getUserNum());
//		System.out.println("duration---->"+event.getDuration());
//		System.out.println("dateReceived---->"+event.getDateReceived());
//		System.out.println("privilege---->"+event.getPrivilege());
//		System.out.println("connectedlinename---->"+event.getConnectedlinename());
//		System.out.println("connectedlinenum---->"+event.getConnectedlinenum());
//		System.out.println("source---->"+event.getSource());
	}
}
