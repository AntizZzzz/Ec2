package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerQuestionnaire;


public interface CustomerQuestionnaireEao extends BaseEao {
	
	/**
	 * 
	 * @param customerId
	 * @param questionnaireId
	 * @return
	 */
	public List<CustomerQuestionnaire> getCustomerQuestionnaireByCustomerId(Long customerId, Long questionnaireId);
	
}
