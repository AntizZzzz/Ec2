package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderStatusEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderStatus;

/**
* Eao实现类：工单状态管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderStatusEaoImpl extends BaseEaoImpl implements WorkOrderStatusEao {
	
	//获得工单状态列表
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderStatus> loadWorkOrderStatusList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	//根据域获得工单状态列表
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderStatus> getAllByDomain(Domain domain) {
		String jpql = "select s from WorkOrderStatus  as s   order by s.id ";
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
	 
}