package com.jiangyifen.ec2.ui.mgr.customermanage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.mgr.MigrateCustomerToCsrService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.CustomerMemberManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.event.Action;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

/**
 * 客户迁移子窗口
 * @author jrh
 */
@SuppressWarnings("serial")
public class MigrateCustomerWindow extends Window implements ClickListener, ValueChangeListener, CloseListener {
	// 电话号码加密
	private final String MOBILE_NUM_SECRET = "mobile_num_secret";
	
	// 客户表格的显示列
	private final Object[] CUSTOMER_VISIBLE_PROPERTIES = new String[] {"id", "name", "accountManager.empNo", "telephones" };
	private final String[] CUSTOMER_COL_HEADERS = new String[] {"客户编号", "姓名", "经理工号", "电话号码" };

	// 客户经理表格的显示列
	private final Object[] USER_VISIBLE_PROPERTIES = new String[] {"username", "empNo", "realName", "department.name" };
	private final String[] USER_COL_HEADERS = new String[] {"用户名", "工号", "姓名", "部门" };

	// 右键单击 action
	private final Action REMOVE = new Action("取消迁移选中客户", ResourceDataMgr.cancel_migrate);
	
	// 显示表格及其数据源容器
	private Table customerTable;				// 待迁移客户
	private Table alternativeUserTable;			// 可选客户经理表格
	private Table selectedUserTable;			// 已选客户经理

	private BeanItemContainer<CustomerResource> customerTableContainer;
	private BeanItemContainer<User> alternativeUserContainer;
	private BeanItemContainer<User> selectedUserContainer;
	
	// 关键字和搜索按钮
	// customerTable 表格的搜索项
	private TextField customerKeyword;
	private Button customerSearch;

	// alternativeUserTable 表格的搜索项
	private TextField leftUserKeyword;
	private Button leftUserSearch;

	// selectedUserTable 表格的搜索项
	private TextField rightUserKeyword;
	private Button rightUserSearch;

	// 两个客户经理表格中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;
	
	// 下面队列中所有成员的优先级全局设置，添加和取消按钮
	private Button removeCustomer;						// 取消移除选中客户
	private Button save;
	private Button cancel;

	private Notification notification;					// 提示信息
	private CustomerMemberManagement customerMemberManagement;

	private User loginUser;
	private Domain domain;
	private boolean isEncryptMobile = true;					// 默认使用加密的电话号码和手机号
	
	private UserService userService;
	private DepartmentService departmentService;
	private TelephoneService telephoneService;
	private MigrateCustomerToCsrService migrateCustomerToCsrService;
	
