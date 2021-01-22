package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.eao.MarketingProjectTaskEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;

public class MarketingProjectTaskServiceImpl implements
		MarketingProjectTaskService {
	private MarketingProjectTaskEao marketingProjectTaskEao;

	// enhance function method

	@Override
	public Long getTaskCountByUser(User user,
			MarketingProjectStatus projectStatus) {
		return marketingProjectTaskEao.getTaskCountByUser(user, projectStatus);
	}
	
//
//	@Override
//	public List<MarketingProject> getProjectInTaskByUser(User user) {
//		return marketingProjectTaskEao.getAllProjectsByUser(user);
//	}
//
//	@Override
//	public List<MarketingProject> getProjectInTaskByUser(User user,
//			MarketingProjectStatus projectStatus) {
//		return marketingProjectTaskEao.getProjectInTaskByUser(user,
//				projectStatus);
//	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProjectTask> loadPageEntities(int startIndex,
			int pageRecords, String selectSql) {
		return marketingProjectTaskEao.loadPageEntities(startIndex,
				pageRecords, selectSql);
	}

	@Override
	public int getEntityCount(String sql) {
		return marketingProjectTaskEao.getEntityCount(sql);
	}

	// common method

	@Override
	public MarketingProjectTask get(Object primaryKey) {
		return marketingProjectTaskEao.get(MarketingProjectTask.class,
				primaryKey);
	}

	@Override
	public void save(MarketingProjectTask marketingProjectTask) {
		marketingProjectTaskEao.save(marketingProjectTask);
	}

	@Override
	public MarketingProjectTask update(MarketingProjectTask marketingProjectTask) {
		return (MarketingProjectTask) marketingProjectTaskEao.update(marketingProjectTask);
	}

	@Override
	public void delete(MarketingProjectTask marketingProjectTask) {
		marketingProjectTaskEao.delete(marketingProjectTask);
	}

	@Override
	public void deleteById(Object primaryKey) {
		marketingProjectTaskEao.delete(MarketingProjectTask.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProjectTask> loadPageEntitys(int start, int length,
			String sql) {
		return marketingProjectTaskEao.loadPageEntities(start, length, sql);
	}

	// getter and setter

	public MarketingProjectTaskEao getMarketingProjectTaskEao() {
		return marketingProjectTaskEao;
	}

	public void setMarketingProjectTaskEao(
			MarketingProjectTaskEao marketingProjectTaskEao) {
		this.marketingProjectTaskEao = marketingProjectTaskEao;
	}

	/**
	 * chb 根据项目取得任务数总数
	 */
	@Override
	public Long getTotalNum(MarketingProject marketingProject, Domain domain) {
		return marketingProjectTaskEao.getTotalNum(marketingProject, domain);
	}


	/**
	 * chb 根据项目取得对应的工号集合
	 * 
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	@Override
	public List<String> getEmpNoListByProject(
			MarketingProject marketingProject, Domain domain) {
		return marketingProjectTaskEao.getEmpNoListByProject(marketingProject,
				domain);
	}

	/**
	 * chb 按照Csr回收
	 * 
	 * @return 回收数目
	 */
	@Override
	public Integer recycleByCsr(MarketingProject marketingProject, User user,
			Domain domain) {
		return marketingProjectTaskEao.recycleByCsr(marketingProject, user,
				domain);
	}

	/**
	 * chb 回收全部
	 * 
	 * @return 回收数目
	 */
	@Override
	public Integer recycleAll(MarketingProject marketingProject, Domain domain) {
		return marketingProjectTaskEao.recycleAll(marketingProject, domain);
	}

	/**
	 * chb 计算已经分配到任务中的Csr数目
	 * 
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	@Override
	public int getCsrCountByProject(MarketingProject marketingProject,
			Domain domain) {
		return marketingProjectTaskEao.getCsrCountByProject(marketingProject,
				domain);
	}

	/**
	 * chb 根据项目取得含有未完资源的Csr集合
	 * 
	 * @param project
	 * @return
	 */
	public List<User> getNotFinishedCsrsByProject(MarketingProject project) {
		return marketingProjectTaskEao.getNotFinishedCsrsByProject(project);
	}
	/**
	 * chb 根据资源和项目来删除Task任务
	 * 
	 * @param resource
	 * @param project
	 */
	@Override
	public void deleteTaskByResource(CustomerResource resource,
			MarketingProject project) {
		marketingProjectTaskEao.deleteTaskByResource(resource, project);
	}

	/**
	 * chb 根据项目清除所有未完成资源，实际已经隐含域了
	 * 
	 * @param marketingProject
	 * @param domain
	 */
	@Override
	public void clearAllUnfinished(MarketingProject marketingProject,
			Domain domain) {
		marketingProjectTaskEao.clearAllUnfinished(marketingProject, domain);
	}

	/**
	 * chb 按照项目和批次回收指定项目的指定批次资源
	 * 
	 * @param project
	 * @param batch
	 * @param domain
	 * @return
	 */
	@Override
	public Long recycleByBatches(MarketingProject project,
			List<CustomerResourceBatch> batches, Domain domain) {
		return marketingProjectTaskEao.recycleByBatches(project, batches, domain);
	}

	/**
	 * chb 按照项目和批次回收指定项目的指定批次资源
	 * 
	 * @param project
	 * @param batch
	 * @param domain
	 * @return
	 */
	@Override
	public Long recycleToBatches(MarketingProject project,
			List<CustomerResourceBatch> batches, Domain domain,Long toBatchId) {
		return marketingProjectTaskEao.recycleToBatches(project, batches, domain,toBatchId);
	}
	
	/**
	 * chb
	 * 对资源进行迁移操作(去重)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	@Override
	public void migrateResourceDelete(MarketingProject projectFrom,
			MarketingProject projectTo) {
		marketingProjectTaskEao.migrateResourceDelete(projectFrom,projectTo);
	}
	/**
	 * chb
	 * 对资源进行迁移操作(迁移)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	@Override
	public void migrateResourceMigrate(MarketingProject projectFrom,
			MarketingProject projectTo) {
		marketingProjectTaskEao.migrateResourceMigrate(projectFrom,projectTo);
	}
	/**
	 * chb
	 * 根据资源的Id值取出把该资源当任务的CSR
	 * @param id
	 * @return
	 */
	@Override
	public User getCsrByCustomerResourceId(Long resourceId,Long projectId) {
		return marketingProjectTaskEao.getCsrByCustomerResourceId(resourceId,projectId);
	}

	/**
	 * chb 自动外呼获取资源使用
	 * 取得制定项目的Task，并且限定取出的Task的Id值大于cursor
	 * @param projectId
	 * @param cursor
	 * @return
	 */
	@Override
	public List<MarketingProjectTask> getStepNotDistributedProjectTaskByMinId(Long projectId,
			Long cursor,Long step) {
		return marketingProjectTaskEao.getStepNotDistributedProjectTaskByMinId(projectId,cursor,step);
	}

}
