package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;


public interface CustomerResourceEao extends BaseEao {
	/**
	 * chb
	 * 根据批次查找批次对应的资源
	 * @param batch
	 * @return
	 */
	public List<CustomerResource> getCustomerResourceByBatch(
			CustomerResourceBatch batch);
	
	
	/**
	 * chb
	 * 根据电话号码查找对应的资源的资源
	 * @param batch
	 * @return
	 */
	public CustomerResource getCustomerResourceByPhoneNumber(
			String phoneNumber, Long domainId);

	/**
	 * chb
	 * 按照步长查找List
	 * @param nativeSql
	 * @param step
	 * @return
	 */
	public List<Long> loadStepRows(String nativeSql, int step);

	/**
	 * chb
	 * 通过Id来验证是否是客户,从ProjectCustomer中间表里验证
	 * @param id
	 * @return
	 */
	public Boolean isCustomerById(Long id, Domain domain);

	/**
	 * jrh 
	 * 	根据搜索语句获取相应的所有客户对象
	 * @param searchSql
	 * @return
	 */
	public List<CustomerResource> getAllBySql(String searchSql);

}
