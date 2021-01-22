package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.UserOutline;


public interface UserOutlineEao extends BaseEao {
	
	/**
	 * 获取一个域中所有的用户与外线的对应关系
	 * @param domain 域
	 * @return List<UserOutline> 返回整个域中用户与外线的关系集合
	 */
	public List<UserOutline> getAllByDomain(Domain domain);

	/**
	 * 根据外线对象及域，获取指定域下，单条外线的所有成员
	 * @param outline	外线对象
	 * @param domain	域对象
	 * @return List<UserOutline> 返回存放用户与外线的关系集合
	 */
	public List<UserOutline> getAllByOutline(SipConfig outline, Domain domain);

	/**
	 * 取出指定用户对应的静态外线
	 * @param loginUser 用户对象的Id值
	 * @param domain	域对象的Id值
	 * @return
	 */
	public UserOutline getByUserId(Long userId, Long domainId);
	
}
