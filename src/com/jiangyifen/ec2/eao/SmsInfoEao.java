package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.SmsInfo;

public interface SmsInfoEao extends BaseEao {
	public List<SmsInfo> getAll();
}
