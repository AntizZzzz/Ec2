package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.DepartmentEao;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;

public class DepartmentServiceImpl implements DepartmentService {

	private DepartmentEao departmentEao;
	
	// common method
	
	@Override
	public Department get(Object primaryKey) {
		return departmentEao.get(Department.class, primaryKey);
	}

	@Override
	public void save(Department department) {
		departmentEao.save(department);
	}

	@Override
	public void update(Department department) {
		departmentEao.update(department);
	}

	@Override
	public void delete(Department department) {
		departmentEao.delete(department);
	}

	@Override
	public void deleteById(Object primaryKey) {
		departmentEao.deleteById(primaryKey);
	}
	
	// enhance function method

	@Override
	public List<Department> getAll(Domain domain) {
		return departmentEao.getAll(domain);
	}
	
	@Override
	public boolean isSuperDepartment(long deptId) {
		return departmentEao.isSuperParent(deptId);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Department> loadPageEntities(int start, int length, String sql) {
		return departmentEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return departmentEao.getEntityCount(sql);
	}

	//getter and setter
	public DepartmentEao getDepartmentEao() {
		return departmentEao;
	}

	public void setDepartmentEao(DepartmentEao departmentEao) {
		this.departmentEao = departmentEao;
	}
	
	/**
	 * chb 根据部门查处能作为父部门的部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	@Override
	public List<Department> excludeSubDepartments(Department dept, Domain domain) {
		return departmentEao.excludeSubDepartments(dept,domain);
	}
	
	/**
	 * chb
	 * 取得子部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	@Override
	public List<Department> getChildDepartments(Department dept, Domain domain){
		return departmentEao.getChildDepartments(dept, domain);
	}

	@Override
	public List<Department> getGovernedDeptsByRole(Long roleId) {
		return departmentEao.getGovernedDeptsByRole(roleId);
	}
}
