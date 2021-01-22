package com.jiangyifen.ec2.ui.csr.workarea.mycustomer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客户资源搜索组件
 * 	供 ProprietaryCustomersTabView 和 MyResourcesTabView 共同使用
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomerResourceComplexFilter extends VerticalLayout implements ClickListener, Content {

	private Notification warning_notification;	// 错误警告提示信息

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private TextField customerName;			// 客户姓名输入文本框
	private ComboBox dialDateScope;			// “联系时间”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
	
	private TextField companyName;			// 公司名称输入文本框
	private TextField expiredDateMoreThan;	// “过期时间搜索起始时间”输入框
	private ComboBox andOrToExpiredDate;	// 关联词选择框，and 或 or
	private TextField expiredDateLessThan;	// “过期时间搜索结束时间”输入框

	private TextField customerPhone;		// 客户手机输入文本框
	private TextField dialCountMoreThan;	// “联系次数>=”输入框
	private ComboBox andOrToDialCount;		// 关联词选择框，and 或 or
	private TextField dialCountLessThan;	// “联系次数<=”输入框
	
	private ComboBox customerLevelSelector; // “客户等级”选择
	private TextField customerId_tf;		// 客户编号输入文本框
	private ComboBox importScope;			// “导入时间”选择框

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 				// 当前的登陆用户
	private Domain domain;					// 当前用户所属域

	private FlipOverTableComponent<CustomerResource> resourceTableFlip;				// 我的资源Tab 页的翻页组件
	private FlipOverTableComponent<CustomerResource> proprietaryCustomersTableFlip;	// 我的客户Tab 页的翻页组件
	
	private CustomerLevelService customerLevelService;	// 客户级别服务类
	
	public CustomerResourceComplexFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		customerLevelService = SpringContextHolder.getBean("customerLevelService");

		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);
		
		gridLayout = new GridLayout(5, 4);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);

		//--------- 第一行  -----------//
		this.createCustomerNameHLayout();
		this.createDialDateScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		
		//--------- 第二行  -----------//
		this.createCompanyNameHLayout();
		this.createExpiredDateMoreThanHLayout();
		this.createAndOrToExpiredDateHLayout();
		this.createExpiredDateLessThanHLayout();

		//--------- 第三行  -----------//
		this.createCustomerPhoneHLayout();
		this.createCountMoreThanHLayout();
		this.createAndOrToCountHLayout();
		this.createCountLessThanHLayout();

		//--------- 第四行  -----------//
		this.createCustomerLevelHLayout();
		this.createCustomerIdHLayout();
		this.createImportDateHLayout();
		
		searchButton = new Button("查 询");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		gridLayout.addComponent(searchButton, 4, 0);
		
		clearButton = new Button("清 空");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		gridLayout.addComponent(clearButton, 4, 1);
	}
	
	/**
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 0, 0);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName = new TextField();
		customerName.setWidth("138px");	
		customerNameHLayout.addComponent(customerName);
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createDialDateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 1, 0);
		
		Label timeScopeLabel = new Label("联 系 时 间 ：");
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
		dialDateScope.setWidth("100px");
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
		gridLayout.addComponent(startTimeHLayout, 2, 0);
				
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
		gridLayout.addComponent(finishTimeHLayout, 3, 0);
		
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
	 * 创建 存放“公司名称” 的布局管理器
	 */
	private void createCompanyNameHLayout() {
		HorizontalLayout companyHLayout = new HorizontalLayout();
		companyHLayout.setSpacing(true);
		gridLayout.addComponent(companyHLayout, 0, 1);
		
		Label campanyLabel = new Label("公司名称：");
		campanyLabel.setWidth("-1px");
		companyHLayout.addComponent(campanyLabel);
		
		companyName = new TextField();
		companyName.setWidth("138px");
		companyHLayout.addComponent(companyName);
	}

	/**
	 * 创建  存放“联系次数>=标签和其选择框” 的布局管理器
	 */
	private void createExpiredDateMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 1, 1);
		
		Label expiredDateMoreThanLabel = new Label("有效时间>=：");
		expiredDateMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(expiredDateMoreThanLabel);
		
		expiredDateMoreThan = new TextField();
		expiredDateMoreThan.setWidth("100px");
		expiredDateMoreThan.setInputPrompt("天");
		expiredDateMoreThan.addValidator(new RegexpValidator("(-?\\d+)||\\d*", "有效时间只能为负数或正数！"));
		expiredDateMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(expiredDateMoreThan);
	}

	/**
	 * 创建  存放“搜索关联词标签和其选择框” 的布局管理器
	 */
	private void createAndOrToExpiredDateHLayout() {
		HorizontalLayout andOrToExpiredHLayout = new HorizontalLayout();
		andOrToExpiredHLayout.setSpacing(true);
		gridLayout.addComponent(andOrToExpiredHLayout, 2, 1);
		
		Label correlativeLabel = new Label("关 联 词 ：", Label.CONTENT_XHTML);
		correlativeLabel.setWidth("-1px");
		andOrToExpiredHLayout.addComponent(correlativeLabel);
		
		andOrToExpiredDate = new ComboBox();
		andOrToExpiredDate.addItem("AND");
		andOrToExpiredDate.addItem("OR");
		andOrToExpiredDate.setValue("AND");
		andOrToExpiredDate.setWidth("154px");
		andOrToExpiredDate.setNullSelectionAllowed(false);
		andOrToExpiredHLayout.addComponent(andOrToExpiredDate);
	}

	/**
	 * 创建  存放“联系次数<=标签和其选择框” 的布局管理器
	 */
	private void createExpiredDateLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 3, 1);
		
		Label expiredDateLessThanLabel = new Label("有效时间<=：");
		expiredDateLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(expiredDateLessThanLabel);
		
		expiredDateLessThan = new TextField();
		expiredDateLessThan.setWidth("113px");
		expiredDateLessThan.setInputPrompt("天");
		expiredDateLessThan.addValidator(new RegexpValidator("(-?\\d+)||\\d*", "有效时间只能为负数或正数！"));
		expiredDateLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(expiredDateLessThan);
	}

	/**
	 * 创建 存放“电话号码” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 0, 2);
		
		Label telephoneLabel = new Label("电话号码：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone = new TextField();
		customerPhone.setWidth("138px");
		telephoneHLayout.addComponent(customerPhone);
	}

	/**
	 * 创建  存放“联系次数>=标签和其选择框” 的布局管理器
	 */
	private void createCountMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 1, 2);
		
		Label talkTimeMoreThanLabel = new Label("联系次数>=：");
		talkTimeMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(talkTimeMoreThanLabel);
		
		dialCountMoreThan = new TextField();
		dialCountMoreThan.setWidth("100px");
		dialCountMoreThan.addValidator(new RegexpValidator("\\d+", "联系次数只能为数字组成！"));
		dialCountMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(dialCountMoreThan);
	}

	/**
	 * 创建  存放“搜索关联词标签和其选择框” 的布局管理器
	 */
	private void createAndOrToCountHLayout() {
		HorizontalLayout andOrHLayout = new HorizontalLayout();
		andOrHLayout.setSpacing(true);
		gridLayout.addComponent(andOrHLayout, 2, 2);
		
		Label correlativeLabel = new Label("关 联 词 ：", Label.CONTENT_XHTML);
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		andOrToDialCount = new ComboBox();
		andOrToDialCount.addItem("AND");
		andOrToDialCount.addItem("OR");
		andOrToDialCount.setValue("AND");
		andOrToDialCount.setWidth("154px");
		andOrToDialCount.setNullSelectionAllowed(false);
		andOrHLayout.addComponent(andOrToDialCount);
	}

	/**
	 * 创建  存放“联系次数<=标签和其选择框” 的布局管理器
	 */
	private void createCountLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 3, 2);
		
		Label talkTimeLessThanLabel = new Label("联系次数<=：");
		talkTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(talkTimeLessThanLabel);
		
		dialCountLessThan = new TextField();
		dialCountLessThan.setWidth("113px");
		dialCountLessThan.addValidator(new RegexpValidator("\\d+", "联系次数只能为数字组成！"));
		dialCountLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(dialCountLessThan);
	}
	
	/**
	 * 创建  存放“客户级别标签和其选择框” 的布局管理器
	 */
	private void createCustomerLevelHLayout() {
		HorizontalLayout levelHLayout = new HorizontalLayout();
		levelHLayout.setSpacing(true);
		gridLayout.addComponent(levelHLayout, 0, 3);
		
		Label levelLabel = new Label("客户级别：");
		levelLabel.setWidth("-1px");
		levelHLayout.addComponent(levelLabel);
		
		BeanItemContainer<CustomerLevel> levelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		levelContainer.addAll(customerLevelService.getAll(domain));
		
		customerLevelSelector = new ComboBox();
		customerLevelSelector.setImmediate(true);
		customerLevelSelector.setWidth("138px");
		customerLevelSelector.setItemCaptionPropertyId("levelName");
		customerLevelSelector.setContainerDataSource(levelContainer);
		customerLevelSelector.setNullSelectionAllowed(true);
		levelHLayout.addComponent(customerLevelSelector);
	}
	
	/**
	 * 创建  存放“客户编号” 的布局管理器
	 */
	private void createCustomerIdHLayout() {
		HorizontalLayout customerIdHLayout = new HorizontalLayout();
		customerIdHLayout.setSpacing(true);
		gridLayout.addComponent(customerIdHLayout, 1, 3);
		
		// 客户编号输入区
		Label customerIdLabel = new Label("客 户 编 号 ：");
		customerIdLabel.setWidth("-1px");
		customerIdHLayout.addComponent(customerIdLabel);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("100px");
		customerIdHLayout.addComponent(customerId_tf);
	}

	/**
	 * 创建  存放“联系时间标签和其选择框” 的布局管理器
	 */
	private void createImportDateHLayout() {
		HorizontalLayout dialDateScopeHLayout = new HorizontalLayout();
		dialDateScopeHLayout.setSpacing(true);
		gridLayout.addComponent(dialDateScopeHLayout, 2, 3);
		
		Label dialDateScopeLabel = new Label("导入时间：");
		dialDateScopeLabel.setWidth("-1px");
		dialDateScopeHLayout.addComponent(dialDateScopeLabel);
		
		importScope = new ComboBox();
		importScope.setImmediate(true);
		importScope.addItem("今天");
		importScope.addItem("昨天");
		importScope.addItem("本周");
		importScope.addItem("上周");
		importScope.addItem("本月");
		importScope.addItem("上月");
		importScope.setWidth("154px");
		importScope.setNullSelectionAllowed(true);
		dialDateScopeHLayout.addComponent(importScope);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(dialCountMoreThan.isValid() == false || dialCountLessThan.isValid() == false) {
				dialCountMoreThan.getApplication().getMainWindow().showNotification("联系次数搜索输入框中只能输入数字！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(expiredDateMoreThan.isValid() == false || expiredDateMoreThan.isValid() == false) {
				expiredDateMoreThan.getApplication().getMainWindow().showNotification("有效时间搜索输入框只能为负数或正数！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!customerId_tf.isValid()) {
				warning_notification.setCaption("客户编号只能由数字组成！");
				customerId_tf.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			
			// 处理搜索事件
			handleSearchEvent();
		} else if(source == clearButton) {
			dialDateScope.select("精确时间");
			startTime.setValue(null);
			finishTime.setValue(null);
			
			dialCountMoreThan.setValue("");
			andOrToDialCount.setValue("AND");
			dialCountLessThan.setValue("");
			
			expiredDateMoreThan.setValue("");
			andOrToExpiredDate.setValue("AND");
			expiredDateLessThan.setValue("");
			
			customerName.setValue("");
			customerPhone.setValue("");
			companyName.setValue("");
			
			customerLevelSelector.setValue(null);
			customerId_tf.setValue("");
			importScope.setValue(null);
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
		} else if(proprietaryCustomersTableFlip != null){
			String countSql = "select count(c) from CustomerResource as c where c.accountManager.id = " +loginUser.getId() + createFixedSql();
			String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.lastDialDate desc";
			proprietaryCustomersTableFlip.setSearchSql(searchSql);
			proprietaryCustomersTableFlip.setCountSql(countSql);
			proprietaryCustomersTableFlip.refreshToFirstPage();
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
		
		// 客户级别查询
		String customerLevelSql = "";
		CustomerLevel customerLevel = (CustomerLevel) customerLevelSelector.getValue();
		if(customerLevel != null) {
			customerLevelSql = " and c.customerLevel.id = " +customerLevel.getId();
		}
		
		// 客户编号查询
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and c.id = " + customerId;
		}
		
		// 资源的导入时间查询
		String importScopeSql = "";
		String dialDateScopeValue = (String) importScope.getValue();
		if(dialDateScopeValue != null) {
			String[] dates = ParseDateSearchScope.parseDateSearchScope(dialDateScopeValue);
			importScopeSql = " and c.importDate >= '" +dates[0]+ "' and c.importDate <= '" +dates[1]+ "'";
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

		// 客户被呼叫次数查询
		String dialCountSql = "";
		String inputDialCountMoreThan = dialCountMoreThan.getValue().toString().trim();
		String inputDialCountLessThan = dialCountLessThan.getValue().toString().trim();
		if(!"".equals(inputDialCountMoreThan) && !"".equals(inputDialCountLessThan)) {
			if(Integer.parseInt(inputDialCountLessThan) < Integer.parseInt(inputDialCountMoreThan)) {
				andOrToDialCount.setValue("OR");
				dialCountSql = " and (c.count >= " + inputDialCountMoreThan + " or c.count <= " +inputDialCountLessThan + ")";
			} else { 
				andOrToDialCount.setValue("AND");
				dialCountSql = " and (c.count >= " + inputDialCountMoreThan + " and c.count <= " +inputDialCountLessThan + ")";
			}
		} else if(!"".equals(inputDialCountMoreThan) && "".equals(inputDialCountLessThan)) {
			dialCountSql = " and c.count >= " + inputDialCountMoreThan;
		} else if("".equals(inputDialCountMoreThan) && !"".equals(inputDialCountLessThan)) {
			dialCountSql = " and c.count <= " + inputDialCountLessThan;
		} 
		
		// 客户的有效期查询
		String expiredDateSql = "";
		String inputExpiredMoreThan = expiredDateMoreThan.getValue().toString().trim();
		String inputExpiredLessThan = expiredDateLessThan.getValue().toString().trim();
		if(!"".equals(inputExpiredMoreThan) && !"".equals(inputExpiredLessThan)) {
			int morethan = Integer.parseInt(inputExpiredMoreThan);
			int lessthan = Integer.parseInt(inputExpiredLessThan);
			if(lessthan < morethan) {
				andOrToExpiredDate.setValue("OR");
				expiredDateSql = " and (c.expireDate >= '" + ParseDateSearchScope.getSpecifyDateStr(morethan) + "' or c.expireDate <= '" +ParseDateSearchScope.getSpecifyDateStr(lessthan) + "')";
			} else { 
				andOrToExpiredDate.setValue("AND");
				expiredDateSql = " and (c.expireDate >= '" + ParseDateSearchScope.getSpecifyDateStr(morethan) + "' and c.expireDate <= '" +ParseDateSearchScope.getSpecifyDateStr(lessthan) + "')";
			}
		} else if(!"".equals(inputExpiredMoreThan) && "".equals(inputExpiredLessThan)) {
			int morethan = Integer.parseInt(inputExpiredMoreThan);
			expiredDateSql = " and c.expireDate >= '" + ParseDateSearchScope.getSpecifyDateStr(morethan)+ "'";
		} else if("".equals(inputExpiredMoreThan) && !"".equals(inputExpiredLessThan)) {
			int lessthan = Integer.parseInt(inputExpiredLessThan);
			expiredDateSql = " and c.expireDate <= '" + ParseDateSearchScope.getSpecifyDateStr(lessthan)+ "'";
		} 
		
		// 创建固定的搜索语句
		return customerLevelSql + customerNameSql + companyNameSql + customerPhoneSql + dialCountSql 
				+ expiredDateSql + specificStartDialTimeSql + specificFinishDialTimeSql + customerIdSql + importScopeSql;
	}

	public void setResourceTableFlip(FlipOverTableComponent<CustomerResource> resourceTableFlip) {
		this.resourceTableFlip = resourceTableFlip;
	}

	public void setProprietaryCustomersTableFlip(
			FlipOverTableComponent<CustomerResource> proprietaryCustomersTableFlip) {
		this.proprietaryCustomersTableFlip = proprietaryCustomersTableFlip;
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return gridLayout;
	}
}
