package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;


public interface RoleEao extends BaseEao {
	
	public List<Role> getAll(Domain domain);

	/**
	 * jrh
	 * 	获取拥有指定角色的人数
	 * @param roleId	角色的Id 值
	 * @return Long 	统计数量
	 */
	public Long getUserCountByRole(Long roleId);
	
}
