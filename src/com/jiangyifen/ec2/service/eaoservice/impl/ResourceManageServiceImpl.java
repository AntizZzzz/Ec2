package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ResourceManageEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.service.eaoservice.ResourceManageService;

public class ResourceManageServiceImpl implements ResourceManageService {

	private ResourceManageEao resourceManageEao;

	@Override
	public List<String> getObjects(String nativeSql) {

		return resourceManageEao.getObjects(nativeSql);
	}

	@Override
	public <T> List<T> getEntity(Class<T> entityClass, String jpql) {

		return resourceManageEao.getEntity(entityClass, jpql);
	}

	public ResourceManageEao getResourceManageEao() {
		return resourceManageEao;
	}

	public void setResourceManageEao(ResourceManageEao resourceManageEao) {
		this.resourceManageEao = resourceManageEao;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CustomerResource> loadPageEntities(int start, int length,String sql) {
		return resourceManageEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return resourceManageEao.getEntityCount(sql);
	}

	@Override
	public int executeUpdate(String nativeSql) {
		return resourceManageEao.executeUpdate(nativeSql);
	}

	@Override
	public void deleteBatch(Long id) {
		resourceManageEao.deleteCustomerResourceAndLink(id);
	}

	@Override
	public void deleteCustomerResourceByListId(List<CustomerResource> list) {
		for (CustomerResource customerResource : list) {
			resourceManageEao.deleteCustomerResourceAndLink(customerResource.getId());
		}
	}

}
