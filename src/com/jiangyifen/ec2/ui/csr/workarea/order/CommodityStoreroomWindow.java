package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CommodityService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
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
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @Description 描述：商品陈列界面
 *
 * @author  jrh
 * @date    2014年3月12日 上午9:09:02
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class CommodityStoreroomWindow extends Window implements ClickListener {

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

	private Button gotoMakeOrder_bt;											// 去下单
	private Button cancel_bt;													// 取消按钮
	
	private EditOrderWindow editOrderWindow;									// 购物车窗口

	private Long projectId;														// 当前订单所成交的项目编号
	private User loginUser;														// 当前登陆用户
	private Domain domain;														// 当前登陆用户所属域
	private Map<Long, Commodity> purchaseCommodities;							// 客户购买的商品
	private Map<Long, Integer> purchaseQty;										// 客户购买的商品对应的数量
	
	private CommodityService commodityService;									// 商品服务类
	
	public CommodityStoreroomWindow(EditOrderWindow editOrderWindow) {
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("商品选择");
		this.editOrderWindow = editOrderWindow;
		
		this.loginUser = SpringContextHolder.getLoginUser();
		
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setWidth("700px");
		windowContent.setSpacing(true);
		windowContent.setMargin(true);
		this.setContent(windowContent);
		
		domain = loginUser.getDomain();
		purchaseCommodities = new HashMap<Long, Commodity>();
		purchaseQty = new HashMap<Long, Integer>();
		
		commodityService = SpringContextHolder.getBean("commodityService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建收索组件(简单搜索、高级搜索)
		createTableFilter(windowContent);
		
		// 创建商品显示表格
		createCommodityTable(windowContent);
		
		HorizontalLayout tableBottom_hlo = new HorizontalLayout();
		tableBottom_hlo.setWidth("100%");
		tableBottom_hlo.setSpacing(true);
		windowContent.addComponent(tableBottom_hlo);
		
		// 创建保存等操作组件
		createOperatorComponents(tableBottom_hlo);
		
		// 创建表格的翻页组件
		createTableFlipComponent(tableBottom_hlo);
	}

	/**
	 * 创建收索组件(简单搜索、高级搜索)
	 */
	private void createTableFilter(VerticalLayout windowContent) {
		HorizontalLayout filter_hl = new HorizontalLayout();
		filter_hl.setSpacing(true);
		filter_hl.setWidth("100%");
		windowContent.addComponent(filter_hl);
		
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
	private void createCommodityTable(VerticalLayout windowContent) {
		commodity_tb = new Table();
		commodity_tb.setWidth("100%");
		commodity_tb.setHeight("-1px");
		commodity_tb.setImmediate(true);
		commodity_tb.setSelectable(false);
		commodity_tb.setStyleName("striped");
		commodity_tb.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		windowContent.addComponent(commodity_tb);

		commodity_tb.addGeneratedColumn("description", new DescriptionColumnGenerator());
	}

	/**
	 *  创建保存等操作组件
	 */
	private void createOperatorComponents(HorizontalLayout tableBottom_hlo) {
		HorizontalLayout operator_hl = new HorizontalLayout();
		operator_hl.setSpacing(true);
		tableBottom_hlo.addComponent(operator_hl);
		
		HorizontalLayout buttons_hl = new HorizontalLayout();
		buttons_hl.setSpacing(true);
		operator_hl.addComponent(buttons_hl);
		
		gotoMakeOrder_bt = new Button("(0) 去下单", this);
		gotoMakeOrder_bt.setIcon(ResourceDataCsr.cart_put_14_ico);
		gotoMakeOrder_bt.setWidth("-1px");
		gotoMakeOrder_bt.setImmediate(true);
		gotoMakeOrder_bt.addStyleName("default");
		buttons_hl.addComponent(gotoMakeOrder_bt);
		
		cancel_bt = new Button("取消", this);
		cancel_bt.setWidth("-1px");
		cancel_bt.setImmediate(true);
		buttons_hl.addComponent(cancel_bt);
	}
	
	/**
	 *  创建表格的翻页组件
	 */
	private void createTableFlipComponent(HorizontalLayout tableBottom_hlo) {
		String commdityIdSql = createDynamicCommdityIdSql();	// 只显示当前工作项目关联的商品
		String countSql = "select count(c) from Commodity as c where c.domain.id = " +domain.getId() +" and "+ commdityIdSql;
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") +" order by c.id desc";
		
		commodityTableFlip = new FlipOverTableComponent<Commodity>(Commodity.class, commodityService, 
				commodity_tb, searchSql , countSql, null);
		
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			commodityTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		commodityTableFlip.setPageLength(9, false);
		commodity_tb.setPageLength(9);
		commodity_tb.setVisibleColumns(VISIBLE_PROPERTIES);
		commodity_tb.setColumnHeaders(COL_HEADERS);

		commodity_tb.addGeneratedColumn("operator", new OperatorColumnGenerator());
		commodity_tb.setColumnHeader("operator", "操作");
		commodity_tb.setColumnAlignment("operator", Table.ALIGN_CENTER);
		
		// 为高级搜索组件设值
		complexFilter.setCommodityTableFlip(commodityTableFlip);

		tableBottom_hlo.addComponent(commodityTableFlip);
		tableBottom_hlo.setComponentAlignment(commodityTableFlip, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 创建根据项目查询商品的Sql
	 * @return
	 */
	private String createDynamicCommdityIdSql() {
		ArrayList<Long> commodityIds = new ArrayList<Long>();
		commodityIds.add(0L);
		
		if(projectId != null) {
			for(Commodity commodity : commodityService.getAllByProjectId(projectId)) {
				commodityIds.add(commodity.getId());
			}
		}
		String commodityIdStr = StringUtils.join(commodityIds, ",");
		return " c.id in ("+commodityIdStr+")";
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
						gotoMakeOrder_bt.setCaption("("+size+") 去购物车下单");
					} else {
						int totalCount = purchaseQty.get(commodityId) + count;
						purchaseQty.put(commodityId, totalCount);
					}
					gotoMakeOrder_bt.getApplication().getMainWindow().showNotification("加入购物车成功！");
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
		if(source == gotoMakeOrder_bt) {
			editOrderWindow.updateTableSource(purchaseCommodities, purchaseQty);
			gotoMakeOrder_bt.getApplication().getMainWindow().removeWindow(this);
		} else if(source == cancel_bt) {
			cancel_bt.getApplication().getMainWindow().removeWindow(this);
		} else if(source == search_button) {
			executeSearch();
		} 
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
	 * 由 EditOrderWindow 调用，当点击继续购物时，需要刷新界面信息
	 * @param purchaseCommodities	客户购买的商品
	 * @param purchaseQty			客户购买的商品对应的数量
	 * @param refresh				刷新表格行中的缓存信息
	 */
	public void updateUiDatas(Order order, Map<Long, Commodity> purchaseCommodities, Map<Long, Integer> purchaseQty) {
		this.projectId = order.getProjectId();
		this.purchaseCommodities = new HashMap<Long, Commodity>(purchaseCommodities);
		this.purchaseQty = new HashMap<Long, Integer>(purchaseQty);
		
		long size = purchaseCommodities.size();
		gotoMakeOrder_bt.setCaption("("+size+") 去下单");
		
		search_button.click();
	}

}
