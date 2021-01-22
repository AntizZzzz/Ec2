package com.jiangyifen.ec2.servlet.debug;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.asteriskjava.manager.event.QueueEntryEvent;
import org.asteriskjava.manager.event.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.BridgetimeInfo;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.bean.MeettingRoomFirstJoinMemberInfo;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.entity.QueueRequestDetail;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.WuRuiShareData;
import com.jiangyifen.ec2.servlet.http.common.pojo.BridgeVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.HangupVo;
import com.jiangyifen.ec2.servlet.http.common.pojo.PopupIncomingVo;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;
import com.vaadin.Application;

/**
 * @Description 描述：用来获取内存中的信息，主要作用是用这个来判断是否存在内存泄露，或者方便查询数据
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/debug/memoryMonitor?shareDataItem=userToExten
 * 
 * @author  JRH
 * @date    2014年8月13日 下午5:13:01
 */
@SuppressWarnings("serial")
public class MemoryMonitorServlet extends HttpServlet{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); 

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.println("success"+" -----Time---> "+System.currentTimeMillis());
			
			String shareDataItem = request.getParameter("shareDataItem");
			shareDataItem = shareDataItem.replace(" ", "");
			
			logger.info("JRH - DEBUG IFACE 请求参数：  "+shareDataItem);
			
