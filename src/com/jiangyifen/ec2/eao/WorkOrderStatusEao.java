package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderStatus;

/**
* Eao接口：工单状态管理
* 
* @author lxy
*
*/
public interface WorkOrderStatusEao extends BaseEao {
	
	
	/**
	 * 获得工单状态列表
	 * @param 	jpql jpql语句
	 * @return 	工单状态列表
	 */
	public List<WorkOrderStatus> loadWorkOrderStatusList(String jpql);
	
	/**
	 * 根据域获得工单状态列表
	 * @param domain
	 * @return
	 */
	public List<WorkOrderStatus> getAllByDomain(Domain domain);
	
}