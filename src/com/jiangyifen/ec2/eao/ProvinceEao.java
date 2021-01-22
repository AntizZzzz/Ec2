package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Province;

public interface ProvinceEao extends BaseEao {
	
	/**
	 * jrh
	 *  获取所有省
	 */
	public List<Province> getAll();
}
