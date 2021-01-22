package com.jiangyifen.ec2.ui.mgr.questionnaire;


import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

//TODO lxy	1305
/**
* 问卷调查添加问卷
* 
* 
* 注入：QuestionnaireEao questionnaireEao
* 
* @author lxy
*
*/
public class EditQuestionnaire extends Window implements ClickListener, ValueChangeListener{

	
	private static final long serialVersionUID = 3542356129862831719L;
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	//=================================页面初始化start=================================================
	//页面展示初始化
	
	private final Object[] TYPICAL_VISIBLE_PROPERTIES = new Object[] {"mainTitle", "subtitle","showcode"};//添加时 form 表格中需要显示的属性列及顺序
	private final String[] TYPICAL_REQUIRED_NAME = new String[] {"主标题", "副标题","问卷类型"};//添加时 form 表格中必须拥有值的字段
	
	
	//页面服务层初始化
	private Form form;			//Form输出
	
	private Button save;		//保存按钮
	private Button cancel;		//取消按钮
	private Label lb_msg;		//提示信息
	private TextArea ta_note;	//备注信息
	
	//业务元素
	//private Domain domain;		//当前用户
	private QuestionnaireService questionnaireService;	 
	private Questionnaire questionnaire;
	
	private QuestionnaireManagement questionnaireManagement;
	public EditQuestionnaire (QuestionnaireManagement questionnaireManagement ){
		
		this.questionnaireManagement = questionnaireManagement;
		
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("编辑问卷");
		this.setWidth("500px");
		this.setHeight("300px");
		
 		questionnaireService = SpringContextHolder.getBean("questionnaireService");	//初始化spring相关属性
		
		VerticalLayout windowContent=new VerticalLayout();	//添加Window内最大的Layout
 		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		// 创建Form 表单
		createFormComponent(windowContent);
		
	}
	
	// 创建Form 表单
	private void createFormComponent(VerticalLayout windowContent) {
		form=new Form();
		form.setValidationVisible(false);
		form.setValidationVisibleOnCommit(true);
		form.setInvalidCommitted(false);
		form.setWriteThrough(false);
		form.setImmediate(true);
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(creatFormFooterComponents());
		windowContent.addComponent(form);
		
	}
	
	private VerticalLayout creatFormFooterComponents() {
		VerticalLayout vl = new VerticalLayout();
		vl.setSpacing(true);
		HorizontalLayout tnote = new HorizontalLayout();
		tnote.setSpacing(true);
		ta_note = new TextArea();
		ta_note.setRows(4);
		ta_note.setColumns(29);
		ta_note.setMaxLength(2000);
		tnote.addComponent(new Label("备注信息&nbsp&nbsp&nbsp&nbsp&nbsp",Label.CONTENT_XHTML));
		tnote.addComponent(ta_note);
		vl.addComponent(tnote);
		
		HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setSpacing(true);
 
		// 保存按钮
		save = new Button("保存问卷", this);
		save.setStyleName("default");
		footerLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消", this);
		footerLayout.addComponent(cancel);
		
		lb_msg = new Label("");
		lb_msg.setWidth("-1px");
 		footerLayout.addComponent(lb_msg);
 		
 		vl .addComponent(footerLayout);
		return vl;
	}
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
 		questionnaire = (Questionnaire) questionnaireManagement.getTable().getValue();
 		createTypicalQuestionnaire();
	}
	
	private void createTypicalQuestionnaire() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<Questionnaire>(questionnaire), Arrays.asList(TYPICAL_VISIBLE_PROPERTIES));
		for (int i = 0; i < TYPICAL_VISIBLE_PROPERTIES.length; i++) {
			form.getField(TYPICAL_VISIBLE_PROPERTIES[i]).setCaption(TYPICAL_REQUIRED_NAME[i].toString() +"：");
		}
		// 回显各组件的值
		form.getField("mainTitle").setValue(questionnaire.getMainTitle());
		form.getField("subtitle").setValue(questionnaire.getSubtitle());
		form.getField("showcode").setValue(questionnaire.getShowcode());
		if(null != questionnaire.getNote()){
			ta_note.setValue(questionnaire.getNote());
		}
	}
	
	private boolean validform(Form form) {
		boolean valiTF = false;
		String mainTitle = (String) form.getField("mainTitle").getValue();
		if(validateText(mainTitle)){
			valiTF = true;
		}else{
			lb_msg.setValue("主标题不能为空!");
			valiTF = false;
		}
		return valiTF;
	}
	
	public boolean validateText(String value){
		if((null==value)||("".equals(value))||("".equals(value.trim()))){
			return false;
		}else{
			return true;
		}
	}
	//=================================页面初始化end=================================================
	//=================================接口实现start=================================================
	@Override
	public void valueChange(ValueChangeEvent event) {
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			if(validform(form)){
				try {
					form.commit();
					Object o_note = ta_note.getValue();
					if(null != o_note){
						questionnaire.setNote(o_note.toString());
					}
					questionnaireService.updateQuestionnaire(questionnaire);
					questionnaireManagement.updateTable(true);
					questionnaireManagement.getTable().setValue(null);
					this.getApplication()
							.getMainWindow()
							.showNotification("修改问卷成功！",
									Notification.TYPE_HUMANIZED_MESSAGE);
					this.getParent().removeWindow(this);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("修改问卷失败_buttonClick_LLXXYY", e);
					this.getApplication()
							.getMainWindow()
							.showNotification("修改问卷失败!",
									Notification.TYPE_ERROR_MESSAGE);
				}  
			}
		}
		if(source == cancel){//取消按钮
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}
	//=================================接口实现end=================================================
	
	//=================================内部类实现end=================================================
	private class MyFieldFactory extends DefaultFieldFactory{
		
		private static final long serialVersionUID = -8056296768695577472L;

		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("mainTitle".equals(propertyId)){
				TextField mainTitle = new TextField();
				mainTitle.focus();
				mainTitle.setRequired(true);
				mainTitle.setWidth("388px");
				mainTitle.setNullRepresentation("");
				mainTitle.setInputPrompt("请输入主标题！");
				mainTitle.setMaxLength(50);
 				return mainTitle;
			}
			
			if("subtitle".equals(propertyId)){
				TextArea subtitle = new TextArea();
				subtitle.setColumns(29);
				subtitle.setRows(1);
				subtitle.setNullRepresentation("");
				subtitle.setInputPrompt("请输入副标题！");
				subtitle.setMaxLength(500);
 				return subtitle;
			}
			
			if("showcode".equals(propertyId)){
 				ComboBox showcode = new ComboBox();
 				showcode.setNullSelectionAllowed(false);
 				showcode.addItem(1);
 				showcode.addItem(2);
 				showcode.setItemCaption(1, "跳转模式");
 				showcode.setItemCaption(2, "全部模式");
 				return showcode;
			}
			return null;
		}
	}
	//=================================内部类实现end=================================================
}
