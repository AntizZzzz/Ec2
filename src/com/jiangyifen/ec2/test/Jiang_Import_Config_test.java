package com.jiangyifen.ec2.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.Ec2Configuration;

public class Jiang_Import_Config_test {
	
	public static void main(String[] args) {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		
		EntityManager em1 = entityMangerFactory.createEntityManager();
		
		em1.getTransaction().begin();
		
		importConfiguration(em1);
		
		em1.getTransaction().commit();
		em1.close();
		entityMangerFactory.close();
	}

	public static void importConfiguration(EntityManager em) {
		Ec2Configuration config1 = new Ec2Configuration();
		
		config1.setKey("mobile_num_secret");
		config1.setValue(true);
		em.persist(config1);
		
		Ec2Configuration config2 = new Ec2Configuration();
		config2.setKey("mobile_num_secret");
		config2.setValue(false);
		
		em.persist(config2);
	}
	
}
