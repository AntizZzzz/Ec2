package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderTypeEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.WorkOrderType;

/**
* Eao实现类：工单类型管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderTypeEaoImpl extends BaseEaoImpl implements WorkOrderTypeEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工单类型列表
	public List<WorkOrderType> loadWorkOrderTypeList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	//根据域获得工单类型列表
	@Override
	@SuppressWarnings("unchecked")
	public List<WorkOrderType> getAllByDomain(Domain domain) {
		String jpql = "select s from WorkOrderType  as s  where  s.domain.id = " + domain.getId() +" order by s.id";
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}