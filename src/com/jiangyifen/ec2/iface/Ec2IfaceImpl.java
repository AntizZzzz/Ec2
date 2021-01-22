package com.jiangyifen.ec2.iface;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.fastagi.UserLogin;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * Ec2系统的接口实现
 * 用户密码是000
 * @author chb
 *
 */
public class Ec2IfaceImpl implements Ec2Iface{
	private static final Logger logger = LoggerFactory
			.getLogger(UserLogin.class);

//	@Override
//	public String userAuthentication(String userName, String password) {
//		UserService userService=SpringContextHolder.getBean("userService");
//		User loginUser = userService.identify(userName, password, RoleType.csr);
//		if(loginUser!=null){
//			return "SUCCESS";
//		}else{
//			return "FAILED";
//		}
//	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean bindToSip(String extenNo, String userName, String realName) {
		CommonService commonService=SpringContextHolder.getBean("commonService");
		//取得域
		Domain domain=commonService.get(Domain.class, 999L);
		//先添加用户到数据库表中
		List<User> userList=commonService.getEntityManager().createQuery("select u from User u where u.username='"+userName+"' and u.password='000'").getResultList();
		User user=null;
		if(userList.size()<1){
			user=new User();
			user.setUsername(userName);
			user.setPassword("000"); 
			user.setRegistedDate(new Date());
			user.setDomain(domain);
			Set<Role> roleSet=new HashSet<Role>();
			List<Role> roleList=commonService.getEntityManager().createQuery("select r from Role r where r.type=com.jiangyifen.ec2.bean.RoleType.csr").getResultList();
			Role role=roleList.get(0);
			roleSet.add(role); 
			user.setRoles(roleSet);
			user=(User)commonService.update(user);
		}else{
			user=userList.get(0);
		}
		//取得用户登陆的Service进行登陆
		UserLoginService userLoginService=SpringContextHolder.getBean("userLoginService");
		User loginUser=userLoginService.login(userName, "000", extenNo, "0.0.0.0", RoleType.csr);
		logger.info("远端用户绑定分机，用户为："+loginUser.getUsername()+" 分机为："+extenNo);
		return true;
	}

	@Override
	public String originate(String exten, String phoneNumber) {
		DialService dialService=SpringContextHolder.getBean("dialService");
		dialService.dial(exten, phoneNumber);
		return "SUCCESS";
	}

	@Override
	public Boolean hangUp(String channel) {
		HangupService hangupService=SpringContextHolder.getBean("hangupService");
		hangupService.hangup(channel);
		return true;
	}

	@Override
	public Boolean hangUp(String userName, String exten) {
		HangupService hangupService=SpringContextHolder.getBean("hangupService");
		Set<String> channels = ShareData.peernameAndChannels.get(exten);
		for(String channel:channels){
			hangupService.hangup(channel);
		}
		return true;
	}

	@Override
	public Boolean redirectCall(String channel, String exten) {
		ChannelRedirectService channelRedirectService=SpringContextHolder.getBean("channelRedirectService");
		channelRedirectService.redirectExten(channel, exten);
		return true;
	}

}
