package com.jiangyifen.ec2.globaldata;

import java.io.IOException;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.asteriskjava.manager.event.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.backgroundthread.GlobalSettingScheduler;
import com.jiangyifen.ec2.backgroundthread.Phone2PhoneScheduler;
import com.jiangyifen.ec2.bean.BridgetimeInfo;
import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.bean.MeettingRoomFirstJoinMemberInfo;
import com.jiangyifen.ec2.bean.MobileAreacode;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.entity.OutlinePool;
import com.jiangyifen.ec2.entity.OutlinePoolOutlineLink;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.QueueRequestDetail;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserExtenPersist;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;
import com.jiangyifen.ec2.globaldata.license.LicenseManager;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.BlackListItemService;
import com.jiangyifen.ec2.service.eaoservice.DomainService;
import com.jiangyifen.ec2.service.eaoservice.Ec2ConfigurationService;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolOutlineLinkService;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolService;
import com.jiangyifen.ec2.service.eaoservice.OutlineToIvrLinkService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserExtenPersistService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.csr.CsrWorkAreaRightView;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrChannelRedirectWindow;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;
import com.jiangyifen.ec2.ui.csr.workarea.incoming.IncomingDialWindow;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutMonitor;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemStatus;
import com.jiangyifen.ec2.ui.mgr.tabsheet.AutoDialout;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrTabSheet;
import com.jiangyifen.ec2.utils.NTPClient;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.Application;
import com.vaadin.ui.VerticalLayout;

/**
 * 需要更新界面的组件 实时更新用户的组件
 */
public class ShareData {
	// //系统过期标示
	// public static volatile Boolean isOutdate=false;
	// //系统即将到期提醒
	// public static volatile Integer outdateWarnDay=1000;

	// 日志工具
	private final static Logger logger = LoggerFactory.getLogger(ShareData.class);

	// ****************************** 用户的全局信息 ********************************//

	/**
	 * Long 用户的 ID 号 Application vaadin 的app
	 */
	public static Map<Long, Application> userToApp = new ConcurrentHashMap<Long, Application>();

	/**
	 * 使用Map<Long, HttpSession> 存的是用户的id 与用户的session对应关系，用此来实现更新组件等需求
	 */
	public static Map<Long, HttpSession> userToSession = new ConcurrentHashMap<Long, HttpSession>();

	/**
	 * 使用Map<String, List<String>> 存储用户当前使用的分机 与 正处于‘呼叫等待状态’并且等的是当前用户的所有客户对应的通道信息
	 */
	public static Map<String, List<String>> userExtenToHoldOnCallerChannels = new ConcurrentHashMap<String, List<String>>();

	// ****************************** 界面组件 ********************************//

	/**
	 * Long 用户的 ID 号 CsrToolBar 需要更新标题的"我的通知"按钮 所存放的工具栏
	 */
	public static Map<Long, CsrToolBar> csrToToolBar = new ConcurrentHashMap<Long, CsrToolBar>();

	/**
	 * Long 用户的 ID 号 CsrStatuInfoShow 这是需要更新的用户状态信息界面 状态栏
	 */
	public static Map<Long, CsrStatusBar> csrToStatusBar = new ConcurrentHashMap<Long, CsrStatusBar>();

	/**
	 * CSR 用户界面中的 电话转接界面
	 */
	public static Map<Long, CsrChannelRedirectWindow> csrToDialRedirectWindow = new ConcurrentHashMap<Long, CsrChannelRedirectWindow>();

	/**
	 * CSR 用户界面的CsrWorkAreaRightView
	 */
	public static Map<Long, CsrWorkAreaRightView> csrToWorkAreaRightView = new ConcurrentHashMap<Long, CsrWorkAreaRightView>();

	/**
	 * Long 用户的 ID 号 userToCurrentTab csr 的当前所处的Tab 页
	 */
	public static Map<Long, VerticalLayout> csrToCurrentTab = new ConcurrentHashMap<Long, VerticalLayout>();

