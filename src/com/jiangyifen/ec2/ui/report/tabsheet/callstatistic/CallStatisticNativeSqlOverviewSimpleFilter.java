package com.jiangyifen.ec2.ui.report.tabsheet.callstatistic;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;
import com.jiangyifen.ec2.ui.FlipOverTableUseNativeSql;
import com.jiangyifen.ec2.utils.DateTransformFactory;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 话务统计总概览简单查询组件
 * 
 * @author jrh
 * 
 * 2013-7-30
 */
@SuppressWarnings("serial")
public class CallStatisticNativeSqlOverviewSimpleFilter extends VerticalLayout implements ClickListener, ValueChangeListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat SDF_MON = new SimpleDateFormat("yyyy-MM");
	private final DecimalFormat DF_TWO_PRECISION = new DecimalFormat("0.00");
	private final DecimalFormat DF_FOUR_PRECISION = new DecimalFormat("0.0000");
	
	private final String[] ALL_COL_HEADERS = new String[] {"日期", "小时", "呼叫总数", "接通总数", 
			"25秒内接通数", "25秒内放弃数", "总通时(秒)", "平均通时(秒)", "平均应答速度(秒)", "接通率"};
	
	private final String[] INCOMING_COL_HEADERS = new String[] {"日期", "小时", "呼入总数", "呼入接通总数", 
			"坐席25秒内接通数", "客户25秒内放弃数", "呼入总通时(秒)", "呼入平均通时(秒)", "呼入平均应答速度(秒)", "呼入接通率", "呼入服务水平"};

	private final String[] OUTGOING_COL_HEADERS = new String[] {"日期", "小时", "呼出总数", "呼出接通总数", 
			"客户25秒内接通数", "坐席25秒内放弃数", "呼出总通时(秒)", "呼出平均通时(秒)", "呼出平均应答速度(秒)", "呼出接通率"};

	@SuppressWarnings("rawtypes")
	private final Class[] HEADERS_CLASS_BY_DAYHOUR = new Class[] {String.class, Integer.class, Long.class, Long.class, 
		Long.class, Long.class, Long.class, Double.class, Double.class, Double.class, Double.class, Double.class};

	//---------------------------------------------------------------------------------------------------------------------------------------- //
	//---------------------------------------------------------------------------------------------------------------------------------------- //
	
	private final String[] ALL_COL_HEADERS_BY_DAY = new String[] {"日期", "呼叫总数", "接通总数", 
			"25秒内接通数", "25秒内放弃数", "总通时(秒)", "平均通时(秒)", "平均应答速度(秒)", "接通率"};
	
	private final String[] INCOMING_COL_HEADERS_BY_DAY = new String[] {"日期", "呼入总数", "呼入接通总数", 
			"坐席25秒内接通数", "客户25秒内放弃数", "呼入总通时(秒)", "呼入平均通时(秒)", "呼入平均应答速度(秒)", "呼入接通率", "呼入服务水平"};

	private final String[] OUTGOING_COL_HEADERS_BY_DAY = new String[] {"日期", "呼出总数", "呼出接通总数", 
			"客户25秒内接通数", "坐席25秒内放弃数", "呼出总通时(秒)", "呼出平均通时(秒)", "呼出平均应答速度(秒)", "呼出接通率"};

	@SuppressWarnings("rawtypes")
	private final Class[] HEADERS_CLASS_BY_DAY = new Class[] {String.class, Long.class, Long.class,  
		Long.class, Long.class, Long.class, Double.class, Double.class, Double.class, Double.class, Double.class};

	//---------------------------------------------------------------------------------------------------------------------------------------- //
	//---------------------------------------------------------------------------------------------------------------------------------------- //

	private final String[] ALL_COL_HEADERS_BY_MON = new String[] {"年月", "呼叫总数", "接通总数", 
			"25秒内接通数", "25秒内放弃数", "总通时(秒)", "平均通时(秒)", "平均应答速度(秒)", "接通率"};

	private final String[] INCOMING_COL_HEADERS_BY_MON = new String[] {"年月", "呼入总数", "呼入接通总数", 
			"坐席25秒内接通数", "客户25秒内放弃数", "呼入总通时(秒)", "呼入平均通时(秒)", "呼入平均应答速度(秒)", "呼入接通率", "呼入服务水平"};

	private final String[] OUTGOING_COL_HEADERS_BY_MON = new String[] {"年月", "呼出总数", "呼出接通总数", 
			"客户25秒内接通数", "坐席25秒内放弃数", "呼出总通时(秒)", "呼出平均通时(秒)", "呼出平均应答速度(秒)", "呼出接通率"};

	@SuppressWarnings("rawtypes")
	private final Class[] HEADERS_CLASS_BY_MON = new Class[] {String.class, Long.class, Long.class,  
		Long.class, Long.class, Long.class, Double.class, Double.class, Double.class, Double.class, Double.class};

	private Table csorTable;
	private GridLayout gridLayout;			// 面板中的布局管理器

	//--------------  第一行  --------------// 
	private ComboBox timeScope_cb;						// “日期范围”选择框
	private PopupDateField startTime_pdf;				// “开始时间”选择框
	private PopupDateField finishTime_pdf;				// “截止时间”选择框
	private ComboBox callDirection_cb;					// "呼叫方向"
	private ComboBox statisticType_cb;					// "统计方式"按天、时；按天；按月

	private Button search_bt;							// 刷新结果按钮
	private Button clear_bt;							// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private Domain domain;								// 当前用户所属域
	private Object[] subtotalDstObjs;					// 根据当前查询条件，做的一个综合统计结果
	private String callDirection = "all";				// 查看报表记录的呼叫方向[全部、呼入、呼出]
	private String statisticType = "byDayHour";			// 查看报表记录的统计方式[按天、时；按天；按月]
	private String specificStartTimeSql = "";			// 报表的查询日期的 起始点
	private String specificEndTimeSql = "";						// 报表的查询日期的 终止点

	private FlipOverTableUseNativeSql csorTableFlip;		// 话务统计总概览管理Tab 页的翻页组件
	
	private CallStatisticOverviewService callStatisticOverviewService;
	
	public CallStatisticNativeSqlOverviewSimpleFilter(Table csorTable) {
		this.setSpacing(true);
		this.csorTable = csorTable;

		domain = SpringContextHolder.getDomain();
		callStatisticOverviewService = SpringContextHolder.getBean("callStatisticOverviewService");
		
		gridLayout = new GridLayout(6, 1);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		//--------- 第一行  -----------//
		this.createImportTimeScopeHLayout();
		this.createStartImportTimeHLayout();
		this.createFinishImportTimeHLayout();
		this.createDirectionHLayout();
		this.createViewTypeHLayout();
		
		//--------- 创建操作按钮 -----------//
		this.createOperateButtons();
	}
	
	/**
	 * 创建  存放“原始数据时间范围标签和其选择框” 的布局管理器
	 */
	private void createImportTimeScopeHLayout() {
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
		timeScope_cb.setWidth("80px");
		timeScope_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		timeScope_cb.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(timeScope_cb);
		
		timeScopeListener = new ValueChangeListener() {
			@Override
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
	private void createStartImportTimeHLayout() {
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
		startTime_pdf.setWidth("153px");
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
	private void createFinishImportTimeHLayout() {
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
				timeScope_cb.removeListener(finishTimeListener);
				timeScope_cb.setValue("精确时间");
				timeScope_cb.addListener(timeScopeListener);
			}
		};
		
		finishTime_pdf = new PopupDateField();
		finishTime_pdf.setImmediate(true);
		finishTime_pdf.setWidth("154px");
		finishTime_pdf.setValue(dates[1]);
		finishTime_pdf.addListener(finishTimeListener);
		finishTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime_pdf.setParseErrorMessage("时间格式不合法");
		finishTime_pdf.setValidationVisible(false);
		finishTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
		finishTimeHLayout.addComponent(finishTime_pdf);
	}
	
	private void createDirectionHLayout() {
		HorizontalLayout directionHLayout = new HorizontalLayout();
		directionHLayout.setSpacing(true);
		gridLayout.addComponent(directionHLayout, 3, 0);
		
		Label directionLabel = new Label("呼叫方向：");
		directionLabel.setWidth("-1px");
		directionHLayout.addComponent(directionLabel);
		
		callDirection_cb = new ComboBox();
		callDirection_cb.setImmediate(true);
		callDirection_cb.addItem("all");
		callDirection_cb.addItem("incoming");
		callDirection_cb.addItem("outgoing");
		callDirection_cb.setItemCaption("all", "全部");
		callDirection_cb.setItemCaption("incoming", "呼入");
		callDirection_cb.setItemCaption("outgoing", "呼出");
		callDirection_cb.setValue("all");
		callDirection_cb.setWidth("80px");
		callDirection_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		callDirection_cb.setNullSelectionAllowed(false);
		directionHLayout.addComponent(callDirection_cb);
	}
	
	private void createViewTypeHLayout() {
		HorizontalLayout viewTypeHLayout = new HorizontalLayout();
		viewTypeHLayout.setSpacing(true);
		gridLayout.addComponent(viewTypeHLayout, 4, 0);
		
		Label viewTypeLabel = new Label("统计方式：");
		viewTypeLabel.setWidth("-1px");
		viewTypeHLayout.addComponent(viewTypeLabel);
		
		statisticType_cb = new ComboBox();
		statisticType_cb.addListener(this);
		statisticType_cb.setImmediate(true);
		statisticType_cb.addItem("byDayHour");
		statisticType_cb.addItem("byDay");
		statisticType_cb.addItem("byMonth");
		statisticType_cb.setItemCaption("byDayHour", "按天、时统计");
		statisticType_cb.setItemCaption("byDay", "按天");
		statisticType_cb.setItemCaption("byMonth", "按月");
		statisticType_cb.setValue("byDayHour");
		statisticType_cb.setWidth("100px");
		statisticType_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		statisticType_cb.setNullSelectionAllowed(false);
		viewTypeHLayout.addComponent(statisticType_cb);
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 5, 0);
		
		search_bt = new Button("查 询", this);
		search_bt.setDescription("快捷键(Ctrl+Q)");
		search_bt.addStyleName("default");
		search_bt.setWidth("60px");
		search_bt.setClickShortcut(KeyCode.Q, ModifierKey.CTRL);
		layout.addComponent(search_bt);
		
		clear_bt = new Button("清 空", this);
		clear_bt.setDescription("快捷键(Ctrl+L)");
		clear_bt.setWidth("60px");
		clear_bt.setClickShortcut(KeyCode.L, ModifierKey.CTRL);
		layout.addComponent(clear_bt);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == statisticType_cb) {	// 当查看类型发生变化时，修改时间范围组件的格式化类型
			String type = (String) statisticType_cb.getValue();
			if("byDayHour".equals(type)) {
				startTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
				startTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
				finishTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
				finishTime_pdf.setResolution(PopupDateField.RESOLUTION_HOUR);
			} else if("byDay".equals(type)) {
				startTime_pdf.setDateFormat("yyyy-MM-dd");
				startTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
				finishTime_pdf.setDateFormat("yyyy-MM-dd");
				finishTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
			} else if("byMonth".equals(type)) {
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
			boolean validValue = comfirmValues();
			if(validValue) {	// 数据校验成功
				try {
					handleSearchEvent();
				} catch (Exception e) {
					e.printStackTrace();
					logger.info(csorTableFlip.getSearchSql());
					logger.info(csorTableFlip.getCountSql());
					logger.error("jrh 话务考核总概览报表查询时出现异常----->"+e.getMessage(), e);
				}
			}
		} else if(source == clear_bt) {
			excuteClearValues();
		} 
	}

	/**
	 * 校验输入信息
	 * @return
	 */
	private boolean comfirmValues() {
		Date startTime = (Date) startTime_pdf.getValue();
		Date endTime = (Date) finishTime_pdf.getValue();
		
		if(startTime == null || endTime == null) {
			startTime_pdf.getApplication().getMainWindow().showNotification("开始时间和截止时间 都不能为空, 并且格式要正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} else if(startTime.after(endTime)) {
			startTime_pdf.getApplication().getMainWindow().showNotification("开始时间不得大于截止时间！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		return true;
	}
	
	/**
	 * 执行清空操作
	 */
	private void excuteClearValues() {
		timeScope_cb.select("今天");
		callDirection_cb.setValue("all");
		statisticType_cb.setValue("byDayHour");
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String nativeSearchSql = "";
		subtotalDstObjs = null;			// 每次点击查询按钮，需要将总计信息置为null, 为了避免重复添加到表格中
		callDirection = (String) callDirection_cb.getValue();
		statisticType = (String) statisticType_cb.getValue();

		// 根据统计类型，来决定要创建的数组subtotalDstObjs 的长度,以及查询用的Native SQL 值
		if("all".equals(callDirection)) {
			if("byDayHour".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM-dd') as startDate_dh, hour, callAmount, bridgedAmount, pickupInSpecifiedSec, abandonInSpecifiedSec," 
						 + " totalCallTime, avgCallTime, avgAnswerTime, bridgedRate";
			} else if("byDay".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM-dd') as startDate_d, sum(callAmount), sum(bridgedAmount)," +
						" sum(pickupInSpecifiedSec), sum(abandonInSpecifiedSec), sum(totalCallTime), sum(totalAnswerTime)";
			} else if("byMonth".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM') as startDate_m, sum(callAmount), sum(bridgedAmount)," +
						" sum(pickupInSpecifiedSec), sum(abandonInSpecifiedSec), sum(totalCallTime), sum(totalAnswerTime)";
			}
		} else if("incoming".equals(callDirection)) {
			if("byDayHour".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM-dd') as startDate_dh, hour, callInAmount, bridgedInAmount, csrPickupInSpecifiedSec, customerAbandonInSpecifiedSec," +
						" totalInCallTime, avgInCallTime, avgInAnswerTime, bridgedInRate, incomingServiceLevel";
			} else if("byDay".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM-dd') as startDate_d, sum(callInAmount),sum(bridgedInAmount), sum(csrPickupInSpecifiedSec)," +
						" sum(customerAbandonInSpecifiedSec), sum(totalInCallTime), sum(totalInAnswerTime)";
			} else if("byMonth".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM') as startDate_m, sum(callInAmount), sum(bridgedInAmount)," +
						" sum(csrPickupInSpecifiedSec), sum(customerAbandonInSpecifiedSec), sum(totalInCallTime), sum(totalInAnswerTime)";;
			}
		} else if("outgoing".equals(callDirection)) {
			if("byDayHour".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM-dd') as startDate_dh, hour, callOutAmount, bridgedOutAmount, customerPickupInSpecifiedSec, csrAbandonInSpecifiedSec," +
						" totalOutCallTime, avgOutCallTime, avgOutAnswerTime, bridgedOutRate";
			} else if("byDay".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM-dd') as startDate_d, sum(callOutAmount), sum(bridgedOutAmount)," +
						" sum(customerPickupInSpecifiedSec), sum(csrAbandonInSpecifiedSec), sum(totalOutCallTime), sum(totalOutAnswerTime)";
			} else if("byMonth".equals(statisticType)) {
				nativeSearchSql = "select to_char(startDate, 'yyyy-MM') as startDate_m, sum(callOutAmount), sum(bridgedOutAmount)," +
						" sum(customerPickupInSpecifiedSec), sum(csrAbandonInSpecifiedSec), sum(totalOutCallTime), sum(totalOutAnswerTime)";
			}
		}

		String dynamicSql = createDynamicSql();			// 获取动态变化的Sql
		String groupOrderBySql = "";					// 根据统计方式设定 排序方案、分组方案
		String nativeCountSql = "";						// 统计共有多少个结果
		
		if("byDayHour".equals(statisticType)) {
			groupOrderBySql = " order by startDate_dh asc, hour asc";
			nativeCountSql = "select count(*) from ec2_report_call_statistic_overview as e where domainId = " + domain.getId()+dynamicSql;
		} else if("byDay".equals(statisticType)) {									// 如果统计方式时按天，则查询共有多少行需要用嵌套查询
			groupOrderBySql = " group by startDate_d order by startDate_d asc";
			nativeCountSql = "select count(*) from (select to_char(startDate, 'yyyy-MM-dd') as startDate_d" +
					" from ec2_report_call_statistic_overview where domainId = " + domain.getId()+dynamicSql+" group by startDate_d) as t_d";
		} else if("byMonth".equals(statisticType)) {								// 如果统计方式时按天，则查询共有多少行需要用嵌套查询
			groupOrderBySql = " group by startDate_m order by startDate_m asc";
			nativeCountSql = "select count(*) from (select to_char(startDate, 'yyyy-MM') as startDate_m" +
					" from ec2_report_call_statistic_overview where domainId = " + domain.getId()+dynamicSql+" group by startDate_m) as t_m";
		}
		nativeSearchSql += " from ec2_report_call_statistic_overview where domainId = " + domain.getId()+ dynamicSql + groupOrderBySql;
		if(csorTableFlip == null) {
			csorTable.setPageLength(25);
			csorTableFlip = new FlipOverTableUseNativeSql(callStatisticOverviewService, csorTable, 24, nativeSearchSql, nativeCountSql, this, true);
		} else {
			csorTableFlip.setSearchSql(nativeSearchSql);
			csorTableFlip.setCountSql(nativeCountSql);
		}
		
		csorTableFlip.refreshToFirstPage();	// 刷新到首页

		// 计算统计信息
		this.statisticSubtotalInfo(specificStartTimeSql, specificEndTimeSql);
		csorTable.addItem(subtotalDstObjs, subtotalDstObjs);	// 绑定小计数据绑定到表格中的最后一行
	}
	
	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句，并且统计按当前时间范围下的小计总量
	 * @return
	 */
	private String createDynamicSql() {
		
		String dateFormat = "yyyy-MM-dd";
		DateTransformFactory dtf = new DateTransformFactory();
		Date startTime = (Date) startTime_pdf.getValue();
		String startDayStr = SDF_DAY.format(startTime);
		String startDayAfter = dtf.getSpecifiedDayAfter(startDayStr, dateFormat);
		String startMonthStr = SDF_MON.format(startTime);
		
		Date endTime = (Date) finishTime_pdf.getValue();
		String endDayStr = SDF_DAY.format(endTime);
		String endDayStrAfter = dtf.getSpecifiedDayAfter(endDayStr, dateFormat);
		
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startTime);
		int startHour = startCal.get(Calendar.HOUR_OF_DAY);
		
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endTime);
		int endYear = endCal.get(Calendar.YEAR);
		int endMonth = endCal.get(Calendar.MONTH) + 2;		// 月份在Calendar 中的下标示以 0 开始的，所以往后推一个月，这里需要加2，才能显示真正的月份值
		int endHour = endCal.get(Calendar.HOUR_OF_DAY);
		
		// 根据不同的查看方式，创建按时间范围的查询条件
		/*if("byDayHour".equals(statisticType)) {
			specificStartTimeSql = " and ((to_char(startDate, 'yyyy-MM-dd') = '" + startDayStr + "' and hour >= " +startHour + ") or to_char(startDate, 'yyyy-MM-dd') > '" + startDayStr + "')";
			specificEndTimeSql = " and ((to_char(startDate, 'yyyy-MM-dd') = '" + endDayStr + "' and hour < " +endHour + ") or to_char(startDate, 'yyyy-MM-dd') < '" + endDayStr + "')";
		} else if("byDay".equals(statisticType)) {
			specificStartTimeSql = " and to_char(startDate, 'yyyy-MM-dd') >= '" + startDayStr + "'";
			specificEndTimeSql = " and to_char(startDate, 'yyyy-MM-dd') <= '" + endDayStr + "'";
		} else if("byMonth".equals(statisticType)) {
			specificStartTimeSql = " and to_char(startDate, 'yyyy-MM-dd') >= '" + startMonthStr + "-01'";
			specificEndTimeSql = " and to_char(startDate, 'yyyy-MM-dd') <= '" +endYear+"-"+ endMonth + "-01'";
		}*/
		
		StringBuffer sbStart = new StringBuffer();
		StringBuffer sbEnd = new StringBuffer();
		if("byDayHour".equals(statisticType)) {
			sbStart.append(" and (startDate >= '");
			sbStart.append(startDayStr);
			sbStart.append("' and startDate < '");
			sbStart.append(startDayAfter);
			sbStart.append("' and hour >= ");
			sbStart.append(startHour);
			sbStart.append(" or startDate > '");
			sbStart.append(startDayStr);
			sbStart.append("')");
			specificStartTimeSql = sbStart.toString();
			sbEnd.append(" and (startDate >= '");
			sbEnd.append(endDayStr);
			sbEnd.append("' and startDate < '");
			sbEnd.append(endDayStrAfter);
			sbEnd.append("' and hour < ");
			sbEnd.append(endHour);
			sbEnd.append(" or startDate < '");
			sbEnd.append(endDayStr);
			sbEnd.append("')");
			specificEndTimeSql =   sbEnd.toString();
		} else if("byDay".equals(statisticType)) {
			sbStart.append(" and startDate >= '");
			sbStart.append(startDayStr);
			sbStart.append("'");
			specificStartTimeSql =  sbStart.toString();
			sbEnd.append(" and startDate <= '");
			sbEnd.append(endDayStr);
			sbEnd.append("'");
			specificEndTimeSql = sbEnd.toString();
		} else if("byMonth".equals(statisticType)) {
			sbStart.append(" and startDate >= '");
			sbStart.append(startMonthStr);
			sbStart.append("-01'");
			specificStartTimeSql =  sbStart.toString();
			sbEnd.append(" and startDate <= '");
			sbEnd.append(endYear);
			sbEnd.append("-");
			sbEnd.append(endMonth);
			sbEnd.append("-01'");
			specificEndTimeSql = sbEnd.toString();
		}
		dtf = null;
		// 创建固定的搜索语句
		return specificStartTimeSql + specificEndTimeSql;
	}

	/**
	 * 根据起始和终止查询时间，计算总计信息
	 */
	private void statisticSubtotalInfo(String specificStartTimeSql, String specificEndTimeSql) {
		String nativeSubtotalSql = "";
		if("all".equals(callDirection)) {
			nativeSubtotalSql = "select sum(callamount),sum(bridgedamount),sum(pickupinspecifiedsec),sum(abandoninspecifiedsec),sum(totalcalltime),sum(totalanswertime)" +
					" from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;
		} else if("incoming".equals(callDirection)) {
			/*nativeSubtotalSql = "select sum(callinamount),sum(bridgedinamount),sum(csrpickupinspecifiedsec),sum(csrabandoninspecifiedsec),sum(totalincalltime),sum(totalinanswertime)" +
					" from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;*/
			nativeSubtotalSql = "select sum(callinamount),sum(bridgedinamount),sum(csrpickupinspecifiedsec),sum(customerAbandonInSpecifiedSec),sum(totalincalltime),sum(totalinanswertime)" +
					" from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;
		} else if("outgoing".equals(callDirection)) {
			/*nativeSubtotalSql = "select sum(calloutamount),sum(bridgedoutamount),sum(customerpickupinspecifiedsec),sum(customerabandoninspecifiedsec),sum(totaloutcalltime),sum(totaloutanswertime)" +
					" from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;*/
			nativeSubtotalSql = "select sum(calloutamount),sum(bridgedoutamount),sum(customerpickupinspecifiedsec),sum(csrAbandonInSpecifiedSec),sum(totaloutcalltime),sum(totaloutanswertime)" +
					" from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;
		}
		buildSubtotalInfoToTable(nativeSubtotalSql);
	}

	/**
	 * 根据当前的查询条件，计算一个在当前条件下的统计值
	 * @param nativeSubtotalSql	查询语句
	 */
	private void buildSubtotalInfoToTable(String nativeSubtotalSql) {
		List<Object[]> subtotals = callStatisticOverviewService.getByNativeSql(nativeSubtotalSql);

		subtotalDstObjs = new Object[csorTable.getColumnHeaders().length];	// 指定要新创建的数组的长度 = 表格显示的列数
		if(subtotals.size() > 0) {
			Object[] sourceObjs = subtotals.get(0);
			for(int i = 0; i < sourceObjs.length; i++) {
				if(sourceObjs[i] == null) {
					sourceObjs[i] = 0;
				}
			}

			long callAmount = Long.valueOf(sourceObjs[0].toString());
			long bridgedAmount = Long.valueOf(sourceObjs[1].toString());
			long pickupInSpecifiedsec = Long.valueOf(sourceObjs[2].toString());
			long abandonInSpecifiedsec = Long.valueOf(sourceObjs[3].toString());
			long totalCallTime = Long.valueOf(sourceObjs[4].toString());
			long totalAnswerTime = Long.valueOf(sourceObjs[5].toString());

			Double avgCallTime = (callAmount > 0) ? ((totalCallTime+0.0) / callAmount) : 0.00;
			Double avgAnswerTime = (bridgedAmount > 0) ? ((totalAnswerTime+0.0) / bridgedAmount) : 0.00;
			Double bridgedRate = (callAmount > 0) ? ((bridgedAmount+0.0) / callAmount) : 0.00;

			int index = 1;	// 总计数据在 中的起始插入点，默认为从第二个位置开始插入，因为第一个位置一定是描述时间的
			if("byDayHour".equals(statisticType)) {	// 如果查看方式为“按天、时查看”，则从数组中的第三个位置开始插入，因为前两个分别为“日期”、“小时”
				index = 2;
			}
			subtotalDstObjs[0] = "小计：";
			subtotalDstObjs[index++] = callAmount;
			subtotalDstObjs[index++] = bridgedAmount;
			subtotalDstObjs[index++] = pickupInSpecifiedsec;
			subtotalDstObjs[index++] = abandonInSpecifiedsec;
			subtotalDstObjs[index++] = totalCallTime;
			subtotalDstObjs[index++] = Double.valueOf(DF_TWO_PRECISION.format(avgCallTime));
			subtotalDstObjs[index++] = Double.valueOf(DF_TWO_PRECISION.format(avgAnswerTime));
			subtotalDstObjs[index++] = Double.valueOf(DF_FOUR_PRECISION.format(bridgedRate));
			
			if("incoming".equals(callDirection)) {		// 如果是呼入，还需要处理“服务水平”指标
				Double incomingServiceLevel = (callAmount > 0) ? ((pickupInSpecifiedsec+0.0) / callAmount) : 0.0;
				subtotalDstObjs[index++] = Double.valueOf(DF_FOUR_PRECISION.format(incomingServiceLevel));
			}
		}
	}

	/**
	 * 用来供翻页组件回调的方法,将总计信息加入表格
	 * 	由于如果客户查看报表的“呼叫方向”发生变化[全部、呼入、呼出]，或者“统计的类型”发生变化[按天时统计、按天统计、按月统计]，
	 * 	以上变化都会导致表格的列头发生变化，所以需要先从表格中移除列头属性
	 *  然后根据查询获得的结果，根据情况组装成一个Object[]数组，将这个数组作为表格的内容
	 * 
	 * @param oldObjArrlist
	 */
	public void flipOverCallBack(List<Object[]> oldObjArrlist) {
		// 先移除所有的标题
		while(csorTableFlip.getTable().getContainerPropertyIds().iterator().hasNext()) {
			Object pid = csorTableFlip.getTable().getContainerPropertyIds().iterator().next();
			csorTableFlip.getTable().removeContainerProperty(pid);
		}
		
		List<Object[]> rebuildedObjArr =  new ArrayList<Object[]>();
		// 设置table列名
		if("all".equals(callDirection)) {
			if("byDayHour".equals(statisticType)) {
				for(int i = 0; i < ALL_COL_HEADERS.length; i++) {
					csorTableFlip.getTable().addContainerProperty(ALL_COL_HEADERS[i], HEADERS_CLASS_BY_DAYHOUR[i], null);
				}
				rebuildedObjArr.addAll(oldObjArrlist);
			} else if("byDay".equals(statisticType)) {
				for(int i = 0; i < ALL_COL_HEADERS_BY_DAY.length; i++) {
					csorTableFlip.getTable().addContainerProperty(ALL_COL_HEADERS_BY_DAY[i], HEADERS_CLASS_BY_DAY[i], null);
				}
				rebuildedObjArr = rebuildTableSourceObjArr(oldObjArrlist, callDirection);	// 根据原始数据，构建符合表格要求的数据集合
			} else if("byMonth".equals(statisticType)) {
				for(int i = 0; i < ALL_COL_HEADERS_BY_MON.length; i++) {
					csorTableFlip.getTable().addContainerProperty(ALL_COL_HEADERS_BY_MON[i], HEADERS_CLASS_BY_MON[i], null);
				}
				rebuildedObjArr = rebuildTableSourceObjArr(oldObjArrlist, callDirection);	// 根据原始数据，构建符合表格要求的数据集合
			}
		} else if("incoming".equals(callDirection)) {
			if("byDayHour".equals(statisticType)) {
				for(int i = 0; i < INCOMING_COL_HEADERS.length; i++) {
					csorTableFlip.getTable().addContainerProperty(INCOMING_COL_HEADERS[i], HEADERS_CLASS_BY_DAYHOUR[i], null);
				}
				rebuildedObjArr.addAll(oldObjArrlist);
			} else if("byDay".equals(statisticType)) {
				for(int i = 0; i < INCOMING_COL_HEADERS_BY_DAY.length; i++) {
					csorTableFlip.getTable().addContainerProperty(INCOMING_COL_HEADERS_BY_DAY[i], HEADERS_CLASS_BY_DAY[i], null);
				}
				rebuildedObjArr = rebuildTableSourceObjArr(oldObjArrlist, callDirection);	// 根据原始数据，构建符合表格要求的数据集合
			} else if("byMonth".equals(statisticType)) {
				for(int i = 0; i < INCOMING_COL_HEADERS_BY_MON.length; i++) {
					csorTableFlip.getTable().addContainerProperty(INCOMING_COL_HEADERS_BY_MON[i], HEADERS_CLASS_BY_MON[i], null);
				}
				rebuildedObjArr = rebuildTableSourceObjArr(oldObjArrlist, callDirection);	// 根据原始数据，构建符合表格要求的数据集合
			}
		} else if("outgoing".equals(callDirection)) {
			if("byDayHour".equals(statisticType)) {
				for(int i = 0; i < OUTGOING_COL_HEADERS.length; i++) {
					csorTableFlip.getTable().addContainerProperty(OUTGOING_COL_HEADERS[i], HEADERS_CLASS_BY_DAYHOUR[i], null);
				}
				rebuildedObjArr.addAll(oldObjArrlist);
			} else if("byDay".equals(statisticType)) {
				for(int i = 0; i < OUTGOING_COL_HEADERS_BY_DAY.length; i++) {
					csorTableFlip.getTable().addContainerProperty(OUTGOING_COL_HEADERS_BY_DAY[i], HEADERS_CLASS_BY_DAY[i], null);
				}
				rebuildedObjArr = rebuildTableSourceObjArr(oldObjArrlist, callDirection);	// 根据原始数据，构建符合表格要求的数据集合
			} else if("byMonth".equals(statisticType)) {
				for(int i = 0; i < OUTGOING_COL_HEADERS_BY_MON.length; i++) {
					csorTableFlip.getTable().addContainerProperty(OUTGOING_COL_HEADERS_BY_MON[i], HEADERS_CLASS_BY_MON[i], null);
				}
				rebuildedObjArr = rebuildTableSourceObjArr(oldObjArrlist, callDirection);	// 根据原始数据，构建符合表格要求的数据集合
			}
		}
		
		for(Object[] objArr : rebuildedObjArr) {	// 绑定数据到表格中
			csorTable.addItem(objArr, objArr);
		}
		if(subtotalDstObjs != null) {	// 当第一次点击查询按钮时，是在查询完之后才统计总计信息的，所以第一次为null,如果之后点击翻页组件，就不会为空了
			csorTable.addItem(subtotalDstObjs, subtotalDstObjs);	// 绑定小计数据绑定到表格中的最后一行
		}
	}

	/**
	 * 根据从数据库中查询的各Object[]原始数据，以及根据呼叫方向进行相应处理，新建一个新的Object[]的List集合，最终返回处理后的结果
	 * @param oldObjArrlist			从数据库中查询的结果集，object数组的List 集合
	 * @param calldirection			当前查询呼叫方向
	 */
	public List<Object[]> rebuildTableSourceObjArr(List<Object[]> oldObjArrlist, String calldirection) {
		List<Object[]> rebuildedObjArr = new ArrayList<Object[]>();
		for (Object[] sourceObjs : oldObjArrlist) {
			Object[] rebuildedObjs = new Object[csorTable.getColumnHeaders().length];	// 指定要新创建的数组的长度 = 表格显示的列数
			
			int index = 0;
			for(; index < sourceObjs.length - 1; index++) {
				if(sourceObjs[index] == null) {
					sourceObjs[index] = 0;
				}
				rebuildedObjs[index] = sourceObjs[index];
			}
			
			long callAmount = Long.valueOf(sourceObjs[1].toString());
			long bridgedAmount = Long.valueOf(sourceObjs[2].toString());
			long totalCallTime = Long.valueOf(sourceObjs[5].toString());
			long totalAnswerTime = Long.valueOf(sourceObjs[6].toString());
			
			Double avgCallTime = (callAmount > 0) ? ((totalCallTime+0.0) / callAmount) : 0.00;
			Double avgAnswerTime = (bridgedAmount > 0) ? ((totalAnswerTime+0.0) / bridgedAmount) : 0.00;
			Double bridgedRate = (callAmount > 0) ? ((bridgedAmount+0.0) / callAmount) : 0.00;
			
			rebuildedObjs[index++] = Double.valueOf(DF_TWO_PRECISION.format(avgCallTime));
			rebuildedObjs[index++] = Double.valueOf(DF_TWO_PRECISION.format(avgAnswerTime));
			rebuildedObjs[index++] = Double.valueOf(DF_FOUR_PRECISION.format(bridgedRate));
			
			if("incoming".equals(calldirection)) {	// 如果是呼入，还需要处理“服务水平”指标
				long csrPickupInSpecifiedSec = Long.valueOf(sourceObjs[3].toString());
				Double incomingServiceLevel = (callAmount > 0) ? ((csrPickupInSpecifiedSec+0.0) / callAmount) : 0.0;
				rebuildedObjs[index++] = Double.valueOf(DF_FOUR_PRECISION.format(incomingServiceLevel));
			}
			rebuildedObjArr.add(rebuildedObjs);
		}
		return rebuildedObjArr;
	}
	
	/**
	 * 获取翻页组件
	 * @return
	 */
	public FlipOverTableUseNativeSql getCsorTableFlip() {
		return csorTableFlip;
	}
	
	/**
	 * 获取查询按钮
	 * @return
	 */
	public Button getSearch_bt() {
		return search_bt;
	}

	/**
	 * 获取按当前条件查询到的总计信息
	 * @return
	 */
	public Object[] getSubtotalDstObjs() {
		return subtotalDstObjs;
	}

	/**
	 * 获取当前的查看方向
	 * @return
	 */
	public String getCallDirection() {
		return callDirection;
	}

	/**
	 * 统计方式[按天、时；按天；按月]
	 * @return
	 */
	public String getStatisticType() {
		return statisticType;
	}
	
}
