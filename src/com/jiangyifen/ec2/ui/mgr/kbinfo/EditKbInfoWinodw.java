package com.jiangyifen.ec2.ui.mgr.kbinfo;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KbInfo;
import com.jiangyifen.ec2.entity.KbInfoType;
import com.jiangyifen.ec2.service.eaoservice.KbInfoService;
import com.jiangyifen.ec2.service.eaoservice.KbInfoTypeService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.tabsheet.KbInfoManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

/**
 * 编辑 知识窗口
 * @author lxy
 *
 */
public class EditKbInfoWinodw extends Window implements Property.ValueChangeListener, ClickListener{
	
	//静态-非业务
	private static final long serialVersionUID = -2964718416132235880L;	 
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	//静态-业务
	private static final String TREE_PROPERTY_NAME = "NAME";
	private static final int TextArea_Rows = 9;			//内容文本区行数
	private static final int TextArea_Columns = 29;		//内容文本区列数
	//持有UI对象
	private KbInfoManagement kbInfoManagement;
	
	//本控件持有对象
	//private BeanItemContainer<> wContainer;
	private HierarchicalContainer hwContainer;		
	
	//页面布局组件
 	private HorizontalLayout    contextLayout;
  	
	//页面组件
 	private Label lb_msg;
 	private Label lb_kbinfo_select;
	private TextField tf_title;
	private TextArea ta_content;
	private Tree tree;
	private ComboBox cb_level;
	private Button bt_edit_kit;
	
	//业务必须对象
	private Domain domain;
	
	//业务本页对象
 	private KbInfo kbInfo;
	//业务注入对象
	private KbInfoService kbInfoService; 
	private KbInfoTypeService kbInfoTypeService;
	
 	public EditKbInfoWinodw(KbInfoManagement kbInfoManagement){
 		this.kbInfoManagement = kbInfoManagement;
		initThis();
		initSpringContext();
		initCompanent();
	}
 	
 	private void initThis() {
 		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("编辑知识");
		this.setWidth("800px");
		this.setHeight("400px");
		 hwContainer = new HierarchicalContainer();
	}

 	private void initSpringContext() {
 		domain = SpringContextHolder.getDomain();
		kbInfoService = SpringContextHolder.getBean("kbInfoService");
		kbInfoTypeService = SpringContextHolder.getBean("kbInfoTypeService");
	}

	private void initCompanent() {
 		buildContextLayout();
 		buildBottomLayout();
 	}

