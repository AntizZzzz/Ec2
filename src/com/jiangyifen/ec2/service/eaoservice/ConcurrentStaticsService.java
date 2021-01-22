package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.ConcurrentStatics;

public interface ConcurrentStaticsService {

	@Transactional
	public ConcurrentStatics get(Object primaryKey);

	@Transactional
	public void save(ConcurrentStatics concurrentStatics);

	@Transactional
	public void update(ConcurrentStatics concurrentStatics);

	@Transactional
	public void delete(ConcurrentStatics concurrentStatics);

	@Transactional
	public void deleteById(Object primaryKey);

	@Transactional
	public List<ConcurrentStatics> loadPageEntitys(int start,int length,String sql);

	@Transactional
	public int getEntityCount(String sql);

}
