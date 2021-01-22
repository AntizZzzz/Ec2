package test.junit_test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 描述：用于测试跟第三方系统对接时，调用第三方的Http方式的接口，或者开发HTTP接口时用来调试的工具
 * 
 *      解析调取接口后的返回结果
 *
 * @author  JRH
 * @date    2014年8月8日 下午5:52:14
 */
public class CopyOfHttpIfaceDebugUtil {

	private static Logger logger = LoggerFactory.getLogger(CopyOfHttpIfaceDebugUtil.class);
	
	public static String BASE_URL="http://192.168.2.160:8080/ec2/http/common/";
	
	private static String accessId = "2014040341134844HTDM1";
	private static String accessKey = "F86E72F43688F5FBDE3607A0AB7B376C";

	/***********************  使用 Junit 来做单元测试  ******************/
	@Test
	public void loginBind() {	// 登录用户与分机绑定
		doPostRequest(BASE_URL+"loginBind", "accessId="+accessId+"&accessKey="+accessKey+"&username=1001&exten=800002");
	}

	@Test
	public void logoutUnbind() { // 退出，用户与分机解绑
		doPostRequest(BASE_URL+"logoutUnbind", "accessId="+accessId+"&accessKey="+accessKey+"&username=1001");
	}
	
	
	/***********************  使用Main 方法来直接调试  *******************/
    public static void main(String[] args) throws Exception {
         
        //-----------------------Do Post---------------------//
        String postUri = "http://192.168.2.160:8080/ec2/http/common/loginBind";
        String postParams = "accessId=2014040341134844HTDM2&accessKey=F86E72F43688F5FBDE3607A0AB7B376C&username=1001&exten=800002";
        doPostRequest(postUri, postParams);

        //-----------------------Do Get---------------------//
        StringBuffer urlBf = new StringBuffer();
        urlBf.append("http://192.168.2.160:8080/ec2/http/common/loginBind?");
        urlBf.append("accessId=2014040341134844HTDM2&accessKey=F86E72F43688F5FBDE3607A0AB7B376C&username=1001&exten=800002");
        
        doGetRequest(urlBf.toString());
        
        doGetOpenStream(urlBf.toString());
    }
    
    //==================================================================//
    /**                       发送请求的方式								   **/
    //==================================================================//

    /**
    * @Description 描述：跟第三方系统对接时，发送 POST 请求，获取返回的结果信息
    *
    * @author  JRH
    * @date    2014年6月30日 下午6:12:15
    * @param uri          请求的URI(即：URL中不包括请求参数的部分)
    * @param params     请求参数
    * @throws Exception 
    */
    public static void doPostRequest(String uri, String params) {
        try {
			URL url = new URL(uri);

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
			httpURLConnection.setRequestProperty("Accept","[*]/[*]");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setConnectTimeout(10000);          // 正常情况下，几毫秒就能连上，如果连不上，可能会造成阻塞，所以设置最大连接时长
			httpURLConnection.setReadTimeout(10000);       		 // 同上
			httpURLConnection.setDoOutput(true);
			
			//取得输出流并写入请求参数
			OutputStream outputStream = httpURLConnection.getOutputStream();
			outputStream.write(params.getBytes("UTF-8"));
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

			/***************** 获取到返回信息后，再做下一步封装等业务处理   *****************/
			//将StringBuffer中的内容输出
			String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
			jsonResult = jsonResult.replace("，", ",");
			 
			// 解析发送结果 
			System.out.println();
			System.out.println("====================================================");
			System.out.println("\t doPostRequest");
			System.out.println(uri+"?"+params);
			System.out.println();
			System.out.println(jsonResult);
		} catch (Exception e) {
			logger.error("JRH 发送 POST 请求失败-->"+e.getMessage(), e); 
		}
    }

    /**
    * @Description 描述：跟第三方系统对接时，发送 GET 请求，获取返回的结果信息
    *
    * @author  JRH
    * @date    2014年6月30日 下午6:09:01
    * @param url     请求路径
    * @throws Exception
    */
    public static void doGetRequest(String url) {
        try {
	         URL requestUrl = new URL(url);
	
	         HttpURLConnection httpURLConnection = (HttpURLConnection) requestUrl.openConnection();
	         httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
	         httpURLConnection.setRequestProperty("Accept","[*]/[*]");
	         httpURLConnection.setRequestMethod("GET");
	         httpURLConnection.setConnectTimeout(10000);          // 正常情况下，几毫秒就能连上，如果连不上，可能会造成阻塞，所以设置最大连接时长
	         httpURLConnection.setReadTimeout(10000);
	         httpURLConnection.setDoOutput(true);
	         
	         //取得输出流并写入请求参数
	         OutputStream outputStream = httpURLConnection.getOutputStream();
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
	         
	         /***************** 获取到返回信息后，再做下一步封装等业务处理   *****************/
	         //将StringBuffer中的内容输出
	         String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
	         jsonResult = jsonResult.replace("，", ",");
	         
	         System.err.println();
	         System.err.println("---------------------------------------------------------");
	         System.err.println("\t doGetRequest");
	         System.err.println(url);
			 System.err.println();
	         System.err.println(jsonResult);
		} catch (Exception e) {
			logger.error("JRH 发送 GET 请求失败-->"+e.getMessage(), e); 
		}
    }

    /**
    * @Description 描述：跟第三方系统对接时，直接通过URL.openStream(); 来访问
    *
    * @author  JRH
    * @date    2014年6月30日 下午6:09:01
    * @param url     请求路径
    * @throws Exception
    */
    private static void doGetOpenStream(String url) {
    	try {
	         URL requestUrl = new URL(url);
	         InputStream is = requestUrl.openStream();
	
	         //读取返回的内容到StringBuffer
	         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	         StringBuffer stringBuffer = new StringBuffer();
	         int ch;
	         while ((ch = bufferedReader.read()) > -1) {
	              stringBuffer.append((char) ch);
	         }
	         bufferedReader.close();
	
	         /***************** 获取到返回信息后，再做下一步封装等业务处理   *****************/
	         //将StringBuffer中的内容输出
	         String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
	         jsonResult = jsonResult.replace("，", ",");
	         
	         System.out.println();
	         System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	         System.out.println("\t doGetOpenStream");
	         System.out.println(url);
			 System.out.println();
	         System.out.println(jsonResult);
		} catch (Exception e) {
			logger.error("JRH 通过 URL.openStream() 请求失败-->"+e.getMessage(), e); 
		}
    }

}
