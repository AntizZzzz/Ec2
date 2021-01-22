package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.QueueRequestDetail;

public interface QueueRequestDetailService {

	@Transactional
	public void save(QueueRequestDetail detail);
}
