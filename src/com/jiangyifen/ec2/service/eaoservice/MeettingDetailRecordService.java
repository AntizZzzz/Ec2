package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：多方通话的记录详情
* 
* @author jrh
*
*/
public interface MeettingDetailRecordService extends FlipSupportService<MeettingDetailRecord>  {
	
	/**
	 * 根据主键ID获得多方通话的记录详情
	 * @param id	主键ID 
	 * @return		多方通话的记录详情，一条或null
	 */
	@Transactional
	public MeettingDetailRecord getMeettingDetailRecordById(Long id);
	
	/**
	 * 保存多方通话的记录详情
	 * @param meettingDetailRecord	多方通话的记录详情
	 * 
	 */
	@Transactional
	public void saveMeettingDetailRecord(MeettingDetailRecord meettingDetailRecord);
	
	/**
	 * 更新多方通话的记录详情
	 * @param meettingDetailRecord	多方通话的记录详情
	 * 
	 */
	@Transactional
	public MeettingDetailRecord updateMeettingDetailRecord(MeettingDetailRecord meettingDetailRecord);
	
	/**
	 * 删除多方通话的记录详情
	 * @param meettingDetailRecord	多方通话的记录详情
	 * 
	 */
	@Transactional
	public void deleteMeettingDetailRecord(MeettingDetailRecord meettingDetailRecord);
	
}