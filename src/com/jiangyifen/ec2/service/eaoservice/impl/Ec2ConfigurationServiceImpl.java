package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.Ec2ConfigurationEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;
import com.jiangyifen.ec2.service.eaoservice.Ec2ConfigurationService;

public class Ec2ConfigurationServiceImpl implements Ec2ConfigurationService {
	
	private Ec2ConfigurationEao ec2ConfigurationEao;
	
	// enhanced method
	
	@Override
	public List<Ec2Configuration> getAllSpecialConfigsByDomain(Domain domain) {
		return ec2ConfigurationEao.getAllSpecialConfigsByDomain(domain);
	}
	
	@Override
	public Ec2Configuration getByKey(String key, Long domainId) {
		return ec2ConfigurationEao.getByKey(key, domainId);
	}
	

	// common method


	@Override
	public Ec2Configuration get(Object primaryKey) {
		return ec2ConfigurationEao.get(Ec2Configuration.class, primaryKey);
	}

	@Override
	public void save(Ec2Configuration config) {
		ec2ConfigurationEao.save(config);
	}

	@Override
	public Ec2Configuration update(Ec2Configuration config) {
		return (Ec2Configuration) ec2ConfigurationEao.update(config);
	}

	@Override
	public void delete(Ec2Configuration config) {
		ec2ConfigurationEao.delete(config);
	}

	@Override
	public void deleteById(Object primaryKey) {
		ec2ConfigurationEao.delete(Ec2Configuration.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Ec2Configuration> loadPageEntitys(int start, int length,
			String sql) {
		return ec2ConfigurationEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return ec2ConfigurationEao.getEntityCount(sql);
	}

	// getter setter

	public Ec2ConfigurationEao getEc2ConfigurationEao() {
		return ec2ConfigurationEao;
	}

	public void setEc2ConfigurationEao(Ec2ConfigurationEao ec2ConfigurationEao) {
		this.ec2ConfigurationEao = ec2ConfigurationEao;
	}
	
}
