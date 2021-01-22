package com.jiangyifen.ec2.ui.mgr.kbinfo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KbInfoType;
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
 * 选择知识类型窗口
 * @author lxy
 *
 */
public class ChoosekbInfoTypeWindow extends Window implements Property.ValueChangeListener, ClickListener{

	//静态-非业务
	private static final long serialVersionUID = 2770669802072048064L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	//静态-业务
	private static final String TREE_PROPERTY_NAME = "NAME";
	//持有UI对象
	private KbInfoManagement kbInfoManagement;
	private ShowKbInfoView showKbInfoView;
	//本控件持有对象
	//private BeanItemContainer<> wContainer;
	private HierarchicalContainer hwContainer;
	
	//页面布局组件
 	private HorizontalLayout    contextLayout;
  	
	//页面组件
 	private Label lb_kbinfo_choose;			//选中知识类型
  	private Button bt_choose;				 
 	private Label lb_msg;
 	private Tree tree;
	
	
	//业务必须对象
	private Domain domain;
	
	//业务本页对象
	
	//业务注入对象
 	private KbInfoTypeService kbInfoTypeService;
	
 	public ChoosekbInfoTypeWindow(KbInfoManagement kbInfoManagement,ShowKbInfoView showKbInfoView){
 		this.kbInfoManagement = kbInfoManagement;
 		this.showKbInfoView = showKbInfoView;
		initThis();
		initSpringContext();
		initCompanent();
	}
 	
	private void initThis() {
 		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("知识类型管理");
		this.setWidth("300px");
		this.setHeight("450px");
		hwContainer = new HierarchicalContainer();
	}

 	private void initSpringContext() {
 		domain = SpringContextHolder.getDomain();
 		kbInfoTypeService = SpringContextHolder.getBean("kbInfoTypeService");
	}

	private void initCompanent() {
 		 buildContextLayout();
 		 buildInfoLayout();
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
	
	private void updateTreeData(){
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

	private void buildInfoLayout() {
		HorizontalLayout hl_show_move = new HorizontalLayout();
		hl_show_move.setSpacing(true);
		hl_show_move.setMargin(false);
		hl_show_move.addComponent(new Label("选中:"));
		lb_kbinfo_choose= new Label("<b><font color='#3333FF'>根目录</font></b>",Label.CONTENT_XHTML);
		hl_show_move.addComponent(lb_kbinfo_choose);
		this.addComponent(hl_show_move);
	 
	}
	
	private void buildBottomLayout() {
		HorizontalLayout hl_bottom = new HorizontalLayout();
		hl_bottom.setSpacing(true);
		hl_bottom.setMargin(false);
		bt_choose  = new Button("确定选中",this);
		hl_bottom.addComponent(bt_choose);
		lb_msg = new Label("<font color='#000000'>提示:1.右边单击选择知识类型，<br>2.双击同一知识类型回到根目录</font>",Label.CONTENT_XHTML);
		lb_msg.setWidth("200px");
		hl_bottom.addComponent(lb_msg);
		this.addComponent(hl_bottom);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_choose) {
			excuteChoose();
		} 
	}

	private void excuteChoose() {
		try {
				if(!WorkUIUtils.stringIsEmpty(tree.getValue())){
					long id = Long.valueOf(tree.getValue().toString());
					KbInfoType kit =  kbInfoTypeService.getKbInfoTypeById(id);
					if(null != kit){
	 					 if(null != kbInfoManagement){
	 						//kbInfoManagement.updateTypeUI(kit);
	 					 }
	 					 if(null != showKbInfoView){
	 						//showKbInfoView.updateTypeUI(kit);
	 					 }
	 					this.getApplication().getMainWindow().removeWindow(this);
					}else{
						this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("知识类型不存在，请重选"));
					}
				}else{
					if(null != kbInfoManagement){
 						//kbInfoManagement.updateTypeUI(null);
  					 }
 					 if(null != showKbInfoView){
 						//showKbInfoView.updateTypeUI(null);
 					 }
					this.getApplication().getMainWindow().removeWindow(this);
				}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("选择知识异常_excuteChoose_LLXXYY", e);
			this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("选择异常"));
		}
	}
 
	/**
	 * 刷新页面
	 */
	public void refreshComponentInfo(){
		updateTreeData();
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
					this.lb_kbinfo_choose.setValue("<b><font color='#3333FF'>"+value+"</font></b>");
				}
	        }else{
	        	this.lb_kbinfo_choose.setValue("<b><font color='#3333FF'>根目录</font></b>");
	        }
		}
	}
	
}