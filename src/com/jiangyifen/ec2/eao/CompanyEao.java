package com.jiangyifen.ec2.eao;

import com.jiangyifen.ec2.entity.Company;

public interface CompanyEao extends BaseEao {
	/**
	 * chb
	 * 通过公司名字选出公司，如果存在返回公司，如果不存在返回null
	 * @param companyName
	 * @param domainId
	 * @return
	 */
	public Company getCompanyByName(String companyName,Long domainId);
	
}
