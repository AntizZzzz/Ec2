package test.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class A {

	javax.transaction.Synchronization synchronization;
	org.aspectj.lang.annotation.Around around;
//	javassist.util.proxy.ProxyObject
//	javassist.util.proxy.Proxy
	
	public static void main(String[] args) {
		SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println(SDF.format(new Date()));
		System.out.println(System.currentTimeMillis());
		
		System.out.println("14186993480550000".length());
		System.out.println(SDF.format(new Date(14186993480550000L)));
	}
}
