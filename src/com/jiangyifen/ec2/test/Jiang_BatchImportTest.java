package com.jiangyifen.ec2.test;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;

public class Jiang_BatchImportTest {
	
	public static void main(String[] args) {
		EntityManagerFactory entityMangerFactory = Persistence.createEntityManagerFactory("ec2");
		EntityManager em = entityMangerFactory.createEntityManager();

		em.getTransaction().begin();
		// 先存储功能模块，因为之后创建用户的时候需要从数据库中获取
//		Jiang_Import_BussinessControl_test.importAdminAuthority(em);
		em.getTransaction().commit();
		
		
//		em.getTransaction().begin();
//		// 批量存储系统配置属性
////		Jiang_ConfigTest.importConfiguration(em);
//		em.getTransaction().commit();
		
		em.getTransaction().begin();
		// 批量导入用户、角色、部门
		importBatchURD(em);
		em.getTransaction().commit();
		
//		em.getTransaction().begin();
//		 //批量创建项目计划
//		Chen_AddProjectsTest.createProjects(em);
//		em.getTransaction().commit();
		
		em.close();
		entityMangerFactory.close();
	}
	
	@SuppressWarnings("unchecked")
	private static void importBatchURD(EntityManager em) {
		Ec2Configuration config1 = new Ec2Configuration();
		config1.setKey("mobile_num_secret");
		config1.setValue(true);
		em.persist(config1);

		Domain defaultDomain = new Domain();
		defaultDomain.setName("default_domain");
		defaultDomain.getConfigs().add(config1);
		em.persist(defaultDomain);
		
		for(int d = 1; d < 3; d++) {
			Ec2Configuration config2 = new Ec2Configuration();
			config2.setKey("mobile_num_secret");
			config2.setValue(false);
			if(d != 1) {
				em.persist(config2);
			}
			
			Domain domain = new Domain();
			domain.setName("Domain " + d);
			if(d != 1) {
				domain.getConfigs().add(config2);
			}
			em.persist(domain);
			
			Chen_AddProjectsTest.createProjects(em,domain);
			Chen_ParseJsonAddress.saveToDBbyEm(em);
			
			
// TODO 自动创建部门
			Set<Department> depts = new HashSet<Department>();
			int deptCount = 5;
			for(long i = 1; i <= deptCount; i++) {
				Department dept = new Department();
				dept.setDomain(domain);
				dept.setName("部门"+i);
				if(i > 1) {
					dept.setParent(em.find(Department.class, i-1));
				}
				em.persist(dept);
				depts.add(dept);
			}
			
			int deptSize = 1;
			int roleCount = 4;
// TODO 自动创建角色			
			for(int i = 1; i <= roleCount; i++) {
				Role role = new Role();
				role.setName("角色"+i);
				role.setDomain(domain);
				for(int x = deptSize; x < ( deptSize + (roleCount/4) ); x++) {
					role.getDepartments().add(em.find(Department.class, (long)x));
				}
				deptSize = deptSize + (roleCount/4);
				em.persist(role);
			}
			
// TODO 自动创建用户			
			int userCount = 25;
			int phoneNumber = 1;
			int roleSize = 1;
			int age = 20;
			int deptGap = 5;
			int deptId = 1;
			for(int i = 1; i <= userCount; i++) {
				User user = new User();
				String empNoStr="1";
				Random rand=new Random();
				for(int k=0;k<3;k++){
					empNoStr+=rand.nextInt(10);
				}
				user.setEmpNo(empNoStr);
				
				user.setRealName(d+"姓名"+i);
				user.setUsername(d+"用户昵称"+i);
				user.setPassword(d+"password"+i);
				if((i % (userCount/5)) == 0) {
					age = 20;
				} else {
					age++;
				}
				user.setAge(age);
				
				if(i % 2 == 0) {
					user.setGender("男");
				} else {
					user.setGender("女");
				}
				
				if(phoneNumber < 10) {
					user.setPhoneNumber(d+"800 0000 00"+phoneNumber);
				} else if(phoneNumber >= 10 && phoneNumber < 100) {
					user.setPhoneNumber(d+"800 0000 0"+phoneNumber);
				} else {
					user.setPhoneNumber(d+"800 0000 "+phoneNumber);
				}
				user.setRegistedDate(new Date());
				user.setEmailAddress(d+"yonghu"+i+"@126.com");
				user.setDomain(domain);
				
				// 每五个用户在同一个部门
				if(i <= deptGap) {
					user.setDepartment(em.find(Department.class, (long)deptId));
				}
				
				if(i % 5 == 0) {
					deptGap = deptGap +5;
					deptId++;
				}
				
				if(roleSize % (roleCount+1) == 0) {
					roleSize = 1;
				}
				
				user.getRoles().add(em.find(Role.class, (long)roleSize));
				roleSize++;
				
				phoneNumber++;
				em.persist(user);
			}

// TODO 手动创建部门
			
			Department department1 = new Department();
			department1.setName("销售部");

			Department department2 = new Department();
			department2.setName("研发部");
			
			Department department3 = new Department();
			department3.setName("人力部");
			
			Department department4 = new Department();
			department4.setName("财务部");
			
			Department department5 = new Department();
			department5.setName("人_财部");
			
			department1.setDomain(domain);
			department2.setDomain(domain);
			department3.setDomain(domain);
			department4.setDomain(domain);
			department5.setDomain(domain);

			department3.setParent(department5);
			department4.setParent(department5);
			
			em.persist(department1);
			em.persist(department2);
			em.persist(department3);
			em.persist(department4);
			em.persist(department5);
			
			deptSize = 1;
			
// TODO 手动创建角色
			
			// 从数据库中获取所有 manager 的权限
			Set<BusinessModel> csrModules = new HashSet<BusinessModel>();
			List<BusinessModel> csrBusinessModels = em.createQuery("select model " +
					"from BusinessModel as model where model.application = 'csr'").getResultList();
			csrModules.addAll(csrBusinessModels);
			
			Role csr = new Role();
			csr.setName("csr_role");
			csr.setDomain(domain);
			csr.setType(RoleType.csr);
			csr.setBusinessModels(csrModules);
			for(int x = deptSize; x < deptSize+5; x++) {
				csr.getDepartments().add(em.find(Department.class, (long)x));
			}
			csr.getDepartments().add(department1);
			em.persist(csr);
			
			// 从数据库中获取所有 manager 的权限
			Set<BusinessModel> partCsrModules = new HashSet<BusinessModel>();
			for(BusinessModel model : csrBusinessModels) {
				if("TaskManage&OutgoingTask&EditCustomer".equals(model.toString()))
					continue;
				partCsrModules.add(model);
			}
			
//			deptSize = deptSize + 5;
			Role partCsr = new Role();
			partCsr.setName("part_csr");
			partCsr.setDomain(domain);
			partCsr.setType(RoleType.csr);
			partCsr.setBusinessModels(partCsrModules);
			for(int x = deptSize; x < deptSize+5; x++) {
				partCsr.getDepartments().add(em.find(Department.class, (long)x));
			}
			partCsr.getDepartments().add(department1);
			em.persist(partCsr);
			
			// 从数据库中获取所有 manager 的权限
			Set<BusinessModel> managerModules = new HashSet<BusinessModel>();
			List<BusinessModel> managerBusinessModels = em.createQuery("select model " +
					"from BusinessModel as model where model.application = 'manager'").getResultList();
			managerModules.addAll(managerBusinessModels);
			
//			deptSize = deptSize + 5;
			Role manager = new Role();
			manager.setName("manager_role");
			manager.setDomain(domain);
			manager.setType(RoleType.manager);
			manager.setBusinessModels(managerModules);
			for(int x = deptSize; x < deptSize+5; x++) {
				manager.getDepartments().add(em.find(Department.class, (long)x));
			}
			manager.getDepartments().add(department2);
			em.persist(manager);
			
			// 添加一个只有部分功能权限的、类型为manager 的角色
			Set<BusinessModel> partMgrModules = new HashSet<BusinessModel>();
			for(int i = 0; i < managerBusinessModels.size(); i++) {
				if(i == 3) break;
				partMgrModules.add(managerBusinessModels.get(i));
			}
			
//			deptSize = deptSize + 5;
			Role partMgr = new Role();
			partMgr.setName("part_manager");
			partMgr.setDomain(domain);
			partMgr.setType(RoleType.manager);
			partMgr.setBusinessModels(partMgrModules);
			for(int x = deptSize; x < deptSize+5; x++) {
				partMgr.getDepartments().add(em.find(Department.class, (long)x));
			}
			partMgr.getDepartments().add(department3);
			em.persist(partMgr);
			
			// 从数据库中获取所有 admin 的权限
			Set<BusinessModel> adminModules = new HashSet<BusinessModel>();
			List<BusinessModel> adminBusinessModels = em.createQuery("select model " +
					"from BusinessModel as model where model.application = 'admin'").getResultList();
			adminModules.addAll(adminBusinessModels);
			
//			deptSize = deptSize + 5;
			Role admin = new Role();
			admin.setName("admin_role");
			admin.setDomain(domain);
//			admin.setType(RoleType.admin);
			admin.setBusinessModels(adminModules);
			for(int x = deptSize; x < deptSize+5; x++) {
				admin.getDepartments().add(em.find(Department.class, (long)x));
			}
			admin.getDepartments().add(department4);
			em.persist(admin);
			

			// 添加一个只有部分功能权限的、类型为Admin 的角色
			Set<BusinessModel> partAdminModules = new HashSet<BusinessModel>();
			for(int i = 0; i < adminBusinessModels.size(); i++) {
				if(i == 2) continue;
				if(i == 4) break;
				partAdminModules.add(adminBusinessModels.get(i));
			}
//			deptSize = deptSize + 5;
			Role part_admin = new Role();
			part_admin.setName("part_admin");
			part_admin.setDomain(domain);
//			part_admin.setType(RoleType.admin);
			part_admin.setBusinessModels(partAdminModules);
			for(int x = deptSize; x < deptSize+5; x++) {
				part_admin.getDepartments().add(em.find(Department.class, (long)x));
			}
			part_admin.getDepartments().add(department5);
			em.persist(part_admin);
			
// TODO 手动创建用户
			
			User adminUser = new User();
			adminUser.setEmpNo("111"+d);
			adminUser.setRealName("All_Admin"+d);
			adminUser.setUsername("admin"+d);
			adminUser.setPassword("admin"+d);
			adminUser.setAge(24);
			adminUser.setGender("男");
			adminUser.setPhoneNumber("1381545265"+d);
			adminUser.setRegistedDate(new Date());
			adminUser.setEmailAddress("xiaoqin"+d+"@126.com");
			adminUser.setDomain(domain);
			
			User partAdminUser = new User();
			partAdminUser.setEmpNo("115"+d);
			partAdminUser.setRealName("Part_Admin"+d);
			partAdminUser.setUsername("padmin"+d);
			partAdminUser.setPassword("padmin"+d);
			partAdminUser.setAge(24);
			partAdminUser.setGender("男");
			partAdminUser.setPhoneNumber("1381545263"+d);
			partAdminUser.setRegistedDate(new Date());
			partAdminUser.setEmailAddress("xiaowang"+d+"@126.com");
			partAdminUser.setDomain(domain);
			
			User managerUser = new User();
			managerUser.setEmpNo("112"+d);
			managerUser.setRealName("All_Manager"+d);
			managerUser.setUsername("manager"+d);
			managerUser.setPassword("manager"+d);
			managerUser.setAge(26);
			managerUser.setGender("男");
			managerUser.setPhoneNumber("1381545862"+d);
			managerUser.setRegistedDate(new Date());
			managerUser.setEmailAddress("xiaoqinang"+d+"@126.com");
			managerUser.setDomain(domain);
			
			User partManagerUser = new User();
			partManagerUser.setEmpNo("116"+d);
			partManagerUser.setRealName("Part_Manager"+d);
			partManagerUser.setUsername("pmanager"+d);
			partManagerUser.setPassword("pmanager"+d);
			partManagerUser.setAge(18);
			partManagerUser.setGender("女");
			partManagerUser.setPhoneNumber("1381545861"+d);
			partManagerUser.setRegistedDate(new Date());
			partManagerUser.setEmailAddress("honghong"+d+"@126.com");
			partManagerUser.setDomain(domain);

			User csrUser = new User();
			csrUser.setEmpNo("113"+d);
			csrUser.setRealName("All_Csr"+d);
			csrUser.setUsername("csrcsr"+d);
			csrUser.setPassword("csrcsr"+d);
			csrUser.setAge(21);
			csrUser.setGender("男");
			csrUser.setPhoneNumber("1384578862"+d);
			csrUser.setRegistedDate(new Date());
			csrUser.setEmailAddress("maolin"+d+"@126.com");
			csrUser.setDomain(domain);
			
			User partCsrUser = new User();
			partCsrUser.setEmpNo("1114"+d);
			partCsrUser.setRealName("Part_Csr"+d);
			partCsrUser.setUsername("partcsr"+d);
			partCsrUser.setPassword("partcsr"+d);
			partCsrUser.setAge(21);
			partCsrUser.setGender("男");
			partCsrUser.setPhoneNumber("1384578862"+d);
			partCsrUser.setRegistedDate(new Date());
			partCsrUser.setEmailAddress("maolin"+d+"@126.com");
			partCsrUser.setDomain(domain);
			
			// 设置用户所在部门
			adminUser.setDepartment(department1);
			partAdminUser.setDepartment(department3);
			managerUser.setDepartment(department2);
			partManagerUser.setDepartment(department3);
			csrUser.setDepartment(department3);
			partCsrUser.setDepartment(department5);
			
			// 向从表对象中添加关联对象
			adminUser.getRoles().add(admin);
			adminUser.getRoles().add(manager);
			partAdminUser.getRoles().add(part_admin);
			managerUser.getRoles().add(manager);
			partManagerUser.getRoles().add(partMgr);
			csrUser.getRoles().add(csr);
			partCsrUser.getRoles().add(partCsr);

			em.persist(adminUser);
			em.persist(partAdminUser);
			em.persist(managerUser);
			em.persist(partManagerUser);
			em.persist(csrUser);
			em.persist(partCsrUser);
		}
		
	}

}
