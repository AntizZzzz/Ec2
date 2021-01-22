package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.MeettingDetailRecord;

/**
* Eao接口：多方通话的记录详情
* 
* @author jrh
*
*/
public interface MeettingDetailRecordEao extends BaseEao {
	
	
	/**
	 * 获得多方通话的记录详情列表
	 * @param 	jpql jpql语句
	 * @return 	多方通话的记录详情列表
	 */
	public List<MeettingDetailRecord> loadMeettingDetailRecordList(String jpql);
	
}