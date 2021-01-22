package com.jiangyifen.ec2.autodialout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.GetVarAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.SetVarAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.AutoDialout;
import com.jiangyifen.ec2.utils.AutoDialConfig;
import com.jiangyifen.ec2.utils.ShareDataElementUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 定期外呼资源消费线程 
 * <p>注意：</p>
 * <p>csr和队列应该随着用户的操作和CSR的登陆发生相应的变换</p>
 * <p>1、用户如果在某个AutoDialTask中，则用户登陆和登出应该刷新相应的  “进行中” 的自动外呼线程的用户和分机</p>
 * <p>2、如果管理员进行了添加或移除CSR的操作，则应该在ec2_user_queue表中添加相应的对应关系，
 * 		再操纵Asterisk将新添加的（处于登陆状态的用户）添加进动态队列，将移除的用户从自动外呼的队列中移除
 * </p>
 * @author chb
 * 

[autodialcounter]
exten => _x.,1,NoCDR()
exten => _x.,n,Set(${EXTEN}=${SHELL(echo "${CHANNELS(${EXTEN})})" | awk -F'SIP' '{print NF-1}')})
exten => _x.,n,Set(GLOBAL(${EXTEN})=${${EXTEN}:0:${MATH(${LEN(${extennums})}-1,int)}})
exten => _x.,n,HangUp()

 *
 */
public class ProjectResourceConsumer extends Thread{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private Long THREAD_SLEEP_TIME=0L;
	private volatile Boolean isLoop;
	private ProjectResourceProducer producerThread;
	private AutoDialoutTask autoDialoutTask;
	private AutoDialoutTaskService autoDialoutTaskService;
	private MarketingProjectTaskService marketingProjectTaskService;
	private SipConfigService sipConfigService;
	private String threadName;
	private User user;

	/**
	 * 放在Channel中
	 */
	private String callType;     //呼叫类型
	private String queueName;        //Task对应的队列信息
	private String domainIdStr;    //域Id的字符串
	private Long domainId;    //域Id
	/**
	 * 应该随着自动外呼或者项目的编辑改变的变量
	 */
	private List<String> extenList=new ArrayList<String>();
	private Set<User> users=new HashSet<User>(); 
	private MarketingProject marketingProject;
	private String outlineNumber;    //Task对应的外线信息
	private String soundFileStr; //语音文件字符串
	private volatile Boolean isAudioPlay;  //是否播放语音
	private volatile Boolean isSystemAjust;  //是否开启系统自动调整
	private volatile Boolean isSoundDialout;//是否是语音外呼
	private volatile Integer callLimit;   //外线的呼叫上限
	private volatile Double ratio;        //呼叫比率
	private volatile Double percentageDepth;  //用户指定的队列深度保持
	private volatile Integer staticExpectedCallers;
	
	/**
	 * 以项目Id为参数构造线程,并启动线程
	 * @param projectId
	 */
	public ProjectResourceConsumer(AutoDialoutTask autoDialoutTask,String producerThreadName) {
		autoDialoutTaskService=SpringContextHolder.getBean("autoDialoutTaskService");
		marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
		sipConfigService=SpringContextHolder.getBean("sipConfigService");
		
		//初始化消费线程的参数
		THREAD_SLEEP_TIME=Long.parseLong(AutoDialConfig.props.getProperty(AutoDialConfig.CONSUMER_SLEEP_TIME));	// 默认睡眠 5 秒
		Long consumerUserId=Long.parseLong(AutoDialConfig.props.getProperty(AutoDialConfig.CONSUMER_USERID));
		user=new User();
		user.setId(consumerUserId);
		
		this.threadName=AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId();
		this.setName(threadName);
		this.autoDialoutTask=autoDialoutTask;
		queueName=autoDialoutTask.getQueue().getName();
		domainIdStr=autoDialoutTask.getDomain().getId().toString();
		domainId=autoDialoutTask.getDomain().getId();
		callType=autoDialoutTask.getDialoutType();
		refreshEditChangeVariable();
		
		isLoop=true;
		//获取生产线程
		producerThread=(ProjectResourceProducer)AutoDialHolder.nameToThread.get(producerThreadName);
		this.setDaemon(true);
		AutoDialHolder.nameToThread.put(threadName, this);
		this.start();
	}

