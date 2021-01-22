package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ProvinceEao;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.service.eaoservice.ProvinceService;

public class ProvinceServiceImpl implements ProvinceService{
	
	private ProvinceEao provinceEao;
	
	// enhanced method
	
	public List<Province> getAll() {
		return provinceEao.getAll();
	}
	
	// common method 
	@Override
	public Province getProvince(Object primaryKey) {
		return provinceEao.get(Province.class, primaryKey);
	}

	@Override
	public void saveProvince(Province province) {
		provinceEao.save(province);
	}

	@Override
	public void updateProvince(Province province) {
		provinceEao.update(province);
	}

	@Override
	public void deleteProvince(Province province) {
		provinceEao.delete(province);
	}

	@Override
	public void deleteProvinceById(Object primaryKey) {
		provinceEao.delete(Province.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Province> loadPageEntitys(int start, int length, String sql) {
		return provinceEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return provinceEao.getEntityCount(sql);
	}

	//Getter and Setter
	public ProvinceEao getProvinceEao() {
		return provinceEao;
	}

	public void setProvinceEao(ProvinceEao provinceEao) {
		this.provinceEao = provinceEao;
	}
	
}
