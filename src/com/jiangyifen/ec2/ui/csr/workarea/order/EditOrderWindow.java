package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.County;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CityService;
import com.jiangyifen.ec2.service.eaoservice.CountyService;
import com.jiangyifen.ec2.service.eaoservice.OrderService;
import com.jiangyifen.ec2.service.eaoservice.ProvinceService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @Description 描述：编辑订单界面
 *
 * @author  jrh
 * @date    2014年3月11日 下午1:46:49
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class EditOrderWindow extends Window implements ClickListener {

	private final DecimalFormat DF_TWO_PRECISION = new DecimalFormat("0.00");
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "commodityName",  "commodityPrice", "stockQty", "description"};
	
	private final String[] COL_HEADERS = new String[] {"编号", "名称", "单价(元)", "库存", "描述"};

	private Table commodity_tb;													// 显示当前公司拥有的商品表格
	private Button shoppingContinue_bt;
	private Label totalCommodities_lb;
	private Label totalExpense_lb;

	private TextField receiverName_tf; 
	private TextField receiverPhone_tf; 
    private ComboBox province_cb;												// 地址 省份
    private ComboBox city_cb;													// 地址 城市
    private ComboBox county_cb;													// 地址 区县
    private TextArea street_ta;													// 地址 街道
    private TextArea note_ta;													// 备注信息
	
	private Button makeOrder_bt;												// 保存按钮
	private Button cancel_bt;													// 取消按钮
	
	private CommodityStoreroomWindow commodityStoreroomWindow;
	private VerticalLayout sourceTableView;

	private BeanItemContainer<Commodity> commodityContainer;
	private BeanItemContainer<Province> provinceContainer;
	private BeanItemContainer<City> cityContainer;
	private BeanItemContainer<County> countyContainer;

	private User loginUser;														// 当前登陆用户
	private Domain domain;														// 当前登陆用户所属域
	private Order order;														// 当前要修改的订单信息
	private ArrayList<Long> oldCommodityIds;									// 订单原来选购的商品
	private ArrayList<String> ownBusinessModels;								// 当前登陆用户目前使用的角色类型所对应的所有权限
	private boolean isEncryptMobile = true;										// 电话号码是否需要加密
	private Map<Long, Commodity> purchaseCommodities;							// 客户购买的商品
	private Map<Long, Integer> purchaseQty;										// 客户购买的商品对应的数量
	private Map<Long, Double> purchasePriceMap;									// 客户购买的商品的售出价
	private Map<Long, TextField> commodityToTf;									// 商品的售出价的修改组件
	private Map<Long, Label> commodityToLb;										// 商品小计
	private HashMap<String, String> phoneNosMap;								// 加密后的号码与原始号码对应项HashMap<加密号码，未加密号码>
	private double totalExpense;												// 客户消费总价
	
	private ProvinceService provinceService;									// 省份服务类
	private CityService cityService;											// 城市服务类
	private CountyService countyService;										// 县区服务类
	private TelephoneService telephoneService;									// 电话号码服务类
	private OrderService orderService;											// 订单服务类
	
	public EditOrderWindow(VerticalLayout sourceTableView) {
		this.center();
		this.setCaption("编辑订单");
		this.setModal(true);
		this.setResizable(false);
		this.setWidth("950px");
		this.sourceTableView = sourceTableView;
		
		this.loginUser = SpringContextHolder.getLoginUser();
		this.ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		VerticalLayout mainContent = new VerticalLayout();
		mainContent.setMargin(true);
		mainContent.setSpacing(true);
		this.setContent(mainContent);
		
		this.domain = loginUser.getDomain();
		this.purchaseCommodities = new HashMap<Long, Commodity>();	// 	客户购买的商品
		this.purchaseQty = new HashMap<Long, Integer>();			//  客户购买的商品对应的数量
		this.oldCommodityIds = new ArrayList<Long>();
		this.purchasePriceMap = new HashMap<Long, Double>();
		this.commodityToTf = new HashMap<Long, TextField>();
		this.commodityToLb = new HashMap<Long, Label>();

		commodityContainer = new BeanItemContainer<Commodity>(Commodity.class);
		provinceContainer = new BeanItemContainer<Province>(Province.class);
		cityContainer = new BeanItemContainer<City>(City.class);
		countyContainer = new BeanItemContainer<County>(County.class);
		
		provinceService = SpringContextHolder.getBean("provinceService");	
		cityService = SpringContextHolder.getBean("cityService");	
		countyService = SpringContextHolder.getBean("countyService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		orderService = SpringContextHolder.getBean("orderService");
		
		provinceContainer.addAll(provinceService.getAll());

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		phoneNosMap = new HashMap<String, String>();
		
		// 创建商品显示表格
		this.createCommodityTable();
		
		// 创建表格下方的组件
		this.createTableBottomComponents();
		
		// 创建收件人姓名与联系方式组件
		this.createReceiverNamePhoneComponents();

		// 创建收件人所在地区 组件
		this.createRegionComponents();
     	
		// 创建收件人所在街道 组件
		this.createStreetComponents();
		
		// 更新订单的备注信息 组件
		this.createNoteComponents();
		
		// 创建操作组件
		this.createOperatorButtons();
	}

	/**
	 *  创建商品显示表格
	 */
	private void createCommodityTable() {
		commodity_tb = new Table("已选商品：");
		commodity_tb.setWidth("100%");
		commodity_tb.setHeight("-1px");
		commodity_tb.setImmediate(true);
		commodity_tb.setSelectable(false);
		commodity_tb.setStyleName("striped");
		commodity_tb.setPageLength(5);
		commodity_tb.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		commodity_tb.setContainerDataSource(commodityContainer);
		commodity_tb.setVisibleColumns(VISIBLE_PROPERTIES);
		commodity_tb.setColumnHeaders(COL_HEADERS);
		this.addComponent(commodity_tb);

		commodity_tb.addGeneratedColumn("description", new DescriptionColumnGenerator());
		commodity_tb.addGeneratedColumn("salePrice", new SalePriceColumnGenerator());
		commodity_tb.setColumnHeader("salePrice", "售出单价");
		commodity_tb.addGeneratedColumn("quantity", new QuantityColumnGenerator());
		commodity_tb.setColumnHeader("quantity", "数量");
		commodity_tb.addGeneratedColumn("subtotal", new SubtotalColumnGenerator());
		commodity_tb.setColumnHeader("subtotal", "小计(元)");
		commodity_tb.addGeneratedColumn("operator", new OperatorColumnGenerator());
		commodity_tb.setColumnHeader("operator", "操作");
		commodity_tb.setColumnAlignment("operator", Table.ALIGN_CENTER);
	}

	/**
	 *  创建表格下方的组件
	 */
	private void createTableBottomComponents() {
		HorizontalLayout bottom_lo = new HorizontalLayout();
		bottom_lo.setWidth("100%");
		bottom_lo.setSpacing(true);
		this.addComponent(bottom_lo);

		shoppingContinue_bt = new Button("继续购物", this);
		shoppingContinue_bt.setImmediate(true);
		shoppingContinue_bt.addStyleName(BaseTheme.BUTTON_LINK);
		bottom_lo.addComponent(shoppingContinue_bt);
		
		HorizontalLayout bottomRight_lo = new HorizontalLayout();
		bottomRight_lo.setSpacing(true);
		bottom_lo.addComponent(bottomRight_lo);
		bottom_lo.setComponentAlignment(bottomRight_lo, Alignment.MIDDLE_RIGHT);
		
		totalCommodities_lb = new Label("", Label.CONTENT_XHTML);
		totalCommodities_lb.setWidth("-1px");
		bottomRight_lo.addComponent(totalCommodities_lb);
		
		totalExpense_lb = new Label("", Label.CONTENT_XHTML);
		totalExpense_lb.setWidth("-1px");
		bottomRight_lo.addComponent(totalExpense_lb);
	}

	/**
	 * 创建收件人姓名与联系方式组件
	 */
	private void createReceiverNamePhoneComponents() {
		HorizontalLayout namePhone_lo = new HorizontalLayout();
		namePhone_lo.setSpacing(true);
		this.addComponent(namePhone_lo);
		
		HorizontalLayout name_lo = new HorizontalLayout();
		namePhone_lo.addComponent(name_lo);
		
		Label name_lb = new Label("收件人姓名：");
		name_lb.setWidth("-1px");
		name_lo.addComponent(name_lb);
		
		receiverName_tf = new TextField();
		receiverName_tf.setWidth("132px");
		receiverName_tf.setRequired(true);
		receiverName_tf.setNullRepresentation("");
		name_lo.addComponent(receiverName_tf);

		HorizontalLayout phone_lo = new HorizontalLayout();
		namePhone_lo.addComponent(phone_lo);
		
		Label phone_lb = new Label("收件人联系电话：");
		phone_lb.setWidth("-1px");
		phone_lo.addComponent(phone_lb);
		
		receiverPhone_tf = new TextField();
		receiverPhone_tf.setRequired(true);
		receiverPhone_tf.setWidth("150px");
		receiverPhone_tf.setNullRepresentation("");
		receiverPhone_tf.addValidator(new RegexpValidator("\\d{7,12}", "电话号码格式错误！"));
		receiverPhone_tf.setValidationVisible(false);
		phone_lo.addComponent(receiverPhone_tf);
	}

	/**
	 * 创建收件人所在地区 组件
	 */
	private void createRegionComponents() {
		HorizontalLayout address_lo = new HorizontalLayout();
		address_lo.setSpacing(true);
		this.addComponent(address_lo);
		
		Label name_lb = new Label("所在地区：");
		name_lb.setWidth("-1px");
		address_lo.addComponent(name_lb);
		
		// 创建 省份 选择框
	 	province_cb = new ComboBox();
//	 	province_cb.setRequired(true);
	 	province_cb.setImmediate(true);
	 	province_cb.setWidth("140px");
	 	province_cb.setInputPrompt(" 请选择省 ");
	 	province_cb.setNullSelectionAllowed(false);
	 	province_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
	 	province_cb.setContainerDataSource(provinceContainer);
	 	address_lo.addComponent(province_cb);
	 	
		// 创建 城市 选择框
	 	city_cb = new ComboBox();
//	 	city_cb.setRequired(true);
	 	city_cb.setImmediate(true);
	 	city_cb.setReadOnly(true);
	 	city_cb.setWidth("140px");
	 	city_cb.setInputPrompt(" 请选择城市 ");
	 	city_cb.setNullSelectionAllowed(false);
	 	city_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
	 	city_cb.setContainerDataSource(cityContainer);
	 	address_lo.addComponent(city_cb);
	 	
		// 创建 县区 选择框
	 	county_cb = new ComboBox();
//	 	county_cb.setRequired(true);
	 	county_cb.setImmediate(true);
	 	county_cb.setReadOnly(true);
	 	county_cb.setWidth("140px");
	 	county_cb.setInputPrompt(" 请选择区县 ");
	 	county_cb.setNullSelectionAllowed(false);
	 	county_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
	 	county_cb.setContainerDataSource(countyContainer);
	 	address_lo.addComponent(county_cb);

		// 为 省份 选择框添加监听器
		province_cb.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				if(province_cb.getValue() == null) {
					city_cb.setReadOnly(true);
					county_cb.setReadOnly(true);
				} else {
					city_cb.setReadOnly(false);
					city_cb.removeAllItems();
     				cityContainer.addAll(cityService.getAllByProvince((Province)province_cb.getValue()));
     				city_cb.setContainerDataSource(cityContainer);
     				if(cityContainer.size() == 1) {
     					city_cb.setValue(cityContainer.firstItemId());
     				}
				}
			}
		});
     	
		// 为 城市 选择框添加监听器
     	city_cb.addListener(new ValueChangeListener() {
     		@Override
     		public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
     			if(city_cb.getValue() == null) {
     				county_cb.setReadOnly(true);
     			} else {
     				county_cb.setReadOnly(false);
     				county_cb.removeAllItems();
     				countyContainer.addAll(countyService.getAllByCity((City)city_cb.getValue()));
     				county_cb.setContainerDataSource(countyContainer);
     				if(countyContainer.size() == 1) {
     					county_cb.setValue(countyContainer.firstItemId());
     				}
     			}
     		}
     	});
	}

	/**
	 *  创建收件人所在街道 组件
	 */
	private void createStreetComponents() {
		HorizontalLayout street_lo = new HorizontalLayout();
		street_lo.setSpacing(true);
		street_lo.setWidth("100%");
		this.addComponent(street_lo);
		
		Label street_lb = new Label("所在街道：");
		street_lb.setWidth("-1px");
		street_lo.addComponent(street_lb);
		street_lo.setComponentAlignment(street_lb, Alignment.MIDDLE_LEFT);
		
		// 创建 街道 输入区域
		street_ta = new TextArea();
		street_ta.setWidth("100%");
		street_ta.setRows(1);
		street_ta.setMaxLength(255);
		street_ta.setNullRepresentation("");
		street_lo.addComponent(street_ta);
		street_lo.setExpandRatio(street_ta, 1.0f);
	}
	
	/**
	 *  更新订单的备注信息
	 */
	private void createNoteComponents() {
		HorizontalLayout note_lo = new HorizontalLayout();
		note_lo.setSpacing(true);
		note_lo.setWidth("100%");
		this.addComponent(note_lo);
		
		Label note_lb = new Label("备注信息：");
		note_lb.setWidth("-1px");
		note_lo.addComponent(note_lb);
		note_lo.setComponentAlignment(note_lb, Alignment.MIDDLE_LEFT);
		
		// 创建 街道 输入区域
		note_ta = new TextArea();
		note_ta.setWidth("100%");
		note_ta.setRows(1);
		note_ta.setMaxLength(1000);
		note_ta.setNullRepresentation("");
		note_lo.addComponent(note_ta);
		note_lo.setExpandRatio(note_ta, 1.0f);
	}

	/**
	 *  创建操作组件
	 */
	private void createOperatorButtons() {
		HorizontalLayout operator_hl = new HorizontalLayout();
		operator_hl.setSpacing(true);
		this.addComponent(operator_hl);
		
		makeOrder_bt = new Button("保存", this);
		makeOrder_bt.setImmediate(true);
		makeOrder_bt.addStyleName("default");
		operator_hl.addComponent(makeOrder_bt);
		
		cancel_bt = new Button("取消", this);
		cancel_bt.setImmediate(true);
		operator_hl.addComponent(cancel_bt);
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
					return description_label;
				}
			} 
			return null;
		}
	}
	
	/**
	 * 创建 单类商品的实际出售单价
	 */
	private class SalePriceColumnGenerator implements Table.ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Commodity commodity = (Commodity) itemId;
			Double originalPrice = commodity.getCommodityPrice();
			final Long commodityId = commodity.getId();
			Double salePrice = purchasePriceMap.get(commodity.getId());
			
			final TextField price_tf = new TextField();
			price_tf.setValidationVisible(false);
			price_tf.setImmediate(true);
			price_tf.setMaxLength(12);
			price_tf.setWidth("70px");
			price_tf.addValidator(new RegexpValidator("\\d+||\\d+\\.{1}\\d{1,2}", "商品出售价格格式错误！"));
			price_tf.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					if(!price_tf.isValid()) {
						commodity_tb.getApplication().getMainWindow().showNotification("商品出售价格格式错误, 请重新修正！", Notification.TYPE_WARNING_MESSAGE);
						price_tf.focus();
					} else {
						Double priceValue = Double.valueOf(price_tf.getValue()+"");
						purchasePriceMap.put(commodityId, priceValue);
						price_tf.setReadOnly(true);
						// 初始化价格统计信息
						initializeStatisticsInfo();
						initializeSubTotalInfo(commodity);
					}
				}
			});
			if(salePrice == null || salePrice.compareTo(originalPrice) == 0) {
				price_tf.setValue(originalPrice);
			} else {
				price_tf.setValue(salePrice);
			}
			
			price_tf.setReadOnly(true);
			commodityToTf.put(commodityId, price_tf);
			
			return price_tf;
		}
	}

	/**
	 * 购买数量 操作组件
	 */
	private class QuantityColumnGenerator implements Table.ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Commodity commodity = (Commodity) itemId;
			int currentQty = purchaseQty.get(commodity.getId());
			
			// 购买数量
			final ComboBox count_cb = new ComboBox();
			count_cb.setWidth("60px");
			count_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
			count_cb.setNullSelectionAllowed(false);
			count_cb.setImmediate(true);
			for(int i = 1; i <= 500; i++) {
				count_cb.addItem(i);
			}
			count_cb.setValue(currentQty);
			count_cb.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					int count = (Integer) count_cb.getValue();
					purchaseQty.put(commodity.getId(), count);
					// 初始化价格统计信息
					initializeStatisticsInfo();
					initializeSubTotalInfo(commodity);
				}
			});
			
			
			// 购买数量减一
			Button minus_bt = new Button();
			minus_bt.addStyleName("borderless");
			minus_bt.setIcon(ResourceDataCsr.minus_14_ico);
			minus_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					int count = (Integer) count_cb.getValue();
					if(count > 1) {
						count_cb.setValue(--count);
						purchaseQty.put(commodity.getId(), count);
					} else {
						commodity_tb.getApplication().getMainWindow().showNotification("购买数量不能小于 1 !", Notification.TYPE_WARNING_MESSAGE);
					}
				}
			});
			
			// 购买数量加一
			Button plus_bt = new Button();
			plus_bt.addStyleName("borderless");
			plus_bt.setIcon(ResourceDataCsr.plus_14_ico);
			plus_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					int count = (Integer) count_cb.getValue();
					if(count < 500) {
						count_cb.setValue(++count);
						purchaseQty.put(commodity.getId(), count);
					} else {
						commodity_tb.getApplication().getMainWindow().showNotification("单次购买数量不能超过500件!", Notification.TYPE_WARNING_MESSAGE);
					}
				}
			});
			
			HorizontalLayout layout = new HorizontalLayout();
			layout.setSpacing(true);
			layout.addComponent(minus_bt);
			layout.addComponent(count_cb);
			layout.addComponent(plus_bt);
			layout.setWidth("-1px");
			
			return layout;
		}
	}
	
	/**
	 * 创建 单类商品价格小计 组件
	 */
	private class SubtotalColumnGenerator implements Table.ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Commodity commodity = (Commodity) itemId;
			Long commodityId = commodity.getId();
			Double salePrice = purchasePriceMap.get(commodityId);
			Double subtotal = purchaseQty.get(commodityId) * salePrice;
			Label subTotal_lb = new Label();
			subTotal_lb.setValue(DF_TWO_PRECISION.format(subtotal));
			commodityToLb.put(commodityId, subTotal_lb);
			return subTotal_lb;
		}
	}
	
	/**
	 * 创建 删除商品  操作组件
	 */
	private class OperatorColumnGenerator implements Table.ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Commodity commodity = (Commodity) itemId;
			
			// 删除购物车
			Button delete_bt = new Button("删除");
			delete_bt.setWidth("-1px");
			delete_bt.addStyleName(BaseTheme.BUTTON_LINK);
			delete_bt.setIcon(ResourceDataCsr.cart_remove_16_ico);
			delete_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					Long commodityId = commodity.getId();
					purchaseCommodities.remove(commodityId);
					purchaseQty.remove(commodityId);
					purchasePriceMap.remove(commodityId);
					commodityToTf.remove(commodityId);
					commodityContainer.removeItem(commodity);
					// 初始化价格统计信息
					initializeStatisticsInfo();
				}
			});
			
			// 修改出售价格
			Button editSalePrice_bt = new Button("修改售出价");
			editSalePrice_bt.setWidth("-1px");
			editSalePrice_bt.addStyleName(BaseTheme.BUTTON_LINK);
			editSalePrice_bt.setIcon(ResourceDataCsr.edit_16_ico);
			editSalePrice_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					Long commodityId = commodity.getId();
					TextField price_tf = commodityToTf.get(commodityId);
					price_tf.setReadOnly(false);
					price_tf.focus();
				}
			});
			
			HorizontalLayout operator_hlo = new HorizontalLayout();
			operator_hlo.setSpacing(true);
			operator_hlo.setWidth("-1px");
			operator_hlo.addComponent(editSalePrice_bt);
			operator_hlo.addComponent(delete_bt);
			
			return operator_hlo;
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == shoppingContinue_bt) {
			showCommodityStoreroomWindow();
		} else if(source == makeOrder_bt) {
			boolean success = executeMakeOrder();
			if(success) {
				try {
					Method method = sourceTableView.getClass().getMethod("refreshTable", boolean.class);
					method.invoke(sourceTableView, true);
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("jrh 更新订单后，刷新界面时出现异常"+e.getMessage(), e);
				} 
				makeOrder_bt.getApplication().getMainWindow().showNotification("更新订单成功！");
				makeOrder_bt.getApplication().getMainWindow().removeWindow(this);
			}
		} else if(source == cancel_bt) {
			cancel_bt.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * @Description 描述：显示商品陈列界面
	 *
	 * @author  jrh
	 * @date    2014年3月12日 上午9:50:07 void
	 */
	private void showCommodityStoreroomWindow() {
		if(commodityStoreroomWindow == null) {
			commodityStoreroomWindow = new CommodityStoreroomWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(commodityStoreroomWindow);
		
		commodityStoreroomWindow.updateUiDatas(order, purchaseCommodities, purchaseQty);
		this.getApplication().getMainWindow().addWindow(commodityStoreroomWindow);
	}

	/**
	 * 执行下单
	 */
	private boolean executeMakeOrder() {
		try {
			if(commodityContainer.size() == 0) {
				receiverName_tf.getApplication().getMainWindow().showNotification("选购商品不能为空，请选购后重试！！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			
			String receiverName = StringUtils.trimToEmpty((String)receiverName_tf.getValue());
			if("".equals(receiverName)) {
				receiverName_tf.getApplication().getMainWindow().showNotification("收件人姓名不能为空！！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			
			String receiverPhone = StringUtils.trimToEmpty((String)receiverPhone_tf.getValue());
			if(isEncryptMobile) {		// 如果现在是加密显示号码，则判断当前值是否在加密号码集合中存在
				if(phoneNosMap.keySet().contains(receiverPhone)) {
					receiverPhone = phoneNosMap.get(receiverPhone);
				} 
			} 
			if(!receiverPhone.matches("\\d{5,12}")) {
				receiverPhone_tf.getApplication().getMainWindow().showNotification("您没有输入正确的电话号码，请核查后重试！！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			String street = StringUtils.trimToEmpty((String)street_ta.getValue());
			if("".equals(street)) {
				street_ta.getApplication().getMainWindow().showNotification("收件人住所所在街道不能为空！！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			
// jrh 按谷声需求，将地址的必填需求去掉了
//			if(!province_cb.isValid() || !city_cb.isValid() || !county_cb.isValid()) {
//				province_cb.getApplication().getMainWindow().showNotification("收件人所在地区的省、市、区都不能为空！！", Notification.TYPE_WARNING_MESSAGE);
//				return false;
//			}
			
			String note = StringUtils.trimToEmpty((String) note_ta.getValue());
			if("".equals(note)) {
				note = null;
			}
			
			String province = "";
			if(province_cb.getValue() != null) {
				province = ((Province) province_cb.getValue()).getName();
			}
			
			String city = "";
			if(city_cb.getValue() != null) {
				city = ((City) city_cb.getValue()).getName();
			}

			String county = "";
			if(county_cb.getValue() != null) {
				county = ((County) county_cb.getValue()).getName();
			}
			
			Set<Orderdetails> orderDetails = order.getOrderdetails();
			ArrayList<Long> currentCommodityIds = new ArrayList<Long>();	// 当前选中的商品
			HashMap<Long, Integer> oldDetailsOrderNums = new HashMap<Long, Integer>();	// 之前订单对应商品的购买数量
			
			for(Commodity commodity : purchaseCommodities.values()) {
				Long commodityId = commodity.getId();
				currentCommodityIds.add(commodityId);
				int count = purchaseQty.get(commodityId);
				Double salePrice = purchasePriceMap.get(commodityId);
				Double subTotalPrice = salePrice * count;
				
				if(!oldCommodityIds.contains(commodityId)) {
					Orderdetails orderDetail = new Orderdetails();
					orderDetail.setCommodity(commodity);
					orderDetail.setCommodityName(commodity.getCommodityName());
					orderDetail.setOrderNum(count);
					orderDetail.setOrder(order);
					orderDetail.setSalePrice(salePrice);
					orderDetail.setSubTotalPrice(subTotalPrice);
					orderDetail.setDomain(domain);
					orderDetails.add(orderDetail);
				} else {
					for(Orderdetails detail : orderDetails) {
						Commodity oldCommodity = detail.getCommodity();
						if(oldCommodity.getId().equals(commodityId)) {
							oldDetailsOrderNums.put(detail.getId(), detail.getOrderNum());	// 必须放在下面几行之前
							detail.setOrderNum(count);
							detail.setSalePrice(salePrice);
							detail.setSubTotalPrice(subTotalPrice);
							break;
						}
					}
				}
			}
			
			// 删除被移除的商品
			ArrayList<Orderdetails> needRemoveDetails = new ArrayList<Orderdetails>();
			for(Orderdetails detail : orderDetails) {
				Commodity oldCommodity = detail.getCommodity();
				if(!currentCommodityIds.contains(oldCommodity.getId())) {
					needRemoveDetails.add(detail);
				}
			}
			for(Orderdetails removeDetail : needRemoveDetails) {
				orderDetails.remove(removeDetail);
			}

			// 从新更新订单信息
			order.setCustomerName(receiverName);
			order.setCustomerPhoneNumber(receiverPhone);
			order.setProvince(province);
			order.setCity(city);
			order.setCounty(county);
			order.setStreet(street);
			order.setNote(note);
			order.setTotalPrice(totalExpense);
			order.setOrderdetails(orderDetails);
			
			orderService.updateOrderAndOrderDetails(order, oldDetailsOrderNums, needRemoveDetails);	// 执行更新
		} catch (Exception e) {
			e.printStackTrace();
			street_ta.getApplication().getMainWindow().showNotification("更新订单失败，请核查后重试！！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("jrh 坐席更新订单失败！---》 "+e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	/**
	 *  初始化统计信息
	 */
	private void initializeStatisticsInfo() {
		long size = purchaseCommodities.size();
		totalCommodities_lb.setValue("<font color='red'>"+size+" 件商品</font>");
		
		totalExpense = 0.00;
		for(Commodity commodity : purchaseCommodities.values()) {
			Long id = commodity.getId();
			Double originalPrice = commodity.getCommodityPrice();
			Double salePrice = purchasePriceMap.get(id);
			int quantity = purchaseQty.get(id);
			if(salePrice == null || salePrice.compareTo(originalPrice) == 0) {
				totalExpense += quantity * originalPrice;
			} else {
				totalExpense += quantity * salePrice;
			}
		}

		totalExpense_lb.setValue("<font color='red'> 总计："+DF_TWO_PRECISION.format(totalExpense)+" 元</font>");
	}
	
	/**
	 *  初始化小计信息
	 */
	private void initializeSubTotalInfo(Commodity commodity) {
		Long id = commodity.getId();
		Double salePrice = purchasePriceMap.get(id);
		int quantity = purchaseQty.get(id);
		
		Label subtotal_lb = commodityToLb.get(id);
		if(subtotal_lb != null) {
			subtotal_lb.setValue(DF_TWO_PRECISION.format(salePrice*quantity));
		}
	}

	/**
	 * 清空数据
	 */
	private void clearComponentsValue() {
		purchasePriceMap.clear();
		purchaseQty.clear();
		purchaseCommodities.clear();
		oldCommodityIds.clear();
		commodityToTf.clear();
		commodityToLb.clear();
		
		receiverName_tf.setValue("");
		receiverPhone_tf.setValue("");
		province_cb.setValue(null);
		if(!city_cb.isReadOnly()) {
			city_cb.setValue(null);
		}
		if(!county_cb.isReadOnly()) {
			county_cb.setValue(null);
		}
		street_ta.setValue("");
		note_ta.setValue("");
	}
	
	/**
	 * @Description 描述：回显订单编辑界面的信息
	 *
	 * @author  jrh
	 * @date    2014年3月11日 下午4:27:53
	 * @param order void
	 */
	public void updateUiDatas(Order order) {
		this.order = order;

		this.clearComponentsValue();
		
		for(Orderdetails orderDetail : order.getOrderdetails()) {
			Commodity comm = orderDetail.getCommodity();
			Long commId = comm.getId();
			purchaseCommodities.put(commId, comm);
			purchaseQty.put(commId, orderDetail.getOrderNum());
			oldCommodityIds.add(commId);
		}
		
		commodityContainer.removeAllItems();
		commodityContainer.addAll(purchaseCommodities.values());
		
		// 设置客户姓名
		receiverName_tf.setValue(order.getCustomerName());
		
		// 设置联系方式
		phoneNosMap.clear();
		receiverPhone_tf.setValue("");
		String originalNum = order.getCustomerPhoneNumber();
		if(isEncryptMobile) {	// 判断电话号码是否要加密
			String encrypted = telephoneService.encryptMobileNo(originalNum);
			phoneNosMap.put(encrypted, originalNum);
			receiverPhone_tf.setValue(encrypted);
		} else {
			phoneNosMap.put(originalNum, originalNum);
			receiverPhone_tf.setValue(originalNum);
		}
		
		// 更新收货地址
		for(Province pro : provinceContainer.getItemIds()) {
			if(pro.getName().equals(order.getProvince())) {
				province_cb.setValue(pro);
				break;
			}
		}
		
		if(!city_cb.isReadOnly()) {
			for(City city : cityContainer.getItemIds()) {
				if(city.getName().equals(order.getCity())) {
					city_cb.setValue(city);
					break;
				}
			}
		}
		
		if(!county_cb.isReadOnly()) {
			for(County county : countyContainer.getItemIds()) {
				if(county.getName().equals(order.getCounty())) {
					county_cb.setValue(county);
					break;
				}
			}
		}
		
		street_ta.setValue(order.getStreet());
		note_ta.setValue(order.getNote());
	}
	
	/**
	 * 由 CommodityOrderCreator 调用，当点击继续购物时，需要刷新界面信息
	 * @param purchaseCommodities	客户购买的商品
	 * @param purchaseQty			客户购买的商品对应的数量
	 */
	public void updateTableSource(Map<Long, Commodity> purchaseCommodities, Map<Long, Integer> purchaseQty) {
		this.purchaseCommodities = purchaseCommodities;
		this.purchaseQty = purchaseQty;
		
		commodityContainer.removeAllItems();
		commodityContainer.addAll(purchaseCommodities.values());
	}
	
}
