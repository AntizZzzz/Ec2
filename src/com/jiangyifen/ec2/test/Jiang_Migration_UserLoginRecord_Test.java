package com.jiangyifen.ec2.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.UserLoginRecord;

public class Jiang_Migration_UserLoginRecord_Test {
	
	private static ArrayList<UserLoginRecord> loginRecords = new ArrayList<UserLoginRecord>();
	
	public static void main(String[] args) {
		run();
	}
	
	public static void run() {
		
		getUserLoginRecordFromOldDataBase();
		
		importUserLoginRecord();
	}

	@SuppressWarnings("unchecked")
	private static void getUserLoginRecordFromOldDataBase() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec_old");
		EntityManager em_old = entityMangerFactory.createEntityManager();
		em_old.getTransaction().begin();
		
		List<Object[]> loginRecordStrs= em_old.createNativeQuery("select * from ec_user_login_record as r").getResultList();
		
		int i = 1;
		for(Object[] os : loginRecordStrs) {
			for(Object o : os) {
				System.out.println(o);
			}
			UserLoginRecord record = new UserLoginRecord();
			Long id = Integer.valueOf(os[0]+"").longValue();
			record.setId(id);
			record.setUsername(os[1].toString());
			record.setExten(os[2].toString());
			record.setLoginDate((Date)os[3]);
			record.setLogoutDate((Date)os[4]);
			loginRecords.add(record);
			
			System.out.println("---------------------" + (i++) + "----------------");
		}
		
		em_old.getTransaction().commit();
		em_old.close();
		entityMangerFactory.close();
	}

	private static void importUserLoginRecord() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em_new = entityMangerFactory.createEntityManager();
		
		em_new.getTransaction().begin();
		
		for(int i=0; i<loginRecords.size(); i++) {
			em_new.persist(loginRecords.toArray()[i]);
		}
		
		em_new.getTransaction().commit();
		em_new.close();
		entityMangerFactory.close();
	}

}
