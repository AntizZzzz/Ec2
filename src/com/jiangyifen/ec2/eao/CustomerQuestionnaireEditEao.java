package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.query.CustomerQuestionnaireQuery;

/**
* Eao接口：客户问卷编辑
* 
* @author lxy
*
*/
public interface CustomerQuestionnaireEditEao extends BaseEao {
	
	
	/**
	 * 获得客户问卷编辑列表
	 * @param 	jpql jpql语句
	 * @return 	客户问卷编辑列表
	 */
	public List<CustomerQuestionnaireQuery> loadCustomerQuestionnaireEditList(String jpql);

	public List<CustomerQuestionnaireQuery> loadPageEntitiesSQL(int start,
			int length, String sql);

	public int getEntityCountSQL(String sql);

	public List<Object[]> loadPageEntitiesSQLArr(int start, int length,
			String sql);

	public List<CustomerQuestionOptions> findResult(Long cqrid, Long qoptionid);
	
	public List<Long> getQuestionnaireRecordFileLink(Long customerQuestionnaireId);

	public List<RecordFile> getRecordFileList(String jpql);
}