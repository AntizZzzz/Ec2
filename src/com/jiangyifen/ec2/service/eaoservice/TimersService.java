package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface TimersService extends FlipSupportService<Timers> {
	// enhanced method

	/**
	 * 根据日期时间值，获取响应时间大于当前时间的所有定时提醒对象
	 * @return List
	 */
	@Transactional
	public List<Timers> getTiemrsByCurrentTime();
	
	/**
	 * 根据要新增或更新的 定时器对象，判断其响应时间是否大于schedule的响应时间
	 * 	如果不大于，则新建 schedule 任务，并且将之前的任务取消
	 * @param isdelete	是否是删除定时器的操作
	 * @param timers	刚刚保存或更新的定时器对象
	 */
	@Transactional
	public void refreshSchedule(boolean isdelete, Timers timers);
	
	
	// common method 
	
	@Transactional
	public Timers get(Object primaryKey);
	
	@Transactional
	public void save(Timers timers);

	@Transactional
	public Timers update(Timers timers);

	@Transactional
	public void delete(Timers timers);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}