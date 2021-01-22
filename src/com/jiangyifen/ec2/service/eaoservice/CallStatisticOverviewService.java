package com.jiangyifen.ec2.service.eaoservice;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.report.entity.CallStatisticOverview;
import com.jiangyifen.ec2.service.common.FlipOverNativeSqlService;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface CallStatisticOverviewService extends FlipSupportService<CallStatisticOverview>,FlipOverNativeSqlService {

	// common method 
	
	@Transactional
	public CallStatisticOverview get(Object primaryKey);
	
	@Transactional
	public void save(CallStatisticOverview notice);

	@Transactional
	public CallStatisticOverview update(CallStatisticOverview notice);

	@Transactional
	public void delete(CallStatisticOverview notice);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	// enhanced method
	/**
	 * 获取日期时间最大的记录
	 * @return
	 */
	@Transactional
	public CallStatisticOverview getLastestDateCallStatisticOverview();

	/**
	 * jrh 根据日期和小时获取呼叫统计概览对象
	 * @param startDate
	 * @param hour
	 * @param domainId
	 * @return
	 */
	@Transactional
	public CallStatisticOverview getByDateAndHour(Date startDate, Integer hour, Long domainId);

	/**
	 * jrh 根据原生Sql 查询
	 * @param nativeSql
	 * @return
	 */
	@Transactional
	public List<Object[]> getByNativeSql(String nativeSql);
	
}
