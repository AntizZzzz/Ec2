package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * @Description 描述：
 *
 * @author  JRH
 * @date    2014年5月26日 下午1:15:40
 * @version v1.0.0
 */
public interface OrderdetailsService extends FlipSupportService<Orderdetails> {

	// common method 
	
	@Transactional
	public Orderdetails get(Object primaryKey);
	
	@Transactional
	public void save(Orderdetails orderdetails);

	@Transactional
	public Orderdetails update(Orderdetails orderdetails);

	@Transactional
	public void delete(Orderdetails orderdetails);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	// enhanced method
	
	
}
