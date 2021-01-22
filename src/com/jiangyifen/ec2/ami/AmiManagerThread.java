package com.jiangyifen.ec2.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.action.AbstractManagerAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.utils.Config;

/**
 * 一个与Asterisk长期保持的连接，用来发送所有的Action请求
 * 
 * @author chb
 */
public class AmiManagerThread extends Thread {
	private static ManagerConnection managerConnection;
	private static Logger logger = LoggerFactory
			.getLogger(AmiManagerThread.class);

	public AmiManagerThread() {
		this.setName("AmiManagerThread");
		// 设置为后台线程，Spring销毁后被销毁
		this.setDaemon(true);
		this.start();
	}

	/**
	 * 线程运行方法
	 */
	public void run() {
		// 我们只需初始化第一次连接，之后如果断线Asterisk会自动重连
		while (true) {
			try {
				String ip = Config.props.getProperty(Config.AMI_IP);
				String username = Config.props.getProperty(Config.AMI_USERNAME);
				String password = Config.props.getProperty(Config.AMI_PWD);
				ManagerConnectionFactory factory = new ManagerConnectionFactory(ip,username, password);
				managerConnection = factory.createManagerConnection();
				managerConnection.login();

				logger.info("chb: AmiManagerThread的线程连接成功！");
				break;
			} catch (Exception e) {
				logger.error("chb: AmiManagerThread的线程连接异常，5秒后将尝试重连...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					//Do nothing
				}
			}
		}

		//线程永久睡眠
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e1) {
			//do nothing
		}

	}

	/**
	 * 发送一个Action到Asterisk
	 * 
	 * @param originateAction
	 */
	public static Boolean sendAction(AbstractManagerAction action) {
		if(managerConnection.getState()!=ManagerConnectionState.CONNECTED)
			return false;
		
		try {
			managerConnection.sendAction(action, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 发送一个Action到Asterisk
	 * @param originateAction
	 */
	public static ManagerResponse sendResponseAction(AbstractManagerAction action) {
		if(managerConnection.getState()!=ManagerConnectionState.CONNECTED){
			logger.error("chb: AmiManagerThread中sendResponseAction方法 发送"+action.getClass()+"失败！没有处于连接状态！");
			return null;
		}
		
		try {
			ManagerResponse response = managerConnection.sendAction(action, 5000);
			return response;
		} catch (Exception e) {
			logger.error("chb: AmiManagerThread中sendResponseAction方法 发送"+action.getClass()+"失败！", e);
			return null;
		}
	}

}
