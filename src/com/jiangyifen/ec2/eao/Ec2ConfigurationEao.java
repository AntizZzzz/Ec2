package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;

public interface Ec2ConfigurationEao extends BaseEao {
	
	// enhanced method
	
	/**
	 * 获取指定域所拥有的所有特殊权限
	 * @param domain 域
	 * @return List<Ec2Configuration> 配置权限
	 */
	public List<Ec2Configuration> getAllSpecialConfigsByDomain(Domain domain);


	/**
	 * jrh	获取指定域下的指定配置对象
	 * @param string
	 * @param domainId
	 * @return
	 */
	public Ec2Configuration getByKey(String key, Long domainId);
	
}
