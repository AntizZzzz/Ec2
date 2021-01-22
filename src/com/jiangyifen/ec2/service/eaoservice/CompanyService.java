package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Company;

public interface CompanyService {

	
	// ========  enhanced method  ========//
	
	/**
	 * chb
	 * 通过公司名字选出公司，如果存在返回公司，如果不存在返回null
	 * @param companyName	公司名称
	 * @param domainId		 公司对象
	 * @return
	 */
	@Transactional
	public Company getCompanyByName(String companyName, Long domainId);

	// ========  common method  ========//
	
	@Transactional
	public Company get(Object primaryKey);
	
	@Transactional
	public void save(Company company);

	@Transactional
	public Company update(Company company);

	@Transactional
	public void delete(Company company);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