	public MigrateCustomerWindow(CustomerMemberManagement customerMemberManagement) {
		this.center();
		this.setModal(true);
		this.addListener((CloseListener)this);
		this.setCaption("迁移客户");
		this.setResizable(false);
		this.customerMemberManagement = customerMemberManagement;
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		userService = SpringContextHolder.getBean("userService");
		departmentService = SpringContextHolder.getBean("departmentService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		migrateCustomerToCsrService = SpringContextHolder.getBean("migrateCustomerToCsrService");
		
		// 创建提示信息
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setSizeUndefined();
		mainLayout.setMargin(true);
		this.setContent(mainLayout);
		
		HorizontalLayout centerLayout = new HorizontalLayout();
		centerLayout.setSpacing(true);
		centerLayout.setWidth("100%");
		mainLayout.addComponent(centerLayout);
		
		// 创建需要迁移的客户显示等组件
		createCustomerTableComponents(centerLayout);

		// 为表格添加右键单击事件
		addActionToTable(customerTable);
		
		// 创建客户经理（User）选择组件
		createUserSelectComponents(centerLayout);

		// 添加表格的拖拽支持
		makeTableDragAble(new SourceIs(selectedUserTable), alternativeUserTable, true);
		makeTableDragAble(new SourceIs(alternativeUserTable), selectedUserTable, false);
	}
	
	/**
	 * 为指定任务表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				table.select(target);
				if(action == REMOVE) {
					removeCustomer.click();
				} 
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {REMOVE};
				}
				return null;
			}
		});
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		LinkedHashMap<Long, CustomerResource> needMigrateCustomers = customerMemberManagement.getSelectedCustomers();
		customerTableContainer.removeAllItems();
		if(needMigrateCustomers.size() > 0) {
			for(CustomerResource customer : needMigrateCustomers.values()) {
				if(customer.getName() == null) {	// 为了便于搜索
					customer.setName("");
				}
			}
			customerTableContainer.addAll(needMigrateCustomers.values());
			// 按客户id号和客户经理工号的升序排列
			customerTableContainer.sort(new Object[] {"id", "accountManager.empNo"}, new boolean[] {true, true});
		} else {
			CustomerResource customer = (CustomerResource) customerMemberManagement.getCustomersTable().getValue();
			if(customer.getName() == null) {	// 为了便于搜索
				customer.setName("");
			}
			customerTableContainer.addBean(customer);
		}

		// 设置待迁移客户表格的标题
		customerTable.setCaption("待迁移客户("+customerTableContainer.size()+")");
		
		// 清空搜索框中的值
		customerKeyword.setValue("");
		leftUserKeyword.setValue("");
		rightUserKeyword.setValue("");
	}
	
	/**
	 *  创建需要迁移的客户显示等组件
	 * @param centerLayout
	 */
	private void createCustomerTableComponents(HorizontalLayout centerLayout) {
		VerticalLayout customerVLayout = new VerticalLayout();
		customerVLayout.setSpacing(true);
		centerLayout.addComponent(customerVLayout);
	
		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		customerVLayout.addComponent(searchHLayout);
	
		customerKeyword = new TextField();
		customerKeyword.setImmediate(true);
		customerKeyword.setInputPrompt("请输入搜索关键字");
		customerKeyword.setDescription("可按客户编号、姓名、及经理工号搜索！");
		customerKeyword.setStyleName("search");
		customerKeyword.addListener(this);
		searchHLayout.addComponent(customerKeyword);
		searchHLayout.setComponentAlignment(customerKeyword, Alignment.MIDDLE_CENTER);
	
		customerSearch = new Button("搜索", this);
		customerSearch.setImmediate(true);
		searchHLayout.addComponent(customerSearch);
		searchHLayout.setComponentAlignment(customerSearch, Alignment.MIDDLE_CENTER);
	
		// 创建表格组件
		customerTable = createFormatColumnTable();
		customerTable.setCaption("待迁移客户");
		customerTable.setStyleName("striped");
		customerTable.setSelectable(true);
		customerTable.setWidth("100%");
		customerTable.setMultiSelect(true);
		customerTable.setNullSelectionAllowed(true);
		customerTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		customerVLayout.addComponent(customerTable);
	
		customerTableContainer = new BeanItemContainer<CustomerResource>(CustomerResource.class);
		customerTableContainer.addNestedContainerProperty("accountManager.empNo");
		customerTable.setContainerDataSource(customerTableContainer);
		customerTable.setPageLength(20);
		customerTable.setVisibleColumns(CUSTOMER_VISIBLE_PROPERTIES);
		customerTable.setColumnHeaders(CUSTOMER_COL_HEADERS);
		
		// 创建底部右侧组件
		HorizontalLayout bottomLeft = new HorizontalLayout();
		bottomLeft.setSpacing(true);
		customerVLayout.addComponent(bottomLeft);
		customerVLayout.setComponentAlignment(bottomLeft, Alignment.MIDDLE_LEFT);
	
		// 保存
		removeCustomer = new Button("取消迁移选中客户", this);
		bottomLeft.addComponent(removeCustomer);
		bottomLeft.setComponentAlignment(removeCustomer, Alignment.MIDDLE_LEFT);
	}

	/**
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@SuppressWarnings("unchecked")
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
                } else if("telephones".equals(colId)) {
            		// 判断是否需要加密
            		Long domainId = loginUser.getDomain().getId();
            		Boolean isEncrypt = ShareData.domainToConfigs.get(domainId).get(MOBILE_NUM_SECRET);
            		if(isEncrypt != null) {
            			isEncryptMobile = isEncrypt;
            		}
            		
            		if(isEncryptMobile == true) {
            			Set<Telephone> telephones = (Set<Telephone>) property.getValue();
            			String encryptNums = "";
            			for(Telephone tp : telephones) {
            				encryptNums+=telephoneService.encryptMobileNo(tp.getNumber()) + ",";
            			}
            			if(encryptNums.endsWith(",")) {
            				encryptNums = encryptNums.substring(0, encryptNums.length() -1);
            			}
            			return encryptNums;
            		} else {
            			Set<Telephone> telephones = (Set<Telephone>) property.getValue();
            			String numbers = "";
            			for(Telephone tp : telephones) {
            				numbers+=tp.getNumber() + ",";
            			}
            			if(numbers.endsWith(",")) {
            				numbers = numbers.substring(0, numbers.length() -1);
            			}
            			return numbers;
            		}
				} 
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 * 创建客户经理（User）选择组件
	 * @param centerLayout
	 */
	private void createUserSelectComponents(HorizontalLayout centerLayout) {
		HorizontalLayout userSelectHLayout = new HorizontalLayout();
		userSelectHLayout.setSpacing(true);
		centerLayout.addComponent(userSelectHLayout);
		
		// 创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
		VerticalLayout leftUserComponents = createLeftUserComponents();
		userSelectHLayout.addComponent(leftUserComponents);
		
		// 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
		VerticalLayout middleUserComponents = createMiddleUserComponents();
		userSelectHLayout.addComponent(middleUserComponents);
		userSelectHLayout.setComponentAlignment(middleUserComponents, Alignment.MIDDLE_CENTER);

		// 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件)
		VerticalLayout rightUserComponents = createRightUserComponents();
		userSelectHLayout.addComponent(rightUserComponents);
	}
	
	/**
	 *  创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
	 * @return
	 */
	 
	private VerticalLayout createLeftUserComponents() {
		VerticalLayout leftUserVLayout = new VerticalLayout();
		leftUserVLayout.setSpacing(true);
		leftUserVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		leftUserVLayout.addComponent(searchHLayout);

		leftUserKeyword = new TextField();
		leftUserKeyword.setImmediate(true);
		leftUserKeyword.setInputPrompt("请输入搜索关键字");
		leftUserKeyword.setDescription("可按用户名、工号、姓名及部门名称搜索！");
		leftUserKeyword.setStyleName("search");
		leftUserKeyword.addListener(this);
		searchHLayout.addComponent(leftUserKeyword);
		searchHLayout.setComponentAlignment(leftUserKeyword, Alignment.MIDDLE_CENTER);

		leftUserSearch = new Button("搜索", this);
		leftUserSearch.setImmediate(true);
		searchHLayout.addComponent(leftUserSearch);
		searchHLayout.setComponentAlignment(leftUserSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		alternativeUserTable = new Table();
		alternativeUserTable.setStyleName("striped");
		alternativeUserTable.setSelectable(true);
		alternativeUserTable.setMultiSelect(true);
		alternativeUserTable.setWidth("100%");
		alternativeUserTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftUserVLayout.addComponent(alternativeUserTable);

		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
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
		List<User> users = userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId());
		alternativeUserTable.setCaption("可选话务员("+users.size()+")"); 
		
		// 为了方便搜索，暂时改成空字符串
		for(User user : users) {
			if(user.getRealName() == null) {
				user.setRealName("");
			}
		}

		alternativeUserContainer = new BeanItemContainer<User>(User.class);
		alternativeUserContainer.addAll(users);
		alternativeUserContainer.addNestedContainerProperty("department.name");
		alternativeUserTable.setContainerDataSource(alternativeUserContainer);
		alternativeUserTable.setPageLength(20);
		alternativeUserTable.setVisibleColumns(USER_VISIBLE_PROPERTIES);
		alternativeUserTable.setColumnHeaders(USER_COL_HEADERS);
		
		return leftUserVLayout;
	}

	/**
	 * 创建左右两侧客户经理（User）表格中间的操作按钮("部分移动、全部移动"按钮)
	 * return 
	 */
	private VerticalLayout createMiddleUserComponents() {
		VerticalLayout operatorVLayout = new VerticalLayout();
		operatorVLayout.setSpacing(true);
		operatorVLayout.setSizeFull();

		// 占位组件
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));

