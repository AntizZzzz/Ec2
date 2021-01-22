package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.backgroundthread.Phone2PhoneScheduler;
import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.eao.Phone2PhoneSettingEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;

public class Phone2PhoneSettingServiceImpl implements Phone2PhoneSettingService {
	
	private Phone2PhoneSettingEao phone2PhoneSettingEao;

	// common method
	
	@Override
	public Phone2PhoneSetting get(Object primaryKey) {
		return phone2PhoneSettingEao.get(Phone2PhoneSetting.class, primaryKey);
	}

	@Override
	public void save(Phone2PhoneSetting incoming2PhoneSetting) {
		phone2PhoneSettingEao.save(incoming2PhoneSetting);
	}

	@Override
	public Phone2PhoneSetting update(Phone2PhoneSetting incoming2PhoneSetting) {
		return (Phone2PhoneSetting) phone2PhoneSettingEao.update(incoming2PhoneSetting);
	}

	@Override
	public void delete(Phone2PhoneSetting incoming2PhoneSetting) {
		phone2PhoneSettingEao.delete(incoming2PhoneSetting);
	}

	@Override
	public void deleteById(Object primaryKey) {
		phone2PhoneSettingEao.delete(Phone2PhoneSetting.class, primaryKey);
	}

	
	// enhanced method
	
	@Override
	public Phone2PhoneSetting getGlobalSettingByDomain(Long domainId) {
		return phone2PhoneSettingEao.getGlobalSettingByDomain(domainId);
	}

	@Override
	public List<Phone2PhoneSetting> getAllCsrCustomSettings(Long domainId) {
		return phone2PhoneSettingEao.getAllCsrCustomSettings(domainId);
	}

	@Override
	public Phone2PhoneSetting getByUser(Long userId) {
		return phone2PhoneSettingEao.getByUser(userId);
	}
	
	// 检查当前外转外配置是否正在运行
	@Override
	public boolean confirmSettingIsRunning(Phone2PhoneSetting p2pSetting) {
		// 如果外转外配置是关闭的，直接返回false
		if(!p2pSetting.getIsStartedRedirect()) {
			return false;
		}
		
		// 获取今天是星期几，判断配置项中的启动日期是否包含几天在内，如果不包含今天，直接跳出循环
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int todayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		
		boolean isContain = checkExecuteDatesContainToday(p2pSetting, todayOfWeek);
		if(isContain == false) {
			return false;
		}
		
		// 如果配置外转外日期包含今天，则比较启动时刻是否 <= 当前时刻 , 并且终止时刻 >= 当前时刻
		String[] startHourMinute = p2pSetting.getStartTime().split(":");
		String[] stopHourMinute = p2pSetting.getStopTime().split(":");
		int startHour = Integer.parseInt(startHourMinute[0]);
		int startMinute = Integer.parseInt(startHourMinute[1]);
		int stopHour = Integer.parseInt(stopHourMinute[0]);
		int stopMinute = Integer.parseInt(stopHourMinute[1]);
		
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = calendar.get(Calendar.MINUTE);
		if(startHour > currentHour) {	// 启动的小时值 大于 当前小时，则肯定未启动
			return false;
		} else if(stopHour < currentHour) {	// 停止的小时值 小于 当前小时，则肯定未启动
			return false;
		} else if(startHour == currentHour && startMinute > currentMinute) {
			return false;
		} else if(stopHour == currentHour && stopMinute < currentMinute) {
			return false;
		}
		
		// 如果以上条件都满足，返回 true
		return true;
	}

