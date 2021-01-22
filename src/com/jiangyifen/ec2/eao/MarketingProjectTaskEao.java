package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;


public interface MarketingProjectTaskEao  extends BaseEao{

	List<MarketingProjectTask> getTaskByProject(MarketingProject marketingProject,
			Domain domain);

	List<MarketingProjectTask> getAssignableTaskByProject(MarketingProject marketingProject, List<CustomerResourceBatch> batches,
			Domain domain);
	
	/**
	 *  jrh
	 *  根据 csr 对象获取分配给自己的所有正在执行中的任务数量
	 * @param user Csr 用户对象
	 * @param projectStatus	项目状态对象
	 * @return int 返回任务总数
	 */
	public Long getTaskCountByUser(User user, MarketingProjectStatus projectStatus);
	
	/**
	 * chb
	 * 根据项目取得任务数总数
	 * 
	 */
	public Long getTotalNum(MarketingProject marketingProject,Domain domain);
	/**
	 * chb
	 * 根据项目、域、用户、取得对应的任务数
	 * @param user
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	public Long getTaskCountByUser(User user, MarketingProject marketingProject,
			Domain domain);

	/**
	 * chb
	 * 根据项目取得对用的工号集合
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	public List<String> getEmpNoListByProject(MarketingProject marketingProject,
			Domain domain);

	/**
	 * chb
	 * 按照Csr回收
	 * @return 回收数目 
	 */
	public Integer recycleByCsr(MarketingProject marketingProject,
			User user, Domain domain);
	/**
	 * chb
	 * 回收全部
	 * @return 回收数目
	 */
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
	public void deleteTaskByResource(CustomerResource resource,
			MarketingProject project);
	
	/**
	 * chb
	 * 根据项目清除所有未完成资源，实际已经隐含域了
	 * @param marketingProject
	 * @param domain
	 */
	public void clearAllUnfinished(MarketingProject marketingProject, Domain domain);

	/**
	 * chb
	 * 按照项目和批次回收指定项目的指定批次资源
	 * @param project
	 * @param batch
	 * @param domain
	 * @return
	 */
	public Long recycleByBatches(MarketingProject project,
			List<CustomerResourceBatch> batches, Domain domain);
	/**
	 * chb
	 * 按照项目和批次回收指定项目的指定批次资源
	 * @param project
	 * @param batch
	 * @param domain
	 * @return
	 */
	public Long recycleToBatches(MarketingProject project,
			List<CustomerResourceBatch> batches, Domain domain,Long toBatchId);

	/**
	 * chb
	 * 执行原生Sql的插入操作
	 * @return
	 */
	public Object executeNativeSql(String nativeSql);
	
	/**
	 * chb
	 * 
	 * 由于追加批次时很难确定是不是批次追加到批次中的资源已经与项目有对应关系
	 * 如果是新资源，一定没有对应关系，如果是旧资源，可能包含旧资源的多个批次已经添加到某个项目
	 * @param customerResource
	 * @param projectId
	 * @param workflowTransferLogId 	工作流迁移日志的编号[山西焦炭]	jrh 2013-07-30
	 * @param domainId
	 */
	@Transactional  //此处添加事物，解决一个管理员多处登陆，往项目中添加同一个批次产生多条Task的问题，这样依然有添加重复资源的风险，但风险极低，所以没有添加同步控制（为了性能和多用户良好体验）
	public Boolean checkThenAddTask(CustomerResource customerResource, Long batchId,Boolean isValidTask,MarketingProject project, Long domainId);
	
	/**
	 * chb
	 * 对资源进行迁移操作(去重)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	public void migrateResourceDelete(MarketingProject projectFrom,
			MarketingProject projectTo);
	/**
	 * chb
	 * 对资源进行迁移操作(迁移)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
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
	public List<MarketingProjectTask> getStepNotDistributedProjectTaskByMinId(
			Long projectId, Long cursor,Long step);
}
