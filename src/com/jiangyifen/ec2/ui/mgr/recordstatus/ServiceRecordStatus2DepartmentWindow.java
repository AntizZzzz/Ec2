package com.jiangyifen.ec2.ui.mgr.recordstatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.ServiceRecordStatus2DepartmentLink;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
 * 按部门设置弹屏界面中显示的客服记录状态内容 组件
 * 	如部门1的弹屏看到的是："停机","关机","成功客户"
 * 	如部门2的弹屏看到的是："停机","预约","成功客户","拒绝"
 * @author jrh
 */
@SuppressWarnings("serial")
public class ServiceRecordStatus2DepartmentWindow extends Window implements ClickListener, ValueChangeListener {

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"statusName", "isAnswered", "isMeanVipCustomer", "direction"};

	private final String[] COL_HEADERS = new String[] {"结果名称", "是否接通", "是否客户", "呼叫方向"};
	
	/**
	 * 主要组件输出
	 */
	// 顶端部门选择组件
	private ComboBox deptSelector;
	private Department nearestSelectedDept;						// 在本次之前最近被选中的队列
	private List<CustomerServiceRecordStatus> oldRecordStatusInDept;		// 某一具体队列所拥有的 “用户-队列”对应关系对象
	private BeanItemContainer<Department> deptContainer;
	
	// 关键字和搜索按钮
	// 左
	private TextField leftKeyword;
	private Button leftSearch;
	// 右
	private TextField rightKeyword;
	private Button rightSearch;

	// 中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;

	// 表格
	private Table leftTable;
	private Table rightTable;
	private BeanItemContainer<CustomerServiceRecordStatus> leftTableContainer;
	private BeanItemContainer<CustomerServiceRecordStatus> rightTableContainer;

	private Button save;
	private Button cancel;
	private Button close;

	private Domain domain;											// 当前登陆用户所属域
	
	private DepartmentService departmentService; 
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;		// 客服记录状态服务类
	
	public ServiceRecordStatus2DepartmentWindow() {
		this.center();
		this.setResizable(false);
		this.setCaption("按部门编辑弹屏显示信息");

		domain = SpringContextHolder.getDomain();
		departmentService = SpringContextHolder.getBean("departmentService");
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		
		deptContainer = new BeanItemContainer<Department>(Department.class);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setWidth("900px");
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		// 创建界面顶部的 队列 选择组件
		createDeptSelectComponents(windowContent);

		// 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮) 
		HorizontalLayout centerHLayout = createCenterHLayout();
		windowContent.addComponent(centerHLayout);
		
		// 创建界面底部组件(保存、取消按钮)
		createBottomComponents(windowContent);

		// 添加拖拽支持
		makeTableDragAble(new SourceIs(rightTable), leftTable, true);
		makeTableDragAble(new SourceIs(leftTable), rightTable, false);
	}

	/**
	 * 创建界面顶部的 队列 选择组件
	 */
	private void createDeptSelectComponents(VerticalLayout windowContent) {
		HorizontalLayout maintopLayout = new HorizontalLayout();
		maintopLayout.setSpacing(true);
		windowContent.addComponent(maintopLayout);
		windowContent.setComponentAlignment(maintopLayout, Alignment.MIDDLE_CENTER);

		Label caption = new Label("<B>部门选择：</B>", Label.CONTENT_XHTML);
		caption.setWidth("-1px");
		maintopLayout.addComponent(caption);

		deptSelector = new ComboBox();
		deptSelector.setNullSelectionAllowed(false);
		deptSelector.setImmediate(true);
		deptSelector.setContainerDataSource(deptContainer);
		deptSelector.setItemCaptionPropertyId("name");
		deptSelector.addListener(this);
		maintopLayout.addComponent(deptSelector);
	}
	
	/** 
	 * 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮)
	 * @return HorizontalLayout
	 */
	private HorizontalLayout createCenterHLayout() {
		HorizontalLayout centerHLayout = new HorizontalLayout();
		centerHLayout.setSpacing(true);
		centerHLayout.setWidth("100%");
		
		// 创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
		VerticalLayout leftComponents = createLeftComponents();
		centerHLayout.addComponent(leftComponents);
		centerHLayout.setExpandRatio(leftComponents, 0.46f);
		
		// 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
		VerticalLayout middleComponents = createMiddleComponents();
		centerHLayout.addComponent(middleComponents);
		centerHLayout.setComponentAlignment(middleComponents, Alignment.MIDDLE_CENTER);
		centerHLayout.setExpandRatio(middleComponents, 0.08f);

		// 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件)
		VerticalLayout rightComponents = createRightComponents();
		centerHLayout.addComponent(rightComponents);
		centerHLayout.setExpandRatio(rightComponents, 0.46f);
		
		return centerHLayout;
	}
	
	/**
	 *  创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
	 * @return
	 */
	 
	private VerticalLayout createLeftComponents() {
		VerticalLayout leftVLayout = new VerticalLayout();
		leftVLayout.setSpacing(true);
		leftVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		leftVLayout.addComponent(searchHLayout);

		Label caption = new Label("结果名称：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		searchHLayout.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);

		leftKeyword = new TextField();
		leftKeyword.setImmediate(true);
		leftKeyword.setStyleName("search");
		leftKeyword.addListener(this);
		searchHLayout.addComponent(leftKeyword);
		searchHLayout.setComponentAlignment(leftKeyword, Alignment.MIDDLE_CENTER);

		leftSearch = new Button("搜索", this);
		leftSearch.setImmediate(true);
		searchHLayout.addComponent(leftSearch);
		searchHLayout.setComponentAlignment(leftSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		// 表格
		leftTable = createFormatColumnTable("可选成员");
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftVLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setPageLength(16);
		leftTable.setVisibleColumns(VISIBLE_PROPERTIES);
		leftTable.setColumnHeaders(COL_HEADERS);
		
		return leftVLayout;
	}

	/**
	 * 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
	 * return 
	 */
	private VerticalLayout createMiddleComponents() {
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
	private VerticalLayout createRightComponents() {
		VerticalLayout rightVLayout = new VerticalLayout();
		rightVLayout.setSpacing(true);
		rightVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		rightVLayout.addComponent(searchHLayout);

		Label caption = new Label("结果名称：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		searchHLayout.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);

		rightKeyword = new TextField();
		rightKeyword.setImmediate(true);
		rightKeyword.setStyleName("search");
		rightKeyword.addListener(this);
		searchHLayout.addComponent(rightKeyword);
		searchHLayout.setComponentAlignment(rightKeyword, Alignment.MIDDLE_CENTER);

		rightSearch = new Button("搜索", this);
		rightSearch.setImmediate(true);
		searchHLayout.addComponent(rightSearch);
		searchHLayout.setComponentAlignment(rightSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		// 表格
		rightTable = createFormatColumnTable("拥有成员");
		rightTable.setStyleName("striped");
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTable.setPageLength(16);
		rightTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightVLayout.addComponent(rightTable);

		rightTableContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(VISIBLE_PROPERTIES);
		rightTable.setColumnHeaders(COL_HEADERS);
		
		return rightVLayout;
	}

	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable(String caption) {
		return new Table(caption) {
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if("isAnswered".equals(colId)) {
					boolean isAnswered = (Boolean) property.getValue();
					return isAnswered ? "接通" : "未接通";
				} else if("isMeanVipCustomer".equals(colId)) {
					boolean isAnswered = (Boolean) property.getValue();
					return isAnswered ? "是" : "不是";
				} else if("enabled".equals(colId)) {
					boolean isAnswered = (Boolean) property.getValue();
					return isAnswered ? "可用" : "不可用";
				} else if("direction".equals(colId)) {
					boolean isOutgoing = "outgoing".equals((String) property.getValue());
					return isOutgoing ? "呼出" : "呼入";
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 * 创建界面底部组件(保存、取消按钮)
	 */
	private void createBottomComponents(VerticalLayout windowContent) {
		HorizontalLayout bottomHLayout = new HorizontalLayout();
		bottomHLayout.setSpacing(true);
		bottomHLayout.setWidth("100%");
		windowContent.addComponent(bottomHLayout);
		
		// 表格底部左侧组件
		HorizontalLayout bottomLeft = new HorizontalLayout();
		bottomLeft.setSpacing(true);
		bottomLeft.setWidth("-1px");
		bottomHLayout.addComponent(bottomLeft);
		bottomHLayout.setComponentAlignment(bottomLeft, Alignment.MIDDLE_LEFT);
		
		// 保存
		save = new Button("保 存", this);
		save.setStyleName("default");
		bottomLeft.addComponent(save);
		bottomLeft.setComponentAlignment(save, Alignment.MIDDLE_LEFT);
		
		// 取消
		cancel = new Button("取 消", this);
		bottomLeft.addComponent(cancel);
		bottomLeft.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);
		
		// 关闭
		close = new Button("关 闭", this);
		bottomLeft.addComponent(close);
		bottomLeft.setComponentAlignment(close, Alignment.MIDDLE_LEFT);

		// 将保存和取消按钮置为不可用状态
		setButtonsEnable(false);
	}
	
	/**
	 * 实现表格的拖曳功能
	 * @param acceptCriterion
	 * @param table
	 * @param isDragout 是否为从 CustomerServiceRecordStatuss 中减少成员
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
				CustomerServiceRecordStatus sourceItem = (CustomerServiceRecordStatus) transferable.getItemId();

				// 选中要Drop到的targetItemId
				table.getContainerDataSource().addItem(sourceItem);
				transferable.getSourceContainer().removeItem(sourceItem);

				// 按工号的升序排列
				leftTableContainer.sort(new Object[] {"direction", "statusName"}, new boolean[] {true, true});
				rightTableContainer.sort(new Object[] {"direction", "statusName"}, new boolean[] {true, true});
				
				// 每一次移除表格中的对象后，就重新设置左右两个表格的标题
				initializeTablesCaption();
				
				// 表格中的数据发生变化后，将保存和取消按钮置为可用
				setButtonsEnable(true);
			}

			public AcceptCriterion getAcceptCriterion() {
				return new com.vaadin.event.dd.acceptcriteria.And(acceptCriterion, AcceptItem.ALL);
			}
		});
	}
	
	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * @param tableFrom 从哪个表取数据
	 * @param tableTo	添加到哪个表
	 * @param isAll 是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if(tableFrom == null||tableTo == null) return;
		
		//如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if(!isAll && ((Collection<CustomerServiceRecordStatus>) tableFrom.getValue()).size() == 0) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的客服记录状态!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<CustomerServiceRecordStatus> csrs = null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			csrs = new ArrayList<CustomerServiceRecordStatus>((Collection<CustomerServiceRecordStatus>)tableFrom.getItemIds());
		} else {
			csrs = (Collection<CustomerServiceRecordStatus>) tableFrom.getValue();
		}
		
		//通过循环来改变TableFrom和TableTo的Item	
		for (CustomerServiceRecordStatus recordStatus : csrs) {
			tableFrom.getContainerDataSource().removeItem(recordStatus);
			tableTo.getContainerDataSource().addItem(recordStatus);
		}
		
		// 将保存与取消按钮置为 可用状态
		setButtonsEnable(true);

		// 初始化表格标题
		initializeTablesCaption();
	}
	
	/**
	 * 由buttonClick调用，执行生成左侧Table的过滤器,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		if(leftTableContainer==null) return;
		
		// 删除之前的所有过滤器
		leftTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String leftKeywordStr = ((String) leftKeyword.getValue()).trim();
		
		Or compareAll = new Or(new Like("statusName", "%" + leftKeywordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		leftTableContainer.sort(new Object[] {"direction", "statusName"}, new boolean[] {true, true});
		
		// 收索完成后初始化表格的标题
		String leftCaption = "可选成员 ( " + leftTableContainer.size() + " )";
		leftTable.setCaption(leftCaption);
	}
	
	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if(rightTableContainer==null) return;
		
		// 删除之前的所有过滤器
		rightTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = ((String) rightKeyword.getValue()).trim();

		Or compareAll = new Or(new Like("statusName", "%" + rightKeywordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		rightTableContainer.sort(new Object[] {"direction", "statusName"}, new boolean[] {true, true});
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == deptSelector) {
			Department dept = (Department) source.getValue();
			nearestSelectedDept = dept;
			updateTableSource(dept);
			leftKeyword.setValue("");
			rightKeyword.setValue("");
			initializeTablesCaption();
		} else if(source == leftKeyword) {
			leftSearch.click();
		} else if(source == rightKeyword) {
			rightSearch.click();
		} 
	}
	
	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == leftSearch) {
			executeLeftSearch();
		} else if (source == add) {
			addToOpposite(leftTable, rightTable, false);
			leftTable.setValue(null);
		} else if (source == addAll) {
			addToOpposite(leftTable, rightTable, true);
		} else if (source == remove) {
			addToOpposite(rightTable, leftTable, false);
		} else if (source == removeAll) {
			addToOpposite(rightTable, leftTable, true);
		} else if (source == rightSearch) {
			executeRightSearch();
		} else if (source == save) {
			// 如果右侧搜索区域不为空，则需要先将右侧区域置空，再保存
			String rightKeywordStr = StringUtils.trimToEmpty((String) rightKeyword.getValue());
			if(!"".equals(rightKeywordStr)) {
				rightKeyword.setValue("");
			}
			// 执行保存
			excuteSave();
		} else if (source == cancel) {
			// 恢复表格中的数据源
			if(nearestSelectedDept != null) {
				updateTableSource(nearestSelectedDept);
			} else {
				updateTableSource(null);
			}
			// 更新表格的标题
			initializeTablesCaption();
			// 将保存与取消按钮置为不可用状态
			setButtonsEnable(false);
		} else if (source == close) {
			close.getApplication().getMainWindow().removeWindow(this);
		} 
	}

	/**
	 * 将保存 和 取消 按钮置为可用 或者不可用
	 * @param enabled
	 */
	public void setButtonsEnable(boolean enabled) {
		save.setEnabled(enabled);
		cancel.setEnabled(enabled);
	}

	/**
	 * 执行保存操作
	 */
	private void excuteSave() {
		//更新队列拥有的客服记录状态
		Set<CustomerServiceRecordStatus> currentSelectedStatuses = new HashSet<CustomerServiceRecordStatus>((Collection<CustomerServiceRecordStatus>)rightTableContainer.getItemIds());
		
		// 循环检验，添加新的 CustomerServiceRecordStatus
		for(CustomerServiceRecordStatus currentSelectedStatus1 : currentSelectedStatuses) {
			// 默认是该用户队列在数据库中不存在
			CustomerServiceRecordStatus oldSelectedStatus1 = null;
			
			// 检验是否已经存在用户 recordStatus ，如果存在则将oldPriority 置为 其老的优先级
			for(CustomerServiceRecordStatus oldStatus : oldRecordStatusInDept) {
				if(currentSelectedStatus1.getId().equals(oldStatus.getId())) {
					oldSelectedStatus1 = oldStatus;
					break;
				}
			}
			
			// 不存在，则添加到数据库
			if(oldSelectedStatus1 == null) {
				ServiceRecordStatus2DepartmentLink status2DeptLink = new ServiceRecordStatus2DepartmentLink();
				status2DeptLink.setDepartmentId(nearestSelectedDept.getId());
				status2DeptLink.setServiceRecordStatusId(currentSelectedStatus1.getId());
				customerServiceRecordStatusService.saveStatus2DeptLink(status2DeptLink);
			} 
		}
		
		// 循环检验，删除移走的CustomerServiceRecordStatus
		for(CustomerServiceRecordStatus oldSelectedStatus2 : oldRecordStatusInDept) {
			// 默认是该用户不存在与队列中
			boolean hasRemoved = true;
			
			// 检验是否已经移除了用户名 为recordStatusname 的 用户 recordStatus ，如果没移除，则将 hasRemoved 置为 false
			for(CustomerServiceRecordStatus recordStatus : currentSelectedStatuses) {
				if(oldSelectedStatus2.getId().equals(recordStatus.getId())) {
					hasRemoved = false;
					break;
				}
			}
			
			// 如果已经移除了该条 recordStatus 则执行删除操作
			if(hasRemoved == true) {
				customerServiceRecordStatusService.deleteStatus2DeptLinkByIds(oldSelectedStatus2.getId(), nearestSelectedDept.getId());
			}
		}
		
		// 更新表格数据源
		updateTableSource(nearestSelectedDept);
		
		// 将保存与取消按钮置为不可用状态
		setButtonsEnable(false);
		
		this.getApplication().getMainWindow().showNotification("保存成功！");
	}

	/**
	 * 重新获取数据源，并刷新两个表格的显示内容
	 * @param dept 
	 */
	private void updateTableSource(Department dept) {
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		if(dept == null) {
			return;
		}
		List<CustomerServiceRecordStatus> recordStatuses = customerServiceRecordStatusService.getAllByEnable(true, domain.getId());
		oldRecordStatusInDept = customerServiceRecordStatusService.getAllByDeptIdAndDirection(dept.getId(), "inAndOut", true, domain.getId());
		
		for(CustomerServiceRecordStatus recordStatus : recordStatuses) {
			if(oldRecordStatusInDept.size() == 0) {
				leftTableContainer.addAll(recordStatuses);
				return;
			}
			
			// 用于标示当前recordStatus 对象是否应该加入到右侧表格当做，如果是，则将其值置为 true ,默认为不是
			boolean needAddToRightTable = false;
			for(CustomerServiceRecordStatus oldSelectedStatus2 : oldRecordStatusInDept) {
				if(recordStatus.getId().equals(oldSelectedStatus2.getId())) {
					rightTableContainer.addBean(recordStatus);
					needAddToRightTable = true;
					break;
				}
			}
			
			// 如果当前recordStatus 不是加入到右侧表格中的，那么就将其加入左侧表格
			if(needAddToRightTable == false) {
				leftTableContainer.addBean(recordStatus);
			}
		}
	}
	
	/**
	 * 初始化左右两个表格的标题
	 */
	public void initializeTablesCaption() {
		// 如果右侧搜索区域不为空，则需要先将右侧区域置空,再初始化标题
		String rightKeywordStr = ((String) rightKeyword.getValue()).trim();
		if(!"".equals(rightKeywordStr)) {
			rightKeyword.setValue("");
		}
		
		String leftCaption = "可选成员 ( " + leftTable.getContainerDataSource().size() + " )";
		String rightCaption = "拥有成员 ( " + rightTable.getContainerDataSource().size() + " )";
		
		leftTable.setCaption(leftCaption);
		rightTable.setCaption(rightCaption);
	}

	/**
	 * 刷新显示队列的表格
	 */
	public void updateTable() {
		List<Department> allDepts = departmentService.getAll(domain);
		deptContainer.removeAllItems();
		deptContainer.addAll(allDepts);
		
		// 回显信息
		if(nearestSelectedDept != null) {	// 如果之前选中的部门不为空
			boolean isExisted = false;
			for(Department dept : allDepts) {
				if(dept.getId() != null && dept.getId().equals(nearestSelectedDept.getId())) {
					deptSelector.select(dept);
					isExisted = true;
					break;
				}
			}
			if(isExisted == false && allDepts.size() > 0) {
				deptSelector.select(allDepts.get(0));
			} else if(isExisted == false && allDepts.size() == 0) {
				deptSelector.select(null);
			}
		} else if(allDepts.size() > 0) {		// 如果之前没有选择任何部门，而该域中的部门不为空，则默认选择第一条部门
			deptSelector.select(allDepts.get(0));
		} else {	// 如果之前没有选择任何部门，并且该域没有部门，则将部门选择框置为空
			deptSelector.select(null);
		}
	}
	
}
