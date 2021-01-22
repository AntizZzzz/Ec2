package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.CallStatisticOverviewEao;
import com.jiangyifen.ec2.report.entity.CallStatisticOverview;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;

public class CallStatisticOverviewServiceImpl implements CallStatisticOverviewService {
	
	private CallStatisticOverviewEao callStatisticOverviewEao;
	
	// enhanced method
	
	/** 获取日期时间最大的记录 */
	@Override
	public CallStatisticOverview getLastestDateCallStatisticOverview() {
		return callStatisticOverviewEao.getLastestDateCallStatisticOverview();
	}
	
	/** jrh 根据日期和小时获取呼叫统计概览对象  <br/>@param startDate <br/>@param hour <br/>@param domainId <br/>@return */
	@Override
	public CallStatisticOverview getByDateAndHour(Date startDate, Integer hour, Long domainId) {
		return callStatisticOverviewEao.getByDateAndHour(startDate, hour, domainId);
	}
	
	/** jrh 根据原生Sql 查询 <br/>@param nativeSql <br/> @return */
	public List<Object[]> getByNativeSql(String nativeSql) {
		return callStatisticOverviewEao.getByNativeSql(nativeSql);
	}

	// --------------------- JPQL 翻页组件方法 ------------------------//
	@SuppressWarnings("unchecked")
	@Override
	public List<CallStatisticOverview> loadPageEntities(int start, int length, String sql) {
		return callStatisticOverviewEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return callStatisticOverviewEao.getEntityCount(sql);
	}

	// --------------------- NativeSQL 翻页组件方法 ------------------------//
	@Override
	public List<Object[]> loadPageInfosByNativeSql(int startIndex,
			int pageSize, String nativeSelectSql) {
		return callStatisticOverviewEao.loadPageInfosByNativeSql(startIndex, pageSize, nativeSelectSql);
	}

	@Override
	public int getInfoCountByNativeSql(String nativeCountSql) {
		return callStatisticOverviewEao.getInfoCountByNativeSql(nativeCountSql);
	}
	
	// common method

	@Override
	public CallStatisticOverview get(Object primaryKey) {
		return callStatisticOverviewEao.get(CallStatisticOverview.class, primaryKey);
	}

	@Override
	public void save(CallStatisticOverview callStatisticOverview) {
		callStatisticOverviewEao.save(callStatisticOverview);
	}

	@Override
	public CallStatisticOverview update(CallStatisticOverview callStatisticOverview) {
		return (CallStatisticOverview) callStatisticOverviewEao.update(callStatisticOverview);
	}

	@Override
	public void delete(CallStatisticOverview callStatisticOverview) {
		callStatisticOverviewEao.delete(callStatisticOverview);
	}

	@Override
	public void deleteById(Object primaryKey) {
		callStatisticOverviewEao.delete(CallStatisticOverview.class, primaryKey);
	}

	public CallStatisticOverviewEao getCallStatisticOverviewEao() {
		return callStatisticOverviewEao;
	}

	public void setCallStatisticOverviewEao(CallStatisticOverviewEao callStatisticOverviewEao) {
		this.callStatisticOverviewEao = callStatisticOverviewEao;
	}

}
