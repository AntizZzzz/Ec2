package com.jiangyifen.ec2.test;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.MobileAreacode;


public class Chen_Common {
	/**
	 *  存储地区和区号的对应关系，为了减轻数据库压力，只加载一次
	 */
	public static List<MobileAreacode> areaPostcodeList=new ArrayList<MobileAreacode>();
	
	
	
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("ec2");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		
		em.getTransaction().commit();
		em.close();
	}
}
