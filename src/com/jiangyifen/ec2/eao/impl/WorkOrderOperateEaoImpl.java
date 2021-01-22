package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderOperateEao;
import com.jiangyifen.ec2.entity.WorkOrderOperate;

/**
* Eao实现类：工单操作管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderOperateEaoImpl extends BaseEaoImpl implements WorkOrderOperateEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工单操作列表
	public List<WorkOrderOperate> loadWorkOrderOperateList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}