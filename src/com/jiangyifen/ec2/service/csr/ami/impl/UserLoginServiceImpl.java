package com.jiangyifen.ec2.service.csr.ami.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.autodialout.ProjectResourceConsumer;
import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserLoginRecord;
import com.jiangyifen.ec2.entity.UserOutline;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.license.LicenseManager;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserExtenPersistService;
import com.jiangyifen.ec2.service.eaoservice.UserLoginRecordService;
import com.jiangyifen.ec2.service.eaoservice.UserOutlineService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.csr.CsrWorkAreaRightView;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;

public class UserLoginServiceImpl implements UserLoginService {

	// 坐席登陆就置忙
	private final String PASUE_EXTEN_AFTER_CSR_LOGIN = "pasue_exten_after_csr_login";
	
	private Phone2PhoneSettingService phone2PhoneSettingService;	// 外转外配置服务类
	private QueueService queueService; 								// 队列服务类
	private QueueMemberRelationService queueMemberRelationService; // 队列成员关系管理服务类
	private QueuePauseService queuePauseService; // 队列置忙服务类
	private QueuePauseRecordService queuePauseRecordService; // 队列成员置忙服务类
	private SipConfigService sipConfigService; // 分机服务类
	private StaticQueueMemberService staticQueueMemberService; 	// 用户登录时自动生成分机与队列的动态队列
	private UserExtenPersistService userExtenPersistService;	// 用户分机持久化服务类
	private UserLoginRecordService userLoginRecordService; // 用户登录记录服务类
	private UserOutlineService userOutlineService; // 用户与外线的静态对应关系服务类
	private UserQueueService userQueueService; // 用户与队列的对应关系服务类
	private UserService userService; // 分机服务类
	private AutoDialoutTaskService autoDialoutTaskService;

	/**
	 * jrh 队列置忙成员	
	 * 		该方法按理不应该放在这，但现在为了方便，以后重构的时候一定要处理好
	 * 
	 * @param user		坐席
	 * @param exten		分机
	 * @param ispause	是否为置忙
	 * @param pauseReason	置忙原因
	 * @param optType	操作类型，是登录还是退出【login、logout】
	 */
	public void pauseQueueMember(User user, String exten, boolean ispause, String pauseReason, String optType) {
		if(user == null) {
			return;
		}

		ArrayList<String> queueNameList = new ArrayList<String>();
		// 从动态队列中查询获取该分机所属的队列
		for (UserQueue uq : userQueueService.getAllByUsername(user.getUsername())) {
			queueNameList.add(uq.getQueueName());
		}
		// 从静态队列中查询获取该分机所属的队列
		for (StaticQueueMember sqm : staticQueueMemberService.getAllBySipname(user.getDomain(), exten)) {
			queueNameList.add(sqm.getQueueName());
		}
		for (String queueName : queueNameList) {
			QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(user.getUsername(), exten, queueName);
			if (queuePauseRecord != null) {
				queuePauseRecord.setUnpauseDate(new Date());
				queuePauseRecordService.update(queuePauseRecord);
			}
			queuePauseService.pause(queueName, exten, ispause, pauseReason);

			/** 
			 * 如果是登陆，并且是置闲，则创建置闲记录【如果是退出置闲，则不需要增加新的置闲记录】
			 * 无论是登录，还是退出，只要是置忙，则需要创建新的置忙记录
			 ************************************************************/
			if(("login".equals(optType) && !ispause) || ispause) {	// 
				QueuePauseRecord newPauseRecord = new QueuePauseRecord();
				Department userDept = user.getDepartment();
				newPauseRecord.setDeptId(userDept.getId());
				newPauseRecord.setDeptName(userDept.getName());
				newPauseRecord.setDomainId(user.getDomain().getId());
				newPauseRecord.setPauseDate(new Date());
				newPauseRecord.setQueue(queueName);
				newPauseRecord.setReason(pauseReason);
				newPauseRecord.setSipname("SIP/" + exten);
				newPauseRecord.setUsername(user.getUsername());
				queuePauseRecordService.save(newPauseRecord);
			}
		}
	}

