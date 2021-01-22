package com.jiangyifen.ec2.servlet.woke.other;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 由Agi调用，对其它用户不影响
 * @author chb
 *
 */
public class WokeRouter{
	private static final Logger logger = LoggerFactory.getLogger(WokeRouter.class);
	
	public static String route(String phonenum,String outlinenum) {
		
		//通过调用URL进行推送
		try {
			//建立指定url的Http连接
			URL url = new URL("http://192.168.0.1"); //沃客装修专用，所以写死
			String params= "phonenum="+phonenum+"&outlinenum="+outlinenum;
logger.info("woke WokeRouter--> phonenum="+phonenum+"&outlinenum="+outlinenum);
			HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
			httpURLConnection.setReadTimeout(2);
			httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			httpURLConnection.setRequestProperty("Accept","[*]/[*]");
			httpURLConnection.setRequestMethod("POST");
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
			String pushReturn=stringBuffer.toString();
logger.info("chb: woke router return '"+pushReturn+"'");
			return pushReturn;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

} 
