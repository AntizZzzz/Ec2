package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Province;


public interface ProvinceService {
	
	// enhanced method
	
	/**
	 * jrh
	 *  获取所有省
	 */
	public List<Province> getAll();
	
	// common method 
	
	@Transactional
	public Province getProvince(Object primaryKey);
	
	@Transactional
	public void saveProvince(Province province);

	@Transactional
	public void updateProvince(Province province);

	@Transactional
	public void deleteProvince(Province province);
	
	@Transactional
	public void deleteProvinceById(Object primaryKey);
	
	@Transactional
	public List<Province> loadPageEntitys(int start,int length,String sql);

	@Transactional
	public int getEntityCount(String sql);
}
