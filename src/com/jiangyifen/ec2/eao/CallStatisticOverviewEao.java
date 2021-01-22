package com.jiangyifen.ec2.eao;

import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.report.entity.CallStatisticOverview;


public interface CallStatisticOverviewEao extends BaseEao {
	
	/**
	 * 获取日期时间最大的记录
	 * @return
	 */
	public CallStatisticOverview getLastestDateCallStatisticOverview();

	/**
	 * jrh 根据日期和小时获取呼叫统计概览对象
	 * @param startDate
	 * @param hour
	 * @param domainId
	 * @return
	 */
	public CallStatisticOverview getByDateAndHour(Date startDate, Integer hour, Long domainId);
	
	/**
	 * jrh 根据原生Sql 查询
	 * @param nativeSql
	 * @return
	 */
	public List<Object[]> getByNativeSql(String nativeSql);
	
}
