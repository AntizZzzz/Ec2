package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;


public interface DepartmentEao extends BaseEao {
	
	public List<Department> getAll(Domain domain);
	
	public boolean isSuperParent(long departmentId);

	/**
	 * chb 根据部门查处能作为父部门的部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	public List<Department> excludeSubDepartments(Department dept, Domain domain);
	
	/**
	 * chb
	 * 取得子部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	public List<Department> getChildDepartments(Department dept, Domain domain);
	
	/**
	 * jrh
	 * 	根据角色的Id，获取该角色管辖的所有部门
	 * @param 	roleId 				角色的Id号
	 * @return  List<Department>	被管辖的部门
	 */
	public List<Department> getGovernedDeptsByRole(Long roleId);
	
	/**
	 * jrh
	 * 	重写删除方法，按对象删除
	 * @param department
	 */
	public void delete(Department department);
	
	/**
	 * jrh
	 * 	重写删除方法， 按id删除
	 * @param primaryKey
	 */
	public void deleteById(Object primaryKey);
	
}
