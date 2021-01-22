package com.jiangyifen.ec2.eao;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.UserLoginRecord;

public interface UserLoginRecordEao extends BaseEao {
	
	//========= enhanced method ========/
	
	/**
	 * 获取指定用户的最近一条 登录记录
	 *  该记录的登录时间不为空，但是退出的时间为空，并且该记录之后不存在指定用户做的“登录与退出时间均不为空的记录”
	 *  也就是说，获取的记录时指定用户用最后做的一条登录记录
	 * @param username	用户名
	 * @return UserLoginRecord 登录记录
	 */
	@Transactional
	public UserLoginRecord getLastRecord(String username);
}