	/**
	 * 线程运行方法
	 */
	@Override
	public void run() {
logger.info("启动消费线程："+threadName);
		try {
			if(callType.equals(AutoDialoutTask.AUTO_DIALOUT)){
				startAutoDialout();
			}else if(callType.equals(AutoDialoutTask.SOUND_DIALOUT)){
				startSoundDialout();
			} 
			//chenhb chenaged 20141113 ***
			//外线正在使用的并发数量
			AmiManagerThread.sendAction(new SetVarAction(outlineNumber,""));
			// ***
		} catch (InterruptedException e) {
			logger.error("chb : 自动外呼或语音外呼消费线程睡眠异常,线程停止",e);
		}
		
			 
		AutoDialHolder.nameToThread.remove(threadName);
		
		//如果生产线程正在运行，则从生产线程中移除消费线程
		if(producerThread!=null&&producerThread.isAlive()){
			producerThread.removeDependConsumer(threadName);
		}
		
		//有可能多个管理员都在看同一个界面，所以更新多个管理员的界面
		autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.PAUSE);
		autoDialoutTaskService.update(autoDialoutTask);
		for(Long mgrId:ShareData.mgrToAutoDialout.keySet()){
			AutoDialout autoDialout=ShareData.mgrToAutoDialout.get(mgrId);
			autoDialout.updateTable(false);
		}
logger.info("停止消费线程:"+threadName);
	}
	
	/**
	 * 开始自动外呼
	 * @throws InterruptedException
	 */
	private void startAutoDialout() throws InterruptedException {
		 while(isLoop&&producerThread.getIsLoop()){
			 	 //chenhb chenaged 20141113 ***
				 if(!StringUtils.isBlank(outlineNumber)){
					logger.info("chenhb--originate--"+outlineNumber+"--thread--"+threadName);
					// 呼叫Action
					OriginateAction originateAction = new OriginateAction();
					originateAction.setChannel("Local/"+outlineNumber+"@autodialcounter");
					originateAction.setTimeout(5*1000L);
					originateAction.setPriority(1);
					originateAction.setAsync(true);
					//通过项目的外线呼出,发送成功返回True
					AmiManagerThread.sendAction(originateAction);
				 }else{
					logger.info("chenhb--not_originate--"+outlineNumber+"--thread--"+threadName);					 
				 }
				 // ***
			 
			   //****************************计算可用坐席数量***************************************//	
		    	//如果没有可以用来呼叫到队列的分机，则线程睡眠5s后重试
		    	if(extenList.size()==0){
		    		logger.info("chenhb--nocsr--"+extenList.size()+"--thread--"+threadName);
		    		logger.info("chb: 自动外呼线程任务中没有CSR或者没有CSR登陆");
		    		//为了实现tq对接呼叫添加的注释
//		    		Thread.sleep(5000);
//		    		continue;
		    	}

		    	//=============解决自动外呼无法停止问题，性能换安全，性能损耗不大===============//
		    	if(autoDialoutTask!=null&&autoDialoutTask.getId()!=null){
			    	//原生Sql查询Task的状态，如果为停止，则停止线程
			    	String nativeSql="select id,autodialouttaskstatus from ec2_auto_dialout_task where id="+autoDialoutTask.getId()+" and autodialouttaskstatus!="+AutoDialoutTaskStatus.RUNNING.getIndex();
			    	CommonService commonService=SpringContextHolder.getBean("commonService");
			    	@SuppressWarnings("unchecked")
					List<Object[]> results = (List<Object[]>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
			    	if(results.size()>0){ // 运行不正常，应该停止
			    		logger.info("chenhb--shouldstop--db_stop_thread--thread--"+threadName);
			    		logger.warn("chb: Thread should stop, now stop");
			    		isLoop=false;
			    		return;
			    	}else{
			    		//运行正常
			    	}
		    	}else{ //如果Task不可见，则停止线程
		    		logger.info("chenhb--task_should_notnull--thread--"+threadName);
		    		isLoop=false;
		    		return;
		    	}
		    			
				//****************************计算应该外呼数量，外线可用的并发数量，得出可以外呼的数量***************************************//	
		    	int outCallNum=0;
		    	if(isSystemAjust){
		    		logger.info("chenhb--isSystemAjust--thread--"+threadName);
					//登陆数量
					Integer loggedIn=AutoDialHolder.queueToLoggedIn.get(queueName);
					if(loggedIn==null){
						loggedIn=0;
					}
					logger.info("chenhb--loggedInCsr--"+loggedIn+"--thread--"+threadName);
					//队列中排队数量
					Integer callers=AutoDialHolder.queueToCallers.get(queueName);
					if(callers==null){
						callers=0;
					}
					//队列中可用坐席数量
					Integer available=AutoDialHolder.queueToAvailable.get(queueName);
					if(available==null){
						available=0;
					}
					//预期排队数量,大于等于1
					logger.info("chenhb--Queue--"+queueName+"Caller"+"--"+callers+"--thread--"+threadName);
					int expectedCallers=0;
					if(loggedIn*percentageDepth<1){
						expectedCallers=1;
					}else{
						expectedCallers=(int)(loggedIn*percentageDepth);
					}
					
					logger.info("chenhb--Queue--AutoAjust:Percentage-->"+percentageDepth+"  --  Ratio->"+ratio+"ExpactedCaller"+expectedCallers+"--thread--"+threadName);

					//如果排队的数量还没有达到预期
					if(expectedCallers-callers>0&&available>0){
logger.info("===========================");
try {
//	System.out.println("没有达到预期");
	System.out.println(new String(("NOT_Reach_Expected").getBytes(), "UTF-8"));
} catch (UnsupportedEncodingException e) {}		
logger.info("===========================");
logger.info("");
						if((expectedCallers-callers)*ratio<1){

logger.info("chenhb--OutcallNumLessThan1"+"--"+((expectedCallers-callers)*ratio)+"--thread--"+threadName);
							outCallNum=1;
						}else{
							if(available>(expectedCallers-callers)){
								available=expectedCallers-callers;
							}
							outCallNum=(int)(available*ratio);
logger.info("chenhb--OutcallNumMoreThan1"+"--"+((expectedCallers-callers)*ratio)+"--thread--"+threadName);
						}
					}else{
logger.info("chenhb--ReachExpected"+"--thread--"+threadName);
					}
				}else{ //手动调整
					//队列中排队数量
					Integer callers=AutoDialHolder.queueToCallers.get(queueName);
					if(callers==null){
						callers=0;
					}
logger.info("chenhb--StaticDeepCall :StaticValue->"+staticExpectedCallers+"------Ratio->"+ratio+"--thread--"+threadName); 
					if(staticExpectedCallers-callers>0){
						outCallNum=(int)((staticExpectedCallers-callers)*ratio);
					}
logger.info("chenhb--StaticDeepCall outCallNum->"+outCallNum+"--thread--"+threadName);
				}

		    	//chenhb chenaged 20141113 ***
		    	int usingConcurrent = 0;
				//外线正在使用的并发数量
		    	ManagerResponse managerResponse = AmiManagerThread.sendResponseAction(new GetVarAction(outlineNumber));
				String concurrentNum = managerResponse.getAttribute("Value");
				if(StringUtils.isNumeric(concurrentNum)){
					usingConcurrent=Integer.parseInt(concurrentNum);
logger.info("chenhb--Ami--"+outlineNumber+"--ConcurrentNum--"+concurrentNum+"--thread--"+threadName);
				}else{
					Set<String> peerChannels = ShareData.peernameAndChannels.get(outlineNumber);
					if(peerChannels!=null){
						usingConcurrent=peerChannels.size();
					}
logger.info("chenhb--Mem--"+outlineNumber+"--ConcurrentNum--"+concurrentNum+"--thread--"+threadName);
				}
				// *** 
					
				//外线可用的并发数量
logger.info("chenhb--OutlineInfo: outline->"+outlineNumber+"  ---  callLimit-->callLimit--thread--"+threadName);

				int couldUseConcurrent=(int) (callLimit-usingConcurrent);
				if(outCallNum>couldUseConcurrent){
					outCallNum=couldUseConcurrent;
				}

logger.info("chenhb--FinalOutcallNum->"+outlineNumber+"  ---  callLimit-->callLimit--thread--"+threadName);


				//****************************进行外呼操作***************************************//	
				for(int i=0;i<outCallNum;i++){
logger.info("chenhb--ForLoop--"+outCallNum+"--thread--"+threadName);
					ConcurrentLinkedQueue<MarketingProjectTask> resourceQueue = producerThread.getResourceQueue();
					if(resourceQueue.size()==0){
logger.info("chenhb--ResourceQueueEmpty"+"--thread--"+threadName);
						break;
					}
					if(i%5==0){
						Thread.sleep(50L);
					}
					originateCall(resourceQueue,queueName); 
				}
logger.info("chenhb--BeforeThreadSleep"+"--thread--"+threadName);
				Thread.sleep(THREAD_SLEEP_TIME);
			}
	}
	
	/**
	 * 开始语音外呼
	 * @throws InterruptedException
	 */
	private void startSoundDialout() throws InterruptedException {
		while(isLoop&&producerThread.getIsLoop()){
			
			//=============解决自动外呼无法停止问题，性能换安全，性能损耗不大===============//
	    	if(autoDialoutTask!=null&&autoDialoutTask.getId()!=null){
		    	//原生Sql查询Task的状态，如果为停止，则停止线程
		    	String nativeSql="select id,autodialouttaskstatus from ec2_auto_dialout_task where id="+autoDialoutTask.getId()+" and autodialouttaskstatus!="+AutoDialoutTaskStatus.RUNNING.getIndex();
		    	CommonService commonService=SpringContextHolder.getBean("commonService");
		    	@SuppressWarnings("unchecked")
				List<Object[]> results = (List<Object[]>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
		    	if(results.size()>0){ //运行不正常，应该停止
		    		logger.warn("chb: Thread should stop, now stop");
		    		isLoop=false;
		    		return;
		    	}else{
		    		//运行正常
		    	}
	    	}else{ //如果Task不可见，则停止线程
	    		logger.warn("chb: Task could not see, now stop");
	    		isLoop=false;
	    		return;
	    	}
			
	    	//chenhb chenaged 20141113 ***
	    	int usingConcurrent = 0;
			//外线正在使用的并发数量
	    	ManagerResponse managerResponse = AmiManagerThread.sendResponseAction(new GetVarAction(outlineNumber));
			String concurrentNum = managerResponse.getAttribute("Value");
			if(StringUtils.isNumeric(concurrentNum)){
				usingConcurrent=Integer.parseInt(concurrentNum);
logger.info("Ami--"+outlineNumber+"--ConcurrentNum--"+concurrentNum);
			}else{
				Set<String> peerChannels = ShareData.peernameAndChannels.get(outlineNumber);
				if(peerChannels!=null){
					usingConcurrent=peerChannels.size();
				}
			}
			// *** 
			
			int couldUseConcurrent=callLimit-usingConcurrent;
//	System.err.println("外呼数量"+couldUseConcurrent);
logger.info("外呼数量"+couldUseConcurrent);
			ConcurrentLinkedQueue<MarketingProjectTask> resourceQueue = null;
			if(producerThread.isAlive()){
				resourceQueue=producerThread.getResourceQueue();
			}else{
				//如果生产线程已经停止，则停止消费线程
				break;
			}
			for(int i=0;i<couldUseConcurrent;i++){
				if(resourceQueue.size()==0){
					logger.error("资源不足或生产线程异常！");
					break;
				}
				originateCall(resourceQueue,"sound");
			}
			Thread.sleep(THREAD_SLEEP_TIME);
		}
	}

	/**
	 * 发起呼叫
	 * @param resourceQueue
	 */
	private void originateCall(ConcurrentLinkedQueue<MarketingProjectTask> resourceQueue,String queueName) {
		MarketingProjectTask marketingProjectTask=resourceQueue.poll();
		marketingProjectTask.setUser(user);
		//向Task中存储呼叫信息
		marketingProjectTask.setIsFinished(true);
		marketingProjectTask.setAutodialId(autoDialoutTask.getId());
		marketingProjectTask.setAutodialName(autoDialoutTask.getAutoDialoutTaskName());
		marketingProjectTask.setAutodialIsAnswered(false);//默认为客户没有应答
		marketingProjectTask.setAutodialIsCsrPickup(false);////默认为CSR没有接起
		marketingProjectTask.setAutodialTime(new Date());
		//存储信息到数据库
		marketingProjectTaskService.update(marketingProjectTask);
		
		CustomerResource resource=marketingProjectTask.getCustomerResource();
		Set<Telephone> telephoneSet = resource.getTelephones();
		
		//获取Id值最大的电话
		Telephone toDialPhone=null;
		Long phoneMaxId=0L;
		for(Telephone telephone:telephoneSet){
			if(phoneMaxId<telephone.getId()){
				toDialPhone=telephone;
				phoneMaxId=telephone.getId();
			}
		}
		if(toDialPhone==null) return;  //应该不会出现

		
		//存放所有变量的Map
		Map<String,String> variables=new HashMap<String, String>();
		variables.put("queueName", queueName);
		variables.put("resourceId", resource.getId().toString());//存储资源Id 备用
		variables.put("projectId", marketingProject.getId().toString());//存储资源Id 备用
		variables.put("taskId",marketingProjectTask.getId().toString());//存储TaskId，以在自动外呼agi中查找资源
		variables.put("soundFile", domainIdStr+"/"+soundFileStr);
		variables.put("isAudioPlay",isAudioPlay.toString());
		variables.put("isSoundDialout",isSoundDialout.toString());
		variables.put("outlineNumber",outlineNumber);	// JRH 20140711 新增，为了解决群呼谈判，获取使用外线问题
		if(isSoundDialout) {
			variables.put("isAutoDial","false");
			variables.put("isSoundDial","true");
		} else {
			variables.put("isAutoDial","true");	
			variables.put("isSoundDial","false");
		}
		
		String todialPhoneNumber = toDialPhone.getNumber();
		
		//==============自动进行归属地识别 Start===================//
		Boolean belong = (Boolean)ShareData.domainToConfigs.get(domainId).get("belong_config");
		if(belong !=null){
			//查找外线区号
			String outline=outlineNumber;
			if(sipConfigService==null){
				sipConfigService=SpringContextHolder.getBean("sipConfigService");
			}
			SipConfig outlineSipConfig=sipConfigService.getOutlineByOutlineName(outline);
			String postCode="";
			postCode=outlineSipConfig.getBelong();
			
			if(postCode!=null&&!postCode.equals("")){
				if(belong==false){ //加0
					todialPhoneNumber = MobileLocUtil.prefixProcessZero(todialPhoneNumber,postCode);
				}else if(belong==true){ //加区号
					todialPhoneNumber = MobileLocUtil.prefixProcessPostCode(todialPhoneNumber,postCode);
				}
			}
		}
		//==============自动进行归属地识别 End===================//
		// 呼叫Action
		OriginateAction originateAction = new OriginateAction();
		originateAction.setChannel("SIP/"+todialPhoneNumber+"@"+outlineNumber);
		originateAction.setContext("incoming");
		if(isSoundDialout){
			originateAction.setExten("sounddial");
		}else{
			originateAction.setExten("autodial");
		}
		originateAction.setVariables(variables);
		originateAction.setTimeout(5*60*1000L);
		originateAction.setPriority(1);
		originateAction.setCallerId(todialPhoneNumber+" <"+todialPhoneNumber+">");
		originateAction.setAsync(true);
		storeCdrInfo(originateAction,marketingProjectTask.getId().toString(), !isSoundDialout,resource.getId().toString());
		
		//进行计数操作
		logger.info("发起呼叫:"+toDialPhone.getNumber()+" 外线："+outlineNumber);

		//通过项目的外线呼出,发送成功返回True
		AmiManagerThread.sendAction(originateAction);
//		AutoDialHolder.inceaseCallingCount(queueName);
	}

	/**
	 * 存储CDR发起方的信息
	 */
	private void storeCdrInfo(OriginateAction originateAction,String taskIdStr, boolean isAutoDial,String resourceId) {
		MarketingProject project=autoDialoutTask.getMarketingProject();
		String projectId=project.getId().toString();
		String projectName=project.getProjectName();
		String autoDialId=autoDialoutTask.getId().toString();
		String autoDialName=autoDialoutTask.getAutoDialoutTaskName();
		String domainId=autoDialoutTask.getDomain().getId().toString();
		
		originateAction.setVariable("CDR(projectId)", projectId);
		originateAction.setVariable("CDR(projectName)", encodeString(projectName));
		originateAction.setVariable("CDR(resourceId)", resourceId);
		originateAction.setVariable("CDR(taskId)", taskIdStr);
		originateAction.setVariable("CDR(isAutoDial)", isAutoDial+"");
		originateAction.setVariable("CDR(autoDialId)", autoDialId);
		originateAction.setVariable("CDR(autoDialName)", encodeString(autoDialName));
		originateAction.setVariable("CDR(domainId)", domainId);
		originateAction.setVariable("CDR(usedOutlineName)", outlineNumber);
		
		StringBuilder stringBuilder=new StringBuilder();
		// jrh 用于设置真实的被叫号码
		stringBuilder.append("destination:"+outlineNumber+"&");
		String userfieldStr=stringBuilder.toString();

		// userfield 不能超过255个字节，否则会出现错误
		originateAction.setVariable("CDR(userfield)", userfieldStr);
	}

	/**
	 * 根据Task对应的用户取出所有的分机,每当用新用户添加时都应该重新刷新线程的用户和分机
	 * @param users
	 * @return
	 */
	public synchronized void refreshEditChangeVariable() {
		autoDialoutTask = autoDialoutTaskService.get(autoDialoutTask.getId());
		isSoundDialout = autoDialoutTask.getDialoutType().equals(AutoDialoutTask.SOUND_DIALOUT);
		ratio = autoDialoutTask.getRatio();
		this.staticExpectedCallers = autoDialoutTask.getStaticExpectedCallers();
		if(staticExpectedCallers==null) staticExpectedCallers=0;
		if(autoDialoutTask.getIsSystemAjust().equals(AutoDialoutTask.SYSTEM_AJUST)){
			percentageDepth = autoDialoutTask.getPercentageDepth() / 100D;
			isSystemAjust = true;
		}else{
			isSystemAjust = false;
		}
		users=autoDialoutTask.getUsers();
		marketingProject = autoDialoutTask.getMarketingProject();
		
		// 取得项目的外线，如果项目没有外线，这返回默认外线
		outlineNumber = ShareDataElementUtil.getOutlineByProject(marketingProject);
		callLimit = ShareDataElementUtil.getOutlineCapabilityByProject(marketingProject);
		if(isSoundDialout){
			if((autoDialoutTask.getConcurrentLimit()!=null)&&(callLimit>autoDialoutTask.getConcurrentLimit())){
				callLimit=autoDialoutTask.getConcurrentLimit();
				logger.info("UseAutoDialTaskCallLimitNotSipCallLimit-->"+callLimit);
			}
		}
		logger.info("ChangeOutline: outline->"+outlineNumber+"  ---  callLimit-->"+callLimit);

		if(callLimit==null){
			callLimit=0;
		}
		
		if(outlineNumber==null){
			outlineNumber="00000";
		}
		
		//获取自动外呼的语音文件、是否播放信息
		if(autoDialoutTask.getDialoutType().equals(AutoDialoutTask.AUTO_DIALOUT)){//如果是自动外呼
			if(autoDialoutTask.getSoundFile()==null){
				soundFileStr="00000";
				isAudioPlay=false;
			}else{
				soundFileStr=autoDialoutTask.getSoundFile().getStoreName();
				if(autoDialoutTask.getPreAudioPlay().equals(AutoDialoutTask.PLAY_PRE_AUDIO)){
					isAudioPlay=true;
				}else if(autoDialoutTask.getPreAudioPlay().equals(AutoDialoutTask.NOT_PLAY_PRE_AUDIO)){
					isAudioPlay=false;
				}
			}
		}else if(autoDialoutTask.getDialoutType().equals(AutoDialoutTask.SOUND_DIALOUT)){ //如果是语音群呼
			soundFileStr=autoDialoutTask.getSoundFile().getStoreName();
			isAudioPlay=true;
		}
		
//		//取外线配置和自动外呼配置的外呼数量限制的最小值
//		SipConfig outlineSip = marketingProject.getSip();
//
//		System.out.println("Project:"+marketingProject);
//		System.out.println("outlineSip:"+outlineSip);
//		
//		if(outlineSip!=null){
//			if(outlineSip.getCall_limit()!=null){
//				callLimit=outlineSip.getCall_limit();
//			}else{
//				//如果外线没有设置最大并发数量，则外线的并发数量为最大值 // TODO jrh 虽然这种情况是不会发生的，可是一旦发生，这样会不会很浪费资源？！
//				callLimit=Integer.MAX_VALUE; // chb 解释： 确实不好，但又不能设置为0 
//			}
//			if((autoDialoutTask.getConcurrentLimit()!=null)&&(callLimit>autoDialoutTask.getConcurrentLimit())){
//				callLimit=autoDialoutTask.getConcurrentLimit();
//			}
//		}else{
//			1String defaultOutline=ShareData.domainToDefaultOutline.get(autoDialoutTask.getDomain().getId());
//			SipConfigService sipConfigService=SpringContextHolder.getBean("sipConfigService");
//			SipConfig sipConfig=sipConfigService.getOutlineByOutlineName(defaultOutline);
//			if(sipConfig!=null){
//				callLimit=sipConfig.getCall_limit();
//			}else{
//				callLimit=0;
//			}
//		}
		
		synchronized (extenList) {
			//可用的分机集合
			extenList = new ArrayList<String>();
			//通过对应关系判断自动外呼的CSR的数量
			for (User user : users) {
				// 找不到用户对分机的绑定关系，说明用户还没有登录，所以坐席不可用
				String exten = ShareData.userToExten.get(user.getId());
				if (exten != null) {
					extenList.add(exten);
				}
			}
    	}
	}
	
	/**
	 * 将字符串[中文]进行编码，然后返回
	 * @param needEncodeStr	 需要编码的信息
	 * @return
	 */
	private String encodeString(String needEncodeStr) {
		try {
			needEncodeStr = URLEncoder.encode(needEncodeStr, "utf-8");
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace();
			logger.error("jrh 自动外呼 为asterisk 而 UrlEncode 中文信息时出现异常-->"+e.getMessage(), e);
		}
		return needEncodeStr;
	}
	
	//============= Getter and Setter ========================//
	public Boolean getIsLoop() {
		return isLoop;
	}

	public void setIsLoop(Boolean isLoop) {
		this.isLoop = isLoop;
	}

	public MarketingProject getMarketingProject() {
		return marketingProject;
	}

	public void setMarketingProject(MarketingProject marketingProject) {
		this.marketingProject = marketingProject;
	}
	
}
