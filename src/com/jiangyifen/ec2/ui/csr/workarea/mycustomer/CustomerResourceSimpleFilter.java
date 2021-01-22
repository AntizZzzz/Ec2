package com.jiangyifen.ec2.ui.csr.workarea.mycustomer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 客户资源搜索组件
 * 	供 ProprietaryCustomersTabView 和 MyResourcesTabView 共同使用
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomerResourceSimpleFilter extends VerticalLayout implements ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private ComboBox dialDateScope;			// “联系时间”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
	
	private TextField customerPhone;		// 客户手机输入文本框
	private TextField customerName;			// 客户姓名输入文本框
	private TextField companyName;			// 公司名称输入文本框

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	private PopupView complexSearchView;	// 复杂搜索界面
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 				// 当前的登陆用户

	private FlipOverTableComponent<CustomerResource> resourceTableFlip;				// 我的资源Tab 页的翻页组件
	private FlipOverTableComponent<CustomerResource> proprietaryCustomersTableFlip;	// 我的客户Tab 页的翻页组件
	private CustomerResourceComplexFilter customerResourceComplexFilter;			// 高级搜索
	
	public CustomerResourceSimpleFilter() {
		this.setSpacing(true);
		loginUser = SpringContextHolder.getLoginUser();
		
		gridLayout = new GridLayout(5, 2);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);

		//--------- 第一行  -----------//
		this.createTimeScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		
		//--------- 第二行  -----------//
		this.createCustomerPhoneHLayout();
		this.createCustomerNameHLayout();
		this.createCompanyNameHLayout();

		//------- 创建操作组件  --------//
		this.createOperateUI();
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createTimeScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
		Label timeScopeLabel = new Label("联系时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		dialDateScope = new ComboBox();
		dialDateScope.setImmediate(true);
		dialDateScope.addItem("今天");
		dialDateScope.addItem("昨天");
		dialDateScope.addItem("本周");
		dialDateScope.addItem("上周");
		dialDateScope.addItem("本月");
		dialDateScope.addItem("上月");
		dialDateScope.addItem("精确时间");
		dialDateScope.setValue("今天");
		dialDateScope.setWidth("110px");
		dialDateScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(dialDateScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)dialDateScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startTime.removeListener(startTimeListener);
				finishTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startTime.setValue(dates[0]);
				finishTime.setValue(dates[1]);
				startTime.addListener(startTimeListener);
				finishTime.addListener(finishTimeListener);
			}
		};
		dialDateScope.addListener(timeScopeListener);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 1, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dialDateScope.removeListener(timeScopeListener);
				dialDateScope.setValue("精确时间");
				dialDateScope.addListener(timeScopeListener);
			}
		};
		
		startTime = new PopupDateField();
		startTime.setWidth("153px");
		startTime.setImmediate(true);
		startTime.setValue(dates[0]);
		startTime.addListener(startTimeListener);
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setParseErrorMessage("时间格式不合法");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTimeHLayout.addComponent(startTime);
	}

	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 2, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dialDateScope.removeListener(finishTimeListener);
				dialDateScope.setValue("精确时间");
				dialDateScope.addListener(timeScopeListener);
			}
		};
		
		finishTime = new PopupDateField();
		finishTime.setImmediate(true);
		finishTime.setWidth("154px");
		finishTime.setValue(dates[1]);
		finishTime.addListener(finishTimeListener);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setParseErrorMessage("时间格式不合法");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTimeHLayout.addComponent(finishTime);
	}

	/**
	 * 创建 存放“电话号码” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 0, 1);
		
		Label telephoneLabel = new Label("电话号码：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone = new TextField();
		customerPhone.setWidth("110px");
		telephoneHLayout.addComponent(customerPhone);
	}
	
	/**
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 1, 1);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName = new TextField();
		customerName.setWidth("153px");	
		customerNameHLayout.addComponent(customerName);
	}

	/**
	 * 创建 存放“公司名称” 的布局管理器
	 */
	private void createCompanyNameHLayout() {
		HorizontalLayout companyHLayout = new HorizontalLayout();
		companyHLayout.setSpacing(true);
		gridLayout.addComponent(companyHLayout, 2, 1);
		
		Label campanyLabel = new Label("公司名称：");
		campanyLabel.setWidth("-1px");
		companyHLayout.addComponent(campanyLabel);
		
		companyName = new TextField();
		companyName.setWidth("154px");
		companyHLayout.addComponent(companyName);
	}

	/**
	 * @Description 描述：创建操作组件
	 *
	 * @author  JRH
	 * @date    2014年7月9日 下午3:01:09 void
	 */
	private void createOperateUI() {
		HorizontalLayout operateBt_hlo = new HorizontalLayout();
		operateBt_hlo.setSpacing(true);
		gridLayout.addComponent(operateBt_hlo, 3, 0);
		
		searchButton = new Button("查 询");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		operateBt_hlo.addComponent(searchButton);
		
		clearButton = new Button("清 空");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		operateBt_hlo.addComponent(clearButton);
		
		customerResourceComplexFilter = new CustomerResourceComplexFilter();
		customerResourceComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(customerResourceComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 3, 1);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			// 处理搜索事件
			handleSearchEvent();
		} else if(source == clearButton) {
			dialDateScope.select("精确时间");
			startTime.setValue(null);
			finishTime.setValue(null);
			customerName.setValue("");
			customerPhone.setValue("");
			companyName.setValue("");
		}
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		// 如果是我的资源界面，则刷新我的资源界面的信息，否则刷新我的客户界面的信息，通过翻页组件进行搜索
		if(resourceTableFlip != null) {
			String countSql = "select count(c) from CustomerResource as c where c.owner.id = " + loginUser.getId() + createFixedSql();
			String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.count asc, c.lastDialDate desc";
			resourceTableFlip.setSearchSql(searchSql);
			resourceTableFlip.setCountSql(countSql);
			resourceTableFlip.refreshToFirstPage();
			logger.info(searchSql);
		} else if(proprietaryCustomersTableFlip != null){
			String countSql = "select count(c) from CustomerResource as c where c.accountManager.id = " +loginUser.getId() + createFixedSql();
			String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.lastDialDate desc";
			proprietaryCustomersTableFlip.setSearchSql(searchSql);
			proprietaryCustomersTableFlip.setCountSql(countSql);
			proprietaryCustomersTableFlip.refreshToFirstPage();
			logger.info(searchSql);
		}
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createFixedSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 客户姓名查询
		String customerNameSql = "";
		String inputCustomerName = customerName.getValue().toString().trim();
		if(!"".equals(inputCustomerName)) {
			customerNameSql = " and c.name like '%" + inputCustomerName + "%'";
		}
		
		// 客户公司查询
		String companyNameSql = "";
		String inputCompanyName = companyName.getValue().toString().trim();
		if(!"".equals(inputCompanyName)) {
			companyNameSql = " and c.company.name like '%" + inputCompanyName + "%'";
		}
		
		// 客户联系方式查询
		String customerPhoneSql = "";
		String phoneNumber = customerPhone.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and c in (select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}
		
		// 最近联系客户的时间
		String specificStartDialTimeSql = "";
		if(startTime.getValue() != null) {
			specificStartDialTimeSql = " and c.lastDialDate >= '" + dateFormat.format(startTime.getValue()) +"'";
		} 
		
		String specificFinishDialTimeSql = "";
		if(finishTime.getValue() != null) {
			specificFinishDialTimeSql = " and c.lastDialDate <= '" + dateFormat.format(finishTime.getValue()) +"'";
		}
		
		// 创建固定的搜索语句
		return customerNameSql + companyNameSql + customerPhoneSql + specificStartDialTimeSql + specificFinishDialTimeSql;
	}

	public void setResourceTableFlip(FlipOverTableComponent<CustomerResource> resourceTableFlip) {
		this.resourceTableFlip = resourceTableFlip;
		customerResourceComplexFilter.setResourceTableFlip(resourceTableFlip);
	}

	public void setProprietaryCustomersTableFlip(
			FlipOverTableComponent<CustomerResource> proprietaryCustomersTableFlip) {
		this.proprietaryCustomersTableFlip = proprietaryCustomersTableFlip;
		customerResourceComplexFilter.setProprietaryCustomersTableFlip(proprietaryCustomersTableFlip);
	}
	
}
