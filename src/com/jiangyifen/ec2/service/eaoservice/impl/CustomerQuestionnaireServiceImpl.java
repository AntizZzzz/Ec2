package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerQuestionnaireEao;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireService;

public class CustomerQuestionnaireServiceImpl implements CustomerQuestionnaireService {
	
	private CustomerQuestionnaireEao customerQuestionnaireEao;

	// common method
	
	@Override
	public CustomerQuestionnaire get(Object primaryKey) {
		return customerQuestionnaireEao.get(CustomerQuestionnaire.class, primaryKey);
	}

	@Override
	public void save(CustomerQuestionnaire customerQuestionnaire) {
		customerQuestionnaireEao.save(customerQuestionnaire);
	}

	@Override
	public CustomerQuestionnaire update(CustomerQuestionnaire customerQuestionnaire) {
		return (CustomerQuestionnaire) customerQuestionnaireEao.update(customerQuestionnaire);
	}

	@Override
	public void delete(CustomerQuestionnaire customerQuestionnaire) {
		customerQuestionnaireEao.delete(customerQuestionnaire);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerQuestionnaireEao.delete(CustomerQuestionnaire.class, primaryKey);
	}

	// enhanced method
	
	@Override
	public List<CustomerQuestionnaire> getCustomerQuestionnaireByCustomerId(
			Long customerId, Long questionnaireId) {
		return customerQuestionnaireEao.getCustomerQuestionnaireByCustomerId(customerId, questionnaireId);
	}

	public CustomerQuestionnaireEao getCustomerQuestionnaireEao() {
		return customerQuestionnaireEao;
	}

	public void setCustomerQuestionnaireEao(CustomerQuestionnaireEao customerQuestionnaireEao) {
		this.customerQuestionnaireEao = customerQuestionnaireEao;
	}


}
