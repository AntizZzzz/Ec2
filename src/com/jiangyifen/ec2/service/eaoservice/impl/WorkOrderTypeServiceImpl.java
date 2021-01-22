package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderTypeEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderType;

import com.jiangyifen.ec2.service.eaoservice.WorkOrderTypeService;
/**
* Service实现类：工单类型管理
* 
* @author lxy
* 
*/
public class WorkOrderTypeServiceImpl implements WorkOrderTypeService {

	
	/** 需要注入的Eao */
	private WorkOrderTypeEao workOrderTypeEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderType> loadPageEntities(int start, int length, String sql) {
		return workOrderTypeEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderTypeEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单类型
	@Override
	public WorkOrderType getWorkOrderTypeById(Long id){
		return workOrderTypeEao.get(WorkOrderType.class, id);
	}
	
	// 保存工单类型
	@Override
	public void saveWorkOrderType(WorkOrderType workOrderType){
		workOrderTypeEao.save(workOrderType);
	}
	
	// 更新工单类型
	@Override
	public void updateWorkOrderType(WorkOrderType workOrderType){
		workOrderTypeEao.update(workOrderType);
	}
	
	// 删除工单类型
	@Override
	public void deleteWorkOrderType(WorkOrderType workOrderType){
		workOrderTypeEao.delete(workOrderType);
	}
	
	//根据域获得工单类型列表
	@Override
	public List<WorkOrderType> getAllByDomain(Domain domain) {
		return workOrderTypeEao.getAllByDomain(domain);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrderType as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrderType as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrderType> list = workOrderTypeEao.loadWorkOrderTypeList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkOrderTypeEao getWorkOrderTypeEao() {
		return workOrderTypeEao;
	}
	
	//Eao注入
	public void setWorkOrderTypeEao(WorkOrderTypeEao workOrderTypeEao) {
		this.workOrderTypeEao = workOrderTypeEao;
	}

}