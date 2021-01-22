package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerServiceRecordStatusNavigationKeyEao;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;

/**
 * 客服记录状态导航键配置持久层操作接口实现类
 * @author JHT
 * @date 2014-11-18 上午10:29:54
 */
public class CustomerServiceRecordStatusNavigationKeyEaoImpl extends BaseEaoImpl implements CustomerServiceRecordStatusNavigationKeyEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getAll(Long domainId) {
		String jpql = "select s from CustomerServiceRecordStatusNavigationKey as s where s.domain.id="+domainId;
		return getEntityManager().createQuery(jpql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getAllByEnable(boolean enabled, Long domainId) {
		String jpql = "select s from CustomerServiceRecordStatusNavigationKey as s where s.enabled="+enabled+" and s.domain.id="+domainId;
		return getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	public boolean existByInputKey(String direction, String inputKey, Long domainId) {
		String jpql = "select count(s) from CustomerServiceRecordStatusNavigationKey as s where s.inputKey='"+inputKey+"' and s.domain.id="+domainId+" and s.serviceRecordStatus.direction='"+direction+"'";
		Number count = (Number) getEntityManager().createQuery(jpql).getSingleResult();
		if(count.intValue()>0){
			return true;
		}
		return false;
	}
	
	@Override
	public boolean existByRecordStatus(Long recordStatusId, Long domainId){
		String jpql = "select count(s) from CustomerServiceRecordStatusNavigationKey as s where s.serviceRecordStatus.id="+recordStatusId+" and s.domain.id="+domainId;
		Number count = (Number) getEntityManager().createQuery(jpql).getSingleResult();
		if(count.intValue() > 0){
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getAllByServiceRecordState(boolean enabled, Long domainId) {
		String jpql = "select s from CustomerServiceRecordStatusNavigationKey as s where s.serviceRecordStatus.enabled="+enabled+" and s.domain.id="+domainId;
		return getEntityManager().createQuery(jpql).getResultList();
	}
	
	/**
	 * jinht
	 * 根据呼叫方向和按键配置查找对应的状态按键集合
	 * @param domainId 所在域
	 * @param inputKey 按键
	 * @param direction 呼叫方向
	 * @return List<CustomerServiceRecordStatusNavigationKey>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecordStatusNavigationKey> getServiceRecordNaviKeyByInfo(Long domainId, String inputKey, String direction){
		String jpql = "select s from CustomerServiceRecordStatusNavigationKey as s where s.domain.id="+domainId+" and s.inputKey='"+inputKey+"' " +
					" and s.serviceRecordStatus.direction='"+direction+"' and s.enabled="+true;	// 必须为可用状态
		return getEntityManager().createQuery(jpql).getResultList();
	}

}
