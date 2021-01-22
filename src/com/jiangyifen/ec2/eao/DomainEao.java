package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;



public interface DomainEao extends BaseEao {

	/**
	 * chb 取得所有的域
	 */
	public List<Domain> getAll();
}
