package com.jiangyifen.ec2.ui.mgr.usermanage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.RoleService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.eaoservice.impl.UserServiceImpl;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.UserManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 批量添加用户
 *
 * @author jrh
 */
@SuppressWarnings("serial")
public class MutiAddUser extends Window implements Button.ClickListener {
	
	private TextField amount;
	private TextField startEmpno;
	private TextField startUsername;
	private OptionGroup passwordType;
	private HorizontalLayout secretLayout;
	private TextField startPassword;
	private ComboBox deptSelector;
	private TwinColSelect roleSelector;
	
	private Label warningLabel;

	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

	private Notification notification;	// 提示信息

	//持有User控制对象的引用
	private UserManagement userManagement;
	private Domain domain;				// 当前登陆用户所属域
	private User loginUser;
	
	private UserService userService;
	private DepartmentService departmentService;
	private RoleService roleService;
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	/**
	 * 添加User 构造函数
	 * @param userManagement 用户显示界面Tab页组件
	 */
	public MutiAddUser(UserManagement userManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("批量添加用户");
		this.userManagement=userManagement;
		
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		
		//Service初始化
		userService = SpringContextHolder.getBean("userService");
		departmentService = SpringContextHolder.getBean("departmentService");
		roleService = SpringContextHolder.getBean("roleService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		// 创建填写用户基本信息的组要组件
		createMainComponents(windowContent);
		
		// 创建保存、取消操作组件
		createOperatorComponents(windowContent);
	}

	@Override
	public void attach() {
		BeanItemContainer<Department> deptContainer=new BeanItemContainer<Department>(Department.class);
//		deptContainer.addAll(departmentService.getAll(domain));
		HashMap<Long, Department> departmentMap = new HashMap<Long, Department>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				for(Department dept : departments) {
					departmentMap.put(dept.getId(), dept);
				}
			}
		}
		deptContainer.addAll(departmentMap.values());
		deptSelector.setContainerDataSource(deptContainer);
		
		BeanItemContainer<Role> roleContainer=new BeanItemContainer<Role>(Role.class);
		roleContainer.addAll(roleService.getAll(domain));
		roleSelector.setContainerDataSource(roleContainer);
		
