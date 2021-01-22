package com.jiangyifen.ec2.ui.mgr.usermanage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.RoleService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
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
public class EditUser extends Window implements Button.ClickListener {

	private static final Object[] USER_COL_ORDER = new Object[] { "empNo", "realName", "username", "password", "age", "gender", "phoneNumber", "emailAddress", "department", "roles" };	// 设置用户表单域的创建项
	private static final String[] USER_COL_NAME = new String[] { "工  号", "真实姓名", "用 户 名", "用户密码", "用户年龄", "用户性别","电话号码", "邮箱地址", "所属部门", "所属角色" };	//设置用户表单中Field的标题名称
	
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
	private String originalPhoneNum;	// 当前被编辑用户的原有手机号
	private List<Role> allRoles;
	
	private String originalEmpNo; 		//比较工号是否和刚开始时相等使用
	private String originalUsername;  	//比较用户名是否和刚开始时相等使用
	private UserService userService;
	private DepartmentService departmentService;
	private RoleService roleService;
	private QueueService queueService;
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员关系管理服务类
	private Phone2PhoneSettingService phone2PhoneSettingService;	// 外转外配置服务类
	private UserQueueService userQueueService;						// 动态队列成员服务类

	/**
	 * 添加User
	 * @param project
	 */
	public EditUser(UserManagement userManagement) {
		this.center();
		this.setModal(true);
		this.userManagement=userManagement;

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		
		//Service初始化
		userService=SpringContextHolder.getBean("userService");
		departmentService=SpringContextHolder.getBean("departmentService");
		roleService=SpringContextHolder.getBean("roleService");
		queueService=SpringContextHolder.getBean("queueService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
		phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		
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
		//创建Form时提前取出所有的角色，以用于回显做准备
		allRoles = roleService.getAll(domain);
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
	@SuppressWarnings("unchecked")
	private void updateFormInfo(){
		this.setCaption("编辑用户");
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<User>(user),Arrays.asList(USER_COL_ORDER));
		for (int i = 0; i < USER_COL_ORDER.length; i++) {
			form.getField(USER_COL_ORDER[i]).setCaption(USER_COL_NAME[i]);
		}
		
		//回显部门		
		ComboBox depts = (ComboBox) form.getField("department");
		Collection<Department> deptSource = (Collection<Department>) depts.getContainerDataSource().getItemIds();
		for(Department dept : deptSource) {
			if(dept.getId().equals(user.getDepartment().getId())){
				form.getField("department").setValue(dept);
				break;
			}
		}
		
		//设置特殊组件的回显信息，设置角色的选中情况，如果用户包含角色，则回显角色
		Set<Role> selectedRoles = user.getRoles();
		Set<Role> roleSelectValue = new HashSet<Role>();
		for(Role role:selectedRoles){
			for(int i=0;i<allRoles.size();i++){
				if(role.getId().equals(allRoles.get(i).getId())){
					roleSelectValue.add(allRoles.get(i));
				}
			}
		}
		form.getField("roles").setValue(roleSelectValue);
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private boolean executeSave() {
		try {
			//form.commit();  已经在按钮单击时commit过了
			user.setRegistedDate(new Date());
			user.setDomain(SpringContextHolder.getDomain());
			user = userService.update(user);
			//刷新userManagement的Table在当前页
			userManagement.refreshTable(false);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存用户失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		return true;
	}
	
	/**
	 *内部类 
	 */
	private class MyFieldFactory extends DefaultFieldFactory{
		private String mailRegex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("empNo".equals(propertyId)){
				TextField empNoField = new TextField();
				empNoField.setRequired(true);
				empNoField.setWidth("180px");
				empNoField.setNullRepresentation("");
				empNoField.setNullSettingAllowed(true);
				empNoField.setRequiredError("职工编号不能为空");
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
				// TODO 武睿定制开发, 去掉用户名验证
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
				roleContainer.addAll(allRoles);
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
//				deptContainer.addAll(departmentService.getAll(domain));
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
				field.addValidator(new RegexpValidator("^[2E80-\\u9fa5\\-]{0,64}$", "年龄只能由长度不大于64的数字组成或为空"));
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
		user = (User)userManagement.getTable().getValue();
		originalEmpNo = user.getEmpNo();
		originalUsername = user.getUsername();
		originalPhoneNum = user.getPhoneNumber();
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
			//工号字段
			String empNo="";
			String currentphoneNum = "";
			try {
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

				// jrh 用户当前电话号码
				Object obj3 = form.getField("phoneNumber").getValue();
				if(obj3 != null) {
					currentphoneNum = obj3.toString();
				}
				
				//取出相同工号或者Csr的用户名
				List<User> sameEmpNoCsrs = userService.getUsersByEmpNo(empNo, domain);
				List<User> sameUsernameCsrs = userService.getUsersByUsername(username);
				//如果是新工号，则进行判断
				if((!originalEmpNo.equals(empNo))&&sameEmpNoCsrs.size()>0){
					this.showNotification("新工号已经被占用，请重新输入工号");
					return;
				}
				//如果是新用户名，则进行判断
				if((!originalUsername.equals(username))&&sameUsernameCsrs.size()>0){
					this.showNotification("新用户名已经被占用，请重新输入用户名");
					return;
				}
				
				form.commit();
			} catch (Exception e) {
				return;
			}
			boolean saveSuccess = executeSave();
			if(saveSuccess) {	
				// 如果更新用户成功，并且工号发生了变化则重新加载 ShareData 中的domainToEmpNos
				if(!originalEmpNo.equals(empNo)) {
					List<String> empNos = new ArrayList<String>();
					for(User user : userService.getAllByDomain(domain)) {
						empNos.add(user.getEmpNo());
					}
				}
				// jrh 如果话务员电话号发生变化，根据实际情况更新队列成员
				updatePhoneInQueueMember(originalPhoneNum, currentphoneNum);
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * jrh
	 * 	如果话务员电话号发生变化，根据实际情况更新队列成员
	 * @param originalPhoneNum	原有电话号码
	 * @param currentPhoneNum	当前电话号码
	 */
	private void updatePhoneInQueueMember(String originalPhoneNum, String currentphoneNum) {
		String defaultOutline = ShareData.domainToDefaultOutline.get(domain.getId());;
		if(defaultOutline == null) {
			return;		// 如果默认外线不存在，则不作任何操作
		}
		
		// 第一步，判断全局配置外转外
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domain.getId());
		// 如果全局配置不存在，则直接返回
		if(globalSetting == null) {
			return;
		}
		
		// 获取所有非自动使用的队列
		List<Queue> allCommonQueues = queueService.getAllByDomain(domain, true);
		List<String> allCommonQueueNames = new ArrayList<String>();
		for(Queue autoQueue : allCommonQueues) {
			allCommonQueueNames.add(autoQueue.getName());
		}
		
		// 对于全局配置而言，添加手机号到队列需要满足 1、全局配置是启动状态，2、外转外配置的呼叫方式为“智能呼叫”(即指定话务员)
		boolean isGlobalRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);
		if(isGlobalRunning) {
			// 如果当前正在进行的方式是“便捷呼叫”，则直接退出
			if(globalSetting.getIsSpecifiedPhones()) {
				return;
			} 
			
			// 如果全局配置是“智能呼叫”，并且处于进行中状态，而且包含当前话务员，则不再处理话务员的自定义配置项
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(user.getId())) {
					updateSpecifyCsrQueueMember(specifiedCsr, originalPhoneNum, currentphoneNum, defaultOutline, allCommonQueueNames);
					return;
				}
			}
		} 

		// 第二步：判断话务员自定义的外转外配置(能执行到这，说明如果是“便捷呼叫”，则当前全局配置一定没有运行外转外)
		// 对自定义的设置而言，添加手机号到队列，需要满足 1、全局配置的外转外呼叫方式不是打到指定的手机号，2、全局配置项中之指定的话务员中不包含当前话务员
		if(!globalSetting.getIsSpecifiedPhones()) {
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				// 如果话务员包含在全局配置中，则以全局配置为主[智能呼叫：管理员对那些选中的话务员是完全控制外转外的]
				if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(user.getId())) {
					return;
				}
			}
		}
		// 3、话务员持有自定义外转外的授权，4、自定义的外转外存在并是开启状态，5、启动时刻 <= 当前时刻
		if(globalSetting.getIsLicensed2Csr()) {
			Phone2PhoneSetting customSetting = phone2PhoneSettingService.getByUser(user.getId());
			if(customSetting != null) {
				boolean isCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
				if(isCustomRunning) {
					updateSpecifyCsrQueueMember(customSetting.getCreator(), originalPhoneNum, currentphoneNum, defaultOutline, allCommonQueueNames);
				}
			}
		}
	}

