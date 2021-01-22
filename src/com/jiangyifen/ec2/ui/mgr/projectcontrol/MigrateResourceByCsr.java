package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class MigrateResourceByCsr extends Window implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件
	 */
	private ComboBox migrateProCombo;
	private ComboBox fromCsrCombo;
	private ComboBox toCsrCombo;
	private Button migrate;
	private Button cancel;
	
	private BeanItemContainer<User> fromContainer;
	private BeanItemContainer<User> toContainer;
	private BeanItemContainer<MarketingProject> projectContainer;

	/**
	 * 其他组件
	 */
	private Domain domain;
	private User loginUser;
	private MarketingProject allProject;
	private CommonService commonService;
	private MarketingProjectService marketingProjectService;
	private DepartmentService departmentService;
	
	public MigrateResourceByCsr() {
		this.initService();
		this.center();
		this.setResizable(false);
		this.setCaption("CSR未打资源转移");
		this.setModal(true);
		
		allProject = new MarketingProject();
		allProject.setProjectName("全部");
		projectContainer=new BeanItemContainer<MarketingProject>(MarketingProject.class);
		
		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		//选择要转移的CSR的选择框约束组件
		HorizontalLayout projectConstraint = new HorizontalLayout();
		projectConstraint.setSpacing(true);
		
		migrateProCombo = new ComboBox("指定任务所属项目");
		migrateProCombo.setDescription("'全部'->表示该用户在所有项目中的拥有的任务转移给另一个用户，如果选择指定项目，则只转移该用户在被选项目中的未完成任务！");
		migrateProCombo.setNullSelectionAllowed(false);
		migrateProCombo.setItemCaptionPropertyId("projectName");
		migrateProCombo.setWidth("200px");
		migrateProCombo.addListener((ValueChangeListener)this);
		migrateProCombo.setContainerDataSource(projectContainer);
		migrateProCombo.setItemCaptionPropertyId("projectName");
		projectConstraint.addComponent(migrateProCombo);
		windowContent.addComponent(projectConstraint);
		
		//选择要转移的CSR的选择框约束组件
		HorizontalLayout fromConstraint = new HorizontalLayout();
		fromConstraint.setSpacing(true);

		//选择目标CSR的组件
		fromCsrCombo=new ComboBox("从 部门-真实姓名-工号");
		fromContainer = new BeanItemContainer<User>(User.class);
		fromCsrCombo.setContainerDataSource(fromContainer);
		fromCsrCombo.setItemCaptionPropertyId("migrateCsr");
		fromCsrCombo.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		fromCsrCombo.setImmediate(true);
		fromCsrCombo.setNullSelectionAllowed(false);
		fromCsrCombo.setWidth("200px");
		fromConstraint.addComponent(fromCsrCombo);
		windowContent.addComponent(fromConstraint);

		//选择转移到的CSR的选择框约束组件
		HorizontalLayout toConstraint = new HorizontalLayout();
		toConstraint.setSpacing(true);
		
		//选择目标CSR的组件
		toCsrCombo=new ComboBox("到 部门-真实姓名-工号");
		toContainer = new BeanItemContainer<User>(User.class);
		toCsrCombo.setContainerDataSource(toContainer);
		toCsrCombo.setItemCaptionPropertyId("migrateCsr");
		toCsrCombo.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		toCsrCombo.setImmediate(true);
		toCsrCombo.setNullSelectionAllowed(false);
		toCsrCombo.setWidth("200px");
		toConstraint.addComponent(toCsrCombo);
		windowContent.addComponent(toConstraint);

		/**
		 * 创建按钮
		 */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		windowContent.addComponent(buttonsLayout);
		windowContent.setComponentAlignment(buttonsLayout,
				Alignment.BOTTOM_RIGHT);

		migrate = new Button("确定");
		migrate.addListener((ClickListener)this);
		buttonsLayout.addComponent(migrate);

		cancel = new Button("取消");
		cancel.addListener((ClickListener)this);
		buttonsLayout.addComponent(cancel);
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		loginUser=SpringContextHolder.getLoginUser();
		departmentService=SpringContextHolder.getBean("departmentService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		commonService=SpringContextHolder.getBean("commonService");
	}

	/**
	 * 由buttonClick调用将资源分配给CSR，使资源变为现实的任务
	 */
	private void executeMigrate() {
		User fromUser = (User)fromCsrCombo.getValue();
		User toUser = (User)toCsrCombo.getValue();
		if(fromUser==null||toUser==null){
			NotificationUtil.showWarningNotification(this, "请先选中要转移资源的CSR和转移的目标CSR");
		}else if(fromUser.getId() != null && fromUser.getId().equals(toUser.getId())){
			NotificationUtil.showWarningNotification(this, "不能转移同一个CSR的数据");
		}else{
			OperationLogUtil.simpleLog(loginUser, "项目控制-按CSR转移任务：从"+fromUser.getUsername()+" 到 "+toUser.getUsername());

			MarketingProject project = (MarketingProject) migrateProCombo.getValue();
			String sql = "update ec2_marketing_project_task set isanswered=false,laststatus=null,user_id="+toUser.getId()+" where user_id="+fromUser.getId()+" and isfinished=false";
			if(project.getId() != null) {
				sql = sql+" and marketingproject_id = " +project.getId();
			}
			commonService.excuteNativeSql(sql, ExecuteType.UPDATE);
			this.getParent().getApplication().getMainWindow().showNotification("按CSR迁移任务 成功！");
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for(Department dept : departments) {
						Long deptId = dept.getId();
						if(!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
		// jrh 获取该用户管辖部门成员创建的项目
		List<MarketingProject> projectList=marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
		
		//重建ComboBox的Container
		projectContainer.removeAllItems();
		projectContainer.addBean(allProject);
		projectContainer.addAll(projectList);
		migrateProCombo.setValue(allProject);
	}

	/**
	 * 点击保存和取消按钮的事件监听器
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == migrate) {
			// 在执行分配的方法时显示提示信息，进行Try Catch操作
			try {
				this.executeMigrate();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.getParent().getApplication().getMainWindow().showNotification("按CSR迁移任务失败，请重试！");
			}
		} else if(source == cancel) {
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * jrh 供待迁移项目选择框使用
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == migrateProCombo) {
			MarketingProject project = (MarketingProject) migrateProCombo.getValue();
			List<User> users = new ArrayList<User>();
			if(project.getId() == null) {
				for(MarketingProject pro : projectContainer.getItemIds()) {
					if(pro.getId() == null) continue;
					List<User> csrs=marketingProjectService.getCsrsByProject(pro, domain);
					users.addAll(csrs);
					logger.info(users.size() + "");
				}
			} else {
				List<User> csrs=marketingProjectService.getCsrsByProject(project, domain);
				users.addAll(csrs);
			}
			
			// 去除重复的话务员对象
			Map<Long, User> userMap = new HashMap<Long, User>();
			for(User user : users) {
				userMap.put(user.getId(), user);
			}
			
			// 将话务员对象排序，按id号由小到大
			List<User> csrs = new ArrayList<User>(userMap.values());
			Collections.sort(csrs, new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {
					return (int) (o1.getId() - o2.getId());
				}
			});
			
			//移除
			fromContainer.removeAllItems();
			toContainer.removeAllItems();
			//添加CSR
			fromContainer.addAll(csrs);
			toContainer.addAll(csrs);
		}
	}
	
}
