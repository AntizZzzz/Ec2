package com.jiangyifen.ec2.service.mgr;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
public interface AssignProjectToCsrService{
	@Transactional
	public void assignProjectCsr(MarketingProject marketingProject, Collection<User> csrs);
}
