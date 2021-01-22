package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.Ec2ConfigurationEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;

public class Ec2ConfigurationEaoImpl extends BaseEaoImpl implements Ec2ConfigurationEao {
	
	// enhanced method
	
// TODO 该方法暂时没有任何用户，如果Ec2Configuration 中删除了 Domain字段，那么该方法就得删除
	/**
	 * 获取指定域所拥有的所有特殊权限
	 * @param domain 域
	 * @return Ec2Configuration 配置权限
	 */
	@SuppressWarnings("unchecked")
	public List<Ec2Configuration> getAllSpecialConfigsByDomain(Domain domain) {
		return getEntityManager().createQuery("select ec from Ec2Configuration as ec where ec.key is not null and ec.value is not null  and ec.domain.id = " + domain.getId()).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Ec2Configuration getByKey(String key, Long domainId) {
		List<Ec2Configuration> list = getEntityManager().createQuery("select ec from Ec2Configuration as ec where ec.key ='" +key+ "' and ec.domain.id = " + domainId).getResultList();
		if(list.size() > 0) {
			return list.get(0);
		}
		return null;
	}
	
	
}
