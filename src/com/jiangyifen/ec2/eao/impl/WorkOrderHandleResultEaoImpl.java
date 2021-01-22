package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderHandleResultEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderHandleResult;

/**
* Eao实现类：工单处置结果
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderHandleResultEaoImpl extends BaseEaoImpl implements WorkOrderHandleResultEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工单处置结果列表
	public List<WorkOrderHandleResult> loadWorkOrderHandleResultList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderHandleResult> getAllByDomain(Domain domain) {
		String jpql = "select s from WorkOrderHandleResult  as s where  s.domain.id = " + domain.getId() +" order by s.id";
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}