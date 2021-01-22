package com.jiangyifen.ec2.utils;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;

public class ShareDataElementUtil {
	/**
	 * 通过项目取得外线，如果项目没有外线， 这返回默认外线
	 * @param project
	 * @return
	 */
	public static String getOutlineByProject(MarketingProject project){
		String outline="";
		//取得项目的外线
		if(project.getSip()!=null){
			outline=project.getSip().getName();
		}
		//取得域的外线
		if(outline==null||outline.equals("")){
			outline = ShareData.domainToDefaultOutline.get(project.getDomain().getId());
		}
		return outline;
	}
	/**
	 * 通过项目取得外线容量
	 * @param project
	 * @return
	 */
	public static Integer getOutlineCapabilityByProject(MarketingProject project){
		//取得项目的外线
		SipConfig projectOutline = project.getSip();
		if(projectOutline!=null){
			return projectOutline.getCall_limit();
		}else{
			String outlineNumber = ShareData.domainToDefaultOutline.get(project.getDomain().getId());
			if(outlineNumber!=null&&!outlineNumber.equals("")){
				SipConfigService sipConfigService=SpringContextHolder.getBean("sipConfigService");
				SipConfig defaultOutline = sipConfigService.getOutlineByOutlineName(outlineNumber);
				if(defaultOutline==null){
					return null;
				}
				return defaultOutline.getCall_limit();
			}else{
				return null;
			}
		}
	}
	/**
	 * 通过项目取得外线队列
	 * @param project
	 * @return
	 */
	public static String getQueueByProject(MarketingProject project){
		String queue="";
		//取得项目的队列
		if(project.getQueue()!=null){
			queue=project.getQueue().getName();
		}
		//取得域的队列
		if(queue==null||queue.equals("")){
			queue = ShareData.domainToDefaultQueue.get(project.getDomain().getId());
		}
		return queue;
	}
	/**
	 * 通过项目取得外线深度
	 * @param project
	 * @return
	 */
	public static Integer getQueueDeepthByProject(MarketingProject project){
		//取得项目的队列
		 Queue projectQueue = project.getQueue();
		if(projectQueue!=null){
			return projectQueue.getMaxlen().intValue();
		}else{
			String queueName =  ShareData.domainToDefaultQueue.get(project.getDomain().getId());
			if(queueName!=null){
				QueueService queueService=SpringContextHolder.getBean("queueService");
				Queue defaultQueue = queueService.getQueueByQueueName(queueName);
				return defaultQueue.getMaxlen().intValue();
			}else{
				return null;
			}
		}
	}
}
