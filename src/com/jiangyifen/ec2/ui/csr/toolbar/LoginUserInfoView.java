package com.jiangyifen.ec2.ui.csr.toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 当前登录用户信息查看与编辑界面
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class LoginUserInfoView extends VerticalLayout implements ClickListener , ValueChangeListener {
	// 用于判断是否拥有编辑用户基础信息的权限
	private static final String BASE_DESIGN_MANAGEMENT_EDIT_CSR_INFO = "base_design_management&edit_csr_info";	
	// 用于判断是否拥有编辑用户密码的权限
	private static final String BASE_DESIGN_MANAGEMENT_EDIT_CSR_PWD_INFO = "base_design_management&edit_csr_pwd_info";	
	
	private final Object[] USER_COL_ORDER = new Object[] { "empNo", "realName", "username", 
			"age", "gender", "phoneNumber", "emailAddress", "department", "roles", "domain", "registedDate"};	// 设置用户表单域的创建项

	private final String[] USER_COL_NAME = new String[] { "职工编号：", "真实姓名：", "用户名：", 
			"用户年龄：", "用户性别：","电话号码：", "邮箱地址：", "所属部门：", "所属角色：", "所 属 域 ：", "注册日期：" };			//设置用户表单中Field的标题名称 

	private Form userForm;
	private PasswordField currentPassword;	// 原密码
	private PasswordField newPassword;		// 新密码
	private PasswordField confirmPassword;	// 确认密码
	private GridLayout pwLayout;			// 存放以上修改密码所需的组件

	private Button edit;					// 信息编辑按钮
	private Button save;					// 信息保存按钮
	private Button cancel;					// 取消编辑按钮
	private CheckBox editPassword;			// 选定是否编辑密码
	private HorizontalLayout handleLayout;	// 存放“编辑、保存、取消、编辑密码”按钮的水平布局管理器 
	
	private Label warningLabel;				// 错误提醒
	private VerticalLayout complexVLayout;	// 存放pwLayout 和 handleLayout
	private Window loginUserWindow;			// 登陆用户信息编辑子窗口
	
	private User loginUser;					// 要新增或修改的User对象
	private String loginUsername;			// 当前登录用户的昵称
	private ArrayList<String> ownBusinessModels;	// 用户持有的所有权限
	private boolean usernameExist = false;	// 用户名是否已存在， 默认是不存在的
	private Boolean isMgr = false;					// 当前登陆用户是否为管理员
	private UserService userService;		// 对角色的各种操作的服务器
	
	public LoginUserInfoView() {
		this.setMargin(true);
		loginUser = SpringContextHolder.getLoginUser();
		loginUsername = loginUser.getUsername();
		
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		userService = SpringContextHolder.getBean("userService");
		
		BeanItem<User> loginUserItem = new BeanItem<User>(loginUser);
		
		userForm = new Form();
		userForm.setImmediate(true);
		userForm.setWriteThrough(false);
		userForm.setFormFieldFactory(new CustomeFieldFactory());
		userForm.setItemDataSource(loginUserItem, Arrays.asList(USER_COL_ORDER));
		for (int i = 0; i < USER_COL_ORDER.length; i++) {
			userForm.getField(USER_COL_ORDER[i]).setCaption(USER_COL_NAME[i]);
		}
		userForm.setReadOnly(true);
		this.addComponent(userForm);
		
		//chb 如果是管理员也让编辑，此处没有考虑管理员作为csr登陆的权限控制
		Set<Role> roles = loginUser.getRoles();
		isMgr = false;
		for(Role role:roles){
			if(role.getType()==RoleType.manager)
				isMgr=true;
		}
		
		// 获取权限模块信息，如果该用户拥有修改用户信息的权限，就创建相应的组件
		if (isMgr || ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_EDIT_CSR_INFO)) {
			userForm.getField("username").setRequired(true);
			userForm.getField("username").addListener(new ValueChangeListener() {
				public void valueChange(ValueChangeEvent event) {
					String username = (String) userForm.getField("username").getValue();
					if(!loginUsername.equals(username)) {
						int count = userService.getEntityCount("select count(u) from User as u where u.username = '" + username + "'");
						usernameExist = (count > 0);
					} else {
						usernameExist = false;
					}
				}
			});
			
			complexVLayout = new VerticalLayout();
			complexVLayout.setSpacing(true);
			this.addComponent(complexVLayout);
			userForm.getFooter().addComponent(complexVLayout);
			
			// 创建密码修改组件
			createPwLayout();
			
			// 创建操作组件 各种按钮以及错误提醒标签
			createOperatorLayout();
			
			// 设置表格属性
			setFormReadOnly(true);
		}
	}

	private void createPwLayout() {
		pwLayout = new GridLayout(2, 3);
		pwLayout.setSpacing(true);
		pwLayout.setVisible(false);
		complexVLayout.addComponent(pwLayout);
		
		currentPassword = new PasswordField();
		currentPassword.setRequired(true);
		pwLayout.addComponent(new Label("原 密 码 ： "), 0, 0);
		pwLayout.addComponent(currentPassword, 1, 0);

		newPassword = new PasswordField();
		newPassword.setRequired(true);
		pwLayout.addComponent(new Label("新 密 码 ： "), 0, 1);
		pwLayout.addComponent(newPassword, 1, 1);

		confirmPassword = new PasswordField();
		confirmPassword.setRequired(true);
		pwLayout.addComponent(new Label("确认密码："), 0, 2);
		pwLayout.addComponent(confirmPassword, 1, 2);
	}

	private void createOperatorLayout() {
		warningLabel = new Label();
		warningLabel.addStyleName("warning");
		warningLabel.setVisible(false);
		complexVLayout.addComponent(warningLabel);

		handleLayout = new HorizontalLayout();
		handleLayout.setSpacing(true);
		complexVLayout.addComponent(handleLayout);

		edit = new Button("编 辑", this);
		edit.setStyleName("default");
		handleLayout.addComponent(edit);
		save = new Button("保 存", this);
		save.setStyleName("default");
		handleLayout.addComponent(save);
		cancel = new Button("取 消", this);
		handleLayout.addComponent(cancel);
		
		editPassword = new CheckBox("修改密码");
		editPassword.setImmediate(true);
		editPassword.addListener((ValueChangeListener) this);
		if(isMgr || ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_EDIT_CSR_PWD_INFO)) {
			handleLayout.addComponent(editPassword);
		}
	}

    public void setFormReadOnly(boolean readOnly) {
        userForm.setReadOnly(readOnly);
        edit.setVisible(readOnly);
        save.setVisible(!readOnly);
        cancel.setVisible(!readOnly);
        editPassword.setVisible(!readOnly);
    }

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == editPassword) {
			Boolean checked = editPassword.booleanValue();
			pwLayout.setVisible(checked);
			currentPassword.setValue("");
			newPassword.setValue("");
			confirmPassword.setValue("");
			if(checked) {
				loginUserWindow.setHeight("515px");
			} else {
				loginUserWindow.setHeight("440px");
				if("PwdError".equals(warningLabel.getData())) {
					warningLabel.setVisible(false);
				}
			}
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			setFormReadOnly(false);
			userForm.getField("empNo").setReadOnly(true);
			userForm.getField("department").setReadOnly(true);
			userForm.getField("roles").setReadOnly(true);
			userForm.getField("domain").setReadOnly(true);
			userForm.getField("registedDate").setReadOnly(true);
		} if(source == save) {
			saveUserInfo();
		} else if(source == cancel) {
			userForm.discard();
			setFormReadOnly(true);
			warningLabel.setVisible(false);
			editPassword.setValue(false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveUserInfo() {
		if(checkSimpleField()) {
			if(!editPassword.booleanValue()) {
				userForm.commit();
			} else {
				String currentPw = (String) currentPassword.getValue();
				String newPw = (String) newPassword.getValue();
				String confirmPw = (String) confirmPassword.getValue();
				if(!currentPw.matches(loginUser.getPassword())) {
					setWarningLabelValue("与原密码不一致！", "PwdError");
					return;
				}
				if(!newPw.matches("\\w{4,32}")) {
					setWarningLabelValue("新密码只能由4-32位字母或数字组成！", "PwdError");
					return;
				}
				if(!newPw.equals(confirmPw)) {
					setWarningLabelValue("两次输入密码不一致！", "PwdError");
					return;
				}
				userForm.commit();
				loginUser = ((BeanItem<User>)userForm.getItemDataSource()).getBean();
				loginUser.setPassword(newPw);
				editPassword.setValue(false);
			}
			
			userService.update(loginUser);
			setFormReadOnly(true);
			warningLabel.setVisible(false);
		}
	}

	private boolean checkSimpleField() {
		String username = StringUtils.trimToEmpty((String) userForm.getField("username").getValue());
		String emailAddress = (String) userForm.getField("emailAddress").getValue();
		String phoneNumber = (String) userForm.getField("phoneNumber").getValue();
		if(!username.matches("\\w{0,64}")) {
			setWarningLabelValue("用户名称 只能由长度不大于64位的字母或数字组成", "BaseError");
			return false;
		} else if("".equals(username)) {
			setWarningLabelValue("昵称不能为空！", "BaseError");
			return false;
		} else if(usernameExist) {
			setWarningLabelValue("该昵称已经存在, 请修正后再提交！", "BaseError");
			return false;
		}
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			if(!phoneNumber.matches("\\d+")) {
				setWarningLabelValue("电话号码只能为数字！", "BaseError");
				return false;
			}
		}
		if(!"".equals(emailAddress) && emailAddress != null) {
			if(!emailAddress.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")) {
				setWarningLabelValue("邮箱格式错误！", "BaseError");
				return false;
			}
		}
		return true;
	}

	private void setWarningLabelValue(String value, String single) {
		warningLabel.setValue(value);
		warningLabel.setData(single);
		warningLabel.setVisible(true);
	}
	
	private class CustomeFieldFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("gender".equals(propertyId)) {
				OptionGroup genderOption = new OptionGroup();
				genderOption.setNullSelectionAllowed(false);
				genderOption.addStyleName("twocolchb");
				genderOption.addStyleName("myopacity");
				genderOption.addItem("男");
				genderOption.addItem("女");
				return genderOption;
			} else if("age".equals(propertyId)) {
				ComboBox ageSelector = new ComboBox();
				ageSelector.setWidth("60px");
				for(int i = 1; i <= 150; i++) {
					ageSelector.addItem(i);
				}
				return ageSelector;
			}
			Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
			if (field instanceof TextField) {
				((TextField) field).setNullRepresentation("");
			}
			return field;
		}
	}

	public void setLoginUserWindow(Window loginUserWindow) {
		this.loginUserWindow = loginUserWindow;
	}

	public Button getCancel() {
		return cancel;
	}
	
}
