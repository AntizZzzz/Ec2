package com.jiangyifen.ec2.fastagi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.QcCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusNavigationKeyService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 这里可优化的地方：
 * 	1.座席呼入这个拨号方案后，可以选择保存的客服记录是呼入进来的通话记录，还是呼出的通话记录。
 * 	2.座席可以根据哪一月的哪一天的客户号码进行保存客服记录。
 */


/**
 * 座席根据拨号方案返回结果创建客服记录
 * @author JHT
 * @date 2014-11-14 下午4:13:05
 */
public class CreateCustomerServiceRecordByExten extends BaseAgiScript {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private CdrService cdrService = SpringContextHolder.getBean("cdrService");	// CDR的业务类
	private CustomerServiceRecordService customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService"); // 保存客服记录业务类
	private TelephoneService telephoneService = SpringContextHolder.getBean("telephoneService");	// 电话号码业务类
	private CustomerServiceRecordStatusNavigationKeyService customerServiceRecordStatusNavigationKeyService = SpringContextHolder.getBean("customerServiceRecordStatusNavigationKeyService");
	private MarketingProjectService marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
	private UserService userService = SpringContextHolder.getBean("userService");
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		
		String resultCode = "";			// 执行结果返回给Asterisk
		User loginUser = null;			// 获取当前登录的座席	
		Domain domain = null;			// 获取当前座席所在的域
		
		String statusCode = request.getParameter("statuscode");			// 获取座席输入的客服记录状态按键
		String telnumber = request.getParameter("telnumber");			// 获取座席输入的客户手机号码
		String extennumber = channel.getVariable("CALLERID(num)");		// 获取当前分机的号码/手机号码
		
		Long userId = ShareData.extenToUser.get(extennumber);
		loginUser = userService.get(userId);
		domain = loginUser.getDomain();
		
		// JRH 从内存里取就行了
		Boolean isByTel = false;
		if(ShareData.domainToConfigs.get(domain.getId()) != null) {
			isByTel = ShareData.domainToConfigs.get(domain.getId()).get("create_customer_service_record_by_exten_type");
		}
		
		if(isByTel == null) {		// 如果从内存中没有取到该信息，就按照上通通话进行创建客服记录
			isByTel = false;
		}
		
		if(statusCode == null || statusCode.trim().length() == 0){	// 说明座席没有输入客服记录状态按键
			resultCode = "no_status_code";
		} else if(isByTel != null && !isByTel){		//说明是按上通电话进行创建客服记录
			resultCode = savePreviousCustomerRecord(loginUser, domain, statusCode);
		} else {	// 说明按手机号创建客服记录
			resultCode = saveInputTelephoneCustomerRecord(loginUser, domain, statusCode, telnumber);
		}
		
