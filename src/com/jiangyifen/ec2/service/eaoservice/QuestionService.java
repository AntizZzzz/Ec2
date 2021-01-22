package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;

public interface QuestionService extends FlipSupportService<Question>  {

	/**
	 * 获得该问卷的第一个问题
	 * @param id
	 * @return
	 */
	@Transactional
	public Question getFirstQuestionByQuestionnaireId(Long id);

	/**
	 * 根据该问卷的问题列表
	 * @param id
	 * @return
	 */
	@Transactional
	public List<Question> getQuestionListByQuestionnaireId(Long id);
	
	/**
	 * 根据问题获得该问题选项列表
	 * @param id
	 * @return
	 */
	@Transactional
	public List<QuestionOptions> getQuestionOptionsListByQuestionId(Long id);
	
	/**
	 * 根据问题获得下一条
	 * @param question
	 * @return
	 */
	@Transactional
	public Question getNextQuestionByOrdernumber(Question question);

	/**
	 * 保存用户问卷
	 * @param customerQuestionnaire
	 */
	@Transactional
	public void saveCustomerQuestionnaire(CustomerQuestionnaire customerQuestionnaire);
	
	/**
	 * 更新用户问卷
	 * @param customerQuestionnaire
	 */
	@Transactional
	public void updateCustomerQuestionnaire(CustomerQuestionnaire customerQuestionnaire);
	
	/**
	 * 根据问题编号查看该编号是否存在跳转关联
	 * @param id
	 */
	@Transactional
	public boolean findQuestionSkipBuId(Long id);

	/**
	 * 根据问卷和问题排序号获得该文当前序号索引值
	 * @param questionnaire
	 * @param ordernumber
	 * @return
	 */
	@Transactional
	public int getIndexByQuestionnaireAndQuestionOrder(
			Long questionnaireid, int ordernumber);

	/**
	 * 保存客户问卷关系和录音文件的关系
	 * @param customerQuestionnaire
	 * @param recordFile
	 */
	@Transactional
	public void saveCustomerQuestionnaireAndChannel(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile);
	
	/**
	 * 更新客户问卷关系和录音文件的关系
	 * @param customerQuestionnaire
	 * @param recordFile
	 */
	@Transactional
	public void updateCustomerQuestionnaireAndChannel(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile);
	
	/**
	 * 删除该客户问卷相关的数据
	 * @param questionnaire
	 * @param customerid
	 * @param loginUser
	 */
	@Transactional
	public Set<RecordFile> deleteCustomerQuestionnaireAndChannel(
			Questionnaire questionnaire, Long customerid, User loginUser);
	
	/**
	 * 根据问卷客户编号获得客户问卷编号
	 * @param questionnaire 问卷编号
	 * @param customerid	客户编号
	 * @param loginUser
	 * @return
	 */
	@Transactional
	public CustomerQuestionnaire getCustomerQuestionnaireByCustomeridQuestionnaireUserid(
			Questionnaire questionnaire, Long customerid,  User loginUser);

	/**
	 * 根据客户问卷关系编号获得正确应该显示的问题
	 * @param customerQuestionnaire
	 * @return
	 */
	@Transactional
	public Question getRightQuestion(CustomerQuestionnaire customerQuestionnaire);

	@Transactional
	public void saveCustomerQuestionnaireAndChannelShowAll(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile,
			List<QuestionOptionsListView> listViews);


	@Transactional
	public CustomerQuestionOptions findOptionIsSelect(long customerQuestionnaireId,Long questionId,Long optionId);
	
	/**
	 * 更新用户问卷
	 * @param customerQuestionnaire
	 */
	@Transactional
	public void updateCustomerQuestionnaireOnly(CustomerQuestionnaire customerQuestionnaire);

	/**
	 * 更新客户问卷 时间和客户问卷
	 * @param customerQuestionnaire
	 * @param recordFile
	 * @param listViews
	 */
	@Transactional
	public void saveCustomerQuestionnaireAndChannelShowAllClearOptions(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile,
			List<QuestionOptionsListView> listViews);
	/**
	 * 更新客户问卷  只用于更新完成状态
	 * @param customerQuestionnaire
	 */
	@Transactional
	public void updateCustomerQuestionnaireOnlyFinish(CustomerQuestionnaire customerQuestionnaire);
}
