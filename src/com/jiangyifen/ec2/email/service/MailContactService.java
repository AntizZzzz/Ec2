package com.jiangyifen.ec2.email.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.email.entity.MailContact;

public interface MailContactService {
	
	@Transactional
	public List<MailContact> getMailContactsByUserId(Long userId);
	
	@Transactional
	public MailContact get(Object primaryKey);
	
	@Transactional
	public void save(MailContact mailContact);
	
	@Transactional
	public MailContact update(MailContact mailContact);
	
	@Transactional
	public void delete(MailContact mailContact);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
