package com.jiangyifen.ec2.ui.csr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class Test {

	public static void main(String[] args) {
//
//		StringBuffer phoneNosSB = new StringBuffer();
//		phoneNosSB.append("1212,212,454,4544433，12，");
//		String phoneNosStr = phoneNosSB.toString();
//		System.out.println();
//		phoneNosStr = phoneNosStr.substring(0, phoneNosStr.length() - 1);
//		ArrayList<String> numbers = new ArrayList<String>();
//		for(String s1 :	phoneNosStr.split(",")) {
//			if(s1.contains("，")) {
//				System.out.println("-->"+s1);
//				for(String s2 : s1.split("，")) {
//					numbers.add(s2);
//				}
//			} else {
//				numbers.add(s1);
//			}
//		}
//		System.out.println(phoneNosStr);
//		System.out.println(numbers);
//		try {
//			URL url = new URL("http://112.4.78.52:8088/DX/messageSend.action?username=shht001&password=123456789&content=hellolaojiang&receivePhone=15861692130");
//			url.openStream();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
        
        
        try {
        	Long startTime = System. currentTimeMillis();
			//建立指定url的Http连接
		      URL url = new URL( "http://112.4.78.52:8088//DX/messageSend.action?");
		      String params= "username=shht001&password=123456789&content=蒋荣辉蒋荣辉蒋荣jfksdjkjjkljkl辉&receivePhone=13816760398\r\n15861692130";
		      HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
		      httpURLConnection.setRequestMethod( "POST");
		      httpURLConnection.setDoOutput( true);
					//取得输出流并写入请求参数
		      OutputStream outputStream=httpURLConnection.getOutputStream();
		      outputStream.write(params.getBytes( "UTF-8"));
		      outputStream.flush();
		      outputStream.close();
					//读取返回的内容到StringBuffer
		      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
					         httpURLConnection.getInputStream(), "UTF-8"));
		      StringBuffer stringBuffer = new StringBuffer();
					int ch;
					while ((ch = bufferedReader.read()) > -1) {
					     stringBuffer.append((char) ch);
		      }
		      bufferedReader.close();
					//将StringBuffer中的内容输出
		      System. out.println( stringBuffer.toString());
		      
		      
		      Long endTime = System. currentTimeMillis();
		      System. out.println( "发送 短信 耗时：" + (endTime - startTime));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
