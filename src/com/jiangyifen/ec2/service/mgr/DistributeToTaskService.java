package com.jiangyifen.ec2.service.mgr;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;

public interface DistributeToTaskService {
	
	/**
	 * chb
	 * 检查Csr数量和资源数量，Csr>资源数量则不允许分配，并返回提示信息
	 * @param target
	 * @param domain
	 * @return 字符串类型的提示信息
	 * 
	 * 因为返回界面相关的字符串，此方法不好！！！！！！！
	 */
	@Transactional
	public String validateCsrAndResource(MarketingProject target,List<CustomerResourceBatch> batches,Domain domain);
	
	/**
	 * chb 
	 * 如果资源足够，则将Csr任务数不到最大值（max）的任务分配到最大值，如果资源不充足，则将资源平均分配给任务最少的几个Csr
	 * @param marketingProject 项目
	 * @param max 每个Csr能接收资源的最大值
	 * @param domain 域
	 * @return 是否分配成功
	 */
	public Long distribute(MarketingProject marketingProject,List<CustomerResourceBatch> batches,Set<User> users,Long max, Domain domain);
	
	/**
	 * jrh 
	 * 	给指定的Csr 分配新的任务
	 * @param marketingProject 		 	 项目
	 * @param maxTotalPickTaskCount 	当前话务员当前最多还能获取的任务条数
	 * @param user					   	需要获取新任务的话务员
	 * @param domain					指定的域对象
	 * @return
	 */
	@Transactional
	public List<MarketingProjectTask> distributeByCsr(MarketingProject marketingProject, int maxTotalPickUpTaskCount, User user, Domain domain);
	
	/**
	 * 为了使之支持事物的设置
	 * @param user
	 * @param assignNum
	 * @param assignableTasks
	 */
	@Transactional
	public void saveOneUserTask(User user, Long assignNum, List<MarketingProjectTask> assignableTasks);

}
