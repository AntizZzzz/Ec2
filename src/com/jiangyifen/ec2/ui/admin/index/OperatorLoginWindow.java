package com.jiangyifen.ec2.ui.admin.index;

import java.util.Calendar;

import com.jiangyifen.ec2.ui.admin.OperatorMainWindow;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 操作员登录弹出窗体
 * 
 * @author JHT
 */
public class OperatorLoginWindow extends Window implements Button.ClickListener{
	private static final long serialVersionUID = -8476340901195112018L;
	
	private PasswordField loginpwdField;										// 登录密码
	private Button loginBtn;													// 登录按钮
	
	public OperatorLoginWindow(){
		this.center();															// 窗体居中显示
		this.setModal(true);													// 弹出该窗体时,显示遮罩层效果
		this.setResizable(false);												// 设置该窗体不可修改大小
		
		VerticalLayout windowContent = new VerticalLayout();					// 添加Window内最大的Layout
		windowContent.setSizeUndefined();
		windowContent.setMargin(true, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		windowContent.addComponent(buildTextFieldLayout());
		
		this.addAction(new ShortcutListener("", KeyCode.ENTER, null) {			// 为登录窗体添加一个回车按钮快捷提交方式
			private static final long serialVersionUID = 6298453649725952080L;
			@Override
			public void handleAction(Object sender, Object target) {
				// target.toString(); 这个在这里是获取这个文本框里面的值
				loginBtn.click();
			}
		});
		
		this.addAction(new ShortcutListener("", KeyCode.ESCAPE, null) {			// 为登录窗体添加一个ESC退出快捷方式
			private static final long serialVersionUID = 3961146292274766635L;
			@Override
			public void handleAction(Object sender, Object target) {
				closeWindow();
			}
		});
	}
	
	// 文本输出域
	private HorizontalLayout buildTextFieldLayout(){
		HorizontalLayout fieldsLayout = new HorizontalLayout();
		loginpwdField = new PasswordField();
		loginpwdField.focus();													// 弹出窗体后, 让此输入框获取到焦点
		fieldsLayout.addComponent(loginpwdField);
		fieldsLayout.addComponent(new Label("&nbsp;&nbsp;", Label.CONTENT_XHTML));
		fieldsLayout.addComponent(buildButtonsLayout());
		return fieldsLayout;
	}
	
	// 按钮输出区域
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		loginBtn = new Button("提交");											// 登录按钮
		loginBtn.setStyleName("default");
		loginBtn.addListener(this);
		buttonsLayout.addComponent(loginBtn);
		
		return buttonsLayout;
	}
	
	// 关闭这个窗体
	private void closeWindow(){
		this.getParent().removeWindow(this);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == loginBtn){
			if(executeLogin()){
				this.getApplication().getMainWindow().addWindow(new OperatorMainWindow());
				closeWindow();
			} else{
				this.showNotification("输入有误!", Notification.TYPE_WARNING_MESSAGE);
				loginpwdField.setValue("");										// 清空文本框的值
				loginpwdField.focus();											// 让此文本框获取到焦点
			}
		} 
	}
	
	//调用执行登录操作
	private boolean executeLogin() {
		try {
			String loginpwd = "";
			Object obj2 = loginpwdField.getValue();
			if (obj2 != null) { loginpwd = obj2.toString(); }					// 获取登录密码
			String getPwdString = getLoginPwd();								// 获取自定义的密码
			if (loginpwd.equals(getPwdString)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	// 设置登录密码
	private String getLoginPwd(){
		StringBuilder loginPwdBuilder = new StringBuilder();
		Calendar dateCalendar = Calendar.getInstance();
		int day = dateCalendar.get(Calendar.DATE);
		int hour = dateCalendar.get(Calendar.HOUR_OF_DAY);
		
		if(day < 10) {
			loginPwdBuilder.append("0"+day);
		} else {
			loginPwdBuilder.append(day);
		}
		
		if(hour<10){
			loginPwdBuilder.append("0"+hour);
		} else {
			loginPwdBuilder.append(hour);
		}
//		System.out.println(loginPwdBuilder.toString());
/*		StringBuilder loginPwdBuilder = new StringBuilder("ht");
		Calendar dateCalendar = Calendar.getInstance();
		int year = dateCalendar.get(Calendar.YEAR);
		loginPwdBuilder.append(year);
		int month = dateCalendar.get(Calendar.MONTH)+1;
		if((month+"").toString().length() == 1){
			loginPwdBuilder.append("0"+month);
		} else {
			loginPwdBuilder.append(month);
		}
		int day = dateCalendar.get(Calendar.DATE)+1;
		if((day+"").toString().length() == 1){
			loginPwdBuilder.append("0"+day);
		} else {
			loginPwdBuilder.append(day);
		}*/
		return loginPwdBuilder.toString();
	}
	
	public static void main(String[] args) {
		OperatorLoginWindow oper = new OperatorLoginWindow();
		System.out.println(oper.getLoginPwd());
	}
	
}
