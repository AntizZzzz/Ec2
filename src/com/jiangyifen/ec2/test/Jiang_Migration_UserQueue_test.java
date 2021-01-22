package com.jiangyifen.ec2.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.UserQueue;

public class Jiang_Migration_UserQueue_test {

	private static List<Object[]> oldUserQueueList;
	
	public static void main(String[] args) {
		migrationUserQueue();
		System.out.println("\n 迁移成功 \n");
	}
	
	public static void migrationUserQueue() {
		getQueueOldDataBase();
		
		importUserQueue();
		
	}

	@SuppressWarnings("unchecked")
	private static void getQueueOldDataBase() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec_old");
		EntityManager em_old = entityMangerFactory.createEntityManager();
		em_old.getTransaction().begin();
		
		oldUserQueueList= em_old.createNativeQuery("select * from ec_user_queue_link").getResultList();
		
		em_old.getTransaction().commit();
		em_old.close();
		entityMangerFactory.close();
	}
	
	@SuppressWarnings("unchecked")
	private static void importUserQueue() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em_new = entityMangerFactory.createEntityManager();
		
		em_new.getTransaction().begin();

		List<Domain> domains = em_new.createQuery("select d from Domain as d").getResultList();
		for(Domain domain : domains) {
			for(Object[] os : oldUserQueueList) {
				UserQueue userQueue = new UserQueue();
				userQueue.setUsername(os[0].toString());
				userQueue.setQueueName(os[1].toString() +"_"+ domain.getId());
				userQueue.setPriority(5);
				userQueue.setDomain(domain);
				
				em_new.persist(userQueue);
			}
		}
		
		
		em_new.getTransaction().commit();
		em_new.close();
		entityMangerFactory.close();
	}
}