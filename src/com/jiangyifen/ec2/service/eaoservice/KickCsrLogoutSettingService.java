package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KickCsrLogoutSetting;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：定时强制坐席退出系统配置项
* 
* @author jrh
*
*/
public interface KickCsrLogoutSettingService extends FlipSupportService<KickCsrLogoutSetting>  {
	
	/**
	 * 根据主键ID获得定时强制坐席退出系统配置项
	 * @param id	主键ID 
	 * @return		定时强制坐席退出系统配置项，一条或null
	 */
	@Transactional
	public KickCsrLogoutSetting getKickCsrLogoutSettingById(Long id);
	
	/**
	 * 保存定时强制坐席退出系统配置项
	 * @param kickCsrLogoutSetting	定时强制坐席退出系统配置项
	 * 
	 */
	@Transactional
	public void saveKickCsrLogoutSetting(KickCsrLogoutSetting kickCsrLogoutSetting);
	
	/**
	 * 更新定时强制坐席退出系统配置项
	 * @param kickCsrLogoutSetting	定时强制坐席退出系统配置项
	 * 
	 */
	@Transactional
	public KickCsrLogoutSetting updateKickCsrLogoutSetting(KickCsrLogoutSetting kickCsrLogoutSetting);
	
	/**
	 * 删除定时强制坐席退出系统配置项
	 * @param kickCsrLogoutSetting	定时强制坐席退出系统配置项
	 * 
	 */
	@Transactional
	public void deleteKickCsrLogoutSetting(KickCsrLogoutSetting kickCsrLogoutSetting);

	/**
	 * 获取指定域下的配置项
	 * @return
	 */
	@Transactional
	public KickCsrLogoutSetting getByDomainId(Long domainId);
	

	/**
	 *  根据强制坐席退出配置项，给指定域，创建定时任务
	 * @param kickCsrLogoutSetting	配置信息
	 * @param domain	指定的域
	 * @return 
	 */
	@Transactional
	public Boolean createGlobalSettingScheduler(KickCsrLogoutSetting kickCsrLogoutSetting, Domain domain);
	
	/**
	 *  终止指定域的强制坐席退出的任务
	 * @param domain	指定的域
	 * @return boolean 是否成功
	 */
	@Transactional
	public Boolean stopGlobalSettingcheduler(Domain domain);
	
	/**
	 *  启动指定域的强制坐席退出的任务
	 * @param domain	指定的域
	 * @return boolean 是否成功
	 */
	@Transactional
	public Boolean startGlobalSettingchedulerByDomain(Domain domain);
	
}