package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.AddProject;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.AdvanceSearchProject;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.AssignByCsr;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.AssignCsr;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.AssignResource;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.AssignTask;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.EditConnectedCommodityWindow;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.EditProject;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.ImportResourceWindow;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.MigrateResource;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.MigrateResourceByCsr;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.ProjectChart;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.RecycleByBatch;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.RecycleByCsr;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.RecycleToBatch;
import com.jiangyifen.ec2.ui.mgr.projectcontrol.TaskDetailWindow;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 项目控制
 * <p>
 * 此页面应该只包含搜索组件、表格组件、表格下的按钮组件
 * </p>
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class ProjectControl extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private ComboBox projectStatus;
	private Button search;
	private Button advanceSearch;

	// 项目表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<MarketingProject> flip;

	// 项目表格按钮组件
	private Button addProject;// 添加项目
//	private Button start;
//	private Button stop;
//	private Button edit;
//	private Button delete;
//	private Button assignCsr;
//	private Button assignResource;
	private Button assignTask;
	private Button assignByCsr;
	private Button recycleByCsr;
	private Button recycleByBatch;
	private Button recycleToBatch;
	private Button migrateResouce;
	private Button migrateResouceByCsr;
	private Button importResouce;
	private Button taskDetail;
	private Button connectedCommodity_bt;	// 编辑关联商品

	// 项目状态信息的图表
	private HorizontalLayout projectChartLayout;
	private ProjectChart projectChart;
	// 项目具体的状态信息
	private Label finishendNumLabel;
	private Label notFinishendNumLabel;
	private Label notDistributeNumLabel;
	private Label csrNumLabel;

	//外围组件
	private VerticalLayout progressOuterLayout;
		
	/**
	 * 右键组件
	 */
	private Action START = new Action("开始");
	private Action STOP = new Action("停止");
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { START, STOP, ADD, EDIT, DELETE };
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private AdvanceSearchProject advanceSearchWindow;
	private EditProject editProjectWindow;
	private AssignCsr assignCsrWindow;
	private AssignResource assignResourceWindow;
	private AssignTask assignTaskWindow;
	private AssignByCsr assignByCsrWindow;
	private RecycleByCsr recycleByCsrWindow;
	private RecycleByBatch recycleByBatchWindow;
	private RecycleToBatch recycleToBatchWindow;
	private MigrateResource migrateResourceWindow;
	private MigrateResourceByCsr migrateResourceByCsrWindow;
	private AddProject addProjectWindow;
	/**
	 * 其他组件
	 */
	// 项目资源数目信息
	private DepartmentService departmentService;
	private MarketingProjectService marketingProjectService;
	private CommonService commonService;
	// 如果当前有选中的项目则会存储当前选中的项目，如果没有选中的项目则会存储null
	private MarketingProject currentSelectProject;
	private Domain domain;
	
	private User loginUser;
	private ImportResourceWindow importResourceWindow;
	private TaskDetailWindow taskDetailWindow;
	private EditConnectedCommodityWindow editConnectedCommodityWindow;

	/**
	 * 构造器
	 */
	public ProjectControl() {
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
		
		addColumn1(table); //添加最后的按钮

		// 添加图表视图Layout
		projectChartLayout = new HorizontalLayout();
		projectChartLayout.setSizeUndefined();
		projectChartLayout.setSizeFull();
		constrantLayout.addComponent(projectChartLayout);

		// 显示项目的状态信息
		constrantLayout.addComponent(buildProjectResourceInfoLayout());
	}

	/**
	 * 将Service进行初始化
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		marketingProjectService = SpringContextHolder
				.getBean("marketingProjectService");
		departmentService=SpringContextHolder.getBean("departmentService");
		commonService = SpringContextHolder.getBean("commonService");
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
		keyWord.setInputPrompt("项目名称");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		// 项目状态
		projectStatus = new ComboBox();
		projectStatus.setInputPrompt("全部");
		projectStatus.addItem(MarketingProjectStatus.NEW);
		projectStatus.addItem(MarketingProjectStatus.RUNNING);
		projectStatus.addItem(MarketingProjectStatus.PAUSE);
		projectStatus.addItem(MarketingProjectStatus.OVER);
		projectStatus.addItem(MarketingProjectStatus.DELETE);
		projectStatus.setWidth("8em");
		searchLayout.addComponent(projectStatus);

		// 搜索按钮
		search = new Button("搜索");
		search.setStyleName("small");
		search.addListener((Button.ClickListener) this);
		searchLayout.addComponent(search);

		// 高级搜索按钮
		advanceSearch = new Button("高级搜索");
		advanceSearch.setStyleName("small");
		advanceSearch.addListener((Button.ClickListener) this);
		searchLayout.addComponent(advanceSearch);

		//新建
		addProject = new Button("添加项目");
		addProject.setStyleName("default");
		addProject.addListener((Button.ClickListener) this);
		searchLayout.addComponent(addProject);
		
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
				} else if("sip".equals(colId)) {
					SipConfig sip =(SipConfig)property.getValue();
					if(sip==null){
						return "";
					}else{
						return sip.getName();
					}
				} else if("queue".equals(colId)) {
					Queue queue =(Queue)property.getValue();
					if(queue==null){
						return "";
					}else{
						return queue.getDescriptionAndName();
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
//		table.addActionHandler(this);
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
		
//		//第一排
//		HorizontalLayout firstLineLayout=new HorizontalLayout();
//		firstLineLayout.setSpacing(true);
//		leftButtonsVerticalLayout.addComponent(firstLineLayout);
		
//		// 开始
//		start = new Button("开始");
//		start.setEnabled(false);
//		start.addListener((Button.ClickListener) this);
//		start.setStyleName(StyleConfig.BUTTON_STYLE);
//		firstLineLayout.addComponent(start);
//
//		// 停止
//		stop = new Button("停止");
//		stop.setEnabled(false);
//		stop.addListener((Button.ClickListener) this);
//		stop.setStyleName(StyleConfig.BUTTON_STYLE);
//		firstLineLayout.addComponent(stop);
//		
//		// 创建编辑按钮
//		edit = new Button("编辑");
//		edit.setEnabled(false);
//		edit.setStyleName(StyleConfig.BUTTON_STYLE);
//		edit.addListener((Button.ClickListener) this);
//		firstLineLayout.addComponent(edit);
//
//		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
//		delete = new Button("删除");
//		delete.setEnabled(false);
//		delete.setStyleName(StyleConfig.BUTTON_STYLE);
//		delete.addListener((Button.ClickListener) this);
//		firstLineLayout.addComponent(delete);
//
//		// 添加CSR
//		assignCsr = new Button("添加/移除CSR");
//		assignCsr.setEnabled(false);
//		assignCsr.setStyleName(StyleConfig.BUTTON_STYLE);
//		assignCsr.addListener((Button.ClickListener) this);
//		firstLineLayout.addComponent(assignCsr);
//
//		// 添加资源
//		assignResource = new Button("添加/移除资源");
//		assignResource.setEnabled(false);
//		assignResource.setStyleName(StyleConfig.BUTTON_STYLE);
//		assignResource.addListener((Button.ClickListener) this);
//		firstLineLayout.addComponent(assignResource);


		//第二排
		HorizontalLayout secondLineLayout=new HorizontalLayout();
		secondLineLayout.setSpacing(true);
		leftButtonsVerticalLayout.addComponent(secondLineLayout);

		// 指派任务
		assignTask = new Button("指派任务");
		assignTask.setEnabled(false);
		assignTask.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(assignTask);

		// 按批次回收
		recycleByBatch = new Button("回收任务");
		recycleByBatch.setEnabled(false);
		recycleByBatch.setStyleName(StyleConfig.BUTTON_STYLE);
		recycleByBatch.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(recycleByBatch);

		// 按CSR回收
		assignByCsr = new Button("按CSR指派任务");
		assignByCsr.setEnabled(false);
		assignByCsr.setStyleName(StyleConfig.BUTTON_STYLE);
		assignByCsr.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(assignByCsr);

		// 按CSR回收
		recycleByCsr = new Button("按CSR回收任务");
		recycleByCsr.setEnabled(false);
		recycleByCsr.setStyleName(StyleConfig.BUTTON_STYLE);
		recycleByCsr.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(recycleByCsr);

		//切换未打资源到其他项目按钮
		migrateResouce = new Button("按项目转移任务");
		migrateResouce.setEnabled(false);
		migrateResouce.setStyleName(StyleConfig.BUTTON_STYLE);
		migrateResouce.addListener((Button.ClickListener) this);
		secondLineLayout.addComponent(migrateResouce);


		//第三排
		HorizontalLayout thirdLineLayout=new HorizontalLayout();
		thirdLineLayout.setSpacing(true);
		leftButtonsVerticalLayout.addComponent(thirdLineLayout);
		
		// 回收成为一个新的批次
		recycleToBatch = new Button("回收到批次");
		recycleToBatch.setDescription("回收指定批次中的所有未拨打的资源");
		recycleToBatch.setEnabled(false);
		recycleToBatch.setStyleName(StyleConfig.BUTTON_STYLE);
		recycleToBatch.addListener((Button.ClickListener) this);
		thirdLineLayout.addComponent(recycleToBatch);

		//资源导入按钮
		importResouce = new Button("导入/追加资源");
		importResouce.setEnabled(false);
		importResouce.setStyleName(StyleConfig.BUTTON_STYLE);
		importResouce.addListener((Button.ClickListener) this);
		thirdLineLayout.addComponent(importResouce);

		//任务详情按钮
		taskDetail = new Button("任务详情");
		taskDetail.setEnabled(false);
		taskDetail.setStyleName(StyleConfig.BUTTON_STYLE);
		taskDetail.addListener((Button.ClickListener) this);
		thirdLineLayout.addComponent(taskDetail);
		
		//任务详情按钮
		connectedCommodity_bt = new Button("编辑关联商品");
		connectedCommodity_bt.setEnabled(false);
		connectedCommodity_bt.setStyleName(StyleConfig.BUTTON_STYLE);
		connectedCommodity_bt.addListener((Button.ClickListener) this);
		thirdLineLayout.addComponent(connectedCommodity_bt);
		
		//切换未打资源到其他项目按钮
		migrateResouceByCsr = new Button("按CSR转移任务");
		migrateResouceByCsr.setStyleName(StyleConfig.BUTTON_STYLE);
		migrateResouceByCsr.addListener((Button.ClickListener) this);
		thirdLineLayout.addComponent(migrateResouceByCsr);
		
//		//第四排
//		HorizontalLayout forthLineLayout=new HorizontalLayout();
//		leftButtonsVerticalLayout.addComponent(forthLineLayout);
//		//第五排
//		HorizontalLayout fifthLineLayout=new HorizontalLayout();
//		leftButtonsVerticalLayout.addComponent(fifthLineLayout);
		
		progressOuterLayout=new VerticalLayout();
		tableButtonsLeft.addComponent(progressOuterLayout);
		
		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<MarketingProject>(MarketingProject.class, marketingProjectService, table, sqlSelect, sqlCount, null);
		table.setPageLength(10);
		flip.setPageLength(10, false);

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "projectName", "marketingProjectStatus", "marketingProjectType", "sip","queue","createDate" };
		String[] columnHeaders = new String[] { "ID", "项目名", "项目状态", "项目类型", "项目外线","项目队列","创建时间" };
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置表格的样式
		this.setStyleGeneratorForTable(table);
		// 生成备注列
		this.addColumn(table);

		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.TOP_RIGHT);
		return tableButtons;
	}

	/**
	 * 创建项目状态输出区域
	 */
	private GridLayout buildProjectResourceInfoLayout() {
		GridLayout gridLayout = new GridLayout(2, 4);

		gridLayout.addComponent(new Label("已完成"), 0, 0);
		gridLayout.addComponent(new Label("未完成"), 0, 1);
		gridLayout.addComponent(new Label("未分配"), 0, 2);
		gridLayout.addComponent(new Label("CSR数量"), 0, 3);

		finishendNumLabel = new Label("");
		notFinishendNumLabel = new Label("");
		notDistributeNumLabel = new Label("");
		csrNumLabel = new Label("");

		gridLayout.addComponent(finishendNumLabel, 1, 0);
		gridLayout.addComponent(notFinishendNumLabel, 1, 1);
		gridLayout.addComponent(notDistributeNumLabel, 1, 2);
		gridLayout.addComponent(csrNumLabel, 1, 3);

		return gridLayout;
	}

	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("备注", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
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
	 * 由 buildTableButtons 调用，设置生成表格行的样式
	 * 
	 * @param table
	 */
	private void setStyleGeneratorForTable(final Table table) {
		// style generator
		table.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				String[] styles = { StyleConfig.PROJECT_CONTROL_NEW_COLOR,
						StyleConfig.PROJECT_CONTROL_RUNNING_COLOR,
						StyleConfig.PROJECT_CONTROL_STOP_COLOR,StyleConfig.PROJECT_CONTROL_STOP_COLOR,StyleConfig.PROJECT_CONTROL_NEW_COLOR};
				if (propertyId == null) {
					MarketingProjectStatus status = (MarketingProjectStatus) table
							.getContainerProperty(itemId,
									"marketingProjectStatus").getValue();
					return styles[status.getIndex()];
				} else {
					return null;
				}
			}
		});
	}
	
