package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MeettingDetailRecordEao;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;

import com.jiangyifen.ec2.service.eaoservice.MeettingDetailRecordService;
/**
* Service实现类：多方通话的记录详情
* 
* @author jrh
* 
*/
public class MeettingDetailRecordServiceImpl implements MeettingDetailRecordService {

	
	/** 需要注入的Eao */
	private MeettingDetailRecordEao meettingDetailRecordEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<MeettingDetailRecord> loadPageEntities(int start, int length, String sql) {
		return meettingDetailRecordEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return meettingDetailRecordEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得多方通话的记录详情
	@Override
	public MeettingDetailRecord getMeettingDetailRecordById(Long id){
		return meettingDetailRecordEao.get(MeettingDetailRecord.class, id);
	}
	
	// 保存多方通话的记录详情
	@Override
	public void saveMeettingDetailRecord(MeettingDetailRecord meettingDetailRecord){
		meettingDetailRecordEao.save(meettingDetailRecord);
	}
	
	// 更新多方通话的记录详情
	@Override
	public MeettingDetailRecord updateMeettingDetailRecord(MeettingDetailRecord meettingDetailRecord){
		return (MeettingDetailRecord)meettingDetailRecordEao.update(meettingDetailRecord);
	}
	
	// 删除多方通话的记录详情
	@Override
	public void deleteMeettingDetailRecord(MeettingDetailRecord meettingDetailRecord){
		meettingDetailRecordEao.delete(meettingDetailRecord);
	}
	
	//Eao注入
	public MeettingDetailRecordEao getMeettingDetailRecordEao() {
		return meettingDetailRecordEao;
	}
	
	//Eao注入
	public void setMeettingDetailRecordEao(MeettingDetailRecordEao meettingDetailRecordEao) {
		this.meettingDetailRecordEao = meettingDetailRecordEao;
	}
}