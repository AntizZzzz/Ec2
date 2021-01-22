package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.util.Arrays;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.AddressService;
import com.jiangyifen.ec2.service.eaoservice.CityService;
import com.jiangyifen.ec2.service.eaoservice.CountyService;
import com.jiangyifen.ec2.service.eaoservice.ProvinceService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class CustomerAddressView extends VerticalLayout implements ValueChangeListener, ClickListener {	

	// 表格显示属性及对应列的名称
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"province", "city", "county", "street", "postCode"};
	private final String[] COL_HEADERS = new String[] {"省", "市", "县/区", "街道", "邮编"};
	
	// 右键单击事件
	private final Action ADD = new Action("添加地址", ResourceDataCsr.add_16_ico);
	private final Action EDIT = new Action("修改地址", ResourceDataCsr.edit_16_ico);
	private final Action DELETE = new Action("删除地址", ResourceDataCsr.delete_16_ico);

	private Button add;							// 添加按钮
	private Button edit;						// 编辑按钮
	private Button delete;						// 删除按钮
	private TextField searchField;				// 地址搜索文本框
	private HorizontalLayout operatorHLayout;	// 存放操作按钮和翻页组件
	
	private Table addressTable;					// 地址信息显示表格
	private FlipOverTableComponent<Address> addressTableFlip;	// 地址信息表格的翻页组件

	private Button save;						// Form 的保存按钮
	private Button cancel;						// Form 的取消
    private ComboBox provinceSelector;			// 地址 省份
    private ComboBox citySelector;				// 地址 城市
    private ComboBox countySelector;			// 地址 区县
    private TextArea streetArea;				// 地址 街道
    private TextField postCodeField;			// 邮政编码输入框
	private Form addressEditor;					// 添加或修改地址信息的表单
	private Window addressEditorWindow; 		// 存放编辑或添加地址信息的Form
	private BeanItem<Address> addressItem;		// 表单的数据源
	
	private String searchSql = "";				// 翻页组件和搜索文本框的查询语句
	private String countSql = "";				// 翻页组件和搜索文本框的统计语句
	private CustomerResource customerResource;	// 客户资源对象
	private AddressService addressService;		// 地址服务类
	private ProvinceService provinceService;	// 省份服务类
	private CityService cityService;			// 城市服务类
	private CountyService countyService;		// 县区服务类
	
	private BeanItemContainer<Province> provinceContainer;
	private BeanItemContainer<City> cityContainer;
	private BeanItemContainer<County> countyContainer;
	
	
	public CustomerAddressView() {
		// 初始化参数
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
//		this.setHeight("168px");
		this.setMargin(true);
		
		addressService = SpringContextHolder.getBean("addressService");	
		provinceService = SpringContextHolder.getBean("provinceService");	
		cityService = SpringContextHolder.getBean("cityService");	
		countyService = SpringContextHolder.getBean("countyService");	

		provinceContainer = new BeanItemContainer<Province>(Province.class);
		provinceContainer.addAll(provinceService.getAll());
		cityContainer = new BeanItemContainer<City>(City.class);
		countyContainer = new BeanItemContainer<County>(County.class);
		
		// 创建操作和搜索组件
		createOperatorComponentLayout();
		
		// 创建地址信息显示表格
		createAddressTable();

		// 创建表格的翻页组件
		createAddressTableFlip();
		
		// 创建编辑地址信息的相关组件
		createAddressEditorComponent();
	}

	/**
	 * 创建操作和搜索组件
	 */
	private void createOperatorComponentLayout() {
		operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		operatorHLayout.setWidth("100%");
		this.addComponent(operatorHLayout);

		searchField = new TextField();
		searchField.setWidth("140px");
		searchField.addListener(this);
		searchField.setImmediate(true);
		searchField.setStyleName("search");
		searchField.setInputPrompt("请输入关键词");
		searchField.setDescription("可按省份、城市、县区名称搜索");
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
		setButtonsEnable(false);
	}

	/**
	 * 设置按钮的可用状态
	 * @param enable 是否可用
	 */
	public void setButtonsEnable(boolean enable) {
		add.setEnabled(enable);
		edit.setEnabled(enable);
		delete.setEnabled(enable);
	}
	
	/**
	 * 创建地址信息显示表格
	 */
	private void createAddressTable() {
		addressTable = new Table();
		addressTable.setWidth("100%");
		addressTable.setHeight("-1px");
		addressTable.addListener(this);
		addressTable.setImmediate(true);
		addressTable.setSelectable(true);
		addressTable.setStyleName("striped");
		addressTable.setNullSelectionAllowed(false);
		addressTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		addActionToAddressTable();
		this.addComponent(addressTable);
	}

	/**
	 * 为地址表格添加右键单击事件
	 */
	private void addActionToAddressTable() {
		// 为表格添加右键单击事件监听器
		addressTable.addActionHandler(new Action.Handler() {
			public void handleAction(Action action, Object sender, Object target) {
				addressTable.select(target);
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
	
	/**
	 * 创建表格的翻页组件
	 */
	private void createAddressTableFlip() {
		addressTableFlip = new FlipOverTableComponent<Address>(Address.class, addressService, 
				addressTable, searchSql, countSql, null);
		addressTable.setVisibleColumns(VISIBLE_PROPERTIES);
		addressTable.setColumnHeaders(COL_HEADERS);
		addressTable.setPageLength(10);
		addressTableFlip.setPageLength(10, false);
		this.addComponent(addressTableFlip);
		this.setComponentAlignment(addressTableFlip, Alignment.TOP_RIGHT);
	}
	
	/**
	 * 创建编辑地址信息的相关组件
	 */
	private void createAddressEditorComponent() {
		// 创建地址编辑子窗口
		addressEditorWindow = new Window();
		addressEditorWindow.setModal(true);
		addressEditorWindow.setWidth("300px");
		addressEditorWindow.setHeight("280px");
		addressEditorWindow.setResizable(false);
		addressEditorWindow.center();
		
		// 创建 省份 选择框
     	provinceSelector = new ComboBox();
     	provinceSelector.setImmediate(true);
     	provinceSelector.setCaption("省份：");
     	provinceSelector.setWidth("198px");
     	provinceSelector.setInputPrompt(" 请选择省 ");
     	provinceSelector.setNullSelectionAllowed(false);
     	provinceSelector.setContainerDataSource(provinceContainer);
     	
		// 创建 城市 选择框
     	citySelector = new ComboBox();
     	citySelector.setImmediate(true);
     	citySelector.setCaption("城市：");
     	citySelector.setWidth("198px");
     	citySelector.setInputPrompt(" 请选择城市 ");
     	citySelector.setNullSelectionAllowed(false);
     	citySelector.setContainerDataSource(cityContainer);

		// 创建 县区 选择框
     	countySelector = new ComboBox();
     	countySelector.setImmediate(true);
    	countySelector.setCaption("县/区：");
     	countySelector.setWidth("198px");
     	countySelector.setInputPrompt(" 请选择区县 ");
     	countySelector.setNullSelectionAllowed(false);
     	countySelector.setContainerDataSource(countyContainer);
     	
		// 创建 街道 输入区域
		streetArea = new TextArea();
		streetArea.setWidth("198px");
		streetArea.setRows(2);
		streetArea.setCaption("街道：");
		streetArea.setNullRepresentation("");
     	
		// 创建 邮政编码 输入框
		postCodeField = new TextField();
		postCodeField.setWidth("198px");
		postCodeField.setCaption("邮编：");
		postCodeField.setNullRepresentation("");
		postCodeField.addValidator(new RegexpValidator("\\d+", "邮编只能是数字的组合"));
		
		// 创建 地址 编辑表单
		addressEditor = new Form();
		addressEditor.setFormFieldFactory(new CustomeFieldFactory());
		addressEditor.setWriteThrough(false);
		addressEditor.setInvalidCommitted(false);

		// 在表单底部创建保存和取消按钮
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		addressEditor.getFooter().addComponent(footerHLayout);
		
		save = new Button("保 存", this);
		save.setStyleName("default");
		save.setImmediate(true);
		cancel = new Button("取 消", this);
		cancel.setImmediate(true);
		footerHLayout.addComponent(save);
		footerHLayout.addComponent(cancel);
		
		// 为 省份 选择框添加监听器
		provinceSelector.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				if(provinceSelector.getValue() == null) {
					citySelector.setReadOnly(true);
					countySelector.setReadOnly(true);
				} else {
					citySelector.setReadOnly(false);
					citySelector.removeAllItems();
     				cityContainer.addAll(cityService.getAllByProvince((Province)provinceSelector.getValue()));
     				citySelector.setContainerDataSource(cityContainer);
     				
     				countySelector.setReadOnly(false);
     				countySelector.removeAllItems();
     				countySelector.setReadOnly(true);
				}
			}
		});
     	
		// 为 城市 选择框添加监听器
     	citySelector.addListener(new ValueChangeListener() {
     		@Override
     		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
     			if(citySelector.getValue() == null) {
     				countySelector.setReadOnly(true);
     			} else {
     				countySelector.setReadOnly(false);
     				countySelector.removeAllItems();
     				countyContainer.addAll(countyService.getAllByCity((City)citySelector.getValue()));
     				countySelector.setContainerDataSource(countyContainer);
     			}
     		}
     	});
	}

	/**
	 * 自定义表单内各组件的生成器
	 */
	private class CustomeFieldFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("province".equals(propertyId) ) {
				return provinceSelector;
			} else if("city".equals(propertyId)) {
				return citySelector;
			} else if("county".equals(propertyId)) {
				return countySelector;
			} else if("street".equals(propertyId)) {		     	
				return streetArea;
			} else if("postCode".equals(propertyId)) {		     	
				return postCodeField;
			}
			return null;
		}
	}

	/**
	 * 根据左侧任务表格的选中项，回显地址信息
	 */
	public void echoCustomerAddress(CustomerResource customerResource) {
		this.customerResource = customerResource;
		setButtonsEnable(false);
		if(customerResource != null && addressTableFlip != null) {
			add.setEnabled(true);
			countSql = "select count(a) from Address as a where a.customerResource.id = " + customerResource.getId();
			searchSql = countSql.replaceFirst("count\\(a\\)", "a") + " order by a.id desc ";
		} else {
			addressTable.getContainerDataSource().removeAllItems();
			this.setCaption("客户地址信息");
			searchSql = "";
			countSql = "";
		}
		if(addressTableFlip != null) {
			addressTableFlip.setSearchSql(searchSql);
			addressTableFlip.setCountSql(countSql);
			addressTableFlip.refreshToFirstPage();
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == searchField && customerResource != null) {
			String searchStr = searchField.getValue().toString().trim();
			countSql = "select count(a) from Address as a where a.customerResource.id = " + customerResource.getId() 
					+ " and ( a.province.name like '%" + searchStr +"%'"
					+ " or a.city.name like '%" + searchStr +"%'"
					+ " or a.county.name like '%" + searchStr +"%' )";
			searchSql = countSql.replaceFirst("count\\(a\\)", "a") + " order by a.id desc ";
			addressTableFlip.setSearchSql(searchSql);
			addressTableFlip.setCountSql(countSql);
			addressTableFlip.refreshToFirstPage();
		} else if(source == addressTable) {
			boolean notNull = (addressTable.getValue() != null);
			edit.setEnabled(notNull);
			delete.setEnabled(notNull);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			editCustomerAddress();
		} else if(source == delete) {
			Address address = (Address) addressTable.getValue();
			if(address != null) {
				Label label = new Label("您确定要删除客户地址信息[<b>"+address.getStreet()+"]</b>吗？", Label.CONTENT_XHTML);
				ConfirmWindow confirmWindow = new ConfirmWindow(label, this, "deleteCustomerAddress");
				this.getApplication().getMainWindow().addWindow(confirmWindow);
			}
//			deleteCustomerAddress();
		} else if(source == add) {
			if(customerResource.getId() == null) {
				add.getApplication().getMainWindow().showNotification("请先保存客户资源后，再为其添加地址信息！", Notification.TYPE_WARNING_MESSAGE);
			} else {
				addCustomerAddress();
			}
		} else if(source == save) {
			saveModifyAddress();
		} else if(source == cancel) {
			addressEditor.discard();
			this.getApplication().getMainWindow().removeWindow(addressEditorWindow);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void editCustomerAddress() {
		addressItem = (BeanItem<Address>) addressTable.getItem(addressTable.getValue());
		addressEditor.setItemDataSource(addressItem, Arrays.asList(VISIBLE_PROPERTIES));
		addressEditorWindow.setCaption("修改地址信息");
		addressEditorWindow.addComponent(addressEditor);
		this.getApplication().getMainWindow().addWindow(addressEditorWindow);
		
		// 根据表格中的值回显Form表单中的地址信息
		echoAddressInForm();
	}
	
	/**
	 * 根据表格中的值回显Form表单中的地址信息
	 */
	private void echoAddressInForm() {
		Address currentAddress = addressItem.getBean();
		Province currentProvince = currentAddress.getProvince();
		City currentCity = currentAddress.getCity();
		County currentCounty = currentAddress.getCounty();
		// 回显信息
		if(currentProvince != null) {
			for(Province province : provinceContainer.getItemIds()) {	// 回显省
				if(province.getId() != null && province.getId().equals(currentAddress.getProvince().getId())) {
					provinceSelector.setValue(province);	break;
				}
			}
			
			if(currentCity != null) {		// 只有在省不为空的情况下，才有回显市的需要
				for(City city : cityContainer.getItemIds()) {
					if(city.getId() != null && city.getId().equals(currentAddress.getCity().getId())) {
						citySelector.setValue(city);	break;
					}
				}
				
				if(currentCounty != null) {		// 只有在省和市都不为空的情况下，才有回显县区的需要
					for(County county : countyContainer.getItemIds()) {
						if(county.getId() != null && county.getId().equals(currentAddress.getCounty().getId())) {
							countySelector.setValue(county);	break;
						}
					}
				}
			}
		}
		// 设置组件的只读属性
		citySelector.setReadOnly(currentProvince != null);
		countySelector.setReadOnly(currentCity != null);
	}

	/**
	 * 由弹出窗口回调确认删除客户地址信息
	 */
	@SuppressWarnings("unchecked")
	public void deleteCustomerAddress(Boolean isConfirmed) {
		if(isConfirmed == true) {
			try {
				addressItem = (BeanItem<Address>) addressTable.getItem(addressTable.getValue());
				addressService.deleteAddressById(addressItem.getBean().getId());
				addressTable.setValue(null);
				addressTableFlip.refreshInCurrentPage();
				this.getApplication().getMainWindow().showNotification("删除成功！");
			} catch (Exception e) {
				this.getApplication().getMainWindow().showNotification("该客户地址信息是客户资源的默认地址，无法进行删除！");
			}
		}
	}

	private void addCustomerAddress() {
		addressItem = new BeanItem<Address>(new Address());
		addressEditor.setItemDataSource(addressItem, Arrays.asList(VISIBLE_PROPERTIES));
		addressEditorWindow.setCaption("添加地址信息");
		addressEditorWindow.addComponent(addressEditor);
		this.getApplication().getMainWindow().addWindow(addressEditorWindow);
		// 设置组件为只读
		citySelector.setReadOnly(true);
		countySelector.setReadOnly(true);
	}

	private void saveModifyAddress() {
		if(!addressEditor.isValid()) {
			this.getApplication().getMainWindow().showNotification("填写信息有误，请更正后再试！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		addressEditor.commit();
		Address address = addressItem.getBean();
		if(address.getId() != null) {
			addressService.updateAddress(address);
		} else {
			address.setCustomerResource(customerResource);
			addressService.saveAddress(address);
			customerResource.getAddresses().add(address);
			addressTableFlip.refreshToFirstPage();
		}
		this.getApplication().getMainWindow().removeWindow(addressEditorWindow);
	}
	
	public FlipOverTableComponent<Address> getAddressTableFlip() {
		return addressTableFlip;
	}

}
