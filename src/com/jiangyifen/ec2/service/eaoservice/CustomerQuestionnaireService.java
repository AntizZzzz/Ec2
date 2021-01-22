package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerQuestionnaire;

public interface CustomerQuestionnaireService {

	// common method 
	
	@Transactional
	public CustomerQuestionnaire get(Object primaryKey);
	
	@Transactional
	public void save(CustomerQuestionnaire customerQuestionnaire);

	@Transactional
	public CustomerQuestionnaire update(CustomerQuestionnaire customerQuestionnaire);

	@Transactional
	public void delete(CustomerQuestionnaire customerQuestionnaire);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	// enhanced method
	/**
	 * 
	 * @param customerId
	 * @param questionnaireId
	 * @return
	 */
	@Transactional
	public List<CustomerQuestionnaire> getCustomerQuestionnaireByCustomerId(Long customerId, Long questionnaireId);
	
}
