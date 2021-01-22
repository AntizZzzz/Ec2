package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderHandleResult;

/**
* Eao接口：工单处置结果
* 
* @author lxy
*
*/
public interface WorkOrderHandleResultEao extends BaseEao {
	
	
	/**
	 * 获得工单处置结果列表
	 * @param 	jpql jpql语句
	 * @return 	工单处置结果列表
	 */
	public List<WorkOrderHandleResult> loadWorkOrderHandleResultList(String jpql);
	
	/**
	 * 根据域获得工单处置列表
	 * @param domain
	 * @return
	 */
	public List<WorkOrderHandleResult> getAllByDomain(Domain domain);
}