package com.jiangyifen.ec2.ui.csr.workarea.marketingtask;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.asteriskjava.fastagi.AgiChannel;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.service.mgr.DistributeToTaskService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.common.DialComponentToTable;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * 我的营销营销任务显示界面
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class MyMarketingTaskTabView extends VerticalLayout implements ValueChangeListener, ClickListener {

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"customerResource.id", "customerResource.name", 
			"customerResource.telephones", "orderTime", "isFinished", "isAnswered", "lastStatus", "marketingProject.projectName"};
	
	private final String[] COL_HEADERS = new String[] {"客户编号", "客户姓名", "电话号码", "预约时间", "完成", "接通", "联系结果", "项目名称"};
	
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private Notification success_notification;					// 成功过提示信息
	private Notification warning_notification;					// 错误警告提示信息
	
	private Integer[] screenResolution;							// 屏幕分辨率
	
	private Button achieveMoreTaskButton;						// 获取更多的营销任务按钮
	
	private Table unfinishedTaskTable;							// 存放未完成的营销任务
	private Table finishedTaskTable;							// 存放已完成的营销任务
	private Table preFocusTable;								// 上一次操作的表格
	private OptionGroup executeAutoDial_og;						// 是否执行自动慢拨号
	
	private UnfinishedMarketingTaskFilter unfinishedTaskComplexFilter;	// 未完成任务搜索组件
	private FinishedMarketingTaskFilter finishedTaskComplexFilter;		// 已完成任务搜索组件
	
	private FlipOverTableComponent<MarketingProjectTask> unfinishedTableFlip;	// 为unfinishedTaskTable添加的翻页组件
	private FlipOverTableComponent<MarketingProjectTask> finishedTableFlip;		// 为finishedTaskTable添加的翻页组件

	private String unfinishedCountSql;							// 未完成营销任务的统计数量SQL
	private String unfinishedSearchSql;							// 未完成营销任务的搜索SQL
	private String finishedCountSql;							// 已完成营销任务的统计数量SQL
	private String finishedSearchSql;							// 已完成营销任务的搜索SQL
	
	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户基本信息查看窗口 
	private OutgoingDialForTaskWindow outgoingDialForTaskWindow;// 客户信息弹屏窗口
	
	private User loginUser;										// 当前登录的用户
	private String exten;										// 当前用户所使用的分机
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限
	private CustomerResource customerResource;					// 表格中选中的营销任务项对应的客户资源对象
	private Long currentWorkingProjectId;						// 当前工作项目的id号
	private MarketingProject currentMarketingProject;			// 当前工作项目
	
	private MarketingProjectTaskService projectTaskService;		// 项目营销任务服务类
	private MarketingProjectService marketingProjectService;	// 项目服务类
	private DistributeToTaskService distributeToTaskService;	// 分配营销任务服务类
	private TimersService timersService;						// 定时器服务类
	
	public MyMarketingTaskTabView() {
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		SpringContextHolder.getHttpSession().setAttribute("myTaskTabView", this);
		
		exten = SpringContextHolder.getExten();
		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		projectTaskService = SpringContextHolder.getBean("marketingProjectTaskService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		distributeToTaskService = SpringContextHolder.getBean("distributeToTaskService");
		timersService = SpringContextHolder.getBean("timersService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		HorizontalLayout taskHLayout = new HorizontalLayout();
		taskHLayout.setWidth("100%");
		taskHLayout.setSpacing(true);
		this.addComponent(taskHLayout);

		// 初始化表格翻页组件的查询语句
		initializeTableFlipSql();
		
		VerticalLayout unfinishedTaskLayout = new VerticalLayout();
		unfinishedTaskLayout.setSpacing(true);
		taskHLayout.addComponent(unfinishedTaskLayout);

		// 创建未完成营销任务的搜索布局管理器，及相应的组件
		createSearchHLayout1(unfinishedTaskLayout);
		
		// 创建未完成营销任务表格和翻页组件
		createUnfinishedComponents(unfinishedTaskLayout);
		
		VerticalLayout finishedTaskLayout = new VerticalLayout();
		finishedTaskLayout.setSpacing(true);
		taskHLayout.addComponent(finishedTaskLayout);

		// 创建已完成营销任务的搜索布局管理器，及相应的组件
		createSearchHLayout2(finishedTaskLayout);
		
		// 创建已完成营销任务表格和翻页组件，及过滤器
		createfinishedComponents(finishedTaskLayout);

		// 设置两个营销任务表格的行数，及翻页组件的每次加载的营销任务数量
		setTablePageLength();
		
		// 创建呼叫客户时需要显示个各种信息的组件
		createOutgoingDialWindow();
	}

	/**
	 * 初始化表格的翻页组件的搜索语句
	 */
	private void initializeTableFlipSql() {
		currentWorkingProjectId = ShareData.extenToProject.get(exten);
		if(currentWorkingProjectId == null) {
			currentWorkingProjectId = 0L;
		} else {
			currentMarketingProject = marketingProjectService.get(currentWorkingProjectId);
		}

		String typeSql = MarketingProjectTaskType.class.getName()+".MARKETING";
		unfinishedCountSql = "Select count(mpt) from MarketingProjectTask as mpt where mpt.user.id = "+loginUser.getId() +
				" and mpt.marketingProject.id = " + currentWorkingProjectId + " and mpt.isFinished = false and mpt.isUseable = true" +
				" and mpt.marketingProjectTaskType = " + typeSql;
		unfinishedSearchSql = unfinishedCountSql.replaceFirst("count\\(mpt\\)", "mpt") + " order by mpt.distributeTime asc,mpt.id asc";
		
		finishedCountSql = "Select count(mpt) from MarketingProjectTask as mpt where mpt.user.id = "+loginUser.getId() +
				" and mpt.marketingProject.id = " + currentWorkingProjectId + " and mpt.isFinished = true and mpt.isUseable = true" +
				" and mpt.marketingProjectTaskType = " + typeSql;
		finishedSearchSql = finishedCountSql.replaceFirst("count\\(mpt\\)", "mpt") + " order by mpt.lastUpdateDate desc, mpt.id asc";
	}
	
	/**
	 * 创建未完成营销任务的搜索布局管理器，及相应的组件, 并加入 营销任务垂直布局管理器中
	 * @param taskVLayout 垂直布局管理器
	 */
	private void createSearchHLayout1(VerticalLayout taskVLayout) {
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setWidth("100%");
		searchHLayout.setHeight("25px");
		searchHLayout.setSpacing(true);
		taskVLayout.addComponent(searchHLayout);

		Label unfinishTableCaption = new Label("<B>未完成任务--营销</B>", Label.CONTENT_XHTML);
		unfinishTableCaption.setWidth("-1px");
		searchHLayout.addComponent(unfinishTableCaption);
		searchHLayout.setExpandRatio(unfinishTableCaption, 1.0f);
		
		// jrh 2013-12-18 -------------- 开始
		HorizontalLayout autoDialHLayout = new HorizontalLayout();
		searchHLayout.addComponent(autoDialHLayout);
		
		Label autoDial_lb = new Label("<font color='blue'><b>自动慢拨号:</b></font>", Label.CONTENT_XHTML);
		autoDial_lb.setWidth("-1px");
		autoDialHLayout.addComponent(autoDial_lb);
		
		executeAutoDial_og = new OptionGroup();
		executeAutoDial_og.setNullSelectionAllowed(false);
		executeAutoDial_og.addItem(true);
		executeAutoDial_og.addItem(false);
		executeAutoDial_og.setItemCaption(true, "开启");
		executeAutoDial_og.setItemCaption(false, "关闭");
		executeAutoDial_og.setValue(false);
		executeAutoDial_og.addListener(this);
		executeAutoDial_og.setImmediate(true);
		executeAutoDial_og.setStyleName("twocol100");
		autoDialHLayout.addComponent(executeAutoDial_og);
		// jrh ------------------------- 结束
		
		unfinishedTaskComplexFilter = new UnfinishedMarketingTaskFilter();
		unfinishedTaskComplexFilter.refresh(currentWorkingProjectId);
		
		PopupView complexSearchView = new PopupView(unfinishedTaskComplexFilter);
		complexSearchView.setWidth("-1px");
		complexSearchView.setHideOnMouseOut(false);
		searchHLayout.addComponent(complexSearchView);
	}
	
	/**
	 * 创建未完成营销任务表格和翻页组件
	 * @param unfinishedTaskLayout
	 */
	private void createUnfinishedComponents(VerticalLayout unfinishedTaskLayout) {
		unfinishedTaskTable = createFormatColumnTable();
		unfinishedTaskTable.setData("unfinished_table");		// 供弹屏关闭时调用表格的Data
		unfinishedTaskLayout.addComponent(unfinishedTaskTable);
		
		// 创建自动生成列
		unfinishedTaskTable.addGeneratedColumn("customerResource.telephones", new DialColumnGenerator());
		unfinishedTaskTable.setColumnWidth("customerResource.telephones", 192);

		// 添加右键单击事件
		addActionToTable(unfinishedTaskTable);
		
		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setWidth("100%");
		bottomLayout.setSpacing(true);
		unfinishedTaskLayout.addComponent(bottomLayout);
		
		achieveMoreTaskButton = new Button("获取新营销任务", this);
		achieveMoreTaskButton.setImmediate(true);
		achieveMoreTaskButton.setStyleName("default");
		bottomLayout.addComponent(achieveMoreTaskButton);
		
		unfinishedTableFlip = createTableFlipOver(unfinishedTaskTable, unfinishedCountSql, unfinishedSearchSql);
		bottomLayout.addComponent(unfinishedTableFlip);
		bottomLayout.setComponentAlignment(unfinishedTableFlip, Alignment.TOP_RIGHT);

		// 为我的未完成营销任务 的高级搜索组件传递 ‘翻页组件’
		unfinishedTaskComplexFilter.setUnfinishedTableFlip(unfinishedTableFlip);
	}
	
	/**
	 * 创建未完成营销任务的搜索布局管理器，及相应的组件, 并加入 营销任务垂直布局管理器中
	 * @param taskVLayout 垂直布局管理器
	 */
	private void createSearchHLayout2(VerticalLayout finishedTaskLayout) {
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setWidth("100%");
		searchHLayout.setHeight("25px");
		finishedTaskLayout.addComponent(searchHLayout);

		Label unfinishTableCaption = new Label("<B>已完成任务--营销</B>", Label.CONTENT_XHTML);
		searchHLayout.addComponent(unfinishTableCaption);
		searchHLayout.setExpandRatio(unfinishTableCaption, 1.0f);

		finishedTaskComplexFilter = new FinishedMarketingTaskFilter();
		finishedTaskComplexFilter.refresh(currentWorkingProjectId);
		
		PopupView complexSearchView = new PopupView(finishedTaskComplexFilter);
		complexSearchView.setWidth("-1px");
		complexSearchView.setHideOnMouseOut(false);
		searchHLayout.addComponent(complexSearchView);
	}
	
	/**
	 *  创建已完成营销任务表格和翻页组件，及过滤器
	 * @param finishedTaskLayout
	 */
	private void createfinishedComponents(VerticalLayout finishedTaskLayout) { 
		finishedTaskTable = createFormatColumnTable();
		finishedTaskTable.setData("finished_table");		// 供弹屏关闭时调用表格的Data
		finishedTaskLayout.addComponent(finishedTaskTable);

		// 创建自动生成列
		finishedTaskTable.addGeneratedColumn("customerResource.telephones", new DialColumnGenerator());
		finishedTaskTable.setColumnWidth("customerResource.telephones", 192);

		// 添加右键单击事件
		addActionToTable(finishedTaskTable);
		
		finishedTableFlip = createTableFlipOver(finishedTaskTable, finishedCountSql, finishedSearchSql);
		finishedTaskLayout.addComponent(finishedTableFlip);
		finishedTaskLayout.setComponentAlignment(finishedTableFlip, Alignment.TOP_RIGHT);

		// 为我的已完成营销问卷任务 的高级搜索组件传递 ‘翻页组件’
		finishedTaskComplexFilter.setFinishedTableFlip(finishedTableFlip);
	}

	/**
	 *  创建格式化 了 营销任务完成状态列和营销任务接通状态列 的 Table对象
	 */
	private Table createFormatColumnTable() {
		Table table = new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date)property.getValue());
				} else if(property.getType() == Boolean.class) {
					if(property.getValue() == null) {
						return "";
					} 
					if(property.getValue().equals(true)) {
						return "是";
					} else {
						return "";
					}
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		
		table.setWidth("100%");
		table.setHeight("-1px");
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setStyleName("striped");
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		return table;
	}
	
	/**
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class DialColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("customerResource.telephones")) {
				MarketingProjectTask projectTask = (MarketingProjectTask) itemId;
				Set<Telephone> telephones = projectTask.getCustomerResource().getTelephones();
				return new DialComponentToTable(source, itemId, telephones, loginUser, ownBusinessModels, MyMarketingTaskTabView.this);
			} 
			return null;
		}
	}

	/**
	 * 为指定营销任务表格 添加右键单击事件
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
	 * 为指定表格创建翻页组件
	 * @param table			需要创建翻页组件的表格
	 * @param countSql		统计查询语言
	 * @param searchSql		搜索查询语言
	 * @return
	 */
	private FlipOverTableComponent<MarketingProjectTask> createTableFlipOver(Table table, String countSql, String searchSql) {
		FlipOverTableComponent<MarketingProjectTask> tableFlipOver = new FlipOverTableComponent<MarketingProjectTask>(MarketingProjectTask.class, 
				projectTaskService, table, searchSql , countSql, null);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			tableFlipOver.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
		return tableFlipOver;
	}

	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			setPageLength(34, 34);
		} else if(screenResolution[1] >= 1050) {
			setPageLength(32, 32);
		} else if(screenResolution[1] >= 900) {
			setPageLength(25, 25);
		} else if(screenResolution[1] >= 768) {
			setPageLength(17, 17);
		} else {
			setPageLength(12, 12);
		}
	}
	
	/**
	 * 设置两个营销任务表格的行数，及翻页组件的每次加载的营销任务数量
	 * @param len1	未  完成记录的显示行数
	 * @param len2	已  完成记录的显示行数
	 */
	private void setPageLength(int len1, int len2) {
		unfinishedTaskTable.setPageLength(len1);
		unfinishedTableFlip.setPageLength(len1, false);
		finishedTaskTable.setPageLength(len2);
		finishedTableFlip.setPageLength(len2, false);
	}
	
	/**
	 *  创建客户信息查看窗口及相关组件
	 */
	private void createCustomerAllInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.csr);
		customerAllInfoWindow.setResizable(false);
		customerAllInfoWindow.setEchoModifyByReflect(this);
		
		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}
	
	/**
	 * 创建呼叫客户时需要显示个各种信息的组件
	 */
	private void createOutgoingDialWindow() {
		outgoingDialForTaskWindow = new OutgoingDialForTaskWindow();

		if(screenResolution[0] >= 1366) {
			outgoingDialForTaskWindow.setWidth("1190px");
		} else {
			outgoingDialForTaskWindow.setWidth("940px");
		}
		
		if(screenResolution[1] > 768) {
			outgoingDialForTaskWindow.setHeight("610px");
		} else if(screenResolution[1] == 768) {
			outgoingDialForTaskWindow.setHeight("560px");
		} else {
			outgoingDialForTaskWindow.setHeight("510px");
		}

		outgoingDialForTaskWindow.setResizable(false);
		outgoingDialForTaskWindow.setStyleName("opaque");
		outgoingDialForTaskWindow.setEchoModifyByReflect(this);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == unfinishedTaskTable) {
			handleTableValueChangeEvent(unfinishedTaskTable, finishedTaskTable);
		} else if(source == finishedTaskTable) {
			handleTableValueChangeEvent(finishedTaskTable, unfinishedTaskTable);
		} else if(source == executeAutoDial_og) {
			boolean isExecuteAutoDial = (Boolean) executeAutoDial_og.getValue();
			outgoingDialForTaskWindow.setExecuteAutoDial(isExecuteAutoDial);
		} 
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == achieveMoreTaskButton) {
			// 检查是否存在当前工作项目
			if(currentMarketingProject == null) {
				warning_notification.setCaption("您当前没有工作项目，所以无法获取新的营销任务！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}

			// 检查当前工作项目是否为营销类型
			if(MarketingProjectTaskType.MARKETING.getIndex() != currentMarketingProject.getMarketingProjectType().getIndex()) {
				warning_notification.setCaption("您当前工作项目的类型不是【营销】项目！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			
			// 检查当前工作项目是否有相应关联的批次
			Set<CustomerResourceBatch> resourceBatchs = currentMarketingProject.getBatches();
			if(resourceBatchs.isEmpty()) {
				warning_notification.setCaption("您当前工作的项目下没有【营销】任务！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}

			// 获取当前项目下坐席最多拥有未完成营销任务总数
			Integer maxUnfinishedTaskCount = currentMarketingProject.getCsrMaxUnfinishedTaskCount();
			if(maxUnfinishedTaskCount == null) {	// 一般不会出现，保险起见
				maxUnfinishedTaskCount = 100;
			}

			// 获取未完成的营销任务总数，如果未完成的营销任务大于设定值，则不获取新营销任务
			int unfinishedTaskCount = unfinishedTableFlip.getTotalRecord();
			if(unfinishedTaskCount >= maxUnfinishedTaskCount) {
				warning_notification.setCaption("您尚有不低于 "+maxUnfinishedTaskCount+" 条未完成的【营销】任务，请完成营销任务后再来获取新【营销】任务！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			
			// 当前话务员当前最多还能获取的任务条数
			int maxTotalPickTaskCount = maxUnfinishedTaskCount - unfinishedTaskCount;
			try {
				List<MarketingProjectTask> projectTasks = distributeToTaskService.distributeByCsr(currentMarketingProject, maxTotalPickTaskCount, loginUser, loginUser.getDomain());
				if(projectTasks.size() > 0) {
					this.getApplication().getMainWindow().showNotification("成功获取 " +projectTasks.size()+ " 条任务！");
				}
				this.createNewTimeNotices(projectTasks);
			} catch (Exception e) {
				warning_notification.setCaption(e.getMessage());
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			
			unfinishedTableFlip.refreshToFirstPage();
		}
	}

	/**
	 * 根据营销任务对象，创建定时提醒
	 * @param projectTasks
	 */
	private void createNewTimeNotices(List<MarketingProjectTask> projectTasks) {
		for(MarketingProjectTask task : projectTasks) {
			Date orderTime = task.getOrderTime();
			if(orderTime == null) {
				continue;
			}
			// 标题
			String title = "";
			for(Telephone phoneNo : task.getCustomerResource().getTelephones()) {
//				TODO 电话号码加密问题
				title = "客户联系电话号为：" +phoneNo+ " ，ID 为："+task.getCustomerResource().getId();
				break;
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(orderTime);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			Timers timers = new Timers();
			timers.setCreator(loginUser);
			timers.setDomain(loginUser.getDomain());
			timers.setResponseTime(orderTime);
			timers.setType("一次");
			String orderNote = "用户名："+loginUser.getUsername() +task.getOrderNote();
			timers.setContent(orderNote);
			timers.setTitle(title);
			timersService.save(timers);
			
			// 更新定时营销任务
			timersService.refreshSchedule(false, timers);
		}
	}

	/**
	 * 处理表格选中项发生变化事件	 回显被选中的Tab 页中的信息
	 * @param focusTable	当前正在操作的表格，并且值发生改变
	 * @param defocusTable	当前未使用的表格
	 */
	private void handleTableValueChangeEvent(Table focusTable, Table defocusTable) {
		// 第一步：获取表格中的选中项，并将选中营销任务对应的资源对象传给响应的组件
		if(focusTable.getValue() != null) {
			customerResource = ((MarketingProjectTask) focusTable.getValue()).getCustomerResource();
			if(defocusTable.getValue() != null) {	// 如果当前未使用的表格的值不为空，则将其置为空
				defocusTable.removeListener(this);
				defocusTable.setValue(null);
				defocusTable.addListener(this);
			}
		}
		
		// 修改呼叫弹屏中的焦点表格
		if(focusTable == unfinishedTaskTable && focusTable != preFocusTable) {
			outgoingDialForTaskWindow.refreshServiceRecordInfoEditor(unfinishedTableFlip, finishedTableFlip);
		} else if(focusTable == finishedTaskTable && focusTable != preFocusTable) {
			outgoingDialForTaskWindow.refreshServiceRecordInfoEditor(finishedTableFlip, unfinishedTableFlip);
		}
		preFocusTable = focusTable;
	}
	
	/**
	 * 供CustomerBaseInfoEditorForm 调用
	 * 当其他组件中改变了客户信息后，则刷新当前正在操作的Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		MarketingProjectTask unfinishedTableValue = (MarketingProjectTask) unfinishedTaskTable.getValue();
		MarketingProjectTask finishedTableValue = (MarketingProjectTask) finishedTaskTable.getValue();
		
		// 根据营销任务表格中的值进行刷新及回显操作
		if(unfinishedTableValue != null) {
			unfinishedTableFlip.refreshInCurrentPage();
		} else if(finishedTableValue != null) {
			finishedTableFlip.refreshInCurrentPage();
		} else {
			unfinishedTableFlip.refreshInCurrentPage();
			finishedTableFlip.refreshInCurrentPage();
		}
	}
	
	/**
	 * 刷新我的营销任务显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			unfinishedTableFlip.refreshToFirstPage();
			finishedTableFlip.refreshToFirstPage();
		} else {
			unfinishedTableFlip.refreshInCurrentPage();
			finishedTableFlip.refreshInCurrentPage();
		}
	}
	
	/**
	 * 供自动生成的拨号按钮使用反射机制调用
	 */
	public void changeCurrentTab() {
		ShareData.csrToCurrentTab.put(loginUser.getId(), MyMarketingTaskTabView.this);
	}
	
	/**
	 * @Description 描述：
	 *
	 * @author  JRH
	 * @date    2014年6月6日 下午8:20:00 
	 */
	public void echoPopupWindowInfo() {
		MarketingProjectTask projectTask = (MarketingProjectTask) preFocusTable.getValue();
		if(projectTask != null) {
			outgoingDialForTaskWindow.refreshTaskInWindow(projectTask);
		}
	}
	
	/**
	 * 当用户正在工作的项目发生变化的时候，刷新我的营销任务模块的相关信息
	 */
	public void refreshMarketingTaskModel() {
		// 第一步：重新初始化表格翻页组件的查询语句
		initializeTableFlipSql();
		
		// 第二步：将各搜索组件中的值还原成默认值
		unfinishedTaskComplexFilter.refresh(currentWorkingProjectId);
		finishedTaskComplexFilter.refresh(currentWorkingProjectId);
		
		// 第三步：重新设置各表格翻页组件的查询语句 并且刷新值首页
		unfinishedTableFlip.setSearchSql(unfinishedSearchSql);
		unfinishedTableFlip.setCountSql(unfinishedCountSql);
		unfinishedTableFlip.refreshToFirstPage();
		
		finishedTableFlip.setSearchSql(finishedSearchSql);
		finishedTableFlip.setCountSql(finishedCountSql);
		finishedTableFlip.refreshToFirstPage();
		
		// 第四步：将各营销任务表格的选中值置空
		unfinishedTaskTable.setValue(null);
		finishedTaskTable.setValue(null);
	}

	/**
	 * 当电话振铃时，弹屏
	 * 	用于在系统中直接点击呼叫按钮，导致的弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		// 回显弹屏信息
		outgoingDialForTaskWindow.echoInformations(customerResource);
		outgoingDialForTaskWindow.setAgiChannel(agiChannel);
		outgoingDialForTaskWindow.center();
		// 这种方式会导致，如果坐席开启了‘自动慢拨号’就会导致一次性将两条任务移到‘已完成任务中’
//		this.getApplication().getMainWindow().removeWindow(outgoingDialForTaskWindow);
		Set<Window> childWins = this.getApplication().getMainWindow().getChildWindows();
		if(childWins == null || !childWins.contains(outgoingDialForTaskWindow)) {
			this.getApplication().getMainWindow().addWindow(outgoingDialForTaskWindow);
		}
	}
	
}
