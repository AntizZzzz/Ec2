package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ResourceManageEao;

public class ResourceManageEaoImpl extends BaseEaoImpl implements
		ResourceManageEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getObjects(String nativeSql) {
		return getEntityManager().createNativeQuery(nativeSql).getResultList();
	}

	@Override
	public <T> List<T> getEntity(Class<T> entityClass, String jpql) {
		return getEntityManager().createQuery(jpql, entityClass).getResultList();
	}

	@Override
	public int executeUpdate(String nativeSql) {
		return getEntityManager().createNativeQuery(nativeSql).executeUpdate();
	}

	@Override
	public void deleteCustomerResourceAndLink(Long id) {
		getEntityManager().createNativeQuery("delete from ec2_customer_resource_ec2_customer_resource_batch where customerresources_id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("delete from ec2_customer_resource_description where customerresource_id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("delete from ec2_customer_service_record where customerresource_id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("delete from ec2_marketing_project_task where customerresource_id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("delete from ec2_telephone where customerresource_id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("update ec2_customer_resource set defaultaddress_id = null where id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("delete from ec2_address where customerresource_id = " + id).executeUpdate();
		getEntityManager().createNativeQuery("delete from ec2_project_customer where customer_resource_id = " + id).executeUpdate();
		// 删除资源
		getEntityManager().createNativeQuery("delete from ec2_customer_resource where id = " + id).executeUpdate();
	}

}
