package com.jiangyifen.ec2.ui.csr.workarea.historyservicerecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.asteriskjava.fastagi.AgiChannel;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.common.DialComponentToTable;
import com.jiangyifen.ec2.ui.csr.workarea.servicerecord.MyServiceRecordAllTabView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * 我的历史客服记录显示界面
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class MyHistoryServiceRecordTabView extends VerticalLayout implements ClickListener {
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"createDate", "serviceRecordStatus",
			"customerResource.id", "customerResource.name", "customerResource.telephones", "orderTime", 
			"orderNote", "recordContent", "customerResource.company", "marketingProject"};
	
	private final String[] COL_HEADERS = new String[] {"联系时间", "联系结果", "客户编号", "客户姓名", 
			"电话号码", "预约时间", "预约内容", "记录内容", "公司名称", "所属项目"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);
	
	private Table myHistoryServiceRecordTable;										// 存放我的历史客服记录查询结果的表格
	private HistoryServiceRecordSimpleFilter simpleHistoryServiceRecordFilter;				// 简单搜索条件组件
	private FlipOverTableComponent<CustomerServiceRecord> historyServiceRecordTableFlip;	// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 
	private OutgoingDialForHistoryServiceWindow outgoingDialWindow;	// 客户信息弹屏窗口
	private DetailSingleCustomerHistoryRecordView detailSingleCustomerHistoryRecordView; // 单个客户的历史服务记录详细信息显示窗口

	private Button switchToDetailView_bt;							// jrh 切换查看客服记录视角的按钮
	private MyServiceRecordAllTabView myServiceRecordAllTabView;// 用于显示客服记录的组界面，也是当前组件的上级界面
	
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限
	
	private CustomerServiceRecordService customerServiceRecordService;
	
	public MyHistoryServiceRecordTabView(MyServiceRecordAllTabView myServiceRecordAllTabView) {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		this.myServiceRecordAllTabView = myServiceRecordAllTabView;
		
		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		
		// 创建搜索组件
		simpleHistoryServiceRecordFilter = new HistoryServiceRecordSimpleFilter();
		simpleHistoryServiceRecordFilter.setHeight("-1px");
		this.addComponent(simpleHistoryServiceRecordFilter);
		
		// 创建历史客服记录显示表格
		createServiceRecordTable();

		// 为表格添加右键单击事件
		addActionToTable(myHistoryServiceRecordTable);

		HorizontalLayout tableBottom_hlo = new HorizontalLayout();
		tableBottom_hlo.setSpacing(true);
		tableBottom_hlo.setWidth("100%");
		this.addComponent(tableBottom_hlo);
		
		// jrh 添加切换查看客服记录视角的按钮
		switchToDetailView_bt = new Button(MyServiceRecordAllTabView.DETAIL_SR_CAPTION, this);
		switchToDetailView_bt.addStyleName("default");
		switchToDetailView_bt.setImmediate(true);
		tableBottom_hlo.addComponent(switchToDetailView_bt);
		tableBottom_hlo.setComponentAlignment(switchToDetailView_bt, Alignment.TOP_LEFT);
		
		// 创建历史客服记录表格的翻页组件
		createTableFlipComponent(tableBottom_hlo);
		
		// 根据分辨率设置表格行数
		setTablePageLength();

		// 为我的历史客服记录Tab 页的  上侧搜索组件 传递  “翻页组件”和“Table组件”
		simpleHistoryServiceRecordFilter.setTableFlipOver(historyServiceRecordTableFlip);
		
		// 创建呼叫客户时需要显示个各种信息的组件
		createOutgoingDialWindow();
		
		// 创建单个客户的历史服务记录详细信息显示窗口及各种组件
		detailSingleCustomerHistoryRecordView = new DetailSingleCustomerHistoryRecordView();
		
		simpleHistoryServiceRecordFilter.getSearchButton().click();
	}

	/**
	 *  创建历史客服记录显示表格
	 */
	private void createServiceRecordTable() {
		myHistoryServiceRecordTable = createFormatColumnTable();
		myHistoryServiceRecordTable.setWidth("100%");
		myHistoryServiceRecordTable.setHeight("-1px");
		myHistoryServiceRecordTable.setImmediate(true);
		myHistoryServiceRecordTable.setSelectable(true);
		myHistoryServiceRecordTable.setStyleName("striped");
		myHistoryServiceRecordTable.setNullSelectionAllowed(false);
		myHistoryServiceRecordTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(myHistoryServiceRecordTable);

		myHistoryServiceRecordTable.addGeneratedColumn("customerResource.telephones", new DialColumnGenerator());
		myHistoryServiceRecordTable.setColumnWidth("customerResource.telephones", 192);
		myHistoryServiceRecordTable.addGeneratedColumn("recordContent", new ContentColumnGenerator());
		myHistoryServiceRecordTable.addGeneratedColumn("orderNote", new OrderNoteColumnGenerator());		
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
				} else if("marketingProject".equals(colId)) {
					MarketingProject mp = (MarketingProject) property.getValue();
					return mp.getProjectName();
				} else if("serviceRecordStatus".equals(colId)) {
					CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) property.getValue();
					String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
					return status.getStatusName() + " - " + direction;
				} else if("customerResource.company".equals(colId)) {
					Company company = (Company) property.getValue();
					return company.getName();
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
			if(columnId.equals("customerResource.telephones")) {
				CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
				Set<Telephone> telephones = serviceRecord.getCustomerResource().getTelephones();
				return new DialComponentToTable(source, itemId, telephones, loginUser, ownBusinessModels, MyHistoryServiceRecordTabView.this);
			} 
			return null;
		}
	}
	
	/**
	 * 用于自动生成记录能容列
	 * 	如果记录内容的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class ContentColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if(columnId.equals("recordContent")) {
				String content = serviceRecord.getRecordContent();
				if(content == null) {
					return null;
				} else if(!"".equals(content.trim())) {
					Label contentLabel = new Label();
					String trimedContent = content.trim();
					if(trimedContent.length() > 8) {
						contentLabel.setValue(trimedContent.substring(0, 8)+"...");
						contentLabel.setDescription(trimedContent);
					} else {
						contentLabel.setValue(trimedContent);
					}
					return contentLabel;
				}
			} 
			return null;
		}
	}
	
	/**
	 * 用于自动生成预约信息显示组件
	 * 	如果预约信息的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class OrderNoteColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if(columnId.equals("orderNote")) {
				String orderNote = serviceRecord.getOrderNote();
				if(orderNote == null) {
					return null;
				} else if(!"".equals(orderNote.trim())) {
					Label orderNoteLabel = new Label();
					String trimedOrderNote = orderNote.trim();
					if(trimedOrderNote.length() > 8) {
						orderNoteLabel.setValue(trimedOrderNote.substring(0, 8)+"...");
						orderNoteLabel.setDescription(trimedOrderNote);
					} else {
						orderNoteLabel.setValue(trimedOrderNote);
					}
					return orderNoteLabel;
				}
			} 
			return null;
		}
	}

	/**
	 * 用于自动生成各种操作按钮显示组件
	 */
	private class OperatorColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(final Table source, final Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			final CustomerResource customerResource = serviceRecord.getCustomerResource();
			PopupView detail = new PopupView(new Content() {
				@Override
				public Component getPopupComponent() {
					return detailSingleCustomerHistoryRecordView;
				}
				
				@Override
				public String getMinimizedValueAsHTML() {
					return "联系详情";
				}
			});
			
			detail.setHideOnMouseOut(false);
			detail.addListener(new ComponentAttachListener() {
				@Override
				public void componentAttachedToContainer(ComponentAttachEvent event) {
					detailSingleCustomerHistoryRecordView.echoHistoryRecord(customerResource);
					source.select(itemId);
				}
			});
			
			return detail;
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
				CustomerResource customerResource = ((CustomerServiceRecord) table.getValue()).getCustomerResource();
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
	private void createTableFlipComponent(HorizontalLayout tableBottom_hlo) {
		historyServiceRecordTableFlip = new FlipOverTableComponent<CustomerServiceRecord>(CustomerServiceRecord.class, 
				customerServiceRecordService, myHistoryServiceRecordTable, "" , "", null);
		
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			historyServiceRecordTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		myHistoryServiceRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		myHistoryServiceRecordTable.setColumnHeaders(COL_HEADERS);

		myHistoryServiceRecordTable.addGeneratedColumn("operators", new OperatorColumnGenerator());
		myHistoryServiceRecordTable.setColumnHeader("operators", "操作");
		
		tableBottom_hlo.addComponent(historyServiceRecordTableFlip);
		tableBottom_hlo.setComponentAlignment(historyServiceRecordTableFlip, Alignment.TOP_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			myHistoryServiceRecordTable.setPageLength(32);
			historyServiceRecordTableFlip.setPageLength(32, false);
		} else if(screenResolution[1] >= 1050) {
			myHistoryServiceRecordTable.setPageLength(30);
			historyServiceRecordTableFlip.setPageLength(30, false);
		} else if(screenResolution[1] >= 900) {
			myHistoryServiceRecordTable.setPageLength(23);
			historyServiceRecordTableFlip.setPageLength(23, false);
		} else if(screenResolution[1] >= 768) {
			myHistoryServiceRecordTable.setPageLength(16);
			historyServiceRecordTableFlip.setPageLength(16, false);
		} else {
			myHistoryServiceRecordTable.setPageLength(10);
			historyServiceRecordTableFlip.setPageLength(10, false);
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
		outgoingDialWindow = new OutgoingDialForHistoryServiceWindow();
		if(screenResolution[0] >= 1366) {
			outgoingDialWindow.setWidth("1190px");
		} else {
			outgoingDialWindow.setWidth("940px");
		}
		
		if(screenResolution[1] > 768) {
			outgoingDialWindow.setHeight("610px");
		} else if(screenResolution[1] == 768) {
			outgoingDialWindow.setHeight("560px");
		} else {
			outgoingDialWindow.setHeight("510px");
		}

		outgoingDialWindow.setResizable(false);
		outgoingDialWindow.setStyleName("opaque");
		outgoingDialWindow.setEchoModifyByReflect(this);
		outgoingDialWindow.setServiceRecordTableFlip(historyServiceRecordTableFlip);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == switchToDetailView_bt) {
			myServiceRecordAllTabView.switchView(MyServiceRecordAllTabView.DETAIL_VIEW_TYPE);
		}
	}
	
	/**	 
	 * 供 CustomerBaseInfoEditorForm 调用
	 * 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		historyServiceRecordTableFlip.refreshInCurrentPage();
	}

	/**
	 * 刷新我的历史客服记录显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			historyServiceRecordTableFlip.refreshToFirstPage();
		} else {
			historyServiceRecordTableFlip.refreshInCurrentPage();
		}
	}

	/**
	 * 供自动生成的拨号按钮使用反射机制调用
	 */
	public void changeCurrentTab() {
		ShareData.csrToCurrentTab.put(loginUser.getId(), MyHistoryServiceRecordTabView.this);
	}
	
	/**
	 * 当电话振铃时，弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		this.getApplication().getMainWindow().removeWindow(outgoingDialWindow);
		// 回显弹屏信息
		outgoingDialWindow.echoInformations(customerResource);
		outgoingDialWindow.setAgiChannel(agiChannel);
		outgoingDialWindow.center();
		this.getApplication().getMainWindow().addWindow(outgoingDialWindow);
	}

}
