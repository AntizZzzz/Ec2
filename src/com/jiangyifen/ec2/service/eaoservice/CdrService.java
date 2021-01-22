package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface CdrService extends FlipSupportService<Cdr> {

	// common
	@Transactional
	public Cdr get(Object primaryKey);
	
	@Transactional
	public void save(Cdr cdr);

	@Transactional
	public Cdr update(Cdr cdr);

	@Transactional
	public void delete(Cdr cdr);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
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
	@Transactional
	public Cdr getNearestCdrByUserId(Long userId, Long domainId);

	/**
	 * 通过订单取得最近24小时的CDR
	 * @param order
	 * @return
	 */
	@Transactional
	public List<Cdr> getRecordByOrder(Order order);

	/**
	 * jrh 根据JPQL 语句获取查询结果
	 * @param jpql
	 * @return
	 */
	@Transactional
	public List<Cdr> getCdrByJpql(String jpql);

	/**
	 * jrh 根据原生Sql 获取 记录信息
	 * @param nativeSql
	 * @return
	 */
	@Transactional
	public List<Object[]> getInfosByNativeSql(String nativeSql);
	
}
