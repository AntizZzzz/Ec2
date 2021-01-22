package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderPriority;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单优先级管理
* 
* @author lxy
*
*/
public interface WorkOrderPriorityService extends FlipSupportService<WorkOrderPriority>  {
	
	/**
	 * 根据主键ID获得工单优先级
	 * @param id	主键ID 
	 * @return		工单优先级，一条或null
	 */
	@Transactional
	public WorkOrderPriority getWorkOrderPriorityById(Long id);
	
	/**
	 * 保存工单优先级
	 * @param workOrderPriority	工单优先级
	 * 
	 */
	@Transactional
	public void saveWorkOrderPriority(WorkOrderPriority workOrderPriority);
	
	/**
	 * 更新工单优先级
	 * @param workOrderPriority	工单优先级
	 * 
	 */
	@Transactional
	public void updateWorkOrderPriority(WorkOrderPriority workOrderPriority);
	
	/**
	 * 删除工单优先级
	 * @param workOrderPriority	工单优先级
	 * 
	 */
	@Transactional
	public void deleteWorkOrderPriority(WorkOrderPriority workOrderPriority);
	
	/**
	 * 根据域获得工单优先级列表
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<WorkOrderPriority> getAllByDomain(Domain domain);
}