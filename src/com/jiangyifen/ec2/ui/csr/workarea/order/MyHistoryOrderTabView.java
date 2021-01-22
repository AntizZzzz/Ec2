package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.DiliverStatus;
import com.jiangyifen.ec2.entity.enumtype.PayStatus;
import com.jiangyifen.ec2.entity.enumtype.QualityStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.OrderService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomDialPopupWindow;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.common.DialComponentToTable;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * 我的订单显示界面
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class MyHistoryOrderTabView extends VerticalLayout implements ValueChangeListener, ClickListener {

	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "generateDate", 
			"customerResource.id", "customerResource.name", "customerResource.telephones", "customerName", 
			"customerPhoneNumber", "street", "totalPrice", "diliverStatus", "qualityStatus"};
	
	private final String[] COL_HEADERS = new String[] {"记录编号", "下单时间", "客户编号", "客户姓名", 
			"电话号码", "收件人姓名", "收件人电话", "收件人地址", "订单总价", "发货状态", "质检状态"};
	
	private final Object[] VISIBLE_PROPERTIES_DETAILS = new Object[] {"id", "commodity.commodityName", 
			"commodity.commodityPrice", "orderNum", "commodity.description"};
	
	private final String[] COL_HEADERS_DETAILS = new String[] {"详单编号", "商品名称", "商品单价", "订购数量", "商品描述"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);
	
	private Table myOrderTable;									// 存放我的订单查询结果的表格
	private Button editOrder_bt;								// 编辑订单
	private HistoryOrderSimpleFilter simpleOrderFilter;			// 简单搜索条件组件
	private FlipOverTableComponent<Order> myOrderTableFlip;		// 为Table添加的翻页组件

	private Table orderDetailsTable;
	private BeanItemContainer<Orderdetails> orderDetailsContainer;

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 
	private CustomDialPopupWindow customDialPopupWindow;		// 客户信息弹屏窗口
	private EditOrderWindow editOrderWindow;					// 编辑订单界面
	
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限
	private boolean isEncryptMobile = true;						// 电话号码默认加密
	
	private OrderService orderService;
	private TelephoneService telephoneService;	// 电话号码服务类
	
	public MyHistoryOrderTabView() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		orderService = SpringContextHolder.getBean("orderService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		
		// 创建搜索组件
		simpleOrderFilter = new HistoryOrderSimpleFilter();
		simpleOrderFilter.setHeight("-1px");
		this.addComponent(simpleOrderFilter);
		
		// 创建订单显示表格
		this.createOrderTable();

		// 为表格添加右键单击事件
		this.addActionToTable(myOrderTable);

		HorizontalLayout bottom_hlo = new HorizontalLayout();
		bottom_hlo.setSpacing(true);
		bottom_hlo.setWidth("100%");
		this.addComponent(bottom_hlo);
		
		editOrder_bt = new Button("编辑订单", this);
		editOrder_bt.addStyleName("default");
		editOrder_bt.setImmediate(true);
		editOrder_bt.setEnabled(false);
		bottom_hlo.addComponent(editOrder_bt);
		
		// 创建订单表格的翻页组件
		this.createTableFlipComponent(bottom_hlo);
		
		// 根据分辨率设置表格行数
		this.setTablePageLength();

		// 为我的订单Tab 页的  上侧搜索组件 传递  “翻页组件”和“Table组件”
		simpleOrderFilter.setTableFlipOver(myOrderTableFlip);
		
		// 创建订单详情显示表格
		this.createOrderDetailsTable();
		
		// 创建呼叫客户时需要显示个各种信息的组件
		this.createOutgoingDialWindow();
	}

	/**
	 *  创建订单显示表格
	 */
	private void createOrderTable() {
		myOrderTable = createFormatColumnTable();
		myOrderTable.setWidth("100%");
		myOrderTable.setHeight("-1px");
		myOrderTable.setImmediate(true);
		myOrderTable.setSelectable(true);
		myOrderTable.setStyleName("striped");
		myOrderTable.addListener(this);
		myOrderTable.setNullSelectionAllowed(false);
		myOrderTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(myOrderTable);

		myOrderTable.addGeneratedColumn("customerResource.telephones", new DialColumnGenerator());
		myOrderTable.setColumnWidth("customerResource.telephones", 192);
		myOrderTable.addGeneratedColumn("street", new AddressColumnGenerator());
	}
	
	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date)property.getValue());
				} else if("customerPhoneNumber".equals(colId)) {
					if(isEncryptMobile) {
						return telephoneService.encryptMobileNo((String) property.getValue());
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class DialColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Order order = (Order) itemId;
			if(columnId.equals("customerResource.telephones")) {
				Set<Telephone> telephones = order.getCustomerResource().getTelephones();
				return new DialComponentToTable(source, itemId, telephones, loginUser, ownBusinessModels, MyHistoryOrderTabView.this);
			} 
			return null;
		}
	}
	
	/**
	 * 用于自动生成收件人地址列
	 * 	如果记录内容的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class AddressColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Order order = (Order) itemId;
			if(columnId.equals("street")) {
				String province = StringUtils.trimToEmpty(order.getProvince());
				String city = StringUtils.trimToEmpty(order.getCity());
				String county = StringUtils.trimToEmpty(order.getCounty());
				String street = StringUtils.trimToEmpty(order.getStreet());
				String address = province+" "+ city+" "+ county+" "+ street;
				if(!"".equals(address)) {
					Label addressLabel = new Label();
					if(address.length() > 12) {
						addressLabel.setValue(address.substring(0, 12)+"...");
						addressLabel.setDescription(address);
					} else {
						addressLabel.setValue(address);
					}
					return addressLabel;
				}
			} 
			return null;
		}
	}
	
	/**
	 * 为指定任务表格 添加右键单击事件
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
				CustomerResource customerResource = ((Order) table.getValue()).getCustomerResource();
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

	// 创建翻页组件
	private void createTableFlipComponent(HorizontalLayout hlo) {
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		
		String countSql = "select count(e) from Order as e where e.csrUserId = " +loginUser.getId()
					+" and e.generateDate >= '" +dateStrs[0]+ "' and e.generateDate <= '" +dateStrs[1]+"'";
		String searchSql = countSql.replaceFirst("count\\(e\\)", "e") + " order by e.generateDate desc";
		
		myOrderTableFlip = new FlipOverTableComponent<Order>(Order.class, 
				orderService, myOrderTable, searchSql , countSql, null);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			myOrderTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		myOrderTable.setVisibleColumns(VISIBLE_PROPERTIES);
		myOrderTable.setColumnHeaders(COL_HEADERS);

		hlo.addComponent(myOrderTableFlip);
		hlo.setComponentAlignment(myOrderTableFlip, Alignment.TOP_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			myOrderTable.setPageLength(27);
			myOrderTableFlip.setPageLength(27, false);
		} else if(screenResolution[1] >= 1050) {
			myOrderTable.setPageLength(25);
			myOrderTableFlip.setPageLength(25, false);
		} else if(screenResolution[1] >= 900) {
			myOrderTable.setPageLength(18);
			myOrderTableFlip.setPageLength(18, false);
		} else if(screenResolution[1] >= 768) {
			myOrderTable.setPageLength(10);
			myOrderTableFlip.setPageLength(10, false);
		} else {
			myOrderTable.setPageLength(6);
			myOrderTableFlip.setPageLength(6, false);
		}
	}
	
	/**
	 *  创建订单详情显示表格
	 */
	private void createOrderDetailsTable() {
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
		this.addComponent(orderDetailsTable);
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
		customDialPopupWindow = new CustomDialPopupWindow();
		if(screenResolution[0] >= 1366) {
			customDialPopupWindow.setWidth("1190px");
		} else {
			customDialPopupWindow.setWidth("940px");
		}
		
		if(screenResolution[1] > 768) {
			customDialPopupWindow.setHeight("610px");
		} else if(screenResolution[1] == 768) {
			customDialPopupWindow.setHeight("560px");
		} else {
			customDialPopupWindow.setHeight("510px");
		}

		customDialPopupWindow.setResizable(false);
		customDialPopupWindow.setStyleName("opaque");
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == myOrderTable) {
			Order order = (Order) myOrderTable.getValue();
			orderDetailsContainer.removeAllItems();
			if(order != null) {
				orderDetailsContainer.removeAllItems();
				orderDetailsContainer.addAll(orderService.getOrderDetailsByOrderId(order.getId()));
				if(PayStatus.PAYED.equals(order.getPayStatus()) || !DiliverStatus.NOTDILIVERED.equals(order.getDiliverStatus()) 
						|| !QualityStatus.CONFIRMING.equals(order.getQualityStatus())) {	// 一但订单已经发货、已质检、已支付，满足上面的任何一种，都不能再修改订单
					editOrder_bt.setEnabled(false);
				} else {
					editOrder_bt.setEnabled(true);
				}
			} else {
				editOrder_bt.setEnabled(false);
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == editOrder_bt) {
			showEditOrderWindow();
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
		
		editOrderWindow.updateUiDatas((Order) myOrderTable.getValue());
		this.getApplication().getMainWindow().addWindow(editOrderWindow);
	}

	/**	 
	 * 供 CustomerBaseInfoEditorForm 调用
	 * 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		myOrderTableFlip.refreshInCurrentPage();
	}

	/**
	 * 刷新我的订单显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			myOrderTableFlip.refreshToFirstPage();
		} else {
			myOrderTableFlip.refreshInCurrentPage();
		}
	}

	/**
	 * 供自动生成的拨号按钮使用反射机制调用
	 */
	public void changeCurrentTab() {
		ShareData.csrToCurrentTab.put(loginUser.getId(), MyHistoryOrderTabView.this);
	}
	
	/**
	 * 当电话振铃时，弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		this.getApplication().getMainWindow().removeWindow(customDialPopupWindow);
		// 回显弹屏信息
		customDialPopupWindow.echoInformations(customerResource);
		customDialPopupWindow.setAgiChannel(agiChannel);
		customDialPopupWindow.center();
		this.getApplication().getMainWindow().addWindow(customDialPopupWindow);
	}

}
