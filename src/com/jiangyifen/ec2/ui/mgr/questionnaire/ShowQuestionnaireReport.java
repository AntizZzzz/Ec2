package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.List;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionOptionsReport;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionReport;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionnaireReport;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ShowQuestionnaireReport extends Window  implements ClickListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4005528004862497833L;
	// 静态

	// 持有UI对象
 	// 本控件持有对象
	private Questionnaire questionnaire;
	 
	// 页面布局组件
	private VerticalLayout windowContent;
 	private VerticalLayout vl_Question;
	private HorizontalLayout vl_botton;
	
	private Label lb_qe_subtitle;
	private Label lb_qe_do;
	private Label lb_qe_effective;
	
	
	
	private Button bt_close;
	
	// 页面控件
	 
	// 业务必须对象

	// 业务本页对象
	private Long questionnaireId;

	// 业务注入对象
	private QuestionnaireService questionnaireService;
	@SuppressWarnings("unused")
	private QuestionService questionService;

	public ShowQuestionnaireReport() {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("欢迎你的到来!");
		this.setWidth("600px");
		this.setHeight("600px");
		initSpringContext();
		
		windowContent = createWindowContentComponents();
 		vl_Question = createQuestionComponents();
		vl_botton = createBottonComponents();
		
 		windowContent.addComponent(vl_Question);
		windowContent.addComponent(vl_botton);
		addComponent(windowContent);
	}
	
	private HorizontalLayout createBottonComponents() {
		HorizontalLayout h = new HorizontalLayout();
		h.setSizeFull();
		bt_close = new Button("关     闭",this);
		bt_close.setWidth("150px");
		h.addComponent(bt_close);
		return h;
	}

	private VerticalLayout createQuestionComponents() {
		VerticalLayout vl_Question_rt = new VerticalLayout();
		vl_Question_rt.setSizeFull();
		return vl_Question_rt;
	}

	private VerticalLayout createWindowContentComponents() {
		VerticalLayout windowContent_rt = new VerticalLayout();
		windowContent_rt.setWidth("100%");
		windowContent_rt.setSpacing(true);
		windowContent_rt.setMargin(false);
		
		lb_qe_subtitle = new Label("",Label.CONTENT_XHTML);
		windowContent_rt.addComponent(lb_qe_subtitle);
		
		lb_qe_do = new Label();
		windowContent_rt.addComponent(lb_qe_do);
		
		lb_qe_effective = new Label();
		windowContent_rt.addComponent(lb_qe_effective);
		
 		return windowContent_rt;
	}
	
	private void initSpringContext(){
		//初始化spring相关属性
		questionnaireService = SpringContextHolder.getBean("questionnaireService");
		questionService = SpringContextHolder.getBean("questionService");
	}

	private void updateReport() {
		QuestionnaireReport qrt = questionnaireService.findQuestionnaireReport(questionnaireId);
		String table_sub= "<table borderColor='#ffffff' style='line-height: 150%; border-collapse: collapse; margin-bottom: 0px;' border='1' cellSpacing='0' cellPadding='0'>"
					+"<TR>"
						+"<TD >&nbsp;"+qrt.getQuestionnaire().getSubtitle()+"</TD>"
					+"</TR>"
				+"</table>";		
		lb_qe_subtitle.setValue(table_sub);
		lb_qe_do.setValue("调查总人数："+qrt.getDoCount()+"人");
		lb_qe_effective.setValue("有效总人数："+qrt.getEffectiveCount()+"人");
		
		List<QuestionReport>  ls1 = qrt.getQuestionReports();
		for (int i = 0; i < ls1.size(); i++) {
			QuestionReport qr1 = ls1.get(i);
			Question q1 = qr1.getQuestion();
			
			Label lb_i = new Label("",Label.CONTENT_XHTML);
			StringBuffer tb_q = new StringBuffer();
			tb_q.append("<table borderColor='#ffffff' style='line-height: 150%; border-collapse: collapse; margin-bottom: 5px;' border='1' cellSpacing='0' cellPadding='0' width='100%'> ");
			
			tb_q.append("<TR>");
					tb_q.append("<TD colSpan=4>&nbsp;");
					tb_q.append((i+1));
					tb_q.append(".");
					tb_q.append(q1.getTitle());
					tb_q.append("</TD>");
					tb_q.append("<TD ><B>&nbsp;");
					tb_q.append(q1.getQuestionType().getName());
					tb_q.append("</B></TD>");
				tb_q.append("</TR>");
				tb_q.append("<TR style='COLOR: white'>");
				tb_q.append("<TD bgColor=#999999>&nbsp;<B>选项名称</B></TD>");
				tb_q.append("<TD bgColor=#999999 align=middle width=50px>&nbsp;<B>选择数</B></TD>");
				tb_q.append("<TD bgColor=#999999 align=middle width=50px>&nbsp;<B>百分比</B></TD>");
				tb_q.append("<TD bgColor=#999999 align=middle width=50px>&nbsp;<B>有效<br>选择数</B></TD>");
				tb_q.append("<TD bgColor=#999999 align=middle width=50px>&nbsp;<B>有效<br>百分比</B></TD>");
				tb_q.append("</TR>");
				
				List<QuestionOptionsReport>  ls2 =	qr1.getOptionsReports();
				for (int j = 0; j < ls2.size(); j++) {
					QuestionOptionsReport q2 = ls2.get(j);
					tb_q.append("<TR>");
					tb_q.append("<TD>&nbsp;");
					tb_q.append(findIntIndex(j,1));
 					tb_q.append(q2.getOptions().getName());
					tb_q.append("</TD>");
					
					tb_q.append("<TD  align=middle>&nbsp;");
					tb_q.append(q2.getDoCount());
					tb_q.append("</TD>");
					
					tb_q.append("<TD align=middle>&nbsp;");
					tb_q.append(q2.getDoRatio()+"%");
					tb_q.append("</TD>");
					 
					tb_q.append("<TD align=middle>&nbsp;");
					tb_q.append(q2.getEffectiveCount());
					tb_q.append("</TD>");
					
					tb_q.append("<TD align=middle>&nbsp;");
					tb_q.append(q2.getEffectiveRatio()+"%");
					tb_q.append("</TD>");
					
					tb_q.append("</TR>");
				}
				
			tb_q.append("</table>");
			
			lb_i.setValue(tb_q.toString());
			vl_Question.addComponent(lb_i);
		}
	}

	private void updateQuestionnaire(Long questionnaireId) {
		questionnaire = questionnaireService.getQuestionnaireById(questionnaireId);
	}

	private void updateWindowContent() {
		if(null!=questionnaire){
 			this.setCaption("问卷报告::"+questionnaire.getMainTitle()+"!");
		}else{
			this.setCaption("问卷为空，请重新选择");
		}
	}
	
	/**
	 * 更新组件
	 * @param questionnaireId
	 */
	public void refreshComponentInfo(Long questionnaireId) {
		this.questionnaireId = questionnaireId;
		updateQuestionnaire(questionnaireId);
 		updateWindowContent();
 		vl_Question.removeAllComponents();
 		updateReport();
	}
	 

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_close){
			this.getApplication().getMainWindow().removeWindow(this);
		}
		
	}
	
	private String findIntIndex(int itemLength, int indexType) {
		String rtn_index = "";
		if(0 == indexType){
			rtn_index = (itemLength+1) + ".";
		}else{
			rtn_index = (char)(itemLength+65) + ".";
		}
		return rtn_index;
	}
	
	public Long getQuestionnaireId() {
		return questionnaireId;
	}

	public void setQuestionnaireId(Long questionnaireId) {
		this.questionnaireId = questionnaireId;
	}
}
