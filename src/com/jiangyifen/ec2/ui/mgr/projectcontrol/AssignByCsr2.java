package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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
public class AssignByCsr2 extends Window implements Button.ClickListener {
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
	
//	// 下面的选择批次组件
//	private Panel checkBoxPanel;
//	private TextField assignNumber;
//	private ArrayList<CheckBox> checkBoxList;

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
	private TaskDetailWindow taskDetailWindow;
	private Set<Long> selectedTasksId;
	private CommonService commonService;
	private Domain domain;

	/**
	 * 构造器
	 */
	public AssignByCsr2(ProjectControl projectControl,TaskDetailWindow taskDetailWindow) {
		this.initService();
		this.center();
		this.setCaption("按CSR指派资源");
		this.setModal(true);
		this.setResizable(false);
		this.projectControl = projectControl;
		this.taskDetailWindow = taskDetailWindow;
		user2taskCount = new HashMap<Long, Long>();

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// 添加中间组件输出
		windowContent.addComponent(buildCenterTablesLayout());

		// 右下角的按钮输出
		HorizontalLayout tempButtonsLayout = buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout,
				Alignment.BOTTOM_RIGHT);

		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	
	/**
	 * 设置选中的Task
	 * @param selectedTasksId
	 */
	public void setSelectedTasksId(Set<Long> selectedTasksId) {
		this.selectedTasksId = selectedTasksId;
	}
	
	/**
	 * 初始化Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		commonService=SpringContextHolder.getBean("commonService");
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
		leftTable.setColumnHeader("taskCount", "已拥有任务数");
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
		rightTable.setColumnHeader("taskCount", "已拥有任务数");
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
				return (taskCount == null) ? 0L : taskCount;
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
		
		//设置CSRId的list集合
		List<Long> csrIdList=new ArrayList<Long>();
		for(User csr:csrs){
			csrIdList.add(csr.getId());
		}
		
		int csrIndex=0;
		
//		String distributeTime=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
		//对Task循环
		for(Long taskId:selectedTasksId){
			if(csrIndex%csrIdList.size()==0){
				csrIndex=0;
			}
			Long csrId=csrIdList.get(csrIndex);
//			原生Sql有缓存问题
//			String nativeSql="update ec2_marketing_project_task set distributetime='"+distributeTime+"',isanswered=false,isfinished=false,user_id="+csrId+" where id="+taskId;
//			commonService.excuteNativeSql(nativeSql, ExecuteType.UPDATE);
			MarketingProjectTask task=commonService.get(MarketingProjectTask.class, taskId);
			task.setDistributeTime(new Date());
			task.setIsAnswered(false);
			task.setIsFinished(false);
			task.setLastStatus(null);
			
			//假user
			User user=new User();
			user.setId(csrId);
			task.setUser(user);
			task=(MarketingProjectTask)commonService.update(task);
			csrIndex++;
		}
		taskDetailWindow.updateTable(false);
		taskDetailWindow.showNotification("成功指派"+selectedTasksId.size()+" 条任务！");
		this.getParent().removeWindow(this);
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
		this.setCaption("按CSR指派-"+selectedTasksId.size()+"-条资源");
		
		// 清空原有数据
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		user2taskCount.clear();
		
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
				+" and marketingproject_id="+project.getId()+" and user_id is not null group by user_id";
		List<Object[]> countResults = (List<Object[]>)commonService.excuteNativeSql(user2taskCountSql, ExecuteType.RESULT_LIST);
		for(Object[] objects : countResults) {
			Long userId = (Long) objects[0];
			Long taskCount = (Long) objects[1];
			user2taskCount.put(userId, taskCount);
		}
		
		// 用于更新左侧表格中的任务数信息
		leftTable.refreshRowCache();
		
//		//=====================按照CSR指派================//
//		checkBoxPanel.removeAllComponents();// 初始化panel
//		checkBoxList = new ArrayList<CheckBox>();// 初始化checkBoxList
//		Set<CustomerResourceBatch> batchesSet = project.getBatches();
//		List<CustomerResourceBatch> batches = new ArrayList<CustomerResourceBatch>(
//				batchesSet);
//		// 约束组件
//		GridLayout constraintLayout = new GridLayout(2, batches.size());
//		for (int i = 0; i < batches.size(); i++) {
//			CustomerResourceBatch batch = batches.get(i);
//			//chb 20130624 对于现实的批次按照是否禁用进行控制
//			 if(batch.getBatchStatus()==BatchStatus.UNUSEABLE){
//				 continue;
//			 }
//			 
//			// CheckBox组件
//			CheckBox batchCheckBox = new CheckBox(batch.getBatchName());
//			batchCheckBox.setData(batch);
//			//信息显示组件
//			String nativeSql="select isfinished,user_id is not null as ishaveuid,count(*) from ec2_marketing_project_task where domain_id="+
//					domain.getId()+" and customerresource_id in(" +
//					"select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") " +
//					"and marketingproject_id="+project.getId()+" group by isfinished,ishaveuid";
//			
//			//查询数据并设置数据源
//			Long notDistributedNum=0L;
//			Long finishedNum = 0L;
//			Long notFinishedNum = 0L;
//			
//			List<Object[]> nums = (List<Object[]>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
//			if(nums.size()>3) throw new RuntimeException("用户Id为null，应该标记为未完成状态！");
//			for(int j=0;j<nums.size();j++){
//				Object[] objects=nums.get(j);
//				Boolean isfinished=(Boolean)objects[0];
//				Boolean ishaveuid=(Boolean)objects[1];
//				Long count=(Long)objects[2];
//				if(isfinished==false&&ishaveuid==false){ //没有用户名，没有完成状态  -- 未分配
//					notDistributedNum=count;
//				}else if(isfinished==true&&ishaveuid==true){ //有用户名，  已完成状态 -- 已完成
//					finishedNum=count;
//				}else if(isfinished==false&&ishaveuid==true){ //有用户名，  未完成状态 -- 未完成
//					notFinishedNum=count;
//				}
//			}
//			//显示的信息
//			Label infoLabel = new Label("    已完成:" + finishedNum +"    未完成:" + notFinishedNum+ "    已分配:"
//					+ (finishedNum+notFinishedNum) + "    未分配:" + notDistributedNum +"    总数:" + (finishedNum+notFinishedNum+notDistributedNum));
//			
//			if(notDistributedNum>0){
//				batchCheckBox.setEnabled(true);
//			}else{
//				batchCheckBox.setEnabled(false);
//			}
//			constraintLayout.addComponent(batchCheckBox,0,i);
//			constraintLayout.addComponent(infoLabel,1,i);
//			//yongList记录所有的CheckBox
//			checkBoxList.add(batchCheckBox);
//			// 添加Layout到Panel
//			checkBoxPanel.addComponent(constraintLayout);
//			assignNumber.setValue("");
//		}
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
