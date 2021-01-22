package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.QueueRequestDetailEao;
import com.jiangyifen.ec2.entity.QueueRequestDetail;
import com.jiangyifen.ec2.service.eaoservice.QueueRequestDetailService;

public class QueueRequestDetailServiceImpl implements QueueRequestDetailService {

	private QueueRequestDetailEao queueRequestDetailEao;

	@Override
	public void save(QueueRequestDetail detail) {
		queueRequestDetailEao.save(detail);
	}

	public QueueRequestDetailEao getQueueRequestDetailEao() {
		return queueRequestDetailEao;
	}

	public void setQueueRequestDetailEao(
			QueueRequestDetailEao queueRequestDetailEao) {
		this.queueRequestDetailEao = queueRequestDetailEao;
	}

}
