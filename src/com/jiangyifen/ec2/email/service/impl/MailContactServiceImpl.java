package com.jiangyifen.ec2.email.service.impl;

import java.util.List;

import com.jiangyifen.ec2.email.eao.MailContactEao;
import com.jiangyifen.ec2.email.entity.MailContact;
import com.jiangyifen.ec2.email.service.MailContactService;

public class MailContactServiceImpl implements MailContactService {

	private MailContactEao mailContactEao;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<MailContact> getMailContactsByUserId(Long userId){
		List result = mailContactEao.getMailContactsByUserId(userId);
		return result;
	}

	@Override
	public MailContact get(Object primaryKey) {
		return mailContactEao.get(MailContact.class, primaryKey);
	}

	@Override
	public void save(MailContact mailContact) {
		mailContactEao.save(mailContact);
	}

	@Override
	public MailContact update(MailContact mailContact) {
		return (MailContact) mailContactEao.update(mailContact);
	}

	@Override
	public void delete(MailContact mailContact) {
		mailContactEao.delete(mailContact);
	}

	@Override
	public void deleteById(Object primaryKey) {
		mailContactEao.delete(MailContact.class, primaryKey);
	}
	
	public MailContactEao getMailContactEao() {
		return mailContactEao;
	}

	public void setMailContactEao(MailContactEao mailContactEao) {
		this.mailContactEao = mailContactEao;
	}
	
}
