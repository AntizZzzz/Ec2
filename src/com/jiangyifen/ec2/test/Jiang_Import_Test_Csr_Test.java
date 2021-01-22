package com.jiangyifen.ec2.test;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;

public class Jiang_Import_Test_Csr_Test {

	public static void main(String[] args) {

		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		
		EntityManager em = entityMangerFactory.createEntityManager();
		
		em.getTransaction().begin();
		
		import_test_csrs(em);
		System.out.println("success");
		
		em.getTransaction().commit();
		em.close();
		entityMangerFactory.close();
	}

	private static void import_test_csrs(EntityManager em) {
		Department dept = getDepartment(em);
		Domain domain = getDomain(em);
		Set<Role> roles = new HashSet<Role>();
		roles.add(getCsrRole(em));
		
		for(int i = 1001; i < 1500; i++) {
			User csr = new User();
			csr.setUsername(""+i);
			csr.setEmpNo(""+i);
			csr.setPassword(""+i);
			csr.setDomain(domain);
			csr.setRealName("csr"+i);
			csr.setDepartment(dept);
			csr.setRegistedDate(new Date());
			csr.setRoles(roles);
			em.persist(csr);
		}
		
	}
	
	private static Domain getDomain(EntityManager em) {
		return (Domain) em.createQuery("select d from Domain as d where d.id = 1").getSingleResult();
	}
	
	private static Department getDepartment(EntityManager em) {
		Department dept = (Department) em.createQuery("select dept from Department as dept where dept.id = 1").getSingleResult();
		return dept;
	}
	
	private static Role getCsrRole(EntityManager em) {
		return (Role) em.createQuery("select r from Role as r where r.name = 'csr' and r.domain.id = 1").getSingleResult();
	}
}
