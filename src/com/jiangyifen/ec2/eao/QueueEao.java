package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;

public interface QueueEao extends BaseEao {
	
	/**
	 * 重写 BaseEao 中的保存实体方法
	 * @param entity	 需要保存的实体
	 */
	@Transactional
	public void save(Queue queue);
	
	/**
	 * 重写 BaseEao 中的更新实体方法
	 * @param entity	 需要更新的实体
	 */
	@Transactional
	public Queue update(Queue queue);
	
	/**
	 * jrh
	 * 获取指定域中的所有队列  
	 * @param domain 		指定域
	 * @return List<Queue>
	 */
	public List<Queue> getAllByDomain(Domain domai);
	
	/**
	 * jrh
	 * 获取指定域中的所有普通队列或自动外呼队列  ,chb 项目选择queue
	 * @param domainId 		指定域
	 * @param isnotAutoDial	是否不是自动外呼队列
	 * @return List<Queue>
	 */
	public List<Queue> getAllByDomain(Long domainId, boolean isnotAutoDial);

	/**
	 * 检验指定域中是否已经存在 名字为 name 的队列
	 * @param name 		队列名称
	 * @param domain	队列所在域
	 * @return boolean  为true 表示已经存在
	 */
//	public boolean existByName(String name);
	public boolean existByName(String name, Domain domain);

		
	/**
	 * jrh 
	 * 在指定的 domain范围内获取其musiconhold 对应的对象为moh 的所有实体
	 * @param moh 		默认语音文件夹
	 * @param domain	指定的域
	 * @return
	 */
	public List<Queue> getAllByMusicOnHold(MusicOnHold moh, Domain domain);

	/**
	 * jrh
	 * 	获取指定域的默认队列
	 * @param domainId	域的id号
	 * @return Queue	返回的默认队列
	 */
	public Queue getDefaultQueueByDomain(Long domainId);
	
	/**
	 * jrh
	 *	 获取系统中当前最大的队列名称，并将String 转化成Long 类型返回，不包括外线
	 * @return Long	返回的系统中最大的队列名（为数字组成）
	 */
	public Long getMaxQueueName();

	/**
	 * jrh
	 * 获取指定域下，所有可用的队列（非自动外呼队列、没有被其他项目使用的队列）
	 * @param domain
	 * @return
	 */
	public List<Queue> getAllUseableSimpleQueueByDomain(Domain domain);
	
	/**
	 * chb
	 * 通过队列名取得队列
	 * @param queueName
	 * @return
	 */
	public Queue getQueueByQueueName(String queueName);

	/**
	 * jrh
	 * 	在指定域下，根据队列的名称，检查是否与之关联的IVRAction 对象，如果有，则表示不能删除	
	 * 
	 * @param queueName	队列名称
	 * @param domainId	域编号
	 * @return boolean  能否删除
	 */
	public boolean checkDeleteAbleByIvrAction(String queueName, Long domainId);

	/**
	 * @Description 描述：根据队列名称和指定的租户所属域的编号，获取队列
	 *
	 * @author  JRH
	 * @date    2014年8月11日 下午3:53:14
	 * @param queueName		队列名称
	 * @param domainId		租户所属域的编号
	 * @return Queue		返回的队列对象
	 */
	public Queue getByName(String queueName, Long domainId);
	
}
