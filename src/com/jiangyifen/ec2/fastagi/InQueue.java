package com.jiangyifen.ec2.fastagi;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：客户呼入队列，并且跟坐席建立通话时调用的AGI
 * 
 * 	dialqueue,n,Queue(${QueueName},tT,,,${TimeOut},agi://${AGISERVERADDR}/inQueue.agi)
 *
 * @author  jrh
 * @date    2014年3月13日 下午2:20:12
 * @version v1.0.0
 */
public class InQueue extends BaseAgiScript {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private QueueService queueService = SpringContextHolder.getBean("queueService");

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		//开始流程
		String queueMember = this.getVariable("MEMBERINTERFACE");
		String queueName = request.getParameter("inQueueName");
		queueMember = StringUtils.trimToEmpty(queueMember);
		queueName = StringUtils.trimToEmpty(queueName);
		String exten = queueMember.substring(queueMember.indexOf('/') + 1);
		
		/***************************** for test *************************/
//		System.out.println("-----MEMBERNAME-----"+this.getVariable("MEMBERNAME"));
//		System.out.println("----QUEUENAME------"+this.getVariable("QUEUENAME"));
//		System.out.println("----inQueueName------"+request.getParameter("inQueueName"));
		
		if("".equals(exten) || "".equals(queueName)) {				// 如果队列成员为空，直接返回【正常不会出现】
			return;		
		}
		
		Queue queue = queueService.getQueueByQueueName(queueName);
		if(queue != null) {											// 正常情况一定不会为空
			String sayContent = null;								// 要播报的数字内容
			String sayDigitsType = queue.getReadDigitsType();		// 从队列中获取其播报配置信息
			
			if (Queue.READ_DIGITS_TYPE_READ_EXTEN.equals(sayDigitsType)) {		// 报服务分机号
				sayContent = exten;
				
			} else if (Queue.READ_DIGITS_TYPE_READ_EMPNO.equals(sayDigitsType)) {		// 报服务坐席工号
				Long userId = ShareData.extenToUser.get(exten);
				if(userId != null) {
					User user = userService.get(userId);
					String empNo = (user != null) ? user.getEmpNo() : null;
					sayContent = (empNo != null) ? empNo : null;
				}
			}

			executeReadFiles(sayContent);	// 执行给客户播报语音文件
		}

	}

	/**
	 * @Description 描述：执行给客户播报语音文件
	 *
	 * @author  jrh
	 * @date    2014年3月13日 下午6:07:43
	 * @param readContent		要播报的数字内容
	 * @throws AgiException void
	 */
	private void executeReadFiles(String readContent) throws AgiException {
		try {
			if(readContent != null) {
				// this.exec("wait", "1");
				this.streamFile("commonsounds/gonghao");
				this.sayDigits(readContent);
				this.streamFile("commonsounds/c_foryou");
			}
		} catch (Exception e) {
			logger.warn("jrh 客户呼入队列，客户未听完[工号xxx为您服务]的语音就挂断，导致的异常，可忽略"+e.getMessage());
		}
	}
	
}
