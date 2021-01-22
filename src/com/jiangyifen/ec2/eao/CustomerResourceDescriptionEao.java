package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;


public interface CustomerResourceDescriptionEao extends BaseEao {
	/**
	 * chb
	 * 取得同一域内所有不同的Key值，以方便整理
	 * DistinctKeys
	 */
	public List<String> getDistinctKeys(Domain domain);
	
	/**
	 * chb
	 * @param toUpdateList 待更新的一组Key值
	 * @param updateTo 更新Key值为updateTo
	 * @param domain 域
	 */
	public void updateKey(List<String> toUpdateList, String updateTo, Domain domain);
	
	/**
	 * chb
	 * 导数据时对描述信息的查询
	 * @param key
	 * @param value
	 * @param resourceId
	 * @param domain
	 * @return
	 */
	public Boolean isExistDescription(String key, String value, Long resourceId, Long domainId);
	

	/**
	 * jrh
	 * 	获取某个资源的所有描述信息
	 * @param customerId	资源编号
	 * @return
	 */
	public List<CustomerResourceDescription> getAllByCustomerId(Long customerId);

	/**
	 * jinht
	 * 导数据时对描述信息的查询
	 * @param key
	 * @param value
	 * @param resourceId
	 * @param domainId
	 * @return
	 */
	public CustomerResourceDescription getExistDescription(String key, String value, Long resourceId, Long domainId);
}
