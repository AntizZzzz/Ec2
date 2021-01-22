package com.jiangyifen.ec2.test;


import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;

public class Jiang_EntityTest {
	
	private static EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
	
	private static EntityManager em = entityMangerFactory.createEntityManager();
	
	public static void main(String[] args) {
		em.getTransaction().begin();
		
		testUser_Role();
		testUser_Role1();
		testQueryUser();
		
		testPageEntitys();
		testUserPageEntitys();
		testMergeVsPersist();
		testGetSearchNumber();
		
		testIsParent();
		testSelectSQL();
		testSelectSQL1();
		testSelectSQL2();
		testSelectSQL3();
		testSelectSQL4();
		testSelectSQL5();
		searchSpecificUserNotice();
		testCountEntity();
		
		testDomainsConfig();
		
		testGetSipConfig();
		
		testGetQueue();
		
		em.getTransaction().commit();
		em.close();
		entityMangerFactory.close();
	}
	
	@SuppressWarnings("unchecked")
	private static void testGetQueue() {
		List<Queue> queues = em.createQuery("select sip from Queue as sip where sip.name = 'baikafei'").getResultList();
		Queue queue = queues.get(0);
		System.out.println(queue);
	}

	@SuppressWarnings("unchecked")
	private static void testGetSipConfig() {
		List<SipConfig> sips = em.createQuery("select sip from SipConfig as sip where sip.name = '8066'").getResultList();
		SipConfig sip = sips.get(0);
		System.out.println(sip);
	}

	@SuppressWarnings("unchecked")
	private static void testDomainsConfig() {
//		Domain d = em.find(Domain.class, (long)4);
//		for(Ec2Configuration ec : d.getConfigs()) {
//			System.out.println(ec.getKey() + "---" + ec.getValue());
//		}
		
//		List<Notice> notices = em.createQuery("").getResultList();
		
//		List<Notice> notices = em.createQuery("select n from Notice as n where n.id in ( select ni.notice.id from NoticeItem as ni where ni.user.id = 30 )").getResultList();
//		for(Notice notice : notices) {
//			System.out.println(notice.getId()+"---->"+notice.getTitle());
//		}
		
		List<BusinessModel> models = em.createQuery("select model " +
				"from BusinessModel as model").getResultList();
		System.out.println(models.size() + "===========fdgsdfgsdfg-----------------============");
		for(BusinessModel m : models) {
			System.out.println(m);
		}
		
		
//		em.createQuery("select ni.notice.id from NoticeItem as ni where ni.user.id = 30");
	}

	@SuppressWarnings("unchecked")
	private static void searchSpecificUserNotice() {
		
		List<NoticeItem> list = em.createQuery("select ni from NoticeItem as ni " +
				"where ni.user.id = 30 order by ni.notice.sendDate desc").getResultList();
		for(NoticeItem item : list) {
			System.out.println("===="+item.getNotice().getId());
		}
		em.createQuery("select ni from NoticeItem as ni where ni.user.id = 30 and ni.newNotice = true ").getResultList();
	}

	@SuppressWarnings("unchecked")
	private static void testSelectSQL5() {
		List<CustomerServiceRecord> records = em.createQuery("select r from OutgoingRecord as r " +
				"where creator.id = 9 and domain = creator.domain and marketingProject.id = 2 and customerResource.id = 1 " +
				"order by createDate desc ").getResultList();
		
		for(CustomerServiceRecord record : records) {
			System.out.println(record.getId()+"  --  "+record.getRecordContent()+"  --  "+record.getCreateDate());
		}
		
		CustomerServiceRecord record = (CustomerServiceRecord) em.createQuery("select r from OutgoingRecord as r " +
				"where creator.id = 9 and domain = creator.domain and marketingProject.id = 2 and customerResource.id = 1 " +
				"order by createDate desc ").getResultList().get(0);
		System.out.println("\n  "+record.getId()+"  --  "+record.getRecordContent()+"  --  "+record.getCreateDate());
		
		
		
		
		
	}

