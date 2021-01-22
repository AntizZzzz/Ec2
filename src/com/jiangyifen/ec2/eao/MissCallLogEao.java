package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.MissCallLog;

/**
* Eao接口：电话漏接日志
* 
* @author jrh
*
*/
public interface MissCallLogEao extends BaseEao {
	
	
	/**
	 * 获得电话漏接日志列表
	 * @param 	jpql jpql语句
	 * @return 	电话漏接日志列表
	 */
	public List<MissCallLog> loadMissCallLogList(String jpql);
	
}