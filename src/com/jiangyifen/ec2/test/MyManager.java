package com.jiangyifen.ec2.test;

import java.io.IOException;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;


public class MyManager extends Thread {


	private static String ip = "192.168.1.241";
	private static String username = "manager";
	private static String password = "123456";

	private static ManagerConnectionFactory factory;
	private static ManagerConnection managerConnection;

	public MyManager() throws IOException {

		try {
			factory = new ManagerConnectionFactory(ip, username, password);
			managerConnection = factory.createManagerConnection();
			managerConnection.addEventListener(new MyEventListener());
			managerConnection.login();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.setDaemon(false);
		this.start();
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		try {
			new MyManager();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
