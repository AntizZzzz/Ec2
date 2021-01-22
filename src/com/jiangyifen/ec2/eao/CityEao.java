package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Province;

public interface CityEao extends BaseEao {
	
	/**
	 * jrh
	 *  获取所有城市
	 */
	public List<City> getAllByProvince(Province province);
}
