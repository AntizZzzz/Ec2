package com.jiangyifen.ec2.email.service;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.email.entity.MailHistory;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface MailHistoryService extends FlipSupportService<MailHistory> {

	// common method 
	@Transactional
	public MailHistory get(Object primaryKey);
	
	@Transactional
	public void save(MailHistory mailHistory);

	@Transactional
	public MailHistory update(MailHistory mailHistory);

	@Transactional
	public void delete(MailHistory mailHistory);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}