	@SuppressWarnings("unchecked")
	private static void testSelectSQL4() {
		User user = em.find(User.class, (long)9);
		List<MarketingProject> list = em.createQuery("select distinct t.marketingProject from MarketingProjectTask as t where t.user.id ="+user.getId()).getResultList();

		for(int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
		
		for(MarketingProject p : list) {
			System.out.println(p+"..............");
		}
	
	}

	@SuppressWarnings("rawtypes")
	private static void testSelectSQL3() {
		User user = em.find(User.class, (long)9);//  where t.user.id ="+user.getId()
		List list = em.createQuery("select t from MarketingProjectTask as t where t.user.id ="+user.getId()).getResultList();
		System.out.println(list.size()+"----------");
		
		System.out.println(em.createQuery("select status from CustomerServiceRecordStatus as status " +
				"where status.domain.id = " + em.find(Domain.class, (long)1).getId()).getResultList().size());
	}

	private static void testCountEntity() {
		Long count = (Long) em.createQuery("Select count(*) from User as user where user.domain.id = 1").getSingleResult();
		System.out.println(count+"<--阿里巴巴1的职员总数");
		
		@SuppressWarnings("unchecked")
		List<Long> domainCounts = em.createQuery("Select count(*) from User as user group by user.domain.id").getResultList();
		
		for(int i = 1; i <= domainCounts.size(); i++) {
			System.out.println(domainCounts.get(i-1) + "<-- 第"+i+"个 Domain 的职员总数");
		}
		
		Long userCount = (Long) em.createQuery("Select count(*) from User as user").getSingleResult();
		System.out.println(userCount+"<--用户 User 的总数");
		
		String sql1 = "Select user from User as user where user.domain.id = 1";
		String sql3 = "Select count(*) from User as user where user in ( " + sql1 + " ) and user.realName like '%姓%'" +
				" or user.username like '%姓%' or user.empNo like '%姓%' or user.department.name = '姓'";
		System.out.println(em.createQuery(sql3) +"......................");
		Long userCount1 = (Long) em.createQuery(sql3).getSingleResult();
		System.out.println(userCount1+"<--用户 User 的总数");
		
		
	}

	@SuppressWarnings("unchecked")
	private static void testSelectSQL2() {
		List<BusinessModel> models = em.createQuery("Select bm from BusinessModel as bm").getResultList();
		for(BusinessModel model : models) {
			System.out.println(model);
		}
	}
	//	   
	@SuppressWarnings("unchecked")
	private static void testSelectSQL1() {
		Query query = em.createQuery("Select user from User as user where user.realName like '%部门100%' " +
				"or user.username like '%部门100%' or user.empNo like '%部门100%' or user.department.name like '%部门100%'");
		List<User> result = query.getResultList();
		for(User user : result) {
			System.out.println(user);
		}
	}

	@SuppressWarnings("unchecked")
	private static void testSelectSQL() {
		Query query = em.createQuery("Select dept from Department as dept order by dept.id desc");
		Query query1 = em.createQuery("Select count(*) from Department");
		
		List<Department> result = query.getResultList();
		for(Department dept : result) {
			System.out.println(dept);
			System.out.println(query1.getSingleResult());
		}
	}

	private static void testMergeVsPersist() {
		Department dept = new Department();
		dept.setName("测试merge和 persist");
		dept.setDomain(em.find(Domain.class, (long)1));
		em.merge(dept);
		
	}
	
	private static void testGetSearchNumber() {
		long deptId = 1;
		try {
			String str = "部门";
			deptId = (long) Integer.parseInt(str);
		} catch (NumberFormatException e) {
			System.out.println(deptId);
		}
		Query query = em.createQuery("Select count(*) from Department as dept where dept.id = " +deptId+ " or dept.name like '部门11'");
//		Query query = em.createQuery("Select count(*) from Department as dept where dept.id < 21 and dept.name like '%部门1%'");
//		long count = (Long) query.setParameter(1, (long)21)
//				.setParameter("name", "%部门1%").getSingleResult();
		long count = (Long) query.getSingleResult();
		System.out.println(count);
	}
	@SuppressWarnings("unchecked")
	private static void testIsParent() {
		Query query = em.createQuery("Select dept.id from Department as dept where dept.parent.id = ?1");
		List<Department> result = query.setParameter(1, (long)2).getResultList();
		System.out.println(result == null);
		System.out.println(result.size() + "----------------->............>>>");
	}
	
	@SuppressWarnings("unchecked")
	private static void testPageEntitys() {
		Long count = (Long) em.createQuery("Select count(*) from Department").getSingleResult();
		System.out.println(count);
		List<Department> depts = em.createQuery("from Department order by id asc").setFirstResult(0)
				.setMaxResults(10).getResultList();
		for(Department dept : depts) {
			System.out.println(dept+"..............");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void testUserPageEntitys() {
		Long count = (Long) em.createQuery("Select count(*) from User").getSingleResult();
		System.out.println(count);
		List<User> users = em.createQuery("from User order by id asc").setFirstResult(0)
				.setMaxResults(10).getResultList();
		for(User user : users) {
			System.out.println(user+"..............");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void testQueryUser() {
		List<User> result = em.createQuery("from User as user where user.username = ?1 and user.password = ?2")
		.setParameter(1, "maomao")
		.setParameter(2, "xiaoqin")
		.getResultList();
		for(int i = 0; i < result.size(); i++) {
			User user =  result.get(i);
			System.out.println(user.getEmailAddress());
			System.out.println(user.getPhoneNumber());
			System.out.println(user.getDepartment());
			System.out.println(user.getRoles().size());
		}
		System.out.println(result.size());
		
	}

	private static void testUser_Role() {

		Domain company1 = new Domain();
		company1.setName("阿里");
		
		em.persist(company1);

		Department department1 = new Department();
		department1.setName("销售部");

		Department department2 = new Department();
		department2.setName("研发部");
		
		Department department3 = new Department();
		department3.setName("人力部");
		
		Department department4 = new Department();
		department4.setName("财务部");
		
		Department department5 = new Department();
		department5.setName("人_财部");
		
		department1.setDomain(company1);
		department2.setDomain(company1);
		department3.setDomain(company1);
		department4.setDomain(company1);
		department5.setDomain(company1);

		department3.setParent(department5);
		department4.setParent(department5);
		
		em.persist(department1);
		em.persist(department2);
		em.persist(department3);
		em.persist(department4);
		em.persist(department5);
		
		Set<Department> depts = new HashSet<Department>();
		depts.add(department1);
		depts.add(department2);
		depts.add(department3);
		depts.add(department4);
		depts.add(department5);
		
		Role role1 = new Role();
		role1.setName("manager");
		role1.setType(RoleType.manager);

		Role role2 = new Role();
		role2.setName("manager");
		role2.setType(RoleType.manager);

		Role role3 = new Role();
		role3.setName("csr");
		role3.setType(RoleType.csr);
		
		role1.setDomain(company1);
		role2.setDomain(company1);
		role3.setDomain(company1);

		role1.getDepartments().add(department1);
		role1.getDepartments().add(department2);
		role1.getDepartments().add(department3);
		role1.getDepartments().add(department4);
		role1.getDepartments().add(department5);
		
		role2.getDepartments().add(department5);

		role3.getDepartments().add(department1);
		role3.getDepartments().add(department3);
		
		em.persist(role1);
		em.persist(role2);
		em.persist(role3);
		
		User user1 = new User();
		user1.setEmpNo("emp_number e1");
		user1.setRealName("张三");
		user1.setUsername("admin1");
		user1.setPassword("amdin1");
		user1.setAge(24);
		user1.setGender("男");
		user1.setPhoneNumber("13815452654");
		user1.setRegistedDate(new Date());
		user1.setEmailAddress("xiaoqin@126.com");
		user1.setDomain(company1);
		
		User user2 = new User();
		user2.setEmpNo("emp_number e2");
		user2.setRealName("李四");
		user2.setUsername("manager");
		user2.setPassword("manager");
		user2.setAge(26);
		user2.setGender("男");
		user2.setPhoneNumber("13815458623");
		user2.setRegistedDate(new Date());
		user2.setEmailAddress("xiaoqinang@126.com");
		user2.setDomain(company1);

		User user3 = new User();
		user3.setEmpNo("emp_number e3");
		user3.setRealName("陶子");
		user3.setUsername("maomao");
		user3.setPassword("maomao");
		user3.setAge(21);
		user3.setGender("女");
		user3.setPhoneNumber("13845788623");
		user3.setRegistedDate(new Date());
		user3.setEmailAddress("taozi@126.com");
		user3.setDomain(company1);
		
		// 设置用户所在部门
		user1.setDepartment(department1);
		user2.setDepartment(department2);
		user3.setDepartment(department3);
		
		// 向从表对象中添加关联对象
		user1.getRoles().add(role1);
		user1.getRoles().add(role2);
		
		user2.getRoles().add(role2);
		
		user3.getRoles().add(role3);

		em.persist(user1);
		em.persist(user2);
		em.persist(user3);
		
	}
	private static void testUser_Role1() {
		
		Domain company1 = new Domain();
		company1.setName("阿里");
		em.persist(company1);
		
		
		Department department1 = new Department();
		department1.setName("销售部");
		
		department1.setDomain(company1);
		
		em.persist(department1);

		Set<Department> depts = new HashSet<Department>();
		depts.add(department1);
		
		Role role1 = new Role();
		role1.setName("manager");
		role1.setType(RoleType.manager);
		
		role1.setDomain(company1);
		
		role1.getDepartments().add(department1);
		
		em.persist(role1);
		
		User user1 = new User();
		user1.setEmpNo("emp_number e1");
		user1.setRealName("张三");
		user1.setUsername("admingfda1");
		user1.setPassword("amdinfas1");
		user1.setAge(24);
		user1.setGender("男");
		user1.setPhoneNumber("11111111111");
		user1.setRegistedDate(new Date());
		user1.setEmailAddress("xiaoqin@126.com");
		user1.setDomain(company1);
		
		// 设置用户所在部门
		user1.setDepartment(department1);
		user1.getRoles().add(role1);
		
		em.persist(user1);
		
	}

}
