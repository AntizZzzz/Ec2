package com.jiangyifen.ec2.service.mgr;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.User;

/**
 * 迁移客户
 * @author jrh
 */
public interface MigrateCustomerToCsrService {

	/**
	 * jrh 
	 * 	迁移客户
	 *  先按平均数分配，如果每个人都分配到相同的数目后还有剩余，则分给最后一个人
	 * @param operator
	 * @param customers
	 * @param users
	 */
	@Transactional
	public void migrateCustomer(User operator, List<CustomerResource> customers, List<User> users);
	
}
