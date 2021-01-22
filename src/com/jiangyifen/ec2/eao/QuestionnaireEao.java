package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;

//TODO lxy	1305
/**
* 问卷调查数据序列化层
* 
* 
* 注入：QuestionnaireEao questionnaireEao
* 
* @author lxy
*
*/
public interface QuestionnaireEao extends BaseEao {

	/**
	 * 根据domain域和问卷状态
	 * 获得该域相关的问卷列表，
	 * @param doaminId	问卷的状态		
	 * @param statecode	问卷的状态,D.设计，E 执行，C 删除
	 * @return 问卷列表 List<Questionnaire>
	 */
	public List<Questionnaire> getAllByDomainId(Long doaminId,String statecode);
	
	/**
	 * 根据问卷的标题获得改标题问卷的个数
	 * @param mainTitle
	 * @return
	 */
	public long getMainTitleCount(String mainTitle,Long domainid);
	/**
	 * 根据jpql获得问题选项的列表----》调整至问题EAO中
	 * @param jpql
	 * @return
	 */
	public List<QuestionOptions> loadQuestionOptionsList(String jpql);

	/**
	 * 删除问题选项
	 * @param id
	 */
	public void deleteQuestionOptionsByQuestionId(Long id);
	/**
	 * 获得根据问卷编号获得问题中最大的序号
	 * @param id
	 * @return
	 */
	public int getMaxQuestionOrderNumberByQuestionnaireId(Long id) ;
	/**
	 * 根据问卷编号获得问题列表
	 * @param id
	 * @return
	 */
	public List<Question> getQuestionByQuestionnaireId(Long id);

	/**
	 *	根据问题编号获得该问题的选项列表
	 * @param id
	 * @return
	 */
	public List<QuestionOptions> getQuestionOptionsByQuestionId(Long id);
	/**
	 * 查询问卷是设计状态的总数
	 * @param id
	 * @return
	 */
	public int findDesignCount(Long id);
	/**
	 * 删除问题相关的所有记录
	 * @param id
	 */
	public void deleteQuestionAndRelation(Long id);
	/**
	 * 删除用户问卷记录
	 * @param id
	 */
	public void deleteCustomerQuestionnaireByQuestionnaireId(Long id);
	/**
	 * 删除项目问卷中间表
	 * @param id
	 */
	public void deleteCustomerMarketingProjectQuestionnaireLink(Long id);

	/**
	 * 删除多有问题类型
	 */
	public void deletAllQuestionType();
	
}