package com.jiangyifen.ec2.ui.mgr.tabsheet.misscalllog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MissCallLog;
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
public class MissCallLogSimpleFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private TextField srcNum_tf;				// 主叫号码输入文本框
	private NativeSelect ringingTimeScope;	// “振铃时间”选择框
	private PopupDateField ringingStartTime;// “开始时间”选择框
	private PopupDateField ringingStopTime;	// “截止时间”选择框

	private TextField destNum_tf;			// 被叫分机输入文本框
	private ComboBox destUser_cb;			// 被叫坐席
	private ComboBox srcUser_cb;			// 主叫坐席

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

	private FlipOverTableComponent<MissCallLog> missCallLogTableFlip;		// 电话漏接记录详情Tab 页的翻页组件
	
	public MissCallLogSimpleFilter() {
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
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		//--------- 第一行  -----------//
		this.createCustomerNameHLayout();
		this.createMigrateDateScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		
		//--------- 第二行  -----------//
		this.createCustomerPhoneHLayout();
		this.createOriginatorHLayout();
		this.createJoinMemberHLayout();
		
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
	 * 创建 存放“主叫号码” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 0, 0);
		
		// 主叫号码输入区
		Label customerNameLabel = new Label("主叫号码：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		srcNum_tf = new TextField();
		srcNum_tf.setWidth("120px");
		srcNum_tf.setImmediate(true);
		srcNum_tf.setNullRepresentation("");
		customerNameHLayout.addComponent(srcNum_tf);
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createMigrateDateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 1, 0);
		
		Label timeScopeLabel = new Label("振铃时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		ringingTimeScope = new NativeSelect();
		ringingTimeScope.setImmediate(true);
		ringingTimeScope.addItem("今天");
		ringingTimeScope.addItem("昨天");
		ringingTimeScope.addItem("本周");
		ringingTimeScope.addItem("上周");
		ringingTimeScope.addItem("本月");
		ringingTimeScope.addItem("上月");
		ringingTimeScope.addItem("精确时间");
		ringingTimeScope.setValue("今天");
		ringingTimeScope.setWidth("133px");
		ringingTimeScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(ringingTimeScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)ringingTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				ringingStartTime.removeListener(startTimeListener);
				ringingStopTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				ringingStartTime.setValue(dates[0]);
				ringingStopTime.setValue(dates[1]);
				ringingStartTime.addListener(startTimeListener);
				ringingStopTime.addListener(finishTimeListener);
			}
		};
		ringingTimeScope.addListener(timeScopeListener);
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
				ringingTimeScope.removeListener(timeScopeListener);
				ringingTimeScope.setValue("精确时间");
				ringingTimeScope.addListener(timeScopeListener);
			}
		};
		
		ringingStartTime = new PopupDateField();
		ringingStartTime.setWidth("153px");
		ringingStartTime.setImmediate(true);
		ringingStartTime.setValue(dates[0]);
		ringingStartTime.addListener(startTimeListener);
		ringingStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		ringingStartTime.setParseErrorMessage("时间格式不合法");
		ringingStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		ringingStartTime.setValidationVisible(false);
		startTimeHLayout.addComponent(ringingStartTime);
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
				ringingTimeScope.removeListener(finishTimeListener);
				ringingTimeScope.setValue("精确时间");
				ringingTimeScope.addListener(timeScopeListener);
			}
		};
		
		ringingStopTime = new PopupDateField();
		ringingStopTime.setImmediate(true);
		ringingStopTime.setWidth("153px");
		ringingStopTime.setValue(dates[1]);
		ringingStopTime.addListener(finishTimeListener);
		ringingStopTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		ringingStopTime.setParseErrorMessage("时间格式不合法");
		ringingStopTime.setResolution(PopupDateField.RESOLUTION_SEC);
		ringingStopTime.setValidationVisible(false);
		finishTimeHLayout.addComponent(ringingStopTime);
	}

	/**
	 * 创建 存放“被叫分机” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout memberNumHLayout = new HorizontalLayout();
		memberNumHLayout.setSpacing(true);
		gridLayout.addComponent(memberNumHLayout, 0, 1);
		
		Label memberNumLabel = new Label("被叫分机：");
		memberNumLabel.setWidth("-1px");
		memberNumHLayout.addComponent(memberNumLabel);
		
		destNum_tf = new TextField();
		destNum_tf.setWidth("120px");
		destNum_tf.setNullRepresentation("");
		memberNumHLayout.addComponent(destNum_tf);
	}

	/**
	 * 创建 存放“被叫坐席：” 的布局管理器
	 */
	private void createOriginatorHLayout() {
		HorizontalLayout originatorHLayout = new HorizontalLayout();
		originatorHLayout.setSpacing(true);
		gridLayout.addComponent(originatorHLayout, 1, 1);
		
		Label originatorLabel = new Label("被叫坐席：");
		originatorLabel.setWidth("-1px");
		originatorHLayout.addComponent(originatorLabel);
		
		destUser_cb = new ComboBox();
		destUser_cb.setWidth("135px");
		destUser_cb.setImmediate(true);
		destUser_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		destUser_cb.setContainerDataSource(userContianer);
		destUser_cb.setItemCaptionPropertyId("migrateCsr");
		originatorHLayout.addComponent(destUser_cb);
	}
	
	/**
	 * 创建 存放“主叫坐席” 的布局管理器
	 */
	private void createJoinMemberHLayout() {
		HorizontalLayout joinMemberHLayout = new HorizontalLayout();
		joinMemberHLayout.setSpacing(true);
		gridLayout.addComponent(joinMemberHLayout, 2, 1);
		
		Label joinMemberLabel = new Label("主叫坐席：");
		joinMemberLabel.setWidth("-1px");
		joinMemberHLayout.addComponent(joinMemberLabel);
		
		srcUser_cb = new ComboBox();
		srcUser_cb.setWidth("128px");
		srcUser_cb.setImmediate(true);
		srcUser_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		srcUser_cb.setContainerDataSource(userContianer);
		srcUser_cb.setItemCaptionPropertyId("migrateCsr");
		joinMemberHLayout.addComponent(srcUser_cb);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(!ringingStartTime.isValid() || !ringingStopTime.isValid()) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间或截止时间格式不正确！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(ringingStartTime.getValue() == null || ringingStopTime.getValue() == null) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
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
		ringingTimeScope.select("今天");
		srcNum_tf.setValue("");
		destNum_tf.setValue("");
		srcUser_cb.setValue(null);
		destUser_cb.setValue(null);
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(m) from MissCallLog as m where " +createDynamicSql();
		String searchSql = countSql.replaceFirst("count\\(m\\)", "m")+" order by m.ringingStateTime desc";
		missCallLogTableFlip.setSearchSql(searchSql);
		missCallLogTableFlip.setCountSql(countSql);
		missCallLogTableFlip.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		// 振铃时间查询
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String joinDateScopeSql = "m.ringingStateTime >= '" + dateFormat.format(ringingStartTime.getValue()) +"' and m.ringingStateTime <= '" + dateFormat.format(ringingStopTime.getValue()) +"'";

		// 主叫号码查询
		String srcNumSql = "";
		String inputSrcNum = StringUtils.trimToEmpty((String) srcNum_tf.getValue());
		if(!"".equals(inputSrcNum)) {
			srcNumSql = " and m.srcNum like '%" + inputSrcNum + "%'";
		}
		
		// 与会成员号码查询
		String destNumSql = "";
		String inputDestNum = StringUtils.trimToEmpty((String) destNum_tf.getValue());
		if(!"".equals(inputDestNum)) {
			destNumSql = " and m.destNum like '%" + inputDestNum + "%'";
		}
		
		// 被叫坐席查询
		String destUserSql = "";
		User destUser = (User) destUser_cb.getValue();
		if(destUser != null) {
			destUserSql = " and m.destUserId = "+destUser.getId();
		}
		
		// 主叫坐席查询
		String srcUserSql = "";
		User srcUser = (User) srcUser_cb.getValue();
		if(srcUser != null) {
			srcUserSql = " and m.srcUserId = "+srcUser.getId();
		}
		
		// 只能查看管理员所管辖部门对应的数据
		String destUserDeptIdSql = " and m.destUserDeptId in ("+StringUtils.join(allGovernedDeptIds, ",")+")";
		
		// 创建固定的搜索语句
		return joinDateScopeSql + destUserSql + srcUserSql + " and m.domainId = " +domain.getId() + destUserDeptIdSql + srcNumSql + destNumSql;
	}

	public void setMeettingRecordTableFlip(FlipOverTableComponent<MissCallLog> missCallLogTableFlip) {
		this.missCallLogTableFlip = missCallLogTableFlip;
	}
	
}
