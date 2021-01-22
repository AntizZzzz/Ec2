package com.jiangyifen.ec2.test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.User;

public class Jiang_TemproryTest {

	public static void main(String[] args) {
		EntityManagerFactory entityMangerFactory = Persistence
				.createEntityManagerFactory("ec2");
		EntityManager em = entityMangerFactory.createEntityManager();
		em.getTransaction().begin();

		 testModels(em);
		
		 testInsertProjectTask(em);
		
//		 testFindTheLastEntity(em);
		
		 testFindTimers(em);
		
		 testEditTimersTime(em, 0, 1, 0);
		
		 testGetMinTesponseTimeToNow(em);
		
		 testGetTastBySql(em);

		testGetRecordBySql(em);

		testGetMaxNameInSip(em);
		
		em.getTransaction().commit();
		em.close();
		entityMangerFactory.close();
	}

	private static void testGetMaxNameInSip(EntityManager em) {
		SipConfig sip = (SipConfig) em.createQuery("select sip from SipConfig as sip where sip.name = (select max(sip2.name) from SipConfig as sip2 where sip2.isOutline = false)").getSingleResult();
		System.out.println(sip);
	}

	@SuppressWarnings("unchecked")
	private static void testGetRecordBySql(EntityManager em) {
		List<CustomerServiceRecord> records = em
				.createQuery(
						"select c from CustomerServiceRecord as c "
								+ "where  c.creator.id = 1 and c.marketingProject.id = 131 "
								+ "and c.direction = 'outgoing'  and c.serviceRecordStatus.id = '14'  "
								+ "and c.createDate >= '2010-11-15 00:00:00' and c.createDate < '2012-11-18 00:00:00'  "
								+ "and c.customerResource.name like '%海平%' "
								+ "and c.customerResource in ("
								+ "select p.customerResource from Telephone as p where p.number like '%605%') "
								+ "order by c.createDate desc").getResultList();

		for (CustomerServiceRecord r : records) {
			System.out.println(r.getId() + " -- "
					+ r.getCustomerResource().getName() + " -- "
					+ r.getServiceRecordStatus() + " -- "
					+ r.getRecordContent());
		}

		System.out.println();
		System.out
				.println("=======================================4===========================================");

	}

	@SuppressWarnings("unchecked")
	private static void testGetTastBySql(EntityManager em) {
		List<MarketingProjectTask> tasks = em
				.createQuery(
						"select mpt from MarketingProjectTask as mpt where mpt.user.id = 1 "
								+ "and mpt.marketingProject.id = 131 and mpt.isFinished = true "
								+ "and mpt.isAnswered = true and mpt.lastStatus = '中途拒绝' "
								+ "and mpt.customerResource.name like '%海平%' ")
				.getResultList();

		for (MarketingProjectTask task : tasks) {
			System.out.println(task.getId() + " -- "
					+ task.getCustomerResource().getName() + " -- "
					+ task.getLastStatus() + " -- " + task.getIsFinished());
		}

		System.out.println();
		System.out
				.println("=======================================0===========================================");

		String phoneNumber = "133****";
		String[] phoneNumbers = new String[2];
		if (phoneNumber.contains("*")) {
			phoneNumbers[0] = phoneNumber
					.substring(0, phoneNumber.indexOf("*"));
			phoneNumbers[1] = phoneNumber.substring(
					phoneNumber.lastIndexOf("*") + 1, phoneNumber.length());
		}
		for (String s : phoneNumbers) {
			System.out.print(s + "\t");
			System.out.println(s.equals(""));
		}

		int countAsterisk = phoneNumber.lastIndexOf("*")
				- phoneNumber.indexOf("*") + 1;
		System.out.println("-----------------------" + countAsterisk);

		// 成功得到想要的结果
		List<MarketingProjectTask> tasks4 = em
				.createQuery(
						"select mpt from MarketingProjectTask as mpt where mpt.user.id = 1 "
								+ "and mpt.marketingProject.id = 131 and mpt.isFinished = true "
								+ "and mpt.isAnswered = true and mpt.lastStatus = '中途拒绝' "
								+ "and mpt.customerResource.name like '%海平%' "
								+ "and mpt.customerResource in ( "
								+ "select p.customerResource from Telephone as p where p.number like '%605%')")
				.getResultList();

		for (MarketingProjectTask task : tasks4) {
			System.out.println(task.getId() + " -- "
					+ task.getCustomerResource().getName() + " -- "
					+ task.getLastStatus() + " -- " + task.getIsFinished());
		}

		System.out.println();
		System.out
				.println("=======================================4===========================================");

		List<Telephone> phones = em
				.createQuery(
						"select p from Telephone as p where p.number like '%133%____%099%'")
				.getResultList();

		for (Telephone p : phones) {
			System.out.println(p.getId() + " -- "
					+ p.getCustomerResource().getName() + " -- "
					+ p.getNumber());
		}

		System.out.println();
		System.out
				.println("=======================================4===========================================");

		List<CustomerResource> tasks5 = em
				.createQuery(
						"select p.customerResource from Telephone as p where p.number like '%605%' "
								+ "and p.customerResource in ( "
								+ "select mpt.customerResource from MarketingProjectTask as mpt where mpt.user.id = 1 "
								+ "and mpt.marketingProject.id = 131 and mpt.isFinished = true "
								+ "and mpt.isAnswered = true and mpt.lastStatus = '中途拒绝' "
								+ "and mpt.customerResource.name like '%海平%' "
								+ ")").getResultList();
		for (CustomerResource o : tasks5) {
			System.out.print(o.getId() + " -- " + o.getName() + " -- "
					+ o.getBirthdayStr() + "\t");
		}

		System.out.println();
		System.out
				.println("=======================================5===========================================");

	}

