package com.jiangyifen.ec2.service.mgr.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.eao.BusinessModelEao;
import com.jiangyifen.ec2.eao.RoleEao;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.service.mgr.ModifyRole;

public class ModifyRoleImpl implements ModifyRole {
	
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private RoleEao roleEao;
	
	private BusinessModelEao businessModelEao;
	
	@Override
	public void save(Role role, BusinessModel businessModel) {
		roleEao.save(role);
		businessModelEao.save(businessModel);
	}

	public RoleEao getRoleEao() {
		return roleEao;
	}

	public void setRoleEao(RoleEao roleEao) {
		this.roleEao = roleEao;
	}

	public BusinessModelEao getBusinessModelEao() {
		return businessModelEao;
	}

	public void setBusinessModelEao(BusinessModelEao businessModelEao) {
		this.businessModelEao = businessModelEao;
	}

}
