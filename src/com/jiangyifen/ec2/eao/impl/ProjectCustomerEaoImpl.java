package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ProjectCustomerEao;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.ProjectCustomer;
import com.jiangyifen.ec2.entity.User;

public class ProjectCustomerEaoImpl extends BaseEaoImpl implements ProjectCustomerEao {

	@SuppressWarnings("unchecked")
	public List<ProjectCustomer> getAllByProject(User accountManager, MarketingProject marketingProject) {
		return getEntityManager()
				.createQuery(
						"select pc from ProjectCustomer as pc where pc.accountProject.id = " + marketingProject.getId()
						+" and pc.accountManager.id = " + accountManager.getId())
				.getResultList();
	}
}
