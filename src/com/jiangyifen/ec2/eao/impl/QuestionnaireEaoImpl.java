package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.Query;

import com.jiangyifen.ec2.eao.QuestionnaireEao;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
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
public class QuestionnaireEaoImpl extends BaseEaoImpl implements
		QuestionnaireEao {
	@Override
	//根据问卷的标题获得改标题问卷的个数
	public long getMainTitleCount(String mainTitle,Long domainid){
		long count = 0;
		String sql  = "select count(s) from Questionnaire as s  where s.mainTitle = '"+mainTitle+"' and  s.domain.id = "+domainid;
		count = this.getEntityCount(sql);
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	//根据jpql获得问题选项的列表----》调整至问题EAO中
	public List<QuestionOptions> loadQuestionOptionsList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	//删除问题选项
	public void deleteQuestionOptionsByQuestionId(Long id) {
		String jpql  = "delete from QuestionOptions as s  where s.question.id = "+id;
		this.getEntityManager().createQuery(jpql).executeUpdate();
	}

	@Override
	//获得根据问卷编号获得问题中最大的序号
	public int getMaxQuestionOrderNumberByQuestionnaireId(Long id) {
		String jpql = "select max(s.ordernumber) from Question as s where s.questionnaire.id = :id"  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", id);
		
		Object result = query.getSingleResult();
		int resultCount = 0;
		if(null!=result){
			resultCount = (Integer) result;
		}
		return resultCount;
	}

	@Override
	//根据问卷编号获得问题列表
	@SuppressWarnings("unchecked")
	public List<Question> getQuestionByQuestionnaireId(Long id) {
		String jpql = "select s from Question as s where s.questionnaire.id = :id order by s.ordernumber"  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", id);
		List<Question> ls = query.getResultList();
		return ls;
	}

	@Override
	//根据问题编号获得该问题的选项列表
	@SuppressWarnings("unchecked")
	public List<QuestionOptions> getQuestionOptionsByQuestionId(Long id) {
		String jpql  = "select s from QuestionOptions as s  where s.question.id = :id order by s.ordernumber";
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", id);
		List<QuestionOptions> ls = query.getResultList();
		return ls;
	}

	@Override
	//查询问卷是设计状态的总数
	public int findDesignCount(Long id) {
		String jpql  = "select count(s) from Questionnaire as s  where s.stateCode = 'D' and s.id = "+id;
		return  this.getEntityCount(jpql);
	}

	/* (non-Javadoc)
	 * @see com.jiangyifen.ec2.eao.QuestionnaireEao#deleteQuestionAndRelation(java.lang.Long)
	 */
	@Override
	//删除问题相关的所有记录
	public void deleteQuestionAndRelation(Long id) {
		String del_jpql_uqo = "delete from CustomerQuestionOptions as s  where s.question.id = "+id;
		String del_jpql_qo = "delete from QuestionOptions as s  where s.question.id ="+id;
		String del_jpql_q = "delete from Question as s  where s.id ="+id;
		
		this.getEntityManager().createQuery(del_jpql_uqo).executeUpdate();
		this.getEntityManager().createQuery(del_jpql_qo).executeUpdate();
		this.getEntityManager().createQuery(del_jpql_q).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	//删除用户问卷记录
	public void deleteCustomerQuestionnaireByQuestionnaireId(Long id) {
		// 由于是多对多的关系，需要先从中间表中查出问卷调查对应的录音文件Id值集合
		String cqIdSql = "select id from ec2_customer_questionnaire where questionnaire_id = "+id;
		List<Long> cqIds = this.getEntityManager().createNativeQuery(cqIdSql).getResultList();
		for(Long cqId : cqIds) {
			// 再从 RecordFile 中查
			String linkTableSql = "select record_file_id from ec2_customer_questionnaire_record_file_link where customer_questionnaire_id = " + cqId;
			List<Long> recordFileIds = getEntityManager().createNativeQuery(linkTableSql).getResultList();
			for(Long recordFileId : recordFileIds) {
				// 最后删除
				String del_jpql_rf = "delete from RecordFile as rf where rf.id = "+recordFileId;
				this.getEntityManager().createQuery(del_jpql_rf).executeUpdate();
			}
		}
		
		// 删除客户问卷
		String del_jpql_uq = "delete from CustomerQuestionnaire as s  where s.questionnaire.id ="+id;
		this.getEntityManager().createQuery(del_jpql_uq).executeUpdate();
	}
	
	//根据domain域和问卷状态 获得该域相关的问卷列表
	@Override
	@SuppressWarnings("unchecked")
	public List<Questionnaire> getAllByDomainId(Long doaminId, String statecode) {
		String jpql = "select s from Questionnaire as s where s.domain.id = :id and s.stateCode = :stateCode  order by s.createTime desc "  ;
		Query query = this.getEntityManager().createQuery(jpql);
		query.setParameter("id", doaminId);
		query.setParameter("stateCode", statecode);
		List<Questionnaire> ls = query.getResultList();
		return ls;
	}

	//删除项目问卷中间表
	@Override
	public void deleteCustomerMarketingProjectQuestionnaireLink(Long questionnaireId) {
		String sql = "delete from ec2_marketing_project_questionnaire_link as s where s.questionnaire_id = "+questionnaireId;
		this.getEntityManager().createNativeQuery(sql);
	}

	//删除多有问题类型
	@Override
	public void deletAllQuestionType() {
		String sql = "delete from QuestionType";
		this.getEntityManager().createNativeQuery(sql);
	}
 
}
