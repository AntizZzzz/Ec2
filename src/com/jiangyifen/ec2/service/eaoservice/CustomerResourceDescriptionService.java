package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface CustomerResourceDescriptionService extends FlipSupportService<CustomerResourceDescription> {
	
	@Transactional
	public CustomerResourceDescription get(Object primaryKey);
	
	@Transactional
	public void save(CustomerResourceDescription customerResourceDescription);

	@Transactional
	public void update(CustomerResourceDescription customerResourceDescription);

	@Transactional
	public void delete(CustomerResourceDescription customerResourceDescription);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	/**
	 * chb
	 * 取得同一域内所有不同的Key值，以方便整理
	 * DistinctKeys
	 */
	public List<String> getDistinctKeys(Domain domain);
	
	/**
	 * chb
	 * @param toUpdateList 待更新的一组Key值
	 * @param updateTo 更新Key值为updateTo
	 * @param domain 域
	 */
	@Transactional
	public void updateKey(List<String> toUpdateList,String updateTo,Domain domain);

	/**
	 * jrh
	 * 	获取某个资源的所有描述信息
	 * @param customerId	资源编号
	 * @return
	 */
	@Transactional
	public List<CustomerResourceDescription> getAllByCustomerId(Long customerId);
	
}
