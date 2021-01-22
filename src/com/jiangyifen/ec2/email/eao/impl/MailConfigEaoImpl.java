package com.jiangyifen.ec2.email.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.impl.BaseEaoImpl;
import com.jiangyifen.ec2.email.eao.MailConfigEao;
import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.entity.User;

public class MailConfigEaoImpl extends BaseEaoImpl implements MailConfigEao {

	@SuppressWarnings("rawtypes")
	@Override
	public MailConfig getMailConfigByUser(User user){
		// 无日期格式,直接用字符串查询
		List result = this.getEntityManager().createQuery("select m from MailConfig m where m.user.id="+user.getId() + " and m.domain.id=" + user.getDomain().getId()).getResultList(); //user包含domain概念
		return (MailConfig)(result.size()>0?result.get(0):null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public MailConfig getDefaultMailConfig(Long domainId){
		List result = this.getEntityManager().createQuery("select m from MailConfig m where m.isDefault='true' and m.domain.id="+domainId).getResultList(); //TODO 需要添加domain概念
		return (MailConfig)(result.size()>0?result.get(0):null);
	}
	
}
