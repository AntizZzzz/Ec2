package com.jiangyifen.ec2.test;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.action.OriginateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.utils.Config;

public class ActionTest {
	private static final Logger logger = LoggerFactory.getLogger(ActionTest.class);

	public static void main(String[] args) {
		//新建连接并发送Action
		String ip= Config.props.getProperty(Config.AMI_IP);
		String username=Config.props.getProperty(Config.AMI_USERNAME);
		String password=Config.props.getProperty(Config.AMI_PWD);
		ManagerConnectionFactory factory = new ManagerConnectionFactory(ip, username, password);
		ManagerConnection managerConnection = factory.createManagerConnection();
		try {
			managerConnection.login();
			
			String todialPhoneNumber = "02160509666";
//			String todialPhoneNumber = "13816760398";
			String outlineNumber = "88860847041";
			
			// 呼叫Action
			OriginateAction originateAction = new OriginateAction();
			originateAction.setChannel("SIP/"+todialPhoneNumber+"@"+outlineNumber);
			originateAction.setContext("incoming");
			originateAction.setExten(todialPhoneNumber);
			originateAction.setPriority(1);
			originateAction.setCallerId(todialPhoneNumber+" <"+todialPhoneNumber+">");
			originateAction.setAsync(true);
			
			managerConnection.sendAction(originateAction);
			
//			CommandAction commandAction = new CommandAction("SendDTMF(1)");
			
			managerConnection.logoff();
		} catch (Exception e) {
			logger.error("chb: 挂断电话出现异常,请查看连接状态是否正常", e);
		}finally{
			try {
				String state = managerConnection.getState().name();
				if("CONNECTED".equals(state) || "RECONNECTING".equals(state)) {
					managerConnection.logoff();
				}
			} catch (IllegalStateException e) {
				logger.warn("chb: asterisk manager登出失败");
			}
		}
	}
}
