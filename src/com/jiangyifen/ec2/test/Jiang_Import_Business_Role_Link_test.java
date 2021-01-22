package com.jiangyifen.ec2.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Role;

/**
 * 将数据表Role 与 功能模块表 BusinessModel 关联起来
 *	插入关联值
 */
public class Jiang_Import_Business_Role_Link_test {

	public static void main(String[] args) {
		buildLink();
	}
	
	public static void buildLink() {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em2 = entityMangerFactory.createEntityManager();
		
		em2.getTransaction().begin();
		
		createLink(em2);
		System.out.println("businessmodel 与 role 关联成功");
		
		em2.getTransaction().commit();
		em2.close();
		entityMangerFactory.close();
	}

	@SuppressWarnings("unchecked")
	public static void createLink(EntityManager em) {
		List<Role> roles = em.createQuery("select r from Role as r").getResultList();
		List<BusinessModel> models = em.createQuery("select bm from BusinessModel as bm").getResultList();
		Set<BusinessModel> csrModels = new HashSet<BusinessModel>();
		Set<BusinessModel> managerModels = new HashSet<BusinessModel>();
		
		for(BusinessModel model : models) {
			if ("manager".equals(model.getApplication())) {
				managerModels.add(model);
			} else if ("csr".equals(model.getApplication())) {
				csrModels.add(model);
			}
		}
		
		for(Role role : roles) {
			if(role.getType().getIndex() == 0) {
				role.setBusinessModels(csrModels);
			} else if(role.getType().getIndex() == 1) {
				role.setBusinessModels(managerModels);
			} 
		}
	}
}
