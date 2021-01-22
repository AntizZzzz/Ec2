package com.jiangyifen.ec2.fastagi;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.asteriskjava.manager.event.QueueEntryEvent;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.jiangyifen.ec2.utils.VoicemailConfig;

/**
 * @Description 描述：客户呼入队列之前，检测队列下成员的状态
 *
 * @author jinht
 *
 * @date 2015-7-6 上午11:24:50 
 * 
 * @version v1.0.0
 */
public class CheckQueueBusyStatus extends BaseAgiScript {

	private QueueService queueService = SpringContextHolder.getBean("queueService");
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		String queueName = request.getParameter("queueName");
		queueName = StringUtils.trimToEmpty(queueName);
		
		if("".equals(queueName)) {		// 如果队列名称为空，则不再继续执行
			return;
		}
		
		Queue queue = queueService.getQueueByQueueName(queueName);
		if(queue == null) {				// 如果根据队列名称没有找到该队列的实体信息，则不在继续执行
			return;
		}

		/**
		 * jinht 设置通道变量，进行判断是否进行语音留言
		 */
		channel.setVariable("voicemailQueueMembersBusy", VoicemailConfig.VOICEMAIL_QUEUE_MEMBERS_BUSY);
		
//		List<String> extensList = ShareData.queue2Members.get(queueName);						// 根据队列名称进行查询该队列下队列成员的信息
		
		Integer usableMembersCount = AutoDialHolder.queueToAvailable.get(queueName);			// 根据队列名称进行查询该队列中可用成员的数量
		if(usableMembersCount == null || usableMembersCount == 0) {
			channel.setVariable("usableMembersCount", "");										// 设置通道变量，标记该队列中所有成员为置忙状态
		} else {
			channel.setVariable("usableMembersCount", ""+usableMembersCount);
		}
		
		List<QueueEntryEvent> queueWaitersList =  AutoDialHolder.queueToWaiters.get(queueName);	// 根据队列名称进行查询该队列中排队人的信息
		if(queueWaitersList == null || queueWaitersList.size() == 0) {
			channel.setVariable("queueWaitersCount", "");										// 设置队列中等待成员的数量
		} else {
			channel.setVariable("queueWaitersCount", ""+queueWaitersList.size());
		}
		
	}

}
