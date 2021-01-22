package com.jiangyifen.ec2.eao;

import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Timers;


public interface TimersEao extends BaseEao {

	/**
	 * 获取定时其中，响应时间大于当前时间的所有定时器中 “响应最小的 定时器集合”
	 * 即响应时间大于当前时间，但又离当前时间最近的所有对象
	 * @return List
	 */
	public List<Timers> getTiemrsToCurrentTime();
	
	/**
	 * 获取定时器中,响应时间大于当前时间,但又是响应时间大于当前时间中的所有定时器中的最小时间值
	 *  即响应时间大于当前时间， 但又离当前时间最近的响应时间
	 * @return Date
	 */
	public Date getNearestTimeToCurrentTime();

}
