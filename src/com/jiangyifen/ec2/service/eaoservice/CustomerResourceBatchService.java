package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.common.FlipSupportService;

@SuppressWarnings("rawtypes")
public interface CustomerResourceBatchService  extends FlipSupportService{
	@Transactional
	public CustomerResourceBatch get(Object primaryKey);
	
	@Transactional
	public void save(CustomerResourceBatch customerResourceBatch);

	@Transactional
	public CustomerResourceBatch update(CustomerResourceBatch customerResourceBatch);
	
	@Transactional
	public void delete(CustomerResourceBatch customerResourceBatch);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	/**
	 * chb
	 * 根据域取出所有的批次
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<CustomerResourceBatch> getAllBatches(Domain domain);

	/**
	 * jrh
	 * 	根据客户资源的Id号和批次Id号， 检查资源是否已经在指定的批次当中 
	 * @param resourceId 	客户资源的Id号
	 * @param batchId		批次的Id号
	 * @return
	 */
	@Transactional
	public boolean checkResourceExistedInBatch(Long resourceId, Long batchId);

	/**
	 * @Description 描述： 根据批次名称到指定域下获取批次集合
	 *
	 * @author  JRH
	 * @date    2014年12月9日 下午12:09:47
	 * @param batchName	批次名称
	 * @param domain	租户
	 * @return List<CustomerResourceBatch>
	 */
	@Transactional
	public List<CustomerResourceBatch> getByName(String batchName, Domain domain);
	
	/**
	 * @Description 描述： 根据批次名称到指定域下获取批次集合
	 *
	 * @author  jinht
	 * @date    2015年6月29日 17:36:21
	 * @param batchName	批次名称
	 * @param domain	租户
	 * @return List<CustomerResourceBatch>
	 */
	@Transactional
	public CustomerResourceBatch getBatchByBatchName(String batchName, Domain domain);

}
