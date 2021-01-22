package com.jiangyifen.ec2.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.entity.Cdr;

public class CdrTest {
	private static EntityManagerFactory entityMangerFactory = Persistence
			.createEntityManagerFactory("ec2");

	public static void main(String[] args) {

		EntityManager em = entityMangerFactory.createEntityManager();
		em.getTransaction().begin();

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startDate = null;
		try {
			startDate = sdf.parse("2010-09-29 07:53:36");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);

		Random rand = new Random();
		for (int i = 0; i < 2000; i++) {
			Cdr cdr = new Cdr();
			cdr.setAccountCode(i + "accountcode");
			cdr.setSrc("80"+i);
			cdr.setAmaflags("amaflags");
			cdr.setUniqueId(rand.nextInt(50000)+"");
			Date date = cal.getTime();
			try {
				date = sdf.parse(sdf.format(date));
				cdr.setAnswerTimeDate(date);
				cdr.setStartTimeDate(date);
				cal.add(Calendar.HOUR_OF_DAY, +1);
				date = cal.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			cdr.setBillableSeconds(5);
			cdr.setCallerId(i+"");
			cdr.setDestination("13816760398");
			cdr.setChannel("SIP/"+i+"-0000003");
			cdr.setDestDeptId(Long.parseLong(i + ""));
			cdr.setDestDeptName("");
			cdr.setDestEmpNo("");
			cdr.setDestinationChannel("SIP/13816760398-000010");
			cdr.setDestinationContext("outgoing");
			cdr.setDestRealName("李四");
			cdr.setDestUserId(Long.parseLong(i + ""));
			cdr.setDestUsername("");
			if(i < 200) {
				cdr.setSrcDeptId(1L);
				cdr.setSrcDeptName("研发部");
				if(i % 2 == 0) {
					cdr.setCdrDirection(CdrDirection.e2o);
					cdr.setDisposition("ANSWERED");
				} else {
					cdr.setCdrDirection(CdrDirection.o2e);
					cdr.setDisposition("NO ANWERED");
				}
			} else if(i%2 == 0) {
				cdr.setCdrDirection(CdrDirection.e2o);
				cdr.setDisposition("ANSWERED");
				cdr.setSrcDeptName("采购部");
				cdr.setSrcDeptId(2L);
			} else {
				cdr.setCdrDirection(CdrDirection.o2e);
				cdr.setSrcDeptId(1L);
				cdr.setSrcDeptName("研发部");
				cdr.setDisposition("NO ANWERED");
			}
			cdr.setDomainId(1L);
			cdr.setDuration(i);
			cdr.setEndTimeDate(new Date());
			cdr.setLastApplication(i + "lastapplication");
			cdr.setLastData(i + "lastdata");
			if(i<200) {
				cdr.setSrcEmpNo("1001");
				cdr.setSrc("1001");
				cdr.setSrcRealName("张三");
				cdr.setSrcUserId(Long.parseLong("2"));
				cdr.setSrcUsername("1001");
			} else {
				cdr.setSrcEmpNo(i+"");
				cdr.setSrc(i+"");
				cdr.setSrcRealName("张三"+i);
				cdr.setSrcUserId(Long.parseLong(i + ""));
				cdr.setSrcUsername(i+"");
			}

			em.persist(cdr);

		}
		
		System.out.println("ok");

		em.getTransaction().commit();
		em.close();
		entityMangerFactory.close();
	}
}
