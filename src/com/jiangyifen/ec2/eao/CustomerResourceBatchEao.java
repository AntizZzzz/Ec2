package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;


public interface CustomerResourceBatchEao extends BaseEao {
	/**
	 * chb
	 * 根据域取出所有的批次
	 * @param domain
	 * @return
	 */
	List<CustomerResourceBatch> getAllBatches(Domain domain);

	/**
	 * jrh
	 * 	根据客户资源的Id号和批次Id号， 检查资源是否已经在指定的批次当中 
	 * @param resourceId 	客户资源的Id号
	 * @param batchId		批次的Id号
	 * @return
	 */
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
	List<CustomerResourceBatch> getByName(String batchName, Domain domain);
	
}
