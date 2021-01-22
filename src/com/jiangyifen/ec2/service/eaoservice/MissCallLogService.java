package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.MissCallLog;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：电话漏接日志
* 
* @author jrh
*
*/
public interface MissCallLogService extends FlipSupportService<MissCallLog>  {
	
	/**
	 * 根据主键ID获得电话漏接日志
	 * @param id	主键ID 
	 * @return		电话漏接日志，一条或null
	 */
	@Transactional
	public MissCallLog getMissCallLogById(Long id);
	
	/**
	 * 保存电话漏接日志
	 * @param missCallLog	电话漏接日志
	 * 
	 */
	@Transactional
	public void saveMissCallLog(MissCallLog missCallLog);
	
	/**
	 * 更新电话漏接日志
	 * @param missCallLog	电话漏接日志
	 * 
	 */
	@Transactional
	public MissCallLog updateMissCallLog(MissCallLog missCallLog);
	
	/**
	 * 删除电话漏接日志
	 * @param missCallLog	电话漏接日志
	 * 
	 */
	@Transactional
	public void deleteMissCallLog(MissCallLog missCallLog);
	
}