	/**
	 * 用户登录系统时，将分机与队列的对应关系添加到动态队列表中
	 * 
	 * @param user
	 *            当前登录者的用户
	 * @param exten
	 *            登录者正在使用的分机号
	 * @param needAddCommonMember
	 *            是否需要添加分机成员到非自动外网使用的队列
	 */
	private void addQueueMemberRelation(User user, String exten, boolean needAddCommonMember) {
		// 获取所有自动外呼使用的队列
		List<Queue> allAutoQueues = queueService.getAllByDomain(user.getDomain(), false);
		List<String> allAutoQueueNames = new ArrayList<String>();
		for(Queue autoQueue : allAutoQueues) {
			allAutoQueueNames.add(autoQueue.getName());
		}
		
		// 取出用户对应的所有的动态队列
		List<UserQueue> userQueueList = userQueueService.getAllByUsername(user.getUsername());
		// 取出用户对应的所有的静态队列
		List<StaticQueueMember> staticQMs = staticQueueMemberService
				.getAllBySipname(user.getDomain(), exten);

		for (UserQueue uq : userQueueList) {
			if(!needAddCommonMember && !allAutoQueueNames.contains(uq.getQueueName())) {
				continue;	// 无论在什么情况，话务员登陆系统后，如果他有对应的自动外呼队列成员，则将其使用的分机加入自动外呼队列
			}
			queueMemberRelationService.addQueueMemberRelation(uq.getQueueName(), exten, uq.getPriority());
		}
		
		// 如果不需要添加非自动外呼使用的队列成员，则直接返回
		if(needAddCommonMember == false) {
			return;
		}
		
		for (StaticQueueMember sqm : staticQMs) {
			queueMemberRelationService.addQueueMemberRelation(sqm.getQueueName(), exten, sqm.getPriority());
		}
	}

	/**
	 * 新建一个 用户登录记录
	 * 
	 * @param loginUser
	 *            用户
	 * @param exten
	 *            分机号
	 * @param ip
	 *            用户使用的ip地址
	 */
	private void addUserLoginRecord(User loginUser, String exten, String ip) {
		UserLoginRecord userLoginRecord = new UserLoginRecord();
		userLoginRecord.setIp(ip);
		userLoginRecord.setExten(exten);
		userLoginRecord.setLoginDate(new Date());
		userLoginRecord.setUsername(loginUser.getUsername());
		userLoginRecord.setRealName(loginUser.getRealName());
		userLoginRecord.setDeptId(loginUser.getDepartment().getId());
		userLoginRecord.setDeptName(loginUser.getDepartment().getName());
		userLoginRecord.setDomainId(loginUser.getDomain().getId());
		userLoginRecordService.save(userLoginRecord);
	}

	/**
	 * 用户退出系统时，从动态队列中删除指定分机与队列的对应关系
	 * 
	 * @param user			用户
	 * @param exten			分机号
	 * @param unneedDeleteMember 	是否需要移除手机成员
	 */
	private void deleteQueueMemberRelation(User user, String exten) {
		// 取出用户对应的所有的队列
		List<UserQueue> userQueueList = userQueueService.getAllByUsername(user.getUsername());
		// 取出用户对应的所有的静态队列
		List<StaticQueueMember> staticQMs = staticQueueMemberService.getAllBySipname(user.getDomain(), exten);

		for (UserQueue uq : userQueueList) {
			queueMemberRelationService.removeQueueMemberRelation(uq.getQueueName(), exten);
		}
		
		for (StaticQueueMember sqm : staticQMs) {
			queueMemberRelationService.removeQueueMemberRelation(sqm.getQueueName(), exten);
		}
	}

