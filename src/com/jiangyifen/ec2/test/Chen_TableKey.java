package com.jiangyifen.ec2.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.TableKeyword;

public class Chen_TableKey {
	public static void main(String[] args) {
		test();
	}
	@SuppressWarnings("rawtypes")
	public static void test() {
		EntityManagerFactory emf=Persistence.createEntityManagerFactory("ec2");
		EntityManager em=emf.createEntityManager();
		em.getTransaction().begin();
		
//		String sql="select e.name from ec2_customer_resource e";
//		Query query = em.createNativeQuery(sql);
//		List aa = query.getResultList();
//		System.out.println(aa);

		String sql1="SELECT d FROM ec2_customer_resource d";
		Query query1 = em.createNativeQuery(sql1);
		List list=query1.getResultList();
		System.out.println(list);
		
		
		em.getTransaction().commit();
	}
	
	public static void createTableKey(Domain domain) {
		EntityManagerFactory emf=Persistence.createEntityManagerFactory("ec2");
		EntityManager em=emf.createEntityManager();
		em.getTransaction().begin();
		
		TableKeyword keyword1=new TableKeyword();
		keyword1.setColumnName("公司");
		keyword1.setDomain(domain);
		em.persist(keyword1);

		TableKeyword keyword2=new TableKeyword();
		keyword2.setColumnName("地址");
		keyword2.setDomain(domain);
		em.persist(keyword2);
		
		TableKeyword keyword3=new TableKeyword();
		keyword3.setColumnName("备注");
		keyword3.setDomain(domain);
		em.persist(keyword3);
		
		TableKeyword keyword4=new TableKeyword();
		keyword4.setColumnName("爱好");
		keyword4.setDomain(domain);
		em.persist(keyword4);

		em.getTransaction().commit();
	}
}
