package com.jiangyifen.ec2.fastagi;

import java.util.Date;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 存储自动外呼的坐席应答信息
 * 
 * @author
 */
public class QueuePickup extends BaseAgiScript {

	private MarketingProjectTaskService marketingProjectTaskService;

	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		
		// 如果坐席接起电话，为Task设置项目的Id，并且将task的状态设置为接起
		String projectId = channel.getVariable("projectId");
		String taskId = channel.getVariable("taskId");
		if (marketingProjectTaskService == null) {
			marketingProjectTaskService = SpringContextHolder
					.getBean("marketingProjectTaskService");
		} 
		MarketingProjectTask task = marketingProjectTaskService.get(Long
				.parseLong(taskId));
		MarketingProject fakeMarketingProject = new MarketingProject();
		fakeMarketingProject.setId(Long.parseLong(projectId));

		task.setMarketingProject(fakeMarketingProject);// 存储项目Id
		task.setAutodialIsCsrPickup(true);// 存储坐席已经应答
		task.setAutodialPickupTime(new Date());

		// 存储Task到数据库
		marketingProjectTaskService.update(task);
	}
}
