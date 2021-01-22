package com.jiangyifen.ec2.ami;

import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.MeetMeJoinEvent;

import com.jiangyifen.ec2.bean.MeettingRoomFirstJoinMemberInfo;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 
 * @Description 描述：新的成员加入会议室的时候促发
 * 
 * @author  jrh
 * @date    2013年12月23日 上午11:42:33
 * @version v1.0.0
 */
public class MeetMeJoinEventListener extends AbstractManagerEventListener {

	/**
	 * 监听MeetMeJoinEvent触发的事件
	 */
	@Override
	protected void handleEvent(MeetMeJoinEvent event) {
		// 从peernamdAndChannel删数据
		
		// 查出当前会议室是属于哪个租户的
		String meettingRoom = event.getMeetMe();
		String channel = event.getChannel();
		MeettingDetailRecord record = new MeettingDetailRecord();
		Long domainId = 0L;
		for(Long key : ShareData.domainToExts.keySet()) {
			if(ShareData.domainToExts.get(key).contains(meettingRoom)) {
				domainId = key;
				break;
			}
		}
		
		// 创建新的会议成员信息，并放到内存中，等MeetMeLeaveListener 的时候，再持久化到数据库中
		ConcurrentHashMap<String, MeettingDetailRecord> meettingRecordMap = ShareData.meetingToMemberRecords.get(meettingRoom);
		if(meettingRecordMap == null) {
			meettingRecordMap = new ConcurrentHashMap<String, MeettingDetailRecord>();
			ShareData.meetingToMemberRecords.put(meettingRoom, meettingRecordMap);
		}
		meettingRecordMap.put(channel, record);
		
		int memberIndex = event.getUserNum();
		String meetingUniqueId = event.getUniqueId();
		boolean isFirstJoin = false;		// 标识是否为第一个进入会议室的成员
		Long originatorId = null;			// 标识会议室发起人的编号
		if(memberIndex == 1) {	// 判断是不是第一个进入会议室的人，如果是，则将第一个进入者对应通道的信息放入内存中[如：通道、通道唯一标识、会议发起人的编号]
			isFirstJoin = true;
			originatorId = ShareData.extenToUser.get(meettingRoom);
			if(originatorId == null) {
				originatorId = ShareData.meettingRoomExtenToMgrIdMap.get(meettingRoom);
			}
			MeettingRoomFirstJoinMemberInfo firstJoinMemberInfo = new MeettingRoomFirstJoinMemberInfo();
			firstJoinMemberInfo.setChannel(channel);
			firstJoinMemberInfo.setUniqueId(event.getUniqueId());
			firstJoinMemberInfo.setOriginatorId(originatorId);
			ShareData.meettingToFirstJoinMemberMap.put(meettingRoom, firstJoinMemberInfo);
		} else {	// 如果当前不是第一个进入会议室的人，则本次会议的唯一标识值，以及会议室发起人的编号，需要到内存中获取
			MeettingRoomFirstJoinMemberInfo originatorInfo = ShareData.meettingToFirstJoinMemberMap.get(meettingRoom);
			meetingUniqueId = originatorInfo.getUniqueId();
			originatorId = originatorInfo.getOriginatorId();
		}
		
		// 获取会议室的发起人的编号，以及当前参加会议
		String callerIdNum = event.getCallerIdNum();
		Long joinMemberId = null;
		String joinMemberNum = callerIdNum;
//		// 方式一：这种方式基本能保证按joinMemberId 不为空，除非本身就找不到joinMemberId
//		Long userId = ShareData.extenToUser.get(callerIdNum);
//		// 如果根据呼叫者号码没有找到对应的坐席编号，那可能是因为参与会议的人不是坐席，而是管理员
//		Long joinMemberId = (userId != null) ? userId : ShareData.meettingRoomExtenToMgrIdMap.get(callerIdNum);
//		if(joinMemberId == null) { 
//			String sippeer = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
//			userId = ShareData.extenToUser.get(sippeer);
//			joinMemberId = (userId != null) ? userId : ShareData.meettingRoomExtenToMgrIdMap.get(sippeer);
//		}
		
		// 方式二: 这种只按callerIdNum 或者 通道中对应的sippeer 中的一个进行查询joinMemberId
		if(callerIdNum != null && !"".equals(callerIdNum)) {	// 首先根据呼叫者编号进行查询，呼入的时候，可能被叫通道没有callerIdNum
			Long userId = ShareData.extenToUser.get(callerIdNum);	// 如果根据呼叫者号码没有找到对应的坐席编号，那可能是因为参与会议的人不是坐席，而是管理员
			joinMemberId = (userId != null) ? userId : ShareData.meettingRoomExtenToMgrIdMap.get(callerIdNum);
		} else {	// 如果callerIdNum 为空，那么就需要从通道中截取sippeer 来进行判断了
			String sippeer = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
			Long userId = ShareData.extenToUser.get(sippeer);
			joinMemberId = (userId != null) ? userId : ShareData.meettingRoomExtenToMgrIdMap.get(sippeer);
			joinMemberNum = sippeer;
		}
		
		
		// 设置成员详情值，这里不止持久化操作，等成员离开会议室的时候在持久化到数据库
		record.setMeettingRoom(meettingRoom);
		record.setMeettingUniqueId(meetingUniqueId);
		record.setChannel(channel);
		record.setChannelUniqueId(event.getUniqueId());
		record.setMemberIndex(memberIndex);
		record.setJoinDate(event.getDateReceived());
		record.setCallerIdName(event.getCallerIdName());
		record.setCallerIdNum(callerIdNum);
		record.setJoinMemberNum(joinMemberNum);
		record.setFirstJoin(isFirstJoin);
		record.setConnectedLineName(event.getConnectedlinename());
		record.setConnectedLineNum(event.getConnectedlinenum());
		record.setSource(event.getSource()+"");
		record.setOriginatorId(originatorId);
		record.setJoinMemberId(joinMemberId);
		record.setDomainId(domainId);
		
//		System.out.println("callerIdName---->"+event.getCallerIdName());
//		System.out.println("callerIdNum---->"+event.getCallerIdNum());
//		System.out.println("channel---->"+event.getChannel());
//		System.out.println("meettingRoom---->"+meettingRoom);
//		System.out.println("uniqueId---->"+event.getUniqueId());
//		System.out.println("memberIndex---->"+event.getUserNum());
//		System.out.println("dateReceived---->"+event.getDateReceived());
//		System.out.println("source---->"+event.getSource());
//		System.out.println("connectedlinename---->"+event.getConnectedlinename());
//		System.out.println("connectedlinenum---->"+event.getConnectedlinenum());
	}
}