	/**
	 * Long 用户的 ID 号 IncomingDialWindow 呼入时需要显示的弹窗
	 */
	public static Map<Long, IncomingDialWindow> csrToIncomingDialWindow = new ConcurrentHashMap<Long, IncomingDialWindow>();

	// **************************** 管理员对应的自动外呼监控页面
	// *******************************//
	/**
	 * Long 用户的 ID 号 AutoDialoutMonitorWindow 自动外呼的表格组件
	 */
	public static Map<Long, AutoDialoutMonitor> mgrToAutoDialoutMonitor = new ConcurrentHashMap<Long, AutoDialoutMonitor>();

	/**
	 * Long 用户的 ID 号 AutoDialout自动外呼的表格组件
	 */
	public static Map<Long, AutoDialout> mgrToAutoDialout = new ConcurrentHashMap<Long, AutoDialout>();

	/**
	 * Long 用户的 ID 号 MgrTabSheet 管理员的主界面
	 */
	public static Map<Long, MgrTabSheet> mgrToTabSheet = new ConcurrentHashMap<Long, MgrTabSheet>();

	/**
	 * String 一通电话的UniqueId,QueueRequestDetail 队列电话详单
	 */
	public static Map<String, QueueRequestDetail> queueRequestDetailMap = new ConcurrentHashMap<String, QueueRequestDetail>();

	/**
	 * 系统信息
	 */
	public static Map<Long, SystemStatus> systemStatusMap = new ConcurrentHashMap<Long, SystemStatus>();

	/**
	 * 保存会议室对应的发起者的相关信息 Map<会议室号，第一个进入会议发起者的通道信息> 如 Map<800001,
	 * MeettingOriginatorInfo>
	 */
	public static Map<String, MeettingRoomFirstJoinMemberInfo> meettingToFirstJoinMemberMap = new ConcurrentHashMap<String, MeettingRoomFirstJoinMemberInfo>();
	/**
	 * Map<会议室号，ConcurrentHashMap<会议室成员的通道, 成员详情记录信息>>
	 */
	public static Map<String, ConcurrentHashMap<String, MeettingDetailRecord>> meetingToMemberRecords = new ConcurrentHashMap<String, ConcurrentHashMap<String, MeettingDetailRecord>>();
	public static Map<String, Long> meettingRoomExtenToMgrIdMap = new ConcurrentHashMap<String, Long>();

	// ****************************** 所有域唯一的对应关系
	// Start********************************//
	// ******用户、分机、外线 Start (域) ***********//
	public static Map<String, Long> extenToUser = new ConcurrentHashMap<String, Long>();
	public static Map<Long, String> userToExten = new ConcurrentHashMap<Long, String>();
	public static Map<String, Long> extenToDomain = new ConcurrentHashMap<String, Long>();
	public static Map<Long, Long> userToDomain = new ConcurrentHashMap<Long, Long>();
	public static Map<Long, Long> userToDepartment = new ConcurrentHashMap<Long, Long>();
	public static Map<String, String> extenToDynamicOutline = new ConcurrentHashMap<String, String>();
	public static Map<String, String> extenToStaticOutline = new ConcurrentHashMap<String, String>();
	public static Map<String, Set<SipConfig>> outlinePoolToOutline = new ConcurrentHashMap<String, Set<SipConfig>>();
	// public static Map<Long,ConcurrentHashMap<String,String>>
	// domainToEmpnoToExten = new
	// ConcurrentHashMap<Long,ConcurrentHashMap<String,String>>();

	// ******项目、队列、外线 Start ***********//
	public static Map<Long, String> projectToQueue = new ConcurrentHashMap<Long, String>();
	public static Map<Long, String> projectToOutline = new ConcurrentHashMap<Long, String>();
	public static Map<String, Long> outlineToProject = new ConcurrentHashMap<String, Long>();

