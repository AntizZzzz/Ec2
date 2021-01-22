package com.jiangyifen.ec2.test;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.event.HangupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.utils.Config;

/**
 * 测试
 */
public class Chen_StatusActionTest extends Thread {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private	ManagerConnection managerConnection;
	
	public Chen_StatusActionTest() {
		this.setName("EventManagerThread");
//		this.setDaemon(true);
		this.start();
	}
	
	public static void main(String[] args) {
		new Chen_StatusActionTest();
	}

	/**
	 * 线程运行方法
	 */
	public void run(){
		//我们只需初始化第一次连接，之后如果断线Asterisk会自动重连
		while (true) {
			try {
				initConnection();
				managerConnection.addEventListener(new AbstractManagerEventListener() {

					/**
					 * 监听StatusAction触发的事件
					 */
					@Override
					protected void handleEvent(HangupEvent event) {
						System.out.println("---------------------------------------------");
						System.out.println("event:"+event.getChannel());
						System.out.println("---------------------------------------------");
					}
					
				});
				
//				Local/999_1356309867602_88861855716
				
				logger.info("chb: 监听Asterisk事件的线程连接成功！");
				break;
			} catch (Exception e) {
				logger.error("chb: 监听Asterisk事件的线程连接异常，5秒后将尝试重连...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					logger.error("chb: 监听Asterisk事件的线程异常，程序不能继续运行", e1);
					throw new RuntimeException("chb: 监听Asterisk事件的线程异常，程序不能继续运行");
				}
			}
		}

		//让线程长时间睡眠
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			logger.error("chb: 监听Asterisk事件的线程异常，程序不能继续运行", e);
			throw new RuntimeException("chb: 监听Asterisk事件的线程异常，程序不能继续运行");
		}
	}
	
	/**
	 * 初始化连接
	 * @throws Exception 连接失败，抛出异常
	 */
	public void initConnection() throws Exception{
		String ip= Config.props.getProperty(Config.AMI_IP);
		String username=Config.props.getProperty(Config.AMI_USERNAME);
		String password=Config.props.getProperty(Config.AMI_PWD);
		ManagerConnectionFactory factory = new ManagerConnectionFactory(ip, username, password);
		managerConnection = factory.createManagerConnection();
		managerConnection.login();
	}
	
	/**
	 * Spring销毁时调用
	 */
	@Override
	public void destroy() {
		try {
			managerConnection.logoff();
		} catch (IllegalStateException e) {
			logger.warn("chb: asterisk manager登出失败");
		}
		this.destroy();
	}
}
