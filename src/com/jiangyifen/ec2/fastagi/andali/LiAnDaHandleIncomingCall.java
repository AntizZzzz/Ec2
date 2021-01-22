package com.jiangyifen.ec2.fastagi.andali;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.jiangyifen.ec2.utils.proputils.LiAnDaIncomingConfig;

/**
 * 
 * @Description 描述：为 【上海利安达贵金属经营有限公司】 专用的Agi
 * 
 * 	根据呼入客户的要求，呼叫到指定的坐席，或队列
 * 
 * 		欢迎致电利安达上海贵金属，
 * 		金融1部请拨1，
 * 		金融2部请拨2，
 * 		金融3部请拨3，
 * 		人事部请拨4，
 * 		联系指定坐席请拨5，
 * 		总机请拨0
 * 
 * @author  jrh
 * @date    2014年2月12日 下午4:03:36
 * @version v1.0.0
 */
public class LiAnDaHandleIncomingCall extends BaseAgiScript {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {

		String uniqueid = channel.getUniqueId();
		String calleridNum = channel.getVariable("CALLERID(num)");
		
		String inputNum = pickOutNumbers(channel.getVariable("InputNum"));
		if("".equals(inputNum)) {
			channel.setVariable("NoNumber", "yes");
			logger.info("jrh-------------主叫没有输入任何数字键！！------");
			return;
		} 
		
		int number = Integer.parseInt(inputNum);
		String username = "";
		
		if(number == 0) {		// 根据用户的按键，找到指定的坐席
			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER0);
		} else if(number == 1) {
			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER1);
		} else if(number == 2) {
			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER2);
		} else if(number == 3) {
			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER3);
			handleCaller(channel, username);
		} else if(number == 4) {
			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER4);
		} else if(number == 5) {
			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER5);
			channel.setVariable("ToSpecifyExten", "yes");
			return;
//	TODO 暂时 利安达公司只要前面几个按键
//		} else if(number == 6) {
//			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER6);
//		} else if(number == 7) {
//			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER7);
//		} else if(number == 8) {
//			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER8);
//		} else if(number == 9) {
//			username = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.INPUT_NUMBER9);
		}

		logger.info("jrh (通道标识)uniqueid:--"+uniqueid+"----(主叫号码)calleridNum:--"+calleridNum+"------(主叫输入的按键为)number：---"+number+"-----(坐席用户名)username：--"+username);
		if(number > 5) {
			logger.info("jrh 暂时 利安达公司只要小于5 的几个数字键，其他键没有设定！");
			return;
		}
		
		if("".equals(username) || username == null) {	// 如果配置文件中没有响应的配置项，则直接返回
			logger.warn("jrh [上海利安达贵金属经营有限公司]--缺少对 ec2.src.propertiefiles.andali.properties 配置，没有指定各按键对应的坐席！！！");
			return;
		}
		
		handleCaller(channel, username);
	}

	/**
	 * 根据通道信息，即被叫坐席的用户名，做出响应的处理
	 * @param channel
	 * @param username
	 * @throws AgiException
	 */
	private void handleCaller(AgiChannel channel, String username)
			throws AgiException {
		List<User> csrs = userService.getUsersByUsername(username);
		User csr = null;
		if(csrs.size() == 0) {	// 如果用户不存在，则呼叫到指定的队列
			dialSpecifiedQueue(channel);	return;
		} 
		
		csr = csrs.get(0);		// 如果用户存在，则取出第一个用户
		String exten = ShareData.userToExten.get(csr.getId());
		
		if(exten == null || "".equals(exten)) {		// 如果用户不在线，则呼叫到指定的队列
			dialSpecifiedQueue(channel); 	return;
		} 
		
		Set<String> csrToChannels = ShareData.peernameAndChannels.get(exten);
		if(csrToChannels.size() > 0) { 	// 说明坐席正在忙，则呼叫队列
			dialSpecifiedQueue(channel);
		} else {						// 如果，坐席是空闲的，则直接呼叫坐席分机
			channel.setVariable("ToExten", exten);
			logger.info("jrh -- dialed exten(被叫的分机) -----------> "+exten);
		}
	}

	/**
	 * 呼叫到指定的队列
	 * @param channel
	 * @throws AgiException
	 */
	private void dialSpecifiedQueue(AgiChannel channel) throws AgiException {
		String specifiedQueue = LiAnDaIncomingConfig.props.getProperty(LiAnDaIncomingConfig.SPECIFIED_QUEUE);
		channel.setVariable("ToQueue", specifiedQueue);
		logger.info("jrh -- dialed specified queue (被叫队列) -----------> "+specifiedQueue);
	}

    /**
     * 取出字符串中的数字，如原始数据位 ‘&’、 ‘0’等
     * @param originalData     原始数据
     * @return
     */
    private String pickOutNumbers(String originalData) {
          String numbers = "";
          originalData = StringUtils.trimToEmpty(originalData);
          for(int i = 0; i < originalData.length(); i++) {
               char c = originalData.charAt(i);
               int assiiCode = (int) c;
               if(assiiCode >= 48 && assiiCode <= 57) {
                    numbers = numbers + c;
               }
          }
          return numbers;
    }
    
}

/**
 * asterisk 拨号方案设置：
 * 
 * [incoming]
exten => _x.,1,GosubIf($["${recFileName}"=""]?monitor,m,1)
exten => _x.,n,Playback(silence/1)
exten => _x.,n,Agi(agi://${AGISERVERADDR}/blacklist.agi?direction=incoming)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;; 为 上海利安达贵金属经营有限公司】 专用的配置文件 ;;;;;;;;;;;

exten => _x.,n,Read(InputNum,commonsounds/li_an_da_incoming_ivr,1,,3,10)
exten => _x.,n,NoOp("客户输入按键---"+${InputNum})
exten => _x.,n,Agi(agi://${AGISERVERADDR}/liAnDaHandleIncomingCall.agi)
exten => _x.,n,GotoIf($["${NoNumber}"="yes"]?stop)
exten => _x.,n,GotoIf($["${ToQueue}"!=""]?dialToQueue)
exten => _x.,n,NoOp(${Exten})
exten => _x.,n,GotoIf($["${ToExten}"!=""]?dialToExten)
exten => _x.,n,GotoIf($["${ToSpecifyExten}"="yes"]?inputExten:stop)

exten => _x.,n(inputExten),Read(SpecifiedExten,commonsounds/li_an_da_input_exten,,,3,10)
exten => _x.,n,NoOp("客户输入分机---"+${SpecifiedExten})
exten => _x.,n,Agi(agi://${AGISERVERADDR}/liAnDaIncomingCallToExten.agi)
exten => _x.,n,GotoIf($["${ToExten}"!=""]?dialToExten)
exten => _x.,n,GotoIf($["${ToQueue}"!=""]?dialToQueue)
exten => _x.,n,GotoIf($["${isHangup}"="yes"]?stop)

exten => _x.,n(stop),Hangup()

exten => _x.,n(dialToQueue),Queue(${ToQueue},tT,,,${TimeOut})
exten => _x.,n,Hangup()

exten => _x.,n(dialToExten),Dial(SIP/${ToExten},${TimeOut},t)
exten => _x.,n,Hangup()

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 *
 */