	/**
	 * jrh 检查外转外配置的执行日期是否包含今天，如果包含则返回true
	 * @param p2pSetting	外转外配置项
	 * @param todayOfWeek	今天是一个星期中的第几天
	 * @return boolean
	 */
	private boolean checkExecuteDatesContainToday(Phone2PhoneSetting p2pSetting,
			int todayOfWeek) {
		String dayOfWeekType = p2pSetting.getDayOfWeekType();
		Set<DayOfWeek> dasOfWeek = p2pSetting.getDaysOfWeek();
		
		// 比对日期
		if("weekday".equals(dayOfWeekType)) {
			if(todayOfWeek == 1 || todayOfWeek == 7) {
				return false;
			} 
		} else if("weekend".equals(dayOfWeekType)) {
			if(todayOfWeek > 1 && todayOfWeek < 7) {
				return false;
			}
		} else {
			List<Integer> indexs = new ArrayList<Integer>();
			for(DayOfWeek dayOfWeek : dasOfWeek) {
				indexs.add(dayOfWeek.getIndex());
			}
			if(!indexs.contains(todayOfWeek)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Phone2PhoneSetting> getAllStartedSettingsByDomain(Long domainId) {
		return phone2PhoneSettingEao.getAllStartedSettingsByDomain(domainId);
	}
	
	@Override
	public Boolean createP2PScheduler(Phone2PhoneSetting p2pSetting) {
		// 获取今天是星期几，判断配置项中的启动日期是否包含几天在内，如果不包含今天，直接跳出循环
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		String[] startHourMinute = p2pSetting.getStartTime().split(":");
		int startHour = Integer.parseInt(startHourMinute[0]);
		int startMinute = Integer.parseInt(startHourMinute[1]);
		String[] stopHourMinute = p2pSetting.getStopTime().split(":");
		int stopHour = Integer.parseInt(stopHourMinute[0]);
		int stopMinute = Integer.parseInt(stopHourMinute[1]);
		
		// 拼接定时任务的启动时刻语句
		StringBuffer startSb = new StringBuffer();
		StringBuffer stopSb = new StringBuffer();
		startSb.append("0 ");
		stopSb.append("0 ");
		startSb.append(startMinute);
		stopSb.append(stopMinute);
		startSb.append(" ");
		stopSb.append(" ");
		startSb.append(startHour);
		stopSb.append(stopHour);
		startSb.append(" ? * ");
		stopSb.append(" ? * ");
		
		String dayOfWeekType = p2pSetting.getDayOfWeekType();
		if("weekday".equals(dayOfWeekType)) {
			startSb.append(" 2-6");
			stopSb.append(" 2-6");
		} else if("weekend".equals(dayOfWeekType)) {
			startSb.append(" 1,7");
			stopSb.append(" 1,7");
		} else {
			for(DayOfWeek dayOfWeek : p2pSetting.getDaysOfWeek()) {
				startSb.append(dayOfWeek.getIndex());
				stopSb.append(dayOfWeek.getIndex());
				startSb.append(",");
				stopSb.append(",");
			}
		}
		
		String startCron = startSb.toString();
		String stopCron = stopSb.toString();
		if(startCron.endsWith(",")) {
			startCron = startCron.substring(0, startCron.length()-1);
			stopCron = stopCron.substring(0, stopCron.length()-1);
		} 

		// 创建定时计数
		String[] cronSchedules = new String[]{startCron, stopCron};
		boolean success = Phone2PhoneScheduler.getSingleton().addTrigger(cronSchedules, p2pSetting);
		return success;
	}

	@Override
	public Boolean stopP2PScheduler(Phone2PhoneSetting p2pSetting) {
		boolean success = Phone2PhoneScheduler.getSingleton().removeTrigger(p2pSetting);
		return success;
	}
	
	@Override
	public Boolean startPhone2PhonechedulerByDomain(Domain domain) {
		Phone2PhoneSetting globalP2P = getGlobalSettingByDomain(domain.getId());
		if(globalP2P == null || !globalP2P.getIsStartedRedirect()) {	// 全局配置项不存在，或者没有开启，则直接退出
			return true;
		}

		createP2PScheduler(globalP2P);	// 启动全局定时器
		
		if(globalP2P.getIsLicensed2Csr()) {	// 如果已经授权让坐席自己设置定时器，则继续添加坐席自定义的定时器
			
			ArrayList<Long> specifiedUserIdLs = new ArrayList<Long>();
			if(!globalP2P.getIsSpecifiedPhones()) {	// 如果全局配置项是否启用的是智能呼叫，则不需要加载全局配置中指定坐席的自定义设置
				for(User csr : globalP2P.getSpecifiedCsrs()) {
					specifiedUserIdLs.add(csr.getId());
				}
			}
			
			List<Phone2PhoneSetting> csrsP2PLs = getAllCsrCustomSettings(domain.getId());
			for(Phone2PhoneSetting csrsP2P : csrsP2PLs) {
				if(!specifiedUserIdLs.contains(csrsP2P.getCreator().getId())) {
					createP2PScheduler(globalP2P);	// 启动个人自定义的定时器
				}
			}
		}
		
		return true;
	}
	
	// getter setter

	public Phone2PhoneSettingEao getPhone2PhoneSettingEao() {
		return phone2PhoneSettingEao;
	}
	
	public void setPhone2PhoneSettingEao(Phone2PhoneSettingEao phone2PhoneSettingEao) {
		this.phone2PhoneSettingEao = phone2PhoneSettingEao;
	}

}
