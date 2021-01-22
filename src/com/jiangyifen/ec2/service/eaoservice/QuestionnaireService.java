package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.QuestionType;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.service.common.FlipSupportService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionAndOptions;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionnaireReport;

public interface QuestionnaireService extends FlipSupportService<Questionnaire>  {

	/**
	 * 保存问卷
	 * @param questionnaire
	 */
	@Transactional
	public void saveQuestionnaire(Questionnaire  questionnaire);
	
	/**
	 * 根据主键获得问卷
	 * @param pk
	 * @return
	 */
	@Transactional
	public  Questionnaire getQuestionnaireById(Object pk);
	
	/**
	 * 获得问卷列表
	 * @param sql
	 * @return
	 */
	@Transactional
	public List<Questionnaire> getQuestionnaireList(String sql);
	
	/**
	 * 删除问卷
	 * @param pk
	 */
	@Transactional
	public  void delQuestionnaireById(Long id);
	
	/**
	 * 更新问卷
	 * @param questionnaire
	 */
	@Transactional
	public void updateQuestionnaire(Questionnaire questionnaire);
	
	/**
	 * 校验问卷标题
	 * @param mainTitle
	 * @return
	 */
	@Transactional
	public boolean validateMainTitle(String mainTitle,Long domainid);
	
	/**
	 * 获得问题类型
	 * @param pk
	 * @return
	 */
	@Transactional
	public QuestionType getQuestionTypeById(Object pk);
	
	/**
	 * 获得问卷选项
	 * @param typeid
	 * @return
	 */
	@Transactional
	public QuestionOptions getQuestionOptionsById(Object typeid);
	
	/**
	 * 保存问题和选项
	 * @param qao
	 * @return
	 */
	@Transactional
	public boolean saveQuestionAndOptions(QuestionAndOptions qao);
	
	/**
	 * 获得问题排序号
	 * @param id
	 * @return
	 */
	@Transactional
	public int getQuestionCountByQuestionnaireId(Long id);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public Question getUpQuestionQuestionnaireId(Long id);

	/**
	 * 设置问卷执行
	 * @param pk
	 */
	@Transactional
	public void stateCodeQuestionnaireById(Object pk);
	
	/**
	 * 获得问题选项列表
	 * @param questionId
	 * @return
	 */
	@Transactional
	public List<QuestionOptions> getQuestionOptionsListByQuestionId(Long questionId);
	
	/**
	 * 更新问题和问题选项
	 * @param qao
	 * @return
	 */
	@Transactional
	public boolean updateQuestionAndOptions(QuestionAndOptions qao);
	
	/**
	 * 删除问题和问题选项
	 * @param id
	 */
	@Transactional
	public void deleteQuestionAndOptions(Long id);
	
	/**
	 * 获得问题
	 * @param id
	 * @return
	 */
	@Transactional
	public  Question getQuestionById(Long id);

	/**
	 * 保存用户与问卷关系
	 * @param userQuestionOptions
	 */
	@Transactional
	public void saveCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions);
	
	/**
	 * 保存用户与用户选项
	 * @param uqos
	 */
	@Transactional
	public void saveCustomerQuestionOptions(List<CustomerQuestionOptions> uqos);
	
	/**
	 * 获得问卷报表
	 * @param id
	 * @return
	 */
	@Transactional
	public QuestionnaireReport findQuestionnaireReport(Long id);
	/**
	 * 判断问卷是否在设计状态
	 * @param id
	 * @return
	 */
	@Transactional
	public boolean isDesign(Long id);
	/**
	 * 关闭问卷
	 * @param id
	 */
	@Transactional
	public void closeQuestionnaireById(Long id);

	/**
	 * 初始化问题类型
	 */
	@Transactional
	public void initQuestionTypes();
	
	/**
	 * 根据domain域和问卷状态
	 * 获得该域相关的问卷列表，
	 * @param doaminId	问卷的状态		
	 * @param statecode	问卷的状态,D.设计，E 执行，C 删除
	 * @return 问卷列表 List<Questionnaire>
	 */
	@Transactional
	public List<Questionnaire> getAllByDomainId(Long doaminId,String statecode);

}
