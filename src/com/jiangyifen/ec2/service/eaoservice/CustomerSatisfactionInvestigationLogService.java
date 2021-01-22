package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerSatisfactionInvestigationLog;


public interface CustomerSatisfactionInvestigationLogService{
	@Transactional
	public void save(CustomerSatisfactionInvestigationLog log);

}
