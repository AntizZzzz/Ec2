package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.customermanage.CustomerResourceSimpleFilter;
import com.jiangyifen.ec2.ui.mgr.customermanage.MigrateCustomerWindow;
import com.jiangyifen.ec2.ui.utils.CustomComboBox;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CustomerMemberManagement extends VerticalLayout implements ClickListener, ValueChangeListener {

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"accountManager.empNo","accountManager.username", "accountManager.realName", "id", "name", "telephones", "company", "sex", "birthday", "customerLevel", "lastDialDate", /*"expireDate",*/ "importDate"};
	
	private final String[] COL_HEADERS = new String[] {"经理工号", "经理用户名", "经理姓名", "客户编号", "客户姓名", "联系方式", "公司名称", "性别", "生日", "客户等级", "最近联系日期", /*"过期时间",*/ "导入时间"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);
	
	private Table customersTable;
	private CustomerResourceSimpleFilter customerSimpleFilter;	// 资源的简单搜索组件
	private CustomComboBox selectType;							// 选择类型（全选当前页、反选当前页等）
	private TextField optCount_tf;								// 待操作的客户条数
	private Button select_bt;									// 选择指定数量
	private Button migrate_bt;									// 迁移按钮
	private FlipOverTableComponent<CustomerResource> customersTableFlip;	// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 
	private MigrateCustomerWindow migrateCustomerWindow;		// 迁移客户子窗口
	
	private Domain domain;										// 当前登录用户所属域
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private boolean isEncryptMobile = true;						// 默认使用加密的电话号码和手机号

	private CustomerResourceService customerResourceService;
	private DepartmentService departmentService;				// 部门服务类

	private LinkedHashMap<Long,CustomerResource> selectedCustomers;
	
	public CustomerMemberManagement() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);

		selectedCustomers = new LinkedHashMap<Long,CustomerResource>();
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		departmentService = SpringContextHolder.getBean("departmentService");

		customerSimpleFilter = new CustomerResourceSimpleFilter();
		customerSimpleFilter.setHeight("-1px");
		this.addComponent(customerSimpleFilter);
		
		// 创建客户表格
		createCustomersTable();
		
		// 为表格添加右键单击事件
		addActionToTable(customersTable);
		
		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);

		// 创建表格下方左侧的组件（迁移，选择类型）
		createTableFooterLeftComponents(tableFooter);
		
		// 创建翻页组件
		createTableFlipComponent(tableFooter);

		// 根据分辨率设置表格行数
		setTablePageLength();

		// 为我的资源Tab 页上方的搜索组件传递翻页组件对象
		customerSimpleFilter.setCustomerMemberManageFlip(customersTableFlip);
	}

	/**
	 * 创建表格下方左侧的组件（迁移，选择类型）
	 * @param tableFooter
	 */
	private void createTableFooterLeftComponents(HorizontalLayout tableFooter) {
		HorizontalLayout footerLeftLayout = new HorizontalLayout();
		footerLeftLayout.setSpacing(true);
		tableFooter.addComponent(footerLeftLayout);
		
		// 创建全选反选组件
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		map.put("SelectCurrentPage", "全选当前页");
		map.put("InvertCurrentPage", "反选当前页");
		map.put("RemoveCurrentPage", "不选当前页");
		map.put("SelectAllPages", "全选所有页");
		map.put("InvertAllPages", "反选所有页");
		map.put("RemoveAllPages", "不选所有页");
		
		Label typeLabel = new Label("<B>全选/反选：</B>", Label.CONTENT_XHTML);
		typeLabel.setWidth("-1px");
		footerLeftLayout.addComponent(typeLabel);
		footerLeftLayout.setComponentAlignment(typeLabel, Alignment.MIDDLE_LEFT);
		
		selectType = new CustomComboBox();
		selectType.setWidth("100px");
		selectType.addValueChangeListener(CustomerMemberManagement.this);
		selectType.setNullSelectionAllowed(true);
		selectType.setDataSource(map);
		footerLeftLayout.addComponent(selectType);
		footerLeftLayout.setComponentAlignment(selectType, Alignment.MIDDLE_LEFT);

		// 迁移总数
		optCount_tf = new TextField();
		optCount_tf.setInputPrompt("迁移总数");
		optCount_tf.setWidth("60px");
		optCount_tf.setMaxLength(10);
		optCount_tf.addValidator(new RegexpValidator("\\d+", "迁移总数只能是大于 0 的数字"));
		optCount_tf.setValidationVisible(false); 
		footerLeftLayout.addComponent(optCount_tf);
		footerLeftLayout.setComponentAlignment(optCount_tf, Alignment.MIDDLE_LEFT);
		
		// 创建选择按钮
		select_bt = new Button("选择", (ClickListener)this);
		select_bt.setImmediate(true);
		footerLeftLayout.addComponent(select_bt);
		footerLeftLayout.setComponentAlignment(select_bt, Alignment.MIDDLE_LEFT);
		
		// 创建迁移按钮
		migrate_bt = new Button("迁移客户", (ClickListener)this);
		migrate_bt.setImmediate(true);
		migrate_bt.setEnabled(false);
		migrate_bt.setStyleName("default");
		footerLeftLayout.addComponent(migrate_bt);
		footerLeftLayout.setComponentAlignment(migrate_bt, Alignment.MIDDLE_LEFT);
	}

	/**
	 * 创建客户显示表格
	 */
	private void createCustomersTable() {
		customersTable = createFormatColumnTable();
		customersTable.setWidth("100%");
		customersTable.setHeight("-1px");
		customersTable.setImmediate(true);
		customersTable.addListener((ValueChangeListener)this);
		customersTable.setSelectable(true);
		customersTable.setStyleName("striped");
		customersTable.setNullSelectionAllowed(false);
		customersTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(customersTable);
	}

	/**
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
            		return dateFormat.format((Date)property.getValue());
                } 
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 为指定任务表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(customerAllInfoWindow == null) {			// 只有单击右键时才创建客户信息查看窗口及相关组件
					createCustomerAllInfoWindow();
				}
				
				table.select(target);
				table.getApplication().getMainWindow().removeWindow(customerAllInfoWindow);
				table.getApplication().getMainWindow().addWindow(customerAllInfoWindow);
				CustomerResource customerResource = (CustomerResource) table.getValue();
				customerAllInfoWindow.initCustomerResource(customerResource);
				
				if(action == BASEINFO) {
					customerAllInfoWindow.echoCustomerBaseInfo(customerResource);
					customerInfoTabSheet.setSelectedTab(0);
				} else if(action == DESCRIPTIONINFO) {
					customerAllInfoWindow.echoCustomerDescription(customerResource);
					customerInfoTabSheet.setSelectedTab(1);
				} else if(action == ADDRESSINFO) {
					customerAllInfoWindow.echoCustomerAddress(customerResource);
					customerInfoTabSheet.setSelectedTab(2);
				} else if(action == HISTORYRECORD) {
					customerAllInfoWindow.echoHistoryRecord(customerResource);
					customerInfoTabSheet.setSelectedTab(3);
				} 
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {BASEINFO, DESCRIPTIONINFO, ADDRESSINFO, HISTORYRECORD};
				}
				return null;
			}
		});
	}
	
	/**
	 * 创建翻页组件
	 */
	private void createTableFlipComponent(HorizontalLayout tableFooter) {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(RoleType.manager.equals(role.getType())) {
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
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句
		String deptIdSql = "";
		for(int i = 0; i < allGovernedDeptIds.size(); i++) {
			if( i == (allGovernedDeptIds.size() - 1) ) {
				deptIdSql += allGovernedDeptIds.get(i);
			} else {
				deptIdSql += allGovernedDeptIds.get(i) +", ";
			}
		}
		String deptSql = " and c.accountManager.department.id in (" +deptIdSql+ ")";

		// 创建初始搜索语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("本月");
		String countSql = "select count(c) from CustomerResource as c where c.domain.id = " +domain.getId() +" and c.lastDialDate >= '" +dateStrs[0]+ "' and c.lastDialDate < '" +dateStrs[1]+"'"+ deptSql;
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.accountManager.id asc, c.lastDialDate desc";
		
		customersTableFlip = new FlipOverTableComponent<CustomerResource>(CustomerResource.class, 
				customerResourceService, customersTable, searchSql , countSql, null);
		customersTableFlip.setSearchSql(searchSql);
		customersTableFlip.setCountSql(countSql);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			customersTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		customersTable.setVisibleColumns(VISIBLE_PROPERTIES);
		customersTable.setColumnHeaders(COL_HEADERS);
		// jrh
		customersTable.addGeneratedColumn("telephones", new TelephonesColumnGenerator());
		customersTable.addGeneratedColumn("migrate", new MigrateColumnGenerator());
		customersTable.setColumnHeader("migrate", "是否迁移");
		customersTable.setColumnWidth("migrate", 50);
		customersTable.setColumnAlignment("migrate", Table.ALIGN_CENTER);
		
		tableFooter.addComponent(customersTableFlip);
		tableFooter.setComponentAlignment(customersTableFlip, Alignment.TOP_RIGHT);
	}

	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("telephones")) {
				CustomerResource customerResource = (CustomerResource) itemId;
				Set<Telephone> telephones = customerResource.getTelephones();
				return new TelephoneNumberInTableLabelStyle(telephones, isEncryptMobile);
			} 
			return null;
		}
	}
	
	/**
	 * 用于自动生成“迁移”列，用于标记 是否将对应行的客户迁移
	 */
	private class MigrateColumnGenerator implements Table.ColumnGenerator {
		
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			final CustomerResource cr = (CustomerResource) itemId;
			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if(value == false) {
						selectedCustomers.remove(cr.getId());
					} else {
						selectedCustomers.put(cr.getId(), cr);
					}
					// 修改删除按钮
					updateOptButtonStatus();
				}
			});
			
			// 如果需要迁移，则勾选CheckBox
			if(selectedCustomers.keySet().contains(cr.getId())) {
				check.setValue(true);
			} 
			
        	return check;
		}
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			customersTable.setPageLength(32);
			customersTableFlip.setPageLength(32, false);
		} else if(screenResolution[1] >= 1050) {
			customersTable.setPageLength(30);
			customersTableFlip.setPageLength(30, false);
		} else if(screenResolution[1] >= 900) {
			customersTable.setPageLength(23);
			customersTableFlip.setPageLength(23, false);
		} else if(screenResolution[1] >= 768) {
			customersTable.setPageLength(16);
			customersTableFlip.setPageLength(16, false);
		}
	}
	
	/**
	 *  创建客户信息查看窗口及相关组件
	 */
	private void createCustomerAllInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.manager);
		customerAllInfoWindow.setResizable(false);
		customerAllInfoWindow.setEchoModifyByReflect(this);
		
		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}
	
	/**
	 * 供 CustomerBaseInfoEditorForm 调用
	 * 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		CustomerResource currentRecord = (CustomerResource) customersTable.getValue();
		customersTableFlip.refreshInCurrentPage();
		for(int i = 0; i < customersTable.getItemIds().size(); i++) {
			CustomerResource resource = (CustomerResource) customersTable.getItemIds().toArray()[i];
			if(resource != null && currentRecord != null && resource.getId() != null && resource.getId().equals(currentRecord.getId())) {
				customersTable.select(resource);	break;
			}
		}
	}

	/**
	 * 当selectType 选中的值发生改变的时候调用
	 * 	由CustomComboBox 类反射执行
	 */
	public void reflectForSelectType() {
		String value = (String) selectType.getValue();
		if(value == null) {
			return;
		}
		
		if("SelectCurrentPage".equals(value)) {
			for(Object customer : customersTable.getItemIds()) {
				Long id = ((CustomerResource) customer).getId();
				selectedCustomers.put(id, (CustomerResource)customer);
			}
		} else if("InvertCurrentPage".equals(value)) {
			// 保存之前在当前页选中的Id 号
			Set<Long> originalIds = new HashSet<Long>();
			for(Object customer : customersTable.getItemIds()) {
				Long id = ((CustomerResource) customer).getId();
				if(selectedCustomers.keySet().contains(id)) {	// 如果之前选中了，则加入set集合
					originalIds.add(id);
				}
				selectedCustomers.put(id, (CustomerResource)customer);
			}
			// 再将在当前页原来选中的去除
			for(Long customerId : originalIds) {
				selectedCustomers.remove(customerId);
			}
		} else if("SelectAllPages".equals(value)) {
			String searchSql = customersTableFlip.getSearchSql();
			List<CustomerResource> customers = customerResourceService.getAllBySql(searchSql);
			for(CustomerResource customer : customers) {
				selectedCustomers.put(customer.getId(), customer);
			}
		} else if("InvertAllPages".equals(value)) {
			// 保存之前所有选中的Id 号
			Set<Long> originalIds = new HashSet<Long>(selectedCustomers.keySet());
			// 先将所有的客户放入集合
			String searchSql = customersTableFlip.getSearchSql();
			List<CustomerResource> customers = customerResourceService.getAllBySql(searchSql);
			for(CustomerResource customer : customers) {
				selectedCustomers.put(customer.getId(), customer);
			}
			// 再将原来选中的去除
			for(Long customerId : originalIds) {
				selectedCustomers.remove(customerId);
			}
		} else if("RemoveCurrentPage".equals(value)) {
			for(Object customer : customersTable.getItemIds()) {
				Long id = ((CustomerResource) customer).getId();
				selectedCustomers.remove(id);
			}
		} else if("RemoveAllPages".equals(value)) {
			selectedCustomers.clear();
		} 
		customersTableFlip.refreshInCurrentPage();
		
		// 修改删除按钮
		updateOptButtonStatus();
		selectType.setValue(null);
	}

	/**
	 * 显示迁移客户的窗口
	 */
	private void showMigrateWindow() {
		if(migrateCustomerWindow == null) {
			migrateCustomerWindow = new MigrateCustomerWindow(this);
		} 
		this.getApplication().getMainWindow().addWindow(migrateCustomerWindow);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == migrate_bt) {
			showMigrateWindow();

		} else if (source == select_bt) { 
			executeSelectItems();
		}
	}

	/**
	 * @Description 描述：执行选中指定条数的客户资源
	 *
	 * @author  JRH
	 * @date    2015年1月7日 下午5:24:23 void
	 */
	private void executeSelectItems() {
		String optCountStr = (String) optCount_tf.getValue();
		if(!optCount_tf.isValid() || StringUtils.isEmpty(optCountStr)) {
			this.getApplication().getMainWindow().showNotification("迁移总数只能是大于 0 的数字", Notification.TYPE_WARNING_MESSAGE);
		} else {
			int optCount = Integer.valueOf(optCountStr);
			String searchSql = customersTableFlip.getSearchSql();
			
			int totalRecord = customersTableFlip.getTotalRecord();
			if(optCount > totalRecord) {
				optCount = totalRecord;
				optCount_tf.setValue(totalRecord+"");
			}

			selectedCustomers.clear();
			if(optCount > 0) {	// 如果输入的值为0 ，则不能查询，JPQL loadPageEntities(0, 0, jpql) 是查询所有符合条件的，而不是一条都不获取
				List<CustomerResource> resourceLs = customerResourceService.loadPageEntities(0, optCount, searchSql);
				for(CustomerResource resource : resourceLs) {
					selectedCustomers.put(resource.getId(), resource);
				}
			}

			customersTableFlip.refreshInCurrentPage();
			
			// 修改删除按钮
			updateOptButtonStatus();
			
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == customersTable) {
			if(selectedCustomers.size() == 0) {
				migrate_bt.setEnabled(customersTable.getValue() != null);
			}
		} 
	}

	/**
	 * 供自身及migrateCustomerWindow 当点击保存时调用
	 * 刷新迁移按钮的标题
	 */
	public void updateOptButtonStatus() {
		// 修改删除按钮
		if(selectedCustomers.size() > 0) {
			migrate_bt.setCaption("迁移勾选项");
			migrate_bt.setEnabled(true);
		} else {
			migrate_bt.setCaption("迁移客户");
			migrate_bt.setEnabled(customersTable.getValue() != null);
		}
	}

	/**
	 * 刷新客户显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			customersTableFlip.refreshToFirstPage();
		} else {
			customersTableFlip.refreshInCurrentPage();
		}
	}
	
	public Table getCustomersTable() {
		return customersTable;
	}

	public LinkedHashMap<Long, CustomerResource> getSelectedCustomers() {
		return selectedCustomers;
	}

}
