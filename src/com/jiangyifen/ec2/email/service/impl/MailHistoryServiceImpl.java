package com.jiangyifen.ec2.email.service.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.email.eao.MailHistoryEao;
import com.jiangyifen.ec2.email.entity.MailHistory;
import com.jiangyifen.ec2.email.service.MailHistoryService;

public class MailHistoryServiceImpl implements MailHistoryService{
	
	private MailHistoryEao mailHistoryEao;

	@SuppressWarnings("unchecked")
	@Override
	public List<MailHistory> loadPageEntities(int start, int length, String sql) {
		return mailHistoryEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return mailHistoryEao.getEntityCount(sql);
	}

	@Override
	@Transactional
	public MailHistory get(Object primaryKey) {
		return mailHistoryEao.get(MailHistory.class, primaryKey);
	}

	@Override
	@Transactional
	public void save(MailHistory mailHistory) {
		mailHistoryEao.save(mailHistory);
		
	}

	@Override
	@Transactional
	public MailHistory update(MailHistory mailHistory) {
		return (MailHistory)mailHistoryEao.update(mailHistory);
	}

	@Override
	@Transactional
	public void delete(MailHistory mailHistory) {
		mailHistoryEao.delete(mailHistory);
		
	}

	@Override
	@Transactional
	public void deleteById(Object primaryKey) {
		mailHistoryEao.delete(MailHistory.class, primaryKey);
		
	}

	public MailHistoryEao getMailHistoryEao() {
		return mailHistoryEao;
	}

	public void setMailHistoryEao(MailHistoryEao mailHistoryEao) {
		this.mailHistoryEao = mailHistoryEao;
	}
	
}
