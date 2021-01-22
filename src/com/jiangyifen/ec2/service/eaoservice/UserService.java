package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface UserService extends FlipSupportService<User> {

	// enhanced method

	/**
	 * 用户认证，检查该用户是否存在，如果存在返回用户对象
	 * 
	 * @param username
	 * @param password
	 * @param roleType
	 * @return
	 */
	@Transactional
	public User identify(String username, String password, RoleType roleType);
	
	/**
	 * 用户认证，检查该用户是否存在，如果存在返回用户对象
	 * 
	 * @param username
	 * @param roleType
	 * @return
	 */
	@Transactional
	public User identify(String username, RoleType roleType);

	/**
	 * jrh 查找数据库中的所有角色，并将其存放到一个 List 集合中
	 * 
	 * @return List<User> 存放所有角色的List
	 */
	@Transactional
	public List<User> getAllByDomain(Domain domain);

	/**
	 * jrh 获取指定域中某一部门的所有用户
	 * 
	 * @param department
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<User> getByDeptment(Department department, Domain domain);

	/**
	 * jrh // 暂时没用 获取指定域下 指定部门的所有客服人员 csr
	 * 
	 * @param deptIds
	 *            各部门Id 号
	 * @param domainId
	 *            各部门Id 号
	 * @return
	 */
	@Transactional
	public List<User> getCsrsByDepartment(List<Long> deptIds, Long domainId);

	// common
	@Transactional
	public User get(Object primaryKey);

	@Transactional
	public void save(User user);

	@Transactional
	public User update(User user);

	@Transactional
	public void delete(User user);

	@Transactional
	public void deleteById(Object primaryKey);

	/**
	 * chb 取出所有事Csr的用户
	 * 
	 * @return
	 */
	@Transactional
	public List<User> getCsrsByDomain(Domain domain);

	/**
	 * chb 根据工号取出域内Csr
	 */
	@Transactional
	public List<User> getUsersByEmpNo(String empNo, Domain domain);

	/**
	 * chb 根据用户名取出全局（所有域）Csr
	 */
	@Transactional
	public List<User> getUsersByUsername(String username);

	/**
	 * jrh
	 * 	一次性添加多个用户对象
	 * @param amountStr				添加数量
	 * @param startEmpnoStr			起始工号
	 * @param startUsernameStr		起始用户名
	 * @param startSecretStr		起始密码
	 * @param pwType				密码配置类型  "SpecifyPW" 每个话务员拥有自己的密码， "SamePW" 本次添加的所有话务员使用相同的密码
	 * @param dept					所属部门
	 * @param roles					拥有角色
	 * @param domain				所属域
	 * @throws Exception 			抛出RuntimeException
	 * 这个方法不能加事务，不然会因为事务回滚而导致错误
	 */
	public Map<String, Integer> addMutiUsers(String amountStr, String startEmpnoStr,
			String startUsernameStr, String startSecretStr, String pwType, 
			Department dept, Set<Role> roles, Domain domain);

	/**
	 * @Description 描述：根据用户名与租户编号，到指定域下查找用户对象
	 *
	 * @author  JRH
	 * @date    2014年8月8日 下午6:19:01
	 * @param username	用户名
	 * @param domainId	租户编号
	 * @return List<User>
	 */
	@Transactional
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
	@Transactional
	public User getByIdInDomain(Long userId, Long domainId);

	@Transactional
	public List<User> getAllUsersByJpql(String sql);//XKP

	/**
	 * @Description 描述：获取指定域下的所有管理员账户
	 *
	 * @author  JRH
	 * @date    2014年12月9日 下午12:03:15
	 * @param domain
	 * @return List<User>
	 */
	@Transactional
	public List<User> getMgrByDomain(Domain domain);
	
	/**
	 * @Description 描述：获取指定域下的 ID 最小的一个管理员用户
	 * 
	 * @author jinht
	 * @date	2015年6月24日 09:41:29
	 * @param 	domainId
	 * @return User
	 */
	@Transactional
	public User getMgrByDomainId(Long domainId);
	
	/**
	 * jinht
	 * 修改语音留言配置文件
	 * @param domain
	 * @param userList
	 * @return
	 */
	@Transactional
	public boolean updateVoicemailConfig(Domain domain, List<User> userList);	
	
}
