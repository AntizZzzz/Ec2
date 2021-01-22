package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CommodityService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class CommodityComplexFilter extends VerticalLayout implements ClickListener, Content {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private GridLayout gridLayout;											// 面板中的布局管理器
	private TextField id_tf;												// 商品id输入框
	private TextField name_tf;												// 商品名称输入框
	private TextField priceMoreThan_tf;										// “单价大于等于”输入框
	private NativeSelect andOr2price_ns;									// 单价关联词选择框，and 或 or
	private TextField priceLessThan_tf;										// “单价小于等于”输入框
	private TextField stockQtyMoreThan_tf;									// “库存大于等于”输入框
	private NativeSelect andOr2stockCount_ns;								// 单价关联词选择框，and 或 or
	private TextField stockQtyLessThan_tf;									// “库存小于等于”输入框

	private Button search_button;											// 刷新结果按钮
	private Button clear_button;											// 清空输入内容

	private FlipOverTableComponent<Commodity> commodityTableFlip;			// 商品显示表格的翻页组件
	
	private Domain domain;													// 当前登陆用户所属域
	private String exten;													// 当前坐席使用分机
	private CommodityService commodityService;								// 商品服务类

	public CommodityComplexFilter(User loginUser) {
		// 初始化各种参数
		this.setSpacing(true);
		this.domain = loginUser.getDomain();

		exten = ShareData.userToExten.get(loginUser.getId());
		commodityService = SpringContextHolder.getBean("commodityService");
		
		// 创建主要组件
		gridLayout = new GridLayout(4, 3);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 创建按商品 ID  的搜索组件
		createIdComponents();
		
		// 创建按商品 名称    的搜索组件
		createNameComponents();
		
		// 创建按商品 单价大于等于    的搜索组件
		createPriceMoreThanComponents();
		
		// 创建按商品 单价关联词   的搜索组件
		createAndOr2priceComponents();
		
		// 创建按商品 单价小于等于    的搜索组件
		createPriceLessThanComponents();

		// 创建按商品 库存大于等于    的搜索组件
		createStockCountMoreThanComponents();
		
		// 创建按商品 库存关联词   的搜索组件
		createAndOr2stockCountComponents();
		
		// 创建按商品 库存大于等于    的搜索组件
		createStockCountLessThanComponents();
		
		// 创建 操作 组件
		createOperatorComponents();
		
	}
	
	/**
	 *  创建按商品 ID  的搜索组件
	 */
	private void createIdComponents() {
		HorizontalLayout id_hl = new HorizontalLayout();
		id_hl.setSpacing(true);
		id_hl.setWidth("100%");
		gridLayout.addComponent(id_hl, 0, 0);
		
		Label id_label = new Label("商品编号：");
		id_label.setWidth("-1px");
		id_hl.addComponent(id_label);
		
		id_tf = new TextField();
		id_tf.setWidth("100%");
		id_tf.addValidator(new RegexpValidator("\\d+", "商品编号只能由数字组成"));
		id_tf.setValidationVisible(false);
		id_hl.addComponent(id_tf);
		id_hl.setExpandRatio(id_tf, 1.0f);
	}

	/**
	 *  创建按商品 名称    的搜索组件
	 */
	private void createNameComponents() {
		HorizontalLayout name_hl = new HorizontalLayout();
		name_hl.setSpacing(true);
		name_hl.setWidth("100%");
		gridLayout.addComponent(name_hl, 1, 0, 2, 0);
		
		Label name_label = new Label("商品名称：");
		name_label.setWidth("-1px");
		name_hl.addComponent(name_label);
		
		name_tf = new TextField();
		name_tf.setWidth("100%");
		name_hl.addComponent(name_tf);
		name_hl.setExpandRatio(name_tf, 1.0f);
	}

	/**
	 *  创建按商品 单价大于等于    的搜索组件
	 */
	private void createPriceMoreThanComponents() {
		HorizontalLayout priceMoreThan_hl = new HorizontalLayout();
		priceMoreThan_hl.setSpacing(true);
		gridLayout.addComponent(priceMoreThan_hl, 0, 1);
		
		Label priceMoreThan_label = new Label("单价大于等于：");
		priceMoreThan_label.setWidth("-1px");
		priceMoreThan_hl.addComponent(priceMoreThan_label);
		
		priceMoreThan_tf = new TextField();
		priceMoreThan_tf.setWidth("100%");
		priceMoreThan_tf.addValidator(new RegexpValidator("\\d+", "价格必须是大于0的整数"));
		priceMoreThan_tf.setValidationVisible(false);
		priceMoreThan_hl.addComponent(priceMoreThan_tf);
	}
	
	/**
	 *  创建按商品 单价关联词   的搜索组件
	 */
	private void createAndOr2priceComponents() {
		HorizontalLayout andOr2price_hl = new HorizontalLayout();
		andOr2price_hl.setSpacing(true);
		gridLayout.addComponent(andOr2price_hl, 1, 1);
		
		Label andOr2price_label = new Label("单价关联词：");
		andOr2price_label.setWidth("-1px");
		andOr2price_hl.addComponent(andOr2price_label);
		
		andOr2price_ns = new NativeSelect();
		andOr2price_ns.addItem("AND");
		andOr2price_ns.addItem("OR");
		andOr2price_ns.setValue("AND");
		andOr2price_ns.setWidth("101px");
		andOr2price_ns.setNullSelectionAllowed(false);
		andOr2price_hl.addComponent(andOr2price_ns);
	}

	/**
	 *  创建按商品 单价小于等于    的搜索组件
	 */
	private void createPriceLessThanComponents() {
		HorizontalLayout priceLessThan_hl = new HorizontalLayout();
		priceLessThan_hl.setSpacing(true);
		gridLayout.addComponent(priceLessThan_hl, 2, 1);
		
		Label priceLessThan_label = new Label("单价小于等于：");
		priceLessThan_label.setWidth("-1px");
		priceLessThan_hl.addComponent(priceLessThan_label);
		
		priceLessThan_tf = new TextField();
		priceLessThan_tf.setWidth("100%");
		priceLessThan_tf.addValidator(new RegexpValidator("\\d+", "价格必须是大于0的整数"));
		priceLessThan_tf.setValidationVisible(false);
		priceLessThan_hl.addComponent(priceLessThan_tf);
	}

	/**
	 *  创建按商品 库存大于等于    的搜索组件
	 */
	private void createStockCountMoreThanComponents() {
		HorizontalLayout stockCountMoreThan_hl = new HorizontalLayout();
		stockCountMoreThan_hl.setSpacing(true);
		gridLayout.addComponent(stockCountMoreThan_hl, 0, 2);
		
		Label stockCountMoreThan_label = new Label("库存大于等于：");
		stockCountMoreThan_label.setWidth("-1px");
		stockCountMoreThan_hl.addComponent(stockCountMoreThan_label);
		
		stockQtyMoreThan_tf = new TextField();
		stockQtyMoreThan_tf.setWidth("100%");
		stockQtyMoreThan_tf.addValidator(new RegexpValidator("\\d+", "库存必须是大于0的整数"));
		stockQtyMoreThan_tf.setValidationVisible(false);
		stockCountMoreThan_hl.addComponent(stockQtyMoreThan_tf);
	}

	/**
	 * 
	 *  创建按商品 库存关联词   的搜索组件
	 */
	private void createAndOr2stockCountComponents() {
		HorizontalLayout andOr2stockCount_hl = new HorizontalLayout();
		andOr2stockCount_hl.setSpacing(true);
		gridLayout.addComponent(andOr2stockCount_hl, 1, 2);
		
		Label andOr2stockCount_label = new Label("库存关联词：");
		andOr2stockCount_label.setWidth("-1px");
		andOr2stockCount_hl.addComponent(andOr2stockCount_label);
		
		andOr2stockCount_ns = new NativeSelect();
		andOr2stockCount_ns.addItem("AND");
		andOr2stockCount_ns.addItem("OR");
		andOr2stockCount_ns.setValue("AND");
		andOr2stockCount_ns.setWidth("101px");
		andOr2stockCount_ns.setNullSelectionAllowed(false);
		andOr2stockCount_hl.addComponent(andOr2stockCount_ns);
	}

	/**
	 *  创建按商品 库存大于等于    的搜索组件
	 */
	private void createStockCountLessThanComponents() {
		HorizontalLayout stockCountLessThan_hl = new HorizontalLayout();
		stockCountLessThan_hl.setSpacing(true);
		gridLayout.addComponent(stockCountLessThan_hl, 2, 2);
		
		Label stockCountLessThan_label = new Label("库存小于等于：");
		stockCountLessThan_label.setWidth("-1px");
		stockCountLessThan_hl.addComponent(stockCountLessThan_label);
		
		stockQtyLessThan_tf = new TextField();
		stockQtyLessThan_tf.setWidth("100%");
		stockQtyLessThan_tf.addValidator(new RegexpValidator("\\d+", "价格必须是大于0的整数"));
		stockQtyLessThan_tf.setValidationVisible(false);
		stockCountLessThan_hl.addComponent(stockQtyLessThan_tf);
	}

	/**
	 *  创建 操作 组件
	 */
	private void createOperatorComponents() {
		// 查询按钮
		search_button = new Button("查 询", (ClickListener)this);
		search_button.setStyleName("default");
		gridLayout.addComponent(search_button, 3, 0);
		gridLayout.setComponentAlignment(search_button, Alignment.MIDDLE_RIGHT);
		
		// 清空按钮
		clear_button = new Button("清 空");
		clear_button.addListener(this);
		gridLayout.addComponent(clear_button, 3, 1);
		gridLayout.setComponentAlignment(clear_button, Alignment.MIDDLE_RIGHT);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_button) {
			if(!priceMoreThan_tf.isValid() || !priceLessThan_tf.isValid()) {
				priceMoreThan_tf.getApplication().getMainWindow().showNotification("商品价格必须是由大是0的整数！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			if(!stockQtyMoreThan_tf.isValid() || !stockQtyLessThan_tf.isValid()) {
				stockQtyMoreThan_tf.getApplication().getMainWindow().showNotification("商品库存必须是是大于0的整数！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			executeSearch();
		} else if(source == clear_button) {
			clearComponentsValue();
		}
	}

	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		try {
			String count_sql = createCountSql();
			String search_sql = count_sql.replaceFirst("count\\(c\\)", "c")  +" order by c.id desc";
			commodityTableFlip.setSearchSql(search_sql);
			commodityTableFlip.setCountSql(count_sql);
			commodityTableFlip.refreshToFirstPage();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 坐席在弹屏界面下高级查询商品时出现异常---》"+e.getMessage(), e);
		}
	}

	/**
	 * 创建动态的 count 搜索语句
	 * @return String 返回搜索语句
	 */
	private String createCountSql() {
		// 按商品Id  查询的搜索语句
		String id_sql = "";
		String inputId = id_tf.getValue().toString().trim();
		if(!"".equals(inputId)) {
			id_sql = " and c.id = " + inputId;
		}
		
		// 只显示当前工作项目关联的商品
		String commdityIdSql = createDynamicCommdityIdSql();
		
		// 按商品名称    查询的搜索语句
		String name_sql = "";
		String inputName = name_tf.getValue().toString().trim();
		if(!"".equals(inputName)) {
			name_sql = " and c.commodityName like '%" + inputName + "%'";
		}

		// 按商品单价范围    查询的搜索语句
		String priceScope_sql = "";
		String inputPriceMT = StringUtils.trimToEmpty((String) priceMoreThan_tf.getValue());
		String inputPriceLT = StringUtils.trimToEmpty((String) priceLessThan_tf.getValue());
		if(!"".equals(inputPriceMT) && !"".equals(inputPriceLT)) {
			if(Integer.parseInt(inputPriceLT) < Integer.parseInt(inputPriceMT)) {
				andOr2price_ns.setValue("OR");
				priceScope_sql = " and (c.commodityPrice >= " + inputPriceMT + " or c.commodityPrice <= " +inputPriceLT + ")";
			} else { 
				andOr2price_ns.setValue("AND");
				priceScope_sql = " and (c.commodityPrice >= " + inputPriceMT + " and c.commodityPrice <= " +inputPriceLT + ")";
			}
		} else if(!"".equals(inputPriceMT) && "".equals(inputPriceLT)) {
			priceScope_sql = " and c.commodityPrice >= " + inputPriceMT;
		} else if("".equals(inputPriceMT) && !"".equals(inputPriceLT)) {
			priceScope_sql = " and c.commodityPrice <= " + inputPriceLT;
		} 

		// 按商品库存范围     查询的搜索语句
		String stockCountScope_sql = "";
		String inputStockCountMT = StringUtils.trimToEmpty((String) stockQtyMoreThan_tf.getValue());
		String inputStockCountLT = StringUtils.trimToEmpty((String) stockQtyLessThan_tf.getValue());
		if(!"".equals(inputStockCountMT) && !"".equals(inputStockCountLT)) {
			if(Integer.parseInt(inputStockCountLT) < Integer.parseInt(inputStockCountMT)) {
				andOr2stockCount_ns.setValue("OR");
				stockCountScope_sql = " and (c.stockQty >= " + inputStockCountMT + " or c.stockQty <= " +inputStockCountLT + ")";
			} else { 
				andOr2stockCount_ns.setValue("AND");
				stockCountScope_sql = " and (c.stockQty >= " + inputStockCountMT + " and c.stockQty <= " +inputStockCountLT + ")";
			}
		} else if(!"".equals(inputStockCountMT) && "".equals(inputStockCountLT)) {
			stockCountScope_sql = " and c.stockQty >= " + inputStockCountMT;
		} else if("".equals(inputStockCountMT) && !"".equals(inputStockCountLT)) {
			stockCountScope_sql = " and c.stockQty <= " + inputStockCountLT;
		} 
		
		String dynamic_sql = id_sql + commdityIdSql + name_sql + priceScope_sql + stockCountScope_sql;
		return "select count(c) from Commodity as c where c.domain.id = " +domain.getId() + dynamic_sql;
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
		return " and c.id in ("+commodityIdStr+")";
	}
	
	/**
	 * 清空组件的值
	 */
	private void clearComponentsValue() {
		id_tf.setValue("");
		name_tf.setValue("");
		
		priceMoreThan_tf.setValue("");
		andOr2price_ns.setValue("AND");
		priceLessThan_tf.setValue("");
		
		stockQtyMoreThan_tf.setValue("");
		andOr2stockCount_ns.setValue("AND");
		stockQtyLessThan_tf.setValue("");
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return gridLayout;
	}

	/**
	 * 设置翻页组件及商品表格的值 
	 * @param commodityTableFlip
	 */
	public void setCommodityTableFlip(FlipOverTableComponent<Commodity> commodityTableFlip) {
		this.commodityTableFlip = commodityTableFlip;
	}

}
