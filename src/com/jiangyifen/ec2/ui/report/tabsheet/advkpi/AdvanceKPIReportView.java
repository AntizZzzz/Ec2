package com.jiangyifen.ec2.ui.report.tabsheet.advkpi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.report.tabsheet.advkpi.pojo.KpiAdvanceVo;
import com.jiangyifen.ec2.ui.report.tabsheet.advkpi.pojo.KpiTableColVo;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

import jxl.Workbook;
import jxl.biff.DisplayFormat;
import jxl.format.Colour;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @Description 描述：中道自定义开发 【高级的KPI报表】
 *
 * @author  JRH
 * @date    2014年5月7日 上午9:48:38
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class AdvanceKPIReportView extends VerticalLayout implements ClickListener, ValueChangeListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final int SPECIFY_OUT_BG_LOW_SEC = 30; 			// 呼出小于xx秒接通次数	30
	public static final int SPECIFY_OUT_BG_LOW_SEC1 = 45;			// 呼出小于xx秒接通次数	45
	public static final int SPECIFY_OUT_BG_LOW_SEC2 = 50; 			// 呼出小于xx秒接通次数	50
	public static final int SPECIFY_OUT_BG_LARGE_SEC1 = 45;			// 呼出大于于xx秒接通次数	45
	public static final int SPECIFY_OUT_BG_LARGE_SEC3 = 120;		// 呼出大于于xx秒接通次数	120
	public static final int SPECIFY_OUT_BG_LARGE_SEC4 = 240;		// 呼出大于于xx秒接通次数	240
	public static final int SPECIFY_OUT_BG_LARGE_SEC5 = 600;		// 呼出大于于xx秒接通次数	600
	public static final int SPECIFY_OUT_BG_LARGE_SEC6 = 1200;		// 呼出大于于xx秒接通次数	1200
	public static final int SPECIFY_OUT_BG_LARGE_SEC7 = 1800;		// 呼出大于于xx秒接通次数	1800
	public static final int SPECIFY_OUT_BG_LARGE_SEC = 300; 		// 呼出大于yy秒接通次数	300
	public static final int SPECIFY_OUT_BG_LARGE_SEC2 = 500; 		// 呼出大于yy秒接通次数	500
	
	public static final int SPECIFY_IN_BG_LOW_SEC = 5; 				// 呼入小于zz秒接通次数	5
	public static final int SPECIFY_IN_BG_LOW_SEC2 = 10; 			// 呼入小于zz秒接通次数	10
	public static final int SPECIFY_IN_BG_LOW_SEC3 = 45; 			// 呼入小于zz秒接通次数	45
	public static final int SPECIFY_IN_BG_LARGE_SEC1 = 45; 			// 呼入大于ww秒接通次数	45
	public static final int SPECIFY_IN_BG_LARGE_SEC3 = 120; 		// 呼入大于ww秒接通次数	120
	public static final int SPECIFY_IN_BG_LARGE_SEC4 = 240; 		// 呼入大于ww秒接通次数	240
	public static final int SPECIFY_IN_BG_LARGE_SEC5 = 600; 		// 呼入大于ww秒接通次数	600
	public static final int SPECIFY_IN_BG_LARGE_SEC6 = 1200; 		// 呼入大于ww秒接通次数	1200
	public static final int SPECIFY_IN_BG_LARGE_SEC7 = 1800; 		// 呼入大于ww秒接通次数	1800
	public static final int SPECIFY_IN_BG_LARGE_SEC = 300; 			// 呼入大于ww秒接通次数	300
	public static final int SPECIFY_IN_BG_LARGE_SEC2 = 500; 		// 呼入大于ww秒接通次数	500
	
	public static final int SPECIFY_IN_BG_LEVEL_SEC = 5; 			// 服务水平内接通数		5
	public static final int SPECIFY_IN_BG_LEVEL_SEC2 = 10; 			// 服务水平内接通数		10

	// 查看记录的统计方式
	public static final String VIEW_TYPE_BY_MON = "by_mon";
	public static final String VIEW_TYPE_BY_DAY = "by_day";
	public static final String VIEW_TYPE_BY_HOUR = "by_hour";
	private final String VIEW_TYPE_DEFAULT = "by_day";
	
	private final SimpleDateFormat SDF_MON = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final NumberFormat PERCENT_FMT = NumberFormat.getPercentInstance();	// 百分比格式化工具

	private final DisplayFormat JXL_PERCENT_FMT = NumberFormats.PERCENT_FLOAT;
	private final DisplayFormat JXL_NUMBER_FMT = NumberFormats.INTEGER; // new jxl.write.NumberFormat("#");	// 百分比格式化工具
	
	// 置忙原因
	private final String PAUSE_REASON_LEAVE = "离开";
	private final String PAUSE_REASON_MEETTING = "会议";
	private final String PAUSE_REASON_BUSY = "忙碌";
	private final String PAUSE_REASON_REST = "休息";
	private final String PAUSE_REASON_DINE = "就餐";
	private final String PAUSE_REASON_TRAIN = "培训";

	private GridLayout gridLayout;						// 面板中的布局管理器
	private ComboBox timeScope_cb;						// “日期范围”选择框
	private PopupDateField startTime_pdf;				// “开始时间”选择框
	private PopupDateField finishTime_pdf;				// “截止时间”选择框
	private ComboBox deptSelector;	 					// 部门选择框
	private ComboBox statisticType_cb;					// "统计方式"按天、时；按天；按月
	private PopupView visibleCol_pv;					// 显示列选择组件
	private EditKpiAdvColumnView editKpiAdvColumnView;	// 编辑显示列的界面组件

	private Button search_bt;							// 刷新结果按钮
	private Button clear_bt;							// 清空输入内容

	private Button exportExcel; 						// 导出记录的按钮
	private Embedded downloader; 						// 存放数据的组件
	
	private Table kpiAdvTable;							// 记录的显示表格

	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private BeanItemContainer<Department> deptContainer;	 		// 当前用户可以看到的部门
	private BeanItemContainer<KpiAdvanceVo> kpiAdvanceContainer;	// 存储所有查出记录的容器
	
	private Map<Long, String> uidToUnameMap;						// 坐席id与用户名的对应关系
	private Map<String, Long> unameToUidMap;						// 用户名与用户id 的对应关系
	private List<Object[]> statisticUinfoRsLs;						// 用户名、编号、部门等基础信息的查询结果	【u.domain_id, u.id, u.realname, u.empno, u.username, u.department_id, d.name as deptname】
	private Map<Long, HashMap<Date, KpiAdvanceVo>> kpiAdvVoMp;		// Map<userId, HashMap<date, KpiAdvanceVo>>
	
	private User loginUser;					// 当前登录用户
	private Domain domain;					// 当前用户所属域
	private String exportPath;				// 导出路径
	private String viewType;				// 获取查看的统计方式 ："by_mon"、"by_day"、"by_hour"
	private ArrayList<Long> govDeptIds;		// 管辖的所有部门编号
	private String selectedDeptIdStr;		// 当前选择的部门编号，形式：1,2,33,44
	private ArrayList<String> ownBusinessModels;	// 当前用户所有用的所有管理角色的权限
	
