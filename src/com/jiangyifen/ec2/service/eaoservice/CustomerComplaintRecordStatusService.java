package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerComplaintRecordStatus;

public interface CustomerComplaintRecordStatusService {

	@Transactional
	public CustomerComplaintRecordStatus get(Object primaryKey);
	
	@Transactional
	public void save(CustomerComplaintRecordStatus customerComplaintRecordStatus);

	@Transactional
	public void update(CustomerComplaintRecordStatus customerComplaintRecordStatus);

	public void delete(CustomerComplaintRecordStatus customerComplaintRecordStatus);
	
	public void deleteById(Object primaryKey);
}