	/**
	 * 更新 用户最近的登录记录, 为其增加 退出时间
	 * 
	 * @param username
	 *            用户名
	 */
	private void updateUserLoginRecord(String username) {
		UserLoginRecordService loginRecordService = SpringContextHolder.getBean("userLoginRecordService");
		UserLoginRecord userLoginRecord = loginRecordService.getLastRecord(username);
		Date logoutDate = new Date();
		if (userLoginRecord != null) {
			userLoginRecord.setLogoutDate(logoutDate);
			loginRecordService.update(userLoginRecord);
		}
	}

	/**
	 * 刷新所有自动外呼线程的消费线程
	 */
	private void refreshAutoDialThread() {
		for (String threadName : AutoDialHolder.nameToThread.keySet()) {
			// 如果是消费线程
			if (threadName.startsWith(AutoDialHolder.AUTODIAL_CONSUMER_PRE)) {
				ProjectResourceConsumer projectResourceConsumer = (ProjectResourceConsumer) AutoDialHolder.nameToThread.get(threadName);
				projectResourceConsumer.refreshEditChangeVariable();
			}
		}
	}

	@Override
	public List<User> loginConflict(String username, String password, String exten, RoleType roleType) {
		// 当前要登录的用户
		User loginUser = userService.identify(username, password, roleType);
		// 检测用户是否存在，考虑到话机登陆，所以在业务层检测
		if (loginUser == null) {
			throw new RuntimeException("用户名或密码不正确");
		}

		// 检查分机是否存在
		if (!sipConfigService.existBySipname(exten, loginUser.getDomain())) {
			throw new RuntimeException("分机不存在");
		}
		
		List<User> users = new ArrayList<User>();
		// 检查用户是否已经登录，返回此用户对象
		Long userId = loginUser.getId();
		if (ShareData.userToExten.containsKey(userId)) {
			users.add(loginUser);
			CsrToolBar conflictUserStatusBar = ShareData.csrToToolBar.get(userId);
			if(conflictUserStatusBar != null) {
				conflictUserStatusBar.updateConflictWindow(loginUser, exten);
			}
		}
		
		// 如果，输入的分机依然被占用
		if (ShareData.extenToUser.containsKey(exten)) {
			Long toLogoutUserId = ShareData.extenToUser.get(exten);
			User toLogoutUser = userService.get(toLogoutUserId);
			// 如果占用分机的人跟占用账号的人不是同一人，则将占用分机的人也加入集合中去
			if(toLogoutUserId != userId && toLogoutUser != null) {	
				users.add(toLogoutUser);
				CsrToolBar conflictExtenStatusBar = ShareData.csrToToolBar.get(toLogoutUserId);
				if(conflictExtenStatusBar != null) {
					conflictExtenStatusBar.updateConflictWindow(loginUser, exten);
				}
			}
		}
		return users;
	}

