package com.jiangyifen.ec2.ui.mgr.tabsheet.csrtimers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @Description 描述：电话漏接记录详情 简单过滤器
 * 
 * @author  jrh
 * @date    2013年12月27日 下午2:55:39
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class CsrTimersOrderSimpleFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private TextField customerId_tf;		// 客户编号输入文本框
	private NativeSelect respTimeScope;	// “响应时间”选择框
	private PopupDateField respStartTime;// “开始时间”选择框
	private PopupDateField respStopTime;	// “截止时间”选择框

	private TextField customerPhone_tf;		// 客户电话输入文本框
	private TextField dialCount_tf;			// 回访次数输入文本框
	private ComboBox csrCreator_cb;			// 创建坐席
	private ComboBox forbid_cb;				// 是否禁止

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 				// 当前的登陆用户
	private Domain domain;					// 当前用户所属域
	private List<Long> allGovernedDeptIds;	// 当前用户所有管辖部门的id号
	private List<Department> allGovernedDept;		//当前用户所有管辖部门
	private BeanItemContainer<User> userContianer;
	private BeanItemContainer<SipConfig> extenContianer;
	
	private UserService userService;
	private SipConfigService sipConfigService;
	private DepartmentService departmentService;

	private FlipOverTableComponent<Timers> csrTimersOrderTableFlip;		// 电话漏接记录详情Tab 页的翻页组件
	
	public CsrTimersOrderSimpleFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		userService = SpringContextHolder.getBean("userService");
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
		userContianer = new BeanItemContainer<User>(User.class);
		userContianer.addAll(userService.getAllByDomain(domain));
		
		extenContianer = new BeanItemContainer<SipConfig>(SipConfig.class);
		extenContianer.addAll(sipConfigService.getAllExtsByDomain(domain));

		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		allGovernedDeptIds = new ArrayList<Long>();
		allGovernedDept = new ArrayList<Department>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
							allGovernedDept.add(dept);
						}
					}
				}
			}
		}
		
		gridLayout = new GridLayout(5, 2);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		//--------- 第一行  -----------//
		this.createCustomerIdHLayout();
		this.createResponseTimeScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		
		//--------- 第二行  -----------//
		this.createCustomerPhoneHLayout();
		this.creatDialCountHLayout();
		this.createCsrCreatorHLayout();
		this.createForbidHLayout();
		
		//--------- 创建操作按钮 -----------//
		this.createOperateButtons();
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		searchButton = new Button("查 询");
		searchButton.setDescription("快捷键(Ctrl+Q)");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		searchButton.setClickShortcut(KeyCode.Q, ModifierKey.CTRL);
		gridLayout.addComponent(searchButton, 4, 0);
		
		clearButton = new Button("清 空");
		clearButton.setDescription("快捷键(Ctrl+L)");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		clearButton.setClickShortcut(KeyCode.L, ModifierKey.CTRL);
		gridLayout.addComponent(clearButton, 4, 1);
	}
	
	/**
	 * 创建 存放“客户编号” 的布局管理器
	 */
	private void createCustomerIdHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 0, 0);
		
		// 客户编号输入区
		Label customerNameLabel = new Label("客户编号：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerId_tf = new TextField();
		customerId_tf.setWidth("120px");
		customerId_tf.setValidationVisible(false);
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户编号只能由数字组成"));
		customerId_tf.setImmediate(true);
		customerId_tf.setNullRepresentation("");
		customerNameHLayout.addComponent(customerId_tf);
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createResponseTimeScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 1, 0);
		
		Label timeScopeLabel = new Label("响应时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		respTimeScope = new NativeSelect();
		respTimeScope.setImmediate(true);
		respTimeScope.addItem("今天");
		respTimeScope.addItem("昨天");
		respTimeScope.addItem("本周");
		respTimeScope.addItem("上周");
		respTimeScope.addItem("本月");
		respTimeScope.addItem("上月");
		respTimeScope.addItem("精确时间");
		respTimeScope.setValue("今天");
		respTimeScope.setWidth("120px");
		respTimeScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(respTimeScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)respTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				respStartTime.removeListener(startTimeListener);
				respStopTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				respStartTime.setValue(dates[0]);
				respStopTime.setValue(dates[1]);
				respStartTime.addListener(startTimeListener);
				respStopTime.addListener(finishTimeListener);
			}
		};
		respTimeScope.addListener(timeScopeListener);
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
				respTimeScope.removeListener(timeScopeListener);
				respTimeScope.setValue("精确时间");
				respTimeScope.addListener(timeScopeListener);
			}
		};
		
		respStartTime = new PopupDateField();
		respStartTime.setWidth("153px");
		respStartTime.setImmediate(true);
		respStartTime.setValue(dates[0]);
		respStartTime.addListener(startTimeListener);
		respStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		respStartTime.setParseErrorMessage("时间格式不合法");
		respStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		respStartTime.setValidationVisible(false);
		startTimeHLayout.addComponent(respStartTime);
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
				respTimeScope.removeListener(finishTimeListener);
				respTimeScope.setValue("精确时间");
				respTimeScope.addListener(timeScopeListener);
			}
		};
		
		respStopTime = new PopupDateField();
		respStopTime.setImmediate(true);
		respStopTime.setWidth("153px");
		respStopTime.setValue(dates[1]);
		respStopTime.addListener(finishTimeListener);
		respStopTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		respStopTime.setParseErrorMessage("时间格式不合法");
		respStopTime.setResolution(PopupDateField.RESOLUTION_SEC);
		respStopTime.setValidationVisible(false);
		finishTimeHLayout.addComponent(respStopTime);
	}

	/**
	 * 创建 存放“客户电话” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout customerPhoneHLayout = new HorizontalLayout();
		customerPhoneHLayout.setSpacing(true);
		gridLayout.addComponent(customerPhoneHLayout, 0, 1);
		
		Label customerPhoneLabel = new Label("客户电话：");
		customerPhoneLabel.setWidth("-1px");
		customerPhoneHLayout.addComponent(customerPhoneLabel);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("120px");
		customerPhone_tf.setValidationVisible(false);
		customerPhone_tf.addValidator(new RegexpValidator("\\d+", "客户电话只能由数字组成"));
		customerPhone_tf.setNullRepresentation("");
		customerPhoneHLayout.addComponent(customerPhone_tf);
	}
	
	/**
	 * 创建 存放“回访次数” 的布局管理器
	 */
	private void creatDialCountHLayout() {
		HorizontalLayout dialCountHLayout = new HorizontalLayout();
		dialCountHLayout.setSpacing(true);
		gridLayout.addComponent(dialCountHLayout, 1, 1);
		
		Label dialCountLabel = new Label("回访次数：");
		dialCountLabel.setWidth("-1px");
		dialCountHLayout.addComponent(dialCountLabel);
		
		dialCount_tf = new TextField();
		dialCount_tf.setWidth("120px");
		dialCount_tf.setValidationVisible(false);
		dialCount_tf.addValidator(new RegexpValidator("\\d+", "回访次数只能由数字组成"));
		dialCount_tf.setNullRepresentation("");
		dialCountHLayout.addComponent(dialCount_tf);
	}

	/**
	 * 创建 存放“创建坐席：” 的布局管理器
	 */
	private void createCsrCreatorHLayout() {
		HorizontalLayout csrCreatorHLayout = new HorizontalLayout();
		csrCreatorHLayout.setSpacing(true);
		gridLayout.addComponent(csrCreatorHLayout, 2, 1);
		
		Label csrCreatorLabel = new Label("创建坐席：");
		csrCreatorLabel.setWidth("-1px");
		csrCreatorHLayout.addComponent(csrCreatorLabel);
		
		csrCreator_cb = new ComboBox();
		csrCreator_cb.setWidth("153px");
		csrCreator_cb.setImmediate(true);
		csrCreator_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		csrCreator_cb.setContainerDataSource(userContianer);
		csrCreator_cb.setItemCaptionPropertyId("migrateCsr");
		csrCreatorHLayout.addComponent(csrCreator_cb);
	}
	
	/**
	 * 创建 存放“是否禁止” 的布局管理器
	 */
	private void createForbidHLayout() {
		HorizontalLayout forbidHLayout = new HorizontalLayout();
		forbidHLayout.setSpacing(true);
		gridLayout.addComponent(forbidHLayout, 3, 1);
		
		Label forbidLabel = new Label("是否禁止：");
		forbidLabel.setWidth("-1px");
		forbidHLayout.addComponent(forbidLabel);
		
		forbid_cb = new ComboBox();
		forbid_cb.setWidth("153px");
		forbid_cb.setImmediate(true);
		forbid_cb.addItem("all");
		forbid_cb.addItem("isforbid");
		forbid_cb.addItem("noforbid");
		forbid_cb.setItemCaption("all", "全部");
		forbid_cb.setItemCaption("isforbid", "是");
		forbid_cb.setItemCaption("noforbid", "否");
		forbid_cb.setValue("all");
		forbid_cb.setNullSelectionAllowed(false);
		forbid_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		forbidHLayout.addComponent(forbid_cb);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(!respStartTime.isValid() || !respStopTime.isValid()) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间或截止时间格式不正确！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(respStartTime.getValue() == null || respStopTime.getValue() == null) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!customerId_tf.isValid()) {
				customerId_tf.getApplication().getMainWindow().showNotification("客户编号 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!customerPhone_tf.isValid()) {
				customerPhone_tf.getApplication().getMainWindow().showNotification("客户电话 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!dialCount_tf.isValid()) {
				dialCount_tf.getApplication().getMainWindow().showNotification("回访次数 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			// 处理搜索事件
			handleSearchEvent();
		} else if(source == clearButton) {
			excuteClearValues();
		}
	}
	
	/**
	 * 执行清空操作
	 */
	private void excuteClearValues() {
		customerId_tf.setValue("");
		respTimeScope.select("今天");
		dialCount_tf.setValue("");
		customerPhone_tf.setValue("");
		csrCreator_cb.setValue(null);
		forbid_cb.setValue("all");
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(t) from Timers as t where " +createDynamicSql();
		String searchSql = countSql.replaceFirst("count\\(t\\)", "t")+" order by t.creator.id asc, t.responseTime desc";
		csrTimersOrderTableFlip.setSearchSql(searchSql);
		csrTimersOrderTableFlip.setCountSql(countSql);
		csrTimersOrderTableFlip.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		// 响应时间查询
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String respScopeSql = " t.responseTime >= '" + dateFormat.format(respStartTime.getValue()) +"' and t.responseTime <= '" + dateFormat.format(respStopTime.getValue()) +"'";

		// 客户编号查询
		String customerIdSql = "";
		String inputCustomerId = (String) customerId_tf.getValue();
		if(!"".equals(inputCustomerId)) {
			customerIdSql = " and t.customerId = " + inputCustomerId;
		}
		
		// 客户号码查询
		String customerPhoneSql = "";
		String inputCustomerPhone = (String) customerPhone_tf.getValue();
		if(!"".equals(inputCustomerPhone)) {
			customerPhoneSql = " and t.customerPhoneNum like '%" + inputCustomerPhone + "%'";
		}
		
		// 回访次数查询
		String dialCountSql = "";
		String inputDialCount = (String) dialCount_tf.getValue();
		if(!"".equals(inputDialCount)) {
			dialCountSql = " and t.dialCount = " + inputDialCount;
		}
		
		// 创建坐席查询
		String csrCreatorSql = "";
		User csrCreator = (User) csrCreator_cb.getValue();
		if(csrCreator != null) {
			csrCreatorSql = " and t.creator.id = "+csrCreator.getId();
		}
		
		// 是否禁止查询
		String forbidSql = "";
		String inputForbid = (String) forbid_cb.getValue();
		if("isforbid".equals(inputForbid)) {
			forbidSql = " and t.isCsrForbidPop = true";
		} else if("noforbid".equals(inputForbid)) {
			forbidSql = " and t.isCsrForbidPop = false";
		}
		
		// 只能查看管理员所管辖部门对应的数据
		String destUserDeptIdSql = " and t.creator.department.id in ("+StringUtils.join(allGovernedDeptIds, ",")+")";
		
		// 创建固定的搜索语句
		return respScopeSql + csrCreatorSql + customerIdSql + " and t.domain.id = " +domain.getId() +" and t.customerId is not null " + dialCountSql + destUserDeptIdSql + forbidSql + customerPhoneSql;
	}

	public void setCsrTimersOrderTableFlip(FlipOverTableComponent<Timers> csrTimersOrderTableFlip) {
		this.csrTimersOrderTableFlip = csrTimersOrderTableFlip;
	}
	
}
