package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.RecordFileEao;
import com.jiangyifen.ec2.entity.RecordFile;

/**
* Eao实现类：录音文件管理
* 
* 注入：BaseEaoImpl
* 
* @author jrh
*
*/
public class RecordFileEaoImpl extends BaseEaoImpl implements RecordFileEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得录音文件列表
	public List<RecordFile> loadRecordFileList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
	
	/** jrh <br> 根据问卷调查对象的Id 值，获取其关联的所有录音文件集合  <br> customerQuestionnaireId 	 问卷调查 CustomerQuestionnaire的Id值*/
	@SuppressWarnings("unchecked")
	@Override
	public List<RecordFile> getByCustomerQuestionnaireId(Long customerQuestionnaireId) {
		// 由于是多对多的关系，需要先从中间表中查出问卷调查对应的录音文件Id值集合
		String linkTableSql = "select record_file_id from ec2_customer_questionnaire_record_file_link where customer_questionnaire_id = " + customerQuestionnaireId;
		List<Long> recordFileIds = getEntityManager().createNativeQuery(linkTableSql).getResultList();
		
		// 再从 RecordFile 中查
		List<RecordFile> recordFiles = new ArrayList<RecordFile>();
		for(Long recordFileId : recordFileIds) {
			RecordFile recordFile = get(RecordFile.class, recordFileId);
			if(recordFile != null) {
				recordFiles.add(recordFile);
			}
		}
		return recordFiles;
	}
	
}