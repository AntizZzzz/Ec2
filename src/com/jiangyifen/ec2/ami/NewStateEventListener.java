package com.jiangyifen.ec2.ami;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.NewStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.entity.ConcurrentStatics;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.globaldata.WuRuiShareData;
import com.jiangyifen.ec2.service.csr.PopupIncomingWindowService;
import com.jiangyifen.ec2.service.eaoservice.ConcurrentStaticsService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.PopupIncomingVo;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 监听NewStateEvent事件 
 * @author chb
 */
public class NewStateEventListener extends AbstractManagerEventListener {
	/**
	 * 并发统计所有变量
	 */
	private static String ALL_SIP_NAME="allexten";
	private static Long STATICS_TIME_STEP=60*1000L;
	private Long lastNewStateEventReceiveTime =null;
	
	// 日志工具
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	//key 为外线名                    value为数量统计   chenh 去掉并发统计的内容
	private Map<String,ConcurrentStatics> concurrentStatisticsMap=new HashMap<String, ConcurrentStatics>();// 应该是不被其它线程访问
	private ConcurrentStaticsService concurrentStaticsService;

	public NewStateEventListener() {
		concurrentStaticsService=SpringContextHolder.getBean("concurrentStaticsService");
		//记录系统启动时间的整数值
		lastNewStateEventReceiveTime=System.currentTimeMillis();
		lastNewStateEventReceiveTime=lastNewStateEventReceiveTime-lastNewStateEventReceiveTime%STATICS_TIME_STEP;
	}
	
	/**
	 * 监听NewStateEvent事件
	 */
	@Override
	protected void handleEvent(NewStateEvent event) {
		String uniqueId = event.getUniqueId();
		String channel = event.getChannel();
		String callerName = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
		
//TODO
//		System.out.println();
//		System.out.println("================================================================================================================================================");
//		System.out.println("NewStateEvent ===>  "+event);
//		System.out.println();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//		System.out.println("NewStateEvent channel--->  "+ channel);
//		System.out.println("NewStateEvent ChannelStateDesc--->  "+ event.getChannelStateDesc());
//		System.out.println("NewStateEvent event.getDateReceived()--->  "+ sdf.format(event.getDateReceived()));
		
		// 往peernamdAndChannel放数据
		Set<String> channelSet = ShareData.peernameAndChannels.get(callerName);
		if (channelSet == null) {
			channelSet = new HashSet<String>();
			ShareData.peernameAndChannels.put(callerName, channelSet);
		}
		this.addChannel(channelSet, channel);
		
//TODO
		loggerInfoForAsteriskChannelRemant(event);
		
		//统计并发数量 
		//2014-5-27 chenhb 去掉并发统计功能  
//		this.processConcurrentStatistics();
		
		// 往channelAndChannelSession放数据
		ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
		if (channelSession == null) {
			channelSession = new ChannelSession();
		}

		String status=event.getChannelStateDesc();
		String connectedlinenum = event.getConnectedlinenum();	// 可以用来判断是呼入还是呼出，如果为null 则是呼出，否则为呼入
		this.updateChannelLifeCycleInfo(event, channel, status);
		
		// 添加属性值到ChannelSession 中去
		this.addAttributesToChannelSession(channel, uniqueId,channelSession, status, connectedlinenum);

		// 改变界面组件的状态（如弹屏，用户的通话状态图标等信息）
		this.updateUiStatus(callerName, channelSession, status, connectedlinenum);
		
		// 如果分机处于Ringing 状态，则执行弹屏
		// if(!StringUtils.isEmpty(callerName)&&"Ringing".equals(status)&&(callerName.matches("^8\\d{5}$"))){
		if(!StringUtils.isEmpty(callerName)&&"Ringing".equals(status)&&(callerName.matches("^6\\d{4}$"))){		// TODO 武睿定制, 分机号改为 6 开头五位数
			/**
			 * @changelog 2014-6-18 上午9:46:45 chenhb <p>description: 
			 * 如果发现 voicemailExten 没有设置，则设置此参数 为振铃的分机，
			 * 这样来保证参数为队列中首次振铃的分机</p>
			 * 
			 * 呼入振铃则设置相应的voicemail变量
			 * @changelog 2014-6-19 下午3:47:59 chenhb <p>description: 取不到对端channel，放弃</p>
			 */
//			//获取参数
//			GetVarAction getVarAction = new GetVarAction(channel, "voicemailUserid");
//			GetVarResponse response =(GetVarResponse)AmiManagerThread.sendResponseAction(getVarAction);
//			String value = null;
//			if(response!=null){
//				value=response.getValue();
//			}
//			logger.info("chenhb: channel-->"+channel+" send getvaraction--> voicemailUserid:"+value);
//			
//			//如果参数为空，则设置参数，否则只是记录重复日志
//			if(StringUtils.isEmpty(value)){
//				Long userid = ShareData.extenToUser.get(callerName);
//				if(userid!=null){
//					SetVarAction setVarAction=new SetVarAction(channel, "voicemailUserid",  userid.toString());
//	                logger.info("chenhb: channel-->"+channel+" send setvaraction--> voicemailUserid:"+value);
//					AmiManagerThread.sendAction(setVarAction);
//				}else{
//					logger.warn("chenhb: userid is null for exten --> "+callerName);
//				}
//			}else{
//				logger.info("chenhb: channel-->"+channel+" voicemailUserid already set voicemailUserid:"+value);
//			}
			
			
			//如果connectedlinenum不为空(呼入)，且主叫不是分机，则调用呼入弹屏
			if(!StringUtils.isEmpty(connectedlinenum)){
				//chb added 20140117  TODO woke
				//chb changed 20140415  TODO meibao
				//改为美宝之家弹屏
//				MeibaoIncomingPop.incomingPop(true,callerName,connectedlinenum); //true 为调用（已经进行异常处理），false为不调用
			}
		}
	}

