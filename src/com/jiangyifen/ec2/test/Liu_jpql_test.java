package com.jiangyifen.ec2.test;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;

public class Liu_jpql_test {

	private static EntityManagerFactory emf = Persistence
			.createEntityManagerFactory("ec2");

	@SuppressWarnings({ "unused", "unchecked" })
	public static void main(String[] args) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		String jpql = "select b from CustomerResourceBatch as b where b.id = 21";

		// String jpql =
		// "select c　from CustomerResource as c,Telephone as t where t.customerResource=c and t.number = 13082822382";
		EntityManager entityManager = emf.createEntityManager();

		entityManager.getTransaction().begin();
		List<CustomerResourceBatch> objects = entityManager.createQuery(jpql).getResultList();
		if(objects.size() > 0) {
			Long t = System.currentTimeMillis();
			Set<CustomerResource> res = objects.get(0).getCustomerResources();

			StringBuffer sb = new StringBuffer();
			
			Long t1 = System.currentTimeMillis();
			System.out.println("=================");
			System.out.println(t1 - t);
			System.out.println("---------------------------------------------------");
			for(CustomerResource c : res) {
				System.out.println(c.getId());
				sb.append(c.getId());
				sb.append(",");
			}
			Long t2 = System.currentTimeMillis();
			System.out.println("**********************************************");
			System.out.println(t2 - t1);
			System.out.println("**********************************************");
			System.out.println(sb.toString());
		}
		System.out.println("objects.size()--> " + objects.size());
//		for (CustomerResource objects2 : objects) {
//
//			System.out.println(objects2.toString());
//
//		}

	}
}
