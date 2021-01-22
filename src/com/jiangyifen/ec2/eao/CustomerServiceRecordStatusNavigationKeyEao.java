package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;

/**
 * 客服记录状态导航键配置持久层操作接口
 * @author JHT
 * @date 2014-11-18 上午9:59:51
 */
public interface CustomerServiceRecordStatusNavigationKeyEao extends BaseEao {

	/**
	 * jinht
	 * 取得所有的导航键配置信息
	 * @param domainId
	 * @return
	 */
	public List<CustomerServiceRecordStatusNavigationKey> getAll(Long domainId);
	
	/**
	 * jinht
	 *  获取指定域下的所有指定状态(可用、停用)的导航键配置
	 * @param enabled 可用状态
	 * @param domainId 指定域的ID
	 * @return List<CustomerServiceRecordStatusNavigationKey>
	 */
	public List<CustomerServiceRecordStatusNavigationKey> getAllByEnable(boolean enabled, Long domainId);
	
	/**
	 * jinht
	 *  是否已经存在这个导航键
	 * @param direction 呼叫方向
	 * @param inputKey 导航键
	 * @param domain 所在域
	 * @return boolean
	 */
	public boolean existByInputKey(String direction, String inputKey, Long domainId);
	
	/**
	 * jinht
	 *  是否已经存在这个客服记录状态
	 * @param recordStatusId 客服记录状态的ID
	 * @param domainId 所在域
	 * @return boolean
	 */
	public boolean existByRecordStatus(Long recordStatusId, Long domainId);
	
	/**
	 * jinht
	 *  根据客服记录状态来查询客服记录导航键集合
	 * @param recordStatus 客服记录状态
	 * @param domain 所在域
	 * @return boolean
	 */
	public List<CustomerServiceRecordStatusNavigationKey> getAllByServiceRecordState(boolean enabled, Long domainId);
	
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
