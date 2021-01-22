package com.jiangyifen.ec2.fastagi;

import java.util.List;
import java.util.Set;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.utils.Phone2PhoneDialConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.jiangyifen.ec2.utils.VoicemailConfig;

/**
 * 根据客户资源查找应该拨打的分机或队列Agi 
 * @author jrh
 */
public class GetResourceManagerExten extends BaseAgiScript { 
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void service(AgiRequest agiRequest, AgiChannel channel) throws AgiException {

		String outLine = channel.getVariable("dest");
		
		Long currentDomainId = null;
		//查看是哪个域的外线，获取当前域的id号
		for(Long domainId : ShareData.domainToOutlines.keySet()){
			List<String> domainOutlines = ShareData.domainToOutlines.get(domainId);
			if(domainOutlines.contains(outLine)){
				currentDomainId = domainId;
				break;
			}
		}
//		TODO
logger.info("currentDomainId -------> " + currentDomainId);
logger.info("outLine -------> " + outLine);
		/************************************ 通过外线找不到对应域 ********************************/
		// 如果没找到当前域，则拨打一个不存在的队列，让客户听盲音
		if(currentDomainId == null) {
			logger.info("在所有域中找不到当前外线"+outLine+", 无法获取域id！");
			channel.setVariable("TimeOut", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_COMMON));
			channel.setVariable("QueueName", "000000");
			return;
		}
		
		/************************************ 域存在，但项目不存在 ********************************/
		Long projectId=ShareData.outlineToProject.get(outLine);
//		TODO
logger.info("projectId -------> " + projectId);
		if(projectId==null) {	// 没有找到项目，打默认队列
			dialDefaultQueue(channel, currentDomainId);
			return;
		} 	

		/************************************ 项目存在，但没开专员路由 ********************************/
		// 通过外线找到了项目
		MarketingProjectService marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		MarketingProject marketingProject = marketingProjectService.get(projectId);
		boolean commissionerRoutingOn = marketingProject.getCommissionerRouting().equals(MarketingProject.COMMISSIONER_ROUTING_ON);
		if(!commissionerRoutingOn) {	// 如果项目没有开启专员路由，则打默认队列，或者项目使用的队列
			dialDefaultQueueOrProjectQueue(channel, outLine, marketingProject);
			return;
		}

		/************************************ 开启了专员路由，但客户经理不存在 ********************************/
		// 如果项目开启了专员路由，则查客户经理
		User accountManager = getAccountManager(agiRequest, channel, currentDomainId);
		if(accountManager == null) {	// 客户经理不存在，则打默认队列，或者项目使用的队列
			dialDefaultQueueOrProjectQueue(channel, outLine, marketingProject);
			return;
		}
		
		/************************************ 客户经理存在，但没有手机号 ********************************/
		// 获取客户经理的手机号
		String csrPhoneNum = accountManager.getPhoneNumber(); 
		// 获取客户经理正在使用的分机，可能为null（空，经理不在线）
		String exten = ShareData.userToExten.get(accountManager.getId());
		
		// 客户经理没有移动电话，则不需要判断是否打手机了，就查看话务员是否在线，在线打分机，不在线打队列
		if(csrPhoneNum == null) {
			dialCommonExtenOrQueue(channel, outLine, marketingProject, exten);
			return;
		}

		/************************************ 开启了专员路由, 客户经理存在，并且拥有手机号，需要判断外转外 ********************************/
		// 获取全局外转外配置项
		Phone2PhoneSettingService phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");

		/************************************ 全局外转外不存在 ********************************/
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(currentDomainId);
		// 如果全局外转外配置不存在，则看话务员是否在线，在线打分机，不在线打队列
		if(globalSetting == null) {
			dialCommonExtenOrQueue(channel, outLine, marketingProject, exten);
			return;
		}

