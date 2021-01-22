package com.jiangyifen.ec2.service.mgr.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.eao.MarketingProjectEao;
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
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.service.mgr.DistributeToTaskService;
import com.jiangyifen.ec2.utils.ShanXiJiaoTanWorkOrderConfig;

public class DistributeToTaskServiceImpl implements DistributeToTaskService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private MarketingProjectTaskEao marketingProjectTaskEao;
	private MarketingProjectEao marketingProjectEao;
	private CommonEao commonEao;

	/**
	 * chb 验证此项目的Csr和Resource是否符合条件
	 */
	@Override
	public String validateCsrAndResource(MarketingProject target,
			List<CustomerResourceBatch> batches, Domain domain) {
		if (target.getUsers().isEmpty()) {
			return "您还没有指派Csr，请先指派Csr再分配任务";
		}
		List<MarketingProjectTask> tasks = marketingProjectTaskEao
				.getAssignableTaskByProject(target, batches, domain);
		if (tasks.isEmpty()) {
			return "当前项目" + target.getProjectName() + "没有资源，或没有可用资源";
		}
		if (target.getUsers().size() > tasks.size()) {
			return "项目" + target.getProjectName() + "的用户数大于资源数，请重新分配！";
		}
		return "pass";
	}

	/**
	 * chb 如果资源足够，则将Csr任务数不到最大值（max）的任务分配到最大值，如果资源不充足，则将资源平均分配给任务最少的几个Csr，
	 * 余数按资源数相对多的优先分配，分配数据不能出现异常，所以没有进行异常捕捉
	 * 
	 * @param marketingProject
	 *            项目
	 * @param max
	 *            每个Csr能接收资源的最大值
	 * @param domain
	 *            域
	 * @return 是否分配成功
	 */
	@Override
	public Long distribute(MarketingProject marketingProject,
			List<CustomerResourceBatch> batches, Set<User> users,
			Long distributeNumLine, Domain domain) {
		//清空Task的缓存
//		commonEao.getEntityManager().getEntityManagerFactory().getCache().evict(MarketingProjectTask.class);
		
		// 如果输入上限超过1000，则分配加载资源并分配出现异常！如有需求请调整分批加载步长
		//---------------------------------------
//		Long loadTaskStep = distributeNumLine;
//		jrh 一批资源可能是好几万，而执行的人可能只有几个，所以任务的分配数量很可能大于1000
//		Long loadTaskStep = 1000L;
//		if (distributeNumLine > 1000L) {
//			throw new RuntimeException("输入数值过大！分配上限不能超过1000条");
//		}
		
		Long startTime1=System.currentTimeMillis();
		// 获取用户与其已拥有任务的数量的对应关系
		HashMap<User, Long> userNumMap = wrapToMap(users, marketingProject, domain);
		Long endTime1=System.currentTimeMillis();
		logger.info("用户包装到Map耗时:"+(endTime1-startTime1)/1000+"秒");
		
		// 根据用户目前含有的资源数对用户进行排序，资源最少的排在前面
		List<User> soredUser = getSortedUser(userNumMap);
		
		// 对应用户现在有的已分配资源数量,由小到大
		List<Long> numList = getSortedNum(userNumMap);
		
		// 按照numList由大到小的排序
		List<Long> numListReverse = new ArrayList<Long>(numList);
		
		Collections.reverse(numListReverse);
		
		// 计算所有可用资源数
		Long startTime2=System.currentTimeMillis();
		Long assignableTaskNum = caculateAssignableTaskNum(marketingProject, batches, domain);
		Long endTime2=System.currentTimeMillis();
		logger.info("计算可用资源数量耗时:"+(endTime2-startTime2)/1000+"秒");
		
		Long startTime3=System.currentTimeMillis();
		// 得出应该分配的数量（由大到小的排序）
		List<Long> toDistributeNums = caculateToDistributeNum(numListReverse,
				assignableTaskNum, distributeNumLine);
		Long endTime3=System.currentTimeMillis();
		logger.info("计算应该分配资源数量耗时:"+(endTime3-startTime3)/1000+"秒");
		
		// 允许分配的资源的集合
		List<MarketingProjectTask> assignableTasks = new ArrayList<MarketingProjectTask>();

		// 以应该分配的数量为标准对用户进行资源分配
		// 如果用户数少于要分配的数量，抛出异常
		if (soredUser.size() < toDistributeNums.size())
			throw new RuntimeException("用户总数量不应该小于要分配资源的用户数量！");
		//只有调用一次分配方法时初始化一次，在distribute方法范围的一个局部变量
		Long minId=0L;//只查询Id值大于minId的Task
		Long startTime4=System.currentTimeMillis();
		for (int i = 0; i < toDistributeNums.size(); i++) {
			//为了节省查询数据库的加载资源，记录一下加载Task的Id值
			
//jrh 注释 可分配的数量即使比需要分配的数量大，也没关系，而且更能保证顺利分配完
				Long startTimeInner=System.currentTimeMillis();
				// jrh 加载 loadTaskStep 条Task,然后放入可指派任务中 == 需要分配多少条，我就加载多少
				List<MarketingProjectTask> stepTasks = loadStepTasks(marketingProject, batches,
						toDistributeNums.get(i), minId, domain);
				Long endTimeInner=System.currentTimeMillis();
				logger.info("加载资源一批资源耗时:"+(endTimeInner-startTimeInner)/1000+"秒");

				assignableTasks.addAll(stepTasks);
				// 对于起始查询Id号进行更新，以不重复查询Task
				if(stepTasks.size() > 0) {
					MarketingProjectTask task = stepTasks.get(stepTasks.size() - 1);
					if(task.getId()>minId) minId=task.getId();	// 因为jrh 已经
				}
			// 取得i对应的用户
			User user = soredUser.get(i);
			((DistributeToTaskService) AopContext.currentProxy()).saveOneUserTask(user, toDistributeNums.get(i), assignableTasks);
		}
		Long endTime4=System.currentTimeMillis();
		logger.info("加载资源并分配耗时:"+(endTime4-startTime4)/1000+"秒");

		// 实际分配的数量
		Long allDistributeNum = 0L;
		for (Long num : toDistributeNums) {
			allDistributeNum += num;
		}
		return allDistributeNum;
	}
	
	/**
	 * jrh 
	 * 	给指定的Csr 分配新的任务
	 * @param marketingProject 		 	 项目
	 * @param maxTotalPickTaskCount 	当前话务员当前最多还能获取的任务条数
	 * @param user					   	需要获取新任务的话务员
	 * @param domain					指定的域对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProjectTask> distributeByCsr(MarketingProject marketingProject, int maxTotalPickTaskCount, User user, Domain domain) {
		Long projectId = marketingProject.getId();
		
		// 默认一次获取的任务总数为1条
		Integer maxOncePickCount = marketingProject.getCsrOnceMaxPickTaskCount();
		if(maxOncePickCount == 0) {
			throw new RuntimeException("当前项目【"+marketingProject.getProjectName()+"】的坐席获取任务功能已被禁止，如果需要，请让管理员开启！");
		}
		
		String shanXiJiaoTanPcMac = ShanXiJiaoTanWorkOrderConfig.props.getProperty(ShanXiJiaoTanWorkOrderConfig.SANXIJIAOTAN_PC_MAC);
		boolean isForShanXiJiaoTan = (shanXiJiaoTanPcMac != null && GlobalData.MAC_ADDRESS.equals(shanXiJiaoTanPcMac)) ? true : false;
		String workflow2JiaoTan = "";
		
		// 首先判断是否是山西焦炭项目的配置，如果是，则按山西焦炭的配置文件走
		if(isForShanXiJiaoTan) {
			Long workOrder1ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER1_PROJECT_ID));
			Long workOrder2ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER2_PROJECT_ID));
			Long workOrder3ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER3_PROJECT_ID));
			if(projectId.equals(workOrder1ProjectId)) {
				workflow2JiaoTan = "workflow1";
			} else if(projectId.equals(workOrder2ProjectId)) {
				workflow2JiaoTan = "workflow2";
			} else if(projectId.equals(workOrder3ProjectId)) {
				workflow2JiaoTan = "workflow3";
			}
		}
		
		if(maxOncePickCount == null) {	// 一般不会出现，保险起见
			maxOncePickCount = 10;
		}
		
		// 如果当前话务员一次获取的任务数大于目前可以获取的最大数量，则将其赋值为 maxTotalPickTaskCount
		if(maxOncePickCount > maxTotalPickTaskCount) {
			maxOncePickCount = maxTotalPickTaskCount;
		}
		
		// 将当前时间往后延一个小时获得 maxOrderTime，用于获取预约时间小于maxOrderTime 的任务
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, +1);
		String maxOrderTime = sdf.format(cal.getTime());

		// 首先按预约时间进行查询, 时间满足 a、预约时间最小  b、比下一小时的当前时刻要小 maxOrderTime
		List<MarketingProjectTask> tasks = new ArrayList<MarketingProjectTask>();
		String sql1 = "select mpt from MarketingProjectTask as mpt where mpt.marketingProject.id = " + marketingProject.getId()
				+" and mpt.isFinished = false and mpt.isUseable = true and mpt.user is null and mpt.orderTime < '"+maxOrderTime+"'"
				+" and mpt.domain.id = "+domain.getId()+" order by mpt.orderTime asc";
		tasks = marketingProjectTaskEao.getEntityManager().createQuery(sql1).setMaxResults(maxOncePickCount).getResultList();
		
		// 如果按预约时间查询，没有查到任务，则再次按没有预约时间的任务进行查询
		if(tasks.isEmpty()) {
			String sql2 = "select mpt from MarketingProjectTask as mpt where mpt.marketingProject.id = " + marketingProject.getId()
					+" and mpt.isFinished = false and mpt.isUseable = true and mpt.user is null and mpt.orderTime is null"
					+" and mpt.domain.id = "+domain.getId()+" order by mpt.id asc";
			tasks = marketingProjectTaskEao.getEntityManager().createQuery(sql2).setMaxResults(maxOncePickCount).getResultList();
		}
		
		// 如果还是查不到任务，则表明当前没有合适的任务可用
		if(tasks.isEmpty()) {
			throw new RuntimeException("当前项目【"+marketingProject.getProjectName()+"】目前没有可用的未分配任务，请稍后重试！");
		}
		
		// 用于保存成功分配后的任务对象
		List<MarketingProjectTask> projectTasks = new ArrayList<MarketingProjectTask>();
		Date now = new Date();
		for(MarketingProjectTask task : tasks) {
			try {
				if(isForShanXiJiaoTan) {	// 山西焦炭
					task = updateWorkflowTransferLog2JiaoTan(marketingProject, user, workflow2JiaoTan, task, now);
				}
				task.setUser(user);
				task.setDistributeTime(now);
				task = (MarketingProjectTask) marketingProjectTaskEao.update(task);
				projectTasks.add(task);
			} catch (Exception e) {
				logger.error("jrh 话务员自己获取新的任务时出现异常---> "+e.getMessage(), e);
				return projectTasks;
			}
		}
//		也可以使用下面的方式，这样的缺点就是没法返回成功指派了多少条任务数
//		((DistributeToTaskService) AopContext.currentProxy()).saveOneUserTask(user, Long.valueOf(maxOncePickCount), tasks);
		
		return projectTasks;
	}

	/**
	 * 取得所有可以分配的资源数
	 * 
	 * @param marketingProject
	 * @param batches
	 * @param domain
	 * @return
	 */
	private Long caculateAssignableTaskNum(MarketingProject marketingProject,
			List<CustomerResourceBatch> batches, Domain domain) {
		// 包装批次的Id
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
		
		// 获取指定项目指定批次的可分配资源的Sql
		String nativeSql = "select count(*) from ec2_marketing_project_task where user_id is null and "
				+ "marketingproject_id="
				+ marketingProject.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " and customerresource_id "
				+ "in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
				+ "where customerresourcebatches_id in " + inSql + ")";
		Long assignableTaskNum=(Long)commonEao.excuteNativeSql(nativeSql, ExecuteType.SINGLE_RESULT);
		return assignableTaskNum;
	}

	/**
	 * 按照步长加载一个步长数量的Task
	 * 
	 * @param loadTaskStep
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<MarketingProjectTask> loadStepTasks(
			MarketingProject marketingProject,
			List<CustomerResourceBatch> batches, Long loadTaskStep,Long minId,
			Domain domain) {
		// 包装批次的Id
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

		// 获取指定项目指定批次的可分配资源的Sql, 按id好由小到大排序
		String nativeSql = "select id from ec2_marketing_project_task where id>"+minId+" and user_id is null "
				+ "and marketingproject_id="
				+ marketingProject.getId()
				+ " and domain_id="
				+ domain.getId()
				+ " and customerresource_id "
				+ "in(select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
				+ "where customerresourcebatches_id in " + inSql + ") order by id asc";
		// 取得可用的TaskId的集合
		List<Long> taskIdList = (List<Long>) commonEao.loadStepRows(nativeSql, loadTaskStep.intValue());

		//如果Task的Id值为空，表示没有查找到相应的Task，返回一个空集合
		if(taskIdList.isEmpty()){
			return new ArrayList<MarketingProjectTask>();
		}
		// 包装成为一个Task的Id
		StringBuilder sbTask = new StringBuilder();
		sbTask.append('(');
		Iterator<Long> taskIter = taskIdList.iterator();
		while (taskIter.hasNext()) {
			Long taskId = taskIter.next();
			sbTask.append(taskId);
			if (taskIter.hasNext())
				sbTask.append(",");
		}
		sbTask.append(')');
		String inTaskSql = sbTask.toString();
		// 隐含有域的概念
//		jrh 任务id 由小到大排序
		String taskSql = "select mpt from MarketingProjectTask mpt where mpt.id in" + inTaskSql + "order by mpt.id asc";
		List<MarketingProjectTask> taskList = marketingProjectTaskEao.getEntityManager().createQuery(taskSql).getResultList();
		return taskList;
	}

	/**
	 * 保存一个为一个用户指派的任务（支持事物）
	 * 
	 * @param user
	 * @param assignNum
	 *            指派数量
	 * @param assignableTasks
	 *            现有的任务集合
	 */
	public void saveOneUserTask(User user, Long assignNum,
			List<MarketingProjectTask> assignableTasks) {
		// 为该用户分配指定数量的任务,此时Task的数量一定大于将要分配的数量
		// 记录将要被移除的Task
		List<MarketingProjectTask> toRemove=new ArrayList<MarketingProjectTask>();
		Date now = new Date();
		for (int j = 0; j < assignNum; j++) {
			MarketingProjectTask task = assignableTasks.get(j);// 不会出现访问越界错误
			// jrh 
//			int success = (Integer) marketingProjectTaskEao.executeNativeSql("update ec2_marketing_project_task set distributetime = now(), user_id = " +user.getId()+ " where id = " +task.getId());
//			if(success < 1) {
//				throw new RuntimeException("为用户 "+user.getUsername()+ " 分配任务id为 "+task.getId()+ " 的任务时失败，请重试！");
//			}
//	jrh 使用JPQL 执行更新操作，在多个批次时，经常出现分配失败的情况
			task = createWorkflowTransferLog2JiaoTan(user, task, now);	// 山西焦炭2013-07-30
			
			task.setUser(user);
			task.setDistributeTime(now);
			task = (MarketingProjectTask) marketingProjectTaskEao.update(task);
			//  每次指派完都保存一条Task，并将用过的Task移除
			toRemove.add(task);
		}
		
		//对Task进行移除操作
		for(MarketingProjectTask task:toRemove){
			assignableTasks.remove(task);
		}
	}

	/**
	 * jrh 为山西焦炭而作，检查信息，并判断创建迁移日志	2013-07-30
	 * @param user	被指派任务的话务员
	 * @param task	任务
	 * @param now	当前时间点
	 * @return
	 */
	private MarketingProjectTask createWorkflowTransferLog2JiaoTan(User user,
			MarketingProjectTask task, Date now) {
		MarketingProject marketingProject = task.getMarketingProject();
		Long projectId = marketingProject.getId();
		
		String shanXiJiaoTanPcMac = ShanXiJiaoTanWorkOrderConfig.props.getProperty(ShanXiJiaoTanWorkOrderConfig.SANXIJIAOTAN_PC_MAC);
		boolean isForShanXiJiaoTan = (shanXiJiaoTanPcMac != null && GlobalData.MAC_ADDRESS.equals(shanXiJiaoTanPcMac)) ? true : false;
		String workflow2JiaoTan = "";
		
		// 首先判断是否是山西焦炭项目的配置，如果是，则按山西焦炭的配置文件走
		if(isForShanXiJiaoTan) {
			Long workOrder1ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER1_PROJECT_ID));
			Long workOrder2ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER2_PROJECT_ID));
			Long workOrder3ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER3_PROJECT_ID));
			if(projectId.equals(workOrder1ProjectId)) {
				workflow2JiaoTan = "workflow1";
			} else if(projectId.equals(workOrder2ProjectId)) {
				workflow2JiaoTan = "workflow2";
			} else if(projectId.equals(workOrder3ProjectId)) {
				workflow2JiaoTan = "workflow3";
			}
		}
		
		if(isForShanXiJiaoTan) {	// 山西焦炭
			task = updateWorkflowTransferLog2JiaoTan(marketingProject, user, workflow2JiaoTan, task, now);
		}
		return task;
	}

	/**
	 * jrh 为山西焦炭创建工作流迁移日志		2013-07-30
	 * @param marketingProject 		 	 项目
	 * @param user					   	需要获取新任务的话务员
	 * @param workflowToJiaoTan			当前是山西焦炭的第几个工作流
	 * @param task						当前操作的任务
	 * @return
	 */
	private MarketingProjectTask updateWorkflowTransferLog2JiaoTan(MarketingProject marketingProject, 
			User user, String workflowToJiaoTan, MarketingProjectTask task, Date now) {
		Domain domain = user.getDomain();
		Long projectId = marketingProject.getId();
		
		CustomerResource  customerResource = task.getCustomerResource();
		WorkflowTransferLog transferLog = null;
		Long workflowTransferLogId = task.getWorkflowTransferLogId();
		
		if(workflowTransferLogId != null) {
			transferLog = commonEao.get(WorkflowTransferLog.class, workflowTransferLogId);
		} else {	// 这种一般不会出现，主要是为了解决以前没有该处理时已经生成的数据
			transferLog = new WorkflowTransferLog();
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
			transferLog.setDomainId(domain.getId());
		}
		
		if(workflowToJiaoTan.equals("workflow1")) {
			transferLog.setWorkflow1PickTaskTime(now);
			transferLog.setWorkflow1ProjectId(projectId);
			transferLog.setWorkflow1ProjectName(marketingProject.getProjectName());
			transferLog.setWorkflow1CsrId(user.getId());
			transferLog.setWorkflow1CsrEmpNo(user.getEmpNo());
			transferLog.setWorkflow1CsrName(user.getRealName());
			transferLog.setWorkflow1CsrUsername(user.getUsername());
			Department dept = user.getDepartment();
			transferLog.setWorkflow1CsrDeptId(dept.getId());
			transferLog.setWorkflow1CsrDeptName(dept.getName());
			if(workflowTransferLogId == null) {	// 新建日志则传递加入土豆池的时间
				transferLog.setMigrateWorkflow1Time(task.getCreateTime());
			}
		} else if(workflowToJiaoTan.equals("workflow2")) {
			transferLog.setWorkflow2PickTaskTime(now);
			transferLog.setWorkflow2ProjectId(projectId);
			transferLog.setWorkflow2ProjectName(marketingProject.getProjectName());
			transferLog.setWorkflow2CsrId(user.getId());
			transferLog.setWorkflow2CsrEmpNo(user.getEmpNo());
			transferLog.setWorkflow2CsrName(user.getRealName());
			transferLog.setWorkflow2CsrUsername(user.getUsername());
			Department dept = user.getDepartment();
			transferLog.setWorkflow2CsrDeptId(dept.getId());
			transferLog.setWorkflow2CsrDeptName(dept.getName());
			if(workflowTransferLogId == null) {	// 新建日志则传递加入土豆池的时间
				transferLog.setMigrateWorkflow2Time(task.getCreateTime());
			}
		} else if(workflowToJiaoTan.equals("workflow3")) {
			transferLog.setWorkflow3PickTaskTime(now);
			transferLog.setWorkflow3ProjectId(projectId);
			transferLog.setWorkflow3ProjectName(marketingProject.getProjectName());
			transferLog.setWorkflow3CsrId(user.getId());
			transferLog.setWorkflow3CsrEmpNo(user.getEmpNo());
			transferLog.setWorkflow3CsrName(user.getRealName());
			transferLog.setWorkflow3CsrUsername(user.getUsername());
			Department dept = user.getDepartment();
			transferLog.setWorkflow3CsrDeptId(dept.getId());
			transferLog.setWorkflow3CsrDeptName(dept.getName());
			if(workflowTransferLogId == null) {	// 新建日志则传递加入土豆池的时间
				transferLog.setMigrateWorkflow3Time(task.getCreateTime());
			}
		}
		if(transferLog.getId() == null) {
			commonEao.save(transferLog);
		} else {
			transferLog = (WorkflowTransferLog) commonEao.update(transferLog);
		}
		task.setWorkflowTransferLogId(transferLog.getId());
		return task;
	}

	/**
	 * chb 将User包装成为Map
	 * 
	 * @param users
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	private HashMap<User, Long> wrapToMap(Set<User> users,
			MarketingProject marketingProject, Domain domain) {
		HashMap<User, Long> map = new HashMap<User, Long>();
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			User user = iter.next();
			Long num = marketingProjectTaskEao.getTaskCountByUser(user,
					marketingProject, domain);
			map.put(user, num);
		}
		return map;
	}

	/**
	 * 取出用户现在拥有的资源数的集合,并按照由小到大排序
	 * 
	 * @param userNumMap
	 * @return
	 */
	private List<Long> getSortedNum(HashMap<User, Long> userNumMap) {
		List<Long> numList = new ArrayList<Long>();
		for (User user : userNumMap.keySet()) {
			numList.add(userNumMap.get(user));
		}
		Collections.sort(numList);
		return numList;
	}

	/**
	 * 取得按照用户的资源数由少到多的排序的一个集合
	 * 
	 * @return
	 */
	private List<User> getSortedUser(final HashMap<User, Long> userNumMap) {
		List<User> sortedUser = new ArrayList<User>(userNumMap.keySet());
		Collections.sort(sortedUser, new Comparator<User>() {
			// 如果用户1对应的资源数大于用户2对应的资源数则为大于，以此类推
			@Override
			public int compare(User user1, User user2) {
				return (int) (userNumMap.get(user1) - userNumMap.get(user2));
			}
		});
		return sortedUser;
	}

	/**
	 * 据每个用户拥有的资源数和全部资源数计算应该为每个用户分配的数值
	 * 
	 * @param numList
	 *            由大到小排序的用户现有资源数集合
	 * @param allNum
	 *            全部资源数
	 * @param assignNum
	 *            管理员指派的资源数
	 * @return 返回一个按用户资源从多到少排序的应该分配的资源数量的集合，如果所有用户资源数量都已经达到管理员指派的值，则返回空集合
	 */
	public List<Long> caculateToDistributeNum(List<Long> numList, Long assignableNum,
			Long assignNum) {
		Long avgNum = 0L;// 平均值
		Long remainNum = 0L;
		
		// 通过可以分配的资源数量，计算资源总量，包括用户现在已经拥有的资源数量
		Long allNum=assignableNum;
		for (Long num : numList) {
			allNum += num;
		}

		// 记录不符合条件的将要被移除的
		List<Long> toRemoveList = new ArrayList<Long>();
		// 通过对所有用户已经分配的数量循环，找出应该分配的数量线
		for (int i = 0; i < numList.size(); i++) {
			// 当前的用户已分配的资源数量
			Long currentNum = numList.get(i);
			// 如果当前用户已分配的资源数大于管理员指派的数量，将移除（不为此用户分配资源）
			if (currentNum >= assignNum) {
				toRemoveList.add(currentNum);
				allNum -= currentNum;// 从总数中减去这个不用分配资源的用户数量
				continue;
			}

			// 剩下的应该分配资源的用户数量
			int toDistributeUserNum = numList.size() - toRemoveList.size();
			// 如果剩下应该分配数大于总数量，说明资源不足，应该继续减少分配资源的用户
			if (toDistributeUserNum * currentNum > allNum) {
				toRemoveList.add(currentNum);
				allNum -= currentNum;// 从总数中减去这个不用分配资源的用户数量
				continue;
			} else if (toDistributeUserNum * assignNum <= allNum) {// 说明资源足够可以分到管理员指派的水平
				avgNum = assignNum;// 平均值
				remainNum = allNum - toDistributeUserNum * assignNum;// 余数
				break;// 退出循环
			} else if (toDistributeUserNum * currentNum <= allNum) {// 说明到达临界点，可以在此步确定能分配的平均值和按平均值分配后的余数，此时可能有一个用户不需要再分配资源
				avgNum = allNum / toDistributeUserNum;
				remainNum = allNum % toDistributeUserNum;
				break;// 退出循环
			}
		}

		// 对不需要分配资源的用户进行移除操作,得到计划分配资源的用户目前资源状态集合
		for (Long num : toRemoveList) {
			numList.remove(num);
		}

		// 存储每个用户应该分配的数量
		List<Long> toDistributeNums = new ArrayList<Long>();
		// 按照avgNum计算每个用户应该分配的资源数量
		for (int i = 0; i < numList.size(); i++) {
			toDistributeNums.add(avgNum - numList.get(i));
		}
		// 如果平均数量达不到管理员指派的数量，并且余数大于0，则对余数的处理
		// 将余数分配给原来含有资源数相对较高的几个用户
		if ((avgNum != assignNum) && (remainNum > 0)) {
			if (toDistributeNums.size() < remainNum)
				throw new RuntimeException("分配余数，分配的用户数量不应该小于余数！");
			for (int i = 0; i < remainNum; i++) {
				// 将原来的部分数值加1
				toDistributeNums.set(i, toDistributeNums.get(i) + 1);
			}
		}
		// 目前的应分配数量是由按用户资源数由多到少的排序，将之变为由少到多的排序
		Collections.reverse(toDistributeNums);
		return toDistributeNums;
	}

	// Getter and Setter
	public MarketingProjectTaskEao getMarketingProjectTaskEao() {
		return marketingProjectTaskEao;
	}

	public void setMarketingProjectTaskEao(
			MarketingProjectTaskEao marketingProjectTaskEao) {
		this.marketingProjectTaskEao = marketingProjectTaskEao;
	}

	public MarketingProjectEao getMarketingProjectEao() {
		return marketingProjectEao;
	}

	public void setMarketingProjectEao(MarketingProjectEao marketingProjectEao) {
		this.marketingProjectEao = marketingProjectEao;
	}

	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}

}
