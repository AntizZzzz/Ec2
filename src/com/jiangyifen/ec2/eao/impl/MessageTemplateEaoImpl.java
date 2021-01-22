package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MessageTemplateEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;

public class MessageTemplateEaoImpl extends BaseEaoImpl implements MessageTemplateEao{

	@SuppressWarnings("unchecked")
	@Override
	public List<MessageTemplate> getMessagesByType(MessageTemplateType type) {
		String sql = "select m from MessageTemplate m where m.type = :index";
		
		return this.getEntityManager().createQuery(sql).setParameter("index", type).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MessageTemplate> getMessagesByCreator(User user) {
		String sql = "select m from MessageTemplate m where m.user.id = "+user.getId()+" order by m.id desc";
		return this.getEntityManager().createQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MessageTemplate> getAllByDomain(Domain domain) {
		String sql = "select m from MessageTemplate m where m.domain.id="+domain.getId()+" order by m.id desc";
		
		return this.getEntityManager().createQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MessageTemplate> getAllByJpql(String searchSql) {
		return this.getEntityManager().createQuery(searchSql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MessageTemplate> getAllByType(MessageTemplateType templateType, Long domainId) {
		String jpql = "select m from MessageTemplate as m where m.type = :templateType and m.domain.id =:domainId order by m.id desc";
		return this.getEntityManager().createQuery(jpql).setParameter("templateType", templateType).setParameter("domainId", domainId).getResultList();
	}
	
}
