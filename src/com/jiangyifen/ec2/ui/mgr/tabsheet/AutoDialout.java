package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.autodialout.ProjectResourceConsumer;
import com.jiangyifen.ec2.autodialout.ProjectResourceProducer;
import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.autodialout.AddAutoDialoutTask;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutAssignCsr;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutAssignPhoneNo;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutMonitor;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutRecycle;
import com.jiangyifen.ec2.ui.mgr.autodialout.AutoDialoutRecycleToBatch;
import com.jiangyifen.ec2.ui.mgr.autodialout.EditAutoDialoutTask;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 自动外呼控制
 * <p>
 * 此页面应该只包含搜索组件、表格组件、表格下的按钮组件
 * </p>
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class AutoDialout extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	private static final Logger logger = LoggerFactory.getLogger(AutoDialout.class);
	
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private ComboBox autoDialoutTaskStatus;
	private Button search;

	// 自动外呼表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<AutoDialoutTask> flip;

	// 自动外呼表格按钮组件
	private Button add;// 新建自动外呼
	private Button start;
	// private Button pause;
	private Button stop;
	private Button edit;
	private Button delete;
	private Button monitor;
	private Button assignCsr;
	private Button assignPhoneNo;		// jrh 添加指定的手机号
	private Button recycle;
	private Button recycleToBatch;
	private Button description;

	/**
	 * 右键组件
	 */
	private Action START = new Action("开始");
	private Action STOP = new Action("停止");
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action MONITOR = new Action("状态监控");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { START, STOP, ADD, EDIT, MONITOR,
			DELETE };
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private EditAutoDialoutTask editAutoDialoutTaskWindow;
	private AutoDialoutAssignCsr assignCsrWindow;
	private AutoDialoutAssignPhoneNo assignPhoneNoWindow;
	private AutoDialoutRecycle recycleWindow;
	private AutoDialoutRecycleToBatch recycleToBatchWindow;
	private AutoDialoutMonitor autoDialoutMonitorWindow;
	private AddAutoDialoutTask addWindow;

	/**
	 * 自动外呼信息组件
	 */
	private Label callCount;
	private Label answeredCount;
	private Label missedCount;
	private Label missedRate;
	private Label answeredRate;

	/**
	 * 其他组件
	 */
	// 自动外呼资源数目信息
	private AutoDialoutTaskService autoDialoutTaskService;
	private DepartmentService departmentService;
	private QueueService queueService;
	private CommonService commonService;
	private UserService userService;								// 用户服务类
	private UserQueueService userQueueService;						// 动态队列成员服务类
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员关系管理服务类
	
	// 如果当前有选中的自动外呼则会存储当前选中的自动外呼，如果没有选中的自动外呼则会存储null
	private AutoDialoutTask autoDialoutTask;
	private Domain domain;
	private User loginUser;

	/**
	 * 构造器
	 */
	public AutoDialout() {
		this.initService();
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索
		constrantLayout.addComponent(buildSearchLayout());
		// 初始化Sql语句
		search.click();
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());

		// 显示自动外呼的状态信息
		constrantLayout.addComponent(buildAutoDialoutInfoLayout());
	}

	/**
	 * 将Service进行初始化
	 */
	private void initService() {
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		
		departmentService = SpringContextHolder.getBean("departmentService");
		autoDialoutTaskService = SpringContextHolder.getBean("autoDialoutTaskService");
		queueService = SpringContextHolder.getBean("queueService");
		commonService = SpringContextHolder.getBean("commonService");
		userService = SpringContextHolder.getBean("userService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
	}

	/**
	 * 创建搜索组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字:"));// 关键字
		keyWord = new TextField();// 输入区域
		keyWord.setWidth("6em");
		keyWord.setStyleName("search");
		keyWord.setInputPrompt("自动外呼名称");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		// 自动外呼状态
		searchLayout.addComponent(new Label("&nbsp;&nbsp;状态",
				Label.CONTENT_XHTML));
		autoDialoutTaskStatus = new ComboBox();
		autoDialoutTaskStatus.setInputPrompt("全部");
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.NEW);
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.RUNNING);
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.PAUSE);
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.OVER);
		autoDialoutTaskStatus.setWidth("8em");
		searchLayout.addComponent(autoDialoutTaskStatus);

		// 搜索按钮
		search = new Button("搜索");
		search.setStyleName("small");
		search.addListener((Button.ClickListener) this);
		searchLayout.addComponent(search);

		return searchLayout;
	}

	/**
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		// 创建表格
		table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
							.format(v);
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.addActionHandler(this);
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(table);
		// 创建按钮
		tabelAndButtonsLayout.addComponent(buildTableButtons());
		return tabelAndButtonsLayout;
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建按钮的输出，为Table设置数据源
	 * 
	 * @return
	 */
	private HorizontalLayout buildTableButtons() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setWidth("100%");
		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);
		tableButtons.addComponent(tableButtonsLeft);

		//对左侧按钮的布局，两排的约束组件
		VerticalLayout leftButtonsVerticalLayout=new VerticalLayout();
		leftButtonsVerticalLayout.setSpacing(true);
		tableButtonsLeft.addComponent(leftButtonsVerticalLayout);
		
		//第一排
		HorizontalLayout firstLineLayout=new HorizontalLayout();
		firstLineLayout.setSpacing(true);
		leftButtonsVerticalLayout.addComponent(firstLineLayout);
				
		// 新建自动外呼
		add = new Button("添加");
		add.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(add);

		// 开始
		start = new Button("开始");
		start.setEnabled(false);
		start.addListener((Button.ClickListener) this);
		start.setStyleName(StyleConfig.BUTTON_STYLE);
		firstLineLayout.addComponent(start);

		// // 暂停
		// pause = new Button("暂停");
		// pause.setEnabled(false);
		// pause.addListener((Button.ClickListener) this);
		// pause.setStyleName(StyleConfig.BUTTON_STYLE);
		// tableButtonsLeft.addComponent(pause);

		// 停止
		stop = new Button("停止");
		stop.setEnabled(false);
		stop.addListener((Button.ClickListener) this);
		stop.setStyleName(StyleConfig.BUTTON_STYLE);
		firstLineLayout.addComponent(stop);

		// 创建编辑按钮
		edit = new Button("编辑");
		edit.setEnabled(false);
		edit.setStyleName(StyleConfig.BUTTON_STYLE);
		edit.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(edit);

		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
		delete = new Button("删除");
		delete.setEnabled(false);
		delete.setStyleName(StyleConfig.BUTTON_STYLE);
		delete.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(delete);

		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
		monitor = new Button("状态监控");
		monitor.setEnabled(false);
		monitor.setStyleName(StyleConfig.BUTTON_STYLE);
		monitor.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(monitor);

		// 添加CSR
		assignCsr = new Button("添加/移除CSR");
		assignCsr.setEnabled(false);
		assignCsr.setStyleName(StyleConfig.BUTTON_STYLE);
		assignCsr.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(assignCsr);
		
		// 添加/移除手机号
		assignPhoneNo = new Button("添加/移除手机成员");
		assignPhoneNo.setEnabled(false);
		assignPhoneNo.setStyleName(StyleConfig.BUTTON_STYLE);
		assignPhoneNo.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(assignPhoneNo);

		//第二排
		HorizontalLayout secondLineLayout=new HorizontalLayout();
		secondLineLayout.setSpacing(true);
		leftButtonsVerticalLayout.addComponent(secondLineLayout);

		//资源回收
		recycle= new Button("资源回收");
		recycle.setEnabled(false);
		recycle.setStyleName(StyleConfig.BUTTON_STYLE);
		recycle.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(recycle);

		//资源回收
		recycleToBatch= new Button("回收到批次");
		recycleToBatch.setEnabled(false);
		recycleToBatch.setStyleName(StyleConfig.BUTTON_STYLE);
		recycleToBatch.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(recycleToBatch);
		
		//参数说明
		description = new Button("参数说明");
		description.setStyleName(StyleConfig.BUTTON_STYLE);
		description.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(description);

		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<AutoDialoutTask>(
				AutoDialoutTask.class, autoDialoutTaskService, table,
				sqlSelect, sqlCount, null);
		// flip.getEntityContainer().addNestedContainerProperty("soundFile.name");
		table.setPageLength(10);
		flip.setPageLength(10, false);
		flip.getEntityContainer().addNestedContainerProperty(
				"marketingProject.projectName");

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "autoDialoutTaskName",
				"marketingProject.projectName", "autoDialoutTaskStatus",
				"preAudioPlay", "dialoutType", "ratio", "isSystemAjust",
				"percentageDepth", "staticExpectedCallers", "createDate" };
		String[] columnHeaders = new String[] { "ID", "自动外呼名", "项目名", "状态",
				"音乐播放", "类型", "比例", "调整类型", "自动调整深度", "固定排队数", "创建时间" };

		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置表格的样式
		this.setStyleGeneratorForTable(table);
		// 生成备注列
		this.addColumn(table);

		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 创建自动外呼状态输出区域
	 */
	private GridLayout buildAutoDialoutInfoLayout() {
		GridLayout gridLayout = new GridLayout(2, 5);
		// 标签
		Label callCountLabel = new Label("呼叫数量");
		callCountLabel.setDescription("发起呼叫的总数");
		gridLayout.addComponent(callCountLabel, 0, 0);
		Label answerCountLabel = new Label("呼通数量");
		answerCountLabel.setDescription("客户接起电话的总数");
		gridLayout.addComponent(answerCountLabel, 0, 1);
		Label missedCountLabel = new Label("漏接数量");
		missedCountLabel.setDescription("客户接起，坐席未接的电话总数");
		gridLayout.addComponent(missedCountLabel, 0, 2);
		Label missedRateLabel = new Label("呼损率(漏接/呼通)");
		missedRateLabel.setDescription("呼通数量/呼叫数量");
		gridLayout.addComponent(missedRateLabel, 0, 3);
		Label answeredRateLabel = new Label("呼通率(呼通/呼叫)");
		answeredRateLabel.setDescription("漏接数量/呼通数量");
		gridLayout.addComponent(answeredRateLabel, 0, 4);
		// 数值
		callCount = new Label("");
		answeredCount = new Label("");
		missedCount = new Label("");
		missedRate = new Label("");
		answeredRate = new Label("");
		gridLayout.addComponent(callCount, 1, 0);
		gridLayout.addComponent(answeredCount, 1, 1);
		gridLayout.addComponent(missedCount, 1, 2);
		gridLayout.addComponent(missedRate, 1, 3);
		gridLayout.addComponent(answeredRate, 1, 4);
		return gridLayout;
	}

	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("备注", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId, Object columnId) {
				// 创建备注显示组件
				Object note = table.getContainerDataSource().getContainerProperty(itemId, "note");
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
	 * 由 buildTableButtons 调用，设置生成表格行的样式
	 * 
	 * @param table
	 */
	private void setStyleGeneratorForTable(final Table table) {
		// style generator
		table.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				// 暂时保持颜色和项目的配色方案一致
				String[] styles = { StyleConfig.PROJECT_CONTROL_NEW_COLOR,
						StyleConfig.PROJECT_CONTROL_RUNNING_COLOR,
						StyleConfig.PROJECT_CONTROL_STOP_COLOR,
						StyleConfig.PROJECT_CONTROL_STOP_COLOR };
				if (propertyId == null) {
					AutoDialoutTaskStatus status = (AutoDialoutTaskStatus) table
							.getContainerProperty(itemId,
									"autoDialoutTaskStatus").getValue();
					return styles[status.getIndex()];
				} else {
					return null;
				}
			}
		});
	}

	/**
	 * 由buttonClick 调用，执行搜索功能
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("AutoDialoutTask");
		// 关键字过滤,自动外呼名
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like autoDialoutTaskName = new SqlGenerator.Like(
				"autoDialoutTaskName", keyWordStr);
		sqlGenerator.addAndCondition(autoDialoutTaskName);

		// 自动外呼状态
		AutoDialoutTaskStatus autoDialoutTaskStatu = (AutoDialoutTaskStatus) autoDialoutTaskStatus
				.getValue();
		if (autoDialoutTaskStatu != null) {
			String statuStr = autoDialoutTaskStatu.getClass().getName() + ".";
			if (autoDialoutTaskStatu.getIndex() == 0) {
				statuStr += "NEW";
			} else if (autoDialoutTaskStatu.getIndex() == 1) {
				statuStr += "RUNNING";
			} else if (autoDialoutTaskStatu.getIndex() == 2) {
				statuStr += "PAUSE";
			} else if (autoDialoutTaskStatu.getIndex() == 3) {
				statuStr += "OVER";
			}
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"autoDialoutTaskStatus", statuStr, false);
			sqlGenerator.addAndCondition(statu);
		}

		// 自动外呼类型
		SqlGenerator.Equal type = new SqlGenerator.Equal("dialoutType",
				AutoDialoutTask.AUTO_DIALOUT, true);
		sqlGenerator.addAndCondition(type);

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
		// jrh 获取当前用户管辖部门创建的自动外呼
		for (Long deptId : allGovernedDeptIds) {
			SqlGenerator.Equal orEqual = new SqlGenerator.Equal("creator.department.id", deptId.toString(), false);
			sqlGenerator.addOrCondition(orEqual);
		}

		// 排序
		sqlGenerator.setOrderBy("id", SqlGenerator.DESC);

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		sqlCount = sqlGenerator.generateCountSql();
		// 更新Table，并使Table处于未选中
		this.updateTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}

	/**
	 * 执行删除操作
	 */
	private void executeDelete() {
		if (autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)) {
			NotificationUtil.showWarningNotification(this,"自动外呼正在运行，不允许删除");
			return;
		}
		Label label = new Label("您确定要删除自动外呼<b>" + autoDialoutTask.getAutoDialoutTaskName() + "</b>?", Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this, "confirmDelete");
		this.getWindow().addWindow(confirmWindow);
	}

	/**
	 * 停止线程
	 */
	private void stopThreads() {
logger.info("chb: autodialstop stopThreads start!");
		// 停止消费线程
		ProjectResourceConsumer consumerThread = (ProjectResourceConsumer) AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_CONSUMER_PRE + autoDialoutTask.getId());
		// 应该一定不为null，但为了安全，进行判断
		if (consumerThread != null) {
logger.info("chb: autodialstop stopThreads set consumer loop false!");
			// 由于销毁线程的方法有缺点和风险，故用此方式使线程自动停止
			consumerThread.setIsLoop(false);

			// 如果只有当前的消费线程依赖生产线程，则停止生产线程
			ProjectResourceProducer producerThread = (ProjectResourceProducer) AutoDialHolder.nameToThread
					.get(AutoDialHolder.AUTODIAL_PRODUCER_PRE
							+ autoDialoutTask.getMarketingProject().getId());
			if (producerThread != null) {
				producerThread.removeDependConsumer(consumerThread.getName());
			}
		}
