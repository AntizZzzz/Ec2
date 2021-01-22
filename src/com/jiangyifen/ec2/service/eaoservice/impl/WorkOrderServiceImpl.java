package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkOrder;
import com.jiangyifen.ec2.entity.WorkOrderFile;
import com.jiangyifen.ec2.entity.WorkOrderOperate;
import com.jiangyifen.ec2.entity.WorkOrderStatus;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderFileService;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderOperateService;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderService;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderStatusService;
/**
* Service实现类：工单管理
* 
* @author lxy
* 
*/
public class WorkOrderServiceImpl implements WorkOrderService {

	
	/** 需要注入的Eao */
	private WorkOrderEao workOrderEao;
	private WorkOrderFileService workOrderFileService;
	private WorkOrderStatusService workOrderStatusService;
	private WorkOrderOperateService workOrderOperateService;
	
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrder> loadPageEntities(int start, int length, String sql) {
		return workOrderEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单
	@Override
	public WorkOrder getWorkOrderById(Long id){
		return workOrderEao.get(WorkOrder.class, id);
	}
	
	// 保存工单
	@Override
	public void saveWorkOrder(WorkOrder workOrder){
		workOrderEao.save(workOrder);
	}
	
	// 更新工单
	@Override
	public void updateWorkOrder(WorkOrder workOrder){
		workOrderEao.update(workOrder);
	}
	
	// 删除工单
	@Override
	public void deleteWorkOrder(WorkOrder workOrder){
		workOrderEao.delete(workOrder);
	}
	
	//保存工单和附件列表
	@Override
	public void saveWorkOrderAndWorkOrderFileList(WorkOrder workOrder,
			List<WorkOrderFile> wofList) {
		WorkOrderStatus workOrderStatus = workOrderStatusService.getWorkOrderStatusById(new Long(1));
		workOrder.setCreateTime(new Date());
		workOrder.setLastUpdateTime(new Date());
		workOrder.setWorkOrderStatus(workOrderStatus);
		workOrderEao.save(workOrder);
		
		WorkOrderOperate workOrderOperate = new WorkOrderOperate();
		workOrderOperate.setType("OPERATE");
		workOrderOperate.setNowStatus(workOrderStatus.getName());
		workOrderOperate.setCreateTime(new Date());
		workOrderOperate.setLastUpdateTime(new Date());
		workOrderOperate.setDomain(workOrder.getDomain());
		workOrderOperate.setWorkOrder(workOrder);
		workOrderOperateService.saveWorkOrderOperate(workOrderOperate);
		if (null != wofList && wofList.size() > 0) {
			for (WorkOrderFile workOrderFile : wofList) {
				workOrderFile.setWorkOrder(workOrder);
				workOrderFileService.saveWorkOrderFile(workOrderFile);
			}
		}
		
	}
	
	@Override
	public List<WorkOrder> getWorkOrderListBuDomainAndUser(Domain domain,
			User loginUser) {
		String jpql = "select s from WorkOrder as s where s.domain.id = "+domain.getId()+" and s.handleUser.id = "+loginUser.getId()+" order by s.workOrderPriority.orderNumber asc ";
		List<WorkOrder> list = workOrderEao.loadWorkOrderList(jpql);
		return list;
	}
		
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrder as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrder as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrder> list = workOrderEao.loadWorkOrderList(jpql);
		return list;
	*
	*/
	
	
	
	//Eao注入
	public WorkOrderEao getWorkOrderEao() {
		return workOrderEao;
	}
	//Eao注入
	public void setWorkOrderEao(WorkOrderEao workOrderEao) {
		this.workOrderEao = workOrderEao;
	}
	//Eao注入
	public WorkOrderFileService getWorkOrderFileService() {
		return workOrderFileService;
	}
	//Eao注入
	public void setWorkOrderFileService(WorkOrderFileService workOrderFileService) {
		this.workOrderFileService = workOrderFileService;
	}
	//Eao注入
	public WorkOrderStatusService getWorkOrderStatusService() {
		return workOrderStatusService;
	}
	//Eao注入
	public void setWorkOrderStatusService(
			WorkOrderStatusService workOrderStatusService) {
		this.workOrderStatusService = workOrderStatusService;
	}
	//Eao注入
	public WorkOrderOperateService getWorkOrderOperateService() {
		return workOrderOperateService;
	}
	//Eao注入
	public void setWorkOrderOperateService(
			WorkOrderOperateService workOrderOperateService) {
		this.workOrderOperateService = workOrderOperateService;
	}
	 

}