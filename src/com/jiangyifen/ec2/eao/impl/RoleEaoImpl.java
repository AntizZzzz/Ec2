package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.RoleEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;

public class RoleEaoImpl extends BaseEaoImpl implements RoleEao {

	@Override
	public List<Role> getAll(Domain domain) {
		List<Role> allRoles = getEntityManager().createQuery("select role from Role as role where role.domain.id = "+domain.getId(), Role.class).getResultList();
		return allRoles;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long getUserCountByRole(Long roleId) {
		List<Long> counts = getEntityManager().createNativeQuery("select count(ur) from ec2_user_role_link as ur where ur.role_id = " + roleId).getResultList();
		if(counts.size() > 0) {
			return counts.get(0);
		}
		return 0L;
	}
	
}
