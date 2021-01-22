package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.WorkflowTransferLog;

/**
* Eao接口：工作流迁移日志
* 
* @author lxy
*
*/
public interface WorkflowTransferLogEao extends BaseEao {
	
	
	/**
	 * 获得工作流迁移日志列表
	 * @param 	jpql jpql语句
	 * @return 	工作流迁移日志列表
	 */
	public List<WorkflowTransferLog> loadWorkflowTransferLogList(String jpql);
	
}