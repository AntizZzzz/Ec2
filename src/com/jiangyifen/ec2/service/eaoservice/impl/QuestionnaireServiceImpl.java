package com.jiangyifen.ec2.service.eaoservice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.QuestionEao;
import com.jiangyifen.ec2.eao.QuestionnaireEao;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.QuestionType;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionAndOptions;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionOptionsReport;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionReport;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionnaireReport;

// TODO lxy	1305
/**
 * 问卷调查服务层
 * 
 * 
 * 注入：QuestionnaireEao questionnaireEao
 * 
 * @author lxy
 *
 */
public class QuestionnaireServiceImpl implements QuestionnaireService {

	//==================================need  end==================================
	private QuestionnaireEao questionnaireEao; 
	private QuestionEao questionEao;
	//==================================need  end==================================
	
	//==================================business start==================================
	@SuppressWarnings("unchecked")
	@Override
	public List<Questionnaire> loadPageEntities(int start, int length, String sql) {
		return questionnaireEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return questionnaireEao.getEntityCount(sql);
	}

	//保存问卷
	@Override
	public void saveQuestionnaire(Questionnaire questionnaire) {
		questionnaireEao.save(questionnaire);
	}
	//根据主键获得问卷
	@Override
	public Questionnaire getQuestionnaireById(Object pk) {
		return questionnaireEao.get(Questionnaire.class, pk);
	}

	//获得问卷列表
	@Override
	public List<Questionnaire> getQuestionnaireList(String sql) {
 		return null;
	}
	
	//删除问卷
	@Override
	public void delQuestionnaireById(Long id) {
		//questionnaireEao.delete(Questionnaire.class, pk);
		//删除问卷关联关系太多
		//1.查询问卷问题
		//2.便利问卷问题
			//删除用户问题选项
			//删除问题选项
			//删除问题
		//3.删除用户问卷
		//4.删除问卷
		Questionnaire questionnaire = getQuestionnaireById(id);
		List<Question> questionList = null;
		if (null != questionnaire){
			questionList = questionEao.getQuestionListByQuestionnaireId(questionnaire.getId());
			for (int i = 0; i < questionList.size(); i++) {
				Question question = questionList.get(i);
				if(null!=question){
					questionnaireEao.deleteQuestionAndRelation(question.getId());
				}
			}

			questionnaireEao.deleteCustomerQuestionnaireByQuestionnaireId(questionnaire.getId());
			// 还需要去除项目于问卷的关联
			questionnaireEao.deleteCustomerMarketingProjectQuestionnaireLink(questionnaire.getId());//ec2_marketing_project_questionnaire_link
			questionnaireEao.delete(questionnaire);
		}
		
	}

	//更新问卷
	@Override
	public void updateQuestionnaire(Questionnaire questionnaire) {
		questionnaireEao.update(questionnaire);
		
	}
	
	//校验问卷标题
	@Override
	public boolean validateMainTitle(String mainTitle,Long domainid) {
		long count = questionnaireEao.getMainTitleCount(mainTitle,domainid);
		if(count>0){
			return false;
		}else{
			return true;
		}
	}
	
	//获得问题类型
	@Override
	public QuestionType getQuestionTypeById(Object pk) {
		return questionnaireEao.get(QuestionType.class, pk);
	}
	
	//获得问卷选项
	@Override
	public QuestionOptions getQuestionOptionsById(Object pk) {
		return questionnaireEao.get(QuestionOptions.class, pk);
	}
	//保存问题和选项
	@Override
	public boolean saveQuestionAndOptions(QuestionAndOptions qao) {
		boolean optTF = false;
		Questionnaire questionnaire = qao.getQuestionnaire();
		int question_ordernumber = 0;
		question_ordernumber = questionnaireEao.getMaxQuestionOrderNumberByQuestionnaireId(questionnaire.getId());
		question_ordernumber = question_ordernumber + 1;
		Question question = qao.getQuestion();
		question.setOrdernumber(question_ordernumber);
		questionnaireEao.save(question);
		List<QuestionOptions> options = new ArrayList<QuestionOptions>();
		options = qao.getQuestionOptions();
		for (int i = 0; i < options.size(); i++) {
			QuestionOptions temp = options.get(i);
			temp.setQuestion(question);
			questionnaireEao.save(temp);
			optTF = true;
		}
		return optTF;
	}
	
