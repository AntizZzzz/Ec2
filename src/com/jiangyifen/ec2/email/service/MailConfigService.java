package com.jiangyifen.ec2.email.service;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.entity.User;

public interface MailConfigService {

	@Transactional
	public MailConfig get(Object primaryKey);
	
	@Transactional
	public void save(MailConfig mailConfig);
	
	@Transactional
	public void update(MailConfig mailConfig);
	
	@Transactional
	public void delete(MailConfig mailConfig);
	
	@Transactional
	public void deleteById(Object primaryKey);

	
	@Transactional
	public MailConfig getDefaultMailConfig(Long domainId);
	
	@Transactional
	public MailConfig getMailConfigByUser(User user);
		
}
