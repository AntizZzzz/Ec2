package com.jiangyifen.ec2.servlet.shiyuan;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.DomainService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.mgr.ImportResourceService;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;


/**
 * @Description 描述：该接口供：世元石油股份有限公司使用
 * 
 *  功能适用对象必须满足以下条件：
 *  	1、该功能只能给单租户，或者多租户下的第一个域(domain ID 最小的租户)
 * 		2、该租户下，必须至少存在一个管理员类型的用户
 * 
 * 	作用：
 * 		1、世元石油 调用此接口来将客户信息导入到EC2 的数据库中。  导入信息包含两个基础信息“姓名、电话”，以及几个描述信息“注册时间、搜索来源、自然来源、计划、活动”。
 * 		2、按天创建批次，每天一个，批次名称规则为：20141209-CRM。 为了不影响接口的调用，所以在EC2系统中，管理员创建批次时，不要以此规则命名
 * 		3、如果批次存在，并且已经有项目再使用，则为这些项目自动创建新添加资源对应的任务
 * 		4、描述信息的存储规则：在存储前会检查key+value(即：描述信息名称+描述信息内容) 跟原来的是否一致，如果一致将不再添加，如果不一致，则会新添加一条客户描述信息。这里不会根据key 来检查是否已经存在该信息，并依此来进行覆盖更新
 * 			如客户13816760333 存在这样的描述信息：[计划=去上海开会]，然后第三方系统再次调用接口导入电话号码为13816760333的客户，此时该客户的信息为 [计划=去美国旅游]，
 * 			那么在调用接口后，客户13816760333将有两个“计划”的描述信息：[计划=去上海开会， 计划=去美国旅游]
 * 
 *  调用方式：
 *  	http://ip:port/ec2/http/shiyuan/importresource?name=张三&phonenum=15214563210&regtime=1418091006972&searchsrc=xx&naturesrc=xx&plan=xx&activity=xx
 *  
 *  测试样例：
 *  	http://192.168.2.160:8080/ec2/http/shiyuan/importresource?name=l的房顶上9B&phonenum=15214553422ffa发送到AF&regtime=141809读书法951006972&searchsrc=华国锋&naturesrc=和具体规范化&plan=x电饭锅&activity=地方
 *  	http://220.248.57.190:8080/ec2/http/shiyuan/importresource?name=张三&phonenum=15214563210&regtime=1418091006972&searchsrc=xx&naturesrc=xx&plan=xx&activity=xx
 *  
 *  数据编码格式：
 *  	UTF-8
 *  
 *  参数描述：
 *  	参数名称		  含义 				参数类型		参数值			非空约束		牵涉到的EC2表
 *  	name        : 客户姓名			字符串		如：张三			   可空			ec2_customer_resource 
 *  	phonenum    : 客户电话			3-14位数字	13816760366		不能为空		ec2_telephone
 *  	regtime     : 注册时间			数字			时间的毫秒值		   可空			ec2_customer_resource_description
 *  	searchsrc   : 搜索来源			字符串		xxx				   可空			ec2_customer_resource_description
 *  	naturesrc   : 自然来源			字符串		xxx				   可空			ec2_customer_resource_description
 *  	plan        : 计划			字符串		xxx				   可空			ec2_customer_resource_description
 *  	activity    : 活动			字符串		xxx				   可空			ec2_customer_resource_description
 * 
 *  返回信息说明：
 *  	格式为：执行结果描述  + 一个时间戳
 *
 *      序号			返回值	                   																	含义
 *      1    failed caused by : We don't support the 'get' request! Please use the 'post' request!			请求失败！原因：不支持get请求，请使用post 请求方式。
 *           time tag : 1418125200237
 *           
 *      2    failed cause by : domain not existed in EC2! 													请求失败！原因：呼叫中心系统租户不存在。
 *           time tag : 1418125200237
 *           
 *      3    failed cause by : The phonenum can't be empty and it must consist of 3 to 14 digits! 			请求失败！原因：电话号码不能为空，并且只能是3-14数字。
 *           time tag : 1418125200237
 *           
 *      4    failed cause by : This domain of call center system that don't have user of manager type!		请求失败！原因：呼叫中心系统下该租户没有管理员类型的用户。
 *           time tag : 1418125200237
 *           
 *      5    failed cause by : Incomplete information!														请求失败！原因：信息不完整。
 *           time tag : 1418125200237
 *           
 *      6    success!																						推送成功。
 *           time tag : 1418125200237
 *           
 *      7    failed caused by : exception!																	请求失败！原因：出现异常。
 *           time tag : 1418125200237
 *           
 *      备注：其中“time tag ”后的数值1418125200237 是一个时间戳，该值为当前时间的毫秒值。    
 * 
 * @author  JRH
 * @date    2014年12月9日 上午9:41:43
 */
