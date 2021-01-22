package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.QuestionTypeEao;
import com.jiangyifen.ec2.entity.QuestionType;

import com.jiangyifen.ec2.service.eaoservice.QuestionTypeService;
/**
* Service实现类：问题类型
* 
* @author lxy
* 
*/
public class QuestionTypeServiceImpl implements QuestionTypeService {

	
	/** 需要注入的Eao */
	private QuestionTypeEao questionTypeEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<QuestionType> loadPageEntities(int start, int length, String sql) {
		return questionTypeEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return questionTypeEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得问题类型
	@Override
	public QuestionType getQuestionTypeById(Long id){
		return questionTypeEao.get(QuestionType.class, id);
	}
	
	// 保存问题类型
	@Override
	public void saveQuestionType(QuestionType questionType){
		questionTypeEao.save(questionType);
	}
	
	// 更新问题类型
	@Override
	public void updateQuestionType(QuestionType questionType){
		questionTypeEao.update(questionType);
	}
	
	// 删除问题类型
	@Override
	public void deleteQuestionType(QuestionType questionType){
		questionTypeEao.delete(questionType);
	}
	
	//获得所有问题类型
	@Override
	public List<QuestionType> getAllQuestionTypeList() {
		String jpql = "select s from QuestionType as s  order by s.id asc ";
		List<QuestionType> list = questionTypeEao.loadQuestionTypeList(jpql);
		return list;
	}	
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from QuestionType as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from QuestionType as s where s. = "++" order by s.ordernumber asc ";
		List<QuestionType> list = questionTypeEao.loadQuestionTypeList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public QuestionTypeEao getQuestionTypeEao() {
		return questionTypeEao;
	}
	
	//Eao注入
	public void setQuestionTypeEao(QuestionTypeEao questionTypeEao) {
		this.questionTypeEao = questionTypeEao;
	}
}