package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.WorkOrderOperate;

/**
* Eao接口：工单操作管理
* 
* @author lxy
*
*/
public interface WorkOrderOperateEao extends BaseEao {
	
	
	/**
	 * 获得工单操作列表
	 * @param 	jpql jpql语句
	 * @return 	工单操作列表
	 */
	public List<WorkOrderOperate> loadWorkOrderOperateList(String jpql);
	
}