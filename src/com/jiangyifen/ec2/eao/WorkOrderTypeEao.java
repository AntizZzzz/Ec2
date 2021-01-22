package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderType;

/**
* Eao接口：工单类型管理
* 
* @author lxy
*
*/
public interface WorkOrderTypeEao extends BaseEao {
	
	
	/**
	 * 获得工单类型列表
	 * @param 	jpql jpql语句
	 * @return 	工单类型列表
	 */
	@Transactional
	public List<WorkOrderType> loadWorkOrderTypeList(String jpql);
	
	/**
	 * 根据域获得工单类型列表
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<WorkOrderType> getAllByDomain(Domain domain);
}