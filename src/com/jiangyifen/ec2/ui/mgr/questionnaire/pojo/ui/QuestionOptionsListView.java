package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.BusinessValidate;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.OptionsListViewInfo;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionOptionsFormValue;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * 问题选项模块,包括该问题的标题，问题的类型的显示，和问题的选项的初始化工作，一个问题一个QuestionOptionsListView对象
 * <br>垂直布局
 * 
 * @author lxy
 *
 */
public class QuestionOptionsListView extends VerticalLayout implements Property.ValueChangeListener{
	
	private static final long serialVersionUID = 8083391311120286713L;
	
	private Label lb_title_index;
	private Label lb_title_text;
	private Label lb_title_info;
	
	private List<HorizontalLayout> hlayoutList = new ArrayList<HorizontalLayout>();
	private List<Label> lb_indexList = new ArrayList<Label>();
	private List<OptionGroup> optionGroupList = new ArrayList<OptionGroup>();
	private List<TextField> tf_textList = new ArrayList<TextField>();
	
	
	private List<QuestionOptions> optionsList;
	private long check_radio_other;
	
	private Question question;
	
	private OptionsListViewInfo optionsListViewInfo;
	
	private QuestionService questionService;
	
	
	/**
	 * 初始化该对象
	 * 问题信息
	 * 问题选项信息
	 * 		选项名称
	 * 		选项文本
	 * 
	 * @param question		问题
	 * @param optionsList	问题选项列表
	 * @param title_index	问题题号
	 */
	public QuestionOptionsListView(Question question,List<QuestionOptions> optionsList,String title_index,OptionsListViewInfo optionsListViewInfo){
		questionService  = SpringContextHolder.getBean("questionService");
		if(null != question){
			this.question = question;
			this.optionsList = optionsList;
			this.optionsListViewInfo = optionsListViewInfo;
			HorizontalLayout titleLayout = new HorizontalLayout();
			lb_title_index = new Label("<b>"+title_index+"</b>",Label.CONTENT_XHTML);
			lb_title_text = new Label("<b>"+question.getTitle()+"</b>",Label.CONTENT_XHTML);
			lb_title_text.setWidth("410px");
			lb_title_info = new Label("["+question.getQuestionType().getName()+"]");
			titleLayout.addComponent(lb_title_index);
			titleLayout.addComponent(lb_title_text);
			titleLayout.addComponent(lb_title_info);
			this.addComponent(titleLayout);
			
			check_radio_other = question.getQuestionType().getId();
			int index_type = 0;			//0：是ABC 1：是123 2：是空
			for (int i = 0; i < optionsList.size(); i++) {
				QuestionOptions questionOptions = optionsList.get(i);
				if(questionOptions != null){
					buildOption(questionOptions,i,index_type,check_radio_other);
				}
			}
		}else{
			this.addComponent(new Label("没有查找到问题,请重试"));
		}
	}
	/**
	 * @param options			问题选项
	 * @param index				索引
	 * @param index_type		索引显示类型  a，b，c 还是1,2,3,还是其他
	 * @param check_radio_other 该选项的所属问题的类型
	 * @return
	 */
	private void buildOption(QuestionOptions options,int index,int index_type,long check_radio_other){
		HorizontalLayout hLayout = new HorizontalLayout();
		Label lb_index = new Label(findIntIndex(index,1));//+":" + options.getId()
		lb_indexList.add(lb_index);
		hLayout.addComponent(lb_index);
		CustomerQuestionOptions selectitem = null;
		if(optionsListViewInfo != null){
			selectitem = questionService.findOptionIsSelect(optionsListViewInfo.getCustomerQuestionnaire().getId(), question.getId(), options.getId());
		}
		if(check_radio_other == 1){//单选选
			List<String> oglist = new ArrayList<String>();
			oglist.add(options.getName());
			OptionGroup optionGroup = new OptionGroup(null,oglist);
			optionGroup.setNullSelectionAllowed(true);
 			optionGroup.setData(options);
 			if(selectitem != null){
 				optionGroup.setValue(options.getName());
 			}
 			optionGroup.addStyleName("myopacity");
	 		optionGroup.setImmediate(true);
	 		optionGroup.addListener(this);
	 		optionGroupList.add(optionGroup);
	 		hLayout.addComponent(optionGroup);
		}
		if(check_radio_other == 2){//多选
			List<String> oglist = new ArrayList<String>();
			oglist.add(options.getName());
			OptionGroup optionGroup = new OptionGroup(null,oglist);
			optionGroup.setNullSelectionAllowed(true);
			optionGroup.setMultiSelect(true);
			optionGroup.addStyleName("myopacity");
	 		optionGroup.setImmediate(true);
	 		if(selectitem != null){
	 			Set<String> optionsSet = new HashSet<String>();
	 			optionsSet.add(options.getName());
	 			optionGroup.setValue(optionsSet);
	 		}
			optionGroup.setData(options);
	 		optionGroup.addListener(this);
	 		optionGroupList.add(optionGroup);
	 		hLayout.addComponent(optionGroup);
		}else if(check_radio_other == 3){
			hLayout.addComponent(new Label(options.getName()));
		}
		
 		TextField tf_text = new TextField();
 		tf_text.setWidth("420px");
 		tf_text.setMaxLength(2000);
 		if(selectitem != null){
 			String tx = selectitem.getText();
 			if(null != tx && tx.trim().length()>0){
 				tf_text.setValue(tx);
 			}
 		} 
		if("TT".equals(options.getType())){
			tf_text.setVisible(true);
		}else{
			tf_text.setVisible(false);
		}
		tf_textList.add(tf_text);
		hLayout.addComponent(tf_text);
		
		hlayoutList.add(hLayout);
		this.addComponent(hLayout);
	}
	
