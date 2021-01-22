package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.CustomerComplaintRecordStatusEao;
import com.jiangyifen.ec2.entity.CustomerComplaintRecordStatus;
import com.jiangyifen.ec2.service.eaoservice.CustomerComplaintRecordStatusService;

public class CustomerComplaintRecordStatusServiceImpl implements CustomerComplaintRecordStatusService{
	private CustomerComplaintRecordStatusEao customerComplaintRecordStatusEao;
	
	@Override
	public CustomerComplaintRecordStatus get(Object primaryKey) {
		return customerComplaintRecordStatusEao.get(CustomerComplaintRecordStatus.class, primaryKey);
	}

	@Override
	public void save(CustomerComplaintRecordStatus customerComplaintRecordStatus) {
		customerComplaintRecordStatusEao.save(customerComplaintRecordStatus);
	}

	@Override
	public void update(CustomerComplaintRecordStatus customerComplaintRecordStatus) {
		customerComplaintRecordStatusEao.update(customerComplaintRecordStatus);
	}

	@Override
	public void delete(CustomerComplaintRecordStatus customerComplaintRecordStatus) {
		customerComplaintRecordStatusEao.delete(customerComplaintRecordStatus);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerComplaintRecordStatusEao.delete(CustomerComplaintRecordStatus.class, primaryKey);
	}
	
	//Getter and Setter
	public CustomerComplaintRecordStatusEao getCustomerComplaintRecordStatusEao() {
		return customerComplaintRecordStatusEao;
	}

	public void setCustomerComplaintRecordStatusEao(
			CustomerComplaintRecordStatusEao customerComplaintRecordStatusEao) {
		this.customerComplaintRecordStatusEao = customerComplaintRecordStatusEao;
	}
	

}
