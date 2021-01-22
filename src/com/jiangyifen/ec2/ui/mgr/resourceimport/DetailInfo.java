package com.jiangyifen.ec2.ui.mgr.resourceimport;

import java.util.Set;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.ui.FlipOverTableComponentUseNativeSql;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceImport;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 由于能显示此窗口，说明只选中了一个批次的条目
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class DetailInfo extends Window implements Button.ClickListener,Window.CloseListener{
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

/**
 * 主要组件
 */
	//关键字和搜索按钮
	private TextField keyWord;
	private Button search;

	//表格
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private boolean isEncryptMobile = true;						// 默认使用加密的电话号码和手机号
	private CustomerResourceService customerResourceService;
	private FlipOverTableComponentUseNativeSql<CustomerResource> flip;
	
/**
 * 其他组件
 */
	//持有资源导入组件的引用
	private ResourceImport resourceImport;
	private CustomerResourceBatch batch;
	
/**
 * 构造器
 * @param resourceImport 持有父组件的引用
 */
	public DetailInfo(ResourceImport resourceImport) {
		this.center();
		this.setModal(true);
		this.addListener((Window.CloseListener)this);
		this.setResizable(false);
		this.resourceImport=resourceImport;
		
		customerResourceService=SpringContextHolder.getBean("customerResourceService");
		
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		//创建一个windowContent的组件
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		windowContent.setSizeUndefined();
		windowContent.setSpacing(true);
		windowContent.setMargin(false, true, true, true);
		this.setContent(windowContent);
		
		// 搜索   
		windowContent.addComponent(buildSearchLayout());
		//Sql语句初始化
		search.click();
		// 表格和按钮
		windowContent.addComponent(buildTabelAndButtonsLayout());
		// 设置Button样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	
	/**
	 *  创建搜索输出（Search）
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSizeUndefined();
		searchLayout.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		searchLayout.addComponent(constrantLayout);

		// 关键字
		constrantLayout.addComponent(new Label("姓名:"));

		// 输入区域
		keyWord = new TextField();
		keyWord.setWidth("6em");
		keyWord.setInputPrompt("姓名");
		keyWord.setDescription("可以按照姓名模糊查询");
		keyWord.setStyleName("search");
		constrantLayout.addComponent(keyWord);

		// 搜索按钮
		search = new Button("搜索");
		search.addListener((Button.ClickListener)this);
		searchLayout.addComponent(search);

		return searchLayout;
	}
	
	/**
	 *  创建表格和按钮输出（Table）
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSizeUndefined();
		tabelAndButtonsLayout.setSpacing(true);
		//创建表格
		table = createFormatColumnTable();
		table.setStyleName("striped");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
//		table.setSizeUndefined();	// jrh
		table.setWidth("673px");
		tabelAndButtonsLayout.addComponent(table);
		
		//创建按钮
		tabelAndButtonsLayout.addComponent(buildButtonsLayout());
		
		return tabelAndButtonsLayout;
	}

	/**
	 * jrh
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} 
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建Table下的按钮输出
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setWidth("100%");
		tableButtons.setSpacing(true);

		// 右侧按钮（翻页组件）
		flip =new FlipOverTableComponentUseNativeSql<CustomerResource>(CustomerResource.class,
				customerResourceService, table, sqlSelect, sqlCount,null);
		table.setPageLength(15);
		flip.setPageLength(15);
		
		//设置表格头部显示
		Object[] visibleColumns=new Object[] { "id", "name","sex", "birthdayStr","telephones"};
		String[] columnHeaders=new String[] { "客户编号", "姓名", "性别","生日","电话号码"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		table.setColumnWidth("name", 150);
		table.setColumnWidth("sex", 100);
		table.setColumnWidth("birthdayStr", 150);
		// jrh
		table.addGeneratedColumn("telephones", new TelephonesColumnGenerator());
		
		//添加翻页组件
		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}
	
	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("telephones")) {
				CustomerResource customerResource = (CustomerResource) itemId;
				Set<Telephone> telephones = customerResource.getTelephones();
				return new TelephoneNumberInTableLabelStyle(telephones, isEncryptMobile);
			} 
			return null;
		}
	}

	/**
	 * 由buttonClick调用执行简单搜索，更新翻页组件到第一页
	 */
	private void executeSearch() {
		// 关键字过滤
		String keyWordStr = "";
		if(keyWord.getValue()!=null){
			keyWordStr=keyWord.getValue().toString().trim();
		}
		this.initNewData();
		if(keyWordStr.equals("")){
			sqlSelect="SELECT r.id, r.name, r.sex, r.birthday FROM ec2_customer_resource r " +
					"where r.id in(select customerresources_id " +
					"from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") " +
					"order by r.id desc";
	
			sqlCount="SELECT count(*) FROM ec2_customer_resource r " +
					"where r.id in(select customerresources_id " +
					"from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") ";
		}else{
			sqlSelect="SELECT r.id, r.name, r.sex, r.birthday FROM ec2_customer_resource r " +
					"where r.name like '%"+keyWordStr+"%' and r.id in(select customerresources_id " +
					"from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") " +
					"order by r.id desc";

			sqlCount="SELECT count(*) FROM ec2_customer_resource r " +
					"where r.name like '%"+keyWordStr+"%' and r.id in(select customerresources_id " +
					"from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") ";

		}
		
		this.updateTable(true);
		if(table!=null){
			table.setValue(null);
		}
	}
	
	/**
	 * 由executeSearch调用，重新初始化重要数据，此处为Batch
	 */
	private void initNewData() {
		batch=(CustomerResourceBatch)resourceImport.getTable().getValue();
		//设置Window的Caption
		this.setCaption("批次 "+batch.getBatchName()+" -- 详细信息");
	}

	/**
	 * attach 方法，每次弹出新窗口，都应该显示相应批次的信息
	 */
	@Override
	public void attach() {
		super.attach();
		this.executeSearch();
	}
	
	/**
	 * 由executeSearch调用更新表格内容
	 * @param isToFirst 是否更新到第一页，default 是 false
	 */
	public void updateTable(Boolean isToFirst) {
		if(flip!=null){
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if(isToFirst){
				flip.refreshToFirstPage();
			}else{
				flip.refreshInCurrentPage();
			}
		}
	}
	/**
	 * 取得Table
	 * @return
	 */
	public Table getTable() {
		return table;
	}
	/**
	 * 取得Batch
	 * @return
	 */
	public CustomerResourceBatch getBatch() {
		return batch;
	}
	/**
	 * 由构造器调用， 设置界面按钮显示
	 */
	public void setButtonsStyle(String style) {
		style="small";
		search.setStyleName(style);
	}
	
	/**
	 * 是否确认删除资源
	 * @param isConfirm
	 */
	public void confirmDelete(Boolean isConfirm){
		CustomerResource customerResource=(CustomerResource)table.getValue();
		if(isConfirm){
			customerResourceService.deleteById(customerResource.getId());
			updateTable(false);
			table.setValue(null);
		}
	}
	
	/**
	 * 监听窗口关闭事件
	 * <p>窗口关闭时更新资源条数</p>
	 */
	@Override
	public void windowClose(CloseEvent e) {
		resourceImport.updateInfoAndStatus();
	}
	
	/**
	 * 监听搜索、添加、编辑、删除 的事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==search){
			executeSearch();
		}
	}
	
}
