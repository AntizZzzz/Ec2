package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.WorkOrder;

/**
* Eao接口：工单管理
* 
* @author lxy
*
*/
public interface WorkOrderEao extends BaseEao {
	
	
	/**
	 * 获得工单列表
	 * @param 	jpql jpql语句
	 * @return 	工单列表
	 */
	public List<WorkOrder> loadWorkOrderList(String jpql);
	
}