	/**
	 * 用户登陆，如果是话机登陆则Ip为0.0.0.0
	 */
	@Override
	public User login(String username, String password, String exten,
			String ip, RoleType roleType) {
		// 登陆
		User loginUser = userService.identify(username, password, roleType);
		if(loginUser == null) {	// 一般不会发生
			throw new RuntimeException("用户不存在");
		}

		// 检查分机是否存在
		if (!sipConfigService.existBySipname(exten, loginUser.getDomain())) {
			throw new RuntimeException("分机不存在");
		}
		
		// 检查用户是否已经登录，如果已经登陆，则先退出
		Long userId = loginUser.getId();
		Long domainId = loginUser.getDomain().getId();
		if (ShareData.userToExten.containsKey(userId)) {
			// 退出用户,退出用户自己所在的分机
			logout(userId, domainId, username, loginUser.getEmpNo(),
					ShareData.userToExten.get(userId), 0L, true);
		} 
		
		// 如果用户已经退出系统，输入的分机依然被占用
		if (ShareData.extenToUser.containsKey(exten)) {
			Long toLogoutUserId = ShareData.extenToUser.get(exten);
			User toLogoutUser = userService.get(toLogoutUserId);
			// 按照分机退出用户
			if(toLogoutUser != null) {	// 一般不会发生，但如果手动操作了数据库，可能会引发
				logout(toLogoutUser.getId(), toLogoutUser.getDomain().getId(),
						toLogoutUser.getUsername(), toLogoutUser.getEmpNo(), exten, 0L, true);
			}
		}
		
		// // TODO 并发上限的设置问题
		// jrh 检查用户并发是否达到上限值，如果达到则不允许登陆系统
		int concurrentMaxUser = 0;
		Map<String, String> licenseMap = LicenseManager.licenseValidate();
		if(LicenseManager.LICENSE_VALID.equals(licenseMap.get(LicenseManager.LICENSE_VALIDATE_RESULT))){
			String licensedCount = licenseMap.get(LicenseManager.LICENSE_COUNT);
			try {
				concurrentMaxUser=Integer.parseInt(licensedCount);
			} catch (Exception e) {
				concurrentMaxUser=0;
				e.printStackTrace();
			}
		}else{
			throw new RuntimeException("对不起，授权已失效，请重新授权");
		}
		
		if (ShareData.extenToUser.size() == concurrentMaxUser) {
			throw new RuntimeException("对不起，同时使用系统的人数已达上限值 ("+ concurrentMaxUser + "人)");
		}

		// Note: userToApp userToSession 在CSR登陆时存储，只存储CSR，不存管理员
		// 1、存储用户与分机的对应关系,如果此时内存中的分机已经被占用，则会自动被覆盖
		ShareData.extenToUser.put(exten, userId);
		ShareData.userToExten.put(userId, exten);
		// 向数据库持久化消息
		UserExtenPersistService userExtenPersistService = SpringContextHolder
				.getBean("userExtenPersistService");
		userExtenPersistService.updateExtenToUser(exten, userId);
		ShareData.extenToDomain.put(exten, domainId);
		ShareData.userToDomain.put(userId, domainId);
		ShareData.userToDepartment.put(userId, loginUser.getDepartment().getId());
		
		// Note: extenToDynamicOutline extenToProject
		// 在CSR页面切项目，管理员编辑、开始、停止、删除时处理
		// Note：domainToDefaultOutline 在ShareData 初始化、修改外线名称、修改默认外线处理了

		// 存储登陆用户工号和分机的对应关系
		// ConcurrentHashMap<String, String> empnoToExten =
		// ShareData.domainToEmpnoToExten.get(domainId);
		// if (empnoToExten == null) {
		// empnoToExten = new ConcurrentHashMap<String, String>();
		// ShareData.domainToEmpnoToExten.put(domainId, empnoToExten);
		// }
		// empnoToExten.put(loginUser.getEmpNo(), exten);

		// Note: Outline 删除，编辑（username、defaultOutline）时需要更改
		UserOutline userOutline = userOutlineService.getByUserId(userId,
				domainId);
		if (userOutline != null) {
			String staticOutline = userOutline.getSip().getName();
			ShareData.extenToStaticOutline.put(exten, staticOutline);
		}

		// 2、projectToQueue、projectToOutline、outlineToProject
		// 在项目开始、结束、添加删除时进行维护，初始化加载时对所有进行中的项目进行维护

		// 2、为用户建立一个peerAndChannels的对应关系（空集合）
		ShareData.peernameAndChannels.put(exten, new HashSet<String>());

		// 3、 增加新的登录记录
		this.addUserLoginRecord(loginUser, exten, ip);

		// 4、刷新（与用户有关的：现在简单做法，还没有实现）正在进行中的自动外呼的用户和分机
		// TODO jinht update
		List<AutoDialoutTask> autoDialoutTaskList = autoDialoutTaskService.getAllByDialoutType(domainId, "自动外呼", AutoDialoutTaskStatus.RUNNING);
		if(autoDialoutTaskList.size() > 0) {
			this.refreshAutoDialThread();
		}

		// 获取全局外转外配置信息
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domainId);
		if(globalSetting == null) {	// 如果全局外转外为空，则可定需要修改分机队列成员
			this.addQueueMemberRelation(loginUser, exten, true);
		} else {	// 判断全局外转外配置是否正在运行等
			boolean isGlobalSettingRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);

