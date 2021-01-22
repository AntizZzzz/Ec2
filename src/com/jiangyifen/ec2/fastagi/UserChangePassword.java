package com.jiangyifen.ec2.fastagi;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 更改用户密码的AGI
 * @author
 *
 */
public class UserChangePassword extends BaseAgiScript {
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {

		this.setVariable("ISPWDCHANGE", "false");

		String username = this.getVariable("USERNAME");
		String pwd = this.getVariable("PWD");
		String newpwd = this.getVariable("NEWPWD");
		String exten = this.getVariable("CALLERID(num)");
		
		//根据分机取得用户
		Long userId=ShareData.extenToUser.get(exten);
		UserService userService=SpringContextHolder.getBean("userService");
		User user=userService.get(userId);
		//如果输对用户名和密码，则允许进行密码修改
		if(user != null && user.getPassword().equals(pwd)&&user.getUsername().equals(username)){
			user.setPassword(newpwd);
			userService.update(user);
			this.setVariable("ISPWDCHANGE", "true");
		}
	}
}
