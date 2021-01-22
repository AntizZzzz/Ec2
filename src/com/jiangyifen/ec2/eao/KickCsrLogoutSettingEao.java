package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.KickCsrLogoutSetting;

/**
* Eao接口：定时强制坐席退出系统配置项
* 
* @author jrh
*
*/
public interface KickCsrLogoutSettingEao extends BaseEao {
	
	
	/**
	 * 获得定时强制坐席退出系统配置项列表
	 * @param 	jpql jpql语句
	 * @return 	定时强制坐席退出系统配置项列表
	 */
	public List<KickCsrLogoutSetting> loadKickCsrLogoutSettingList(String jpql);

	/**
	 * 获取指定域下的配置项
	 * @return
	 */
	public KickCsrLogoutSetting getByDomainId(Long domainId);
	
}