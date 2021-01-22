package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.PauseReasonEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.PauseReason;

public class PauseReasonEaoImpl extends BaseEaoImpl implements PauseReasonEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<PauseReason> getAllByEnabled(Domain domain, boolean enabled) {
		return getEntityManager().createQuery("select r from PauseReason as r where r.domain.id = " + domain.getId() + " and r.enabled = "+enabled+" order by r.id").getResultList();
	}

}
