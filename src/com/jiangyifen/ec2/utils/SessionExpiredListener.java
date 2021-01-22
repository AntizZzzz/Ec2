package com.jiangyifen.ec2.utils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.jiangyifen.ec2.entity.User;

/**
 * session 超时监听器
 */
public class SessionExpiredListener implements HttpSessionListener {
	
	/*private final Logger logger = LoggerFactory.getLogger(this.getClass());*/
	
	/*private UserLoginService loginService;*/

	@Override
	public void sessionCreated(HttpSessionEvent event) {
	}
	
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
    	HttpSession session = event.getSession();
    	/*Long timeoutMsec = session.getMaxInactiveInterval() * 1000L;*/
        User loginUser = (User) session.getAttribute("loginUser");        // 取得登录的用户名

        if(loginUser != null) {
//
//        	/*********************  下面几行虽然解决了自己踢自己的情况    ****************************/
//        	/*********************  但是，又会引发，坐席直接关机的时候，在超时的时候，系统不会为其自动退出    ****************************/
//        	/*********************  暂时把下面几行注释掉 **************************************************************/
//        	//--------------------------------------  开始 注释  ----------------------------------------//
//        	// 如果ShareData 中的session 不为空，表示是用户登录冲突时踢人，但可能是自己踢自己，如果是自己踢自己，则并不做处理
//        	HttpSession hts = ShareData.userToSession.get(loginUser.getId());
//        	if(hts != null) {
//        		return;
//        	}
//        	logger.info("------------用户："+loginUser.getMigrateCsr()+"， 因Session 超时而自动退出系统！");
//        	//--------------------------------------  结束注释  ----------------------------------------//
//			
//			
//        	// 如果是正常超时，则
//// 错误方法       	String exten = (String) session.getAttribute("exten");			// jrh 2013-11-01 不能到session 中取，因为当是由于使用相同分机登陆冲突导致的退出时，等被踢者的session超时时，同样能到session中找到他之前使用的分机，但其实此时该分机的使用者是踢他的人
//        	String exten = ShareData.userToExten.get(loginUser.getId());	// 当用户自己点击退出按钮退出系统或者是被他人踢出时，都已经调用过一次logout 方法，清理了内存信息，此时，这里的exten 获取应该是 null,所以不需要再次调用退出方法了
//        	loginService = SpringContextHolder.getBean("userLoginService");
//
//			//如果用户还没有登出
//// 错误方法	if(ShareData.extenToUser.get(exten) != null){	// 不能用这个来处理，因为：如果这样处理，当是由于使用相同分机登陆冲突导致的退出，就好出问题
//			if(exten != null){
//				loginService.logout(loginUser.getId(),loginUser.getDomain().getId(),loginUser.getUsername(),loginUser.getEmpNo(),exten, timeoutMsec, true);
//			}
//			/***********************管理员处理*************************/
//			//如果超时的是管理员，则对管理员的自动外呼组件进行移除
//			ShareData.mgrToAutoDialout.remove(loginUser.getId());
//			
//			AutoDialoutMonitor autodialMonitor=ShareData.mgrToAutoDialoutMonitor.get(loginUser.getId());
//			if(autodialMonitor!=null){
//				autodialMonitor.stopThread();
//			}
//			ShareData.mgrToAutoDialoutMonitor.remove(loginUser.getId());
//			
//
//			// 停止监控线程
//			MgrTabSheet tabSheet = ShareData.mgrToTabSheet.get(loginUser.getId());
//			if(tabSheet != null) {
//				tabSheet.stopSupperviceThread(null, "onTabChange");
//			}
//			
//			//如果超时的是管理员，则对管理员的监控组件进行移除
//			ShareData.systemStatusMap.remove(loginUser.getId());
        }
    }
}