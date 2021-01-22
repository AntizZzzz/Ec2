package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.eao.CustomerQuestionOptionsEao;
import com.jiangyifen.ec2.eao.QuestionEao;
import com.jiangyifen.ec2.eao.RecordFileEao;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionOptionsFormValue;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;

public class QuestionServiceImpl implements QuestionService {

	//==================================need  end==================================
	private QuestionEao questionEao; 
	private RecordFileEao recordFileEao;
	private CustomerQuestionOptionsEao customerQuestionOptionsEao;
	//==================================need  end==================================
	
	//==================================business start==================================
	@Override
	@SuppressWarnings("unchecked")
	public List<Question> loadPageEntities(int start, int length, String sql) {
		return questionEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return questionEao.getEntityCount(sql);
	}
	
	//获得该问卷的第一个问题
	@Override
	public Question getFirstQuestionByQuestionnaireId(Long id) {
		return questionEao.getFirstQuestionByQuestionnaireId(id);
	}
	
	//根据该问卷的问题列表
	@Override
	public List<Question> getQuestionListByQuestionnaireId(Long id) {
		return questionEao.getQuestionListByQuestionnaireId(id);
	}
	
	//根据问题获得该问题选项列表
	@Override
	public List<QuestionOptions> getQuestionOptionsListByQuestionId(Long id) {
		return questionEao.getQuestionOptionsListByQuestionId(id);
	}
	//根据问题获得下一条
	@Override
	public Question getNextQuestionByOrdernumber(Question question) {
		return questionEao.getNextQuestionByOrdernumber(question.getQuestionnaire().getId(),question.getOrdernumber());
	}
	//保存用户问卷
	@Override
	public void saveCustomerQuestionnaire(CustomerQuestionnaire customerQuestionnaire) {
		customerQuestionnaire.setStartTime(new Date());
		customerQuestionnaire.setFinish("start");//start 没有完成，end已经完成
		questionEao.save(customerQuestionnaire);
	}
	
	//更新用户问卷
	@Override
	public void updateCustomerQuestionnaire(CustomerQuestionnaire customerQuestionnaire) {
		customerQuestionnaire.setEndTime(new Date());
		customerQuestionnaire.setFinish("end");//start 没有完成，end已经完成
		questionEao.update(customerQuestionnaire);
	}
	
	//根据问题编号查看该编号是否存在跳转关联
	@Override
	public boolean findQuestionSkipBuId(Long id) {
		boolean bl_tf = false;
		int listCount = questionEao.findQuestionCountSkipBuId(id);
		if (listCount > 0){
			bl_tf = true;
		}else{
			bl_tf = false;
		}
 		return bl_tf;
	}
	
	//根据问卷和问题排序号获得该文当前序号索引值
	@Override
	public int getIndexByQuestionnaireAndQuestionOrder(
			Long questionnaireid, int ordernumber) {
		String sql = " select count(s) from Question as s where s.questionnaire.id = "+questionnaireid+" and s.ordernumber <= "+ordernumber;
		return questionEao.getEntityCount(sql);
	}
	
	//保存客户问卷关系和录音文件的关系
	@Override
	public void saveCustomerQuestionnaireAndChannel(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile) {
		recordFileEao.save(recordFile);
		customerQuestionnaire.getRecordFiles().add(recordFile);
		customerQuestionnaire.setStartTime(new Date());
		customerQuestionnaire.setFinish("start");//start 没有完成，end已经完成
		questionEao.save(customerQuestionnaire);
		
	}
	
	//更新客户问卷关系和录音文件的关系
	@Override
	public void updateCustomerQuestionnaireAndChannel(CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile) {
		recordFileEao.save(recordFile);
		customerQuestionnaire.getRecordFiles().add(recordFile);
		questionEao.update(customerQuestionnaire);
	}
	
	//删除该客户问卷相关的数据
	@Override
	@SuppressWarnings("unchecked")
	public Set<RecordFile> deleteCustomerQuestionnaireAndChannel(
			Questionnaire questionnaire, Long customerid, User loginUser) {
		Set<RecordFile> recordFiles = new HashSet<RecordFile>();
		String sql = "select s from CustomerQuestionnaire as s where s.questionnaire.id = "+questionnaire.getId()+" and s.customerid = "+customerid;
		List<CustomerQuestionnaire> list = questionEao.loadPageEntities(0, 1, sql);
		CustomerQuestionnaire customerQuestionnaire = null;
		if((null != list)&&(list.size() == 1)){
			customerQuestionnaire = list.get(0);
		}
		if(null != customerQuestionnaire){ 
			//删除或修改该
			//删除客户问卷选项
			//客户问卷关系
			questionEao.deleteCustomerQuestionOptionsByCustomerQuestionnaireId(customerQuestionnaire.getId());
			// 获取将要被删除的客户问卷对应的录音文件集合
			recordFiles = customerQuestionnaire.getRecordFiles();
			questionEao.delete(customerQuestionnaire);
		}
		return recordFiles;
	}
		
	//根据问卷客户编号获得客户问卷编号
	@Override
	@SuppressWarnings("unchecked")
	public CustomerQuestionnaire getCustomerQuestionnaireByCustomeridQuestionnaireUserid(Questionnaire questionnaire, Long customerid, User loginUser) {
		String sql = "select s from CustomerQuestionnaire as s where s.questionnaire.id = "+questionnaire.getId()+" and s.customerid = "+customerid;
		List<CustomerQuestionnaire> list = questionEao.loadPageEntities(0, 1, sql);
		if((null != list)&&(list.size() == 1)){
			return list.get(0);
		}else{
			return null;
		}
	}
	
