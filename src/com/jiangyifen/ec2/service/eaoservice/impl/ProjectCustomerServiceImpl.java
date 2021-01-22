package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ProjectCustomerEao;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.ProjectCustomer;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.ProjectCustomerService;

public class ProjectCustomerServiceImpl implements ProjectCustomerService {
	
	private ProjectCustomerEao projectCustomerEao;
	
	// ========  enhanced method  ========//

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectCustomer> loadPageEntities(int start, int length,
			String sql) {
		return projectCustomerEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return projectCustomerEao.getEntityCount(sql);
	}

	public List<ProjectCustomer> getAllByProject(User accountManager, MarketingProject marketingProject) {
		return projectCustomerEao.getAllByProject(accountManager, marketingProject);
	}

	//========  common method ========//
	
	@Override
	public ProjectCustomer get(Object primaryKey) {
		return projectCustomerEao.get(ProjectCustomer.class, primaryKey);
	}

	@Override
	public void save(ProjectCustomer projectCustomer) {
		projectCustomerEao.save(projectCustomer);
	}

	@Override
	public void update(ProjectCustomer projectCustomer) {
		projectCustomerEao.update(projectCustomer);
	}

	@Override
	public void delete(ProjectCustomer projectCustomer) {
		projectCustomerEao.delete(projectCustomer);
	}

	@Override
	public void deleteById(Object primaryKey) {
		projectCustomerEao.delete(ProjectCustomer.class, primaryKey);
	}

	//==========  getter setter  ==========//
	
	public ProjectCustomerEao getProjectCustomerEao() {
		return projectCustomerEao;
	}

	public void setProjectCustomerEao(ProjectCustomerEao projectCustomerEao) {
		this.projectCustomerEao = projectCustomerEao;
	}
	
}
