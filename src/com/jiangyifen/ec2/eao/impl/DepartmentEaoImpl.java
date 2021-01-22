package com.jiangyifen.ec2.eao.impl;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import com.jiangyifen.ec2.eao.DepartmentEao;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;

@SuppressWarnings("unchecked")
public class DepartmentEaoImpl extends BaseEaoImpl implements DepartmentEao {
	
	@Override
	public List<Department> getAll(Domain domain) {
		List<Department> departmentList = getEntityManager().createQuery("select dept from Department as dept where dept.domain.id="+domain.getId()+" order by dept.id asc",
				Department.class).getResultList();
		return departmentList;
	}
	
	@Override
	public boolean isSuperParent(long departmentId) {
		Query query = getEntityManager().createQuery("Select dept.id from Department as dept where dept.parent.id = ?1");
		List<Department> result = query.setParameter(1, departmentId).getResultList();
		if(result.size() != 0) {
			return true;
		}
		return false;
	}

	/**
	 * chb 根据部门查处能作为父部门的部门,将自身也移除
	 * @param dept
	 * @param domain
	 * @return
	 */
	@Override
	public List<Department> excludeSubDepartments(Department dept, Domain domain) {
		//所有部门
		List<Department> allDeptList = getEntityManager().
				createQuery("select dept from Department as dept where dept.domain.id="+domain.getId(),
				Department.class).getResultList();
		//所有子部门
		List<Department> childDeptList=getChildDeptList(dept,domain);
		childDeptList.add(dept);//将自身也算作子部门
		List<Long> childIds=new ArrayList<Long>();
		for(Department dept2:childDeptList){
			childIds.add(dept2.getId());
		}
		//标记要移除的部门
		List<Department> toRemove=new ArrayList<Department>();
		for(int i=0;i<allDeptList.size();i++){
			Long parentId = allDeptList.get(i).getId();
			if(childIds.contains(parentId)){
				toRemove.add(allDeptList.get(i));
			}
		}
		//进行移除操作
		for(int i=0;i<toRemove.size();i++){
			allDeptList.remove(toRemove.get(i));
		}
		return allDeptList;//返回剔除后的结果
	}
	/**
	 * chb
	 * 取得子部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	@Override
	public List<Department> getChildDepartments(Department dept, Domain domain) {
		return getChildDeptList(dept, domain);
	}
	
	/**
	 * chb
	 * 辅助 excludeSubDepartments 递归查找子部门
	 * @param dept
	 * @param domain
	 * @return
	 */
	private List<Department> getChildDeptList(Department dept, Domain domain) {
		// 查找Dept的子部门
		List<Department> deptList = getEntityManager().
				createQuery("select dept from Department as dept where dept.domain.id="+domain.getId()+" and dept.parent.id="+dept.getId(),
				Department.class).getResultList();
		//如果没有子部门，返回空集合
		if(deptList.size()==0){ 
			return deptList;
		}else{
			//如果有子部门，将所有子部门进行迭代添加到deptList后返回
			for(int i=0;i<deptList.size();i++){
				deptList.addAll(getChildDeptList(deptList.get(i), domain));
			}
			return deptList;
		}
	}
	
	@Override
	public List<Department> getGovernedDeptsByRole(Long roleId) {
		// 先从中间表中查出部门id
		List<Long> deptIds = getEntityManager().createNativeQuery("select department_id from ec2_role_department_link where role_id = " +roleId).getResultList();
		String idSql = deptIds.size() > 0 ? "" : "-1";
		for(int i = 0; i < deptIds.size(); i++) {
			if( i == (deptIds.size() -1) ) {
				idSql += deptIds.get(i);
			} else {
				idSql += deptIds.get(i) +", ";
			}
		}
		
		// 在通过部门id 查出部门集合
		return getEntityManager().createQuery("select d from Department as d where d.id in ("+ idSql+") order by d.id asc").getResultList();
	}

	@Override
	public void delete(Department department) {
		// jrh 删除部门与客服记录状态之间的对应关系
		String sql = "delete from ec2_service_record_status_2_department_link where departmentid = "+ department.getId();
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		// 删部门
		this.delete(department);
	}

	@Override
	public void deleteById(Object primaryKey) {
		// jrh 删除部门与客服记录状态之间的对应关系
		String sql = "delete from ec2_service_record_status_2_department_link where departmentid = "+ primaryKey;
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		// 删部门
		this.delete(Department.class, primaryKey);
	}

}
