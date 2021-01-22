package com.jiangyifen.ec2.test;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.response.ManagerResponse;

public class AMITest  {


	private static String ip = "192.168.1.200";
	private static String username = "manager";
	private static String password = "123456";

	private static ManagerConnectionFactory factory;
	private static ManagerConnection managerConnection;


	
	public static void main(String[] args){
		try {
			
			factory = new ManagerConnectionFactory(ip, username, password);
			managerConnection = factory.createManagerConnection();
			
			managerConnection.addEventListener(new MyEventListener());
			

			// 呼叫Action
			OriginateAction originateAction = new OriginateAction();
			originateAction.setChannel("SIP/810006@800001");
			originateAction.setCallerId("800001");
			originateAction.setExten("810006@800001");
			originateAction.setContext("autodial");
			originateAction.setPriority(1);
			originateAction.setAsync(true);
			
			
//			QueuePauseAction action  = new QueuePauseAction();
//			action.setInterface("SIP/8500");
//			action.setPaused(true);
//			action.setActionId("QueuePauseActionId");
//			action.setQueue("default");
			
			managerConnection.login();
			
			ManagerResponse response1 = managerConnection.sendAction(originateAction, 2000);
			System.out.println("+++ "+response1);
			
			managerConnection.logoff();
//			
			
			managerConnection.login();
			
			
			StatusAction statusAction = new StatusAction();
//			statusAction.setActionId("StatusActionId");
			ManagerResponse response2 = managerConnection.sendAction(statusAction, 2000);
			System.out.println("response2  -->  " + response2);
			
			managerConnection.logoff();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
