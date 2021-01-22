package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.DiliverStatus;
import com.jiangyifen.ec2.entity.enumtype.PayStatus;
import com.jiangyifen.ec2.entity.enumtype.QualityStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.OrderService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.order.EditOrderWindow;
import com.jiangyifen.ec2.ui.mgr.ordermanange.ExportOrderWindow;
import com.jiangyifen.ec2.ui.mgr.servicerecord.MgrCdrTable;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer.ComponentAttachListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 订单管理
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class OrderManagement extends VerticalLayout implements ComponentAttachListener,
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	private static final String BUSINESS_MANAGEMENT_DOWNLOAD_ORDER_INFO = "business_management&download_order_info";
	
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField orderCustomerName;
	private TextField orderCustomerPhoneNumber;
	private ComboBox qualifyStatus;
	private ComboBox diliverStatus;
	private TextField projectName_tf;	// jrh 按项目名称查询

	private ComboBox timeScope;			// “时间范围”选择框
	private PopupDateField startTime;	// “开始时间”选择框
	private PopupDateField finishTime;	// “截止时间”选择框
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	private Button searchButton; 		// 刷新结果按钮
	private Button clearButton; 		// 清空输入内容
	private Button editOrder_bt;		// jrh 编辑订单
	private Button exportOrder;
	
	private ExportOrderWindow exportOrderWindow;
	
	private MgrCdrTable mgrCdrTable;
	
	// 订单表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<Order> flip;

	// 订单表格按钮组件
	private Button delete;

	/**
	 * 右键组件
	 */
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] {DELETE };
	
	/**
	 * 弹出窗口
	 */
	private EditOrderWindow editOrderWindow;				// 编辑订单界面
	
	private Table orderDetailsTable;
	private BeanItemContainer<Orderdetails> orderDetailsContainer;
	
	/**
	 * 其他组件
	 */
	private boolean isEncryptMobile = true; // 默认使用加密的电话号码和手机号
	private ArrayList<String> ownBusinessModels;
	private CommonService commonService;
	private OrderService orderService;
	private UserService userService;
	// 如果当前有选中的订单则会存储当前选中的订单，如果没有选中的订单则会存储null
	private Order currentSelectOrder;
	private CdrService cdrService;
	private TelephoneService telephoneService;
