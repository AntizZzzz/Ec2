package com.jiangyifen.ec2.service.csr.ami;

import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.User;

public interface UserLoginService {
	
	/**
	 * 处理用户登录冲突问题
	 * 	当一个用户登录是，如若发现该用户或者该分机已在别处登录，则返回已登录用户对象
	 * 	并且告诉已登录者，有人在别处使用自己的账号或分机登录
	 * 
	 * @param username		当前登录用户
	 * @param password		用户密码
	 * @param exten			分机号码
	 * @param roleType		用户登录时使用的角色
	 */
	public List<User> loginConflict(String username,String password, String exten, RoleType roleType);
	
	/**
	 * 用户登录系统
	 *  用户登录时要做的工作：
	 * 		1、判断用户是否登录，如果已经在别处登录，那么就先执行退出工作
	 * 		2、建立与 IP 电话的绑定
	 * 		3、建立并保存用户登录系统的记录，此时只用登录时间
	 * @param username		当前登录用户
	 * @param password		用户密码
	 * @param exten			分机号码
	 * @param ip			用户登录时使用的Ip 地址
	 * @param roleType		用户登录时使用的角色
	 */
	public User login(String username,String password, String exten, String ip, RoleType roleType);

	
	
	/**
	 * 用户退出系统
	 *   用户退出时要做的工作：
	 * 		1、将 IP 电话置闲 使用 QueueMemberPauseService 
	 * 		2、更新置忙记录用户置忙记录 使用QueueMemberPauseService
	 * 		3、解除与 IP 电话的绑定
	 * 		4、清理当前登录用户各种组件所暂用的内存
	 * 		5、更新用户登录记录中的退出时间		如果是由于session超时而导致的退出，则将退出时间减去 timeoutMsec 个毫秒值，如果不是就减0毫秒
	 * 
	 * @param userId				用户id
	 * @param domainId				所属域id
	 * @param username				用户名
	 * @param empNo					工号
	 * @param exten					分机
	 * @param timeoutMsec			超时时间
	 * @param isCloseApp			用户推迟时是否要关闭用户对应的 vaadin application
	 * @return
	 */
	public String logout(Long userId,Long domainId,String username, String empNo,String exten,Long timeoutMsec, boolean isCloseApp);
	
}