/** 基础信息类 *******************************/
			if("extenToUser".equals(shareDataItem) || "eu".equals(shareDataItem)) {
				Map<String, Long> extenToUser=ShareData.extenToUser;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ExtenToUser==========分机与用户的对应关系=========总条数:"+extenToUser.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten:extenToUser.keySet()){
					strBf.append("Exten:"+exten+" ---> UserId:"+extenToUser.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========ExtenToUser==========分机与用户的对应关系=========\r\n");
				out.println(strBf.toString());
			} else if("userToExten".equals(shareDataItem) || "ue".equals(shareDataItem)) {
				Map<Long, String> userToExten= ShareData.userToExten;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========UserToExten==========用户与分机的对应关系=========总条数:"+userToExten.size()+"\r\n");
				strBf.append("\r\n");
				for(Long userId:userToExten.keySet()){
					strBf.append("User:"+userId+" ---> Exten:"+userToExten.get(userId)+"\r\n");
				}
				strBf.append("\r\n==========UserToExten==========用户与分机的对应关系=========\r\n");
				out.println(strBf.toString());
			} else if("extenToDomain".equals(shareDataItem) || "ed".equals(shareDataItem)) {
				Map<String, Long> extenToDomain = ShareData.extenToDomain;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ExtenToDomain==========在线分机与租户所属域的对应关系=========总条数:"+extenToDomain.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten:extenToDomain.keySet()){
					strBf.append("Exten:"+exten+"  -->   DomainId:"+extenToDomain.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========ExtenToDomain==========在线分机与租户所属域的对应关系\r\n");
				out.println(strBf.toString());
			} else if("userToDomain".equals(shareDataItem) || "ud".equals(shareDataItem)) {
				Map<Long, Long> userToDomain = ShareData.userToDomain;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========UserToDomain==========在线用户与租户所属域的对应关系=========总条数:"+userToDomain.size()+"\r\n");
				strBf.append("\r\n");
				for(Long userId:userToDomain.keySet()){
					strBf.append("UserId:"+userId+"  -->  DomainId:"+userToDomain.get(userId)+"\r\n");
				}
				strBf.append("\r\n==========UserToDomain==========在线用户与租户所属域的对应关系\r\n");
				out.println(strBf.toString());
			} else if("extenToStaticOutline".equals(shareDataItem) || "eso".equals(shareDataItem)) {
				Map<String,String> extenToStaticOutline=ShareData.extenToStaticOutline;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========extenToStaticOutline==========分机与静态外线的对应关系=========总条数:"+extenToStaticOutline.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten:extenToStaticOutline.keySet()){
					strBf.append("Exten:"+exten+" ---> StaticOutline:"+extenToStaticOutline.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========extenToStaticOutline==========分机与静态外线的对应关系\r\n");
				out.println(strBf.toString());
			} else if("extenToDynamicOutline".equals(shareDataItem) || "edo".equals(shareDataItem)) {
				Map<String,String> extenToDynamicOutline=ShareData.extenToDynamicOutline;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========extenToDynamicOutline==========分机与动态外线的对应关系=========总条数:"+extenToDynamicOutline.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten:extenToDynamicOutline.keySet()){
					strBf.append("Exten:"+exten+" ---> DynamicOutline:"+extenToDynamicOutline.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========extenToDynamicOutline==========分机与动态外线的对应关系\r\n");
				out.println(strBf.toString());
			} else if("projectToOutline".equals(shareDataItem) || "po".equals(shareDataItem)) {
				Map<Long, String> projectToOutline = ShareData.projectToOutline;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ProjectToOutline==========项目到外线的对应关系=========总条数:"+projectToOutline.size()+"\r\n");
				strBf.append("\r\n");
				for(Long projectId:projectToOutline.keySet()){
					strBf.append("ProjectId:"+projectId+" ---> Outline:"+projectToOutline.get(projectId)+"\r\n");
				}
				strBf.append("\r\n==========ProjectToOutline==========项目到外线的对应关系\r\n");
				out.println(strBf.toString());
			} else if("outlineToProject".equals(shareDataItem) || "op".equals(shareDataItem)) {
				Map<String,Long> outlineToProject=ShareData.outlineToProject;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========OutlineToProject==========外线到项目的对应关系=========总条数:"+outlineToProject.size()+"\r\n");
				strBf.append("\r\n");
				for(String outline:outlineToProject.keySet()){
					strBf.append("Outline:"+outline+" ---> ProjectId:"+outlineToProject.get(outline)+"\r\n");
				}
				strBf.append("\r\n==========OutlineToProject==========外线到项目的对应关系\r\n");
				out.println(strBf.toString());
			} else if("extenToProject".equals(shareDataItem) || "ep".equals(shareDataItem)) {
				Map<String,Long> extenToProject=ShareData.extenToProject;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ExtenToProject==========分机到项目的对应关系=========总条数:"+extenToProject.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten:extenToProject.keySet()){
					strBf.append("Exten:"+exten+" ---> ProjectId:"+extenToProject.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========ExtenToProject==========分机到项目的对应关系\r\n");
				out.println(strBf.toString());
			} else if("projectToQueue".equals(shareDataItem) || "pq".equals(shareDataItem)) {
				Map<Long, String> projectToQueue = ShareData.projectToQueue;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ProjectToQueue==========项目到队列的对应关系=========总条数:"+projectToQueue.size()+"\r\n");
				strBf.append("\r\n");
				for(Long projectId:projectToQueue.keySet()){
					strBf.append("ProjectId:"+projectId+" ---> Queue:"+projectToQueue.get(projectId)+"\r\n");
				}
				strBf.append("\r\n==========ProjectToQueue==========项目到队列的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToDefaultOutline".equals(shareDataItem) || "ddo".equals(shareDataItem)) {
				Map<Long,String> domainToDefaultOutline=ShareData.domainToDefaultOutline;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========domainToDefaultOutline==========域跟默认外线的对应关系=========总条数:"+domainToDefaultOutline.size()+"\r\n");
				strBf.append("\r\n");
				for(Long domainId:domainToDefaultOutline.keySet()){
					strBf.append("Domain:"+domainId+" ---> DefaultOutline:"+domainToDefaultOutline.get(domainId)+"\r\n");
				}
				strBf.append("\r\n==========domainToDefaultOutline==========域跟默认外线的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToDefaultQueue".equals(shareDataItem) || "ddq".equals(shareDataItem)) {
				Map<Long, String> domainToDefaultQueue = ShareData.domainToDefaultQueue;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========DomainToDefaultQueue==========域跟默认队列的对应关系=========总条数:"+domainToDefaultQueue.size()+"\r\n");
				strBf.append("\r\n");
				for(Long domainId:domainToDefaultQueue.keySet()){
					strBf.append("DomainId:"+domainId+" ---> Queue:"+domainToDefaultQueue.get(domainId)+"\r\n");
				}
				strBf.append("\r\n==========DomainToDefaultQueue==========域跟默认队列的对应关系\r\n");
				out.println(strBf.toString());
			} else if("userToDepartment".equals(shareDataItem) || "udt".equals(shareDataItem)) {
				Map<Long, Long> userToDepartment = ShareData.userToDepartment;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========DomainToDefaultQueue==========用户与其部门的对应关系=========总条数:"+userToDepartment.size()+"\r\n");
				strBf.append("\r\n");
				for(Long userId:userToDepartment.keySet()){
					strBf.append("UserId:"+userId+" ---> DepartmentId:"+userToDepartment.get(userId)+"\r\n");
				}
				strBf.append("\r\n==========DomainToDefaultQueue==========用户与其部门的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToExts".equals(shareDataItem) || "de".equals(shareDataItem)) {
				Map<Long, List<String>> domainToExts = ShareData.domainToExts;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========DomainToExts==========各域跟分机的对应关系=========域总条数:"+domainToExts.size()+"\r\n");
				strBf.append("\r\n");
				for(Long domainId:domainToExts.keySet()){
					List<String> extenLs = domainToExts.get(domainId);
					strBf.append("DomainId:"+domainId+"  ,分机总数："+extenLs.size()+"  --->:"+extenLs+"\r\n");
				}
				strBf.append("\r\n==========DomainToExts==========各域跟分机的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToOutlines".equals(shareDataItem) || "do".equals(shareDataItem)) {
				Map<Long, List<String>> domainToOutlines = ShareData.domainToOutlines;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========DomainToOutlines==========各域跟外线的对应关系=========域总条数:"+domainToOutlines.size()+"\r\n");
				strBf.append("\r\n");
				for(Long domainId:domainToOutlines.keySet()){
					List<String> outlineLs = domainToOutlines.get(domainId);
					strBf.append("DomainId:"+domainId+"  ,外线总数："+outlineLs.size()+"  --->:"+outlineLs+"\r\n");
				}
				strBf.append("\r\n==========DomainToOutlines==========各域跟外线的对应关系\r\n");
				out.println(strBf.toString());
				
/** 通话信息 *******************************/
			} else if("peernameAndChannels".equals(shareDataItem) || "pac".equals(shareDataItem)) {
				Map<String, Set<String>> peernameAndChannels = ShareData.peernameAndChannels;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========PeernameAndChannels==========分机或外线与其相关通道的对应关系=========总条数:"+peernameAndChannels.size()+"\r\n");
				strBf.append("\r\n");
				for(String peerName:peernameAndChannels.keySet()){
					strBf.append("PeerName:"+peerName+"\r\n");
					Set<String> channels=peernameAndChannels.get(peerName);
					for(String channel:channels){
						strBf.append("				Channel:"+channel+"\r\n");
					}
				}
				strBf.append("\r\n==========PeernameAndChannels==========分机或外线与其相关通道的对应关系\r\n");
				out.println(strBf.toString());
			} else if("channelAndChannelSession".equals(shareDataItem) || "cacs".equals(shareDataItem)) {
				Map<String, ChannelSession> channelAndChannelSession = ShareData.channelAndChannelSession;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ChannelAndChannelSession==========通道与ChannelSession的对应关系=========总条数:"+channelAndChannelSession.size()+"\r\n");
				strBf.append("\r\n");
				for(String channel:channelAndChannelSession.keySet()){
					strBf.append("Channel:"+channel+"  -->   ChannelSession:"+channelAndChannelSession.get(channel)+"\r\n");
				}
				strBf.append("\r\n==========ChannelAndChannelSession==========通道与ChannelSession的对应关系\r\n");
				out.println(strBf.toString());
			} else if("channelToChannelLifeCycle".equals(shareDataItem) || "cclc".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("========= ChannelToChannelLifeCycle =========通道与通道的周期的对应关系=========总条数:"+ShareData.channelToChannelLifeCycle.size()+"\r\n");
				strBf.append("\r\n");
				for(String channel : ShareData.channelToChannelLifeCycle.keySet()) {
					strBf.append("Channel:"+channel+"  -->   ChannelLifeCycle:"+ShareData.channelToChannelLifeCycle.get(channel)+"\r\n");
				}
				strBf.append("\r\n=========ChannelToChannelLifeCycle=========通道与通道的周期的对应关系\r\n");
				out.println(strBf.toString());
			} else if("callerNumToChannelLifeCycle".equals(shareDataItem) || "cnclc".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("========= CallerNumToChannelLifeCycle =========呼入者的电话与通道的周期的对应关系=========总条数:"+ShareData.callerNumToChannelLifeCycle.size()+"\r\n");
				strBf.append("\r\n");
				for(String callerNum : ShareData.callerNumToChannelLifeCycle.keySet()) {
					strBf.append("CallerNum:"+callerNum+"  -->   ChannelLifeCycle:"+ShareData.callerNumToChannelLifeCycle.get(callerNum)+"\r\n");
				}
				strBf.append("\r\n=========CallerNumToChannelLifeCycle=========呼入者的电话与通道的周期的对应关系\r\n");
				out.println(strBf.toString());
			} else if("userExtenToHoldOnCallerChannels".equals(shareDataItem) || "uehocc".equals(shareDataItem)) {
				Map<String, List<String>> exten2Channels = ShareData.userExtenToHoldOnCallerChannels;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========UserExtenToHoldOnCallerChannels==========分机与呼叫保持中的电话通道的对应关系=========总条数:"+exten2Channels.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten : exten2Channels.keySet()) {
					strBf.append("Exten:" + exten + "  == hold on channels --> "+ exten2Channels.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========UserExtenToHoldOnCallerChannels==========分机与呼叫保持中的电话通道的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToIncomingDialInfoMap".equals(shareDataItem) || "didi".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("========= DomainToIncomingCallerAgiRequests =========所有域下Map<域的id, ConcurrentHashMap<主叫号码，agi请求>>的对应关系=========域总条数:"+ShareData.domainToIncomingDialInfoMap.size()+"\r\n");
				strBf.append("\r\n");
				for(Long id : ShareData.domainToIncomingDialInfoMap.keySet()) {
					Map<String, IncomingDialInfo> m = ShareData.domainToIncomingDialInfoMap.get(id);
					strBf.append(" domainId "+id+" ------------总呼入数："+m.size()+"\r\n");
					if(m != null) {
						for(String callerNum : m.keySet()) {
							strBf.append("CallerNum:" + callerNum + "  --> "+ m.get(callerNum)+"\r\n");
						}
					}
				}
				strBf.append("\r\n========= DomainToIncomingCallerAgiRequests =========所有域下Map<域的id, ConcurrentHashMap<主叫号码，agi请求>>的对应关系\r\n");
				out.println(strBf.toString());
				
			} else if("csrToPopupIncomingVoMap".equals(shareDataItem) || "cpiv".equals(shareDataItem)) {
				Map<String, PopupIncomingVo> csrToPopupIncomingVoMap = WuRuiShareData.csrToPopupIncomingVoMap;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========CsrToPopupIncomingVoMap==========用户与武睿来电信息的对应关系=========总条数:"+csrToPopupIncomingVoMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String username : csrToPopupIncomingVoMap.keySet()) {
					strBf.append("Username:" + username + "   --> "+ csrToPopupIncomingVoMap.get(username)+"\r\n");
				}
//				strBf.append("\r\n==========CsrToPopupIncomingVoMap==========用户与武睿来电信息的对应关系\r\n");
				strBf.append("\r\n");
				strBf.append("\r\n");
				
				Map<String, BridgeVo> csrToBridgeVoMap = WuRuiShareData.csrToBridgeVoMap;
				strBf.append("\r\n");
				strBf.append("==========csrToBridgeVoMap==========用户与武睿接起信息的对应关系=========总条数:"+csrToBridgeVoMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten : csrToBridgeVoMap.keySet()) {
					strBf.append("Exten:" + exten + " --> " + csrToBridgeVoMap.get(exten) + "\r\n");
				}
//				strBf.append("\r\n==========csrToBridgeVoMap==========用户与武睿接起信息的对应关系\r\n");
				strBf.append("\r\n");
				strBf.append("\r\n");
				
				Map<String, HangupVo> csrToHangupVoMap = WuRuiShareData.csrToHangupVoMap;
				strBf.append("\r\n");
				strBf.append("==========csrToHangupVoMap==========用户与武睿挂断信息的对应关系=========总条数:"+csrToHangupVoMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten : csrToHangupVoMap.keySet()) {
					strBf.append("Exten:" + exten + " --> " + csrToHangupVoMap.get(exten) + "\r\n");
				}
				strBf.append("\r\n");
//				strBf.append("\r\n==========csrToHangupVoMap==========用户与武睿挂断信息的对应关系\r\n");
				strBf.append("\r\n");
				strBf.append("\r\n");
				
				out.println(strBf.toString());
				
/** IVR 信息 *******************************/
			} else if("ivrMenusMap".equals(shareDataItem) || "imm".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("================= IVRMenu ====================IVR导航菜单集合=========总条数:"+ShareData.ivrMenusMap.size()+"\r\n");
				strBf.append("\r\n");
				for(IVRMenu menu : ShareData.ivrMenusMap.values()) {
					strBf.append(menu.toLoggerString()+"\r\n");
				}
				strBf.append("\r\n================= IVRMenu ====================IVR导航菜单集合\r\n");
				out.println(strBf.toString());
			} else if("outlineIdToIvrLinkMap".equals(shareDataItem) || "oiilm".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("================= OutlineIdToIvrLinkMap ====================外线与IVR导航菜单的对应关系=========外线总条数:"+ShareData.outlineIdToIvrLinkMap.size()+"\r\n");
				strBf.append("\r\n");
				for(Long key : ShareData.outlineIdToIvrLinkMap.keySet()) {
					for(OutlineToIvrLink link : ShareData.outlineIdToIvrLinkMap.get(key)) {
						strBf.append("外线编号："+key+"  --> 对应:"+link.toLoggerString()+"\r\n");
					}
				}
				strBf.append("\r\n================= OutlineIdToIvrLinkMap ====================外线与IVR导航菜单的对应关系\r\n");
				out.println(strBf.toString());
				
/** 实时监控 *******************************/
			} else if("statusEvents".equals(shareDataItem) || "se".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("================= StatusEvents ====================Asterisk-Sippeer状态事件的信息=========总条数:"+ShareData.statusEvents.size()+"\r\n");
				strBf.append("\r\n");
				for(StatusEvent se : ShareData.statusEvents) {
					strBf.append("\r\n"+se.toString()+"\r\n");
				}
				strBf.append("\r\n================= StatusEvents ====================Asterisk-Sippeer状态事件的信息\r\n");
				out.println(strBf.toString());
			} else if("extenStatusMap".equals(shareDataItem) || "esm".equals(shareDataItem)) {
				Map<String, ExtenStatus> extenStatusMap = ShareData.extenStatusMap;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ExtenStatusMap==========分机与分机状态的对应关系=========总条数:"+extenStatusMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String exten:extenStatusMap.keySet()){
					strBf.append("Exten:"+exten+"  -->   StatusMap:"+extenStatusMap.get(exten)+"\r\n");
				}
				strBf.append("\r\n==========ExtenStatusMap==========分机与分机状态的对应关系\r\n");
				out.println(strBf.toString());
			} else if("channelToBridgetime".equals(shareDataItem) || "cb".equals(shareDataItem)) {
				Map<String, BridgetimeInfo> channelToBridgetime = ShareData.channelToBridgetime;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========ChannelToBridgetime==========通道与接通情况信息的对应关系=========总条数:"+channelToBridgetime.size()+"\r\n");
				strBf.append("\r\n");
				for(String channel:channelToBridgetime.keySet()){
					strBf.append("Channel:"+channel+"  -->   StatusMap:"+channelToBridgetime.get(channel)+"\r\n");
				}
				strBf.append("\r\n==========ChannelToBridgetime==========通道与接通情况信息的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToConfigs".equals(shareDataItem) || "dc".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========DomainConfigs=========域跟配置信息的对应关系=========租户总条数:"+ShareData.domainToConfigs.size()+"\r\n");
				strBf.append("\r\n");
				for(Long id : ShareData.domainToConfigs.keySet()) {
					Map<String, Boolean> m = ShareData.domainToConfigs.get(id);
					strBf.append("\r\n domainId "+id+" ------------配置项总数:"+m.size()+"\r\n");
					for(String key : m.keySet()) {
						String value = m.get(key) ? "是" : "否";
						strBf.append("	      "+ key+ "	:	"+value+"\r\n");
					}
				}
				strBf.append("\r\n=========DomainConfigs=========域跟配置信息的对应关系\r\n");
				out.println(strBf.toString());
			} else if("queue2Members".equals(shareDataItem) || "qm".equals(shareDataItem)) {
				Map<String, List<String>> queue2Members = ShareData.queue2Members;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========Queue2Members=========队列与队列成员的对应关系=========队列总条数:"+queue2Members.size()+"\r\n");
				strBf.append("\r\n");
				for(String queue:queue2Members.keySet()) {
					List<String> memberLs = queue2Members.get(queue);
					strBf.append("Queue:"+queue+"   成员总数："+memberLs.size()+" --->"+memberLs+"\r\n");
				}
				strBf.append("\r\n=========Queue2Members=========队列与队列成员的对应关系\r\n");
				out.println(strBf.toString());
			} else if("queueRequestDetailMap".equals(shareDataItem) || "qrdm".equals(shareDataItem)) {
				Map<String, QueueRequestDetail> queueRequestDetailMap = ShareData.queueRequestDetailMap;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========QueueRequestDetailMap=========电话的UniqueId跟队列电话详单的对应关系=========总条数:"+queueRequestDetailMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String uniqueid:queueRequestDetailMap.keySet()) {
					strBf.append("Uniqueid:"+uniqueid+"   --->"+queueRequestDetailMap.get(uniqueid)+"\r\n");
				}
				strBf.append("\r\n=========QueueRequestDetailMap=========电话的UniqueId跟队列电话详单的对应关系\r\n");
				out.println(strBf.toString());
				
//---自动外呼数据---------//
			} else if("queueToWaiters".equals(shareDataItem) || "qw".equals(shareDataItem)) {
				Map<String,List<QueueEntryEvent>> queueToWaiters = AutoDialHolder.queueToWaiters;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("自动外呼--AutoDialHolder \r\n");
				strBf.append("=========QueueToWaiters=========队列中的排队人信息=========队列总条数:"+queueToWaiters.size()+"\r\n");
				strBf.append("\r\n");
				for(String queue:queueToWaiters.keySet()) {
					List<QueueEntryEvent> qeeLs = queueToWaiters.get(queue);
					strBf.append("Queue:"+queue+"   排队人数:"+qeeLs.size()+"\r\n");
					for(QueueEntryEvent qee : qeeLs) {
						strBf.append("       排队者信息:"+qee+"\r\n");
					}
				}
				strBf.append("\r\n=========QueueToWaiters=========队列中的排队人信息\r\n");
				out.println(strBf.toString());
			} else if("queueToCallers".equals(shareDataItem) || "qc".equals(shareDataItem)) {
				Map<String,Integer> queueToCallers = AutoDialHolder.queueToCallers;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("自动外呼--AutoDialHolder \r\n");
				strBf.append("=========QueueToCallers=========队列中跟队列的通话数量的对应关系=========总条数:"+queueToCallers.size()+"\r\n");
				strBf.append("\r\n");
				for(String queue:queueToCallers.keySet()) {
					strBf.append("Queue:"+queue+"   通话总数:"+queueToCallers.get(queue)+"\r\n");
				}
				strBf.append("\r\n=========QueueToWaiters=========队列中跟队列的通话数量的对应关系\r\n");
				out.println(strBf.toString());
			} else if("queueToLoggedIn".equals(shareDataItem) || "qli".equals(shareDataItem)) {
				Map<String,Integer> queueToLoggedIn = AutoDialHolder.queueToLoggedIn;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("自动外呼--AutoDialHolder \r\n");
				strBf.append("=========QueueToLoggedIn=========队列中跟在线分机成员总数的对应关系=========总条数:"+queueToLoggedIn.size()+"\r\n");
				strBf.append("\r\n");
				for(String queue:queueToLoggedIn.keySet()) {
					strBf.append("Queue:"+queue+"   在线分机成员总数:"+queueToLoggedIn.get(queue)+"\r\n");
				}
				strBf.append("\r\n=========QueueToLoggedIn=========队列中跟在线分机成员总数的对应关系\r\n");
				out.println(strBf.toString());
			} else if("queueToAvailable".equals(shareDataItem) || "qa".equals(shareDataItem)) {
				Map<String,Integer> queueToAvailable = AutoDialHolder.queueToAvailable;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("自动外呼--AutoDialHolder \r\n");
				strBf.append("=========QueueToAvailable=========队列中跟可用成员总数的对应关系=========总条数:"+queueToAvailable.size()+"\r\n");
				strBf.append("\r\n");
				for(String queue:queueToAvailable.keySet()) {
					strBf.append("Queue:"+queue+"   通话总数:"+queueToAvailable.get(queue)+"\r\n");
				}
				strBf.append("\r\n=========QueueToAvailable=========队列中跟可用成员总数的对应关系\r\n");
				out.println(strBf.toString());
			} else if("nameToThread".equals(shareDataItem) || "nt".equals(shareDataItem)) {
				Map<String, Thread> nameToThread = AutoDialHolder.nameToThread;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("自动外呼--AutoDialHolder \r\n");
				strBf.append("=========NameToThread=========线程的名字跟线程的对应关系=========总条数:"+nameToThread.size()+"\r\n");
				strBf.append("\r\n");
				for(String name:nameToThread.keySet()) {
					strBf.append("ThreadName:"+name+"   线程:"+nameToThread.get(name)+"\r\n");
				}
				strBf.append("\r\n=========NameToThread=========线程的名字跟线程的对应关系\r\n");
				out.println(strBf.toString());
				
/** 黑名单 *******************************/
			} else if("domainToIncomingBlacklist".equals(shareDataItem) || "dib".equals(shareDataItem)) {
				Map<Long, List<String>> domainToIncomingBlacklist = ShareData.domainToIncomingBlacklist;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========DomainToIncomingBlacklist=========各域跟呼入方向的黑名单的对应关系=========租户总条数:"+domainToIncomingBlacklist.size()+"\r\n");
				strBf.append("\r\n");
				for(Long id : domainToIncomingBlacklist.keySet()) {
					List<String> incomingBlackList = ShareData.domainToIncomingBlacklist.get(id);
					strBf.append(" DomainId:"+id+"   黑名单总数："+incomingBlackList.size()+" --->"+incomingBlackList+"\r\n");
				}
				strBf.append("\r\n=========DomainToIncomingBlacklist=========各域跟呼入方向的黑名单的对应关系\r\n");
				out.println(strBf.toString());
			} else if("domainToOutgoingBlacklist".equals(shareDataItem) || "dob".equals(shareDataItem)) {
				Map<Long, List<String>> domainToOutgoingBlacklist = ShareData.domainToOutgoingBlacklist;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========DomainToOutgoingBlacklist=========各域跟呼出方向的黑名单的对应关系=========租户总条数:"+domainToOutgoingBlacklist.size()+"\r\n");
				strBf.append("\r\n");
				for(Long id : domainToOutgoingBlacklist.keySet()) {
					List<String> outgoingBlackList = ShareData.domainToOutgoingBlacklist.get(id);
					strBf.append(" DomainId:"+id+"   黑名单总数："+outgoingBlackList.size()+" --->"+outgoingBlackList+"\r\n");
				}
				strBf.append("\r\n=========DomainToOutgoingBlacklist=========各域跟呼出方向的黑名单的对应关系\r\n");
				out.println(strBf.toString());
			} else if("outlineToIncomingBlacklist".equals(shareDataItem) || "oib".equals(shareDataItem)) {
				Map<Long, List<String>> outlineToIncomingBlacklist = ShareData.outlineToIncomingBlacklist;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========OutlineToIncomingBlacklist=========外线与呼入方向的黑名单的对应关系=========外线总条数:"+outlineToIncomingBlacklist.size()+"\r\n");
				strBf.append("\r\n");
				for(Long outlineId : ShareData.outlineToIncomingBlacklist.keySet()) {
					List<String> m = ShareData.outlineToIncomingBlacklist.get(outlineId);
					strBf.append("外线Id:"+outlineId+"   黑名单---> "+m+"\r\n");
				}
				strBf.append("\r\n=========OutlineToIncomingBlacklist=========外线与呼入方向的黑名单的对应关系\r\n");
				out.println(strBf.toString());
			} else if("outlineToOutgoingBlacklist".equals(shareDataItem) || "oob".equals(shareDataItem)) {
				Map<Long, List<String>> outlineToOutgoingBlacklist = ShareData.outlineToOutgoingBlacklist;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========OutlineToOutgoingBlacklist=========外线与呼出方向的黑名单的对应关系=========外线总条数:"+outlineToOutgoingBlacklist.size()+"\r\n");
				strBf.append("\r\n");
				for(Long outlineId : ShareData.outlineToOutgoingBlacklist.keySet()) {
					List<String> m = ShareData.outlineToOutgoingBlacklist.get(outlineId);
					strBf.append("外线Id:"+outlineId+"   黑名单---> "+m+"\r\n");
				}
				strBf.append("\r\n=========OutlineToOutgoingBlacklist=========外线与呼出方向的黑名单的对应关系\r\n");
				out.println(strBf.toString());

/** 会议室相关信息 *******************************/
			} else if("meettingToFirstJoinMemberMap".equals(shareDataItem) || "mtfjm".equals(shareDataItem)) {
				Map<String, MeettingRoomFirstJoinMemberInfo> meettingToFirstJoinMemberMap = ShareData.meettingToFirstJoinMemberMap;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========MeettingToFirstJoinMemberMap=========会议室号跟第一个进入会议室的成员通道信息的对应关系=========总条数:"+meettingToFirstJoinMemberMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String meetingRoom : meettingToFirstJoinMemberMap.keySet()) {
					strBf.append("会议室:"+meetingRoom+"   通道信息---> "+meettingToFirstJoinMemberMap.get(meetingRoom)+"\r\n");
				}
				strBf.append("\r\n=========MeettingToFirstJoinMemberMap=========会议室号跟第一个进入会议室的成员通道信息的对应关系\r\n");
				out.println(strBf.toString());
				
			} else if("meetingToMemberRecords".equals(shareDataItem) || "mtmr".equals(shareDataItem)) {
				Map<String, ConcurrentHashMap<String, MeettingDetailRecord>> meetingToMemberRecords = ShareData.meetingToMemberRecords;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("=========MeetingToMemberRecords=========Map<会议室号，ConcurrentHashMap<会议室成员的通道, 成员详情记录信息>>=========会议室总数:"+meetingToMemberRecords.size()+"\r\n");
				strBf.append("\r\n");
				for(String meetingRoom : meetingToMemberRecords.keySet()) {
					ConcurrentHashMap<String, MeettingDetailRecord> mdrMap = meetingToMemberRecords.get(meetingRoom);
					strBf.append("会议室:"+meetingRoom+"   , 会议成员个数:"+mdrMap.size()+"\r\n");
					for(MeettingDetailRecord mdr : mdrMap.values()) {
						strBf.append("        成员 ---> "+mdr+"\r\n");
					}
				}
				strBf.append("\r\n=========MeetingToMemberRecords=========Map<会议室号，ConcurrentHashMap<会议室成员的通道, 成员详情记录信息>>\r\n");
				out.println(strBf.toString());
				
			} else if("meettingRoomExtenToMgrIdMap".equals(shareDataItem) || "mretmim".equals(shareDataItem)) {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				Map<String, Long> meettingRoomExtenToMgrIdMap = ShareData.meettingRoomExtenToMgrIdMap;
				strBf.append("=========MeettingRoomExtenToMgrIdMap=========管理员发起的会议室Map<分机号，管理员编号>=========总条数:"+meettingRoomExtenToMgrIdMap.size()+"\r\n");
				strBf.append("\r\n");
				for(String meetingRoom : meettingRoomExtenToMgrIdMap.keySet()) {
					strBf.append("会议室:"+meetingRoom+"   , 管理员用户编号:"+meettingRoomExtenToMgrIdMap.get(meetingRoom)+"\r\n");
				}
				strBf.append("\r\n=========MeettingRoomExtenToMgrIdMap=========管理员发起的会议室Map<分机号，管理员编号>\r\n");
				out.println(strBf.toString());

/** 其他信息 *******************************/
			} else if("userToApp".equals(shareDataItem) || "ua".equals(shareDataItem)) {
				Map<Long, Application> userToApplication = ShareData.userToApp;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========UserToApplication==========登录界面的用户与Application的对应关系\r\n");
				strBf.append("\r\n");
				for(Long userId:userToApplication.keySet()){
					strBf.append("UserId:"+userId+"   Application:"+userToApplication.get(userId).hashCode()+"\r\n");
				}
				strBf.append("\r\n==========UserToApplication==========登录界面的用户与Application的对应关系\r\n");
				out.println(strBf.toString());
				
			} else if("userToSession".equals(shareDataItem) || "us".equals(shareDataItem)) {
				Map<Long, HttpSession> userToSession = ShareData.userToSession;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========UserToSession==========登录界面的用户与Session的对应关系\r\n");
				strBf.append("\r\n");
				for(Long userId:userToSession.keySet()){
					strBf.append("UserId:"+userId+"   , Session:"+userToSession.get(userId)+"\r\n");
				}
				strBf.append("\r\n==========UserToSession==========登录界面的用户与Session的对应关系\r\n");
				out.println(strBf.toString());
				
			} else if("csrToToolBar".equals(shareDataItem) || "ct".equals(shareDataItem)) {
				Map<Long, CsrToolBar> csrToToolBar = ShareData.csrToToolBar;
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("==========UserToSession==========登录界面的用户与坐席界面工具栏的对应关系\r\n");
				strBf.append("\r\n");
				for(Long userId:csrToToolBar.keySet()){
					strBf.append("UserId:"+userId+"   , CsrToolBar:"+csrToToolBar.get(userId).getClass()+"\r\n");
				}
				strBf.append("\r\n==========UserToSession==========登录界面的用户与坐席界面工具栏的对应关系\r\n");
				out.println(strBf.toString());
				
			} else {
				StringBuffer strBf = new StringBuffer();
				strBf.append("\r\n");
				strBf.append("请求参数shareDataItem 可以为下面任意值或者使用括号中对应的缩写值\r\n");
				strBf.append("\r\n");
				strBf.append("/** 基础信息类 *******************************/ \r\n");
				strBf.append("extenToUser(eu)						userToExten(ue) \r\n");
				strBf.append("extenToDomain(ed)					userToDomain(ud) \r\n");
				strBf.append("extenToStaticOutline(eso)				extenToDynamicOutline(edo) \r\n");
				strBf.append("projectToOutline(po)					outlineToProject(op) \r\n");
				strBf.append("extenToProject(ep)					projectToQueue(pq) \r\n");
				strBf.append("domainToDefaultOutline(ddo)				domainToDefaultQueue(ddq) \r\n");
				strBf.append("userToDepartment(udt)					domainToExts(de) \r\n");
				strBf.append("domainToOutlines(do) \r\n");
				strBf.append("\r\n");
				strBf.append("/** 通话信息类 *******************************/ \r\n");
				strBf.append("peernameAndChannels(pac)				channelAndChannelSession(cacs) \r\n");
				strBf.append("channelToChannelLifeCycle(cclc)				callerNumToChannelLifeCycle(cnclc) \r\n");
				strBf.append("userExtenToHoldOnCallerChannels(uehocc)			domainToIncomingDialInfoMap(didi) \r\n");
				strBf.append("\r\n");
				strBf.append("/** IVR 信息类 *******************************/ \r\n");
				strBf.append("ivrMenusMap(imm)					outlineIdToIvrLinkMap(oiilm) \r\n");
				strBf.append("\r\n");
				strBf.append("/** 实时监控类 *******************************/ \r\n");
				strBf.append("statusEvents(se)					extenStatusMap(esm) \r\n");
				strBf.append("channelToBridgetime(cb)					domainToConfigs(dc) \r\n");
				strBf.append("queue2Members(qm)					queueRequestDetailMap(qrdm) \r\n");
				strBf.append("//---自动外呼数据类---AutoDialHolder------//\r\n");
				strBf.append("queueToWaiters(qw)					queueToCallers(qc) \r\n");
				strBf.append("queueToLoggedIn(qli)					queueToAvailable(qa) \r\n");
				strBf.append("nameToThread(nt) \r\n");
				strBf.append("\r\n");
				strBf.append("/** 黑名单类 *******************************/ \r\n");
				strBf.append("domainToIncomingBlacklist(dib)				domainToOutgoingBlacklist(dob) \r\n");
				strBf.append("outlineToIncomingBlacklist(oib)				outlineToOutgoingBlacklist(oob) \r\n");
				strBf.append("\r\n");
				strBf.append("/** 会议室相关信息类 *******************************/ \r\n");
				strBf.append("meettingToFirstJoinMemberMap(mtfjm)			meetingToMemberRecords(mtmr) \r\n");
				strBf.append("meettingRoomExtenToMgrIdMap(mretmim) \r\n");
				strBf.append("\r\n");
				strBf.append("/** 其他信息类 *******************************/ \r\n");
				strBf.append("userToApp(ua)						userToSession(us) \r\n");
				strBf.append("csrToPopupIncomingVoMap					csrToPopupIncomingVoMap \r\n");
				strBf.append("csrToToolBar() \r\n");
				strBf.append("\r\n");
				out.println(strBf.toString());
			}
			
			//======================向客户端返回结果============================//
			out.close();
		} catch (Exception e) {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			out.println("获取失败，出现异常！");
			out.close();
			logger.error("获取失败，出现异常！"+e.getMessage(), e);
		}
		
	}


	/***
	 * 
	 *	/** 基础信息类 ******************************* 
		extenToUser(eu)							userToExten(ue) 
		extenToDomain(ed)						userToDomain(ud) 
		extenToStaticOutline(eso)				extenToDynamicOutline(edo) 
		projectToOutline(po)					outlineToProject(op) 
		extenToProject(ep)						projectToQueue(pq) 
		domainToDefaultOutline(ddo)				domainToDefaultQueue(ddq) 
		userToDepartment(udt)					domainToExts(de) 
		domainToOutlines(do) 
		
		/** 通话信息类 ******************************* 
		peernameAndChannels(pac)				channelAndChannelSession(cacs) 
		channelToChannelLifeCycle(cclc)			callerNumToChannelLifeCycle(cnclc) 
		userExtenToHoldOnCallerChannels(uehocc)	domainToIncomingDialInfoMap(didi) 
		
		/** IVR 信息类 ******************************* 
		ivrMenusMap(imm)						outlineIdToIvrLinkMap(oiilm) 
		
		/** 实时监控类 ******************************* 
		statusEvents(se)						extenStatusMap(esm) 
		channelToBridgetime(cb)					domainToConfigs(dc) 
		queue2Members(qm)						queueRequestDetailMap(qrdm) 
		
		//---自动外呼数据类---AutoDialHolder------//
		queueToWaiters(qw)						queueToCallers(qc) 
		queueToLoggedIn(qli)					queueToAvailable(qa) 
		nameToThread(nt) 
		
		/** 黑名单类 ******************************* 
		domainToIncomingBlacklist(dib)			domainToOutgoingBlacklist(dob) 
		outlineToIncomingBlacklist(oib)			outlineToOutgoingBlacklist(oob) 
		
		/** 会议室相关信息类 ******************************* 
		meettingToFirstJoinMemberMap(mtfjm)		meetingToMemberRecords(mtmr) 
		meettingRoomExtenToMgrIdMap(mretmim) 
		
		/** 其他信息类 *******************************
		userToApp(ua)							userToSession(us)
		csrToToolBar()
	
	 *
	 ***/
	
}
