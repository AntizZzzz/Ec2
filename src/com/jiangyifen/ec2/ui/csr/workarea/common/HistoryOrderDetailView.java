package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.OrderdetailsService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * @Description 描述：
 *
 * @author  JRH
 * @date    2014年5月26日 上午10:33:38
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class HistoryOrderDetailView extends VerticalLayout implements ValueChangeListener {
	
	// 用户是否有权限查看历史订单记录
	private final String TASK_MANAGEMENT_HISTORY_ORDER = "task_management&history_order";
	
	private final Object[] VISIBLE_PROPERTIES_DETAILS = new Object[] {"order.generateDate", "order.id", "commodity.commodityName", 
			"commodity.commodityPrice", "orderNum", "commodity.description"};
	
	private final String[] COL_HEADERS_DETAILS = new String[] {"下单时间", "订单编号", "商品名称", "商品单价", "订购数量", "商品描述"};

	private PopupView complexSearchView;				// 复杂搜索界面
	private TextField searchField;						// 按商品名称搜索
	private HistoryOrderDetailFilter orderDetailFilter;	// 存放高级收索条件的组件
	private HorizontalLayout searchHLayout;				// 存放搜索组件的布局管理器
	
	private Table orderDetailsTable;
	private FlipOverTableComponent<Orderdetails> detailTableFlip;	// 历史订单详情Table的翻页组件

	private User loginUser;										// 当前登录用户
	private RoleType roleType;									// 角色类型
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限
	private CustomerResource customerResource;					// 当前选中任务对应的客户资源

	private String searchSql = "";				// 查询语句
	private String countSql = "";				// 统计语句
	private String searchByCreatorSpanSql = "";	// 按创建者所属部门进行查询客服记录[设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录，默认为可以]
	
	private OrderdetailsService orderdetailsService;
	private DepartmentService departmentService;
	
	public HistoryOrderDetailView(User loginUser, RoleType roleType) {
		this.setSpacing(true);
		this.setMargin(true); 
		this.loginUser = loginUser;
		this.roleType = roleType;

		orderdetailsService = SpringContextHolder.getBean("orderdetailsService");
		departmentService = SpringContextHolder.getBean("departmentService");

		// 获取当前登陆用户所有权限
		ownBusinessModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			} else if(role.getType().equals(RoleType.manager)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			}
		}

		// 初始化客服记录按部门查询的语句
		initializeSearchByCreatorDeptIdSql(roleType);
		
		// 创建历史订单详情表格
		createOrderDetailsTable();
		
		// 创建并添加翻页组件
		createTableFlipOver();

		// 创建搜索组件
		createSearchComponent(roleType);
		
		// 将个组将加入总界面
		this.addComponent(searchHLayout);
		this.addComponent(orderDetailsTable);
		this.addComponent(detailTableFlip);
		this.setComponentAlignment(detailTableFlip, Alignment.BOTTOM_RIGHT);
	}

	/**
	 * 初始化客服记录按部门查询的语句
	 * @param roleType	当前登陆用户目前正使用的角色类型【csr、manager】
	 */
	private void initializeSearchByCreatorDeptIdSql(RoleType roleType) {
		// 获取全局配置，看当前话务员是否能够看到其他部门的坐席针对同一资源做的客服记录
		Map<String,Boolean> domainConfigs = ShareData.domainToConfigs.get(loginUser.getDomain().getId());
		Boolean viewRecordSpanDeptAble = domainConfigs.get("csr_view_order_span_department");
		if(roleType.equals(RoleType.csr)) {
			if(viewRecordSpanDeptAble == null) {	// 查看整个域针对该客户做的记录
				searchByCreatorSpanSql = "";
			} else if(!viewRecordSpanDeptAble) {	// 查看当前坐席针对该客户做的记录
				searchByCreatorSpanSql = " and o.order.csrUserId = "+loginUser.getId();
			} else {	// 查看当前坐席所属部门，整个部门下针对该客户做的记录
				searchByCreatorSpanSql = " and EXISTS ( select u.id from User as u where u.id = o.order.csrUserId and u.department.id = "+loginUser.getDepartment().getId()+" ) ";
			}
		} else if(roleType.equals(RoleType.manager)) {	// 如果是管理员调用，则只能看到自己管辖部门下的坐席创建的客服记录
			// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
			List<Long> allGovernedDeptIds = new ArrayList<Long>();
			for (Role role : loginUser.getRoles()) {
				if (role.getType().equals(RoleType.manager)) {
					List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
					if(departments.isEmpty()) {
						allGovernedDeptIds.add(0L);
					} else {
						for (Department dept : departments) {
							Long deptId = dept.getId();
							if (!allGovernedDeptIds.contains(deptId)) {
								allGovernedDeptIds.add(deptId);
							}
						}
					}
				}
			}
			// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
			String deptIdsStr = "";
			for (int i = 0; i < allGovernedDeptIds.size(); i++) {
				if (i == (allGovernedDeptIds.size() - 1)) {
					deptIdsStr += allGovernedDeptIds.get(i);
				} else {
					deptIdsStr += allGovernedDeptIds.get(i) + ",";
				}
			}
			searchByCreatorSpanSql = " and o.order.csrUserId in ( select id from User as u where u.department.id in (" + deptIdsStr+ ") )";
		}
	}
	
	/**
	 *  创建订单详情显示表格
	 */
	private void createOrderDetailsTable() {
		orderDetailsTable = createFormatColumnTable();
		orderDetailsTable.setWidth("100%");
		orderDetailsTable.setHeight("-1px");
		orderDetailsTable.setSelectable(true);
		orderDetailsTable.setStyleName("striped");
		orderDetailsTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(orderDetailsTable);
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
				} else if(property.getType() == Date.class) {
					if(property.getValue() != null) {
                		return dateFormat.format((Date)property.getValue());
                	}
                	return "";
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
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
					if(label_caption.length() > 10) {
						label_caption = label_caption.substring(0, 10) + "...";
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
	 * 创建并添加翻页组件
	 */
	private void createTableFlipOver() {
		detailTableFlip = new FlipOverTableComponent<Orderdetails>(Orderdetails.class, 
				orderdetailsService, orderDetailsTable, searchSql, countSql, null);
		for(int i = 0; i < VISIBLE_PROPERTIES_DETAILS.length; i++) {
			detailTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES_DETAILS[i].toString());
		}
		orderDetailsTable.setVisibleColumns(VISIBLE_PROPERTIES_DETAILS);
		orderDetailsTable.setColumnHeaders(COL_HEADERS_DETAILS);
		orderDetailsTable.addGeneratedColumn("commodity.description", new DescriptionColumnGenerator());
		orderDetailsTable.setColumnExpandRatio("commodity.description", 0.5f);
		orderDetailsTable.setPageLength(5);
		detailTableFlip.setPageLength(5, false);
	}
	
	/**
	 * 创建搜索组件
	 * 		高级搜索组件需要以翻页组件为基础
	 */
	private void createSearchComponent(RoleType roleType) {
		searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		searchHLayout.setWidth("100%");

		searchField = new TextField();
		searchField.setWidth("140px");
		searchField.addListener(this);
		searchField.setImmediate(true);
		searchField.setStyleName("search");
		searchField.setInputPrompt("请输入商品名称");
		searchField.setDescription("可按商品名称模糊检索");
		searchHLayout.addComponent(searchField);
		searchHLayout.setComponentAlignment(searchField, Alignment.MIDDLE_LEFT);
		
		orderDetailFilter = new HistoryOrderDetailFilter(loginUser, roleType);
		orderDetailFilter.setHeight("-1px");
		orderDetailFilter.setTableFlipOver(detailTableFlip);
		
		complexSearchView = new PopupView(orderDetailFilter);
		complexSearchView.setHideOnMouseOut(false);
		searchHLayout.addComponent(complexSearchView);
		searchHLayout.setExpandRatio(complexSearchView, 1.0f);
		searchHLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == searchField) {
			// 如果是坐席角色类型，则只有当其拥有相应的权限的时候，才能查看历史服务记录
			if(roleType.equals(RoleType.csr) && !ownBusinessModels.contains(TASK_MANAGEMENT_HISTORY_ORDER)) {
				searchField.getApplication().getMainWindow().showNotification("对不起，您无权查看客户订单详情", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			String searchVal = StringUtils.trimToEmpty((String) searchField.getValue());
			countSql = "select count(o) from Orderdetails as o"
					+ " where o.order.customerResource.id = " + customerResource.getId() +" and o.commodityName like '%"+searchVal+"%' "+ searchByCreatorSpanSql;
			searchSql = countSql.replaceFirst("count\\(o\\)", "o") + " order by o.id desc ";
			
			detailTableFlip.setSearchSql(searchSql);
			detailTableFlip.setCountSql(countSql);
			detailTableFlip.refreshToFirstPage();
		}
	}

	/**
	 *  根据MyTaskLeftView 中的任务表格中选中的任务项，显示该条任务对应客户相关的所有的订单详情信息
	 * @param countSql 查询语句
	 */
	public void echoOrderDetailInfos(CustomerResource customerResource) {
		// 如果是坐席角色类型，则只有当其拥有相应的权限的时候，才能查看历史服务记录
		if(roleType.equals(RoleType.csr) && !ownBusinessModels.contains(TASK_MANAGEMENT_HISTORY_ORDER)) {
			searchField.getApplication().getMainWindow().showNotification("对不起，您无权查看客户订单详情", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		orderDetailFilter.setCustomerResource(customerResource);
		this.countSql = "select count(o) from Orderdetails as o"
				+ " where o.order.customerResource.id = " + customerResource.getId() + searchByCreatorSpanSql;
		this.customerResource = customerResource;
		if(customerResource != null && detailTableFlip != null) {
			searchSql = countSql.replaceFirst("count\\(o\\)", "o") + " order by o.id desc ";
			detailTableFlip.setSearchSql(searchSql);
			detailTableFlip.setCountSql(countSql);
			detailTableFlip.refreshToFirstPage();
		} else {
			orderDetailsTable.getContainerDataSource().removeAllItems();
		}
	}

	public FlipOverTableComponent<Orderdetails> getDetailTableFlip() {
		return detailTableFlip;
	}
	
}
