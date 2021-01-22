package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.CustomerServiceRecordEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;

public class CustomerServiceRecordServiceImpl implements CustomerServiceRecordService {

	private CustomerServiceRecordEao customerServiceRecordEao;

	// enhanced method
	
	@Override
	public CustomerServiceRecord get(CustomerResource customer, MarketingProject project, User user) {
		return customerServiceRecordEao.get(customer, project, user);
	}
	
	@Override
	public List<CustomerServiceRecord> getAllByCustomer(CustomerResource customer) {
		return customerServiceRecordEao.getAllByCustomer(customer);
	}
	
	// common method

	@Override
	public CustomerServiceRecord get(Object primaryKey) {
		return customerServiceRecordEao.get(CustomerServiceRecord.class, primaryKey);
	}

	@Override
	public void save(CustomerServiceRecord serviceRecord) {
		customerServiceRecordEao.save(serviceRecord);
	}

	@Override
	public CustomerServiceRecord update(CustomerServiceRecord serviceRecord) {
		return (CustomerServiceRecord) customerServiceRecordEao.update(serviceRecord);
	}

	@Override
	public void delete(CustomerServiceRecord serviceRecord) {
		customerServiceRecordEao.delete(serviceRecord);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerServiceRecordEao.delete(CustomerServiceRecord.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecord> loadPageEntities(int start, int length, String sql) {
		if("".equals(sql.trim())) {	// 代表不查询数据库
			return new ArrayList<CustomerServiceRecord>();
		}
		return customerServiceRecordEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		if("".equals(sql.trim())) {	// 代表不查数据库
			return 0;
		}
		return customerServiceRecordEao.getEntityCount(sql);
	}

	// setter getter
	
	public CustomerServiceRecordEao getCustomerServiceRecordEao() {
		return customerServiceRecordEao;
	}
	
	public void setCustomerServiceRecordEao(CustomerServiceRecordEao customerServiceRecordEao) {
		this.customerServiceRecordEao = customerServiceRecordEao;
	}

}
