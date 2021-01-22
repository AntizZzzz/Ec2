package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderPriorityEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderPriority;

/**
* Eao实现类：工单优先级管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderPriorityEaoImpl extends BaseEaoImpl implements WorkOrderPriorityEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工单优先级列表
	public List<WorkOrderPriority> loadWorkOrderPriorityList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderPriority> getAllByDomain(Domain domain) {
		String jpql = "select s from WorkOrderPriority  as s where  s.domain.id = " + domain.getId() +" order by s.orderNumber";
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}