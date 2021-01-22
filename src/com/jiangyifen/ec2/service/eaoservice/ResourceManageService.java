package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface ResourceManageService extends
FlipSupportService<CustomerResource> {

	@Transactional
	public List<String> getObjects(String nativeSql);

	@Transactional
	public <T> List<T> getEntity(Class<T> entityClass, String jpql);

	@Transactional
	public int executeUpdate(String nativeSql);

	@Transactional
	public void deleteBatch(Long id);

	@Transactional
	public void deleteCustomerResourceByListId(List<CustomerResource> list);

}
