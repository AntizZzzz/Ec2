package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.mgr.DistributeResourceToProjecService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 为项目添加Resource
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class AssignResource extends Window implements Button.ClickListener {
	/**
	 * 主要组件输出
	 */
	// 关键字和搜索按钮
	// 左
	private TextField leftKeyWord;
	private Button leftSearch;
	// 右
	private TextField rightKeyWord;
	private Button rightSearch;

	// 中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;

	// 表格
	private Table leftTable;
	private Object[] columns = new String[] { "id", "batchName", "user.username", "user.realName" };
	private String[] headers = new String[] { "ID", "批次名", "创建者用户名", "创建者姓名" };
	private Table rightTable;
	private BeanItemContainer<CustomerResourceBatch> leftTableContainer;
	private BeanItemContainer<CustomerResourceBatch> rightTableContainer;
	// 下面的添加和取消按钮
	private Button assign;
	private Button cancel;

	//进度条组件
	private VerticalLayout progressOuterLayout;
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	
	private Window mainWindow;
	
	
	/**
	 * 其他组件输出
	 */
	private Domain domain;
	private User loginUser;
	private DepartmentService departmentService;
	private CustomerResourceBatchService customerResourceBatchService;
	private DistributeResourceToProjecService distributeResourceToProjecService;
	private MarketingProjectService marketingProjectService;
	private CommonService commonService;
	// 持有从Table取出来的MarketingProject引用
	private MarketingProject project;
	// 持有调用它的组件引用ProjectControl,以刷新父组件
	private ProjectControl projectControl;
	// 右侧组件刚开始批次数据，用于计算本次分配的数目
	private int originalSize;
	// 右侧组件中的原始批次数据，用来控制不能移除的操作
	private Set<CustomerResourceBatch> rightTableOriginalSet;
	//此全局变量供excuteAssign和confirmDelete使用
	private Collection<CustomerResourceBatch> batches;

	/**
	 * 构造器
	 * 
	 * @param project
	 */
	public AssignResource(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.projectControl = projectControl;

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		/* =====中间的两个表格和按钮========= */
		HorizontalLayout tempTablesLayout = buildTablesLayout();
		windowContent.addComponent(tempTablesLayout);
		windowContent.setExpandRatio(tempTablesLayout, 1.0f);

		//创建进度条组件
		progressOuterLayout=projectControl.getProgressLayout();
		progressLayout=new HorizontalLayout();
		progressOuterLayout.addComponent(progressLayout);
		
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
		
		/* =========下面的按钮输出=========== */
		HorizontalLayout tempButtonsLayout = buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout,
				Alignment.BOTTOM_RIGHT);

		// 设置按钮样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
		// 添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		// 对于移除的批次任务进行删除操作
		commonService = SpringContextHolder.getBean("commonService");
		departmentService=SpringContextHolder.getBean("departmentService");
		customerResourceBatchService=SpringContextHolder.getBean("customerResourceBatchService");
		distributeResourceToProjecService=SpringContextHolder.getBean("distributeResourceToProjecService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
	}

	/**
	 * 创建主要输出部分，中间的两个Table组件和Table按钮组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildTablesLayout() {
		HorizontalLayout tablesLayout = new HorizontalLayout();
		tablesLayout.setSpacing(true);
		// 要想剔除左侧Table包含的右侧Table的Item，必须先创建右侧组件，再创建左侧组件
		VerticalLayout tempRight = buildRight();
		VerticalLayout tempLeft = buildLeft();

		// 左侧输出
		tablesLayout.addComponent(tempLeft);
		tablesLayout.setExpandRatio(tempLeft, 8.0f);

		// 中间输出
		VerticalLayout tempCenter = buildCenter();
		tablesLayout.addComponent(tempCenter);
		tablesLayout.setExpandRatio(tempCenter, 1.5f);

		// 右侧输出
		tablesLayout.addComponent(tempRight);
		tablesLayout.setExpandRatio(tempRight, 8.0f);
		return tablesLayout;
	}

	/**
	 * 添加Csr左侧输出
	 * 
	 * @return
	 */
	private VerticalLayout buildLeft() {
		// 左侧
		VerticalLayout leftLayout = new VerticalLayout();

		leftLayout.setSpacing(true);

		// 搜索组件
		leftLayout.addComponent(buildLeftSearch());

		// Table的状态Label
		leftLayout.addComponent(new Label("全部批次"));

		// 初始化Sql语句
		leftSearch.click();

		// 表格组件 和翻页组件
		leftLayout.addComponent(buildLeftTable());

		return leftLayout;
	}

	/**
	 * 添加Csr中间输出
	 * 
	 * @return
	 */
	private VerticalLayout buildCenter() {
		VerticalLayout centerLayout = new VerticalLayout();
		centerLayout.setSpacing(true);
		// 占位组件
		centerLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));

		// 按钮组件
		addAll = new Button(">>>");
		addAll.addListener((Button.ClickListener) this);
		centerLayout.addComponent(addAll);
		centerLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);

		add = new Button(">>");
		add.addListener((Button.ClickListener) this);
		centerLayout.addComponent(add);
		centerLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove = new Button("<<");
		remove.addListener((Button.ClickListener) this);
		centerLayout.addComponent(remove);
		centerLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);

		removeAll = new Button("<<<");
		removeAll.addListener((Button.ClickListener) this);
		centerLayout.addComponent(removeAll);
		centerLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);

		return centerLayout;
	}

	/**
	 * 创建此窗口最下边按钮的输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("23%");
		// 添加
		assign = new Button("确定");
		assign.addListener((Button.ClickListener) this);
		buttonsLayout.addComponent(assign);
		buttonsLayout.setComponentAlignment(assign, Alignment.MIDDLE_CENTER);
		// 取消
		cancel = new Button("取消");
		cancel.addListener((Button.ClickListener) this);
		buttonsLayout.addComponent(cancel);
		buttonsLayout.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		return buttonsLayout;
	}

	/**
	 * 由 buildLeft 调用，左侧的搜索组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildLeftSearch() {
		HorizontalLayout leftSearchLayout = new HorizontalLayout();
		leftSearchLayout.setSpacing(true);

		HorizontalLayout constraintLayout = new HorizontalLayout();
		constraintLayout.addComponent(new Label("关键字"));
		// 关键字
		leftKeyWord = new TextField();
		leftKeyWord.setWidth("5em");
		leftKeyWord.setStyleName("search");
		leftKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(leftKeyWord);
		leftSearchLayout.addComponent(constraintLayout);

		// 搜索按钮
		leftSearch = new Button("搜索");
		leftSearch.addListener((Button.ClickListener) this);
		leftSearchLayout.addComponent(leftSearch);

		return leftSearchLayout;
	}

	/**
	 * 添加Csr右侧输出
	 * 
	 * @return
	 */
	private VerticalLayout buildRight() {
		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setSpacing(true);
		// 搜索组件
		rightLayout.addComponent(buildRightSearch());

		// Table的状态Label
		rightLayout.addComponent(new Label("已选批次"));

		// 表格组件
		rightLayout.addComponent(buildRightTable());
		return rightLayout;
	}

	/**
	 * 由 buildRight 调用，右侧的搜索组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildRightSearch() {
		HorizontalLayout rightSearchLayout = new HorizontalLayout();
		rightSearchLayout.setSpacing(true);

		HorizontalLayout constraintLayout = new HorizontalLayout();
		constraintLayout.addComponent(new Label("关键字"));
		// 关键字
		rightKeyWord = new TextField();
		rightKeyWord.setWidth("5em");
		rightKeyWord.setStyleName("search");
		rightKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(rightKeyWord);
		rightSearchLayout.addComponent(constraintLayout);

		// 搜索按钮
		rightSearch = new Button("搜索");
		rightSearch.addListener((Button.ClickListener) this);
		rightSearchLayout.addComponent(rightSearch);

		return rightSearchLayout;
	}

	/**
	 * 由 buildLeft 调用，左侧表格和翻页组件,此时sqlSelect和sqlCount应该已经被初始化
	 * 
	 * @return
	 */
	private VerticalLayout buildLeftTable() {
		VerticalLayout tableLayout = new VerticalLayout();
		tableLayout.setWidth("100%");
		// 表格
		leftTable = new Table();
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		tableLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<CustomerResourceBatch>(
				CustomerResourceBatch.class);
		leftTableContainer.addNestedContainerProperty("user.username");
		leftTableContainer.addNestedContainerProperty("user.realName");
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setPageLength(10);
		leftTable.setVisibleColumns(columns);
		leftTable.setColumnHeaders(headers);
		// 设置表格的样式
		this.setStyleGeneratorForTable(leftTable);
		// 设置生成的备注信息
		this.addColumn(leftTable);
		return tableLayout;
	}

	/**
	 * 由buildRight调用，创建右侧表格组件,由于批次信息没法回显，也不应该回显，每次总是空的
	 * 
	 * @return
	 */
	private VerticalLayout buildRightTable() {
		VerticalLayout tableLayout = new VerticalLayout();
		tableLayout.setWidth("100%");
		// 表格
		rightTable = new Table();
		rightTable.setStyleName("striped");
		rightTable.setPageLength(10);
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setImmediate(true);
		rightTable.setWidth("100%");
		rightTableContainer = new BeanItemContainer<CustomerResourceBatch>(
				CustomerResourceBatch.class);
		rightTable.setContainerDataSource(rightTableContainer);
		rightTableContainer.addNestedContainerProperty("user.username");
		rightTableContainer.addNestedContainerProperty("user.realName");
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		// 设置表格的样式
		this.setStyleGeneratorForRightTable(rightTable);
		// 设置生成的备注信息
		this.addColumn(rightTable);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}

	/**
	 * 由 buildLeftTableAndFlip 和 buildRightTable 调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("备注", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Object note = table.getContainerDataSource()
						.getContainerProperty(itemId, "note");
				String longNote = "";
				if (note != null && note.toString() != null) {
					longNote = note.toString();
				}
				String shortNote = longNote;
				if (shortNote.length() < 5) {
					shortNote += "...";
				} else {
					shortNote = shortNote.substring(0, 5) + "...";
				}
				Label label = new Label(shortNote);
				label.setDescription(longNote);
				return label;
			}

		});
	}

	/**
	 * 由buildLeftTableAndFlip 和 buildRightTable 调用，设置表格行显示格式
	 * 
	 * @param batchTable2
	 */
	private void setStyleGeneratorForTable(final Table batchTable2) {
		// style generator
		batchTable2.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				// if (propertyId == null) {
				// Object
				// obj=batchTable2.getContainerDataSource().getContainerProperty(itemId,
				// "isGenerated").getValue();
				// Boolean isGenerated=false;
				// if(obj!=null){
				// isGenerated=(Boolean)obj;
				// }
				// return
				// isGenerated?StyleConfig.RESOURCE_IMPORT_FILTER_COLOR:StyleConfig.RESOURCE_IMPORT_BATCH_COLOR;
				// }else{
				// return null;
				// }
				return null;
			}
		});
	}

	/**
	 * 由buildLeftTableAndFlip 和 buildRightTable 调用，设置表格行显示格式
	 * 
	 * @param batchTable2
	 */
	private void setStyleGeneratorForRightTable(final Table batchTable3) {
		// style generator
		batchTable3.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				// if (propertyId == null) {
				// Object
				// obj=batchTable3.getContainerDataSource().getContainerProperty(itemId,
				// "isGenerated").getValue();
				// Boolean isGenerated=false;
				// if(obj!=null){
				// isGenerated=(Boolean)obj;
				// }
				// return
				// isGenerated?StyleConfig.RESOURCE_IMPORT_FILTER_COLOR:StyleConfig.RESOURCE_IMPORT_BATCH_COLOR;
				// }else{
				// return null;
				// }
				return null;
			}
		});
	}

	/**
	 * 由buildRightTable和attach调用，更新右侧表格的回显信息
	 */
	private void updateRightTable() {
		rightTableContainer.removeAllItems();

		// 先让right初始化，然后设置Container，从而生成想要的样式
		rightTableOriginalSet = project.getBatches();
		
		//记录下弹出窗口时所选的项目的批次，以及批次的数量
		// jrh 为了可以按话务员姓名进行搜索，如果不设置，查询时，会报空指针异常
		for(CustomerResourceBatch crb : rightTableOriginalSet) {
			User user = crb.getUser();
			if(user.getRealName() == null) {
				user.setRealName("");
			}
		}
		rightTableContainer.addAll(rightTableOriginalSet);
		originalSize = rightTableOriginalSet.size();
	}

	/**
	 * 更新左侧表格
	 */
	private void updateLeftTable() {
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
		
		// jrh 只能指派本部门及其所有角色的管辖部门创建的批次
		List<CustomerResourceBatch> entitys = new ArrayList<CustomerResourceBatch>();
		for(CustomerResourceBatch batch : customerResourceBatchService.getAllBatches(domain)) {
			for(Long deptId : allGovernedDeptIds) {
				if(batch.getUser().getDepartment().getId() != null && batch.getUser().getDepartment().getId().equals(deptId)) {
					entitys.add(batch);
					break;
				}
			}
		}
		
		// 移除在右侧组件中出现的Item
		Collection<CustomerResourceBatch> batches =rightTableContainer.getItemIds();
		//记录将
		List<CustomerResourceBatch> toRemove=new ArrayList<CustomerResourceBatch>();
		for (CustomerResourceBatch leftBatch : entitys) {
			//移除不可用的批次
			if(leftBatch.getBatchStatus()==BatchStatus.UNUSEABLE){
				toRemove.add(leftBatch);
				continue;
			}
			//移除出现在右边的批次
			for (CustomerResourceBatch rightBatch : batches) {
				if(leftBatch.getId().equals(rightBatch.getId())){
					toRemove.add(leftBatch);
				}
			}
		}
		//从Entity中移除实体
		for(int i=0;i<toRemove.size();i++){
			entitys.remove(toRemove.get(i));
		}
		//向左组件中添加信息
		leftTableContainer.removeAllItems();
		// jrh 为了可以按话务员姓名进行搜索，如果不设置，查询时，会报空指针异常
		for(CustomerResourceBatch crb : entitys) {
			User user = crb.getUser();
			if(user.getRealName() == null) {
				user.setRealName("");
			}
		}
		leftTableContainer.addAll(entitys);
	}

	/**
	 * 由 构造器 调用，设置按钮的样式
	 * 
	 * @param style
	 */
	private void setButtonsStyle(String style) {
		style = "small";
		leftSearch.setStyleName(style);
		rightSearch.setStyleName(style);
		add.setStyleName(style);
		addAll.setStyleName(style);
		remove.setStyleName(style);
		removeAll.setStyleName(style);
	}

	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * 
	 * @param tableFrom
	 *            从哪个表取数据
	 * @param tableTo
	 *            添加到哪个表
	 * @param isAddAll
	 *            是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if (tableFrom == null || tableTo == null)
			return;

		// 如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if (!isAll)
			if (tableFrom.getValue() == null) {
				this.getApplication()
						.getMainWindow()
						.showNotification("请选择要添加或移除的批次!",
								Window.Notification.TYPE_HUMANIZED_MESSAGE);
				return;
			}

		// 从tableFrom中取出所有选中的Csr
		Collection<CustomerResourceBatch> batches = null;
		if (isAll) {
			// 出现 java.util.ConcurrentModificationException异常，所以包装
			batches = new ArrayList<CustomerResourceBatch>(
					(Collection<CustomerResourceBatch>) tableFrom.getItemIds());
		} else {
			batches = (Collection<CustomerResourceBatch>) tableFrom.getValue();
		}
		// 通过循环来改变TableFrom和TableTo的Item
		for (CustomerResourceBatch batch : batches) {
			tableFrom.getContainerDataSource().removeItem(batch);
			tableTo.getContainerDataSource().addItem(batch);
		}
	}

	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		// 搜索符合条件的批次信息
		if(rightTableContainer==null) return;
		rightTableContainer.removeAllContainerFilters();
		// 关键字
		String rightKeyWordStr = "";
		if (rightKeyWordStr.equals("")) {
			rightKeyWordStr = rightKeyWord.getValue().toString();
		}
		Or compareAll = new Or(new Like("batchName", "%" + rightKeyWordStr + "%", false), 
				new Like("user.username", "%" + rightKeyWordStr + "%", false), 
				new Like("user.realName", "%" + rightKeyWordStr + "%", false), 
				new Like("note", "%" + rightKeyWordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 将搜索结果按批次名称升序排列
		rightTableContainer.sort(new Object[] {"batchName"}, new boolean[] {true});
	}

	/**
	 * 由buttonClick调用，执行生成左侧Table的搜索的Sql,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		// 搜索符合条件的批次信息
		if(leftTableContainer==null) return;
		leftTableContainer.removeAllContainerFilters();
		// 关键字
		String leftKeyWordStr = "";
		if (leftKeyWordStr.equals("")) {
			leftKeyWordStr = leftKeyWord.getValue().toString();
		}
		Or compareAll = new Or(new Like("batchName", "%" + leftKeyWordStr + "%", false), 
				new Like("user.username", "%" + leftKeyWordStr + "%", false), 
				new Like("user.realName", "%" + leftKeyWordStr + "%", false), 
				new Like("note", "%" + leftKeyWordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 将搜索结果按批次名称升序排列
		leftTableContainer.sort(new Object[] {"batchName"}, new boolean[] {true});
	}

	/**
	 * 执行分配任务
	 */
	@SuppressWarnings("unchecked")
	private void executeAssign() {
		leftKeyWord.setValue("");
		executeLeftSearch();
		rightKeyWord.setValue("");
		executeRightSearch();
		
		
		//初始化进度条
		pi.setEnabled(true);
		pi.setValue(0f);
		progressLayout.addComponent(new Label("添加移除进度:"));
		progressLayout.addComponent(pi);
				
		// 获取选中批次的批次Id集合
		batches = (Collection<CustomerResourceBatch>) rightTable.getItemIds();
		
		// 组装成Map和Set集合，key批次的Id，Value为批次
		Map<Long, CustomerResourceBatch> batchMap = new HashMap<Long, CustomerResourceBatch>();
		for (CustomerResourceBatch batch : batches) {
			batchMap.put(batch.getId(), batch);
		}
		//取出Map中的批次的Id的集合
		Set<Long> rightTableBatchIdsSet = batchMap.keySet();

		/**==============获取将要被移除批次域项目中其它批次的交集信息，并存入到msgList中================**/
		// 删除某个批次可能会删除其他批次相关记录的提示信息
		List<String> msgList = new ArrayList<String>();

		//对原来的批次进行循环
		if(rightTableBatchIdsSet.size() > 0) {	// jrh  只有不是移除所有批次，才需要判断是否存在重复资源
			for (CustomerResourceBatch oriBatch : rightTableOriginalSet) {
				// 如果已经添加过的批次不在现在的新批次中,则删除项目中对应Task 时应该有的提示信息
				if (!batches.contains(oriBatch)) {//如果现在右侧组件中的批次不包含原来的批次，则查看目前要移除的批次是不是与现有的其它批次存在交集，如果有显示提示信息
					Long batchId = oriBatch.getId();//现在将要移除的批次
					//select a in(select b in (select c where d))
					//select c where d ,取出一个批次的所有资源Id
					//select b in (select c where d) ,取出在Task中被使用的指定项目的此批次资源的Id
					//select a in(select b in (select c where d)) ，根据资源选出资源对应的批次
					//批次的Id对应可以隐含域的概念，project.getId()可以对应项目
					
					// jrh 修改 原因：无论一个批次中的资源是否与其他批次中重复，它在项目中都会有对应的任务，所以之前的第二部嵌套查询是多余的
					String getBatchSql = "select customerresourcebatches_id from ec2_customer_resource_ec2_customer_resource_batch where customerresources_id in("
							+ "select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
							+ "where customerresourcebatches_id=" + batchId + ")";
					
//				String getBatchSql2 = "select customerresourcebatches_id from ec2_customer_resource_ec2_customer_resource_batch where customerresources_id in("
//						+ "select customerresource_id from ec2_marketing_project_task where marketingproject_id="+project.getId()+" and customerresource_id in("
//						+ "select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch "
//						+ "where customerresourcebatches_id=" + batchId + "))";
					List<Long> batchIdList = (List<Long>) commonService.excuteNativeSql(getBatchSql, ExecuteType.RESULT_LIST);

					//对批次进行去重操作，不使用distict语句,此处不排序，不使用distinct应该没有副作用
					Set<Long> batchIds=new HashSet<Long>(batchIdList);
					// 此处由于查找的是要删除的批次，则此批次一定没有出现在右侧表格中
					//对于出现在此批次中资源的交集批次的Id进行循环
//	TODO jrh  可以考虑，如果移除的批次中的资源在其他批次中也存在，则只移除仅存在于当前被移除批次中的资源
					for (Long tempId : batchIds) {
						// 如果交集中的批次出现在右侧表格中，则将提示信息添加到msgList中
						if (rightTableBatchIdsSet.contains(tempId)) {
							msgList.add("移除批次 <B>" + oriBatch.getBatchName() + "</B> 会同时移除批次 <B>"
									+ batchMap.get(tempId).getBatchName()
									+ " </B>的若干共用资源！");
						}
					}
				}
			}
		}

		/**==============查看msgList，如果List中没有信息，则批次资源没有冲突，以下是没有冲突的处理流程================**/
		// 没有批次资源冲突情况，直接移除
		if (msgList.isEmpty()) {
			/**=====查看msgList，如果List中没有信息，则批次资源没有冲突，直接删除=====**/
			for (CustomerResourceBatch batch : rightTableOriginalSet) {
				// 如果已经添加过的批次不在现在的新批次中,则删除项目中对应Task
				if (!batches.contains(batch)) {
					Long batchId = batch.getId();

					// 删除project和batch的关联
					String deleteBatchSql = "delete from ec2_markering_project_ec2_customer_resource_batch "
							+ "where batches_id="
							+ batchId
							+ " and marketingproject_id=" + project.getId();

					commonService.excuteNativeSql(deleteBatchSql,
							ExecuteType.UPDATE);
					// 从Task表中删除Task
					//根据项目批次定义一个Task，并删除
					String deleteTaskSql = "delete from ec2_marketing_project_task "
							+ "where marketingproject_id="+project.getId()+" and customerresource_id in(select customerresources_id "
							+ "from ec2_customer_resource_ec2_customer_resource_batch "
							+ "where customerresourcebatches_id="
							+ batchId
							+ ")";
					commonService.excuteNativeSql(deleteTaskSql,
							ExecuteType.UPDATE);
				}
			}
			//删除旧批次后的后处理
			afterDeleteOldProcessThread();
			/**==============查看msgList，如果List中有信息，弹出确认窗口================**/
		} else {
			Label msgLabel = new Label("", Label.CONTENT_XHTML);
			for (String msg : msgList) {
				if (msgLabel.getValue().equals("")) {
					msgLabel.setValue(msg);
				} else {
					msgLabel.setValue(msgLabel.getValue() + "</br>" + msg);
				}
			}
			// 如果含有删除的批次所对应的资源在Task中有其他批次也在使用，则显示提示信息
			ConfirmWindow confirmWindow = new ConfirmWindow(msgLabel, this,
					"confirmDelete");
			this.getApplication().getMainWindow().addWindow(confirmWindow);
		}

	}
	
	/**
	 * 如果有共用资源时提示给用户消息，确认则移除
	 * 
	 * @param isConfirmed
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if (!isConfirmed) {// 取消
			return;
		}
		//用户确认删除时的程序处理
		for (CustomerResourceBatch batch : rightTableOriginalSet) {
			
OperationLogUtil.simpleLog(loginUser, "项目控制-资源管理：移除资源"+batch.getBatchName()+" Id:"+batch.getId());
			
			// 如果已经添加过的批次不在现在的新批次中,则删除项目中对应Task
			if (!batches.contains(batch)) {
				Long batchId = batch.getId();

				// 删除project和batch的关联
				String deleteBatchSql = "delete from ec2_markering_project_ec2_customer_resource_batch "
						+ "where batches_id="
						+ batchId
						+ " and marketingproject_id=" + project.getId();

				commonService.excuteNativeSql(deleteBatchSql,
						ExecuteType.UPDATE);
				// 从Task表中删除Task
				//根据项目和批次对Task表和进行删除操作
				String deleteTaskSql = "delete from ec2_marketing_project_task "
						+ "where marketingproject_id="+project.getId()+" and customerresource_id in(select customerresources_id "
						+ "from ec2_customer_resource_ec2_customer_resource_batch "
						+ "where customerresourcebatches_id="
						+ batchId
						+")";
				commonService
						.excuteNativeSql(deleteTaskSql, ExecuteType.UPDATE);
			}
		}
		afterDeleteOldProcessThread();
//		this.getParent().removeWindow(this);
	}

	private void afterDeleteOldProcessThread(){
		//启动线程操作
		new Thread(){
			@Override
			public void run() {
				afterDeleteOldProcess();
			}
		}.start();
		AssignResource.this.getParent().removeWindow(AssignResource.this);
	}
	
	/**
	 * 删除旧批次后的后处理
	 */
	private void afterDeleteOldProcess() {
		/**=====将现在的批次信息关联到中间表=====**/
		// 更新批次信息到批次表
		project.setBatches(new HashSet<CustomerResourceBatch>(batches));
		marketingProjectService.update(project);

		/**=====从右侧表格中选出将要被添加的批次，然后将批次的资源添加到项目中=====**/
		// 将要添加的批次
		List<CustomerResourceBatch> toAddBatches = new ArrayList<CustomerResourceBatch>();
		for (CustomerResourceBatch batch : batches) {
			// 如果以前的批次中不包含现在的新批次，则添加新批次资源
			if (!rightTableOriginalSet.contains(batch)) {
OperationLogUtil.simpleLog(loginUser, "项目控制-资源管理：添加资源"+batch.getBatchName()+" Id:"+batch.getId());
				toAddBatches.add(batch);
			}
		}
		// jrh 此处应该保证一条资源在通一个项目中最多出现一条任务
		distributeResourceToProjecService.assignProjectResourceByBatch(project, toAddBatches, domain,pi);
		
		/**=====显示批次添加的信息=====**/
		// 成功分配的CSR记录数
		int successAssignNum = batches.size() - originalSize;
		String batchStr = "添加了" + successAssignNum;
		if (successAssignNum < 0) {
			batchStr = "移除了" + (-successAssignNum);
		}
		//导入完成后进度条的处理进度条
		pi.setEnabled(false);
		progressLayout.removeAllComponents();
		
		progressOuterLayout.removeComponent(progressLayout);
		
		projectControl.updateProjectResourceInfo();
		NotificationUtil.showWarningNotification(mainWindow, "成功给项目"
				+ project.getProjectName() + batchStr + "批资源！");
	}

	/**
	 * 每次切换页面时刷新Table里面数据的值
	 */
	@Override
	public void attach() {
		super.attach();
		project = projectControl.getCurrentSelect();
		this.setCaption("项目 " + project.getProjectName() + " 添加/移除资源");
		// 更新左侧组件和右侧组件的回显信息
		this.updateRightTable();//先更新右侧组件，然后再更新右侧组件的时候更新左侧组件
		this.updateLeftTable();
		// 更新信息的显示
		projectControl.updateProjectResourceInfo();
		mainWindow=this.getApplication().getMainWindow();
	}

	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 
	 * 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == leftSearch) {
			try {
				executeLeftSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "搜索异常！");
			}
		}else if(event.getButton()==add){
			rightKeyWord.setValue("");
			executeRightSearch();
			addToOpposite(leftTable, rightTable,false);
		}else if(event.getButton()==addAll){
			rightKeyWord.setValue("");
			executeRightSearch();
			addToOpposite(leftTable, rightTable,true);
		}else if(event.getButton()==remove){
			leftKeyWord.setValue("");
			executeLeftSearch();
			addToOpposite(rightTable,leftTable,false);
		}else if(event.getButton()==removeAll){
			leftKeyWord.setValue("");
			executeLeftSearch();
			addToOpposite(rightTable,leftTable,true);
		} else if (event.getButton() == rightSearch) {
			try {
				executeRightSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "搜索异常！");
			}
		} else if (event.getButton() == assign) {
			try {
				Long start = System.currentTimeMillis();
				executeAssign();
				Long end = System.currentTimeMillis();
				System.out.println("给项目添加批次总耗时 --> "+ (end - start)/1000 +" s");
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "项目添加资源出现异常！");
			}
		} else if (event.getButton() == cancel) {
			this.getParent().removeWindow(this);
		}
	}
}
