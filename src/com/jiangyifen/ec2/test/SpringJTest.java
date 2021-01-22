package com.jiangyifen.ec2.test;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringJTest {

	public static void main(String[] args) {
		//D:\\workspace\\ec2\\WebContent\\WEB-INF\\applicationContextConfig.xml
		@SuppressWarnings("unused")
		ApplicationContext ctx = new FileSystemXmlApplicationContext("D:\\workspace\\ec2\\WebContent\\WEB-INF\\applicationContextConfig.xml");
		

	}
}
