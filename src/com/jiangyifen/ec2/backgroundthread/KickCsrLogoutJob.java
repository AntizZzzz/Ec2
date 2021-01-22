package com.jiangyifen.ec2.backgroundthread;

import java.util.ArrayList;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：定时强制坐席退出系统的任务
 * 
 * @author  jrh
 * @date    2013年12月19日 上午11:06:27
 * @version v1.0.0
 */
public class KickCsrLogoutJob implements Job {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());	// 日志工具
	
	public final static String KICKED_DOMAIN_ID = "kickedDomainId";			// 待处理的租户对应的域编号
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private UserLoginService userLoginService = SpringContextHolder.getBean("userLoginService");

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDataMap dataMap = context.getTrigger().getJobDataMap();
			Domain domain = (Domain) dataMap.get(KICKED_DOMAIN_ID);
			Long domainId = domain.getId();
			
			ArrayList<Long> loginCsrIds = new ArrayList<Long>(ShareData.userToExten.keySet());
			for(User user : userService.getAllByDomain(domain)) {
				Long userId = user.getId();
				if(loginCsrIds.contains(userId)) {
					String exten = ShareData.userToExten.get(userId);
					userLoginService.logout(userId, domainId, user.getUsername(), user.getEmpNo(), exten, 0L, true);
				}
			}
			logger.info("jrh 将坐席退出系统的定时器，执行成功！当前执行的租户所属域的编号为："+domainId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 将坐席退出系统的定时器运行出现异常！--->"+e.getMessage(), e);
		}
	}

}