logger.info("chb: autodialstop stopThreads end!");
	}

	/**
	 * 启动线程
	 */
	private void startThreads() {
		Long projectId = autoDialoutTask.getMarketingProject().getId();

		// ==============获取资源线程处理========================//
		// 如果ThreadHolder中没有启动此项目中的资源获取线程，则启动，如果已经启动则不再启动
		// 以“前缀+项目Id”作为生产线程的名字
		ProjectResourceProducer producerThread = (ProjectResourceProducer) AutoDialHolder.nameToThread
				.get(AutoDialHolder.AUTODIAL_PRODUCER_PRE + projectId);
		if (producerThread == null || producerThread.getIsLoop() == false) {
			producerThread = new ProjectResourceProducer(autoDialoutTask, projectId);
		}

		// ==============消费资源线程处理========================//
		// 启动消费线程
		Thread consumerThread = AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_CONSUMER_PRE + autoDialoutTask.getId());
		// 应该一定为null，但为了安全，进行判断
		if (consumerThread == null) {
			// 参数为生产线程名字和消费线程名字
			consumerThread = new ProjectResourceConsumer(autoDialoutTask, AutoDialHolder.AUTODIAL_PRODUCER_PRE + projectId);
		} else {
			System.err.println("原线程" + AutoDialHolder.AUTODIAL_CONSUMER_PRE + autoDialoutTask.getId() + "还未停止,原线程被替换！");
			((ProjectResourceConsumer) consumerThread).setIsLoop(false);
			// 参数为生产线程名字和消费线程名字
			consumerThread = new ProjectResourceConsumer(autoDialoutTask, AutoDialHolder.AUTODIAL_PRODUCER_PRE + projectId);
		}
		producerThread.addDependConsumer(consumerThread);
	}

	/**
	 * 显示添加CSR的窗口
	 */
	private void showAssignCsrWindow() {
		if (assignCsrWindow == null) {
			try {
				assignCsrWindow = new AutoDialoutAssignCsr(this);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(assignCsrWindow);
		this.getApplication().getMainWindow().addWindow(assignCsrWindow);
	}
	
	/**
	 * 显示管理手机成员窗口
	 */
	private void showAssignPhoneNoWindow() {
		if (assignPhoneNoWindow == null) {
			try {
				assignPhoneNoWindow = new AutoDialoutAssignPhoneNo(this);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(assignPhoneNoWindow);
		this.getApplication().getMainWindow().addWindow(assignPhoneNoWindow);
	}

	/**
	 * 显示管回收资源窗口
	 */
	private void showRecycleWindow() {
		if (recycleWindow == null) {
			try {
				recycleWindow = new AutoDialoutRecycle(this);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(recycleWindow);
		this.getApplication().getMainWindow().addWindow(recycleWindow);
	}

	/**
	 * 显示按批次回收资源窗口
	 */
	private void showRecycleToBatchWindow() {
		if (recycleToBatchWindow == null) {
			try {
				recycleToBatchWindow = new AutoDialoutRecycleToBatch(this);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(recycleToBatchWindow);
		this.getApplication().getMainWindow().addWindow(recycleToBatchWindow);
	}

	/**
	 * 显示添加CSR的窗口
	 */
	private void showAutoDialoutMonitorWindow() {
		if (autoDialoutMonitorWindow == null) {
			try {
				autoDialoutMonitorWindow = new AutoDialoutMonitor(this);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"弹出窗口失败");
				return;
			}
		}
		// 先移除，再添加Window
		this.getWindow().removeWindow(autoDialoutMonitorWindow);
		this.getWindow().addWindow(autoDialoutMonitorWindow);
	}

	/**
	 * 显示自动外呼窗口
	 */
	private void showAddAutoDialoutTaskWindow() {
		if (addWindow == null) {
			addWindow = new AddAutoDialoutTask(this);
		}
		this.getWindow().removeWindow(addWindow);
		this.getWindow().addWindow(addWindow);
	}

	/**
	 * 由buttonClick调用，显示编辑自动外呼窗口
	 */
	private void showEditAutoDialoutTaskWindow() {
		if (editAutoDialoutTaskWindow == null) {
			editAutoDialoutTaskWindow = new EditAutoDialoutTask(this);
		}
		this.getWindow().removeWindow(editAutoDialoutTaskWindow);
		this.getWindow().addWindow(editAutoDialoutTaskWindow);
	}

	/**
	 * 显示帮助窗口
	 */
	private void showHelpWindow() {
		Window helpWindow=new Window("说明信息");
		helpWindow.setWidth("50em");
		helpWindow.setResizable(false);
		helpWindow.center();
		VerticalLayout windowLayout=new VerticalLayout();
		windowLayout.setMargin(true);
		windowLayout.setSpacing(true);
		
		//说明监控参数
		Panel upperPanel=new Panel("监控参数说明");
//		upperPanel.setWidth("30em");
		StringBuilder stringBuilder1=new StringBuilder();
//		stringBuilder1.append("<font color=' red'>监控参数说明</font> </br>");
		stringBuilder1.append("<b>项目:</b>自动外呼使用的项目</br>");
		stringBuilder1.append("<b>自动外呼:</b>监控的是哪个自动外呼</br></br>");
		
		stringBuilder1.append("<b>外线:</b>自动外呼当前使用的外线</br>");
		stringBuilder1.append("<b>并发数:</b>当前这条外线上的并发，该数字为正在振铃和已经建立通话的总数</br>");
		stringBuilder1.append("<b>外线容量:</b>是当前外线允许的最大并发数,请确认此数字小于等于外线实际支持的并发总数</br></br>");
		
		stringBuilder1.append("<b>CSR空闲数:</b>当前可用坐席的总数</br>");
		stringBuilder1.append("<b>CSR登陆数:</b>所有加入到队列中的坐席总数</br></br>");

		stringBuilder1.append("<b>队列:</b>呼入的电话进入到系统中的哪个队列</br>");
		stringBuilder1.append("<b>排队数:</b>在队列中等待坐席接起的客户数量</br>");
		stringBuilder1.append("<b>队列长度:</b>队列中最多能容纳多少客户</br></br>");
		
		Label spyNoteLabel=new Label(stringBuilder1.toString(),Label.CONTENT_XHTML);
		upperPanel.addComponent(spyNoteLabel);
		windowLayout.addComponent(upperPanel);

		//说明统计数据
		Panel lowerPanel=new Panel("统计参数说明");
		StringBuilder stringBuilder2=new StringBuilder();
//		stringBuilder2.append("<font color=' red'>统计参数说明</font> </br>");
		stringBuilder2.append("<b>呼叫数量:</b>发起呼叫的总数</br>");
		stringBuilder2.append("<b>呼通数量:</b>客户接起电话的总数</br>");
		stringBuilder2.append("<b>漏接数量:</b>客户接起，坐席未接的电话总数</br></br>");
		
		stringBuilder2.append("<b>呼通率:</b>呼通数量/呼叫数量</br>");
		stringBuilder2.append("<b>呼损率:</b>漏接数量/呼通数量</br></br>");

		Label staticsNoteLabel=new Label(stringBuilder2.toString(),Label.CONTENT_XHTML);
		lowerPanel.addComponent(staticsNoteLabel);
		windowLayout.addComponent(lowerPanel);
		
		helpWindow.setContent(windowLayout);
		this.getWindow().addWindow(helpWindow);
	}
	
	/**
	 * 由executeSearch调用更新表格内容
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 * @param isToFirst
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	/**
	 * 由弹出窗口回调确认删除自动外呼
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed == true) {
			try {
				
				autoDialoutTaskService.deleteById(autoDialoutTask.getId());
OperationLogUtil.simpleLog(loginUser, "自动外呼-删除："+autoDialoutTask.getAutoDialoutTaskName());
				Queue queue = autoDialoutTask.getQueue();
				// jrh 删除动态队列里的成员
				List<UserQueue> userQueues = userQueueService.getAllByQueueName(queue.getName(), domain);
				for(UserQueue userQueue : userQueues) {
					// 如果删除了某条动态成员，并且该用户在线， 则需要向Asterisk 发送 Action
					List<User> users = userService.getUsersByUsername(userQueue.getUsername());
					User user = null;
					if(users.size() > 0) {
						user = users.get(0);
						String exten = ShareData.userToExten.get(user.getId());
						if(exten != null) {
							queueMemberRelationService.removeQueueMemberRelation(queue.getName(), exten);
						}
					}
					// 删除动态队列成员
					userQueueService.deleteById(userQueue.getId());
				}
				
				// 同时应该删除队列，并刷新Asterisk
				queueService.deleteById(queue.getId());
				// 将Queue刷新到Asterisk
				queueService.updateAsteriskQueueFile(domain);
				// 更新Table数据，并使Table处于未被选中状态
				this.updateTable(false);
				table.setValue(null);
			} catch (Exception e) {
				NotificationUtil.showWarningNotification(this,"删除异常！");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 更新自动外呼显示的信息
	 */
	public void updateInfo() {
		DecimalFormat df=new DecimalFormat("#00.00");
		if (table == null || table.getValue() == null) {
			callCount.setValue("");
			answeredCount.setValue("");
			missedCount.setValue("");
			missedRate.setValue("");
			answeredRate.setValue("");
			return;
		}

		//呼叫总数
		String callCountSql = "select count(*) from ec2_marketing_project_task where domain_id="+ domain.getId()
				+ " and autodialid="+autoDialoutTask.getId()+" and isfinished=true"; //加isfinished和域都 不起作用
		//客户应答总数
		String answeredCountSql = "select count(*) from ec2_marketing_project_task where domain_id="+ domain.getId()
				+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and isfinished=true";
		//坐席漏接总数
		String missedCountSql = "select count(*) from ec2_marketing_project_task where domain_id="+ domain.getId()
				+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and autodialiscsrpickup=false";

		// 查询数据并设置数据源
		Long callCountNum = 0L;
		Long answeredCountNum = 0L;
		Long missedCountNum = 0L;

		callCountNum=(Long) commonService.excuteNativeSql(callCountSql, ExecuteType.SINGLE_RESULT);
		answeredCountNum=(Long) commonService.excuteNativeSql(answeredCountSql,ExecuteType.SINGLE_RESULT);
		missedCountNum=(Long) commonService.excuteNativeSql(missedCountSql,ExecuteType.SINGLE_RESULT);
		
		//为label设值
		callCount.setValue(callCountNum);
		answeredCount.setValue(answeredCountNum);
		missedCount.setValue(missedCountNum);
		if(answeredCountNum!=0){
			missedRate.setValue(df.format(missedCountNum*100.0/answeredCountNum)+"%");
		}else{
			missedRate.setValue(df.format(0f)+"%");
		}
		if(callCountNum!=0){
			answeredRate.setValue(df.format(answeredCountNum*100.0/callCountNum)+"%");
		}else{
			answeredRate.setValue(df.format(0f)+"%");
		}
	}

	/**
	 * 返回FlipOver的一个引用
	 * 
	 * @return
	 */
	public FlipOverTableComponent<AutoDialoutTask> getFlip() {
		return flip;
	}

	/**
	 * 返回Table的一个引用
	 * 
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlSelect
	 */
	public void setSqlSelect(String sqlSelect) {
		this.sqlSelect = sqlSelect;
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlCount
	 */
	public void setSqlCount(String sqlCount) {
		this.sqlCount = sqlCount;
	}

	/**
	 * 取得当前选中的自动外呼
	 * 
	 * @return
	 */
	public AutoDialoutTask getCurrentSelect() {
		return autoDialoutTask;
	}

	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if (target == null) {
			return new Action[] { ADD };
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (START == action) {
			start.click();
		} else if (STOP == action) {
			stop.click();
		} else if (ADD == action) {
			add.click();
		} else if (EDIT == action) {
			edit.click();
		} else if (MONITOR == action) {
			monitor.click();
		} else if (DELETE == action) {
			delete.click();
		}
	}

	/**
	 * 按钮单击监听器
	 * <p>
	 * 搜索、高级搜索、新建自动外呼、开始、停止、添加CSR/添加资源 、指派任务
	 * </p>
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == search) {
			// 普通搜索
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"搜索出现错误");
			}
		} else if (event.getButton() == start) {
OperationLogUtil.simpleLog(loginUser, "自动外呼-开始："+autoDialoutTask.getAutoDialoutTaskName());
			
			//判断号码是否呼完，如果呼完提示用户 
			String sql="select count(mpt) from MarketingProjectTask mpt where mpt.user=null and mpt.marketingProject.id="+autoDialoutTask.getMarketingProject().getId();
			Long count=(Long)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
			if(count<1){
				NotificationUtil.showWarningNotification(this,"资源不足，请在项目["+autoDialoutTask.getMarketingProject().getProjectName()+"]中添加资源");
				return;
			}
			
			try {
				// 如果是没有在进行中的项目则调用startThreads操作
				if (!autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)) {
					autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.RUNNING);
					autoDialoutTask = autoDialoutTaskService.update(autoDialoutTask);
					startThreads();
					this.updateTable(false);
				} else {
					NotificationUtil.showWarningNotification(this,"自动外呼正在运行！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"自动外呼开始异常！");
			}
		} else if (event.getButton() == stop) {// ||event.getButton() == pause)
logger.info("chb: autodialstop button clicked!");

OperationLogUtil.simpleLog(loginUser, "自动外呼-停止："+autoDialoutTask.getAutoDialoutTaskName());

			// 自动外呼结束
			try {
				if (autoDialoutTask.getAutoDialoutTaskStatus().equals(
						AutoDialoutTaskStatus.RUNNING)) {
logger.info("chb: autodialstop running stop!");
					// 先停止线程，然后更新自动外呼的状态，如果停止线程失败，则不会改变自动外呼正在运行的状态
					stopThreads();
					autoDialoutTask
							.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.OVER);
					autoDialoutTask = autoDialoutTaskService
							.update(autoDialoutTask);
					// 让线程睡眠3s，以保证线程安全停止
					Thread.sleep(3000);
					this.updateTable(false);
				} else {
logger.info("chb: autodialstop notrunning stop!");
					stopThreads();
					NotificationUtil.showWarningNotification(this,"自动外呼还没有运行！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"自动外呼结束异常！");
			}
		} else if (event.getButton() == delete) {
			// 自动外呼删除
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"删除出现异常！");
			}
		} else if (event.getButton() == monitor) {
			showAutoDialoutMonitorWindow();
		} else if (event.getButton() == edit) {
			showEditAutoDialoutTaskWindow();
		} else if (event.getButton() == add) {
			showAddAutoDialoutTaskWindow();
		} else if (event.getButton() == assignCsr) {
			// /查看自动外呼是不是有可用的CSR
			String nativeSql = "select count(*) from ec2_markering_project_ec2_user where marketingproject_id="
					+ autoDialoutTask.getMarketingProject().getId();
			Long csrNum = (Long) commonService.excuteNativeSql(nativeSql,
					ExecuteType.SINGLE_RESULT);
			if (csrNum <= 0) {
				NotificationUtil.showWarningNotification(this,"目前还没有可用的CSR");
				return;
			}
			showAssignCsrWindow();
		} else if (event.getButton() == assignPhoneNo) {
			showAssignPhoneNoWindow();
		}else if (event.getButton() == recycle) {
			showRecycleWindow();
		}else if (event.getButton() == recycleToBatch) {
			showRecycleToBatchWindow();
		}else if (event.getButton() == description) {
			showHelpWindow();
		}
	}

	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		autoDialoutTask = (AutoDialoutTask) table.getValue();
		// 改变状态信息（项目数目状态）
		updateInfo();
		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			start.setEnabled(true);
			stop.setEnabled(true);
			monitor.setEnabled(true);
			assignCsr.setEnabled(true);
			assignPhoneNo.setEnabled(true);
			recycle.setEnabled(true);
			recycleToBatch.setEnabled(true);
			delete.setEnabled(true);
			edit.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			start.setEnabled(false);
			stop.setEnabled(false);
			monitor.setEnabled(false);
			assignCsr.setEnabled(false);
			assignPhoneNo.setEnabled(false);
			recycleToBatch.setEnabled(false);
			recycle.setEnabled(false);
			delete.setEnabled(false);
			edit.setEnabled(false);
		}
	}
}
