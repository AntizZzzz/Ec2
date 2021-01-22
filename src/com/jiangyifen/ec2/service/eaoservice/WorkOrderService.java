package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkOrder;
import com.jiangyifen.ec2.entity.WorkOrderFile;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单管理
* 
* @author lxy
*
*/
public interface WorkOrderService extends FlipSupportService<WorkOrder>  {
	
	/**
	 * 根据主键ID获得工单
	 * @param id	主键ID 
	 * @return		工单，一条或null
	 */
	@Transactional
	public WorkOrder getWorkOrderById(Long id);
	
	/**
	 * 保存工单
	 * @param workOrder	工单
	 * 
	 */
	@Transactional
	public void saveWorkOrder(WorkOrder workOrder);
	
	/**
	 * 更新工单
	 * @param workOrder	工单
	 * 
	 */
	@Transactional
	public void updateWorkOrder(WorkOrder workOrder);
	
	/**
	 * 删除工单
	 * @param workOrder	工单
	 * 
	 */
	@Transactional
	public void deleteWorkOrder(WorkOrder workOrder);

	/**
	 * 保存工单和附件列表
	 * @param workOrder
	 * @param wofList
	 */
	@Transactional
	public void saveWorkOrderAndWorkOrderFileList(WorkOrder workOrder,
			List<WorkOrderFile> wofList);

	/**
	 * 根据域和用户获得该用户在该域下的所有相关工单
	 * @param domain
	 * @param loginUser
	 * @return
	 */
	public List<WorkOrder> getWorkOrderListBuDomainAndUser(Domain domain,
			User loginUser);
	
}