package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Order;

public interface CdrEao extends BaseEao {
	
	/**
	 * chb
	 * 根据serviceRecord(客服记录取得CDR的集合)
	 * @param serviceRecord
	 */
	public List<Cdr> getRecordByServiceRecord(CustomerServiceRecord serviceRecord);

	/**
	 * jrh	获取话务员最近的通话记录
	 * @param userId
	 * @param domainId
	 * @return
	 */
	public Cdr getNearestCdrByUserId(Long userId, Long domainId);

	public List<Cdr> getRecordByOrder(Order order);


	/**
	 * jrh 根据JPQL 语句获取查询结果
	 * @param jpql
	 * @return
	 */
	public List<Cdr> getCdrByJpql(String jpql);
	

	/**
	 * jrh 根据原生Sql 获取 记录信息
	 * @param nativeSql
	 * @return
	 */
	public List<Object[]> getInfosByNativeSql(String nativeSql);
}
