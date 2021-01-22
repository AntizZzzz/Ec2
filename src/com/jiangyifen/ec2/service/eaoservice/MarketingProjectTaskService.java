package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface MarketingProjectTaskService extends FlipSupportService<MarketingProjectTask> {

	@Transactional
	public MarketingProjectTask get(Object primaryKey);
	
	@Transactional
	public void save(MarketingProjectTask marketingProjectTask);

	@Transactional
	public MarketingProjectTask update(MarketingProjectTask marketingProjectTask);

	public void delete(MarketingProjectTask marketingProjectTask);
	
	public void deleteById(Object primaryKey);
	
	@Transactional
	public List<MarketingProjectTask> loadPageEntitys(int start,int length,String sql);
//
//	/**
//	 *  jrh
//	 *  获取分配给某一用户的所有的项目
//	 * @param user 被分配过项目的用户
//	 * @return List 存放项目的集合
//	 */
//	@Transactional
//	public List<MarketingProject> getProjectInTaskByUser(User user);
//	
//	/**
//	 *  jrh
//	 *  获取分配给某一用户的处于指定状态的所有项目
//	 * @param user 被分配过项目的用户
//	 * @param projectStatus 项目状态，如果为 null 则表示获取分配给user 的所有项目
//	 * @return List 存放项目的集合
//	 */
//	@Transactional
//	public List<MarketingProject> getProjectInTaskByUser(User user, MarketingProjectStatus projectStatus);
	
	/**
	 *  jrh
	 *  根据 csr 对象获取分配给自己的所有正在执行中的任务数量
	 * @param user Csr 用户对象
	 * @param projectStatus	项目状态对象
	 * @return int 返回任务总数
	 */
	@Transactional
	public Long getTaskCountByUser(User user, MarketingProjectStatus projectStatus);
	
	/**
	 * chb
	 * 根据项目取得任务数总数
	 * 
	 */
	public Long getTotalNum(MarketingProject target,Domain domain);
	/**
	 * chb
	 * 根据项目取得对用的工号集合
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	public List<String> getEmpNoListByProject(
			MarketingProject marketingProject, Domain domain);
	/**
	 * chb
	 * 按照Csr回收
	 * @return 回收数目 
	 */
	@Transactional
	public Integer recycleByCsr(MarketingProject marketingProject,
			User user, Domain domain);

	/**
	 * chb
	 * 回收全部
	 * @return 回收数目
	 */
	@Transactional
	public Integer recycleAll(MarketingProject marketingProject, Domain domain);
	
	/**
	 * chb
	 *  计算已经分配到任务中的Csr数目
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	public int getCsrCountByProject(MarketingProject marketingProject,
			Domain domain);
	/**
	 * chb
	 * 根据项目取得含有未完资源的Csr集合
	 * @param project
	 * @return
	 */
	public List<User> getNotFinishedCsrsByProject(MarketingProject project);
	/**
	 * chb
	 * 根据资源和项目来删除Task任务
	 * @param resource
	 * @param project
	 */
	@Transactional
	public void deleteTaskByResource(CustomerResource resource,
			MarketingProject project);

	/**
	 * chb
	 * 根据项目清除所有未完成资源，实际已经隐含域了
	 * @param marketingProject
	 * @param domain
	 */
	@Transactional
	public void clearAllUnfinished(MarketingProject marketingProject,
			Domain domain);
	/**
	 * chb
	 * 按照项目和批次回收指定项目的指定批次资源
	 * @param project
	 * @param batch
	 * @param domain
	 * @return
	 */
	@Transactional
	public Long recycleByBatches(MarketingProject project,
			List<CustomerResourceBatch> batches, Domain domain);

	/**
	 * chb
	 * 按照项目和批次回收指定项目的指定批次资源到一个新的批次
	 * @param project
	 * @param batch
	 * @param domain
	 * @return
	 */
	@Transactional
	public Long recycleToBatches(MarketingProject project,
			List<CustomerResourceBatch> batches, Domain domain,Long toBatchId);

	/**
	 * chb
	 * 对资源进行迁移操作(去重)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	@Transactional
	public void migrateResourceDelete(MarketingProject projectFrom,
			MarketingProject projectTo);
	/**
	 * chb
	 * 对资源进行迁移操作(迁移)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	@Transactional
	public void migrateResourceMigrate(MarketingProject projectFrom,
			MarketingProject projectTo);
	/**
	 * chb
	 * 根据资源的Id值取出把该资源当任务的CSR
	 * @param id
	 * @return
	 */
	public User getCsrByCustomerResourceId(Long resourceId,Long projectId);

	/**
	 * chb 自动外呼获取资源使用
	 * 取得制定项目的Task，并且限定取出的Task的Id值大于cursor
	 * @param projectId
	 * @param cursor
	 * @return
	 */
	public List<MarketingProjectTask> getStepNotDistributedProjectTaskByMinId(Long projectId,
			Long cursor,Long step);
}