	// ********************** Peername、Channel、ChannelSession
	// Start***********************************//
	// 描述分机当前有几路通话
	public static Map<String, Set<String>> peernameAndChannels = new ConcurrentHashMap<String, Set<String>>();
	public static Map<String, ChannelSession> channelAndChannelSession = new ConcurrentHashMap<String, ChannelSession>();
	public static List<StatusEvent> statusEvents = new ArrayList<StatusEvent>();
	// Map<channel, ChannelLifeCycle>
	public static Map<String, ChannelLifeCycle> channelToChannelLifeCycle = new ConcurrentHashMap<String, ChannelLifeCycle>();
	// Map<主叫号码，ChannelLifeCycle>【用于电话漏接日志的创建】
	public static Map<String, ChannelLifeCycle> callerNumToChannelLifeCycle = new ConcurrentHashMap<String, ChannelLifeCycle>();

	// jrh 存储指定域下，呼入客户与其发起的呼叫时所调用的AgiRequest
	// 的对应关系（只针对呼入，一般情况下一个呼入号码只会对应一条通道信息，以后如果是外线呼入Ec 就会出现问题）
	// Map<域的id, ConcurrentHashMap<主叫号码，呼入信息>>
	// 如Map<1,ConcurrentHashMap<"13816760365", 呼入信息>>
	public static Map<Long, ConcurrentHashMap<String, IncomingDialInfo>> domainToIncomingDialInfoMap = new ConcurrentHashMap<Long, ConcurrentHashMap<String, IncomingDialInfo>>();

	// ****************************** 用户呼入时找人 经常变化 Start
	// **********************************//
	/**
	 * 在项目点停止、暂停时并没有 移除分机和项目的对应关系
	 */
	public static Map<String, Long> extenToProject = new ConcurrentHashMap<String, Long>();

	// ****************************** 加载初始化 Start
	// **********************************//
	// jrh 按域设呼入置黑名单
	public static Map<Long, List<String>> domainToIncomingBlacklist = new ConcurrentHashMap<Long, List<String>>();
	// jrh 按域设置呼出黑名单
	public static Map<Long, List<String>> domainToOutgoingBlacklist = new ConcurrentHashMap<Long, List<String>>();
	// jrh 按外线设置呼入黑名单Map<外线Id,呼入黑名单集合>
	public static Map<Long, List<String>> outlineToIncomingBlacklist = new ConcurrentHashMap<Long, List<String>>();
	// jrh 按外线设置呼出黑名单Map<外线Id,呼出黑名单集合>
	public static Map<Long, List<String>> outlineToOutgoingBlacklist = new ConcurrentHashMap<Long, List<String>>();

	public static List<Domain> domainList = new ArrayList<Domain>();
	public static Map<Long, String> domainToDefaultOutline = new ConcurrentHashMap<Long, String>();
	public static Map<Long, String> domainToDefaultQueue = new ConcurrentHashMap<Long, String>();
	public static Map<String, ExtenStatus> extenStatusMap = new ConcurrentHashMap<String, ExtenStatus>();
	public static Map<String, List<String>> queue2Members = new ConcurrentHashMap<String, List<String>>();

	// ****************************** 外线和分机的并发统计 Start
	// **********************************//
	public static Map<Long, List<String>> domainToExts = new ConcurrentHashMap<Long, List<String>>();
	public static Map<Long, List<String>> domainToOutlines = new ConcurrentHashMap<Long, List<String>>();
	public static Map<Long, ConcurrentHashMap<String, Boolean>> domainToConfigs = new ConcurrentHashMap<Long, ConcurrentHashMap<String, Boolean>>();

	// ****************************** 后来添加
	// *************************************************//
	/**
	 * 在Bridge时添加，在记录CDR的时候进行移除操作
	 */
	public static Map<String, BridgetimeInfo> channelToBridgetime = new ConcurrentHashMap<String, BridgetimeInfo>();

	/**
	 * 在Bridge时添加，在记录CDR的时候进行移除操作 Map<String uniqueId, String fileName>
	 */
	public static Map<String, String> recordFileName = new ConcurrentHashMap<String, String>();

	/**
	 * 存储地区和区号的对应关系，为了减轻数据库压力，只加载一次
	 */
	public static List<MobileAreacode> areacodeList = new ArrayList<MobileAreacode>();
	public static Map<String, MobileAreacode> areacodeMap = new ConcurrentHashMap<String, MobileAreacode>(); // 回显使用

