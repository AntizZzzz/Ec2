package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单类型管理
* 
* @author lxy
*
*/
public interface WorkOrderTypeService extends FlipSupportService<WorkOrderType>  {
	
	/**
	 * 根据主键ID获得工单类型
	 * @param id	主键ID 
	 * @return		工单类型，一条或null
	 */
	@Transactional
	public WorkOrderType getWorkOrderTypeById(Long id);
	
	/**
	 * 保存工单类型
	 * @param workOrderType	工单类型
	 * 
	 */
	@Transactional
	public void saveWorkOrderType(WorkOrderType workOrderType);
	
	/**
	 * 更新工单类型
	 * @param workOrderType	工单类型
	 * 
	 */
	@Transactional
	public void updateWorkOrderType(WorkOrderType workOrderType);
	
	/**
	 * 删除工单类型
	 * @param workOrderType	工单类型
	 * 
	 */
	@Transactional
	public void deleteWorkOrderType(WorkOrderType workOrderType);
	
	/**
	 * 根据域获得工单类型列表
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<WorkOrderType> getAllByDomain(Domain domain);
}