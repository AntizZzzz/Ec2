package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.Domain;

public interface CustomerLevelService {
	
	// enhanced method
	
	// common method
	
	@Transactional
	public CustomerLevel get(Object primaryKey);
	
	@Transactional
	public void save(CustomerLevel customerLevel);

	@Transactional
	public void update(CustomerLevel customerLevel);

	@Transactional
	public void delete(CustomerLevel customerLevel);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	@Transactional
	public List<CustomerLevel> loadPageEntitys(int start,int length,String sql);

	@Transactional
	public int getEntityCount(String sql);

	/**
	 * chb 
	 * 取得所有的等级信息
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<CustomerLevel> getAll(
			Domain domain);

	/**
	 * jrh 
	 * 	在指定域中，获取所有高于或等于当前级别的CustomerLevel对象
	 * @param level		级别对象
	 * @param domain	指定的域
	 * @return
	 */
	@Transactional
	public List<CustomerLevel> getAllSuperiorLevel(CustomerLevel level, Domain domain);

}