		// 返回的执行结果
		channel.setVariable("RESULT_CODE", resultCode);
		
	}
	
	// 按上通通话创建客服记录类型进行保存客服记录
	private String savePreviousCustomerRecord(User loginUser, Domain domain, String statusCode){
		// 查询语法优化 根据当前用户查询他最近一次的通话记录
		//  JRH 这里再加个时间限制，只能针对一天以内呼叫记录加CDR s.starttimedate < now() and s.starttimedate > （now() - 1天）
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		String finishTimeDateStr = SDF_SEC.format(calendar.getTime());
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		String startTimeDateStr = SDF_SEC.format(calendar.getTime());
		String jpqlCdr = "select s from Cdr as s where (s.srcUserId="+loginUser.getId()+" or s.destUserId="+loginUser.getId()+") and s.startTimeDate >= '"+startTimeDateStr+"' and s.startTimeDate < '"+finishTimeDateStr+"' and s.domainId="+domain.getId()+ " ORDER BY s.id DESC";

		List<Cdr> cdrList = cdrService.loadPageEntities(0, 1, jpqlCdr);
		if(cdrList != null && cdrList.size() == 1){ //代表查询有结果
			Cdr cdr = cdrList.get(0);
			String callerId = cdr.getSrc();	// 获取主叫号码
			String destination = cdr.getDestination();	// 获取被叫号码
			Long marketProjectId = cdr.getProjectId();	// 获取项目的ID
			
			String direction = "uncommon_dial";
			CdrDirection cdrDirection = cdr.getCdrDirection();	// 获取呼叫方向
			if(CdrDirection.e2o.equals(cdrDirection)) {
				direction = "outgoing";
			} else if(CdrDirection.o2e.equals(cdrDirection)) {
				direction = "incoming";
			} else {	// 呼叫方向如果不是呼入、呼出，则直接退出，也就是不符合条件
				return "uncommon_dial";
			}
			
			// 获取客服记录状态按键所对应的客服记录状态
			List<CustomerServiceRecordStatusNavigationKey> navigationKeyList = customerServiceRecordStatusNavigationKeyService.getServiceRecordNaviKeyByInfo(domain.getId(), statusCode, direction);
			if(navigationKeyList == null || navigationKeyList.size() == 0){
				return "no_status_code";
			}
			
			Telephone telephone = null;
			if(CdrDirection.e2o.equals(cdrDirection)){	// 呼出方向
				telephone = telephoneService.getByNumber(destination, domain.getId());	// 获取手机号
			} else if(CdrDirection.o2e.equals(cdrDirection)){	// 呼入方向
				telephone = telephoneService.getByNumber(callerId, domain.getId());
			}
			if(telephone == null){	// 表示不存在这个电话，返回没有这个客户
				return "no_customer";
			}
			
			CustomerResource customerResource = telephone.getCustomerResource();
			if(customerResource == null){	// 如果根据手机号没有查询到客户信息，返回没有这个客户
				return "no_customer";
			}
			
			MarketingProject marketingProject = null;
			if(marketProjectId != null) {
				marketingProject = marketingProjectService.get(marketProjectId);
			}

			CustomerServiceRecord customerServiceRecord = new CustomerServiceRecord();	// 客服记录实体类
			customerServiceRecord.setServiceRecordStatus(navigationKeyList.get(0).getServiceRecordStatus());	// 设置状态
			customerServiceRecord.setDirection(direction);	// 呼叫方向
			customerServiceRecord.setCreator(loginUser);	// 客服记录创建人
			customerServiceRecord.setDomain(domain);		// 所在域
			customerServiceRecord.setMarketingProject(marketingProject);	// 项目
			customerServiceRecord.setQcCsr(QcCsr.QUALIFIED);	// 合格
			customerServiceRecord.setCreateDate(cdr.getStartTimeDate());	// 获取CDR的拨打电话的时间即为客服记录的创建时间
			customerServiceRecord.setCustomerResource(customerResource);	// 设置客户信息
			
			if(cdr.getUniqueId() != null){	// 如果这通通话的唯一标示存在
				String recordFileName = dateFormat.format(cdr.getStartTimeDate())+"-"+cdr.getUniqueId()+".wav";
				customerServiceRecord.setRecordFileName(recordFileName);
			}
			
			try {
				customerServiceRecordService.save(customerServiceRecord);
				return "success";	// 保存成功
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("JHT 无电脑座席保存客服记录出现异常 --》 "+e.getMessage(),e);
				return "failer";	// 保存失败
			}
		} else {	// 这个座席没有呼叫记录，也就是不符合条件
			return "uncommon_dial";
		}
		
	}
	
	// 按客户电话号码类型进行保存客服记录
	private String saveInputTelephoneCustomerRecord(User loginUser, Domain domain, String statusCode, String telnumber){
		
		// 获取客服记录状态按键所对应的客服记录状态
		List<CustomerServiceRecordStatusNavigationKey> navigationKeyList = customerServiceRecordStatusNavigationKeyService.getServiceRecordNaviKeyByInfo(domain.getId(), statusCode, "outgoing");
		if(navigationKeyList == null || navigationKeyList.size() == 0){
			return "no_status_code";
		}
		
		Telephone telephone = telephoneService.getByNumber(telnumber, domain.getId());	// 获取手机号实体对象
		if(telephone == null){	// 表示不存在这个电话，返回没有这个客户
			return "no_customer";
		}
		
		CustomerResource customerResource = telephone.getCustomerResource();
		if(customerResource == null){	// 如果根据手机号没有查询到客户信息，返回没有这个客户
			return "no_customer";
		}
		
		CustomerServiceRecord customerServiceRecord = new CustomerServiceRecord();	// 客服记录实体类
		customerServiceRecord.setServiceRecordStatus(navigationKeyList.get(0).getServiceRecordStatus());	// 设置状态
		customerServiceRecord.setDirection("outgoing");	// 呼叫方向
		customerServiceRecord.setCreator(loginUser);	// 客服记录创建人
		customerServiceRecord.setDomain(domain);		// 所在域
		customerServiceRecord.setMarketingProject(null);	// 项目
		customerServiceRecord.setQcCsr(QcCsr.QUALIFIED);	// 合格
		customerServiceRecord.setCreateDate(new Date());	
		customerServiceRecord.setCustomerResource(customerResource);	// 设置客户信息
		
		try {
			customerServiceRecordService.save(customerServiceRecord);
			return "success";	// 保存成功
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("JHT 无电脑座席保存客服记录出现异常 --》 "+e.getMessage(),e);
			return "failer";	// 保存失败
		}
		
	}

}
