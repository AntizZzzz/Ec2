package com.jiangyifen.ec2.backgroundthread;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.KickCsrLogoutSettingService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：全局定时任务管理器
 * 	目前只用来管理定时让坐席退出系统的功能，以后如果还有类似的定时任务需要开发，如果功能不大，就可以在这个类里面添加一个定时任务管理组
 * 
 * 	新增定时任务管理组需要新建的参数有：
 * 			1、启动任务的static final 变量  		 如：XXX_JOB
 * 			2、启动任务组的static final 变量 		 如：XXX_GROUP
 * 			3、JobDetail						 如：xxxJob(需要一个实现类)
 * 
 * @author  jrh
 * @date    2013年12月19日 上午10:00:40
 * @version v1.0.0
 */
public class GlobalSettingScheduler {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// 定时让坐席退出系统的  启动任务
	private static final String KICK_CSR_JOB = "kickCsrJob";
	// 定时让坐席退出系统的  启动任务组
	private static final String KICK_CSR_GROUP = "kickCsrGroup";
		
	private static GlobalSettingScheduler globalSettingScheduler;
	
	private Scheduler scheduler;	// 计划
	private JobDetail kickCsrLogoutJob ;	// 定时让坐席退出系统

	/**
	 * 构造器初始化
	 * @throws SchedulerException
	 */
	private GlobalSettingScheduler(){
		try {
			//初始化Scheduler
			SchedulerFactory sf = new StdSchedulerFactory();
			scheduler = sf.getScheduler();
			scheduler.start();
			
			//初始化Job
			kickCsrLogoutJob = JobBuilder.newJob(KickCsrLogoutJob.class)
					.withIdentity(KICK_CSR_JOB, KICK_CSR_GROUP).storeDurably(true)
					.build();
			scheduler.addJob(kickCsrLogoutJob, false);
		} catch (SchedulerException e) {
			logger.error("创建全局定时任务的Scheduler 和 	Job 时出错"+e.getMessage(), e);
		}
	}

	/**
	 * 取得单例
	 * @return
	 */
	public static GlobalSettingScheduler getSingleton(){
		if(globalSettingScheduler == null){
			globalSettingScheduler = new GlobalSettingScheduler();
		}
		
		return globalSettingScheduler;
	}

	/**
	 * 给指定的租户创建一个定时让坐席退出系统的检测器
	 * @param cronSchedules 定时启动时间
	 * @param domainId		租户所属域的编号
	 * @return Boolean 		是否成功
	 */
	public Boolean addKickCsrTrigger(String cronSchedule, Domain domain) {
		Long domainId = domain.getId();
		try {
			// 在新建之前，需要先移除一下旧的定时器
			removeKickCsrTrigger(domain);
			
			// 然后创建定时定时启动剔除坐席的监控器
			Trigger startTrigger = TriggerBuilder.newTrigger()
					.withIdentity(domainId.toString(), KICK_CSR_GROUP)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule))
					.forJob(kickCsrLogoutJob)
					.build();
			startTrigger.getJobDataMap().put(KickCsrLogoutJob.KICKED_DOMAIN_ID, domain);
			Date executeStartTime = scheduler.scheduleJob(startTrigger);
			logger.info("成功创建定时让坐席退出系统的定时器，租户对应的 domain id 为 "+domainId+" 将在"+executeStartTime+" 启动！");
		} catch (SchedulerException e) {
			logger.error("创建定时让坐席退出系统的定时器失败，租户对应的 domain id 为 "+domainId+"任务失败！"+e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	/**
	 * 当管理员关闭自动让坐席退出系统的功能，或者更改定时的策略时，都需要将原有的定时任务关闭
	 * @param domainId 需要停止定时让坐席退出系统的租户，其所属域的编号
	 * @return Boolean 是否移除成功
	 */
	public boolean removeKickCsrTrigger(Domain domain) {
		Long domainId = domain.getId();
		try {
			Trigger oldStartKickCsrTrigger = scheduler.getTrigger(TriggerKey.triggerKey(domainId.toString(), KICK_CSR_GROUP));
			if(oldStartKickCsrTrigger != null) {
				scheduler.unscheduleJob(oldStartKickCsrTrigger.getKey());
			}
			logger.info("成功停止定时让坐席退出系统的定时器，租户对应的 domain id 为 "+domainId);
		} catch (SchedulerException e) {
			logger.error("停止定时让坐席退出系统的定时器失败，租户对应的 domain id 为 "+domainId+"停止出现异常！"+e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	/**
	 * @Description 启动指定租户的强制坐席退出的定时器
	 * @author  JiangRH
	 * @param domain void
	 */
	public void startSchedulerByDomain(Domain domain) {
		try {
			KickCsrLogoutSettingService kickCsrLogoutSettingService = SpringContextHolder.getBean("kickCsrLogoutSettingService");
			kickCsrLogoutSettingService.startGlobalSettingchedulerByDomain(domain);
		} catch (Exception e) {
			logger.error("JRH 程序重启时，启动定时强制坐席退出的定时器失败！", e);
		}
	}
	
}
