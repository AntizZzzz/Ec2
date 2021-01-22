package com.jiangyifen.ec2.fastagi;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

//TODO
/**
 * @Description 描述：检查当前主呼叫的号码是否为其他租户的分机或者队列等，如果是，则应该改成呼出，而不能按内部呼叫流程走
 * 
 * z转接时有问题    
 *
 * @author  JRH
 */
public class CheckDialAcrossDomain extends BaseAgiScript{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private QueueService queueService = SpringContextHolder.getBean("queueService");
	
	@Override
	public void service(AgiRequest request, AgiChannel channel)	throws AgiException {
		
		try {
			
			/****************************** 获取参数, 初始化通道变量  ******************************************/
			
			String calleridnum = StringUtils.trimToEmpty(request.getCallerIdNumber());
			String destlinenum = StringUtils.trimToEmpty(request.getParameter("destlinenum"));
			String outlinenum = StringUtils.trimToEmpty(channel.getVariable("outline"));
			
			channel.setVariable("ACROSSTYPE", "");
			
//			/***************************** for test ***********************/
//			String extension = request.getExtension();
//			System.out.println("extension-------"+extension);
//			System.out.println("calleridnum-------"+calleridnum);
//			System.out.println("destlinenum-------"+destlinenum);
//			System.out.println("outlinenum-------"+outlinenum);
			
			/***************************** for check across domain dial exten ***********************/
			
			Long callerDomainId = 0L;
			Long calleeDomainId = 0L;
			Long outlineDomainId = 0L;
			if( ShareData.domainToExts != null ) {
				for(Long domainId : ShareData.domainToExts.keySet()) {
					List<String> extenLs = ShareData.domainToExts.get(domainId);
					if(extenLs != null) {
						if(extenLs.contains(calleridnum)) {	// 主叫所属域
							callerDomainId = domainId;
						}
						if(extenLs.contains(destlinenum)) {	// 被叫所属域
							calleeDomainId = domainId;
						}
					}
				}
			}
			
			if(outlinenum != null && !"".equals(outlinenum)) {		// 获取外线对应的域
				for(Long domainId : ShareData.domainToOutlines.keySet()) {
					List<String> outlineLs = ShareData.domainToOutlines.get(domainId);
					if(outlineLs != null && outlineLs.contains(outlinenum)) {
						outlineDomainId = domainId;
						break;
					}
				}
			}
			
			// if(destlinenum.matches("8{1}\\d{5}") && calleridnum.matches("8{1}\\d{5}") && !callerDomainId.equals(calleeDomainId)) {
			// TODO 武睿定制, 分机号改为 6 开头 五位数
			if(destlinenum.matches("6{1}\\d{4}") && calleridnum.matches("6{1}\\d{4}") && !callerDomainId.equals(calleeDomainId)) {	// 解决跨域情况：1、分机呼叫其他域的分机； 2、分机呼叫本域内分机后，然后转接给其他域的分机 【这种情况 calleridnum = '分机号'】
				channel.setVariable("ACROSSTYPE", "acrossDomainExt");
				logger.info("JRH ---> exten:"+calleridnum+" [originate or transfer] a call to exten:"+destlinenum+" which is acrossing domain!");
				return;
			// } else if(destlinenum.matches("8{1}\\d{5}") && !calleeDomainId.equals(outlineDomainId)) {
			// TODO 武睿定制, 分机号改为 6 开头五位数
			} else if(destlinenum.matches("6{1}\\d{4}") && !calleeDomainId.equals(outlineDomainId)) { // 解决跨域情况：1、客户呼入，坐席接起后转接到其他域的分机；2、坐席呼叫手机，然后转接到其他域的分机  【这两种情况 calleridnum = '手机号码'】
				channel.setVariable("ACROSSTYPE", "acrossDomainExt");
				logger.info("JRH ---> exten:"+calleridnum+" [transfer] a call to exten:"+destlinenum+" which is acrossing domain!");
				return;
			}
			
			/***************************** for check across domain dial queue ***********************/
			
			if(destlinenum.matches("9{1}\\d{5}") && !"900000".equals(destlinenum)) {	// 被叫是队列,但不是特殊队列 900000
				Queue destQueue = queueService.getQueueByQueueName(destlinenum);
				if(destQueue != null) {
					calleeDomainId = destQueue.getDomain().getId();
				}

				if(!callerDomainId.equals(calleeDomainId)) {
					channel.setVariable("ACROSSTYPE", "acrossDomainQueue");
					logger.info("JRH ---> exten:"+calleridnum+" originate a call to queue:"+destlinenum+" which is acrossing domain!");
					return;
				}
			}

			/***************************** for check across domain dial others ***********************/
			
		} catch (Exception e) {
			logger.error("JRH --- 判断跨域呼叫Agi出现异常-->"+e.getMessage(), e);
		}
		
	}
	
}
