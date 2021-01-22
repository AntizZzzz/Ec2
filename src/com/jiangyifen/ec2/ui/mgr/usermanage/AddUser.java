package com.jiangyifen.ec2.ui.mgr.usermanage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.RoleService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.UserManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 *添加，可以添加用户
 * @author chb
 */
@SuppressWarnings("serial")
public class AddUser extends Window implements Button.ClickListener {
//	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final Object[] USER_COL_ORDER = new Object[] { "empNo", "realName", "username","password", "age", "gender", "phoneNumber", "emailAddress", "department", "roles" };	// 设置用户表单域的创建项
	private static final String[] USER_COL_NAME = new String[] { "工  号", "真实姓名", "用 户 名", "用户密 码", "用户年龄", "用户性别","电话号码", "邮箱地址", "所属部门", "所属角色" };	//设置用户表单中Field的标题名称
	
/**
 * 主要组件输出	
 */
	//Form输出
	private Form form;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

/**
 * 其他组件
 */
	//持有User控制对象的引用
	private UserManagement userManagement;
	private User user;
	private User loginUser;
	private Domain domain;				// 当前登陆用户所属域
	
	private UserService userService;
	private DepartmentService departmentService;
	private RoleService roleService;
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	/**
	 * 添加User
	 * @param project
	 */
	public AddUser(UserManagement userManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.userManagement=userManagement;
		
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		
		//Service初始化
		userService = SpringContextHolder.getBean("userService");
		departmentService = SpringContextHolder.getBean("departmentService");
		roleService = SpringContextHolder.getBean("roleService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//From输出
		form=new Form();
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
		form.addStyleName("chb");
		form.setImmediate(true);
		form.setWriteThrough(false);	// 设置为 false 禁用 form 缓存; 设置为 true 表示启用缓存
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(buildButtonsLayout());
		windowContent.addComponent(form);
	}
	
	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		// 保存按钮
		save = new Button("保存");
		save.setStyleName("default");
		save.addListener(this);
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);

