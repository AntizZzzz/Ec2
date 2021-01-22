package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserQueueEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;

public class UserQueueServiceImpl implements UserQueueService {
	
	private UserQueueEao userQueueEao;
	
	// ========  enhanced method  ========//
	
	@Override
	public List<UserQueue> getAllByUsername(String username) {
		return userQueueEao.getAllByUsername(username);
	}
	
	@Override
	public List<UserQueue> getAllByQueueName(String queueName, Domain domain) {
		return userQueueEao.getAllByQueueName(queueName, domain.getId());
	}
	
	@Override
	public List<UserQueue> getAllByQueueName(String queueName, Long domainId) {
		return userQueueEao.getAllByQueueName(queueName, domainId);
	}

	//========  common method ========//
	
	@Override
	public UserQueue get(Object primaryKey) {
		return userQueueEao.get(UserQueue.class, primaryKey);
	}

	@Override
	public void save(UserQueue userQueue) {
		userQueueEao.save(userQueue);
	}

	@Override
	public UserQueue update(UserQueue userQueue) {
		return (UserQueue) userQueueEao.update(userQueue);
	}

	@Override
	public void delete(UserQueue userQueue) {
		userQueueEao.delete(userQueue);
	}

	@Override
	public void deleteById(Object primaryKey) {
		userQueueEao.delete(UserQueue.class, primaryKey);
	}

	//==========  getter setter  ==========//
	
	public UserQueueEao getUserQueueEao() {
		return userQueueEao;
	}

	public void setUserQueueEao(UserQueueEao userQueueEao) {
		this.userQueueEao = userQueueEao;
	}

	/**
	 * chb 根据队列名，用户名和域Id移除一条制定的对应关系
	 * @param queueName
	 * @param userName
	 * @param id
	 */
	@Override
	public void removeRelation(String queueName, String userName, Long id) {
		userQueueEao.removeRelation(queueName,userName,id);
	}

}
