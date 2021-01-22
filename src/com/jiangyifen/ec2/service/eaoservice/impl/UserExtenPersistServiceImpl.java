package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserExtenPersistEao;
import com.jiangyifen.ec2.entity.UserExtenPersist;
import com.jiangyifen.ec2.service.eaoservice.UserExtenPersistService;

public class UserExtenPersistServiceImpl implements UserExtenPersistService {
	private UserExtenPersistEao userExtenPersistEao;
	// enhance function method

	// common method
	@Override
	public UserExtenPersist getUserExtenPersist(Object primaryKey) {
		return userExtenPersistEao.get(UserExtenPersist.class, primaryKey);
	}

	@Override
	public void saveUserExtenPersist(UserExtenPersist userExtenPersist) {
		userExtenPersistEao.save(userExtenPersist);
	}

	@Override
	public void updateUserExtenPersist(UserExtenPersist userExtenPersist) {
		userExtenPersistEao.update(userExtenPersist);
	}

	@Override
	public void deleteUserExtenPersist(UserExtenPersist userExtenPersist) {
		userExtenPersistEao.delete(userExtenPersist);
	}

	@Override
	public void deleteUserExtenPersistById(Object primaryKey) {
		userExtenPersistEao.delete(UserExtenPersist.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserExtenPersist> loadPageEntitys(int start, int length, String sql) {
		return userExtenPersistEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return userExtenPersistEao.getEntityCount(sql);
	}

	//getter and setter
	public UserExtenPersistEao getUserExtenPersistEao() {
		return userExtenPersistEao;
	}

	public void setUserExtenPersistEao(UserExtenPersistEao userExtenPersistEao) {
		this.userExtenPersistEao = userExtenPersistEao;
	}
	
	/**
	 * chb 取得所有的对应关系
	 */
	@Override
	public List<UserExtenPersist> getAll() {
		return userExtenPersistEao.getAll();
	}
	/**
	 * chb持久化分机用户的对应关系
	 * @param exten
	 * @param userId
	 */
	@Override
	public void updateExtenToUser(String exten, Long userId) {
		userExtenPersistEao.updateExtenToUser(exten,userId);
	}
	/**
	 * chb移除分机用户的对应关系
	 * @param exten
	 * @param userId
	 */
	@Override
	public void removeExtenToUser(String exten) {
		userExtenPersistEao.removeExtenToUser(exten);
	}
}
