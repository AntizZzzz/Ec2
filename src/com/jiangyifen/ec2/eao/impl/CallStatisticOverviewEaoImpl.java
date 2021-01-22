package com.jiangyifen.ec2.eao.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.CallStatisticOverviewEao;
import com.jiangyifen.ec2.report.entity.CallStatisticOverview;

public class CallStatisticOverviewEaoImpl extends BaseEaoImpl implements CallStatisticOverviewEao {
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	/** 获取日期时间最大的记录 */
	@SuppressWarnings("unchecked")
	@Override
	public CallStatisticOverview getLastestDateCallStatisticOverview() {
		String sql = "select e from CallStatisticOverview as e where e.createDate = " +
				"(select max(e1.createDate) from CallStatisticOverview as e1) " +
				"and e.hour = (select max(e2.createDate) from CallStatisticOverview as e2)";
		List<CallStatisticOverview> list = this.getEntityManager().createQuery(sql).getResultList();
		if(list.size() > 0) {	// 一般不会出现记录数大于1
			list.get(0);
		}
		return null;
	}

	/** jrh 根据日期和小时获取呼叫统计概览对象  <br/>@param startDate <br/>@param hour <br/>@param domainId <br/>@return */
	@SuppressWarnings("unchecked")
	@Override
	public CallStatisticOverview getByDateAndHour(Date startDate, Integer hour, Long domainId) {
		String sql = "select e from CallStatisticOverview as e where e.startDate = '" +sdf.format(startDate)+ "' and e.hour = "+hour+" and e.domainId = "+domainId;
		List<CallStatisticOverview> list = this.getEntityManager().createQuery(sql).getResultList();
		if(list.size() > 0) {	// 一般不会出现记录数大于1
			return list.get(0);
		}
		return null;
	}
	
	/** jrh 根据原生Sql 查询 <br/>@param nativeSql <br/> @return */
	@SuppressWarnings("unchecked")
	public List<Object[]> getByNativeSql(String nativeSql) {
		return this.getEntityManager().createNativeQuery(nativeSql).getResultList();
	}
	
}
