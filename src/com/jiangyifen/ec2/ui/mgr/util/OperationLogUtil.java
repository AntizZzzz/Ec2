package com.jiangyifen.ec2.ui.mgr.util;

import java.util.Date;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * <p>操作日志</p>
 * 
 * <p>操作日志工具类</p>
 *
 * @version $Id: OperationLogUtil.java 2014-5-28 上午10:52:52 chenhb $
 *
 */
public class OperationLogUtil {

	/**
	 * 记录日志信息
	 * @param user
	 * @param operationDate
	 * @param description
	 * @return
	 */
	public static Boolean simpleLog(User user,String description){
		CommonService commonService=SpringContextHolder.getBean("commonService");
		
	 	try {
			// 记录导出日志
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(user.getDomain());
			operationLog.setOperateDate(new Date());
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			operationLog.setDescription(description);
			commonService.save(operationLog);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
