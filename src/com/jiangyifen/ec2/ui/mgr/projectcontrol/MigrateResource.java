package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class MigrateResource extends Window implements Button.ClickListener{
	/**
	 * 主要组件
	 */
	private ComboBox targetProjectSelect;
	private BeanItemContainer<MarketingProject> projectContainer;
	private Button migrate;
	private Button cancel;

	/**
	 * 其他组件
	 */
	private Domain domain;
	private User loginUser;
	private ProjectControl projectControl;
	private MarketingProject project;
	private MarketingProjectService marketingProjectService;
	private DepartmentService departmentService;
	private MarketingProjectTaskService marketingProjectTaskService;

	public MigrateResource(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setResizable(false);
		this.setCaption("转移项目中为完成的任务");
		this.setModal(true);
		this.projectControl = projectControl;

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//选择转移到的项目的选择框约束组件
		HorizontalLayout targetSelectConstraint = new HorizontalLayout();
		targetSelectConstraint.setSpacing(true);

		//选择目标项目的组件
		targetProjectSelect=new ComboBox("请选择目标项目:");
		targetProjectSelect.setImmediate(true);
		projectContainer=new BeanItemContainer<MarketingProject>(MarketingProject.class);
		targetProjectSelect.setContainerDataSource(projectContainer);
		targetProjectSelect.setNullSelectionAllowed(false);
		targetProjectSelect.setItemCaptionPropertyId("projectName");
		targetProjectSelect.setWidth("180px");
		targetSelectConstraint.addComponent(targetProjectSelect);

		windowContent.addComponent(targetSelectConstraint);

		/**
		 * 创建按钮
		 */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		windowContent.addComponent(buttonsLayout);
		windowContent.setComponentAlignment(buttonsLayout,
				Alignment.BOTTOM_RIGHT);

		migrate = new Button("确定");
		migrate.addListener(this);
		buttonsLayout.addComponent(migrate);

		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		departmentService=SpringContextHolder.getBean("departmentService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
	}

	/**
	 * 由buttonClick调用将资源分配给CSR，使资源变为现实的任务
	 */
	private void executeMigrate() {
		MarketingProject projectTo=(MarketingProject)targetProjectSelect.getValue();
		OperationLogUtil.simpleLog(loginUser, "项目控制-按项目转移任务：转移到"+projectTo.getProjectName());
		try {
			//此处逻辑上属于同一个事物的处理却因为事务的关系要分在两个事物中，以后再处理
			marketingProjectTaskService.migrateResourceDelete(project,projectTo);
			marketingProjectTaskService.migrateResourceMigrate(project,projectTo);
			projectControl.updateTable(false);
			this.getApplication().getMainWindow().showNotification("成功转移资源！");
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("迁移资源出错！");
			return;
		}
		this.getParent().removeWindow(this);
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		// 项目不应该为null
		project = projectControl.getCurrentSelect();
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
		// jrh 获取该用户管辖部门成员创建的项目
		List<MarketingProject> projectList=marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());

		//从项目列表中移除当前表格选中的项目
		for(int i=0;i<projectList.size();i++){
			if(project.getId() != null && project.getId().equals(projectList.get(i).getId())){
				projectList.remove(projectList.get(i));
				break;
			}
		}
		
		//重建ComboBox的Container
		projectContainer.removeAllItems();
		projectContainer.addAll(projectList);
		if(projectList.size()>0){
			targetProjectSelect.select(projectList.get(0));
		}
	}

	/**
	 * 点击保存和取消按钮的事件监听器
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == migrate) {
			// 在执行分配的方法时显示提示信息，进行Try Catch操作
			this.executeMigrate();
		} else {
			this.getParent().removeWindow(this);
		}
	}
}
