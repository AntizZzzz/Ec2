package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.service.eaoservice.OrderService;

public class OrderServiceImpl implements OrderService {
	
	private CommonEao commonEao ;

	// common method 
	
	@Override
	public Order get(Object primaryKey) {
		return commonEao.get(Order.class, primaryKey);
	}

	@Override
	public void save(Order order) {
		commonEao.save(order);
	}

	@Override
	public Order update(Order order) {
		return (Order) commonEao.update(order);
	}

	@Override
	public void delete(Order order) {
		commonEao.delete(order);
	}

	@Override
	public void deleteById(Object primaryKey) {
		commonEao.delete(Order.class, primaryKey);
	}

	// enhance function method
	
	/**  jrh  保存订单，已经订单详情 <br/> @param order */
	@Override
	public void saveOrderAndOrderDetails(Order order) {
		for(Orderdetails orderDetail : order.getOrderdetails()) {
			// 更新商品的库存
			Commodity commodity = orderDetail.getCommodity();
			long currentQty = commodity.getStockQty();	// 当前库存
			if(currentQty > 0) {
				long stockQty = currentQty - orderDetail.getOrderNum();
				if(stockQty < 0) {
					stockQty = 0;
				}
				commodity.setStockQty(stockQty);
				commonEao.update(commodity);
			}
			
			commonEao.save(orderDetail);
		}
		commonEao.save(order);
	}
	
	/**  jrh  更新订单，以及订单详情、商品库存等信息 <br/> @param order */
	@Override
	public void updateOrderAndOrderDetails(Order order, HashMap<Long, Integer> oldDetailsOrderNums, ArrayList<Orderdetails> needRemoveDetails) {
		for(Orderdetails detail : needRemoveDetails) {	// 删除详细记录，需要将库存量上升
			commonEao.delete(Orderdetails.class, detail.getId());
			// 更新商品的库存
			Commodity commodity = detail.getCommodity();
			long currentQty = commodity.getStockQty();	// 当前库存
			long stockQty = currentQty + detail.getOrderNum();
			commodity.setStockQty(stockQty);
			commonEao.update(commodity);
		}
		
		for(Orderdetails orderDetail : order.getOrderdetails()) {
			// 更新商品的库存
			Integer oldOrderNum = oldDetailsOrderNums.get(orderDetail.getId());
			if(oldOrderNum == null) {
				oldOrderNum = 0;
			}
			Commodity commodity = orderDetail.getCommodity();
			long currentQty = commodity.getStockQty()+oldOrderNum;	// 当前库存+当前要更新的商品订单详情的个数
			if(currentQty > 0) {
				long stockQty = currentQty - orderDetail.getOrderNum();
				if(stockQty < 0) {
					stockQty = 0;
				}
				commodity.setStockQty(stockQty);
				commonEao.update(commodity);
			}

			if(orderDetail.getId() == null) {
				commonEao.save(orderDetail);
			} else {
				commonEao.update(orderDetail);
			}
		}

		commonEao.update(order);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Orderdetails> getOrderDetailsByOrderId(Long orderId) {
		return commonEao.getEntityManager().createQuery("select e from Orderdetails as e where e.order.id = "+orderId).getResultList();
	}

	//flip method
	@Override
	public int getEntityCount(String sql) {
		return commonEao .getEntityCount(sql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Order> loadPageEntities(int startIndex, int pageRecords,
			String selectSql) {
		return commonEao .loadPageEntities(startIndex, pageRecords, selectSql);
	}

	//getter and setter
	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}

}
