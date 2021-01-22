package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import com.jiangyifen.ec2.eao.CustomerServiceRecordEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.User;

public class CustomerServiceRecordEaoImpl extends BaseEaoImpl implements CustomerServiceRecordEao {

	@SuppressWarnings("unchecked")
	@Override
	public CustomerServiceRecord get(CustomerResource customer, MarketingProject project,User user) {
		List<CustomerServiceRecord> recordList = (List<CustomerServiceRecord>) getEntityManager().createQuery("select c from CustomerServiceRecord as c " +
				"where c.creator.id = "+ user.getId() + " and c.marketingProject.id = " + project.getId() + " and c.customerResource.id = " + customer.getId() +
						" order by c.createDate desc ").getResultList();
		if(recordList.size() > 0) {
			return recordList.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerServiceRecord> getAllByCustomer(CustomerResource customer) {
		EntityManager em = getEntityManager().getEntityManagerFactory().createEntityManager();
		List<CustomerServiceRecord> recordList = (List<CustomerServiceRecord>) em.createQuery("select c from CustomerServiceRecord as c " +
				"where c.customerResource.id = " + customer.getId() + " order by c.createDate desc ").getResultList();
		return recordList;
	}
	
}
