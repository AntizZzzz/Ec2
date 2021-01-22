package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.TelephoneEao;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;

public class TelephoneServiceImpl implements TelephoneService {
	
	private TelephoneEao telephoneEao;
	
	// ========  enhanced method  ========//
	
	@Override
	public Telephone getByNumber(String number, Long domainId) {
		if(number == null) {
			return null;
		}
		return telephoneEao.getByNumber(number, domainId);
	}
	
	/**
	 * jrh 给电话号码加密后并返回
	 * @param mobileNo	电话号
	 * @return String   机密后的号码
	 */
	public String encryptMobileNo(String mobileNo) {
		return telephoneEao.encryptMobileNo(mobileNo);
	}
	
	//========  common method ========//
	@Override
	public Telephone get(Object primaryKey) {
		return telephoneEao.get(Telephone.class, primaryKey);
	}

	@Override
	public void save(Telephone telephone) {
		telephoneEao.save(telephone);
	}

	@Override
	public Telephone update(Telephone telephone) {
		return (Telephone) telephoneEao.update(telephone);
	}

	@Override
	public void delete(Telephone telephone) {
		telephoneEao.delete(telephone);
	}

	@Override
	public void deleteById(Object primaryKey) {
		telephoneEao.delete(Telephone.class, primaryKey);
	}

	//==========  getter setter  ==========//
	
	public TelephoneEao getTelephoneEao() {
		return telephoneEao;
	}
	
	public void setTelephoneEao(TelephoneEao telephoneEao) {
		this.telephoneEao = telephoneEao;
	}
}