	/**********************************
	 * ivr 内存维护区 开始
	 ***************************************/

	// Map<IVRMenu 的编号， IVRMenu对象>
	public static Map<Long, IVRMenu> ivrMenusMap = new ConcurrentHashMap<Long, IVRMenu>();

	// Map<外线编号， ArrayList<OutlineToIvrLink对象>>
	public static Map<Long, ArrayList<OutlineToIvrLink>> outlineIdToIvrLinkMap = new ConcurrentHashMap<Long, ArrayList<OutlineToIvrLink>>();

	/**********************************
	 * ivr 内存维护区 结束
	 ***************************************/

	/**
	 * 在类加载时初始化的集合
	 */
	static {
//		BlackListItemService blackListItemService = SpringContextHolder.getBean("blackListItemService");
		SipConfigService sipConfigService = SpringContextHolder.getBean("sipConfigService");
		DomainService domainService = SpringContextHolder.getBean("domainService");
		QueueService queueService = SpringContextHolder.getBean("queueService");
		CommonService commonService = SpringContextHolder.getBean("commonService");
		MarketingProjectService marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
//		UserExtenPersistService userExtenPersistService = SpringContextHolder.getBean("userExtenPersistService");
		Ec2ConfigurationService ec2ConfigurationService = SpringContextHolder.getBean("ec2ConfigurationService");
		Phone2PhoneSettingService phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
		IvrMenuService ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
		OutlineToIvrLinkService outlineToIvrLinkService = SpringContextHolder.getBean("outlineToIvrLinkService");
		OutlinePoolService outlinePoolService = SpringContextHolder.getBean("outlinePoolService");
		OutlinePoolOutlineLinkService outlinePoolOutlineLinkService = SpringContextHolder
				.getBean("outlinePoolOutlineLinkService");

		// 加载所有域
		domainList = domainService.getAll();
		// 暂时不考虑不同域问题
		loadAreaPostcodeList(commonService);
		// 加载项目
		loadRunningProjectRelation(marketingProjectService);

		// 加载黑名单
		for (Domain domain : domainList) {
			loadDomainDefautQueue(domain, queueService);
			loadDefaultOutline(domain, sipConfigService);
			List<SipConfig> allOutlineList = loadDomainOutlines(domain, sipConfigService);
			loadDomainExts(domain, sipConfigService);
			loadDomain2Configs(domain, ec2ConfigurationService);
//			loadBlackList(domain, allOutlineList, blackListItemService);

			loadPhone2PhoneSchedulers(domain, phone2PhoneSettingService);
			// 项目--队列 项目--外线 外线--项目

			loadIvrMenus(domain, ivrMenuService);

			loadOutlineToIvrLinks(domain, outlineToIvrLinkService);

			// JRH 2015-03-19
			GlobalSettingScheduler.getSingleton().startSchedulerByDomain(domain);
			Phone2PhoneScheduler.getSingleton().startSchedulerByDomain(domain);
		}

//		loadAllUserExtenRelation(userExtenPersistService);

		// TODO
		initWD();
		
		loadOutlinePoolToOutline(outlinePoolService, outlinePoolOutlineLinkService, sipConfigService);

		// 检查asterisk channel 残留的问题，即导致软电话无法继续拨打电话[目前只对特定用户开启]
		if (GlobalVariable.mac_asterisk_channel_remnant.equals(GlobalData.MAC_ADDRESS)) {
			removeBugFile();
		}
	}