	//获得问题排序号
	@Override
	public int getQuestionCountByQuestionnaireId(Long id) {
		String sql = " select count(s) from Question as s where s.questionnaire.id = "+id;
		return questionnaireEao.getEntityCount(sql);
	}
	//获得问题信息
	@Override
	@SuppressWarnings("unchecked")
	public Question getUpQuestionQuestionnaireId(Long id) {
		String sql = "select s from Question as s where s.questionnaire.id = "+id+" order by s.createTime desc ";
		List<Question> list = questionnaireEao.loadPageEntities(0, 1, sql);
		if((null != list)&&(list.size() == 1)){
			return list.get(0);
		}
		return null;
	}
	//设置问卷执行
	@Override
	public void stateCodeQuestionnaireById(Object pk) {
		 Questionnaire q = this.getQuestionnaireById(pk);
		 if(q!=null){
			 q.setStateCode("E");//执行
		 }
		this.updateQuestionnaire(q);
	}
	//获得问题选项列表
	@Override
	public List<QuestionOptions> getQuestionOptionsListByQuestionId(
			Long questionId) {
		String jpql = "select s from QuestionOptions as s where s.question.id = "+questionId+" order by s.ordernumber asc ";
		List<QuestionOptions> list = questionnaireEao.loadQuestionOptionsList(jpql);
		return list;
	}
	//更新问题和问题选项
	@Override
	public boolean updateQuestionAndOptions(QuestionAndOptions qao) {
		boolean optTF = false;
 		Question question = qao.getQuestion();
		questionnaireEao.update(question);
		questionnaireEao.deleteQuestionOptionsByQuestionId(question.getId());
		List<QuestionOptions> options = new ArrayList<QuestionOptions>();
		options = qao.getQuestionOptions();
		for (int i = 0; i < options.size(); i++) {
			QuestionOptions temp = options.get(i);
			temp.setQuestion(question);
			questionnaireEao.save(temp);
			optTF = true;
		}
		return optTF;
	}
	//删除问题和问题选项
	@Override
	public void deleteQuestionAndOptions(Long id) {
		Question q = questionnaireEao.get(Question.class, id);
		if(null!=q){
			questionnaireEao.deleteQuestionOptionsByQuestionId(id);
			questionnaireEao.delete(q);
		}
	}
	//获得问题
	@Override
	public Question getQuestionById(Long id) {
		return questionnaireEao.get(Question.class, id);
	}
	//保存用户与问卷关系
	@Override
	public void saveCustomerQuestionOptions(CustomerQuestionOptions customerQuestionOptions) {
		questionnaireEao.save(customerQuestionOptions);
	}
	//保存用户与用户选项
	@Override
	public void saveCustomerQuestionOptions(List<CustomerQuestionOptions> uqos) {
		for (int i = 0; i < uqos.size(); i++) {
			CustomerQuestionOptions u =uqos.get(i);
			questionnaireEao.save(u);
		}
		
	}
	//获得问卷报表
	@Override
	public QuestionnaireReport findQuestionnaireReport(Long id){
		QuestionnaireReport qnr_list_rt = new QuestionnaireReport();
		List<Question> qls = new ArrayList<Question>();
		Questionnaire qne = questionnaireEao.get(Questionnaire.class, id);
		if(null!=qne){
			
			int doCount = this.findCustomerQuestionnaireDoCount(id);				//获得所有人数
			int effectiveCount  = this.findCustomerQuestionnaireEffectiveCount(id);	//获得有效统计人数
			qnr_list_rt.setDoCount(doCount);
			qnr_list_rt.setEffectiveCount(effectiveCount);
			qnr_list_rt.setQuestionnaire(qne);
			qls = questionnaireEao.getQuestionByQuestionnaireId(qne.getId());
			List<QuestionReport> qr_list_rt = new ArrayList<QuestionReport>();
			for (int i = 0; i < qls.size(); i++) {
				Question q = qls.get(i);
 				List<QuestionOptions> qos =  questionnaireEao.getQuestionOptionsByQuestionId(q.getId());
				List<QuestionOptionsReport> qor_list_rt = new ArrayList<QuestionOptionsReport>();
				for (int j = 0; j < qos.size(); j++) {
					QuestionOptions qo = qos.get(j);
 					int optionDoCount = this.findOptionDoCount(qo.getId());
 					int questionDoCount = this.findQuestionDoCount(q.getId());
 					
 					int optionEffectiveCount = this.findOptionEffectiveCount(qo.getId());
 					int questionEffectiveCount = this.findQuestionEffectiveCount(q.getId());
 					
					QuestionOptionsReport qor =  new QuestionOptionsReport();
					qor.setOptions(qo);
					
					qor.setDoCount(optionDoCount);
					qor.setDoRatio(div((double)optionDoCount,(double)questionDoCount));
					qor.setEffectiveCount(optionEffectiveCount);
					qor.setEffectiveRatio(div((double)optionEffectiveCount,(double)questionEffectiveCount));
					
					qor_list_rt.add(qor);
				}
				QuestionReport qr = new QuestionReport();
				qr.setQuestion(q);
				qr.setOptionsReports(qor_list_rt);
				qr_list_rt.add(qr);
			}
			qnr_list_rt.setQuestionReports(qr_list_rt);
			 
		}
		return qnr_list_rt;
		
	}
	//总统计选项数
	private int findOptionDoCount(Long id){
		String jpql = "select count(s) from CustomerQuestionOptions as s where s.questionOptions.id = " +id; 
		return this.getEntityCount(jpql);
	}
	//总统计做问题人数
	private int findQuestionDoCount(Long id){
		String jpql = "select count( DISTINCT s.customerQuestionnaire) from CustomerQuestionOptions as s where s.question.id = " +id; 
		return this.getEntityCount(jpql);
	}
	
