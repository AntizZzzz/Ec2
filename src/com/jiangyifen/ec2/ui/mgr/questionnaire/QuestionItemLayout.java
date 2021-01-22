package com.jiangyifen.ec2.ui.mgr.questionnaire;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class QuestionItemLayout extends HorizontalLayout implements ClickListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1020686403228512839L;

	private Label lb_index;
	private TextField item_title;
	private TextField item_spik;//item_spik = new TextField();
	private Button btn_skip ;
 	
	public QuestionItemLayout(int itemLength,int indexType){
		this.setWidth("100%");
		this.setSpacing(true);
		 
		String int_index =  findIntIndex(itemLength,indexType);
		lb_index = new Label(int_index);
		this.addComponent(lb_index);
		
		item_title = new TextField();
		item_title.setWidth("440px");
		item_title.setMaxLength(200);
		this.addComponent(item_title);
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
	
	
	
}
