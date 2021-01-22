package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.backgroundthread.TimerSchedule;
import com.jiangyifen.ec2.eao.TimersEao;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

public class TimersServiceImpl implements TimersService {
	private TimersEao timersEao;
//	private TimerSchedule timerSchedule;		// 定时器任务对象， 是一个 TimerTask

	// enhanced methods
	@SuppressWarnings("unchecked")
	@Override
	public List<Timers> loadPageEntities(int start, int length, String sql) {
		return timersEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return timersEao.getEntityCount(sql);
	}

	public List<Timers> getTiemrsByCurrentTime() {
		return timersEao.getTiemrsToCurrentTime();
	}
	
	// common methods
	@Override
	public Timers get(Object primaryKey) {
		return timersEao.get(Timers.class, primaryKey);
	}

	@Override
	public void save(Timers timers) {
		timersEao.save(timers);
	}

	@Override
	public Timers update(Timers timers) {
		return (Timers) timersEao.update(timers);
	}

	@Override
	public void refreshSchedule(boolean isdelete, Timers timers) {
		Date nearestResponseTime = timersEao.getNearestTimeToCurrentTime();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE)-5);
		Date verifyDate = cal.getTime();
		
		if(isdelete) {	// 如果删除的定时器的时间比即将响应的定时器的时间小，并且比当前时间的5分钟前的时刻大，那么重新设置schedule 任务
			if(nearestResponseTime != null && nearestResponseTime.after(timers.getResponseTime()) && verifyDate.before(timers.getResponseTime())) {
				TimerSchedule timerSchedule=SpringContextHolder.getBean("timerSchedule");
				timerSchedule.refreshSchedule();
			}
		} else {
			// 如果最近的响应时间大于当前要保存的定时器响应时间，或者不存在比当前响应时间大的定时器，那么就重新创建 schedule 任务
			// nearestResponseTime.compareTo(timers.getResponseTime()) >= 0) 其中“=0” 等于0 的情况出现在，当前出来刚加入的定时器以外，没有其他的比当前时间大的定时器
			if(nearestResponseTime == null || (nearestResponseTime != null 
					&& nearestResponseTime.compareTo(timers.getResponseTime()) >= 0) ) {
				TimerSchedule timerSchedule=SpringContextHolder.getBean("timerSchedule");
				timerSchedule.refreshSchedule();
			}
		}
	}
	
	@Override
	public void delete(Timers timers) {
		timersEao.delete(timers);
	}

	@Override
	public void deleteById(Object primaryKey) {
		timersEao.delete(Timers.class, primaryKey);
	}

	// Getter Setter

	public TimersEao getTimersEao() {
		return timersEao;
	}

	public void setTimersEao(TimersEao timersEao) {
		this.timersEao = timersEao;
	}

}
