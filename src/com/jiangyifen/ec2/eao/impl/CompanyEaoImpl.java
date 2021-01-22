package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CompanyEao;
import com.jiangyifen.ec2.entity.Company;

public class CompanyEaoImpl extends BaseEaoImpl implements CompanyEao {

	/**
	 * chb
	 * 通过公司名字选出公司，如果存在返回公司，如果不存在返回null
	 * @param companyName
	 * @param domainId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Company getCompanyByName(String companyName, Long domainId) {
		//没有域的概念，并且不知道当没有记录时，getSingleResult是否返回null
		String sql="select c from Company c where c.name='"+companyName+"' and c.domain.id="+domainId;
		List<Company> companys=(List<Company>)getEntityManager().createQuery(sql).getResultList();
		if(companys.size()==0) return null;
		if(companys.size()>1){
			throw new RuntimeException("出现同名公司！");
		}
		return companys.get(0);
	}

}
