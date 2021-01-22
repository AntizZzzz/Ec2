package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.backgroundthread.GlobalSettingScheduler;
import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.eao.KickCsrLogoutSettingEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KickCsrLogoutSetting;
import com.jiangyifen.ec2.service.eaoservice.KickCsrLogoutSettingService;
/**
* Service实现类：定时强制坐席退出系统配置项
* 
* @author jrh
* 
*/
public class KickCsrLogoutSettingServiceImpl implements KickCsrLogoutSettingService {

	
	/** 需要注入的Eao */
	private KickCsrLogoutSettingEao kickCsrLogoutSettingEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<KickCsrLogoutSetting> loadPageEntities(int start, int length, String sql) {
		return kickCsrLogoutSettingEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return kickCsrLogoutSettingEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得定时强制坐席退出系统配置项
	@Override
	public KickCsrLogoutSetting getKickCsrLogoutSettingById(Long id){
		return kickCsrLogoutSettingEao.get(KickCsrLogoutSetting.class, id);
	}
	
	// 保存定时强制坐席退出系统配置项
	@Override
	public void saveKickCsrLogoutSetting(KickCsrLogoutSetting kickCsrLogoutSetting){
		kickCsrLogoutSettingEao.save(kickCsrLogoutSetting);
	}
	
	// 更新定时强制坐席退出系统配置项
	@Override
	public KickCsrLogoutSetting updateKickCsrLogoutSetting(KickCsrLogoutSetting kickCsrLogoutSetting){
		return (KickCsrLogoutSetting)kickCsrLogoutSettingEao.update(kickCsrLogoutSetting);
	}
	
	// 删除定时强制坐席退出系统配置项
	@Override
	public void deleteKickCsrLogoutSetting(KickCsrLogoutSetting kickCsrLogoutSetting){
		kickCsrLogoutSettingEao.delete(kickCsrLogoutSetting);
	}
	
	@Override
	public KickCsrLogoutSetting getByDomainId(Long domainId) {
		return kickCsrLogoutSettingEao.getByDomainId(domainId);
	}

	@Override
	public Boolean createGlobalSettingScheduler(KickCsrLogoutSetting kickCsrLogoutSetting, Domain domain) {
		StringBuffer startSb = new StringBuffer();
		startSb.append("0 ");
		startSb.append(kickCsrLogoutSetting.getLaunchMinute());
		startSb.append(" ");
		startSb.append(kickCsrLogoutSetting.getLaunchHour());
		startSb.append(" ? * ");

		String dayOfWeekType = kickCsrLogoutSetting.getDayOfWeekType();
		if("weekday".equals(dayOfWeekType)) {
			startSb.append(" 2-6");
		} else if("weekend".equals(dayOfWeekType)) {
			startSb.append(" 1,7");
		} else {
			for(DayOfWeek dayOfWeek : kickCsrLogoutSetting.getDaysOfWeek()) {
				startSb.append(dayOfWeek.getIndex());
				startSb.append(",");
			}
		}
		
		String cronSchedule = startSb.toString();
		if(cronSchedule.endsWith(",")) {
			cronSchedule = cronSchedule.substring(0, cronSchedule.length()-1);
		} 
		
		boolean success = GlobalSettingScheduler.getSingleton().addKickCsrTrigger(cronSchedule, domain);
		return success;
	}

	@Override
	public Boolean stopGlobalSettingcheduler(Domain domain) {
		boolean success = GlobalSettingScheduler.getSingleton().removeKickCsrTrigger(domain);
		return success;
	}
	
	@Override
	public Boolean startGlobalSettingchedulerByDomain(Domain domain) {
		KickCsrLogoutSetting kickCsrLogoutSetting = getByDomainId(domain.getId());
		if(kickCsrLogoutSetting == null || !kickCsrLogoutSetting.getIsLaunch()) {
			return true;
		}
		
		return createGlobalSettingScheduler(kickCsrLogoutSetting, domain);
	}

	//Eao注入
	public KickCsrLogoutSettingEao getKickCsrLogoutSettingEao() {
		return kickCsrLogoutSettingEao;
	}
	
	//Eao注入
	public void setKickCsrLogoutSettingEao(KickCsrLogoutSettingEao kickCsrLogoutSettingEao) {
		this.kickCsrLogoutSettingEao = kickCsrLogoutSettingEao;
	}
	
	
}