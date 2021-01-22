package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.report.entity.AutodialDetail;

public interface AutodialDetailService {

	@Transactional
	public void save(AutodialDetail detail);
}
