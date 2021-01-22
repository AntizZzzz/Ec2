package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;

public interface Ec2ConfigurationService {
	
	// enhanced method
	
// TODO 该方法暂时没有任何用户，如果Ec2Configuration 中删除了 Domain字段，那么该方法就得删除
	/**
	 * 获取指定域所拥有的所有特殊权限
	 * @param domain 域
	 * @return List<Ec2Configuration> 配置权限
	 */
	@Transactional
	public List<Ec2Configuration> getAllSpecialConfigsByDomain(Domain domain);

	/**
	 * jrh	获取指定域下的指定配置对象
	 * @param string
	 * @param domainId
	 * @return
	 */
	@Transactional
	public Ec2Configuration getByKey(String key, Long domainId);
	
	// common method 
	
	@Transactional
	public Ec2Configuration get(Object primaryKey);
	
	@Transactional
	public void save(Ec2Configuration config);
	
	@Transactional
	public Ec2Configuration update(Ec2Configuration config);
	
	@Transactional
	public void delete(Ec2Configuration config);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	@Transactional
	public List<Ec2Configuration> loadPageEntitys(int start,int length,String sql);
	
	@Transactional
	public int getEntityCount(String sql);

}
