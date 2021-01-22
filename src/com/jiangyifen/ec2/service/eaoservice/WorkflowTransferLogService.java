package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.WorkflowTransferLog;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工作流迁移日志
* 
* @author jrh
*
*/
public interface WorkflowTransferLogService extends FlipSupportService<WorkflowTransferLog>  {
	
	/**
	 * 根据主键ID获得工作流迁移日志
	 * @param id	主键ID 
	 * @return		工作流迁移日志，一条或null
	 */
	@Transactional
	public WorkflowTransferLog getWorkflowTransferLogById(Long id);
	
	/**
	 * 保存工作流迁移日志
	 * @param workflowTransferLog	工作流迁移日志
	 * 
	 */
	@Transactional
	public void saveWorkflowTransferLog(WorkflowTransferLog workflowTransferLog);
	
	/**
	 * 更新工作流迁移日志
	 * @param workflowTransferLog	工作流迁移日志
	 * 
	 */
	@Transactional
	public WorkflowTransferLog updateWorkflowTransferLog(WorkflowTransferLog workflowTransferLog);
	
	/**
	 * 删除工作流迁移日志
	 * @param workflowTransferLog	工作流迁移日志
	 * 
	 */
	@Transactional
	public void deleteWorkflowTransferLog(WorkflowTransferLog workflowTransferLog);
	
}