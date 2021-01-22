package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;

public interface CountyService {
	
	// enhanced method
	
	/**
	 * jrh
	 *  获取指定城市的所有区县
	 */
	public List<County> getAllByCity(City city);
	
	// common method 
	
	@Transactional
	public County getCounty(Object primaryKey);
	
	@Transactional
	public void saveCounty(County county);
	
	@Transactional
	public void updateCounty(County county);
	
	@Transactional
	public void deleteCounty(County county);
	
	@Transactional
	public void deleteCountyById(Object primaryKey);
	
	@Transactional
	public List<County> loadPageEntitys(int start,int length,String sql);
	
	@Transactional
	public int getEntityCount(String sql);
}
