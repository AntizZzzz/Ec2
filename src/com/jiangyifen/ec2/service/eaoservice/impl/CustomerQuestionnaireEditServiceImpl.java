package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.CustomerQuestionnaireEditEao;
import com.jiangyifen.ec2.eao.QuestionEao;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireEditService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.query.CustomerQuestionnaireQuery;
/**
* Service实现类：客户问卷编辑
* 
* @author lxy
* 
*/
public class CustomerQuestionnaireEditServiceImpl implements CustomerQuestionnaireEditService {

	
	/** 需要注入的Eao */
	private CustomerQuestionnaireEditEao customerQuestionnaireEditEao; 
	private QuestionEao questionEao;
	@Override
 	public List<Object[]> loadPageEntitiesArr(int start, int length, String sql,Long questionnaireId) {
		List<Object[]> li = customerQuestionnaireEditEao.loadPageEntitiesSQLArr(start, length, sql);
		List<Question> lqQuestions = new ArrayList<Question>();
		lqQuestions = questionEao.getQuestionListByQuestionnaireId(questionnaireId);
		List<Object[]> lirt = new ArrayList<Object[]>();
		int lg = 12+lqQuestions.size();									//TODO 注意12的大小和前台对应
		String finish = "";
		Object[] ol_ar = null;
		Object[] jj = null;
		for (int i = 0; i < li.size(); i++) {
			ol_ar = li.get(i);
			jj = new Object[lg];
			for (int k = 0; k < ol_ar.length; k++) {
				if(k==11){
					finish = ol_ar[k].toString();
					if("start".equals(finish)){
						finish = "开始";
					}else if("end".equals(finish)){
						finish = "完成";
					}
					jj[k] = finish;
				}else{
					jj[k] = ol_ar[k];
				}
				
			}
			for (int j = 0; j < lqQuestions.size(); j++) {
				Question q = lqQuestions.get(j);
				jj[j + 12] = (Object) findCustomerResult(q.getId(),(Long) ol_ar[0]);
			}
			lirt.add(jj);
		}
		return lirt;
	}
	private String findCustomerResult(Long cqrid,Long qoptionid){
		StringBuffer stringBuffer = new StringBuffer("");//问题编号 客户问卷编号
		List<CustomerQuestionOptions> ls = customerQuestionnaireEditEao.findResult(cqrid,qoptionid);
		CustomerQuestionOptions cqo = null;
		QuestionOptions qo = null;
		for (int i = 0; i < ls.size(); i++) {
			cqo = ls.get(i);
			if(cqo != null){
				//if("TT".equals(cqo.getQuestionOptions().getType())){
				qo = cqo.getQuestionOptions();
				if(i!=0){
					stringBuffer.append(",");
				}
				stringBuffer.append(qo.getName());
				stringBuffer.append((null!=cqo.getText()?cqo.getText():""));	
			}
		}
		return stringBuffer.toString();
	}
	// 根据jpql语句和分页参数实现分页查询
	@Override
 	public List<CustomerQuestionnaireQuery> loadPageEntities(int start, int length, String sql) {
		return customerQuestionnaireEditEao.loadPageEntitiesSQL(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return customerQuestionnaireEditEao.getEntityCountSQL(sql);
	}
		
	// 根据主键ID获得客户问卷编辑
	@Override
	public CustomerQuestionnaireQuery getCustomerQuestionnaireEditById(Long id){
		return customerQuestionnaireEditEao.get(CustomerQuestionnaireQuery.class, id);
	}
	
	// 保存客户问卷编辑
	@Override
	public void saveCustomerQuestionnaireEdit(CustomerQuestionnaireQuery customerQuestionnaireQuery){
		customerQuestionnaireEditEao.save(customerQuestionnaireQuery);
	}
	
	// 更新客户问卷编辑
	@Override
	public CustomerQuestionnaireQuery updateCustomerQuestionnaireEdit(CustomerQuestionnaireQuery customerQuestionnaireQuery){
		return (CustomerQuestionnaireQuery)customerQuestionnaireEditEao.update(customerQuestionnaireQuery);
	}
	
	// 删除客户问卷编辑
	@Override
	public void deleteCustomerQuestionnaireEdit(CustomerQuestionnaireQuery customerQuestionnaireQuery){
		customerQuestionnaireEditEao.delete(customerQuestionnaireQuery);
	}
	
	@Override
	public List<RecordFile> getRecordFileList(Long customerQuestionnaireId,Domain domain) {
		List<RecordFile> rflsrt = new ArrayList<RecordFile>();
		List<Long> ls = customerQuestionnaireEditEao.getQuestionnaireRecordFileLink(customerQuestionnaireId);
		 
		int longIdLength = ls.size();
		if(ls.size() > 0){
			StringBuffer idsBuffer = new StringBuffer("");
			for (int i = 0; i < longIdLength; i++) {
				if(i==0){
					idsBuffer.append(ls.get(i));
				}else{
					idsBuffer.append(","+ls.get(i));
				}
			}
			//查询
			if(idsBuffer.length()>0){
				String jpql = "select s from RecordFile as s where s.domain.id = "+ domain.getId() +" and s.id in (";
				jpql += idsBuffer.toString();
				jpql += ")";
				rflsrt =  customerQuestionnaireEditEao.getRecordFileList(jpql);
			}
		}
		return rflsrt;
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from CustomerQuestionnaireEdit as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from CustomerQuestionnaireEdit as s where s. = "++" order by s.ordernumber asc ";
		List<CustomerQuestionnaireEdit> list = customerQuestionnaireEditEao.loadCustomerQuestionnaireEditList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public CustomerQuestionnaireEditEao getCustomerQuestionnaireEditEao() {
		return customerQuestionnaireEditEao;
	}
	
	//Eao注入
	public void setCustomerQuestionnaireEditEao(CustomerQuestionnaireEditEao customerQuestionnaireEditEao) {
		this.customerQuestionnaireEditEao = customerQuestionnaireEditEao;
	}
	public QuestionEao getQuestionEao() {
		return questionEao;
	}
	public void setQuestionEao(QuestionEao questionEao) {
		this.questionEao = questionEao;
	}
	
	
	
}