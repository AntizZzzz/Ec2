package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MissCallLogEao;
import com.jiangyifen.ec2.entity.MissCallLog;
import com.jiangyifen.ec2.service.eaoservice.MissCallLogService;

/**
* Service实现类：电话漏接日志
* 
* @author jrh
* 
*/
public class MissCallLogServiceImpl implements MissCallLogService {

	
	/** 需要注入的Eao */
	private MissCallLogEao missCallLogEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<MissCallLog> loadPageEntities(int start, int length, String sql) {
		return missCallLogEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return missCallLogEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得电话漏接日志
	@Override
	public MissCallLog getMissCallLogById(Long id){
		return missCallLogEao.get(MissCallLog.class, id);
	}
	
	// 保存电话漏接日志
	@Override
	public void saveMissCallLog(MissCallLog missCallLog){
		missCallLogEao.save(missCallLog);
	}
	
	// 更新电话漏接日志
	@Override
	public MissCallLog updateMissCallLog(MissCallLog missCallLog){
		return (MissCallLog)missCallLogEao.update(missCallLog);
	}
	
	// 删除电话漏接日志
	@Override
	public void deleteMissCallLog(MissCallLog missCallLog){
		missCallLogEao.delete(missCallLog);
	}
	
	//Eao注入
	public MissCallLogEao getMissCallLogEao() {
		return missCallLogEao;
	}
	
	//Eao注入
	public void setMissCallLogEao(MissCallLogEao missCallLogEao) {
		this.missCallLogEao = missCallLogEao;
	}
}