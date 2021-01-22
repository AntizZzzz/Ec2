package test.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.BusHandle;
import com.jiangyifen.ec2.utils.MD5Encrypt;

/**
 * @Description 描述：Ec2 与 中道CRM 对接调用接口的工具类
 *
 * @author  JRH
 * @date    2014年6月30日 上午10:43:21
 * @version v1.0.0
 */
public class CrmIfaceJointUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(CrmIfaceJointUtil.class);
	
	public static void main(String[] args) {
		BusHandle busHandle = null;
		String pwd = MD5Encrypt.getMD5Str("1111", "utf-8");			// 密码需要用 MD5 加密
		busHandle = loginCrmConfirm("10011111111", pwd);
		busHandle = loginCrmConfirm("test", "test");
		System.out.println();
		System.out.println();
//		busHandle = changeCrmUserPwd("1001", "12345", "8888");
//		busHandle = changeCrmUserPwd("1081", "1111", "12345");
		
		System.out.println(busHandle);
	}
	
	/**
	 * @Description 描述：
	 *
	 * @author  JRH
	 * @date    2014年6月30日 上午11:49:23
	 * @param userName
	 * @param pwd
	 * @return BusHandle
	 */
	public static BusHandle loginCrmConfirm(String userName, String pwd) {

		BusHandle busHandle = new BusHandle();
		
	    try {
//	    	TODO
	    	
	    	StringBuffer urlBf = new StringBuffer();
	    	urlBf.append("http://skype.e2say.com/CIPProving.aspx");
	    	urlBf.append("?username="+userName);
	    	urlBf.append("&password="+pwd);
	    	
			URL url = new URL(urlBf.toString());

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setConnectTimeout(10000);
			httpURLConnection.setReadTimeout(10000);
			httpURLConnection.setDoOutput(true);
			
			//取得输出流并写入请求参数
			OutputStream outputStream = httpURLConnection.getOutputStream();
//			outputStream.write(paramsBf.toString().getBytes("UTF-8"));
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
			String jsonResult = StringUtils.trimToEmpty(stringBuffer.toString());
			
			System.out.println(jsonResult);
			
			// 解析发送结果 
//			logger.info("JRH 请求登录认证CRM用户信息的JSON结果--->"+jsonResult);
//			jsonResult = "{\"code\":1,\"userinfo\":{\"name\":\"1001\",\"password\":\"1111\",\"username\":\"1001\",\"age\":4},\"aaa\":4}";
//			
//			JSONObject jsonObj = JSONObject.fromObject(jsonResult);  
//			CrmLoginUserBo crmLoginUserBo = (CrmLoginUserBo) JSONObject.toBean(jsonObj, CrmLoginUserBo.class);  
//			logger.info("JRH 请求登录认证CRM用户信息的Ec2段封装后结果--->"+crmLoginUserBo);
//			
//			System.out.println(crmLoginUserBo);
//			
//			if(crmLoginUserBo != null) {
//				CrmUserInfo crmUserInfo = crmLoginUserBo.getUserinfo();
//				HashMap<String, Object> resultMap = new HashMap<String, Object>();
//				resultMap.put("crmUserInfo", crmUserInfo);
//				
//				busHandle.setResultMap(resultMap);
//				
//				if(crmLoginUserBo.getCode() == null) {
//					busHandle.setNotice("验证返回信息有误(CRM)");
//					busHandle.setSuccess(false);
//				} else if(crmLoginUserBo.getCode() == 0) {
//					busHandle.setNotice("用户名或密码不正确(CRM)");
//					busHandle.setSuccess(false);
//				} else if(crmLoginUserBo.getCode() == 1) {
//					busHandle.setNotice("用户验证成功(CRM)");
//					busHandle.setSuccess(true);
//				} else {
//					busHandle.setNotice("验证返回信息有误(CRM)");
//					busHandle.setSuccess(false);
//				}
//			}
			
			return busHandle;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("JRH --- EC2 向CRM 发送登录认证CRM用户信息请求失败，可能CRM无法连接上-->"+e.getMessage(), e);
			
			busHandle.setSuccess(false);
			busHandle.setNotice("向CRM 登录认证，请求失败");
			return busHandle;
		}
	}

}
