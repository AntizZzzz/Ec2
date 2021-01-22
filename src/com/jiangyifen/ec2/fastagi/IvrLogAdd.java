package com.jiangyifen.ec2.fastagi;

import java.util.Date;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.IVRLog;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.service.eaoservice.IVRLogService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.jiangyifen.ec2.utils.VoicemailConfig;

/**
 * 
 * @Description 描述：添加IVR Log 日志的agi
 * 
 * @author  jrh
 * @date    2014年2月20日 上午10:49:02
 * @version v1.0.0
 */
public class IvrLogAdd extends BaseAgiScript {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private IVRLogService ivrLogService = SpringContextHolder.getBean("ivrLogService");

	private SipConfigService sipConfigService = SpringContextHolder.getBean("sipConfigService");
	
	public void service(AgiRequest request, AgiChannel channel)	throws AgiException {

		/****************************** 获取参数******************************************/
		
		String ivrId = channel.getVariable("ivrId");
		String actionId = channel.getVariable("actionId");
		String optionId = channel.getVariable("optionId");
		String pressNumber = channel.getVariable("readnumber");
		String srcChannel = channel.getName();
		String uniqueid = channel.getUniqueId();
		String context = request.getContext();
		String extension = request.getExtension();
		String callerIdNumber = request.getCallerIdNumber();
		String callerIdName = request.getCallerIdName();
		String dest = channel.getVariable("dest");
		String node = request.getParameter("node");		// 暂时没有用，以后可能需要Agi(agi://${AGISERVERADDR}/ivrLogAdd.agi?node=${node})
		
		SipConfig outline = sipConfigService.getOutlineByOutlineName(dest);
		Long domainId = (outline != null) ? outline.getDomain().getId() : 0L;
		
		/**
		 * jinht 设置通道变量，进行判断是否进行语音留言
		 */
		channel.setVariable("voicemailExtenNoAnswer", VoicemailConfig.VOICEMAIL_EXTEN_NO_ANSWER);
		
		/****************************** 创建IVR Log记录 ******************************************/
		
		IVRLog ivrlog = new IVRLog();
		ivrlog.setCreateDate(new Date());
		ivrlog.setIvrId(ivrId);
		ivrlog.setActionId(actionId);
		ivrlog.setOptionId(optionId);
		ivrlog.setPressNumber(pressNumber);
		ivrlog.setChannel(srcChannel);
		ivrlog.setUniqueid(uniqueid);
		ivrlog.setContext(context);
		ivrlog.setExtension(extension);
		ivrlog.setCallerIdNumber(callerIdNumber);
		ivrlog.setCallerIdName(callerIdName);
		ivrlog.setDest(dest);
		ivrlog.setNode(node);
		ivrlog.setDomainId(domainId);

		
		/****************************** 存储IVR Log记录 ******************************************/
		
		try {
			ivrLogService.save(ivrlog);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 创建IVR Log 时 出现异常，所处extension为 ：["+extension+"]---->"+e.getMessage(), e);
		}
	}
	
}
