package com.jiangyifen.ec2.ui.csr.workarea.mycustomer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.asteriskjava.fastagi.AgiChannel;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.common.DialComponentToTable;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * 我的客户信息查看与管理界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class ProprietaryCustomersTabView extends VerticalLayout {
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "name", "company", "telephones", "sex", "birthday", "count", "customerLevel", "lastDialDate", /*"expireDate",*/ "importDate"};
	
	private final String[] COL_HEADERS = new String[] {"客户编号", "客户姓名", "公司名称", "电话号码", "性别", "生日", "联系次数", "客户级别", "最近联系时间", /*"过期时间",*/ "导入时间"};
		
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);
	
	private Table proprietaryCustomersTable;
	private CustomerResourceSimpleFilter customerResourceSimpleFilter;				// 资源的搜索组件
	private FlipOverTableComponent<CustomerResource> proprietaryCustomersTableFlip;	// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 
	private OutgoingDialForResourceWindow outgoingDialWindow;	// 客户信息弹屏窗口
	
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限

	private CustomerResourceService customerResourceService;
	
	public ProprietaryCustomersTabView() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);

		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");

		customerResourceSimpleFilter = new CustomerResourceSimpleFilter();
		customerResourceSimpleFilter.setHeight("-1px");
		this.addComponent(customerResourceSimpleFilter);
		
		// 创建客户表格
		createCustomersTable();
		
		// 为表格添加右键单击事件
		addActionToTable(proprietaryCustomersTable);
		
		// 创建翻页组件
		createTableFlipComponent();

		// 根据分辨率设置表格行数
		setTablePageLength();

		// 为我的资源Tab 页上方的搜索组件传递翻页组件对象
		customerResourceSimpleFilter.setProprietaryCustomersTableFlip(proprietaryCustomersTableFlip);
		
		// 创建呼叫客户时需要显示个各种信息的组件
		createOutgoingDialWindow();
	}

	/**
	 * 创建客户显示表格
	 */
	private void createCustomersTable() {
		proprietaryCustomersTable = createFormatColumnTable();
		proprietaryCustomersTable.setWidth("100%");
		proprietaryCustomersTable.setHeight("-1px");
		proprietaryCustomersTable.setImmediate(true);
		proprietaryCustomersTable.setSelectable(true);
		proprietaryCustomersTable.setStyleName("striped");
		proprietaryCustomersTable.setNullSelectionAllowed(false);
		proprietaryCustomersTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(proprietaryCustomersTable);
		
		proprietaryCustomersTable.addGeneratedColumn("telephones", new DialColumnGenerator());
		proprietaryCustomersTable.setColumnWidth("telephones", 192);
	}

	/**
	 * 创建格式化显示列的表格
	 * @return
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
			if(columnId.equals("telephones")) {
				Set<Telephone> telephones = ((CustomerResource) itemId).getTelephones();
				return new DialComponentToTable(source, itemId, telephones, loginUser, ownBusinessModels, ProprietaryCustomersTabView.this);
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
				CustomerResource customerResource = (CustomerResource) table.getValue();
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
	 * 创建翻页组件
	 */
	private void createTableFlipComponent() {
		// 设置初始查询语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		String countSql = "select count(c) from CustomerResource as c where c.owner.id = " +loginUser.getId()+ " and c.lastDialDate >= '"+dateStrs[0]+"' and c.lastDialDate <= '"+dateStrs[1]+"'";
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.lastDialDate desc";
		
		proprietaryCustomersTableFlip = new FlipOverTableComponent<CustomerResource>(CustomerResource.class, 
				customerResourceService, proprietaryCustomersTable, searchSql , countSql, null);
		proprietaryCustomersTableFlip.setSearchSql(searchSql);
		proprietaryCustomersTableFlip.setCountSql(countSql);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			proprietaryCustomersTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		proprietaryCustomersTable.setVisibleColumns(VISIBLE_PROPERTIES);
		proprietaryCustomersTable.setColumnHeaders(COL_HEADERS);
		
		this.addComponent(proprietaryCustomersTableFlip);
		this.setComponentAlignment(proprietaryCustomersTableFlip, Alignment.TOP_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			proprietaryCustomersTable.setPageLength(29);
			proprietaryCustomersTableFlip.setPageLength(29, false);
		} else if(screenResolution[1] >= 1050) {
			proprietaryCustomersTable.setPageLength(27);
			proprietaryCustomersTableFlip.setPageLength(27, false);
		} else if(screenResolution[1] >= 900) {
			proprietaryCustomersTable.setPageLength(19);
			proprietaryCustomersTableFlip.setPageLength(19, false);
		} else if(screenResolution[1] >= 768) {
			proprietaryCustomersTable.setPageLength(13);
			proprietaryCustomersTableFlip.setPageLength(13, false);
		} else {
			proprietaryCustomersTable.setPageLength(10);
			proprietaryCustomersTableFlip.setPageLength(10, false);
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
		outgoingDialWindow = new OutgoingDialForResourceWindow();
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
		outgoingDialWindow.setProprietaryCustomersTableFlip(proprietaryCustomersTableFlip);
	}
	
	/**
	 * 供 CustomerBaseInfoEditorForm 调用
	 * 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		proprietaryCustomersTableFlip.refreshInCurrentPage();
	}

	/**
	 * 供自动生成的拨号按钮使用反射机制调用
	 */
	public void changeCurrentTab() {
		ShareData.csrToCurrentTab.put(loginUser.getId(), ProprietaryCustomersTabView.this);
	}
	
	/**
	 * 刷新我的专有客户显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			proprietaryCustomersTableFlip.refreshToFirstPage();
		} else {
			proprietaryCustomersTableFlip.refreshInCurrentPage();
		}
	}
	
	/**
	 * 当电话振铃时，弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		this.getApplication().getMainWindow().removeWindow(outgoingDialWindow);
		// 在弹屏前，回显弹屏中的信息
		outgoingDialWindow.echoInformations(customerResource);
		outgoingDialWindow.setAgiChannel(agiChannel);
		outgoingDialWindow.center();
		this.getApplication().getMainWindow().addWindow(outgoingDialWindow);
	}

}