	/**
	 * jrh
	 * 	根据话务员对象，如果话务员电话号发生变化，并且不在线，则更新队列中的手机成员
	 * @param csr				话务员对象
	 * @param originalPhoneNum	原有电话号码
	 * @param currentPhoneNum	当前电话号码
	 * @param outlineName		默认外线
	 * @param allCommonQueues	所有非自动外呼使用的队列
	 */
	private void updateSpecifyCsrQueueMember(User csr, String originalPhoneNum, String currentPhoneNum, 
			String outlineName, List<String> allCommonQueues) {
		// 如果用户已经登陆，则不做处理， 因为用户已登录的情况，呼入队列时找分机
		if(ShareData.userToExten.keySet().contains(csr.getId())) {
			return;
		}

		// 只有话务员不在线，才需要将话务员的手机号移入队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(!allCommonQueues.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				continue;
			}
			if(originalPhoneNum != null && !"".equals(originalPhoneNum)) {
				queueMemberRelationService.removeQueueMemberRelation(queueName, originalPhoneNum+"@"+outlineName);
			}
			if(currentPhoneNum != null && !"".equals(currentPhoneNum)) {
				queueMemberRelationService.addQueueMemberRelation(queueName, currentPhoneNum+"@"+outlineName, 5);
			}
		}
	}
	
}
