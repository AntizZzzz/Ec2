package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CityEao;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.service.eaoservice.CityService;

public class CityServiceImpl implements CityService{
	
	private CityEao cityEao;
	
	// enhanced method
	
	public List<City> getAllByProvince(Province province) {
		return cityEao.getAllByProvince(province);
	}
	
	// common method 
	
	@Override
	public City getCity(Object primaryKey) {
		return cityEao.get(City.class, primaryKey);
	}

	@Override
	public void saveCity(City city) {
		cityEao.save(city);
	}

	@Override
	public void updateCity(City city) {
		cityEao.update(city);
	}

	@Override
	public void deleteCity(City city) {
		cityEao.delete(city);
	}

	@Override
	public void deleteCityById(Object primaryKey) {
		cityEao.delete(City.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<City> loadPageEntitys(int start, int length, String sql) {
		return cityEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return cityEao.getEntityCount(sql);
	}

	//Getter and Setter
	public CityEao getCityEao() {
		return cityEao;
	}

	public void setCityEao(CityEao cityEao) {
		this.cityEao = cityEao;
	}
	
}