	//根据客户问卷关系编号获得正确应该显示的问题
	@Override
	@SuppressWarnings("unchecked")
	public Question getRightQuestion(CustomerQuestionnaire customerQuestionnaire) {
		//根据编号获得客户选项的最后一条一句编号生产自增规则
		//获得该条的问题
		//获得下一个问题
		String sql = "select s from CustomerQuestionOptions as s where s.customerQuestionnaire.id = "+customerQuestionnaire.getId()+" order by s.id desc";
		List<CustomerQuestionOptions> list = questionEao.loadPageEntities(0, 1, sql);
		CustomerQuestionOptions option = null;
		if((null != list)&&(list.size() == 1)){
			option = list.get(0);
		}

		Question question = null;
		if(null != option){
			question = option.getQuestion();
			if(question.getQuestionType().getId() == 1){
				//跳转
				//如果跳转没空则正常跳转下一题
				if(null != option.getQuestionOptions().getSkip()){
					return option.getQuestionOptions().getSkip();
				}else{
					return questionEao.getNextQuestionByOrdernumber(question.getQuestionnaire().getId(),question.getOrdernumber());
				}
			}else{
				//正常顺序下一题
				return questionEao.getNextQuestionByOrdernumber(question.getQuestionnaire().getId(),question.getOrdernumber());
			}
		}else{
			return questionEao.getFirstQuestionByQuestionnaireId(customerQuestionnaire.getQuestionnaire().getId());
		}
	}
	
	@Override
	public void saveCustomerQuestionnaireAndChannelShowAll(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile,
			List<QuestionOptionsListView> listViews) {
		for (int i = 0; i < listViews.size(); i++) {
			QuestionOptionsListView qolv = listViews.get(i);
			int opts_size = qolv.getFormValue().size();
			if (opts_size > 0) {
				for (QuestionOptionsFormValue qofv : qolv.getFormValue()) {
					CustomerQuestionOptions customerQuestionOptions = new CustomerQuestionOptions();
					customerQuestionOptions.setCustomerQuestionnaire(customerQuestionnaire);
					customerQuestionOptions.setQuestion(qolv.getQuestion());
					customerQuestionOptions.setQuestionOptions(qofv.getOptions());
					customerQuestionOptions.setText(qofv.getOptionText());
 					 if("open".equals(qolv.getQuestion().getQuestionType().getValue())){
 						 if(qofv.getOptionText()!=null&&(qofv.getOptionText().trim().length()>0)) {
							 questionEao.save(customerQuestionOptions);
						 }
					 }else{
						 questionEao.save(customerQuestionOptions);
					 }
					
				}				
			} 
		}
	}

	@Override
	public void saveCustomerQuestionnaireAndChannelShowAllClearOptions(
			CustomerQuestionnaire customerQuestionnaire, RecordFile recordFile,
			List<QuestionOptionsListView> listViews) {
		
		questionEao.deleteCustomerQuestionOptionsByCustomerQuestionnaireId(customerQuestionnaire.getId());
		for (int i = 0; i < listViews.size(); i++) {
			QuestionOptionsListView qolv = listViews.get(i);
			int opts_size = qolv.getFormValue().size();
			if (opts_size > 0) {
				for (QuestionOptionsFormValue qofv : qolv.getFormValue()) {
					CustomerQuestionOptions customerQuestionOptions = new CustomerQuestionOptions();
					customerQuestionOptions.setCustomerQuestionnaire(customerQuestionnaire);
					customerQuestionOptions.setQuestion(qolv.getQuestion());
					customerQuestionOptions.setQuestionOptions(qofv.getOptions());
					customerQuestionOptions.setText(qofv.getOptionText());
 					 if("open".equals(qolv.getQuestion().getQuestionType().getValue())){
 						 if(qofv.getOptionText()!=null&&(qofv.getOptionText().trim().length()>0)) {
							 questionEao.save(customerQuestionOptions);
						 }
					 }else{
						 questionEao.save(customerQuestionOptions);
					 }
					
				}				
			} 
		}
	}
	
	@Override
	public CustomerQuestionOptions findOptionIsSelect(long customerQuestionnaireId,
			Long questionId, Long optionId) {
		return   customerQuestionOptionsEao.findOptionIsSelect(customerQuestionnaireId,questionId,optionId);
	}
	
	//更新用户问卷
	@Override
	public void updateCustomerQuestionnaireOnly(CustomerQuestionnaire customerQuestionnaire) {
		customerQuestionnaire.setEndTime(new Date());
		questionEao.update(customerQuestionnaire);	
	}
		
	@Override
	public void updateCustomerQuestionnaireOnlyFinish(
				CustomerQuestionnaire customerQuestionnaire) {
		questionEao.update(customerQuestionnaire);	
	}	
	//==================================business  end==================================
	
	//==================================get set start==================================
	
	public QuestionEao getQuestionEao() {
		return questionEao;
	}

	public void setQuestionEao(QuestionEao questionEao) {
		this.questionEao = questionEao;
	}

	public RecordFileEao getRecordFileEao() {
		return recordFileEao;
	}

	public void setRecordFileEao(RecordFileEao recordFileEao) {
		this.recordFileEao = recordFileEao;
	}
	
	public CustomerQuestionOptionsEao getCustomerQuestionOptionsEao() {
		return customerQuestionOptionsEao;
	}

	public void setCustomerQuestionOptionsEao(
			CustomerQuestionOptionsEao customerQuestionOptionsEao) {
		this.customerQuestionOptionsEao = customerQuestionOptionsEao;
	}

	//==================================get set start==================================


}
