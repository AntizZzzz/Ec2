package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.StaticQueueMember;

public interface StaticQueueMemberEao extends BaseEao {

	/**
	 * 获取所有的分机与队列的对应关系
	 * @return 
	 */
	public List<StaticQueueMember> getAll();
	
	/**
	 * 根据域domain和分机interface名称 获取指定域中指定分机与队列的所有对应关系对象
	 * @param domain  指定域
	 * @param sipname 分机名称 如 : SIP/8500
	 * @return
	 */
	public List<StaticQueueMember> getAllBySipname(Domain domain, String sipname);
	
	/**
	 * 获取指定域中指定队列名的所有分机和队列的对应关系
	 * @param domain  	域
	 * @param queueName	队列名
	 * @return
	 */
	public List<StaticQueueMember> getAllByQueueName(Long domainId, String queueName);

}
