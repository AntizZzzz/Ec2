package com.jiangyifen.ec2.ui.mgr.questionnaire;


import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
public class AddQuestionnaire extends Window implements ClickListener, ValueChangeListener{

	
	private static final long serialVersionUID = 3542356129862831719L;
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//=================================页面初始化start=================================================
	//页面展示初始化
	
	//页面元素
	private final Object[] TYPICAL_VISIBLE_PROPERTIES = new Object[] {"mainTitle", "subtitle","showcode"};//添加时 form 表格中需要显示的属性列及顺序
	private final String[] TYPICAL_REQUIRED_NAME = new String[] {"主标题", "副标题","问卷类型"};//添加时 form 表格中必须拥有值的字段
	 
	//页面服务层初始化
	private Form form;			//Form输出
	
	private Button save;		//保存按钮
	private Button cancel;		//取消按钮
	private Label lb_msg;		//提示信息
	private TextArea ta_note;	//备注信息
	
	//业务元素
	private Domain domain;		//当前用户
	private Questionnaire questionnaire;
	private QuestionnaireService questionnaireService;	 
	private QuestionnaireManagement questionnaireManagement;
	
	public AddQuestionnaire (QuestionnaireManagement questionnaireManagement){
		
		this.questionnaireManagement = questionnaireManagement;
		
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("添加问卷");
		this.setWidth("500px");
		this.setHeight("300px");
	
		//初始化spring相关属性
		domain = SpringContextHolder.getDomain();
		questionnaireService = SpringContextHolder.getBean("questionnaireService");
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
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
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
 		form.setWriteThrough(false);
		form.setImmediate(true);
 		form.setFormFieldFactory(new MyFieldFactory());		//添加字段
		form.setFooter(creatFormFooterComponents());		//添加底部
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
		// 简单编辑分机的各字段的值
		createTypicalSipConfig();
		ta_note.setValue("");
	}

	private void createTypicalSipConfig() {
		questionnaire = new Questionnaire();
		this.lb_msg.setValue("");
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<Questionnaire>(questionnaire), Arrays.asList(TYPICAL_VISIBLE_PROPERTIES));
		for (int i = 0; i < TYPICAL_VISIBLE_PROPERTIES.length; i++) {
			form.getField(TYPICAL_VISIBLE_PROPERTIES[i]).setCaption(TYPICAL_REQUIRED_NAME[i].toString() +"：");
		}
		form.getField("showcode").setValue(1);
	}
	//=================================页面初始化end=================================================
	//=================================接口实现start=================================================
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == form) {
			 
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){//保存按钮
			if(validform(form)){//验证表单通过
				try {
					form.commit();
					questionnaire.setCreateTime(new Date());
					questionnaire.setStateCode("D"); //设计
					questionnaire.setDomain(domain);
					Object o_note = ta_note.getValue();
					if(null != o_note){
						questionnaire.setNote(o_note.toString());
					}
					if (questionnaireService.validateMainTitle(questionnaire.getMainTitle(),domain.getId())) {
						questionnaireService.saveQuestionnaire(questionnaire);
						questionnaireManagement.updateTable(true);
						questionnaireManagement.getTable().setValue(null);
						this.getApplication()
								.getMainWindow()
								.showNotification("保存问卷成功!",
										Notification.TYPE_HUMANIZED_MESSAGE);
						this.getParent().removeWindow(this);
					} else {
						lb_msg.setValue("问卷主标题已经存在,请修改!");
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("保存问卷失败_buttonClick_LLXXYY", e);
					this.getApplication()
							.getMainWindow()
							.showNotification("保存问卷失败!",
									Notification.TYPE_ERROR_MESSAGE);
				}
			}
		}
		if(source == cancel){//取消按钮
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}
	//=================================接口实现end=================================================
	
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

	//=================================内部类实现end=================================================
	//表单字段添加
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
