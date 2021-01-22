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
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 外转外定时停止任务
 * @author jrh
 *
 */
public class Phone2PhoneStopJob implements Job {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// 从JobDetail 自带的Map结合，用于存放外转外配置的key
	public static final String P2PSETTING = "phone2phoneSetting";

	private UserService userService = SpringContextHolder.getBean("userService");												// 用户服务类
	private QueueService queueService = SpringContextHolder.getBean("queueService");											// 队列服务类
	private UserQueueService userQueueService = SpringContextHolder.getBean("userQueueService");								// 动态队列成员服务类
	private StaticQueueMemberService staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService"); 		// 静态队列成员服务类
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
			stoptGlobalP2PSchedule(p2pSetting, defaultOutline, allCommonQueueNames);
			logger.info("------------global setting stop");
		} else {
			User creator = p2pSetting.getCreator();
			removeCsrPhone2QueueMember(creator, defaultOutline, allCommonQueueNames);
			logger.info("------------custom setting stop");
		}
	}

	/**
	 * 终止全局外转外配置（如果是“便捷呼叫”，移除手机成员后，还需要将分机加入队列）
	 * @param gloablSetting			全局外转外配置
	 * @param defaultOutline		默认外线
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void stoptGlobalP2PSchedule(Phone2PhoneSetting gloablSetting,
			String defaultOutline, List<String> allCommonQueueNames) {
		// 如果当前是“便捷呼叫”（转呼到指定的手机号），则将指定的所有手机号加入所有队列
		if(gloablSetting.getIsSpecifiedPhones()) {
			// 第一步： 将所有的手机从各非自动外呼生成的队列移除
			for(String phoneNumber : gloablSetting.getSpecifiedPhones()) {
				for(String queueName : allCommonQueueNames) {
					queueMemberRelationService.removeQueueMemberRelation(queueName, phoneNumber+"@"+defaultOutline);
				}
			}
			// 第二步：将所有的在线话务员使用的分机加入各队列
			for(Long csrId : ShareData.userToExten.keySet()) {
				User csr = userService.get(csrId);
				addExten2QueueMember(csr, allCommonQueueNames);
			}
		} else { // 如果当前是指定了话务员才有转呼手机，则在停止时，需要将话务员的手机从队列成员中移除（这里不需要考虑将分机移入队列，因为在线的话，分机肯定在队列中了）
			for(User specifiedCsr : gloablSetting.getSpecifiedCsrs()) {
				removeCsrPhone2QueueMember(specifiedCsr, defaultOutline, allCommonQueueNames);
			}
		}
	}

	/**
	 * 将分机加入队列成员
	 * @param csrId					话务员id
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void addExten2QueueMember(User csr, List<String> allCommonQueueNames) {
		String exten = ShareData.userToExten.get(csr.getId());
		// 取出用户对应的所有的动态队列
		List<UserQueue> userQueueList = userQueueService.getAllByUsername(csr.getUsername());
		for (UserQueue userQueue : userQueueList) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				queueMemberRelationService.addQueueMemberRelation(queueName, exten, userQueue.getPriority());
			}
		}
		
		// 取出用户对应的所有的静态队列
		List<StaticQueueMember> staticQMs = staticQueueMemberService.getAllBySipname(csr.getDomain(), exten);
		for (StaticQueueMember sqm : staticQMs) {
			queueMemberRelationService.addQueueMemberRelation(sqm.getQueueName(), exten, sqm.getPriority());
		}
	}
	
	/**
	 * 根据话务员对象，如果话务员有电话，并且不在线，则移除手机成员
	 * @param csr					话务员对象
	 * @param defaultOutline		默认外线
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void removeCsrPhone2QueueMember(User csr,
			String defaultOutline, List<String> allCommonQueueNames) {
		// 如果用户已经登陆，则不做处理， 因为用户已登录的情况，呼入队列时找分机
		if(ShareData.userToExten.keySet().contains(csr.getId())) {
			return;
		}
		String phoneNum = csr.getPhoneNumber();
		if(phoneNum == null || "".equals(phoneNum)) {	// 话务员必须有电话号
			return;
		}
		// 只有话务员不在线，才需要将话务员的手机号移出队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				queueMemberRelationService.removeQueueMemberRelation(queueName, phoneNum+"@"+defaultOutline);
			}
		}
	}

}
