package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.MessageTemplateEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;

public class MessageTemplateServiceImpl implements MessageTemplateService {

	private MessageTemplateEao messageTemplateEao;

	@Override
	@Transactional
	public void save(MessageTemplate message) {
		messageTemplateEao.save(message);
	}

	@Override
	@Transactional
	public void delete(MessageTemplate message) {
		messageTemplateEao.delete(MessageTemplate.class,message.getId());

	}

	@Override
	@Transactional
	public MessageTemplate update(MessageTemplate message) {
		return (MessageTemplate) messageTemplateEao.update(message);

	}

	@Override
	@Transactional
	public List<MessageTemplate> getMessagesByType(MessageTemplateType type) {

		return messageTemplateEao.getMessagesByType(type);
	}

	@Override
	@Transactional
	public List<MessageTemplate> getMessagesByCreator(User user) {

		return messageTemplateEao.getMessagesByCreator(user);
	}

	@Override
	@Transactional
	public List<MessageTemplate> getAllByDomain(Domain domain) {
		return messageTemplateEao.getAllByDomain(domain);
	}
	
	@Override
	public List<MessageTemplate> getAllByJpql(String searchSql) {
		return messageTemplateEao.getAllByJpql(searchSql);
	}

	@Override
	public List<MessageTemplate> getAllByType(MessageTemplateType templateType, Long domainId) {
		return messageTemplateEao.getAllByType(templateType, domainId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MessageTemplate> loadPageEntities(int start, int length, String sql) {
		return messageTemplateEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return messageTemplateEao.getEntityCount(sql);
	}
	
	
	// *******************setter和 getter方法******************//

	public MessageTemplateEao getMessageTemplateEao() {
		return messageTemplateEao;
	}
	
	public void setMessageTemplateEao(MessageTemplateEao messageTemplateEao) {
		this.messageTemplateEao = messageTemplateEao;
	}

}
