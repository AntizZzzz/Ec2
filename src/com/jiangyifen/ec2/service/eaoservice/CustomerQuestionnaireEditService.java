package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.service.common.FlipSupportService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.query.CustomerQuestionnaireQuery;

/**
* Service接口：客户问卷编辑
* 
* @author lxy
*
*/
public interface CustomerQuestionnaireEditService extends FlipSupportService<CustomerQuestionnaireQuery>  {
	
	@Transactional
	public List<Object[]> loadPageEntitiesArr(int start, int length, String sql,Long questionnaireId);
	/**
	 * 根据主键ID获得客户问卷编辑
	 * @param id	主键ID 
	 * @return		客户问卷编辑，一条或null
	 */
	@Transactional
	public CustomerQuestionnaireQuery getCustomerQuestionnaireEditById(Long id);
	
	/**
	 * 保存客户问卷编辑
	 * @param customerQuestionnaireQuery	客户问卷编辑
	 * 
	 */
	@Transactional
	public void saveCustomerQuestionnaireEdit(CustomerQuestionnaireQuery customerQuestionnaireQuery);
	
	/**
	 * 更新客户问卷编辑
	 * @param customerQuestionnaireQuery	客户问卷编辑
	 * 
	 */
	@Transactional
	public CustomerQuestionnaireQuery updateCustomerQuestionnaireEdit(CustomerQuestionnaireQuery customerQuestionnaireQuery);
	
	/**
	 * 删除客户问卷编辑
	 * @param customerQuestionnaireQuery	客户问卷编辑
	 * 
	 */
	@Transactional
	public void deleteCustomerQuestionnaireEdit(CustomerQuestionnaireQuery customerQuestionnaireQuery);
	
	
	@Transactional
	public List<RecordFile> getRecordFileList(Long customerQuestionnaireId, Domain domain);
	
}