package com.jiangyifen.ec2.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.CustomerResource;

public class CascadeDeleteTest {
	private static EntityManagerFactory entityMangerFactory = Persistence
			.createEntityManagerFactory("ec2");

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EntityManager entityManager = entityMangerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		CustomerResource customerResource = entityManager.find(
				CustomerResource.class, 123598L);

		entityManager.remove(customerResource);

		entityManager.getTransaction().commit();

		entityManager.close();
		entityMangerFactory.close();
	}

}
