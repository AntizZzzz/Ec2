package com.jiangyifen.ec2.service.mgr;

import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Role;

public interface ModifyRole {

	public void save(Role role, BusinessModel businessModel);
	
}
