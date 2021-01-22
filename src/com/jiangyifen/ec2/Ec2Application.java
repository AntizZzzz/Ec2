package com.jiangyifen.ec2;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.ui.Ec2LoginLayout;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutMonitor;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemInfo;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.Window.Notification;


@SuppressWarnings("serial")
public class Ec2Application extends Application implements HttpServletRequestListener {

	private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static HttpServletResponse response;
    public static HttpServletRequest request;
    
	private Window mainWindow;
	private Ec2LoginLayout ec2LoginLayout;
	
	private UserLoginService loginService;

	/** jrh 为了实现按快捷键登陆系统，在主界面上使用一个隐藏的按钮来实现 */
	private Button hiddenLogin_bt; 						// 隐藏的登录按钮
	
	@Override
	public void init() {
		this.setTheme("my-chameleon");
		this.setLogoutURL("/ec2");
		loginService = SpringContextHolder.getBean("userLoginService");

		VerticalLayout main_lo = new VerticalLayout();
		
		mainWindow = new Window(SystemInfo.getPageTitle());
		SpringContextHolder.getHttpSession().setAttribute("mainWindow", mainWindow);
		mainWindow.setContent(main_lo);
		this.setMainWindow(mainWindow);
		
		// 关闭浏览器是 退出系统，并清理内存和解除与Ip电话的绑定
		mainWindow.addListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				User loginUser = SpringContextHolder.getLoginUser();
				String exten = SpringContextHolder.getExten();
				if(loginUser != null && exten != null) {
					loginService.logout(loginUser.getId(),loginUser.getDomain().getId(),loginUser.getUsername(),loginUser.getEmpNo(),exten,0L, true);
					
					/***********************管理员处理*************************/
					//如果超时的是管理员，则对管理员的自动外呼组件进行移除
					ShareData.mgrToAutoDialout.remove(loginUser.getId());
					AutoDialoutMonitor autodialMonitor=ShareData.mgrToAutoDialoutMonitor.get(loginUser.getId());
					if(autodialMonitor!=null){
						autodialMonitor.stopThread();
					}
					ShareData.mgrToAutoDialoutMonitor.remove(loginUser.getId());
				}
				getMainWindow().getApplication().close();
			}
		});

		ec2LoginLayout = new Ec2LoginLayout();
		main_lo.addComponent(ec2LoginLayout);
		
		// 用于使用快捷键登陆		【隐藏的登录功能】
		hiddenLogin_bt = new Button(null);
		hiddenLogin_bt.addStyleName("invisible");		// 这个样式可能有点浏览器不支持
		hiddenLogin_bt.addStyleName("borderless");
		main_lo.setExpandRatio(ec2LoginLayout, 1.0f);
		main_lo.addComponent(hiddenLogin_bt);
		
		hiddenLogin_bt.setClickShortcut(KeyCode.ENTER);
		hiddenLogin_bt.addListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				ec2LoginLayout.loginByShutCut();
			}
		});
	}
	
	public static SystemMessages getSystemMessages() {
		CustomizedSystemMessages systemMessages = new CustomizedSystemMessages();
		systemMessages.setSessionExpiredCaption("您的页面已过期");
		systemMessages.setSessionExpiredMessage("请单击这里，进行重新连接");
		systemMessages.setSessionExpiredNotificationEnabled(false);
		
		systemMessages.setCommunicationErrorCaption("网络断开");
		systemMessages.setCommunicationErrorMessage("请检查网络，或与管理员联系");
		systemMessages.setCommunicationErrorNotificationEnabled(false);
		
		systemMessages.setInternalErrorCaption("服务器内部异常");
		systemMessages.setInternalErrorMessage("请单击这里，进行重新连接");
		systemMessages.setInternalErrorNotificationEnabled(false);
		
		systemMessages.setOutOfSyncCaption("与服务器同步失败");
		systemMessages.setOutOfSyncMessage("请单击这里，进行重新连接");
		systemMessages.setOutOfSyncNotificationEnabled(false);
		
		systemMessages.setCookiesDisabledCaption("Cookies不可用");
		systemMessages.setCookiesDisabledMessage("请单击这里，进行重新连接");
		return systemMessages;
	}

	@Override
	public void onRequestStart(HttpServletRequest request,
			HttpServletResponse response) {
		Ec2Application.request = request;
		Ec2Application.response = response;
		
		//如果baseurl不存在则处理
		Object baseurlObject = SpringContextHolder.getHttpSession().getAttribute("baseurl");
		String baseurl =baseurlObject==null?null:baseurlObject.toString();
		if(StringUtils.isEmpty(baseurl)){
			String ip=null;
			//按ip访问处理
			String url=request.getRequestURL().toString();
			url=url.substring(url.indexOf("//")+2);
			try {
				ip=url.substring(0,url.indexOf(":"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			//不知域名访问有无：
			if(StringUtils.isEmpty(ip)){
				try {
					ip=url.substring(0,url.indexOf("/"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//如果ip截取成功，则设置attribute
			if(!StringUtils.isBlank(ip)){
				SpringContextHolder.getHttpSession().setAttribute("baseurl", "http://"+ip+":${port}/monitor/");
			}
		}
	}

	@Override
	public void onRequestEnd(HttpServletRequest request,
			HttpServletResponse response) {
		// todo something		
	}

	/**
	 * jrh 2014-01-23
	 * 捕捉组件异常, 并输出到指定的文件中 
	 * 这样为了给用户更好的体验，避免在用户操作导致系统出现异常时，界面出现红色感叹号
	 */
	@Override
	public void terminalError(Terminal.ErrorEvent event) {
		 // super.terminalError(event); not show this error on component
		// /D:/EclipseWorkspace/emm/WebContent/WEB-INF/classes/
		
		//输出到标准输出流的日志
		event.getThrowable().printStackTrace();
		
		//存储异常信息
		String classPath=this.getClass().getResource("/").getFile().toString();
		try {
			File terminalErrorFile=new File(classPath+"ec2_terminalerror.log");

			if(!terminalErrorFile.exists()){
				terminalErrorFile.createNewFile();
			}

			//追加到日志文件
			FileWriter fileWriter = new FileWriter(terminalErrorFile,true);  
			PrintWriter printWriter=new PrintWriter(fileWriter);
			
			printWriter.print(SDF.format(new Date())+"============================================================\r\n");

			if(printWriter!=null){
				event.getThrowable().printStackTrace(printWriter);
			}
			
			printWriter.flush();
			fileWriter.flush();
			printWriter.close();
			fileWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//界面给用户提示信息
	    if (getMainWindow() != null) {
	    	Notification notification=new Notification("对不起，操作失败！", Notification.TYPE_TRAY_NOTIFICATION);
	    	notification.setPosition(Notification.POSITION_BOTTOM_RIGHT);
	        getMainWindow().showNotification(notification);
	    }
	}
	
}

