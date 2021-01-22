package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.WorkOrderOperate;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单操作管理
* 
* @author lxy
*
*/
public interface WorkOrderOperateService extends FlipSupportService<WorkOrderOperate>  {
	
	/**
	 * 根据主键ID获得工单操作
	 * @param id	主键ID 
	 * @return		工单操作，一条或null
	 */
	@Transactional
	public WorkOrderOperate getWorkOrderOperateById(Long id);
	
	/**
	 * 保存工单操作
	 * @param workOrderOperate	工单操作
	 * 
	 */
	@Transactional
	public void saveWorkOrderOperate(WorkOrderOperate workOrderOperate);
	
	/**
	 * 更新工单操作
	 * @param workOrderOperate	工单操作
	 * 
	 */
	@Transactional
	public void updateWorkOrderOperate(WorkOrderOperate workOrderOperate);
	
	/**
	 * 删除工单操作
	 * @param workOrderOperate	工单操作
	 * 
	 */
	@Transactional
	public void deleteWorkOrderOperate(WorkOrderOperate workOrderOperate);
	
}