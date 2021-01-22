package com.jiangyifen.ec2.fastagi;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 
 * @Description 描述：根据客户按键信息，呼叫指定的分机
 * 
 * agi 的名称为 ： dialSpecifiedExtenByIvr.agi
 * 
 * 		IVRAction.AGI_NAMES_MAP.put("dialSpecifiedExtenByIvr.agi", "呼叫指定分机");
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
 */
public class DdialSpecifiedExtenByIvr extends BaseAgiScript {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/*private IVROptionService ivrOptionService = SpringContextHolder.getBean("ivrOptionService");*/
	
	public static void main(String[] args) {
		System.out.println("8000011".matches("8\\d{5}"));
	}
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {

		try {

			/****************************** 获取参数 ******************************************/
			
			String pressExtenNum = StringUtils.trimToEmpty(channel.getVariable("readnum"));
			Long currentActionId = Long.parseLong(channel.getVariable("actionId"));
			String toReadForAgi = channel.getVariable("toReadForAgi");
			String toReadForAgiAgain = channel.getVariable("toReadForAgiAgain");
			String outlineName = channel.getVariable("dest");

			/*********************************  for test ************************************/
			
			String calledOutline = channel.getVariable("dest");
			Long ivrMenuId = Long.parseLong(channel.getVariable("ivrId"));
			String extension = request.getExtension();
			String toQueue = channel.getVariable("toQueue");
			String toExten = channel.getVariable("toExten");
			String toMobile = channel.getVariable("toMobile");
			String toPlayback = channel.getVariable("toPlayback");
			String needHangup = channel.getVariable("needHangup");
			
			logger.info("extension-------"+extension);
			logger.info("ivrMenuId-------"+ivrMenuId);
			logger.info("calledOutline-------"+calledOutline);
			logger.info("readnumber-------"+pressExtenNum);
			logger.info("channel.getVariable('dest')-------"+channel.getVariable("dest"));
			
			logger.info("toQueue -----> "+toQueue);
			logger.info("toExten -----> "+toExten);
			logger.info("toMobile -----> "+toMobile);
			logger.info("toPlayback -----> "+toPlayback);
			logger.info("toReadForAgi -----> "+toReadForAgi);
			logger.info("toReadForAgiAgain -----> "+toReadForAgiAgain);
			logger.info("needHangup -----> "+needHangup);
			logger.info("outlineName -----> "+outlineName);
			

			/****************************** 重新初始化通道参数 ******************************************/
			
			initTranParms(channel);
			
			
			/*****************************  检验输入的号码是否为当前租户的分机号，如果不是则让其重新输入  ************************/
			
			Long domainId = 0L;
			boolean isOwnDomainExten = false;	// 分机号是否属于当前域
			// if(pressExtenNum.matches("8\\d{5}")) {
			if(pressExtenNum.matches("6\\d{4}")) {		// 武睿定制, 分机改为 5 位数
				for(Long did : ShareData.domainToOutlines.keySet()) {
					List<String> outlineLs = ShareData.domainToOutlines.get(did);
					if(outlineLs != null && outlineLs.contains(outlineName)) {
						domainId = did;
						break;
					}
				}

				List<String> extenLs = ShareData.domainToExts.get(domainId);
				if(extenLs != null && extenLs.contains(pressExtenNum)) {
					isOwnDomainExten = true;
				}
			}
			
			if(!isOwnDomainExten) {	// 如果客户的按键不是正确的分机号，则让客户重新按键。那么需要将 toReadForAgi 置空，然后重新送一个参数 toReadForAgiAgain
				String againSound = ("".equals(toReadForAgi)) ? toReadForAgiAgain : toReadForAgi;
				channel.setVariable("needHangup", "no");
				channel.setVariable("toReadForAgi", "");
				channel.setVariable("toReadForAgiAgain", againSound);
				logger.warn("jrh 客户输入的分机Exten: ["+pressExtenNum+"]及当前 ActionId ["+currentActionId+"},发现输入的按键信息不是正确的分机号，返回让客户重新输入或挂断！");
				return;
			}

			boolean isCsrOnline = false;	// 坐席是否在线
			if(ShareData.userToExten.values() != null) {
				isCsrOnline = ShareData.userToExten.values().contains(pressExtenNum);
			}
			
			if(!isCsrOnline) {	// 坐席不在线,值直接挂断
				channel.setVariable("needHangup", "yes");
				logger.warn("jrh 客户输入的分机Exten: ["+pressExtenNum+"]及当前 ActionId ["+currentActionId+"},分机对应的坐席不在线csr is outline，挂断！");
				return;
			}
			
			channel.setVariable("toExten", pressExtenNum);
			channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_EXTEN_TIMEOUT);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 根据客户按键执行呼叫指定分机 出现异常---->"+e.getMessage(), e);
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
		channel.setVariable("toPlayback", "");
		channel.setVariable("needHangup", "no");
		channel.setVariable("toReadAgain", "");
		channel.setVariable("toReadForAgi", "");
		channel.setVariable("toReadForAgiAgain", "");
	}
}
