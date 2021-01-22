package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：客户问题选项管理
* 
* @author lxy
*
*/
public interface CustomerQuestionOptionsService extends FlipSupportService<CustomerQuestionOptions>  {
	
	/**
	 * 根据主键ID获得客户问题选项
	 * @param id	主键ID 
	 * @return		客户问题选项，一条或null
	 */
	@Transactional
	public CustomerQuestionOptions getCustomerQuestionOptionsById(Long id);
	
	/**
	 * 保存客户问题选项
	 * @param customerQuestionOptions	客户问题选项
	 * 
	 */
	@Transactional
	public void saveCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions);
	
	/**
	 * 更新客户问题选项
	 * @param customerQuestionOptions	客户问题选项
	 * 
	 */
	@Transactional
	public CustomerQuestionOptions updateCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions);
	
	/**
	 * 删除客户问题选项
	 * @param customerQuestionOptions	客户问题选项
	 * 
	 */
	@Transactional
	public void deleteCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions);
	
}