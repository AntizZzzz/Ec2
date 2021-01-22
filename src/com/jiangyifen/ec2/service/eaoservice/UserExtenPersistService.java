package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.UserExtenPersist;

public interface UserExtenPersistService {

	@Transactional
	public UserExtenPersist getUserExtenPersist(Object primaryKey);

	@Transactional
	public void saveUserExtenPersist(UserExtenPersist userExtenPersist);

	@Transactional
	public void updateUserExtenPersist(UserExtenPersist userExtenPersist);

	@Transactional
	public void deleteUserExtenPersist(UserExtenPersist userExtenPersist);

	@Transactional
	public void deleteUserExtenPersistById(Object primaryKey);

	@Transactional
	public List<UserExtenPersist> loadPageEntitys(int start,int length,String sql);

	@Transactional
	public int getEntityCount(String sql);

	public List<UserExtenPersist> getAll();

	/**
	 * chb持久化分机用户的对应关系
	 * @param exten
	 * @param userId
	 */
	@Transactional
	public void updateExtenToUser(String exten, Long userId);
	/**
	 * chb移除分机用户的对应关系
	 * @param exten
	 * @param userId
	 */
	@Transactional
	public void removeExtenToUser(String exten);
	
}
