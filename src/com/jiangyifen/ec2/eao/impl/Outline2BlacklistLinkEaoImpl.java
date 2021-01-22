package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.Outline2BlacklistLinkEao;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;

/**
* Eao实现类：外线黑名单对应关系管理
* 
* 注入：BaseEaoImpl
* 
* @author jrh
*
*/
public class Outline2BlacklistLinkEaoImpl extends BaseEaoImpl implements Outline2BlacklistLinkEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得外线黑名单对应关系列表
	public List<Outline2BlacklistItemLink> loadOutline2BlacklistLinkList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}