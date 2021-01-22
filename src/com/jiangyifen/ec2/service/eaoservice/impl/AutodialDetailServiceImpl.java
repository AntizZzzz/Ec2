package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.AutodialDetailEao;

import com.jiangyifen.ec2.report.entity.AutodialDetail;
import com.jiangyifen.ec2.service.eaoservice.AutodialDetailService;

public class AutodialDetailServiceImpl implements AutodialDetailService {

	private AutodialDetailEao autodialDetailEao;

	@Override
	public void save(AutodialDetail detail) {
		autodialDetailEao.save(detail);
	}

	public AutodialDetailEao getAutodialDetailEao() {
		return autodialDetailEao;
	}

	public void setAutodialDetailEao(AutodialDetailEao autodialDetailEao) {
		this.autodialDetailEao = autodialDetailEao;
	}

}
