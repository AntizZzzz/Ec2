package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Telephone;

public interface TelephoneService {

	
	// ========  enhanced method  ========//
	
	/**
	 * jrh 
	 * 	根据手机号获取电话号码从指定域中获取对象
	 * @param number
	 * @param domainId
	 * @return
	 */
	@Transactional
	public Telephone getByNumber(String number, Long domainId);

	/**
	 * jrh
	 * 	给电话号码加密后并返回
	 * @param mobileNo	电话号
	 * @return String   机密后的号码
	 */
	@Transactional
	public String encryptMobileNo(String mobileNo);
	
	// ========  common method  ========//
	
	@Transactional
	public Telephone get(Object primaryKey);
	
	@Transactional
	public void save(Telephone telephone);

	@Transactional
	public Telephone update(Telephone telephone);

	@Transactional
	public void delete(Telephone telephone);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
