package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerLevelEao;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.Domain;

public class CustomerLevelEaoImpl extends BaseEaoImpl implements CustomerLevelEao {
	/**
	 * chb 
	 * 取得所有的状态信息
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerLevel> getAll(Domain domain) {
		List<CustomerLevel> levelList = getEntityManager().createQuery("select cl from CustomerLevel as cl " +
				"where cl.domain.id = " + domain.getId() +" order by cl.id asc").getResultList();
		return levelList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerLevel> getAllSuperiorLevel(CustomerLevel level, Domain domain) {
		List<CustomerLevel> levelList = getEntityManager().createQuery("select cl from CustomerLevel as cl " +
				"where cl.domain.id = " + domain.getId() +" and cl.level >= " +level.getLevel()+ " order by cl.levelName asc").getResultList();
		return levelList;
	}

}
