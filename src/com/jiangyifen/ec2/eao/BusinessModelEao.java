package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;

public interface BusinessModelEao extends BaseEao {

	List<BusinessModel> getByRoleType(RoleType roleType);

//	List<BusinessModel> getAll();
	/**
	 * chb
	 * 取出所有的BusinessModel对象
	 * @return
	 */
	public List<BusinessModel> getAll();

	/**
	 * jrh
	 * 	获取属于指定角色类型的所有权限
	 * @param roleType
	 * @return
	 */
	public List<BusinessModel> getAllModelsByRoleType(RoleType roleType);

	/**
	 * @Description 描述：根据角色的编号，获取该角色拥有的所有权限
	 *
	 * @author  JRH
	 * @date    2014年12月12日 下午12:53:35
	 * @param roleId
	 * @return List<BusinessModel>
	 */
	public List<BusinessModel> getModelsByRoleId(Long roleId);
	
}