		return buttonsLayout;
	}
	
	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo(){
		//如果项目为null，则自己新建项目，否则为编辑项目
		this.setCaption("新建用户");
		user=new User();
		
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<User>(user),Arrays.asList(USER_COL_ORDER));
		for (int i = 0; i < USER_COL_ORDER.length; i++) {
			form.getField(USER_COL_ORDER[i]).setCaption(USER_COL_NAME[i]);
		}
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 * @return
	 */
	private boolean executeSave() {
		try {
			//已经在按钮单击时commit过了
			user.setRegistedDate(new Date());
			user.setDomain(domain);
			userService.update(user);
			//刷新userManagement的Table在当前页
			userManagement.refreshTable(true);
			userManagement.getTable().setValue(null);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存用户失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		/**
		 * @changelog 2014-6-19 上午10:34:18 chenhb <p>description: 按照用户ID添加语音信箱</p>
		 * 获取域内所有用户，更新用户语音信箱配置文件
		 */
		List<User> userList = userService.getAllByDomain(domain);
		boolean isSuccess = userService.updateVoicemailConfig(domain, userList);
		if(isSuccess) {
			reloadAsteriskService.reloadVoicemail();
		}
		
		
		return true;
	}
	
	private class MyFieldFactory extends DefaultFieldFactory{
		private String mailRegex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("empNo".equals(propertyId)){
				TextField empNoField = new TextField();
				empNoField.setRequired(true);
				empNoField.setNullRepresentation("");
				empNoField.setNullSettingAllowed(true);
				empNoField.setRequiredError("职工编号不能为空");
				empNoField.setWidth("180px");
				empNoField.addValidator(new RegexpValidator("\\d{1,32}", "工号只能由长度不大于32位的数字组成"));
				return empNoField;
			}else if("password".equals(propertyId)){
				TextField passwordField=new TextField();
				passwordField.setNullRepresentation("");
				passwordField.setRequired(true);
				passwordField.setWidth("180px");
				passwordField.setRequiredError("登录密码不能为空");
				passwordField.addValidator(new RegexpValidator("\\w{0,32}", "密码只能由长度不大于32位的字符组成"));
				return passwordField;
			}else if("username".equals(propertyId)){
				TextField username=new TextField();
				username.setNullRepresentation("");
				username.setRequired(true);
				username.setWidth("180px");
				username.setRequiredError("用户名不能为空");
				// TODO 武睿定制, 去掉用户名验证
				// username.addValidator(new RegexpValidator("\\w{0,64}", "用户名只能由长度不大于64位的字目或数字组成"));
				return username;
			}else if("gender".equals(propertyId)) {
				OptionGroup optionGroup=new OptionGroup("用户性别", Arrays.asList("男","女"));
				optionGroup.setStyleName("twocolchb");
				return optionGroup;
			} else if ("roles".equals(propertyId)) {
				TwinColSelect roleSelect = new TwinColSelect();
				roleSelect.setItemCaptionPropertyId("name");
				roleSelect.setLeftColumnCaption("所有角色");
			    roleSelect.setRightColumnCaption("所选角色");
		        roleSelect.setMultiSelect(true);
		        roleSelect.setImmediate(true);
		        roleSelect.setWidth("350px");
		        roleSelect.setRows(4);
		        roleSelect.setRequired(true);
		        roleSelect.setRequiredError("请为用户选择应有的角色");
		        //设置RoleSelect的数据源
		        BeanItemContainer<Role> roleContainer=new BeanItemContainer<Role>(Role.class);
				roleContainer.addAll(roleService.getAll(domain));
				roleSelect.setContainerDataSource(roleContainer);
				return roleSelect;
			} else if ("department".equals(propertyId)) {
				ComboBox depts = new ComboBox();
				BeanItemContainer<Department> deptContainer=new BeanItemContainer<Department>(Department.class);
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
//				List<Department> allDepts = departmentService.getAll(domain);
//				deptContainer.addAll(allDepts);
				depts.setContainerDataSource(deptContainer);
				depts.setItemCaptionPropertyId("name");
				depts.setNullSelectionAllowed(false);
				depts.setInputPrompt("请选择部门");
				depts.setTextInputAllowed(false);
				depts.setImmediate(true);
				depts.setWidth("180px");
				depts.setRequired(true);
				depts.setRequiredError("请为用户选择部门");
				return depts;
			}
			Field field = DefaultFieldFactory.get().createField(item,
					propertyId, uiContext);
			
			if("realName".equals(propertyId)) {
				field.addValidator(new RegexpValidator("^[2E80-\\u9fa5\\-]{0,64}$", "年龄只能由长度不大于4的数字组成或为空"));
			} else if("age".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,4}", "年龄只能由长度不大于4的数字组成或为空"));
			} else if("phoneNumber".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,32}", "电话只能由长度不大于32位的数字组成或为空"));
			} else if("emailAddress".equals(propertyId)) {
				field.addValidator(new RegexpValidator(mailRegex, "邮箱格式不正确"));
			}
			
			if (field instanceof TextField) {
				((TextField) field).setNullRepresentation("");
				((TextField) field).setNullSettingAllowed(true);
			}
			field.setWidth("180px");
			return field;
		}
	}
	

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		user=null;
		updateFormInfo();
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			Domain domain = SpringContextHolder.getDomain();
			try {
				//工号字段
				String empNo="";
				Object obj1 = form.getField("empNo").getValue();
				if(obj1!=null){
					empNo=obj1.toString();
				}	

				//用户名字段
				String username="";
				Object obj2 = form.getField("username").getValue();
				if(obj2!=null){
					username=obj2.toString();
				}	
				
				//取出相同工号或者Csr的用户名
				List<User> sameEmpNoCsrs = userService.getUsersByEmpNo(empNo, domain);
				List<User> sameUsernameCsrs = userService.getUsersByUsername(username);
				if(sameEmpNoCsrs.size()>0){
					this.showNotification("工号已经被占用，请重新输入工号");
					return;
				}
				if(sameUsernameCsrs.size()>0){
					this.showNotification("用户名已经被占用，请重新输入用户名");
					return;
				}

				form.commit();
			} catch (Exception e) {
				return;
			}
			boolean saveSuccess = executeSave();
			if(saveSuccess) {
				// 如果添加用户成功，则重新加载 ShareData 中的domainToEmpNos
				List<String> empNos = new ArrayList<String>();
				for(User user : userService.getAllByDomain(domain)) {
					empNos.add(user.getEmpNo());
				}
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}
}
