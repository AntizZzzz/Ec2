package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.ProjectCustomer;
import com.jiangyifen.ec2.entity.User;


public interface ProjectCustomerEao extends BaseEao {

	/**
	 * 获取指定用户在指定项目中拥有的所有客户对应关系
	 * @param accountManager	用户
	 * @param marketingProject	项目
	 * @return
	 */
	public List<ProjectCustomer> getAllByProject(User accountManager, MarketingProject marketingProject);
	
}
