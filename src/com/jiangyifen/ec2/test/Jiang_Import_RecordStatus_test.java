package com.jiangyifen.ec2.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.CustomerComplaintRecordStatus;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;

public class Jiang_Import_RecordStatus_test {
	
	public static void main(String[] args) {
		importRecordStatus();
	}

	public static void importRecordStatus() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		
		EntityManager em1 = entityMangerFactory.createEntityManager();
		
		em1.getTransaction().begin();
		
		@SuppressWarnings("unchecked")
		List<Domain> domainList = em1.createQuery("select d from Domain as d").getResultList();
		for(Domain domain : domainList) {
			importOutgoingServiceRecordStatus(em1, domain);
			System.out.println("外呼服务记录状态创建完成！");

			importIngoingServiceRecordStatus(em1, domain);
			System.out.println("呼入服务记录状态创建完成！");
			
			importComplainRecordStatus(em1, domain);
			System.out.println("投诉记录状态创建完成！");
		}
		
		em1.getTransaction().commit();
		em1.close();
		entityMangerFactory.close();
	}

	private static void importOutgoingServiceRecordStatus(EntityManager em, Domain domain) {
		// 创建呼出记录的状态
		String[] statuNames = new String[] {"忙音",	 		"停机",		 "关机",
											"无效号码",		"无人接听",	 "预约", 
											"需再联系", 		"意向客户",  	 "成功客户",
											"拒绝-风险",  		"拒绝-信任度",	 "中途拒绝", 
											"拒绝-资金问题",	"拒绝-其他"
		};
		
		Boolean[] isAnswereds = new Boolean[] {	false, 	false, 	false, 
													false, 	false, 	true, 
													true, 	true, 	true, 
													true, 	true, 	true, 
													true, 	true				
		};
		
		for(int i = 0; i < statuNames.length; i++) {
			CustomerServiceRecordStatus status = new CustomerServiceRecordStatus();
			status.setDomain(domain);
			status.setStatusName(statuNames[i]);
			status.setIsAnswered(isAnswereds[i]);
			status.setDirection("outgoing");
			status.setEnabled(true);
			if("成功客户".equals(statuNames[i])) {
				status.setIsMeanVipCustomer(true);
			} else {
				status.setIsMeanVipCustomer(false);
			}
			em.persist(status);
		}
		
	}
	

	private static void importIngoingServiceRecordStatus(EntityManager em, Domain domain) {
		// 创建呼出记录的状态
		String[] statuNames = new String[] {"预约", 		"需再联系", 		"意向客户",  	 
											"成功客户", 	"拒绝-风险",   	"拒绝-信任度",	 
											"中途拒绝",	"拒绝-资金问题",  	"拒绝-其他"
		};
//		// 创建呼出记录的状态
//		String[] statuNames = new String[] {"预约(呼入)", 		"需再联系(呼入)", 		"意向客户(呼入)",  	 
//				"成功客户(呼入)", 	"拒绝-风险(呼入)",   	"拒绝-信任度(呼入)",	 
//				"中途拒绝(呼入)",	"拒绝-资金问题(呼入)",  	"拒绝-其他(呼入)"
//		};
		
		Boolean[] isAnswereds = new Boolean[] {	 	true, true, 	true, 	
													true, 	true, 	true, 
													true, 	true,	true			
		};
		
		for(int i = 0; i < statuNames.length; i++) {
			CustomerServiceRecordStatus status = new CustomerServiceRecordStatus();
			status.setDomain(domain);
			status.setStatusName(statuNames[i]);
			status.setIsAnswered(isAnswereds[i]);
			status.setDirection("incoming");
			status.setEnabled(true);
			if("成功客户".equals(statuNames[i])) {
				status.setIsMeanVipCustomer(true);
			} else {
				status.setIsMeanVipCustomer(false);
			}
			em.persist(status);
		}
				
	}

	private static void importComplainRecordStatus(EntityManager em, Domain domain) {
		
		String[] statuNames = new String[] {"已受理", "处理中", "已关闭"};
			
		for(int i = 0; i < statuNames.length; i++) {
			CustomerComplaintRecordStatus status = new CustomerComplaintRecordStatus();
			status.setDomain(domain);
			status.setStatusName(statuNames[i]);
			em.persist(status);
		}
	}
}
