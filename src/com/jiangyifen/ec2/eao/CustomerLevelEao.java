package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.Domain;

public interface CustomerLevelEao extends BaseEao {
	
	/**
	 * chb 
	 * 取得所有的等级信息
	 * @param domain
	 * @return
	 */
	public List<CustomerLevel> getAll(Domain domain);


	/**
	 * jrh 
	 * 	在指定域中，获取所有高于或等于当前级别的CustomerLevel对象
	 * @param level		级别对象
	 * @param domain	指定的域
	 * @return
	 */
	public List<CustomerLevel> getAllSuperiorLevel(CustomerLevel level, Domain domain);
	
}