	private void buildContextLayout(){
		contextLayout = new HorizontalLayout();
		contextLayout.setSizeFull();
		contextLayout.setSpacing(true);
		
		Panel panelTree = new Panel("知识类型");
		panelTree.setHeight("330px");
		tree = new Tree();
		tree.setImmediate(true);
 		tree.setItemCaptionPropertyId(TREE_PROPERTY_NAME);
  		long domainid = domain.getId();
 		List<KbInfoType> list =  kbInfoTypeService.getKbInfoTypeListByDomain(domainid);
  		if(list.size()>0){
 			for (KbInfoType kit : list) {
 				hwContainer.addContainerProperty(TREE_PROPERTY_NAME, String.class, null);
 		 		Item item = hwContainer.addItem(kit.getId());
 		  		item.getItemProperty(TREE_PROPERTY_NAME).setValue(kit.getName());
 		  		hwContainer.setChildrenAllowed(kit.getId(), true);
 		  		initTreeData(kit,domainid);
			}
 		}
  		tree.setContainerDataSource(hwContainer);//ExampleUtil.getHardwareContainer()
 		tree.addListener(this);
  		panelTree.addComponent(tree);
		contextLayout.addComponent(panelTree);
		contextLayout.setExpandRatio(panelTree, 0.4f);
		
		Panel panelForm = new Panel("编辑知识");
		HorizontalLayout hl_show_lb = new HorizontalLayout();
		hl_show_lb.setSpacing(true);
		hl_show_lb.addComponent(new Label("类型:"));
		lb_kbinfo_select = new Label("<b><font color='#3333FF'>根目录</font></b>",Label.CONTENT_XHTML);
		hl_show_lb.addComponent(lb_kbinfo_select);
		panelForm.addComponent(hl_show_lb);
		
		GridLayout gl_form = new GridLayout(2,3);
 		gl_form.setSpacing(true);
		gl_form.addComponent(new Label("标题<font color='red'>*</font>",Label.CONTENT_XHTML));
		tf_title = new TextField();
		tf_title.setMaxLength(200);
		tf_title.setWidth("390px");
		gl_form.addComponent(tf_title);
		panelForm.addComponent(gl_form);
		
		gl_form.addComponent(new Label("内容<font color='red'>*</font>",Label.CONTENT_XHTML));
		ta_content = new TextArea();
		ta_content.setRows(TextArea_Rows);
		ta_content.setColumns(TextArea_Columns);
		ta_content.setMaxLength(10000);
		gl_form.addComponent(ta_content);
		
		gl_form.addComponent(new Label("级别",Label.CONTENT_XHTML));
		HorizontalLayout hl_level = new HorizontalLayout();
		hl_level.setSpacing(true);
		cb_level = new ComboBox();
		for (int i = 1; i < 10; i++) {
			cb_level.addItem(i);
		}
		cb_level.setWidth("80px");
		cb_level.setNullSelectionAllowed(false);
		cb_level.setImmediate(true);
		cb_level.setValue(1);
		hl_level.addComponent(cb_level);
		hl_level.addComponent(new Label("<font color='red'>9级最高,1级最低</font>",Label.CONTENT_XHTML));
		gl_form.addComponent(hl_level);
		
		HorizontalLayout hl_bottom = new HorizontalLayout();
		hl_bottom.setSpacing(true);
		bt_edit_kit = new Button("保存知识",this);
		hl_bottom.addComponent(bt_edit_kit);
		lb_msg = new Label("",Label.CONTENT_XHTML);
		hl_bottom.addComponent(lb_msg);
		panelForm.addComponent(hl_bottom);
		
		contextLayout.addComponent(panelForm);
		contextLayout.setExpandRatio(panelForm, 0.6f);
		
		this.addComponent(contextLayout);
	}
	
	private void initTreeData(KbInfoType infoType,long domainid){
		if(null != infoType){ 
			List<KbInfoType> lsitem = kbInfoTypeService.getKbInfoTypeListByParenetId(infoType.getId(),domainid);
		  	if(lsitem.size()>0){
		 			for (KbInfoType kititem : lsitem) {
		 				Item itemitem = hwContainer.addItem(kititem.getId());
		 				itemitem.getItemProperty(TREE_PROPERTY_NAME).setValue(kititem.getName());
		 				hwContainer.setParent(kititem.getId(), infoType.getId());
		 					int count = kbInfoTypeService.getKbInfoTypeCountByParenetId(kititem.getId(),domainid);
		 					if(count>0){
		 						hwContainer.setChildrenAllowed(kititem.getId(), true);
		 						initTreeData(kititem,domainid);
		 					}else{
		 						hwContainer.setChildrenAllowed(kititem.getId(), false);
		 					}
		 			}
		  	}
		}
	}
	
	public void updateTreeData(){
		hwContainer.removeAllItems();
		long domainid = domain.getId();
 		List<KbInfoType> list =  kbInfoTypeService.getKbInfoTypeListByDomain(domainid);
  		if(list.size()>0){
 			for (KbInfoType kit : list) {
 				hwContainer.addContainerProperty(TREE_PROPERTY_NAME, String.class, null);
 		 		Item item = hwContainer.addItem(kit.getId());
 		  		item.getItemProperty(TREE_PROPERTY_NAME).setValue(kit.getName());
 		  		hwContainer.setChildrenAllowed(kit.getId(), true);
 		  		initTreeData(kit,domainid);
			}
 		}
  		tree.setContainerDataSource(hwContainer);
	}
	
	private void buildBottomLayout() {
		HorizontalLayout hl_show = new HorizontalLayout();
		hl_show.setSpacing(true);
		String showHtml = "<font color='#000000'>提示:1.左边单击选择知识类型，2.双击同一知识类型回到根目录</font>";
		hl_show.addComponent(new Label(showHtml,Label.CONTENT_XHTML));
		this.addComponent(hl_show);
	}
		
	public void refreshComponentInfo(KbInfo kbInfo){
		updateUI(kbInfo);
		updateTreeData();
	}
	
