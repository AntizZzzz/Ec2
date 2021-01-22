package com.jiangyifen.ec2.service.mgr.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.eao.CustomerResourceEao;
import com.jiangyifen.ec2.eao.MigrateCustomerLogEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MigrateCustomerLog;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.mgr.MigrateCustomerToCsrService;

public class MigrateCustomerToCsrServiceImpl implements MigrateCustomerToCsrService {
	
	private CustomerResourceEao customerResourceEao;
	private MigrateCustomerLogEao migrateCustomerLogEao;

	@Override
	public void migrateCustomer(User operator, List<CustomerResource> customers, List<User> users) {
		int customerCount = customers.size();
		int userCount = users.size();
		int average = customerCount / userCount;		// 每个人按平均值算，可最大分配个数
		int loop = 1;									// 当个人已分配的人数
		int userIndex = 0;								// 当前用户在Users 集合中的序号
		
		for(CustomerResource customer : customers) {
			// 先按平均数分配，如果每个人都分配到相同的数目后还有剩余，则分给最后一个人
			if(userIndex != (userCount-1) && loop > average) {
				++userIndex;
				loop = 1;
			}
			loop ++;
			
			User oldManager = customer.getAccountManager();
			User newManager = users.get(userIndex);

			MigrateCustomerLog log = new MigrateCustomerLog();
			// 设置操作者信息
			log.setOperatorUserId(operator.getId());
			log.setOperatorUsername(operator.getUsername());
			log.setOperatorEmpNo(operator.getEmpNo());
			log.setOperatorRealName(operator.getRealName());
			log.setOperatorDeptId(operator.getDepartment().getId());
			log.setOperatorDeptName(operator.getDepartment().getName());
			
			// 设置原客户经理信息
			log.setOldManagerUserId(oldManager.getId());
			log.setOldManagerUsername(oldManager.getUsername());
			log.setOldManagerEmpNo(oldManager.getEmpNo());
			log.setOldManagerRealName(oldManager.getRealName());
			log.setOldManagerDeptId(oldManager.getDepartment().getId());
			log.setOldManagerDeptName(oldManager.getDepartment().getName());
		
			// 设置当前客户经理信息
			log.setNewManagerUserId(newManager.getId());
			log.setNewManagerUsername(newManager.getUsername());
			log.setNewManagerEmpNo(newManager.getEmpNo());
			log.setNewManagerRealName(newManager.getRealName());
			log.setNewManagerDeptId(newManager.getDepartment().getId());
			log.setNewManagerDeptName(newManager.getDepartment().getName());
			
			// 设置客户信息
			log.setCustomerId(customer.getId());
			log.setCustomerName(customer.getName());
			if(customer.getCompany() != null) {
				log.setCustomerCompanyName(customer.getCompany().getName());
			}
			
			// 设置如果客户有多个电话，则任意选一个电话号码
			Set<Telephone> telephones = (Set<Telephone>) customer.getTelephones();
			for(Telephone tp : telephones) {
				log.setCustomerDefaultPhone(tp.getNumber());
			}
			
			// 设置其他信息
			log.setMigratedDate(new Date());
			log.setDomainId(operator.getDomain().getId());
			
			migrateCustomerLogEao.save(log);
			
			customer.setAccountManager(newManager);
			customerResourceEao.update(customer);
		}
	}

	public CustomerResourceEao getCustomerResourceEao() {
		return customerResourceEao;
	}

	public void setCustomerResourceEao(CustomerResourceEao customerResourceEao) {
		this.customerResourceEao = customerResourceEao;
	}

	public MigrateCustomerLogEao getMigrateCustomerLogEao() {
		return migrateCustomerLogEao;
	}

	public void setMigrateCustomerLogEao(MigrateCustomerLogEao migrateCustomerLogEao) {
		this.migrateCustomerLogEao = migrateCustomerLogEao;
	}
	
}
