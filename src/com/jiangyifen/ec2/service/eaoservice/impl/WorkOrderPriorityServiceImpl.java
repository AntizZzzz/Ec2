package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderPriorityEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderPriority;

import com.jiangyifen.ec2.service.eaoservice.WorkOrderPriorityService;
/**
* Service实现类：工单优先级管理
* 
* @author lxy
* 
*/
public class WorkOrderPriorityServiceImpl implements WorkOrderPriorityService {

	
	/** 需要注入的Eao */
	private WorkOrderPriorityEao workOrderPriorityEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderPriority> loadPageEntities(int start, int length, String sql) {
		return workOrderPriorityEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderPriorityEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单优先级
	@Override
	public WorkOrderPriority getWorkOrderPriorityById(Long id){
		return workOrderPriorityEao.get(WorkOrderPriority.class, id);
	}
	
	// 保存工单优先级
	@Override
	public void saveWorkOrderPriority(WorkOrderPriority workOrderPriority){
		workOrderPriorityEao.save(workOrderPriority);
	}
	
	// 更新工单优先级
	@Override
	public void updateWorkOrderPriority(WorkOrderPriority workOrderPriority){
		workOrderPriorityEao.update(workOrderPriority);
	}
	
	// 删除工单优先级
	@Override
	public void deleteWorkOrderPriority(WorkOrderPriority workOrderPriority){
		workOrderPriorityEao.delete(workOrderPriority);
	}
	
	//根据域获得工单优先级列表
	@Override
	public List<WorkOrderPriority> getAllByDomain(Domain domain) {
		return workOrderPriorityEao.getAllByDomain(domain);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrderPriority as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrderPriority as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrderPriority> list = workOrderPriorityEao.loadWorkOrderPriorityList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkOrderPriorityEao getWorkOrderPriorityEao() {
		return workOrderPriorityEao;
	}
	
	//Eao注入
	public void setWorkOrderPriorityEao(WorkOrderPriorityEao workOrderPriorityEao) {
		this.workOrderPriorityEao = workOrderPriorityEao;
	}

	
}