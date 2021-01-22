package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerQuestionOptionsEao;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;

/**
* Eao实现类：客户问题选项管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class CustomerQuestionOptionsEaoImpl extends BaseEaoImpl implements CustomerQuestionOptionsEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得客户问题选项列表
	public List<CustomerQuestionOptions> loadCustomerQuestionOptionsList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public CustomerQuestionOptions findOptionIsSelect(Long customerQuestionnaireId,
			Long questionId, Long optionId) {
		String jpql  = "select s from CustomerQuestionOptions as s  where s.customerQuestionnaire.id = "+customerQuestionnaireId 
		+" and s.question.id = "+ questionId
		+" and s.questionOptions.id = " + optionId;
		List<CustomerQuestionOptions> list = this.loadPageEntities(0, 1, jpql);
		if((null != list)&&(list.size() == 1)){
			return list.get(0);
		}else{
			return null;
		}
	}
}