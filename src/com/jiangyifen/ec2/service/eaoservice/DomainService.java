package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;

public interface DomainService {

	@Transactional
	public Domain getDomain(Object primaryKey);

	@Transactional
	public void saveDomain(Domain domain);

	@Transactional
	public void updateDomain(Domain domain);

	@Transactional
	public void deleteDomain(Domain domain);

	@Transactional
	public void deleteDomainById(Object primaryKey);

	@Transactional
	public List<Domain> loadPageEntitys(int start,int length,String sql);

	@Transactional
	public int getEntityCount(String sql);

	public List<Domain> getAll();
	
}
