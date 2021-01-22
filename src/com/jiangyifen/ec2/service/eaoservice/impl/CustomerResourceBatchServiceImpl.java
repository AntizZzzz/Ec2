package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerResourceBatchEao;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;

public class CustomerResourceBatchServiceImpl implements
		CustomerResourceBatchService {
	private CustomerResourceBatchEao customerResourceBatchEao;
	// enhance function method

	// common method
	@Override
	public CustomerResourceBatch get(Object primaryKey) {
		return customerResourceBatchEao.get(CustomerResourceBatch.class, primaryKey);
	}

	@Override
	public void save(
			CustomerResourceBatch customerResourceBatch) {
		customerResourceBatchEao.save(customerResourceBatch);
	}

	@Override
	public CustomerResourceBatch update(
			CustomerResourceBatch customerResourceBatch) {
		return (CustomerResourceBatch)customerResourceBatchEao.update(customerResourceBatch);
	}

	@Override
	public void delete(
			CustomerResourceBatch customerResourceBatch) {
		customerResourceBatchEao.delete(customerResourceBatch);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerResourceBatchEao.delete(CustomerResourceBatch.class, primaryKey);
	}


	
	//flip method
	@Override
	public int getEntityCount(String sql) {
		return customerResourceBatchEao.getEntityCount(sql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResourceBatch> loadPageEntities(int startIndex, int pageRecords,
			String selectSql) {
		return customerResourceBatchEao.loadPageEntities(startIndex, pageRecords, selectSql);
	}

	//getter and setter
	public CustomerResourceBatchEao getCustomerResourceBatchEao() {
		return customerResourceBatchEao;
	}

	public void setCustomerResourceBatchEao(
			CustomerResourceBatchEao customerResourceBatchEao) {
		this.customerResourceBatchEao = customerResourceBatchEao;
	}
	/**
	 * chb
	 * 根据域取出所有的批次
	 * @param domain
	 * @return
	 */
	@Override
	public List<CustomerResourceBatch> getAllBatches(Domain domain) {
		return customerResourceBatchEao.getAllBatches(domain);
	}
	
	/** jrh  根据客户资源的Id号和批次Id号， 检查资源是否已经在指定的批次当中   @param resourceId 客户资源的Id号  @param batchId 批次的Id号	 */
	@Override
	public boolean checkResourceExistedInBatch(Long resourceId, Long batchId) {
		return customerResourceBatchEao.checkResourceExistedInBatch(resourceId, batchId);
	}

	@Override
	public List<CustomerResourceBatch> getByName(String batchName, Domain domain) {
		return customerResourceBatchEao.getByName(batchName, domain);
	}
	
	@Override
	public CustomerResourceBatch getBatchByBatchName(String batchName, Domain domain) {
		List<CustomerResourceBatch> batchsList = customerResourceBatchEao.getByName(batchName, domain);
		if(batchsList == null || batchsList.size() == 0) {
			return null;
		}
		return batchsList.get(batchsList.size() - 1);
	}

}
