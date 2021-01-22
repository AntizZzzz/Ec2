package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.KbInfoEao;
import com.jiangyifen.ec2.entity.KbInfo;

/**
* Eao实现类：知识库管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class KbInfoEaoImpl extends BaseEaoImpl implements KbInfoEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得知识库列表
	public List<KbInfo> loadKbInfoList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}