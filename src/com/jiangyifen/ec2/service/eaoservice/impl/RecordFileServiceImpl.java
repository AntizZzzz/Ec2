package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.RecordFileEao;
import com.jiangyifen.ec2.entity.RecordFile;

import com.jiangyifen.ec2.service.eaoservice.RecordFileService;

/**
* Service实现类：录音文件管理
* 
* @author jrh
* 
*/
public class RecordFileServiceImpl implements RecordFileService {

	
	/** 需要注入的Eao */
	private RecordFileEao recordFileEao; 
	 
	/** jrh <br> 根据问卷调查对象的Id 值，获取其关联的所有录音文件集合  <br> customerQuestionnaireId 	 问卷调查 CustomerQuestionnaire的Id值*/
	@Override
	public List<RecordFile> getByCustomerQuestionnaireId(
			Long customerQuestionnaireId) {
		return recordFileEao.getByCustomerQuestionnaireId(customerQuestionnaireId);
	}

	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<RecordFile> loadPageEntities(int start, int length, String sql) {
		return recordFileEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return recordFileEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得录音文件
	@Override
	public RecordFile getRecordFileById(Long id){
		return recordFileEao.get(RecordFile.class, id);
	}
	
	// 保存录音文件
	@Override
	public void saveRecordFile(RecordFile recordFile){
		recordFileEao.save(recordFile);
	}
	
	// 更新录音文件
	@Override
	public RecordFile updateRecordFile(RecordFile recordFile){
		return (RecordFile) recordFileEao.update(recordFile);
	}
	
	// 删除录音文件
	@Override
	public void deleteRecordFile(RecordFile recordFile){
		recordFileEao.delete(recordFile);
	}
	
	
	//Eao注入
	public RecordFileEao getRecordFileEao() {
		return recordFileEao;
	}
	
	//Eao注入
	public void setRecordFileEao(RecordFileEao recordFileEao) {
		this.recordFileEao = recordFileEao;
	}
}