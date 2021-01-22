package com.jiangyifen.ec2.ami;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.CdrEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.WuRuiShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.HangupVo;
import com.jiangyifen.ec2.servlet.http.common.utils.FastJsonUtil;
import com.jiangyifen.ec2.utils.ExternalInterface;
import com.jiangyifen.ec2.utils.HttpIfaceUtil;
import com.jiangyifen.ec2.utils.LoggerUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 监听CdrEvent事件
 * 
 * @author chb
 */
public class CdrEventListener extends AbstractManagerEventListener {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private CdrService cdrService;
	private CommonService commonService;
	private UserService userService;
	private QueueService queueService;
	private MarketingProjectService marketingProjectService;
	private TelephoneService telephoneService;

	/**
	 * 监听CDR事件
	 */
	@Override
	protected void handleEvent(CdrEvent event) {
		// System.out.println(event);
		if("s".equals(event.getDestination())) {
			logger.warn("jinht invalid CDR, CdrEvents's destination context is 's'!  src = " + event.getSrc() + " destination = " +  event.getDestination());
			return;
		}
		
		System.out.println(event);
		
		if(event.getUniqueId() == null) {
			logger.warn("JRH invalid CDR，CdrEvent's uniqueid is null!  src = " + event.getSrc() + " destination = " +  event.getDestination());
			return;
		}

		
		if("default".equals(event.getDestinationContext())) {
			logger.warn("JRH invalid CDR，CdrEvent's destination context is 'default'!  src = " + event.getSrc() + " destination = " +  event.getDestination());
			return;
		}
		
		//logger.info("T1_" + event.getUniqueId() + "_" + event.toString());

		try {
			// 从Event中取出我们需要的字段
			String accountCode = event.getAccountCode();
			String amaflags = event.getAmaFlags();
			Date answerTimeDate = event.getAnswerTimeAsDate();
			Integer billableSeconds = event.getBillableSeconds();
			String callerId = event.getCallerId();
			String channel = event.getChannel();
			String destination = event.getDestination();
			String destinationChannel = event.getDestinationChannel();
			String destinationContext = event.getDestinationContext();
			
			/* 老版本里面，在开启自动慢拨号的时候，由于该线程默认会每 5s 执行一次，所以在界面会出现 CDR 记录没有客户编号或者客户编号为 345 以及主叫号码为空的问题  */
			if("autodialcounter".equals(destinationContext)){
				return;
			}
			
			String disposition = event.getDisposition();
			Integer duration = event.getDuration();
			Date endTimeDate = event.getEndTimeAsDate();
			String lastApplication = event.getLastApplication();
			String lastData = event.getLastData();
			String src = event.getSrc();
			Date startTimeDate = event.getStartTimeAsDate();
			String uniqueId = event.getUniqueId();
			String userField = event.getUserField();

			if(channel.startsWith("DAHDI/pseudo-")) {	// 这种CDR 是三方会议导致的，不需要存，三方会议会自行存储
				logger.warn("JRH CdrEvent this cdr create by meeting room , don't need save--> 不合乎要求的呼叫记录，该记录将被忽略，不存入数据");
				return;
			}
			
			// ============================ Extended fields
			// ======================================//
			if (userService == null) {
				userService = SpringContextHolder.getBean("userService");
			}
			if (queueService == null) {
				queueService = SpringContextHolder.getBean("queueService");
			}
			if (cdrService == null) {
				cdrService = SpringContextHolder.getBean("cdrService");
				commonService = SpringContextHolder.getBean("commonService");
			}
			if (marketingProjectService == null) {
				marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
			}
			if (telephoneService == null) {
				telephoneService = SpringContextHolder.getBean("telephoneService");
			}
			//logger.info("T2_" + event.getUniqueId());

			Cdr cdr = new Cdr();
			String taskId = "";
			Long domainId = null;
			taskId = storeSrcInfo(cdr, userField, event);
			
			// 可能在cdr 的参数中已经获取到了域的信息
			if(!cdr.getDomainId().equals(0L)) {
				domainId = cdr.getDomainId();
			}
			
			//logger.info("T3_" + event.getUniqueId());
			// 用户信息
			Long destUserId = null;
			String destEmpNo = null;
			String destUsername = null;
			String destRealName = null;

			// 部门信息
			Long destDeptId = null;
			String destDeptName = null;

			String callDirection = "";
			// 主叫
			String srcSippeer = findSrcSippeer(channel);

			domainId = initialCdrDomainId(cdr, domainId, destinationContext, destination, lastData, srcSippeer);

			Long userId = ShareData.extenToUser.get(srcSippeer);
			if(userId == null) {
				userId = cdr.getSrcUserId();
			}
			
			/**
			 * 详见 com.jiangyifen.ec2.bean.CdrDirection
			 */
			if (userId != null) {
				callDirection += "e2";
			} else {
				if(userId == null && domainId != null) {
					List<String> extLs = ShareData.domainToExts.get(domainId);
					if(extLs != null && extLs.contains(src)) {
						callDirection += "e2";
					} else {
						callDirection += "o2";
					}
				} else {
					callDirection += "o2";
				}
			}
			
			User srcUser = null;
			if(callDirection.startsWith("e2")) {	// jrh 如果呼叫方向起始点是分机，则需要判断坐席用户的信息是否全 [2014-06-25 开始]
				if(userId != null) {
					srcUser = userService.get(userId);
				}
				
				if(cdr.getSrcUserId() == null) {
					cdr.setSrcUserId(userId);
					
					if (srcUser != null) { // 主叫是外线
						cdr.setSrcEmpNo(srcUser.getEmpNo());
						cdr.setSrcRealName(srcUser.getRealName());
						cdr.setSrcUsername(srcUser.getUsername());
						Department dept = srcUser.getDepartment();
						if (dept != null) {
							cdr.setSrcDeptId(dept.getId());
							cdr.setSrcDeptName(dept.getName());
						}
					}
				}
			}	// jrh [2014-06-25 结束]
			
			// 被叫
			String destPeerRefer = destination;		// 被叫信息，默认为 dialplan 中的 destination 值
			String destSippeer = null;
			User destUser = null;

			if (destinationChannel != null) { 	// 可能呼叫没有建立，找不到DestChannel
				destSippeer = findDestSippeer(destinationChannel);
				destPeerRefer = destSippeer;		// 如果存在被叫通道，则被叫信息则改成被叫通道中对应的SipPeer 值

				// jrh 解决呼入没有domainId 的情况
				domainId = initialCdrDomainId(cdr, domainId, destinationContext, destination, lastData, destSippeer);
			}
			
//			if(destPeerRefer != null) {
			if(StringUtils.isNotBlank(destPeerRefer)) {
				
				/*****************************************************************************************/
				/*****************************************************************************************/
				//				TODO 这里如果内存出现泄露，可能会导致原本是呼入的电话，被统计成外传外的电话，原因是根据分机找不到用户编号
				/*****************************************************************************************/
				/*****************************************************************************************/
				
				destUserId = ShareData.extenToUser.get(destPeerRefer);
				
				if (destUserId != null) {
					destUser = userService.get(destUserId);
				}

				if(srcUser != null && destUser != null) {	// 处理跨域呼叫问题，比如租户1下1001 呼叫 800180， 而800180 是另一个租户2的分机，此时不应该写被叫坐席的信息
					Long srcDid = srcUser.getDomain().getId();
					Long destDid = destUser.getDomain().getId();
					if(!srcDid.equals(destDid)) {
						destUserId = null;
						destUser = null;
					}
				}
				
				if (destUser != null) { // 主叫是外线
					destEmpNo = destUser.getEmpNo();
					destUsername = destUser.getUsername();
					destRealName = destUser.getRealName();
					Department department = destUser.getDepartment();
					if (department != null) {
						destDeptId = department.getId();
						destDeptName = department.getName();
					}
				}
			}
			
			String calledNum = "";
			if (destination != null) {
				// jrh 如果被叫号码已经写入，则，不需要再写，因为在storeSrcInfo 时已经做过了
				if ("".equals(cdr.getDestination())) {
					cdr.setDestination(destination);
					calledNum = destination;
				} else {
					calledNum = cdr.getDestination();
				}
				domainId = initialCdrDomainId(cdr, domainId, destinationContext, destination, lastData, destination);
			}

			// 将解析出来的字段存入CDR
			if (src != null) {
				cdr.setSrc(src);
				domainId = initialCdrDomainId(cdr, domainId, destinationContext, destination, lastData, src);
			}

			// jrh start 解决呼叫黑名单导致的呼叫方向错误问题，如果不加，呼叫方向就成了 内部呼叫
			if (calledNum.contains("blacklist")) {
				callDirection += "o";
			}	// jrh end
			else if (destUser != null) {
				callDirection += "e";
			} else if (domainId != null) {	// 如果没有被叫坐席，那也不能直接断定被叫是“分机/外线”，还是“外部手机”，所以需要进一步判断
				List<String> extLs = ShareData.domainToExts.get(domainId);
				List<String> outlineLs = ShareData.domainToOutlines.get(domainId);
				String dnt = cdr.getDestination();
				if(extLs != null && !extLs.contains(dnt) && outlineLs != null && !outlineLs.contains(dnt)) {	// 只要被叫号码不是分机也不是外线，则就认为是呼手机
					callDirection += "o";
				} else {
					callDirection += "e";
				}
				
			} else {	// JRH domainId 都不存在，则该呼叫记录本身没有什么价值，所以就默认为被叫为分机吧
				callDirection += "e";
			}
			
			CdrDirection direction = null;
			if (callDirection.equals("e2e")) {
				direction = CdrDirection.e2e;
			} else if (callDirection.equals("e2o")) {
				direction = CdrDirection.e2o;
			} else if (callDirection.equals("o2e")) {
				direction = CdrDirection.o2e;
			} else if (callDirection.equals("o2o")) {
				direction = CdrDirection.o2o;
			}
			
			
			if (direction != null) {
				cdr.setCdrDirection(direction);
			}
			if (accountCode != null) {
				cdr.setAccountCode(accountCode);
			}
			if (amaflags != null) {
				cdr.setAmaflags(amaflags);
			}
			if (answerTimeDate != null) {
				cdr.setAnswerTimeDate(answerTimeDate);
			}
			if (billableSeconds != null) {
				cdr.setBillableSeconds(billableSeconds);
			}
			if (callerId != null) {
				cdr.setCallerId(callerId);
			}
			
			
			try {
				if (channel != null) {
					cdr.setChannel(channel);
					
					// jrh 统计振铃时长和双方实际通话时长
					ChannelLifeCycle selfChannelLifeCycle = ShareData.channelToChannelLifeCycle.get(channel);
					if (selfChannelLifeCycle != null) {
						
						Date endTime = event.getEndTimeAsDate();
						Date originateCallTime = selfChannelLifeCycle.getOriginateDialTime();
						Date bridgedTime = selfChannelLifeCycle.getBridgedTime();
						
						if (originateCallTime != null) {
							Integer ringDuration = 0;
							Integer ec2_billableSeconds = 0;
							if (bridgedTime != null) {
								ec2_billableSeconds = (int) (((endTime.getTime() + 500) - bridgedTime.getTime()) / 1000);
								if (ec2_billableSeconds < 0) {
									ec2_billableSeconds = 0;
								}
								cdr.setEc2_billableSeconds(ec2_billableSeconds);
								cdr.setBridgeTimeDate(bridgedTime);
								cdr.setIsBridged(true);

								// v1.7.5.1 20140604     客凯易新增if 判断  lastInQueueTimeDate != null， 该版本之前，lastInQueueTimeDate 一定为空
								Date lastInQueueTimeDate = cdr.getLastInQueueTimeDate();
								if(lastInQueueTimeDate != null) {
									ringDuration = (int) (((bridgedTime.getTime() + 500) - lastInQueueTimeDate.getTime()) / 1000);
								} else {
									ringDuration = (int) (((bridgedTime.getTime() + 500) - originateCallTime.getTime()) / 1000);
								}
								
								ShareData.channelToChannelLifeCycle.remove(selfChannelLifeCycle.getBridgedChannel());
							} else {
								// v1.7.5.1 20140604     客凯易新增if 判断  lastInQueueTimeDate != null， 该版本之前，lastInQueueTimeDate 一定为空
								Date lastInQueueTimeDate = cdr.getLastInQueueTimeDate();
								if(lastInQueueTimeDate != null) {
									ringDuration = (int) (((event.getEndTimeAsDate().getTime() + 500) - lastInQueueTimeDate.getTime()) / 1000);
								} else {
									if(CdrDirection.o2e.equals(direction)) {	// 如果是呼入，并且来电客户没有进队列，则振铃时长为 0 
										ringDuration = 0;
									} else {
										ringDuration = (int) (((event.getEndTimeAsDate().getTime() + 500) - originateCallTime.getTime()) / 1000);
									}
								}
							}
							if (ringDuration < 0) {
								ringDuration = 0;
							}
							cdr.setRingDuration(ringDuration);
						}

						// 清理内存
						ShareData.channelToChannelLifeCycle.remove(channel);
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (destDeptId != null) {
				cdr.setDestDeptId(destDeptId);
			}
			if (destDeptName != null) {
				cdr.setDestDeptName(destDeptName);
			}
			if (destEmpNo != null) {
				cdr.setDestEmpNo(destEmpNo);
			}
			if (destinationChannel != null) {
				cdr.setDestinationChannel(destinationChannel);
			}
			if (destinationContext != null) {
				cdr.setDestinationContext(destinationContext);
			}
			if (destRealName != null) {
				cdr.setDestRealName(destRealName);
			}
			if (destUserId != null) {
				cdr.setDestUserId(destUserId);
			}
			if (destUsername != null) {
				cdr.setDestUsername(destUsername);
			}
			if (disposition != null) {
				cdr.setDisposition(disposition);
			}
			if (duration != null) {
				cdr.setDuration(duration);
			}
			if (endTimeDate != null) {
				cdr.setEndTimeDate(endTimeDate);
			} else {	// JRH 2015-01-14 ：如果没有结束时间（具体什么情况会导致这种情况，暂不明确），则用时间监听到的时间为准
				cdr.setEndTimeDate(event.getDateReceived());
			}
			if (lastApplication != null) {
				cdr.setLastApplication(lastApplication);
			}
			if (lastData != null) {
				cdr.setLastData(lastData);
			}
			if (startTimeDate != null) {
				cdr.setStartTimeDate(startTimeDate);
			}
			if (uniqueId != null) {
				cdr.setUniqueId(uniqueId);
			}

			// jrh 如果是普通呼入而非自动外呼，这里就一定会为空，这是一个漏洞:解决呼入没有项目的名称的问题
			String projectName = StringUtils.trimToEmpty(cdr.getProjectName());
			if ("".equals(projectName)) {
				boolean isfinded = false;
				String outlineName = cdr.getOutlineName();
				if(!"".equals(outlineName)) {		// 优先根据外线判断项目	20140711
					MarketingProject marketingProject = marketingProjectService.getAllByOutlineName(outlineName, domainId);
					if(marketingProject != null) {
						cdr.setProjectId(marketingProject.getId());
						cdr.setProjectName(marketingProject.getProjectName());
						isfinded = true;
					}
				} 
				
				if (!isfinded && StringUtils.isNotBlank(destSippeer)) { // 检查是否已经找到项目，被叫分机是否为空
					Long projectId = ShareData.extenToProject.get(destSippeer);
					cdr.setProjectId(projectId);
					if (projectId != null) {
						MarketingProject project = marketingProjectService.get(projectId);
						if (project != null) {
							cdr.setProjectName(project.getProjectName());
						}
					}
				}
			}
			
			String usedOutlineName = cdr.getOutlineName();
			if(usedOutlineName == null || "".equals(usedOutlineName)) {
				if(domainId != null) {
					List<String> outlineLs = ShareData.domainToOutlines.get(domainId);
					if(outlineLs != null) {
						if(outlineLs.contains(srcSippeer)) {
							usedOutlineName = srcSippeer;
						} else if(outlineLs.contains(destSippeer)) {
							usedOutlineName = destSippeer;
						}
					}
				}
			}
			cdr.setOutlineName(usedOutlineName); 
			
			//logger.info("T4_" + event.getUniqueId());

			// 更新Task信息为已经拨打状态
			updateTaskInfo(cdr, taskId);

			//logger.info("T5_" + event.getUniqueId());

			// jrh 将资源编号写入呼叫记录
			updateCustomerResourceId(cdr, callDirection);

			//logger.info("T6_" + event.getUniqueId());

			// 存储CDR记录
			storeCdrInfo(cdr);

			/**
			 * TODO 益盟定制开发 -------------------- 将 UniqueID 推送
			 */
			HangupVo hangupVo = new HangupVo();
			if("incoming".equals(cdr.getDestinationContext())) {
				hangupVo.setDestination("incoming");
				hangupVo.setUsername(cdr.getDestUsername());
				if(cdr.getDestinationChannel() != null && !"".equals(cdr.getDestinationChannel())) {
					String exten = cdr.getDestinationChannel().substring(cdr.getDestinationChannel().indexOf("/")+1, cdr.getDestinationChannel().indexOf("-"));
					hangupVo.setExten(exten);
				}
				hangupVo.setPhoneNumber(cdr.getSrc());
				hangupVo.setOutline(cdr.getOutlineName());
				hangupVo.setCreateTime((new Date()).getTime());
				hangupVo.setUniqueId(cdr.getUniqueId());
				WuRuiShareData.csrToHangupVoMap.put(hangupVo.getUsername(), hangupVo);
				
			} else if("outgoing".equals(cdr.getDestinationContext())) {
				hangupVo.setDestination("outgoing");
				hangupVo.setUsername(cdr.getSrcUsername());
				hangupVo.setExten(cdr.getSrc());
				hangupVo.setPhoneNumber(cdr.getDestination());
				hangupVo.setOutline(cdr.getOutlineName());
				hangupVo.setCreateTime((new Date()).getTime());
				hangupVo.setUniqueId(cdr.getUniqueId());
				WuRuiShareData.csrToHangupVoMap.put(hangupVo.getUsername(), hangupVo);
			}
			
			// jinht 推送 CDR 话单到第三方系统
			if("true".equals(ExternalInterface.PUSH_CDR_TO_THIRD_PARTY_SYSTEM_IS_OPEN)) {
				final Cdr tempCdr = cdr;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);		// 延迟 2s 推送 CDR 话单信息
						} catch (Exception e) { }
						String params = FastJsonUtil.toJson(tempCdr);
						HttpIfaceUtil.doPostRequest(ExternalInterface.PUSH_CDR_TO_THIRD_PARTY_SYSTEM_URL, "cdr="+params);
					}
				}).start();
			}
			
