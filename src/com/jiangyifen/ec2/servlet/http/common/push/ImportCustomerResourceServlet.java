package com.jiangyifen.ec2.servlet.http.common.push;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.alibaba.fastjson.JSON;
import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.mgr.ImportResourceService;
import com.jiangyifen.ec2.servlet.http.common.pojo.PushResourceBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.FastJsonUtil;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：该接口用来推送客户资源信息
 * 
 * 	功能适用对象必须满足以下条件：
 * 		1、在推送客户资源的时候，该资源要保存到的域下面，必须至少存在一个管理员类型的用户
 *		2、推送过来的资源中必须包含有效的手机号码
 *		3、由于系统配置的自动创建批次是关闭的，所以这里批次名称也是一定要填写的，并且这个批次一定是在系统中存在的，否则是推送不成功的
 *
 *	作用：
 *		1、由第三方系统来调用此接口推送客户资源保存到我方 EC2 呼叫中心系统中，推送过来的数据以 JSON 格式进行
 *		2、批次信息的处理，从 JSON 中取得批次的名称，如果系统中存在该批次的名称，则将此资源导入到该批次中
 *		3、多个描述信息字段，放到 JSON 中
 *		4、可以配置推送数据按照每天创建一个批次、每月创建一个批次或者每年创建一个批次进行配置，配置信息在 iface_joint.properties 文件中有说明
 *
 *	调用方式：
 *		http://192.168.1.160:8088/ec2/http/common/push/importCustomerResource?accessId=xxxxx&accessKey=xxxxxx&json=xxxxxxx
 *
 *	测试样例：
 *		http://192.168.1.160:8088/ec2/http/common/push/importCustomerResource?accessId=xxxxx&accessKey=xxxxxx&json={"addresses":[{"mobile":"","name":"","postCode":"","street":"上海马戏城 1 号口"}],"birthday":"1992-02-07 12:00:00","customerResourceBatch":"批次测试","customer_source":"网络来源","descs":[{"createDate":"1970-01-01 00:00:00","key":"邮箱","lastUpdateDate":"1970-01-01 00:00:00","value":"abatchfood@yahoo.com"},{"createDate":"1970-01-01 00:00:00","key":"爱好","lastUpdateDate":"1970-01-01 00:00:00","value":"play basketball"}],"domainId":"1","name":"王萌萌","note":"预留字段","sex":"男","telephones":[{"number":"13713215827"},{"number":"13923175802"}]}
 *
 *	数据编码格式：
 *		UTF-8
 *
 *	参数描述：
 *		参数名称			含义				参数类型			参数值			非空约束		涉及到的 EC2 表
 *
 *
 *	想要做的更灵活性的配置：
 *		1、客户可以自己选择推送资源过来的时候，这条资源如果已经在数据库中存在，那么将不保存此资源，或者按已经存在的资源来更新，或者按当前的资源更新
 *		2、推送过来的资源和批次关联，是否添加到项目对应的任务
 *
 * @author jinht
 *
 * @date 2015-6-23 上午9:40:31 
 *
 */
@SuppressWarnings("serial")
public class ImportCustomerResourceServlet extends HttpServlet {

