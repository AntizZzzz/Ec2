package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.CdrEao;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.service.eaoservice.CdrService;

public class CdrServiceImpl implements CdrService {
	
	private CdrEao cdrEao;

	// enhanced method 
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Cdr> loadPageEntities(int startIndex, int pageRecords, String selectSql) {
		if(selectSql == null || "".equals(selectSql)) {
			return new ArrayList<Cdr>();
		}
		return cdrEao.loadPageEntities(startIndex, pageRecords, selectSql);
	}

	@Override
	public int getEntityCount(String countSql) {
		if(countSql == null || "".equals(countSql)) {
			return 0;
		}
		return cdrEao.getEntityCount(countSql);
	}

	// common method 
	
	@Override
	public Cdr get(Object primaryKey) {
		return cdrEao.get(Cdr.class, primaryKey);
	}

	@Override
	public void save(Cdr cdr) {
		cdrEao.save(cdr);
	}

	@Override
	public Cdr update(Cdr cdr) {
		return (Cdr)cdrEao.update(cdr);
	}

	@Override
	public void delete(Cdr cdr) {
		cdrEao.delete(cdr);
	}

	@Override
	public void deleteById(Object primaryKey) {
		cdrEao.delete(Cdr.class, primaryKey);
	}

	/**
	 * chb
	 * 根据serviceRecord(客服记录取得CDR的集合)
	 * @param serviceRecord
	 */
	@Override
	public List<Cdr> getRecordByServiceRecord(
			CustomerServiceRecord serviceRecord) {
		return cdrEao.getRecordByServiceRecord(serviceRecord);
	}

	/**
	 * jrh	获取话务员最近的通话记录
	 * @param userId
	 * @param domainId
	 * @return
	 */
	@Override
	public Cdr getNearestCdrByUserId(Long userId, Long domainId) {
		return cdrEao.getNearestCdrByUserId(userId, domainId);
	}
	
	/**  jrh 根据JPQL 语句获取查询结果 */
	@Override
	public List<Cdr> getCdrByJpql(String jpql) {
		return cdrEao.getCdrByJpql(jpql);
	}

	/** <br/> jrh 根据原生Sql 获取 记录信息 <br/> @param nativeSql <br/> @return */
	@Override
	public List<Object[]> getInfosByNativeSql(String nativeSql) {
		return cdrEao.getInfosByNativeSql(nativeSql);
	}
	
	@Override
	public List<Cdr> getRecordByOrder(Order order) {
		return cdrEao.getRecordByOrder(order);
	}

	public CdrEao getCdrEao() {
		return cdrEao;
	}

	public void setCdrEao(CdrEao cdrEao) {
		this.cdrEao = cdrEao;
	}
	
}
