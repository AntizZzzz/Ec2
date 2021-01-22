package com.jiangyifen.ec2.fastagi;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * 队列中没有空闲成员进入语音信箱(随机获取队列成员的号码)
 * 
 * <p>根据队列随机获取用户Id</p>
 * 
 * <p>在语音信箱，没有进入队列的情况下调用</p>
 *
 * @version $Id: GetQueueRandomUserid.java 2014-6-19 下午1:17:38 chenhb $
 *
 */
public class GetQueueRandomUserid extends BaseAgiScript {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {

		String queuename = request.getParameter("queuename");
		
		UserQueueService userQueueService=SpringContextHolder.getBean("userQueueService");
		QueueService queueService=SpringContextHolder.getBean("queueService");

		// 获取该队列的所有 用户队列对应关系
		Queue queue=queueService.getQueueByQueueName(queuename);
		if(queue==null){
			return;
		}
		
		UserService userService=SpringContextHolder.getBean("userService");
		
		//队列列表
		List<UserQueue> userQueues = userQueueService.getAllByQueueName(queuename, queue.getDomain());
		if(userQueues != null && userQueues.size() > 0) {
			int randResult = new Random().nextInt(userQueues.size());
			UserQueue userQueue = userQueues.get(randResult);
			//获取用户名，获取id
			if(userQueue!=null){
				String username=userQueue.getUsername();
				if(StringUtils.isEmpty(username)){
					//do nothing
				}else{
					List<User> userList = userService.getUsersByUsername(username);
					if(userList!=null&&userList.size()>0){
						User user = userList.get(0);
						if(user!=null){
							channel.setVariable("voicemailUserid",  user.getId().toString());
			                logger.info("chenhb: channel-->"+channel.getName()+" voicemailUserid --> "+user.getId());
						}
					}
				}
			}
		}else{
			return;
		}
		
	}

}