	private static void loadOutlinePoolToOutline(OutlinePoolService outlinePoolService, OutlinePoolOutlineLinkService outlinePoolOutlineLinkService, SipConfigService sipConfigService) {
		if(domainList != null && domainList.size() > 0) {
			for(Domain domain : domainList) {
				List<OutlinePool> outlinePools = outlinePoolService.getAllByDomain(domain);
				Set<SipConfig> sipConfigs = null;
				if(outlinePools != null && outlinePools.size() > 0) {
					for(OutlinePool outlinePoolTmp : outlinePools) {
						List<OutlinePoolOutlineLink> links = outlinePoolOutlineLinkService.getAllByPoolId(outlinePoolTmp.getId());
						if(links != null && links.size() > 0) {
							sipConfigs = new HashSet<SipConfig>();
							for(OutlinePoolOutlineLink outlineLink : links) {
								SipConfig sipConfig = sipConfigService.get(outlineLink.getOutlineId());
								if(sipConfig != null) {
									sipConfigs.add(sipConfig);
								}
							}
							if(sipConfigs.size() > 0) {
								outlinePoolToOutline.put(outlinePoolTmp.getPoolNum(), sipConfigs);
							}
						}
					}
				}
			}
		}
	}

	// TODO 到时候记得删除它，如果以后不要了，就可以删掉他
	private static void removeBugFile() {
		try {
			Runtime.getRuntime().exec(
					new String[] { "/bin/sh", "-c", "echo ''> /opt/apache-tomcat-7.0.32/logs/bug_channelRemnant.log" });
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("jrh 启动时删除用于检测asterisk channel 残留问题的日志文件失败--->" + e.getMessage(), e);
		}
	}

	// *************************************加载初始化的方法*****************************************************//

	// 加载黑名单
	private static void loadBlackList(Domain domain, List<SipConfig> allOutlineList,
			BlackListItemService blackListItemService) {
		Long domainId = domain.getId();
		// 加载域范围内的黑名单
		domainToIncomingBlacklist.put(domainId,
				blackListItemService.getAllPhoneNumsByDirection(BlackListItem.TYPE_INCOMING, domain));
		domainToOutgoingBlacklist.put(domainId,
				blackListItemService.getAllPhoneNumsByDirection(BlackListItem.TYPE_OUTGOING, domain));
		// 加载外线范围内的黑名单
		for (SipConfig outline : allOutlineList) {
			Long outlineId = outline.getId();
			outlineToIncomingBlacklist.put(outlineId, blackListItemService.getAllPhoneNumsByOutlineUsername(outlineId,
					BlackListItem.TYPE_INCOMING, domainId));
			outlineToOutgoingBlacklist.put(outlineId, blackListItemService.getAllPhoneNumsByOutlineUsername(outlineId,
					BlackListItem.TYPE_OUTGOING, domainId));
		}
	}

	// 加载地区区号集合
	@SuppressWarnings("unchecked")
	private static void loadAreaPostcodeList(CommonService commonService) {
		String nativeSql = "select mobilearea,areacode from ec2_mobileloc group by mobilearea,areacode order by mobilearea";
		List<Object[]> resultList = (List<Object[]>) commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
		for (Object[] oneResult : resultList) {
			String mobileArea = (String) oneResult[0];
			String areaCode = (String) oneResult[1];
			MobileAreacode mobileAreacode = new MobileAreacode(mobileArea, areaCode);
			areacodeList.add(mobileAreacode);
			areacodeMap.put(areaCode, mobileAreacode);
		}
	}

	/**
	 * 项目--队列 项目--外线 外线--项目
	 * 
	 * @param queueService
	 *            不区分域
	 */
	private static void loadRunningProjectRelation(MarketingProjectService marketingProjectService) {
		List<MarketingProject> projectList = marketingProjectService.getAll();
		for (MarketingProject project : projectList) {
			// 如果项目处于运行状态
			if (MarketingProjectStatus.RUNNING.equals(project.getMarketingProjectStatus())) {
				// 项目和队列
				if (project.getQueue() != null) {
					projectToQueue.put(project.getId(), project.getQueue().getName());
				}
				// 项目和外线
				SipConfig projectSip = project.getSip();
				if (projectSip != null) {
					projectToOutline.put(project.getId(), projectSip.getName());
					outlineToProject.put(projectSip.getName(), project.getId());
				}
			}
		}
	}

