package com.jiangyifen.ec2.ui.mgr.commoditymanange;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.enumtype.CommodityStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.CommodityManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.UiUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *添加，可以添加商品
 * @author chb
 */
@SuppressWarnings("serial")
public class AddCommodity extends Window implements Button.ClickListener {
/**
 * 主要组件输出	
 */
	//Form输出
	private FormLayout formLayout;

	//form组件
	private TextField commodityName;
	private ComboBox commodityStatusComboBox;
	private TextField commodityPrice;
	private TextField stockQty_tf;
	private TextArea description;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

/**
 * 其他组件
 */
	//持有Commodity控制对象的引用
	private CommodityManagement commodityManagement;
	private Commodity commodity;
	private Domain domain;				// 当前登陆商品所属域
	
	private CommonService commonService;
	
	/**
	 * 添加Commodity
	 * @param project
	 */
	public AddCommodity(CommodityManagement commodityManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.commodityManagement=commodityManagement;
		
		domain = SpringContextHolder.getDomain();
		//Service初始化
		commonService=SpringContextHolder.getBean("commonService");
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//From输出
		formLayout=buildFormLayout();
		windowContent.addComponent(formLayout);
	}
	
	/**
	 * 创建form表格内的输出
	 * @return
	 */
	private FormLayout buildFormLayout() {
		FormLayout formLayout=new FormLayout();
		//商品名
		commodityName = new TextField("商品名称");
		commodityName.setRequired(true);
		commodityName.setNullRepresentation("");
		commodityName.setRequiredError("商品名称不能为空");
		commodityName.setInputPrompt("请输入商品名称");
		commodityName.setWidth("180px");
		formLayout.addComponent(commodityName);
		
		//商品状态
		commodityStatusComboBox=new ComboBox("商品状态");
		CommodityStatus[] commodityStatus = CommodityStatus.values();
		for(CommodityStatus commodityStatusTemp:commodityStatus){
			commodityStatusComboBox.addItem(commodityStatusTemp);
		}
		commodityStatusComboBox.setRequired(true);
		commodityStatusComboBox.setTextInputAllowed(false);
		commodityStatusComboBox.setNullSelectionAllowed(false);
		commodityStatusComboBox.setWidth("180px");
		commodityStatusComboBox.setRequiredError("商品状态不能为空");
		formLayout.addComponent(commodityStatusComboBox);
		
		//商品价格
		commodityPrice = new TextField("商品价格");
		commodityPrice.setRequired(true);
		commodityPrice.setPropertyDataSource(new TextField());
		commodityPrice.setNullRepresentation("");
		commodityPrice.setInputPrompt("请输入商品价格");
		formLayout.addComponent(commodityPrice);
		
		//商品库存
		stockQty_tf = new TextField("商品库存");
		stockQty_tf.setRequired(true);
		stockQty_tf.setNullRepresentation("");
		stockQty_tf.addValidator(new RegexpValidator("\\d+", "库存量必须由数字组成"));
		stockQty_tf.setValidationVisible(false);
		formLayout.addComponent(stockQty_tf);

		//商品描述
		description = new TextArea("描述信息");
		description.setNullRepresentation("");
		description.setColumns(15);
		description.setRows(3);
		description.setWordwrap(true);
		description.setInputPrompt("请输入描述信息！");
		formLayout.addComponent(description);
		
		formLayout.addComponent(buildButtonsLayout());
		return formLayout;
	}
	
	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存");
		save.setStyleName("default");
		save.addListener(this);
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);

		return buttonsLayout;
	}
	
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 * @return
	 */
	private boolean executeSave() {
		try {
			//商品名称是否为空的判断
			String commodityNameStr=UiUtil.getComponentValue(commodityName);
			if(commodityNameStr.equals("")){
				throw new RuntimeException("商品名称不能为空");
			}

			//商品价格是否为空的判断
			String commodityPriceStr=UiUtil.getComponentValue(commodityPrice);
			if(commodityPriceStr.equals("")){
				throw new RuntimeException("商品价格不能为空");
			}
			
			Double commodityPriceDouble=0.0d;
			try {
				commodityPriceDouble=Double.parseDouble(commodityPriceStr);
			} catch (Exception e) {
				throw new RuntimeException("商品价格必须是整数或小数");
			}

			// 商品库存
			String stockQtyStr = StringUtils.trimToEmpty((String) stockQty_tf.getValue());
			if("".equals(stockQtyStr) || !stockQty_tf.isValid()) {
				throw new RuntimeException("商品库存不能为空，并且只能由数字组成");
			}
			long stockQty = Long.parseLong(stockQtyStr);
			
			//商品的描述信息
			String descriptionStr=UiUtil.getComponentValue(description);
			
			//==============持久化=====================//
			//商品
			commodity=new Commodity();
			commodity.setDomain(domain);
			commodity.setCommodityName(commodityNameStr);
			commodity.setCommodityStatus((CommodityStatus)commodityStatusComboBox.getValue());
			commodity.setCommodityPrice(commodityPriceDouble);
			commodity.setStockQty(stockQty);
			commodity.setDescription(descriptionStr);
			commodity=(Commodity) commonService.update(commodity);
			
			//刷新commodityManagement的Table在当前页
			commodityManagement.updateTable(true);
			commodityManagement.getTable().setValue(null);
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getMessage()!=null){
				this.getApplication().getMainWindow().showNotification("保存商品失败，"+e.getMessage(), Notification.TYPE_WARNING_MESSAGE);
			}else{
				this.getApplication().getMainWindow().showNotification("保存商品失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			}
			return false;
		} 
		return true;
	}
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		commodityName.setValue(null);
		commodityStatusComboBox.setValue(CommodityStatus.ONSALE);
		commodityPrice.setValue(null);
		stockQty_tf.setValue("");
		description.setValue(null);
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			Boolean isSuccess=executeSave();
			if(isSuccess){
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			this.getParent().removeWindow(this);
		}
	}
}
