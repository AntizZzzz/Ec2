package com.jiangyifen.ec2.backgroundthread;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 外转外定时任务
 * @author jrh
 *
 */
public class Phone2PhoneStartJob implements Job {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// 从JobDetail 自带的Map结合，用于存放外转外配置的key
	public static final String P2PSETTING = "phone2phoneSetting";

	private QueueService queueService = SpringContextHolder.getBean("queueService");											// 队列服务类
	private UserQueueService userQueueService = SpringContextHolder.getBean("userQueueService");								// 动态队列成员服务类
	private QueueMemberRelationService queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");	// 队列成员关系管理服务类
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getTrigger().getJobDataMap();
		
		Phone2PhoneSetting p2pSetting = (Phone2PhoneSetting) dataMap.get(Phone2PhoneStartJob.P2PSETTING);
		Domain domain = p2pSetting.getDomain();

		// 如果默认外线不存在，则直接退出
		String defaultOutline = ShareData.domainToDefaultOutline.get(domain.getId());
		if(defaultOutline == null) {
			return ;
		}
		
		// 获取所有非自动使用的队列
		List<Queue> allCommonQueues = queueService.getAllByDomain(domain, true);
		List<String> allCommonQueueNames = new ArrayList<String>();
		for(Queue autoQueue : allCommonQueues) {
			allCommonQueueNames.add(autoQueue.getName());
		}
			
		// 判断启动的是全局配置项，还是自定义配置项
		if(p2pSetting.getIsGlobalSetting()) {
			startGlobalP2PSchedule(p2pSetting, defaultOutline, allCommonQueueNames);
			logger.info("------------global setting start");
		} else {
			User creator = p2pSetting.getCreator();
			addCsrPhone2QueueMember(creator, defaultOutline, allCommonQueueNames);
			logger.info("------------custom setting start");
		}
	}


	/**
	 * 启动全局外转外
	 * @param gloablSetting
	 */
	private void startGlobalP2PSchedule(Phone2PhoneSetting gloablSetting, String defaultOutline, 
			List<String> allCommonQueueNames) {
	
		// 如果当前是“便捷呼叫”（转呼到指定的手机号），则将指定的所有手机号加入所有队列
		if(gloablSetting.getIsSpecifiedPhones()) {
			// 第一步： 将所有的手机加入各非自动外呼生成的队列
			for(String phoneNumber : gloablSetting.getSpecifiedPhones()) {
				for(String queueName : allCommonQueueNames) {
					queueMemberRelationService.addQueueMemberRelation(queueName, phoneNumber+"@"+defaultOutline, 5);
				}
			}
			// 第二步：将所有的动态队列成员对应的分机，及静态队列中的分机，从非自动外呼生成的队列中移除（实现方法是将所有在线的分机从队列中移除，这样可以不必查数据库，减少数据库的压力）
			for(String exten : ShareData.userToExten.values()) { 
				for(String queueName : allCommonQueueNames) {	// 无论普通动态队列还是静态队列都得移除分机成员
					queueMemberRelationService.removeQueueMemberRelation(queueName, exten);
				}
			}
		} else { // 如果当前是指定了话务员才有转呼手机，则将指定话务员的手机号加入队列
			for(User currentCsr : gloablSetting.getSpecifiedCsrs()) {
				addCsrPhone2QueueMember(currentCsr, defaultOutline, allCommonQueueNames);
			}
		}
	}
	
	/**
	 * 根据话务员对象，如果话务员有电话，并且不在线，则添加手机成员
	 * @param csr					话务员对象
	 * @param defaultOutline		默认外线
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void addCsrPhone2QueueMember(User csr, String defaultOutline, List<String> allCommonQueueNames) {
		// 如果用户已经登陆，则不做处理， 因为用户已登录的情况，呼入队列时找分机
		if(ShareData.userToExten.keySet().contains(csr.getId())) {
			return;
		}
		String phoneNum = csr.getPhoneNumber();
		if(phoneNum == null || "".equals(phoneNum)) {	// 话务员必须有电话号
			return;
		}
		// 只有话务员不在线，才需要将话务员的手机号加入队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				queueMemberRelationService.addQueueMemberRelation(queueName, phoneNum+"@"+defaultOutline, 5);
			}
		}
	}

}
