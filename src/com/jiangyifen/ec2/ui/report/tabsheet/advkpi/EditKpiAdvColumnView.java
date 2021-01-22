package com.jiangyifen.ec2.ui.report.tabsheet.advkpi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.report.tabsheet.advkpi.pojo.KpiTableColVo;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @Description 描述：
 *
 * @author  JRH
 * @date    2014年5月16日 上午午 11:00:00
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class EditKpiAdvColumnView extends VerticalLayout implements ClickListener, ValueChangeListener, Content {

	private final String[] COLS_BASE = new String[] {			// 基础信息
			"startDate", "hour", "deptName", "csrName", "empno", "username"
		};

	private final String[] HEADER_BASE = new String[] {			// 基础信息列头
			"日期", "时段", "部门", "姓名", "工号", "用户名"
		};
	
	private final String[] COLS_LOGIN = new String[] {			// 登陆信息
			"loginCt", "onlineDtStr", "readyDtStr", "effectWorkDtStr", "effectWorkRt"
		};

	private final String[] HEADER_LOGIN = new String[] {		// 基础信息列头
			"登陆次量", "在线时长", "就绪时长", "有效工作时长", "有效工作率"
		};
	
	private final String[] COLS_PAUSE = new String[] { 			// 置忙信息
			"pauseCt", "pauseDtStr", "leavePsCt", "leavePsDtStr", "meettingPsCt", "meettingPsDtStr", "busyPsCt", "busyPsDtStr", 
			"restPsCt", "restPsDtStr", "dinePsCt", "dinePsDtStr", "trainPsCt", "trainPsDtStr"
		};

	private final String[] HEADER_PAUSE = new String[] {		// 置忙信息列头
			"置忙次量", "置忙时长", "离开次量", "离开时长", "会议次量", "会议时长", "忙碌次量", "忙碌时长", 
			"休息次量", "休息时长", "就餐次量", "就餐时长", "培训次量", "培训时长"
		};
	
	private final String[] COLS_TOTAL_CALL = new String[] { 	// 话务总统计详情 
			"dialCt", "bridgeCt", "unbridgeCt", "bridgeRt", "dialDtStr", "dialBgDtStr", "effectDialRt", "perHourDialCt"
	};

	private final String[] HEADER_TOTAL_CALL = new String[] {	// 话务总统计详情 列头
			"话务总量", "话务接通总量", "话务未接通总量", "话务接通率%", "话务总时长", "话务接通时长", "有效话务率%", "每小时话务量"
		};
	
	private final String[] COLS_CALL_OUT = new String[] { 		// 呼出话务统计详情
			"outDialCt", "outBgCt", "outUnbgCt", "outBgRt", "outBgLowSecCt", "outBgLowSecRt", "outBgLowSecCt1", "outBgLowSecRt1", "outBgLowSecCt2", "outBgLowSecRt2", "outBgLargeSecCt1", "outBgLargeSecRt1", 
			"outBgLargeSecCt3", "outBgLargeSecRt3", "outBgLargeSecCt4", "outBgLargeSecRt4", "outBgLargeSecCt", "outBgLargeSecRt", "outBgLargeSecCt2", "outBgLargeSecRt2", "outBgLargeSecCt5", "outBgLargeSecRt5", "outBgLargeSecCt6", "outBgLargeSecRt6", "outBgLargeSecCt7", "outBgLargeSecRt7",
			"outMaxBgDtStr", "outDialDtStr", "outRingDtStr", "outAvgRingDtStr", "outBgDtStr", "outAvgBgDtStr", "outEffectDtRt", "outAfterCt", "outAfterDtStr", "outPerHourCt"
	};

	private final String[] HEADER_CALL_OUT = new String[] {		// 呼出话务统计详情列头
			"呼出总量", "呼出接通量", "呼出未接通量", "呼出接通率", "小于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LOW_SEC+"秒呼出接通量", "小于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LOW_SEC+"秒呼出接通率", "小于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LOW_SEC1+"秒呼出接通量", "小于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LOW_SEC1+"秒呼出接通率",
			"小于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LOW_SEC2+"秒呼出接通量", "小于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LOW_SEC2+"秒呼出接通率", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC1+"秒呼出接通量", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC1+"秒呼出接通率", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC3+"秒呼出接通量",
			"大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC3+"秒呼出接通率", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC4+"秒呼出接通量", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC4+"秒呼出接通率", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC+"秒呼出接通量", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC+"秒呼出接通率",  "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC2+"秒呼出接通量", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC2+"秒呼出接通率",
			"大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC5+"秒呼出接通量", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC5+"秒呼出接通率", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC6+"秒呼出接通量", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC6+"秒呼出接通率", "大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC7+"秒呼出接通量",
			"大于"+AdvanceKPIReportView.SPECIFY_OUT_BG_LARGE_SEC7+"秒呼出接通率","最大呼出通话时长", "呼出总时长", "呼出等待时长", "平均呼出等待时长", "呼出接通时长", "平均呼出接通时长", "有效呼出时长占有率", "呼出话后处理次量", "呼出话后处理时长", "每小时呼出量"
		};
	
	private final String[] COLS_CALL_INNER = new String[] { 	// 内部话务统计详情
			"innerDialCt", "innerBgCt", "innerBgRt", "innerDialDtStr", "innerBgDtStr", "innerMaxBgDtStr", "innerPerHourCt"
	};

	private final String[] HEADER_CALL_INNER = new String[] {	// 内部话务统计详情列头
			"内部呼叫总量", "内部呼叫接通量", "内部呼叫接通率", "内部呼叫时长", "内部呼叫接通时长", "最大内部呼叫接通时长", "每小时内部呼叫量"
		};
	
	private final String IN_MISS_CT = "inMissCt";				// 呼入漏接数，这个比较特殊，它需要从另一种表单独统计
	
	private final String[] COLS_CALL_IN = new String[] { 		// 呼入话务统计详情
			"inDialCt", "inBgCt", "inUnbgCt", "inBgRt", IN_MISS_CT, "inBgLevelCt", "inLevelRt", "inBgLevelCt2", "inLevelRt2", 
			"inBgLowSecCt", "inBgLowSecRt", "inBgLowSecCt2","inBgLowSecRt2", "inBgLowSecCt1", "inBgLowSecRt1", "inBgLargeSecCt1", "inBgLargeSecRt1", "inBgLargeSecCt3", "inBgLargeSecRt3",
			"inBgLargeSecCt4", "inBgLargeSecRt4", "inBgLargeSecCt", "inBgLargeSecRt", "inBgLargeSecCt2", "inBgLargeSecRt2", "inBgLargeSecCt5", "inBgLargeSecRt5", "inBgLargeSecCt6", "inBgLargeSecRt6",  "inBgLargeSecCt7", "inBgLargeSecRt7", 
			"inMaxBgDtStr", "inDialDtStr", "inRingDtStr", "inAvgRingDtStr", 
			"inBgDtStr", "inAvgBgDtStr", "inEffectDtRt", "inAfterCt", "inAfterDtStr", "inPerHourCt"
	};

	private final String[] HEADER_CALL_IN = new String[] {			// 呼入话务统计详情列头
			"呼入总量", "呼入接通量", "呼入未接通量", "呼入接通率", "漏接量", AdvanceKPIReportView.SPECIFY_IN_BG_LEVEL_SEC+"秒服务水平内接通量", AdvanceKPIReportView.SPECIFY_IN_BG_LEVEL_SEC+"秒服务水平%", AdvanceKPIReportView.SPECIFY_IN_BG_LEVEL_SEC2+"秒服务水平内接通量", AdvanceKPIReportView.SPECIFY_IN_BG_LEVEL_SEC2+"秒服务水平%", 
			"小于"+AdvanceKPIReportView.SPECIFY_IN_BG_LOW_SEC+"秒呼入接通量", "小于"+AdvanceKPIReportView.SPECIFY_IN_BG_LOW_SEC+"秒呼入接通率", "小于"+AdvanceKPIReportView.SPECIFY_IN_BG_LOW_SEC2+"秒呼入接通量", "小于"+AdvanceKPIReportView.SPECIFY_IN_BG_LOW_SEC2+"秒呼入接通率", "小于"+AdvanceKPIReportView.SPECIFY_IN_BG_LOW_SEC3+"秒呼入接通量", "小于"+AdvanceKPIReportView.SPECIFY_IN_BG_LOW_SEC3+"秒呼入接通率",
			"大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC1+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC1+"秒呼入接通率", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC3+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC3+"秒呼入接通率","大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC4+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC4+"秒呼入接通率", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC+"秒呼入接通率",
			"大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC2+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC2+"秒呼入接通率", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC5+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC5+"秒呼入接通率","大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC6+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC6+"秒呼入接通率","大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC7+"秒呼入接通量", "大于"+AdvanceKPIReportView.SPECIFY_IN_BG_LARGE_SEC7+"秒呼入接通率",
			"最大呼入通话时长", "呼入总时长", "呼入等待时长", "平均呼入等待时长", 
			"呼入接通时长", "平均呼入接通时长", "有效呼入时长占有率", "呼入话后处理次量", "呼入话后处理时长", "每小时呼入量"
		};
	
	private Map<String, Integer> colIndexMap;				// 维护待显示属性列与其对应的排序位置
	private Map<String, String> colHeaderMap;				// 维护待显示属性列与其对应的中文显示名称
	
	private BeanItemContainer<String> baseCol_bic; 			// 基础信息   对应的属性
	private BeanItemContainer<String> loginCol_bic; 		// 登陆   对应的属性
	private BeanItemContainer<String> pauseCol_bic;  		// 置忙   对应的属性
	private BeanItemContainer<String> totalCallCol_bic;  	// 话务总览   对应的属性
	private BeanItemContainer<String> callOutCol_bic;  		// 呼出方向   对应的属性
	private BeanItemContainer<String> callInnerCol_bic;  	// 内部呼叫方向   对应的属性
	private BeanItemContainer<String> callInCol_bic;  		// 呼入方向    对应的属性
	
	private OptionGroup baseCol_og;							// 基础信息   对应的属性选择项的组件
	private OptionGroup loginCol_og;						// 登陆   对应的属性选择项的组件
	private OptionGroup pauseCol_og;						// 置忙  对应的属性选择项的组件
	private OptionGroup totalCallCol_og;					// 话务总览  对应的属性选择项的组件
	private OptionGroup callOutCol_og;						// 呼出方向  对应的属性选择项的组件
	private OptionGroup callInnerCol_og;					// 内部呼叫方向  对应的属性选择项的组件
	private OptionGroup callInCol_og;						// 呼入方向  对应的属性选择项的组件

	private Button selectAll_bt;
	private Button invertSelect_bt;
	
	private CheckBox optBase_cb;
	private CheckBox optLogin_cb;
	private CheckBox optPause_cb;
	private CheckBox optTotalCall_cb;
	private CheckBox optCallOut_cb;
	private CheckBox optCallInner_cb;
	private CheckBox optCallIn_cb;
	private CommonService commonService;
	
//	private String viewType;								// 查看的统计方式 ："by_mon"、"by_day"、"by_hour"
	
	public EditKpiAdvColumnView() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("-1px");
		this.setHeight("-1px");
		commonService = SpringContextHolder.getBean("commonService");
		initializeColMap();		// 初始化要显示的属性与其中文名称对应的Map集合，以及各属性的排序位置Map集合
		
		createGlobalSelectUi();	// 创建 全局选择组件【全选、反选】等
		
		createBaseColUi();		// 创建  基础信息   对应的属性选择项的组件
		
		createLoginColUi();		// 创建  登陆   对应的属性选择项的组件
		
		createPauseColUi();		// 创建  置忙   对应的属性选择项的组件
		
		createTotalCallColUi();	// 创建   话务总览  对应的属性选择项的组件
		
		createCallOutColUi();	// 创建   呼出方向  对应的属性选择项的组件
		
		createCallInnerColUi();	// 创建   内部呼叫方向  对应的属性选择项的组件
		
		createCallInColUi();	// 创建   呼入方向  对应的属性选择项的组件

	}

	/**
	 * @Description 描述：初始化要显示的属性与其中文名称对应的Map集合，以及各属性的排序位置Map集合
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:15:54 void
	 */
	private void initializeColMap() {
		colIndexMap = new HashMap<String, Integer>();
		colHeaderMap = new HashMap<String, String>();
		
		int header_index = 1;
		for(int i = 0; i < COLS_BASE.length; i++) {
			String col = COLS_BASE[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_BASE[i]);
			++header_index;
		}

		for(int i = 0; i < COLS_LOGIN.length; i++) {
			String col = COLS_LOGIN[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_LOGIN[i]);
			++header_index;
		}
		
		for(int i = 0; i < COLS_PAUSE.length; i++) {
			String col = COLS_PAUSE[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_PAUSE[i]);
			++header_index;
		}
		
		for(int i = 0; i < COLS_TOTAL_CALL.length; i++) {
			String col = COLS_TOTAL_CALL[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_TOTAL_CALL[i]);
			++header_index;
		}
		
		for(int i = 0; i < COLS_CALL_OUT.length; i++) {
			String col = COLS_CALL_OUT[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_CALL_OUT[i]);
			++header_index;
		}
		
		for(int i = 0; i < COLS_CALL_INNER.length; i++) {
			String col = COLS_CALL_INNER[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_CALL_INNER[i]);
			++header_index;
		}
		
		for(int i = 0; i < COLS_CALL_IN.length; i++) {
			String col = COLS_CALL_IN[i];
			colIndexMap.put(col, header_index);
			colHeaderMap.put(col, HEADER_CALL_IN[i]);
			++header_index;
		}
	}

	/**
	 * @Description 描述：创建 全局选择组件【全选、反选】等
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:14:59 void
	 */
	private void createGlobalSelectUi() {
		HorizontalLayout result_hlo = new HorizontalLayout();
		result_hlo.setSpacing(true);
		this.addComponent(result_hlo);
	
		Label result_lb = new Label("<font color='blue'><b>选择显示列：</b></font>", Label.CONTENT_XHTML);
		result_lb.setWidth("-1px");
		result_hlo.addComponent(result_lb);
		
		selectAll_bt = new Button("全选", this);
		selectAll_bt.setImmediate(true);
		selectAll_bt.setStyleName(BaseTheme.BUTTON_LINK);
		result_hlo.addComponent(selectAll_bt);
		
		invertSelect_bt = new Button("反选", this);
		invertSelect_bt.setImmediate(true);
		invertSelect_bt.setStyleName(BaseTheme.BUTTON_LINK);
		result_hlo.addComponent(invertSelect_bt);
	}

	/**
	 * @Description 描述：创建  基础信息   对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:13:33 void
	 */
	private void createBaseColUi() {
		VerticalLayout base_vlo = new VerticalLayout();
		this.addComponent(base_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		base_vlo.addComponent(layout);
		
		Label caption_lb = new Label("基础信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optBase_cb = new CheckBox("全选子列");
		optBase_cb.addListener((ValueChangeListener)this);
		optBase_cb.setImmediate(true);
		layout.addComponent(optBase_cb);
		
		baseCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_BASE) {
			baseCol_bic.addBean(col);
		}
		
		baseCol_og = new OptionGroup(null, baseCol_bic);
		baseCol_og.addStyleName("fivecol750");		// 按三列排版
		baseCol_og.addStyleName("boldcaption");
		baseCol_og.setMultiSelect(true);
		baseCol_og.setImmediate(true);
		for(String itemId : baseCol_bic.getItemIds()) {
			baseCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		base_vlo.addComponent(baseCol_og);
	}

	/**
	 * @Description 描述：创建  登陆   对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:12:56 void
	 */
	private void createLoginColUi() {
		VerticalLayout login_vlo = new VerticalLayout();
		this.addComponent(login_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		login_vlo.addComponent(layout);
		
		Label caption_lb = new Label("登陆信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optLogin_cb = new CheckBox("全选子列");
		optLogin_cb.addListener((ValueChangeListener)this);
		optLogin_cb.setImmediate(true);
		layout.addComponent(optLogin_cb);
		layout.setComponentAlignment(optLogin_cb, Alignment.MIDDLE_CENTER);
		
		loginCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_LOGIN) {
			loginCol_bic.addBean(col);
		}
		
		loginCol_og = new OptionGroup(null, loginCol_bic);
		loginCol_og.addStyleName("fivecol750");		// 按三列排版
		loginCol_og.addStyleName("boldcaption");
		loginCol_og.setMultiSelect(true);
		loginCol_og.setImmediate(true);
		for(String itemId : loginCol_bic.getItemIds()) {
			loginCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		login_vlo.addComponent(loginCol_og);
	}

	/**
	 * @Description 描述：创建  置忙   对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:12:16 void
	 */
	private void createPauseColUi() {
		VerticalLayout pause_vlo = new VerticalLayout();
//		TODO
//		this.addComponent(pause_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		pause_vlo.addComponent(layout);
		
		Label caption_lb = new Label("置忙信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optPause_cb = new CheckBox("全选子列");
		optPause_cb.addListener((ValueChangeListener)this);
		optPause_cb.setImmediate(true);
		layout.addComponent(optPause_cb);
		layout.setComponentAlignment(optPause_cb, Alignment.MIDDLE_CENTER);
		
		pauseCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_PAUSE) {
			pauseCol_bic.addBean(col);
		}
		
		pauseCol_og = new OptionGroup(null, pauseCol_bic);
		pauseCol_og.addStyleName("fivecol750");		// 按三列排版
		pauseCol_og.addStyleName("boldcaption");
		pauseCol_og.setMultiSelect(true);
		pauseCol_og.setImmediate(true);
		for(String itemId : pauseCol_bic.getItemIds()) {
			pauseCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		pause_vlo.addComponent(pauseCol_og);
	}

	/**
	 * @Description 描述：创建   话务总览    对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:10:05 void
	 */
	private void createTotalCallColUi() {
		VerticalLayout totalCall_vlo = new VerticalLayout();
		this.addComponent(totalCall_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		totalCall_vlo.addComponent(layout);
		
		Label caption_lb = new Label("总话务信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optTotalCall_cb = new CheckBox("全选子列");
		optTotalCall_cb.addListener((ValueChangeListener)this);
		optTotalCall_cb.setImmediate(true);
		layout.addComponent(optTotalCall_cb);
		layout.setComponentAlignment(optTotalCall_cb, Alignment.MIDDLE_CENTER);
		
		totalCallCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_TOTAL_CALL) {
			totalCallCol_bic.addBean(col);
		}
		
		totalCallCol_og = new OptionGroup(null, totalCallCol_bic);
		totalCallCol_og.addStyleName("fivecol750");		// 按三列排版
		totalCallCol_og.addStyleName("boldcaption");
		totalCallCol_og.setMultiSelect(true);
		totalCallCol_og.setImmediate(true);
		for(String itemId : totalCallCol_bic.getItemIds()) {
			totalCallCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		totalCall_vlo.addComponent(totalCallCol_og);
	}

	/**
	 * @Description 描述：创建   呼出方向  对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:09:31 void
	 */
	private void createCallOutColUi() {
		VerticalLayout callOut_vlo = new VerticalLayout();
		this.addComponent(callOut_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		callOut_vlo.addComponent(layout);
		
		Label caption_lb = new Label("呼出话务信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optCallOut_cb = new CheckBox("全选子列");
		optCallOut_cb.addListener((ValueChangeListener)this);
		optCallOut_cb.setImmediate(true);
		layout.addComponent(optCallOut_cb);
		layout.setComponentAlignment(optCallOut_cb, Alignment.MIDDLE_CENTER);
		
		callOutCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_CALL_OUT) {
			callOutCol_bic.addBean(col);
		}
		
		callOutCol_og = new OptionGroup(null, callOutCol_bic);
		callOutCol_og.addStyleName("fivecol750");		// 按三列排版
		callOutCol_og.addStyleName("boldcaption");
		callOutCol_og.setMultiSelect(true);
		callOutCol_og.setImmediate(true);
		for(String itemId : callOutCol_bic.getItemIds()) {
			callOutCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		callOut_vlo.addComponent(callOutCol_og);
	}

	/**
	 * @Description 描述：创建   内部呼叫方向  对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:08:26 void
	 */
	private void createCallInnerColUi() {
		VerticalLayout callInner_vlo = new VerticalLayout();
		this.addComponent(callInner_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		callInner_vlo.addComponent(layout);
		
		Label caption_lb = new Label("内部话务信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optCallInner_cb = new CheckBox("全选子列");
		optCallInner_cb.addListener((ValueChangeListener)this);
		optCallInner_cb.setImmediate(true);
		layout.addComponent(optCallInner_cb);
		layout.setComponentAlignment(optCallInner_cb, Alignment.MIDDLE_CENTER);
		
		callInnerCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_CALL_INNER) {
			callInnerCol_bic.addBean(col);
		}
		
		callInnerCol_og = new OptionGroup(null, callInnerCol_bic);
		callInnerCol_og.addStyleName("fivecol750");		// 按三列排版
		callInnerCol_og.addStyleName("boldcaption");
		callInnerCol_og.setMultiSelect(true);
		callInnerCol_og.setImmediate(true);
		for(String itemId : callInnerCol_bic.getItemIds()) {
			callInnerCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		this.addComponent(callInnerCol_og);
	}

	/**
	 * @Description 描述：创建   呼入方向  对应的属性选择项的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:07:06 void
	 */
	private void createCallInColUi() {
		VerticalLayout callIn_vlo = new VerticalLayout();
		this.addComponent(callIn_vlo);
		
		HorizontalLayout layout = new HorizontalLayout();
		callIn_vlo.addComponent(layout);
		
		Label caption_lb = new Label("呼入话务信息：");
		caption_lb.addStyleName("boldfont");
		layout.addComponent(caption_lb);
		
		optCallIn_cb = new CheckBox("全选子列");
		optCallIn_cb.addListener((ValueChangeListener)this);
		optCallIn_cb.setImmediate(true);
		layout.addComponent(optCallIn_cb);
		layout.setComponentAlignment(optCallIn_cb, Alignment.MIDDLE_CENTER);
		
		callInCol_bic = new BeanItemContainer<String>(String.class);
		for(String col : COLS_CALL_IN) {
			callInCol_bic.addBean(col);
		}
		
		callInCol_og = new OptionGroup(null, callInCol_bic);
		callInCol_og.addStyleName("fivecol750");		// 按三列排版
		callInCol_og.addStyleName("boldcaption");
		callInCol_og.setMultiSelect(true);
		callInCol_og.setImmediate(true);
		for(String itemId : callInCol_bic.getItemIds()) {
			callInCol_og.setItemCaption(itemId, colHeaderMap.get(itemId));
		}
		callIn_vlo.addComponent(callInCol_og);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == optBase_cb) {
			boolean isselected = (Boolean) optBase_cb.getValue();
			if(isselected) {
				for(String col : baseCol_bic.getItemIds()) {
					baseCol_og.select(col);
				}
			} else {
				baseCol_og.setValue(null);
			}
		} else if(source == optLogin_cb) {
			boolean isselected = (Boolean) optLogin_cb.getValue();
			if(isselected) {
				for(String col : loginCol_bic.getItemIds()) {
					loginCol_og.select(col);
				}
			} else {
				loginCol_og.setValue(null);
			}
		} else if(source == optPause_cb) {
			boolean isselected = (Boolean) optPause_cb.getValue();
			if(isselected) {
				for(String col : pauseCol_bic.getItemIds()) {
					pauseCol_og.select(col);
				}
			} else {
				pauseCol_og.setValue(null);
			}
		} else if(source == optTotalCall_cb) {
			boolean isselected = (Boolean) optTotalCall_cb.getValue();
			if(isselected) {
				for(String col : totalCallCol_bic.getItemIds()) {
					totalCallCol_og.select(col);
				}
			} else {
				totalCallCol_og.setValue(null);
			}
		} else if(source == optCallOut_cb) {
			boolean isselected = (Boolean) optCallOut_cb.getValue();
			if(isselected) {
				for(String col : callOutCol_bic.getItemIds()) {
					callOutCol_og.select(col);
				}
			} else {
				callOutCol_og.setValue(null); 
			}
		} else if(source == optCallInner_cb) {
			boolean isselected = (Boolean) optCallInner_cb.getValue();
			if(isselected) {
				for(String col : callInnerCol_bic.getItemIds()) {
					callInnerCol_og.select(col);
				}
			} else {
				callInnerCol_og.setValue(null);
			}
		} else if(source == optCallIn_cb) {
			boolean isselected = (Boolean) optCallIn_cb.getValue();
			if(isselected) {
				for(String col : callInCol_bic.getItemIds()) {
					callInCol_og.select(col);
				}
			} else {
				callInCol_og.setValue(null);
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == selectAll_bt) {			// 全选所有选项
			executeSelectAll();	
		} else if(source == invertSelect_bt) {	// 反选所有选项
			executeInvert();
		}
	}

	/**
	 * @Description 描述：全选所有选项
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午10:59:57 void
	 */
	private void executeSelectAll() {
		for(String col : baseCol_bic.getItemIds()) {
			baseCol_og.select(col);
		}
		for(String col : loginCol_bic.getItemIds()) {
			loginCol_og.select(col);
		}
		/*for(String col : pauseCol_bic.getItemIds()) {
//			TODO
//			pauseCol_og.select(col);
		}*/
		for(String col : totalCallCol_bic.getItemIds()) {
			totalCallCol_og.select(col);
		}
		for(String col : callOutCol_bic.getItemIds()) {
			callOutCol_og.select(col);
		}
		for(String col : callInnerCol_bic.getItemIds()) {
			callInnerCol_og.select(col);
		}
		for(String col : callInCol_bic.getItemIds()) {
			callInCol_og.select(col);
		}
	}

	/**
	 * @Description 描述：反选所有选项
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午10:58:01 void
	 */
	@SuppressWarnings("unchecked")
	private void executeInvert() {
		Set<String> selectedCols = (Set<String>) baseCol_og.getValue();
		baseCol_og.setValue(null);
		for(String col : baseCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				baseCol_og.select(col);
			}
		}
		
		selectedCols = (Set<String>) loginCol_og.getValue();
		loginCol_og.setValue(null);
		for(String col : loginCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				loginCol_og.select(col);
			}
		}
		
		selectedCols = (Set<String>) pauseCol_og.getValue();
		pauseCol_og.setValue(null);
		for(String col : pauseCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				pauseCol_og.select(col);
			}
		}
		
		selectedCols = (Set<String>) totalCallCol_og.getValue();
		totalCallCol_og.setValue(null);
		for(String col : totalCallCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				totalCallCol_og.select(col);
			}
		}
		
		selectedCols = (Set<String>) callOutCol_og.getValue();
		callOutCol_og.setValue(null);
		for(String col : callOutCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				callOutCol_og.select(col);
			}
		}
		
		selectedCols = (Set<String>) callInnerCol_og.getValue();
		callInnerCol_og.setValue(null);
		for(String col : callInnerCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				callInnerCol_og.select(col);
			}
		}
		
		selectedCols = (Set<String>) callInCol_og.getValue();
		callInCol_og.setValue(null);
		for(String col : callInCol_bic.getItemIds()) {
			if(!selectedCols.contains(col)) {
				callInCol_og.select(col);
			}
		}
	}

	/**
	 * @Description 描述：获取高级KPI报表显示表格中 能看到那些属性列，并且各属性列对应的表格头
	 * 
	 * @author  JRH
	 * @date    2014年5月20日 上午11:01:09
	 * @return kpiTableColVo 该实体用来维护高级KPI报表显示表格中 能看到那些属性列，并且各属性列对应的表格头
	 */
	@SuppressWarnings("unchecked")
	public KpiTableColVo getVisibleColAndHeadersMap(String viewType) {
		Set<String> selectedCols = new HashSet<String>();
		selectedCols.addAll((Set<String>)baseCol_og.getValue());
		selectedCols.addAll((Set<String>)loginCol_og.getValue());
//		TODO JRH 隐藏置忙时间的统计 2014-11-13
//		selectedCols.addAll((Set<String>)pauseCol_og.getValue());
		selectedCols.addAll((Set<String>)totalCallCol_og.getValue());
		selectedCols.addAll((Set<String>)callOutCol_og.getValue());
		selectedCols.addAll((Set<String>)callInnerCol_og.getValue());
		selectedCols.addAll((Set<String>)callInCol_og.getValue());
		
		selectedCols.add("startDate");	// 必选项
		baseCol_og.select("startDate"); 
		if(AdvanceKPIReportView.VIEW_TYPE_BY_HOUR.equals(viewType)) {	// 如果当前查看方式是按
			selectedCols.add("hour");
		} else {
			selectedCols.remove("hour");
		}
		
		ArrayList<String> visible_colLs = new ArrayList<String>(selectedCols);	// 可见列
		Collections.sort(visible_colLs, new Comparator<String>() {	// 重新排序
			@Override
			public int compare(String o1, String o2) {
				int index1 = colIndexMap.get(o1);
				int index2 = colIndexMap.get(o2);
				return index1 - index2;
			}
		});
		
		// 设置显示表格的列头
		String[] visible_header_arr = new String[visible_colLs.size()];
		for(int i = 0; i < visible_colLs.size(); i++) {
			String col = visible_colLs.get(i);
			visible_header_arr[i] = colHeaderMap.get(col);
		}

		KpiTableColVo kpiTableColVo = new KpiTableColVo();
		kpiTableColVo.setVisibleCols(visible_colLs.toArray());
		kpiTableColVo.setVisibleHeaders(visible_header_arr);
		for(String col : COLS_BASE) {
			if(visible_colLs.contains(col)) {
				kpiTableColVo.setBaseVisible(true);
				break;
			}
		}
		for(String col : COLS_LOGIN) {
			if(visible_colLs.contains(col)) {
				kpiTableColVo.setLoginVisible(true);
				break;
			}
		}
		for(String col : COLS_PAUSE) {
			if(visible_colLs.contains(col)) {
				kpiTableColVo.setPauseVisible(true);
				break;
			}
		}
		for(String col : COLS_TOTAL_CALL) {
			if(visible_colLs.contains(col)) {
				kpiTableColVo.setTotalCallVisible(true);
				break;
			}
		}
		for(String col : COLS_CALL_OUT) {
			if(visible_colLs.contains(col)) {
				kpiTableColVo.setCallOutVisible(true);
				break;
			}
		}
		for(String col : COLS_CALL_INNER) {
			if(visible_colLs.contains(col)) {
				kpiTableColVo.setCallInnerVisible(true);
				break;
			}
		}
		for(String col : COLS_CALL_IN) {
			if(!IN_MISS_CT.equals(col) && visible_colLs.contains(col)) {
				kpiTableColVo.setCallInVisible(true);
				break;
			}
		}
		
		if(visible_colLs.contains(IN_MISS_CT)) {
			kpiTableColVo.setMissCallInVisible(true);
		}
		
		return kpiTableColVo;
	}

	/**
	 * @Description 描述：初始化组件值
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:04:47 void
	 */
	public void rebuildColUi(String viewType) {
		if(AdvanceKPIReportView.VIEW_TYPE_BY_HOUR.equals(viewType)) {
			baseCol_bic.addItemAfter("startDate", "hour");
			baseCol_og.select("hour");
			initSelectCos();
		} else {
			baseCol_bic.removeItem("hour");
			initSelectCos();
		}
	}
	
	public void initSelectCos() {
		String jpql = "select s from KpiTableColVo as s";
		List<KpiTableColVo> kpiTableColVos = commonService.getEntitiesByJpql(jpql);
		if(null != kpiTableColVos && kpiTableColVos.size() > 0) {
			KpiTableColVo kpiTableColVo = kpiTableColVos.get(0);
			if(null != kpiTableColVo) {
				Object[] visibleCols = kpiTableColVo.getVisibleCols();
				boolean baseVisible = kpiTableColVo.isBaseVisible();
				boolean callInnerVisible = kpiTableColVo.isCallInnerVisible();
				boolean callInVisible = kpiTableColVo.isCallInVisible();
				boolean callOutVisible = kpiTableColVo.isCallOutVisible();
				boolean loginVisible = kpiTableColVo.isLoginVisible();
				boolean totalCallVisible = kpiTableColVo.isTotalCallVisible();
				if(baseVisible) {
					for(int i = 0, size = visibleCols.length; i < size; i++) {
						baseCol_og.select(visibleCols[i]);
					}
//					optBase_cb.setValue(true);
				}
				if(callInnerVisible) {
					for(int i = 0, size = visibleCols.length; i < size; i++) {
						callInnerCol_og.select(visibleCols[i]);
					}
//					optCallInner_cb.setValue(true);
				}
				if(callInVisible) {
					for(int i = 0, size = visibleCols.length; i < size; i++) {
						callInCol_og.select(visibleCols[i]);
					}
//					optCallIn_cb.setValue(true);
				}
				if(callOutVisible) {
					for(int i = 0, size = visibleCols.length; i < size; i++) {
						callOutCol_og.select(visibleCols[i]);
					}
//					optCallOut_cb.setValue(true);
				}
				if(loginVisible) {
					for(int i = 0, size = visibleCols.length; i < size; i++) {
						loginCol_og.select(visibleCols[i]);
					}
//					optLogin_cb.setValue(true);
				}
				if(totalCallVisible) {
					for(int i = 0, size = visibleCols.length; i < size; i++) {
						totalCallCol_og.select(visibleCols[i]);
					}
//					optTotalCall_cb.setValue(true);
				}
			}
		}
	}

	/**
	 * @Description 描述：获取选择全部的组件
	 *
	 * @author  JRH
	 * @date    2014年5月20日 上午11:58:30
	 * @return Button
	 */
	public Button getSelectAll_bt() {
		return selectAll_bt;
	}

	@Override
	public String getMinimizedValueAsHTML() {
		StringBuffer htmlStrBf = new StringBuffer();
		
//		htmlStrBf.append("<div class='v-filterselect' style='width: 93px;'>");
//		htmlStrBf.append("<input class='v-filterselect-input' type='text' style='width: 77px;' value='请选择显示列'></input>");
//		htmlStrBf.append("<div class='v-filterselect-button'></div>");
//		htmlStrBf.append("</div>");
		
		htmlStrBf.append("<div class='v-filterselect' style='width: 112px;'>");
		htmlStrBf.append("<div class='v-filterselect-button'></div>");
		htmlStrBf.append("请选择显示列");
		htmlStrBf.append("</div>");

		return htmlStrBf.toString();
	}

	@Override
	public Component getPopupComponent() {
		return this;
	}
	
}
