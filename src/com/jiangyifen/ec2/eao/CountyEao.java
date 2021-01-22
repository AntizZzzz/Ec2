package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;

public interface CountyEao extends BaseEao {
	
	/**
	 * jrh
	 *  获取所有区县
	 */
	public List<County> getAllByCity(City city);
}
