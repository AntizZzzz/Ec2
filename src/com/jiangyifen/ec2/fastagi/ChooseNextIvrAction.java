package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.service.eaoservice.IVROptionService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：根据客户按键信息，查找租户针对当前外线设置的下一层次的IVR 
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
 * @date    2014年2月19日 下午9:52:44
 * @version v1.0.0
 */
public class ChooseNextIvrAction extends BaseAgiScript {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private IVROptionService ivrOptionService = SpringContextHolder.getBean("ivrOptionService");
	
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {

		try {

			/****************************** 获取参数 ******************************************/
			
			String pressNum = channel.getVariable("readnumber");
			Long currentActionId = Long.parseLong(channel.getVariable("actionId"));
			String toRead = channel.getVariable("toRead");
			String toReadAgain = channel.getVariable("toReadAgain");
			
//			/*********************************  for test ************************************/
//			
//			String calledOutline = channel.getVariable("dest");
//			Long ivrMenuId = Long.parseLong(channel.getVariable("ivrId"));
//			String extension = request.getExtension();
//			String toQueue = channel.getVariable("toQueue");
//			String toExten = channel.getVariable("toExten");
//			String toMobile = channel.getVariable("toMobile");
//			String toPlayback = channel.getVariable("toPlayback");
//			String needHangup = channel.getVariable("needHangup");
//			String dest = channel.getVariable("dest");
//			
//			System.out.println("extension-------"+extension);
//			System.out.println("ivrMenuId-------"+ivrMenuId);
//			System.out.println("calledOutline-------"+calledOutline);
//			System.out.println("readnumber-------"+pressNum);
//			System.out.println("channel.getVariable('dest')-------"+channel.getVariable("dest"));
//			
//			System.out.println("toQueue -----> "+toQueue);
//			System.out.println("toExten -----> "+toExten);
//			System.out.println("toMobile -----> "+toMobile);
//			System.out.println("toPlayback -----> "+toPlayback);
//			System.out.println("toRead -----> "+toRead);
//			System.out.println("toReadAgain -----> "+toReadAgain);
//			System.out.println("needHangup -----> "+needHangup);
//			System.out.println("dest -----> "+dest);
			

			/****************************** 重新初始化通道参数 ******************************************/
			
			initTranParms(channel);
			
			
			/*****************************  检验是否符合走IVR 流程，并获取相应的参数  ************************/
			
			IVROption ivrOption = ivrOptionService.getByActionIdAndPressNum(pressNum, currentActionId);
			if(ivrOption == null) {	// 如果客户的按键在没有对应的Option，则让客户重新按键。那么需要将 ToRead 置空，然后重新送一个参数 toReadAgain
				String againSound = ("".equals(toRead)) ? toReadAgain : toRead;
				channel.setVariable("needHangup", "no");
				channel.setVariable("toRead", "");
				channel.setVariable("toReadAgain", againSound);
				logger.warn("jrh 根据用户的按键 ["+pressNum+"]及当前 ActionId ["+currentActionId+"},在IVR Option 中没有找到对应的 IVR Option 对象，返回让客户重新输入或挂断！");
				return;
			}

			IVRAction nextIvrAction = ivrOption.getNextIvrAction();
			Long nextIvrActionId = nextIvrAction.getId();
			SoundFile soundFile = nextIvrAction.getSoundFile();
			IVRActionType actionType = nextIvrAction.getActionType();
			
			if(soundFile == null && (IVRActionType.toRead.equals(actionType) 
					|| IVRActionType.toPlayback.equals(actionType) || IVRActionType.toReadForAgi.equals(actionType))) {
				channel.setVariable("actionId", "");
				channel.setVariable("optionId", "");
				channel.setVariable("needHangup", "yes");
				logger.warn("jrh 根据客户的按键，查到的IVR 的Action 需要语音，但是却没有关联的语音文件， 所以讲执行挂断！");
				return;			// 如果是需要播放语音的情况，但是没有语音文件，则直接返回，并挂断
			}
			
			
			/*********************************** 给通道设置信息-传特殊参 ************************************/
			
			// 根据Action 的类型，向通道设置参数
			if(IVRActionType.toRead.equals(actionType)) {	// Read语音
				Integer errorOpportunity = nextIvrAction.getErrorOpportunity();
				String errorOpportunityStr = (errorOpportunity == null) ? "0" : errorOpportunity.toString();
				channel.setVariable("toRead", soundFile.getStoreName());
				channel.setVariable("errorOpportunity", errorOpportunityStr);
				
			} else if(IVRActionType.toPlayback.equals(actionType)) {	// 播放语音
				channel.setVariable("toPlayback", soundFile.getStoreName());
				
			} else if(IVRActionType.toReadForAgi.equals(actionType)) {	// Read语音根据按键到执行Agi
				Integer errorOpportunity = nextIvrAction.getErrorOpportunity();
				String errorOpportunityStr = (errorOpportunity == null) ? "0" : errorOpportunity.toString();
				channel.setVariable("toReadForAgi", soundFile.getStoreName());
				channel.setVariable("agiNameByIvr", nextIvrAction.getAgiName());
				channel.setVariable("errorOpportunity", errorOpportunityStr);
				
			} else if(IVRActionType.toExten.equals(actionType)) {	// 打分机
				String toExtenName = nextIvrAction.getExtenName();
				if("".equals(toExtenName)) {
					channel.setVariable("needHangup", "yes");
					logger.warn("jrh 根据客户的按键，查到的IVR 的Action 希望打分机，却没有指定的分机， 所以讲执行挂断！");
					return;
				}
				channel.setVariable("toExten", toExtenName);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_EXTEN_TIMEOUT);
				
			} else if(IVRActionType.toQueue.equals(actionType)) {	// 打队列
				String toQueueName = nextIvrAction.getQueueName();
				if("".equals(toQueueName)) {
					channel.setVariable("needHangup", "yes");
					logger.warn("jrh 根据客户的按键，查到的IVR 的Action 希望打队列，却没有指定的队列， 所以讲执行挂断！");
					return;
				}
				channel.setVariable("toQueue", toQueueName);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_QUEUE_TIMEOUT);
				
			} else if(IVRActionType.toMobile.equals(actionType)) {	// 转呼手机
				String toMobileNum = nextIvrAction.getMobileNumber();
				String useOutline = nextIvrAction.getOutlineName();
				if("".equals(toMobileNum) || "".equals(useOutline)) {
					logger.warn("jrh 根据客户的按键，查到的IVR 的Action 希望转呼外部手机，却没有指定手机号，或者没有指定要使用的外线号码， 所以讲执行挂断！");
					return;
				}
				channel.setVariable("toMobile", toMobileNum);
				channel.setVariable("useOutline", useOutline);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_MOBILE_TIMEOUT);
				
			} else if(IVRActionType.toVoicemail.equals(actionType)) {	// 语音留言
				String toQueueName = nextIvrAction.getQueueName();
				if("".equals(toQueueName)) {
					channel.setVariable("needHangup", "yes");
					logger.warn("jrh 根据客户的按键，查到的IVR 的Action 希望打队列，却没有指定的队列， 所以讲执行挂断！");
					return;
				}
				channel.setVariable("toVoicemail", toQueueName);
				channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_QUEUE_TIMEOUT);
			}
			
			
			/*************************** 向通道传递普通参数  ****************************/
			
			channel.setVariable("actionId", nextIvrActionId.toString());
			channel.setVariable("optionId", ivrOption.getId().toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 获取下一级别的IVR Action 出现异常---->"+e.getMessage(), e);
		}
	}

	/**
	 * 根据初始化向通道传递的参数
	 * @param channel			通道
	 * @param isHangup			返回后是否需要挂断
	 * @throws AgiException		抛出异常
	 */
	private void initTranParms(AgiChannel channel) throws AgiException {
		channel.setVariable("toRead", "");
		channel.setVariable("toQueue", "");
		channel.setVariable("toExten", "");
		channel.setVariable("toMobile", "");
		channel.setVariable("toVoicemail", "");
		channel.setVariable("toPlayback", "");
		channel.setVariable("needHangup", "no");
		channel.setVariable("toReadAgain", "");
	}
}
