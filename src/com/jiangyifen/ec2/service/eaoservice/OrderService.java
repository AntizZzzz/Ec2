package com.jiangyifen.ec2.service.eaoservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface OrderService extends FlipSupportService<Order> {

	// common method 
	
	@Transactional
	public Order get(Object primaryKey);
	
	@Transactional
	public void save(Order order);

	@Transactional
	public Order update(Order order);

	@Transactional
	public void delete(Order order);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	// enhanced method
	
	/**
	 * jrh
	 * 保存订单，以及订单详情、商品库存等信息
	 * @param order
	 */
	@Transactional
	public void saveOrderAndOrderDetails(Order order);
	
	/**
	 * jrh
	 * 更新订单，以及订单详情、商品库存等信息
	 * @param order					// 当前要更新的订单
	 * @param oldDetailsOrderNums	// 之前订单对应商品的购买数量
	 * @param needRemoveDetails		// 需要移除的商品
	 */
	@Transactional
	public void updateOrderAndOrderDetails(Order order, HashMap<Long, Integer> oldDetailsOrderNums, ArrayList<Orderdetails> needRemoveDetails);

	/**
	 * jrh
	 * 	根据订单编号查询订单详情
	 * @param orderId
	 * @return
	 */
	@Transactional
	public List<Orderdetails> getOrderDetailsByOrderId(Long orderId);
	
}