	public void updateUI(KbInfo kbInfo){
		this.lb_msg.setValue("");
		if(null != kbInfo){
			this.kbInfo = kbInfo;
			KbInfoType kit = kbInfo.getKbInfoType();
			String type = "";
			if(null != kit){
				type = kit.getName();
				this.tree.select(kit.getId());
			}else{
				type = "根目录";
				this.tree.select(null);
			}
			this.lb_kbinfo_select.setValue("<b><font color='#3333FF'>"+type+"</font></b>");
			this.tf_title.setValue(kbInfo.getTitle());
			this.ta_content.setValue(kbInfo.getContent());
			this.cb_level.setValue(kbInfo.getLevel());
			
		}else{
			this.lb_msg.setValue("没有知识对象");
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_edit_kit) {
			excuteEditKit();
		}
	}

	private void excuteEditKit(){
		try {
			if (editKitValidateForm()) {
					KbInfoType kit = null;
					if(!WorkUIUtils.stringIsEmpty(this.tree.getValue())){			//校验父类型
						long pid = Long.valueOf(this.tree.getValue().toString());
						kit =  kbInfoTypeService.getKbInfoTypeById(pid);
					}
					List<KbInfo> ls = kbInfoService.getKbInfoCountByTitle(this.tf_title.getValue().toString(),kit,domain.getId());
					if(ls.size() < 1){//再该域下  该类型下 该名称 有多余两条记录
						kbInfo.setTitle(this.tf_title.getValue().toString());
						kbInfo.setContent(this.ta_content.getValue().toString());
						kbInfo.setLastUpdateDate(new Date());
						kbInfo.setKbInfoType(kit); 
						int level = 1;
						if(!WorkUIUtils.stringIsEmpty(this.cb_level.getValue())){
							level = Integer.valueOf(this.cb_level.getValue().toString());
						}
						kbInfo.setLevel(level);
						kbInfoService.updateKbInfo(kbInfo);
 						showWindowMsgInfo("更新成功[" + this.tf_title.getValue()+"]");
						this.getApplication().getMainWindow().removeWindow(this);
						
					}else if(ls.size() == 1){
						if(kbInfo.getId() != null && kbInfo.getId().equals(ls.get(0).getId())){
							kbInfo.setTitle(this.tf_title.getValue().toString());
							kbInfo.setContent(this.ta_content.getValue().toString());
							kbInfo.setLastUpdateDate(new Date());
							kbInfo.setKbInfoType(kit); 
							int level = 1;
							if(!WorkUIUtils.stringIsEmpty(this.cb_level.getValue())){
								level = Integer.valueOf(this.cb_level.getValue().toString());
							}
							kbInfo.setLevel(level);
							kbInfoService.updateKbInfo(kbInfo);
	 						showWindowMsgInfo("更新成功[" + this.tf_title.getValue()+"]");
							this.getApplication().getMainWindow().removeWindow(this);
						}else{
							this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("该类型知识，已经存在，请更改标题"));
						}
					}else{
						this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("该类型知识，已经存在，请更改标题"));
					}
				 
				kbInfoManagement.updateTable(true);
				kbInfoManagement.getTable().setValue(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("更新知识异常_excuteEditKit_LLXXYY", e);
			showWindowMsgInfo("更新异常");
		}
	}
	
	private boolean editKitValidateForm(){
		if(!WorkUIUtils.stringIsEmpty(this.tf_title.getValue())){			//校验父类型{
			if(!WorkUIUtils.stringIsEmpty(this.ta_content.getValue())){			//校验父类型{
				return true;
			}else{
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("内容不能为空,请填写"));	
				return false;
			}
		}else{
			this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("标题不能为空,请填写"));
			return false;
		}
	}
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == tree){					//树形选择时
			if (source.getValue() != null) {
				Item item = hwContainer.getItem(source.getValue());
				String value = "根目录";
				if(null != item){
					value = item.getItemProperty(TREE_PROPERTY_NAME).toString();
					this.lb_kbinfo_select.setValue("<b><font color='#3333FF'>"+value+"</font></b>");
				}
	        } else {
	        	this.lb_kbinfo_select.setValue("<b><font color='#3333FF'>根目录</font></b>"); 
	        }
		}
	}
	/**
	 * 显示
	 * @param msg
	 */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE);
	}
}
