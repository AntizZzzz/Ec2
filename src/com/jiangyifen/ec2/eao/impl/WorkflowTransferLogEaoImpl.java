package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkflowTransferLogEao;
import com.jiangyifen.ec2.entity.WorkflowTransferLog;

/**
* Eao实现类：工作流迁移日志
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkflowTransferLogEaoImpl extends BaseEaoImpl implements WorkflowTransferLogEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工作流迁移日志列表
	public List<WorkflowTransferLog> loadWorkflowTransferLogList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}