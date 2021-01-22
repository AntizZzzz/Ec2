package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerQuestionOptionsEao;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;

import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionOptionsService;
/**
* Service实现类：客户问题选项管理
* 
* @author lxy
* 
*/
public class CustomerQuestionOptionsServiceImpl implements CustomerQuestionOptionsService {

	
	/** 需要注入的Eao */
	private CustomerQuestionOptionsEao customerQuestionOptionsEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<CustomerQuestionOptions> loadPageEntities(int start, int length, String sql) {
		return customerQuestionOptionsEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return customerQuestionOptionsEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得客户问题选项
	@Override
	public CustomerQuestionOptions getCustomerQuestionOptionsById(Long id){
		return customerQuestionOptionsEao.get(CustomerQuestionOptions.class, id);
	}
	
	// 保存客户问题选项
	@Override
	public void saveCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions){
		customerQuestionOptionsEao.save(customerQuestionOptions);
	}
	
	// 更新客户问题选项
	@Override
	public CustomerQuestionOptions updateCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions){
		return (CustomerQuestionOptions)customerQuestionOptionsEao.update(customerQuestionOptions);
	}
	
	// 删除客户问题选项
	@Override
	public void deleteCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions){
		customerQuestionOptionsEao.delete(customerQuestionOptions);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from CustomerQuestionOptions as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from CustomerQuestionOptions as s where s. = "++" order by s.ordernumber asc ";
		List<CustomerQuestionOptions> list = customerQuestionOptionsEao.loadCustomerQuestionOptionsList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public CustomerQuestionOptionsEao getCustomerQuestionOptionsEao() {
		return customerQuestionOptionsEao;
	}
	
	//Eao注入
	public void setCustomerQuestionOptionsEao(CustomerQuestionOptionsEao customerQuestionOptionsEao) {
		this.customerQuestionOptionsEao = customerQuestionOptionsEao;
	}
}