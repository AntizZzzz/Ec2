package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CountyEao;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;
import com.jiangyifen.ec2.service.eaoservice.CountyService;

public class CountyServiceImpl implements CountyService{
	
	private CountyEao countyEao;
	
	// enhanced method

	public List<County> getAllByCity(City city) {
		return countyEao.getAllByCity(city);
	}
	
	// common method 

	@Override
	public County getCounty(Object primaryKey) {
		return countyEao.get(County.class, primaryKey);
	}

	@Override
	public void saveCounty(County county) {
		countyEao.save(county);
	}

	@Override
	public void updateCounty(County county) {
		countyEao.update(county);
	}

	@Override
	public void deleteCounty(County county) {
		countyEao.delete(county);
	}

	@Override
	public void deleteCountyById(Object primaryKey) {
		countyEao.delete(County.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<County> loadPageEntitys(int start, int length, String sql) {
		return countyEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return countyEao.getEntityCount(sql);
	}

	//Getter and Setter
	public CountyEao getCountyEao() {
		return countyEao;
	}

	public void setCountyEao(CountyEao countyEao) {
		this.countyEao = countyEao;
	}
	
}
