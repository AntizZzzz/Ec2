package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CountyEao;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;

public class CountyEaoImpl extends BaseEaoImpl implements CountyEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<County> getAllByCity(City city) {
		return getEntityManager().createQuery("select c from County as c where c.city.id = " + city.getId()).getResultList();
	}

}
