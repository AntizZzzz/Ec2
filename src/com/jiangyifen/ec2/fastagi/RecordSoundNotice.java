package com.jiangyifen.ec2.fastagi;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：判断是否需要进行录音提醒
 *
 * @author  JRH
 * @date    2014年9月3日 下午2:26:44
 */
public class RecordSoundNotice extends BaseAgiScript {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private QueueService queueService = SpringContextHolder.getBean("queueService");

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {

		//开始流程
		String queueName = request.getParameter("inQueueName");
		queueName = StringUtils.trimToEmpty(queueName);
		
		/***************************** for test *************************/
//		System.out.println("----inQueueName------"+request.getParameter("inQueueName"));
		
		if("".equals(queueName)) {				// 如果队列成员为空，直接返回【正常不会出现】
			return;		
		}
		
		Queue queue = queueService.getQueueByQueueName(queueName);
		if(queue != null && queue.getIsrecordSoundNotice()) {	// 队列不为空，并且队列开启了录音提醒
			try {
				this.streamFile("commonsounds/record_sound_notice");
			} catch (Exception e) {
				logger.warn("jrh 客户呼入队列，客户未听完[您的通话可能被录音，请谅解]的语音就挂断，导致的异常，可忽略"+e.getMessage());
			}
		}
		
	}
	
}
