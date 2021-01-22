package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderHandleResultEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderHandleResult;

import com.jiangyifen.ec2.service.eaoservice.WorkOrderHandleResultService;
/**
* Service实现类：工单处置结果
* 
* @author lxy
* 
*/
public class WorkOrderHandleResultServiceImpl implements WorkOrderHandleResultService {

	
	/** 需要注入的Eao */
	private WorkOrderHandleResultEao workOrderHandleResultEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderHandleResult> loadPageEntities(int start, int length, String sql) {
		return workOrderHandleResultEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderHandleResultEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单处置结果
	@Override
	public WorkOrderHandleResult getWorkOrderHandleResultById(Long id){
		return workOrderHandleResultEao.get(WorkOrderHandleResult.class, id);
	}
	
	// 保存工单处置结果
	@Override
	public void saveWorkOrderHandleResult(WorkOrderHandleResult workOrderHandleResult){
		workOrderHandleResultEao.save(workOrderHandleResult);
	}
	
	// 更新工单处置结果
	@Override
	public void updateWorkOrderHandleResult(WorkOrderHandleResult workOrderHandleResult){
		workOrderHandleResultEao.update(workOrderHandleResult);
	}
	
	// 删除工单处置结果
	@Override
	public void deleteWorkOrderHandleResult(WorkOrderHandleResult workOrderHandleResult){
		workOrderHandleResultEao.delete(workOrderHandleResult);
	}
	
	//根据域获得工单处置列表
	@Override
	public List<WorkOrderHandleResult> getAllByDomain(Domain domain) {
		return workOrderHandleResultEao.getAllByDomain(domain);
	}
		
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrderHandleResult as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrderHandleResult as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrderHandleResult> list = workOrderHandleResultEao.loadWorkOrderHandleResultList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkOrderHandleResultEao getWorkOrderHandleResultEao() {
		return workOrderHandleResultEao;
	}
	
	//Eao注入
	public void setWorkOrderHandleResultEao(WorkOrderHandleResultEao workOrderHandleResultEao) {
		this.workOrderHandleResultEao = workOrderHandleResultEao;
	}

	
}