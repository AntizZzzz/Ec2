package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ProvinceEao;
import com.jiangyifen.ec2.entity.Province;

public class ProvinceEaoImpl extends BaseEaoImpl implements ProvinceEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Province> getAll() {
		return getEntityManager().createQuery("select p from Province as p").getResultList();
	}

}
