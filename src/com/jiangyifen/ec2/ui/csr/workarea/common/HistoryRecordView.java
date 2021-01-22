package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
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

@SuppressWarnings("serial")
public class HistoryRecordView extends VerticalLayout implements ValueChangeListener, ClickListener {

	// 坐席是否可以查看客户的历史服务记录
	private static final String BASE_DESIGN_MANAGEMENT_CSR_POP_SERVICE_RECORD_VIEWABLE = "base_design_management&csr_pop_service_record_viewable";
	// 座席是否可以编辑自己拨打的历史服务记录
	private static final String BASE_DESIGN_MANAGEMENT_CSR_POP_SERVICE_RECORD_EDIT = "base_design_management&csr_pop_service_record_edit";
	// 管理员是否可以编辑自己在座席界面拨打的客服记录
	private static final String BASE_DESIGN_MANAGEMENT_MANAGER_POP_SERVICE_RECORD_EDIT = "base_design_management&manager_pop_service_record_edit";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"createDate", "serviceRecordStatus", "marketingProject", "creator", "creator.department"};
	
	private final String[] COL_HEADERS = new String[] {"联系时间", "联系结果", "项目名称", "创建人", "所属部门"};
	
	private PopupView complexSearchView;		// 复杂搜索界面
	private TextField searchField;				// 按项目或联系结果搜索
	private HistoryRecordFilter recordFilter;	// 存放高级收索条件的组件
	private HorizontalLayout searchHLayout;		// 存放搜索组件的布局管理器
	
	private TextArea recordContent;				// 记录的详细内容
	private Table historyOutgoingRecordTable;	// 历史记录显示表格
	private FlipOverTableComponent<CustomerServiceRecord> historyTableFlip;	// 历史记录Table的翻页组件
	
	private Button edit;						// 编辑按钮
	private Button save;						// 保存按钮
	private Button cancel;						// 取消按钮
	private HorizontalLayout operatorHLayout;	// 存放以上按钮
	
	private User loginUser;						// 当前登录用户
	private String searchSql = "";				// 查询语句
	private String countSql = "";				// 统计语句
	
	private RoleType roleType;					// 角色类型
	private CustomerResource customerResource;	// 当前选中任务对应的客户资源
	private String searchByCreatorSpanSql = "";	// 按创建者所属部门进行查询客服记录[设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录，默认为可以]
	private ArrayList<String> ownBusinessModels;// 当前登陆用户目前使用的角色类型所对应的所有权限

	private CustomerServiceRecordService cutomerServiceRecordService;
	private DepartmentService departmentService;
	
	/**
	 * 历史客服记录查看界面构造函数
	 * @param loginUser	当前登陆用户
	 * @param roleType	当前登陆用户目前正使用的角色类型【csr、manager】
	 */
	public HistoryRecordView(User loginUser, RoleType roleType) {
		this.setSpacing(true);
		this.setMargin(true);
		this.loginUser = loginUser;
		this.roleType = roleType;

		cutomerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
		// 获取当前登陆用户所有权限
		ownBusinessModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			} else if(role.getType().equals(RoleType.manager)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			}
		}
		
		// 初始化客服记录按部门查询的语句
		initializeSearchByCreatorDeptIdSql(roleType);
		
		// 创建历史记录表格
		createHistoryOutgoingRecordTable();
		
		// 创建并添加翻页组件
		createTableFlipOver();
		
		// 创建搜索组件
		createSearchComponent(roleType);
		
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
		
		// 是否显示编辑克服历史记录的按钮
		whetherDisplayHistoryEditButton();
	}

	/**
	 * 初始化客服记录按部门查询的语句
	 * @param roleType	当前登陆用户目前正使用的角色类型【csr、manager】
	 */
	private void initializeSearchByCreatorDeptIdSql(RoleType roleType) {
		// 获取全局配置，看当前话务员是否能够看到其他部门的坐席针对同一资源做的客服记录
		Map<String,Boolean> domainConfigs = ShareData.domainToConfigs.get(loginUser.getDomain().getId());
		Boolean viewRecordSpanDeptAble = domainConfigs.get("csr_view_record_span_department");
		if(roleType.equals(RoleType.csr)) {
			if(viewRecordSpanDeptAble == null) {	// 查看整个域针对该客户做的记录
				searchByCreatorSpanSql = "";
			} else if(!viewRecordSpanDeptAble) {	// 查看当前坐席针对该客户做的记录
				searchByCreatorSpanSql = " and c.creator.id = "+loginUser.getId();
			} else {	// 查看当前坐席所属部门，整个部门下针对该客户做的记录
				searchByCreatorSpanSql = " and c.creator.department.id = "+loginUser.getDepartment().getId();
			}
		} else if(roleType.equals(RoleType.manager)) {	// 如果是管理员调用，则只能看到自己管辖部门下的坐席创建的客服记录
			// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
			List<Long> allGovernedDeptIds = new ArrayList<Long>();
			for (Role role : loginUser.getRoles()) {
				if (role.getType().equals(RoleType.manager)) {
					List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
					if(departments.isEmpty()) {
						allGovernedDeptIds.add(0L);
					} else {
						for (Department dept : departments) {
							Long deptId = dept.getId();
							if (!allGovernedDeptIds.contains(deptId)) {
								allGovernedDeptIds.add(deptId);
							}
						}
					}
				}
			}
			// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
			String deptIdsStr = "";
			for (int i = 0; i < allGovernedDeptIds.size(); i++) {
				if (i == (allGovernedDeptIds.size() - 1)) {
					deptIdsStr += allGovernedDeptIds.get(i);
				} else {
					deptIdsStr += allGovernedDeptIds.get(i) + ",";
				}
			}
			searchByCreatorSpanSql = " and c.creator.department.id in (" + deptIdsStr+ ")";
		}
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
	private void createSearchComponent(RoleType roleType) {
		searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		searchHLayout.setWidth("100%");

		searchField = new TextField();
		searchField.setWidth("140px");
		searchField.addListener(this);
		searchField.setImmediate(true);
		searchField.setStyleName("search");
		searchField.setInputPrompt("请输入联系结果");
		searchField.setDescription("可按联系结果名称检索");
		searchHLayout.addComponent(searchField);
		searchHLayout.setComponentAlignment(searchField, Alignment.MIDDLE_LEFT);
		
		recordFilter = new HistoryRecordFilter(loginUser, roleType);
		recordFilter.setHeight("-1px");
		recordFilter.setTableFlipOver(historyTableFlip);
		
		complexSearchView = new PopupView(recordFilter);
		complexSearchView.setHideOnMouseOut(false);
		searchHLayout.addComponent(complexSearchView);
		searchHLayout.setExpandRatio(complexSearchView, 1.0f);
		searchHLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 创建编辑按钮组件
	 */
	private void createOperatorsHLayout() {
		operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		
		Label contentTitle = new Label("详细记录内容：");
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
		recordContent.setHeight("40px");
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
				} else if("creator".equals(colId)) {
					User csr = (User) property.getValue();
					String creatorInfo = csr.getEmpNo();
					if(csr.getRealName() != null) {
						creatorInfo = creatorInfo + " - " + csr.getRealName();
					}
					return creatorInfo;
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
		// 如果是坐席角色类型，则只有当其拥有相应的权限的时候，才能查看历史服务记录
		if(roleType.equals(RoleType.csr) && !ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_CSR_POP_SERVICE_RECORD_VIEWABLE)) {
			return;
		}
		recordFilter.setCustomerResource(customerResource);
		this.countSql = "select count(c) from CustomerServiceRecord as c"
				+ " where c.customerResource.id = " + customerResource.getId() + searchByCreatorSpanSql;
		this.customerResource = customerResource;
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
	
	/**
	 * jinht
	 * 是否显示编辑历史记录的按钮
	 * 描述：首先获取当前登录的用户是以哪种角色进行登录的，然后查看当前登录的角色的用户下是否有编辑自己拨打的历史客服记录的权限
	 */
	private void whetherDisplayHistoryEditButton() {
		// 如果是坐席角色类型，则只有当其拥有相应的权限的时候，才能编辑历史服务记录
		if(roleType.equals(RoleType.csr) && ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_CSR_POP_SERVICE_RECORD_EDIT)) {
			edit.setVisible(true);
		// 如果是管理员角色类型，则只有当其拥有相应的权限的时候，才能编辑历史服务记录
		} else if(roleType.equals(RoleType.manager) && ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MANAGER_POP_SERVICE_RECORD_EDIT)) {
			edit.setVisible(true);
		} else {
			edit.setVisible(false);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == historyOutgoingRecordTable) {
			CustomerServiceRecord outgoingRecord = (CustomerServiceRecord) historyOutgoingRecordTable.getValue();
			recordContent.setReadOnly(false);
			if(outgoingRecord != null) {
				recordContent.setValue(outgoingRecord.getRecordContent());
			} else {
				recordContent.setValue("");
			}
			recordContent.setReadOnly(true);
			edit.setEnabled(outgoingRecord != null && outgoingRecord.getCreator().getId() != null && outgoingRecord.getCreator().getId().equals(loginUser.getId()));
//			edit.setVisible(true);
			// 是否显示编辑克服历史记录的按钮
			whetherDisplayHistoryEditButton();
			setComponentReadOnly(true);
		} else if(source == searchField) {
			// 如果是坐席角色类型，则只有当其拥有相应的权限的时候，才能查看历史服务记录
			if(roleType.equals(RoleType.csr) && !ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_CSR_POP_SERVICE_RECORD_VIEWABLE)) {
				return;
			}
			String searchStr = StringUtils.trimToEmpty((String) searchField.getValue());
			countSql = "select count(csr) from CustomerServiceRecord as csr where "
					+ " csr.customerResource.id = " + customerResource.getId() + searchByCreatorSpanSql
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
		} else if(source == save) {
			outgoingRecord.setRecordContent((String) recordContent.getValue());
			cutomerServiceRecordService.update(outgoingRecord);
			setComponentReadOnly(true);
		} else if(source == cancel) {
			recordContent.setValue(outgoingRecord.getRecordContent());
			setComponentReadOnly(true);
		}
		edit.setVisible(recordContent.isReadOnly());
	}

	public FlipOverTableComponent<CustomerServiceRecord> getHistoryTableFlip() {
		return historyTableFlip;
	}

}
