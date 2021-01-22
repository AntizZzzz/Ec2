package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.UserLoginRecordEao;
import com.jiangyifen.ec2.entity.UserLoginRecord;
import com.jiangyifen.ec2.service.eaoservice.UserLoginRecordService;

public class UserLoginRecordServiceImpl implements UserLoginRecordService {

	private UserLoginRecordEao userLoginRecordEao;
	
	//========= enhanced method ========/

	@Override
	public UserLoginRecord getLastRecord(String username) {
		return userLoginRecordEao.getLastRecord(username);
	}

	//========= common method ========/

	@Override
	public UserLoginRecord get(Object primaryKey) {
		return userLoginRecordEao.get(UserLoginRecord.class, primaryKey);
	}

	@Override
	public void save(UserLoginRecord userLoginRecord) {
		userLoginRecordEao.save(userLoginRecord);
	}

	@Override
	public void update(UserLoginRecord userLoginRecord) {
		userLoginRecordEao.update(userLoginRecord);
	}

	@Override
	public void delete(UserLoginRecord userLoginRecord) {
		userLoginRecordEao.delete(userLoginRecord);
	}

	@Override
	public void deleteById(Object primaryKey) {
		userLoginRecordEao.delete(UserLoginRecord.class, primaryKey);
	}

	//======== getter setter ========/
	
	public UserLoginRecordEao getUserLoginRecordEao() {
		return userLoginRecordEao;
	}

	public void setUserLoginRecordEao(UserLoginRecordEao userLoginRecordEao) {
		this.userLoginRecordEao = userLoginRecordEao;
	}

}
