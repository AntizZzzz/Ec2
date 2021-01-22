package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.WorkOrderFile;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：工单附件管理
* 
* @author lxy
*
*/
public interface WorkOrderFileService extends FlipSupportService<WorkOrderFile>  {
	
	/**
	 * 根据主键ID获得工单附件
	 * @param id	主键ID 
	 * @return		工单附件，一条或null
	 */
	@Transactional
	public WorkOrderFile getWorkOrderFileById(Long id);
	
	/**
	 * 保存工单附件
	 * @param workOrderFile	工单附件
	 * 
	 */
	@Transactional
	public void saveWorkOrderFile(WorkOrderFile workOrderFile);
	
	/**
	 * 更新工单附件
	 * @param workOrderFile	工单附件
	 * 
	 */
	@Transactional
	public void updateWorkOrderFile(WorkOrderFile workOrderFile);
	
	/**
	 * 删除工单附件
	 * @param workOrderFile	工单附件
	 * 
	 */
	@Transactional
	public void deleteWorkOrderFile(WorkOrderFile workOrderFile);
	
}