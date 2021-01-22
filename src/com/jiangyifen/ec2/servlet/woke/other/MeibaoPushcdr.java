package com.jiangyifen.ec2.servlet.woke.other;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 美宝之家推送话单
 * @author chb
 *
 */
public class MeibaoPushcdr{
	private static final Logger logger = LoggerFactory.getLogger(MeibaoPushcdr.class);
	
//	public static void main(String[] args) {
//		Map<String,String> paramterMap=new HashMap<String, String>();
//		paramterMap.put("src", "8001");
//		paramterMap.put("dst", "150");
//		paramterMap.put("other", "oth");
//		pushCdr(true, paramterMap);
//	}
	public static Boolean pushCdr(Boolean ispush,Map<String,String> paramterMap) {
		if(!ispush){
			//不推送
			return false;
		}
		
		
		//通过调用URL进行推送
		try {
			//建立指定url的Http连接
			URL url = new URL("http://192.168.1.131/mbcrm/Home/Call/receiveCallRecord/"); //美宝之家专用，所以写死
			StringBuilder paramsBuilder= new StringBuilder();
			for(String key:paramterMap.keySet()){
				if(paramsBuilder.length()==0){
					paramsBuilder.append(key+"="+paramterMap.get(key));
				}else{
					paramsBuilder.append("&"+key+"="+paramterMap.get(key));
				}
			}
logger.info("paramsBuilder ------>"+paramsBuilder.toString());
logger.info("meibao params     --> "+paramsBuilder.toString());
			HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
			httpURLConnection.setReadTimeout(2);
			httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			httpURLConnection.setRequestProperty("Accept","[*]/[*]");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setDoOutput( true);
			
			//取得输出流并写入请求参数
			OutputStream outputStream=httpURLConnection.getOutputStream();
			outputStream.write(paramsBuilder.toString().getBytes( "UTF-8"));
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
logger.info("chb: Push return '"+pushReturn+"'");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		//返回成功消息
		return true;
	}

} 
