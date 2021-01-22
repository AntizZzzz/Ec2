package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.CustomerSatisfactionInvestigationLogEao;
import com.jiangyifen.ec2.entity.CustomerSatisfactionInvestigationLog;
import com.jiangyifen.ec2.service.eaoservice.CustomerSatisfactionInvestigationLogService;

public class CustomerSatisfactionInvestigationLogServiceImpl implements CustomerSatisfactionInvestigationLogService{
	private CustomerSatisfactionInvestigationLogEao  customerSatisfactionInvestigationLogEao;

	@Override
	public void save(CustomerSatisfactionInvestigationLog log) {
		customerSatisfactionInvestigationLogEao.save(log);
	}

	public CustomerSatisfactionInvestigationLogEao getCustomerSatisfactionInvestigationLogEao() {
		return customerSatisfactionInvestigationLogEao;
	}

	public void setCustomerSatisfactionInvestigationLogEao(
			CustomerSatisfactionInvestigationLogEao customerSatisfactionInvestigationLogEao) {
		this.customerSatisfactionInvestigationLogEao = customerSatisfactionInvestigationLogEao;
	}
}
