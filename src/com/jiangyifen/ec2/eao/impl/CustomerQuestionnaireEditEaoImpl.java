package com.jiangyifen.ec2.eao.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.CustomerQuestionnaireEditEao;
import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.query.CustomerQuestionnaireQuery;

/**
* Eao实现类：客户问卷编辑
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class CustomerQuestionnaireEditEaoImpl extends BaseEaoImpl implements CustomerQuestionnaireEditEao {
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	@SuppressWarnings("unchecked")
	//获得客户问卷编辑列表
	public List<CustomerQuestionnaireQuery> loadCustomerQuestionnaireEditList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	public List<CustomerQuestionnaireQuery> loadPageEntitiesSQL(int start,
			int length, String sql) {
		
		@SuppressWarnings("unchecked")
		List<Object[]> ls = this.getEntityManager().createNativeQuery(sql).setFirstResult(start).setMaxResults(length).getResultList();
		List<CustomerQuestionnaireQuery> lsrt = new ArrayList<CustomerQuestionnaireQuery>();
		
		for (int i = 0; i < ls.size(); i++) {
			Object[] t = (Object[]) ls.get(i);
			CustomerQuestionnaireQuery cqq = new CustomerQuestionnaireQuery();
			if(t[0] != null){
				Long id = Long.valueOf(t[0].toString());
				cqq.setId(id);
			}
			if(t[1] != null){
				Long csrId = Long.valueOf(t[1].toString());
				cqq.setCsrId(csrId);
			}
			if(t[2] != null){
				cqq.setCsrRealName(t[2].toString());
			}
			if(t[3] != null){
				cqq.setCsrUserName(t[3].toString());
			}
			if(t[4] != null){
				cqq.setCsrEmpNo(t[4].toString());
			}
			if(t[5] != null){
				Long cresId = Long.valueOf(t[5].toString());
				cqq.setCresId(cresId);
			}
			if(t[6] != null){
				cqq.setCresName(t[6].toString());
			}
			if(t[7] != null){
				cqq.setTel_number(t[7].toString());
			}
			if(t[8] != null){
				cqq.setQuestionnaireName(t[8].toString());
			}
			if(t[9] != null){
				Date sd;
				try {
					sd = sdf.parse(t[9].toString());
				} catch (ParseException e) {
					e.printStackTrace();
					sd = null;
				}
				cqq.setQuestionnaireStartTime(sd);
			}
			if(t[10] != null){
				Date ed ;
				try {
					ed = sdf.parse(t[9].toString());
				} catch (ParseException e) {
					e.printStackTrace();
					ed = null;
				}
				cqq.setQuestionnaireEndTime(ed);
			}
			if(t[11] != null){
				String finish = t[11].toString();
				if("start".equals(finish)){
					finish = "开始";
				}else{
					finish = "完成";
				}
				cqq.setQuestionnaireFinish(finish);
			}
			 
 			lsrt.add(cqq);
		}
		return lsrt;
	}
	

	@Override
	public int getEntityCountSQL(String sql) {
		return super.getEntityCountByNativeSql(sql);
	}
	
	@Override
	public List<Object[]> loadPageEntitiesSQLArr(int start, int length,
			String sql) {
		@SuppressWarnings("unchecked")
		List<Object[]> ls = this.getEntityManager().createNativeQuery(sql).setFirstResult(start).setMaxResults(length).getResultList();
		return ls;
	}
	
	@SuppressWarnings("unchecked")
	public List<CustomerQuestionOptions> findResult(Long cqrid, Long qoptionid){
		String jpql = "select s from CustomerQuestionOptions as s where s.question.id = "+cqrid+" and s.customerQuestionnaire.id = "+qoptionid+" order by s.id"  ;
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Long> getQuestionnaireRecordFileLink(
			Long customerQuestionnaireId) {
		String sql = "select record_file_id from ec2_customer_questionnaire_record_file_link  ec2_record_file    where customer_questionnaire_id = " + customerQuestionnaireId;
		List<Long> ls = this.getEntityManager().createNativeQuery(sql).getResultList(); 
		return ls;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<RecordFile> getRecordFileList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}