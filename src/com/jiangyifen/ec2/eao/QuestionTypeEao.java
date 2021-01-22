package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.QuestionType;

/**
* Eao接口：问题类型
* 
* @author lxy
*
*/
public interface QuestionTypeEao extends BaseEao {
	
	
	/**
	 * 获得问题类型列表
	 * @param 	jpql jpql语句
	 * @return 	问题类型列表
	 */
	public List<QuestionType> loadQuestionTypeList(String jpql);
	
}