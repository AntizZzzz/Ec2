package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderHandleResult;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单处置结果
* 
* @author lxy
*
*/
public interface WorkOrderHandleResultService extends FlipSupportService<WorkOrderHandleResult>  {
	
	/**
	 * 根据主键ID获得工单处置结果
	 * @param id	主键ID 
	 * @return		工单处置结果，一条或null
	 */
	@Transactional
	public WorkOrderHandleResult getWorkOrderHandleResultById(Long id);
	
	/**
	 * 保存工单处置结果
	 * @param workOrderHandleResult	工单处置结果
	 * 
	 */
	@Transactional
	public void saveWorkOrderHandleResult(WorkOrderHandleResult workOrderHandleResult);
	
	/**
	 * 更新工单处置结果
	 * @param workOrderHandleResult	工单处置结果
	 * 
	 */
	@Transactional
	public void updateWorkOrderHandleResult(WorkOrderHandleResult workOrderHandleResult);
	
	/**
	 * 删除工单处置结果
	 * @param workOrderHandleResult	工单处置结果
	 * 
	 */
	@Transactional
	public void deleteWorkOrderHandleResult(WorkOrderHandleResult workOrderHandleResult);
	
	/**
	 * 根据域获得工单处置列表
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<WorkOrderHandleResult> getAllByDomain(Domain domain);
	
}