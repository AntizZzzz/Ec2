package com.jiangyifen.ec2.ui.csr.workarea.historyservicerecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 单个客户的历史服务记录详细信息显示界面
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class DetailSingleCustomerHistoryRecordView extends VerticalLayout implements ValueChangeListener, ClickListener {

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"createDate", "serviceRecordStatus", 
			"customerResource.id", "customerResource.name", "customerResource.telephones", "orderTime", 
			"orderNote", "customerResource.company", "marketingProject"};
	
	private final String[] COL_HEADERS = new String[] {"联系时间", "联系结果", "客户编号", "客户姓名", "电话号码", "预约时间", "预约内容", "公司名称", "所属项目"};
	
	private TextField searchField;				// 按项目或联系结果搜索
	private PopupView complexSearchView;		// 复杂搜索界面
	private HorizontalLayout searchHLayout;		// 存放搜索组件的布局管理器
	
	private TextArea recordContent;				// 记录的详细内容
	private Table historyOutgoingRecordTable;	// 历史记录显示表格
	private DetailSingleCustomerHistoryRecordFilter recordFilter;			// 存放高级收索条件的组件
	private FlipOverTableComponent<CustomerServiceRecord> historyTableFlip;	// 历史记录Table的翻页组件
	
	private Button edit;						// 编辑按钮
	private Button save;						// 保存按钮
	private Button cancel;						// 取消按钮
	private HorizontalLayout operatorHLayout;	// 存放以上按钮
	
	private User loginUser;						// 当前登录用户
	private String searchSql = "";				// 查询语句
	private String countSql = "";				// 统计语句
	private ArrayList<String> ownBusinessModels;// 当前登陆用户目前使用的角色类型所对应的所有权限
	private boolean isEncryptMobile = true;		// 判断电话是否需要加密
	private CustomerResource customerResource;	// 当前选中任务对应的客户资源
	
	private CustomerServiceRecordService cutomerServiceRecordService;
	private TelephoneService telephoneService;
	
	public DetailSingleCustomerHistoryRecordView() {
		this.setSpacing(true);
		this.setWidth("80%");
		
		loginUser = SpringContextHolder.getLoginUser();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		cutomerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		telephoneService = SpringContextHolder.getBean("telephoneService");

		// 创建历史记录表格
		createHistoryOutgoingRecordTable();
		
		// 创建并添加翻页组件
		createTableFlipOver();
		
		// 创建搜索组件
		createSearchComponent();
		
		// 创建编辑按钮组件
		createOperatorsHLayout();

		// 创建记录详细能容显示区域
		createRecordContentArea();

		// 设置组件的状态属性
		setComponentReadOnly(true);
		
		// 将个组将加入总界面
		this.addComponent(searchHLayout);
		this.addComponent(historyOutgoingRecordTable);
		this.addComponent(historyTableFlip);
		this.setComponentAlignment(historyTableFlip, Alignment.BOTTOM_RIGHT);
		this.addComponent(operatorHLayout);
		this.addComponent(recordContent);
	}

	/**
	 * 创建历史记录表格
	 */
	private void createHistoryOutgoingRecordTable() {
		historyOutgoingRecordTable = createFormatColumnTable();
		historyOutgoingRecordTable.setWidth("100%");
		historyOutgoingRecordTable.addListener(this);
		historyOutgoingRecordTable.setImmediate(true);
		historyOutgoingRecordTable.setSelectable(true);
		historyOutgoingRecordTable.setStyleName("striped");
		historyOutgoingRecordTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
	}
	
	/**
	 * 创建并添加翻页组件
	 */
	private void createTableFlipOver() {
		historyTableFlip = new FlipOverTableComponent<CustomerServiceRecord>(CustomerServiceRecord.class, 
					cutomerServiceRecordService, historyOutgoingRecordTable, searchSql, countSql, null);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			historyTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		historyOutgoingRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		historyOutgoingRecordTable.setColumnHeaders(COL_HEADERS);
		historyOutgoingRecordTable.setPageLength(5);
		historyTableFlip.setPageLength(5, false);
	}
	
	/**
	 * 创建搜索组件
	 * 		高级搜索组件需要以翻页组件为基础
	 */
	private void createSearchComponent() {
		searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		searchHLayout.setWidth("100%");
		
		String creatorInfo = loginUser.getEmpNo();
		if(loginUser.getRealName() != null) {
			creatorInfo = creatorInfo + " - " + loginUser.getRealName();
		}
		
		Label tableTitle = new Label("单个客户记录详情 <B>创建人：</B>"+creatorInfo, Label.CONTENT_XHTML);
		tableTitle.setWidth("-1px");
		searchHLayout.addComponent(tableTitle);
		
		recordFilter = new DetailSingleCustomerHistoryRecordFilter(loginUser);
		recordFilter.setHeight("-1px");
		recordFilter.setTableFlipOver(historyTableFlip);
		
		complexSearchView = new PopupView(recordFilter);
		complexSearchView.setHideOnMouseOut(false);
		searchHLayout.addComponent(complexSearchView);
		searchHLayout.setExpandRatio(complexSearchView, 1.0f);
		searchHLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
		
		searchField = new TextField();
		searchField.setWidth("140px");
		searchField.addListener(this);
		searchField.setImmediate(true);
		searchField.setStyleName("search");
		searchField.setInputPrompt("请输入联系结果");
		searchField.setDescription("可按联系结果名称检索");
		searchHLayout.addComponent(searchField);
		searchHLayout.setComponentAlignment(searchField, Alignment.TOP_RIGHT);
	}

	/**
	 * 创建编辑按钮组件
	 */
	private void createOperatorsHLayout() {
		operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		
		Label contentTitle = new Label("历史记录-详细记录内容：");
		contentTitle.setWidth("-1px");
		operatorHLayout.addComponent(contentTitle);
		operatorHLayout.setComponentAlignment(contentTitle, Alignment.MIDDLE_LEFT);
		
		edit = new Button("编 辑", this);
		edit.setStyleName("default");
		edit.setEnabled(false);
		operatorHLayout.addComponent(edit);
		
		save = new Button("保 存", this);
		save.setStyleName("default");
		operatorHLayout.addComponent(save);
		
		cancel = new Button("取 消", this);
		operatorHLayout.addComponent(cancel);
	}
	
	/**
	 * 创建记录详细能容显示区域
	 */
	private void createRecordContentArea() {
		recordContent = new TextArea();
		recordContent.setWidth("100%");
		recordContent.setRows(2);
	}

	/**
	 * 设置组件的状态属性
	 * @param readOnly 是否是只读
	 */
	private void setComponentReadOnly(boolean readOnly) {
		recordContent.setReadOnly(readOnly);
		save.setVisible(!readOnly);
		cancel.setVisible(!readOnly);
	}
	
	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@SuppressWarnings("unchecked")
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if(property.getType() == Date.class) {
					if(property.getValue() != null) {
                		return dateFormat.format((Date)property.getValue());
                	}
                	return "";
				} else if("serviceRecordStatus".equals(colId)) {
					CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) property.getValue();
					String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
					return status.getStatusName() + " - " + direction;
				} else if("customerResource.telephones".equals(colId)) {
					Set<Telephone> telephones = (Set<Telephone>) property.getValue();
					String numbers = "";
					if(isEncryptMobile == true) {
						for(Telephone telephone : telephones) {
							numbers = numbers + telephoneService.encryptMobileNo(telephone.getNumber())+",";
						}
					} else {
						for(Telephone telephone : telephones) {
							numbers = numbers + telephone.getNumber()+",";
						}
					}
					if(numbers.endsWith(",")) {
						numbers = numbers.substring(0, numbers.length() -1);
					}
					return numbers;
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 *  根据MyTaskLeftView 中的任务表格中选中的任务项，显示该条任务对应客户相关的所有历史外呼记录
	 * @param countSql 查询语句
	 */
	public void echoHistoryRecord(CustomerResource customerResource) {
		this.customerResource = customerResource;
		this.countSql = "select count(c) from CustomerServiceRecord as c"
				+ " where c.customerResource.id = " + customerResource.getId()+" and c.creator.id = "+loginUser.getId();

		recordFilter.setCustomerResource(customerResource);
		if(customerResource != null && historyTableFlip != null) {
			searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.createDate desc ";
			historyTableFlip.setSearchSql(searchSql);
			historyTableFlip.setCountSql(countSql);
			historyTableFlip.refreshToFirstPage();
			
			// 默认选中第一项
			selectFirstItemInTable();
		} else {
			historyOutgoingRecordTable.getContainerDataSource().removeAllItems();
		}
	}

	private void selectFirstItemInTable() {
		// 在查询到结果后，默认选择 Table 的第一条记录
		BeanItemContainer<CustomerServiceRecord> taskBeanItemContainer = historyTableFlip.getEntityContainer();
		if(taskBeanItemContainer.size() > 0) {
			Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
			historyOutgoingRecordTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
		} else {
			historyOutgoingRecordTable.setValue(null);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == historyOutgoingRecordTable) {
			CustomerServiceRecord outgoingRecord = (CustomerServiceRecord) historyOutgoingRecordTable.getValue();
			recordContent.setReadOnly(false);
			if(outgoingRecord != null) {
				String content = StringUtils.trimToEmpty(outgoingRecord.getRecordContent());
				recordContent.setValue(content);
				recordContent.setVisible(!"".equals(content));
			} else {
				recordContent.setValue("");
				recordContent.setVisible(false);
			}
			edit.setEnabled(outgoingRecord != null && outgoingRecord.getCreator().getId() != null && outgoingRecord.getCreator().getId().equals(loginUser.getId()));
			edit.setVisible(true);
			setComponentReadOnly(true);
		} else if(source == searchField) {
			String searchStr = searchField.getValue().toString().trim();
			countSql = "select count(csr) from CustomerServiceRecord as csr where "
					+ " csr.customerResource.id = " + customerResource.getId()
					+ " and csr.serviceRecordStatus.statusName like '%" + searchStr +"%'" ;
			searchSql = countSql.replaceFirst("count\\(csr\\)", "csr") + " order by csr.id desc ";
			
			historyTableFlip.setSearchSql(searchSql);
			historyTableFlip.setCountSql(countSql);
			historyTableFlip.refreshToFirstPage();
			// 默认选中第一项
			selectFirstItemInTable();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		CustomerServiceRecord outgoingRecord = (CustomerServiceRecord) historyOutgoingRecordTable.getValue();
		if(source == edit) {
			setComponentReadOnly(false);
			recordContent.setVisible(true);
		} else if(source == save) {
			String content = StringUtils.trimToEmpty((String) recordContent.getValue());
			outgoingRecord.setRecordContent(content);
			cutomerServiceRecordService.update(outgoingRecord);
			setComponentReadOnly(true);
			recordContent.setVisible(!"".equals(content));
		} else if(source == cancel) {
			String content = StringUtils.trimToEmpty(outgoingRecord.getRecordContent());
			recordContent.setValue(outgoingRecord.getRecordContent());
			setComponentReadOnly(true);
			recordContent.setVisible(!"".equals(content));
		}
		edit.setVisible(recordContent.isReadOnly());
	}

	public FlipOverTableComponent<CustomerServiceRecord> getHistoryTableFlip() {
		return historyTableFlip;
	}

}
