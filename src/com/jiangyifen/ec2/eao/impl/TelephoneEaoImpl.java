package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.TelephoneEao;
import com.jiangyifen.ec2.entity.Telephone;

public class TelephoneEaoImpl extends BaseEaoImpl implements TelephoneEao {
	/**
	 * chb
	 * 是否存在电话号码为*的电话，存在返回ture，不存在返回false
	 * @param phoneNumber
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Boolean isExistPhoneNumber(String phoneNumber,Long domainId) {
		String sql="select t from Telephone t where t.number='"+phoneNumber+"' and t.domain.id="+domainId;
		List<Telephone> phoneList=getEntityManager().createQuery(sql).getResultList();
		return phoneList.size()>0?true:false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Telephone getByNumber(String phoneNumber, Long domainId) {
		String otherPhoneNumber = "";	// 因为带0的电话和不带0的，实际属于一个电话
		if(phoneNumber.startsWith("0")) {
			otherPhoneNumber = phoneNumber.substring(1);
		} else {
			otherPhoneNumber = "0"+phoneNumber;
		}
		String sql="select t from Telephone t where (t.number='"+phoneNumber+"' or t.number='"+otherPhoneNumber+"') and t.domain.id="+domainId;
		List<Telephone> phoneList=getEntityManager().createQuery(sql).getResultList();
		if(phoneList.size() > 0) {
			return phoneList.get(0);
		}
		return null;
	}
	
	/**
	 * jrh 给电话号码加密后并返回
	 * @param mobileNo	电话号
	 * @return String   机密后的号码
	 */
	@Override
	public String encryptMobileNo(String mobileNo) {
		if(mobileNo == null) {
			return "";
		}
		
		int len = mobileNo.length();
		if(len >= 11){
			if(!mobileNo.startsWith("0")) {
				return mobileNo.substring(0, 3)+"******"+mobileNo.substring(9);
			} else {
				return mobileNo.substring(0, 4)+"******"+mobileNo.substring(10);
			}
		} else if(len > 8) {
			return mobileNo.substring(0, mobileNo.length()-7)+"****"+mobileNo.substring(mobileNo.length()-3);
		} else if(len >= 7){
			return mobileNo.substring(0, 2)+"****"+mobileNo.substring(6);
		} else {
			return mobileNo;
		}
	}
	
}
