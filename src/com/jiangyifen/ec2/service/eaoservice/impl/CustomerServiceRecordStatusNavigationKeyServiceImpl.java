package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerServiceRecordStatusNavigationKeyEao;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusNavigationKeyService;

/**
 * 客服记录状态导航键配置业务层操作接口实现类
 * @author JHT
 * @date 2014-11-18 上午10:50:53
 */
public class CustomerServiceRecordStatusNavigationKeyServiceImpl implements CustomerServiceRecordStatusNavigationKeyService {

	private CustomerServiceRecordStatusNavigationKeyEao customerServiceRecordStatusNavigationKeyEao;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> loadPageEntities(int start, int length, String sql) {
		return customerServiceRecordStatusNavigationKeyEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return customerServiceRecordStatusNavigationKeyEao.getEntityCount(sql);
	}

	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getAll(Domain domain) {
		return customerServiceRecordStatusNavigationKeyEao.getAll(domain.getId());
	}

	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getAllByEnable(boolean enabled, Domain domain) {
		return customerServiceRecordStatusNavigationKeyEao.getAllByEnable(enabled, domain.getId());
	}
	
	@Override
	public void save(CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey){
		customerServiceRecordStatusNavigationKeyEao.save(serviceRecordStatusNavigationKey);
	}
	
	@Override
	public void update(CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey){
		customerServiceRecordStatusNavigationKeyEao.update(serviceRecordStatusNavigationKey);
	}
	
	@Override
	public void delete(CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey){
		customerServiceRecordStatusNavigationKeyEao.delete(serviceRecordStatusNavigationKey);
	}

	@Override
	public void deleteById(Object primaryKey) {
		customerServiceRecordStatusNavigationKeyEao.delete(CustomerServiceRecordStatusNavigationKey.class, primaryKey);
	}

	@Override
	public boolean existByInputKey(String direction, String inputKey, Domain domain) {
		return customerServiceRecordStatusNavigationKeyEao.existByInputKey(direction, inputKey, domain.getId());
	}
	
	@Override
	public boolean existByRecordStatus(Long recordStatusId, Domain domain){
		return customerServiceRecordStatusNavigationKeyEao.existByRecordStatus(recordStatusId, domain.getId());
	}
	
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getAllByServiceRecordState(CustomerServiceRecordStatus recordStatus, Domain domain) {
		return customerServiceRecordStatusNavigationKeyEao.getAllByServiceRecordState(recordStatus.getEnabled(), domain.getId());
	}
	
	public CustomerServiceRecordStatusNavigationKeyEao getCustomerServiceRecordStatusNavigationKeyEao() {
		return customerServiceRecordStatusNavigationKeyEao;
	}

	public void setCustomerServiceRecordStatusNavigationKeyEao(CustomerServiceRecordStatusNavigationKeyEao customerServiceRecordStatusNavigationKeyEao) {
		this.customerServiceRecordStatusNavigationKeyEao = customerServiceRecordStatusNavigationKeyEao;
	}
	
	/**
	 * jinht
	 * 根据呼叫方向和按键配置查找对应的状态按键集合
	 * @param domainId 所在域
	 * @param inputKey 按键
	 * @param direction 呼叫方向
	 * @return List<CustomerServiceRecordStatusNavigationKey>
	 */
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getServiceRecordNaviKeyByInfo(Long domainId, String inputKey, String direction){
		return customerServiceRecordStatusNavigationKeyEao.getServiceRecordNaviKeyByInfo(domainId, inputKey, direction);
	}

}