		/************************************ 全局外转外存在  ********************************/
		// 判断全局外转外是否正在进行中
		boolean isGlobalRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);
		if(isGlobalRunning) {	// 全局外转外正在运行
			// 如果当前正在进行的方式是“便捷呼叫”，则直接打入队列
			if(globalSetting.getIsSpecifiedPhones()) {
//				TODO 待修改
				dialDefaultQueueOrProjectQueue(channel, outLine, marketingProject);
				return;
			} 
			
			// 如果全局配置是“智能呼叫”,而且这些话务员中包含当前被选中的CSR，此时不需要考虑话务员自定义的配置了，按配置规则打分机或手机
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(accountManager.getId())) {
					dialAccordingRedirectType(channel, outLine,	marketingProject, csrPhoneNum, exten, 
							globalSetting.getRedirectTypes(), globalSetting.getNoanswerTimeout());
					return;
				}
			}
		} 

		/************************************ 话务员自定义外转外不存在 ********************************/
		Phone2PhoneSetting customSetting = phone2PhoneSettingService.getByUser(accountManager.getId());
		// 如果自定义外转外配置不存在，则看话务员是否在线，在线打分机，不在线打队列
		if(customSetting == null) {
			dialCommonExtenOrQueue(channel, outLine, marketingProject, exten);
			return;
		}

		/************************************ 话务员自定义外转外存在，但当前客户经理  受  管理员控制 ********************************/
		// 能执行到这，说明当前全局配置一定没有运行外转外
		// 全局配置转呼方式是“智能呼叫”，如果全局配置项中之指定的话务员中包含当前话务员，则看话务员是否在线，在线打分机，不在线打队列
		if(!globalSetting.getIsSpecifiedPhones()) {
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				// 如果话务员包含在全局配置中，则以全局配置为主[智能呼叫：管理员对那些选中的话务员是完全控制外转外的]
				if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(accountManager.getId())) {
					dialCommonExtenOrQueue(channel, outLine, marketingProject, exten);
					return;
				}
			}
			
		}

		/************************************ 话务员自定义外转外存在，但当前客户经理  不受  管理员控制 ********************************/
		// 判断1、话务员持有自定义外转外的授权，2、自定义的外转外存在并是开启状态，3、启动时刻 <= 当前时刻, 并且终止时刻 >= 当前时刻，如果都满足，就按配置规则呼叫分机或手机
		if(globalSetting.getIsLicensed2Csr()) {
			boolean isCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
			if(isCustomRunning) {
				dialAccordingRedirectType(channel, outLine, marketingProject, csrPhoneNum, exten, 
						customSetting.getRedirectTypes(), customSetting.getNoanswerTimeout());
				return;
			}
		}
	}

	/**
	 * jrh 呼叫默认队列，如果没有默认队列，则呼000000 队列（这个队列实际是不存在的） 
	 * @param channel	通道
	 * @param outLine	外线
	 * @throws AgiException	异常
	 */
	private void dialDefaultQueue(AgiChannel channel, Long domainId)
			throws AgiException {
		// 获取默认队列
		String toDialQueue = ShareData.domainToDefaultQueue.get(domainId);
		
		//没有找到域内的默认Queue
		if(toDialQueue == null){
			logger.info("域id 为 "+domainId+" 的域中没有找到默认的队列！");
			toDialQueue="000000";
		}
	
		channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_QUEUE_TIMEOUT);
		channel.setVariable("QueueName", toDialQueue);
	}

	/**
	 * jrh
	 *  不需要分机外转外配置规则进行呼叫
	 *  用户在线，直接打分机，
	 * @param channel					当前呼叫通道
	 * @param outLine					客户呼入时通过的外线
	 * @param marketingProject			通过外线找到的当前项目
	 * @param exten						客户经理正在使用的分机
	 * @throws AgiException			向上抛出异常
	 */
	private void dialCommonExtenOrQueue(AgiChannel channel, String outLine, MarketingProject marketingProject, String exten) throws AgiException {
//		boolean commissionerRoutingOn = marketingProject.getCommissionerRouting().equals(MarketingProject.COMMISSIONER_ROUTING_ON);
		if(exten != null) {
			channel.setVariable("voicemailCommissionerRoute", VoicemailConfig.VOICEMAIL_COMMISSIONER_ROUTE);
			channel.setVariable("TimeOut", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_COMMON));
			channel.setVariable("DialType", "common");
			channel.setVariable("resourceManagerExten", exten);
//		} else if(commissionerRoutingOn) {
//			channel.hangup();
//			logger.warn("JRH 项目【"+marketingProject.getProjectName()+"】开启了专员路由，但客户经理不在线，呼入-直接挂断！");
		} else {
			dialDefaultQueueOrProjectQueue(channel, outLine, marketingProject);
		}
	}

	/**
	 * jrh
	 * 	拨打默认队列，或者项目使用的队列
	 * @param channel					 当前呼叫通道
	 * @param outLine					客户呼入时通过的外线
	 * @param marketingProject			通过外线找到的当前项目
	 * @throws AgiException				向上抛出异常
	 */
	private void dialDefaultQueueOrProjectQueue(AgiChannel channel,
			String outLine, MarketingProject marketingProject)
			throws AgiException {
		Queue projectQueue = marketingProject.getQueue();
		if(projectQueue == null) {	// 项目没有指定队列, 打默认队列
			dialDefaultQueue(channel, marketingProject.getDomain().getId());
		} else { 	// 存在队列，打项目队列
			channel.setVariable("TimeOut", GlobalVariable.AGI_INCOMING_TO_QUEUE_TIMEOUT);
			channel.setVariable("QueueName", projectQueue.getName());
		}
		return;
	}

	/**
	 * 根据查找资源是否存在-->是否独享客户-->是否是某个CSR打出的客户--> 返回 CSR 对象
	 * 	jrh  此处的专员路由获取客户经理是有问题的：
	 * 			因为一条资源可能是多个话务员的客户，因为这条资源可能是在不同的项目中成为了客户，而特有客户经理却只能有一个
	 * 			而且一旦特有客户经理被产生，就不再会被改动，这样以后客户打进来电话，可能就找的是最早成为特有客户经理的CSR，而这实际上是不合理的
	 * @param request		请求
	 * @param channel		通道
	 * @param domainId		当前域id
	 * @return User			返回客户经理
	 */
	private User getAccountManager(AgiRequest request,AgiChannel channel,Long domainId){
		String customerPhoneNumber = request.getCallerIdNumber();
		//根据电话号码取得客户资源
		CustomerResourceService customerResourceService=SpringContextHolder.getBean("customerResourceService");
		CustomerResource customerResource=customerResourceService.getCustomerResourceByPhoneNumber(customerPhoneNumber, domainId);
		if(customerResource==null){
			return null;
		}
		//如果资源被独享，则找到客户经理
		User accountManager=customerResource.getAccountManager();
		return accountManager;

		//	//如果资源没有被独享，则查看是谁的客户
		//	User cusOwner=null;
		//	Set<ProjectCustomer> projectCustomerSet = customerResource.getProjectCustomers();
		//	List<ProjectCustomer> projectCustomerList=new ArrayList<ProjectCustomer>(projectCustomerSet);
		//	// jrh 将项目-客户对象按id由大到小进行排序
		//	Collections.sort(projectCustomerList, new Comparator<ProjectCustomer>() {
		//		@Override
		//		public int compare(ProjectCustomer pc1, ProjectCustomer pc2) {
		//			return (int) ((pc2.getId() - pc1.getId()));
		//		}
		//		
		//	});
		//	
		//	for(ProjectCustomer projectCustomer : projectCustomerList) {
		//		cusOwner = projectCustomer.getAccountManager();
		//		if(cusOwner != null){
		//			exten = ShareData.userToExten.get(cusOwner.getId());
		//			if(exten != null) {
		//				return exten;
		//			}
		//		}
		//	}
		//	
		//	//如果也不是客户则从Task中查找此客户是谁的Task
		//	MarketingProjectTaskService marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
		//	User taskOwner=marketingProjectTaskService.getCsrByCustomerResourceId(customerResource.getId(),marketingProject.getId());
		// 	return csr;
	}

	/**
	 * jrh
	 *  当外转外正在执行时，根据外转外配置中的转接时机类型（无人接听转、遇忙转、不在线转），制定呼叫方案（打手机或打分机的时机）
	 * @param channel					 当前呼叫通道
	 * @param outLine					客户呼入时通过的外线
	 * @param marketingProject			通过外线找到的当前项目
	 * @param csrPhoneNum				客户经理的手机号码
	 * @param exten						客户经理正在使用的分机
	 * @param redirectType				转接时机类型
	 * @throws AgiException				向上抛出异常
	 */
	private void dialAccordingRedirectType(AgiChannel channel, String outLine,
			MarketingProject marketingProject, String csrPhoneNum,
			String exten, Set<String> redirectType, Integer noanswerTimeout) throws AgiException {
		try {
			if(exten == null) {	// 客户经理不在线
				if(redirectType.contains("unonline")) {		// 配置了不在线转手机，则直接呼手机
					channel.setVariable("TimeOut", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_UNONLINE));
					channel.setVariable("DialType", "unonline");
					channel.setVariable("OutLine", outLine);
					channel.setVariable("PhoneNumber", csrPhoneNum);
				} else {		// 没配置了，则直接挂断【这里以后应该做成可配置，是直接挂断、或者打入队列 dialDefaultQueueOrProjectQueue(channel, outLine, marketingProject);】
					this.hangup();
				}
			} else {	// 客户经理在线
				Set<String> currentChannels = ShareData.peernameAndChannels.get(exten);		// 获取分机的当前通话路数
				if(currentChannels != null && currentChannels.size() > 0) {	// 判断客户经理忙
					if(redirectType.contains("busy")) {		// 配置了遇忙转手机，直接打手机
						channel.setVariable("TimeOut", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_BUSY));
						channel.setVariable("DialType", "busy");
						channel.setVariable("OutLine", outLine);
						channel.setVariable("PhoneNumber", csrPhoneNum);
					} else {		// 没配置了遇忙转手机，则直接打分机，让客户听盲音
						channel.setVariable("TimeOut", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_COMMON));
						channel.setVariable("DialType", "common");
						channel.setVariable("resourceManagerExten", exten);
					}
				} else {	// 客户经理闲，肯定直接打分机
					if(redirectType.contains("noanswer")) {		// 配置了无人接听转手机,打分机，等待时长设为 TIMEOUT_NOANSWER
						channel.setVariable("TimeOut", noanswerTimeout.toString());
						channel.setVariable("TimeOut2", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_NOANSWER));
						channel.setVariable("DialType", "noanswer");
						channel.setVariable("OutLine", outLine);
						channel.setVariable("PhoneNumber", csrPhoneNum);
					} else {		// 没有配置了无人接听转手机,打分机，等待时长设为 TIMEOUT_COMMON
						channel.setVariable("TimeOut", Phone2PhoneDialConfig.props.getProperty(Phone2PhoneDialConfig.TIMEOUT_COMMON));
						channel.setVariable("DialType", "common");
					}
					channel.setVariable("resourceManagerExten", exten);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("jrh 客户呼入，根据外转外进行呼叫出现异常，该异常可忽略--"+e.getMessage(), e);
		}
	}

}
