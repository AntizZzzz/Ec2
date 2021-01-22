package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;

/**
* Eao接口：客户问题选项管理
* 
* @author lxy
*
*/
public interface CustomerQuestionOptionsEao extends BaseEao {
	
	
	/**
	 * 获得客户问题选项列表
	 * @param 	jpql jpql语句
	 * @return 	客户问题选项列表
	 */
	public List<CustomerQuestionOptions> loadCustomerQuestionOptionsList(String jpql);

	public CustomerQuestionOptions findOptionIsSelect(Long customerQuestionnaireId,
			Long questionId, Long optionId);
	
}