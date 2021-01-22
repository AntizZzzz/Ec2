package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderFileEao;
import com.jiangyifen.ec2.entity.WorkOrderFile;

import com.jiangyifen.ec2.service.eaoservice.WorkOrderFileService;
/**
* Service实现类：工单附件管理
* 
* @author lxy
* 
*/
public class WorkOrderFileServiceImpl implements WorkOrderFileService {

	
	/** 需要注入的Eao */
	private WorkOrderFileEao workOrderFileEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderFile> loadPageEntities(int start, int length, String sql) {
		return workOrderFileEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return workOrderFileEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得工单附件
	@Override
	public WorkOrderFile getWorkOrderFileById(Long id){
		return workOrderFileEao.get(WorkOrderFile.class, id);
	}
	
	// 保存工单附件
	@Override
	public void saveWorkOrderFile(WorkOrderFile workOrderFile){
		workOrderFileEao.save(workOrderFile);
	}
	
	// 更新工单附件
	@Override
	public void updateWorkOrderFile(WorkOrderFile workOrderFile){
		workOrderFileEao.update(workOrderFile);
	}
	
	// 删除工单附件
	@Override
	public void deleteWorkOrderFile(WorkOrderFile workOrderFile){
		workOrderFileEao.delete(workOrderFile);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from WorkOrderFile as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from WorkOrderFile as s where s. = "++" order by s.ordernumber asc ";
		List<WorkOrderFile> list = workOrderFileEao.loadWorkOrderFileList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public WorkOrderFileEao getWorkOrderFileEao() {
		return workOrderFileEao;
	}
	
	//Eao注入
	public void setWorkOrderFileEao(WorkOrderFileEao workOrderFileEao) {
		this.workOrderFileEao = workOrderFileEao;
	}
}