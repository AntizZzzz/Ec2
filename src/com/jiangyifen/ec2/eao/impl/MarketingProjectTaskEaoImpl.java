package com.jiangyifen.ec2.eao.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.eao.MarketingProjectTaskEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkflowTransferLog;
import com.jiangyifen.ec2.entity.enumtype.CustomerQuestionnaireFinishStatus;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectType;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.utils.ShanXiJiaoTanWorkOrderConfig;

public class MarketingProjectTaskEaoImpl extends BaseEaoImpl implements
		MarketingProjectTaskEao {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * chb 根据项目取得所有任务
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProjectTask> getTaskByProject(
			MarketingProject marketingProject, Domain domain) {
		return getEntityManager().createQuery(
				"select mpt from MarketingProjectTask as mpt where mpt.marketingProject.id="
						+ marketingProject.getId() + " and mpt.domain="
						+ domain.getId()).getResultList();
	}

	/**
	 * chb 根据项目取得可以分配的任务集合
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProjectTask> getAssignableTaskByProject(
			MarketingProject marketingProject,
			List<CustomerResourceBatch> batches, Domain domain) {
		List<MarketingProjectTask> taskList = new ArrayList<MarketingProjectTask>();
		// 处理的到按照批次Id的一个 eg：（1,2,3,4）
		StringBuilder sbBatch = new StringBuilder();
		sbBatch.append('(');
		Iterator<CustomerResourceBatch> batchIter = batches.iterator();
		while (batchIter.hasNext()) {
			CustomerResourceBatch batch = batchIter.next();
			sbBatch.append(batch.getId());
			if (batchIter.hasNext())
				sbBatch.append(",");
		}
		sbBatch.append(')');
		String inSql = sbBatch.toString();

		// =============================================================//
		Long recordId = Long.MAX_VALUE;
		int step = 1000;
		// 每次加载step步长条数据
		List<Long> records = null;
		for (;;) {
			// batchId 隐含 domain 概念，不使用distinct语句可以节省性能
			String nativeSql = "SELECT customerresources_id FROM ec2_customer_resource_ec2_customer_resource_batch where customerresources_id<"
					+ recordId
					+ " and customerresourcebatches_id in "
					+ inSql
					+ " order by customerresources_id desc";
			records = (List<Long>) getEntityManager()
					.createNativeQuery(nativeSql).setFirstResult(0)
					.setMaxResults(step).getResultList();
			if (records.size() > 0) {
				// 取得最小的Id值
				recordId = records.get(records.size() - 1);
			} else {
				break;
			}
			// 对于数据进行处理,此时records.size>0
			StringBuilder sb = new StringBuilder();
			sb.append('(');
			Iterator<Long> recordsIter = records.iterator();
			while (recordsIter.hasNext()) {
				Long id = recordsIter.next();
				sb.append(id);
				if (recordsIter.hasNext())
					sb.append(",");
			}
			sb.append(')');
			String inTaskSql = sb.toString();
			// Task的customerresources_id如果在通过批次查处的集合中，将Task存储,有缘nativeSql中已经有distict关键字，则取出的resource不重复-->Task不重复
			String taskSql = "select mpt from MarketingProjectTask mpt where mpt.user is null and mpt.marketingProject.id="
					+ marketingProject.getId()
					+ " and mpt.customerResource.id in" + inTaskSql;
			// 最多取出的Task是1000条
			List<MarketingProjectTask> partTaskList = (List<MarketingProjectTask>) getEntityManager()
					.createQuery(taskSql).getResultList();
			taskList.addAll(partTaskList);
			// =============================================================//
		}
		return taskList;
	}

	/**
	 * chb 根据项目取得任务数总数
	 * 
	 */
	@Override
	public Long getTotalNum(MarketingProject marketingProject, Domain domain) {
		String nativeSql = "select count(*) from ec2_marketing_project_task where marketingproject_id="
				+ marketingProject.getId()
				+ " and "
				+ "domain_id="
				+ domain.getId();
		Long count = (Long) getEntityManager().createNativeQuery(nativeSql)
				.getSingleResult();
		return count;
	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public List<MarketingProject> getProjectInTaskByUser(User user) {
//		return getEntityManager()
//				.createQuery(
//						"select distinct mpt.marketingProject from MarketingProjectTask as mpt where mpt.user.id = "
//								+ user.getId() + " order by mpt.id asc")
//								.getResultList();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public List<MarketingProject> getProjectInTaskByUser(User user,
//			MarketingProjectStatus projectStatus) {
//		if (projectStatus != null) {
//			String statusStr = getProjectStatuSql(projectStatus);
//			return getEntityManager()
//					.createQuery(
//							"select distinct mpt.marketingProject from MarketingProjectTask as mpt where mpt.user.id = "
//									+ user.getId()
//									+ " and mpt.marketingProject.marketingProjectStatus = "
//									+ statusStr).getResultList();
//		}
//		return getEntityManager()
//				.createQuery(
//						"select distinct mpt.marketingProject from MarketingProjectTask as mpt where mpt.user.id = "
//								+ user.getId() + " order by mpt.id asc")
//				.getResultList();
//	}
//
	@Override
	public Long getTaskCountByUser(User user,
			MarketingProjectStatus projectStatu) {
		String countSql = "select count(mpt) from MarketingProjectTask as mpt where mpt.user.id = "
				+ user.getId();
		if (projectStatu != null) {
			String statuStr = getProjectStatuSql(projectStatu);
			countSql += " and mpt.marketingProject.marketingProjectStatus = "
					+ statuStr;
		}
		Long count = (Long) getEntityManager().createQuery(countSql)
				.getSingleResult();
		return count;
	}

	/**
	 * jrh 根据项目状态对象，得到用于查询项目状态的字符串语句
	 * 
	 * @param projectStatus
	 *            项目状态
	 * @return
	 */
	private String getProjectStatuSql(MarketingProjectStatus projectStatus) {
		String statuStr = projectStatus.getClass().getName() + ".";
		if (projectStatus.getIndex() == 0) {
			statuStr += "NEW";
		} else if (projectStatus.getIndex() == 1) {
			statuStr += "RUNNING";
		} else if (projectStatus.getIndex() == 2) {
			statuStr += "OVER";
		}
		return statuStr;
	}

	/**
	 * chb 根据项目、域、用户、取得对应的任务数
	 * 
	 * @param user
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	@Override
	public Long getTaskCountByUser(User user,
			MarketingProject marketingProject, Domain domain) {
		String nativeSql = "select count(*) from ec2_marketing_project_task where user_id="
				+ user.getId()
				+ " and isfinished=false"
				+ " and marketingproject_id="
				+ marketingProject.getId() + " and domain_id=" + domain.getId();
		Long userTaskNum = (Long) getEntityManager().createNativeQuery(
				nativeSql).getSingleResult();
		return userTaskNum;
	}

	/**
	 * chb 根据项目取得对用的工号集合
	 * 
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getEmpNoListByProject(
			MarketingProject marketingProject, Domain domain) {
		List<String> empList = getEntityManager()
				.createQuery(
						"select mpt.user.empNo from MarketingProjectTask as mpt where mpt.marketingProject.id="
								+ marketingProject.getId()
								+ " and mpt.domain.id="
								+ domain.getId()
								+ " group by mpt.user.empNo").getResultList();
		return empList;
	}

	/**
	 * chb 按照Csr回收
	 * 
	 * @return 回收数目
	 */
	@Override
	public Integer recycleByCsr(MarketingProject marketingProject, User user,
			final Domain domain) {
		// 此处注释的为性能比较低下的版本
		// String sql="select mpt from MarketingProjectTask mpt" +
		// " where mpt.isFinished=null and mpt.user.id='"+user.getId()+"' and mpt.marketingProject.id="
		// + marketingProject.getId()
		// + " and mpt.domain.id="
		// + domain.getId();
		// List<MarketingProjectTask>
		// taskList=getEntityManager().createQuery(sql).getResultList();
		// recycle(taskList);
		// return taskList.size();
		String sql = "select count(mpt) from MarketingProjectTask mpt"
				+ " where mpt.isFinished=false and mpt.user.id='"
				+ user.getId() + "' and mpt.marketingProject.id="
				+ marketingProject.getId() + " and mpt.domain.id="
				+ domain.getId();
		Long longNum = (Long) getEntityManager().createQuery(sql)
				.getSingleResult();

		getEntityManager()
				.createQuery(
						"update  MarketingProjectTask mpt set mpt.user=null,mpt.isAnswered=false,mpt.isFinished=false,mpt.lastStatus=null "
								+ "where mpt.isFinished=false and mpt.user.id="
								+ user.getId()
								+ " and mpt.marketingProject.id="
								+ marketingProject.getId()
								+ " and mpt.domain.id=" + domain.getId())
				.executeUpdate();
		return longNum.intValue();
	}

	/**
	 * chb 回收全部
	 * 
	 * @return 回收数目
	 */
	@Override
	public Integer recycleAll(final MarketingProject marketingProject,
			final Domain domain) {
		return getEntityManager()
				.createQuery(
						"update  MarketingProjectTask mpt set mpt.user=null,mpt.isAnswered=false,mpt.isFinished=false,mpt.lastStatus=null "
								+ " where mpt.isFinished=false and mpt.user is not null"
								+ " and mpt.marketingProject.id="
								+ marketingProject.getId()
								+ " and mpt.domain.id=" + domain.getId())
				.executeUpdate();
	}

	/**
	 * chb 计算已经分配到任务中的Csr数目(Csr必须在任务记录中才算数)
	 * 
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	@Override
	public int getCsrCountByProject(MarketingProject marketingProject,
			Domain domain) {
		Long count = (Long) getEntityManager()
				.createQuery(
						"select count(distinct mpt.user) from MarketingProjectTask as mpt where mpt.marketingProject.id="
								+ marketingProject.getId()
								+ " and mpt.user is not null and mpt.domain.id="
								+ domain.getId()).getSingleResult();
		return count.intValue();
	}

	/**
	 * chb 根据项目取得含有未完资源的Csr集合
	 * 
	 * @param project
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getNotFinishedCsrsByProject(MarketingProject project) {
		return getEntityManager().createQuery(
				"select distinct e.user " + "from MarketingProjectTask e "
						+ "where e.isFinished=false and "
						+ "e.user is not null and " + "e.marketingProject.id="
						+ project.getId() + " order by e.id").getResultList();
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
		getEntityManager().createQuery(
				"delete " + "from MarketingProjectTask mpt "
						+ "where mpt.customerResource.id=" + resource.getId()
						+ " and " + "mpt.marketingProject.id="
						+ project.getId()).executeUpdate();
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
		getEntityManager().createQuery(
				"delete " + "from MarketingProjectTask mpt "
						+ "where mpt.marketingProject.id="
						+ marketingProject.getId() + " and " + "mpt.domain.id="
						+ domain.getId() + " and mpt.user=null")
				.executeUpdate();
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
		Long totalRecycleCount = 0L;
		// 处理的到按照批次Id的一个 eg：（1,2,3,4）
		StringBuilder sbBatch = new StringBuilder();
		sbBatch.append('(');
		Iterator<CustomerResourceBatch> batchIter = batches.iterator();
		while (batchIter.hasNext()) {
			CustomerResourceBatch batch = batchIter.next();
			sbBatch.append(batch.getId());
			if (batchIter.hasNext())
				sbBatch.append(",");
		}
		sbBatch.append(')');
		String inSql = sbBatch.toString();

		// =============================================================//
		String nativeCountSql = "select count(*) from ec2_marketing_project_task where user_id is not null and isfinished=false and "
				+ "marketingproject_id="
				+ project.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " "
				+ "and customerresource_id in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where "
				+ "customerresourcebatches_id in" + inSql + ")";

		String nativeRecycleSql = "update ec2_marketing_project_task set distributetime=null,"
				+ "isanswered=false,isfinished=false,laststatus=null,lastupdatedate=null,user_id=null "
				+ "where marketingproject_id="
				+ project.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " "
				+ " and isfinished=false and user_id is not null and "
				+ "customerresource_id in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
				+ "where customerresourcebatches_id in" + inSql + ")";

		// 在回收之前进行计数操作
		totalRecycleCount = (Long) getEntityManager().createNativeQuery(
				nativeCountSql).getSingleResult();
		// 进行回收操作
		getEntityManager().createNativeQuery(nativeRecycleSql).executeUpdate();

		return totalRecycleCount;
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
		Long totalRecycleCount = 0L;
		// 处理的到按照批次Id的一个 eg：（1,2,3,4）
		StringBuilder sbBatch = new StringBuilder();
		sbBatch.append('(');
		Iterator<CustomerResourceBatch> batchIter = batches.iterator();
		while (batchIter.hasNext()) {
			CustomerResourceBatch batch = batchIter.next();
			sbBatch.append(batch.getId());
			if (batchIter.hasNext())
				sbBatch.append(",");
		}
		sbBatch.append(')');
		String inSql = sbBatch.toString();
		
		// =============================================================//
		String nativeCountSql = "select count(*) from ec2_marketing_project_task where isfinished=false and "
				+ "marketingproject_id="
				+ project.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " "
				+ "and customerresource_id in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where "
				+ "customerresourcebatches_id in" + inSql + ")";
		
		
		String nativeRecycleSql = "insert into ec2_customer_resource_ec2_customer_resource_batch(customerresources_id,customerresourcebatches_id) (select customerresource_id,"+toBatchId
				+ " from ec2_marketing_project_task where marketingproject_id="
				+ project.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " "
				+ " and isfinished=false and "
				+ "customerresource_id in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
				+ "where customerresourcebatches_id in" + inSql + "))";
		
		String nativeCleanSql = "delete from ec2_marketing_project_task where marketingproject_id="
				+ project.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " "
				+ " and isfinished=false and "
				+ "customerresource_id in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
				+ "where customerresourcebatches_id in" + inSql + ")";
		
		// 在回收之前进行计数操作
		totalRecycleCount = (Long) getEntityManager().createNativeQuery(
				nativeCountSql).getSingleResult();
		// 进行回收操作
		getEntityManager().createNativeQuery(nativeRecycleSql).executeUpdate();
		
		//执行清理操作
		getEntityManager().createNativeQuery(nativeCleanSql).executeUpdate();
		
		return totalRecycleCount;
	}

	/**
	 * chb 执行原生Sql的插入操作
	 * 
	 * @return
	 */
	@Override
	public Object executeNativeSql(String nativeSql) {
		return getEntityManager().createNativeQuery(nativeSql).executeUpdate();
	}

	/**
	 * chb 检查Task表，看Task表是否有通过资源，项目和域确定的唯一条目，如果有则不插入数据，如果没有则插入新数据
	 * 由于追加批次时很难确定是不是批次追加到批次中的资源已经与项目有对应关系
	 * 如果是新资源，一定没有对应关系，如果是旧资源，可能包含旧资源的多个批次已经添加到某个项目
	 * 
	 * @param customerResourceId
	 * @param project
	 * @param domainId
	 * @return 返回True表示存储成功，返回false表示已经存在，没有重新存储
	 */
	@Override
	public Boolean checkThenAddTask(CustomerResource customerResource, Long batchId, Boolean isValidTask, MarketingProject project, 
			Long domainId) {
		Long projectId = project.getId();
		Long customerResourceId = customerResource.getId();
		
		// 任务的类型跟项目类型相同
		MarketingProjectType type = project.getMarketingProjectType();
		int typeIndex = 0;
		if(type != null) {
			typeIndex = type.getIndex();
		}
		String nativeCountSql = "select count(*) from ec2_marketing_project_task "
				+ "where customerresource_id=" + customerResourceId + " and marketingproject_id="
				+ projectId + " and marketingprojecttasktype = "+typeIndex+" and domain_id="	+ domainId + "";
		
		// 任务数量
		Long count = (Long) getEntityManager().createNativeQuery(nativeCountSql).getSingleResult();

		// jrh 此处应该保证一条资源在通一个项目中最多出现一条任务
		if (count >= 1) {
			// 说明已经存储一个相应的Task，不用重复存储
			logger.info("项目Id为 "+projectId+" 的项目中已经包含id 值为 "+customerResourceId+" 的资源所对应的任务！");
			return false;
		} else {
			Long workflowTransferLogId = createWorkflowTransferLog2JiaoTan(customerResource, project);	// jrh 山西焦炭
			
			// 任务的Id
			String sequenceSql = "select nextval('seq_ec2_marketing_project_task_id')";
			Long taskId = (Long) getEntityManager().createNativeQuery(sequenceSql).getSingleResult();
			//分配不需要时间，指派给CSR时才需要时间
			// jrh 如果当前项目是问卷类型，则插入问卷任务完成状态字段的值
			String questionnaireKeySql = "";
			String questionnaireStatusSql = "";
			if(typeIndex == MarketingProjectType.QUESTIONNAIRE.getIndex()) {
				questionnaireKeySql = ", customerquestionnairefinishstatus";
				questionnaireStatusSql = ", "+CustomerQuestionnaireFinishStatus.UN_STARTED.getIndex();
			}
			// jrh 添加任务创建时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
			//TODO chb 改为StringBuilder
			String nativeInsertSql = "INSERT INTO ec2_marketing_project_task( "
					+ "id, createtime,isanswered, isfinished,isuseable, "
					+ "customerresource_id,batchid, marketingproject_id, marketingprojecttasktype"+questionnaireKeySql+", workflowtransferlogid, domain_id) "
					+ "VALUES (" + taskId + ", '"+sdf.format(new Date())+"',false,false,"+isValidTask+", "
					+ customerResourceId + ","  + batchId+"," + projectId +"," + typeIndex + questionnaireStatusSql + "," +workflowTransferLogId+ "," + domainId +") ";
			try {
				getEntityManager().createNativeQuery(nativeInsertSql).executeUpdate();
			} catch (Exception e) {
				logger.info("项目中已经包含此条资源！");
				return false;
			}
		}
		return true;
	}

	/**
	 * jrh 为山西焦炭而作，检查信息，并判断创建迁移日志	2013-07-30
	 * @param customerResource	资源
	 * @param marketingProject	项目
	 * @return
	 */
	@Transactional
	private Long createWorkflowTransferLog2JiaoTan(CustomerResource customerResource, MarketingProject marketingProject) {
		if(customerResource == null) {
			return null;
		}
		
		Long projectId = marketingProject.getId();
		String shanXiJiaoTanPcMac = ShanXiJiaoTanWorkOrderConfig.props.getProperty(ShanXiJiaoTanWorkOrderConfig.SANXIJIAOTAN_PC_MAC);
		boolean isForShanXiJiaoTan = (shanXiJiaoTanPcMac != null && GlobalData.MAC_ADDRESS.equals(shanXiJiaoTanPcMac)) ? true : false;
		String workflowToJiaoTan = "";		// 当前处理的项目属于第几工作流
		Long workflowTransferLogId = null;	// 工作流日志编号
		
		// 首先判断是否是山西焦炭项目的配置，如果是，则按山西焦炭的配置文件走
		if(isForShanXiJiaoTan) {
			Long workOrder1ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER1_PROJECT_ID));
			Long workOrder2ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER2_PROJECT_ID));
			Long workOrder3ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER3_PROJECT_ID));
			if(projectId.equals(workOrder1ProjectId)) {
				workflowToJiaoTan = "workflow1";
			} else if(projectId.equals(workOrder2ProjectId)) {
				workflowToJiaoTan = "workflow2";
			} else if(projectId.equals(workOrder3ProjectId)) {
				workflowToJiaoTan = "workflow3";
			}
			workflowTransferLogId = updateWorkflowTransferLog2JiaoTan(marketingProject, workflowToJiaoTan, customerResource);
		}
		return workflowTransferLogId;
	}


	/**
	 * jrh 为山西焦炭创建工作流迁移日志	2013-07-30
	 * @param marketingProject 		 	 项目
	 * @param workflowToJiaoTan			当前是山西焦炭的第几个工作流
	 * @param customerResource			当前创建任务对应的资源
	 * @return
	 */
	private Long updateWorkflowTransferLog2JiaoTan(MarketingProject marketingProject, String workflowToJiaoTan, CustomerResource customerResource) {
		Date now = new Date();
		Long projectId = marketingProject.getId();

		WorkflowTransferLog transferLog = new WorkflowTransferLog();
		transferLog.setCustomerName(customerResource.getName());
		transferLog.setCustomerResourceId(customerResource.getId());
		String phoneNo = "";
		for(Telephone telephone : customerResource.getTelephones()) {
			phoneNo = telephone.getNumber();
			break;
		}
		transferLog.setPhoneNo(phoneNo);
		
		User importor = customerResource.getOwner();
		if(importor != null) {
			transferLog.setImportorId(importor.getId());
			transferLog.setImportorEmpNo(importor.getEmpNo());
			
			Department importorDept = importor.getDepartment();
			transferLog.setImportorDeptId(importorDept.getId());
			transferLog.setImportorDeptName(importorDept.getName());
		}
		
		transferLog.setImportCustomerTime(customerResource.getImportDate());
		transferLog.setDomainId(customerResource.getDomain().getId());
		
		if(workflowToJiaoTan.equals("workflow1")) {
			transferLog.setMigrateWorkflow1Time(now);
			transferLog.setWorkflow1ProjectId(projectId);
			transferLog.setWorkflow1ProjectName(marketingProject.getProjectName());
		} else if(workflowToJiaoTan.equals("workflow2")) {
			transferLog.setMigrateWorkflow2Time(now);
			transferLog.setWorkflow2ProjectId(projectId);
			transferLog.setWorkflow2ProjectName(marketingProject.getProjectName());
		} else if(workflowToJiaoTan.equals("workflow3")) {
			transferLog.setMigrateWorkflow3Time(now);
			transferLog.setWorkflow3ProjectId(projectId);
			transferLog.setWorkflow3ProjectName(marketingProject.getProjectName());
		}
		this.getEntityManager().persist(transferLog);
		return transferLog.getId();
	}
	
	/**
	 * chb
	 * 对资源进行迁移操作(去重)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	@Override
	public void migrateResourceDelete(MarketingProject projectFrom,
			MarketingProject projectTo) {//项目的Id隐含有域的概念
		//删除原项目中与目标项目资源Id相同的任务,此Sql语句只看isFinshed字段
		//条件:未完成、原项目、资源Id出现在目标项目(目标项目不去识别是否完成)
		String deleteNativeSql="delete from ec2_marketing_project_task where isfinished=false " +
				"and marketingproject_id="+projectFrom.getId()+" and customerresource_id " +
				"in(select customerresource_id from ec2_marketing_project_task where marketingproject_id="+projectTo.getId()+")";
		getEntityManager().createNativeQuery(deleteNativeSql).executeUpdate();
	}
	/**
	 * chb
	 * 对资源进行迁移操作(迁移)
	 * @param projectFrom 从哪个项目往外迁移
	 * @param projectTo 迁移到哪个项目
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void migrateResourceMigrate(MarketingProject projectFrom,
			MarketingProject projectTo) {//项目的Id隐含有域的概念

		//从Task中找出推出所有资源要转移项目的未完成资源的应该有的批次
		//在中间表中查询批次（项目所有未完成资源），通过GroupBy来得到所有的批次
		String batchesSql="select customerresourcebatches_id from ec2_customer_resource_ec2_customer_resource_batch "+ 
		"where customerresources_id in "+
		"(select customerresource_id from ec2_marketing_project_task where isfinished=false and marketingproject_id="+projectFrom.getId()+") "+
		"group by customerresourcebatches_id"; 
		List<Long> batchIdsList=getEntityManager().createNativeQuery(batchesSql).getResultList();
		
		//所有查找出来的批次和从项目中的批次的交集，即出现在查出来的批次中也出现在从项目中的批次
		List<Long> bothBatchIds=new ArrayList<Long>();
		Set<CustomerResourceBatch> fromBatches = projectFrom.getBatches();
		for(CustomerResourceBatch batch:fromBatches){
			if(batchIdsList.contains(batch.getId())){
				bothBatchIds.add(batch.getId());
			}
		}
		
		//将批次和要转移到的项目相关联
		Set<CustomerResourceBatch> projectToBatches = projectTo.getBatches();
		for(Long batchId:bothBatchIds){
			CustomerResourceBatch fakeBatch = new CustomerResourceBatch();
			fakeBatch.setId(batchId);
			projectToBatches.add(fakeBatch);
		}
		//存储到批次
		getEntityManager().merge(projectTo);
		
		//标记源项目的对应的Task中的项目Id为目标项目的Id
		//由于已经对重复的资源进行了删除，对于剩余的资源直接进行更新操作即可,域不变,资源不变
		String updateNativeSql="update ec2_marketing_project_task set distributetime=null,"
				+ " isanswered=false,isfinished=false,laststatus=null,lastupdatedate=null,user_id=null,marketingproject_id="+projectTo.getId()+
				"where isfinished=false and marketingproject_id="+projectFrom.getId();
		getEntityManager().createNativeQuery(updateNativeSql).executeUpdate();
		
		// jrh TODO
		String fromTaskCustomerSql = "select customerresource_id from ec2_marketing_project_task where isfinished=true and marketingproject_id="+projectFrom.getId();
		List<Long> fromTaskCustomerIdsList = getEntityManager().createNativeQuery(fromTaskCustomerSql).getResultList();
		// jrh 需要删除的项目与批次的对应关系中 批次对象
		List<Long> removeP2B = new ArrayList<Long>();
		if(fromTaskCustomerIdsList.size() > 0) {
			for(CustomerResourceBatch batch : fromBatches) {
				String batchSql = "select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id = " +batch.getId();
				List<Long> customerIdsInBatch = getEntityManager().createNativeQuery(batchSql).getResultList();
				boolean isExisted = false;
				for(Long customerId : fromTaskCustomerIdsList) {
					if(customerIdsInBatch.contains(customerId)) {
						isExisted = true;	break;
					}
				}
				if(isExisted == false) {
					removeP2B.add(batch.getId());
				}
			} 
		} else {
			for(CustomerResourceBatch batch : fromBatches) {
				removeP2B.add(batch.getId());
			}
		}

		// jrh 如果存在需要删除的项目与批次对应的中间对象，则执行删除操作
		if(removeP2B.size() > 0) {
			String deleteP2BatchIds = "";
			for(Long batchId : removeP2B) {
				deleteP2BatchIds = deleteP2BatchIds +batchId+ ",";
			}
			if(deleteP2BatchIds.endsWith(",")) {
				deleteP2BatchIds = deleteP2BatchIds.substring(0, deleteP2BatchIds.length() - 1);
			}
			String deleteP2BSql = "delete from ec2_markering_project_ec2_customer_resource_batch where marketingproject_id = " +projectFrom.getId()+ " and batches_id in(" +deleteP2BatchIds+ ")";
			getEntityManager().createNativeQuery(deleteP2BSql).executeUpdate();
		
			// jrh 更新项目对象，清理缓存
			Set<CustomerResourceBatch> remainedSet = new HashSet<CustomerResourceBatch>();
			for(CustomerResourceBatch projectToBatche : projectToBatches) {
				if(!removeP2B.contains(projectToBatche.getId())) {
					remainedSet.add(projectToBatche);
				}
			}
			projectFrom.setBatches(remainedSet);
			getEntityManager().merge(projectFrom);
		}
	}
	
	/**
	 * chb
	 * 根据资源的Id值取出把该资源当任务的CSR
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public User getCsrByCustomerResourceId(Long resourceId,Long projectId) {
		String sql="select mpt from MarketingProjectTask mpt where mpt.customerResource.id="+resourceId+" and mpt.marketingProject.id="+projectId;
		List<MarketingProjectTask> tasks=getEntityManager().createQuery(sql).getResultList();
		if(tasks.size()!=0){
			return tasks.get(0).getUser();//如果任务并没有分配，则返回空
		}
		//如果没有查找到任务则返回空
		return null;
	}

	
	/**
	 * chb 自动外呼获取资源使用
	 * 取得制定项目的Task，并且限定取出的Task的Id值大于cursor
	 * @param projectId
	 * @param cursor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public  List<MarketingProjectTask> getStepNotDistributedProjectTaskByMinId(
			Long projectId, Long cursor,Long step) {
		//用户Id为null，项目Id为projectId，Id大于Cursor ,小到大排序
		String sql="select mpt from MarketingProjectTask mpt where mpt.user=null and mpt.marketingProject.id="+projectId+" and mpt.id>"+cursor+" order by mpt.id";
		EntityManager e = getEntityManager();
		List<MarketingProjectTask> tasks;
		synchronized(e){
			Query q = e.createQuery(sql);
			Query q1 = q.setFirstResult(0).setMaxResults(step.intValue());
			tasks=q1.getResultList();
		}
		return tasks;
	}
}
