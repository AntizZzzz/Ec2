package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderOperateEao;
import com.jiangyifen.ec2.entity.WorkOrderOperate;

import com.jiangyifen.ec2.service.eaoservice.WorkOrderOperateService;
/**
* Service实现类：工单操作管理
* 
* @author lxy
* 
*/
public class WorkOrderOperateServiceImpl implements WorkOrderOperateService {

	
	/** 需要注入的Eao */
	private WorkOrderOperateEao workOrderOperateEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderOperate> loadPageEntities(int start, int length, String sql) {
		return workOrderOperateEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderOperateEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单操作
	@Override
	public WorkOrderOperate getWorkOrderOperateById(Long id){
		return workOrderOperateEao.get(WorkOrderOperate.class, id);
	}
	
	// 保存工单操作
	@Override
	public void saveWorkOrderOperate(WorkOrderOperate workOrderOperate){
		workOrderOperateEao.save(workOrderOperate);
	}
	
	// 更新工单操作
	@Override
	public void updateWorkOrderOperate(WorkOrderOperate workOrderOperate){
		workOrderOperateEao.update(workOrderOperate);
	}
	
	// 删除工单操作
	@Override
	public void deleteWorkOrderOperate(WorkOrderOperate workOrderOperate){
		workOrderOperateEao.delete(workOrderOperate);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrderOperate as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrderOperate as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrderOperate> list = workOrderOperateEao.loadWorkOrderOperateList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkOrderOperateEao getWorkOrderOperateEao() {
		return workOrderOperateEao;
	}
	
	//Eao注入
	public void setWorkOrderOperateEao(WorkOrderOperateEao workOrderOperateEao) {
		this.workOrderOperateEao = workOrderOperateEao;
	}
}