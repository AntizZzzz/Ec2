package com.jiangyifen.ec2.fastagi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.IVRActionService;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：根据客户打进的信息，查找租户针对当前外线设置的IVR 
 * 
 * 	IVR action 的操作有：
 * 		1、呼叫分机：				 toExten 
 * 		2、呼入队列: 				 toQueue
 * 		3、转呼手机: 				 toMobile
 * 		4、Playback语音: 		 	 toPlayback
 * 		5、Read语音:		 		 toRead
 * 		6、Read语音根据按键到执行Agi:   toReadForAgi 	如-根据按键将呼入客户接通到指定坐席（该坐席是客户自己选择的）
 * 
 * @author  jrh
 * @date    2014年2月19日 下午4:15:19
 * @version v1.0.0
 */
public class ChooseIvr extends BaseAgiScript {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private IvrMenuService ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
	
	private IVRActionService ivrActionService = SpringContextHolder.getBean("ivrActionService");
	
	private SipConfigService sipConfigService = SpringContextHolder.getBean("sipConfigService");
	
	
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		try {

			/****************************** 获取参数******************************************/
			
			Date occurTime = new Date();
			String calledOutline = channel.getVariable("dest");
			
			/***************************** for test ***********************/
//			String extension = request.getExtension();
//			System.out.println("extension-------"+extension);
//			System.out.println("calledOutline-------"+calledOutline);
			
			
			/*****************************  检验是否符合走IVR 流程，并获取相应的参数  ************************/
			
			if(calledOutline == null || "".equals(calledOutline)) {	// 如果主叫号码为空，则直接返回，走普通的呼入流程
				logger.warn("jrh agi 没有传递 客户拨打的外线号码！");
				return;
			}

			SipConfig outline = sipConfigService.getOutlineByOutlineName(calledOutline);
			if(outline == null) {	// 如果外线不存在，那就根本无法根据外线找到租户对应的IVR ，则直接返回，走普通的呼入流程
				logger.warn("jrh 系统没有找到 通过Agi 传过来的 客户拨打的外线号码对应 外线 SipConfig！");
				return;
			}
			
			Long domainId = outline.getDomain().getId();
			ArrayList<OutlineToIvrLink> otilList = ShareData.outlineIdToIvrLinkMap.get(outline.getId());
			if(otilList == null || otilList.size() == 0) {	// 如果当前租户没有 IVR ，则直接返回，走普通的呼入流程
				logger.warn("jrh 当前外线没有关联的 IVR 导航对象！");
				return;
			}
			
			IVRMenu ivrMenu = findSuitableIVRMenu(otilList, occurTime);	// 从已经按优先级排序了的OutlineToIvrLink集合中查找出符合条件的 IVRMenu
			if(ivrMenu == null) {	// 如果当前租户没有 IVR ，则直接返回，走普通的呼入流程
				logger.warn("jrh 当前外线没有关联的 IVR 导航对象！");
				return;
			}
			
			Long ivrMenuId = ivrMenu.getId();
			IVRAction ivrAction = ivrActionService.getRootIVRActionByIVRMenu(ivrMenuId);
			if(ivrAction == null) {	// 如果当前租户的 IVR没有设置操作项，那就相当于没有任何用处，所以直接退出，走普通的呼入流程
				logger.warn("jrh 当前IVR 没有一个 Root Action 动作！");
				return;
			}

			Long ivrActionId = ivrAction.getId();
			SoundFile welcomeSoundFile = ivrMenu.getWelcomeSoundFile();		// 获取 欢迎词 语音文件 在磁盘上存储的实际名称
			String welcomeSoundName = (welcomeSoundFile != null) ? welcomeSoundFile.getStoreName() : "";
			
			SoundFile closeSoundFile = ivrMenu.getCloseSoundFile();		// 获取 结束语 语音文件 在磁盘上存储的实际名称
			String closeSoundName = (closeSoundFile != null) ? closeSoundFile.getStoreName() : "";
			
			SoundFile soundFile = ivrAction.getSoundFile();
			IVRActionType actionType = ivrAction.getActionType();
			if(soundFile == null && (IVRActionType.toRead.equals(actionType) 
					|| IVRActionType.toPlayback.equals(actionType) || IVRActionType.toReadForAgi.equals(actionType))) {
				logger.warn("jrh 当前IVR 的Action 需要语音，但是却没有关联的语音文件！");
				return;			// 如果是需要播放语音的情况，但是没有语音文件，则直接返回，走普通的呼入流程
			}
			
			
			/*********************************** 给通道设置信息-传特殊参 ************************************/
			
			// 根据Action 的类型，向通道设置参数
			if(IVRActionType.toRead.equals(actionType)) {	// Read语音
				Integer errorOpportunity = ivrAction.getErrorOpportunity();
				String errorOpportunityStr = (errorOpportunity == null) ? "0" : errorOpportunity.toString();
				channel.setVariable("toRead", soundFile.getStoreName());
				channel.setVariable("errorOpportunity", errorOpportunityStr);
				
			} else if(IVRActionType.toPlayback.equals(actionType)) {	// 播放语音
				channel.setVariable("toPlayback", soundFile.getStoreName());
				
			} else if(IVRActionType.toReadForAgi.equals(actionType)) {	// Read语音根据按键到执行Agi
				Integer errorOpportunity = ivrAction.getErrorOpportunity();
				String errorOpportunityStr = (errorOpportunity == null) ? "0" : errorOpportunity.toString();
				channel.setVariable("toReadForAgi", soundFile.getStoreName());
				channel.setVariable("agiNameByIvr", ivrAction.getAgiName());
				channel.setVariable("errorOpportunity", errorOpportunityStr);
				
			} else if(IVRActionType.toExten.equals(actionType)) {	// 打分机
				String toExtenName = ivrAction.getExtenName();
				if("".equals(toExtenName)) {
					logger.warn("jrh 当前IVR Action 希望打分机，却没有指定的分机！");
					return;
				}
				channel.setVariable("toExten", toExtenName);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_EXTEN_TIMEOUT);
				
			} else if(IVRActionType.toQueue.equals(actionType)) {	// 打队列
				String toQueueName = ivrAction.getQueueName();
				if("".equals(toQueueName)) {
					logger.warn("jrh 当前IVR Action 希望打队列，却没有指定的队列！");
					return;
				}
				channel.setVariable("toQueue", toQueueName);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_QUEUE_TIMEOUT);
				
			} else if(IVRActionType.toMobile.equals(actionType)) {	// 转呼手机
				String toMobileNum = ivrAction.getMobileNumber();
				String useOutline = ivrAction.getOutlineName();
				if("".equals(toMobileNum) || "".equals(useOutline)) {
					logger.warn("jrh 当前IVR Action 希望转呼外部手机，却没有指定手机号，或者没有指定要使用的外线号码！");
					return;
				}
				channel.setVariable("toMobile", toMobileNum);
				channel.setVariable("useOutline", useOutline);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_MOBILE_TIMEOUT);
				
			} else if(IVRActionType.toVoicemail.equals(actionType)) {
				String toQueueName = ivrAction.getQueueName();
				if("".equals(toQueueName)) {
					logger.warn("jinht 当前IVR Action 希望打队列进行语音留言，却没有指定的队列！");
					return;
				}
				channel.setVariable("toVoicemail", toQueueName);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_QUEUE_TIMEOUT);
				
			}
			
			
			/*************************** 向通道传递普通参数  ****************************/
			
			channel.setVariable("ivrId", ivrMenuId.toString());
			channel.setVariable("actionId", ivrActionId.toString());
			channel.setVariable("ivrSoundDir", domainId.toString());
			channel.setVariable("domainId", domainId.toString());
			channel.setVariable("closeSoundName", closeSoundName);
			channel.setVariable("toWelcome", welcomeSoundName);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 获取IVR 的第一级 Action 出现异常---->"+e.getMessage(), e);
		}
	}


	/**
	 * 从已经按优先级排序了的OutlineToIvrLink集合中查找出符合条件的 IVRMenu
	 * @author jrh
	 * @param otilList	已经按优先级排序了的OutlineToIvrLink集合
	 * @return IVRMenu  返回找到的IVRMenu 对象
	 */
	private IVRMenu findSuitableIVRMenu(ArrayList<OutlineToIvrLink> otilList, Date occurTime) {
		IVRMenu ivrMenu = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(occurTime);
		int dayofweek_index = calendar.get(Calendar.DAY_OF_WEEK);
		int current_hour = calendar.get(Calendar.HOUR_OF_DAY);
		int current_min = calendar.get(Calendar.MINUTE);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date current_date = calendar.getTime();

		for(OutlineToIvrLink link : otilList) {	// 这个list 是按优先级排序的
			//-------- 第 1 步，检查Link是否已经被禁用，如果禁用了，则检查下一个 ------//
			if(!link.getIsUseable()) {
				continue;
			}
			
			//-------- 第 2 步，检查今天是周几，并且Link 在今天是否生效，如果不生效，则检查下一个Link ------//
			if(dayofweek_index == 1 && !link.getIsSunEffect()) {
				continue;
			} else if(dayofweek_index == 2 && !link.getIsMonEffect()) {
				continue;
			} else if(dayofweek_index == 3 && !link.getIsTueEffect()) {
				continue;
			} else if(dayofweek_index == 4 && !link.getIsWedEffect()) {
				continue;
			} else if(dayofweek_index == 5 && !link.getIsThuEffect()) {
				continue;
			} else if(dayofweek_index == 6 && !link.getIsFriEffect()) {
				continue;
			} else if(dayofweek_index == 7 && !link.getIsSatEffect()) {
				continue;
			}
			
			//-------- 第 3 步，检查日期范围(年月日）是否符合，即检查当前日期是否在Link 的生效日期范围内 ------//
			if(current_date.before(link.getEffectDate()) || current_date.after(link.getExpireDate())) {
				continue;
			}
			
			//-------- 第 4 步，检查当前时刻 是否符合，即检查当前小时跟分钟，是否在Link 的生效时段范围内 ------//
			if(current_hour < link.getStartHour() || (current_hour == link.getStartHour() && current_min < link.getStartMin())
					 || current_hour > link.getStopHour() || (current_hour == link.getStopHour() && current_min > link.getStopMin()) ) {
				continue;
			}
			
			//-------- 第 5 步，如果以上三层过滤都通过了，那说明已经找到IVR，从内存中查出IVRMenu，然后退出for循环 ------//
			Long ivrMenuId = link.getIvrMenuId();
//			logger.info("成功找到外线与IVR的关联对象："+link.toLoggerString());
			logger.warn("成功找到外线与IVR的关联对象："+link.toLoggerString());
			
			ivrMenu = ShareData.ivrMenusMap.get(ivrMenuId);
			if(ivrMenu == null) {	// 正常情况下，这不会出现
				ivrMenu = ivrMenuService.getById(ivrMenuId);
			}
			
			break;	// 一定要退出for 循环，因为采用的是 优先级策略 
		}
		return ivrMenu;
	}
	
}
