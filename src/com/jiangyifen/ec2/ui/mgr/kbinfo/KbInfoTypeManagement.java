package com.jiangyifen.ec2.ui.mgr.kbinfo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;

/**
 * 知识类型管理
 * @author lxy
 *
 */
public class KbInfoTypeManagement extends Window implements Property.ValueChangeListener, ClickListener,Window.CloseListener{

	//静态-非业务
	private static final long serialVersionUID = -4491867389979831407L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	//静态-业务
	private static final String TREE_PROPERTY_NAME = "NAME";
	//持有UI对象
	private KbInfoManagement kbInfoManagement;
	private AddKbInfoTypeWinodw addKbInfoTypeWinodw;
	private EditKbInfoTypeWinodw editKbInfoTypeWinodw;
	//本控件持有对象
 	private HierarchicalContainer hwContainer;		
	
	//页面布局组件
 	private HorizontalLayout    contextLayout;
  	
	//页面组件
 	private Label lb_kbinfo_select;			//选中知识类型
 	private Button bt_add;					//添加
 	private Button bt_edit;					//保存修改
	private Button bt_delete;				//删除
	private Button bt_reload;				//重新载入
 	private Label lb_msg;
 	private Tree tree;
	
	//业务必须对象
	private Domain domain;
 	
	//业务本页对象
	
	//业务注入对象
	private KbInfoService kbInfoService; 
	private KbInfoTypeService kbInfoTypeService;
	
 	public KbInfoTypeManagement(KbInfoManagement kbInfoManagement){
 		this.kbInfoManagement = kbInfoManagement;
		initThis();
		initSpringContext();
		initCompanent();
	}
 	
 	private void initThis() {
 		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.addListener((CloseListener) this);
		this.setCaption("知识类型管理");
		this.setWidth("600px");
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
 		 buildMainBottomLayout();
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
		contextLayout.setExpandRatio(panelTree, 0.5f);
		
		Panel panelForm = new Panel("操作");
		panelForm.setHeight("330px");
		
		HorizontalLayout hl_show_lb = new HorizontalLayout();
		hl_show_lb.setSpacing(true);
		hl_show_lb.setMargin(false,false,true,false);
		hl_show_lb.addComponent(new Label("类型:"));
		lb_kbinfo_select = new Label("<b><font color='#3333FF'>根目录</font></b>",Label.CONTENT_XHTML);
		lb_kbinfo_select.setWidth("250px");
		hl_show_lb.addComponent(lb_kbinfo_select);
		panelForm.addComponent(hl_show_lb);
		
		HorizontalLayout hl_add = new HorizontalLayout();
		hl_add.setSpacing(true);
		hl_add.setMargin(false,false,true,false);
		/*tf_add_type = new TextField();
		hl_add.addComponent(tf_add_type);*/
		bt_add = new Button("添加类型",this);
		hl_add.addComponent(bt_add);
		panelForm.addComponent(hl_add);
		
		HorizontalLayout hl_edit = new HorizontalLayout();
		hl_edit.setSpacing(true);
		hl_edit.setMargin(false,false,true,false);
		/*tf_edit_type = new TextField();
		hl_edit.addComponent(tf_edit_type);*/
		bt_edit = new Button("编辑类型",this);
		hl_edit.addComponent(bt_edit);
		panelForm.addComponent(hl_edit);
		
		HorizontalLayout hl_delete = new HorizontalLayout();
		hl_delete.setSpacing(true);
		hl_delete.setMargin(false,false,true,false);
 		bt_delete = new Button("删除类型",this);
 		hl_delete.addComponent(bt_delete);
 		panelForm.addComponent(hl_delete);
 		
 		HorizontalLayout hl_reload = new HorizontalLayout();
 		hl_reload.setSpacing(true);
  		bt_reload = new Button("重新加载",this);
 		hl_reload.addComponent(bt_reload);
		panelForm.addComponent(hl_reload);
		
		HorizontalLayout hl_info = new HorizontalLayout();
		hl_info.setSpacing(true);
		hl_info.setMargin(true);
		lb_msg = new Label("",Label.CONTENT_XHTML);
		hl_info.addComponent(lb_msg);
		panelForm.addComponent(hl_info);
		
		contextLayout.addComponent(panelForm);
		contextLayout.setExpandRatio(panelForm, 0.5f);
		
		this.addComponent(contextLayout);
	}
	/**在该域下加载知识类型树**/
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
	
