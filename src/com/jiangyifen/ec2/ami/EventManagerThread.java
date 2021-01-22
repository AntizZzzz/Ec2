package com.jiangyifen.ec2.ami;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.utils.Config;

/**
 * 一个与Asterisk长期保持的连接，只用来监听Asterisk的Event
 */
public class EventManagerThread extends Thread {
	private static ManagerConnection managerConnection;
	private static Logger logger = LoggerFactory.getLogger(EventManagerThread.class);
	
	public EventManagerThread() {
		this.setName("EventManagerThread");
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
				managerConnection.addEventListener(new BridgeEventListener());
				managerConnection.addEventListener(new CdrEventListener());
				managerConnection.addEventListener(new QueueCallerAbandonEventListener());
				managerConnection.addEventListener(new HangupEventListener());
				managerConnection.addEventListener(new NewStateEventListener());
				managerConnection.addEventListener(new QueueMemberPausedEventListener());
				managerConnection.addEventListener(new NewChannelEventListener());
				managerConnection.addEventListener(new MonitorStopEventListener());
				managerConnection.addEventListener(new MeetMeJoinEventListener());
				managerConnection.addEventListener(new MeetMeLeaveEventListener());
//				managerConnection.addEventListener(new JoinEventListener());
				managerConnection.login();
				
				logger.info("chb: EventManagerThread的线程连接成功！");
				break;
			} catch (Exception e) {
				logger.error("chb: EventManagerThread的线程连接异常，5秒后将尝试重连...");
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
}
