package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.UserOutline;

public interface UserOutlineService {

	//=================== common methods ===================//
	
	@Transactional
	public UserOutline get(Object primaryKey);
	
	@Transactional
	public void save(UserOutline userOutline);

	@Transactional
	public void update(UserOutline userOutline);

	@Transactional
	public void delete(UserOutline userOutline);
	
	@Transactional
	public void deleteById(Object primaryKey);

	//=================== enhanced methods ===================//

	/**
	 * 获取一个域中所有的用户与外线的对应关系
	 * @param domain 域
	 * @return List<UserOutline> 返回整个域中用户与外线的关系集合
	 */
	@Transactional
	public List<UserOutline> getAllByDomain(Domain domain);
	
	/**
	 * 根据外线对象及域，获取指定域下，单条外线的所有成员
	 * @param outline	外线对象
	 * @param domain	域对象
	 * @return List<UserOutline> 返回存放用户与外线的关系集合
	 */
	@Transactional
	public List<UserOutline> getAllByOutline(SipConfig outline, Domain domain);

	/**
	 * 取出指定用户对应的静态外线
	 * @param userId 	用户对象的Id 值
	 * @param domainId	域对象
	 * @return
	 */
	@Transactional
	public UserOutline getByUserId(Long userId, Long domainId);
		
}
