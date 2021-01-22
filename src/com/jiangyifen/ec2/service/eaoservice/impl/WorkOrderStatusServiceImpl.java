package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderStatusEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderStatus;

import com.jiangyifen.ec2.service.eaoservice.WorkOrderStatusService;
/**
* Service实现类：工单状态管理
* 
* @author lxy
* 
*/
public class WorkOrderStatusServiceImpl implements WorkOrderStatusService {

	
	/** 需要注入的Eao */
	private WorkOrderStatusEao workOrderStatusEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderStatus> loadPageEntities(int start, int length, String sql) {
		return workOrderStatusEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderStatusEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单状态
	@Override
	public WorkOrderStatus getWorkOrderStatusById(Long id){
		return workOrderStatusEao.get(WorkOrderStatus.class, id);
	}
	
	// 保存工单状态
	@Override
	public void saveWorkOrderStatus(WorkOrderStatus workOrderStatus){
		workOrderStatusEao.save(workOrderStatus);
	}
	
	// 更新工单状态
	@Override
	public void updateWorkOrderStatus(WorkOrderStatus workOrderStatus){
		workOrderStatusEao.update(workOrderStatus);
	}
	
	// 删除工单状态
	@Override
	public void deleteWorkOrderStatus(WorkOrderStatus workOrderStatus){
		workOrderStatusEao.delete(workOrderStatus);
	}
	
	//根据域获得工单状态列表
	@Override
	public List<WorkOrderStatus> getAllByDomain(Domain domain) {
		return workOrderStatusEao.getAllByDomain(domain);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrderStatus as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrderStatus as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrderStatus> list = workOrderStatusEao.loadWorkOrderStatusList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkOrderStatusEao getWorkOrderStatusEao() {
		return workOrderStatusEao;
	}
	
	//Eao注入
	public void setWorkOrderStatusEao(WorkOrderStatusEao workOrderStatusEao) {
		this.workOrderStatusEao = workOrderStatusEao;
	}

}