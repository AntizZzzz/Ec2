package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CommodityService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerBaseInfoView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 创建订单前，在弹窗中选中商品的界面
 *
 * @author jrh
 *  2013-8-9
 */
@SuppressWarnings("serial")
public class CommodityOrderCreator extends VerticalLayout implements ClickListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "commodityName",  "commodityPrice", "stockQty", "description"};
	
	private final String[] COL_HEADERS = new String[] {"编号", "名称", "单价", "库存", "描述"};

	private Notification notification;											// 提示信息
	
	private TextField nameSearch_tf;											// 商品名称搜索输入框
	private Button search_button;												// 搜索按钮
	private CommodityComplexFilter complexFilter;								// 存放高级收索条件的组件
	private PopupView complexSearchView;										// 复杂搜索界面
	private Table commodity_tb;													// 显示当前公司拥有的商品表格
	private FlipOverTableComponent<Commodity> commodityTableFlip;				// 为Table添加的翻页组件

	private Button viewShoppingCart_bt;											// 进入购物车
	private Button close_bt;													// 关闭按钮
	private Label makeOrderSuccess_lb;											// 已成功创建订单标签
	
	private ShoppingCartWindow shoppingCartWindow;								// 购物车窗口
	private CustomerBaseInfoView customerBaseInfoView;							// 客户资源对象的基本信息界面

	private FlipOverTableComponent<MarketingProjectTask> focusTaskTableFlip;		// 当前  正在  操作的任务表格的翻页组件
	private FlipOverTableComponent<MarketingProjectTask> unfocusTaskTableFlip;		// 当前  没有  操作的任务表格的翻页组件
	
	private User loginUser;														// 当前登陆用户
	private String exten;														// 当前坐席使用分机
	private Domain domain;														// 当前登陆用户所属域
	private CustomerResource customerResource;									// 当前客户对象
	private Map<Long, Commodity> purchaseCommodities;							// 客户购买的商品
	private Map<Long, Integer> purchaseQty;										// 客户购买的商品对应的数量
	private MarketingProjectTask currentTask;									// 用户获取任务表格中被选择的任务项
	
	private CommodityService commodityService;									// 商品服务类
	private MarketingProjectTaskService marketingProjectTaskService;			// 项目任务服务类
	
	public CommodityOrderCreator(User loginUser, CustomerBaseInfoView customerBaseInfoView) {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
		this.loginUser = loginUser;
		this.customerBaseInfoView = customerBaseInfoView;

		domain = loginUser.getDomain();
		exten = ShareData.userToExten.get(loginUser.getId());
		purchaseCommodities = new HashMap<Long, Commodity>();
		purchaseQty = new HashMap<Long, Integer>();
		
		commodityService = SpringContextHolder.getBean("commodityService");
		marketingProjectTaskService = SpringContextHolder.getBean("marketingProjectTaskService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建收索组件(简单搜索、高级搜索)
		createTableFilter();
		
		// 创建商品显示表格
		createCommodityTable();
		
		// 创建表格的翻页组件
		createTableFlipComponent();
		
		// 创建保存等操作组件
		createOperatorComponents();
	}

	/**
	 * 创建收索组件(简单搜索、高级搜索)
	 */
	private void createTableFilter() {
		HorizontalLayout filter_hl = new HorizontalLayout();
		filter_hl.setSpacing(true);
		filter_hl.setWidth("100%");
		this.addComponent(filter_hl);
		
		Label purchaseCount_label = new Label("<B>商品搜索：</B>", Label.CONTENT_XHTML);
		purchaseCount_label.setWidth("-1px");
		filter_hl.addComponent(purchaseCount_label);
		filter_hl.setComponentAlignment(purchaseCount_label, Alignment.MIDDLE_LEFT);
		
		nameSearch_tf = new TextField();
		nameSearch_tf.setWidth("140px");
		nameSearch_tf.setImmediate(true);
		nameSearch_tf.setStyleName("search");
		nameSearch_tf.setInputPrompt("请输入商品名称");
		nameSearch_tf.setDescription("可按商品名称搜索");
		filter_hl.addComponent(nameSearch_tf);
		filter_hl.setComponentAlignment(nameSearch_tf, Alignment.MIDDLE_LEFT);
		
		search_button = new Button("收 索", this);
		search_button.setImmediate(true);
		filter_hl.addComponent(search_button);
		filter_hl.setComponentAlignment(search_button, Alignment.MIDDLE_LEFT);

		complexFilter = new CommodityComplexFilter(loginUser);
		complexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(complexFilter);
		complexSearchView.setHideOnMouseOut(false);
		filter_hl.addComponent(complexSearchView);
		filter_hl.setExpandRatio(complexSearchView, 1.0f);
		filter_hl.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}

	/**
	 *  创建商品显示表格
	 */
	private void createCommodityTable() {
		commodity_tb = new Table();
		commodity_tb.setWidth("100%");
		commodity_tb.setHeight("-1px");
		commodity_tb.setImmediate(true);
		commodity_tb.setSelectable(false);
		commodity_tb.setStyleName("striped");
		commodity_tb.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(commodity_tb);

		commodity_tb.addGeneratedColumn("description", new DescriptionColumnGenerator());
	}

	/**
	 *  创建表格的翻页组件
	 */
	private void createTableFlipComponent() {
		String commdityIdSql = createDynamicCommdityIdSql();	// 只显示当前工作项目关联的商品
		String countSql = "select count(c) from Commodity as c where c.domain.id = " +domain.getId() +" and "+ commdityIdSql;
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") +" order by c.id desc";
		
		commodityTableFlip = new FlipOverTableComponent<Commodity>(Commodity.class, commodityService, 
				commodity_tb, searchSql , countSql, null);
		
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			commodityTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		commodityTableFlip.setPageLength(9, true);
		commodity_tb.setPageLength(9);
		commodity_tb.setVisibleColumns(VISIBLE_PROPERTIES);
		commodity_tb.setColumnHeaders(COL_HEADERS);

		commodity_tb.addGeneratedColumn("operator", new OperatorColumnGenerator());
		commodity_tb.setColumnHeader("operator", "操作");
		commodity_tb.setColumnWidth("operator", 200);
		commodity_tb.setColumnAlignment("operator", Table.ALIGN_CENTER);
		
		// 为高级搜索组件设值
		complexFilter.setCommodityTableFlip(commodityTableFlip);

		this.addComponent(commodityTableFlip);
		this.setComponentAlignment(commodityTableFlip, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 创建根据项目查询商品的Sql
	 * @return
	 */
	private String createDynamicCommdityIdSql() {
		ArrayList<Long> commodityIds = new ArrayList<Long>();
		commodityIds.add(0L);
		Long projectId = ShareData.extenToProject.get(exten);
		if(projectId != null) {
			for(Commodity commodity : commodityService.getAllByProjectId(projectId)) {
				commodityIds.add(commodity.getId());
			}
		}
		String commodityIdStr = StringUtils.join(commodityIds, ",");
		return " c.id in ("+commodityIdStr+")";
	}

	/**
	 *  创建保存等操作组件
	 */
	private void createOperatorComponents() {
		HorizontalLayout operator_hl = new HorizontalLayout();
		operator_hl.setSpacing(true);
		operator_hl.setWidth("100%");
		operator_hl.setMargin(false, true, false, true);
		this.addComponent(operator_hl);
		
		HorizontalLayout buttons_hl = new HorizontalLayout();
		buttons_hl.setSpacing(true);
		operator_hl.addComponent(buttons_hl);
		
		viewShoppingCart_bt = new Button("(0) 去购物车下单", this);
		viewShoppingCart_bt.setIcon(ResourceDataCsr.cart_put_14_ico);
		viewShoppingCart_bt.setImmediate(true);
		viewShoppingCart_bt.addStyleName("default");
		buttons_hl.addComponent(viewShoppingCart_bt);
		
		close_bt = new Button("关闭", this);
		close_bt.setImmediate(true);
		buttons_hl.addComponent(close_bt);
		
		makeOrderSuccess_lb = new Label("<font color='red'>创建订单成功</font>", Label.CONTENT_XHTML);
		makeOrderSuccess_lb.setWidth("-1px");
		makeOrderSuccess_lb.setVisible(false);
		operator_hl.addComponent(makeOrderSuccess_lb);
		operator_hl.setComponentAlignment(makeOrderSuccess_lb, Alignment.BOTTOM_RIGHT);
	}

	/**
	 * 创建商品描述信息组件
	 */
	private class DescriptionColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Commodity commodity = (Commodity) itemId;
			if(columnId.equals("description")) {
				String description = commodity.getDescription();
				if(description != null) {
					String label_caption = description;
					if(label_caption.length() > 5) {
						label_caption = label_caption.substring(0, 5) + "...";
					}
					Label description_label = new Label(label_caption);
					description_label.setDescription(description);
					description_label.setWidth("-1px");
					return description_label;
				}
			} 
			return null;
		}
	}
	
	/**
	 * 创建接操作组件
	 */
	private class OperatorColumnGenerator implements Table.ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Commodity commodity = (Commodity) itemId;
			
			Label purchaseCount_lb = new Label("<font color='blue'>数量:</font>", Label.CONTENT_XHTML);
			purchaseCount_lb.setWidth("-1px");

			// 购买数量
			final ComboBox count_cb = new ComboBox();
			count_cb.setWidth("60px");
			count_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
			count_cb.setNullSelectionAllowed(false);
			for(int i = 1; i <= 500; i++) {
				count_cb.addItem(i);
			}
			count_cb.setValue(1);
			
			// 购买数量减一
			Button minus_bt = new Button();
			minus_bt.setWidth("-1px");
			minus_bt.addStyleName("borderless");
			minus_bt.setIcon(ResourceDataCsr.minus_14_ico);
			minus_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					int count = (Integer) count_cb.getValue();
					if(count > 1) {
						count_cb.setValue(--count);
					} else {
						commodity_tb.getApplication().getMainWindow().showNotification("购买数量不能小于 1 !", Notification.TYPE_WARNING_MESSAGE);
					}
				}
			});

			// 购买数量加一
			Button plus_bt = new Button();
			plus_bt.setWidth("-1px");
			plus_bt.addStyleName("borderless");
			plus_bt.setIcon(ResourceDataCsr.plus_14_ico);
			plus_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					int count = (Integer) count_cb.getValue();
					if(count < 500) {
						count_cb.setValue(++count);
					} else {
						commodity_tb.getApplication().getMainWindow().showNotification("单次购买数量不能超过500件!", Notification.TYPE_WARNING_MESSAGE);
					}
				}
			});

			// 加入购物车
			Button purchase_bt = new Button("加入");
			purchase_bt.setWidth("-1px");
			purchase_bt.addStyleName(BaseTheme.BUTTON_LINK);
			purchase_bt.setIcon(ResourceDataCsr.cart_put_14_ico);
			purchase_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					int count = (Integer) count_cb.getValue();
					Long commodityId = commodity.getId();
					if(!purchaseCommodities.keySet().contains(commodityId)) {
						purchaseCommodities.put(commodityId, commodity);
						purchaseQty.put(commodityId, count);
						long size = purchaseCommodities.size();
						viewShoppingCart_bt.setCaption("("+size+") 去购物车下单");
					} else {
						int totalCount = purchaseQty.get(commodityId) + count;
						purchaseQty.put(commodityId, totalCount);
					}
					viewShoppingCart_bt.getApplication().getMainWindow().showNotification("加入购物车成功！");
				}
			});
			
			HorizontalLayout layout = new HorizontalLayout();
			layout.setSpacing(true);
			layout.addComponent(purchaseCount_lb);
			layout.addComponent(minus_bt);
			layout.addComponent(count_cb);
			layout.addComponent(plus_bt);
			layout.addComponent(purchase_bt);
			layout.setComponentAlignment(purchaseCount_lb, Alignment.MIDDLE_LEFT);
			layout.setWidth("-1px");
			
			return layout;
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == viewShoppingCart_bt) {
			showShoppingCart();
		} else if(source == close_bt) {
			close_bt.getApplication().getMainWindow().removeWindow(this.getWindow());
		} else if(source == search_button) {
			executeSearch();
		} 
	}

	/**
	 * 显示购物车详情界面
	 */
	private void showShoppingCart() {
		// 如果客户基础信息处于编辑状态，则先保存客户基础信息，然后再创建后续记录
		boolean isSaveResourceSuccess = customerBaseInfoView.getCustomerBaseInfoEditorForm().checkAndSaveCustomerResourceInfo();
		if(!isSaveResourceSuccess) {
			this.getApplication().getMainWindow().showNotification("客户基础信息填写有误，保存失败！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		if(shoppingCartWindow == null) {
			shoppingCartWindow = new ShoppingCartWindow(loginUser, this);
		}
		shoppingCartWindow.update(purchaseCommodities, purchaseQty);
		this.getApplication().getMainWindow().addWindow(shoppingCartWindow);
	}

	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		try {
			String commdityIdSql = createDynamicCommdityIdSql();	// 只显示当前工作项目关联的商品
			String commodityName = StringUtils.trimToEmpty((String) nameSearch_tf.getValue());
			String countSql = "select count(c) from Commodity as c where c.domain.id = " +domain.getId()+ 
					" and c.commodityName like '%"+commodityName+"%' and "+commdityIdSql;
			String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.id desc";
			
			commodityTableFlip.setSearchSql(searchSql);
			commodityTableFlip.setCountSql(countSql);
			commodityTableFlip.refreshToFirstPage();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 坐席在弹屏界面下按商品名称检索商品时出现异常---》"+e.getMessage(), e);
		}
	}

	/**
	 * 清空组件的值
	 */
	public void clearComponentsValue() {
		nameSearch_tf.setValue("");
		purchaseCommodities.clear();
		purchaseQty.clear();
		viewShoppingCart_bt.setCaption("(0) 去购物车下单");
		commodity_tb.refreshRowCache();
		if(shoppingCartWindow != null) {	// 如果购物车窗口已经创建，则关闭窗口时，也清空信息
			shoppingCartWindow.clearComponentsValue();
		}
	}
	
	/**
	 * 当关闭窗口时将创建订单成功的提示标签设为不可见
	 * @param visible
	 */
	public void setMakeOrderSuccessLabelVisible(boolean visible) {
		makeOrderSuccess_lb.setVisible(visible);
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	/**
	 * 获取关闭按钮
	 * @return
	 */
	public Button getClose_bt() {
		return close_bt;
	}

	/**
	 * 由 ShoppingCartWindow 调用，当点击继续购物时，需要刷新界面信息
	 * @param purchaseCommodities	客户购买的商品
	 * @param purchaseQty			客户购买的商品对应的数量
	 * @param refresh				刷新表格行中的缓存信息
	 */
	public void update(Map<Long, Commodity> purchaseCommodities, Map<Long, Integer> purchaseQty, boolean refresh) {
		this.purchaseCommodities = purchaseCommodities;
		this.purchaseQty = purchaseQty;
		long size = purchaseCommodities.size();
		viewShoppingCart_bt.setCaption("("+size+") 去购物车下单");
		if(refresh) {
			commodity_tb.refreshRowCache();
		}
	}
	
	/**
	 * 根据当前正在操作的表格，来刷新响应的组件
	 * @param focusTaskTableFlip
	 * @param unfocusTaskTableFlip
	 */
	public void refreshComponent(FlipOverTableComponent<MarketingProjectTask> focusTaskTableFlip, 
			FlipOverTableComponent<MarketingProjectTask> unfocusTaskTableFlip) {
		this.focusTaskTableFlip = focusTaskTableFlip;
		this.unfocusTaskTableFlip = unfocusTaskTableFlip;
	}

	/**
	 * @Description 描述：当坐席点击呼叫按钮后，更新当前被叫客户对应的任务对象
	 *
	 * @author  JRH
	 * @date    2014年6月6日 下午8:46:34
	 * @param marketingProjectTask void
	 */
	public void refreshCurrentTask(MarketingProjectTask marketingProjectTask) {
		this.currentTask = marketingProjectTask;
	}
	
	/**
	 * 如果当前是操作的模块是‘我的任务-营销’，关闭窗口时，刷新我的任务界面
	 */
	public void updateMarketingTaskTable() {
		try {
			if(currentTask != null) {
				currentTask.setIsFinished(true);
				currentTask.setIsAnswered(false);
				currentTask.setLastUpdateDate(new Date());
				if(makeOrderSuccess_lb.isVisible()) {
					currentTask.setLastStatus("成功下单");
					currentTask.setIsAnswered(true);
				}
				currentTask = marketingProjectTaskService.update(currentTask);
				focusTaskTableFlip.refreshInCurrentPage();
				unfocusTaskTableFlip.refreshToFirstPage();
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 坐席在‘我的任务-营销’摸了创建订单，关闭弹窗并刷新界面时出现异常---》"+e.getMessage(), e);
		}
	}
	
}
