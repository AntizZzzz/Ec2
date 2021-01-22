package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceDescriptionService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class CustomerDescriptionView extends VerticalLayout implements ClickListener, ValueChangeListener {

	// 表格显示属性及对应列的名称
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"key", "value"};
	private final String[] COL_HEADERS = new String[] {"描述名称", "描述内容"};
	
	// 右键单击事件
	private final Action ADD = new Action("添加描述", ResourceDataCsr.add_16_ico);
	private final Action EDIT = new Action("修改描述", ResourceDataCsr.edit_16_ico);
	private final Action DELETE = new Action("删除描述", ResourceDataCsr.delete_16_ico);
	
	private Button add;							// Table 的添加按钮
	private Button edit;						// Table 的编辑按钮
	private Button editAll;						// Table 的编辑所有按钮
	private Button delete;						// Table 的删除按钮
	private TextField searchField;				// 搜索描述对象的文本框
	private HorizontalLayout operatorHLayout;	// 存放操作按钮和翻页组件
	
	private Table descriptionTable;				// 描述信息显示表格
	private FlipOverTableComponent<CustomerResourceDescription> descriptionTableFlip;	// 描述信息表格的翻页组件

	private Button save;						// Form 的保存按钮
	private Button cancel;						// Form 的取消
	private Button saveAll;						// Form 的保存按钮
	private Button cancelAll;						// Form 的取消
	private Form descriptionEditor;				// 添加或修改描述信息的表单
	private Window descriptionEditorWindow; 	// 存放编辑或添加描述信息的Form
	private VerticalLayout allDescriptionLayout; 	//所有描述信息的输出
	private List<TextField>descriptionTextFieldList;
	private List<CustomerResourceDescription> descriptionList;
	private Window descriptionAllEditorWindow; 	// 存放编辑或添加描述信息的Form
	private BeanItem<CustomerResourceDescription> descriptionItem;	// 表单的数据源

	private User loginUser;						// 当前登陆用户
	private String searchSql = "";				// 翻页组件和搜索文本框的查询语句
	private String countSql = "";				// 翻页组件和搜索文本框的统计语句
	private CustomerResource customerResource;	// 客户资源对象
	private CustomerResourceDescriptionService descriptionService;	// 描述信息服务类
	
	public CustomerDescriptionView(User loginUser) {
		this.setSpacing(true);
		this.loginUser = loginUser;
//		this.setHeight("168px");
		this.setMargin(true);
		descriptionService = SpringContextHolder.getBean("customerResourceDescriptionService");

		// 创建操作和搜索组件
		createOperatorComponentLayout();
		
		// 创建描述信息显示表格
		createDescriptionTable();
		
		// 创建表格的翻页组件
		createDescriptionTableFlip();
		
		// 创建编辑描述信息组件
		createDescriptionEditorComponent();

		// 创建编辑描述信息组件
		createDescriptionAllEditorComponent();
	}

	private void createOperatorComponentLayout() {
		operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		operatorHLayout.setWidth("100%");
		this.addComponent(operatorHLayout);

		searchField = new TextField();
		searchField.setWidth("140px");
		searchField.setImmediate(true);
		searchField.setStyleName("search");
		searchField.setInputPrompt("请输入描述名称");
		searchField.addListener((ValueChangeListener)this);
		operatorHLayout.addComponent(searchField);
		operatorHLayout.setExpandRatio(searchField, 1);
		operatorHLayout.setComponentAlignment(searchField, Alignment.MIDDLE_LEFT);

		HorizontalLayout rightHLayout = new HorizontalLayout();
		rightHLayout.setSpacing(true);
		operatorHLayout.addComponent(rightHLayout);
		
		add = new Button("添 加", (ClickListener) this);
		add.setStyleName(BaseTheme.BUTTON_LINK);
		rightHLayout.addComponent(add);
		
		edit = new Button("编 辑", (ClickListener) this);
		edit.setStyleName(BaseTheme.BUTTON_LINK);
		rightHLayout.addComponent(edit);
		
		delete = new Button("删 除", (ClickListener) this);
		delete.setStyleName(BaseTheme.BUTTON_LINK);
		rightHLayout.addComponent(delete);
		
		editAll = new Button("编辑全部", (ClickListener) this);
		editAll.setStyleName(BaseTheme.BUTTON_LINK);
		rightHLayout.addComponent(editAll);
		
		setButtonsEnable(false);
	}

	private void createDescriptionTable() {
		descriptionTable = new Table();
		descriptionTable.setWidth("100%");
		descriptionTable.addListener(this);
		descriptionTable.setImmediate(true);
		descriptionTable.setSelectable(true);
		descriptionTable.setStyleName("striped");
		descriptionTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		addActionToDescriptionTable();
		this.addComponent(descriptionTable);
	}
	
	private void createDescriptionTableFlip() {
		descriptionTableFlip = new FlipOverTableComponent<CustomerResourceDescription>(CustomerResourceDescription.class, 
				descriptionService, descriptionTable, searchSql, countSql, null);
		descriptionTable.setVisibleColumns(VISIBLE_PROPERTIES);
		descriptionTable.setColumnHeaders(COL_HEADERS);
		descriptionTable.setPageLength(10);
		descriptionTableFlip.setPageLength(10, false);
		this.addComponent(descriptionTableFlip);
		this.setComponentAlignment(descriptionTableFlip, Alignment.TOP_RIGHT);
	}

	private void createDescriptionEditorComponent() {
		// 创建 描述信息 编辑子窗口
		descriptionEditorWindow = new Window();
		descriptionEditorWindow.setModal(true);
		descriptionEditorWindow.setWidth("300px");
		descriptionEditorWindow.setHeight("180px");
		descriptionEditorWindow.setResizable(false);
		descriptionEditorWindow.center();
		
		// 创建 描述信息 编辑表单
		descriptionEditor = new Form();
		descriptionEditor.setFormFieldFactory(new CustomeFieldFactory());
		descriptionEditor.setItemDataSource(descriptionItem);
		descriptionEditor.setWriteThrough(false);
		descriptionEditor.setInvalidCommitted(false);
		
		// 在表单底部创建保存和取消按钮
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		descriptionEditor.getFooter().addComponent(footerHLayout);
		
		save = new Button("保 存", this);
		save.setStyleName("default");
		save.setImmediate(true);
		cancel = new Button("取 消", this);
		cancel.setImmediate(true);
		footerHLayout.addComponent(save);
		footerHLayout.addComponent(cancel);
	}

	private void createDescriptionAllEditorComponent() {
		// 创建 描述信息 编辑子窗口
		descriptionAllEditorWindow = new Window();
		descriptionAllEditorWindow.setCaption("修改所有描述信息");
		descriptionAllEditorWindow.setModal(true);
		descriptionAllEditorWindow.setWidth("50em");
		descriptionAllEditorWindow.setResizable(false);
		descriptionAllEditorWindow.center();
		
		//创建描述内容编辑组件
		allDescriptionLayout=new VerticalLayout();
		allDescriptionLayout.setSpacing(true);
		descriptionAllEditorWindow.addComponent(allDescriptionLayout);
		
		// 在表单底部创建保存和取消按钮
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		footerHLayout.setMargin(true, false, false, false);
		descriptionAllEditorWindow.addComponent(footerHLayout);
		
		saveAll = new Button("保 存", this);
		saveAll.setStyleName("default");
		saveAll.setImmediate(true);
		cancelAll = new Button("取 消", this);
		cancelAll.setImmediate(true);
		footerHLayout.addComponent(saveAll);
		footerHLayout.addComponent(cancelAll);
	}
	
	/**
	 * 为表格添加右键单击事件监听器
	 */
	private void addActionToDescriptionTable() {
		descriptionTable.addActionHandler(new Action.Handler() {
			public void handleAction(Action action, Object sender, Object target) {
				descriptionTable.select(target);
				if(action == ADD) {
					add.click();
				} else if(action == EDIT) {
					edit.click();
				} else if(action == DELETE){
					delete.click();
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(customerResource != null) {
					if(target != null) {
						return new Action[] {ADD, EDIT, DELETE};
					}
					return new Action[]{ADD};
				}
				return null;
			}
		});
	}
	
	public void setButtonsEnable(boolean enable) {
		add.setEnabled(enable);
		edit.setEnabled(enable);
		delete.setEnabled(enable);
	}
	
	/**
	 * 根据任务表格中指定任务对应的客户对象，回显该客户的所有描述信息
	 */
	public void echoCustomerDescription(CustomerResource customerResource) {
		this.customerResource = customerResource;
		setButtonsEnable(false);
		if(customerResource != null && descriptionTableFlip != null) {
			add.setEnabled(true);
			countSql = "select count(crd) from CustomerResourceDescription as crd where crd.customerResource.id = " + customerResource.getId();
			searchSql = countSql.replaceFirst("count\\(crd\\)", "crd") + " order by crd.lastUpdateDate desc, crd.id desc ";
		} else {
			descriptionTable.getContainerDataSource().removeAllItems();
			this.setCaption("客户的描述信息");
			searchSql = "";
			countSql = "";
		} 
		if(descriptionTableFlip != null) {
			descriptionTableFlip.setSearchSql(searchSql);
			descriptionTableFlip.setCountSql(countSql);
			descriptionTableFlip.refreshToFirstPage();
		}
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == searchField && customerResource != null) {
			String searchStr = searchField.getValue().toString().trim();
			countSql = "select count(crd) from CustomerResourceDescription as crd where crd.customerResource.id = " + customerResource.getId() 
					+ " and crd.key like '%" + searchStr +"%'";
			searchSql = countSql.replaceFirst("count\\(crd\\)", "crd") + " order by crd.lastUpdateDate desc, crd.id desc ";
			descriptionTableFlip.setSearchSql(searchSql);
			descriptionTableFlip.setCountSql(countSql);
			descriptionTableFlip.refreshToFirstPage();
			descriptionTable.select(null);
		} else if(source == descriptionTable) {
			boolean notNull = (descriptionTable.getValue() != null);
			edit.setEnabled(notNull);
			delete.setEnabled(notNull);
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			editCustomerDescription();
		} else if(source == delete) {
			CustomerResourceDescription crd = (CustomerResourceDescription) descriptionTable.getValue();
			if(crd != null) {
				Label label = new Label("您确定要删除描述信息[<b>"+crd.getKey()+"]</b>吗？", Label.CONTENT_XHTML);
				ConfirmWindow confirmWindow = new ConfirmWindow(label, this, "deleteCustomerDescription");
				this.getApplication().getMainWindow().addWindow(confirmWindow);
			}
//			deleteCustomerDescription();
		} else if(source == add) {
			if(customerResource.getId() == null) {
				add.getApplication().getMainWindow().showNotification("请先保存客户资源后，再为其添加描述信息！", Notification.TYPE_WARNING_MESSAGE);
			} else {
				addCustomerDescription();
			}
		} else if(source == save) {
			saveModifyDescription();
		} else if(source == editAll) {
			editAllCustomerDescription();
		}else if(source == saveAll) {
			saveAllCustomerDescription();
			this.getApplication().getMainWindow().removeWindow(descriptionAllEditorWindow);
		}else if(source == cancelAll) {
			this.getApplication().getMainWindow().removeWindow(descriptionAllEditorWindow);
		}else if(source == cancel) {
			descriptionEditor.discard();
			this.getApplication().getMainWindow().removeWindow(descriptionEditorWindow);
		}
	}

	private void addCustomerDescription() {
		descriptionItem = new BeanItem<CustomerResourceDescription>(new CustomerResourceDescription());
		descriptionEditor.setItemDataSource(descriptionItem);
		descriptionEditorWindow.setCaption("添加描述信息");
		descriptionEditorWindow.addComponent(descriptionEditor);
		this.getApplication().getMainWindow().addWindow(descriptionEditorWindow);
	}
	
	@SuppressWarnings("unchecked")
	private void editCustomerDescription() {
		descriptionItem = (BeanItem<CustomerResourceDescription>) descriptionTable.getItem(descriptionTable.getValue());
		descriptionEditor.setItemDataSource(descriptionItem);
		descriptionEditorWindow.setCaption("修改描述信息");
		descriptionEditorWindow.addComponent(descriptionEditor);
		this.getApplication().getMainWindow().addWindow(descriptionEditorWindow);
	}

	@SuppressWarnings("unchecked")
	private void editAllCustomerDescription() {
		descriptionAllEditorWindow.setScrollable(true);
		
		Container beanItemContainer = descriptionTable.getContainerDataSource();
		Collection<CustomerResourceDescription> items = (Collection<CustomerResourceDescription>)beanItemContainer.getItemIds();
		allDescriptionLayout.removeAllComponents();
		descriptionTextFieldList=new ArrayList<TextField>();
		descriptionList=new ArrayList<CustomerResourceDescription>();
		for(CustomerResourceDescription resourceDescription: items){
			HorizontalLayout horizontalLayout=new HorizontalLayout();
			Label caption_lb = new Label(resourceDescription.getKey());
			caption_lb.setWidth("-1px");
			horizontalLayout.setWidth("100%");
			horizontalLayout.setSpacing(true);
			horizontalLayout.addComponent(caption_lb);
			horizontalLayout.setData(resourceDescription);//引用存储在组件中
			TextField textField=new TextField();
			textField.setImmediate(true);
			textField.setWidth("100%");
			textField.setWriteThrough(true);
			textField.setValue(resourceDescription.getValue());
			horizontalLayout.addComponent(textField);
			horizontalLayout.setExpandRatio(textField, 1.0f);
			allDescriptionLayout.addComponent(horizontalLayout);
			descriptionTextFieldList.add(textField);
			descriptionList.add(resourceDescription);
		}
		this.getApplication().getMainWindow().addWindow(descriptionAllEditorWindow);
	}

	/**
	 * 由弹出窗口回调确认删除描述信息
	 */
	@SuppressWarnings("unchecked")
	public void deleteCustomerDescription(Boolean isConfirmed) {
		if(isConfirmed == true) {
			descriptionItem = (BeanItem<CustomerResourceDescription>) descriptionTable.getItem(descriptionTable.getValue());
			descriptionService.deleteById(descriptionItem.getBean().getId());
			descriptionTable.setValue(null);
			descriptionTableFlip.refreshInCurrentPage();
			this.getApplication().getMainWindow().showNotification("删除成功！");
		}
	}

	private void saveModifyDescription() {
		if(!descriptionEditor.isValid()) {
			if(descriptionEditor.getField("key").getValue() == null || descriptionEditor.getField("value").getValue() == null) {
				NotificationUtil.showWarningNotification(CustomerDescriptionView.this, "描述名称和描述内容都不能为空！");
			}
			return;
		}
		descriptionEditor.commit();
		CustomerResourceDescription description = descriptionItem.getBean();
		if(description.getId() != null) {
			descriptionService.update(description);
		} else {
			description.setDomain(loginUser.getDomain());
			description.setCustomerResource(customerResource);
			descriptionService.save(description);
			descriptionTableFlip.refreshToFirstPage();
		}
		this.getApplication().getMainWindow().removeWindow(descriptionEditorWindow);
	}

	private void saveAllCustomerDescription() {
		for(int i=0;i<descriptionTextFieldList.size();i++){
			TextField textField=descriptionTextFieldList.get(i);
			String value=null;
			if(textField!=null){
				value=(String)textField.getValue();
			}
			CustomerResourceDescription description=descriptionList.get(i);
			//都不为空
			if(value!=null&&description!=null){
				description.setValue(value);
				descriptionService.update(description);
			}
		}
		descriptionTableFlip.refreshInCurrentPage();
	}
	
	private class CustomeFieldFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("key".equals(propertyId) ) {
				TextField keyField = new TextField();
				keyField.setRequired(true);
				keyField.setCaption("描述名称：");
				keyField.setNullRepresentation("");
				keyField.addValidator(new RegexpValidator(".*[^ ].*", "描述名不能为全空！"));
				return keyField;
			} else if("value".equals(propertyId)) {
				TextField valueField = new TextField();
				valueField.setRequired(true);
				valueField.setCaption("描述内容：");
				valueField.setNullRepresentation("");
				valueField.addValidator(new RegexpValidator( ".*[^ ].*", "描述内容不能为全空！"));
				return valueField;
			}
			return null;
		}
	}

	public FlipOverTableComponent<CustomerResourceDescription> getDescriptionTableFlip() {
		return descriptionTableFlip;
	}

}
