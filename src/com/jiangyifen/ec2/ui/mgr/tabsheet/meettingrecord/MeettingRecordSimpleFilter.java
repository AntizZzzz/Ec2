package com.jiangyifen.ec2.ui.mgr.tabsheet.meettingrecord;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
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
 * @Description 描述：三方通话记录详情 简单过滤器
 * 
 * @author  jrh
 * @date    2013年12月25日 下午2:55:39
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class MeettingRecordSimpleFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private ComboBox meettingRoom_cb;		// 会议室号码输入文本框
	private NativeSelect migrateScope;		// “与会时间”选择框
	private PopupDateField joinStartTime;	// “开始时间”选择框
	private PopupDateField joinStopTime;	// “截止时间”选择框

	private TextField joinMemberNum_tf;		// 与会者号码输入文本框
	private ComboBox originators_cb;		// 发起人
	private ComboBox joinMembers_cb;			// 与会坐席

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private Domain domain;					// 当前用户所属域
	private BeanItemContainer<User> userContianer;
	private BeanItemContainer<SipConfig> extenContianer;
	
	private UserService userService;
	private SipConfigService sipConfigService;

	private FlipOverTableComponent<MeettingDetailRecord> meettingRecordTableFlip;		// 三方通话记录详情Tab 页的翻页组件
	
	public MeettingRecordSimpleFilter() {
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		
		userService = SpringContextHolder.getBean("userService");
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		
		userContianer = new BeanItemContainer<User>(User.class);
		userContianer.addAll(userService.getAllByDomain(domain));
		
		extenContianer = new BeanItemContainer<SipConfig>(SipConfig.class);
		extenContianer.addAll(sipConfigService.getAllExtsByDomain(domain));
		
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
	 * 创建 存放“会议室号码” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 0, 0);
		
		// 会议室号码输入区
		Label customerNameLabel = new Label("会议室号码：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		meettingRoom_cb = new ComboBox();
		meettingRoom_cb.setWidth("120px");
		meettingRoom_cb.setImmediate(true);
		meettingRoom_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		meettingRoom_cb.setItemCaptionPropertyId("name");
		meettingRoom_cb.setContainerDataSource(extenContianer);
		customerNameHLayout.addComponent(meettingRoom_cb);
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createMigrateDateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 1, 0);
		
		Label timeScopeLabel = new Label("与会时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		migrateScope = new NativeSelect();
		migrateScope.setImmediate(true);
		migrateScope.addItem("今天");
		migrateScope.addItem("昨天");
		migrateScope.addItem("本周");
		migrateScope.addItem("上周");
		migrateScope.addItem("本月");
		migrateScope.addItem("上月");
		migrateScope.addItem("精确时间");
		migrateScope.setValue("本周");
		migrateScope.setWidth("133px");
		migrateScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(migrateScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)migrateScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				joinStartTime.removeListener(startTimeListener);
				joinStopTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				joinStartTime.setValue(dates[0]);
				joinStopTime.setValue(dates[1]);
				joinStartTime.addListener(startTimeListener);
				joinStopTime.addListener(finishTimeListener);
			}
		};
		migrateScope.addListener(timeScopeListener);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本周");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 2, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				migrateScope.removeListener(timeScopeListener);
				migrateScope.setValue("精确时间");
				migrateScope.addListener(timeScopeListener);
			}
		};
		
		joinStartTime = new PopupDateField();
		joinStartTime.setWidth("153px");
		joinStartTime.setImmediate(true);
		joinStartTime.setValue(dates[0]);
		joinStartTime.addListener(startTimeListener);
		joinStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		joinStartTime.setParseErrorMessage("时间格式不合法");
		joinStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		joinStartTime.setValidationVisible(false);
		startTimeHLayout.addComponent(joinStartTime);
	}

	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本周");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 3, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				migrateScope.removeListener(finishTimeListener);
				migrateScope.setValue("精确时间");
				migrateScope.addListener(timeScopeListener);
			}
		};
		
		joinStopTime = new PopupDateField();
		joinStopTime.setImmediate(true);
		joinStopTime.setWidth("153px");
		joinStopTime.setValue(dates[1]);
		joinStopTime.addListener(finishTimeListener);
		joinStopTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		joinStopTime.setParseErrorMessage("时间格式不合法");
		joinStopTime.setResolution(PopupDateField.RESOLUTION_SEC);
		joinStopTime.setValidationVisible(false);
		finishTimeHLayout.addComponent(joinStopTime);
	}

	/**
	 * 创建 存放“与会者号码” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout memberNumHLayout = new HorizontalLayout();
		memberNumHLayout.setSpacing(true);
		gridLayout.addComponent(memberNumHLayout, 0, 1);
		
		Label memberNumLabel = new Label("与会者号码：");
		memberNumLabel.setWidth("-1px");
		memberNumHLayout.addComponent(memberNumLabel);
		
		joinMemberNum_tf = new TextField();
		joinMemberNum_tf.setWidth("120px");
		joinMemberNum_tf.setNullRepresentation("");
		memberNumHLayout.addComponent(joinMemberNum_tf);
	}

	/**
	 * 创建 存放“发 起 人 ：” 的布局管理器
	 */
	private void createOriginatorHLayout() {
		HorizontalLayout originatorHLayout = new HorizontalLayout();
		originatorHLayout.setSpacing(true);
		gridLayout.addComponent(originatorHLayout, 1, 1);
		
		Label originatorLabel = new Label("发 起 人 ：");
		originatorLabel.setWidth("-1px");
		originatorHLayout.addComponent(originatorLabel);
		
		originators_cb = new ComboBox();
		originators_cb.setWidth("135px");
		originators_cb.setImmediate(true);
		originators_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		originators_cb.setContainerDataSource(userContianer);
		originators_cb.setItemCaptionPropertyId("migrateCsr");
		originatorHLayout.addComponent(originators_cb);
	}
	
	/**
	 * 创建 存放“与会坐席” 的布局管理器
	 */
	private void createJoinMemberHLayout() {
		HorizontalLayout joinMemberHLayout = new HorizontalLayout();
		joinMemberHLayout.setSpacing(true);
		gridLayout.addComponent(joinMemberHLayout, 2, 1);
		
		Label joinMemberLabel = new Label("与会坐席：");
		joinMemberLabel.setWidth("-1px");
		joinMemberHLayout.addComponent(joinMemberLabel);
		
		joinMembers_cb = new ComboBox();
		joinMembers_cb.setWidth("128px");
		joinMembers_cb.setImmediate(true);
		joinMembers_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		joinMembers_cb.setContainerDataSource(userContianer);
		joinMembers_cb.setItemCaptionPropertyId("migrateCsr");
		joinMemberHLayout.addComponent(joinMembers_cb);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(!joinStartTime.isValid() || !joinStopTime.isValid()) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间或截止时间格式不正确！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(joinStartTime.getValue() == null || joinStopTime.getValue() == null) {
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
		migrateScope.select("本周");
		meettingRoom_cb.setValue(null);
		joinMemberNum_tf.setValue("");
		originators_cb.setValue(null);
		joinMembers_cb.setValue(null);
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(m) from MeettingDetailRecord as m where " +createDynamicSql();
		String searchSql = "select m from MeettingDetailRecord as m group by m.meettingUniqueId,m.id,m.memberIndex having " +createDynamicSql()
				+ " order by m.meettingUniqueId desc,m.memberIndex asc,m.id asc";
		meettingRecordTableFlip.setSearchSql(searchSql);
		meettingRecordTableFlip.setCountSql(countSql);
		meettingRecordTableFlip.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		// 与会时间查询
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String joinDateScopeSql = "m.joinDate >= '" + dateFormat.format(joinStartTime.getValue()) +"' and m.joinDate <= '" + dateFormat.format(joinStopTime.getValue()) +"'";

		// 会议室号码查询
		String meettingRoomSql = "";
		SipConfig meettingRoom = (SipConfig) meettingRoom_cb.getValue();
		if(meettingRoom != null) {
			meettingRoomSql = " and m.meettingRoom like '%" + meettingRoom.getName() + "%'";
		}
		
		// 与会成员号码查询
		String memberNumSql = "";
		String inputMemberNum = StringUtils.trimToEmpty((String) joinMemberNum_tf.getValue());
		if(!"".equals(inputMemberNum)) {
			memberNumSql = " and m.joinMemberNum like '%" + inputMemberNum + "%'";
		}
		
		// 会议发起人查询
		String originatorSql = "";
		User originator = (User) originators_cb.getValue();
		if(originator != null) {
			originatorSql = " and m.originatorId = "+originator.getId();
		}
		
		// 与会坐席查询
		String joinMemberSql = "";
		User joinMember = (User) joinMembers_cb.getValue();
		if(joinMember != null) {
			joinMemberSql = " and m.joinMemberId = "+joinMember.getId();
		}
		
		// 创建固定的搜索语句
		return joinDateScopeSql + originatorSql + joinMemberSql +" and m.domainId = " +domain.getId()+ meettingRoomSql + memberNumSql;
	}

	public void setMeettingRecordTableFlip(FlipOverTableComponent<MeettingDetailRecord> meettingRecordTableFlip) {
		this.meettingRecordTableFlip = meettingRecordTableFlip;
	}
	
}
