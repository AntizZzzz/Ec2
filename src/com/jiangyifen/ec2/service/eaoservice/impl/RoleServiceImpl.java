package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.RoleEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.service.eaoservice.RoleService;

public class RoleServiceImpl implements RoleService {
	
	private RoleEao roleEao;

	// common method
	
	@Override
	public Role get(Object primaryKey) {
		return roleEao.get(Role.class, primaryKey);
	}
	
	@Override
	public void save(Role role) {
		roleEao.save(role);
	}

	@Override
	public void update(Role role) {
		roleEao.update(role);
	}

	@Override
	public void delete(Role role) {
		roleEao.delete(role);
	}

	@Override
	public void deleteById(Object primaryKey) {
		roleEao.delete(Role.class, primaryKey);
	}
	
	// enhance function method
	
	@Override
	public List<Role> getAll(Domain domain) {
		return roleEao.getAll(domain);
	}
	
	/**
	 * jrh
	 * 	获取拥有指定角色的人数
	 * @param roleId	角色的Id 值
	 * @return Long 	统计数量
	 */
	@Override
	public Long getUserCountByRole(Long roleId) {
		return roleEao.getUserCountByRole(roleId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Role> loadPageEntities(int start, int length, String sql) {
		return roleEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return roleEao.getEntityCount(sql);
	}
	
	//getter and setter
	public RoleEao getRoleEao() {
		return roleEao;
	}

	public void setRoleEao(RoleEao roleEao) {
		this.roleEao = roleEao;
	}


}
