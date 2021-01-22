package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.SmsInfo;

public interface SmsInfoService {
	
	// enhanced method
	public List<SmsInfo> getAll();
	
	// common method 
	@Transactional
	public SmsInfo getSmsInfo(Object primaryKey);
	
	@Transactional
	public void saveSmsInfo(SmsInfo smsInfo);
	
	@Transactional
	public void updateSmsInfo(SmsInfo smsInfo);
	
	@Transactional
	public void deleteSmsInfo(SmsInfo smsInfo);
	
	@Transactional
	public void deleteSmsInfoById(Object primaryKey);
}
