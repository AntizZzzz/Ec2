package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.WorkOrderFileEao;
import com.jiangyifen.ec2.entity.WorkOrderFile;

/**
* Eao实现类：工单附件管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class WorkOrderFileEaoImpl extends BaseEaoImpl implements WorkOrderFileEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得工单附件列表
	public List<WorkOrderFile> loadWorkOrderFileList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}