package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.service.common.FlipSupportNativeSqlService;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface CustomerResourceService extends FlipSupportNativeSqlService<CustomerResource>, FlipSupportService<CustomerResource> {

	@Transactional
	public CustomerResource get(Object primaryKey);
	
	@Transactional
	public void save(CustomerResource customerResource);

	@Transactional
	public CustomerResource update(CustomerResource customerResource);

	@Transactional
	public void delete(CustomerResource customerResource);
	
	@Transactional
	public void deleteById(Object primaryKey);

	/**
	 * chb
	 * 根据批次查找批次对应的资源
	 * @param batch
	 * @return
	 */
	public List<CustomerResource> getCustomerResourceByBatch(
			CustomerResourceBatch batch);
	
	/**
	 * jrh
	 * 根据电话号码和域取得CustomerResource
	 * @param customerPhoneNumber
	 * @param id
	 */
	public CustomerResource getCustomerResourceByPhoneNumber(String customerPhoneNumber, Long domainId);

	/**
	 * jrh 
	 * 	根据搜索语句获取相应的所有客户对象
	 * @param searchSql
	 * @return
	 */
	@Transactional
	public List<CustomerResource> getAllBySql(String searchSql);
	
}
