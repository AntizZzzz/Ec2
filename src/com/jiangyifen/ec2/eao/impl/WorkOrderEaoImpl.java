package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderEao;
import com.jiangyifen.ec2.entity.WorkOrder;

/**
* Eao实现类：工单管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderEaoImpl extends BaseEaoImpl implements WorkOrderEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工单列表
	public List<WorkOrder> loadWorkOrderList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}