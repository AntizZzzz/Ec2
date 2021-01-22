package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;

public interface BusinessModelService {

//	public Collection<String> getByRoleName(String roleName);	// chb 获取指定角色拥有功能的name 字符串
//	

	@Transactional
	public List<BusinessModel> getModelsByRoleType(RoleType roleType);
	
	/**
	 * jrh
	 * 	获取属于指定角色类型的所有权限
	 * @param roleType
	 * @return
	 */
	@Transactional
	public List<BusinessModel> getAllModelsByRoleType(RoleType roleType);
	
	// common
	@Transactional
	public BusinessModel get(Object primaryKey);

	@Transactional
	public void save(BusinessModel businessModel);

	@Transactional
	public void update(BusinessModel businessModel);

	@Transactional
	public void delete(BusinessModel businessModel);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	@Transactional
	public List<BusinessModel> loadPageEntitys(int start,int length,String sql);

	@Transactional
	public int getEntityCount(String sql);

	/**
	 * chb
	 * 取出所有的BusinessModel对象
	 * @return
	 */
	public List<BusinessModel> getAll();
	

	/**
	 * @Description 描述：根据角色的编号，获取该角色拥有的所有权限
	 *
	 * @author  JRH
	 * @date    2014年12月12日 下午12:53:35
	 * @param roleId
	 * @return List<BusinessModel>
	 */
	@Transactional
	public List<BusinessModel> getModelsByRoleId(Long roleId);

}