//	private long execute_tc = 0;			// 执行次数

	private DepartmentService departmentService;		// 部门服务类
	private CommonService commonService;
	private CallStatisticOverviewService callStatisticOverviewService;
	
	public AdvanceKPIReportView() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		exportPath = ConfigProperty.PATH_EXPORT;
		uidToUnameMap = new HashMap<Long, String>();
		unameToUidMap = new HashMap<String, Long>();
		kpiAdvVoMp = new HashMap<Long, HashMap<Date,KpiAdvanceVo>>();
		
		PERCENT_FMT.setMaximumFractionDigits(2);
		
		departmentService = SpringContextHolder.getBean("departmentService");
		commonService = SpringContextHolder.getBean("commonService");
		callStatisticOverviewService = SpringContextHolder.getBean("callStatisticOverviewService");

		/******************************************** UI 构建 ****************************************************/

		gridLayout = new GridLayout(5, 2);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false);
		this.addComponent(gridLayout);

		//--------- 第一行  -----------//
		this.createTimeScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		this.createDeptHLayout();
		
		//--------- 第二行  -----------//
		this.createViewTypeHLayout();
		this.createEditViewColHLayout();
		
		//--------- 创建操作按钮 -----------//
		this.createOperateButtons();

		//--------- 创建显示记录的表格 -----------//
		this.createAdvKpiTable();

	}
	
	@Override
	public void attach() {
		// 初始化可以查看的列数据
		editKpiAdvColumnView.rebuildColUi(VIEW_TYPE_DEFAULT);
//		editKpiAdvColumnView.getSelectAll_bt().click();
		String jpql = "select s from KpiTableColVo as s";
		List<KpiTableColVo> kpiTableColVos = commonService.getEntitiesByJpql(jpql);
		KpiTableColVo kpiTableColVo = null;
		if(null != kpiTableColVos && kpiTableColVos.size() > 0) {
			kpiTableColVo = kpiTableColVos.get(0);
		} else {
			kpiTableColVo = editKpiAdvColumnView.getVisibleColAndHeadersMap(viewType);
		}
		kpiAdvTable.setVisibleColumns(kpiTableColVo.getVisibleCols());
		kpiAdvTable.setColumnHeaders(kpiTableColVo.getVisibleHeaders());
		
		clear_bt.click();
	}

	/**
	 * 创建  存放“原始数据时间范围标签和其选择框” 的布局管理器
	 */
	private void createTimeScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
		Label timeScopeLabel = new Label("时间范围：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		timeScope_cb = new ComboBox();
		timeScope_cb.setImmediate(true);
		timeScope_cb.addItem("今天");
		timeScope_cb.addItem("昨天");
		timeScope_cb.addItem("本周");
		timeScope_cb.addItem("上周");
		timeScope_cb.addItem("本月");
		timeScope_cb.addItem("上月");
		timeScope_cb.addItem("精确时间");
		timeScope_cb.setValue("今天");
		timeScope_cb.setWidth("100px");
		timeScope_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		timeScope_cb.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(timeScope_cb);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)timeScope_cb.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startTime_pdf.removeListener(startTimeListener);
				finishTime_pdf.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startTime_pdf.setValue(dates[0]);
				finishTime_pdf.setValue(dates[1]);
				startTime_pdf.addListener(startTimeListener);
				finishTime_pdf.addListener(finishTimeListener);
			}
		};
		timeScope_cb.addListener(timeScopeListener);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 1, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope_cb.removeListener(timeScopeListener);
				timeScope_cb.setValue("精确时间");
				timeScope_cb.addListener(timeScopeListener);
			}
		};
		
		startTime_pdf = new PopupDateField();
		startTime_pdf.setWidth("120px");
		startTime_pdf.setImmediate(true);
		startTime_pdf.setValue(dates[0]);
		startTime_pdf.addListener(startTimeListener);
		startTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime_pdf.setParseErrorMessage("时间格式不合法");
		startTime_pdf.setValidationVisible(false);
		startTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
		startTimeHLayout.addComponent(startTime_pdf);
	}

	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 2, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope_cb.removeListener(timeScopeListener);
				timeScope_cb.setValue("精确时间");
				timeScope_cb.addListener(timeScopeListener);
			}
		};
		
		finishTime_pdf = new PopupDateField();
		finishTime_pdf.setImmediate(true);
		finishTime_pdf.setWidth("120px");
		finishTime_pdf.setValue(dates[1]);
		finishTime_pdf.addListener(finishTimeListener);
		finishTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime_pdf.setParseErrorMessage("时间格式不合法");
		finishTime_pdf.setValidationVisible(false);
		finishTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
		finishTimeHLayout.addComponent(finishTime_pdf);
	}
	
	private void createDeptHLayout() {
		Map<Long, Department> allGovDeptMap = new HashMap<Long, Department>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					allGovDeptMap.put(dept.getId(), dept);
				}
			}
		}

		Department allDept = new Department();
		allDept.setName("全部");
		
		govDeptIds = new ArrayList<Long>();
		govDeptIds.addAll(allGovDeptMap.keySet());
		
		deptContainer = new BeanItemContainer<Department>(Department.class);
		deptContainer.addBean(allDept);
		deptContainer.addAll(allGovDeptMap.values());
		
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 3, 0);

		Label deptLabel = new Label("部门选择：");
		deptLabel.setWidth("-1px");
		deptHLayout.addComponent(deptLabel);
		
		deptSelector = new ComboBox();
		deptSelector.setWidth("100px");
		deptSelector.addListener(this);
		deptSelector.setNullSelectionAllowed(false);
		deptSelector.setContainerDataSource(deptContainer);
		deptSelector.setItemCaptionPropertyId("name");
		deptSelector.setValue(allDept);
		deptHLayout.addComponent(deptSelector);
	}
	
	private void createViewTypeHLayout() {
		HorizontalLayout viewTypeHLayout = new HorizontalLayout();
		viewTypeHLayout.setSpacing(true);
		gridLayout.addComponent(viewTypeHLayout, 0, 1);
		
		Label viewTypeLabel = new Label("统计方式：");
		viewTypeLabel.setWidth("-1px");
		viewTypeHLayout.addComponent(viewTypeLabel);
		
		statisticType_cb = new ComboBox();
		statisticType_cb.addListener((ValueChangeListener)this);
		statisticType_cb.setImmediate(true);
		statisticType_cb.addItem(VIEW_TYPE_BY_HOUR);
		statisticType_cb.addItem(VIEW_TYPE_BY_DAY);
		statisticType_cb.addItem(VIEW_TYPE_BY_MON);
		statisticType_cb.setItemCaption(VIEW_TYPE_BY_HOUR, "按天、时统计");
		statisticType_cb.setItemCaption(VIEW_TYPE_BY_DAY, "按天");
		statisticType_cb.setItemCaption(VIEW_TYPE_BY_MON, "按月");
		statisticType_cb.setValue(VIEW_TYPE_DEFAULT);
		statisticType_cb.setWidth("100px");
		statisticType_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		statisticType_cb.setNullSelectionAllowed(false);
		viewTypeHLayout.addComponent(statisticType_cb);
	}
	
	private void createEditViewColHLayout() {
		HorizontalLayout editColHLayout = new HorizontalLayout();
		editColHLayout.setSpacing(true);
		gridLayout.addComponent(editColHLayout, 1, 1);
		
		Label viewTypeLabel = new Label("显 示 列 ：");
		viewTypeLabel.setWidth("-1px");
		editColHLayout.addComponent(viewTypeLabel);

		editKpiAdvColumnView = new EditKpiAdvColumnView();
		visibleCol_pv = new PopupView(editKpiAdvColumnView);
		visibleCol_pv.setHideOnMouseOut(false);
		editColHLayout.addComponent(visibleCol_pv);
		editColHLayout.setComponentAlignment(visibleCol_pv, Alignment.MIDDLE_LEFT);
		
		visibleCol_pv.addListener(new PopupVisibilityListener() {
			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if(visibleCol_pv.isPopupVisible()) {	// 在PopupView 可见的时候，为其内部OptionGroup 更改数据源
					editKpiAdvColumnView.rebuildColUi((String) statisticType_cb.getValue());
				} else {
					KpiTableColVo kpiTableColVo = editKpiAdvColumnView.getVisibleColAndHeadersMap(viewType);
					String jpql = "select s from KpiTableColVo as s";
					List<KpiTableColVo> kpiTableColVos = commonService.getEntitiesByJpql(jpql);
					if(null != kpiTableColVos && kpiTableColVos.size() > 0 ) {
						commonService.delete(KpiTableColVo.class, kpiTableColVos.get(0).getId());
					}
					commonService.save(kpiTableColVo);
				}
			}
		});
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 4, 0);
		
		search_bt = new Button("查 询", this);
		search_bt.addStyleName("default");
		search_bt.setWidth("60px");
		search_bt.setImmediate(true);
		layout.addComponent(search_bt);
		
		clear_bt = new Button("清 空", this);
		clear_bt.setWidth("58px");
		clear_bt.setImmediate(true);
		layout.addComponent(clear_bt);
		
		exportExcel = new Button("导出Excel", this);
		exportExcel.setImmediate(true);
		if (ownBusinessModels.contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_ADVANCE_KPI)) {
			layout.addComponent(exportExcel);
		}

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
	}

	/**
	 * 创建表格
	 */
	private void createAdvKpiTable() {
		kpiAdvanceContainer = new BeanItemContainer<KpiAdvanceVo>(KpiAdvanceVo.class);

		kpiAdvTable = createFormatColumnTable();
		kpiAdvTable.setWidth("100%");
		kpiAdvTable.setHeight("-1px");
		kpiAdvTable.setHeight("100%");
		kpiAdvTable.setSelectable(true);
		kpiAdvTable.setStyleName("striped");
		kpiAdvTable.setNullSelectionAllowed(false);
		kpiAdvTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		kpiAdvTable.setContainerDataSource(kpiAdvanceContainer);
		this.addComponent(kpiAdvTable);
		this.setComponentAlignment(kpiAdvTable, Alignment.MIDDLE_CENTER);
		this.setExpandRatio(kpiAdvTable, 1.0f);
	}

	/**
	 * 创建格式化显示列的表格“注意：这个表格中的数据时数组Object[]”
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if("startDate".equals(colId)) {
					if(VIEW_TYPE_BY_MON.equals(viewType)) {
						return SDF_MON.format(property.getValue());
					} else {
						return SDF_DAY.format(property.getValue());
					}
					
				} else if("hour".equals(colId)) { 
					int hour = (Integer) property.getValue();
					if(hour < 10) {
						return "0"+hour+":00-0"+hour+":59";
					} else {
						return hour+":00-"+hour+":59";
					}
                } else if ("effectWorkRt".equals(colId) || "bridgeRt".equals(colId) 
                		|| "effectDialRt".equals(colId) || "outBgRt".equals(colId) 
                		|| "outBgLowSecRt".equals(colId) || "outBgLowSecRt1".equals(colId)
                		|| "outBgLowSecRt2".equals(colId) || "outBgLargeSecRt".equals(colId) 
                		|| "outBgLargeSecRt1".equals(colId) || "outBgLargeSecRt2".equals(colId) 
                		|| "outBgLargeSecRt3".equals(colId)|| "outBgLargeSecRt4".equals(colId) 
                		|| "outBgLargeSecRt5".equals(colId)|| "outBgLargeSecRt6".equals(colId) 
                		|| "outBgLargeSecRt7".equals(colId) || "outEffectDtRt".equals(colId) 
                		|| "innerBgRt".equals(colId) || "inBgRt".equals(colId) || "inLevelRt".equals(colId)
                		|| "inBgLowSecRt1".equals(colId) || "inBgLargeSecRt3".equals(colId) 
                		|| "inBgLargeSecRt4".equals(colId) || "inBgLargeSecRt5".equals(colId)
                		|| "inBgLargeSecRt6".equals(colId) || "inBgLargeSecRt7".equals(colId) 
                		|| "inLevelRt2".equals(colId) || "inBgLowSecRt".equals(colId) 
                		|| "inBgLowSecRt2".equals(colId) || "inBgLargeSecRt".equals(colId)
                		|| "inBgLargeSecRt".equals(colId) || "inEffectDtRt".equals(colId)) {
                	return PERCENT_FMT.format((Double) property.getValue());
                }
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == statisticType_cb) {	// 当查看类型发生变化时，修改时间范围组件的格式化类型
			String type = (String) statisticType_cb.getValue();
			if(VIEW_TYPE_BY_HOUR.equals(type)) {
				startTime_pdf.setDateFormat("yyyy-MM-dd H");
				startTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
				finishTime_pdf.setDateFormat("yyyy-MM-dd H");
				finishTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
			} else if(VIEW_TYPE_BY_DAY.equals(type)) {
				startTime_pdf.setDateFormat("yyyy-MM-dd");
				startTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
				finishTime_pdf.setDateFormat("yyyy-MM-dd");
				finishTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
			} else if(VIEW_TYPE_BY_MON.equals(type)) {
				startTime_pdf.setDateFormat("yyyy-MM");
				startTime_pdf.setResolution(PopupDateField.RESOLUTION_MONTH);
				finishTime_pdf.setDateFormat("yyyy-MM");
				finishTime_pdf.setResolution(PopupDateField.RESOLUTION_MONTH);
			}
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_bt) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					search_bt.setEnabled(false);
					
//					Long st = System.currentTimeMillis();
					try {
						boolean isvalid = executeConfirm();		// 执行验证，检查各选择框中的值，如果符合查询条件，则返回true
						
						if(isvalid) {
							executeSearch(viewType);			// 执行搜索
						}
					} catch (Exception e1) {
						logger.error("JRH 管理员查询高级KPI详情时，出现异常-->"+e1.getMessage(), e1);
						search_bt.getApplication().getMainWindow().showNotification("对不起，您所选外线为空，无数据可查！", Notification.TYPE_WARNING_MESSAGE);
					}
//					
//					try {
//						Long sp = System.currentTimeMillis();
//						System.out.println();
//						System.out.println("统计类型： "+viewType+" -----执行总次数----- "+execute_tc);
//						System.out.println("总 --执行时间范围【 "+SDF_SEC.format(startTime_pdf.getValue())+" - "+SDF_SEC.format(finishTime_pdf.getValue())+" 】---耗时： "+((sp - st) / 1000) +" 秒   【("+(sp - st)+" 毫秒)】");
//					} catch (Exception e) { }
					
					search_bt.setEnabled(true);
				}
			}).start();
		} else if(source == clear_bt) {
			timeScope_cb.select("今天");
			statisticType_cb.setValue(VIEW_TYPE_DEFAULT);
			deptSelector.setValue(deptContainer.getIdByIndex(0));
		} else if (source == exportExcel) {
			// 导出高级KPI详情
			new Thread(new Runnable() {
				@Override
				public void run() {
					exportExcel.setEnabled(false);
					executeExportExcel();			// 执行导出操作
					exportExcel.setEnabled(true);
				}
			}).start();
		}
	}

	/**
	 * @Description 描述：执行验证，检查各选择框中的值，如果符合查询条件，则返回true
	 *
	 * @author  JRH
	 * @date    2014年5月21日 上午9:30:37
	 * @return boolean
	 */
	private boolean executeConfirm() {
		if(govDeptIds.size() == 0) {
			this.getApplication().getMainWindow().showNotification("对不起，您当前没有管辖部门，无数据可查！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!startTime_pdf.isValid() || !finishTime_pdf.isValid()) {
			this.getApplication().getMainWindow().showNotification("开始时间或截止时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		Date start_time = (Date) startTime_pdf.getValue();
		if(start_time.after(new Date())) {
			this.getApplication().getMainWindow().showNotification("对不起, 开始时间不能超过当前时间！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
	
		viewType = (String) statisticType_cb.getValue();	// 获取查看的统计方式 ："by_mon"、"by_day"、"by_hour"
	
		Date[] fmt_input_ts = executeRebuildTimeScope(viewType);	// 根据查看的统计方式，来重新配置时间范围值
		Date fmt_in_start_time = fmt_input_ts[0];
		Date fmt_in_end_time = fmt_input_ts[1];
		
		if(!fmt_in_end_time.after(fmt_in_start_time)) {
			this.getApplication().getMainWindow().showNotification("对不起，截止时间必须大于开始时间，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
	
		startTime_pdf.setValue(fmt_in_start_time); 
		finishTime_pdf.setValue(fmt_in_end_time); 
		
		String warning = "对不起，单次只能查询一个月范围内的数据，请修改后重试！";
		Calendar cal = Calendar.getInstance();
		cal.setTime(fmt_in_end_time);
		if(VIEW_TYPE_BY_HOUR.equals(viewType)) {
			cal.add(Calendar.DAY_OF_YEAR, -1);
			warning = "对不起，按天、时统计查询时，单次只能查询一天范围内的数据，请修改后重试！";
		} else {
			cal.add(Calendar.MONTH, -1);
		}
		Date minStartDate = cal.getTime();
		
		if(start_time.before(minStartDate)) {
			this.getApplication().getMainWindow().showNotification(warning, Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}

	/**
	 * @Description 描述：根据查看的统计方式，来重新配置时间范围值
	 *
	 * @author  JRH
	 * @date    2014年5月7日 下午7:41:49
	 * @param viewType	查看的统计方式 ："by_mon"、"by_day"、"by_hour"	
	 * @return Date[]
	 */
	private Date[] executeRebuildTimeScope(String viewType) {
		Date input_start_time = (Date) startTime_pdf.getValue();
		Date input_end_time = (Date) finishTime_pdf.getValue();
		
		Date now_dt = new Date();
		if(now_dt.before(input_end_time)) {	// 如果当前时间比格式按指定要求格式化后的时间还要小，则进一步处理
			input_end_time = executeFormatTime(viewType, now_dt);
		}
		finishTime_pdf.setValue(input_end_time); 
		
		Calendar ini_cal = Calendar.getInstance();
		if(VIEW_TYPE_BY_HOUR.equals(viewType)) {		// 根据查看类型：修改开始时间，以及终止统计的时间
			ini_cal.setTime(input_start_time);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_start_time = ini_cal.getTime();

			ini_cal.setTime(input_end_time);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_end_time = ini_cal.getTime();
		} else if(VIEW_TYPE_BY_DAY.equals(viewType)) {
			ini_cal.setTime(input_start_time);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_start_time = ini_cal.getTime();

			ini_cal.setTime(input_end_time);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_end_time = ini_cal.getTime();
		} else if(VIEW_TYPE_BY_MON.equals(viewType)) {
			ini_cal.setTime(input_start_time);
			ini_cal.set(Calendar.DAY_OF_MONTH, 1);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_start_time = ini_cal.getTime();

			ini_cal.setTime(input_end_time);
			ini_cal.set(Calendar.DAY_OF_MONTH, 1);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_end_time = ini_cal.getTime();
		}
		
		return new  Date[]{input_start_time, input_end_time};
	}

	/**
	 * @Description 描述：格式化截止时间
	 *		规则：
	 *		            分、秒、毫秒直接置为0
	 *			按小时：分钟  > 0 , 小时+1
	 *			按天：    小时  > 0 , 天数+1
	 *  		按月：    天数  > 0 , 月份+1
	 *  
	 * @author  JRH
	 * @date    2014年5月7日 下午8:15:45
	 * @param viewType	查看的统计方式 ："by_mon"、"by_day"、"by_hour"	
	 * @param datetime	带格式化的时间
	 * @return Date
	 */
	private Date executeFormatTime(String viewType, Date datetime) {
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(datetime);
		if(VIEW_TYPE_BY_DAY.equals(viewType)) {		// 根据查看类型：修改截止时间
			if(cal_end.get(Calendar.HOUR_OF_DAY) > 0) {
				cal_end.add(Calendar.DAY_OF_YEAR, +1);
			}
			cal_end.set(Calendar.HOUR_OF_DAY, 0);
		} else if(VIEW_TYPE_BY_MON.equals(viewType)) {
			if(cal_end.get(Calendar.DAY_OF_MONTH) > 0) {
				cal_end.add(Calendar.MONTH, +1);
			}
			cal_end.set(Calendar.DAY_OF_MONTH, 1);
		} else if(VIEW_TYPE_BY_HOUR.equals(viewType)) {
			if(cal_end.get(Calendar.MINUTE) > 0) {
				cal_end.add(Calendar.HOUR_OF_DAY, +1);
			}
		}
	
		cal_end.set(Calendar.MINUTE, 0);
		cal_end.set(Calendar.SECOND, 0);
		cal_end.set(Calendar.MILLISECOND, 0);
		Date input_end_time = cal_end.getTime();
		return input_end_time;
	}

	/**
	 * 制作表格
	 */
	private boolean executeExportExcel() {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			String statisticStr = "按天、时统计_";
			if(VIEW_TYPE_BY_DAY.equals(viewType)) {
				statisticStr = "按天统计_";
			} else if(VIEW_TYPE_BY_MON.equals(viewType)) {
				statisticStr = "按月统计_";
			}
			
			String fileName = "/高级KPI详情_"+statisticStr;
			
			//======================chb 操作日志======================//
			file = new File(new String((exportPath + fileName
					+ new Date().getTime() + ".xls").getBytes("UTF-8"),	"ISO-8859-1"));
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("无法在指定位置创建新Excel文件！");
				}
			} else {
				throw new RuntimeException("Excel文件已经存在，请重新创建！");
			}

			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "UTF-8");

			// 记录导出日志
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(loginUser.getUsername());
			operationLog.setRealName(loginUser.getRealName());
			WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			operationLog.setIp(ip);
			operationLog.setDescription("导出高级KPI详情");
			operationLog.setProgrammerSee("");
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			// 记录查出来的客服记录Id值
			KpiAdvanceVo firstLog = (KpiAdvanceVo) kpiAdvTable.firstItemId();
			if (firstLog == null) {
				this.getApplication().getMainWindow().showNotification("选出的结果为空，不能导出高级KPI详情！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			String[] visible_header_arr = kpiAdvTable.getColumnHeaders();
			Object[] visible_col_arr = kpiAdvTable.getVisibleColumns();
			ArrayList<String> visible_colLs = new ArrayList<String>();
			for(Object col : visible_col_arr) {
				visible_colLs.add(col.toString());
			}

			Long processCount = 0L;
			int sheetIndex = 0;
			int col_len = visible_header_arr.length;
			
			WritableCellFormat percentCellFormat = new WritableCellFormat(JXL_PERCENT_FMT);
			WritableCellFormat numberCellFormat = new WritableCellFormat(JXL_NUMBER_FMT);
			
			
			for (KpiAdvanceVo kpiAdv : kpiAdvanceContainer.getItemIds()) {
				
				WritableCellFormat cf = null;
				
				// 如果当前已经查到超过50000条记录，则新建一个sheet
				if ((processCount % 50000) == 0) {
					++sheetIndex;
					sheet = writableWorkbook.createSheet("Sheet" + sheetIndex, sheetIndex);
					for (int ch = 0; ch < col_len; ch++) {	// 设置excel列名
						sheet.addCell(new jxl.write.Label(ch, 0, visible_header_arr[ch], cellFormat));
					}
				}
				
				int row = sheet.getRows();		// 获取当前行数
				
				for(int col = 0; col < col_len; col++) {
					String colName = visible_colLs.get(col);
					String colValue = "";
					try {
						String colMethod = "get"+colName.substring(0,1).toUpperCase() + colName.substring(1);
						Method method = kpiAdv.getClass().getMethod(colMethod);
						Object originateVal = method.invoke(kpiAdv);

						if(originateVal == null) {
							colValue = "";
						} else if("hour".equals(colName)) { 
							int hour = (Integer) originateVal;
							if(hour < 10) {
								colValue = "0"+hour+":00-0"+hour+":59";
							} else {
								colValue = hour+":00-"+hour+":59";
							}
						} else if ("effectWorkRt".equals(colName) || "bridgeRt".equals(colName) 
		                		|| "effectDialRt".equals(colName) || "outBgRt".equals(colName) 
		                		|| "outBgLowSecRt".equals(colName) || "outBgLowSecRt2".equals(colName) 
		                		|| "outBgLargeSecRt".equals(colName) || "outBgLargeSecRt2".equals(colName) 
		                		|| "outEffectDtRt".equals(colName) || "innerBgRt".equals(colName) 
		                		|| "inBgRt".equals(colName) || "inLevelRt".equals(colName) 
		                		|| "inLevelRt2".equals(colName) || "inBgLowSecRt".equals(colName) 
		                		|| "inBgLowSecRt2".equals(colName) || "inBgLargeSecRt".equals(colName)
		                		|| "inBgLargeSecRt".equals(colName) || "inEffectDtRt".equals(colName)) {
							colValue = PERCENT_FMT.format((Double) originateVal);
						} else if("startDate".equals(colName)) {
							if(VIEW_TYPE_BY_MON.equals(viewType)) {
								colValue = SDF_MON.format(originateVal);
							} else {
								colValue = SDF_DAY.format(originateVal);
							}
						} else {
							colValue = originateVal.toString();
						}

						if(colName.endsWith("Rt") || colName.endsWith("Rt2")) {		// 如果是比率，按百分比格式处理
							cf = percentCellFormat;
							double value = (Double) originateVal;
							jxl.write.Number numberCell = new jxl.write.Number(col, row, value, cf);
							sheet.addCell(numberCell);				// 创建单元格数据
						} else if(colName.endsWith("Ct") || colName.endsWith("Ct2")) {		// 如果是次数，按数值类型处理
							cf = numberCellFormat;
							double value = Double.valueOf(colValue);
							jxl.write.Number numberCell = new jxl.write.Number(col, row, value, cf);
							sheet.addCell(numberCell);				// 创建单元格数据
						} else {
							sheet.addCell(new jxl.write.Label(col, row, colValue));				// 创建单元格数据
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				++processCount;		// 处理记录数自增1
				
			}
			
			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
			writableWorkbook.close();

			// 文件创建好后，下载文件
			executeDownloadFile(file);
		} catch (Exception e) {
			// 导出完成之后是导出按钮可用
			exportExcel.setEnabled(true);
			logger.error(e.getMessage() + " 导出高级KPI详情Excel出现异常!", e);
			this.getApplication().getMainWindow().showNotification("高级KPI详情导出失败，请重试!", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	/**
	 * 文件创建好之后，创建文件
	 * 
	 * @param file
	 */
	private void executeDownloadFile(File file) {
		// 下载报表
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		this.getApplication().getMainWindow().addComponent(downloader);
	
		downloader.addListener(new RepaintRequestListener() {
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if (downloader.getParent() != null) {
					((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}

	/**
	 * @Description 描述：
	 *
	 * @author  JRH
	 * @date    2014年5月7日 下午7:19:16 void
	 * @param viewType 	查看的统计方式 ："by_mon"、"by_day"、"by_hour"
	 */
	private void executeSearch(String viewType) {
		// 重新构造选择的部门编号的查询字符串
		Department selectedDept = (Department) deptSelector.getValue();
		selectedDeptIdStr = "";
		if(selectedDept.getId() == null) {
			selectedDeptIdStr = StringUtils.join(govDeptIds, ",");
		} else {
			selectedDeptIdStr = selectedDept.getId().toString();
		}

		// 清空上次查询的数据
		kpiAdvanceContainer.removeAllItems();
		kpiAdvVoMp.clear();

		// 根据用户选择的显示列，进行处理
//		KpiTableColVo kpiTableColVo = editKpiAdvColumnView.getVisibleColAndHeadersMap(viewType);
		String jpql = "select s from KpiTableColVo as s";
		List<KpiTableColVo> kpiTableColVos = commonService.getEntitiesByJpql(jpql);
		KpiTableColVo kpiTableColVo = kpiTableColVos.get(0);
		kpiAdvTable.setVisibleColumns(kpiTableColVo.getVisibleCols());
		kpiAdvTable.setColumnHeaders(kpiTableColVo.getVisibleHeaders());
		
		Date input_start_time = (Date) startTime_pdf.getValue();
		Date input_finish_time = (Date) finishTime_pdf.getValue();
		
		Calendar calc_cal = Calendar.getInstance();
		calc_cal.setTime(input_start_time);
		Date startTime = calc_cal.getTime();
		
//		execute_tc = 0;
		
		// 初始化生成报表需要用到的基础信息集合
		boolean isneedContinue = buildUserInfos();

		// 如果需要继续查询（存在用户），则根据时间范围，按时间跨度依次查询
		while(isneedContinue && startTime.before(input_finish_time)) {
			
//			Long st = System.currentTimeMillis();
			
			if(VIEW_TYPE_BY_HOUR.equals(viewType)) {		// 根据查看类型：修改截止时间
				calc_cal.add(Calendar.HOUR_OF_DAY, +1);
			} else if(VIEW_TYPE_BY_DAY.equals(viewType)) {
				calc_cal.add(Calendar.DAY_OF_YEAR, +1);
			} else if(VIEW_TYPE_BY_MON.equals(viewType)) {
				calc_cal.add(Calendar.MONTH, +1);
			}
			Date finishTime = calc_cal.getTime();
			
			String start_time_str = SDF_SEC.format(startTime);
			String finish_time_str = SDF_SEC.format(finishTime);

			/***************************************************************************/
			// 根据用户信息及时间段构造KPI实体
			if(kpiTableColVo.isBaseVisible()) {
				buildAdvKpiByUinfoTime(statisticUinfoRsLs, startTime);
			}
			
			// 根据查询时间段，统计用户登陆信息  [ 登陆次数、登陆时长 ]
			if(kpiTableColVo.isLoginVisible()) {
				buildAdvKpiLoginInfos(startTime, start_time_str, finish_time_str);
			}

			// 根据查询时间段，统计用户置忙信息  [ 置忙次数、置忙时长 ]
			if(kpiTableColVo.isPauseVisible()) {
				buildAdvKpiPauseLogInfos(startTime, start_time_str, finish_time_str);
			}
			
			// 根据查询时间段，统计呼出话务详情   [ 呼出总数,   呼出接通数,   最大呼出通话时长,   呼出等待时长,   呼出小于xx秒接通次数,   呼出大于yy秒接通次数 ]
			if(kpiTableColVo.isCallOutVisible() || kpiTableColVo.isTotalCallVisible()) {
				buildAdvKpiOutDialInfos(startTime, start_time_str, finish_time_str);
			}

			// 根据查询时间段，统计   [ 呼出\内部呼叫时长,   呼出\内部接通时长]
			if(kpiTableColVo.isCallOutVisible() || kpiTableColVo.isCallInnerVisible() || kpiTableColVo.isTotalCallVisible()) {
				buildAdvKpiOiDialDtBgDtInfos(startTime, start_time_str, finish_time_str);
			}

			// 根据查询时间段，统计内部话务详情    统计   [ 内部呼叫数,   内部接通数,   最大内部呼叫时长,   每小时内呼数 ]
			if(kpiTableColVo.isCallInnerVisible() || kpiTableColVo.isTotalCallVisible()) {
				buildAdvKpiInnerDialInfos(startTime, start_time_str, finish_time_str);
			}
			
			// 根据查询时间段，统计呼入话务详情 统计   [ 呼入总数,   呼入接通数,   最大呼入通话时长,   呼入振铃时长,   呼入小于zz秒接通次数,  呼入大于ww秒接通次数,   服务水平内接通数    ]
			if(kpiTableColVo.isCallInVisible() || kpiTableColVo.isTotalCallVisible()) {
				buildAdvKpiInDialInfos(startTime, start_time_str, finish_time_str);
			}
			
			// 根据查询时间段，统计   [ 呼入总时长,   呼入接通时长 ]
			if(kpiTableColVo.isCallInVisible() || kpiTableColVo.isTotalCallVisible()) {
				buildAdvKpiInDialDtBgDtInfos(startTime, start_time_str, finish_time_str);
			}
			
			// 根据查询时间段，统计   [ 呼入个人漏接量 ]
			if(kpiTableColVo.isMissCallInVisible()) {
				buildAdvKpiInMissCtInfos(startTime, start_time_str, finish_time_str);
			}
			
			// 根据查询时间段，统计呼入/呼出 话后处理信息 	统计   [ 呼入/呼出 话后处理次数,   呼入/呼出 话后处理时长    ]
			if(kpiTableColVo.isCallOutVisible() || kpiTableColVo.isCallInVisible()) {
				buildAdvKpiCallAfterInfos(startTime, start_time_str, finish_time_str);
			}
			
			buildCalcAdvKpiInfos(startTime, finishTime, kpiTableColVo);

			/***************************************************************************/

//			Long sp = System.currentTimeMillis();
//			System.out.println(">>>单步--执行时间范围【 "+SDF_SEC.format(startTime)+" - "+SDF_SEC.format(finishTime)+" 】---耗时： "+((sp - st) / 1000) +" 秒   【("+(sp - st)+" 毫秒)】");
			
			startTime = finishTime;					// 开始时间 赋值为上次的结束时间

//			++execute_tc;		// 更新执行次数，用于自己测试而已

		}
		
		kpiAdvTable.refreshRowCache();		// 执行完成后必须要刷新表格的缓存，不然数据的更新将无法显示出来
		
	}

	/**
	 * @Description 描述：初始化生成报表需要用到的基础信息集合
	 *
	 * @author  JRH
	 * @date    2014年5月7日 下午7:24:42 void
	 */
	private boolean buildUserInfos() {
		
		uidToUnameMap.clear();
		unameToUidMap.clear();
		
		// 初始化坐席id与用户名的对应关系    及  用户名与用户id 的对应关系
		String uid2UnameSql = "select id, username from ec2_user where domain_id = " + domain.getId() + " and department_id in("+selectedDeptIdStr+") order by id";

		List<Object[]> uid2UnameRsLs = callStatisticOverviewService.getByNativeSql(uid2UnameSql);
		for(Object[] objs : uid2UnameRsLs) {
			if(objs.length >= 2) {
				Long uid = (Long) objs[0];
				String uname = (String) objs[1];
				uidToUnameMap.put(uid, uname);
				unameToUidMap.put(uname, uid); 
			}
		}

		if(uidToUnameMap.size() == 0) {
			this.getApplication().getMainWindow().showNotification("对不起，您当前管辖部门下没有用户，无数据可查！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		// 获取用户与部门的基础信息
		String stcUinfoSql = "select u.domain_id, u.id, u.realname, u.empno, u.username, u.department_id, d.name as deptname"
				+ " from ec2_user as u left join ec2_department as d on u.department_id = d.id "
				+ " where u.domain_id = " + domain.getId() + " and u.department_id in("+selectedDeptIdStr+") order by u.id asc";
		statisticUinfoRsLs = callStatisticOverviewService.getByNativeSql(stcUinfoSql);
		
		return true;
	}
	
	/**
	 * @Description 描述：根据用户信息及时间段构造KPI实体
	 *
	 * @author  JRH
	 * @date    2014年5月7日 下午5:19:51
	 * @param finishTime		截止查询时间
	 * @param startTime			开始查询时间
	 */
	private void buildAdvKpiByUinfoTime(List<Object[]> stcUinfoRsLs, Date startTime) {
		Calendar sta_cal = Calendar.getInstance();
		sta_cal.setTime(startTime);
		int startHour = sta_cal.get(Calendar.HOUR_OF_DAY);
		
		sta_cal.set(Calendar.HOUR_OF_DAY, 0);
		Date startDate = sta_cal.getTime();
		
		for(Object[] objs : stcUinfoRsLs) {
			if(objs.length >= 7) {
				Long did = (Long) objs[0];
				Long uid = (Long) objs[1];
				String realname = (String) objs[2];
				String empno = (String) objs[3];
				String username = (String) objs[4];
				Long deptid = (Long) objs[5];
				String deptname = (String) objs[6];
				
				KpiAdvanceVo kpiVo = new KpiAdvanceVo();
				kpiVo.setDomainId(did);
				kpiVo.setCsrId(uid);
				kpiVo.setCsrName(realname);
				kpiVo.setEmpno(empno);
				kpiVo.setUsername(username);
				kpiVo.setDeptId(deptid);
				kpiVo.setDeptName(deptname);
				kpiVo.setStartDate(startDate);
				kpiVo.setHour(startHour);
				kpiAdvanceContainer.addBean(kpiVo);
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp == null) {
					date2KpiVoMp = new HashMap<Date, KpiAdvanceVo>();
					kpiAdvVoMp.put(uid, date2KpiVoMp);
				}
				
				date2KpiVoMp.put(startTime, kpiVo);
			}
		}
	}

	/**
	 * @Description 描述：根据查询时间段，统计客户登陆信息 [ 登陆次数、登陆时长 ]
	 * 		
	 * @author  JRH
	 * @date    2014年5月8日 上午11:38:22
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiLoginInfos(Date startTime, String start_time_str, String finish_time_str) {
		// TODO jinht update : logindate > start_time_str
		String loingLogSql = "select "
				+ " username, "
				+ " sum(case when (logindate >='"+start_time_str+"' and logindate < '"+finish_time_str+"') then 1 else 0 end) as loginCt, "
				+ " sum(case when logoutdate is null "
					+ " then "
						+ " ( (case when (now() >= '"+finish_time_str+"') then '"+finish_time_str+"' else now() end) - (case when logindate >= '"+start_time_str+"' then logindate else '"+start_time_str+"' end) ) "
					+ " else "
						+ " ( (case when (logoutdate >= '"+finish_time_str+"') then '"+finish_time_str+"' else logoutdate end) - (case when logindate >= '"+start_time_str+"' then logindate else '"+start_time_str+"' end) ) "
					+ "	end) as online_duration "
				+ " from ec2_user_login_record "
				+ " where (logoutdate > '"+start_time_str+"' or logoutdate is null) and logindate > '"+start_time_str+"' and logindate < '"+finish_time_str+"' and domainid = " +domain.getId()+ " and deptid in ("+selectedDeptIdStr+")"
				+ " group by username "
				+ " order by username ";

		List<Object[]> loginLogRsLs = callStatisticOverviewService.getByNativeSql(loingLogSql);
		for(Object[] objs : loginLogRsLs) {
			String uname = (String) objs[0];
			Long uid = unameToUidMap.get(uname);
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				Long loginCt = (Long) objs[1];
				Object[] onlineDtInfos = buildDateStr(buildDurationParams(objs[2]+""));
				Long onlineDt = (Long) onlineDtInfos[0];			
				String onlineDtStr = (String) onlineDtInfos[1];

				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					kpiVo.setLoginCt(loginCt);
					kpiVo.setOnlineDt(onlineDt);
					kpiVo.setOnlineDtStr(onlineDtStr);
				}
			}
		}
		
	}

	/**
	 * @Description 描述：根据查询时间段，统计用户置忙信息 [ 置忙次数、置忙时长 ]
	 *
	 * @author  JRH
	 * @date    2014年5月8日 上午11:42:12
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiPauseLogInfos(Date startTime, String start_time_str, String finish_time_str) {

		String pauseLogSql = "select "
				+ " t1.username, "
				+ " t1.reason, "
				+ " sum(case when (pausedate >='" + start_time_str + "' and pausedate < '" + finish_time_str + "') then 1 else 0 end ) as pauseCt, "
				+ " sum(case when unpausedate is null "
					+ " then "
						+ " ( (case when (now() >= '" + finish_time_str + "') then '" + finish_time_str + "' else now() end) - (case when pausedate >= '" + start_time_str + "' then pausedate else '" + start_time_str + "' end) ) "
					+ " else "
						+ " ( (case when (unpausedate >= '" + finish_time_str + "') then '" + finish_time_str + "' else unpausedate end) - (case when pausedate >= '" + start_time_str + "' then pausedate else '" + start_time_str + "' end) ) "
					+ " end) as pauseDuration "
				+ " from ec2_queue_member_pause_event_log as t1 "
				+ " where ((unpausedate > '" + start_time_str + "' or unpausedate is null) and pausedate < '" + finish_time_str + "') and domainid = " +domain.getId()+ " and deptid in ("+selectedDeptIdStr+") and EXISTS (select t2.reason from ec2_pause_reason as t2 where t2.reason = t1.reason)"
				+ " group by t1.username, t1.reason "
				+ " order by t1.username, t1.reason ";

		List<Object[]> pauseLogRsLs = callStatisticOverviewService.getByNativeSql(pauseLogSql);
		for(Object[] objs : pauseLogRsLs) {
			String uname = (String) objs[0];
			
			Long uid = unameToUidMap.get(uname);
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				String reason = (String) objs[1];
				Long pauseCt = (Long) objs[2];
				Object[] pauseDtInfos = buildDateStr(buildDurationParams(objs[3]+""));
				Long pauseDt = (Long) pauseDtInfos[0];			
				String pauseDtStr = (String) pauseDtInfos[1];
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					if(PAUSE_REASON_BUSY.equals(reason)) {
						kpiVo.setBusyPsCt(pauseCt);
						kpiVo.setBusyPsDt(pauseDt);
						kpiVo.setBusyPsDtStr(pauseDtStr);
					} else if(PAUSE_REASON_DINE.equals(reason)) {
						kpiVo.setDinePsCt(pauseCt);
						kpiVo.setDinePsDt(pauseDt);
						kpiVo.setDinePsDtStr(pauseDtStr);
					} else if(PAUSE_REASON_LEAVE.equals(reason)) {
						kpiVo.setLeavePsCt(pauseCt);
						kpiVo.setLeavePsDt(pauseDt);
						kpiVo.setLeavePsDtStr(pauseDtStr);
					} else if(PAUSE_REASON_MEETTING.equals(reason)) {
						kpiVo.setMeettingPsCt(pauseCt);
						kpiVo.setMeettingPsDt(pauseDt);
						kpiVo.setMeettingPsDtStr(pauseDtStr);
					} else if(PAUSE_REASON_REST.equals(reason)) {
						kpiVo.setRestPsCt(pauseCt);
						kpiVo.setRestPsDt(pauseDt);
						kpiVo.setRestPsDtStr(pauseDtStr);
					} else if(PAUSE_REASON_TRAIN.equals(reason)) {
						kpiVo.setTrainPsCt(pauseCt);
						kpiVo.setTrainPsDt(pauseDt);
						kpiVo.setTrainPsDtStr(pauseDtStr);
					}
				}
			}
		}
	}

	/**
	 * @Description 描述： 根据查询时间段，统计呼出话务详情
	 *	 	 统计  [ 呼出总数,   呼出接通数,   最大呼出通话时长,   呼出等待时长,   呼出小于xx秒接通次数,   呼出大于yy秒接通次数 ]
	 *
	 * @author  JRH
	 * @date    2014年5月8日 下午1:54:15
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiOutDialInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String dialOutLogSql = "select "
				+ " srcuserid, "
				+ " count(*) as outDialCt, "
				+ " sum(case when isbridged is true then 1 else 0 end) as outBgCt, "
				+ " max(ec2_billableseconds) as outMaxBgDt, "
				+ " sum(ringduration) as outHoDt, "
				+ " sum(case when (isbridged is true and ec2_billableseconds <= " + SPECIFY_OUT_BG_LOW_SEC + " ) then 1 else 0 end) as outBgLowSecCt, "
				+ " sum(case when (isbridged is true and ec2_billableseconds <= " + SPECIFY_OUT_BG_LOW_SEC2 + " ) then 1 else 0 end) as outBgLowSecCt2, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC + " ) then 1 else 0 end) as outBgLargeSecCt, "
				+ " sum(case when (ec2_billableseconds >  " + SPECIFY_OUT_BG_LARGE_SEC2 + " ) then 1 else 0 end) as outBgLargeSecCt2, "
				+ " sum(case when (isbridged is true and ec2_billableseconds <= " + SPECIFY_OUT_BG_LOW_SEC1 + " ) then 1 else 0 end) as outBgLowSecCt1, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC1 + " ) then 1 else 0 end) as outBgLargeSecCt1, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC3 + " ) then 1 else 0 end) as outBgLargeSecCt3, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC4 + " ) then 1 else 0 end) as outBgLargeSecCt4, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC5 + " ) then 1 else 0 end) as outBgLargeSecCt5, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC6 + " ) then 1 else 0 end) as outBgLargeSecCt6, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_OUT_BG_LARGE_SEC7 + " ) then 1 else 0 end) as outBgLargeSecCt7 "
				+ " from cdr "
				+ " where starttimedate >='" + start_time_str + "' and starttimedate < '" + finish_time_str + "' and cdrdirection = 1 and srcuserid is not null and domainid = " +domain.getId()+ " and srcdeptid in ("+selectedDeptIdStr+") "
				+ " group by srcuserid "
				+ " order by srcuserid ";

		
		List<Object[]> dialOutLogRsLs = callStatisticOverviewService.getByNativeSql(dialOutLogSql);
		for(Object[] objs : dialOutLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				
				Long outDialCt = (Long) objs[1];
				Long outBgCt = (Long) objs[2];
				Long outMaxBgDt = Long.parseLong(objs[3].toString());			
				String outMaxBgDtStr = DateUtil.getTime(outMaxBgDt);
				Long outRingDt = (Long) objs[4];
				String outRingDtStr = (String) DateUtil.getTime(outRingDt);
				Long outBgLowSecCt = (Long) objs[5]; 
				Long outBgLowSecCt2 = (Long) objs[6]; 
				Long outBgLargeSecCt = (Long) objs[7]; 
				Long outBgLargeSecCt2 = (Long) objs[8]; 
				Long outBgLowSecCt1 = (Long) objs[9]; 
				Long outBgLargeSecCt1 = (Long) objs[10]; 
				Long outBgLargeSecCt3 = (Long) objs[11]; 
				Long outBgLargeSecCt4 = (Long) objs[12]; 
				Long outBgLargeSecCt5 = (Long) objs[13]; 
				Long outBgLargeSecCt6 = (Long) objs[14]; 
				Long outBgLargeSecCt7 = (Long) objs[15]; 
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					kpiVo.setOutDialCt(outDialCt);
					kpiVo.setOutBgCt(outBgCt);
					kpiVo.setOutMaxBgDt(outMaxBgDt);
					kpiVo.setOutMaxBgDtStr(outMaxBgDtStr);
					kpiVo.setOutRingDt(outRingDt);
					kpiVo.setOutRingDtStr(outRingDtStr);
					kpiVo.setOutBgLowSecCt(outBgLowSecCt);
					kpiVo.setOutBgLowSecCt2(outBgLowSecCt2);
					kpiVo.setOutBgLargeSecCt(outBgLargeSecCt);
					kpiVo.setOutBgLargeSecCt2(outBgLargeSecCt2);
					kpiVo.setOutBgLowSecCt1(outBgLowSecCt1);
					kpiVo.setOutBgLargeSecCt1(outBgLargeSecCt1);
					kpiVo.setOutBgLargeSecCt3(outBgLargeSecCt3);
					kpiVo.setOutBgLargeSecCt4(outBgLargeSecCt4);
					kpiVo.setOutBgLargeSecCt5(outBgLargeSecCt5);
					kpiVo.setOutBgLargeSecCt6(outBgLargeSecCt6);
					kpiVo.setOutBgLargeSecCt7(outBgLargeSecCt7);
				}
			}
		}
	}

	/**
	 * @Description 描述：根据查询时间段，统计   [ 呼出\内部呼叫时长,   呼出\内部接通时长]
	 *			
	 * @author  JRH
	 * @date    2014年5月8日 下午5:24:08
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiOiDialDtBgDtInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String ioHoldBgLogSql = "select "
				+ " srcuserid , "
				+ " cdrdirection , "
				+ " sum( (case when (endtimedate >= '" + finish_time_str + "') then '" + finish_time_str + "' else endtimedate end) - (case when starttimedate < '" + start_time_str + "' then '" + start_time_str + "' else starttimedate end) ) as ioDialDt, "
				+ " sum( (case when (endtimedate >= '" + finish_time_str + "') then '" + finish_time_str + "' else endtimedate end) - (case when bridgetimedate < '" + start_time_str + "' then '" + start_time_str + "' else bridgetimedate end) ) as ioBgDt "
				+ " from cdr "
				+ " where starttimedate < '" + finish_time_str + "' and endtimedate >= '" + start_time_str + "' and (cdrdirection = 0 or cdrdirection = 1) and srcuserid is not null and domainid = " +domain.getId()+ " and srcdeptid in ("+selectedDeptIdStr+") "
				+ " group by srcuserid , cdrdirection "
				+ " order by srcuserid , cdrdirection ";

		List<Object[]> ioHoldBgLogRsLs = callStatisticOverviewService.getByNativeSql(ioHoldBgLogSql);
		for(Object[] objs : ioHoldBgLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				
				Integer cdrdirection = (Integer) objs[1];
				Object[] ioDialDtInfos = buildDateStr(buildDurationParams(objs[2]+""));
				Long ioDialDt = (Long) ioDialDtInfos[0];
				String ioDialDtStr = (String) ioDialDtInfos[1];
				Object[] ioBgDtInfos = buildDateStr(buildDurationParams(objs[3]+""));
				Long ioBgDt = (Long) ioBgDtInfos[0];
				String ioBgDtStr = (String) ioBgDtInfos[1];
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					if(cdrdirection == 0) {					// 内部呼叫
						kpiVo.setInnerDialDt(ioDialDt);
						kpiVo.setInnerDialDtStr(ioDialDtStr);
						kpiVo.setInnerBgDt(ioBgDt);
						kpiVo.setInnerBgDtStr(ioBgDtStr);
					} else if(cdrdirection == 1) {			// 呼出
						kpiVo.setOutDialDt(ioDialDt);
						kpiVo.setOutDialDtStr(ioDialDtStr);
						kpiVo.setOutBgDt(ioBgDt);
						kpiVo.setOutBgDtStr(ioBgDtStr);
					}
				}
			}
		}
	}

	/**
	 * @Description 描述：根据查询时间段，统计内部话务详情	
	 * 		统计   [ 内部呼叫数,   内部接通数,   最大内部呼叫时长]
	 *			
	 * @author  JRH
	 * @date    2014年5月8日 下午5:36:08
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiInnerDialInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String innerDialLogSql = "select "
				+ " srcuserid, "
				+ " count(*) as innerDialCt, "
				+ " sum(case when isbridged is true then 1 else 0 end) as innerBgCt, "
				+ " max(ec2_billableseconds) as innerMaxBgDt "
				+ " from cdr "
				+ " where starttimedate >='" + start_time_str + "' and starttimedate < '" + finish_time_str + "' and cdrdirection = 0 and srcuserid is not null and domainid = " +domain.getId()+ " and srcdeptid in ("+selectedDeptIdStr+") "
				+ " group by srcuserid "
				+ " order by srcuserid ";
		
		List<Object[]> innerDialLogRsLs = callStatisticOverviewService.getByNativeSql(innerDialLogSql);
		for(Object[] objs : innerDialLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				
				Long innerDialCt = (Long) objs[1];
				Long innerBgCt = (Long) objs[2];
				Long innerMaxBgDt = Long.parseLong(objs[3].toString());			
				String innerMaxBgDtStr = DateUtil.getTime(innerMaxBgDt);
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					kpiVo.setInnerDialCt(innerDialCt);
					kpiVo.setInnerBgCt(innerBgCt);
					kpiVo.setInnerMaxBgDt(innerMaxBgDt);
					kpiVo.setInnerMaxBgDtStr(innerMaxBgDtStr);
				}
			}
		}
	}

	/**
	 * @Description 描述：根据查询时间段，统计呼入话务详情 
	 * 		统计   [ 呼入总数,   呼入接通数,   最大呼入通话时长,   呼入振铃时长,   呼入小于zz秒接通次数,  呼入大于ww秒接通次数,   服务水平内接通数    ]
	 *			
	 * @author  JRH
	 * @date    2014年5月8日 下午5:43:08
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiInDialInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String inDialLogSql = "select "
				+ " destuserid, "
				+ " count(*) as inDialCt, "
				+ " sum(case when isbridged is true then 1 else 0 end) as inBgCt, "
				+ " max(ec2_billableseconds) as inMaxBgDt, "
				+ " sum(ringduration) as inRingDt, "
				+ " sum(case when (isbridged is true and ec2_billableseconds <= " + SPECIFY_IN_BG_LOW_SEC + " ) then 1 else 0 end) as inBgLowSecCt, "
				+ " sum(case when (isbridged is true and ec2_billableseconds <= " + SPECIFY_IN_BG_LOW_SEC2 + " ) then 1 else 0 end) as inBgLowSecCt2, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC + " ) then 1 else 0 end) as inBgLargeSecCt, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC2 + " ) then 1 else 0 end) as inBgLargeSecCt2, "
				+ " sum(case when (isbridged is true and ringduration <= " + SPECIFY_IN_BG_LEVEL_SEC + " ) then 1 else 0 end) as inBgLevelCt, "
				+ " sum(case when (isbridged is true and ringduration <= " + SPECIFY_IN_BG_LEVEL_SEC2 + " ) then 1 else 0 end) as inBgLevelCt2, "
				+ " sum(case when (isbridged is true and ec2_billableseconds <= " + SPECIFY_IN_BG_LOW_SEC3 + " ) then 1 else 0 end) as inBgLowSecCt1, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC1 + " ) then 1 else 0 end) as inBgLargeSecCt1, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC3 + " ) then 1 else 0 end) as inBgLargeSecCt3, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC4 + " ) then 1 else 0 end) as inBgLargeSecCt4, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC5 + " ) then 1 else 0 end) as inBgLargeSecCt5, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC6 + " ) then 1 else 0 end) as inBgLargeSecCt6, "
				+ " sum(case when (ec2_billableseconds > " + SPECIFY_IN_BG_LARGE_SEC7 + " ) then 1 else 0 end) as inBgLargeSecCt7  "
				+ " from cdr "
				+ " where starttimedate >='" + start_time_str + "' and starttimedate < '" + finish_time_str + "' and cdrdirection = 2 and destuserid is not null and domainid = " +domain.getId()+ " and destdeptid in ("+selectedDeptIdStr+") "
				+ " group by destuserid "
				+ " order by destuserid ";

		List<Object[]> inDialLogRsLs = callStatisticOverviewService.getByNativeSql(inDialLogSql);
		
		for(Object[] objs : inDialLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				
				Long inDialCt = (Long) objs[1];
				Long inBgCt = (Long) objs[2];
				Long inMaxBgDt = Long.parseLong(objs[3].toString());			
				String inMaxBgDtStr = DateUtil.getTime(inMaxBgDt);
				Long inRingDt = (Long) objs[4];
				String inRingDtStr = (String) DateUtil.getTime(inRingDt);
				Long inBgLowSecCt = (Long) objs[5]; 
				Long inBgLowSecCt2 = (Long) objs[6]; 
				Long inBgLargeSecCt = (Long) objs[7]; 
				Long inBgLargeSecCt2 = (Long) objs[8]; 
				Long inBgLevelCt = (Long) objs[9]; 
				Long inBgLevelCt2 = (Long) objs[10]; 
				Long inBgLowSecCt1 = (Long) objs[11]; 
				Long inBgLargeSecCt1 = (Long) objs[12]; 
				Long inBgLargeSecCt3 = (Long) objs[13]; 
				Long inBgLargeSecCt4 = (Long) objs[14]; 
				Long inBgLargeSecCt5 = (Long) objs[15]; 
				Long inBgLargeSecCt6 = (Long) objs[16]; 
				Long inBgLargeSecCt7 = (Long) objs[17]; 
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					kpiVo.setInDialCt(inDialCt);
					kpiVo.setInBgCt(inBgCt);
					kpiVo.setInMaxBgDt(inMaxBgDt);
					kpiVo.setInMaxBgDtStr(inMaxBgDtStr);
					kpiVo.setInRingDt(inRingDt);
					kpiVo.setInRingDtStr(inRingDtStr);
					kpiVo.setInBgLowSecCt(inBgLowSecCt);
					kpiVo.setInBgLowSecCt2(inBgLowSecCt2);
					kpiVo.setInBgLargeSecCt(inBgLargeSecCt);
					kpiVo.setInBgLargeSecCt2(inBgLargeSecCt2);
					kpiVo.setInBgLevelCt(inBgLevelCt);
					kpiVo.setInBgLevelCt2(inBgLevelCt2);
					kpiVo.setInBgLowSecCt1(inBgLowSecCt1);
					kpiVo.setInBgLargeSecCt1(inBgLargeSecCt1);
					kpiVo.setInBgLargeSecCt3(inBgLargeSecCt3);
					kpiVo.setInBgLargeSecCt4(inBgLargeSecCt4);
					kpiVo.setInBgLargeSecCt5(inBgLargeSecCt5);
					kpiVo.setInBgLargeSecCt6(inBgLargeSecCt6);
					kpiVo.setInBgLargeSecCt7(inBgLargeSecCt7);
				}
			}
		}
	}

	/**
	 * @Description 描述：根据查询时间段，统计   [ 呼入总时长,   呼入接通时长 ]
	 *			
	 * @author  JRH
	 * @date    2014年5月8日 下午6:28:08
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiInDialDtBgDtInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String inDtBgLogSql = "select "
				+ " destuserid, "
				+ " sum( (case when (endtimedate >= '" + finish_time_str + "') then '" + finish_time_str + "' else endtimedate end) - (case when starttimedate < '" + start_time_str + "' then '" + start_time_str + "' else starttimedate end) ) as inDialDt, "
				+ " sum( (case when (endtimedate >= '" + finish_time_str + "') then '" + finish_time_str + "' else endtimedate end) - (case when bridgetimedate < '" + start_time_str + "' then '" + start_time_str + "' else bridgetimedate end) ) as inBgDt "
				+ " from cdr "
				+ " where starttimedate < '" + finish_time_str + "' and endtimedate >= '" + start_time_str + "' and cdrdirection = 2 and destuserid is not null and domainid = " +domain.getId()+ " and destdeptid in ("+selectedDeptIdStr+") "
				+ " group by destuserid "
				+ " order by destuserid ";

		List<Object[]> inDtBgLogRsLs = callStatisticOverviewService.getByNativeSql(inDtBgLogSql);
		for(Object[] objs : inDtBgLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				
				Object[] inDialDtInfos = buildDateStr(buildDurationParams(objs[1]+""));
				Long inDialDt = (Long) inDialDtInfos[0];
				String inDialDtStr = (String) inDialDtInfos[1];
				Object[] inBgDtInfos = buildDateStr(buildDurationParams(objs[2]+""));
				Long inBgDt = (Long) inBgDtInfos[0];
				String inBgDtStr = (String) inBgDtInfos[1];
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					kpiVo.setInDialDt(inDialDt);
					kpiVo.setInDialDtStr(inDialDtStr);
					kpiVo.setInBgDt(inBgDt);
					kpiVo.setInBgDtStr(inBgDtStr);
				}
			}
		}
	}
	
	/**
	 * @Description 描述：根据查询时间段，统计   [ 呼入个人漏接电话数量 ]
	 *			
	 * @author  JRH
	 * @date    2014年5月8日 下午6:28:08
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiInMissCtInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String inMissCtLogSql = "select "
				+ " destuserid, "
				+ " count(*) "
				+ " from ec2_miss_call_log "
				+ " where ringingstatetime < '" + finish_time_str + "' and ringingstatetime >= '" + start_time_str + "' and destuserid is not null and domainid = " +domain.getId()+ " and destuserdeptid in ("+selectedDeptIdStr+") "
				+ " group by destuserid "
				+ " order by destuserid ";
		
		List<Object[]> inMissCtLogRsLs = callStatisticOverviewService.getByNativeSql(inMissCtLogSql);
		for(Object[] objs : inMissCtLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个
				
				Long inMissCt = (Long) objs[1];
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					kpiVo.setInMissCt(inMissCt);
				}
			}
		}
	}
	
	/**
	 * @Description 描述：根据查询时间段，统计呼入/呼出 话后处理信息 	统计   [ 呼入/呼出 话后处理次数,   呼入/呼出 话后处理时长    ]
	 *			
	 * @author  JRH
	 * @date    2014年5月8日 下午6:14:08
	 * @param finishTime        截止查询时间
	 * @param start_time_str 	格式化后的单次开始时间字符串
	 * @param finish_time_str 	格式化后的单次截止时间字符串
	 */
	private void buildAdvKpiCallAfterInfos(Date startTime, String start_time_str, String finish_time_str) {
		
		String callAfterLogSql = "select "
				+ " userid, "
				+ " direction, "
				+ " sum(case when (startdate >= '" + start_time_str + "' and startdate < '" + finish_time_str + "') then 1 else 0 end) as afterCt, "
				+ " sum(case when finishdate is null "
					+ "	then "
						+ " ( (case when (now() >= '" + finish_time_str + "') then '" + finish_time_str + "' else now() end) - (case when startdate >= '" + start_time_str + "' then startdate else '" + start_time_str + "' end) ) "
					+ " else "
						+ " ( (case when (finishdate >= '" + finish_time_str + "') then '" + finish_time_str + "' else finishdate end) - (case when startdate >= '" + start_time_str + "' then startdate else '" + start_time_str + "' end) ) "
					+ " end) as afterDt "
				+ " from ec2_call_after_handle_log "
				+ " where startdate < '" + finish_time_str + "' and (finishdate >= '" + start_time_str + "' or finishdate is null) and domainid = " +domain.getId()+ " and deptid in ("+selectedDeptIdStr+") "
				+ " group by userid, direction "
				+ " order by userid, direction ";

		List<Object[]> callAfterLogRsLs = callStatisticOverviewService.getByNativeSql(callAfterLogSql);
		
		for(Object[] objs : callAfterLogRsLs) {
			Long uid = (Long) objs[0];
			
			if(uid != null) {	// 如果用户编号不存在，则直接处理下一个

				String direction = (String) objs[1];
				Long afterCt = (Long) objs[2];
				Object[] afterDtInfos = buildDateStr(buildDurationParams(objs[3]+""));
				Long afterDt = (Long) afterDtInfos[0];
				String afterDtStr = (String) afterDtInfos[1];
				
				// 维护每个人与其在各时段的统计信息 的对应关系
				HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
				if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
					KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);
					if("outgoing".equals(direction)) {					// 呼出
						kpiVo.setOutAfterCt(afterCt);
						kpiVo.setOutAfterDt(afterDt);
						kpiVo.setOutAfterDtStr(afterDtStr);
					} else if("incoming".equals(direction)) {			// 呼入
						kpiVo.setInAfterCt(afterCt);
						kpiVo.setInAfterDt(afterDt);
						kpiVo.setInAfterDtStr(afterDtStr);
					}
				}
			}
		}
	}
	
	/**
	 * @Description 描述：统计不需要通过SQL来处理的信息
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午4:09:17 void
	 * @param finishTime        截止查询时间
	 * @param startTime 		开始查询时间
	 * @param kpiTableColVo 	哪些统计信息需要考察【基础信息、登陆信息、置忙信息、 话务总统计详情、呼出话务统计详情、内部话务统计详情、呼入话务统计详情】
	 */
	private void buildCalcAdvKpiInfos(Date startTime, Date finishTime, KpiTableColVo kpiTableColVo) {
		for(Long uid : kpiAdvVoMp.keySet()) {
			HashMap<Date, KpiAdvanceVo> date2KpiVoMp = kpiAdvVoMp.get(uid);
			if(date2KpiVoMp != null && date2KpiVoMp.get(startTime) != null) {
				KpiAdvanceVo kpiVo = date2KpiVoMp.get(startTime);

				// 就绪时长、有效工作时长、 有效工作率
				if(kpiTableColVo.isLoginVisible()) {
					kpiVo.calcLoginInfos();
				}
				
				// 置忙总次数、置忙总时长
				if(kpiTableColVo.isPauseVisible()) {
					kpiVo.calcPauseInfos();
				}
				
				// 话务总量、话务接通总量、话务接通总量、话务接通率%、话务总时长、话务接通时长、有效话务率%、每小时话务量
				if(kpiTableColVo.isTotalCallVisible()) {
					kpiVo.calcAllDialInfos();
				}
				
				// 呼出未接通数、呼出接通率%、呼出通话时长过短比例%-30、呼出通话时长过短比例%-50、呼出通话时长过长比例%-300、呼出通话时长过长比例%-500、平均呼出等待时长、呼出平均接通时长、有效呼出时长占有率%、每小时呼出数
				if(kpiTableColVo.isCallOutVisible()) {
					kpiVo.calcOutDialInfos();
				}
				
				// 未内部接通数、内部接通率、每小时内呼数
				if(kpiTableColVo.isCallInnerVisible()) {
					kpiVo.calcInnerDialInfos();
				}
				
				// 呼入未接通数、呼入接通率%、服务水平%-5、服务水平%-10、呼入通话时长过短比例%-5、呼入通话时长过短比例%-10、呼入通话时长过长比例%-300、呼入通话时长过长比例%-500、呼入平均振铃时长、呼入平均接通时长、有效呼入时长占有率%、每小时呼入数
				if(kpiTableColVo.isCallInVisible()) {
					kpiVo.calcInDialInfos();
				}
			}
		}

	}

	/**
	 * 解析由NativeSql 查出来的时间差，获取其中的天、小时、分钟、秒，对应的值
	 * @param dateInfoStr
	 * @return
	 */
	private Integer[] buildDurationParams(String dateInfoStr) {
		Integer[] params = new Integer[]{0,0,0,0};
		String[] dts = dateInfoStr.split(" ");
		if(dts != null && dts.length > 10) {
			dts[10] = dts[10].substring(0, dts[10].indexOf("."));
			params[0] = Integer.parseInt(dts[4]);
			params[1] = Integer.parseInt(dts[6]);
			params[2] = Integer.parseInt(dts[8]);
			params[3] = Integer.parseInt(dts[10]);
		}
		return params;
	}
	
	/**
	 * 根据天、小时、分钟、秒，对应的值，组装成 7天 10:10:00 这样的字符串
	 * @param dateParams
	 * @return Object[] [3600, '01:00:00']
	 */
	private Object[] buildDateStr(Integer[] dateParams) {
		int day = dateParams[0];
		int hour = dateParams[1];
		int minute = dateParams[2];
		int second = dateParams[3];
		
		Long duration_sec_long = (long) (day*24*3600 + hour*3600 + minute*60 + second);
		
		int addMin = second / 60;				// 如果秒数大于60，则算出多余的分钟数
		int addHour = (minute + addMin) / 60;	// 如果分钟大于60，则算出多余的小时数
		int addDay = (hour + addHour) / 24;		// 如果小时大于24，则算出多余的天数
		second = second % 60;
		minute = (minute + addMin) % 60;
		hour = (hour + addHour) % 24;
		day = day + addDay;
		
		String onlineTimeStr = "";
		if(day > 0) {
			onlineTimeStr = day+"天 ";
		} 
		if(hour >= 0 && hour < 10) {
			onlineTimeStr += "0"+hour+":";
		} else {
			onlineTimeStr += hour+":";
		}
		if(minute >= 0 && minute < 10) {
			onlineTimeStr += "0"+minute+":";
		} else {
			onlineTimeStr += minute+":";
		}
		if(second >= 0 && second < 10) {
			onlineTimeStr += "0"+second;
		} else {
			onlineTimeStr += second;
		}
		
		return new Object[]{duration_sec_long, onlineTimeStr};
	}
	
}
