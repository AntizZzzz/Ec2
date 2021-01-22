package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.DistributeToTaskService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 按CSR指派组件
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class AssignByCsr extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件输出
	 */
	/* ======= 中间的左侧（表格和搜索）和右侧（表格和搜索）输出 ======== */
	private HorizontalLayout centerTablesLayout;
	// 关键字和搜索按钮
	// 左
	private TextField leftKeyWord;
	private Button leftSearch;
	// 右
	private TextField rightKeyWord;
	private Button rightSearch;

	// 中间的添加按钮
	private VerticalLayout centerButtonsLayout;
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;

	// 表格
	private Object[] columns = new String[] { "id", "username", "realName", "empNo",
			"department.name" };
	private String[] headers = new String[] { "ID", "用户名", "姓名", "工号", "部门" };
	private Table leftTable;
	private BeanItemContainer<User> leftTableContainer;
	private VerticalLayout tableLeftLayout;

	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private VerticalLayout tableRightLayout;

	
	private Map<Long, Long> user2taskCount;
	private Map<Long, Long> user2NotFinishedTaskCount;
	
	// 下面的选择批次组件
	private Panel checkBoxPanel;
	private TextField assignNumber;
	private ArrayList<CheckBox> checkBoxList;

	private User loginUser=SpringContextHolder.getLoginUser();
	/* ===========下方的指派和取消按钮============ */

	// 回收和取消按钮
	private Button assign;
	private Button cancel;

	/**
	 * 其他组件
	 */
	// 持有从Table取出来的MarketingProject引用
	private MarketingProject project;
	// 持有调用它的组件引用ProjectControl,以刷新父组件
	private ProjectControl projectControl;
	private CommonService commonService;
	private DistributeToTaskService distributeToTaskService;
	private Domain domain;

	/**
	 * 构造器
	 */
	public AssignByCsr(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setCaption("按CSR指派资源");
		this.setModal(true);
		this.setResizable(false);
		this.projectControl = projectControl;
		user2taskCount = new HashMap<Long, Long>();
		user2NotFinishedTaskCount = new HashMap<Long, Long>();

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// 添加中间组件输出
		windowContent.addComponent(buildCenterTablesLayout());

		// 选择批次和指定分配数目组件
		checkBoxPanel = new Panel();
		checkBoxPanel.setWidth("40em");
		windowContent.addComponent(checkBoxPanel);

		HorizontalLayout textFieldConstraint = new HorizontalLayout();
		textFieldConstraint.setSpacing(true);
		textFieldConstraint.addComponent(new Label("个人拥有任务上限数:"));

		assignNumber = new TextField();
		assignNumber.setWidth("120px");
		assignNumber.setInputPrompt("上限值不要过大！");
		assignNumber.setDescription("<B>实际分配数量 = 任务上限值 - 已拥有任务数(未分配任务充足的情况下)</B>");
		assignNumber.addValidator(new RegexpValidator("[1-9]\\d*", "指派数量只能是大于0的数字，请重新输入！"));
//		TODO
//		assignNumber.setDescription("最多不能超过"
//				+ ConfigProperty.ASSIGN_RESOURCE_NUMBER + "条！");
		textFieldConstraint.addComponent(assignNumber);
		windowContent.addComponent(textFieldConstraint);

		// 右下角的按钮输出
		HorizontalLayout tempButtonsLayout = buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout,
				Alignment.BOTTOM_RIGHT);

		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	/**
	 * 初始化Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		commonService=SpringContextHolder.getBean("commonService");
		distributeToTaskService = SpringContextHolder.getBean("distributeToTaskService");
	}

	/**
	 * 创建中间的组件组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildCenterTablesLayout() {
		centerTablesLayout = new HorizontalLayout();
		centerTablesLayout.setSpacing(true);

		// 左侧输出
		tableLeftLayout = buildLeft();
		centerTablesLayout.addComponent(tableLeftLayout);
		centerTablesLayout.setExpandRatio(tableLeftLayout, 8.0f);

		// 中间输出
		centerButtonsLayout = buildCenterButtons();
		centerTablesLayout.addComponent(centerButtonsLayout);
		centerTablesLayout.setExpandRatio(centerButtonsLayout, 1.5f);

		// 右侧输出
		tableRightLayout = buildRight();
		centerTablesLayout.addComponent(tableRightLayout);
		centerTablesLayout.setExpandRatio(tableRightLayout, 8.0f);

		// 添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);
		return centerTablesLayout;
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
		leftLayout.addComponent(new Label("全部CSR"));

		// 表格组件 和翻页组件
		leftLayout.addComponent(buildLeftTable());
		return leftLayout;
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
		rightLayout.addComponent(new Label("已选CSR"));

		// 表格组件
		rightLayout.addComponent(buildRightTable());
		return rightLayout;
	}

	/**
	 * 添加Csr中间输出
	 * 
	 * @return
	 */
	private VerticalLayout buildCenterButtons() {
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
	 * 由 buildLeft 调用，左侧表格
	 * 
	 * @return
	 */
	private VerticalLayout buildLeftTable() {
		VerticalLayout tableLayout = new VerticalLayout();
		tableLayout.setWidth("100%");
		// 表格
		leftTable = new Table();
		leftTable.setStyleName("striped");
		leftTable.setPageLength(7);
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTableContainer = new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setVisibleColumns(columns);
		leftTable.setColumnHeaders(headers);
		leftTable.addGeneratedColumn("taskCount", new HasAssignedTaskCounts());
		leftTable.setColumnHeader("taskCount", "任务数情况");
		leftTable.setColumnAlignment("taskCount", Table.ALIGN_CENTER);
		tableLayout.addComponent(leftTable);
		return tableLayout;
	}
	
	/**
	 * 由buildRight调用，创建右侧表格组件
	 * 
	 * @return
	 */
	private VerticalLayout buildRightTable() {
		VerticalLayout tableLayout = new VerticalLayout();
		tableLayout.setWidth("100%");
		// 表格
		rightTable = new Table();
		rightTable.setStyleName("striped");
		rightTable.setPageLength(7);
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTableContainer = new BeanItemContainer<User>(User.class);
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		rightTable.addGeneratedColumn("taskCount", new HasAssignedTaskCounts());
		rightTable.setColumnHeader("taskCount", "任务数情况");
		rightTable.setColumnAlignment("taskCount", Table.ALIGN_CENTER);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}
	
	/**
	 * jrh 用于显示个话务员已经拥有多少个任务
	 */
	private class HasAssignedTaskCounts implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Long userId = ((User) itemId).getId();
			if(columnId.equals("taskCount")) {
				Long taskCount = user2taskCount.get(userId);
				Long taskNotFinishedCount = user2NotFinishedTaskCount.get(userId);
				String taskCountStr= (taskCount == null) ? "0" : taskCount+"";
				String taskNotFinishedCountStr= (taskNotFinishedCount == null) ? "0" : taskNotFinishedCount+"";
				return "已完成:"+taskCountStr+"/未完成:"+taskNotFinishedCountStr;
			} 
			return null;
		}
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
	 * 创建此窗口最下边按钮的输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("23%");
		// 添加
		assign = new Button("指派");
		assign.addListener((Button.ClickListener) this);
		buttonsLayout.addComponent(assign);
		buttonsLayout.setComponentAlignment(assign, Alignment.MIDDLE_CENTER);
		// 取消
		cancel = new Button("关闭");
		cancel.addListener((Button.ClickListener) this);
		buttonsLayout.addComponent(cancel);
		buttonsLayout.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		return buttonsLayout;
	}

	/**
	 * 由buttonClick调用,完成左侧组件的搜索
	 */
	private void executeLeftSearch() {
		// 搜索符合条件的批次信息
		leftTableContainer.removeAllContainerFilters();
		// 关键字
		String leftKeyWordStr = "";
		if (leftKeyWordStr.equals("")) {
			leftKeyWordStr = leftKeyWord.getValue().toString();
		}
		Or compareAll = new Or(new Like("username", "%" + leftKeyWordStr + "%",
				false), new Like("empNo", "%" + leftKeyWordStr + "%", false),
	             new Like("realName", "%" + leftKeyWordStr + "%", false), 
				new Like("department.name", "%" + leftKeyWordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 搜索后将结果按用户名升序排列
		leftTableContainer.sort(new Object[] {"username"}, new boolean[] {true});
	}

	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		// 搜索符合条件的批次信息
		rightTableContainer.removeAllContainerFilters();
		// 关键字
		String rightKeyWordStr = "";
		if (rightKeyWordStr.equals("")) {
			rightKeyWordStr = rightKeyWord.getValue().toString();
		}
		Or compareAll = new Or(
				new Like("username", "%" + rightKeyWordStr + "%", false), 
				new Like("empNo", "%" + rightKeyWordStr + "%", false),
	            new Like("realName", "%" + rightKeyWordStr + "%", false), 
	            new Like("department.name", "%" + rightKeyWordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 搜索后将结果按用户名升序排列
		rightTableContainer.sort(new Object[] {"username"}, new boolean[] {true});
	}

	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * 
	 * @param tableFrom
	 *            从哪个表取数据
	 * @param tableTo
	 *            添加到哪个表
	 * @param isAll
	 *            是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if (tableFrom == null || tableTo == null)
			return;

		// 如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if (!isAll)
			if (tableFrom.getValue() == null) {
				this.getApplication().getMainWindow()
						.showNotification("请选择要添加或移除的CSR!",
								Window.Notification.TYPE_HUMANIZED_MESSAGE);
				return;
			}

		// 从tableFrom中取出所有选中的Csr
		Collection<User> csrs = null;
		if (isAll) {
			// 出现 java.util.ConcurrentModificationException异常，所以包装
			csrs = new ArrayList<User>(
					(Collection<User>) tableFrom.getItemIds());
		} else {
			csrs = (Collection<User>) tableFrom.getValue();
		}
		// 通过循环来改变TableFrom和TableTo的Item
		for (User csr : csrs) {
			tableFrom.getContainerDataSource().removeItem(csr);
			tableTo.getContainerDataSource().addItem(csr);
		}
	}

	/**
	 * 由 构造器 调用，设置按钮的样式
	 * 
	 * @param style
	 */
	private void setButtonsStyle(String style) {
		style = "small";
		// 搜索按钮和添加移除按钮
		leftSearch.setStyleName(style);
		rightSearch.setStyleName(style);
		add.setStyleName(style);
		addAll.setStyleName(style);
		remove.setStyleName(style);
		removeAll.setStyleName(style);

		// 最下面的两个按钮
		style = "";
		assign.setStyleName(style);
		cancel.setStyleName(style);
	}

	/**
	 * 执行回收
	 */
	@SuppressWarnings("unchecked")
	private void executeAssign() {
		leftKeyWord.setValue("");
		executeLeftSearch();
		rightKeyWord.setValue("");
		executeRightSearch();
		
		
		Collection<User> csrs = (Collection<User>) rightTable.getContainerDataSource().getItemIds();
		if(csrs.isEmpty()){
			NotificationUtil.showWarningNotification(this, "请选择至少一个CSR进行分派");
			return;
		}
		// 由于assignNumber.getValue()可能为null，所以没用toString()方法
		// jrh 判断输入的指派数量是否合法
		if(!"".equals(assignNumber.getValue()) && assignNumber.isValid()) {
			int num = Integer.parseInt((assignNumber.getValue() + "").toString());
			
			Window mainWindow = this.getApplication().getMainWindow();
			// 将用户选中的批次添加到要分配的集合当中
			List<CustomerResourceBatch> toDistributeBatch = new ArrayList<CustomerResourceBatch>();
			for (CheckBox checkBox : checkBoxList) {
				if((Boolean)checkBox.getValue()){
					CustomerResourceBatch batch=(CustomerResourceBatch) checkBox.getData();
					toDistributeBatch.add(batch);
OperationLogUtil.simpleLog(loginUser, "项目控制-按CSR指派(批次)："+batch.getBatchName()+" Id:"+batch.getId());
				}
			}
			if (toDistributeBatch.isEmpty()) {
				// 如果用户没有选择批次，显示提示信息，不关闭窗口
				NotificationUtil.showWarningNotification(this, "请选择至少一个批次！！！");
				return;
			}

			//进行分配操作
			Long distributeNum = distributeToTaskService.distribute(project,
					toDistributeBatch, new HashSet<User>(csrs), num+0L, domain);

			mainWindow.showNotification("成功分配" + distributeNum + "个资源给CSR");
			projectControl.updateTable(false);
			projectControl.updateProjectResourceInfo();
			
			// jrh 每次指派完成后，更新界面信息
			updateComponentsInfo();
		} else {
			assignNumber.setValue("");
			Notification notif = new Notification("上限值只能是大于0的数字，请重新输入！");
			this.showNotification(notif);
		}
	}

	/**
	 * 每次Attach时根据MarketingProject重查数据库，重设左侧TableContainer的数据源
	 */
	@Override
	public void attach() {
		super.attach();
		updateComponentsInfo();
	}
	
	/**
	 * 每次指派完成后\或项目管理界面的按CSR 指派按钮后，更新界面信息
	 */
	@SuppressWarnings("unchecked")
	private void updateComponentsInfo() {
		// 清空原有数据
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		user2taskCount.clear();
		user2NotFinishedTaskCount.clear();
		
		// 由于只是查询，所以不需要事物支持
		project = (MarketingProject) projectControl.getTable().getValue();
		Set<User> csrs = project.getUsers();
		// jrh 为了可以按话务员姓名进行搜索，如果不设置，查询时，会报空指针异常
		if(csrs != null) {
			for(User csr : csrs) {
				if(csr.getRealName() == null) {
					csr.setRealName("");
				}
			}
		}
		leftTableContainer.addAll(csrs);
		
		// jrh 计算当前项目中各用户已经拥有的任务数量
		String user2taskCountSql = "select user_id, count(*) as count from ec2_marketing_project_task where domain_id="+domain.getId()
				+" and marketingproject_id="+project.getId()+" and user_id is not null and isfinished=true group by user_id";
		String user2NotFinishedTaskCountSql = "select user_id, count(*) as count from ec2_marketing_project_task where domain_id="+domain.getId()
				+" and marketingproject_id="+project.getId()+" and user_id is not null and isfinished is not true  group by user_id";
		List<Object[]> countResults = (List<Object[]>)commonService.excuteNativeSql(user2taskCountSql, ExecuteType.RESULT_LIST);
		List<Object[]> countNotFinishedResults = (List<Object[]>)commonService.excuteNativeSql(user2NotFinishedTaskCountSql, ExecuteType.RESULT_LIST);
		for(Object[] objects : countResults) {
			Long userId = (Long) objects[0];
			Long taskCount = (Long) objects[1];
			user2taskCount.put(userId, taskCount);
		}
		for(Object[] objects : countNotFinishedResults) {
			Long userId = (Long) objects[0];
			Long taskNotFinishedCount = (Long) objects[1];
			user2NotFinishedTaskCount.put(userId, taskNotFinishedCount);
		}
		
		// 用于更新左侧表格中的任务数信息
		leftTable.refreshRowCache();
		
		//=====================按照CSR指派================//
		checkBoxPanel.removeAllComponents();// 初始化panel
		checkBoxList = new ArrayList<CheckBox>();// 初始化checkBoxList
		Set<CustomerResourceBatch> batchesSet = project.getBatches();
		List<CustomerResourceBatch> batches = new ArrayList<CustomerResourceBatch>(
				batchesSet);
		// 约束组件
		GridLayout constraintLayout = new GridLayout(2, batches.size());
		for (int i = 0; i < batches.size(); i++) {
			CustomerResourceBatch batch = batches.get(i);
			//chb 20130624 对于现实的批次按照是否禁用进行控制
			 if(batch.getBatchStatus()==BatchStatus.UNUSEABLE){
				 continue;
			 }
			 
			// CheckBox组件
			CheckBox batchCheckBox = new CheckBox(batch.getBatchName());
			batchCheckBox.setData(batch);
			//信息显示组件
			String nativeSql="select isfinished,user_id is not null as ishaveuid,count(*) from ec2_marketing_project_task where domain_id="+
					domain.getId()+" and customerresource_id in(" +
					"select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") " +
					"and marketingproject_id="+project.getId()+" group by isfinished,ishaveuid";
			
			//查询数据并设置数据源
			Long notDistributedNum=0L;
			Long finishedNum = 0L;
			Long notFinishedNum = 0L;
			
			List<Object[]> nums = (List<Object[]>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
			if(nums.size()>3) throw new RuntimeException("用户Id为null，应该标记为未完成状态！");
			for(int j=0;j<nums.size();j++){
				Object[] objects=nums.get(j);
				Boolean isfinished=(Boolean)objects[0];
				Boolean ishaveuid=(Boolean)objects[1];
				Long count=(Long)objects[2];
				if(isfinished==false&&ishaveuid==false){ //没有用户名，没有完成状态  -- 未分配
					notDistributedNum=count;
				}else if(isfinished==true&&ishaveuid==true){ //有用户名，  已完成状态 -- 已完成
					finishedNum=count;
				}else if(isfinished==false&&ishaveuid==true){ //有用户名，  未完成状态 -- 未完成
					notFinishedNum=count;
				}
			}
			//显示的信息
			Label infoLabel = new Label("    已完成:" + finishedNum +"    未完成:" + notFinishedNum+ "    已分配:"
					+ (finishedNum+notFinishedNum) + "    未分配:" + notDistributedNum +"    总数:" + (finishedNum+notFinishedNum+notDistributedNum));
			
			if(notDistributedNum>0){
				batchCheckBox.setEnabled(true);
			}else{
				batchCheckBox.setEnabled(false);
			}
			constraintLayout.addComponent(batchCheckBox,0,i);
			constraintLayout.addComponent(infoLabel,1,i);
			//yongList记录所有的CheckBox
			checkBoxList.add(batchCheckBox);
			// 添加Layout到Panel
			checkBoxPanel.addComponent(constraintLayout);
			assignNumber.setValue("");
		}
	}

	/**
	 * 监听搜索、高级搜索，按钮的单击事件 加 按钮事件（add，addAll，remove，removeAll） 监听 add和cancel按钮的事件
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == leftSearch) {
			executeLeftSearch();
		} else if(event.getButton()==add){
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
			executeRightSearch();
		} else if (event.getButton() == assign || event.getButton() == cancel) {
			bottomButtonClick(event);
		}
	}

	/**
	 * 点击回收或取消按钮的事件,为了统一，将其设置为public
	 * 
	 * @param event
	 */
	public void bottomButtonClick(ClickEvent event) {
		if (event.getButton() == assign) {
			try {
				Long startTime=System.currentTimeMillis();
				executeAssign();
				Long endTime=System.currentTimeMillis();
				logger.info("按CSR指派耗时:"+(endTime-startTime)/1000+"秒");
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "按CSR指派失败!");
			}
		} else if (event.getButton() == cancel) {
			this.getParent().removeWindow(this);
			return;
		}
	}

}
