package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MeettingDetailRecordEao;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;

/**
* Eao实现类：多方通话的记录详情
* 
* 注入：BaseEaoImpl
* 
* @author jrh
*
*/
public class MeettingDetailRecordEaoImpl extends BaseEaoImpl implements MeettingDetailRecordEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得多方通话的记录详情列表
	public List<MeettingDetailRecord> loadMeettingDetailRecordList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}