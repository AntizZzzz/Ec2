package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.Query;

import com.jiangyifen.ec2.eao.QuestionEao;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
//TODO lxy	1305
/**
* 问卷调查服务层
* 
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class QuestionEaoImpl extends BaseEaoImpl implements QuestionEao {

	//根据问卷编号获得该问卷第一条问题
	@Override
	public Question getFirstQuestionByQuestionnaireId(Long id) {
		String jpql = "select s from Question as s where s.questionnaire.id = :id order by s.ordernumber "  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", id);
		@SuppressWarnings("unchecked")
		List<Question> ls = query.getResultList();
		if(null != ls && ls.size()>0){
			return ls.get(0);
		}else{
			return null;
		}
	}
	
	//根据问卷编号获得该问卷问题列表
	@Override
	public List<Question> getQuestionListByQuestionnaireId(Long id) {
		String jpql = "select s from Question as s where s.questionnaire.id = :id order by s.ordernumber"  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", id);
		@SuppressWarnings("unchecked")
		List<Question> ls = query.getResultList();
		return ls;
	}
	//根据问题编号获得该问题选项
	@Override
	public List<QuestionOptions> getQuestionOptionsListByQuestionId(Long id) {
		String jpql = "select s from QuestionOptions as s where s.question.id = :id order by s.ordernumber"  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", id);
		@SuppressWarnings("unchecked")
		List<QuestionOptions> ls = query.getResultList();
		return ls;
	}
	//获得下一条问题
	@Override
	public Question getNextQuestionByOrdernumber(Long questionnaireId,int ordernumber) {
		String jpql = "select s from Question as s where s.questionnaire.id = :questionnaireId and s.ordernumber > :ordernumber order by s.ordernumber"  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("questionnaireId", questionnaireId);
		query.setParameter("ordernumber", ordernumber);
		@SuppressWarnings("unchecked")
		List<Question> ls = query.getResultList();
		if(null != ls && ls.size()>0){
			return ls.get(0);
		}else{
			return null;
		}
	}

	//根据问题编号查看该编号是否存在跳转关联总数
	@Override
	public int findQuestionCountSkipBuId(Long id) {
		int count = 0;
		String sql  = "select count(s) from QuestionOptions as s  where s.skip.id = "+id;
		count = this.getEntityCount(sql);
		return count;
	}

	@Override
	public void deleteCustomerQuestionOptionsByCustomerQuestionnaireId(Long id) {
		String del_jpql = "delete from CustomerQuestionOptions as s  where s.customerQuestionnaire.id ="+id;
		this.getEntityManager().createQuery(del_jpql).executeUpdate();
	}

}
