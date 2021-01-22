package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：录音文件管理
* 
* @author jrh
*
*/
public interface RecordFileService extends FlipSupportService<RecordFile>  {
	
	/**
	 * 根据主键ID获得录音文件
	 * @param id	主键ID 
	 * @return		录音文件，一条或null
	 */
	@Transactional
	public RecordFile getRecordFileById(Long id);
	
	/**
	 * 保存新录音文件
	 * @param recordFile	录音文件
	 * 
	 */
	@Transactional
	public void saveRecordFile(RecordFile recordFile);
	
	/**
	 * 更新录音文件
	 * @param recordFile	录音文件
	 * 
	 */
	@Transactional
	public RecordFile updateRecordFile(RecordFile recordFile);
	
	/**
	 * 删除录音文件
	 * @param recordFile	录音文件
	 * 
	 */
	@Transactional
	public void deleteRecordFile(RecordFile recordFile);
	
	
	// enhanced methods
	
	/**
	 * jrh
	 * 根据问卷调查对象的Id 值，获取其关联的所有录音文件集合
	 * @param customerQuestionnaireId 	 问卷调查 CustomerQuestionnaire的Id值
	 * @return List<RecordFile>			 录音文件集合
	 */
	@Transactional
	public List<RecordFile> getByCustomerQuestionnaireId(Long customerQuestionnaireId);
	
}