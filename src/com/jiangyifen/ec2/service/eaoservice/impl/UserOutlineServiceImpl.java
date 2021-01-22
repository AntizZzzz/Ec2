package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.UserOutlineEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.UserOutline;
import com.jiangyifen.ec2.service.eaoservice.UserOutlineService;

public class UserOutlineServiceImpl implements UserOutlineService {
	
	private UserOutlineEao userOutlineEao;
	
	//============== enhanced function method  ===============//


	@Override
	public List<UserOutline> getAllByOutline(SipConfig outline, Domain domain) {
		return userOutlineEao.getAllByOutline(outline, domain);
	}
	
	@Override
	public List<UserOutline> getAllByDomain(Domain domain) {
		return userOutlineEao.getAllByDomain(domain);
	}

	@Override
	public UserOutline getByUserId(Long userId, Long domainId) {
		return userOutlineEao.getByUserId(userId, domainId);
	}
	
	//============== common method  ===============//

	@Override
	public UserOutline get(Object primaryKey) {
		return userOutlineEao.get(UserOutline.class, primaryKey);
	}

	@Override
	public void save(UserOutline userOutline) {
		userOutlineEao.save(userOutline);
	}

	@Override
	public void update(UserOutline userOutline) {
		userOutlineEao.update(userOutline);
	}

	@Override
	public void delete(UserOutline userOutline) {
		userOutlineEao.delete(userOutline);
	}

	@Override
	public void deleteById(Object primaryKey) {
		userOutlineEao.delete(UserOutline.class, primaryKey);
	}

	//===================== getter and setter ====================//
	
	public UserOutlineEao getUserOutlineEao() {
		return userOutlineEao;
	}

	public void setUserOutlineEao(UserOutlineEao userOutlineEao) {
		this.userOutlineEao = userOutlineEao;
	}

	
}