	private static void testGetMinTesponseTimeToNow(EntityManager em) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String now = df.format(new Date());
		Date responseTime = (Date) em.createQuery(
				"select min(t2.responseTime) from Timers as t2 where t2.responseTime > '"
						+ now + "'").getSingleResult();
		System.out.println(df.format(responseTime));

		// List list =
		// em.createQuery("select distinct mpt.marketingProject from MarketingProjectTask as mpt where mpt.user.id = 1").getResultList();
		// System.out.println(list.size() + " =====size");
	}

	/**
	 * 更改定时器的响应时间，使得其向后推迟一段时间
	 * 
	 * @param em
	 * @param day
	 * @param hour
	 * @param minute
	 */
	@SuppressWarnings("unchecked")
	private static void testEditTimersTime(EntityManager em, long day,
			long hour, long minute) {
		List<Timers> timersList = em.createQuery("select t from Timers as t")
				.getResultList();
		for (Timers timers : timersList) {
			Date time = timers.getResponseTime();
			long delay = day * 24 * 60 * 60 * 1000 + hour * 60 * 60 * 1000
					+ minute * 60 * 1000;
			Date responseTime = new Date(time.getTime() + delay);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(responseTime);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.SECOND, 0);
			timers.setResponseTime(calendar.getTime());
			em.persist(timers);
		}

	}

	@SuppressWarnings("unchecked")
	private static void testFindTimers(EntityManager em) {
		final SimpleDateFormat df = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");

		List<Timers> timersList1 = em
				.createQuery(
						"select t from Timers as t where t.responseTime = "
								+ "(select min(t2.responseTime) from Timers as t2 where t2.responseTime > '"
								+ df.format(new Date()) + "')").getResultList();
		List<Timers> timersList = em.createQuery(
				"select t from Timers as t where t.responseTime > '"
						+ df.format(new Date())
						+ "' order by t.responseTime asc").getResultList();
		for (Timers timers : timersList1) {
			System.out.println(timers.getCreator().getId() + "****"
					+ timers.getId() + " ---- " + timers.getTitle() + "  ---  "
					+ df.format(timers.getResponseTime()));
		}

		Collections.sort(timersList, new Comparator<Timers>() {
			@Override
			public int compare(Timers t1, Timers t2) {
				return df.format(t1.getResponseTime()).compareTo(
						df.format(t2.getResponseTime()));
			}
		});
		System.out.println();
		System.out.println();
		System.out.println();
		for (Timers timers : timersList) {
			System.out.println(timers.getCreator().getId() + "****"
					+ timers.getId() + " **** " + timers.getTitle()
					+ "  ****  " + df.format(timers.getResponseTime()));
		}
	}

	// private static void testFindTheLastEntity(EntityManager em) {
	// // select info from UserLoginInfo as info where info.date = Thu Aug 02
	// // 00:00:00 CST 2012 and info.hour = 17
	// Date date = new Date(112, 7, 26, 0, 0, 0);
	// SimpleDateFormat format = new SimpleDateFormat(
	// "yyyy-MM-dd HH:mm:ss.SSS");
	// List<UserOnlineTimeLength> infos = em.createQuery(
	// "select info from UserLoginInfo as info where info.loginDate > '"
	// + format.format(date) + "'").getResultList();
	// for (UserOnlineTimeLength info : infos) {
	// System.out.println(info.getUsername() + " --- "
	// + info.getLoginDate() + " -- " + info.getHour());
	// }
	// }

	private static void testInsertProjectTask(EntityManager em) {
		MarketingProjectTask task = new MarketingProjectTask();
		task.setDomain(em.find(Domain.class, (long) 50));
		task.setCustomerResource(em.find(CustomerResource.class, (long) 612));
		task.setMarketingProject(em.find(MarketingProject.class, (long) 587));
		task.setUser(em.find(User.class, (long) 999));
		em.persist(task);

	}

	private static void testModels(EntityManager em) {
		// 从数据库中获取所有 manager 的权限
		Set<BusinessModel> csrModules = new HashSet<BusinessModel>();
		@SuppressWarnings("unchecked")
		List<BusinessModel> csrBusinessModels = em
				.createQuery(
						"select model "
								+ "from BusinessModel as model where model.application = 'csr'")
				.getResultList();
		csrModules.addAll(csrBusinessModels);

		Role role1 = (Role) em.find(Role.class, (long) 5);
		role1.setBusinessModels(csrModules);
		em.merge(role1);

		Role role2 = (Role) em.find(Role.class, (long) 14);
		role2.setBusinessModels(csrModules);
		em.merge(role2);

		// 从数据库中获取所有 manager 的权限
		Set<BusinessModel> partCsrModules = new HashSet<BusinessModel>();
		for (BusinessModel model : csrBusinessModels) {
			if ("TaskManage&OutgoingTask&EditCustomer".equals(model.toString()))
				continue;
			partCsrModules.add(model);
		}

		for (int i = 1; i < 3; i++) {
			Domain domain = em.find(Domain.class, (long) i);
			Role partCsr = new Role();
			partCsr.setName("part_csr");
			partCsr.setDomain(domain);
			partCsr.setType(RoleType.csr);
			partCsr.setBusinessModels(partCsrModules);
			for (int x = 1; x < 6; x++) {
				partCsr.getDepartments().add(
						em.find(Department.class, (long) x));
			}
			em.persist(partCsr);
		}
	}
}
