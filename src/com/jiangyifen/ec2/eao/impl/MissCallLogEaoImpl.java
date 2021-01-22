package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MissCallLogEao;
import com.jiangyifen.ec2.entity.MissCallLog;

/**
* Eao实现类：电话漏接日志
* 
* 注入：BaseEaoImpl
* 
* @author jrh
*
*/
public class MissCallLogEaoImpl extends BaseEaoImpl implements MissCallLogEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得电话漏接日志列表
	public List<MissCallLog> loadMissCallLogList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}