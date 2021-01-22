package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.RecordFile;

/**
* Eao接口：录音文件管理
* 
* @author jrh
*
*/
public interface RecordFileEao extends BaseEao {
	
	
	/**
	 * 获得录音文件列表
	 * @param 	jpql jpql语句
	 * @return 	录音文件列表
	 */
	public List<RecordFile> loadRecordFileList(String jpql);

	/**
	 * jrh
	 * 根据问卷调查对象的Id 值，获取其关联的所有录音文件集合
	 * @param customerQuestionnaireId 	 问卷调查 CustomerQuestionnaire的Id值
	 * @return List<RecordFile>			 录音文件集合
	 */
	public List<RecordFile> getByCustomerQuestionnaireId(Long customerQuestionnaireId);
	
}