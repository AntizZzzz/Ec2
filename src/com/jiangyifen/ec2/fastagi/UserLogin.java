package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 用户登陆操作Agi 
 * @author
 */
public class UserLogin extends BaseAgiScript {

	private static final Logger logger = LoggerFactory
			.getLogger(UserLogin.class);


	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {

		this.setVariable("ISLOGIN", "false");
		String username = this.getVariable("USERNAME");
		String pwd = this.getVariable("PWD");
		String exten = this.getVariable("CALLERID(num)");
		//取得用户登陆的Service进行登陆
		UserLoginService userLoginService=SpringContextHolder.getBean("userLoginService");
		User loginUser = null;
		try {
			loginUser = userLoginService.login(username, pwd, exten, "0.0.0.0", RoleType.csr);
		} catch (Exception e) {
			logger.error("Username " + username + " login " + exten + " login failed!"+e.getMessage(), e);
			return;
		}

		if(loginUser != null){
			setVariable("ISLOGIN", "true");
		}
		logger.info("Username " + username + " login " + exten + ".");
	}
}
