package com.jiangyifen.ec2.test;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;

public class Chen_AddProjectsTest {
	private static EntityManagerFactory entityManagerFactory;

	// private static Random rand = new java.util.Random();
	public static void setUp() {
		entityManagerFactory = Persistence.createEntityManagerFactory("ec2");
	}

	public static void tearDown() {
		entityManagerFactory.close(); 
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		Chen_AddProjectsTest.setUp();
		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

//		Chen_AddProjectsTest.createProjects(em);
		
//		String sql="select e from CustomerServiceRecord e where (  e.creator.department.id=1 or e.creator.department.id=78) and e.domain.id=1 order by e.id desc";
		
//		SELECT *,max(dateline) as max_line FROM posts GROUP BY  tid HAVING dateline=max(dateline)
		
		@SuppressWarnings("unused")
		String sql="select e from CustomerServiceRecord e group by e.customerResource.id  where (  e.creator.department.id=1 or e.creator.department.id=78) and e.domain.id=1 order by e.id desc";
		

		em.getTransaction().commit();
		em.close();
		Chen_AddProjectsTest.tearDown();
	}

//	private static void test(EntityManager em) {
//		
//		Long count=(Long)em.createQuery("select count(*) from (select user_id from ec_marketing_project_task where marketingproject_id=1 group by user_id) as foo").getSingleResult();
//		System.out.println(Integer.parseInt(count + ""));
//	}

	
	
	public static void createProjects(EntityManager em, Domain domain) {
		Random rand = new Random();
		
		for (int count = 1; count <= 35; count++) {
			MarketingProject project = new MarketingProject();
			project.setProjectName("project_" + count);

			GregorianCalendar calendar = new GregorianCalendar();
			calendar.add(GregorianCalendar.DAY_OF_YEAR, -(rand.nextInt(365)));
			Date date = calendar.getTime();
			project.setCreateDate(date);
			project.setNote("有些人并不明白懂得享受孤独是一种可贵品质，他们无法安静，拒绝孤单，总是拉帮结派的娱乐来证明自己的存在，缺乏安静的观察与思考，他们不相信孤独的力量，内心无法平静，因此在满目创痍的繁华背后，找不到真正的自己。");
			project.setDomain(domain);
			project.setMarketingProjectStatus(MarketingProjectStatus.NEW);
			
			em.persist(project);
		}
	}
	
	
}
