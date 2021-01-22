package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.KickCsrLogoutSettingEao;
import com.jiangyifen.ec2.entity.KickCsrLogoutSetting;

/**
* Eao实现类：定时强制坐席退出系统配置项
* 
* 注入：BaseEaoImpl
* 
* @author jrh
*
*/
public class KickCsrLogoutSettingEaoImpl extends BaseEaoImpl implements KickCsrLogoutSettingEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得定时强制坐席退出系统配置项列表
	public List<KickCsrLogoutSetting> loadKickCsrLogoutSettingList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public KickCsrLogoutSetting getByDomainId(Long domainId) {
		String jpql = "select e from KickCsrLogoutSetting as e where e.domainId = "+domainId;
		List<KickCsrLogoutSetting> settings = this.getEntityManager().createQuery(jpql).getResultList();
		if(settings.size() > 0) {
			return settings.get(0);
		}
		return null;
	}
	
}