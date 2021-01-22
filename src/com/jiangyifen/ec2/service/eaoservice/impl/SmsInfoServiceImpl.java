package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.SmsInfoEao;
import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.service.eaoservice.SmsInfoService;

public class SmsInfoServiceImpl implements SmsInfoService{
	private SmsInfoEao smsInfoEao;
	
	@Override
	public List<SmsInfo> getAll() {
		return smsInfoEao.getAll();
	}
	
	@Override
	@Transactional
	public SmsInfo getSmsInfo(Object primaryKey) {
		return smsInfoEao.get(SmsInfo.class, primaryKey);
	}
	
	@Override
	@Transactional
	public void saveSmsInfo(SmsInfo smsInfo) {
		smsInfoEao.save(smsInfo);
	}
	
	@Override
	@Transactional
	public void updateSmsInfo(SmsInfo smsInfo) {
		smsInfoEao.update(smsInfo);
	}
	
	@Override
	@Transactional
	public void deleteSmsInfo(SmsInfo smsInfo) {
		smsInfoEao.delete(smsInfo);
	}
	
	@Override
	@Transactional
	public void deleteSmsInfoById(Object primaryKey) {
		smsInfoEao.delete(SmsInfo.class, primaryKey);
	}
	
	

	//Getter and Setter
	public SmsInfoEao getSmsInfoEao() {
		return smsInfoEao;
	}

	public void setSmsInfoEao(SmsInfoEao smsInfoEao) {
		this.smsInfoEao = smsInfoEao;
	}
	
}