		// 清空原有数据
		amount.setValue("");
		startEmpno.setValue("");
		startUsername.setValue("");
		startPassword.setValue("");
		warningLabel.setValue("");
		warningLabel.setVisible(false);
		passwordType.setValue("Same2Username");
	}
	
	/**
	 *  创建填写用户基本信息的组要组件
	 * @param windowContent
	 */
	private void createMainComponents(VerticalLayout windowContent) {
		// 一次性添加书数量
		HorizontalLayout amountLayout = new HorizontalLayout();
		amountLayout.setSpacing(true);
		windowContent.addComponent(amountLayout);
		
		Label amountLabel = new Label("添加数量：");
		amountLabel.setWidth("-1px");
		amountLayout.addComponent(amountLabel);
		
		amount = new TextField();
		amount.setWidth("184px");
		amount.setInputPrompt("一次最多添加1000人");
		amount.setRequired(true);
		amountLayout.addComponent(amount);
		
		// 起始工号输入组件
		HorizontalLayout empnoLayout = new HorizontalLayout();
		empnoLayout.setSpacing(true);
		windowContent.addComponent(empnoLayout);
		
		Label empnoLabel = new Label("起始工号：");
		empnoLabel.setWidth("-1px");
		empnoLayout.addComponent(empnoLabel);
		
		startEmpno = new TextField();
		startEmpno.setWidth("184px");
		startEmpno.setRequired(true);
		startEmpno.setInputPrompt("只能是数字");
		empnoLayout.addComponent(startEmpno);
		
		// 起始用户名输入组件
		HorizontalLayout usernameLayout = new HorizontalLayout();
		usernameLayout.setSpacing(true);
		windowContent.addComponent(usernameLayout);
		
		Label usernameLabel = new Label("起始用户名：");
		usernameLabel.setWidth("-1px");
		usernameLayout.addComponent(usernameLabel);
		
		startUsername = new TextField();
		startUsername.setWidth("170px");
		startUsername.setRequired(true);
		usernameLayout.addComponent(startUsername);
		
		// 起始密码输入组件
		HorizontalLayout pwTypeLayout = new HorizontalLayout();
		pwTypeLayout.setSpacing(true);
		windowContent.addComponent(pwTypeLayout);
		
		Label pwTypeLabel = new Label("密码类型：");
		pwTypeLabel.setWidth("-1px");
		pwTypeLayout.addComponent(pwTypeLabel);
		
		passwordType = new OptionGroup();
		passwordType.setStyleName("twocol200");
		passwordType.addItem("Same2Username");
		passwordType.addItem("AllSamePW");
		passwordType.setItemCaption("Same2Username", "与用户名相同");
		passwordType.setItemCaption("AllSamePW", "共用相同密码");
		passwordType.setImmediate(true);
		pwTypeLayout.addComponent(passwordType);
	
		passwordType.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				boolean visible = "AllSamePW".equals(event.getProperty().getValue());
				secretLayout.setVisible(visible);
			}
		});
		
		// 起始密码输入组件
		secretLayout = new HorizontalLayout();
		secretLayout.setSpacing(true);
		secretLayout.setImmediate(true);
		windowContent.addComponent(secretLayout);
		
		Label secretLabel = new Label("共用密码：");
		secretLabel.setWidth("-1px");
		secretLayout.addComponent(secretLabel);
		
		startPassword = new TextField();
		startPassword.setWidth("184px");
		startPassword.setRequired(true);
		secretLayout.addComponent(startPassword);

		// 部门选择组件
		HorizontalLayout deptLayout = new HorizontalLayout();
		deptLayout.setSpacing(true);
		windowContent.addComponent(deptLayout);
		
		Label deptLabel = new Label("所属部门：");
		deptLabel.setWidth("-1px");
		deptLayout.addComponent(deptLabel);
		
		deptSelector = new ComboBox();
		deptSelector.setWidth("184px");
		deptSelector.setItemCaptionPropertyId("name");
		deptSelector.setNullSelectionAllowed(false);
		deptSelector.setTextInputAllowed(false);
		deptSelector.setImmediate(true);
		deptSelector.setInputPrompt("请选择部门");
		deptSelector.setRequired(true);
		deptLayout.addComponent(deptSelector);
		
		// 拥有角色选择组件
		HorizontalLayout roleLayout = new HorizontalLayout();
		roleLayout.setSpacing(true);
		windowContent.addComponent(roleLayout);
		
		Label roleLabel = new Label("拥有角色：");
		roleLabel.setWidth("-1px");
		roleLayout.addComponent(roleLabel);
		
		roleSelector = new TwinColSelect();
		roleSelector.setWidth("350px");
		roleSelector.setItemCaptionPropertyId("name");
		roleSelector.setLeftColumnCaption("所有角色");
	    roleSelector.setRightColumnCaption("所选角色");
        roleSelector.setMultiSelect(true);
        roleSelector.setImmediate(true);
        roleSelector.setRows(4);
        roleSelector.setRequired(true);
		roleLayout.addComponent(roleSelector);
	}
	
	/**
	 * 创建保存、取消操作组件
	 * @return
	 */
	private HorizontalLayout createOperatorComponents(VerticalLayout windowContent) {
		warningLabel = new Label("", Label.CONTENT_XHTML);
		warningLabel.setVisible(false);
		warningLabel.setWidth("-1px");
		windowContent.addComponent(warningLabel);
		
		HorizontalLayout operatorLayout = new HorizontalLayout();
		operatorLayout.setSpacing(true);
		windowContent.addComponent(operatorLayout);
		
		// 保存按钮
		save = new Button("保存");
		save.setStyleName("default");
		save.addListener((ClickListener)this);
		operatorLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消");
		cancel.addListener((ClickListener)this);
		operatorLayout.addComponent(cancel);

		return operatorLayout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save) {
			excuteSave();
		} else if(source == cancel) {
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * 执行保存操作
	 */
	@SuppressWarnings("unchecked")
	private void excuteSave() {
		String amountStr = amount.getValue().toString().trim();
		String startEmpnoStr = startEmpno.getValue().toString().trim();
		String startUsernameStr = startUsername.getValue().toString().trim();
		String startPasswordStr = startPassword.getValue().toString().trim();
		String pwType = (String) passwordType.getValue();
		Department dept = (Department) deptSelector.getValue();
		Set<Role> roles = (Set<Role>) roleSelector.getValue();
		try {
			Map<String, Integer> resultMap = userService.addMutiUsers(amountStr, startEmpnoStr, startUsernameStr, startPasswordStr, pwType, dept, roles, domain);
			reloadAsteriskService.reloadVoicemail();
			
			StringBuffer sb = new StringBuffer();
			sb.append("计划添加用户人数：" +amountStr+ "<br/>");
			sb.append("成功添加用户人数："+resultMap.get(UserServiceImpl.IMPORT_SUCCESS)+"<br/>");
			sb.append("已经存在用户人数："+resultMap.get(UserServiceImpl.IMPORT_IGNORED)+"<br/>");
			notification.setCaption(sb.toString());
			this.getApplication().getMainWindow().showNotification(notification);
			userManagement.refreshTable(true);
			this.getParent().removeWindow(this);
		} catch (Exception e) {
			warningLabel.setValue("<font color='red'>"+e.getMessage()+"</font>");
			warningLabel.setVisible(true);
		}
	}

}