	/**更新数数据**/
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
	
	/*初始化下边提示信息*/
	private void buildMainBottomLayout() {
		HorizontalLayout hl_show = new HorizontalLayout();
		hl_show.setSpacing(true);
		String showHtml = "";//<font color='#000000'>提示:1.如果没有选中左边知识类型，或是没有类型可选，则默认添加至根目录。2.如选中类型，则添加至该类型下。</font>";
		hl_show.addComponent(new Label(showHtml,Label.CONTENT_XHTML));
		this.addComponent(hl_show);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_add) {
			if(null == addKbInfoTypeWinodw){
				addKbInfoTypeWinodw = new AddKbInfoTypeWinodw(this);
			}
			KbInfoType kbInfoType = null;
			if(!WorkUIUtils.stringIsEmpty(tree.getValue())){			//校验父类型
				long pid = Long.valueOf(tree.getValue().toString());
				kbInfoType =  kbInfoTypeService.getKbInfoTypeById(pid);
			}else{
				kbInfoType = null;
			}
			addKbInfoTypeWinodw.refreshComponentInfo(kbInfoType);
			this.getApplication().getMainWindow().addWindow(addKbInfoTypeWinodw);
		}else if(source == bt_edit) {
			if(null == editKbInfoTypeWinodw){
				editKbInfoTypeWinodw = new EditKbInfoTypeWinodw(this);
			}
			KbInfoType kbInfoType = null;
			if(!WorkUIUtils.stringIsEmpty(tree.getValue())){			//校验父类型
				long pid = Long.valueOf(tree.getValue().toString());
				kbInfoType =  kbInfoTypeService.getKbInfoTypeById(pid);
				editKbInfoTypeWinodw.refreshComponentInfo(kbInfoType);
				this.getApplication().getMainWindow().addWindow(editKbInfoTypeWinodw);
			}else{
				kbInfoType = null;
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("根目录不能编辑"));
			}
			
		}else if(source == bt_delete) {
			excuteDeleteType();
		}else if(source == bt_reload){
			this.tree.select(null);
			this.lb_msg.setValue("");
			updateTreeData();
		}
	}

	
	
	
	/**删除类型**/
	private void excuteDeleteType(){
		try {
			if(deleteTypeValidateForm()){
				long id = Long.valueOf(tree.getValue().toString());
				KbInfoType kit =  kbInfoTypeService.getKbInfoTypeById(id);
				if(null != kit){
					int count = kbInfoService.getKbInfoCountByTypeId(id);
					if(count > 0 ){
						this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("该类型下存在["+count+"]条知识,不能删除<br>请对类型下知识处理后再删除。"));
						updateTreeData();
					}else{
						kbInfoService.deleteKbInfoTypeByTypeId(id);
						this.lb_kbinfo_select.setValue("<b><font color='#3333FF'>根目录</font></b>");
						this.tree.select(null);
						updateTreeData();
						this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("删除成功"));
					}
				}else{
					this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("不存在该类型"));
					updateTreeData();
				}
 			}
		} catch (Exception e) {
			e.printStackTrace();
			this.lb_kbinfo_select.setValue("<b><font color='#3333FF'>根目录</font></b>");
			this.tree.select(null);
			logger.error("删除知识类型异常_excuteAddType_LLXXYY", e);
			this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("删除异常"));
		}
	}
	
	/**校验删除类型**/
	private boolean deleteTypeValidateForm(){
		long selecttype = -1;
		if(!WorkUIUtils.stringIsEmpty(tree.getValue())){			//校验父类型
			selecttype = Long.valueOf(tree.getValue().toString());
			if(selecttype > -1){
					return true;
			}else{
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("未选中类型，请在左边选中类型"));
				return false;
			}
		}else{
			this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("未选中类型，请在左边选中类型"));
			return false;
		}
	}
	
	public void updateTreeDataAndParentUi(){
		this.updateTreeData();
		this.tree.select(null);
		this.lb_kbinfo_select.setValue("<b><font color='#3333FF'>根目录</font></b>"); 
		kbInfoManagement.updateTreeData();
	}
	
	/**
	 * 刷新页面组件
	 */
	public void refreshComponentInfo(){
		this.lb_msg.setValue("");
		this.tree.setValue(null);
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
			this.lb_msg.setValue("");
		}
	}

	@Override
	public void windowClose(CloseEvent e) {
 		kbInfoManagement.updateTreeData();
	}
	//----------------------------业务通用类
	
}
