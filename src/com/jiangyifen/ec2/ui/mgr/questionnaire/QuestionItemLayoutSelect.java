package com.jiangyifen.ec2.ui.mgr.questionnaire;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class QuestionItemLayoutSelect extends HorizontalLayout implements ClickListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1020686403228512839L;

	private Label lb_index;
	private TextField item_title;/**标题 */
	private TextField item_spik;//item_spik = new TextField();
	private Button btn_skip ;
	private CheckBox cb_tf_text;
	
	private EditSkipQuestion editSkipQuestion;
	private SelectQuestion selectQuestion;
	
	/** 初始化一个空的问题选项对象*/
	public QuestionItemLayoutSelect(int itemLength,int indexType,EditSkipQuestion editSkipQuestion){
		this.editSkipQuestion = editSkipQuestion;
 		 
		String int_index =  findIntIndex(itemLength,indexType);
		lb_index = new Label(int_index);
		
		item_title = new TextField();
		item_title.setWidth("260px");
		item_title.setMaxLength(200);
		
		cb_tf_text=new CheckBox("文本");
		
		btn_skip = new Button("转至");
		btn_skip.addListener(this);
		
		item_spik = new TextField();
		item_spik.setWidth("30px");
		item_spik.setEnabled(false);
		item_spik.setMaxLength(10);
		
		this.addComponent(lb_index);
		this.addComponent(item_title);
		this.addComponent(cb_tf_text);
		this.addComponent(btn_skip);
		this.addComponent(item_spik);
	}
	
	/** 初始化一个有值的问题选项对象*/
	public QuestionItemLayoutSelect(EditSkipQuestion editSkipQuestion,int itemLength,int indexType,String item_title_value,String item_spik_value,boolean item_spik_enabled,boolean item_text_value){
		this.editSkipQuestion = editSkipQuestion;
 		 
		String int_index =  findIntIndex(itemLength,indexType);
		lb_index = new Label(int_index);
		
		item_title = new TextField();
		item_title.setValue(item_title_value);
		item_title.setWidth("260px");
		item_title.setMaxLength(200);
		
		cb_tf_text=new CheckBox("文本");
		cb_tf_text.setValue(item_text_value);
		
		btn_skip = new Button("转至");
		btn_skip.addListener(this);
		btn_skip.setEnabled(item_spik_enabled);
		
		item_spik = new TextField();
		item_spik.setValue(item_spik_value);
		item_spik.setWidth("30px");
		item_spik.setEnabled(false);
		
		this.addComponent(lb_index);
		this.addComponent(item_title);
		this.addComponent(cb_tf_text);
		this.addComponent(btn_skip);
		this.addComponent(item_spik);
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
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btn_skip == source){
			  selectQuestion = new SelectQuestion(this.item_spik,editSkipQuestion);
			  this.getApplication().getMainWindow().addWindow(selectQuestion);
		}
	}

	public Label getLb_index() {
		return lb_index;
	}

	public void setLb_index(Label lb_index) {
		this.lb_index = lb_index;
	}

	public TextField getItem_title() {
		return item_title;
	}

	public void setItem_title(TextField item_title) {
		this.item_title = item_title;
	}

	public TextField getItem_spik() {
		return item_spik;
	}

	public void setItem_spik(TextField item_spik) {
		this.item_spik = item_spik;
	}

	public Button getBtn_skip() {
		return btn_skip;
	}

	public void setBtn_skip(Button btn_skip) {
		this.btn_skip = btn_skip;
	}
	
	public void setSkipEnabled(boolean enabled){
		this.btn_skip.setEnabled(enabled);
	}

	public CheckBox getCb_tf_text() {
		return cb_tf_text;
	}

	public void setCb_tf_text(CheckBox cb_tf_text) {
		this.cb_tf_text = cb_tf_text;
	}
	
	public void setCB_Text(boolean tf){
		cb_tf_text.setValue(tf);
	}
}