			// 5、 添加分机(用户)与队列的对应关系
			// 判断对于全局配置而言是否需要添加分机队列成员（如果满足 1、外转外正在运行，2、外转外配置的呼叫方式为“便捷呼叫(即所有呼入都打到指定手机)”，则不添加分机成员到队列）
			boolean needAddExten2QueueMember = !(isGlobalSettingRunning && globalSetting.getIsSpecifiedPhones());
			this.addQueueMemberRelation(loginUser, exten, needAddExten2QueueMember);
			
			// 6、修改外转外时，队列中的手机成员
			// 判断对于全局配置而言是否需要更新手机队列成员（如果满足 1、正在运行，2、外转外配置的呼叫方式为“智能呼叫(即指定话务员)”，则更新手机成员到队列）
			boolean needUpdatePhone2QueueMember = (isGlobalSettingRunning && !globalSetting.getIsSpecifiedPhones());
			this.updatePhoneInQueueMember(loginUser, true, globalSetting, needUpdatePhone2QueueMember);
		}
		
		// jrh 2013-12-17 判断员工登陆系统后是否需要默认置忙       ---------------  开始
		ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(domainId);
		if(domainConfigs != null) {
			Boolean ispauseExten = domainConfigs.get(PASUE_EXTEN_AFTER_CSR_LOGIN);
			if(ispauseExten != null && ispauseExten) {	// 如果开启了登录置忙，则置忙，否则添加置闲记录
				pauseQueueMember(loginUser, exten, true, GlobalVariable.DEFAULT_LOGIN_EXTEN_PAUSE_REASON, "login");
			} else {
				pauseQueueMember(loginUser, exten, false, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, "login");
			}
		} else {	// 默认登陆就置闲
			pauseQueueMember(loginUser, exten, false, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, "login");
		}	//	------------- 结束
		
		return loginUser;
	}

	/**
	 * 用户退出系统时，解除与IP 电话的绑定 清理内存，并将用户退出时间加入用户登录系统的记录中
	 * 
	 * @param loginUser
	 *            登录用户
	 * @param exten
	 *            分机号
	 * @param timeoutMsec
	 *            session 超时时长
	 * @param isCloseApp
	 *            是否需要关闭application，如果是一个账号在管理员域坐席界面的切换，则不需要关闭application，否则需要关闭
	 */
	@Override
	public String logout(Long userId, Long domainId, String username,
			String empNo, String exten, Long timeoutMsec, boolean isCloseApp) {
		// 当前要退出系统的话务员对象
		User logoutCsr = userService.get(userId);
		if(logoutCsr == null) {
			return "true";
		}
		// 如果不为null，才进行清除操作，如果为null则可能是用户已经退出，然后Session超时二次退出
		/************* 如果是用系统登陆的，进行退出时执行下面操作 **********/
		if (ShareData.csrToToolBar.get(userId) != null) {
			// 将Session置为超时，踢掉其它地方的登陆  登出特殊处理，在页面中存储 
			ShareData.userToSession.remove(userId);
			
// TODO start 
			// 登出特殊处理，在页面中存储
			Application app = ShareData.userToApp.get(userId);
			if (app != null && isCloseApp) {// 如果是空，则可能是分机登陆，对分机登陆的结果进行踢出操作，不能关闭页面
				app.close();
			}
			ShareData.userToApp.remove(userId);

			// 2、清理内存中用户与界面组件的管理
			ShareData.csrToToolBar.remove(userId);
			ShareData.csrToStatusBar.remove(userId);
			// 在退出系统前需要将用户的转接弹出的可刷新字段置为false，不然后台始终会有一个线程在哪里刷新
			if (ShareData.csrToDialRedirectWindow.get(userId) != null) {
				ShareData.csrToDialRedirectWindow.get(userId).setGotoRun(false);
			}
			ShareData.csrToDialRedirectWindow.remove(userId);
			ShareData.csrToCurrentTab.remove(userId);
			ShareData.csrToIncomingDialWindow.remove(userId);

			// 停止会议室监控线程
			CsrWorkAreaRightView rightView = ShareData.csrToWorkAreaRightView.get(userId);
			if(rightView != null) {
				rightView.stopSupperviceThread(null);
			}
			ShareData.csrToWorkAreaRightView.remove(userId);

			// jrh 清除呼叫保持客户与话务员的关系
			ShareData.userExtenToHoldOnCallerChannels.remove(exten);
			
			//如果超时的是管理员，则对管理员的监控组件进行移除
			ShareData.systemStatusMap.remove(userId);
		}
		
		// 将分机在各队列中置闲
		pauseQueueMember(logoutCsr, exten, false, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, "logout");

		/************* 无论是系统退出，还是软电话退出，下面代码都要执行 **********/

		// 1、清理用户和登陆时关联的信息
		ShareData.extenToUser.remove(exten);
		ShareData.userToExten.remove(userId);
		ShareData.extenToDomain.remove(exten);
		ShareData.userToDomain.remove(userId);
		ShareData.userToDepartment.remove(userId);
		ShareData.extenToProject.remove(exten);
		ShareData.extenToDynamicOutline.remove(exten);
		ShareData.extenToStaticOutline.remove(exten);
		ShareData.extenToProject.remove(exten);

		// ConcurrentHashMap<String, String> empnoToExten =
		// ShareData.domainToEmpnoToExten.get(domainId);
		// if (empnoToExten != null) {
		// empnoToExten.remove(empNo);
		// }

		// 2、从数据库移除用户与分机对应的持久化消息
		userExtenPersistService.removeExtenToUser(exten);
		
		// 3、为移除peerAndChannels的对应关系（空集合）
		ShareData.peernameAndChannels.remove(exten);
		for (String channel : ShareData.channelAndChannelSession.keySet()) {
			String peername = channel.substring(channel.indexOf("/") + 1,
					channel.indexOf("-"));
			if (peername.equals(exten)) {
				ShareData.channelAndChannelSession.remove(channel);
			}
		}

		// 4、 将用户退出时间加入用户登录系统的记录中
		this.updateUserLoginRecord(username);

		// 5、刷新（与用户有关的：现在简单做法，还没有实现）正在进行中的自动外呼的用户和分机
		refreshAutoDialThread();
		
		// 6、 移除分机与队列的对应关系
		// 判断对于全局配置而言是否需要移除分机队列成员（前两个条件用于判断全局外转外是否已经启动）
		//（如果满足 1、全局配置是开启状态，2、启动时刻 <= 当前时刻，3、外转外配置的呼叫方式为“便捷呼叫(即所有呼入都打到指定手机)”，则不需要从队列中删除分机成员）
//		boolean unneedRemoveExten2QueueMember = (isGlobalSettingRunning && globalSetting.getIsSpecifiedPhones());
//		this.deleteQueueMemberRelation(logoutCsr, exten, unneedRemoveExten2QueueMember);
//		TODO 如果为了简单，而且少查数据库，则可以考虑，无论什么情况，用户退出时，都将其使用的分机从各种队列成员中移除
		// 这样做的好处是：1、不用从数据库查出所有的自动外呼使用的队列；2、能够确保用户退出后，将分机从队列成员中移除
		// 		缺点是：可能会向asterisk 多发几个action
		this.deleteQueueMemberRelation(logoutCsr, exten);
		
		// 7、修改外转外时，队列中的手机成员
		// 判断对于全局配置而言是否需要移除手机队列成员
		//（如果满足 1、全局配置是开启状态，2、启动时刻 <= 当前时刻，3、外转外配置的呼叫方式为“智能呼叫(即指定话务员)”，则添加手机成员到队列）
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domainId);
		if(globalSetting != null) {
			boolean isGlobalSettingRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);
			this.updatePhoneInQueueMember(logoutCsr, false, globalSetting, isGlobalSettingRunning);
		}

		return "true";
	}

	/**
	 * jrh
	 *  当用户登陆系统或者退出系统时，更加实际情况对队列中的手机成员进行加入或移出操作（定时外转外配置）
	 * @param csr		当前登陆或者退出系统的话务员
	 * @param isLogin	是否为登陆系统
	 */
	private void updatePhoneInQueueMember(User csr, boolean isLogin, 
			Phone2PhoneSetting globalSetting, boolean isGlobalSettingRunning) {
		// 如果话务员对象为空，则直接返回
		if(csr == null) {
			return;
		}
		
		// 话务员必须有电话号,如果电话号不存在，则直接返回
		String phoneNum = csr.getPhoneNumber();
		if(phoneNum == null || "".equals(phoneNum)) {
			return;
		}
		
		Domain domain = csr.getDomain();
		String defaultOutline = ShareData.domainToDefaultOutline.get(domain.getId());
		if(defaultOutline == null) {
			return;		// 如果默认外线不存在，则不作任何操作
		}

		// 获取所有非自动使用的队列
		List<Queue> allCommonQueues = queueService.getAllByDomain(domain, true);
		List<String> allCommonQueueNames = new ArrayList<String>();
		for(Queue autoQueue : allCommonQueues) {
			allCommonQueueNames.add(autoQueue.getName());
		}
		
		// 第一步，判断全局配置外转外
		// 对于全局配置而言，添加手机号到队列需要满足 1、全局配置是开启状态，2、启动时刻 <= 当前时刻，3、外转外配置的呼叫方式为“智能呼叫”(即指定话务员)
		if(isGlobalSettingRunning) {
			// 如果当前正在进行的方式是“便捷呼叫”，则直接退出
			if(globalSetting.getIsSpecifiedPhones()) {
				return;
			}

			// 如果全局配置是“智能呼叫”，并且处于进行中状态,而且这些话务员中包含当前被选中的CSR，此时不需要考虑话务员自定义的配置了
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(csr.getId())) {
					updateSpecifyCsrQueueMember(specifiedCsr, phoneNum, defaultOutline, allCommonQueueNames, isLogin);
					// 如果在全局中已经找到配置，则不需要考虑话务员自定义的配置了
					return;
				}
			}
		} 

		// 第二步：判断话务员自定义的外转外配置(能执行到这，说明如果是“便捷呼叫”，则当前全局配置一定没有运行外转外)
		// 对应自定义的设置而言，添加手机号到队列，需要满足 1、全局配置的外转外呼叫方式不是打到指定的手机号，2、全局配置项中之指定的话务员中不包含当前话务员
		if(!globalSetting.getIsSpecifiedPhones()) {
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				// 如果话务员包含在全局配置中，则以全局配置为主[智能呼叫：管理员对那些选中的话务员是完全控制外转外的]
				if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(csr.getId())) {
					return;
				}
			}
			
		}
		// 3、话务员持有自定义外转外的授权，4、自定义的外转外存在并是开启状态，5、启动时刻 <= 当前时刻
		if(globalSetting.getIsLicensed2Csr()) { 
			Phone2PhoneSetting customSetting = phone2PhoneSettingService.getByUser(csr.getId());
			if(customSetting != null) {
				boolean isCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
				if(isCustomRunning) {
					updateSpecifyCsrQueueMember(csr, phoneNum, defaultOutline, allCommonQueueNames, isLogin);
				}
			}
		}
	}

	/**
	 * jrh
	 *  根据话务员对象，如果话务员电话号发生变化，则更新队列中的手机成员
	 * @param csr				话务员对象
	 * @param outlineName		默认外线
	 * @param isLogin			是否为登陆系统
	 */
	private void updateSpecifyCsrQueueMember(User csr, String phoneNum, String outlineName, List<String> allCommonQueueNames, boolean isLogin) {
		// 只有话务员不在线，才需要将话务员的手机号移入队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(!isLogin && allCommonQueueNames.contains(queueName)) {	// 如果是退出系统，则将电话号加入到非自动外呼使用的队列中
				queueMemberRelationService.addQueueMemberRelation(userQueue.getQueueName(), phoneNum+"@"+outlineName, 5);
			} else {	// 如果是登陆系统，则将电话号从队列成员中移除（为了保证队列成员的准确性，不在当前是队列是否为自动外呼使用的队列）
				queueMemberRelationService.removeQueueMemberRelation(userQueue.getQueueName(), phoneNum+"@"+outlineName);
			}
		}
	}

	public Phone2PhoneSettingService getPhone2PhoneSettingService() {
		return phone2PhoneSettingService;
	}

	public void setPhone2PhoneSettingService(
			Phone2PhoneSettingService phone2PhoneSettingService) {
		this.phone2PhoneSettingService = phone2PhoneSettingService;
	}

	public QueueService getQueueService() {
		return queueService;
	}

	public void setQueueService(QueueService queueService) {
		this.queueService = queueService;
	}

	public QueueMemberRelationService getQueueMemberRelationService() {
		return queueMemberRelationService;
	}

	public void setQueueMemberRelationService(
			QueueMemberRelationService queueMemberRelationService) {
		this.queueMemberRelationService = queueMemberRelationService;
	}

	public QueuePauseService getQueuePauseService() {
		return queuePauseService;
	}

	public void setQueuePauseService(QueuePauseService queuePauseService) {
		this.queuePauseService = queuePauseService;
	}

	public QueuePauseRecordService getQueuePauseRecordService() {
		return queuePauseRecordService;
	}

	public void setQueuePauseRecordService(
			QueuePauseRecordService queuePauseRecordService) {
		this.queuePauseRecordService = queuePauseRecordService;
	}

	public SipConfigService getSipConfigService() {
		return sipConfigService;
	}

	public void setSipConfigService(SipConfigService sipConfigService) {
		this.sipConfigService = sipConfigService;
	}

	public StaticQueueMemberService getStaticQueueMemberService() {
		return staticQueueMemberService;
	}

	public void setStaticQueueMemberService(
			StaticQueueMemberService staticQueueMemberService) {
		this.staticQueueMemberService = staticQueueMemberService;
	}

	public UserExtenPersistService getUserExtenPersistService() {
		return userExtenPersistService;
	}

	public void setUserExtenPersistService(
			UserExtenPersistService userExtenPersistService) {
		this.userExtenPersistService = userExtenPersistService;
	}

	public UserLoginRecordService getUserLoginRecordService() {
		return userLoginRecordService;
	}

	public void setUserLoginRecordService(
			UserLoginRecordService userLoginRecordService) {
		this.userLoginRecordService = userLoginRecordService;
	}

	public UserOutlineService getUserOutlineService() {
		return userOutlineService;
	}

	public void setUserOutlineService(UserOutlineService userOutlineService) {
		this.userOutlineService = userOutlineService;
	}

	public UserQueueService getUserQueueService() {
		return userQueueService;
	}

	public void setUserQueueService(UserQueueService userQueueService) {
		this.userQueueService = userQueueService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public AutoDialoutTaskService getAutoDialoutTaskService() {
		return autoDialoutTaskService;
	}

	public void setAutoDialoutTaskService(
			AutoDialoutTaskService autoDialoutTaskService) {
		this.autoDialoutTaskService = autoDialoutTaskService;
	}
	
}
