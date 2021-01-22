package com.jiangyifen.ec2.email.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.impl.BaseEaoImpl;
import com.jiangyifen.ec2.email.eao.MailContactEao;
import com.jiangyifen.ec2.email.entity.MailContact;

public class MailContactEaoImpl extends BaseEaoImpl implements MailContactEao {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<MailContact> getMailContactsByUserId(Long userId){
		List result = this.getEntityManager().createQuery("select m from MailContact m where m.user.id="+userId).getResultList(); //user包含domain概念
		return result;
	}

}
