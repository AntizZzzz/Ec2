package com.jiangyifen.ec2.eao;

import java.util.List;

public interface ResourceManageEao extends BaseEao {

	public List<String> getObjects(String nativeSql);

	public <T> List<T> getEntity(Class<T> entityClass, String jpql);

	public int executeUpdate(String nativeSql);

	public void deleteCustomerResourceAndLink(Long id);
	
}