			//logger.info("T7_" + event.getUniqueId());

		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("CDREL_EC_handleEvent", e);
		}
	}

	private Cdr storeCdrInfo(Cdr cdr) {
		try {
			cdr = cdrService.update(cdr);
			
			//对cdr做参数包装
			/*Map<String,String> cdrparamsMap=new HashMap<String, String>();
			cdrparamsMap.put("usrc", cdr.getSrc());
			cdrparamsMap.put("udst", cdr.getDestination());
			if(CdrDirection.e2e==cdr.getCdrDirection()){
				cdrparamsMap.put("udirect", "in2in");
			}else if(CdrDirection.e2o==cdr.getCdrDirection()){
				cdrparamsMap.put("udirect", "in2out");
			}else if(CdrDirection.o2e==cdr.getCdrDirection()){
				cdrparamsMap.put("udirect", "out2in");
			}else if(CdrDirection.o2o==cdr.getCdrDirection()){
				cdrparamsMap.put("udirect", "out2out");
			}
			cdrparamsMap.put("duration", cdr.getDuration()+"");
			if(cdr.getStartTimeDate()!=null){
				cdrparamsMap.put("startTime", cdr.getStartTimeDate().getTime()+"");
			}
			if(cdr.getEndTimeDate()!=null){
				cdrparamsMap.put("endTime", cdr.getEndTimeDate().getTime()+"");
			}
			if(cdr.getBridgeTimeDate()!=null){
				cdrparamsMap.put("bridgeTime", cdr.getBridgeTimeDate().getTime()+"");
			}
			cdrparamsMap.put("uniqueId", cdr.getUniqueId());
			//推送CDR
//			MeibaoPushcdr.pushCdr(true, cdrparamsMap);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cdr;
	}

	private void updateTaskInfo(Cdr cdr, String taskId) {
		try {
			if ((cdr.getIsAutoDial() != null) && cdr.getIsAutoDial() && cdr.getDestUserId() != null) {
				String nativeSql = "update ec2_marketing_project_task set user_id="
						+ cdr.getDestUserId() + "  where id=" + taskId;
				commonService.excuteNativeSql(nativeSql, ExecuteType.UPDATE);
			}
		} catch (Exception e) {
			logger.error("将完成的MarketProjectTask 设置 完成该任务的坐席编号时出现异常--->updateTaskInfo");
		}
	}

	/**
	 * jrh 2013-11-04
	 * 	跟新呼叫记录中的资源id信息
	 * @param cdr				呼叫记录对象
	 * @param callDirection		呼叫方向
	 */
	private void updateCustomerResourceId(Cdr cdr, String callDirection) {
		Long resourceId = cdr.getResourceId();
		if(resourceId == null) {
			Telephone tel = null;
			if (callDirection.equals("e2o")) { // 呼出
				String dest = cdr.getDestination();
				tel = telephoneService.getByNumber(dest, cdr.getDomainId());
			} else if (callDirection.equals("o2e")) {	// 呼入
				String srcNo = cdr.getSrc();
				tel = telephoneService.getByNumber(srcNo, cdr.getDomainId());
			} else if (callDirection.equals("o2o")) {	// 外转外
				String srcNo = cdr.getSrc();
				tel = telephoneService.getByNumber(srcNo, cdr.getDomainId());
				if(tel == null) {
					String dest = cdr.getDestination();
					tel = telephoneService.getByNumber(dest, cdr.getDomainId());
				} 
			} else if(callDirection.equals("e2e")) {	// 正常情况下，这种情况是不需要处理的
				String srcNo = cdr.getSrc();
				String destNo = cdr.getDestination();
				List<String> extenList = ShareData.domainToExts.get(cdr.getDomainId());
				if(!extenList.contains(srcNo)) {
					tel = telephoneService.getByNumber(srcNo, cdr.getDomainId());
				} 
				if(tel == null && !extenList.contains(destNo)) {
					tel = telephoneService.getByNumber(destNo, cdr.getDomainId());
				} 
			}
			if(tel != null) {
				cdr.setResourceTelephoneId(tel.getId());	// 设置呼入者号码对应的Id
				
				if(tel.getCustomerResource() != null) {
					resourceId = tel.getCustomerResource().getId();	
				}
			}
			cdr.setResourceId(resourceId);
		}
	}

	private String findSrcSippeer(String channel) {
		String sippeer = "";
		try {
			if(channel != null && channel.contains("/") && channel.contains("-") ) {
				sippeer = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
			}
		} catch (Exception e) {
			logger.error("通过通道信息截取(substring)主叫信息出现异常！");
		}
		return sippeer;
	}

	
	private String findDestSippeer(String destinationChannel) {
		String sippeer = "";
		try {
			if(destinationChannel != null && destinationChannel.contains("/") && destinationChannel.contains("-") ) {
				sippeer = destinationChannel.substring( destinationChannel.indexOf("/") + 1, destinationChannel.indexOf("-"));
			}
		} catch (Exception e) {
			logger.error("通过通道信息截取(substring)被叫信息出现异常！");
		}
		return sippeer;
	}

	/**
	 * jrh 初始化呼叫记录域Id
	 * 
	 * @param cdr
	 * @param id
	 *            域id
	 * @param refer
	 *            分机或者外线
	 */
	private Long initialCdrDomainId(Cdr cdr, Long id, String destinationContext, String destination, String lastdata, String refer) {
		try {
			if (id == null) {
				if ("autodial".equals(destination)) {
					String queueName = lastdata.substring(0, 6);
					Queue queue = null;
					try {
						queue = queueService.getQueueByQueueName(queueName);
					} catch (Exception e) {
						LoggerUtil.logInfo(this, "自动外呼不是呼入队列！");
					}
					id = (queue != null) ? queue.getDomain().getId() : null;
					if(id != null) {
						cdr.setDomainId(id);
					}
				}
			}

			if (id == null) {
				for (Long domainId : ShareData.domainToExts.keySet()) {
					if (ShareData.domainToExts.get(domainId).contains(refer)) {
						cdr.setDomainId(domainId);
						id = domainId;
					}
				}
			}
			
			if (id == null) {
				for (Long domainId : ShareData.domainToOutlines.keySet()) {
					if (ShareData.domainToOutlines.get(domainId)
							.contains(refer)) {
						cdr.setDomainId(domainId);
						id = domainId;
					}
				}
			}
			
			return id;
		} catch (Exception e) {
			logger.error("初始化租户的编号出现异常！-->CDREL_EC_initialCdrDomainId");
			return null;
		}
	}

	/**
	 * 存储呼叫发起方信息，包含自动外呼 ,return taskId [这些信息主叫坐席的信息(呼出)，客户呼入使用的外线信息等]
	 */
	private String storeSrcInfo(Cdr cdr, String userField, CdrEvent event) {
		
/********************************************************************************************************/
		
		String srcUserId = event.getSrcUserId();									// 设置 主叫坐席编号
		if (srcUserId != null && !"".equals(srcUserId)) {
			cdr.setSrcUserId(Long.parseLong(srcUserId));
		}
		String srcEmpNo = event.getSrcEmpNo();										// 设置 主叫坐席工号
		cdr.setSrcEmpNo(StringUtils.trimToEmpty(srcEmpNo));

		String srcUsername = event.getSrcUsername();								// 设置 主叫坐席用户名
		srcUsername = StringUtils.trimToEmpty(srcUsername);
		cdr.setSrcUsername(decodeString(srcUsername));
		
		String srcRealName = event.getSrcRealName();								// 设置 主叫坐席真实姓名
		srcRealName = StringUtils.trimToEmpty(srcRealName);
		cdr.setSrcRealName(decodeString(srcRealName));

		String srcDeptId = event.getSrcDeptId();									// 设置 主叫坐席所属部门编号
		if (srcDeptId != null && !"".equals(srcDeptId)) {
			cdr.setSrcDeptId(Long.parseLong(srcDeptId));
		}
		
		String srcDeptName = event.getSrcDeptName();								// 设置 主叫坐席所属部门
		srcDeptName = StringUtils.trimToEmpty(srcDeptName);
		cdr.setSrcDeptName(decodeString(srcDeptName));
		
		String projectId = event.getProjectId();									// 设置 项目编号
		if (projectId != null && !"".equals(projectId)) {
			cdr.setProjectId(Long.parseLong(projectId));
		}
		
		String projectName = event.getProjectName();								// 设置 项目名称
		projectName = StringUtils.trimToEmpty(projectName);
		cdr.setProjectName(decodeString(projectName));
		
		String resourceId = event.getResourceId();									// 设置 资源编号
		if (resourceId != null && !"".equals(resourceId)) {
			cdr.setResourceId(Long.parseLong(resourceId));
		}
		
		String isAutoDial = event.getIsAutoDial();									// 设置 是否为自动外呼  true 为自动外呼，false 为语音群发, null 为人工呼叫
		if (isAutoDial != null && isAutoDial.equals("true")) {
			cdr.setIsAutoDial(true);
		} else if (isAutoDial != null && isAutoDial.equals("false")) {
			cdr.setIsAutoDial(false);
		} else {
			cdr.setIsAutoDial(null);
		}
		
		String autoDialId = event.getAutoDialId();									// 设置 自动外呼编号
		if (autoDialId != null && !"".equals(autoDialId)) {
			cdr.setAutoDialId(Long.parseLong(autoDialId));
		}
		 
		String autoDialName = event.getAutoDialName();								// 设置 自动外呼名称
		autoDialName = StringUtils.trimToEmpty(autoDialName);
		cdr.setAutoDialName(decodeString(autoDialName));
		
		String domainId = event.getDomainId();										// 设置 租户所属域的编号
		if (domainId != null && !"".equals(domainId)) {
			cdr.setDomainId(Long.parseLong(domainId));
		}

		// v1.7.5.1  新增，按区分呼叫外线		20140604
		String usedOutlineName = event.getUsedOutlineName();
		usedOutlineName = StringUtils.trimToEmpty(usedOutlineName);
		cdr.setOutlineName(usedOutlineName);
		
		// v1.7.5.1  新增，按队列区分		20140604
		String usedQueueName = event.getParamA();
		usedQueueName = StringUtils.trimToEmpty(usedQueueName);
		cdr.setQueueName(usedQueueName);
		
		// v1.7.5.1  客凯易新增		20140604
		String lastInQueueTimeDateStr = event.getParamB();
		if(lastInQueueTimeDateStr != null && !"".equals(lastInQueueTimeDateStr)) {
			Date lastInQueueTimeDate = new Date();
			lastInQueueTimeDate.setTime(Long.parseLong(lastInQueueTimeDateStr)*1000); // 从asterisk 获取的是 秒数，而这用的是毫秒值
			cdr.setLastInQueueTimeDate(lastInQueueTimeDate); 
		}
		
		String taskId = event.getTaskId();

		// Cdr(userfield) 		
		try {
			if(userField != null) {
				String[] info = userField.split("&");
				Map<String, String> keyValueMap = new HashMap<String, String>();
				for (String field : info) {
					String[] keyValue = field.split(":");
					if (keyValue.length == 1) {
						keyValueMap.put(keyValue[0], "");
					} else if (keyValue[1].equals("@@")) {
						keyValueMap.put(keyValue[0], "");
					} else {
						keyValueMap.put(keyValue[0], keyValue[1]);
					}
				}
				
				// jrh 该字段暂时用来做统计语音群发是客户的按键信息
				String customerClick = keyValueMap.get("userfield");
				if (customerClick != null) {
					cdr.setUserField(customerClick);
				}
				
				// jrh 设置实际的被叫号码
				String destination = keyValueMap.get("destination");
				if (destination != null) {
					cdr.setDestination(destination);
				}
			}
			return taskId;
		} catch (Exception e) {
			logger.error("初始化Cdr 的基础信息是出现异常--->CDREL_EC_storeSrcInfo");
			return "";
		}
	}
	
	/**
	 * 
	 * 将字符串[中文]进行解码，然后返回
	 * @param needDecodeStr	需要解码的信息
	 * @return
	 */
	private String decodeString(String needDecodeStr) {
		try {
			needDecodeStr = URLDecoder.decode(needDecodeStr, "utf-8");
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace();
			logger.error("jrh 自动外呼 为asterisk 而 UrlEncode 中文信息时出现异常-->"+e.getMessage(), e);
		}
		return needDecodeStr;
	}
	
}

//System.out.println("srcUserId----------"+srcUserId);
//System.out.println("srcEmpNo----------"+srcEmpNo);
//System.out.println("srcUsername----------"+srcUsername);
//System.out.println("srcRealName----------"+srcRealName);
//System.out.println("srcDeptId----------"+srcDeptId);
//System.out.println("projectId----------"+srcDeptId);
//System.out.println("projectName----------"+srcDeptId);
//System.out.println("resourceId----------"+srcDeptId);
//System.out.println("isAutoDial----------"+isAutoDial);
//System.out.println("autoDialId----------"+autoDialId);
//System.out.println("autoDialName----------"+autoDialName);
//System.out.println("domainId----------"+domainId);
//System.out.println("taskId----------"+taskId);
