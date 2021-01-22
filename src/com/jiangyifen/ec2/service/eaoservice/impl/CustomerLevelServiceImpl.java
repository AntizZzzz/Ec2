package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerLevelEao;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;

public class CustomerLevelServiceImpl implements CustomerLevelService {
	
	private CustomerLevelEao customerLevelEao;
	
	// common method
	@Override
	public CustomerLevel get(Object primaryKey) {
		return customerLevelEao.get(CustomerLevel.class, primaryKey);
	}

	@Override
	public void save(CustomerLevel customerLevel) {
		customerLevelEao.save(customerLevel);
	}

	@Override
	public void update(CustomerLevel customerLevel) {
		customerLevelEao.update(customerLevel);
	}

	@Override
	public void delete(CustomerLevel customerLevel) {
		customerLevelEao.delete(customerLevel);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerLevelEao.delete(CustomerLevel.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerLevel> loadPageEntitys(int start, int length, String sql) {
		return customerLevelEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return customerLevelEao.getEntityCount(sql);
	}

	/**
	 * chb 
	 * 取得所有的等级
	 * @param domain
	 * @return
	 */
	@Override
	public List<CustomerLevel> getAll(Domain domain) {
		return customerLevelEao.getAll(domain);
	}
	
	@Override
	public List<CustomerLevel> getAllSuperiorLevel(CustomerLevel level, Domain domain) {
		return customerLevelEao.getAllSuperiorLevel(level, domain);
	}

	// setter getter
	
	public CustomerLevelEao getCustomerLevelEao() {
		return customerLevelEao;
	}
	
	public void setCustomerLevelEao(CustomerLevelEao customerLevelEao) {
		this.customerLevelEao = customerLevelEao;
	}
}
