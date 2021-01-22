package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerComplaintRecord;

public interface CustomerComplaintRecordService{

	@Transactional
	public CustomerComplaintRecord get(Object primaryKey);
	
	@Transactional
	public void save(CustomerComplaintRecord customerResource);

	@Transactional
	public void update(CustomerComplaintRecord customerResource);

	public void delete(CustomerComplaintRecord customerResource);
	
	public void deleteById(Object primaryKey);
		
}
