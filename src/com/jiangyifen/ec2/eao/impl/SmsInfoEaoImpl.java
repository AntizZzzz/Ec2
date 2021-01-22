package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.SmsInfoEao;
import com.jiangyifen.ec2.entity.SmsInfo;

public class SmsInfoEaoImpl extends BaseEaoImpl implements SmsInfoEao {
	@SuppressWarnings("unchecked")
	@Override
	public List<SmsInfo> getAll() {
		return getEntityManager().createQuery("select s from SmsInfo as s order by s.id desc").getResultList();
	}

}
