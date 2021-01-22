package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.StaticQueueMember;

public interface StaticQueueMemberService {
	// ========  enhanced method  ========//

	/**
	 * 获取在指定域中的所有的分机与队列的对应关系
	 * @param domain  指定域
	 * @return 
	 */
	@Transactional
	public List<StaticQueueMember> getAllByDomain(Domain domain);

	/**
	 * 根据域domain和分机interface名称 获取指定域中指定分机与队列的所有对应关系对象
	 * @param domain  指定域
	 * @param sipname 分机名称 如 : SIP/8500
	 * @return
	 */
	@Transactional
	public List<StaticQueueMember> getAllBySipname(Domain domain, String sipname);

	/**
	 * 获取指定域中指定队列名的所有分机和队列的对应关系
	 * @param domain  	域
	 * @param queueName	队列名
	 * @return
	 */
	@Transactional
	public List<StaticQueueMember> getAllByQueueName(Domain domain, String queueName);

	/**
	 * 获取指定域中指定队列名的所有分机和队列的对应关系
	 * @param domain  	域
	 * @param queueName	队列名
	 * @return
	 */
	@Transactional
	public List<StaticQueueMember> getAllByQueueName(Long domainId, String queueName);
	
	// ========  common method  ========//
	
	@Transactional
	public StaticQueueMember get(Object primaryKey);
	
	@Transactional
	public void save(StaticQueueMember queueMember);

	@Transactional
	public StaticQueueMember update(StaticQueueMember queueMember);

	@Transactional
	public void delete(StaticQueueMember queueMember);
	
	@Transactional
	public void deleteById(Object primaryKey);


	
}
