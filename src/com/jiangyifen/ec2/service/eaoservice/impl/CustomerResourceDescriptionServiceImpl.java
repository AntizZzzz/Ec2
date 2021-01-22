package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.CustomerResourceDescriptionEao;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceDescriptionService;

public class CustomerResourceDescriptionServiceImpl implements
		CustomerResourceDescriptionService {

	private CustomerResourceDescriptionEao customerResourceDescriptionEao;

	// enhance function method

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResourceDescription> loadPageEntities(int start, int length, String sql) {
		if ("".equals(sql.trim())) {
			return new ArrayList<CustomerResourceDescription>();
		}
		return customerResourceDescriptionEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		if ("".equals(sql.trim())) {
			return 0;
		}
		return customerResourceDescriptionEao.getEntityCount(sql);
	}
	
	// common method
	@Override
	public CustomerResourceDescription get(Object primaryKey) {
		return customerResourceDescriptionEao.get(
				CustomerResourceDescription.class, primaryKey);
	}

	@Override
	public void save(CustomerResourceDescription customerResourceDescription) {
		if(customerResourceDescription != null) {
			customerResourceDescription.setLastUpdateDate(new Date());
			customerResourceDescription.setCreateDate(new Date());
		}
		customerResourceDescriptionEao.save(customerResourceDescription);
	}

	@Override
	public void update(CustomerResourceDescription customerResourceDescription) {
		if(customerResourceDescription != null) {
			customerResourceDescription.setLastUpdateDate(new Date());
		}
		customerResourceDescriptionEao.update(customerResourceDescription);
	}

	@Override
	public void delete(CustomerResourceDescription customerResourceDescription) {
		customerResourceDescriptionEao.delete(customerResourceDescription);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerResourceDescriptionEao.delete(
				CustomerResourceDescription.class, primaryKey);
	}

	// getter and setter
	public CustomerResourceDescriptionEao getCustomerResourceDescriptionEao() {
		return customerResourceDescriptionEao;
	}

	public void setCustomerResourceDescriptionEao(
			CustomerResourceDescriptionEao customerResourceDescriptionEao) {
		this.customerResourceDescriptionEao = customerResourceDescriptionEao;
	}

	/**
	 * chb 取得同一域内所有不同的Key值，以方便整理 DistinctKeys
	 */
	@Override
	public List<String> getDistinctKeys(Domain domain) {
		return customerResourceDescriptionEao.getDistinctKeys(domain);
	}

	/**
	 * chb
	 * 
	 * @param toUpdateList
	 *            待更新的一组Key值
	 * @param updateTo
	 *            更新Key值为updateTo
	 * @param domain
	 *            域
	 */
	@Override
	public void updateKey(List<String> toUpdateList, String updateTo,
			Domain domain) {
		customerResourceDescriptionEao.updateKey(toUpdateList, updateTo, domain);
	}

	/** jrh 	获取某个资源的所有描述信息 <br/> @param customerId	资源编号 <br/> @return */
	@Override
	public List<CustomerResourceDescription> getAllByCustomerId(Long customerId) {
		return customerResourceDescriptionEao.getAllByCustomerId(customerId);
	}

}
