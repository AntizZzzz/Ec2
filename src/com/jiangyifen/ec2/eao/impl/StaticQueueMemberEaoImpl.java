package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.StaticQueueMemberEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.StaticQueueMember;

public class StaticQueueMemberEaoImpl extends BaseEaoImpl implements StaticQueueMemberEao {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<StaticQueueMember> getAll() {
		return getEntityManager().createQuery("select sqm from StaticQueueMember as sqm").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<StaticQueueMember> getAllBySipname(Domain domain, String sipname) {
		return getEntityManager().createQuery("select sqm from StaticQueueMember as sqm where sqm.sipname = '" + sipname + "' and sqm.domain.id = " + domain.getId()).getResultList();
	}

	/**
	 * 获取指定域中指定队列名的所有分机和队列的对应关系
	 * @param domain  	域
	 * @param queueName	队列名
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<StaticQueueMember> getAllByQueueName(Long domainId, String queueName) {
		return getEntityManager().createQuery("select sqm from StaticQueueMember as sqm where sqm.queueName = '" + queueName + "' and sqm.domain.id = " + domainId).getResultList();
	}
}