		// 按钮组件
		addAll = new Button(">>>", this);
		operatorVLayout.addComponent(addAll);
		operatorVLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);

		add = new Button(">>", this);
		operatorVLayout.addComponent(add);
		operatorVLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove = new Button("<<", this);
		operatorVLayout.addComponent(remove);
		operatorVLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);

		removeAll = new Button("<<<", this);
		operatorVLayout.addComponent(removeAll);
		operatorVLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);
		
		// 占位组件
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		
		return operatorVLayout;
	}

	/**
	 * 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件)
	 * return 
	 */
	private VerticalLayout createRightUserComponents() {
		VerticalLayout rightUserVLayout = new VerticalLayout();
		rightUserVLayout.setSpacing(true);
		rightUserVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		rightUserVLayout.addComponent(searchHLayout);

		rightUserKeyword = new TextField();
		rightUserKeyword.setImmediate(true);
		rightUserKeyword.setInputPrompt("请输入搜索关键字");
		rightUserKeyword.setDescription("可按用户名、工号、姓名及部门名称搜索！");
		rightUserKeyword.setStyleName("search");
		rightUserKeyword.addListener(this);
		searchHLayout.addComponent(rightUserKeyword);
		searchHLayout.setComponentAlignment(rightUserKeyword, Alignment.MIDDLE_CENTER);

		rightUserSearch = new Button("搜索", this);
		rightUserSearch.setImmediate(true);
		searchHLayout.addComponent(rightUserSearch);
		searchHLayout.setComponentAlignment(rightUserSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		selectedUserTable = new Table("已选话务员");
		selectedUserTable.setStyleName("striped");
		selectedUserTable.setSelectable(true);
		selectedUserTable.setMultiSelect(true);
		selectedUserTable.setWidth("100%");
		selectedUserTable.setPageLength(20);
		selectedUserTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightUserVLayout.addComponent(selectedUserTable);

		selectedUserContainer = new BeanItemContainer<User>(User.class);
		selectedUserContainer.addNestedContainerProperty("department.name");
		selectedUserTable.setContainerDataSource(selectedUserContainer);
		selectedUserTable.setVisibleColumns(USER_VISIBLE_PROPERTIES);
		selectedUserTable.setColumnHeaders(USER_COL_HEADERS);
		
		// 创建底部右侧组件
		HorizontalLayout bottomRight = new HorizontalLayout();
		bottomRight.setSpacing(true);
		rightUserVLayout.addComponent(bottomRight);
		rightUserVLayout.setComponentAlignment(bottomRight, Alignment.MIDDLE_RIGHT);
	
		// 保存
		save = new Button("保 存", this);
		save.setStyleName("default");
		bottomRight.addComponent(save);
		bottomRight.setComponentAlignment(save, Alignment.MIDDLE_LEFT);
		
		// 取消
		cancel = new Button("取 消", this);
		bottomRight.addComponent(cancel);
		bottomRight.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);
		
		return rightUserVLayout;
	}
	
	/**
	 * 实现表格的拖曳功能
	 * @param acceptCriterion
	 * @param table
	 * @param isDragout 是否为从 UserQueues 中减少成员
	 */
	private void makeTableDragAble(final ClientSideCriterion acceptCriterion, final Table table, final boolean isDragout) {
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {
			public void drop(DragAndDropEvent dropEvent) {
				// 验证传递过来的是否是可传递的对象
				DataBoundTransferable transferable = (DataBoundTransferable) dropEvent.getTransferable();
				
				// 不是BeanItemContainer则不响应，直接返回
				if (!(transferable.getSourceContainer() instanceof BeanItemContainer)) {
					return;
				}

				// 获取源sourceItemId
				User sourceItem = (User) transferable.getItemId();

				// 选中要Drop到的targetItemId
				table.getContainerDataSource().addItem(sourceItem);
				transferable.getSourceContainer().removeItem(sourceItem);

				// 按工号的升序排列
				alternativeUserContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
				selectedUserContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
				
				// 每一次移除表格中的对象后，就重新设置左右两个表格的标题
				initializeTablesCaption();
			}

			public AcceptCriterion getAcceptCriterion() {
				return new com.vaadin.event.dd.acceptcriteria.And(acceptCriterion, AcceptItem.ALL);
			}
		});
	}
	
	/**
	 * 初始化左右两个表格的标题
	 */
	public void initializeTablesCaption() {
		// 如果右侧搜索区域不为空，则需要先将右侧区域置空,再初始化标题
		String rightKeywordStr = ((String) rightUserKeyword.getValue()).trim();
		if(!"".equals(rightKeywordStr)) {
			rightUserKeyword.setValue("");
		}
		
		String leftCaption = "可选话务员 ( " + alternativeUserContainer.size() + " )";
		String rightCaption = "拥有成员 ( " + selectedUserContainer.size() + " )";
		
		alternativeUserTable.setCaption(leftCaption);
		selectedUserTable.setCaption(rightCaption);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == customerKeyword) {
			customerSearch.click();
		} else if(source == leftUserKeyword) {
			leftUserSearch.click();
		} else if(source == rightUserKeyword) {
			rightUserSearch.click();
		} 
	}

	@Override
	public void windowClose(CloseEvent e) {
		// 还原用户表格的内容
		restoreUserContainers();
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == add) {
			addToOpposite(alternativeUserTable, selectedUserTable, false);
			alternativeUserTable.setValue(null);
		} else if (source == addAll) {
			addToOpposite(alternativeUserTable, selectedUserTable, true);
		} else if (source == remove) {
			addToOpposite(selectedUserTable, alternativeUserTable, false);
		} else if (source == removeAll) {
			addToOpposite(selectedUserTable, alternativeUserTable, true);
		} else if (source == customerSearch) {
			executeCustomerSearch();
		} else if (source == leftUserSearch) {
			executeLeftUserSearch();
		} else if (source == rightUserSearch) {
			executeRightUserSearch();
		} else if (source == removeCustomer) {
			removeSelectedCustomer();
		} else if (source == save) {
			excuteSave();
		} else if (source == cancel) {
			// 还原用户表格的内容
			restoreUserContainers();
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * @param tableFrom 从哪个表取数据
	 * @param tableTo	添加到哪个表
	 * @param isAll 	是否转移全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if(tableFrom == null||tableTo == null) return;
		
		//如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if(!isAll && ((Collection<User>) tableFrom.getValue()).size() == 0) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的用户!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<User> csrs = null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			csrs = new ArrayList<User>((Collection<User>)tableFrom.getItemIds());
		} else {
			csrs = (Collection<User>) tableFrom.getValue();
		}
		
		//通过循环来改变TableFrom和TableTo的Item	
		for (User user : csrs) {
			tableFrom.getContainerDataSource().removeItem(user);
			tableTo.getContainerDataSource().addItem(user);
		}
		
		// 初始化表格标题
		initializeTablesCaption();
	}

	/**
	 * 当点击customerSearch 时调用
	 */
	private void executeCustomerSearch() {
		if(customerTableContainer==null) return;
		
		// 删除之前的所有过滤器
		customerTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String customerKeywordStr = ((String) customerKeyword.getValue()).trim();
		Long customerId = 0L;
		if(customerKeywordStr.matches("\\d+")) {
			customerId = Long.parseLong(customerKeywordStr);
		}
		Or compareAll = new Or(
				 new com.vaadin.data.util.filter.Compare.Equal("id", customerId),
	             new Like("name", "%" + customerKeywordStr + "%", true), 
	             new Like("accountManager.empNo", "%" + customerKeywordStr + "%", true));
		customerTableContainer.addContainerFilter(compareAll);
		
		// 按客户id号和客户经理工号的升序排列
		customerTableContainer.sort(new Object[] {"id", "accountManager.empNo"}, new boolean[] {true, true});
	}

	/**
	 * 当点击leftUserSearch 时调用
	 */
	private void executeLeftUserSearch() {
		if(alternativeUserContainer==null) return;
		
		// 删除之前的所有过滤器
		alternativeUserContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String leftKeywordStr = ((String) leftUserKeyword.getValue()).trim();
		
		Or compareAll = new Or(
				 new Like("empNo", "%" + leftKeywordStr + "%", true), 
	             new Like("username", "%" + leftKeywordStr + "%", true), 
	             new Like("realName", "%" + leftKeywordStr + "%", true), 
	             new Like("department.name", "%" + leftKeywordStr + "%", true));
		alternativeUserContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		alternativeUserContainer.sort(new Object[] {"empNo"}, new boolean[] {true});

		// 收索完成后初始化表格的标题
		String leftCaption = "可选成员 ( " + alternativeUserContainer.size() + " )";
		alternativeUserTable.setCaption(leftCaption);
	}

	/**
	 * 当点击rightUserSearch 时调用
	 */
	private void executeRightUserSearch() {
		if(selectedUserContainer==null) return;
		
		// 删除之前的所有过滤器
		selectedUserContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = ((String) rightUserKeyword.getValue()).trim();

		Or compareAll = new Or(
				 new Like("empNo", "%" + rightKeywordStr + "%", true), 
	             new Like("username", "%" + rightKeywordStr + "%", true), 
	             new Like("realName", "%" + rightKeywordStr + "%", true), 
	             new Like("department.name", "%" + rightKeywordStr + "%", true));
		selectedUserContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		selectedUserContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
	}

	/**
	 * 取消迁移选中的客户
	 */
	@SuppressWarnings("unchecked")
	private void removeSelectedCustomer() {
		Set<CustomerResource> selectedCustomerSet = (Set<CustomerResource>) customerTable.getValue();
		
		if(selectedCustomerSet.size() == 0) {
			this.getApplication().getMainWindow().showNotification("取消移交的客户不能为空，请选择客户后重试！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		// 从容器中移除选中项
		for(CustomerResource customer : selectedCustomerSet) {
			customerTableContainer.removeItem(customer);
		}
		customerTable.setValue(null);

		// 设置待移交客户表格的标题
		customerTable.setCaption("待移交客户("+customerTableContainer.size()+")");
	}
	
	/**
	 * 执行客户迁移
	 */
	private void excuteSave() {
		// 如果客户搜索框的值不为空, 则需先将搜索框置空,再初始化标题，这样才能保证迁移的数据时正确的
		String customerKeywordStr = ((String) customerKeyword.getValue()).trim();
		if(!"".equals(customerKeywordStr)) {
			customerKeyword.setValue("");
		}
		
		List<CustomerResource> customers = new ArrayList<CustomerResource>(customerTableContainer.getItemIds());
		if(customers.size() == 0) {
			notification.setCaption("<font color='red'><B>待迁移客户不能为空，请添加客户后重试！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		// 由于加载的时候，如果真实姓名是null 则将其置为了空字符串，所以要还原
		for(CustomerResource customer : customers) {
			if("".equals(customer.getName())) {
				customer.setName(null);
			}
		}
		
		// 如果右侧搜索区域不为空，则需要先将右侧区域置空,再初始化标题，这样才能保证迁移的数据时正确的
		String rightKeywordStr = ((String) rightUserKeyword.getValue()).trim();
		if(!"".equals(rightKeywordStr)) {
			rightUserKeyword.setValue("");
		}
		
		List<User> users = new ArrayList<User>(selectedUserContainer.getItemIds());
		if(users.size() == 0) {
			notification.setCaption("<font color='red'><B>已选话务员不能为空，请选择话务员后重试！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		
		// 由于加载的时候，如果真实姓名是null 则将其置为了空字符串，所以要还原
		for(User user : users) {
			if("".equals(user.getRealName())) {
				user.setRealName(null);
			}
		}
		
		try {
			migrateCustomerToCsrService.migrateCustomer(loginUser, customers, users);
			customerMemberManagement.getSelectedCustomers().clear();
			customerMemberManagement.updateTable(true);
			customerMemberManagement.updateOptButtonStatus();
			// 还原用户表格的内容
			restoreUserContainers();
			notification.setCaption("迁移客户成功！");
			this.getApplication().getMainWindow().showNotification(notification);
			this.getApplication().getMainWindow().removeWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
			notification.setCaption("<font color='red'><B>迁移客户失败，请重试！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
		}
	}
	
	/**
	 * 还原用户表格的Container
	 */
	public void restoreUserContainers() {
		alternativeUserContainer.addAll(selectedUserContainer.getItemIds());
		alternativeUserContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
		selectedUserContainer.removeAllItems();
		
		String leftCaption = "可选话务员( " + alternativeUserContainer.size() + " )";
		String rightCaption = "拥有成员( " + selectedUserContainer.size() + " )";
		
		alternativeUserTable.setCaption(leftCaption);
		selectedUserTable.setCaption(rightCaption);
	}
	
}
