package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Province;

public interface CityService {
	
	// enhanced method
	
	/**
	 * jrh
	 *  获取指定省的所有城市
	 */
	public List<City> getAllByProvince(Province province);
	
	// common method 
	
	@Transactional
	public City getCity(Object primaryKey);
	
	@Transactional
	public void saveCity(City city);
	
	@Transactional
	public void updateCity(City city);
	
	@Transactional
	public void deleteCity(City city);
	
	@Transactional
	public void deleteCityById(Object primaryKey);
	
	@Transactional
	public List<City> loadPageEntitys(int start,int length,String sql);
	
	@Transactional
	public int getEntityCount(String sql);
}
