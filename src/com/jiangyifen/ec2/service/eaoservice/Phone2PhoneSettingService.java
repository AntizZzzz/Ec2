package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;

/**
 * 外转外服务接口
 * @author jrh
 *
 */
public interface Phone2PhoneSettingService {

	// common method 
	
	@Transactional
	public Phone2PhoneSetting get(Object primaryKey);
	
	@Transactional
	public void save(Phone2PhoneSetting incoming2PhoneSetting);

	@Transactional
	public Phone2PhoneSetting update(Phone2PhoneSetting incoming2PhoneSetting);

	@Transactional
	public void delete(Phone2PhoneSetting incoming2PhoneSetting);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	
	// enhanced method
	
	/**
	 * jrh
	 * 	获取指定域下的全局外转外配置对象
	 * @param domainId
	 * @return
	 */
	@Transactional
	public Phone2PhoneSetting getGlobalSettingByDomain(Long domainId);
	
	/**
	 * jrh
	 * 	获取指定域下的所有话务员自定义的外转外配置对象
	 * @param domainId
	 * @return
	 */
	@Transactional
	public List<Phone2PhoneSetting> getAllCsrCustomSettings(Long domainId);
	
	/**
	 * jrh
	 * 	获取指定用户持有的外转外配置对象
	 * @param userId
	 * @return
	 */
	@Transactional
	public Phone2PhoneSetting getByUser(Long userId);

	/**
	 * jrh
	 *  检查当前外转外配置是否正在运行（已经开始手机呼手机）
	 *  配置运行，需要满足以下条件：
	 *    当前外转外配置是为开启状态，以及日期包含今天，并且启动转接的时刻是否 <= 当前时刻，终止时刻 >= 当前时刻
	 * @param p2pSetting	外转外配置对象
	 * @return boolean 		满足条件返回 true
	 */
	@Transactional
	public boolean confirmSettingIsRunning(Phone2PhoneSetting p2pSetting);

	/**
	 * jrh
	 *  获取每个域下的所有 isStartedRedirect = true 的外转外配置项
	 * @param domainId							指定的域的id号
	 * @return List<Phone2PhoneSetting> 		返回符合条件的外转外配置项
	 */
	@Transactional
	public List<Phone2PhoneSetting> getAllStartedSettingsByDomain(Long domainId);

	/**
	 * jrh 
	 *  根据外转外配置项，创建定时任务,并纳入ShareData 的管理
	 * @param p2pSetting	外转外配置
	 * @return Scheduler	定时任务
	 */
	@Transactional
	public Boolean createP2PScheduler(Phone2PhoneSetting p2pSetting);
	
	/**
	 * jrh 
	 *  根据外转外配置项，终止定时任务,并纳入ShareData 的管理
	 * @param p2pSetting	外转外配置
	 * @return Scheduler	定时任务
	 */
	@Transactional
	public Boolean stopP2PScheduler(Phone2PhoneSetting p2pSetting);

	@Transactional
	public Boolean startPhone2PhonechedulerByDomain(Domain domain);
	
}
