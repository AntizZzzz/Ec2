package com.jiangyifen.ec2.debug;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.event.QueueEntryEvent;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.vaadin.Application;

/**
 * 调试ShareData中的信息的调试线程
 * 
 * @author chb
 */
public class DebugThread extends Thread {
	
	public DebugThread() {
		this.setName("DebugThread");
		// 设置为后台线程，Spring销毁后被销毁
		this.setDaemon(true);
		this.start();
	}

	/**
	 * 线程运行方法
	 */
	public void run() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			try {
				System.out.println("请输入要查看的ShareData元素名：");
				String inputStr=scanner.nextLine().trim().toLowerCase();
				if(inputStr.equalsIgnoreCase("blacklist")||inputStr.equalsIgnoreCase("bl")){
					printBlackList();
				}else if(inputStr.equalsIgnoreCase("usertoexten")||inputStr.equalsIgnoreCase("ue")){
					printUserToExten();
				}else if(inputStr.equalsIgnoreCase("userExtenToHoldOnCallerChannels")||inputStr.equalsIgnoreCase("uehcc")){
					printUserExtenToHoldOnCallerChannels();
				}else if(inputStr.equalsIgnoreCase("usertodomain")||inputStr.equalsIgnoreCase("ud")){
					printUserToDomain();
				}else if(inputStr.equalsIgnoreCase("extentodomain")||inputStr.equalsIgnoreCase("etd")){
					printExtenToDomain();
				}else if(inputStr.equalsIgnoreCase("extenstatus")||inputStr.equalsIgnoreCase("es")){
					printExtenStatus();
				}else if(inputStr.equalsIgnoreCase("domaintodefaultoutline")||inputStr.equalsIgnoreCase("do")){
					printDomainToDefaultOutline();
				}else if(inputStr.equalsIgnoreCase("domaintodefaultqueue")||inputStr.equalsIgnoreCase("dq")){
					printDomainToDefaultQueue();
				}else if(inputStr.equalsIgnoreCase("extentodynamicoutline")||inputStr.equalsIgnoreCase("ed")){
					printExtenToDynamicOutline();
				}else if(inputStr.equalsIgnoreCase("extentostaticoutline")||inputStr.equalsIgnoreCase("eo")){
					printExtenToStaticOutline();
				}else if(inputStr.equalsIgnoreCase("extentouser")||inputStr.equalsIgnoreCase("eu")){
					printExtenToUser();
				}else if(inputStr.equalsIgnoreCase("extentoproject")||inputStr.equalsIgnoreCase("ep")){
					printExtenToProject();
				}else if(inputStr.equalsIgnoreCase("domaintoempnotoexten")||inputStr.equalsIgnoreCase("de")){
					printDomainToEmpnoToExten();
				}else if(inputStr.equalsIgnoreCase("domaintoexts")||inputStr.equalsIgnoreCase("dte")){
					printDomainToExts();
				}else if(inputStr.equalsIgnoreCase("domaintooutlines")||inputStr.equalsIgnoreCase("dto")){
					printDomainToOutlines();
				}else if(inputStr.equalsIgnoreCase("outlinetoproject")||inputStr.equalsIgnoreCase("op")){
					printOutlineToProject();
				}else if(inputStr.equalsIgnoreCase("projecttoqueue")||inputStr.equalsIgnoreCase("pq")){
					printProjectToQueue();
				}else if(inputStr.equalsIgnoreCase("projecttooutline")||inputStr.equalsIgnoreCase("po")){
					printProjectToOutline();
				}else if(inputStr.equalsIgnoreCase("usertoapplication")||inputStr.equalsIgnoreCase("ua")){
					printUserToApplication();
				}else if(inputStr.equalsIgnoreCase("usertosession")||inputStr.equalsIgnoreCase("us")){
					printUserToSession();
				}else if(inputStr.equalsIgnoreCase("peernameandchannels")||inputStr.equalsIgnoreCase("pac")){
					printPeernameAndChannels();
				}else if(inputStr.equalsIgnoreCase("channelandchannelsession")||inputStr.equalsIgnoreCase("cacs")){
					printChannelAndChannelSession();
				}else if(inputStr.equalsIgnoreCase("DomainToConfigs")||inputStr.equalsIgnoreCase("dc")){
					printDomainConfigs();
				}else if(inputStr.equalsIgnoreCase("OutlineToIncomingBlacklist")||inputStr.equalsIgnoreCase("oib")){
					printOutlineToIncomingBlacklist();
				}else if(inputStr.equalsIgnoreCase("OutlineToOutgoingBlacklist")||inputStr.equalsIgnoreCase("oob")){
					printOutlineToOutgoingBlacklist();
				}else if(inputStr.equalsIgnoreCase("ChannelToChannelLifeCycle")||inputStr.equalsIgnoreCase("cclc")){
					printChannelToChannelLifeCycle();
				}else if(inputStr.equalsIgnoreCase("ChannelToChannelLifeCycleAttachment")||inputStr.equalsIgnoreCase("cclca")){
					printChannelToChannelLifeCycleAttachment();
				}else if(inputStr.equalsIgnoreCase("QueueToWaiters")||inputStr.equalsIgnoreCase("qtw")){
					printQueueToWaiters();
				}else if(inputStr.equalsIgnoreCase("DomainToIncomingCallerAgiRequests")||inputStr.equalsIgnoreCase("dtica")){
					printDomainToIncomingCallerAgiRequests();
				}else if(inputStr.equalsIgnoreCase("IvrMenusMap")||inputStr.equalsIgnoreCase("imm")){
					printIvrMenusMap();
				}else if(inputStr.equalsIgnoreCase("OutlineToIvrLink")||inputStr.equalsIgnoreCase("otil")){
					printOutlineToIvrLinks();
				}else if(inputStr.equalsIgnoreCase("showall")||inputStr.equalsIgnoreCase("sa")){
					printBlackList();
					printUserToExten();
					printUserToDomain();
					printExtenToDomain();
					printDomainToDefaultOutline();
					printExtenToDynamicOutline();
					printExtenToStaticOutline();
					printExtenToUser();
					printExtenToProject();
					printDomainToEmpnoToExten();
					printDomainToDefaultQueue();
					printOutlineToProject();
					printChannelAndChannelSession();
					printUserExtenToHoldOnCallerChannels();
					printDomainConfigs();
				}else{
					System.out.println("没有找到您输入的元素，请确认输入是否正确");
					System.out.println("//***************所有可查询元素*********************//");
					System.out.println("UserToApplication(ua)    UserToSession(us)");
					System.out.println("------------------------------------------------");
					System.out.println("UserToExten(ue)          ExtenToUser(eu)");
					System.out.println("UserToDomain(ud)          ExtenToDomain(etd)");
					System.out.println("ExtenToProject(ep)       ExtenToStaticOutline(eo)");
					System.out.println("ExtenToDynamicOutline(ed) DomainToEmpnoToExten(de)");
					System.out.println("UserExtenToHoldOnCallerChannels(uehcc) ");
					System.out.println("ChannelToChannelLifeCycle(cclc) ChannelToChannelLifeCycleAttachment(cclca)");
					System.out.println("------------------------------------------------");
					System.out.println("OutlineToProject(op)     ProjectToQueue(pq)");
					System.out.println("ProjectToOutline(po) 	 OutlineToIncomingBlacklist(oib)");
					System.out.println("OutlineToOutgoingBlacklist(oob)");
					System.out.println("DomainToIncomingCallerAgiRequests(dtica)");
					System.out.println("------------------------------------------------");
					System.out.println("BlackList(bl)            DomainToDefaultOutline(do)");
					System.out.println("DomainToDefaultQueue(dq) DomainToExts(dte)");
					System.out.println("DomainToOutlines(dto) DomainToConfigs(dc)");
					System.out.println("QueueToWaiters(qtw) IvrMenusMap(imm)");
					System.out.println("OutlineToIvrLink(otil)");
					System.out.println("------------------------------------------------");
					System.out.println("PeernameAndChannels(pac) ChannelAndChannelSession(cacs)");
					System.out.println("------------------------------------------------");
					System.out.println("ShowAll(sa)");
					System.out.println("//**********************************************//");
					System.out.println();
				}
			} catch (Exception e) {
				System.out.println("出现异常！！！");
				e.printStackTrace();
				break;
			}
		}
	}

	private void printUserExtenToHoldOnCallerChannels() {
		Map<String, List<String>> exten2Channels = ShareData.userExtenToHoldOnCallerChannels;
		System.out.println("==========UserExtenToHoldOnCallerChannels==========");
		for(String exten : exten2Channels.keySet()) {
			System.out.println("Exten ---> " + exten + "  == hold on channels --> "+ exten2Channels.get(exten));
		}
		System.out.println("==========UserExtenToHoldOnCallerChannels==========");
		System.out.println();
	}

	private void printExtenStatus() {
		Map<String, ExtenStatus> extenStatusMap = ShareData.extenStatusMap;
		System.out.println("==========ExtenStatusMap==========");
		for(String exten:extenStatusMap.keySet()){
			System.out.println("Exten:"+exten+"  -->   StatusMap:"+extenStatusMap.get(exten));
		}
		System.out.println("==========ExtenStatusMap==========");
		System.out.println();
	}

	private void printChannelAndChannelSession() {
		Map<String, ChannelSession> channelAndChannelSession = ShareData.channelAndChannelSession;
		System.out.println("==========ChannelAndChannelSession==========");
		for(String channel:channelAndChannelSession.keySet()){
			System.out.println("Channel:"+channel+"  -->   ChannelSession:"+channelAndChannelSession.get(channel));
		}
		System.out.println("==========ChannelAndChannelSession==========");
		System.out.println();
	}

	private void printPeernameAndChannels() {
		Map<String, Set<String>> peernameAndChannels = ShareData.peernameAndChannels;
		System.out.println("==========PeernameAndChannels==========");
		for(String peerName:peernameAndChannels.keySet()){
			System.out.println("PeerName:"+peerName);
			Set<String> channels=peernameAndChannels.get(peerName);
			for(String channel:channels){
				System.out.println("		Channel:"+channel);
			}
		}
		System.out.println("==========PeernameAndChannels==========");
		System.out.println();
	}

	private void printExtenToDomain() {
		Map<String, Long> extenToDomain = ShareData.extenToDomain;
		System.out.println("==========ExtenToDomain==========");
		for(String exten:extenToDomain.keySet()){
			System.out.println("Exten:"+exten+"  -->   DomainId:"+extenToDomain.get(exten));
		}
		System.out.println("==========ExtenToDomain==========");
		System.out.println();
	}

	private void printUserToDomain() {
		Map<Long, Long> userToDomain = ShareData.userToDomain;
		System.out.println("==========UserToDomain==========");
		for(Long userId:userToDomain.keySet()){
			System.out.println("UserId:"+userId+"  -->  DomainId:"+userToDomain.get(userId));
		}
		System.out.println("==========UserToDomain==========");
		System.out.println();
	}

	private void printDomainToOutlines() {
		Map<Long, List<String>> domainToOutlines = ShareData.domainToOutlines;
		System.out.println("==========DomainToOutlines==========");
		for(Long domainId:domainToOutlines.keySet()){
			System.out.println("DomainId:"+domainId);
			List<String> outlines=domainToOutlines.get(domainId);
			for(String outline:outlines){
				System.out.println("DomainId:"+domainId+"  -->  Outline:"+outline);
			}
		}
		System.out.println("==========DomainToOutlines==========");
		System.out.println();
	}

	private void printDomainToExts() {
		Map<Long, List<String>> domainToExts = ShareData.domainToExts;
		System.out.println("==========DomainToExts==========");
		for(Long domainId:domainToExts.keySet()){
			System.out.println("DomainId:"+domainId);
			List<String> exts=domainToExts.get(domainId);
			for(String ext:exts){
				System.out.println("DomainId:"+domainId+"  -->  Ext:"+ext);
			}
		}
		System.out.println("==========DomainToExts==========");
		System.out.println();
	}

	private void printUserToSession() {
		Map<Long, HttpSession> userToSession = ShareData.userToSession;
		System.out.println("==========UserToSession==========");
		for(Long userId:userToSession.keySet()){
			System.out.println("UserId:"+userId);
		}
		System.out.println("==========UserToSession==========");
		System.out.println();
	}

	private void printUserToApplication() {
		Map<Long, Application> userToApplication = ShareData.userToApp;
		System.out.println("==========UserToApplication==========");
		for(Long userId:userToApplication.keySet()){
			System.out.println("UserId:"+userId+" Application:"+userToApplication.get(userId).hashCode());
		}
		System.out.println("==========UserToApplication==========");
		System.out.println();
	}

	private void printDomainToDefaultQueue() {
		Map<Long, String> domainToDefaultQueue = ShareData.domainToDefaultQueue;
		System.out.println("==========DomainToDefaultQueue==========");
		for(Long domainId:domainToDefaultQueue.keySet()){
			System.out.println("DomainId:"+domainId+" Queue:"+domainToDefaultQueue.get(domainId));
		}
		System.out.println("==========DomainToDefaultQueue==========");
		System.out.println();
	}

	private void printProjectToOutline() {
		Map<Long, String> projectToOutline = ShareData.projectToOutline;
		
		System.out.println("==========ProjectToOutline==========");
		for(Long projectId:projectToOutline.keySet()){
			System.out.println("ProjectId:"+projectId+" Outline:"+projectToOutline.get(projectId));
		}
		System.out.println("==========ProjectToOutline==========");
		System.out.println();
	}

	private void printProjectToQueue() {
		Map<Long, String> projectToQueue = ShareData.projectToQueue;
		
		System.out.println("==========ProjectToQueue==========");
		for(Long projectId:projectToQueue.keySet()){
			System.out.println("ProjectId:"+projectId+" Queue:"+projectToQueue.get(projectId));
		}
		System.out.println("==========ProjectToQueue==========");
		System.out.println();
	}

	private void printDomainToEmpnoToExten() {
//		Map<Long, ConcurrentHashMap<String, String>> domainToEmpnoToExten = ShareData.domainToEmpnoToExten;
//		
//		System.out.println("==========DomainToEmpnoToExten==========");
//		for(Long domainId:domainToEmpnoToExten.keySet()){
//			System.out.println("DomainId:"+domainId);
//			ConcurrentHashMap<String, String> empnoToExten=domainToEmpnoToExten.get(domainId);
//			for(String empNo:empnoToExten.keySet()){
//				System.out.println("		EmpNo:"+empNo+"  -->  Exten:"+empnoToExten.get(empNo));
//			}
//		}
//		System.out.println("==========DomainToEmpnoToExten==========");
//		System.out.println();
	}

	private void printExtenToStaticOutline() {
		Map<String,String> extenToStaticOutline=ShareData.extenToStaticOutline;
		
		System.out.println("==========extenToStaticOutline==========");
		for(String exten:extenToStaticOutline.keySet()){
			System.out.println("Exten:"+exten+" ---> StaticOutline:"+extenToStaticOutline.get(exten));
		}
		System.out.println("==========extenToStaticOutline==========");
		System.out.println();
	}

	private void printExtenToDynamicOutline() {
		Map<String,String> extenToDynamicOutline=ShareData.extenToDynamicOutline;
		
		System.out.println("==========extenToDynamicOutline==========");
		for(String exten:extenToDynamicOutline.keySet()){
			System.out.println("Exten:"+exten+" ---> DynamicOutline:"+extenToDynamicOutline.get(exten));
		}
		System.out.println("==========extenToDynamicOutline==========");
		System.out.println();
	}

	private void printDomainToDefaultOutline() {
		Map<Long,String> domainToDefaultOutline=ShareData.domainToDefaultOutline;
		
		System.out.println("==========domainToDefaultOutline==========");
		for(Long domainId:domainToDefaultOutline.keySet()){
			System.out.println("Domain:"+domainId+" ---> DefaultOutline:"+domainToDefaultOutline.get(domainId));
		}
		System.out.println("==========domainToDefaultOutline==========");
		System.out.println();
	}

	/**
	 * 外线到项目
	 */
	private void printOutlineToProject() {
		Map<String,Long> outlineToProject=ShareData.outlineToProject;
		
		System.out.println("==========OutlineToProject==========");
		for(String outline:outlineToProject.keySet()){
			System.out.println("Outline:"+outline+" ---> ProjectId:"+outlineToProject.get(outline));
		}
		System.out.println("==========OutlineToProject==========");
		System.out.println();	}


	/**
	 * 分机到项目
	 */
	private void printExtenToProject() {
		Map<String,Long> extenToProject=ShareData.extenToProject;
		
		System.out.println("==========ExtenToProject==========");
		for(String exten:extenToProject.keySet()){
			System.out.println("Exten:"+exten+" ---> ProjectId:"+extenToProject.get(exten));
		}
		System.out.println("==========ExtenToProject==========");
		System.out.println();
	}


	/**
	 * 显示分机和用户的对应关系
	 */
	private void printExtenToUser() {
		Map<String, Long> extenToUser=ShareData.extenToUser;
		
		System.out.println("==========ExtenToUser==========");
		for(String exten:extenToUser.keySet()){
			System.out.println("Exten:"+exten+" ---> UserId:"+extenToUser.get(exten));
		}
		System.out.println("==========ExtenToUser==========");
		System.out.println();
	}

	/**
	 * 显示用户和分机的对应关系
	 */
	private void printUserToExten() {
		Map<Long, String> userToExten= ShareData.userToExten;
		
		System.out.println("==========UserToExten==========");
		for(Long userId:userToExten.keySet()){
			System.out.println("User:"+userId+" ---> Exten:"+userToExten.get(userId));
		}
		System.out.println("==========UserToExten==========");
		System.out.println();
	}

	/**
	 * 所有黑名单信息
	 */
	private void printBlackList() {
		for(Long id : ShareData.domainToIncomingBlacklist.keySet()) {
			List<String> incomingBlackList = ShareData.domainToIncomingBlacklist.get(id);
			List<String> outgoingBlackList = ShareData.domainToOutgoingBlacklist.get(id);
			
			System.out.println("域domain----"+id+"==========BlackList========== 开始");
			System.out.print("-------< incoming  ：");
			System.out.println(incomingBlackList);
			
			System.out.print("-------> outgoing  ：");
			System.out.println(outgoingBlackList);
			System.out.println("域domain----"+id+"==========BlackList========== 结束");
			System.out.println();
		}
	}
	
	/**
	 * 所有域下的配置信息
	 */
	private void printDomainConfigs() {
		System.out.println("=========domainConfigs=========");
		for(Long id : ShareData.domainToConfigs.keySet()) {
			Map<String, Boolean> m = ShareData.domainToConfigs.get(id);
			System.out.println("----------domainId "+id+" ------------");
			for(String key : m.keySet()) {
				String value = m.get(key) ? "是" : "否";
				System.out.println("	"+ key+ "	:	"+value);
			}
			System.out.println();
		}
		System.out.println("=========domainConfigs=========");
	}
	
	/**
	 * 所有域下Map<域的id, ConcurrentHashMap<主叫号码，agi请求>> 如Map<1,ConcurrentHashMap<"13816760365", agi请求>>
	 */
	private void printDomainToIncomingCallerAgiRequests() {
		System.out.println("========= DomainToIncomingCallerAgiRequests =========");
		for(Long id : ShareData.domainToIncomingDialInfoMap.keySet()) {
			Map<String, IncomingDialInfo> m = ShareData.domainToIncomingDialInfoMap.get(id);
			System.out.println("----------domainId "+id+" ------------");
			if(m != null) {
				String keys = StringUtils.join(m.keySet(), ",");
				System.out.println("callers : ["+keys+"]");
				System.out.println();
			}
		}
		System.out.println("========= DomainToIncomingCallerAgiRequests =========");
	}
	
	/**
	 * 所有呼入黑名单
	 */
	private void printOutlineToIncomingBlacklist() {
		System.out.println("=========OutlineToIncomingBlacklist=========");
		for(Long outlineId : ShareData.outlineToIncomingBlacklist.keySet()) {
			List<String> m = ShareData.outlineToIncomingBlacklist.get(outlineId);
			System.out.println("----------outlineUsername Id "+outlineId+" ------------");
			System.out.println(m);
			System.out.println();
		}
		System.out.println("=========OutlineToIncomingBlacklist=========");
	}
	
	/**
	 * 所有呼入黑名单
	 */
	private void printOutlineToOutgoingBlacklist() {
		System.out.println("=========OutlineToOutgoingBlacklist=========");
		for(Long outlineId : ShareData.outlineToOutgoingBlacklist.keySet()) {
			List<String> m = ShareData.outlineToOutgoingBlacklist.get(outlineId);
			System.out.println("----------outlineId "+outlineId+" ------------");
			System.out.println(m);
			System.out.println();
		}
		System.out.println("=========OutlineToOutgoingBlacklist=========");
	}
	
	/**
	 * 所有通道的周期
	 */
	private void printChannelToChannelLifeCycle() {
		System.out.println("========= ChannelToChannelLifeCycle =========");
		for(String channel : ShareData.channelToChannelLifeCycle.keySet()) {
			ChannelLifeCycle channelLifeCycle = ShareData.channelToChannelLifeCycle.get(channel);
			System.out.println("---------- "+channelLifeCycle+" ------------");
			System.out.println();
		}
		System.out.println("=========ChannelToChannelLifeCycle=========");
//		System.out.println("=========暂时不用，重构或10月份再用=========");
	}
	
	/**
	 * 所有通道的周期2
	 */
	private void printChannelToChannelLifeCycleAttachment() {
//------------------------------------------------------------------------------------------------------------------
//		System.out.println("========= ChannelToChannelLifeCycleAttachment =========");
//		for(String channel : ShareData.channelToChannelLifeCycleAttachment.keySet()) {
//			ChannelLifeCycle channelLifeCycle = ShareData.channelToChannelLifeCycleAttachment.get(channel);
//			System.out.println("---------- "+channelLifeCycle+" ------------");
//			System.out.println();
//		}
//		System.out.println("=========ChannelToChannelLifeCycleAttachment=========");
//------------------------------------------------------------------------------------------------------------------
	}
	
	/**
	 * 所有队列等待成员
	 */
	private void printQueueToWaiters() {
		System.out.println("========= QueueToWaiters =========");
		for(String queue : AutoDialHolder.queueToWaiters.keySet()) {
			List<QueueEntryEvent> events = AutoDialHolder.queueToWaiters.get(queue);
			System.out.println("队列："+queue+"--排队人数："+events.size());
			for(QueueEntryEvent qee : events) {
				System.out.println(qee);
			}
		}
		System.out.println("========= QueueToWaiters =========");
//		System.out.println("=========暂时不用，重构或10月份再用=========");
	}
	
	
	private void printIvrMenusMap() {
		System.out.println("================= IVRMenu ====================");
		for(IVRMenu menu : ShareData.ivrMenusMap.values()) {
			System.out.println("编号："+menu.getId()+"----------"+menu.getIvrMenuName()+"----"+menu.getDescription());
			System.out.println(menu.toLoggerString());
		}
		System.out.println("================= IVRMenu ====================");
	}
	
	private void printOutlineToIvrLinks() {
		System.out.println("================= OutlineToIvrLink ====================");
		for(Long key : ShareData.outlineIdToIvrLinkMap.keySet()) {
			for(OutlineToIvrLink link : ShareData.outlineIdToIvrLinkMap.get(key)) {
				System.out.println("外线编号："+key+"  对应:"+link.toLoggerString());
			}
		}
		System.out.println("================= OutlineToIvrLink ====================");
	}
	
}