	/**
	 * jrh
	 *  更新通道生命周期的相关信息
	 * @param event		事件
	 * @param channel	通道
	 * @param status	状态
	 */
	private void updateChannelLifeCycleInfo(NewStateEvent event, String channel, String status) {
		if(channel.startsWith("AsyncGoto/SIP/")) {	// 如果是转接，被转接通道会生成一个新的通道 如：AsyncGoto/SIP/88860847041-00000016
			return ;
		}
		
		// jrh 保存通道的生命周期
		ChannelLifeCycle channelLifeCycle = ShareData.channelToChannelLifeCycle.get(channel);
		if(channelLifeCycle == null) {
			channelLifeCycle = new ChannelLifeCycle(channel);
			ShareData.channelToChannelLifeCycle.put(channel, channelLifeCycle);
		}
		
		if("Ring".equals(status)) {
			channelLifeCycle.setRingStateTime(event.getDateReceived());
			// 如果是主叫，则将主叫信息存放到内存中，以便在挂断时使用【用于电话漏接日志的创建】
			ShareData.callerNumToChannelLifeCycle.put(event.getCallerIdNum(), channelLifeCycle);
		} else if("Ringing".equals(status)) {
			channelLifeCycle.setRingingStateTime(event.getDateReceived());
		} else if("Up".equals(status)) {
			channelLifeCycle.setUpStateTime(event.getDateReceived());
		} else if("Down".equals(status)) {
			channelLifeCycle.setDownStateTime(event.getDateReceived());
		} else if("Dialing".equals(status)) {
			channelLifeCycle.setDialingStateTime(event.getDateReceived());
		} else if("Busy".equals(status)) {
			channelLifeCycle.setBusyStateTime(event.getDateReceived());
		} else if("Dialing Offhook".equals(status)) {
			channelLifeCycle.setDialingOffhookStateTime(event.getDateReceived());
		} else if("Rsrvd".equals(status)) {
			channelLifeCycle.setRsrvdStateTime(event.getDateReceived());
		} else if("OffHook".equals(status)) {
			channelLifeCycle.setOffHookStateTime(event.getDateReceived());
		}
		
		// 设置通道联系对象
		String connectedlinenum = channelLifeCycle.getConnectedlinenum();
		String connectedlinename = channelLifeCycle.getConnectedlinenum();
		if(connectedlinenum == null) {
			channelLifeCycle.setConnectedlinenum(event.getConnectedlinenum());
		}
		if(connectedlinename == null) {
			channelLifeCycle.setConnectedlinename(event.getConnectedlinename());
		}
	}

