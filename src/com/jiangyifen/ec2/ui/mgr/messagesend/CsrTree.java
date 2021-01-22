package com.jiangyifen.ec2.ui.mgr.messagesend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.NoticeSend;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;

/**
 * 选择Csr的Tree结构
 * 
 * @author chb
 */
@SuppressWarnings("serial")
public class CsrTree extends HorizontalLayout implements Property.ValueChangeListener {
	
	private Tree tree;
	private DepartmentService departmentService;
	private HashMap<Long, List<User>> deptCsr;
	private NoticeSend messageSend;
	private User loginUser;
	
	// 构造器
	public CsrTree(NoticeSend messageSend) {
		this.messageSend=messageSend;
		this.setWidth("100%");
//		this.setMargin(false, true, false, false);
		this.setSpacing(true);
		//提前创建一个空的Map
		deptCsr = new HashMap<Long, List<User>>();
		loginUser = SpringContextHolder.getLoginUser();
		departmentService=SpringContextHolder.getBean("departmentService");
		
		//创建Tree
		Panel panel=new Panel();
		panel.setSizeFull();
		tree=new Tree();
		tree.setCaption("点击CSR或部门添加");
		tree.setImmediate(true);
		tree.setSelectable(true);
		tree.addListener(this);
		tree.setItemCaptionPropertyId("info");
	    tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		tree.setContainerDataSource(createTreeContainer());
		panel.addComponent(tree);
		this.addComponent(panel);
		
	}

	private HierarchicalContainer createTreeContainer() {
		// 创建Container
		HierarchicalContainer csrsContainer = new HierarchicalContainer();
		csrsContainer.addContainerProperty("info", String.class, null);

		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Department> allGovernedDepts = new ArrayList<Department>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					boolean hasAdded = false;
					for(Department department : allGovernedDepts) {
						if(department.getId() != null && department.getId().equals(dept.getId())) {
							hasAdded = true;
						}
					}
					if(hasAdded == false) {
						allGovernedDepts.add(dept);
					}
				}
			}
		}
		for (Department dept : allGovernedDepts) {
			Long deptId=dept.getId();
			Item itemDept = csrsContainer.addItem(deptId);
			itemDept.getItemProperty("info").setValue(dept.getName());
			csrsContainer.setChildrenAllowed(deptId, false);
			// 向部门中添加Csr
			List<User> deptUsers = new ArrayList<User>(dept.getUsers());
			// 排除非Csr用户
			deptUsers = excludeNotCsr(deptUsers);
			// 为User 排序
			Collections.sort(deptUsers, new Comparator<User>() {
				@Override
				public int compare(User user1, User user2) {
					if (user1.getId() > user2.getId()) {
						return 1;
					} else if (user1.getId() < user2.getId()) {
						return -1;
					}
					return 0;
				}
			});

			// 添加部门Id和User的对应关系
			deptCsr.put(dept.getId(), deptUsers);

			// 部门中由用户，则设为可有子Node
			if (deptUsers.size() > 0) {
				csrsContainer.setChildrenAllowed(deptId, true);
				// 为部门添加用户
				for (User user : deptUsers) {
					// 以用户的工号为Id添加到Container中
					String userId=user.getEmpNo();
					Item itemCsr = csrsContainer.addItem(userId);
					itemCsr.getItemProperty("info").setValue(
							user.getUsername() + "(" + user.getEmpNo() + ")");
					csrsContainer.setParent(userId, deptId);
					csrsContainer.setChildrenAllowed(userId, false);
				}
			}else{
				//TODO chb 是否保留无Csr的部门
//				csrsContainer.removeItem(deptId);
			}
		}
		return csrsContainer;
	}

	/**
	 * 排除不是Csr的用户
	 * 
	 * @param deptUsers
	 * @return
	 */
	private List<User> excludeNotCsr(List<User> deptUsers) {
		List<User> toRemove = new ArrayList<User>();
		for (int i = 0; i < deptUsers.size(); i++) {
			Boolean isCsr = false;
			Set<Role> roles = deptUsers.get(i).getRoles();
			// 看用户中是否有Csr这个角色,如果有则标记为Csr
			for (Role role : roles) {
				if (role.getType() == RoleType.csr) {
					isCsr = true;
				}
			}
			// 如果不是Csr,在所有用户中移除Csr
			if (!isCsr) {
				toRemove.add(deptUsers.get(i));
			}
		}
		// 移除操作
		for (User user : toRemove) {
			deptUsers.remove(user);
		}
		return deptUsers;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if(tree.getValue()==null){
			return;
		}else{
			if(tree.getValue() instanceof Long){
				messageSend.addSelectedCsrs(deptCsr.get(tree.getValue()));
			}else{
				messageSend.addSelectedCsr(tree.getValue().toString());
			}
		}
	}
}
