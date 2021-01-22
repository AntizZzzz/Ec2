package com.jiangyifen.ec2.service.csr.ami.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.asteriskjava.manager.action.QueuePauseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.bean.BusHandle;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 队列置忙Service
 * @author jrh
 */
public class QueuePauseServiceImpl implements QueuePauseService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void pause(String queue, String exten, boolean paused, String reason) {
		try {
			reason = URLEncoder.encode(reason, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("jrh 队列置忙，将置忙原因 UrlEncode 出现异常-->"+e.getMessage(), e);
		}
		
		// 队列置忙操作
		QueuePauseAction queuePauseAction = new QueuePauseAction("SIP/" + exten,queue,paused,reason);
		AmiManagerThread.sendAction(queuePauseAction);
		
	}

	@Override
	public boolean pauseAllByUser(List<String> queueNames, User user, String exten,
			boolean paused, String pauseAsteriskReason, String pauseRecordReason) {
		try {
			QueuePauseRecordService queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
			Date currentDate = new Date();
			for(String queueName : queueNames) {
				QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(user.getUsername(), exten, queueName);
				if(queuePauseRecord != null) {// 如果是重新置忙，为了确保数据的完整性，先检查是否已经存在置忙的记录，如果有，则先置闲，再置忙（这一步只是为了数据库中的记录完整）
					queuePauseRecord.setUnpauseDate(currentDate);
					queuePauseRecordService.update(queuePauseRecord);
				}
				// 创建新的置忙、置闲记录
				createNewQueuePauseRecord(user, exten, pauseRecordReason, queuePauseRecordService, currentDate, queueName);
				this.pause(queueName, exten, paused, pauseAsteriskReason);
			}
		} catch (Exception e) {
			logger.error("jrh 队列置忙, 出现异常-->"+e.getMessage(), e);
			return false;
		}

		return true;
	}
	
	@Override
	public boolean pauseAllByUser(List<String> queueNames, User user, String exten,
			boolean paused, String pauseAsteriskReason, String pauseRecordReason, boolean iscreatePauseLog) {
		try {
			QueuePauseRecordService queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
			Date currentDate = new Date();
			
			for(String queueName : queueNames) {
				
				if(iscreatePauseLog) {	// 判断是否需要创建置忙记录
					QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(user.getUsername(), exten, queueName);
					if(queuePauseRecord != null) {// 如果是重新置忙，为了确保数据的完整性，先检查是否已经存在置忙的记录，如果有，则先置闲，再置忙（这一步只是为了数据库中的记录完整）
						queuePauseRecord.setUnpauseDate(currentDate);
						queuePauseRecordService.update(queuePauseRecord);
					}
					// 创建新的置忙、置闲记录
					createNewQueuePauseRecord(user, exten, pauseRecordReason, queuePauseRecordService, currentDate, queueName);
				}
				
				this.pause(queueName, exten, paused, pauseAsteriskReason);
			}
		} catch (Exception e) {
			logger.error("jrh 队列置忙, 出现异常-->"+e.getMessage(), e);
			return false;
		}
		
		return true;
	}

	/**
	 * @Description 描述：创建置忙记录
	 *
	 * @author  jrh
	 * @date    2014年4月3日 上午10:03:55
	 * @param queuePauseRecordService
	 * @param user					用户对象			
	 * @param exten					分机
	 * @param pauseRecordReason		用于记录数据的原因
	 * @param currentDate			置忙时间
	 * @param queueName 			置忙队列
	 */
	private void createNewQueuePauseRecord(User user, String exten,
			String pauseRecordReason, QueuePauseRecordService queuePauseRecordService,
			Date currentDate, String queueName) {
		QueuePauseRecord newPauseRecord = new QueuePauseRecord();
		newPauseRecord.setUsername(user.getUsername());
		newPauseRecord.setSipname("SIP/" + exten);
		newPauseRecord.setPauseDate(currentDate);
		newPauseRecord.setReason(pauseRecordReason);
		newPauseRecord.setQueue(queueName);
		newPauseRecord.setDeptId(user.getDepartment().getId());
		newPauseRecord.setDeptName(user.getDepartment().getName());
		newPauseRecord.setDomainId(user.getDomain().getId());
		queuePauseRecordService.save(newPauseRecord);
	}

