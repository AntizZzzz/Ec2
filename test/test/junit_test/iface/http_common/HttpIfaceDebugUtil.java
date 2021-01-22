package test.junit_test.iface.http_common;

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
 *              解析调取接口后的返回结果
 * 
 * @author JRH
 * @date 2014年8月8日 下午5:52:14
 */
public class HttpIfaceDebugUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpIfaceDebugUtil.class);

	public static String BASE_URL = "http://192.168.2.160:8080/ec2/http/common/";	// 固定的访问路径

	private static String accessId = "2014040341134844HTDM1";						// 访问接口的安全码
	
	private static String accessKey = "F86E72F43688F5FBDE3607A0AB7B376C";			// 访问接口的秘钥

	
	/*********************** 使用 Junit 来做单元测试 ******************/
	
	@Test
	public void test() { 	// 登录用户与分机绑定
//		doPostRequest("http://192.168.2.160:8080/ec2/http/shiyuan/importresource", "name=l的房顶上9B&phonenum=18814553422ffa发送到AF&regtime=141809读书法951006972&searchsrc=华国锋&naturesrc=和具体规范化&plan=x电饭锅&activity=地方");
//		doGetRequest("http://192.168.2.160:8080/ec2/http/shiyuan/importresource?name=l的房顶上9B&phonenum=19914553422ffa发送到AF&regtime=141809读书法951006972&searchsrc=华国锋&naturesrc=和具体规范化&plan=x电饭锅&activity=地方");
		doGetOpenStream("http://192.168.2.160:8080/ec2/http/shiyuan/importresource?name=l的房顶上9B&phonenum=19914553422ffa发送到AF&regtime=141809读书法951006972&searchsrc=华国锋&naturesrc=和具体规范化&plan=x电饭锅&activity=地方");
	}
	
	@Test
	public void loginBind() { 	// 登录用户与分机绑定
		doPostRequest(BASE_URL + "loginBind", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1002&exten=800002");
		doPostRequest(BASE_URL + "loginBind", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001&exten=800001");
	}

	@Test
	public void logoutUnbind() { // 退出，用户与分机解绑
		doPostRequest(BASE_URL + "logoutUnbind", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	
	@Test
	public void dial() { // 发起呼叫
		doPostRequest(BASE_URL + "dial", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001&destNum=13816760398");
//		doPostRequest(BASE_URL + "dial", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001&destNum=dfas11");
	}
	
	@Test
	public void hangup() { // 挂断
		doPostRequest(BASE_URL + "hangup", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	
	@Test
	public void pause() { // 置忙
		doPostRequest(BASE_URL + "pauseUser", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	
	@Test
	public void unpause() { // 置闲
		doPostRequest(BASE_URL + "unpauseUser", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	
	@Test
	public void transferCallInner() { // 转接到内部分机
		doPostRequest(BASE_URL + "transferCallInner", "accessId=" + accessId + "&accessKey=" + accessKey + "&srcUsername=1001&destUsername=1002");
	}
	
	@Test
	public void transferCallOuter() { // 转接到外部手机
		doPostRequest(BASE_URL + "transferCallOuter", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001&destNum=13816760398");
	}
	
	@Test
	public void transferCallQueue() { // 转接到队列
		doPostRequest(BASE_URL + "transferCallQueue", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001&queue=900001");
	}
	
	@Test
	public void spyUser() { // 监听
		doPostRequest(BASE_URL + "spyUser", "accessId=" + accessId + "&accessKey=" + accessKey + "&destUsername=1001&mgrExten=800002");
	}
	
	@Test
	public void whisperUser() { // 密语
		doPostRequest(BASE_URL + "whisperUser", "accessId=" + accessId + "&accessKey=" + accessKey + "&destUsername=1001&mgrExten=800002");
	}
	
	@Test
	public void aquireBdInfo() { // 获取指定坐席的绑定信息
		doPostRequest(BASE_URL + "aquireBdInfo", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	
	@Test
	public void aquireAllBdInfos() { // 获取所有坐席的绑定信息
		doPostRequest(BASE_URL + "aquireAllBdInfos", "accessId=" + accessId + "&accessKey=" + accessKey);
	}
	
	@Test
	public void holdCall() { // 呼叫保持
		doPostRequest(BASE_URL + "holdCall", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	
	@Test
	public void unholdCall() { // 恢复呼叫保持中的电话
		doPostRequest(BASE_URL + "unholdCall", "accessId=" + accessId + "&accessKey=" + accessKey + "&username=1001");
	}
	

	/*********************** 使用Main 方法来直接调试 *******************/
	
	public static void main(String[] args) throws Exception {

		// -----------------------Do Post---------------------//
		String postUri = "http://192.168.2.160:8080/ec2/http/common/loginBind";
		String postParams = "accessId=2014040341134844HTDM2&accessKey=F86E72F43688F5FBDE3607A0AB7B376C&username=1001&exten=800002";
		doPostRequest(postUri, postParams);

		// -----------------------Do Get---------------------//
		StringBuffer urlBf = new StringBuffer();
		urlBf.append("http://192.168.2.160:8080/ec2/http/common/loginBind?");
		urlBf.append("accessId=2014040341134844HTDM2&accessKey=F86E72F43688F5FBDE3607A0AB7B376C&username=1001&exten=800002");

		doGetRequest(urlBf.toString());

		doGetOpenStream(urlBf.toString());
	}

	// ==================================================================//
	/**                           发送请求的方式                                                                                     **/
	// ==================================================================//

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

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
			httpURLConnection.setRequestProperty("Accept", "[*]/[*]");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setConnectTimeout(10000); 	// 正常情况下，几毫秒就能连上，如果连不上，可能会造成阻塞，所以设置最大连接时长
			httpURLConnection.setReadTimeout(10000); 		// 同上
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
	private static void doGetOpenStream(String url) {
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
