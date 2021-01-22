package com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr.otil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.service.eaoservice.OutlineToIvrLinkService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述：添加外线与IVR 的关联界面
 * 
 * @author  jrh
 * @date    2014年3月6日 下午8:30:46
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class AddOtilWindow extends Window implements Button.ClickListener, ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ComboBox ivrMenus_cb;				// IVRMenu 选择
	private OptionGroup dateType_og;			// 日期范围选择[永久生效，自定义]
	private HorizontalLayout effectDate_hlo;	// 存放 effectDate_pdf 、 expireDate_pdf等组件
	private PopupDateField effectDate_pdf;		// 起效日期
	private PopupDateField expireDate_pdf;		// 失效日期
	private OptionGroup dayOfWeekType_og;		// 生效日类型(工作日、周末、自定义)
	private OptionGroup daysOfWeek_og;			// 具体生效日(星期一、星期二、星期三...)
	private ComboBox startHour_cb;				// 开始生效的小时设置
	private ComboBox startMin_cb;				// 开始生效的分钟设置
	private ComboBox stopHour_cb;				// 结束失效的小时设置
	private ComboBox stopMin_cb;				// 结束失效的分钟设置

	private Button save_bt;										// 保存按钮
	private Button cancel_bt;									// 取消按钮
	
	private OutlineToIvrLinkManageView otilManageView;

	private SipConfig outline; 
	private Domain domain;										// 当前用户所属域
	private BeanItemContainer<IVRMenu> ivrMenuContainer;		// IVRMenu集合
    
	private IvrMenuService ivrMenuService;
	private OutlineToIvrLinkService outlineToIvrLinkService;
	
	public AddOtilWindow(OutlineToIvrLinkManageView otilManageView) {
		this.setCaption("添加 (按外线配置呼入IVR)");
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.otilManageView = otilManageView;
		
		domain = SpringContextHolder.getDomain();
		ivrMenuContainer = new BeanItemContainer<IVRMenu>(IVRMenu.class);

		ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
		outlineToIvrLinkService = SpringContextHolder.getBean("outlineToIvrLinkService");

		// 创建menu时弹出的窗口中最外层组件
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);

		GridLayout gridLayout = new GridLayout(2, 4);
		gridLayout.setSpacing(true);
		gridLayout.setColumnExpandRatio(1, 1.0f);
		this.addComponent(gridLayout);
		
		this.createOutlineSelectorComponent(gridLayout);
		this.createEffectDates(gridLayout);
		this.createEffectDays(gridLayout);
		this.createEffectTimeScope(gridLayout);
		this.createOperateUi(windowContent);

	}

	/**
	 * 创建外线选择器
	 * @param main_pn
	 */
	private void createOutlineSelectorComponent(GridLayout gridLayout) {
		Label caption = new Label("IVR关联：");
		caption.setWidth("-1px");
		gridLayout.addComponent(caption, 0, 0);
		gridLayout.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
		
		ivrMenus_cb = new ComboBox();
		ivrMenus_cb.setNullSelectionAllowed(false);
		ivrMenus_cb.setImmediate(true);
		ivrMenus_cb.setWidth("360px");
		ivrMenus_cb.setContainerDataSource(ivrMenuContainer);
		gridLayout.addComponent(ivrMenus_cb, 1, 0);
	}
	
	/**
	 * 生效日期组件
	 * @author jrh
	 * @param gridLayout
	 */
	private void createEffectDates(GridLayout gridLayout) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Label caption = new Label("生效日期：");
		caption.setWidth("-1px");
		gridLayout.addComponent(caption, 0, 1);
		
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 1, 1);
		
		dateType_og = new OptionGroup();
		dateType_og.addItem("forever");
		dateType_og.addItem("custom");
		dateType_og.setItemCaption("forever", "永久");
		dateType_og.setItemCaption("custom", "自定义");
		dateType_og.setImmediate(true);
		dateType_og.addListener(this);
		dateType_og.setWidth("450px");
		dateType_og.addStyleName("threecol");
		layout.addComponent(dateType_og);

		effectDate_hlo = new HorizontalLayout();
		effectDate_hlo.setSpacing(true);
		effectDate_hlo.setVisible(false);
		effectDate_hlo.setSizeUndefined();
		layout.addComponent(effectDate_hlo);
		
		effectDate_pdf = new PopupDateField();
		effectDate_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		effectDate_pdf.setInputPrompt("如:2012-08-18");
		effectDate_pdf.setDateFormat("yyyy-MM-dd");
		effectDate_pdf.setValidationVisible(false);
		effectDate_pdf.setWidth("167px");
		effectDate_pdf.setImmediate(true);
		effectDate_pdf.setValue(calendar.getTime());
		effectDate_hlo.addComponent(effectDate_pdf);

		Label separate_lb = new Label(" 至 ");
		separate_lb.setWidth("-1px");
		effectDate_hlo.addComponent(separate_lb);

		calendar.add(Calendar.DAY_OF_YEAR, +60);
		
		expireDate_pdf = new PopupDateField();
		expireDate_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		expireDate_pdf.setInputPrompt("如:2012-08-19");
		expireDate_pdf.setDateFormat("yyyy-MM-dd");
		expireDate_pdf.setValidationVisible(false);
		expireDate_pdf.setImmediate(true);
		expireDate_pdf.setWidth("167px");
		expireDate_pdf.setValue(calendar.getTime());
		effectDate_hlo.addComponent(expireDate_pdf);
		
		// 设置默认值
		dateType_og.setValue("forever");
	}
	
	/**
	 * 创建转接日期设置组件 (工作日、双休日、自定义)
	 * @param panelContent
	 */
	private void createEffectDays(GridLayout gridLayout) {
		Label caption = new Label("周几生效：");
		caption.setWidth("-1px");
		gridLayout.addComponent(caption, 0, 2);

		VerticalLayout dayOfWeekType_vlo = new VerticalLayout();
		dayOfWeekType_vlo.setSpacing(true);
		gridLayout.addComponent(dayOfWeekType_vlo, 1, 2);

		dayOfWeekType_og = new OptionGroup();
		dayOfWeekType_og.addItem("weekday");
		dayOfWeekType_og.setItemCaption("weekday", "工作日");
		dayOfWeekType_og.addItem("weekend");
		dayOfWeekType_og.setItemCaption("weekend", "周末");
		dayOfWeekType_og.addItem("custom");
		dayOfWeekType_og.setItemCaption("custom", "自定义");
		dayOfWeekType_og.setWidth("450px");
		dayOfWeekType_og.addStyleName("threecol");
		dayOfWeekType_og.setValue("weekday");
		dayOfWeekType_og.setNullSelectionAllowed(false);
		dayOfWeekType_og.setImmediate(true);
		dayOfWeekType_og.addListener(this);
		dayOfWeekType_og.addStyleName("myopacity");
		dayOfWeekType_vlo.addComponent(dayOfWeekType_og);

		BeanItemContainer<DayOfWeek> container = new BeanItemContainer<DayOfWeek>(DayOfWeek.class);
		container.addBean(DayOfWeek.mon);
		container.addBean(DayOfWeek.tue);
		container.addBean(DayOfWeek.wen);
		container.addBean(DayOfWeek.thu);
		container.addBean(DayOfWeek.fri);
		container.addBean(DayOfWeek.sat);
		container.addBean(DayOfWeek.sun);
		
		daysOfWeek_og = new OptionGroup();
		daysOfWeek_og.setContainerDataSource(container);
		daysOfWeek_og.setItemCaptionPropertyId("name");
		daysOfWeek_og.setMultiSelect(true);
		daysOfWeek_og.setNullSelectionAllowed(false);
		daysOfWeek_og.setImmediate(true);
		daysOfWeek_og.addStyleName("threecol");
		daysOfWeek_og.addStyleName("myopacity");
		dayOfWeekType_vlo.addComponent(daysOfWeek_og);
		
		dayOfWeekType_og.setValue("weekday");	// 设置默认值
	}
	
	/**
	 * 创建执行呼叫手机的具体时间设置 (18:00 - 23:59 )
	 * @param panelContent
	 */
	private void createEffectTimeScope(GridLayout gridLayout) {
		Label caption = new Label("生效时段：");
		caption.setWidth("-1px");
		gridLayout.addComponent(caption, 0, 3);

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 1, 3);
		
		startHour_cb = new ComboBox();
		startHour_cb.setImmediate(true);
		startHour_cb.setWidth("57px");
		startHour_cb.setNullSelectionAllowed(false);
		layout.addComponent(startHour_cb);
		
		Label space1 = new Label(" ：");
		space1.setWidth("-1px");
		layout.addComponent(space1);

		startMin_cb = new ComboBox();
		startMin_cb.setImmediate(true);
		startMin_cb.setWidth("57px");
		startMin_cb.setNullSelectionAllowed(false);
		layout.addComponent(startMin_cb);

		Label to = new Label(" 至 ");
		to.setWidth("-1px");
		layout.addComponent(to);

		stopHour_cb = new ComboBox();
		stopHour_cb.setImmediate(true);
		stopHour_cb.setWidth("57px");
		stopHour_cb.setNullSelectionAllowed(false);
		layout.addComponent(stopHour_cb);
		
		Label space2 = new Label(" ：");
		space2.setWidth("-1px");
		layout.addComponent(space2);

		stopMin_cb = new ComboBox();
		stopMin_cb.setImmediate(true);
		stopMin_cb.setWidth("57px");
		stopMin_cb.setNullSelectionAllowed(false);
		layout.addComponent(stopMin_cb);

		for(int hour = 0; hour < 24; hour++) {
			startHour_cb.addItem(hour);
			stopHour_cb.addItem(hour);
		}
		
		for(int minute = 0; minute < 60; minute++) {
			startMin_cb.addItem(minute);
			stopMin_cb.addItem(minute);
		}
		
		startHour_cb.setValue(8);
		stopHour_cb.setValue(23);
		startMin_cb.setValue(0);
		stopMin_cb.setValue(59);
	}

	/**
	 * 创建操作组件
	 * @param windowContent 窗口布局管理器
	 */
	private void createOperateUi(VerticalLayout windowContent) {
		// 存放下一步，取消按键
		HorizontalLayout operateUi_hlo = new HorizontalLayout();
		operateUi_hlo.setSpacing(true);
		operateUi_hlo.setWidth("-1px");
		windowContent.addComponent(operateUi_hlo);

		// 保存按键
		save_bt = new Button("保存", this);
		save_bt.setStyleName("default");
		save_bt.setImmediate(true);
		operateUi_hlo.addComponent(save_bt);
		operateUi_hlo.setComponentAlignment(save_bt, Alignment.BOTTOM_LEFT);

		// 取消menu
		cancel_bt = new Button("取消", this);
		cancel_bt.setImmediate(true);
		operateUi_hlo.addComponent(cancel_bt);
		operateUi_hlo.setComponentAlignment(cancel_bt, Alignment.BOTTOM_LEFT);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == dateType_og) {
			String type = (String) dateType_og.getValue();
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			if("forever".equals(type)) {
				effectDate_pdf.setValue(cal.getTime());
				cal.add(Calendar.YEAR, +500);
				expireDate_pdf.setValue(cal.getTime());
				effectDate_hlo.setVisible(false);
			} else if("custom".equals(type)) {
				effectDate_pdf.setValue(cal.getTime());
				cal.add(Calendar.DAY_OF_YEAR, +60);
				expireDate_pdf.setValue(cal.getTime());
				effectDate_hlo.setVisible(true);
			}
		} else if (source == dayOfWeekType_og) {
			String type = (String) dayOfWeekType_og.getValue();
			Set<DayOfWeek> set = new HashSet<DayOfWeek>();
			daysOfWeek_og.setReadOnly(false);
			if("weekday".equals(type)) {
				set.add(DayOfWeek.mon);
				set.add(DayOfWeek.tue);
				set.add(DayOfWeek.wen);
				set.add(DayOfWeek.thu);
				set.add(DayOfWeek.fri);
				daysOfWeek_og.setValue(set);
				daysOfWeek_og.setReadOnly(true);
			} else if ("weekend".equals(type)) {
				set.add(DayOfWeek.sat);
				set.add(DayOfWeek.sun);
				daysOfWeek_og.setValue(set);
				daysOfWeek_og.setReadOnly(true);
			} else if ("custom".equals(type)) {
				set.add(DayOfWeek.mon);
				set.add(DayOfWeek.tue);
				set.add(DayOfWeek.wen);
				set.add(DayOfWeek.thu);
				set.add(DayOfWeek.fri);
				set.add(DayOfWeek.sat);
				set.add(DayOfWeek.sun);
				daysOfWeek_og.setValue(set);
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_bt) {
			try {
				excuteSave();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("对不起，按外线配置呼入IVR失败！", Notification.TYPE_WARNING_MESSAGE);
				logger.error("jrh 管理员按外线配置呼入IVR保存 时出现异常--->"+e.getMessage(), e);
			}
		} else if(source == cancel_bt) {
			this.getApplication().getMainWindow().removeWindow(this);	// 关闭弹出的窗口
		}
	}
	

	@SuppressWarnings("unchecked")
	private void excuteSave() {
		IVRMenu ivrMenu = (IVRMenu) ivrMenus_cb.getValue();
		if(ivrMenu == null) {
			this.getApplication().getMainWindow().showNotification("对不起，IVR语音导航流程不能为空！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		// 获取参数
		Set<DayOfWeek> dayOfWeeks = (Set<DayOfWeek>) daysOfWeek_og.getValue();
		Date effectDate = (Date) effectDate_pdf.getValue();
		Date expireDate = (Date) expireDate_pdf.getValue();
		int startHour = (Integer) startHour_cb.getValue();
		int startMin = (Integer) startMin_cb.getValue();
		int stopHour = (Integer) stopHour_cb.getValue();
		int stopMin = (Integer) stopMin_cb.getValue();
		
		// 创建新对象
		OutlineToIvrLink outlineToIvrLink = new OutlineToIvrLink();
		outlineToIvrLink.setIvrMenuId(ivrMenu.getId());
		outlineToIvrLink.setOutlineId(outline.getId());
		outlineToIvrLink.setDomainId(domain.getId());
		outlineToIvrLink.setCreateDate(new Date());
		outlineToIvrLink.setEffectDate(effectDate);
		outlineToIvrLink.setExpireDate(expireDate);
		outlineToIvrLink.setStartHour(startHour);
		outlineToIvrLink.setStartMin(startMin);
		outlineToIvrLink.setStopHour(stopHour);
		outlineToIvrLink.setStopMin(stopMin);
		outlineToIvrLink.setIsMonEffect(dayOfWeeks.contains(DayOfWeek.mon));
		outlineToIvrLink.setIsTueEffect(dayOfWeeks.contains(DayOfWeek.tue));
		outlineToIvrLink.setIsWedEffect(dayOfWeeks.contains(DayOfWeek.wen));
		outlineToIvrLink.setIsThuEffect(dayOfWeeks.contains(DayOfWeek.thu));
		outlineToIvrLink.setIsFriEffect(dayOfWeeks.contains(DayOfWeek.fri));
		outlineToIvrLink.setIsSatEffect(dayOfWeeks.contains(DayOfWeek.sat));
		outlineToIvrLink.setIsSunEffect(dayOfWeeks.contains(DayOfWeek.sun));
		
		outlineToIvrLinkService.save(outlineToIvrLink);

		// 执行成功后，更新内存
		ArrayList<OutlineToIvrLink> otilArrLs = ShareData.outlineIdToIvrLinkMap.get(outline.getId());
		if(otilArrLs == null) {
			otilArrLs = new ArrayList<OutlineToIvrLink>();
			ShareData.outlineIdToIvrLinkMap.put(outline.getId(), otilArrLs);
		}
		otilArrLs.add(outlineToIvrLink);
		
		// 刷新界面，关闭当前窗口
		otilManageView.refreshTable(true);
		this.getApplication().getMainWindow().showNotification("添加成功！");
		this.getApplication().getMainWindow().removeWindow(this);
	}

	/**
	 * 每次显示该弹窗时，需要处理该弹窗中对应的组件值，或数据源
	 * @author jrh
	 * @param outline	当前操作的外线对象
	 */
	public void updateUiDataSouce(SipConfig outline) {
		this.outline = outline;
		
		ivrMenuContainer.removeAllItems();
		ivrMenuContainer.addAll(ivrMenuService.getAllByDomain(domain.getId(), IVRMenuType.customize));
		
		ivrMenus_cb.setValue(null);
		dateType_og.setValue("forever");
		dayOfWeekType_og.setValue(null);
		dayOfWeekType_og.setValue("weekday");
		startHour_cb.setValue(8);
		startMin_cb.setValue(0);
		stopHour_cb.setValue(23);
		stopMin_cb.setValue(59);
	}

}
