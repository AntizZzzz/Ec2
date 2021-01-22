package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.UserQueue;

public interface UserQueueService {
	// ========  enhanced method  ========//
	
	/**
	 * 根据用户名称 username 获取指定用户与队列的所有关联
	 * @param username 用户名称
	 * @return 
	 */
	@Transactional
	public List<UserQueue> getAllByUsername(String username);
	
	/**
	 * 根据队列名称 queueName 获取指定用户与队列的所有关联
	 * @param queueName 队列名称
	 * @return
	 */
	@Transactional
	public List<UserQueue> getAllByQueueName(String queueName, Domain domain);
	
	/**
	 * 根据队列名称 queueName 获取指定用户与队列的所有关联
	 * @param queueName 队列名称
	 * @return
	 */
	@Transactional
	public List<UserQueue> getAllByQueueName(String queueName, Long domainId);

	// ========  common method  ========//
	
	@Transactional
	public UserQueue get(Object primaryKey);
	
	@Transactional
	public void save(UserQueue userQueue);

	@Transactional
	public UserQueue update(UserQueue userQueue);

	@Transactional
	public void delete(UserQueue userQueue);
	
	@Transactional
	public void deleteById(Object primaryKey);

	/**
	 * chb 根据队列名，用户名和域Id移除一条制定的对应关系
	 * @param queueName
	 * @param userName
	 * @param id
	 */
	@Transactional
	public void removeRelation(String queueName, String userName, Long id);


}
