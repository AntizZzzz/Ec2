package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;

//TODO lxy	1305
/**
* 问卷调查数据序列化层
* 
* 
* 注入： 
* 
* @author lxy
*
*/
public interface QuestionEao extends BaseEao {
	/**
	 * 根据问卷编号获得该问卷第一条问题
	 * @param id
	 * @return
	 */
	Question getFirstQuestionByQuestionnaireId(Long id);
	
	/**
	 * 根据问卷编号获得该问卷问题列表
	 * @param id
	 * @return
	 */
	List<Question> getQuestionListByQuestionnaireId(Long id);

	/**
	 * 根据问题编号获得该问题选项
	 * @param id
	 * @return
	 */
	List<QuestionOptions> getQuestionOptionsListByQuestionId(Long id);

	/**
	 * 获得下一条问题
	 * @param questionnaireId
	 * @param ordernumber
	 * @return
	 */
	Question getNextQuestionByOrdernumber(Long questionnaireId,int ordernumber);

	/**
	 * 根据问题编号查看该编号是否存在跳转关联总数
	 * @param id
	 * @return
	 */
	int findQuestionCountSkipBuId(Long id);

	/**
	 * 删除客户问题选项通过客户问卷关系编号
	 * @param id
	 */
	void deleteCustomerQuestionOptionsByCustomerQuestionnaireId(Long id);

}