	/**
	 *  jrh 改变界面组件的状态（如弹屏，用户的通话状态图标等信息）
	 * @param callerName		当前通道对应的分机（把他当做分机号）
	 * @param ringingExtenChannelSession	通道对应的信息
	 * @param status			通道状态
	 * @param connectedlinenum	通道联系的另一方
	 */
	private void updateUiStatus(String callerName, ChannelSession ringingExtenChannelSession, String status, String connectedlinenum) {
		Long csrId= ShareData.extenToUser.get(callerName);
		
		// 用户没有登录
		if(csrId == null) {
			return;
		}
		
		// 只有在用户已经登陆的情况下才有更新组件的必要
		UserService userService=SpringContextHolder.getBean("userService");
		User currentCsr = userService.get(csrId);
		
		// 一般情况下，这里肯定成立
		if(currentCsr != null) {
			CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(currentCsr.getId());
			
			String callerNum = ringingExtenChannelSession.getConnectedlinenum();
			
			// 检查用户是否登陆了系统，如果是，则需要更新界面
			if(csrStatusBar != null) {
				// 修改Csr 用户状态栏中 通话状态的图标
				Set<String> channels = ShareData.peernameAndChannels.get(callerName);
				if (channels != null && channels.size() > 0) {
					csrStatusBar.updateCallStatus(1); // 通话状态
				} else {
					csrStatusBar.updateCallStatus(0); // 通话状态
				}
			}
			
			// 如果主叫号码和被叫号码都是分机，从系统呼出、表示为发送originateAction 时asterisk呼分机
			for(Long domainId : ShareData.domainToExts.keySet()) {
				for(String exten : ShareData.domainToExts.get(domainId)) {
					if(exten.equals(connectedlinenum)) {
						System.out.println("分机自己呼叫自己");
						return;
					}
				}
			}
			
			// 如果分机处于Ringing 状态，则执行弹屏
			if("Ringing".equals(status)) {
				SipConfig outlineObj = null;
				String vasOutline = "";
				
				try {
					ConcurrentHashMap<String, IncomingDialInfo> incomingCallerToDialInfos = ShareData.domainToIncomingDialInfoMap.get(currentCsr.getDomain().getId());
					if(incomingCallerToDialInfos != null) {
						IncomingDialInfo dialInfo = incomingCallerToDialInfos.get(callerNum);
						if(dialInfo != null) {
							vasOutline = dialInfo.getVasOutline();
							// System.err.println("用户ID: " + currentCsr.getId() + ", 用户名: " + currentCsr.getUsername() + ", 被叫外线: " + dialInfo.getVasOutline() + ", 被叫分机: " + callerName + ", 主叫号码: " + connectedlinenum + ", channelSession: " + ringingExtenChannelSession);
						}
					}
					
					if(incomingCallerToDialInfos != null) {
						IncomingDialInfo incomingDialInfo = incomingCallerToDialInfos.get(callerNum);
						if(incomingDialInfo != null) {
							SipConfigService sipConfigService = SpringContextHolder.getBean("sipConfigService");
							outlineObj = sipConfigService.getOutlineByOutlineName(vasOutline);
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(csrStatusBar != null) {
					if(outlineObj == null || (outlineObj != null && outlineObj.getIspopupWin())) {	// 如果外线不存在，或者外线存在并且外线配置为需要弹屏，则显示呼入弹窗
						PopupIncomingWindowService popupIncomingWindowService = SpringContextHolder.getBean("popupIncomingWindowService");
						popupIncomingWindowService.popupIncomingWindow(currentCsr, ringingExtenChannelSession);
						
						// 呼入弾屏时, 调用第三方系统接口推送数据
						/*if("true".equals(ExternalInterface.INCOMING_ELASTIC_SCREEN_INTERFACE_IS_OPEN)) {
							// 用户Id, 用户名, 被叫外面, 被叫分机, 主叫号码
							final String params = MessageFormat.format(ExternalInterface.INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS, currentCsr.getId(), currentCsr.getUsername(), vasOutline, callerName, connectedlinenum);
							new Thread(new Runnable() {
								@Override
								public void run() {
									HttpIfaceUtil.doPostRequest(ExternalInterface.INCOMING_ELASTIC_SCREEN_INTERFACE_URL, params);
								}
							}).start();
							
						}*/
					}
				}

				 if(outlineObj == null || (outlineObj != null && outlineObj.getIspopupWin())) {	// 如果外线不存在，或者外线存在并且外线配置为需要弹屏，则显示呼入弹窗
					/********************* 武睿定制开发, 呼入弾屏内存信息维护 START **********************/
					PopupIncomingVo popupIncomingVo = new PopupIncomingVo();
					popupIncomingVo.setOutline(vasOutline);
					popupIncomingVo.setCreateTime(new Date().getTime());
					popupIncomingVo.setPhoneNumber(connectedlinenum);
					popupIncomingVo.setExten(callerName);
					popupIncomingVo.setUsername(currentCsr.getUsername());
					WuRuiShareData.csrToPopupIncomingVoMap.put(currentCsr.getUsername(), popupIncomingVo);
					/********************* 武睿定制开发, 呼入弾屏内存信息维护 END **********************/
				 }
			}
			
		}
	}

	/**
	 * 处理并发的计数，并且向数据库中存储每分钟的并发数据
	 */
	@SuppressWarnings("unused")
	private void processConcurrentStatistics() {
		//现在的时间
		Long nowTimeMillis=System.currentTimeMillis();
		
		//看是不是向数据库中存储过数据，如果存储过则更新lastNewStateEventReceiveTime的时间
		Boolean isPersistData=false;
		
		//记录当前的每条外线和所有分机并发数量
		/**=================================== 外线统计 ================================================================**/
		//如果外线发生变化，则此处的记录依据内存中的ShareData中的外线发生相应变化，外线的并发统计中，外线的条数不会因外线的移除而减少
		for(Domain domain:ShareData.domainList){
			//如果域中没有外线，继续
			if(ShareData.domainToOutlines.get(domain.getId())==null){
				continue;
			}
			for(String outlineName:ShareData.domainToOutlines.get(domain.getId())){
				//取得此外线的并发数量
				Set<String> channels = ShareData.peernameAndChannels.get(outlineName);
				int count = 0;
				if(channels!=null){
					count=channels.size();
				}
				
				ConcurrentStatics concurrentStatics=concurrentStatisticsMap.get(outlineName);
				//如果内存中还没有此外线的并发记录，则添加记录，否则更新已经存在的记录的最大最小值
				if(concurrentStatics==null){
					concurrentStatics=new ConcurrentStatics();
					concurrentStatics.setPeer(outlineName);
					concurrentStatics.setDomain(domain);
					concurrentStatics.setType(ConcurrentStatics.OUTLINE);
					concurrentStatics.setMin(count);
					concurrentStatics.setMax(count);
					concurrentStatisticsMap.put(outlineName, concurrentStatics);
				}else{
					if(count<concurrentStatics.getMin()){ //小于最小
						concurrentStatics.setMin(count);
					}else if(count>concurrentStatics.getMax()){ //大于最大
						concurrentStatics.setMax(count);
					}else{
						//介于两者之间  Noop
					}
				}
				
				// 如果跨分钟
				if(nowTimeMillis-lastNewStateEventReceiveTime>=STATICS_TIME_STEP){
					Long overMinCount=(nowTimeMillis-lastNewStateEventReceiveTime)/(STATICS_TIME_STEP);
					// 向数据库中插入一分钟或者几分钟的数据
					for(Long i=1L;i<=overMinCount;i++){
						Date date=new Date(lastNewStateEventReceiveTime+i*STATICS_TIME_STEP);
						concurrentStatics.setDateTime(date);
						concurrentStaticsService.update(concurrentStatics);
					}
					// 将此并发数量设置为下一分钟的初始并发数量
					concurrentStatics.setMin(count);
					concurrentStatics.setMax(count);
					isPersistData=true;
				}
			}
		}
		
		/**=================================== 分机统计 ================================================================**/
		for(Domain domain:ShareData.domainList){
			//如果域中没有分机，继续
			if(ShareData.domainToOutlines.get(domain.getId())==null){
				continue;
			}
			//一个域内所有分机的并发总数计数
			int staticsAllCount=0;
			for(String extNo:ShareData.domainToExts.get(domain.getId())){
				//取得此分机的并发数量
				Set<String> channels = ShareData.peernameAndChannels.get(extNo);
				int count = 0;
				if(channels!=null){
					count=channels.size();
				}
				staticsAllCount+=count;
			}
			
			ConcurrentStatics concurrentStatics=concurrentStatisticsMap.get(ALL_SIP_NAME);
			//如果内存中还没有分机的并发记录，则添加记录，否则更新已经存在的记录的最大最小值
			if(concurrentStatics==null){
				concurrentStatics=new ConcurrentStatics();
				concurrentStatics.setPeer(ALL_SIP_NAME);
				concurrentStatics.setDomain(domain);
				concurrentStatics.setType(ConcurrentStatics.ALLSIP);
				concurrentStatics.setMin(staticsAllCount);
				concurrentStatics.setMax(staticsAllCount);
				concurrentStatisticsMap.put(ALL_SIP_NAME, concurrentStatics);
			}else{
				if(staticsAllCount<concurrentStatics.getMin()){ //小于最小
					concurrentStatics.setMin(staticsAllCount);
				}else if(staticsAllCount>concurrentStatics.getMax()){ //大于最大
					concurrentStatics.setMax(staticsAllCount);
				}else{
					//介于两者之间  Noop
				}
			}
			
			// 如果跨分钟
			if(nowTimeMillis-lastNewStateEventReceiveTime>=STATICS_TIME_STEP){
				Long overMinCount=(nowTimeMillis-lastNewStateEventReceiveTime)/STATICS_TIME_STEP;
				// 向数据库中插入一分钟或者几分钟的数据
				for(Long i=1L;i<=overMinCount;i++){
					Date date=new Date(lastNewStateEventReceiveTime+i*STATICS_TIME_STEP);
					concurrentStatics.setDateTime(date);
					concurrentStaticsService.update(concurrentStatics);
				}
				// 将此并发数量设置为下一分钟的初始并发数量
				concurrentStatics.setMin(staticsAllCount);
				concurrentStatics.setMax(staticsAllCount);
				isPersistData=true;
			}
		}
		
		//如果更新过数据，说明跨分钟，则更新时间
		if(isPersistData==true){
			//记录系统启动时间的整数值
			lastNewStateEventReceiveTime=nowTimeMillis-nowTimeMillis%STATICS_TIME_STEP;
		}
	}
	
	/**
	 * 线程安全地添加一个Channel
	 * @param channelSet
	 * @param channel
	 */
	private void addChannel(Set<String> channelSet, String channel) {
		synchronized (channelSet) {
			channelSet.add(channel);
		}
	}

	/**
	 * 往ChannelSession中添加属性
	 * @param channelAndChannelSession
	 * @param channel
	 * @param channelSession
	 * @param event
	 */
	private void addAttributesToChannelSession(String channel, String uniqueId,ChannelSession channelSession,String status, String connectedlinenum) {
		synchronized (ShareData.channelAndChannelSession) {
			channelSession.setStatus(status);
			channelSession.setChannel(channel);
			channelSession.setChannelUniqueId(uniqueId);
			channelSession.setConnectedlinenum(connectedlinenum);
			ShareData.channelAndChannelSession.put(channel, channelSession);
		}
	}
	
	/**
	 * 用于定位asterisk channel 残留的问题
	 * @param event
	 */
	private void loggerInfoForAsteriskChannelRemant(NewStateEvent event) {
		if(GlobalVariable.mac_asterisk_channel_remnant.equals(GlobalData.MAC_ADDRESS)) {
			StringBuffer strBf = new StringBuffer();
			strBf.append("jrh check channel remnant--> ");
			strBf.append("NewStateEventListener   - ");
			strBf.append("JrhAddByNewState   : ");
			strBf.append(" channel=");
			strBf.append(event.getChannel());
			strBf.append(", calleridname=");
			strBf.append(event.getCallerIdName());
			strBf.append(", calleridnum=");
			strBf.append(event.getCallerIdNum());
			strBf.append(", connectedlinenum=");
			strBf.append(event.getConnectedlinenum());
			strBf.append(", channelstatedesc=");
			strBf.append(event.getChannelStateDesc());
			strBf.append(", uniqueid=");
			strBf.append(event.getUniqueId());
			LOGGER.warn(strBf.toString());
		}
	}
	
}
