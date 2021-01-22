package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;

/**
 * 获取一些基本信息的工具类
 *
 * @author jinht
 *
 * @date 2015-3-26 下午1:19:20 
 *
 */
public class AchieveBasicUtil {

	
	private List<Department> deptList;
	
	/**
	 * jinht
	 * 获取该登录用户权限管理下的所有管理的部门信息
	 * @param loginUser  当前登录的用户
	 * @param deptList   部门集合
	 * @return List<Department> 部门集合
	 */
	public List<Department> getDepartments(User loginUser, List<Department> deptList) {
		if(deptList != null && deptList.size() > 0) {
			deptList.clear();
		} else {
			deptList = new ArrayList<Department>();
		}
		
		Department department = new Department();
		department.setName("所有部门");
		department.setId(0L);
		deptList.add(department);
		
		// 这里需要考虑，两个角色里面都存在同一个部门的管理，需要进行去重
		Map<Long, Department> departmentMap = new HashMap<Long, Department>();
		// 获取当前登录用户所拥有的角色
		Set<Role> roleSet = loginUser.getRoles();
		if(roleSet != null && roleSet.size() > 0) {
			// 获取角色下所管理的部门
			for(Role role : roleSet) {
				Set<Department> deptSet = role.getDepartments();
				if(deptSet != null && deptSet.size() > 0) {
					for(Department dept : deptSet) {	// 进行去重
						departmentMap.put(dept.getId(), dept);
					}
				}
			}
		}
		
		// 在这里把 map 集合中的数据转存到 list 集合中，因为 map 集合存放的数据是无序的，所以为了让 combox 默认选中第一行数据为 “全部”，所以这样处理 
		if(departmentMap != null && departmentMap.size() > 0) {
			for(Entry<Long, Department> entry : departmentMap.entrySet()) {
				deptList.add(entry.getValue());
			}
		}
		
		setDeptList(deptList);
		
		return deptList;
	}

	/**
	 * jinht
	 * 根据传入的参数，进行设置部门 ComboBox 选择下拉框组件
	 * @param cmbDept          部门下拉框组件
	 * @param loginUser        当前登录的用户
	 * @param deptList         部门的 List 集合
	 * @param deptContainer    部门的 BeanItemContainer 组装箱集合
	 * @return ComboBox 部门下拉框组件
	 */
	public ComboBox getCmbDeptReport(ComboBox cmbDept, User loginUser, List<Department> deptList, BeanItemContainer<Department> deptContainer) {
		cmbDept = new ComboBox();
		cmbDept.setWidth("100px");
		cmbDept.setInputPrompt("部门");
		cmbDept.setImmediate(true);
		cmbDept.setDescription("所有部门表示当前登录用户所管理的所有部门！");
		cmbDept.setNullSelectionAllowed(false);
		// 获取部门集合列表
		deptList = getDepartments(loginUser, deptList);
		deptContainer.addAll(deptList);
		cmbDept.setContainerDataSource(deptContainer);
		cmbDept.setValue(deptList.get(0));
		cmbDept.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		return cmbDept;
	}
	
	
	/**
	 * jinht
	 * 获取当前登录用户所管理的部门的 id，结果例如“1,2,3”这样的字符串
	 * @return String
	 */
	public String getDeptIds() {
		
		getDepartments(SpringContextHolder.getLoginUser(), null);
		
		if(getDeptList() != null) {
			List<Long> idsList = new ArrayList<Long>();
			for(Department dept : getDeptList()) {
				idsList.add(dept.getId());
			}
			
			if(idsList != null && idsList.size() > 0) {
				idsList.remove(0L);
				return StringUtils.join(idsList, ",");
			}
		}
		
		return null;
	}
	
	/**
	 * jinht
	 * 获取当前登录用户所管理的部门的 name，结果例如“1,2,3”这样的字符串
	 * @return String
	 */
	public String getDeptNames() {
		
		if(getDeptList() != null) {
			List<String> namesList = new ArrayList<String>();
			for(Department dept : getDeptList()) {
				namesList.add("'"+dept.getName()+"'");
			}
			
			if(namesList != null && namesList.size() > 0) {
				namesList.remove("'所有部门'");
				return StringUtils.join(namesList, ",");
			}
		}
		
		return null;
	}

	/**
	 * 获取当前用户所管理的部门
	 * @return
	 */
	public List<Department> getDeptList() {
		return deptList;
	}

	/**
	 * 设置当前用户所管理的部门
	 */
	public void setDeptList(List<Department> deptList) {
		this.deptList = deptList;
	}
	
}
