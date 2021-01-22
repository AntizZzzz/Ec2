package com.jiangyifen.ec2.eao;


import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;

public interface UserEao extends BaseEao {
	
	/**
	 * jrh
	 * Admin、Manage用户登录
	 * @param username
	 * @param password
	 */
	public User identify(String username, String password,RoleType roleType);
	
	/**
	 * jrh
	 * Admin、Manage用户登录
	 * @param username
	 */
	public User identify(String username, RoleType roleType);
	
	/**
	 * jrh
	 * 查找数据库中的所有角色，并将其存放到一个 List 集合中
	 * @return List<User> 存放所有角色的List
	 */
	public List<User> getAllByDomain(Domain domain);

	/**
	 * jrh
	 * 获取指定域中某一部门的所有用户
	 * @param department
	 * @param domain
	 * @return
	 */
	public List<User> getByDeptment(Department department, Domain domain);
	
	/**
	 * jrh 
	 * 获取指定域下 指定部门的所有客服人员 csr
	 * @param deptIds	各部门Id 号
	 * @param domainId	各部门Id 号
	 * @return
	 */
	public List<User> getCsrsByDepartment(List<Long> deptIds, Long domainId);

	/**
	 * chb
	 * 取出所有事Csr的用户
	 * @return
	 */
	public List<User> getCsrs(Domain domain);
	
	/**
	 * chb
	 * 根据工号取出域内Csr
	 */
	public List<User> getUsersByEmpNo(String empNo,Domain domain);

	/**
	 * chb
	 * 根据用户名取出全局（所有域）Csr
	 */
	public List<User> getUsersByUsername(String username);

	/**
	 * @Description 描述：根据用户名与租户编号，到指定域下查找用户对象
	 *
	 * @author  JRH
	 * @date    2014年8月8日 下午6:19:01
	 * @param username	用户名
	 * @param domainId	租户编号
	 * @return List<User>
	 */
	public List<User> getUsersByUsername(String username, Long domainId);

	/**
	 * @Description 描述：在指定租户下根据用户编号获取用户对象
	 *
	 * @author  JRH
	 * @date    2014年8月12日 下午2:03:37
	 * @param userId	用户编号
	 * @param domainId	租户编号
	 * @return User		返回用户信息
	 */
	public User getByIdInDomain(Long userId, Long domainId);
	
	/**
	 * @Description 根据jpql查询所有的用户
	 * @author XKP
	 * @param sql
	 * @return
	 */
	public List<User> getAllUsersByJpql(String sql);

	/**
	 * @Description 描述：获取指定域下的所有管理员账户
	 *
	 * @author  JRH
	 * @date    2014年12月9日 下午12:03:15
	 * @param domain
	 * @return List<User>
	 */
	public List<User> getMgrByDomain(Domain domain);
	
	/**
	 * @Description 描述：获取指定域下的所有管理员账户
	 * 
	 * @author jinht
	 * @date	2015年6月24日 09:41:29
	 * @param 	domainId
	 * @return User
	 */
	public List<User> getMgrByDomainId(Long domainId);
}
