package com.jiangyifen.ec2.eao;

import com.jiangyifen.ec2.entity.Telephone;

public interface TelephoneEao extends BaseEao {
	/**
	 * chb
	 * 是否存在电话号码为*的电话，存在返回ture，不存在返回false
	 * @param phoneNumber
	 * @return
	 */
	public Boolean isExistPhoneNumber(String phoneNumber,Long domainId);

	/**
	 * jrh 根据手机号获取电话号码从指定域中获取对象
	 * @param number
	 * @param domainId
	 * @return
	 */
	public Telephone getByNumber(String number, Long domainId);
	
	/**
	 * jrh 给电话号码加密后并返回
	 * @param mobileNo	电话号
	 * @return String   机密后的号码
	 */
	public String encryptMobileNo(String mobileNo);

//	/**
//	 * jrh 获取指定域中的所有电话对象
//	 * @param domainId			指定域的id号
//	 * @return List<Telephone>  返回的电话对象集合
//	 */
//	public List<Telephone> getAllByDomainId(Long domainId);

}
