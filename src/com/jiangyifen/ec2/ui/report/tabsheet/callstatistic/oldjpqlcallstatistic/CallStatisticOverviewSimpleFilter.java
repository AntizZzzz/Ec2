package com.jiangyifen.ec2.ui.report.tabsheet.callstatistic.oldjpqlcallstatistic;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.report.entity.CallStatisticOverview;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
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
public class CallStatisticOverviewSimpleFilter extends VerticalLayout implements ClickListener {

	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
	private final SimpleDateFormat SDF_HOUR = new SimpleDateFormat("HH");
	private final DecimalFormat DF_TWO_PRECISION = new DecimalFormat("0.00");
	private final DecimalFormat DF_FOUR_PRECISION = new DecimalFormat("0.0000");

	// 全部-- 按天、时统计
	private final Object[] ALL_VISIBLE_PROPERTIES = new Object[] {"startDate", "hour", "callAmount", "bridgedAmount", 
			"pickupInSpecifiedSec", "abandonInSpecifiedSec", "totalCallTime", "avgCallTime", "avgAnswerTime", "bridgedRate"};
	
	private final String[] ALL_COL_HEADERS = new String[] {"日期", "小时", "呼叫总数", "接通总数", 
			"25秒内接通数", "25秒内放弃数", "总通时(秒)", "平均通时(秒)", "平均应答速度(秒)", "接通率"};
	
	// 呼入-- 按天、时统计
	private final Object[] INCOMING_VISIBLE_PROPERTIES = new Object[] {"startDate", "hour", "callInAmount", "bridgedInAmount", 
			"csrPickupInSpecifiedSec", "customerAbandonInSpecifiedSec", "totalInCallTime", "avgInCallTime", "avgInAnswerTime", "bridgedInRate", "incomingServiceLevel"};
	
	private final String[] INCOMING_COL_HEADERS = new String[] {"日期", "小时", "呼入总数", "呼入接通总数", 
			"坐席25秒内接通数", "客户25秒内放弃数", "呼入总通时(秒)", "呼入平均通时(秒)", "呼入平均应答速度(秒)", "呼入接通率", "呼入服务水平"};
	
	// 呼出-- 按天、时统计
	private final Object[] OUTGOING_VISIBLE_PROPERTIES = new Object[] {"startDate", "hour", "callOutAmount", "bridgedOutAmount", 
			"customerPickupInSpecifiedSec", "csrAbandonInSpecifiedSec", "totalOutCallTime", "avgOutCallTime", "avgOutAnswerTime", "bridgedOutRate"};
	
	private final String[] OUTGOING_COL_HEADERS = new String[] {"日期", "小时", "呼出总数", "呼出接通总数", 
			"客户25秒内接通数", "坐席25秒内放弃数", "呼出总通时(秒)", "呼出平均通时(秒)", "呼出平均应答速度(秒)", "呼出接通率"};
	
	private Table csorTable;
	private GridLayout gridLayout;			// 面板中的布局管理器

	//--------------  第一行  --------------// 
	private ComboBox timeScope_cb;				// “日期范围”选择框
	private PopupDateField startTime_pdf;		// “开始时间”选择框
	private PopupDateField finishTime_pdf;		// “截止时间”选择框
	private ComboBox direction_cb;

	private Button search_bt;							// 刷新结果按钮
	private Button clear_bt;							// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private Domain domain;								// 当前用户所属域
	private String direction = "all";
	private CallStatisticOverview subtotal;

	private FlipOverTableComponent<CallStatisticOverview> csorTableFlip;		// 话务统计总概览管理Tab 页的翻页组件
	
	private CallStatisticOverviewService callStatisticOverviewService;
	