	/**
	 * 编码格式
	 */
	private final String ENCODE_TYPE = "UTF-8";
	/**
	 * 日期转换工具类
	 */
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyyMMdd");
	private final SimpleDateFormat SDF_MONTH = new SimpleDateFormat("yyyyMM");
	private final SimpleDateFormat SDF_YEAR = new SimpleDateFormat("yyyy");
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset="+ENCODE_TYPE);	// 不然中文会出现乱码
		PrintWriter out = response.getWriter();
		out.println("failed caused by : We don't support the 'get' request! Please use the 'post' request! [请求失败！原因：不支持 get 请求，请使用 post 请求方式] <br/><br/>");
		out.println("time tag : " + System.currentTimeMillis());
		out.close();
		logger.info("jinht --> 使用 get 请求方式调用 EC2 导入资源接口失败，我们不支持 get 请求！We don't support the 'get' request !");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.executeBusiness(request, response);
	}
	
	/**
	 * @throws Exception 
	 * @Description 描述：执行业务处理
	 */
	private void executeBusiness(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/html; charset="+ENCODE_TYPE);	// 不然中文会出现乱码
		PrintWriter out = response.getWriter();
		
		try {
			String json = this.getParamValue(request, "json", ENCODE_TYPE);
			logger.info(json);
			PushResourceBo pushResourceBo = JSON.parseObject(json, PushResourceBo.class);
			
			/* 判断推送资源的 POJO 类，是否转换正常 */
			if(pushResourceBo == null) {
				logger.info("jinht --> 使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的参数不符合要求！"+json);
				out.println("failed cause by : Call EC2 import resource interface failure using post request , The parameters passed in the interface do not meet the requirements. [请求失败！原因：使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的参数不符合要求！] <br/><br/>");
				out.println("time tag : " + System.currentTimeMillis());
				out.close();
				return;
			}
			
			/* 判断推送资源要保存到的域 */
			String domainIdStr = pushResourceBo.getDomainId();
			if(domainIdStr == null || "".equals(domainIdStr)) {
				logger.info("jinht --> 使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的租户编号不能为空！"+json);
				out.println("使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的租户编号不能为空！ <br/><br/>");
				out.println("time tag : " + System.currentTimeMillis());
				out.close();
				return;
			}
			
			Long domainId = Long.valueOf(domainIdStr);
			UserService userService = SpringContextHolder.getBean("userService");		// 获取数据库中的第一个域下的所有管理员用户，并使用ID值最小的一个管理员用户
			User mgrUser = userService.getMgrByDomainId(domainId);
			if(mgrUser == null) {
				logger.info("jinht --> 使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的租户编号下找不到管理员账户！"+json);
				out.println("使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的租户编号下找不到管理员账户！ <br/><br/>");
				out.println("time tag : " + System.currentTimeMillis());
				out.close();
				return;
			}
			
			/* 判断电话号码是否有效 */
			if(pushResourceBo.getTelephones() == null || pushResourceBo.getTelephones().size() < 1) {
				logger.info("jinht --> 使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的电话号码参数为空！"+json);
				out.println("使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的电话号码参数为空！"+json+"<br/><br/>");
				out.println("time tag : " + System.currentTimeMillis());
				out.close();
				return;
			}
			
			List<Telephone> telephonesList = new ArrayList<Telephone>();
			for(Telephone telephone : pushResourceBo.getTelephones()) {
				if(telephone != null) {		// 判断这个电话号码实体类是否为空
					String phoneNum = this.pickOutNumbers(telephone.getNumber());		// 提取电话号码中有效的内容
					if(!"".equals(phoneNum)) {		// 提取出来的电话号码如果不为空字符串，则添加到电话号码的集合中
						telephonesList.add(telephone);
					}
				}
			}
			
			if(telephonesList.size() == 0) {
				logger.info("jinht --> 使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的电话号码不是有效的数据！[电话号码不能为空，并且只能是3-14数字]"+json);
				out.println("使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的电话号码不是有效的数据！[电话号码不能为空，并且只能是3-14数字]"+json+"<br/><br/>");
				out.println("time tag : " + System.currentTimeMillis());
				out.close();
				return;
			}
			
			/* 资源导入的批次 */
			CustomerResourceBatchService customerResourceBatchService = SpringContextHolder.getBean("customerResourceBatchService");
			CustomerResourceBatch resourceBatch = null;
			
			boolean isAppend = false;		// 是否为追加资源
			
			if("true".equals(AnalyzeIfaceJointUtil.PUSH_RESOURCE_IS_CREATE_BATCH)) {		// 推送过来的数据是否按照每天一个批次进行创建保存数据
				String batchName = "";
				Date date = new Date();
				if("day".equals(AnalyzeIfaceJointUtil.PUSH_RESOURCE_CREATE_BATCH_DATE)) {
					batchName = SDF_DAY.format(date);
				} else if("month".equals(AnalyzeIfaceJointUtil.PUSH_RESOURCE_CREATE_BATCH_DATE)) {
					batchName = SDF_MONTH.format(date);
				} else if("year".equals(AnalyzeIfaceJointUtil.PUSH_RESOURCE_CREATE_BATCH_DATE)) {
					batchName = SDF_YEAR.format(date);
				} else {	// 如果以上条件都不满足，则表示按天进行创建批次
					batchName = SDF_DAY.format(date);
				}
				batchName = batchName+AnalyzeIfaceJointUtil.PUSH_RESOURCE_CREATE_BATCH_END_NAME;
				resourceBatch = customerResourceBatchService.getBatchByBatchName(batchName, mgrUser.getDomain());
				if(resourceBatch == null) {
					resourceBatch = new CustomerResourceBatch();
					resourceBatch.setBatchName(batchName);
					resourceBatch.setBatchStatus(BatchStatus.USEABLE);
					resourceBatch.setCount(0L);
					resourceBatch.setCreateDate(date);
					resourceBatch.setCustomerResources(new HashSet<CustomerResource>());
					resourceBatch.setDomain(mgrUser.getDomain());
					resourceBatch.setNote("CRM 第三方系统向 EC2 推送而来的资源");
					resourceBatch.setUser(mgrUser);
				}
				resourceBatch = (CustomerResourceBatch) customerResourceBatchService.update(resourceBatch);
			} else {
				String batchName = pushResourceBo.getCustomerResourceBatch();	// 获取传送进来的批次名称
				resourceBatch = customerResourceBatchService.getBatchByBatchName(batchName, mgrUser.getDomain());		// 这里获取到的批次集合，是按照 id 升序排列的，所以当获取第一个时，就会出现根据最早添加的批次进行导入该资源，这样明显是不合理的，所以这里要获取最新添加的那个批次
				if(resourceBatch == null) {
					logger.info("jinht --> 使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的批次的名称在系统中不存在！"+json);
					out.println("使用 post 请求方式调用 EC2 导入资源接口失败，接口中传递的批次的名称在系统中不存在！"+json+"<br/><br/>");
					out.println("time tag : " + System.currentTimeMillis());
					out.close();
					return;
				} else {
					isAppend = true;
				}
			}
			
			/* 客户资源信息设置 */
			CustomerResourceService customerResourceService = SpringContextHolder.getBean("customerResourceService");
			CustomerResource customer = null;
			for(Telephone phoneNumber : telephonesList) {	// 根据电话号码进行查询看是否系统中已经存在这个客户了
				customer = customerResourceService.getCustomerResourceByPhoneNumber(phoneNumber.getNumber(), domainId);
				if(customer != null) {
					break;
				}
			}
			if(customer == null) {	// 如果还是为空，就创建一个客户资源的对象
				customer = new CustomerResource();
				customer.setImportDate(new Date());
				customer.setDomain(mgrUser.getDomain());
				customer.setCount(0);
				customer.setExpireDate(new Date(0));
			}
			customer.setName(pushResourceBo.getName());
			if(customer.getOwner() == null) {
				customer.setOwner(mgrUser);
			}
			if(customer.getAccountManager() == null) {
				customer.setAccountManager(mgrUser);
			}
			if(!"".equals(StringUtils.trimToEmpty(pushResourceBo.getBirthday()))) {
				customer.setBirthday(pushResourceBo.getBirthday());
			}
			if(!"".equals(StringUtils.trimToEmpty(pushResourceBo.getCustomer_source()))) {
				customer.setCustomer_source(pushResourceBo.getCustomer_source());
			}
			if(!"".equals(StringUtils.trimToEmpty(pushResourceBo.getNote()))) {
				customer.setNote(pushResourceBo.getNote());
			}
			if(!"".equals(StringUtils.trimToEmpty(pushResourceBo.getSex()))) {
				customer.setSex(pushResourceBo.getSex());
			}
			
			/*customer.setTelephones(new HashSet<Telephone>(telephonesList));
			if(pushResourceBo.getAddresses() != null && pushResourceBo.getAddresses().size() != 0) {
				customer.setAddresses(new HashSet<Address>(pushResourceBo.getAddresses()));
			}*/
			
			/* 设置描述信息字段 */
			HashMap<String, String> descriptionMap = new HashMap<String, String>();
			for(CustomerResourceDescription description : pushResourceBo.getDescs()) {
				if(description != null) {
					descriptionMap.put(description.getKey(), description.getValue());
				}
			}
			
			/* 执行导入资源到项目中 */
			ImportResourceService importResourceService = SpringContextHolder.getBean("importResourceService");
			String result = importResourceService.importOneCustomer(isAppend, resourceBatch, customer, descriptionMap, telephonesList, pushResourceBo.getAddresses(), pushResourceBo.getCompanyName());
			
			OperationLogUtil.simpleLog(mgrUser, "第三方 CRM 调用 Ec2 的接口来插入客户资源，执行结果："+result);	// 添加操作日志
			
			logger.info("jinht --> 第三方 CRM 使用Post 方式调用接口结果："+result);
			out.println(result+" <br/><br/>");
			out.println("time tag : "+System.currentTimeMillis());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jinht --> 第三方 CRM 调用接口导入资源出现异常-->"+e.getMessage(), e); 
			out.println("failed caused by : exception![请求失败！原因：出现异常] <br/><br/>");
			out.println("time tag : "+System.currentTimeMillis());
			out.close();
		}
		
	}

	/**
	 * @Description 描述：按指定的编码获取请求中的参数，如果参数值为 null，则直接返回空字符串
	 *
	 * @param request			HttpServletRequest
	 * @param param				参数名称
	 * @param encodeType		返回信息的编码类型
	 * @return					返回的结果
	 * @throws Exception
	 */
	private String getParamValue(HttpServletRequest request, String param, String encodeType) throws Exception {
		String value = request.getParameter(param);
		if(!StringUtils.isEmpty(value)) {
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
	
	
	/**
	 * test 
	 */
	public static void main(String[] args) {
		
		Telephone telephone1 = new Telephone();
		telephone1.setNumber("13713215827");
		
//		Telephone telephone2 = new Telephone();
//		telephone2.setNumber("15823195737");
		
		List<Telephone> telephones = new ArrayList<Telephone>();
		telephones.add(telephone1);
//		telephones.add(telephone2);
		
		Address address = new Address();
		address.setStreet("共和新路 2750 弄 2 号楼 502 室");
		
		List<Address> addresses = new ArrayList<Address>();
		addresses.add(address);
		
		CustomerResourceDescription description1 = new CustomerResourceDescription();
		description1.setKey("邮箱");
		description1.setValue("abatchfood@yahoo.com");
		
		CustomerResourceDescription description2 = new CustomerResourceDescription();
		description2.setKey("爱好");
		description2.setValue("play basketball");
		
		List<CustomerResourceDescription> descriptions = new ArrayList<CustomerResourceDescription>();
		descriptions.add(description1);
		descriptions.add(description2);
		
		PushResourceBo pushResourceBo = new PushResourceBo();
		pushResourceBo.setTelephones(telephones);
		pushResourceBo.setAddresses(addresses);
		pushResourceBo.setDescs(descriptions);
		
		pushResourceBo.setBirthday("1992-02-07 12:00:00");
		pushResourceBo.setName("王萌萌");
		pushResourceBo.setSex("男");
		pushResourceBo.setNote("预留字段");
		pushResourceBo.setCustomer_source("网络来源");
		pushResourceBo.setCustomerResourceBatch("批次测试");
		pushResourceBo.setDomainId("1");
		
		// json 格式：{"addresses":[{"mobile":"","name":"共和新路 2750 弄 2 号楼 502 室","postCode":"","street":""}],"birthday":"1992-02-07 12:00:00","customerResourceBatch":"带整顿","customer_source":"网络来源","descriptions":[{"createDate":"1970-01-01 00:00:00","key":"邮箱","lastUpdateDate":"1970-01-01 00:00:00","value":"958867874@qq.com"},{"createDate":"1970-01-01 00:00:00","key":"爱好","lastUpdateDate":"1970-01-01 00:00:00","value":"play basketball"}],"name":"小明","note":"预留字段","sex":"男","telephones":[{"number":"15824735464"}]}
		/*{
			
			"name":"小明",
			"sex":"男",
			"customerResourceBatch":"带整顿",
			"customer_source":"网络来源",
			"note":"预留字段",
			"birthday":"1992-02-07 12:00:00",
			"telephones":[
				{
					"number":"15824735464"
				}
			],
			"addresses":[
				{
					"name":"共和新路 2750 弄 2 号楼 502 室"
				}
			],
			"descriptions":[
				{
					"key":"邮箱","value":"958867874@qq.com"
				},
				{
					"key":"爱好","value":"play basketball"
				}
			]
		}*/
		System.out.println(FastJsonUtil.toJson(pushResourceBo));
		
		String json = FastJsonUtil.toJson(pushResourceBo);
		
		PushResourceBo pushResourceBo1 = JSON.parseObject(json, PushResourceBo.class);
		System.out.println(pushResourceBo1);
		System.out.println(pushResourceBo1.getAddresses().get(0).getName());
		
		ImportCustomerResourceServlet importCustomerResourceServlet = new ImportCustomerResourceServlet();
		
		System.out.println("--"+(importCustomerResourceServlet.pickOutNumbers(null)==null) + "--");
		
		System.out.println(new Date(0));
		
	}
	
	
}
