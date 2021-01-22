package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.UserQueue;

public interface UserQueueEao extends BaseEao {

	
	/**
	 * 根据用户名称 username 获取指定用户与队列的所有关联
	 * @param username 用户名称
	 * @return 
	 */
	public List<UserQueue> getAllByUsername(String username);

	
	/**
	 * 根据队列名称 queueName 获取指定用户与队列的所有关联
	 * @param queueName 队列名称
	 * @return
	 */
	public List<UserQueue> getAllByQueueName(String queueName, Long domainId);

	/**
	 * chb 根据队列名，用户名和域Id移除一条制定的对应关系
	 * @param queueName
	 * @param userName
	 * @param id
	 */
	public void removeRelation(String queueName, String userName, Long id);


}
