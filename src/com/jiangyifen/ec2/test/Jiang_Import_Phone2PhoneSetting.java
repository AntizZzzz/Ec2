package com.jiangyifen.ec2.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.User;

public class Jiang_Import_Phone2PhoneSetting {
	

	public static void main(String[] args) {
		importGlobalP2PSetting();
	}

	@SuppressWarnings("unchecked")
	private static void importGlobalP2PSetting() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em2 = entityMangerFactory.createEntityManager();

		em2.getTransaction().begin();

		List<Domain> domainList = em2.createQuery("select d from Domain as d").getResultList();
		for(Domain domain : domainList) {
//			importGlobalP2PSetting(em2, domain);
			importCustomP2PSetting2(em2, domain);
		}
		System.out.println("成功");

		em2.getTransaction().commit();
		em2.close();
		entityMangerFactory.close();
	}

	@SuppressWarnings("unused")
	private static void importGlobalP2PSetting(EntityManager em2, Domain domain) {
		User creator = (User) em2.createQuery("select u from User as u where u.id = 1").getSingleResult();
		Phone2PhoneSetting p2pSetting = new Phone2PhoneSetting();
		p2pSetting.setCreator(creator);
		p2pSetting.setDayOfWeekType("custom");
		
		Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
		daysOfWeek.add(DayOfWeek.mon);
		daysOfWeek.add(DayOfWeek.fri);
		p2pSetting.setDaysOfWeek(daysOfWeek);
		p2pSetting.setDomain(domain);
		
		p2pSetting.setIsGlobalSetting(true);
		p2pSetting.setIsLicensed2Csr(false);
		p2pSetting.setIsStartedRedirect(false);
		p2pSetting.setIsSpecifiedPhones(false);

		Set<String> types = new HashSet<String>();
		types.add("busy");
		types.add("unonline");
		p2pSetting.setRedirectTypes(types);
		
		p2pSetting.setStartTime("18:00");
		p2pSetting.setStopTime("22:59");
		
		Set<String> phones = new HashSet<String>();
		phones.add("13816760398");
		phones.add("15002580432");
		p2pSetting.setSpecifiedPhones(phones);
		
		em2.persist(p2pSetting);
//		em2.merge(p2pSetting);
	}
	
	/**
	 * 
	 * @param em2
	 * @param domain
	 */
	private static void importCustomP2PSetting2(EntityManager em2, Domain domain) {
		User creator = (User) em2.createQuery("select u from User as u where u.id = 2").getSingleResult();
		Phone2PhoneSetting p2pSetting = new Phone2PhoneSetting();
		p2pSetting.setCreator(creator);
		p2pSetting.setDayOfWeekType("custom");
		
		Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
		daysOfWeek.add(DayOfWeek.mon);
		daysOfWeek.add(DayOfWeek.fri);
//		p2pSetting.setDaysOfWeek(daysOfWeek);
		p2pSetting.setDomain(domain);

		p2pSetting.setIsGlobalSetting(false);
		p2pSetting.setIsStartedRedirect(false);
		
		Set<String> types = new HashSet<String>();
		types.add("busy");
		types.add("unonline");
		p2pSetting.setRedirectTypes(types);
		
		p2pSetting.setStartTime("18:00");
		p2pSetting.setStopTime("22:59");
		
		em2.persist(p2pSetting);
	}

}
