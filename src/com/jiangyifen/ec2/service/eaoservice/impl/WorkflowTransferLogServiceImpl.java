package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkflowTransferLogEao;
import com.jiangyifen.ec2.entity.WorkflowTransferLog;

import com.jiangyifen.ec2.service.eaoservice.WorkflowTransferLogService;

/**
* Service实现类：工作流迁移日志
* 
* @author jrh
* 
*/
public class WorkflowTransferLogServiceImpl implements WorkflowTransferLogService {

	/** 需要注入的Eao */
	private WorkflowTransferLogEao workflowTransferLogEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkflowTransferLog> loadPageEntities(int start, int length, String sql) {
		return workflowTransferLogEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workflowTransferLogEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工作流迁移日志
	@Override
	public WorkflowTransferLog getWorkflowTransferLogById(Long id){
		return workflowTransferLogEao.get(WorkflowTransferLog.class, id);
	}
	
	// 保存工作流迁移日志
	@Override
	public void saveWorkflowTransferLog(WorkflowTransferLog workflowTransferLog){
		workflowTransferLogEao.save(workflowTransferLog);
	}
	
	// 更新工作流迁移日志
	@Override
	public WorkflowTransferLog updateWorkflowTransferLog(WorkflowTransferLog workflowTransferLog){
		return (WorkflowTransferLog) workflowTransferLogEao.update(workflowTransferLog);
	}
	
	// 删除工作流迁移日志
	@Override
	public void deleteWorkflowTransferLog(WorkflowTransferLog workflowTransferLog){
		workflowTransferLogEao.delete(workflowTransferLog);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkflowTransferLog as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkflowTransferLog as s where s. = "++" order by s.ordernumber asc ";
		List<WorkflowTransferLog> list = workflowTransferLogEao.loadWorkflowTransferLogList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkflowTransferLogEao getWorkflowTransferLogEao() {
		return workflowTransferLogEao;
	}
	
	//Eao注入
	public void setWorkflowTransferLogEao(WorkflowTransferLogEao workflowTransferLogEao) {
		this.workflowTransferLogEao = workflowTransferLogEao;
	}
}