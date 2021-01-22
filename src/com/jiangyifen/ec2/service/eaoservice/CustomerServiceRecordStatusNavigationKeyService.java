package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * 客服记录状态导航键配置业务层操作接口
 * @author JHT
 * @date 2014-11-18 上午10:43:28
 */
public interface CustomerServiceRecordStatusNavigationKeyService extends FlipSupportService<CustomerServiceRecordStatusNavigationKey> {

	/**
	 * jinht
	 *  取的所有客服记录状态导航键配置
	 * @param domain 所在域
	 * @return List<CustomerServiceRecordStatusNavigationKey>
	 */
	@Transactional
	public List<CustomerServiceRecordStatusNavigationKey> getAll(Domain domain);

	/**
	 * jinht
	 * 获取指定域下的所有指定状态(可用、停用)的导航键配置
	 * @param enabled 可用状态
	 * @param domain 所在域
	 * @return List<CustomerServiceRecordStatusNavigationKey>
	 */
	@Transactional
	public List<CustomerServiceRecordStatusNavigationKey> getAllByEnable(boolean enabled, Domain domain);
	
	@Transactional
	public void save(CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey);
	
	@Transactional
	public void update(CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey);
	
	@Transactional
	public void delete(CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	/**
	 * jinht
	 *  是否已经存在这个导航键
	 * @param direction 呼叫方向
	 * @param inputKey 导航键
	 * @param domain 所在域
	 * @return boolean
	 */
	public boolean existByInputKey(String direction, String inputKey, Domain domain);
	
	/**
	 * jinht
	 *  是否已经存在这个客服记录状态
	 * @param recordStatusId 客服记录状态ID
	 * @param domain 所在域
	 * @return boolean
	 */
	public boolean existByRecordStatus(Long recordStatusId, Domain domain);
	
	/**
	 * jinht
	 *  根据客服记录状态来查询客服记录导航键集合
	 * @param recordStatus 客服记录状态
	 * @param domain 所在域
	 * @return boolean
	 */
	public List<CustomerServiceRecordStatusNavigationKey> getAllByServiceRecordState(CustomerServiceRecordStatus recordStatus, Domain domain);
	
	/**
	 * jinht
	 * 根据呼叫方向和按键配置查找对应的状态按键集合
	 * @param domainId 所在域
	 * @param inputKey 按键
	 * @param direction 呼叫方向
	 * @return List<CustomerServiceRecordStatusNavigationKey>
	 */
	public List<CustomerServiceRecordStatusNavigationKey> getServiceRecordNaviKeyByInfo(Long domainId, String inputKey, String direction);
	
}