	/**
	 * 加载指定域的默认队列
	 * 
	 * @param domain
	 * @param queueService
	 */
	private static void loadDomainDefautQueue(Domain domain, QueueService queueService) {
		Queue defaultQueue = queueService.getDefaultQueueByDomain(domain.getId());
		if (defaultQueue != null) {
			domainToDefaultQueue.put(domain.getId(), defaultQueue.getName());
		}
	}

	// 加载默认的外线
	private static void loadDefaultOutline(Domain domain, SipConfigService sipConfigService) {
		SipConfig defaultOutline = sipConfigService.getDefaultOutlineByDomain(domain);
		if (defaultOutline != null) {
			domainToDefaultOutline.put(domain.getId(), defaultOutline.getName());
		}
	}

	// 加载域内所有的外线
	private static List<SipConfig> loadDomainOutlines(Domain domain, SipConfigService sipConfigService) {
		// 添加指定域包含的所有外线
		List<SipConfig> allOutlineList = sipConfigService.getAllOutlinesByDomain(domain);
		List<String> allOutlines = new ArrayList<String>();
		for (SipConfig sipConfig : allOutlineList) {
			allOutlines.add(sipConfig.getName());
		}
		domainToOutlines.put(domain.getId(), allOutlines);
		return allOutlineList;
	}

	// 加载域内所有的分机
	private static void loadDomainExts(Domain domain, SipConfigService sipConfigService) {
		// 添加指定域包含的所有分机
		List<SipConfig> allExtList = sipConfigService.getAllExtsByDomain(domain);
		List<String> allExts = new ArrayList<String>();
		for (SipConfig sipConfig : allExtList) {
			allExts.add(sipConfig.getName());
		}
		domainToExts.put(domain.getId(), allExts);
	}

	/**
	 * jrh 加载整个域的配置文件
	 * 
	 * @param domain
	 * @param ec2ConfigurationService
	 */
	private static void loadDomain2Configs(Domain domain, Ec2ConfigurationService ec2ConfigurationService) {
		ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<String, Boolean>();
		List<Ec2Configuration> configs = ec2ConfigurationService.getAllSpecialConfigsByDomain(domain);
		for (Ec2Configuration config : configs) {
			map.put(config.getKey(), config.getValue());
		}
		domainToConfigs.put(domain.getId(), map);
	}

	// *************************************加载初始化的方法*****************************************************//

	/**
	 * jrh 为isStartedRedirect = true 的外转外配置项，创建定时任务
	 */
	private static void loadPhone2PhoneSchedulers(Domain domain, Phone2PhoneSettingService phone2PhoneSettingService) {
		// 取出全局配置
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domain.getId());
		if (globalSetting == null) {
			return;
		}

