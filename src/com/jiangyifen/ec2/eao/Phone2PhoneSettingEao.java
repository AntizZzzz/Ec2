package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Phone2PhoneSetting;

/**
 * 外转外配置
 * @author jrh
 */
public interface Phone2PhoneSettingEao extends BaseEao {
	
	/**
	 * jrh
	 * 	获取指定域下的全局外转外配置对象
	 * @param domainId
	 * @return
	 */
	public Phone2PhoneSetting getGlobalSettingByDomain(Long domainId);

	/**
	 * jrh
	 * 	获取指定域下的所有话务员自定义的外转外配置对象
	 * @param domainId
	 * @return
	 */
	public List<Phone2PhoneSetting> getAllCsrCustomSettings(Long domainId);
	
	/**
	 * jrh
	 * 	获取指定用户持有的外转外配置对象
	 * @param userId
	 * @return
	 */
	public Phone2PhoneSetting getByUser(Long userId);

	/**
	 * jrh
	 *  获取每个域下的所有 isStartedRedirect = true 的外转外配置项
	 * @param domainId							指定的域的id号
	 * @return List<Phone2PhoneSetting> 		返回符合条件的外转外配置项
	 */
	public List<Phone2PhoneSetting> getAllStartedSettingsByDomain(Long domainId);
	
}
