package com.jiangyifen.ec2.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

//http://127.0.0.1:8080/ec2/trade/addresource?username=张三&mail=zhangsan@163.com&phonenum=15214563210&gender=男&location=上海

@SuppressWarnings("serial")
public class AddResourceServlet extends HttpServlet{
	private Logger logger=LoggerFactory.getLogger(getClass());
	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
		
		//super.service(httpServletRequest, httpServletResponse);
		PrintWriter out = httpServletResponse.getWriter();
		
		try {
			//获取信息
			String username = httpServletRequest.getParameter("username");
			if(!StringUtils.isEmpty(username)){
				username=new String(username.getBytes("ISO-8859-1"),"utf-8");
			}else{
				username="";
			}
			String phonenum = httpServletRequest.getParameter("phonenum");
			if(!StringUtils.isEmpty(phonenum)){
				phonenum=new String(phonenum.getBytes("ISO-8859-1"),"utf-8");
			}else{
				phonenum="";
			}
			String gender = httpServletRequest.getParameter("gender");
			if(!StringUtils.isEmpty(gender)){
				gender=new String(gender.getBytes("ISO-8859-1"),"utf-8");
			}else{
				gender="";
			}
			String mail = httpServletRequest.getParameter("mail");
			if(!StringUtils.isEmpty(mail)){
				mail=new String(mail.getBytes("ISO-8859-1"),"utf-8");
			}else{
				mail="";
			}
			String location= httpServletRequest.getParameter("location");
			if(!StringUtils.isEmpty(location)){
				location=new String(location.getBytes("ISO-8859-1"),"utf-8");
			}else{
				location="";
			}
			String trader= httpServletRequest.getParameter("trader");
			if(!StringUtils.isEmpty(trader)){
				trader=new String(trader.getBytes("ISO-8859-1"),"utf-8");
			}else{
				trader="";
			}
			
logger.info("chb: http://www.1trade.com.cn/-->username:"+username);
logger.info("chb: http://www.1trade.com.cn/-->mail:"+mail);
logger.info("chb: http://www.1trade.com.cn/-->phonenum:"+phonenum);
logger.info("chb: http://www.1trade.com.cn/-->gender:"+gender);
logger.info("chb: http://www.1trade.com.cn/-->location:"+location);
logger.info("chb: http://www.1trade.com.cn/-->trader:"+trader);

			//获取commonService
			CommonService commonService=SpringContextHolder.getBean("commonService");
			if(commonService==null){
logger.error("chb: http://www.1trade.com.cn/-->commonService-null");
			}
			
			//获取domain和用户user
			Domain domain=null;
			String sql="select d from Domain d";
			List<Domain> domainList = (List<Domain>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
//			if(domainList.size()!=1){
//logger.error("chb: http://www.1trade.com.cn/-->domaincount:"+domainList.size());
//				return;
//			}else{
//			}
			domain=domainList.get(0); //TODO

			User user=null;
			String sql2="select u from User u where u.username='mgr'";
			user = (User)commonService.excuteSql(sql2, ExecuteType.SINGLE_RESULT);
			if(user==null){
logger.error("chb: http://www.1trade.com.cn/-->user:null");
				return;
			}
			
			//================================获取批次、如果不存在则创建批次
			CustomerResourceBatch customerResourceBatch=commonService.get(CustomerResourceBatch.class, 1000000L); //100万号批次为指定的批次
			if(customerResourceBatch==null){
				customerResourceBatch=new CustomerResourceBatch();
				customerResourceBatch.setBatchName("网站注册用户");
				customerResourceBatch.setBatchStatus(BatchStatus.USEABLE);
				customerResourceBatch.setCount(0L);
				customerResourceBatch.setCreateDate(new Date());
				customerResourceBatch.setCustomerResources(new HashSet<CustomerResource>());
				customerResourceBatch.setDomain(domain);
				customerResourceBatch.setId(1000000L);
				customerResourceBatch.setNote("网站注册用户的资料信息");
				customerResourceBatch.setUser(user);
				customerResourceBatch=(CustomerResourceBatch)commonService.update(customerResourceBatch);
logger.error("chb: http://www.1trade.com.cn/-->new customerResourceBatch:"+customerResourceBatch.getBatchName());
			}
			
			//================================获取项目、如果不存在则创建项目
			MarketingProject marketingProject=commonService.get(MarketingProject.class, 97L); //100万号项目为指定的项目
			if(marketingProject==null){
				marketingProject=new MarketingProject();
				Set<CustomerResourceBatch> batches=new HashSet<CustomerResourceBatch>();
				batches.add(customerResourceBatch);
				marketingProject.setBatches(batches);
				marketingProject.setCreateDate(new Date());
				marketingProject.setCommissionerRouting(MarketingProject.COMMISSIONER_ROUTING_OFF);
				marketingProject.setCreater(user);
				marketingProject.setCsrMaxUnfinishedTaskCount(20);
				marketingProject.setCsrOnceMaxPickTaskCount(5);
				marketingProject.setDomain(domain);
				marketingProject.setId(1000000L);
				marketingProject.setMarketingProjectStatus(MarketingProjectStatus.RUNNING);
				marketingProject.setMarketingProjectType(MarketingProjectType.MARKETING);
				marketingProject.setNote("网站注册添加用户到此项目");
				marketingProject.setProjectName("网站注册");
				marketingProject.setQueue(null);
				marketingProject.setSip(null);
				marketingProject.setUsers(null);
				marketingProject=(MarketingProject)commonService.update(marketingProject);
logger.error("chb: http://www.1trade.com.cn/-->new marketingProject:"+marketingProject.getProjectName());
			}
			
			//================================生成资源、添加到批次和项目
			CustomerResource customerResource=new CustomerResource();
			customerResource.setName(username);
			customerResource.setImportDate(new Date());
			customerResource.setSex(gender);
			customerResource.setDomain(domain);
			customerResource=(CustomerResource)commonService.update(customerResource);
			
			Telephone telephone=new Telephone();
			telephone.setNumber(phonenum);
			telephone.setCustomerResource(customerResource);
			telephone.setDomain(domain);
			telephone=(Telephone)commonService.update(telephone);
			Set<Telephone> telephones=new HashSet<Telephone>();
			telephones.add(telephone);
			customerResource.setTelephones(telephones);
			
			Set<CustomerResourceBatch> customerResourceBatches=new HashSet<CustomerResourceBatch>();
			customerResourceBatches.add(customerResourceBatch);
			customerResource.setCustomerResourceBatches(customerResourceBatches);
			
			//存储CustomerResource
			customerResource=(CustomerResource)commonService.update(customerResource);
logger.error("chb: http://www.1trade.com.cn/-->new customerResource:"+customerResource.getName());
			
			CustomerResourceDescription customerResourceDescription=new CustomerResourceDescription();
			customerResourceDescription.setKey("email");
			customerResourceDescription.setValue(mail);
			customerResourceDescription.setDomain(domain);
			customerResourceDescription.setCustomerResource(customerResource);
			customerResourceDescription=(CustomerResourceDescription)commonService.update(customerResourceDescription);
logger.error("chb: http://www.1trade.com.cn/-->new customerResourceDescription:"+customerResourceDescription.getValue());

			CustomerResourceDescription customerResourceDescription1=new CustomerResourceDescription();
			customerResourceDescription1.setKey("所在地区");
			customerResourceDescription1.setValue(location);
			customerResourceDescription1.setDomain(domain);
			customerResourceDescription1.setCustomerResource(customerResource);
			customerResourceDescription1=(CustomerResourceDescription)commonService.update(customerResourceDescription1);
logger.error("chb: http://www.1trade.com.cn/-->new customerResourceDescription:"+customerResourceDescription1.getValue());
			
			CustomerResourceDescription customerResourceDescription2=new CustomerResourceDescription();
			customerResourceDescription2.setKey("券商");
			customerResourceDescription2.setValue(trader);
			customerResourceDescription2.setDomain(domain);
			customerResourceDescription2.setCustomerResource(customerResource);
			customerResourceDescription2=(CustomerResourceDescription)commonService.update(customerResourceDescription2);
logger.error("chb: http://www.1trade.com.cn/-->new customerResourceDescription:"+customerResourceDescription2.getValue());

			//================================生成Task
			MarketingProjectTask marketingProjectTask=new MarketingProjectTask();
			marketingProjectTask.setBatchId(customerResourceBatch.getId());
			marketingProjectTask.setCreateTime(new Date());
			marketingProjectTask.setCustomerResource(customerResource);
			marketingProjectTask.setDomain(domain);
			marketingProjectTask.setIsFinished(false);
			marketingProjectTask.setIsUseable(true);
			marketingProjectTask.setLastStatus(null);
			marketingProjectTask.setLastUpdateDate(new Date());
			marketingProjectTask.setMarketingProject(marketingProject);
			marketingProjectTask.setMarketingProjectTaskType(MarketingProjectTaskType.MARKETING);
			commonService.update(marketingProjectTask);
		} catch (Exception e) {
			e.printStackTrace();
			out.println("failed");
			out.close();
		}
		
		//======================向客户端返回结果============================//
		out.println("success");
		out.close();
	}
}
