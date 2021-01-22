package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.DomainEao;
import com.jiangyifen.ec2.entity.Domain;

public class DomainEaoImpl extends BaseEaoImpl implements DomainEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Domain> getAll() {
		String sql="select d from Domain as d order by d.id asc";
		return getEntityManager().createQuery(sql).getResultList();
	}
}
