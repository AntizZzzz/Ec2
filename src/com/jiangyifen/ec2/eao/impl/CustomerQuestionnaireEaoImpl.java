package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerQuestionnaireEao;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;

public class CustomerQuestionnaireEaoImpl extends BaseEaoImpl implements CustomerQuestionnaireEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerQuestionnaire> getCustomerQuestionnaireByCustomerId(
			Long customerId, Long questionnaireId) {
		return getEntityManager().createQuery("select cq from CustomerQuestionnaire as cq where cq.customerid = " +customerId+ " and cq.questionnaire.id = " +questionnaireId).getResultList();
	}

}