//	SelectSql: >>>>select e from MarketingProject e where ( 
//	e.marketingProjectStatus=com.jiangyifen.ec2.bean.MarketingProjectStatus.NEW 
//	or e.marketingProjectStatus=com.jiangyifen.ec2.bean.MarketingProjectStatus.RUNNING
//	or e.marketingProjectStatus=com.jiangyifen.ec2.bean.MarketingProjectStatus.PAUSE
//	or e.marketingProjectStatus=com.jiangyifen.ec2.bean.MarketingProjectStatus.OVER 
//	or e.creater.department.id=1 or e.creater.department.id=12) 
//	and e.domain.id=1 order by e.id desc<<<< 
	
	/**
	 * 添加组件
	 * @param table
	 */
	private void addColumn1(Table table) {
		table.addGeneratedColumn("操作", new Table.ColumnGenerator() {
	    	public Component generateCell(Table source, Object itemId,Object columnId) {
	    		HorizontalLayout buttonsLayout=new HorizontalLayout();
	    			    		
	    		//开始
	    		Button start=new Button("开始");
	    		start.setStyleName(BaseTheme.BUTTON_LINK);
	    		start.setData(itemId);
	    		start.addListener((Button.ClickListener)ProjectControl.this);
	    		buttonsLayout.addComponent(start);
	    		
	    		//分隔条
	    		buttonsLayout.addComponent(new Label("&nbsp;/&nbsp;",Label.CONTENT_XHTML));
	    		
	    		//停止
	    		Button stop=new Button("停止");
	    		stop.setStyleName(BaseTheme.BUTTON_LINK);
	    		stop.setData(itemId);
	    		stop.addListener((Button.ClickListener)ProjectControl.this);
	    		buttonsLayout.addComponent(stop);
	    		
	    		//分隔条
	    		buttonsLayout.addComponent(new Label("&nbsp;/&nbsp;",Label.CONTENT_XHTML));
	    		
	    		//编辑
	    		Button edit=new Button("编辑");
	    		edit.setStyleName(BaseTheme.BUTTON_LINK);
	    		edit.setData(itemId);
	    		edit.addListener((Button.ClickListener)ProjectControl.this);
	    		buttonsLayout.addComponent(edit);
	    		
	    		//分隔条
	    		buttonsLayout.addComponent(new Label("&nbsp;/&nbsp;",Label.CONTENT_XHTML));
	    		
	    		//删除
	    		Button delete=new Button("删除");
        		delete.setStyleName(BaseTheme.BUTTON_LINK);
        		delete.setData(itemId);
        		delete.addListener((Button.ClickListener)ProjectControl.this);
        		buttonsLayout.addComponent(delete);
        		
        		//分隔条
        		buttonsLayout.addComponent(new Label("&nbsp;/&nbsp;",Label.CONTENT_XHTML));
        		
        		//CSR管理
        		Button csrManage=new Button("CSR管理");
        		csrManage.setStyleName(BaseTheme.BUTTON_LINK);
        		csrManage.setData(itemId);
        		csrManage.addListener((Button.ClickListener)ProjectControl.this);
        		buttonsLayout.addComponent(csrManage);
        		
        		//分隔条
        		buttonsLayout.addComponent(new Label("&nbsp;/&nbsp;",Label.CONTENT_XHTML));
        		
        		//资源管理
        		Button resourceManage=new Button("资源管理");
        		resourceManage.setStyleName(BaseTheme.BUTTON_LINK);
        		resourceManage.setData(itemId);
        		resourceManage.addListener((Button.ClickListener)ProjectControl.this);
        		buttonsLayout.addComponent(resourceManage);
	
        		return buttonsLayout;
	    	}
	    });
	}


	/**
	 * 由buttonClick 调用，执行搜索功能
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("MarketingProject");
		// 关键字过滤,项目名
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like projectName = new SqlGenerator.Like("projectName",
				keyWordStr);
		sqlGenerator.addAndCondition(projectName);
		
		// 项目状态
		MarketingProjectStatus projectStatu = (MarketingProjectStatus) projectStatus
				.getValue();
		if (projectStatu != null) {
			String statuStr = projectStatu.getClass().getName() + ".";
			if (projectStatu.getIndex() == 0) {
				statuStr += "NEW";
			} else if (projectStatu.getIndex() == 1) {
				statuStr += "RUNNING";
			}else if (projectStatu.getIndex() == 2) {
				statuStr += "PAUSE";
			}  else if (projectStatu.getIndex() == 3) {
				statuStr += "OVER";
			}  else if (projectStatu.getIndex() == 4) {
				statuStr += "DELETE";
			}
			
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"marketingProjectStatus", statuStr, false);
			sqlGenerator.addAndCondition(statu);
		}
		
		if (projectStatu != null&&projectStatu.getIndex() == 4) {
			//如果不为null，并且搜索的为删除，则不添加!=删除条件
		}else{
			//增添删除不可以等于Delete
			String conditionStr1=MarketingProjectStatus.class.getName() + ".DELETE";
			SqlGenerator.Equal condition1 = new SqlGenerator.Equal(
					"marketingProjectStatus !", conditionStr1, false);
			sqlGenerator.addAndCondition(condition1);
		}
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					Long deptId = dept.getId();
					if(!allGovernedDeptIds.contains(deptId)) {
						allGovernedDeptIds.add(deptId);
					}
				}
			}
		}
		// jrh 只能查看本部门及其子部门创建的项目
		if(allGovernedDeptIds.isEmpty()) {
			SqlGenerator.Equal orEqual = new SqlGenerator.Equal("creater.department.id", "0", false);
			sqlGenerator.addOrCondition(orEqual);
		} else {
			for(Long deptId : allGovernedDeptIds) {
				SqlGenerator.Equal orEqual = new SqlGenerator.Equal("creater.department.id", deptId.toString(), false);
				sqlGenerator.addOrCondition(orEqual);
			}
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
		//chenhb comment ，允许假删除
//		CommonService commonService = SpringContextHolder
//				.getBean("commonService");
//		// 如果项目被Task、投诉记录、客服记录引用就不允许删除 Note:隐含域
//		String sqlComplaintRecord = "select count(*) from ec2_customer_complaint_record where marketingproject_id="
//				+ currentSelectProject.getId();
//		String sqlServiceRecord = "select count(*) from ec2_customer_service_record where marketingproject_id="
//				+ currentSelectProject.getId();
//		String sqlTask = "select count(*) from ec2_marketing_project_task where marketingproject_id="
//				+ currentSelectProject.getId();
//		// 投诉记录数量
//		Long count = (Long) commonService.excuteNativeSql(sqlComplaintRecord,
//				ExecuteType.SINGLE_RESULT);
//		if (count > 0) {
//			this.getApplication().getMainWindow().showNotification("项目含有投诉记录,不能被删除!");
//			return;
//		}
//		// 客服记录数量
//		count = (Long) commonService.excuteNativeSql(sqlServiceRecord,
//				ExecuteType.SINGLE_RESULT);
//		if (count > 0) {
//			this.getApplication().getMainWindow().showNotification("项目含有客服记录,不能被删除!");
//			return;
//		}
//		// 任务引用数量
//		count = (Long) commonService.excuteNativeSql(sqlTask,
//				ExecuteType.SINGLE_RESULT);
//		if (count > 0) {
//			this.getApplication().getMainWindow().showNotification("项目已经添加客户资源,不能被删除!");
//			return;
//		}
//		
//		//更改分机和外线的对应关系
//		for(String exten:ShareData.extenToProject.keySet()){
//			Long marketingProjectId =ShareData.extenToProject.get(exten);
//			if(currentSelectProject.getId()==marketingProjectId){
//				this.getApplication().getMainWindow().showNotification("项目正在被分机"+exten+"使用,不能被删除!");
//				return;
//			}
//		}
		
		// 在confirmDelete方法中删除与批次的关联、与用户的关联
		Label label = new Label("您确定要删除项目<b>"
				+ currentSelectProject.getProjectName()+"(删除项目不影响自动外呼停止)" + "</b>?",
				Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmDelete");
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}

	/**
	 * 由buttonClick调用显示高级搜索窗口，由于用户点击的高级搜索
	 */
	private void showAdvanceSearchWindow() {
		if (advanceSearchWindow == null) {
			advanceSearchWindow = new AdvanceSearchProject(this);
		}
		this.getApplication().getMainWindow().removeWindow(advanceSearchWindow);
		this.getApplication().getMainWindow().addWindow(advanceSearchWindow);
	}

	/**
	 * 显示添加CSR的窗口
	 */
	private void showAssignCsrWindow() {
		if (assignCsrWindow == null) {
			try {
				assignCsrWindow = new AssignCsr(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(assignCsrWindow);
		this.getApplication().getMainWindow().addWindow(assignCsrWindow);
	}

	/**
	 * 显示添加资源的窗口
	 */
	private void showAssignResourceWindow() {
		// 出处判断资源是不是存在
	//为了支持多个进度条组件每次都是重新创建组件
		if (assignResourceWindow != null) {
			this.getApplication().getMainWindow().removeWindow(assignResourceWindow);
		}
		
		try {
			assignResourceWindow = new AssignResource(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			return;
		}
	
		this.getApplication().getMainWindow().addWindow(assignResourceWindow);
	}

	/**
	 * 添加项目窗口
	 */
	private void showAddProjectWindow() {
		if (addProjectWindow == null) {
			addProjectWindow = new AddProject(this);
		}
		this.getApplication().getMainWindow().removeWindow(addProjectWindow);
		this.getApplication().getMainWindow().addWindow(addProjectWindow);
	}

	/**
	 * buttonClick，弹出指派任务窗口
	 */
	private void showAssignTaskWindow() {
		if (assignTaskWindow == null) {
			try {
				assignTaskWindow = new AssignTask(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(assignTaskWindow);
		this.getApplication().getMainWindow().addWindow(assignTaskWindow);
	}

	/**
	 * 由buttonClick调用，显示编辑项目窗口
	 */
	private void showEditProjectWindow() {
		if (editProjectWindow == null) {
			editProjectWindow = new EditProject(this);
		}
		this.getApplication().getMainWindow().removeWindow(editProjectWindow);
		this.getApplication().getMainWindow().addWindow(editProjectWindow);
	}

	/**
	 * 由buttonClick调用 显示按照CSR指派任务的弹出窗口
	 */
	private void showAssignByCsrWindow() {
		if (assignByCsrWindow == null) {
			try {
				assignByCsrWindow = new AssignByCsr(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(assignByCsrWindow);
		this.getApplication().getMainWindow().addWindow(assignByCsrWindow);
	}

	/**
	 * 由buttonClick调用 显示回收CSR的弹出窗口
	 */
	private void showRecycleByCsrWindow() {
		if (recycleByCsrWindow == null) {
			try {
				recycleByCsrWindow = new RecycleByCsr(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(recycleByCsrWindow);
		this.getApplication().getMainWindow().addWindow(recycleByCsrWindow);
	}

	/**
	 * 由buttonClick调用 显示转移项目资源窗口
	 */
	private void showMigrateResourceWindow() {
		if (migrateResourceWindow == null) {
			try {
				migrateResourceWindow = new MigrateResource(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(migrateResourceWindow);
		this.getApplication().getMainWindow().addWindow(migrateResourceWindow);
	}
	
	/**
	 * 由buttonClick调用 显示转移项目资源窗口
	 */
	private void showMigrateResourceByCsrWindow() {
		if (migrateResourceByCsrWindow == null) {
			try {
				migrateResourceByCsrWindow = new MigrateResourceByCsr();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(migrateResourceByCsrWindow);
		this.getApplication().getMainWindow().addWindow(migrateResourceByCsrWindow);
	}

	/**
	 * 由buttonClick调用 显示转移项目资源窗口
	 */
	private void showImportResourceWindow() {
		try {
			importResourceWindow = new ImportResourceWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("弹出窗口失败");
		}
		this.getApplication().getMainWindow().removeWindow(importResourceWindow);
		this.getApplication().getMainWindow().addWindow(importResourceWindow);
	}

	/**
	 * 由buttonClick调用 显示转移项目资源窗口
	 */
	private void showTaskDetailWindow() {
		try {
			taskDetailWindow = new TaskDetailWindow(this);
			this.getApplication().getMainWindow().removeWindow(taskDetailWindow);
			this.getApplication().getMainWindow().addWindow(taskDetailWindow);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("弹出窗口失败");
		}
	}
	
	/**
	 * 由buttonClick调用 显示转移项目资源窗口
	 */
	private void showEditConnectedCommodityWindow() {
		try {
			editConnectedCommodityWindow = new EditConnectedCommodityWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("弹出窗口失败");
		}
		editConnectedCommodityWindow.echoWindowInfo(currentSelectProject);
		this.getApplication().getMainWindow().removeWindow(editConnectedCommodityWindow);
		this.getApplication().getMainWindow().addWindow(editConnectedCommodityWindow);
	}

	/**
	 * 由buttonClick调用 显示按批次回收的弹出窗口
	 */
	private void showRecycleByBatchWindow() {
		if (recycleByBatchWindow == null) {
			try {
				recycleByBatchWindow = new RecycleByBatch(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(recycleByBatchWindow);
		this.getApplication().getMainWindow().addWindow(recycleByBatchWindow);
	}
	
	/**
	 * 由buttonClick调用 显示回收到批次窗口
	 */
	private void showRecycleToBatchWindow() {
		Set<CustomerResourceBatch> batches = currentSelectProject.getBatches();
		if(batches.size()<1){
			NotificationUtil.showWarningNotification(this,"项目中没有对应的批次");
			return;
		}
		
		if (recycleToBatchWindow == null) {
			try {
				recycleToBatchWindow = new RecycleToBatch(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(recycleToBatchWindow);
		this.getApplication().getMainWindow().addWindow(recycleToBatchWindow);
	}

	/**
	 * 进度组件
	 * @return
	 */
	public VerticalLayout getProgressLayout(){
		return progressOuterLayout;
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
	 * 由valueChange和构造器调用，更新项目资源数目信息,添加资源,分配任务(confirmThrough)
	 */
	public void updateProjectResourceInfo() {
		if (table == null || table.getValue() == null) {
			finishendNumLabel.setValue("");
			notFinishendNumLabel.setValue("");
			notDistributeNumLabel.setValue("");
			csrNumLabel.setValue("");

			projectChartLayout.removeAllComponents();// 移除图表输出中的图表
			return;
		}

		//项目  user_id
		String nativeSql="select isfinished,user_id is not null as ishaveuid,count(*) from ec2_marketing_project_task where domain_id="+
				domain.getId()+" and isuseable is not false and marketingproject_id="+currentSelectProject.getId()+" group by isfinished,ishaveuid";
		
		//查询数据并设置数据源
		Long notDistributedNum=0L;
		Long finishedNum = 0L;
		Long notFinishedNum = 0L;
		
		@SuppressWarnings("unchecked")
		List<Object[]> nums = (List<Object[]>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
//		if(nums.size()>3) throw new RuntimeException("用户Id为null，应该标记为未完成状态！");
		for(int i=0;i<nums.size();i++){
			Object[] objects=nums.get(i);
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

		// 如果还没有创建图表则创建
		if (projectChart == null) {
			projectChart = new ProjectChart();
		}
		// 将图表显示出来
		projectChart.updateProjectChart(finishedNum, notFinishedNum, notDistributedNum);
		projectChartLayout.removeAllComponents();
		projectChartLayout.addComponent(projectChart);

		// 直接用marketingProject.getUsers().size()可能比较好性能，而且数据表又是多对多关系，除非用原生SQL从数据库的连接表中查询
		int csrNum = currentSelectProject.getUsers().size();
		finishendNumLabel.setValue(finishedNum);
		notFinishendNumLabel.setValue(notFinishedNum);
		notDistributeNumLabel.setValue(notDistributedNum);
		csrNumLabel.setValue(csrNum);
	}

	/**
	 * 由弹出窗口回调确认删除项目,此时marketingProjectService不应该为null
	 */
	public void confirmDelete(Boolean isConfirmed) {
		try {
			if (isConfirmed == true) {
				CommonService commonService = SpringContextHolder
						.getBean("commonService");

//				// 删除批次的关联，用户的关联 // Note:隐含域概念
//				String sqlBatch = "delete from ec2_markering_project_ec2_customer_resource_batch where marketingproject_id="
//						+ currentSelectProject.getId();
//				String sqlUser = "delete from ec2_markering_project_ec2_user where marketingproject_id="
//						+ currentSelectProject.getId();
//				// jrh 删除项目与问卷的中间表
//				String project2QuestionnarieSql = "delete from ec2_marketing_project_questionnaire_link where project_id = "
//						+ currentSelectProject.getId();;
//				String sqlProject = "delete from ec2_markering_project where id="
//						+ currentSelectProject.getId();
//				//清空ec2_sip_conf中与项目的对应关系
//				String sqlUpdateSipConfig = "update ec2_sip_conf set marketingproject_id=null where marketingproject_id="+ currentSelectProject.getId();
//
//				commonService.excuteNativeSql(sqlUpdateSipConfig, ExecuteType.UPDATE);
//				commonService.excuteNativeSql(sqlBatch, ExecuteType.UPDATE);
//				commonService.excuteNativeSql(sqlUser, ExecuteType.UPDATE);
//				commonService.excuteNativeSql(project2QuestionnarieSql, ExecuteType.UPDATE);
//				commonService.excuteNativeSql(sqlProject, ExecuteType.UPDATE);
				
				//此状态不为运行，所以会将项目停止，对于自动外呼不影响
				currentSelectProject.setMarketingProjectStatus(MarketingProjectStatus.DELETE);
				currentSelectProject=(MarketingProject)commonService.update(currentSelectProject);
				// 更新Table数据，并使Table处于未被选中状态
				this.updateTable(false);
				table.setValue(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("项目含有关联资源，不允许删除！");
		}
	}

	/**
	 * 返回FlipOver的一个引用
	 * 
	 * @return
	 */
	public FlipOverTableComponent<MarketingProject> getFlip() {
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
	 * 取得当前选中的项目
	 * 
	 * @return
	 */
	public MarketingProject getCurrentSelect() {
		return currentSelectProject;
	}

	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[] {ADD};
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (START == action) {
//			start.click();
		} else if (STOP == action) {
//			stop.click();
		} else if (ADD == action) {
			addProject.click();
		} else if (EDIT == action) {
//			edit.click();
		} else if (DELETE == action) {
//			delete.click();
		}
	}

	/**
	 * 按钮单击监听器
	 * <p>
	 * 搜索、高级搜索、新建项目、开始、停止、添加CSR/添加资源 、指派任务
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
				this.getApplication().getMainWindow().showNotification("搜索出现错误");
			}
		} else if (event.getButton() == advanceSearch) {
			// 高级搜索
			keyWord.setValue("");
			projectStatus.setValue(null);
			showAdvanceSearchWindow();
		} else if (event.getButton().getCaption().equals("开始")) {
			currentSelectProject =(MarketingProject)event.getButton().getData();
			table.select(currentSelectProject);
			try {
				currentSelectProject
						.setMarketingProjectStatus(MarketingProjectStatus.RUNNING);
				currentSelectProject = marketingProjectService
						.update(currentSelectProject);
				//如果项目存在对应的外线，则存储项目和外线的对应关系、外线与项目的对应关系
				SipConfig sipConfig=currentSelectProject.getSip();
				if(sipConfig!=null){
					ShareData.projectToOutline.put(currentSelectProject.getId(), sipConfig.getName());
					ShareData.outlineToProject.put(sipConfig.getName(),currentSelectProject.getId());
				}
				//如果项目存在对应的队列，则存储项目和队列的对应关系
				Queue queue=currentSelectProject.getQueue();
				if(queue!=null){
					ShareData.projectToQueue.put(currentSelectProject.getId(), queue.getName());
				}
				//添加分机和外线的对应关系
				for(String exten:ShareData.extenToProject.keySet()){
					Long marketingProjectId =ShareData.extenToProject.get(exten);
					if(currentSelectProject.getId() != null && currentSelectProject.getId().equals(marketingProjectId)){
						if( currentSelectProject.getSip()==null){
							ShareData.extenToDynamicOutline.remove(exten);
						}else{
							ShareData.extenToDynamicOutline.put(exten, currentSelectProject.getSip().getName());
						}
					}
				}
				OperationLogUtil.simpleLog(loginUser, "项目控制-项目开始："+currentSelectProject.getProjectName()+" Id:"+currentSelectProject.getId());
				this.updateTable(false);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("项目开始异常！");
			}
		} else if (event.getButton().getCaption().equals("停止")) {
			currentSelectProject =(MarketingProject)event.getButton().getData();
			table.select(currentSelectProject);
			// 项目结束
			try {
				currentSelectProject
						.setMarketingProjectStatus(MarketingProjectStatus.OVER);
				currentSelectProject = marketingProjectService
						.update(currentSelectProject);
				//如果项目存在对应的外线，则移除项目和外线的对应关系、外线与项目的对应关系
				SipConfig sipConfig=currentSelectProject.getSip();
				if(sipConfig!=null){
					ShareData.projectToOutline.remove(currentSelectProject.getId());
					ShareData.outlineToProject.remove(sipConfig.getName());
				}
				//如果项目存在对应的队列，则移除项目和队列的对应关系
				Queue queue=currentSelectProject.getQueue();
				if(queue!=null){
					ShareData.projectToQueue.remove(currentSelectProject.getId());
				}
				//移除分机和外线的对应关系
				for(String exten:ShareData.extenToProject.keySet()){
					Long marketingProjectId =ShareData.extenToProject.get(exten);
					if(currentSelectProject.getId() != null && currentSelectProject.getId().equals(marketingProjectId)){
						ShareData.extenToDynamicOutline.remove(exten);
					}
				}
				this.updateTable(false);
				OperationLogUtil.simpleLog(loginUser, "项目控制-项目停止："+currentSelectProject.getProjectName()+" Id:"+currentSelectProject.getId());
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("项目结束异常！");
			}
		} else if (event.getButton().getCaption().equals("删除")) {
			currentSelectProject =(MarketingProject)event.getButton().getData();
			table.select(currentSelectProject);
			
			// 项目删除
			try {
				executeDelete();
				OperationLogUtil.simpleLog(loginUser, "项目控制-项目删除："+currentSelectProject.getProjectName()+" Id:"+currentSelectProject.getId());
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("删除出现异常！");
			}
		} else if (event.getButton().getCaption().equals("编辑")) {
			currentSelectProject =(MarketingProject)event.getButton().getData();
			table.select(currentSelectProject);
			
			showEditProjectWindow();
		} else if (event.getButton() == addProject) {
			showAddProjectWindow();
		} else if (event.getButton().getCaption().equals("CSR管理")) {
			currentSelectProject =(MarketingProject)event.getButton().getData();
			table.select(currentSelectProject);

			// /查看项目是不是有可用的CSR
			String nativeSql = "select count(*) from ec2_user where domain_id="
					+ domain.getId();
			Long csrNum = (Long) commonService.excuteNativeSql(nativeSql,
					ExecuteType.SINGLE_RESULT);
			if (csrNum <= 0) {
				this.getApplication().getMainWindow().showNotification("目前还没有可用的CSR");
				return;
			}
			showAssignCsrWindow();
		} else if (event.getButton().getCaption().equals("资源管理")) {
			currentSelectProject =(MarketingProject)event.getButton().getData();
			table.select(currentSelectProject);
			showAssignResourceWindow();
		} else if (event.getButton() == recycleByBatch) {
			Set<CustomerResourceBatch> batchesSet = currentSelectProject.getBatches();
			if (batchesSet.size() > 0) {
				showRecycleByBatchWindow();
			} else {
				this.getApplication().getMainWindow().showNotification(
						"项目" + currentSelectProject.getProjectName()
								+ "没有添加可回收的批次资源!");
			}
		} else if (event.getButton() == assignByCsr) {
			Set<CustomerResourceBatch> batchesSet = currentSelectProject.getBatches();
			if (batchesSet.size() > 0) {
				showAssignByCsrWindow();
			} else {
				this.getApplication().getMainWindow().showNotification(
						"项目" + currentSelectProject.getProjectName()
								+ "没有添加任何资源，请先添加资源再指派!");
			}
		} else if (event.getButton() == recycleByCsr) {
			showRecycleByCsrWindow();
		} else if (event.getButton() == assignTask) {
			Set<CustomerResourceBatch> batchesSet = currentSelectProject.getBatches();
			if (batchesSet.size() > 0) {
				showAssignTaskWindow();
			} else {
				this.getApplication().getMainWindow().showNotification(
						"项目" + currentSelectProject.getProjectName()
								+ "没有添加任何资源，请先添加资源再指派!");
			}
		}else if (event.getButton() == migrateResouce) {
			showMigrateResourceWindow();
		}else if (event.getButton() == migrateResouceByCsr) {
			showMigrateResourceByCsrWindow();
		}else if (event.getButton() == recycleToBatch) {
			showRecycleToBatchWindow();
		}else if (event.getButton() == importResouce) {
			showImportResourceWindow();
		}else if (event.getButton() == taskDetail) {
			showTaskDetailWindow();
		}else if (event.getButton() == connectedCommodity_bt) {
			showEditConnectedCommodityWindow();
		}
	}

	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
//			start.setEnabled(true);
//			stop.setEnabled(true);
//			assignCsr.setEnabled(true);
//			assignResource.setEnabled(true);
//			delete.setEnabled(true);
//			edit.setEnabled(true);
			recycleByBatch.setEnabled(true);
			recycleToBatch.setEnabled(true);
			assignByCsr.setEnabled(true);
			recycleByCsr.setEnabled(true);
			assignTask.setEnabled(true);
			migrateResouce.setEnabled(true);
			importResouce.setEnabled(true);
			taskDetail.setEnabled(true);
			connectedCommodity_bt.setEnabled(true);
//			migrateResouceByCsr.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
//			start.setEnabled(false);
//			stop.setEnabled(false);
//			assignCsr.setEnabled(false);
//			assignResource.setEnabled(false);
//			delete.setEnabled(false);
//			edit.setEnabled(false);
			recycleByBatch.setEnabled(false);
			recycleToBatch.setEnabled(false);
			assignByCsr.setEnabled(false);
			recycleByCsr.setEnabled(false);
			assignTask.setEnabled(false);
			migrateResouce.setEnabled(false);
			importResouce.setEnabled(false);
			taskDetail.setEnabled(false);
			connectedCommodity_bt.setEnabled(false);
//			migrateResouceByCsr.setEnabled(false);
		}
		// 维护表格中当前选中的项目
		currentSelectProject = (MarketingProject) table.getValue();
		// 改变状态信息（项目数目状态）
		updateProjectResourceInfo();
	}

	
}
