package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.jiangyifen.ec2.eao.CustomerResourceEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;

public class CustomerResourceServiceImpl implements CustomerResourceService {
	private CustomerResourceEao customerResourceEao;
	
	// enhance function method

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResource> loadPageEntities(int start, int length,
			String sql) {
		return customerResourceEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return customerResourceEao.getEntityCount(sql);
	}
	
	/**
	 * jrh 
	 * 	根据搜索语句获取相应的所有客户对象
	 * @param searchSql
	 * @return
	 */
	@Override
	public List<CustomerResource> getAllBySql(String searchSql) {
		return customerResourceEao.getAllBySql(searchSql);
	}
	
	// common method
	@Override
	public CustomerResource get(Object primaryKey) {
		return customerResourceEao.get(CustomerResource.class, primaryKey);
	}

	@Override
	public void save(CustomerResource customerResource) {
		customerResourceEao.save(customerResource);
	}

	@Override
	public CustomerResource update(CustomerResource customerResource) {
		return (CustomerResource) customerResourceEao.update(customerResource);
	}

	@Override
	public void delete(CustomerResource customerResource) {
		customerResourceEao.delete(customerResource);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerResourceEao.delete(CustomerResource.class, primaryKey);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public List<CustomerResource> loadPageEntitiesByNativeSql(int start, int length,
			String sql) {
		List<CustomerResource> resourceList=new ArrayList<CustomerResource>();
		
		List list=customerResourceEao.loadPageEntitiesByNativeSql(start, length, sql);
		for(int i=0;i<list.size();i++){
			Object[] objs=(Object[])list.get(i);
			Long id=(Long)objs[0];
			String name=(String)objs[1];
			String sex=(String)objs[2];
			String birthday=(String)objs[3];
			
			//TelephoneList集合 
			@SuppressWarnings("unchecked")
			List<Telephone> phoneList=customerResourceEao.getEntityManager().createQuery("select t from Telephone t where t.customerResource.id="+id).getResultList();
			CustomerResource customerResource=new CustomerResource();
			customerResource.setId(id);
			customerResource.setName(name);
			customerResource.setSex(sex);
			customerResource.setBirthdayStr(birthday);
			customerResource.setTelephones(new HashSet<Telephone>(phoneList));
			
			resourceList.add(customerResource);
		}
		return resourceList;
	}

	@Override
	public int getEntityCountByNativeSql(String sql) {
		return customerResourceEao.getEntityCountByNativeSql(sql);
	}

	//getter and setter
	public CustomerResourceEao getCustomerResourceEao() {
		return customerResourceEao;
	}

	public void setCustomerResourceEao(CustomerResourceEao customerResourceEao) {
		this.customerResourceEao = customerResourceEao;
	}
	/**
	 * chb
	 * 根据批次查找批次对应的资源
	 * @param batch
	 * @return
	 */
	@Override
	public List<CustomerResource> getCustomerResourceByBatch(
			CustomerResourceBatch batch) {
		return customerResourceEao.getCustomerResourceByBatch(batch);
	}

	/**
	 * 根据电话号码和域取得CustomerResource
	 * @param customerPhoneNumber
	 * @param id
	 */
	@Override
	public CustomerResource getCustomerResourceByPhoneNumber(String customerPhoneNumber, Long domainId) {
		return customerResourceEao.getCustomerResourceByPhoneNumber(customerPhoneNumber, domainId);
	}

}
