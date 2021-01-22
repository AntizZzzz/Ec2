package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.CompanyEao;
import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.service.eaoservice.CompanyService;

public class CompanyServiceImpl implements CompanyService {
	
	private CompanyEao companyEao;
	
	// ========  enhanced method  ========//
	
	@Override
	public Company getCompanyByName(String companyName, Long domainId) {
		return companyEao.getCompanyByName(companyName, domainId);
	}

	//========  common method ========//
	@Override
	public Company get(Object primaryKey) {
		return companyEao.get(Company.class, primaryKey);
	}

	@Override
	public void save(Company company) {
		companyEao.save(company);
	}

	@Override
	public Company update(Company company) {
		return (Company)companyEao.update(company);
	}

	@Override
	public void delete(Company company) {
		companyEao.delete(company);
	}

	@Override
	public void deleteById(Object primaryKey) {
		companyEao.delete(Company.class, primaryKey);
	}

	//==========  getter setter  ==========//
	
	public CompanyEao getCompanyEao() {
		return companyEao;
	}
	
	public void setCompanyEao(CompanyEao companyEao) {
		this.companyEao = companyEao;
	}
}