@SuppressWarnings("serial")
public class ImportResourceServlet extends HttpServlet {
	
	private final String PARAM_ENCODE_TYPE = "UTF-8";
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyyMMdd");
	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset="+PARAM_ENCODE_TYPE);	// 不然中文会出现乱码
		PrintWriter out = response.getWriter();
		out.println("failed caused by : We don't support the 'get' request! Please use the 'post' request! [请求失败！原因：不支持get请求，请使用post 请求方式]<br/><br/>");
		out.println("time tag : "+System.currentTimeMillis());
		out.close();
		logger.info("JRH--> 世元公司的CRM 使用get 请求方式调用Ec2 的导入资源接口失败，我们不支持get 请求！We don't support the 'get' request!");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.executeBusiness(request, response);
	}

	/**
	 * @Description 描述：执行业务处理
	 */
	private void executeBusiness(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html; charset="+PARAM_ENCODE_TYPE);	// 不然中文会出现乱码
		
		PrintWriter out = response.getWriter();
		
		try {
			/* 获取参数值 */
			String name = this.getParamValue(request, "name", PARAM_ENCODE_TYPE);
			String phonenum = this.getParamValue(request, "phonenum", PARAM_ENCODE_TYPE);
			String regtime = this.getParamValue(request, "regtime", PARAM_ENCODE_TYPE);
			String searchsrc = this.getParamValue(request, "searchsrc", PARAM_ENCODE_TYPE);
			String naturesrc = this.getParamValue(request, "naturesrc", PARAM_ENCODE_TYPE);
			String plan = this.getParamValue(request, "plan", PARAM_ENCODE_TYPE);
			String activity = this.getParamValue(request, "activity", PARAM_ENCODE_TYPE);
			
			phonenum = pickOutNumbers(phonenum); // 取出参数值中的数字
			if(StringUtils.isEmpty(phonenum)){
				logger.info("JRH--> 世元公司的CRM 使用Post 方式调用接口：failed cause by : The phonenum can't be empty and it must consist of 3 to 14 digits![请求失败！原因：电话号码不能为空，并且只能是3-14数字]");
				out.println("failed cause by : The phonenum can't be empty and it must consist of 3 to 14 digits![请求失败！原因：电话号码不能为空，并且只能是3-14数字] <br/><br/>");
				out.println("time tag : "+System.currentTimeMillis());
				out.close();
				return;
			}
			
			regtime = pickOutNumbers(regtime);	// 取出参数值中的数字
			if(!StringUtils.isEmpty(regtime)){	// 将日期的毫秒值，修改成完整的日期格式
				long regtimeMillsec = Long.parseLong(regtime);
				Date regtimeDate = new Date(regtimeMillsec);
				regtime = SDF_SEC.format(regtimeDate);
			}
			
//			System.out.println("JRH:-->name:"+name);
//			System.out.println("JRH:-->phonenum:"+phonenum);
//			System.out.println("JRH:-->regtime:"+regtime);
//			System.out.println("JRH:-->searchsrc:"+searchsrc);
//			System.out.println("JRH:-->naturesrc:"+naturesrc);
//			System.out.println("JRH:-->plan:"+plan);
//			System.out.println("JRH:-->activity:"+activity);
			
			/* 获取数据库中的第一个域  */
			DomainService domainService = SpringContextHolder.getBean("domainService");
			List<Domain> domainLs = domainService.getAll();
			Domain domain = null;
			if(domainLs.size() >= 1) {
				domain = domainLs.get(0);
			} else {
				logger.info("JRH--> 世元公司的CRM 使用Post 方式调用接口：failed cause by : domain not existed in EC2![请求失败！原因：呼叫中心系统租户不存在]");
				out.println("failed cause by : domain not existed in EC2![请求失败！原因：呼叫中心系统租户不存在] <br/><br/>");
				out.println("time tag : "+System.currentTimeMillis());
				out.close();
				return;
			}

			/* 获取数据库中的第一个域下的所有管理员用户，并使用ID值最小的一个管理员用户 */
			UserService userService = SpringContextHolder.getBean("userService");
			List<User> mgrLs = userService.getMgrByDomain(domain);
			User mgr = null;
			if(mgrLs.size() >= 1) {
				mgr = mgrLs.get(0);
			} else {
				logger.info("JRH--> 世元公司的CRM 使用Post 方式调用接口：failed cause by : This domain of call center system that don't have user of manager type![请求失败！原因：呼叫中心系统下该租户没有管理员类型的用户]");
				out.println("failed cause by : This domain of call center system that don't have user of manager type![请求失败！原因：呼叫中心系统下该租户没有管理员类型的用户] <br/><br/>");
				out.println("time tag : "+System.currentTimeMillis());
				out.close();
				return;
			}
			
			CustomerResourceBatchService customerResourceBatchService = SpringContextHolder.getBean("customerResourceBatchService");
			String batchName = SDF_DAY.format(new Date()) + "-CRM";
			List<CustomerResourceBatch> batchLs = customerResourceBatchService.getByName(batchName, domain);
			boolean isAppend = false;
			CustomerResourceBatch resourceBatch = null;
			if(batchLs.size() > 0) {
				resourceBatch = batchLs.get(0);
				isAppend = true;
			} else {
				resourceBatch = new CustomerResourceBatch();
				resourceBatch.setBatchName(batchName);
				resourceBatch.setBatchStatus(BatchStatus.USEABLE);
				resourceBatch.setCount(0L);
				resourceBatch.setCreateDate(new Date());
				resourceBatch.setCustomerResources(new HashSet<CustomerResource>());
				resourceBatch.setDomain(domain);
				resourceBatch.setNote("CRM第三方系统向EC2推送而来的资源");
				resourceBatch.setUser(mgr);
				resourceBatch = (CustomerResourceBatch) customerResourceBatchService.update(resourceBatch);
			}
			
			CustomerResourceService customerResourceService = SpringContextHolder.getBean("customerResourceService");
			CustomerResource customer = customerResourceService.getCustomerResourceByPhoneNumber(phonenum, domain.getId());
			if(customer == null) {
				customer = new CustomerResource();
				customer.setName(name);
				customer.setImportDate(new Date());
				customer.setDomain(domain);
				customer.setOwner(mgr);
				customer.setCount(0);
				customer.setExpireDate(new Date(0));
			} else if(customer.getAccountManager() == null) {	// 非成功客户，则更新相关基础信息
				customer.setName(name);
			}
			
			HashMap<String, String> descriptionMap = new HashMap<String, String>();
			descriptionMap.put("注册时间", regtime);
			descriptionMap.put("搜索来源", searchsrc);
			descriptionMap.put("自然来源", naturesrc);
			descriptionMap.put("计划", plan);
			descriptionMap.put("活动", activity);
			
			ImportResourceService importResourceService = SpringContextHolder.getBean("importResourceService");
			String result = importResourceService.importOneCustomer(isAppend, resourceBatch, customer, descriptionMap, phonenum, "", "");

			OperationLogUtil.simpleLog(mgr, "第三方CRM 调用Ec2的接口来插入客户资源，执行结果："+result);	// 添加操作日志
			
			logger.info("JRH--> 世元公司的CRM 使用Post 方式调用接口结果："+result);
			out.println(result+" <br/><br/>");
			out.println("time tag : "+System.currentTimeMillis());
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("JRH--> 世元石油公司调用接口导入资源出现异常-->"+e.getMessage(), e); 
			out.println("failed caused by : exception![请求失败！原因：出现异常] <br/><br/>");
			out.println("time tag : "+System.currentTimeMillis());
			out.close();
		}
	}

	/**
	 * @Description 描述：按指定过的编码获取请求中的参数，如果参数值为null，则返回空字符串
	 *
	 * @param request		HttpServletRequest
	 * @param param			参数名
	 * @param encodeType	返回信息的编码类型
	 * @return
	 * @throws UnsupportedEncodingException String
	 * 
	 * @author  JRH
	 * @date    2014年12月9日 下午2:01:35
	 */
	private String getParamValue(HttpServletRequest request, String param, String encodeType) throws UnsupportedEncodingException {
		String value = request.getParameter(param);
		if(!StringUtils.isEmpty(value)){
			value = new String(value.getBytes("ISO-8859-1"), encodeType);
		} else {
			value = "";
		}
		return value;
	}
	
	
    /**
     * 取出文件中的电话号码，如原始数据位 ‘ 135&%￥1676jrh0399  ’, 执行后返回 135167603989
     * 
     * 如果电话号码长度不在 3-14 位 的区间内，则返回空字符串，表示号码格式错误
     * 
     * @param originalPhoneNum     原始数据
     * @return
     */
    private String pickOutNumbers(String originalPhoneNum) {
          String phoneNum = "";
          originalPhoneNum = StringUtils.trimToEmpty(originalPhoneNum);
          for(int i = 0; i < originalPhoneNum.length(); i++) {
               char c = originalPhoneNum.charAt(i);
               int assiiCode = (int) c;
               if(assiiCode >= 48 && assiiCode <= 57) {
                    phoneNum = phoneNum + c;
               }
          }
          int len = phoneNum.length();
          if(len < 3 || len > 14) {	// 号码长度不正确
        	  return "";
          } 
          return phoneNum;
    }
    
}
