package com.jiangyifen.ec2.test;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AsyncAgiServer;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.asteriskjava.manager.DefaultManagerConnection;
import org.asteriskjava.manager.ManagerConnection;

public class SampleScript extends BaseAgiScript
{
    public void service(AgiRequest request, AgiChannel channel) throws AgiException
    {
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("----------------------------------");
//        channel.streamFile("tt-monkeys");
        System.out.println(request);
        System.out.println("----------------------------------");
    }
 
    public static void main(String[] args) throws Exception
    {
        ManagerConnection connection;
        AsyncAgiServer agiServer;
 
        connection = new DefaultManagerConnection("192.168.100.41", "manager", "123456");
        agiServer = new AsyncAgiServer(new SampleScript());
        connection.addEventListener(agiServer);
        connection.login();
 
        while (true)
        {
            Thread.sleep(1000L);
        }
    }
}