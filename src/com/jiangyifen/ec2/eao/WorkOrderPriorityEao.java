package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderPriority;

/**
* Eao接口：工单优先级管理
* 
* @author lxy
*
*/
public interface WorkOrderPriorityEao extends BaseEao {
	
	
	/**
	 * 获得工单优先级列表
	 * @param 	jpql jpql语句
	 * @return 	工单优先级列表
	 */
	public List<WorkOrderPriority> loadWorkOrderPriorityList(String jpql);
	
	/**
	 * 根据域获得工单优先级列表
	 * @param domain
	 * @return
	 */
	public List<WorkOrderPriority> getAllByDomain(Domain domain);
}