package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.MigrateCustomerLog;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface MigrateCustomerLogService extends FlipSupportService<MigrateCustomerLog> {
	
	// enhanced method
	

	// common method 
	
	@Transactional
	public MigrateCustomerLog get(Object primaryKey);
	
	@Transactional
	public void save(MigrateCustomerLog migrateCustomerLog);

	@Transactional
	public MigrateCustomerLog update(MigrateCustomerLog migrateCustomerLog);

	@Transactional
	public void delete(MigrateCustomerLog migrateCustomerLog);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
