package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface RoleService extends FlipSupportService<Role> {

	// enhanced method
	
	@Transactional
	public List<Role> getAll(Domain domain);

	/**
	 * jrh
	 * 	获取拥有指定角色的人数
	 * @param roleId	角色的Id 值
	 * @return Long 	统计数量
	 */
	@Transactional
	public Long getUserCountByRole(Long roleId);
	
	// common
	@Transactional
	public Role get(Object primaryKey);
	
	@Transactional
	public void save(Role role);

	@Transactional
	public void update(Role role);

	@Transactional
	public void delete(Role role);
	
	@Transactional
	public void deleteById(Object primaryKey);

	
}
