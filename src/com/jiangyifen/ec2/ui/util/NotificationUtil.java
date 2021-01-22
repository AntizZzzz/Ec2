package com.jiangyifen.ec2.ui.util;

import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author chenhb
 *
 */
public class NotificationUtil {
	/**
	 * 在屏幕中间显示警告Notification
	 * @param component
	 * @param notificationContent
	 */
	public static void showWarningNotification(Component component,String notificationContent){
		Notification notification=new Notification(notificationContent, Notification.TYPE_HUMANIZED_MESSAGE);
		notification.setDelayMsec(1500);
		/*notification.setPosition(Notification.POSITION_CENTERED_TOP);*/
		notification.setPosition(Notification.POSITION_CENTERED);
		
		//获取Session中的Window
		Window window=null;
		try {
			window=SpringContextHolder.getMainWindow();
		} catch (Exception e) {
			System.out.println("chenhb: WarningNotification Exception -> "+e.getMessage());
		}
		if(window!=null){
			window.showNotification(notification);
		}else{
			component.getWindow().showNotification(notification);
		}
	}

	/**
	 * 在屏幕中间显示警告Notification
	 * @param component
	 * @param notificationContent
	 */
	public static void showWarningNotification(Window window,String notificationContent){
		Notification notification=new Notification(notificationContent, Notification.TYPE_HUMANIZED_MESSAGE);
		notification.setDelayMsec(1500);
		/*notification.setPosition(Notification.POSITION_CENTERED_TOP);*/
		notification.setPosition(Notification.POSITION_CENTERED);
		
		//Window
		if(window!=null){
			window.showNotification(notification);
		}else{
			System.out.println("chenhb: Window is null");
		}
	}

	/**
	 * 直接传入application
	 * 
	 * @param application
	 * @param notificationContent
	 */
	public static void showWarningNotification(Application application,String notificationContent){
		Notification notification=new Notification(notificationContent, Notification.TYPE_HUMANIZED_MESSAGE);
		/*notification.setPosition(Notification.POSITION_CENTERED_TOP);*/
		notification.setPosition(Notification.POSITION_CENTERED);
		
		//获取Session中的Window
		application.getMainWindow().showNotification(notification);
	}
	
}
