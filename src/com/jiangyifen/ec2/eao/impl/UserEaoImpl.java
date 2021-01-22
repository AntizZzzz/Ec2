package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.eao.UserEao;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;

@SuppressWarnings("unchecked")
public class UserEaoImpl extends BaseEaoImpl implements UserEao {

	@Override
	public User identify(String username, String password,RoleType roleType) {
		List<User> result = getEntityManager()
				.createQuery("select u from User as u where u.username = :username and u.password = :password ")
				.setParameter("username", username).setParameter("password", password).getResultList();
		// jrh 不适用下面的方式是为了防止暴力注入
//		List<User> result = getEntityManager()
//				.createQuery("select u from User as u where u.username = '"
//								+ username + "' and u.password = '"
//								+ password + "'").getResultList();
		//判断用户是否有相应的角色
		if (result.size() > 0) {
			User user=result.get(0);
			for (Role role : user.getRoles()) {
				if (roleType.equals(role.getType())) {
					return user;
				}
			}
		}
		return null;
	}
	
	@Override
	public User identify(String username, RoleType roleType) {
		List<User> result = getEntityManager().createQuery("select u from User as u where u.username = :username ").setParameter("username", username).getResultList();
		//判断用户是否有相应的角色
		if (result.size() > 0) {
			User user=result.get(0);
			for (Role role : user.getRoles()) {
				if (roleType.equals(role.getType())) {
					return user;
				}
			}
		}
		return null;
	}

	@Override
	public List<User> getAllByDomain(Domain domain) {
		List<User> allUsers = getEntityManager().createQuery(
				"select u from User as u where u.domain.id = " + domain.getId() + " order by u.empNo asc").getResultList();
		return allUsers;
	}

	@Override
	public List<User> getByDeptment(Department department, Domain domain) {
		List<User> allUsers = getEntityManager().createQuery(
				"select u from User as u where u.department.id=" + department.getId()+ " and u.domain.id=" + domain.getId() + " order by u.empNo asc").getResultList();
		return allUsers;
	}
	
	@Override
	public List<User> getCsrsByDepartment(List<Long> deptIds, Long domainId) {
		/**
		 * jinht 
		 * 如果 DeptIds 的值只有一个，那么就没必要用带有 in 的 SQL 语句
		 */
		if(deptIds != null && deptIds.size() == 1) {
			String sqlSelect="select u from User u where u.domain.id=" +domainId +" and u.department.id = " +deptIds.get(0)+ " order by u.empNo asc";
			return getCsrsBySql(sqlSelect);
		}
		
		String deptIdSql = StringUtils.join(deptIds, ",");
		String sqlSelect="select u from User u where u.domain.id=" +domainId +" and u.department.id in (" +deptIdSql+ ") order by u.empNo asc";
		return getCsrsBySql(sqlSelect);
	}
	
	/**
	 * chb
	 * 取出所有Csr用户
	 * @return
	 */
	@Override
	public List<User> getCsrs(Domain domain) {
		String sqlSelect="select u from User u where u.domain.id="+domain.getId() + " order by u.empNo asc";
		return getCsrsBySql(sqlSelect);
	}
	
	/**
	 * 内部调用，根据sql取得Csr
	 * @param sqlSelect
	 * @return
	 */
	private List<User> getCsrsBySql(String sqlSelect){
		//默认一个域内的用户不是太多，让EL管理
		List<User> allUser = getEntityManager().createQuery(sqlSelect).getResultList();
		List<User> toRemove = new ArrayList<User>();
		for (int i = 0; i < allUser.size(); i++) {
			Boolean isCsr = false;
			Set<Role> roles = allUser.get(i).getRoles();
			// 看用户中是否有Csr这个角色,如果有则标记为Csr
			for (Role role : roles) {
				if (role.getType() == RoleType.csr) {
					isCsr = true;
				}
			}
			// 如果不是Csr,在所有用户中移除Csr
			if (!isCsr) {
				toRemove.add(allUser.get(i));
			}
		}
		// 移除操作
		for (User user : toRemove) {
			allUser.remove(user);
		}
		return allUser;
	}
	
	/**
	 * chb
	 * 根据工号取出域内User
	 */
	public List<User> getUsersByEmpNo(String empNo,Domain domain){
		List<User> users = getEntityManager().createQuery(
				"select u from User as u where u.empNo='"+empNo+"' and u.domain.id="
						+ domain.getId() + " order by u.empNo asc").getResultList();
		return users;
	}

	/**
	 * chb
	 * 根据用户名取出全局（所有域）User
	 */
	public List<User> getUsersByUsername(String username){
		List<User> users = getEntityManager().createQuery(
				"select u from User as u where u.username='"+username+"' order by u.empNo asc").getResultList();
		return users;
	}

	@Override
	public List<User> getUsersByUsername(String username, Long domainId) {
		String jpql = "select u from User as u where u.username='"+username+"' and u.domain.id = "+domainId+" order by u.empNo asc";
		List<User> users = getEntityManager().createQuery(jpql).getResultList();
		return users;
	}

	@Override
	public User getByIdInDomain(Long userId, Long domainId) {
		String jpql = "select u from User as u where u.id='"+userId+"' and u.domain.id = "+domainId+" order by u.id asc";
		List<User> userLs = getEntityManager().createQuery(jpql).getResultList();
		if(userLs.size() > 0) {
			return userLs.get(0);
		}
		return null;
	}

	@Override
	public List<User> getAllUsersByJpql(String sql) {//XKP
		return getEntityManager().createQuery(sql).getResultList();
	}

	@Override
	public List<User> getMgrByDomain(Domain domain) {
		// 第一步，取出指定域下所有管理员的ID
		String nativeSql = "select ur.user_id from ec2_role as r right join ec2_user_role_link as ur on r.id = ur.role_id where r.type = 1 and r.domain_id = "+domain.getId()+" order by ur.user_id asc";
		EntityManager em = this.getEntityManager();
		List<Long> mgrIdLs = em.createNativeQuery(nativeSql).getResultList();
		if(mgrIdLs.size() == 0) {
			return new ArrayList<User>();
		}
		
		String mgrIdStr = StringUtils.join(mgrIdLs, ",");
		
		// 第二步，查出所有用户
		StringBuffer jpqlStrBf = new StringBuffer();
		jpqlStrBf.append("select u from User as u");
		jpqlStrBf.append(" where u.id in ( ");
		jpqlStrBf.append(mgrIdStr);
		jpqlStrBf.append(" ) order by u.id asc");

		return em.createQuery(jpqlStrBf.toString()).getResultList();
		
	}
	
	@Override
	public List<User> getMgrByDomainId(Long domainId) {
		// 第一步，取出指定域下所有管理员的ID
		String nativeSql = "select ur.user_id from ec2_role as r right join ec2_user_role_link as ur on r.id = ur.role_id where r.type = 1 and r.domain_id = "+domainId+" order by ur.user_id asc";
		EntityManager em = this.getEntityManager();
		List<Long> mgrIdLs = em.createNativeQuery(nativeSql).getResultList();
		if(mgrIdLs.size() == 0) {
			return new ArrayList<User>();
		}
		
		String mgrIdStr = StringUtils.join(mgrIdLs, ",");
		
		// 第二步，查出所有用户
		StringBuffer jpqlStrBf = new StringBuffer();
		jpqlStrBf.append("select u from User as u");
		jpqlStrBf.append(" where u.id in ( ");
		jpqlStrBf.append(mgrIdStr);
		jpqlStrBf.append(" ) order by u.id asc");

		return em.createQuery(jpqlStrBf.toString()).getResultList();
	}
	
}
