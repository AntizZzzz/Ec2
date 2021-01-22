package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface CustomerServiceRecordService extends FlipSupportService<CustomerServiceRecord> {
	
	// enhanced method 
	
	/**
	 *  jrh
	 *  根据指定用户和指定项目获取对应域中对应用户的最新外呼记录
	 */
	@Transactional
	public CustomerServiceRecord get(CustomerResource customer, MarketingProject project, User user);
	
	/**
	 *  jrh
	 *  根据指定客户对象，获取针对该客户的所有外呼记录
	 */
	@Transactional
	public List<CustomerServiceRecord> getAllByCustomer(CustomerResource customer);
	
	// common method 
	
	@Transactional
	public CustomerServiceRecord get(Object primaryKey);
	
	@Transactional
	public void save(CustomerServiceRecord serviceRecord);

	@Transactional
	public CustomerServiceRecord update(CustomerServiceRecord serviceRecord);

	@Transactional
	public void delete(CustomerServiceRecord serviceRecord);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