		// 判断全局外转外的转接方式，如果是固定电话转接，则不需要考虑自定义的设置
		Boolean isSpecifiedPhones = globalSetting.getIsSpecifiedPhones();
		if (isSpecifiedPhones) {
			if (globalSetting.getIsStartedRedirect()) {
				phone2PhoneSettingService.createP2PScheduler(globalSetting);
			}
		} else {
			Boolean isLicensed2Csr = globalSetting.getIsLicensed2Csr();
			Set<User> currentCsrs = globalSetting.getSpecifiedCsrs();
			List<Long> currentCsrIds = new ArrayList<Long>();
			for (User csr : currentCsrs) {
				currentCsrIds.add(csr.getId());
			}

			if (isLicensed2Csr) { // 如果话务员持有权限，则获取所有已经开启的，为全局外转外控制，以及不在管理员控制范围内的话务员自定义外转外创建定定时任务
				// 获取所有处于开启状态的外转外配置
				List<Phone2PhoneSetting> p2pSettings = phone2PhoneSettingService
						.getAllStartedSettingsByDomain(domain.getId());
				for (Phone2PhoneSetting p2pSetting : p2pSettings) {
					Long creatorId = p2pSetting.getCreator().getId();
					if (!currentCsrIds.contains(creatorId)) { // 因为管理员对选定的话务员有绝对管理权
						phone2PhoneSettingService.createP2PScheduler(p2pSetting);
					}
				}
			} else if (globalSetting.getIsStartedRedirect()) { // 如果没有授权给话务员，则判断全局外转外是否开启，如果开启，则创建定时任务
				phone2PhoneSettingService.createP2PScheduler(globalSetting);
			}
		}
	}

	// *************************************加载初始化的方法*****************************************************//

	// 加载IVRMenu
	private static void loadIvrMenus(Domain domain, IvrMenuService ivrMenuService) {
		List<IVRMenu> ivrMenus = ivrMenuService.getAllByDomain(domain.getId(), IVRMenuType.customize);
		for (IVRMenu menu : ivrMenus) {
			ivrMenusMap.put(menu.getId(), menu);
		}
	}

	// 加载外线与IVR 所有可用的对应关系
	private static void loadOutlineToIvrLinks(Domain domain, OutlineToIvrLinkService outlineToIvrLinkService) {
		List<OutlineToIvrLink> links = outlineToIvrLinkService.getAllByDomain(domain.getId());
		for (OutlineToIvrLink link : links) {
			Long outlineId = link.getOutlineId();
			ArrayList<OutlineToIvrLink> linkList = outlineIdToIvrLinkMap.get(outlineId);
			if (linkList == null) {
				linkList = new ArrayList<OutlineToIvrLink>();
			}
			linkList.add(link);
			outlineIdToIvrLinkMap.put(outlineId, linkList);
		}
	}

	/**
	 * 加载停止程序前的对应关系
	 * 
	 * @param userExtenPersistService
	 */
	private static void loadAllUserExtenRelation(UserExtenPersistService userExtenPersistService) {

		boolean isLicenseValid = false;

		// 如果系统过期显示提示信息
		try {
			Map<String, String> licenseMap = LicenseManager.licenseValidate();
			String validateResult = licenseMap.get(LicenseManager.LICENSE_VALIDATE_RESULT);
			if (LicenseManager.LICENSE_VALID.equals(validateResult)) {
				String licensedDate = licenseMap.get(LicenseManager.LICENSE_DATE);
				Date stopDate = LicenseManager.simpleDateFormat.parse(licensedDate);
				Long times = stopDate.getTime() - new Date().getTime();
				int outdateWarnDay = (int) (times / (24 * 3600 * 1000));
				if (outdateWarnDay >= 0) {
					isLicenseValid = true;
				}
			}
		} catch (ParseException e1) {
			logger.error("JRH ---> License信息验证时出现异常，解析日期出现错误，将导致免登陆功能失效！" + e1.getMessage(), e1);
		}

		if (isLicenseValid) { // 只有在License 是正确的才执行免登陆功能
			List<UserExtenPersist> userToExtenPersistList = userExtenPersistService.getAll();
			UserLoginService userLoginService = SpringContextHolder.getBean("userLoginService");
			UserService userService = SpringContextHolder.getBean("userService");

			for (UserExtenPersist userExtenPersist : userToExtenPersistList) {
				// jrh 既然已经能知道重启tomcat 前登陆了哪些人，那么就可以先做登出[为了保证信息的完善，如用户登陆日志等]，再做登陆 2013-09-13
				User csr = userService.get(userExtenPersist.getUserId());
				if (csr == null) {
					userExtenPersistService.deleteUserExtenPersistById(userExtenPersist.getId());
				} else {
					userLoginService.logout(csr.getId(), csr.getDomain().getId(), csr.getUsername(), csr.getEmpNo(),
							userExtenPersist.getExten(), 0L, true);
					try {
						userLoginService.login(csr.getUsername(), csr.getPassword(), userExtenPersist.getExten(),
								"0.0.0.0", RoleType.csr);
					} catch (Exception e) {
						// 出现异常，导致这个异常的原因是：分机不存在，即用户使用的分机目前已经是其他租户的了【租户添加一个分机800010，最后又删除了，而之后另一个租户有创建了一个分机800010】
						logger.error(
								"jrh RuntimeException: cause by user use a exten which belong to other domain. 如果 出现异常，导致这个异常的原因是：分机不存在，即用户使用的分机目前已经是其他租户的了【租户添加一个分机800010，最后又删除了，而之后另一个租户有创建了一个分机800010】");
					}
				}
			}
		} else {
			logger.warn("JRH ---> License 已失效，可能是授权过期，将导致免登陆功能失效！请更新License授权信息后，重启系统，以便执行免登陆功能！");
		}

	}

	/**
	 * 每小时同步时间即可
	 */
	private static void initWD() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Date now = NTPClient.getNtpDate();
						// chb 如果获取ntp时间失败，则获取本地时间
						if (now == null) {
							now = new Date();
						}
					} catch (Exception e) {
						logger.warn("chb: Ntp update failed", e.getMessage());
					}

					try {
						// 休眠一小时
						Thread.sleep(60 * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	// private static void initWD() {
	// final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	// try {
	//// TODO edit date
	// String stopDateStr = GlobalVariable.EC2_EXPIRED_DATE;
	// if(stopDateStr == null || "".equals(stopDateStr)) {
	// stopDateStr = "1970-01-01";
	// }
	// final Date stopDate = sdf.parse(stopDateStr);
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// while (true) {
	// try {
	// Date now = NTPClient.getNtpDate();
	// //chb 如果获取ntp时间失败，则获取本地时间
	// if(now==null){
	// now=new Date();
	// }
	//
	// //过期提醒设置
	// if (now != null) {
	// Long times=stopDate.getTime()-now.getTime();
	// outdateWarnDay=(int)(times/(24*3600*1000));
	// }
	//
	// //过期停用设置
	// if (now != null && now.after(stopDate)) {
	// logger.info("对不起，您的系统已到期（截止日期为"+stopDate+" 00:00:00）");
	// isOutdate=true;
	//// chb 因为停掉Asterisk会影响系统的正常运行，输入大量日志，所以不停止asterisk，只是阻止登陆，阻止打电话
	//// Runtime.getRuntime().exec("service asterisk stop");
	//// Runtime.getRuntime().exec("killall asterisk");
	//// Runtime.getRuntime().exec("killall java");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// try {
	// // 休眠一小时
	// Thread.sleep(60 * 60 * 1000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }).start();
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private static void initWD2MAC() {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// while (true) {
	// try {
	// String currentEth0Mac = getEth0MacAddress();
	// boolean macChanged = currentEth0Mac.equalsIgnoreCase(GlobalData.MAC_ADDRESS);
	// if (!macChanged) {
	// logger.info("对不起，您的服务器因当前eth0 的 MAC
	// 地址为["+currentEth0Mac+"]与配置Mac不相符而被终止呼叫中心服务(eth0 配置的 MAC
	// 为["+GlobalData.MAC_ADDRESS+"])");
	// Runtime.getRuntime().exec("service asterisk stop");
	// Runtime.getRuntime().exec("killall asterisk");
	// Runtime.getRuntime().exec("killall java");
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// try {
	// // 休眠一小时
	// Thread.sleep(60 * 60 * 1000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }).start();
	// }

	/**
	 * Linux下获取ETH0的MAC地址
	 * 
	 * @return
	 */
	public static String getEth0MacAddress() {
		String macString = "";
		try {
			NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
			if (networkInterface == null) {
				networkInterface = NetworkInterface.getByName("em1");
			}
			if (networkInterface != null) {
				byte[] mac = networkInterface.getHardwareAddress();
				if (mac == null)
					return macString;
				StringBuilder builder = new StringBuilder();
				for (byte b : mac) {
					builder.append(hexByte(b));
					builder.append(":");
				}
				builder.deleteCharAt(builder.length() - 1);
				macString = builder.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LoggerFactory.getLogger(ShareData.class).error("ShareData 获取 eth0 Mac 地址时出错 --> " + e.getMessage(), e);
		}
		return macString;
	}

	/**
	 * 按两位 16进制返回
	 * 
	 * @param b
	 * @return
	 */
	private static String hexByte(byte b) {
		return String.format("%02x", b);
	}

}
