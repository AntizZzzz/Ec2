package com.jiangyifen.ec2.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.PauseReason;

public class Jiang_Import_PauseReason_test {
	public static void main(String[] args) {
		createPauseReasonsToDB();
	}

	// 创建置忙原因
	public static void createPauseReasonsToDB() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		
		EntityManager em1 = entityMangerFactory.createEntityManager();
		
		em1.getTransaction().begin();
		
		@SuppressWarnings("unchecked")
		List<Domain> domainList = em1.createQuery("select d from Domain as d").getResultList();
		for(Domain domain : domainList) {
			importPauseReason(em1, domain);
		}
		System.out.println("导入置忙原因成功！");
		em1.getTransaction().commit();
		em1.close();
		entityMangerFactory.close();
	}

	private static void importPauseReason(EntityManager em, Domain domain) {
		String[] reasons = new String[]{"有事离开", "开会中", "忙碌中"};
		
		for(String reason : reasons) {
			PauseReason pauseReason = new PauseReason();
			pauseReason.setEnabled(true);
			pauseReason.setReason(reason);
			pauseReason.setDomain(domain);
			em.persist(pauseReason);
		}
	}
	
}
