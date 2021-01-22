package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.Phone2PhoneSettingEao;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;

public class Phone2PhoneSettingEaoImpl extends BaseEaoImpl implements Phone2PhoneSettingEao {

	@SuppressWarnings("unchecked")
	@Override
	public Phone2PhoneSetting getGlobalSettingByDomain(Long domainId) {
		List<Phone2PhoneSetting> p2pSettings = getEntityManager().createQuery("select p from Phone2PhoneSetting as p where p.domain.id = " +domainId+ " and p.isGlobalSetting = true").getResultList();
		if(p2pSettings.size() > 0) {
			return p2pSettings.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Phone2PhoneSetting> getAllCsrCustomSettings(Long domainId) {
		List<Phone2PhoneSetting> p2pSettings = getEntityManager().createQuery("select p from Phone2PhoneSetting as p where p.domain.id = " +domainId+ " and p.isGlobalSetting = false").getResultList();
		return p2pSettings;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Phone2PhoneSetting getByUser(Long userId) {
		// 之所以要加上 isGlobalSetting = false 判断，是因为一个用户可能拥有多个角色，而全局配置的创建者可能持有CSR 角色
		List<Phone2PhoneSetting> p2pSettings = getEntityManager().createQuery("select p from Phone2PhoneSetting as p where p.creator.id = " +userId+ " and p.isGlobalSetting = false").getResultList();
		if(p2pSettings.size() > 0) {
			return p2pSettings.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Phone2PhoneSetting> getAllStartedSettingsByDomain(Long domainId) {
		List<Phone2PhoneSetting> p2pSettings = getEntityManager().createQuery("select p from Phone2PhoneSetting as p where p.domain.id = " +domainId+ " and p.isStartedRedirect = true").getResultList();
		return p2pSettings;
	}

}