//	private Domain domain;
//	
	private User loginUser;

	//外围组件
	private VerticalLayout progressOuterLayout;
	
	/**
	 * 构造器
	 */
	public OrderManagement() {
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
		searchButton.click();
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());
		
		// 初始化
		mgrCdrTable = new MgrCdrTable(isEncryptMobile);
	}

	/**
	 * 将Service进行初始化
	 */
	private void initService() {
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		loginUser = SpringContextHolder.getLoginUser();
//		domain = SpringContextHolder.getDomain();
		userService= SpringContextHolder.getBean("userService");
		orderService = SpringContextHolder
				.getBean("orderService");
		cdrService=SpringContextHolder.getBean("cdrService");
		commonService = SpringContextHolder.getBean("commonService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
	}

	/**
	 * 创建搜索组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		// Grid布局管理器
		GridLayout gridLayout = new GridLayout(9, 3);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false, true, false, true);
		searchLayout.addComponent(gridLayout);
		
		// 时间范围选中框
		gridLayout.addComponent(new Label("时间范围："), 0, 0);
		gridLayout.addComponent(buildTimeScopeComboBox(), 1, 0);
		
		// 开始时间选中框
		gridLayout.addComponent(new Label("起始下单："), 2, 0);
		
		startTime = new PopupDateField();
		startTime.setWidth("160px");
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTime.setImmediate(true);
		startTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("全部");
				timeScope.addListener(timeScopeListener);
			}
		};
		startTime.addListener(startTimeListener);
		gridLayout.addComponent(startTime, 3, 0);

		// 截止时间选中框
		gridLayout.addComponent(new Label("截止下单："), 4, 0);
		
		finishTime = new PopupDateField();
		finishTime.setWidth("160px");
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTime.setImmediate(true);
		finishTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("全部");
				timeScope.addListener(timeScopeListener);
			}
		};
		finishTime.addListener(finishTimeListener);
		gridLayout.addComponent(finishTime, 5, 0);

		// 发货状态
		Label diliverStatus_lb = new Label("发货状态:");
		diliverStatus_lb.setWidth("-1px");
		gridLayout.addComponent(diliverStatus_lb,6,0);// 关键字
		diliverStatus = new ComboBox();
		diliverStatus.setWidth("160px");
		diliverStatus.setInputPrompt("全部");
		diliverStatus.addItem(DiliverStatus.NOTDILIVERED);
		diliverStatus.addItem(DiliverStatus.DILIVERED);
		diliverStatus.addItem(DiliverStatus.RECEIVEED);
		gridLayout.addComponent(diliverStatus,7,0);
		
		// 查询按钮
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		searchButton.addStyleName("small");
		gridLayout.addComponent(searchButton, 8, 0);

		gridLayout.addComponent(new Label("订货人:"),0,1);// 关键字
		orderCustomerName = new TextField();// 输入区域
		orderCustomerName.setWidth("90px");
		orderCustomerName.setInputPrompt("订货人姓名");
		gridLayout.addComponent(orderCustomerName,1,1);

		gridLayout.addComponent(new Label("订货电话:"),2,1);// 关键字
		orderCustomerPhoneNumber = new TextField();// 输入区域
		orderCustomerPhoneNumber.setWidth("160px");
		orderCustomerPhoneNumber.setInputPrompt("订货人电话");
		gridLayout.addComponent(orderCustomerPhoneNumber,3,1);
		
		// 订单状态
		gridLayout.addComponent(new Label("质检状态:"),4,1);// 关键字
		qualifyStatus = new ComboBox();
		qualifyStatus.setWidth("160px");
		qualifyStatus.setInputPrompt("全部");
		qualifyStatus.addItem(QualityStatus.CONFIRMING);
		qualifyStatus.addItem(QualityStatus.CONFIRMED);
		qualifyStatus.addItem(QualityStatus.FINISHED);
		qualifyStatus.addItem(QualityStatus.CANCELED);
		gridLayout.addComponent(qualifyStatus,5,1);
		
		// jrh 下单项目
		Label project_lb = new Label("下单项目:");
		project_lb.setWidth("-1px");
		gridLayout.addComponent(project_lb, 6, 1);// 关键字
		projectName_tf = new TextField();// 输入区域
		projectName_tf.setWidth("160px");
		projectName_tf.setInputPrompt("项目名称");
		gridLayout.addComponent(projectName_tf, 7, 1);
		
		// 清空按钮
		clearButton = new Button("清 空", (ClickListener) this);
		clearButton.addStyleName("small");
		gridLayout.addComponent(clearButton, 8, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
		
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
				if(v == null) {
					return "";
				} else if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
							.format(v);
				} else if("customerPhoneNumber".equals(colId)) {
					if(isEncryptMobile) {
						return telephoneService.encryptMobileNo(v.toString());
					}
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

		// 创建订单详情显示表格
		tabelAndButtonsLayout.addComponent(createOrderDetailsTable());

		return tabelAndButtonsLayout;
	}
	
	/**
	 * 创建时间段的组合框
	 * 
	 * @return
	 */
	private ComboBox buildTimeScopeComboBox() {
		
		timeScope = new ComboBox();
		timeScope.addItem("全部");
		timeScope.addItem("今天");
		timeScope.addItem("昨天");
		timeScope.addItem("本周");
		timeScope.addItem("上周");
		timeScope.addItem("本月");
		timeScope.addItem("上月");
		timeScope.setValue("全部");
		timeScope.setWidth("90px");
		timeScope.setNullSelectionAllowed(false);
		timeScope.setImmediate(true);
		timeScopeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				startTime.removeListener(startTimeListener);
				finishTime.removeListener(finishTimeListener);
				String scopeValue=(String)timeScope.getValue();
				if(scopeValue.equals("全部")){
					startTime.setValue(null);
					finishTime.setValue(null);
				}else{
					Date[] dates=parseToDate((String)timeScope.getValue());
					startTime.setValue(dates[0]);
					finishTime.setValue(dates[1]);
				}
				startTime.addListener(startTimeListener);
				finishTime.addListener(finishTimeListener);
			}
		};
		timeScope.addListener(timeScopeListener);
		return timeScope;
	}

	/**
	 *  创建订单详情显示表格
	 */
	private Table createOrderDetailsTable() {
		
		Object[] VISIBLE_PROPERTIES_DETAILS = new Object[] {"id", "commodity.commodityName", 
				"commodity.commodityPrice", "salePrice", "orderNum", "subTotalPrice", "commodity.description"};
		

		String[] COL_HEADERS_DETAILS = new String[] {"详单编号", "商品名称", "商品单价", "售出单价", "订购数量", "小计", "商品描述"};
		
		
		orderDetailsContainer = new BeanItemContainer<Orderdetails>(Orderdetails.class);
		for(int i = 0; i < VISIBLE_PROPERTIES_DETAILS.length; i++) {
			orderDetailsContainer.addNestedContainerProperty(VISIBLE_PROPERTIES_DETAILS[i].toString());
		}
		
		orderDetailsTable = new Table("订单详情：");
		orderDetailsTable.setWidth("100%");
		orderDetailsTable.setHeight("-1px");
		orderDetailsTable.setPageLength(4);
		orderDetailsTable.setSelectable(false);
		orderDetailsTable.setStyleName("striped");
		orderDetailsTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		orderDetailsTable.setContainerDataSource(orderDetailsContainer);
		orderDetailsTable.setVisibleColumns(VISIBLE_PROPERTIES_DETAILS);
		orderDetailsTable.setColumnHeaders(COL_HEADERS_DETAILS);
		orderDetailsTable.addGeneratedColumn("commodity.description", new DescriptionColumnGenerator());
		orderDetailsTable.setColumnExpandRatio("commodity.description", 0.5f);
		return orderDetailsTable;
	}
	
	/**
	 * 创建商品描述信息组件
	 */
	private class DescriptionColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Commodity commodity = ((Orderdetails) itemId).getCommodity();
			if(columnId.equals("commodity.description")) {
				String description = commodity.getDescription();
				if(description != null) {
					String label_caption = description;
					if(label_caption.length() > 40) {
						label_caption = label_caption.substring(0, 40) + "...";
					}
					Label description_label = new Label(label_caption);
					description_label.setWidth("-1px");
					description_label.setDescription(description);
					return description_label;
				}
			} 
			return null;
		}
	}

	/**
	 * @Description 描述：显示编辑订单的界面
	 *
	 * @author  jrh
	 * @date    2014年3月11日 下午3:39:44 void
	 */
	private void showEditOrderWindow() {
		if(editOrderWindow == null) {
			editOrderWindow = new EditOrderWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(editOrderWindow);
		
		editOrderWindow.updateUiDatas((Order) table.getValue());
		this.getApplication().getMainWindow().addWindow(editOrderWindow);
	}
	
	/**
	 * 由buttonClick
	 */
	private void showExportOrderWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (exportOrderWindow == null) {
			exportOrderWindow =new ExportOrderWindow(this);
		} 
		this.getWindow().addWindow(exportOrderWindow);
	}
	
	/**
	 * 解析字符串到日期
	 * @param timeScopeValue
	 * @return
	 */
	// 将用户选择的组合框中的值解析为日期字符串
	private static Date[] parseToDate(String timeScopeValue) {
		Date[] dates = new Date[2]; // 存放开始于结束时间
		Calendar cal = Calendar.getInstance(); // 取得当前时间
		cal.setTime(new Date());

		if ("今天".equals(timeScopeValue)) {
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("昨天".equals(timeScopeValue)) {
			cal.add(Calendar.DAY_OF_WEEK, -1);
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("本周".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 本周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 本周的最后一天
		} else if ("本月".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 本月第一天

			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 本月最后一天
		} else if ("上周".equals(timeScopeValue)) {
			cal.add(Calendar.WEEK_OF_MONTH, -1);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 上周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 上周的最后一天
		} else if ("上月".equals(timeScopeValue)) {
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 上月月第一天

			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 上月最后一天
		}
		//目的是将日期格式的时分秒设为00:00:00
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates[0]=sdf.parse(sdf.format(dates[0]));
			dates[1]=sdf.parse(sdf.format(dates[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dates;
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
		
//		//新建
//		edit= new Button("编辑");
//		edit.addListener((Button.ClickListener) this);
//		firstLineLayout.addComponent(edit);

		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
		delete = new Button("删除");
		delete.setEnabled(false);
		delete.setStyleName(StyleConfig.BUTTON_STYLE);
		delete.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(delete);

		editOrder_bt = new Button("编辑订单", this);
		editOrder_bt.setImmediate(true);
		editOrder_bt.setEnabled(false);
		firstLineLayout.addComponent(editOrder_bt);
		
		exportOrder= new Button("导出订单");
		exportOrder.setEnabled(true); // 创建时为不可用
		exportOrder.addListener((Button.ClickListener) this);
		if(ownBusinessModels.contains(BUSINESS_MANAGEMENT_DOWNLOAD_ORDER_INFO)) {
			firstLineLayout.addComponent(exportOrder);
		}
		
		//进度条组件
		progressOuterLayout=new VerticalLayout();
		firstLineLayout.addComponent(progressOuterLayout);
		
		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<Order>(
				Order.class, orderService, table,
				sqlSelect, sqlCount, null);
		table.setPageLength(10);
		flip.setPageLength(10, false);
		
//		BeanItemContainer<Order> container=(BeanItemContainer<Order>)table.getContainerDataSource();
//		container.addNestedContainerProperty("user.username");
		
		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "customerName","customerPhoneNumber","totalPrice","diliverStatus","qualityStatus","generateDate", "projectName"};
		String[] columnHeaders = new String[] { "ID","订货人姓名","订货人电话","订单总价","发货状态","订单状态","下单日期", "下单项目"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置表格的样式
		this.setStyleGeneratorForTable(table);
		// 生成备注列
		this.addColumn0(table);
		this.addColumn1(table);
		this.addColumn2(table);
		this.addProjectNameColumn(table);
		this.addColumn6(table);
		this.addColumn3(table);
		//通话查看
		this.addColumn5(table);

		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 由 buildButtonsAndFlipLayout调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn5(final Table table) {
		table.addGeneratedColumn("通话查看", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId, Object columnId) {
				// 使Popup创建多个,但Table只创建一个
				PopupView popup = new PopupView(new Content() {
					@Override
					public String getMinimizedValueAsHTML() {
						return "通话查看";
					}

					@Override
					public Component getPopupComponent() {
						return mgrCdrTable;
					}
				});
				popup.setData(itemId);
				popup.setHideOnMouseOut(false);
				popup.addListener(OrderManagement.this);
				return popup;
			}
		});
	}
	
	
	/**
	 * 由Popup组件的弹出事件attach调用
	 * 
	 * @param event
	 */
	@Override
	public void componentAttachedToContainer(ComponentAttachEvent event) {
		PopupView popupView = (PopupView) event.getComponent();
		Order order = (Order) popupView
				.getData();
		table.setValue(order);
		// 根据ServiceRecord查询，没查到，查到一个，查到多个
		List<Cdr> cdrs = cdrService.getRecordByOrder(order);
		mgrCdrTable.setTableItems(cdrs);
	}

	
	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn0(final Table table) {
		table.addGeneratedColumn("下单坐席", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Order order=(Order)itemId;
				Long csrId=order.getCsrUserId();
				User user=userService.get(csrId);
				String empNo=user.getEmpNo();
				String username=user.getUsername();
				Label label = new Label(empNo+"("+username+")");
				return label;
			}
		});
	}
	
	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn1(final Table table) {
		table.addGeneratedColumn("质检管理员", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Order order=(Order)itemId;
				Long mgrId=order.getMgrUserId();
				User mgr=null;
				if(mgrId!=null){
					mgr=userService.get(mgrId);
				}
				String mgrName="";
				if(mgr!=null&&!StringUtils.isEmpty(mgr.getUsername())){
					mgrName=mgr.getUsername();
				}
				Label label = new Label(mgrName);
				return label;
			}
		});
	}
	
	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn2(final Table table) {
		table.addGeneratedColumn("收货地址", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				Order order=(Order)itemId;
				String longNote = "";
				if(order.getProvince()!=null&&order.getCity()!=null&&order.getCounty()!=null){
					longNote=order.getProvince()+"(省)"+order.getCity()+"(市)"+order.getCounty()+"(区、县)";
				}
				//添加地址信息
				if(order.getStreet()!=null){
					longNote=longNote.concat("  "+order.getStreet().trim());
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
	 * jrh
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addProjectNameColumn(final Table table) {
		table.addGeneratedColumn("备注", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				Order order = (Order)itemId;
				String longNote = order.getNote();
				if(longNote == null) {
					return new Label();
				} 
				
				String shortNote = longNote;
				if (shortNote.length() > 5) {
					shortNote = shortNote.substring(0, 5) + "...";
				}
				Label label = new Label(shortNote);
				label.setDescription(longNote);
				label.setWidth("-1px");
				return label;
			}
		});
	}
	
	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn3(final Table table) {
		table.addGeneratedColumn("审核操作", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				final Order order=(Order)itemId;
//				if(order.getOrderStatus()==OrderStatus.CONFIRMING){
	
				MenuBar menuBar=new MenuBar();
				MenuItem menuItem = menuBar.addItem("审核", null);
				for(QualityStatus qualifyStatus:QualityStatus.values()){
					final QualityStatus qualityStatus= qualifyStatus;
					menuItem.addItem(qualifyStatus.toString(), new MenuBar.Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							order.setQualityStatus(qualityStatus);
							order.setMgrUserId(loginUser.getId());
							commonService.update(order);
							OrderManagement.this.refreshTable(false);
							NotificationUtil.showWarningNotification(OrderManagement.this,"订单 "+order.getId()+" 更改审核状态  "+qualityStatus.getName());
						}
					});
				}
				return menuBar;
			}
		});
	}
	
	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn6(final Table table) {
		table.addGeneratedColumn("发货操作", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				final Order order=(Order)itemId;
//				if(order.getOrderStatus()==OrderStatus.CONFIRMING){
				
				MenuBar menuBar=new MenuBar();
				MenuItem menuItem = menuBar.addItem("发货", null);
				for(DiliverStatus diliverStatus:DiliverStatus.values()){
					final DiliverStatus fDiliverStatus= diliverStatus;
					menuItem.addItem(diliverStatus.toString(), new MenuBar.Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							order.setDiliverStatus(fDiliverStatus);
							commonService.update(order);
							OrderManagement.this.refreshTable(false);
							NotificationUtil.showWarningNotification(OrderManagement.this,"订单 "+order.getId()+" 更改发货状态  "+fDiliverStatus.getName());
						}
					});
				}
				return menuBar;
			}
		});
	}

	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	@SuppressWarnings("unused")
	private void addColumn4(final Table table) {
		table.addGeneratedColumn("详单查看", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Order order=(Order)itemId;
//				Long csrId=order.getCsrUserId();
//				User user=userService.get(csrId);
//				String empNo=user.getEmpNo();
//				String username=user.getUsername();
//				Label label = new Label(empNo+"("+username+")");
				Button button=new Button("查看");
				button.setStyleName(BaseTheme.BUTTON_LINK);
				button.addListener(new Button.ClickListener() {
					
					@Override
					public void buttonClick(ClickEvent event) {
						NotificationUtil.showWarningNotification(OrderManagement.this,"Not Finished!!!");
					}
				});
				return button;
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
				return null;
			}
		});
	}

	/**
	 * 由buttonClick 调用，执行搜索功能
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("Order");
		// 关键字过滤,订单名
		String orderCustomerNameStr = orderCustomerName.getValue().toString();

		//订货人
		SqlGenerator.Like customerName = new SqlGenerator.Like("customerName",
				orderCustomerNameStr);
		sqlGenerator.addAndCondition(customerName);

		//订货电话
		String ordererPhoneNumberStr = orderCustomerPhoneNumber.getValue().toString();
		SqlGenerator.Like ordererPhoneNumber = new SqlGenerator.Like("customerPhoneNumber", ordererPhoneNumberStr);
		sqlGenerator.addAndCondition(ordererPhoneNumber);

		//时间范围
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeFromStr="";
		if(startTime.getValue()!=null) timeFromStr=sdf.format(startTime.getValue()).toString();
		String timeToStr="";
		if(finishTime.getValue()!=null) timeToStr=sdf.format(finishTime.getValue()).toString();
		SqlGenerator.Between timeBetween=new SqlGenerator.Between("generateDate", timeFromStr, timeToStr, true);
		sqlGenerator.addAndCondition(timeBetween);
		
		// 订单状态
		QualityStatus orderStatu = (QualityStatus) qualifyStatus
				.getValue();
		if (orderStatu != null) {
			String statuStr = orderStatu.getClass().getName() + ".";
			if (orderStatu.getIndex() == 0) {
				statuStr += "CONFIRMING";
			} else if (orderStatu.getIndex() == 1) {
				statuStr += "CONFIRMED";
			}if (orderStatu.getIndex() == 2) {
				statuStr += "FINISHED";
			} else if (orderStatu.getIndex() == 3) {
				statuStr += "CANCELED";
			}
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"qualityStatus", statuStr, false);
			sqlGenerator.addAndCondition(statu);
		}

		// 发货状态
		DiliverStatus diliverStatu = (DiliverStatus) diliverStatus.getValue();
		if (diliverStatu != null) {
			String statuStr = diliverStatu.getClass().getName() + ".";
			if (diliverStatu.getIndex() == 0) {
				statuStr += "NOTDILIVERED";
			} else if (diliverStatu.getIndex() == 1) {
				statuStr += "DILIVERED";
			}else if (diliverStatu.getIndex() == 2) {
				statuStr += "RECEIVEED";
			}
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"diliverStatus", statuStr, false);
			sqlGenerator.addAndCondition(statu);
		}

		//订货电话
		String projectNameStr = StringUtils.trimToEmpty((String) projectName_tf.getValue());
		if(!"".equals(projectNameStr)) {
			SqlGenerator.Like projectName = new SqlGenerator.Like("projectName", projectNameStr);
			sqlGenerator.addAndCondition(projectName);
		}
		
		// 排序
		sqlGenerator.setOrderBy("id", SqlGenerator.DESC);

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		
		sqlCount = sqlGenerator.generateCountSql();
		// 更新Table，并使Table处于未选中
		this.refreshTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}

	/**
	 * 执行删除操作
	 */
	private void executeDelete() {
		// 在confirmDelete方法中删除与批次的关联、与用户的关联
		Label label = new Label("您确定要删除订单?", Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmDelete");
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}
	
	/**
	 * 确定删除
	 * @param isConfirmed
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if(isConfirmed){
			CommonService commonService = SpringContextHolder.getBean("commonService");
			// 先删详单，再删订单
			for(Orderdetails orderDetail : currentSelectOrder.getOrderdetails()) {
				commonService.delete(Orderdetails.class, orderDetail.getId());
			}
			commonService.delete(Order.class, currentSelectOrder.getId());
			//更新Table，并使Table处于未选中
			this.refreshTable(true);
			if (table != null) {
				table.setValue(null);
				orderDetailsContainer.removeAllItems();
			}
		}
	}

	/**
	 * 由executeSearch调用更新表格内容
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 * @param isToFirst
	 */
	public void refreshTable(boolean isToFirst) {
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
	 * 返回FlipOver的一个引用
	 * 
	 * @return
	 */
	public FlipOverTableComponent<Order> getFlip() {
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

	public String getSqlSelect() {
		return sqlSelect;
	}
	
	public String getSqlCount() {
		return sqlCount;
	}
	
	/**
	 * 取得当前选中的订单
	 * 
	 * @return
	 */
	public Order getCurrentSelect() {
		return currentSelectOrder;
	}

	/**
	 * 进度组件
	 * @return
	 */
	public VerticalLayout getProgressLayout(){
		return progressOuterLayout;
	}
	
	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[] {};
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
//		if (EDIT == action) {
//			edit.click();
//		} else 
		if (DELETE == action) {
			delete.click();
		}
	}
	
	/**
	 * 初始化表格
	 */
	@Override
	public void attach() {
		super.attach();
//		edit.setEnabled(false);
		if(table!=null){
			this.refreshTable(true);
		}
	}
	
	/**
	 * 按钮单击监听器
	 * <p>
	 * 搜索、高级搜索、新建订单、开始、停止、添加CSR/添加资源 、指派任务
	 * </p>
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == searchButton) {
			// 普通搜索
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("搜索出现错误");
			}
		} else if (event.getButton() == delete) {
			// 订单删除
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("删除出现异常！");
			}
		} else if (clearButton == event.getButton()) {
			//初始化查询条件的选择
			startTime.setValue(null);
			finishTime.setValue(null);
			qualifyStatus.setValue(null);
			orderCustomerName.setValue("");
			orderCustomerPhoneNumber.setValue("");
			projectName_tf.setValue("");
		} else if (editOrder_bt == event.getButton()) {
			showEditOrderWindow();
		} else if (exportOrder == event.getButton()) {
			showExportOrderWindow();
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
			delete.setEnabled(true);
			// jrh 添加编辑功能
			Order order = (Order) table.getValue();
			if(PayStatus.PAYED.equals(order.getPayStatus()) || !DiliverStatus.NOTDILIVERED.equals(order.getDiliverStatus()) 
					|| !QualityStatus.CONFIRMING.equals(order.getQualityStatus())) {	// 一但订单已经发货、已质检、已支付，满足上面的任何一种，都不能再修改订单
				editOrder_bt.setEnabled(false);
			} else {
				editOrder_bt.setEnabled(true);
			}
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			delete.setEnabled(false);
			editOrder_bt.setEnabled(false);
		}
		// 维护表格中当前选中的订单
		currentSelectOrder = (Order) table.getValue();
		
		if(currentSelectOrder != null) {
			orderDetailsContainer.removeAllItems();
			orderDetailsContainer.addAll(orderService.getOrderDetailsByOrderId(currentSelectOrder.getId()));
		}
	}
}