	/**获得该问题的选项前标题  ABC 123 */
	private String findIntIndex(int itemLength, int indexType) {
		String rtn_index = "";
		if(0 == indexType){
			rtn_index = (itemLength+1) + ".";
		}else{
			rtn_index = (char)(itemLength+65) + ".";
		}
		return rtn_index;
	}
	
	/**当问题为单选时，只能有一个选项选中 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();//List<OptionGroup> optionGroupList
		if(check_radio_other == 1){
			for (int i = 0; i < optionGroupList.size(); i++) {
				optionGroupList.get(i).removeListener(this);
			}
			for (int i = 0; i < optionGroupList.size(); i++) {
				OptionGroup og = optionGroupList.get(i);
				String value = optionsList.get(i).getName();
				if(source == og){
	  				og.setValue(value);
				} else {
					og.setValue(null);
				}
			}
			for (int i = 0; i < optionGroupList.size(); i++) {
				optionGroupList.get(i).addListener(this);
			}
		}
	}
	
	/**
	 * 校验表单的数据
	 * 
	 * @return 校验对象包括是否通过校验标示
	 * 和校验返回的文本信息
	 * 
	 */
	public BusinessValidate validateFormValue() {
		BusinessValidate businessValidate = new BusinessValidate();
		businessValidate.setBtf(false);
		if (check_radio_other == 1){								//单选
			if(optionsList.size() == optionGroupList.size()){
				boolean tf = false;
				for (int i = 0; i < optionsList.size(); i++) {		//遍历问题选项
					OptionGroup og = optionGroupList.get(i);
					Object selectValue = og.getValue();
					String sltv = (String) selectValue;
					if(null != sltv){
						tf = true;
					}
				}
				if(tf){
					businessValidate.setBtf(true);	//OK通过
				}else{
					businessValidate.setBtf(false);	//没有选中
					businessValidate.setBmsg("必须选中一项");
				}
			}else{
				businessValidate.setBtf(false);						//选项和点选框个数不对应
				businessValidate.setBmsg("选项和单选框个数不对应");
			}
		}else if(check_radio_other == 2){				 			//多选
			if(optionsList.size() == optionGroupList.size()){
				boolean tf = false;
				for (int i = 0; i < optionsList.size(); i++) {		//遍历问题选项
					OptionGroup og = optionGroupList.get(i);
					Object selectValue = og.getValue();
					@SuppressWarnings("unchecked")
					Set<String> sets = (Set<String>) selectValue;
					if(null != sets && sets.size() > 0){
						tf = true;
					}
				}
				if(tf){
					businessValidate.setBtf(true);	//OK通过
				}else{
					businessValidate.setBtf(false);	//没有选中
					businessValidate.setBmsg("必须选中一项");
				}
			}else{
				businessValidate.setBtf(false);						//选项和点选框个数不对应
				businessValidate.setBmsg("选项和多选框个数不对应");
			}
		}else {
			if(optionsList.size() == tf_textList.size()){
				boolean tf = false;
				for (int i = 0; i < optionsList.size(); i++) {		//遍历问题选项
						String textVlue = (String) tf_textList.get(i).getValue();
						if(textVlue != null && textVlue.trim().length() > 0){
							tf = true;
						}
				}
				if(tf){
					businessValidate.setBtf(true);		//OK通过
				}else{
					businessValidate.setBtf(false);		//必须填写一个
					businessValidate.setBmsg("必须填写一个");
				}
			}else{
				businessValidate.setBtf(false);
				businessValidate.setBmsg("选项和文本框个数不对应");
			}
		}
		return businessValidate;
	}
	
	/**
	 * 
	 * 获得表单的数据
	 * 
	 * @return
	 */
	public List<QuestionOptionsFormValue> getFormValue() {
		List<QuestionOptionsFormValue> rt = new ArrayList<QuestionOptionsFormValue>();
		QuestionOptionsFormValue qofv = null;
		for (int i = 0; i < optionsList.size(); i++) {			//遍历问题选项
			boolean findValue = false;							//标示可以获得该值
			if (check_radio_other == 1){						//单选
				OptionGroup og = optionGroupList.get(i);
				Object selectValue = og.getValue();
				String sltv = (String) selectValue;
				if(null != sltv){
					findValue = true;
				}
			}else if(check_radio_other == 2){
				OptionGroup og = optionGroupList.get(i);
				Object selectValue = og.getValue();
				@SuppressWarnings("unchecked")
				Set<String> sets = (Set<String>) selectValue;	//多选
				if(sets.size() > 0){
					findValue = true;
				}
			}else{												//文本
					findValue = true;
			}
			if(findValue){
				qofv = new QuestionOptionsFormValue();
				qofv.setOptions(optionsList.get(i));
				String textVlue = (String) tf_textList.get(i).getValue();
				if(textVlue != null && textVlue.trim().length() > 0){
					qofv.setTextTF(true);
					qofv.setOptionText(textVlue);
				}else{
					qofv.setTextTF(false);
				}
				rt.add(qofv);
			}
		}
		return rt;
	}
	public Question getQuestion() {
		return question;
	}
	public void setQuestion(Question question) {
		this.question = question;
	}
	
	 
	
}
