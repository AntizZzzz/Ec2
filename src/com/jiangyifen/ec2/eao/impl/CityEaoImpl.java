package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CityEao;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Province;

public class CityEaoImpl extends BaseEaoImpl implements CityEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<City> getAllByProvince(Province province) {
		return getEntityManager().createQuery("select c from City as c where c.province.id = " + province.getId()).getResultList();
	}

}
