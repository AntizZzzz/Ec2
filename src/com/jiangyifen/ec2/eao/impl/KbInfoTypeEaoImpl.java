package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.KbInfoTypeEao;
import com.jiangyifen.ec2.entity.KbInfoType;

/**
* Eao实现类：知识类型管理
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class KbInfoTypeEaoImpl extends BaseEaoImpl implements KbInfoTypeEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得知识类型列表
	public List<KbInfoType> loadKbInfoTypeList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}