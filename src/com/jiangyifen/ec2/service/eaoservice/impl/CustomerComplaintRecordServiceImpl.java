package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.CustomerComplaintRecordEao;
import com.jiangyifen.ec2.entity.CustomerComplaintRecord;
import com.jiangyifen.ec2.service.eaoservice.CustomerComplaintRecordService;

public class CustomerComplaintRecordServiceImpl implements CustomerComplaintRecordService {
	private CustomerComplaintRecordEao customerComplaintRecordEao;
	// enhance function method

	// common method
	@Override
	public CustomerComplaintRecord get(Object primaryKey) {
		return customerComplaintRecordEao.get(CustomerComplaintRecord.class, primaryKey);
	}

	@Override
	public void save(CustomerComplaintRecord customerComplaintRecord) {
		customerComplaintRecordEao.save(customerComplaintRecord);
	}

	@Override
	public void update(CustomerComplaintRecord customerComplaintRecord) {
		customerComplaintRecordEao.update(customerComplaintRecord);
	}

	@Override
	public void delete(CustomerComplaintRecord customerComplaintRecord) {
		customerComplaintRecordEao.delete(customerComplaintRecord);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerComplaintRecordEao.delete(CustomerComplaintRecord.class, primaryKey);
	}

	//getter and setter
	public CustomerComplaintRecordEao getCustomerComplaintRecordEao() {
		return customerComplaintRecordEao;
	}

	public void setCustomerComplaintRecordEao(CustomerComplaintRecordEao customerComplaintRecordEao) {
		this.customerComplaintRecordEao = customerComplaintRecordEao;
	}
	
}
