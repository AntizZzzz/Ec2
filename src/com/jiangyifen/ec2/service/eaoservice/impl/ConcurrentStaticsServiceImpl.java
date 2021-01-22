package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ConcurrentStaticsEao;
import com.jiangyifen.ec2.entity.ConcurrentStatics;
import com.jiangyifen.ec2.service.eaoservice.ConcurrentStaticsService;

public class ConcurrentStaticsServiceImpl implements ConcurrentStaticsService {
	private ConcurrentStaticsEao concurrentStaticsEao;
	// enhance function method

	// common method
	@Override
	public ConcurrentStatics get(Object primaryKey) {
		return concurrentStaticsEao.get(ConcurrentStatics.class, primaryKey);
	}

	@Override
	public void save(ConcurrentStatics concurrentStatics) {
		concurrentStaticsEao.save(concurrentStatics);
	}

	@Override
	public void update(ConcurrentStatics concurrentStatics) {
		concurrentStaticsEao.update(concurrentStatics);
	}

	@Override
	public void delete(ConcurrentStatics concurrentStatics) {
		concurrentStaticsEao.delete(concurrentStatics);
	}

	@Override
	public void deleteById(Object primaryKey) {
		concurrentStaticsEao.delete(ConcurrentStatics.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ConcurrentStatics> loadPageEntitys(int start, int length, String sql) {
		return concurrentStaticsEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return concurrentStaticsEao.getEntityCount(sql);
	}

	//getter and setter
	public ConcurrentStaticsEao getConcurrentStaticsEao() {
		return concurrentStaticsEao;
	}

	public void setConcurrentStaticsEao(ConcurrentStaticsEao concurrentStaticsEao) {
		this.concurrentStaticsEao = concurrentStaticsEao;
	}

}
