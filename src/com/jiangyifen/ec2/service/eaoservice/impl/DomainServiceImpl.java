package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.DomainEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.DomainService;

public class DomainServiceImpl implements DomainService {
	private DomainEao domainEao;
	// enhance function method

	// common method
	@Override
	public Domain getDomain(Object primaryKey) {
		return domainEao.get(Domain.class, primaryKey);
	}

	@Override
	public void saveDomain(Domain domain) {
		domainEao.save(domain);
	}

	@Override
	public void updateDomain(Domain domain) {
		domainEao.update(domain);
	}

	@Override
	public void deleteDomain(Domain domain) {
		domainEao.delete(domain);
	}

	@Override
	public void deleteDomainById(Object primaryKey) {
		domainEao.delete(Domain.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Domain> loadPageEntitys(int start, int length, String sql) {
		return domainEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return domainEao.getEntityCount(sql);
	}

	//getter and setter
	public DomainEao getDomainEao() {
		return domainEao;
	}

	public void setDomainEao(DomainEao domainEao) {
		this.domainEao = domainEao;
	}
	
	/**
	 * chb 取得所有的域
	 */
	@Override
	public List<Domain> getAll() {
		return domainEao.getAll();
	}
}
