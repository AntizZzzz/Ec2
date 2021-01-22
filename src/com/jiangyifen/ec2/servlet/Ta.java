package com.jiangyifen.ec2.servlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Ta {
	public static void main(String[] args) {
		myGet();
	}
	public static void myGet() {
		try {
			//建立指定url的Http连接
			URL url = new URL("http://etrademarkets.xicp.net:8080/ec2/trade/addresource");
			StringBuilder params= new StringBuilder();
			params.append("username="+URLEncoder.encode("测试2", "UTF-8"));
			params.append("&mail="+URLEncoder.encode("abc@163.com", "UTF-8"));
			params.append("&phonenum="+URLEncoder.encode("15188888888", "UTF-8"));
			params.append("&gender="+URLEncoder.encode("男", "UTF-8"));
			params.append("&location="+URLEncoder.encode("上海市", "UTF-8"));
			params.append("&trader="+URLEncoder.encode("国泰君安", "UTF-8"));
System.out.println(params);
//http://192.168.1.203:8080/ec2/trade/addresource?username=%E6%B5%8B%E8%AF%952&mail=abc%40163.com&phonenum=15214563213&gender=%E7%94%B7&location=%E4%B8%8A%E6%B5%B7%E5%B8%82
			HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setDoOutput( true);
			
			//取得输出流并写入请求参数
			OutputStream outputStream=httpURLConnection.getOutputStream();
			outputStream.write(params.toString().getBytes( "UTF-8"));
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
			String result=stringBuffer.toString();
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Add  failed");
		}
	}

}
