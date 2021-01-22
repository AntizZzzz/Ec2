package com.jiangyifen.ec2.test;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.report.entity.CallStatisticOverview;

public class Jiang_JPATest {
	
	private static EntityManagerFactory entityManagerFactory;
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		Long time = System.currentTimeMillis();
		System.out.println();
		
		entityManagerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

//		Jiang_JPATest.testLoadUser();
//		Jiang_JPATest.testLoadDepartment();
//		Jiang_JPATest.testRemoveDepartment();
//		Jiang_JPATest.testRemoveRole();
//		Jiang_JPATest.testRemoveUser();
//		testUpdateDepartment();
//		testGetMaxMusicOnHoldName(em);
//		testNativeQueryToUser(em);
//		testGetProjectsByUser(em);
//		testGetResource(em);
//		
//		test(em, "1001", "800001", "900005");
//		test2(em);
//		test3(em);
//		test4(em);
		test(em);
		testAA(em);

		em.getTransaction().commit();
		em.close();
		entityManagerFactory.close();

		System.out.print("spend time --------------   ");
		System.out.println(System.currentTimeMillis() - time);
	}
	
	@SuppressWarnings("unchecked")
	private static void testAA(EntityManager em) {
		String sql = "select p from QueuePauseRecord as p where p.pauseDate is not null and p.unpauseDate is null and p.username = '1001' and p.sipname = 'SIP/800001' and p.queue = '900001' and p.pauseDate = (select max(p1.pauseDate) from QueuePauseRecord as p1 where p1.pauseDate is not null and p1.unpauseDate is null and p1.username = '1001' and p1.sipname = 'SIP/800001' and p1.queue = '900001') and NOT EXISTS(select p2 from QueuePauseRecord as p2 where p2.pauseDate is not null and p2.unpauseDate is not null and p2.username = '1001' and p2.sipname = 'SIP/800001' and p2.queue = '900001' and p2.pauseDate >= p.pauseDate)";
		List<QueuePauseRecord> pauseList = em.createQuery(sql).getResultList();
		System.out.println(pauseList.size());
		for(QueuePauseRecord r : pauseList) {
			System.out.println(r);
		}
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static void test4(EntityManager em) {
		String s2 = "select u from User as u inner join u.department as d where d.id = 127";
		
		System.out.println();
		long t1 = System.currentTimeMillis();
		List<User> list = em.createQuery(s2).getResultList();
		long t2 = System.currentTimeMillis();
		System.out.println(StringUtils.join(list, ","));
		System.out.println("耗时毫秒： ---->"+(t2-t1));
		System.out.println("集合size --->"+list.size());
		System.out.println();
		System.out.println();
		System.out.println();
	}
	
	@SuppressWarnings("unchecked")
	private static void test(EntityManager em) {
//------------- 原始
//		String s2 = "select c from CustomerServiceRecord as c where c.creator.department.id in (127) "		// 耗时毫秒： ---->182868
//				+ "and c.createDate >= '2012-12-02 00:00:00' and c.createDate <= '2013-12-03 00:00:00' "
//				+ "and c.id in (select max(csr2.id) from CustomerServiceRecord as csr2 group by csr2.customerResource.id) order by c.createDate desc";
//------------- 改变1
//		String s2 = "select c from CustomerServiceRecord as c where "	// 耗时毫秒： ---->180782
//				+ "c.id in (select max(csr2.id) from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "
//				+ "and csr2.createDate >= '2012-12-02 00:00:00' and csr2.createDate <= '2013-12-03 00:00:00' group by csr2.customerResource.id)"
//				+ "order by c.createDate desc";
//------------- 改变2
//		String s2 = "select c from CustomerServiceRecord as c inner join "
//				+ "(select max(csr2.id) as mcid from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "
//				+ "and csr2.createDate >= '2012-12-02 00:00:00' and csr2.createDate <= '2013-12-03 00:00:00' group by csr2.customerResource.id) as maxc on c.id = maxc.mcid "
//				+ "order by c.createDate desc";
//------------- 改变3
//		String s2 = "select max(csr2.id) as mcid from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "	// 耗时毫秒： ---->1159
//				+ "and csr2.createDate >= '2012-12-02 00:00:00' and csr2.createDate <= '2013-12-03 00:00:00' group by csr2.customerResource.id "
//				+ "order by csr2.createDate desc";
//		String s2 = "select max(csr2.id) as mcid from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "	// 耗时毫秒： ---->1159
//				+ "and csr2.createDate >= '2013-11-02 00:00:00' and csr2.createDate <= '2013-12-03 00:00:00' group by csr2.customerResource.id,csr2.createDate "
//				+ "order by csr2.createDate desc";
		String s2 = "select max(csr2.id) as mcid from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "	// 耗时毫秒： ---->1159
				+ "and csr2.createDate >= '2013-11-02 00:00:00' and csr2.createDate <= '2013-12-03 00:00:00' group by csr2.customerResource.id "
				+ "order by mcid desc";
		System.out.println();
		long t1 = System.currentTimeMillis();
		List<Long> list = em.createQuery(s2).getResultList();
		long t2 = System.currentTimeMillis();
		String idSql = StringUtils.join(list, ",");
		System.out.println(idSql);
		System.out.println(idSql.length());
		System.out.println("耗时毫秒： ---->"+(t2-t1));
		System.out.println("集合size --->"+list.size());
		System.out.println();
		
//		String s3 = "select c from CustomerServiceRecord as c where c.id in ("+idSql+") order by c.createDate desc";		// 耗时毫秒： ---->1159
//		if(list.size() == 0) {
//			s3 = "select c from CustomerServiceRecord as c where c.id = 0"
//					+ "order by c.createDate desc";
//		}
//		List<CallStatisticOverview> list2 = em.createQuery(s3).getResultList();
//		long t3 = System.currentTimeMillis();
//		System.out.println("总耗时毫秒：  -->"+(t3-t1));
//		System.out.println("集合size --->"+list2.size());
//		System.out.println();
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private static void test2(EntityManager em) {
		String s2 = "select c from CustomerServiceRecord as c where  c.creator.department.id in (127) "
				+ "and c.createDate >= '2012-12-02 00:00:00' and c.createDate <= '2013-12-03 00:00:00' "
				+ "and c.id in (select max(csr2.id) from CustomerServiceRecord as csr2 where c.creator.department.id in (127) group by csr2.customerResource.id) order by c.createDate desc";
		
		System.out.println();
		long t1 = System.currentTimeMillis();
		List<CallStatisticOverview> list = em.createQuery(s2).getResultList();
		long t2 = System.currentTimeMillis();
		System.out.println("耗时毫秒： ---->"+(t2-t1));
		System.out.println("集合size --->"+list.size());
		System.out.println();
		System.out.println();
		System.out.println();
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private static void test3(EntityManager em) {
		String s2 = "select max(csr2.id) from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "
				+ "group by csr2.customerResource.id";			// 耗时毫秒： ---->1032
//		String s2 = "select max(csr2.id) from CustomerServiceRecord as csr2 where csr2.creator.department.id in (127) "		// 耗时毫秒： ---->541
//				+ "and csr2.createDate >= '2013-11-02 00:00:00' and csr2.createDate <= '2013-12-03 00:00:00' group by csr2.customerResource.id";
		
		System.out.println();
		long t1 = System.currentTimeMillis();
		List<Long> list = em.createQuery(s2).getResultList();
		long t2 = System.currentTimeMillis();
		System.out.println(StringUtils.join(list, ","));
		System.out.println("耗时毫秒： ---->"+(t2-t1));
		System.out.println("集合size --->"+list.size());
		System.out.println();
		System.out.println();
		System.out.println();
	}



	@SuppressWarnings({ "unchecked", "unused" })
	private static void test1(EntityManager em) {
		
//		String nativeSql = "select count(rb) from ec2_customer_resource_ec2_customer_resource_batch as rb where rb.customerresources_id = 21 and rb.customerresourcebatches_id = 703";
//		Long count = (Long) em.createNativeQuery(nativeSql).getSingleResult();
//		System.out.println(count);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, +1);
		cal.add(Calendar.YEAR, -1);
		String maxOrderTime = sdf.format(cal.getTime());
		System.out.println(maxOrderTime);
		
		String sql = "select mpt from MarketingProjectTask as mpt where mpt.marketingProject.id = 93" 
				+ " and mpt.isFinished = false and mpt.user is null and mpt.domain.id = 1 and mpt.orderTime < '"+maxOrderTime+"' order by mpt.orderTime asc";
		List<MarketingProjectTask> ids = em.createQuery(sql).setMaxResults(2).getResultList();
		System.out.println(sql);
		System.out.println();
		for(MarketingProjectTask q : ids) {
			System.out.println(q);
		}
		
		System.out.println("-------------------------------------------------");
		if(ids.size() == 0) {
			String sql2 = "select mpt from MarketingProjectTask as mpt where mpt.marketingProject.id = 93" 
					+ " and mpt.isFinished = false and mpt.user is null and mpt.domain.id = 1 and mpt.orderTime is null order by mpt.orderTime asc";
			List<MarketingProjectTask> id2s = em.createQuery(sql2).setMaxResults(2).getResultList();
			System.out.println();
			System.out.println(sql2);
			System.out.println();
			for(MarketingProjectTask q : id2s) {
				System.out.println(q);
			}
		}
	}


	@SuppressWarnings({ "unchecked", "unused" })
	private static void test(EntityManager em, String username, String exten, String queue) {
		String jpasql = "select p from QueuePauseRecord as p "
				+ "where p.pauseDate is not null and p.unpauseDate is null " 
				+ "and p.username = '" + username + "' " 
				+ "and p.sipname = 'SIP/"+ exten + "' " 
				+ "and p.queue = '" + queue + "' " 
				+ "and p.pauseDate = (select max(p1.pauseDate) from QueuePauseRecord as p1 "
					+ "where p1.pauseDate is not null and p1.unpauseDate is null " 
					+ "and p1.username = '" + username + "' " 
					+ "and p1.sipname = 'SIP/"+ exten + "' " 
					+ "and p1.queue = '" + queue + "') "
				+ "and NOT EXISTS(select p2 from QueuePauseRecord as p2 "
					+ "where p2.pauseDate is not null and p2.unpauseDate is not null " 
					+ "and p2.username = '" + username + "' " 
					+ "and p2.sipname = 'SIP/"+ exten + "' " 
					+ "and p2.queue = '" + queue + "' " 
					+ "and p2.pauseDate >= p.pauseDate) "
				+ "and 2 > ( select count(p3) from QueuePauseRecord as p3 " 
					+ "where p3.username = '" + username + "' " 
					+ "and p3.sipname = 'SIP/" + exten + "' " 
					+ "and p3.pauseDate is not null and p3.unpauseDate is null "
					+ "and p3.pauseDate >= p.pauseDate " 
					+ "and p3.queue != '" + queue + "' )";
		String nativeSql = "select * from ec2_queue_member_pause_event_log as p "
				+ "where p.pauseDate is not null and p.unpauseDate is null " 
				+ "and p.username = '" + username + "' " 
				+ "and p.sipname = 'SIP/"+ exten + "' " 
				+ "and p.queue = '" + queue + "' " 
				+ "and p.pauseDate = (select max(p1.pauseDate) from ec2_queue_member_pause_event_log as p1 "
					+ "where p1.pauseDate is not null and p1.unpauseDate is null " 
					+ "and p1.username = '" + username + "' " 
					+ "and p1.sipname = 'SIP/"+ exten + "' " 
					+ "and p1.queue = '" + queue + "') "
				+ "and NOT EXISTS(select p2 from ec2_queue_member_pause_event_log as p2 "
					+ "where p2.pauseDate is not null and p2.unpauseDate is not null " 
					+ "and p2.username = '" + username + "' " 
					+ "and p2.sipname = 'SIP/"+ exten + "' " 
					+ "and p2.queue = '" + queue + "' " 
					+ "and p2.pauseDate >= p.pauseDate) "
				+ "and 2 > ( select count(p3) from ec2_queue_member_pause_event_log as p3 " 
					+ "where p3.username = '" + username + "' " 
					+ "and p3.sipname = 'SIP/" + exten + "' " 
					+ "and p3.pauseDate is not null and p3.unpauseDate is null "
					+ "and p3.pauseDate >= p.pauseDate " 
					+ "and p3.queue != '" + queue + "' )";
		
		System.out.println(nativeSql);
		
		
		List<QueuePauseRecord> pauseList = em.createQuery(jpasql).getResultList();
		System.out.println(pauseList+ "=============" + pauseList.size());
		for(QueuePauseRecord r : pauseList) {
			System.out.println(r.getId());
		}
		
		List<QueuePauseRecord> pauseList2 = em.createNativeQuery(nativeSql).getResultList();
		System.out.println(pauseList2 + "=============" + pauseList2.size());
		for(QueuePauseRecord r : pauseList2) {
			System.out.println(r.getId());
		}
		
		
	}


	@SuppressWarnings({ "unchecked", "unused" })
	private static void testGetResource(EntityManager em) {
//		String sql = "select c from CustomerResource as c where c.owner.id = 2 and c.importDate >= '2013-01-21 00:00:00' and c.importDate <= '2013-01-28 00:00:00' and (c.count >= 34 or c.count <= 5) and (c.expireDate >= '2013-02-04 16:28:28' or c.expireDate <= '2013-02-01 16:28:28') and c.name like '%富商大贾%' and c.company.name like '%富商大贾%'";
//		String sql = "select c from CustomerResource as c where c.owner.id = 2 and c.importDate >= '2013-01-21 00:00:00' and c.importDate <= '2013-01-28 00:00:00' and (c.count >= 34 or c.count <= 5) and (c.expireDate >= '2013-02-04 16:28:28' or c.expireDate <= '2013-02-01 16:28:28') and c.name like '%富商大贾%' and c.company.name like '%富商大贾%' and c.lastDialDate >= '2013-01-30 00:00:00' and c.lastDialDate <= '2013-01-31 00:00:00'";
		String sql = "select c from CustomerResource as c where c.owner.id = 1 and c.importDate >= '2013-01-21 00:00:00' and c.importDate <= '2013-01-28 00:00:00' and (c.count >= 34 or c.count <= 5) and (c.expireDate >= '2013-02-04 16:28:28' or c.expireDate <= '2013-02-01 16:28:28') and c.name like '%富商大贾%' and c.company.name like '%富商大贾%' and c in (select p.customerResource from Telephone as p where p.number like '%12132%')  and c.lastDialDate >= '2013-01-30 00:00:00' and c.lastDialDate <= '2013-01-31 00:00:00'";
		System.out.println(em.createQuery(sql).getResultList().size());
		List<CustomerResource> list = em.createQuery(sql).getResultList();
		if(list.size() > 0) {
			System.out.println(list.get(0));
		}
	}


	@SuppressWarnings({ "unchecked", "unused" })
	private static void testGetProjectsByUser(EntityManager em) {
		List<Long> projectIds = em.createNativeQuery("select marketingproject_id from ec2_markering_project_ec2_user where users_id = 10").getResultList();
		String idSql = projectIds.size() > 0 ? "" : "-1";
		for(int i = 0; i < projectIds.size(); i++) {
			if( i == (projectIds.size() -1) ) {
				idSql += projectIds.get(i);
			} else {
				idSql += projectIds.get(i) +", ";
			}
		}
		List<MarketingProject> projects = em.createQuery("select p from MarketingProject as p where p.id in ("+ idSql+")").getResultList();
		System.out.println(projects);
	}


	@SuppressWarnings({ "unchecked", "unused" })
	private static void testNativeQueryToUser(EntityManager em) {
		List<Long> counts = em.createNativeQuery("select count(ur) from ec2_user_role_link as ur where ur.role_id = 2").getResultList();
		System.out.println(counts.get(0));
		System.out.println(counts.size());
	}


	@SuppressWarnings({ "unchecked", "unused" })
	private static void testGetMaxMusicOnHoldName(EntityManager em) {
		// create a couple of events...
		Long time = System.currentTimeMillis();
		List<MusicOnHold> mohs = em.createQuery("select moh from MusicOnHold as moh where moh.name = " 
				+ "(select max(moh2.name) from MusicOnHold as moh2 where moh2.name != 'default')").getResultList();
//		if(mohs.size() > 0) {
//			return Long.parseLong(mohs.get(0).getName());
//		}

		System.out.print("============   ");
		System.out.println(System.currentTimeMillis() - time);
		for(MusicOnHold moh : mohs) {
			System.out.println(moh);
		}
		
	}


	public static List<Department> testLoadDepartment() {

		// create a couple of events...
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();
		List<Department> result = entityManager.createQuery("from Department",
				Department.class).getResultList();
		for (Department u : result) {
			System.out.println(u);
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return result;
	}
	
	public static void testUpdateDepartment1() {
		EntityManager em = entityManagerFactory
				.createEntityManager();
		
		em.getTransaction().begin();
		Department d = em.find(Department.class, (long)23);
		Department d2 = em.find(Department.class, (long)27);
		
		Department newer = new Department();
		newer.setId(d2.getId());
		newer.setName(d2.getName());
		newer.setParent(d2.getParent());
		newer.setRoles(d2.getRoles());
		newer.setUsers(d2.getUsers());
		
		Role r1 = em.find(Role.class, (long)16);	// admin
		
		Set<Department> ds1 = new HashSet<Department>();
		ds1.add(d);
		ds1.add(d2);
		r1.setDepartments(ds1);
		
		System.out.println(d);
		System.out.println(d.getRoles());
		System.out.println(d.getId());
		System.out.println(d2);
		System.out.println(d2.getRoles());
		System.out.println(d2.getId());
		
		em.merge(r1);
		
		em.getTransaction().commit();
		em.close();
		
	}

	public static void testUpdateDepartment() {
		EntityManager em = entityManagerFactory
				.createEntityManager();

		em.getTransaction().begin();
		Department d = em.find(Department.class, (long)23);
		Department d2 = em.find(Department.class, (long)27);
		
		Role r1 = em.find(Role.class, (long)16);	// admin
		
		Set<Department> ds1 = new HashSet<Department>();
		ds1.add(d);
		ds1.add(d2);
		r1.setDepartments(ds1);
		
		System.out.println(d);
		System.out.println(d.getRoles());
		System.out.println(d.getId());
		System.out.println(d2);
		System.out.println(d2.getRoles());
		System.out.println(d2.getId());
		
		em.merge(r1);
		
		em.getTransaction().commit();
		em.close();
		
	}
	
	public static List<User> testLoadUser() {

		// create a couple of events...
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();
		List<User> result = entityManager.createQuery("from User", User.class)
				.getResultList();
		for (User u : result) {
			System.out.println(u);
			for (Role r : u.getRoles()) {
				System.out.println(r);
			}
		}

		entityManager.getTransaction().commit();
		entityManager.close();

		return result;
	}
	
	
	public static void testRemoveUser() {

		// create a couple of events...
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();
		List<User> result = entityManager.createQuery("from User", User.class)
				.getResultList();
		for (User u : result) {
			System.out.println(u);
		}
		User user = result.get(0);
		System.out.println(user);
		entityManager.remove(user);

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public static void testRemoveRole() {

		// create a couple of events...
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();
		List<Role> result = entityManager.createQuery("from Role", Role.class)
				.getResultList();
		for (Role u : result) {
			System.out.println(u);
		}
		Role r = result.get(0);
		System.out.println(r);
		entityManager.remove(r);

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public static void testRemoveDepartment() {

		// create a couple of events...
		EntityManager entityManager = entityManagerFactory
				.createEntityManager();

		entityManager.getTransaction().begin();
		List<Department> result = entityManager.createQuery("from Department",
				Department.class).getResultList();
		for (Department u : result) {
			System.out.println("query " + u);
		}
		Department r3 = result.get(3);
		entityManager.remove(r3);
		Department r2 = result.get(2);
		entityManager.remove(r2);
		Department r1 = result.get(1);
		entityManager.remove(r1);
		Department r0 = result.get(0);
		entityManager.remove(r0);

		entityManager.getTransaction().commit();
		entityManager.close();
	}
	

}