	////总统计选项数	有效
	private int findOptionEffectiveCount(Long id){
		String jpql = "select count(s) from CustomerQuestionOptions as s where s.questionOptions.id = " +id +" and s.customerQuestionnaire.finish ='1'"; 
		return this.getEntityCount(jpql);
	}
	//总统计做问题人数 有效
	private int findQuestionEffectiveCount(Long id){
		String jpql = "select count( DISTINCT s.customerQuestionnaire) from CustomerQuestionOptions as s where s.question.id = " +id+" and s.customerQuestionnaire.finish ='1'"; 
		return this.getEntityCount(jpql);
	}	
		
	//查询做问卷总数
	private int findCustomerQuestionnaireDoCount(Long id){
		String jpql = "select count(s) from CustomerQuestionnaire as s where s.questionnaire.id = " +id;
		return this.getEntityCount(jpql);
	}
	//查询做完问卷，即有效问卷的总数
	private int findCustomerQuestionnaireEffectiveCount(Long id){
		String jpql = "select count(s) from CustomerQuestionnaire as s where s.questionnaire.id = " +id+ " and s.finish = '1'";
		return this.getEntityCount(jpql);
	}	
	//==================================business  end==================================
	/**
	 * 获得两数据的百分比
	 * 已乘以100
	 * @param v1
	 * @param v2
	 * @return
	 */
	 public   double div(double v1, double v2) {
		 double div_rt = 0;
		 if(v1>0&&v2>0){
			 BigDecimal b1 = new BigDecimal(Double.toString(v1));
			 BigDecimal b2 = new BigDecimal(Double.toString(v2));
			 div_rt =    b1.divide(b2, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();
		 }else{
			 div_rt = 0;
		 }
		 return div_rt;
	 }
	
	 //判断问卷是否在设计状态
	@Override
	public boolean isDesign(Long id) {
		int dis_count = questionnaireEao.findDesignCount(id);
		if(dis_count==1){
			return true;
		}else{
			return false;
		}
	}
	
	//关闭问卷
	@Override
	public void closeQuestionnaireById(Long id) {
		Questionnaire q = questionnaireEao.get(Questionnaire.class, id);
		if(null!=q){
			q.setStateCode("C");//关闭
			questionnaireEao.update(q);
		}
	}
	
	//初始化问题类型
	@Override
	public void initQuestionTypes() {
		String jpql = "select count(s) from QuestionType as s ";
		int ecount = questionnaireEao.getEntityCount(jpql);
		if(ecount != 3){
			QuestionType qtype1 = new QuestionType();
			qtype1.setId(new Long(1));
			qtype1.setName("单选");
			qtype1.setValue("radio");
			qtype1.setCreateTime(new Date());
			
			QuestionType qtype2 = new QuestionType();
			qtype2.setId(new Long(2));
			qtype2.setName("多选");
			qtype2.setValue("checkbox");
			qtype2.setCreateTime(new Date());
			
			QuestionType qtype3 = new QuestionType();
			qtype3.setId(new Long(3));
			qtype3.setName("开放");
			qtype3.setValue("open");
			qtype3.setCreateTime(new Date());
			
		/*	QuestionType qtype4 = new QuestionType();
			qtype4.setId(new Long(4));
			qtype4.setName("混合");
			qtype4.setValue("blend");
			qtype4.setCreateTime(new Date());*/
			
			questionnaireEao.deletAllQuestionType();
			questionnaireEao.save(qtype1);
			questionnaireEao.save(qtype2);
			questionnaireEao.save(qtype3);
			//questionnaireEao.save(qtype4);
 		}
		
	}
	/**
	 * 根据domain域和问卷状态
	 * 获得该域相关的问卷列表，
	 * @param doaminId	问卷的状态		
	 * @param statecode	问卷的状态,D.设计，E 执行，C 删除
	 * @return 问卷列表 List<Questionnaire>
	 */
	@Override
	public List<Questionnaire> getAllByDomainId(Long doaminId, String statecode) {
		return questionnaireEao.getAllByDomainId(doaminId, statecode);
	}
	
	
	// ==================================get set start==================================
	public QuestionnaireEao getQuestionnaireEao() {
		return questionnaireEao;
	}
	public void setQuestionnaireEao(QuestionnaireEao questionnaireEao) {
		this.questionnaireEao = questionnaireEao;
	}
	public QuestionEao getQuestionEao() {
		return questionEao;
	}
	public void setQuestionEao(QuestionEao questionEao) {
		this.questionEao = questionEao;
	}
	// ==================================get set end==================================

}
