package com.jiangyifen.ec2.fastagi;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 存储自动外呼的应答信息
 * @author
 */
public class AutodialAnswered extends BaseAgiScript {

	private MarketingProjectTaskService marketingProjectTaskService;

	public void service(AgiRequest agiRequest, AgiChannel agiChannel)
			throws AgiException {
		//根据TaskId取出Task，设置为自动外呼客户接通状态
		String taskId = agiChannel.getVariable("taskId");
		if(marketingProjectTaskService==null){
			marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
		}
		MarketingProjectTask task=marketingProjectTaskService.get(Long.parseLong(taskId));
		task.setAutodialAnsweredTime(new Date());
		task.setAutodialIsAnswered(true);
		//存储Task到数据库
		marketingProjectTaskService.update(task);

		/*****************  为呼入弹屏，收集呼入信息 (最好另做一个agi)  ****************************/
		collectInfosToIncomingWindow(agiRequest, agiChannel, task);		
		/******************************************************************************/
	}

	/**
	 * @Description 描述：为呼入弹屏，收集呼入信息
	 *
	 * @author  JRH
	 * @date    2014年7月11日 下午8:31:00
	 * @param agiRequest
	 * @param agiChannel
	 * @param task
	 * @throws AgiException void
	 */
	private void collectInfosToIncomingWindow(AgiRequest agiRequest, AgiChannel agiChannel, MarketingProjectTask task) throws AgiException{
		/**************2014-07-11 新增，为了节省后期更新的问题，暂时不重新写一个agi, 直接在这里处理主叫使用的是那条外线 打进系统的问题 *******************/
		/***********Map<域的id, ConcurrentHashMap<主叫号码，呼入信息>> 如Map<1,ConcurrentHashMap<"13816760365", 呼入信息>>*********/
		
		Long domainId = task.getDomain().getId();
		String outlineNumber = agiChannel.getVariable("outlineNumber");

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
				incomingDialInfo.setVasOutline(outlineNumber);
				incomingDialInfo.setAutoDial(true);
				incomingDialInfo.setDialType("autodial");
				incomingDialInfo.setTaskId(task.getId());
				incomingDialInfo.setProjectId(task.getMarketingProject().getId());
				incomingCallerToDialInfos.put(callerIdNumber, incomingDialInfo);
			}
		}
	}
	
//	AgiRequest[script='autodialAnswered.agi',requestURL='agi://192.168.1.160/autodialAnswered.agi',
//	channel='SIP/88860847041-0000003f',uniqueId='1405082533.63',type='SIP',language='en',
//	callerIdNumber='13816760398',callerIdName='13816760398',dnid='null',rdnis='null',context='incoming',
//	extension='autodial',priority='2',enhanced='false',accountCode='null',systemHashcode=314434333]
	
}