	public CallStatisticOverviewSimpleFilter() {
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		callStatisticOverviewService = SpringContextHolder.getBean("callStatisticOverviewService");
		
		gridLayout = new GridLayout(5, 1);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		//--------- 第一行  -----------//
		this.createImportTimeScopeHLayout();
		this.createStartImportTimeHLayout();
		this.createFinishImportTimeHLayout();
		this.createDirectionHLayout();
		
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
		
		Label timeScopeLabel = new Label("数据时间：");
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
		
		direction_cb = new ComboBox();
		direction_cb.setImmediate(true);
		direction_cb.addItem("all");
		direction_cb.addItem("incoming");
		direction_cb.addItem("outgoing");
		direction_cb.setItemCaption("all", "全部");
		direction_cb.setItemCaption("incoming", "呼入");
		direction_cb.setItemCaption("outgoing", "呼出");
		direction_cb.setValue("all");
		direction_cb.setWidth("100px");
		direction_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		direction_cb.setNullSelectionAllowed(false);
		directionHLayout.addComponent(direction_cb);
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 4, 0);
		
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
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_bt) {
			boolean validValue = comfirmValues();
			if(validValue) {	// 数据校验成功
				handleSearchEvent();
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
		if(startTime_pdf.getValue() == null || finishTime_pdf.getValue() == null) {
			startTime_pdf.getApplication().getMainWindow().showNotification("开始时间和截止时间 都不能为空, 并且格式要正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		return true;
	}
	
	/**
	 * 执行清空操作
	 */
	private void excuteClearValues() {
		timeScope_cb.select("上月");
		direction_cb.setValue("all");
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(e) from CallStatisticOverview as e where e.domainId = " +domain.getId() + createDynamicSql();
		String searchSql = countSql.replaceFirst("count\\(e\\)", "e") + " order by e.startDate desc, e.hour desc";
		if(csorTableFlip == null) {
			csorTableFlip = new FlipOverTableComponent<CallStatisticOverview>(CallStatisticOverview.class, 
					callStatisticOverviewService, csorTable, searchSql , countSql, this);
			csorTable.setPageLength(19);
			csorTableFlip.setPageLength(18, true);
		}
		csorTableFlip.setSearchSql(searchSql);
		csorTableFlip.setCountSql(countSql);
		
		direction = (String) direction_cb.getValue();
		if("all".equals(direction)) {
			csorTableFlip.getTable().setVisibleColumns(ALL_VISIBLE_PROPERTIES);
			csorTableFlip.getTable().setColumnHeaders(ALL_COL_HEADERS);
		} else if("incoming".equals(direction)) {
			csorTableFlip.getTable().setVisibleColumns(INCOMING_VISIBLE_PROPERTIES);
			csorTableFlip.getTable().setColumnHeaders(INCOMING_COL_HEADERS);
		} else if("outgoing".equals(direction)) {
			csorTableFlip.getTable().setVisibleColumns(OUTGOING_VISIBLE_PROPERTIES);
			csorTableFlip.getTable().setColumnHeaders(OUTGOING_COL_HEADERS);
		}
		
		csorTableFlip.refreshToFirstPage();
		csorTable.getContainerDataSource().addItem(subtotal);
	}
	
	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		// 资源的导入日期查询
		Date startTime = (Date) startTime_pdf.getValue();
		String startDate = SDF_SEC.format(startTime);
		String startHour = SDF_HOUR.format(startTime);
		if(startHour.startsWith("0")) {
			startHour = startHour.substring(1);
		}
		
		Date endTime = (Date) finishTime_pdf.getValue();
		String endDate = SDF_SEC.format(endTime);
		String endHour = SDF_HOUR.format(endTime);
		if(endHour.startsWith("0")) {
			endHour = endHour.substring(1);
		}

		String specificStartTimeSql = " and ((e.startDate = '" + startDate + "' and e.hour >= " +startHour + ") or e.startDate > '" + startDate + "') ";
		String specificEndTimeSql = " and ((e.startDate = '" + endDate + "' and e.hour < " +endHour + ") or e.startDate < '" + endDate + "') ";
		
		this.statisticSubtotalInfo(specificStartTimeSql, specificEndTimeSql);
		
		// 创建固定的搜索语句
		return specificStartTimeSql + specificEndTimeSql;
	}

	/**
	 * 更新总计信息
	 */
	private void statisticSubtotalInfo(String specificStartTimeSql, String specificEndTimeSql) {
		direction = (String) direction_cb.getValue();
		if("all".equals(direction)) {
			String nativeSql = "select sum(callamount),sum(bridgedamount),sum(pickupinspecifiedsec),sum(abandoninspecifiedsec),sum(totalcalltime),sum(totalanswertime) " +
					"from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;
			List<Object[]> subtotals = callStatisticOverviewService.getByNativeSql(nativeSql);
			if(subtotals.size() > 0) {
				subtotal = new CallStatisticOverview();
				Object[] objs = subtotals.get(0);
				for(int i = 0; i < objs.length; i++) {
					if(objs[i] == null) {
						objs[i] = 0;
					}
				}
				
				subtotal.setStartDate(new Date());
				subtotal.setCallAmount(Long.valueOf(objs[0].toString()));
				subtotal.setBridgedAmount(Long.valueOf(objs[1].toString()));
				subtotal.setPickupInSpecifiedSec(Long.valueOf(objs[2].toString()));
				subtotal.setAbandonInSpecifiedSec(Long.valueOf(objs[3].toString()));
				subtotal.setTotalCallTime(Long.valueOf(objs[4].toString()));
				subtotal.setTotalAnswerTime(Long.valueOf(objs[5].toString()));
				
				Double avgCallTime = (subtotal.getCallAmount() > 0) ? ((subtotal.getTotalCallTime()+0.0) / subtotal.getCallAmount()) : 0.00;
				Double avgAnswerTime = (subtotal.getBridgedAmount() > 0) ? ((subtotal.getTotalAnswerTime()+0.0) / subtotal.getBridgedAmount()) : 0.00;
				Double bridgedRate = (subtotal.getCallAmount() > 0) ? ((subtotal.getBridgedAmount()+0.0) / subtotal.getCallAmount()) : 0.00;
				
				subtotal.setAvgCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgCallTime)));
				subtotal.setAvgAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgAnswerTime)));
				subtotal.setBridgedRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedRate)));
			}
		} else if("incoming".equals(direction)) {
			String nativeSql = "select sum(callinamount),sum(bridgedinamount),sum(csrpickupinspecifiedsec),sum(csrabandoninspecifiedsec),sum(totalincalltime),sum(totalinanswertime) " +
					"from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;
			List<Object[]> subtotals = callStatisticOverviewService.getByNativeSql(nativeSql);
			if(subtotals.size() > 0) {
				subtotal = new CallStatisticOverview();
				Object[] objs = subtotals.get(0);
				for(int i = 0; i < objs.length; i++) {
					if(objs[i] == null) {
						objs[i] = 0;
					}
				}
				
				subtotal.setStartDate(new Date());
				subtotal.setCallInAmount(Long.valueOf(objs[0].toString()));
				subtotal.setBridgedInAmount(Long.valueOf(objs[1].toString()));
				subtotal.setCsrPickupInSpecifiedSec(Long.valueOf(objs[2].toString()));
				subtotal.setCsrAbandonInSpecifiedSec(Long.valueOf(objs[3].toString()));
				subtotal.setTotalInCallTime(Long.valueOf(objs[4].toString()));
				subtotal.setTotalInAnswerTime(Long.valueOf(objs[5].toString()));
				
				Double avgInCallTime = (subtotal.getCallInAmount() > 0) ? ((subtotal.getTotalInCallTime()+0.0) / subtotal.getCallInAmount()) : 0.00;
				Double avgInAnswerTime = (subtotal.getBridgedInAmount() > 0) ? ((subtotal.getTotalInAnswerTime()+0.0) / subtotal.getBridgedInAmount()) : 0.00;
				Double bridgedInRate = (subtotal.getCallInAmount() > 0) ? ((subtotal.getBridgedInAmount()+0.0) / subtotal.getCallInAmount()) : 0.00;
				Double incomingServiceLevel = (subtotal.getCallInAmount() > 0) ? ((subtotal.getCsrPickupInSpecifiedSec()+0.0) / subtotal.getCallInAmount()) : 0.0;
				
				subtotal.setAvgInCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgInCallTime)));
				subtotal.setAvgInAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgInAnswerTime)));
				subtotal.setBridgedInRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedInRate)));
				subtotal.setIncomingServiceLevel(Double.valueOf(DF_FOUR_PRECISION.format(incomingServiceLevel)));
			}
		} else if("outgoing".equals(direction)) {
			String nativeSql = "select sum(calloutamount),sum(bridgedoutamount),sum(customerpickupinspecifiedsec),sum(customerabandoninspecifiedsec),sum(totaloutcalltime),sum(totaloutanswertime) " +
					"from ec2_report_call_statistic_overview as e where e.domainid = "+domain.getId()+specificStartTimeSql + specificEndTimeSql;
			List<Object[]> subtotals = callStatisticOverviewService.getByNativeSql(nativeSql);
			if(subtotals.size() > 0) {
				subtotal = new CallStatisticOverview();
				Object[] objs = subtotals.get(0);
				for(int i = 0; i < objs.length; i++) {
					if(objs[i] == null) {
						objs[i] = 0;
					}
				}
				
				subtotal.setStartDate(new Date());
				subtotal.setCallOutAmount(Long.valueOf(objs[0].toString()));
				subtotal.setBridgedOutAmount(Long.valueOf(objs[1].toString()));
				subtotal.setCustomerPickupInSpecifiedSec(Long.valueOf(objs[2].toString()));
				subtotal.setCustomerAbandonInSpecifiedSec(Long.valueOf(objs[3].toString()));
				subtotal.setTotalOutCallTime(Long.valueOf(objs[4].toString()));
				subtotal.setTotalOutAnswerTime(Long.valueOf(objs[5].toString()));
				
				Double avgOutCallTime = (subtotal.getCallOutAmount() > 0) ? ((subtotal.getTotalOutCallTime()+0.0) / subtotal.getCallOutAmount()) : 0.00;
				Double avgOutAnswerTime = (subtotal.getBridgedOutAmount() > 0) ? ((subtotal.getTotalOutAnswerTime()+0.0) / subtotal.getBridgedOutAmount()) : 0.00;
				Double bridgedOutRate = (subtotal.getCallOutAmount() > 0) ? ((subtotal.getBridgedOutAmount()+0.0) / subtotal.getCallOutAmount()) : 0.00;
				
				subtotal.setAvgOutCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgOutCallTime)));
				subtotal.setAvgOutAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgOutAnswerTime)));
				subtotal.setBridgedOutRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedOutRate)));
			}
		}
	}
	
	/**
	 * 传递显示表格
	 * @param csorTable
	 */
	public void setCsorTable(Table csorTable) {
		this.csorTable = csorTable;
	}

	/**
	 * 获取翻页组件
	 * @return
	 */
	public FlipOverTableComponent<CallStatisticOverview> getCsorTableFlip() {
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
	public CallStatisticOverview getSubtotal() {
		return subtotal;
	}

	/**
	 * 用来供翻页组件回调的方法,将总计信息加入表格
	 * 
	 * @param records
	 */
	public void flipOverCallBack(List<CallStatisticOverview> csos) {
		csos.add(subtotal);
	}
	
	/**
	 * 获取当前的查看方向
	 * @return
	 */
	public String getDirection() {
		return direction;
	}

}
