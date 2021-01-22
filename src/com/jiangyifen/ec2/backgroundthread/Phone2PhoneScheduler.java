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
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 外转外启动计划
 * @author jrh
 *
 */
public class Phone2PhoneScheduler {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// 启动任务
	private static final String START_JOB = "startJob";
	// 启动任务组
	private static final String START_GROUP = "startGroup";
	// 停止任务
	private static final String STOP_JOB = "stopJob";
	// 停止任务组
	private static final String STOP_GROUP = "stopGroup";

	private static Phone2PhoneScheduler p2pSchedule;
	private Scheduler scheduler;	// 计划
	private JobDetail startJob ;	// 启动任务
	private JobDetail stopJob ;		// 停止任务
	
	/**
	 * 构造器初始化
	 * @throws SchedulerException
	 */
	private Phone2PhoneScheduler(){
		try {
			//初始化Scheduler
			SchedulerFactory sf = new StdSchedulerFactory();
			scheduler = sf.getScheduler();
			scheduler.start();
			
			//初始化Job
			startJob = JobBuilder.newJob(Phone2PhoneStartJob.class)
					.withIdentity(START_JOB, START_GROUP).storeDurably(true)
					.build();
			stopJob = JobBuilder.newJob(Phone2PhoneStartJob.class)
					.withIdentity(STOP_JOB, STOP_GROUP).storeDurably(true)
					.build();
			scheduler.addJob(startJob, false);
			scheduler.addJob(stopJob, false);
		} catch (SchedulerException e) {
			logger.error("创建外转外定时任务的Scheduler 和 	Job 时出错"+e.getMessage(), e);
		}
	}
	
	/**
	 * 取得单例
	 * @return
	 */
	public static Phone2PhoneScheduler getSingleton(){
		if(p2pSchedule==null){
			p2pSchedule=new Phone2PhoneScheduler();
		}
		return p2pSchedule;
	}
	
	/**
	 * 为Phone2PhoneStartJob和Phone2PhoneStopJob 各添加一个Trigger
	 * @param cronSchedules 定时启动时间
	 * @param p2pSetting	外转外配置对象
	 * @return Boolean 是否成功
	 */
	public Boolean addTrigger(String[] cronSchedules, Phone2PhoneSetting p2pSetting) {
		Long userId = p2pSetting.getCreator().getId();
		try {
			// 在新建之前，需要先移除一下旧的定时器
			removeTrigger(p2pSetting);
			
			// 然后创建定时开启外传外的定时器    以及   定时关闭外传外的定时器
			Trigger startTrigger = TriggerBuilder.newTrigger()
					.withIdentity(userId.toString(), START_GROUP)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedules[0]))
					.forJob(startJob)
					.build();
			startTrigger.getJobDataMap().put(Phone2PhoneStartJob.P2PSETTING, p2pSetting);
			Date executeStartTime = scheduler.scheduleJob(startTrigger);
			logger.info("创建启动定时器 id 为 "+p2pSetting.getId()+" 的外转外定时任务在"+executeStartTime+" 启动！");
				
			Trigger stopTrigger = TriggerBuilder.newTrigger()
					.withIdentity(userId.toString(), STOP_GROUP)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronSchedules[1]))
					.forJob(stopJob)
					.build();
			stopTrigger.getJobDataMap().put(Phone2PhoneStartJob.P2PSETTING, p2pSetting);
			Date executeStopTime = scheduler.scheduleJob(stopTrigger);
			logger.info("创建终止定时器 id 为 "+p2pSetting.getId()+" 的外转外定时任务在"+executeStopTime+" 终止！");
		} catch (SchedulerException e) {
			logger.error("为用户id 为 "+userId+"的话务员或管理员创建定时外转外任务失败！"+e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	/**
	 * 为Phone2PhoneStartJob和Phone2PhoneStopJob 各移除一个Trigger
	 * @param p2pSetting	外转外配置对象
	 * @return Boolean 是否移除成功
	 */
	public boolean removeTrigger(Phone2PhoneSetting p2pSetting) {
		Long userId = p2pSetting.getCreator().getId();
		try {
			Trigger oldStartTrigger = scheduler.getTrigger(TriggerKey.triggerKey(userId.toString(), START_GROUP));
			Trigger oldStopTrigger = scheduler.getTrigger(TriggerKey.triggerKey(userId.toString(), STOP_GROUP));
			if(oldStartTrigger != null) {
				scheduler.unscheduleJob(oldStartTrigger.getKey());
			}
			if(oldStopTrigger != null) {
				scheduler.unscheduleJob(oldStopTrigger.getKey());
			}
			logger.info("终止外传外定时器 id 为 "+p2pSetting.getId()+" 的外转外定时任务终止！");
		} catch (SchedulerException e) {
			logger.error("创建启动定时器  为用户id 为 "+userId+"的话务员或管理员关闭定时外转外任务失败！"+e.getMessage(), e);
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
			Phone2PhoneSettingService phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
			phone2PhoneSettingService.startPhone2PhonechedulerByDomain(domain);
		} catch (Exception e) {
			logger.error("JRH 程序重启时，启动定时 执行外转外功能 的定时器失败！", e);
		}
	}
	
}
