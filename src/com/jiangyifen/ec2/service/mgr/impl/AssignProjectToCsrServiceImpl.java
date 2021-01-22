package com.jiangyifen.ec2.service.mgr.impl;

import java.util.Collection;
import java.util.Iterator;

import com.jiangyifen.ec2.eao.MarketingProjectEao;
import com.jiangyifen.ec2.eao.UserEao;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.mgr.AssignProjectToCsrService;

public class AssignProjectToCsrServiceImpl implements AssignProjectToCsrService {
	private UserEao userEao;
	private MarketingProjectEao marketingProjectEao;

	@Override
	public void assignProjectCsr(MarketingProject marketingProject, Collection<User> csrs) {
		Iterator<User> iter = csrs.iterator();
		while (iter.hasNext()) {
			User user=iter.next();
			marketingProject.getUsers().add(user);
		}
		marketingProjectEao.update(marketingProject);
	}

	//getter and setter
	public UserEao getUserEao() {
		return userEao;
	}

	public void setUserEao(UserEao userEao) {
		this.userEao = userEao;
	}

	public MarketingProjectEao getMarketingProjectEao() {
		return marketingProjectEao;
	}

	public void setMarketingProjectEao(MarketingProjectEao marketingProjectEao) {
		this.marketingProjectEao = marketingProjectEao;
	}
}
