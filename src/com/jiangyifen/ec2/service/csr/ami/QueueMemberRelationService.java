package com.jiangyifen.ec2.service.csr.ami;



public interface QueueMemberRelationService {

	/**
	 * jrh
	 * 	添加队列成员
	 * @param queueName	队列名称
	 * @param exten		要从队列中移除的分机
	 * @param priority	分机的优先级
	 */
	public void addQueueMemberRelation(String queueName, String exten, Integer priority);
	
	/**
	 * jrh
	 * 	移除队列成员
	 * @param queueName	队列名称
	 * @param exten		要从队列中移除的分机
	 */
	public void removeQueueMemberRelation(String queueName, String exten);

}
