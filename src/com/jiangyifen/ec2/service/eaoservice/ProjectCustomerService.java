package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.ProjectCustomer;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface ProjectCustomerService extends FlipSupportService<ProjectCustomer> {
	// ========  enhanced method  ========//

	/**
	 * 获取指定用户在指定项目中拥有的所有客户对应关系
	 * @param accountManager	用户
	 * @param marketingProject	项目
	 * @return
	 */
	@Transactional
	public List<ProjectCustomer> getAllByProject(User accountManager, MarketingProject marketingProject);

	
	// ========  common method  ========//
	
	@Transactional
	public ProjectCustomer get(Object primaryKey);
	
	@Transactional
	public void save(ProjectCustomer projectCustomer);

	@Transactional
	public void update(ProjectCustomer projectCustomer);

	@Transactional
	public void delete(ProjectCustomer projectCustomer);
	
	@Transactional
	public void deleteById(Object primaryKey);

}
