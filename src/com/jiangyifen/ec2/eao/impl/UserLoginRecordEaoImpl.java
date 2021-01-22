package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserLoginRecordEao;
import com.jiangyifen.ec2.entity.UserLoginRecord;

public class UserLoginRecordEaoImpl extends BaseEaoImpl implements
		UserLoginRecordEao {

	// ========= enhanced method ========/

	@SuppressWarnings("unchecked")
	@Override
	public UserLoginRecord getLastRecord(String username) {
		List<UserLoginRecord> loginRecordList = getEntityManager()
				.createQuery(
					"select r from UserLoginRecord as r where r.username ='"
							+ username + "' and r.loginDate is not null and r.logoutDate is null " 
							+ "and NOT EXISTS ( select r1 from UserLoginRecord as r1 where r1.username ='"
							+ username + "' and r1.loginDate is not null and r1.logoutDate is not null " 
							+ "and r1.loginDate > r.loginDate )"
							+ "order by r.loginDate desc")
				.getResultList();
		
		if(loginRecordList.size() > 0) {
			return loginRecordList.get(0); 
		}
		return null;
	}
}
