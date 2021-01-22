package com.jiangyifen.ec2.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.AddressService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.DomainService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.service.eaoservice.RoleService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * @author chb
 *
 */
public class Chen_Migration_DB {
	private static ApplicationContext ac;
	private static EntityManager em_now;
	private static EntityManager em_old;
	private static CustomerResourceBatchService batchService;
	private static CustomerResourceService resourceService;
	private static MarketingProjectTaskService taskService;
	private static CustomerServiceRecordService customerServiceRecordService;
	private static DomainService domainService;
	private static DepartmentService departmentService;
	private static RoleService roleService;
	private static UserService userService;
	private static CustomerServiceRecordStatusService customerServiceRecordStatusService;
	private static MarketingProjectService marketingProjectService;
	
	//域
	private static Domain domain;
	
	static{
		ac=new ClassPathXmlApplicationContext("config/applicationContext*.xml");
		em_now=(EntityManager)ac.getBean("entityManager");
		em_old=(EntityManager)ac.getBean("entityManager1");
		batchService=SpringContextHolder.getBean("customerResourceBatchService");
		resourceService=SpringContextHolder.getBean("customerResourceService");
		taskService=SpringContextHolder.getBean("marketingProjectTaskService");
		domainService=SpringContextHolder.getBean("domainService");
		departmentService=SpringContextHolder.getBean("departmentService");
		customerServiceRecordService=SpringContextHolder.getBean("customerServiceRecordService");
		roleService=SpringContextHolder.getBean("roleService");
		userService=SpringContextHolder.getBean("userService");
		customerServiceRecordStatusService=SpringContextHolder.getBean("customerServiceRecordStatusService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		
		//新建一个名为migrate的Domain
		persistNewDomain();
	}
	/**
	 * 代码执行
	 * @param args
	 */
	public static void main(String[] args) {
		//*******项目上线一定去掉导CDR**********//
//		if(DeveloperUtil.isImportCdr){
//			Liu_Import_cdr.importCdr(ac,em_now,em_old);
//		}
		migrateAll(true);
	}
	/**
	 * @param isAccessCreate 是否创建权限控制
	 */
	public static void migrateAll(Boolean isAccessCreate) {
		Date startDate=new Date();
		
		//迁移部门   Note:目前只能支持三级部门关系
		migrateDepartmentParent();
		
		//迁移角色
		migrateRole();
		
		//迁移用户
		migrateUser();
		
// TODO 不用迁移，实体类发生变化了 jrh
//		//迁移记录状态
//		migrateRecordStatus();
		
				
		/**
		 * 是否创建权限控制表
		 */
		//是否创建权限控制
		if(isAccessCreate){
			// 导入置忙原因
			Jiang_Import_PauseReason_test.createPauseReasonsToDB();
			// 导入呼叫记录的结果状态
			Jiang_Import_RecordStatus_test.importRecordStatus();
			//导入权限控制
			Jiang_Import_BussinessControl_test.importAllBusinessControl();
			//创建角色和权限控制的连接表
			Jiang_Import_Business_Role_Link_test.buildLink();
			//创建初始的默认表格头
			Chen_TableKey.createTableKey(domain);
		}
		
//		//迁移批次、新建项目、维护项目和用户的关系
//		migrateBatches();
		
		System.out.println("开始时间:"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(startDate));
		System.out.println("结束时间:"+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
	}

	/**
	 * Start
	 * 对SuperDepartment进行迁移   不对User和Role的连接表进行管理
	 */
	@SuppressWarnings("unchecked")
	private static void migrateDepartmentParent() {
System.out.println();
System.out.println("迁移父部门开始...");
		//查找并持久化父部门		部门名、部门描述 （ 父部门为null）
		List<Object[]> departmentsParent= em_old.createNativeQuery("select dept.departmentname,dept.description from ec_department as dept where dept.pdname is null").getResultList();
		for(Object[] deptTemp:departmentsParent){
			Department department=new Department();
			department.setDescription(deptTemp[1].toString());
			department.setDomain(domain);
			department.setName(deptTemp[0].toString());
			department.setParent(null);
			department.setRoles(null);
			department.setUsers(null);
			departmentService.save(department);
System.out.println("     "+"成功迁移父部门:"+department.getName());
			//将此部门的所有子部门持久化
			migrateDepartmentSub(department);
		}
System.out.println("迁移部门结束。");
System.out.println();
	}
	//对SubDepartment进行迁移   不对User和Role的连接表进行管理
	@SuppressWarnings("unchecked")
	private static void migrateDepartmentSub(Department department) {
		//查找并持久化父部门		部门名、部门描述 （ 父部门为null）
		List<Object[]> departmentsChild= em_old.createNativeQuery("select dept.departmentname,dept.description from ec_department as dept where dept.pdname='"+department.getName()+"'").getResultList();
		for(Object[] deptTemp:departmentsChild){
			Department dept=new Department();
			dept.setDescription(deptTemp[1].toString());
			dept.setDomain(domain);
			dept.setName(deptTemp[0].toString());
			dept.setParent(department);
			dept.setRoles(null);
			dept.setUsers(null);
			departmentService.save(dept);
			migrateDepartmentSub(dept);
		}
System.out.println("     "+"成功迁移此父部门中的子部门"+departmentsChild.size()+"个");
	}
	/**
	 * Start
	 * 迁移Role，对Deptment的连接表进行管理  不对User的连接表进行管理
	 */
	@SuppressWarnings("unchecked")
	private static void migrateRole() {
System.out.println();
System.out.println("迁移角色开始...");
		//查找并持久化角色		角色名、角色描述
		List<Object[]> roles= em_old.createNativeQuery("select role.rolename,role.description from ec_role as role").getResultList();
		for(Object[] role:roles){
			String roleName=role[0].toString();
			String description=role[1].toString();
			//创建Role
			Role roleTemp=new Role();
			roleTemp.setBusinessModels(null);
			roleTemp.setDescription(description);
			roleTemp.setDomain(domain);
			roleTemp.setName(roleName);
			if(roleName.equals("agent")){
				roleTemp.setType(RoleType.csr);
			}else{
				roleTemp.setType(RoleType.manager);
			}
			roleTemp.setUsers(null);
			//根据角色名查找角色对应的部门，以维护关系
			roleTemp.setDepartments(getDepartmentsByRoleName(roleName));
			roleService.save(roleTemp);
		}
System.out.println("成功迁移角色"+roles.size()+"个。");
System.out.println();
	}
	//根据角色名取得角色对应的所有部门,维护部门和角色的关系
	@SuppressWarnings("unchecked")
	private static Set<Department> getDepartmentsByRoleName(String rolename) {
		Set<Department> departments=new HashSet<Department>();
		List<String> deptNames= em_old.createNativeQuery("select link.departmentname from ec_role_department_link as link where link.rolename='"+rolename+"'").getResultList();
		for(String temp:deptNames){
			Department dept=(Department)em_now.createQuery("select d from Department as d where d.name='"+temp+"'").getSingleResult();
			departments.add(dept);
		}
System.out.println("     "+"角色"+rolename+"成功与"+departments.size()+"个部门建立了关联关系");
		return departments;
	}
	/**
	 * Start
	 * 迁移User，对Deptment和Role的连接表进行管理 
	 */
	@SuppressWarnings("unchecked")
	private static void migrateUser() {
System.out.println();
System.out.println("迁移CSR开始...");
		//查找并持久化用户		用户名、密码、电子邮件、姓名、工号、角色Id、部门Id
		List<Object[]> userStrs= em_old.createNativeQuery("select u.username,u.password,u.email,u.name,u.hid,u.rolename,u.departmentname from ec_user as u").getResultList();
		for(Object[] obj:userStrs){
			String userName="";
			String password="";
			String emailAddress="";
			String realName="";
			String hid="";
			String roleName="";
			String deptName="";
			try {
				userName=(String)obj[0];
				password=(String)obj[1];
				emailAddress=(String)obj[2];
				realName=(String)obj[3];
				hid=(String)obj[4];  //分机号
				roleName=(String)obj[5];
				deptName=(String)obj[6];
			} catch (Exception e) {
			}
			
			//创建User
			User user=new User();
			user.setAge(null);
			user.setDepartment(getDepartmentByDepartmentName(deptName));
			user.setDomain(domain);
			user.setEmailAddress(emailAddress);
			user.setEmpNo(hid);//工号和用户名相同
			user.setGender(null);
			user.setPassword(password);
			user.setPhoneNumber(null);
			user.setRealName(realName);
			user.setRegistedDate(null);
			user.setRoles(getRolesByRoleName(roleName));
			user.setUsername(userName);
			user.setRegistedDate(new Date());
			userService.save(user);
			System.out.println("     迁移CSR并创建部门关联关系、角色管理关系成功！");
		}
System.out.println("成功迁移"+userStrs.size()+"个CSR。");
System.out.println();
	}
	//根据部门名字取得相应的部门
	//根据用户名取得部门
	private static Department getDepartmentByDepartmentName(String departmentName) {
		Department dept=(Department)em_now.createQuery("select d from Department d where d.name='"+departmentName+"'").getSingleResult();
		return dept;
	}
	//根据角色名字取得相应的角色,将以前的一对一对应为一对多
	//根据角色名取得用户对应的角色
	private static Set<Role> getRolesByRoleName(String roleName) {
		Set<Role> roles=new HashSet<Role>();
		Role role=(Role)em_now.createQuery("select r from Role r where r.name='"+roleName+"'").getSingleResult();
		roles.add(role);
		return roles;
	}
	
	/**
	 * Start
	 * 迁移记录状态信息(RecordStatus)
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private static void migrateRecordStatus() {
System.out.println();
System.out.println("迁移状态开始...");
		List<Object[]> resultTypeStrs = em_old.createNativeQuery("select r.resulttype,r.banswered from eccrm_dialtask_resulttype as r").getResultList();
		for(Object[] resultTypeStr:resultTypeStrs){
			//是否应答
			Boolean isAnswered=(Boolean)resultTypeStr[1];
			//通话结果类型
			String statuName=resultTypeStr[0].toString();
			
			//创建相应的外呼状态
			CustomerServiceRecordStatus status=new CustomerServiceRecordStatus();
			status.setDomain(domain);
			status.setIsAnswered(isAnswered);
			status.setStatusName(statuName);
			status.setDirection("outgoing");
			customerServiceRecordStatusService.save(status);
		}
System.out.println("成功迁移了"+resultTypeStrs.size()+"个状态！");
System.out.println();
	}
	
	/**
	 * Start
	 * 迁移批次信息(Batch)
	 * 要求批次Id必须与项目Id对应
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	private static void migrateBatches() {
System.out.println();
System.out.println("迁移批次开始...");
		//批次Id、批次名、创建者、（创建者姓名）、导入日期、（开始日期）、（结束日期）、计数、（批次状态使用还是未使用）
		List records = em_old.createNativeQuery("select batch.name,batch.owner,batch.importdate,batch.count,batch.id,batch.state from eccrm_dialtask as batch").getResultList();
		for(Object obj:records){
			Object[] temp=(Object[])obj;
			String name=(String)temp[0]; //姓名
			String owner=(String)temp[1]; //创建者
			Date date=(Date)temp[2]; //导入日期
			Long count=(Long)temp[3]; //计数
			Long id=(Long)temp[4]; //以前批次表中的Id值
			Integer state=(Integer)temp[5]; //批次状态改为项目状态
			
			//包装成现在的新批次
			CustomerResourceBatch batch=new CustomerResourceBatch();
			batch.setBatchName(name);
			batch.setUser(parseStringToUser(owner));
			batch.setCreateDate(date);
			batch.setCount(count);
//			batch.setIsGenerated(false);
//			batch.setIsOriginal(false);
			batch.setDomain(domain);
			batchService.save(batch);
			
			//将每个批次映射成现有的一个项目
			MarketingProject project=new MarketingProject();
			Set<CustomerResourceBatch> batches=new HashSet<CustomerResourceBatch>();
			batches.add(batch);
			project.setBatches(batches);
			project.setCreateDate(date);
			project.setDomain(domain);
			if(state==0){
				project.setMarketingProjectStatus(MarketingProjectStatus.RUNNING);
			}else if(state==1){
				project.setMarketingProjectStatus(MarketingProjectStatus.OVER);
			}
			project.setNote(null);
			project.setProjectName(name);
			project.setCreater(parseStringToUser(owner));
			project.setUsers(parseUsersByBatch(batch,id));
			marketingProjectService.save(project);
			//迁移此批次对应的资源，并生成任务
			migrateBatchesResourceAndTask(id,batch,project);
		}
System.out.println("成功迁移了"+records.size()+"个批次！");
System.out.println("所有迁移结束！！！");
System.out.println("Success!!!");
	}
	//通过批次Id查找此批次里所有的CSR（批次--项目，也就是项目对应的用户）
	@SuppressWarnings("unchecked")
	private static Set<User> parseUsersByBatch(CustomerResourceBatch batch,Long id) {//后面Id为旧的Id
		List<String> csrNames= em_old.createNativeQuery("select distinct r.owner from eccrm_dialtask_item as r where r.dialtaskid="+id+" and r.owner is not null").getResultList();
		Set<User> csrs=new HashSet<User>();
		for(String csrName:csrNames){
			User user=(User)em_now.createQuery("select u from User u where u.username='"+csrName+"'").getSingleResult();
			csrs.add(user);
		}
System.out.println("     持久化批次"+batch.getBatchName()+"、项目共有一批数据和"+csrs.size()+"个CSR...");
		return csrs;
	}

	//将名字字符串转为user
	//根据Owner的字符串解析出相应的用户
	private static User parseStringToUser(String owner) {
		User user=(User)em_now.createQuery("select u from User u where u.username='"+owner+"'").getSingleResult();
		return user;
	}

	/**
	 * Start  By  migrateBatches()
	 * 迁移批次资源信息(BatchResource),同时生成资源表和任务表
	 * id 和 Batch 和 Project 呈线性关系，id为原来表中的Id值 
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private static void migrateBatchesResourceAndTask(Long id,CustomerResourceBatch batch,MarketingProject project) {
			System.out.println("     "+"     "+"持久化批次"+batch.getBatchName()+"的资源和任务...");
			//电话号1、电话号2、姓名、性别、生日、公司、省、城市、地址、描述信息、资源使用者工号、是否完成、是否应答、联系结果
			//将一个批次的所有资源放入集合，此集合可能有些大
			List<Object[]> resources= em_old.createNativeQuery("select resource.phonenumber,resource.phonenumber2,resource.name,resource.sex,resource.birthday,resource.company,resource.province,resource.city,resource.address,resource.remark,resource.owner,resource.isfinished,resource.isanswered,resource.result,resource.id from eccrm_dialtask_item as resource where resource.dialtaskid="+id).getResultList();
			
			for(Object[] obj:resources){
				Object[] temp=obj;
				//资源的普通信息
				String mobile=excludeZero((String)temp[0]); //手机
				String phone=excludeZero((String)temp[1]); //电话
				String name=(String)temp[2]; //姓名
				String sex=(String)temp[3]; //性别
				String birthday=(String)temp[4]; //生日
				String company=(String)temp[5]; //公司
				String province=(String)temp[6]; //省
				String city=(String)temp[7]; //城市
				String address=(String)temp[8]; //地址
				String remark=(String)temp[9]; //资源状态
				
				//资源的任务信息(生成Task的相关信息)
				String owner=(String)temp[10];
				Boolean isFinished=(Boolean)temp[11];
				Boolean isAnswered=(Boolean)temp[12];
				String result=(String)temp[13];
				Long resourceId=(Long)temp[14];
				
				//包装成现在的新批次
				CustomerResource resource=new CustomerResource();
				//TODO
//				resource.setBirthday(birthday);
//				resource.setCity(city);
//				resource.setProvince(province);
//				resource.setCompany(company);
//				resource.setIsCustomer(false);
//				resource.setCustomerResourceBatch(batch);
				//TODO
				//持久化一个新的Address
				Address initalAddress=new Address();
				initalAddress.setName(address);
				AddressService addressService=SpringContextHolder.getBean("addressService");
				initalAddress.setStreet(address);//.setName(address);
				addressService.saveAddress(initalAddress);
				resource.setDefaultAddress(initalAddress);
				Set<Address> addresses=new HashSet<Address>();
				addresses.add(initalAddress);
				resource.setAddresses(addresses);
				//TODO
//				resource.setMobile(mobile);
//				resource.setName(name);
//				resource.setNote(remark);
//				resource.setPhone(phone);
//				resource.setProvince(province);
//				resource.setSecretMobile(parseToSecretNumber(mobile));
//				resource.setSecretPhone(parseToSecretNumber(phone));
				//TODO
				resource.setSex(sex);
				resource.setDomain(domain);
				resourceService.save(resource);
				
				//向Task表中存储数据
				MarketingProjectTask task=new MarketingProjectTask();
				task.setCustomerResource(resource);
				task.setDomain(domain);//统一的域
				task.setIsFinished(isFinished);
				task.setIsAnswered(isAnswered);	
				task.setMarketingProject(project);//根据批次Id去决定项目Id，所以要求批次Id必须与项目Id对应
				task.setLastStatus(result);
				task.setUser(parseUser(owner));
				taskService.save(task);
				
				System.out.println("     "+"     "+"     "+"资源:"+resource.getName()+"  -- 任务:"+task.getId());
				//迁移与此条资源相关的记录
				migrateResourceRecord(resourceId,resource,project);
			}
			System.out.println("     "+"     "+"持久化批次"+batch.getBatchName()+"的资源和任务结束,共持久化了"+resources.size()+"个资源和任务！");
	}
	
	/**
	 * 将字符串前后的0去除
	 * @param contents
	 * @return
	 */
	private static String excludeZero(String contents) {
		if(contents==null){
			return null;
		}
		if(contents.trim().equals("")){
			contents=contents.trim();
			return contents;
		}
		int len = contents.length();
		int st = 0;
		int off = 0;
		char[] val = contents.toCharArray();

		while ((st < len) && (val[off + st] == '0')) {
		    st++;
		}
		return contents.substring(st, len-1);
	}
	
	//解析出来一个任务表中使用资源
	//根据工号解析任务执行者
	private static User parseUser(String owner) {
		if(owner==null) return null; //如果owner为空，则返回null
		User u=(User)em_now.createQuery("select u from User u where u.username='"+owner+"'").getSingleResult();
		return u;
	}

	/**
	 * Start  By  migrateBatches()  By  migrateBatchesResourceAndTask()
	 * 迁移批次资源的记录信息(Record)
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	private static void migrateResourceRecord(Long oldResourceId,CustomerResource resource,MarketingProject project) {
		//（记录Id）、（批次Id）、（资源Id）、姓名、电话、日期、是否应答、记录结果、记录者、记录内容
		List records = em_old.createNativeQuery("select record.name,record.tel,record.date,record.isanswered,record.result,record.owner,record.remark from eccrm_dialtask_service_record as record where record.dialtaskitemid="+oldResourceId).getResultList();
		for(Object obj:records){
			Object[] temp=(Object[])obj;

			String name=(String)temp[0]; //姓名
			String tel=(String)temp[1]; //电话
			Date createDate=(Date)temp[2]; //记录日期
			Boolean isAnswered=(Boolean)temp[3]; //是否应答
			String result=(String)temp[4]; //记录的结果
			String owner=(String)temp[5]; //谁创建的这条记录
			String recordContent=(String)temp[6];
			//包装成现在的新批次
			CustomerServiceRecord record=new CustomerServiceRecord();
			record.setCreateDate(createDate);
			record.setCreator(parseCreater(owner));
			record.setCustomerResource(resource);
			record.setDomain(domain);//使用同一个域
			record.setTitle("");
			record.setDirection("outgoing");
			record.setMarketingProject(project);
			record.setServiceRecordStatus(parseStatus(isAnswered,result));
			record.setRecordContent(recordContent);
			
			customerServiceRecordService.save(record);
		}
	}
	//解析标题

	//通过名字解析出哪个人写的这条记录，写记录的人不可能为null
	//通过记录者的名字解析为记录者
	private static User parseCreater(String owner) {
		User u=(User)em_now.createQuery("select u from User u where u.username='"+owner+"'").getSingleResult();
		return u;
	}


	//通过是否应答解析外呼状态
	@SuppressWarnings("unchecked")
	private static CustomerServiceRecordStatus parseStatus(
			Boolean isAnswered,String result) {
		//TODO chb 状态信息(eccrm_dialtask_resulttype) 和 Record(eccrm_dialtask_service_record  result字段) 里面的状态信息不一致  
		//于是，先查询如果有，则存Id，如果没有则新建状态，并存储
		List<CustomerServiceRecordStatus> serviceRecords=(List<CustomerServiceRecordStatus>)em_now.createQuery("select csrs from CustomerServiceRecordStatus csrs where csrs.statusName='"+result+"'").getResultList();
		if(serviceRecords.isEmpty()){
			CustomerServiceRecordStatus recordStatus=new CustomerServiceRecordStatus();
			recordStatus.setDomain(domain);
			recordStatus.setIsAnswered(isAnswered);
			recordStatus.setStatusName(result);
			customerServiceRecordStatusService.save(recordStatus);
			System.out.println("******"+"新建了一个状态");
			return recordStatus;
		}else{
			return serviceRecords.get(0);
		}
	}

	//====================初始化操作============================
	private static void persistNewDomain() {
		domain=new Domain();
		domain.setName("future oriental");
		domainService.saveDomain(domain);
	}
	/**
	 * 解析为带*字符串
	 * @param mobile
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String parseToSecretNumber(String mobile) {
		if(mobile==null) return null;
		if(mobile.length()>=11){
			return mobile.substring(0, mobile.length()-8)+"****"+mobile.substring(mobile.length()-4,mobile.length());
		}else if(mobile.length()>=7){
			return mobile.substring(0, mobile.length()-6)+"***"+mobile.substring(mobile.length()-3,mobile.length());
		}else{
			return mobile;
		}
	}
}



