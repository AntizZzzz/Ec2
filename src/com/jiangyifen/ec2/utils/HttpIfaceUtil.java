package com.jiangyifen.ec2.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.junit_test.iface.http_common.HttpIfaceDebugUtil;

public class HttpIfaceUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpIfaceDebugUtil.class);

	// 链接访问超时时长设置，毫秒 ms
	private static int CONNECT_TIMEOUT = 10000;
	// 访问读取超时时长设置，毫秒 ms
	private static int READ_TIMEOUT = 10000;
	
	/*********************** 使用Main 方法来直接调试 *******************/
	
	public static void main(String[] args) throws Exception {

		// -----------------------Do Post---------------------//
//		String postUri = "http://61.152.162.4/WebForm/AcceptSearch.aspx?";
//		String postParams = "mobile=13888888888";
//		doPostRequest(postUri, postParams);
//		System.out.println("执行成功");
		
		// http://192.168.1.161:8080/ec2/http/common/push/importCustomerResource?
		// json={"addresses":[{"mobile":"","name":"","postCode":"","street":"上海马戏城 1 号口"}],"birthday":"1992-02-07 12:00:00","customerResourceBatch":"批次测试","customer_source":"网络来源","descs":[{"createDate":"1970-01-01 00:00:00","key":"邮箱","lastUpdateDate":"1970-01-01 00:00:00","value":"abatchfood@yahoo.com"},{"createDate":"1970-01-01 00:00:00","key":"爱好","lastUpdateDate":"1970-01-01 00:00:00","value":"play basketball"}],"domainId":"1","name":"王萌萌","note":"预留字段","sex":"男","telephones":[{"number":"13713215827"},{"number":"13923175802"}]}
		
		/*Long number = (long) (Math.random() * 6000000000L + 13000000000L);
		
		for(int i = 0; i < 1000000; i++) {
			number = (long) (Math.random() * 6000000000L + 13000000000L);
			System.out.println(number);
			String json = "{\"addresses\":[{\"street\":\"上海马戏城 1 号口\"}],\"customerResourceBatch\":\"20150629测试\",\"customer_source\":\"网络来源\",\"descs\":[{\"key\":\"爱好\",\"value\":\"play basketball\"}],\"domainId\":\"1\",\"name\":\""+CreateRandomField.getRandomEnglishName()+"\",\"note\":\"预留字段\",\"sex\":\"男\",\"telephones\":[{\"number\":\""+number+"\"}]}";
			
			HttpIfaceUtil.doPostRequest("http://192.168.1.161:8080/ec2/http/common/push/importCustomerResource", "json="+json);
		}*/
		
		HttpIfaceUtil.doPostRequest(ExternalInterface.INCOMING_ELASTIC_SCREEN_INTERFACE_URL, "mobile=131111");
		
		// -----------------------Do Get---------------------//
		/*StringBuffer urlBf = new StringBuffer();
		urlBf.append("http://192.168.2.160:8080/ec2/http/common/loginBind?");
		urlBf.append("accessId=2014040341134844HTDM2&accessKey=F86E72F43688F5FBDE3607A0AB7B376C&username=1001&exten=800002");

		doGetRequest(urlBf.toString());

		doGetOpenStream(urlBf.toString());*/
	}

	// ==================================================================//
	/**                            发送请求的方式                                                          **/
	// =================================================================//

	/**
	 * @Description 描述：跟第三方系统对接时，发送 POST 请求，获取返回的结果信息
	 * 
	 * @author JRH
	 * @date 2014年6月30日 下午6:12:15
	 * @param uri 		请求的URI(即：URL中不包括请求参数的部分)
	 * @param params 	请求参数
	 */
	public static void doPostRequest(String uri, String params) {
		try {
			URL url = new URL(uri);
			
			params += params + "&_"+(new Date()).getTime();
			
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
			httpURLConnection.setRequestProperty("Accept", "[*]/[*]");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT); 	// 正常情况下，几毫秒就能连上，如果连不上，可能会造成阻塞，所以设置最大连接时长
			httpURLConnection.setReadTimeout(READ_TIMEOUT); 		// 同上
			httpURLConnection.setDoOutput(true);

			// 取得输出流并写入请求参数
			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(params.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();
			
			// 读取返回的内容到StringBuffer
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
			StringBuffer stringBuffer = new StringBuffer();
			int ch;
			while ((ch = bufferedReader.read()) > -1) {
				stringBuffer.append((char) ch);
			}
			bufferedReader.close();

			/***************** 获取到返回信息后，再做下一步封装等业务处理 *****************/
			// 将StringBuffer中的内容输出
			String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
			jsonResult = jsonResult.replace("，", ",");

			// 解析发送结果
			System.out.println();
			System.out.println("====================================================");
			System.out.println("\t doPostRequest");
			System.out.println(uri + "?" + params);
			System.out.println();
			System.out.println(jsonResult);
		} catch (Exception e) {
			logger.error("JRH 发送 POST 请求失败-->" + e.getMessage(), e);
		}
	}

	/**
	 * @Description 描述：跟第三方系统对接时，发送 GET 请求，获取返回的结果信息
	 * 
	 * @author JRH
	 * @date 2014年6月30日 下午6:09:01
	 * @param url 	请求路径
	 */
	public static void doGetRequest(String url) {
		try {
			URL requestUrl = new URL(url);

			HttpURLConnection httpURLConnection = (HttpURLConnection) requestUrl.openConnection();
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
			httpURLConnection.setRequestProperty("Accept", "[*]/[*]");
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setConnectTimeout(10000); 	// 正常情况下，几毫秒就能连上，如果连不上，可能会造成阻塞，所以设置最大连接时长
			httpURLConnection.setReadTimeout(10000);

			// 读取返回的内容到StringBuffer
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
			StringBuffer stringBuffer = new StringBuffer();
			int ch;
			while ((ch = bufferedReader.read()) > -1) {
				stringBuffer.append((char) ch);
			}
			bufferedReader.close();

			/***************** 获取到返回信息后，再做下一步封装等业务处理 *****************/
			// 将StringBuffer中的内容输出
			String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
			jsonResult = jsonResult.replace("，", ",");

			System.err.println();
			System.err.println("---------------------------------------------------------");
			System.err.println("\t doGetRequest");
			System.err.println(url);
			System.err.println();
			System.err.println(jsonResult);
		} catch (Exception e) {
			logger.error("JRH 发送 GET 请求失败-->" + e.getMessage(), e);
		}
	}

	/**
	 * @Description 描述：跟第三方系统对接时，直接通过URL.openStream(); 来访问
	 * 
	 * @author JRH
	 * @date 2014年6月30日 下午6:09:01
	 * @param url 	请求路径
	 */
	public static void doGetOpenStream(String url) {
		try {
			URL requestUrl = new URL(url);
			InputStream is = requestUrl.openStream();

			// 读取返回的内容到StringBuffer
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuffer stringBuffer = new StringBuffer();
			int ch;
			while ((ch = bufferedReader.read()) > -1) {
				stringBuffer.append((char) ch);
			}
			bufferedReader.close();

			/***************** 获取到返回信息后，再做下一步封装等业务处理 *****************/
			// 将StringBuffer中的内容输出
			String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
			jsonResult = jsonResult.replace("，", ",");

			System.out.println();
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			System.out.println("\t doGetOpenStream");
			System.out.println(url);
			System.out.println();
			System.out.println(jsonResult);
		} catch (Exception e) {
			logger.error("JRH 通过 URL.openStream() 请求失败-->" + e.getMessage(), e);
		}
	}
	
}
