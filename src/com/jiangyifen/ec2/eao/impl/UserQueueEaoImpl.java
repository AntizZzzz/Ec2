package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserQueueEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.UserQueue;

public class UserQueueEaoImpl extends BaseEaoImpl implements UserQueueEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<UserQueue> getAllByUsername(String username) {
		return getEntityManager().createQuery("select uq from UserQueue as uq where uq.username = '" + username + "' order by uq.queueName asc").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserQueue> getAllByQueueName(String queueName, Long domainId) {
		return getEntityManager().createQuery("select uq from UserQueue as uq where uq.queueName = '" + queueName + "' and uq.domain.id = " + domainId + " order by uq.username asc").getResultList();
	}

	/**
	 * chb 根据队列名，用户名和域Id移除一条制定的对应关系
	 * @param queueName
	 * @param username
	 * @param id
	 */
	@Override
	public void removeRelation(String queueName, String username, Long domainId) {
		String sql="delete from UserQueue as uq where uq.username='"+username+"' and uq.queueName='"+queueName+"' and uq.domain.id="+domainId;
		getEntityManager().createQuery(sql).executeUpdate();
	}

}
