package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 用户登出操作Agi 
 * @author
 */
public class UserLogout extends BaseAgiScript {
	private static final Logger logger = LoggerFactory
			.getLogger(UserLogout.class);

	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		this.setVariable("ISLOGOUT", "false");
		String channelName=request.getChannel();
		String exten = channelName.substring(channelName.indexOf("/") + 1, channelName.indexOf("-"));
		//取得用户登陆的Service进行登出
		Long userId=ShareData.extenToUser.get(exten);
		UserService userService=SpringContextHolder.getBean("userService");
		User user=null;
		if(userId!=null){
			user=userService.get(userId);
		}
		
		//如果用户已经登陆，并且输对密码，则可以进行登出操作
		if(user != null){
			UserLoginService userLoginService=SpringContextHolder.getBean("userLoginService");
			String result=userLoginService.logout(user.getId(),user.getDomain().getId(),user.getUsername(),user.getEmpNo(),exten,0L, true);
			setVariable("ISLOGOUT", result);
			logger.info("Username " + user.getUsername() + " logout " + exten + ".");
		}
		
//		String username = this.getVariable("USERNAME");
//		String pwd = this.getVariable("PWD");
//		String channelName=request.getChannel();
//		String exten = channelName.substring(channelName.indexOf("/") + 1, channelName.indexOf("-"));
//		//取得用户登陆的Service进行登出
//		Long userId=ShareData.extenToUser.get(exten);
//		UserService userService=SpringContextHolder.getBean("userService");
//		User user=null;
//		if(userId!=null){
//			user=userService.get(userId);
//		}
//		
//		//如果用户已经登陆，并且输对密码，则可以进行登出操作
//		if(user != null && user.getPassword().equals(pwd)){
//			UserLoginService userLoginService=SpringContextHolder.getBean("userLoginService");
//			String result=userLoginService.logout(user.getId(),user.getDomain().getId(),username,user.getEmpNo(),exten,0L);
//			setVariable("ISLOGOUT", result);
//		}
//		logger.info("Username " + username + " logout " + exten + ".");
	}
}
