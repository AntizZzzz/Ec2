package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface QueueService  extends FlipSupportService<Queue> {
	// ========  enhanced method  ========//
	
	/**
	 * jrh
	 * 获取指定域中的所有队列  
	 * @param domain 		指定域
	 * @return List<Queue>
	 */
	@Transactional
	public List<Queue> getAllByDomain(Domain domain);
	
	/**
	 * jrh
	 * 获取指定域中的所有普通队列或自动外呼队列  ,chb 项目选择queue
	 * @param domain 		指定域
	 * @param isnotAutoDial	是否不是自动外呼队列
	 * @return List<Queue>
	 */
	@Transactional
	public List<Queue> getAllByDomain(Domain domain, boolean isnotAutoDial);

	/**
	 * jrh
	 * 获取指定域中的所有普通队列或自动外呼队列  ,chb 项目选择queue
	 * @param domainId 		指定域
	 * @param isnotAutoDial	是否不是自动外呼队列
	 * @return List<Queue>
	 */
	@Transactional
	public List<Queue> getAllByDomain(Long domainId, boolean isnotAutoDial);
	
	/**
	 * 检验指定域中是否已经存在 名字为 name 的队列
	 * @param name 		队列名称
	 * @param domain	队列所在域
	 * @return boolean  为true 表示已经存在
	 */
	@Transactional
	public boolean existByName(String name, Domain domain);
	
	/**
	 *  更新asterisk 的 sip.conf 文件
	 *  读取DB 中的sip_conf 表，并将其中的内容写入asterisk 的sip_conf 文件中去
	 * @return boolean 如果为true 表示 更新文件成功
	 */
	@Transactional
	public boolean updateAsteriskQueueFile(Domain domain);
	
	/**
	 * jrh 
	 * 在指定的 domain范围内获取其musiconhold 对应的对象为moh 的所有实体
	 * @param moh 		默认语音文件夹
	 * @param domain	指定的域
	 * @return
	 */
	@Transactional
	public List<Queue> getAllByMusicOnHold(MusicOnHold moh, Domain domain);

	/**
	 * jrh
	 * 	获取指定域的默认队列
	 * @param domainId	域的id号
	 * @return Queue	返回的默认队列
	 */
	@Transactional
	public Queue getDefaultQueueByDomain(Long domainId);

	/**
	 * jrh
	 * 获取指定域下，所有可用的队列（非自动外呼队列、没有被其他项目使用的队列）
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<Queue> getAllUseableSimpleQueueByDomain(Domain domain);
	
	// ========  common method  ========//
	
	@Transactional
	public Queue get(Object primaryKey);
	
	/**
	 * 保存新建的队列，并自动为其设置队列名
	 * @param queue
	 */
//	@Transactional  // 由于牵涉到同步问题，所以将其事务加到了 Eao 上
	public void save(Queue queue);

	/**
	 * 更新或保存新建的队列，如果是新建的，则自动为其设置队列名
	 * @param queue
	 * @return
	 */
//	@Transactional  // 由于牵涉到同步问题，所以将其事务加到了 Eao 上
	public Queue update(Queue queue);

	@Transactional
	public void delete(Queue queue);
	
	@Transactional
	public void deleteById(Object primaryKey);

	/**
	 * chb
	 * 通过队列名取得队列
	 * @param queueName
	 * @return
	 */
	@Transactional
	public Queue getQueueByQueueName(String queueName);

	/**
	 * jrh 根据JPQL 查询所有符合条件的Queue
	 * @param jpql
	 * @return
	 */
	@Transactional
	public List<Queue> getAllByJpql(String jpql);

	/**
	 * jrh
	 * 	在指定域下，根据队列的名称，检查是否与之关联的IVRAction 对象，如果有，则表示不能删除	
	 * 
	 * @param queueName	队列名称
	 * @param domainId	域编号
	 * @return boolean  能否删除
	 */
	@Transactional
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
	@Transactional
	public Queue getByName(String queueName, Long domainId);


}
