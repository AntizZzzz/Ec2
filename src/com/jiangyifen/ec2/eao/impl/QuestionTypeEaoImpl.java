package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.QuestionTypeEao;
import com.jiangyifen.ec2.entity.QuestionType;

/**
* Eao实现类：问题类型
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class QuestionTypeEaoImpl extends BaseEaoImpl implements QuestionTypeEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得问题类型列表
	public List<QuestionType> loadQuestionTypeList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}