package com.jiangyifen.ec2.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.Ec2Application;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.license.LicenseManager;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.BusinessModelService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.csr.CsrMainView;
import com.jiangyifen.ec2.ui.mgr.MgrMain;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * Admin 用户的登陆界面
 * 
 * @author Administrator
 */
@SuppressWarnings("serial")
public class LoginLayout extends VerticalLayout implements ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Boolean isValid=true;
	
	private TextField usernameTextField; 		// 用户名输入区
	private PasswordField passwordTextField; 	// 用户密码输入区
	private TextField extenNoField; 			// CSR 用户 分机号输入区
	private Button login; 						// 登录按钮
	private NativeButton forget; 				// 忘记密码按钮
	private Label warningLabel; 				// 错误提示标签

	private Button confirm;						// 当用户登陆出现冲突时, 用于 确认 继续登陆
	private Button cancel;						// 当用户登陆出现冲突时, 用于 取消 继续登陆
	private RichTextArea conflictNotice;		// 发生冲突者描述信息
	private Window conflictManageWindow;		// 创建冲突处理界面 
	
	private WebBrowser webBrowser;				// 当前用户所使用的浏览器
	private RoleType roleType; 					// 当前登录界面对应的角色类型
	private String username = ""; 				// 当前登录者的用户名
	private String password = ""; 				// 当前登录者的密码
	private String exten = ""; 					// 当前登录者使用的分机
	private User loginUser; 					// 当前登录的用户对象

	private UserService userService; 			// 用户服务类
	private UserLoginService userLoginService; 	// 用户登录服务类
	private BusinessModelService businessModelService; 

	private Ec2LoginLayout ec2LoginLayout;
	private Application app;
	
	/**
	 * 登陆界面的构造函数
	 * 
	 * @param roleType 当前登者所使用的角色
	 */
	public LoginLayout(RoleType roleType, Ec2LoginLayout ec2LoginLayout) {
		// 初始化该类需要的一些字段
		this.setSpacing(true);
		this.setMargin(true);
		this.roleType = roleType;
		this.ec2LoginLayout = ec2LoginLayout;
		
		userLoginService = SpringContextHolder.getBean("userLoginService");
		userService = SpringContextHolder.getBean("userService");
		businessModelService = SpringContextHolder.getBean("businessModelService");
		
		// 获取Cookie值
		this.getCookies();
		
		Panel loginPanel = new Panel(roleType.name().toUpperCase()+ " LOGIN INTERFACE");
		loginPanel.setWidth("380px");
		loginPanel.setHeight("260px");
		this.addComponent(loginPanel);
		this.setComponentAlignment(loginPanel, Alignment.BOTTOM_CENTER);

		// 创建登录用的主要组件
		VerticalLayout panelContent = (VerticalLayout) loginPanel.getContent();
		createLoginMainComponents(panelContent, roleType);
		
		// 创建冲突处理界面 (当用户登陆出现冲突时, 用于确认是否继续登陆)
		createConflictManageWindow();
	}
	
	// 获取cookie   jrh 20131022
	private void getCookies() {
        // jrh 从浏览器获取cookie
        Cookie[] cookies = Ec2Application.request.getCookies();
        if(cookies != null) {
        	for (int i = 0; i < cookies.length; i++) {
        		if ("username".equals(cookies[i].getName())) {
        			username = cookies[i].getValue();
        		} else if("exten".equals(cookies[i].getName())) {
        			exten = cookies[i].getValue();
        		}
        	}
        }

      System.out.println("username找到cookie了------：》[username---"+username + "--exten---"+exten+" ]");
	}
	
	/**
	 * 创建登录用的主要组件
	 * 
	 * @param panelContent 登录面板的不觉管理器
	 * @param roleType		当前登陆者的角色
	 */
	private void createLoginMainComponents(VerticalLayout panelContent, RoleType roleType) {
		GridLayout gridLayout = new GridLayout(2, 5);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true);
		panelContent.addComponent(gridLayout);
		panelContent.setComponentAlignment(gridLayout, Alignment.MIDDLE_CENTER);

		// 添加用户名标签和文本输入框
		Label username_lb = new Label("用&nbsp;&nbsp;户&nbsp;&nbsp;名：",Label.CONTENT_XHTML);
		gridLayout.addComponent(username_lb, 0, 0);
		usernameTextField = new TextField();
		usernameTextField.setWidth("170px");
    	usernameTextField.setValue(username);
		gridLayout.addComponent(usernameTextField, 1, 0);

		// 添加密码标签和文本输入框
		Label password = new Label("密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码：",Label.CONTENT_XHTML);
		gridLayout.addComponent(password, 0, 1);
		passwordTextField = new PasswordField();
		passwordTextField.setWidth("170px");
		gridLayout.addComponent(passwordTextField, 1, 1);

		// 如果是 客服Csr 系统登录界面，则添加分机输入框
		if (roleType.equals(RoleType.csr)) {
			Label extenNoLabel = new Label("分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机：",Label.CONTENT_XHTML);
			gridLayout.addComponent(extenNoLabel, 0, 2);
			extenNoField = new TextField();
			extenNoField.setWidth("170px");
	    	extenNoField.setValue(exten);
			gridLayout.addComponent(extenNoField, 1, 2);
		}

		// 添加提示标签（默认是隐藏状态）
		String warningMsg="<font color='red'>用户名或密码错误，请重试！</font>";
		warningLabel = new Label(warningMsg, Label.CONTENT_XHTML);
		warningLabel.setVisible(false);
		
		gridLayout.addComponent(warningLabel, 1, 3);
		gridLayout.setComponentAlignment(warningLabel, Alignment.MIDDLE_CENTER);

		// 创建登录和忘记密码按钮
		login = new Button("登  录", this);
		forget = new NativeButton("忘记密码", this);
		
		//如果系统过期显示提示信息
		Map<String, String> licenseMap = LicenseManager.licenseValidate();
		String validateResult=licenseMap.get(LicenseManager.LICENSE_VALIDATE_RESULT);
		if(LicenseManager.LICENSE_VALID.equals(validateResult)){
			SpringContextHolder.getHttpSession().removeAttribute("businessModels");
			String licensedDate = licenseMap.get(LicenseManager.LICENSE_DATE);
			try {
				Date stopDate=LicenseManager.simpleDateFormat.parse(licensedDate);
				Long times=stopDate.getTime()-new Date().getTime();
				int outdateWarnDay=(int)(times/(24*3600*1000));
				
				if(outdateWarnDay < 7){
					isValid=true;
					warningLabel.setValue("<font color='red'>系统还有"+outdateWarnDay +"天过期,请联系管理员！</font>");
					if(outdateWarnDay < 0) {
						warningLabel.setValue("<font color='red'>系统已经过期,请联系管理员！</font>");
						isValid=false;
					} else if(outdateWarnDay  == 0) {
						warningLabel.setValue("<font color='red'>系统明天即将过期,请尽快联系管理员！</font>");
					}
					warningLabel.setVisible(true);
				}
//				//如果是管理员，则登陆按钮继续可用
//				if(roleType==RoleType.manager){
//					login.setEnabled(true);
//				}
//				
			} catch (Exception e) {
				e.printStackTrace();
				login.setEnabled(false);
				forget.setEnabled(false);
			}
			
		}else{
			warningLabel.setVisible(true);
			warningLabel.setValue("<font color='red'>系统授权已经失效,请联系管理员！</font>");
			login.setEnabled(false);
			forget.setEnabled(false);
			isValid=false;
			

			//chb 特殊处理，允许管理员过期登陆
			if(roleType==RoleType.manager){
				if(isValid==false){
					login.setEnabled(true);
				}else{
					//normal login
				}
			}

		}

		HorizontalLayout operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		operatorHLayout.addComponent(login);
		operatorHLayout.addComponent(forget);
		gridLayout.addComponent(operatorHLayout, 1, 4);
	}

	/**
	 * 创建冲突处理界面 (当用户登陆出现冲突时, 用于确认是否继续登陆)
	 */
	private void createConflictManageWindow() {
		conflictManageWindow = new Window("登陆冲突");
		conflictManageWindow.setModal(true);
		conflictManageWindow.setResizable(false);
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		mainLayout.setSizeUndefined();
		mainLayout.setWidth("400px");
		conflictManageWindow.setContent(mainLayout);

		Label captionLabel = new Label("<font color='blue'><B>当前冲突人员：</B></font>", Label.CONTENT_XHTML);
		captionLabel.setWidth("-1px");
		mainLayout.addComponent(captionLabel);
		
		conflictNotice = new RichTextArea();
		conflictNotice.setReadOnly(true);
		conflictNotice.setWriteThrough(false);
		conflictNotice.setWidth("-1px");
		mainLayout.addComponent(conflictNotice);
		
		String placeholder = "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";
		Label noticeLabel = new Label("<font color='red'><B>"+placeholder+"如果您确认登陆，以上人员将被迫退出系统！</B></font>", Label.CONTENT_XHTML);
		noticeLabel.setWidth("-1px");
		mainLayout.addComponent(noticeLabel);
		
		Label confirmLabel = new Label("<B>"+placeholder+"您确定要继续登陆吗？？</B>", Label.CONTENT_XHTML);
		confirmLabel.setWidth("-1px");
		mainLayout.addComponent(confirmLabel);
		
		// 在表单底部创建保存和取消按钮
		HorizontalLayout operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		mainLayout.addComponent(operatorHLayout);

		confirm = new Button("确定", this);
		confirm.setImmediate(true);
		cancel = new Button("取消", this);
		cancel.setStyleName("default");
		cancel.setImmediate(true);
		operatorHLayout.addComponent(confirm);
		operatorHLayout.addComponent(cancel);
	}
	
	/**
	 * 处理登录事件 根据用户填写的username、password、exten，查找数据库中是否存在该对象
	 */
	public void handleLoginClickEvent() {
		// 获取WebBrowser对象
		if(app == null) {
			app = this.getApplication();
		}
		WebApplicationContext context = (WebApplicationContext) app.getContext();
		webBrowser = context.getBrowser();

		// 取出用户名
		if (usernameTextField.getValue() != null) {
			username = usernameTextField.getValue().toString().trim();
		}
		
//		 TODO delete
// if(RoleType.csr.equals(roleType)) {
// username = "1001";
// } else if(RoleType.manager.equals(roleType)) {
// username = "mgr";
// }
		if (username.equals("")) {
			warningLabel.setValue("<font color='red'>用户名不能为空</font>");
			warningLabel.setVisible(true);
			return;
		}

		// 取出密码
		if (passwordTextField.getValue() != null) {
			password = passwordTextField.getValue().toString().trim();
		}
//		 TODO delete
// if(RoleType.csr.equals(roleType)) {
// password = "8888";
// } else if(RoleType.manager.equals(roleType)) {
// password = "123456";
// password = "1001";
// }
		if (password.equals("")) {
			warningLabel.setValue("<font color='red'>密码不能为空</font>");
			warningLabel.setVisible(true);
			return;
		}

		// 进行登陆操作
		if (roleType.equals(RoleType.csr)) {
			if (extenNoField.getValue() != null) {
				exten = extenNoField.getValue().toString().trim();
			}
// TODO
//exten = "800001";
			if (exten.equals("")) {
				warningLabel.setValue("<font color='red'>分机不能为空</font>");
				warningLabel.setVisible(true);
				return;
			}
			try {
				List<User> existedUsers = userLoginService.loginConflict(username, password, exten, roleType);
				boolean loginAble = existedUsers.size() == 0 ? true : false; 
				if(loginAble == false) {
					showConflictManageWindow(existedUsers);
					return;
				} else {
					String ip = webBrowser.getAddress(); 	// 当前用户使用的 ip 地址
					loginUser = userLoginService.login(username, password, exten, ip, roleType);
				}
			} catch (Exception e) {
				logger.warn("CSR登陆失败！ 失败原因：" + e.getMessage(), e);
				warningLabel.setValue("<font color='red'>" + e.getMessage()+ "</font>");
				warningLabel.setVisible(true);
				return;
			}
		} else if (roleType.equals(RoleType.manager)) {
			loginUser = userService.identify(username, password, RoleType.manager);
		}

		
		// 当没有发现冲突用户时才会执行到这里
		handleAfterLogin(webBrowser, exten);

		
	}

	/**
	 * 当用户登陆后，保存相关信息到内存
	 * 
	 * @param webBrowser
	 * @param exten
	 */
	private void handleAfterLogin(WebBrowser webBrowser, String exten) {
		// 获取主窗口
		Window mainWindow = app.getMainWindow();
		// 如果登陆不成功
		if (loginUser == null) {
			warningLabel.setValue("<font color='red'>请检查用户名或密码是否正确</font>");
			warningLabel.setVisible(true);
			return;
		}

		// jrh 存cookie
        saveCookies();

		// ----------------登陆成功 向Session中存储需要的信息  -------------------------------- //

		// 存储分辨率信息
		Integer[] screenResolution = new Integer[2];
		screenResolution[0] = webBrowser.getScreenWidth();
		screenResolution[1] = webBrowser.getScreenHeight();
		
		HttpSession session = SpringContextHolder.getHttpSession();
		session.setAttribute("screenResolution", screenResolution);

		// 存储权限
		ArrayList<String> businessModels = new ArrayList<String>();
		
		if(isValid==false){			//过期了，则只显示修改licence 的权限
			businessModels.add(MgrAccordion.SYSTEM_INFO_MANAGEMENT);
			businessModels.add(MgrAccordion.SYSTEM_INFO_MANAGEMENT_SYSTEM_LISCENCE);
			session.setAttribute("businessModels", businessModels);
		} else {
			HashMap<String, BusinessModel> ownBusinessModleMap = new HashMap<String, BusinessModel>();
			for (Role role : loginUser.getRoles()) {
				if (role.getType().equals(roleType)) {	// 当前登录者可能包含多个管理员角色、或多个Csr角色
					
					List<BusinessModel> bmResults = businessModelService.getModelsByRoleId(role.getId());	// JRH-20141212 使用原生SQL从DB中取出权限，这样如果管理员将自己的某个权限去掉了，研发人员就可以到后台往ec2_role_businessmodel_link加条记录就可以了
//					Set<BusinessModel> bmResults = role.getBusinessModels();		// 不使用这个老方法 JRH-20141212 找到了对应的角色
					
					for (BusinessModel model : bmResults) {
						if (model.getModule() != null) { // 不保存只有application的模块
							ownBusinessModleMap.put(model.toString(), model);
						}
					}
				}
			}
			businessModels.addAll(ownBusinessModleMap.keySet());
			session.setAttribute("businessModels", businessModels);
		}
		
		// 存储登陆的用户
		session.setAttribute("loginUser", loginUser);
		// 存储登陆的用户当前登陆所使用的角色类型
		session.setAttribute("roleType", roleType);

		// -------- 显示登录的页面 ------------- //

		if (roleType.equals(RoleType.csr)) {
			// 存储当前用户与vaadin application 的对应关系,用于弹窗使用
			ShareData.userToApp.put(loginUser.getId(), app);
			// 存储用户和Session的对应关系，如果是CSR登陆，则将相应的Session管理起来
			ShareData.userToSession.put(loginUser.getId(), session);

			// 将分机存入Session
			session.setAttribute("exten", exten);

			mainWindow.setContent(new CsrMainView(this));
		} else if (roleType.equals(RoleType.manager)) {
			mainWindow.removeAllComponents();
			mainWindow.setContent(new MgrMain(this));
		}
	}

	/**
	 *  jrh 2013-10-23
	 * 保存用户输入的信息到Cookie中
	 */
	private void saveCookies() {
		Cookie cookie1 = new Cookie("username", username);
        cookie1.setPath("/ec2");
        cookie1.setMaxAge(3600*24*30); // One month
        Ec2Application.response.addCookie(cookie1);

        if(roleType.equals(RoleType.csr)) {
        	Cookie cookie2 = new Cookie("exten", exten);
        	cookie2.setPath("/ec2");
        	cookie2.setMaxAge(3600*24*30); // One month
        	Ec2Application.response.addCookie(cookie2);
        }
        System.out.println("写cookie username=====:"+username+"  ; exten=====:"+exten);
	}
	
	/**
	 * 显示冲突处理界面
	 * @param existedUsers 与当前用户冲突的用户
	 */
	private void showConflictManageWindow(List<User> existedUsers) {
		StringBuffer contents = new StringBuffer();
		String placeholder = "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";
		for(User user : existedUsers) {
			String exten = ShareData.userToExten.get(user.getId());
			String content = "<B>"+placeholder+"用户名：</B>" +user.getUsername()+ ",<B> 工号：</B>" +user.getEmpNo()+ ",<B> 正使用分机：</B>" +exten+"<br/>";
			contents.append(content);
		}
		conflictNotice.setReadOnly(false);
		conflictNotice.setValue(contents.toString());
		conflictNotice.setReadOnly(true);

		conflictManageWindow.center();
		this.getApplication().getMainWindow().removeWindow(conflictManageWindow);
		this.getApplication().getMainWindow().addWindow(conflictManageWindow);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == login) {
			try {
				handleLoginClickEvent();
			} catch (Exception e) {
				warningLabel.setValue("<font color='red'>登陆出现错误，请重试！</font>");
				warningLabel.setVisible(true);
				logger.error("用户登陆出错！", e);
			}
		} else if(source == confirm) {
			String ip = webBrowser.getAddress(); 	// 当前用户使用的 ip 地址
			try {
				this.getApplication().getMainWindow().removeWindow(conflictManageWindow);
				loginUser = userLoginService.login(username, password, exten, ip, roleType);
				handleAfterLogin(webBrowser, exten);
			} catch (Exception e) {
				logger.warn("CSR登陆失败！ 失败原因：" + e.getMessage(), e);
				warningLabel.setValue("<font color='red'>登陆失败，请重试！</font>");
				warningLabel.setVisible(true);
				return;
			}
		} else if(source == cancel) {
			this.getApplication().getMainWindow().removeWindow(conflictManageWindow);
		} else if (source == forget) {
			this.getApplication().getMainWindow().showNotification("请咨询管理员!", Notification.TYPE_WARNING_MESSAGE);
		}
	}
	
	public void loginClick() {
		login.click();
	}
	
	public TextField getUsernameTextField() {
		return usernameTextField;
	}

	public PasswordField getPasswordTextField() {
		return passwordTextField;
	}

	public TextField getExtenNoField() {
		return extenNoField;
	}

	/**
	 * 要切换至的角色类型，如要切换至管理员界面，就传值RoleType.manager
	 * @param roleType
	 */
	public void switchAccountView(RoleType roleType) {
		app.getMainWindow().removeAllComponents();
		app.getMainWindow().addComponent(ec2LoginLayout);
		TabSheet tabSheet = ec2LoginLayout.getLoginTabSheet();
		if(roleType.equals(RoleType.manager)) {
			tabSheet.setSelectedTab(1);
		} else if(roleType.equals(RoleType.csr)) {
			tabSheet.setSelectedTab(0);
		}
		
		LoginLayout loginLayout = (LoginLayout) tabSheet.getSelectedTab();
		loginLayout.getUsernameTextField().setValue(loginUser.getUsername());
		loginLayout.getPasswordTextField().setValue(loginUser.getPassword());
		if(roleType.equals(RoleType.csr)) {
			loginLayout.getExtenNoField().setValue(exten);
		}
		loginLayout.loginClick();
	}


}
