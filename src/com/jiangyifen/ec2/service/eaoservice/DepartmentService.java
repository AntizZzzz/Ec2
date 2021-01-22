package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface DepartmentService extends FlipSupportService<Department> {
	
	@Transactional
	public List<Department> getAll(Domain domain);
	
	@Transactional
	public 	boolean isSuperDepartment(long deptId);

	// common
	@Transactional
	public Department get(Object primaryKey);
	
	@Transactional
	public void save(Department department);

	@Transactional
	public void update(Department department);

	@Transactional
	public void delete(Department department);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	/**
	 * chb 根据部门查处能作为父部门的部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<Department> excludeSubDepartments(Department dept,
			Domain domain);

	/**
	 * chb
	 * 取得子部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<Department> getChildDepartments(Department dept, Domain domain);
	
	/**
	 * jrh
	 * 	根据角色的Id，获取该角色管辖的所有部门
	 * @param 	roleId 				角色的Id号
	 * @return  List<Department>	被管辖的部门
	 */
	@Transactional
	public List<Department> getGovernedDeptsByRole(Long roleId);
}