	@Override
	public BusHandle pauseAllByUid(Long userId, boolean paused, String pauseAsteriskReason, String pauseRecordReason) {
		BusHandle busHandle = new BusHandle();
		busHandle.setSuccess(false);
		busHandle.setNotice("操作失败！");
		try {
			UserQueueService userQueueService = SpringContextHolder.getBean("userQueueService");
			StaticQueueMemberService staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
			UserService userService = SpringContextHolder.getBean("userService");
			
			User user = userService.get(userId);
			if(user == null) {
				busHandle.setNotice("用户不存在！");
				return busHandle;
			}

			String exten = ShareData.userToExten.get(user.getId());
			if(exten == null || "".equals(exten)) {
				busHandle.setNotice("用户尚未登陆系统，无法置忙！");
				return busHandle;
			}
			
			ArrayList<String> queueNameList = new ArrayList<String>();			// 队列名集合
			// 从动态队列中查询获取该分机所属的队列
			for(UserQueue userQueue : userQueueService.getAllByUsername(user.getUsername())) {
				queueNameList.add(userQueue.getQueueName());
			}
			
			// 从静态队列中查询获取该分机所属的队列
			for(StaticQueueMember sqm : staticQueueMemberService.getAllBySipname(user.getDomain(), exten)) {
				queueNameList.add(sqm.getQueueName());
			}
			
			boolean result = pauseAllByUser(queueNameList, user, exten, paused, pauseAsteriskReason, pauseRecordReason);
			busHandle.setSuccess(result);
			if(result) {
				busHandle.setNotice("操作成功！");
			}
			return busHandle;
		} catch (Exception e) {
			logger.error("jrh 将指定用户的所有队列执行置忙、置闲操作时出现异常-->"+e.getMessage(), e);
			return busHandle;
		}
	}

	@Override
	public BusHandle pauseAllAndCreateLog(User loginUser, String exten, String reason, boolean isPaused, boolean isCreateLog) {
		BusHandle busHandle = new BusHandle();
		busHandle.setSuccess(false);
		busHandle.setNotice("失败，未知错误！");
		
		try {
			UserQueueService userQueueService = SpringContextHolder.getBean("userQueueService");
			StaticQueueMemberService staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
			QueuePauseRecordService queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");

			List<String> queueNameLs = new ArrayList<String>();

			// 从动态队列中查询获取该分机所属的队列
			for(UserQueue userQueue : userQueueService.getAllByUsername(loginUser.getUsername())) {
				queueNameLs.add(userQueue.getQueueName());
			}
			
			// 从静态队列中查询获取该分机所属的队列
			for(StaticQueueMember sqm : staticQueueMemberService.getAllBySipname(loginUser.getDomain(), exten)) {
				queueNameLs.add(sqm.getQueueName());
			}

			if(queueNameLs.size() == 0) {
				busHandle.setNotice("当前分机尚未关联队列！");
				return busHandle;
			}
			
			// 如果所有队列都闲，则将所有队列直接置忙
			Date currentDate = new Date();
			for(String queueName : queueNameLs) {
				this.pause(queueName, exten, isPaused, reason);
				
				if(isCreateLog) {	// 检查是否需要创建置忙记录
					QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(loginUser.getUsername(), exten, queueName);
					if(queuePauseRecord != null) {
						queuePauseRecord.setUnpauseDate(currentDate);
						queuePauseRecordService.update(queuePauseRecord);
					}
					
					createNewPauseRecord(loginUser, reason, currentDate, queueName, exten);
				}
			}

			busHandle.setSuccess(true);
			busHandle.setNotice("成功！");
		} catch (Exception e) {
			busHandle.setNotice("未知错误！");
			logger.error("JRH 将指定用户的所有队列执行置忙、置闲操作时出现异常-->"+e.getMessage(), e);
		}
		
		return busHandle;
	}

	/**
	 * @Description 描述：创建置忙置闲记录
	 *
	 * @author  JRH
	 * @date    2014年8月11日 下午12:11:38
	 * @param loginUser		当前需要置忙或置闲的坐席
	 * @param reason		置忙置闲的原因
	 * @param currentDate	置忙置闲的时间
	 * @param queueName		待置忙置闲的队列
	 * @param exten 		需要置忙置闲的分机
	 */
	private void createNewPauseRecord(User loginUser, String reason, Date currentDate, String queueName, String exten) {

		QueuePauseRecordService queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
		
		QueuePauseRecord newPauseRecord = new QueuePauseRecord();
		newPauseRecord.setUsername(loginUser.getUsername());
		newPauseRecord.setSipname("SIP/" + exten);
		newPauseRecord.setPauseDate(currentDate);
		newPauseRecord.setReason(reason);
		newPauseRecord.setQueue(queueName);
		newPauseRecord.setDeptId(loginUser.getDepartment().getId());
		newPauseRecord.setDeptName(loginUser.getDepartment().getName());
		newPauseRecord.setDomainId(loginUser.getDomain().getId());
		queuePauseRecordService.save(newPauseRecord);
	}
	
}
