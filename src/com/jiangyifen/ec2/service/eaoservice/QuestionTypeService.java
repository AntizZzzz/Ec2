package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.QuestionType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：问题类型
* 
* @author lxy
*
*/
public interface QuestionTypeService extends FlipSupportService<QuestionType>  {
	
	/**
	 * 根据主键ID获得问题类型
	 * @param id	主键ID 
	 * @return		问题类型，一条或null
	 */
	@Transactional
	public QuestionType getQuestionTypeById(Long id);
	
	/**
	 * 保存问题类型
	 * @param questionType	问题类型
	 * 
	 */
	@Transactional
	public void saveQuestionType(QuestionType questionType);
	
	/**
	 * 更新问题类型
	 * @param questionType	问题类型
	 * 
	 */
	@Transactional
	public void updateQuestionType(QuestionType questionType);
	
	/**
	 * 删除问题类型
	 * @param questionType	问题类型
	 * 
	 */
	@Transactional
	public void deleteQuestionType(QuestionType questionType);

	/**
	 * 获得所有问题类型
	 * @return
	 */
	@Transactional
	public List<QuestionType> getAllQuestionTypeList();
	
}