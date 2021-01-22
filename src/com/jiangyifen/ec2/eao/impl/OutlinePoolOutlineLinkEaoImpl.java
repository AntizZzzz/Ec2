package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.OutlinePoolOutlineLinkEao;
import com.jiangyifen.ec2.entity.OutlinePoolOutlineLink;

public class OutlinePoolOutlineLinkEaoImpl extends BaseEaoImpl implements OutlinePoolOutlineLinkEao {

	@Override
	public void save(OutlinePoolOutlineLink outlinePoolOutlineLink) {
		getEntityManager().persist(outlinePoolOutlineLink);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OutlinePoolOutlineLink> getAllByDomain(Long poolId) {
		return getEntityManager().createQuery(
				"select q from OutlinePoolOutlineLink as q where q.poolId = " + poolId + " order by q.poolId desc")
				.getResultList();
	}

	@Override
	public void delete(Long poolId) {
		String deleteLinkSql = "delete from ec2_outline_pool_outline_link where poolid = " + poolId;
		this.getEntityManager().createNativeQuery(deleteLinkSql).executeUpdate();
	}

}
