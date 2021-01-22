package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderStatus;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单状态管理
* 
* @author lxy
*
*/
public interface WorkOrderStatusService extends FlipSupportService<WorkOrderStatus>  {
	
	/**
	 * 根据主键ID获得工单状态
	 * @param id	主键ID 
	 * @return		工单状态，一条或null
	 */
	@Transactional
	public WorkOrderStatus getWorkOrderStatusById(Long id);
	
	/**
	 * 保存工单状态
	 * @param workOrderStatus	工单状态
	 * 
	 */
	@Transactional
	public void saveWorkOrderStatus(WorkOrderStatus workOrderStatus);
	
	/**
	 * 更新工单状态
	 * @param workOrderStatus	工单状态
	 * 
	 */
	@Transactional
	public void updateWorkOrderStatus(WorkOrderStatus workOrderStatus);
	
	/**
	 * 删除工单状态
	 * @param workOrderStatus	工单状态
	 * 
	 */
	@Transactional
	public void deleteWorkOrderStatus(WorkOrderStatus workOrderStatus);
	
	/**
	 * 根据域获得工单状态列表
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<WorkOrderStatus> getAllByDomain(Domain domain);
}