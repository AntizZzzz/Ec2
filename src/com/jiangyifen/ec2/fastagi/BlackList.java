package com.jiangyifen.ec2.fastagi;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 *
 * @author jrh
 *  
 */
/**
 * 
 * @Description 描述：检查呼入呼出是否是黑名单里面的用户Agi 
 * 
 * @author  jrh
 * @date    2013年07月25日  下午1:16:02
 * @version v1.0.2
 */
public class BlackList extends BaseAgiScript {
	private static final Logger logger = LoggerFactory.getLogger(BlackList.class);
	
	// 全局配置项中的黑名单配置方式是否按外线进行区分的
	private static final String SETTING_BLACKLIST_BY_OUTLINE = "setting_blacklist_by_outline"; 
	
	private static SipConfigService sipConfigService = SpringContextHolder.getBean("sipConfigService");

	public void service(AgiRequest agiRequest, AgiChannel agiChannel)
			throws AgiException {

		//取得呼叫方向和主叫号码
		String direction = agiRequest.getParameter("direction");
		
		if("outgoing".equals(direction)) {
			String callerChannel = agiRequest.getChannel();
			String exten = callerChannel.substring(callerChannel.indexOf("/")+1, callerChannel.indexOf("-"));	// 如果是呼出，则一定是分机

			String outlineName = agiChannel.getVariable("outline");

			String destNum = agiRequest.getDnid();	
			if(destNum == null) {	// 如果被叫号码为空 ，则直接返回
				return;
			}

			Long domainId = ShareData.extenToDomain.get(exten);
			if(domainId == null) {	// 这种情况一般不会存在
				return;
			}

			boolean isBlacklistByOutline = false;	// 是否按外线区分黑名单，默认为不是
			Map<String, Boolean> configs = ShareData.domainToConfigs.get(domainId);
			Boolean settingValue = configs.get(SETTING_BLACKLIST_BY_OUTLINE);
			
			if(settingValue != null) {	// 如果有关于黑名单的配置项，则将isBlacklistByOutline 赋值
				isBlacklistByOutline = (boolean) settingValue;
			}
			
			if(isBlacklistByOutline) {	// 按外线使用黑名单
				SipConfig outline = sipConfigService.getOutlineByOutlineName(outlineName);
				if(outline == null) {	// 如果外线不存在，那就根本无法判断当时主叫拨打的是不是黑名单成员
					return;
				}
				
				List<String> outgoingBlacklist = ShareData.outlineToOutgoingBlacklist.get(outline.getId());
				if(outgoingBlacklist != null && outgoingBlacklist.contains(destNum)) {
					agiChannel.hangup();
					logger.info("BlackList: " +destNum+ " is in outgoing black list[setting blacklist by outline]. Use outline : " +outlineName);
					return;
				} 
			} else {	// 按全局使用黑名单
				if (ShareData.domainToOutgoingBlacklist.get(domainId).contains(destNum)) {
					agiChannel.hangup();
					logger.info("BlackList: " +destNum+ " is in outgoing black list. [setting blacklist by global setting]. Current domain's id is : " +domainId);
				}
				return;
			}
		} else if("incoming".equals(direction)) {
			String calledOutline = agiChannel.getVariable("dest");
			String srcNum = agiRequest.getCallerIdNumber();
			if(srcNum == null) {	// 如果主叫号码为空，则直接返回
				return;
			}

			SipConfig outline = sipConfigService.getOutlineByOutlineName(calledOutline);
			if(outline == null) {	// 如果外线不存在，那就根本无法判断当时主叫拨打的是不是黑名单成员
				return;
			}
			
			Long domainId = outline.getDomain().getId();

			/*****************  为呼入弹屏，收集呼入信息 (最好另做一个agi)  ****************************/
			collectInfosToIncomingWindow(agiRequest, domainId);		
			/******************************************************************************/
			
			boolean isBlacklistByOutline = false;	// 是否按外线区分黑名单，默认为不是
			Map<String, Boolean> configs = ShareData.domainToConfigs.get(domainId);
			Boolean settingValue = configs.get(SETTING_BLACKLIST_BY_OUTLINE);
			
			if(settingValue != null) {	// 如果有关于黑名单的配置项，则将isBlacklistByOutline 赋值
				isBlacklistByOutline = (boolean) settingValue;
			}
			
			if(isBlacklistByOutline) {	// 按外线使用黑名单
				List<String> blacklists = ShareData.outlineToIncomingBlacklist.get(outline.getId());
				if(blacklists != null && blacklists.contains(srcNum)) {
					agiChannel.hangup();
					logger.info("BlackList: " +srcNum+ " is in incoming black list[setting blacklist by outline]. Use outline : " +outline.getUsername());
					return;
				} 
			} else {	// 按全局使用黑名单
				if (ShareData.domainToIncomingBlacklist.get(domainId).contains(srcNum)) {
					agiChannel.hangup();
					logger.info("BlackList: " +srcNum+ " is in incoming black list. [setting blacklist by global setting]. Current domain's id is : " +domainId);
				}
				return;
			}
		}

	}

	/**
	 * @Description 描述：为呼入弹屏，收集呼入信息
	 *
	 * @author  jrh
	 * @date    2014年3月14日 下午2:48:47
	 * @param agiRequest
	 * @param domainId void
	 */
	private void collectInfosToIncomingWindow(AgiRequest agiRequest, Long domainId) {
		/**************2013-11-21 新增，为了节省后期更新的问题，暂时不重新写一个agi, 直接在这里处理主叫使用的是那条外线 打进系统的问题 *******************/
		/***********Map<域的id, ConcurrentHashMap<主叫号码，呼入信息>> 如Map<1,ConcurrentHashMap<"13816760365", 呼入信息>>*********/
		ConcurrentHashMap<String, IncomingDialInfo> incomingCallerToDialInfos = ShareData.domainToIncomingDialInfoMap.get(domainId);
		if(incomingCallerToDialInfos == null) {
			incomingCallerToDialInfos = new ConcurrentHashMap<String, IncomingDialInfo>();
			ShareData.domainToIncomingDialInfoMap.put(domainId, incomingCallerToDialInfos);
		}
		
		synchronized (incomingCallerToDialInfos) {
			String callerIdNumber = agiRequest.getCallerIdNumber();
			if(!"".equals(callerIdNumber) && callerIdNumber != null) {
				IncomingDialInfo incomingDialInfo = new IncomingDialInfo();
				incomingDialInfo.setCallerNumber(callerIdNumber);
				incomingDialInfo.setVasOutline(agiRequest.getDnid());
				incomingDialInfo.setAutoDial(false);
				incomingDialInfo.setDialType("incoming");
				incomingCallerToDialInfos.put(callerIdNumber, incomingDialInfo);
			}
		}
	}

//	AgiRequest[script='blacklist.agi',requestURL='agi://192.168.1.160/blacklist.agi?direction=incoming',
//	channel='SIP/88860847041-0000003d',uniqueId='1405082462.61',type='SIP',language='en',
//	callerIdNumber='02161851888',callerIdName='null',dnid='88860847041',rdnis='null',context='incoming',
//	extension='88860847041',priority='5',enhanced='false',accountCode='null',systemHashcode=292609932]

}