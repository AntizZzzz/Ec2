package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.OrderdetailsEao;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.service.eaoservice.OrderdetailsService;

/**
 * @Description 描述：
 *
 * @author  JRH
 * @date    2014年5月26日 下午1:15:34
 * @version v1.0.0
 */
public class OrderdetailsServiceImpl implements OrderdetailsService {
	
	private OrderdetailsEao orderdetailsEao;

	public OrderdetailsEao getOrderdetailsEao() {
		return orderdetailsEao;
	}

	public void setOrderdetailsEao(OrderdetailsEao orderdetailsEao) {
		this.orderdetailsEao = orderdetailsEao;
	}
	
	// common method
	
	@Override
	public Orderdetails get(Object primaryKey) {
		return orderdetailsEao.get(Orderdetails.class, primaryKey);
	}

	@Override
	public void save(Orderdetails orderdetails) {
		orderdetailsEao.save(orderdetails);
	}

	@Override
	public Orderdetails update(Orderdetails orderdetails) {
		return (Orderdetails) orderdetailsEao.update(orderdetails);
	}

	@Override
	public void delete(Orderdetails orderdetails) {
		orderdetailsEao.delete(orderdetails);
	}

	@Override
	public void deleteById(Object primaryKey) {
		orderdetailsEao.delete(Orderdetails.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Orderdetails> loadPageEntities(int start, int length, String sql) {
		return orderdetailsEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return orderdetailsEao.getEntityCount(sql);
	}

	// enhanced method

}
