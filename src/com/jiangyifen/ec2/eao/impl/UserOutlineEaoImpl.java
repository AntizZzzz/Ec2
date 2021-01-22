package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserOutlineEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.UserOutline;

public class UserOutlineEaoImpl extends BaseEaoImpl implements UserOutlineEao {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<UserOutline> getAllByDomain(Domain domain) {
		return getEntityManager().createQuery("select uo from UserOutline as uo where uo.domain.id = " +domain.getId()+ " order by uo.user.empNo asc").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserOutline> getAllByOutline(SipConfig outline, Domain domain) {
		return getEntityManager().createQuery("select uo from UserOutline as uo where uo.sip.id = " +outline.getId()
				+ " and uo.domain.id = " +domain.getId()+ " order by uo.user.empNo asc").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserOutline getByUserId(Long userId, Long domainId) {
		List<UserOutline> list = getEntityManager().createQuery("select uo from UserOutline as uo where uo.user.id = " +userId ).getResultList();
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
}
