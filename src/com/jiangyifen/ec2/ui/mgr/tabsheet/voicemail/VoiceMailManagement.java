package com.jiangyifen.ec2.ui.mgr.tabsheet.voicemail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.Voicemail;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.eaoservice.VoicemailService;
import com.jiangyifen.ec2.ui.FlipOverTableComponentUseNativeSql;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * <p>语音留言管理</p>
 * 
 * <p>只适用于单个与domainid=1</p>
 *
 * @version $Id: VoicemailManage.java 2014-6-20 上午10:44:37 chenhb $
 *
 */
@SuppressWarnings("serial")
public class VoiceMailManagement extends VerticalLayout implements Button.ClickListener{
//	private Logger logger = LoggerFactory.getLogger(getClass());

	//搜索组件
	private ComboBox timeScope;			// “留言时间”选择框
	private PopupDateField startTime;		// “起始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
//	private ComboBox outlineComboBox;		// “呼叫外线”下拉框
	private ComboBox empNoComboBox;		// 工号选择框
	private TextField phoneNumberField;	// 电话号码
//	private TextField durationField;		// 持续时长
	
	private Button searchButton;
	private Button clearButton;
	
	//baseurl 指向monitor下,更改为指向voicemail
	private String baseurl=BaseUrlUtils.getBaseUrl().replace("monitor", "voicemail");
	private Label player;								// 播放器显示标签
	private HorizontalLayout playerLayout;				// 存放播放器的布局管理器
	private ArrayList<String> ownBusinessModels;		// 当前用户拥有的权限

	
	// 表格组件
	private Table table;
	private User loginUser;
	private String sqlSelect;
	private String sqlCount;
	private VoicemailService voicemailService;
	private DepartmentService departmentService;
	private FlipOverTableComponentUseNativeSql<Voicemail> flip;
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	
	public VoiceMailManagement() {
		this.setWidth("100%");
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		voicemailService = SpringContextHolder.getBean("voicemailService");
		departmentService = SpringContextHolder.getBean("departmentService");
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);
		
		constrantLayout.addComponent(buildSearchLayout());
		
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());

	}

	/**
	 * 创建搜索输出
	 * @return
	 */
	private Component buildSearchLayout() {
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
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
						}
					}
				}
			}
		}
		
		GridLayout gridLayout = new GridLayout(8, 3);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false, true, false, true);
		
		// 时间范围选中框
		gridLayout.addComponent(new Label("留言时间："), 0, 0);
		gridLayout.addComponent(buildTimeScopeComboBox(), 1, 0);
		
		// 开始时间选中框
		gridLayout.addComponent(new Label("起始时间："), 2, 0);
		
		startTime = new PopupDateField();
		startTime.setWidth("160px");
		startTime.setValidationVisible(false);
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTime.setImmediate(true);
		startTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("全部");
				timeScope.addListener(timeScopeListener);
			}
		};
		startTime.addListener(startTimeListener);
		gridLayout.addComponent(startTime, 3, 0);

		// 截止时间选中框
		gridLayout.addComponent(new Label("截止时间："), 4, 0);
		
		finishTime = new PopupDateField();
		finishTime.setWidth("160px");
		finishTime.setValidationVisible(false);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTime.setImmediate(true);
		finishTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("全部");
				timeScope.addListener(timeScopeListener);
			}
		};
		finishTime.addListener(finishTimeListener);
		gridLayout.addComponent(finishTime, 5, 0);
		
		
//		private ComboBox userComboBox;      // “用户信箱”下拉框
//		private ComboBox outlineComboBox;   // “呼叫外线”下拉框
//		private TextField phoneNumberField; // 电话号码
//		private TextField durationField;    // 持续时长

		
		// 任务项目选择框
		gridLayout.addComponent(new Label("用户信箱："), 0, 1);
		gridLayout.addComponent(buildEmpNoComboBox(allGovernedDeptIds), 1, 1);

		// 呼叫结果选择框
//		gridLayout.addComponent(new Label("外线号码："), 2, 1);
//		gridLayout.addComponent(buildOutlineComboBox(allGovernedDeptIds), 3, 1);

		// 呼叫结果选择框
		gridLayout.addComponent(new Label("电话号码："), 2, 1);
		phoneNumberField=new TextField();
		gridLayout.addComponent(phoneNumberField, 3, 1);

		// 持续时长选择框
//		gridLayout.addComponent(new Label("持续时长："), 4, 1);
//		durationField=new TextField();
//		gridLayout.addComponent(durationField, 5, 1);
		
		// 查询按钮
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		searchButton.addStyleName("small");
		gridLayout.addComponent(searchButton, 6, 0);
		
		// 清空按钮
		clearButton = new Button("清 空", (ClickListener) this);
		clearButton.addStyleName("small");
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);


		timeScope.setValue("今天");
		
		return gridLayout;
	}

//	/**
//	 * 创建客服工号的ComboBox
//	 * 
//	 * @return
//	 */
//	private ComboBox buildOutlineComboBox(List<Long> allGovernedDeptIds) {
//		// 从数据库中获取“外线”信息，并绑定到Container中
//		BeanItemContainer<SipConfig> outlineContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
//		SipConfig allOutline=new SipConfig();
//		allOutline.setName("全部");
//		outlineContainer.addBean(allOutline);
//		SipConfigService sipConfigService=SpringContextHolder.getBean("sipConfigService");
//		outlineContainer.addAll(sipConfigService.getAllOutlinesByDomain(SpringContextHolder.getDomain()));
//		
//		// 创建ComboBox
//		outlineComboBox = new ComboBox();
//		outlineComboBox.setContainerDataSource(outlineContainer);
//		outlineComboBox.setItemCaptionPropertyId("name");
//		outlineComboBox.setValue(allOutline);
//		outlineComboBox.setWidth("158px");
//		outlineComboBox.setNullSelectionAllowed(false);
//		return outlineComboBox;
//	}
	
	
	/**
	 * 创建客服工号的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildEmpNoComboBox(List<Long> allGovernedDeptIds) {
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User user=new User();
		user.setEmpNo("全部");
		userContainer.addBean(user);
		UserService userService=SpringContextHolder.getBean("userService");
		userContainer.addAll(userService.getCsrsByDepartment(allGovernedDeptIds, SpringContextHolder.getDomain().getId()));
		
		// 创建ComboBox
		empNoComboBox = new ComboBox();
		empNoComboBox.setContainerDataSource(userContainer);
		empNoComboBox.setItemCaptionPropertyId("empNo");
		empNoComboBox.setValue(user);
		empNoComboBox.setWidth("158px");
		empNoComboBox.setNullSelectionAllowed(false);
		return empNoComboBox;
	}
	
	
	/**
	 * 创建时间段的组合框
	 * 
	 * @return
	 */
	private ComboBox buildTimeScopeComboBox() {
		
		timeScope = new ComboBox();
		timeScope.addItem("全部");
		timeScope.addItem("今天");
		timeScope.addItem("昨天");
		timeScope.addItem("本周");
		timeScope.addItem("上周");
		timeScope.addItem("本月");
		timeScope.addItem("上月");
		timeScope.setValue("全部");
		timeScope.setWidth("158px");
		timeScope.setNullSelectionAllowed(false);
		timeScope.setImmediate(true);
		timeScopeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
//				System.err.println(startTimeListener);
				startTime.removeListener(startTimeListener);
//				System.err.println(finishTimeListener);
				finishTime.removeListener(finishTimeListener);
				String scopeValue=(String)timeScope.getValue();
				if(scopeValue.equals("全部")){
					startTime.setValue(null);
					finishTime.setValue(null);
				}else{
					Date[] dates=parseToDate((String)timeScope.getValue());
					startTime.setValue(dates[0]);
					finishTime.setValue(dates[1]);
				}
				startTime.addListener(startTimeListener);
				finishTime.addListener(finishTimeListener);
			}
		};
		timeScope.addListener(timeScopeListener);
		return timeScope;
	}
	
	/**
	 * 解析字符串到日期
	 * @param timeScopeValue
	 * @return
	 */
	// 将用户选择的组合框中的值解析为日期字符串
	private static Date[] parseToDate(String timeScopeValue) {
		Date[] dates = new Date[2]; // 存放开始于结束时间
		Calendar cal = Calendar.getInstance(); // 取得当前时间
		cal.setTime(new Date());

		if ("今天".equals(timeScopeValue)) {
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("昨天".equals(timeScopeValue)) {
			cal.add(Calendar.DAY_OF_WEEK, -1);
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("本周".equals(timeScopeValue)) {
			int day = cal.get(Calendar.DAY_OF_WEEK);
			if(day == 1) {
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
			
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 本周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 本周的最后一天
		} else if ("本月".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 本月第一天

			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 本月最后一天
		} else if ("上周".equals(timeScopeValue)) {
			cal.add(Calendar.WEEK_OF_MONTH, -1);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 上周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 上周的最后一天
		} else if ("上月".equals(timeScopeValue)) {
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 上月月第一天

			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 上月最后一天
		}
		//目的是将日期格式的时分秒设为00:00:00
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates[0]=sdf.parse(sdf.format(dates[0]));
			dates[1]=sdf.parse(sdf.format(dates[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dates;
	}

	/**
	 * 创建表格和按钮
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setWidth("100%");
		tabelAndButtonsLayout.setSpacing(true);
		//创建表格
		table = createFormatColumnTable();
		table.setStyleName("striped");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setWidth("100%");
		table.addGeneratedColumn("url", new ListenTypeColumnGenerator());
		table.setColumnAlignment("url", Table.ALIGN_CENTER);
		
		tabelAndButtonsLayout.addComponent(table);
		
		//Sql语句初始化
		searchButton.click();
				
		//创建按钮
		tabelAndButtonsLayout.addComponent(buildButtonsLayout());
		
		return tabelAndButtonsLayout;
	}
	/**
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
	 * 用于自动生成“试听录音”列
	 */
	private class ListenTypeColumnGenerator implements Table.ColumnGenerator {
		@SuppressWarnings("unused")
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Voicemail voicemail= (Voicemail) itemId;
			String tmpUrl = baseurl+voicemail.getPartFilename();
			final String url = tmpUrl;

			if (url != null) {
				final Button listen = new Button("试听");
				listen.setStyleName(BaseTheme.BUTTON_LINK);
				listen.setIcon(ResourceDataCsr.listen_type_ico);
				listen.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if (player != null) {
							player.setValue(null);
							playerLayout.removeAllComponents();
						}

						// 判断当前是否对电话进行了加密，如果加密了，则播放器上的录音访问路径也不能显示电话号码，只能显示不带电话的录音名称
						String path = "<EMBED src='" +url+ "' hidden='false' autostart='true' width=360 height=63 " 
									+ "type=audio/x-ms-wma volume='0' loop='0' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
					
						player = new Label(path, Label.CONTENT_XHTML);
						playerLayout.addComponent(player);
					}
				});

				Link typelink = new Link("下载", new ExternalResource(url));
				typelink.setIcon(ResourceDataCsr.down_type_ico);
				typelink.setTargetName("_blank");
				

				HorizontalLayout layout = new HorizontalLayout();
				layout.setSpacing(true);
//				layout.setWidth("100%");
				layout.addComponent(listen);
				layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);

				if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_CALL_RECORD)) {
					layout.addComponent(typelink);
					layout.setComponentAlignment(typelink, Alignment.MIDDLE_RIGHT);
				}

				return layout;
				
			} else {
				return new Label("无录音文件");
			}
		}
	}

	/**
	 * 创建播放录音组件
	 * 
	 * @param tableFooterLayout
	 */
	private HorizontalLayout createPlayerLayout() {
		playerLayout = new HorizontalLayout();
		playerLayout.setSpacing(true);

		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 "
				+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		playerLayout.addComponent(player);
		return playerLayout;
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
		flip =new FlipOverTableComponentUseNativeSql<Voicemail>(Voicemail.class,
				voicemailService, table, sqlSelect, sqlCount,null);
		table.setPageLength(15);
		flip.setPageLength(15);
		
		//设置表格头部显示
		Object[] visibleColumns=new Object[] { "id", "username","cusnum","duration", "fromOutline","origtimeDatetime","url"};
		String[] columnHeaders=new String[] { "编号", "姓名", "客户号码","持续时长","呼叫外线","发起时间","下载/试听"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		
		// 创建播放录音组件
		HorizontalLayout playerLayout = createPlayerLayout();
		tableButtons.addComponent(playerLayout);
		tableButtons.setComponentAlignment(playerLayout, Alignment.MIDDLE_LEFT);
		
		//添加翻页组件
		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.TOP_RIGHT);
		return tableButtons;
	}

	/**
	 * 由buttonClick调用，执行搜索
	 */
	private void executeSearch() {
		// 用户工号选择信息
		User user= (User) empNoComboBox.getValue();
		String empNoStr = "";
		if ("全部".equals(user.getEmpNo())) {
			empNoStr = "";
		} else {
			empNoStr = user.getEmpNo().toString();
		}

		// 外线号码选择信息
//		SipConfig outline= (SipConfig) outlineComboBox.getValue();
//		String outlineStr = "";
//		if ("全部".equals(outline.getName())) {
//			outlineStr  = "";
//		} else {
//			outlineStr = outline.getName();
//		}
		
		//对于指定时间范围进行查询
		SimpleDateFormat sdf = new SimpleDateFormat(
		 "yyyy-MM-dd HH:mm:ss");
		Date startTimeValue = (Date)startTime.getValue();
		Date finishTimeValue = (Date)finishTime.getValue();
		String startTimeStr="";
		if(startTimeValue!=null){	
			startTimeStr=sdf.format(startTimeValue);
		}
		String finishTimeStr="";
		if(finishTimeValue!=null){
			finishTimeStr=sdf.format(finishTimeValue);
		}
		
		//电话号码
		String phoneNumber = (String)phoneNumberField.getValue();
		phoneNumber=phoneNumber.trim();
		if(!phoneNumber.equals("")&&!StringUtils.isNumeric(phoneNumber)){
			NotificationUtil.showWarningNotification(this, "电话号码不合法");
		}

//		//持续时长
//		String durationValue= (String)durationField.getValue();
//		durationValue=durationValue.trim();
//		if(!durationValue.equals("")&&!StringUtils.isNumeric(durationValue)){
//			NotificationUtil.showWarningNotification(this, "持续时长");
//		}
		
		sqlSelect="select v.id,v.userid,u.username,v.cusnum,v.fromOutline,v.origtimeDatetime,v.duration,v.partfilename from ec2_voicemail v left join ec2_user u on v.userid=u.id where v.domainid= " + loginUser.getDomain().getId();
		sqlCount="select count(v.id) from ec2_voicemail v left join ec2_user u on v.userid=u.id where v.domainid="+loginUser.getDomain().getId();
		
		if(!StringUtils.isEmpty(empNoStr)){
			sqlSelect += " and u.username='"+empNoStr+"' ";
			sqlCount += " and u.username='"+empNoStr+"' ";
		}
		
		if(!StringUtils.isEmpty(phoneNumber)){
			sqlSelect += " and v.cusnum like '%"+phoneNumber+"%' ";
			sqlCount += " and v.cusnum like '%"+phoneNumber+"%' ";
		}
		
		if(!StringUtils.isEmpty(startTimeStr)){
			sqlSelect += " and v.origtimeDatetime > '"+startTimeStr+"'";
			sqlCount  += " and v.origtimeDatetime > '"+startTimeStr+"'";
		}
		
		if(!StringUtils.isEmpty(finishTimeStr)){
			sqlSelect += " and v.origtimeDatetime < '"+finishTimeStr+"'";
			sqlCount  += " and v.origtimeDatetime < '"+finishTimeStr+"'";
		}
		
//		if(!StringUtils.isEmpty(durationValue)){
//			sqlSelect += " and v.duration<"+durationValue+" ";
//			sqlCount += " and v.duration<"+durationValue+" ";
//		}
		
		sqlSelect =sqlSelect +" order by v.id desc";
//		System.err.println(sqlSelect);
		
		// 更新Table，并使Table处于未选中
		this.updateTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}
	
	/**
	 * 由executeSearch调用更新表格内容
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 * @param isToFirst
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	
	@Override
	public void buttonClick(ClickEvent event) {
		if (searchButton == event.getButton()) {
			try {
				if(startTime.getValue() == null){
					//do nothing
				}else if(!startTime.isValid()) {
					NotificationUtil.showWarningNotification(this, "起始联系时间格式不正确，请修改后重试！");
					return;
				}
				
				if(finishTime.getValue() == null){
					//do nothing
				}else if(!finishTime.isValid()) {
					NotificationUtil.showWarningNotification(this, "截止联系时间格式不正确，请修改后重试！");
					return;
				}
				
					Date startTimeDate = (Date) startTime.getValue();
					Date finishTimeDate = (Date) finishTime.getValue();
					
					if(startTimeDate!=null&&finishTimeDate!=null){
						Calendar cal = Calendar.getInstance();
						cal.setTime(finishTimeDate);
						cal.add(Calendar.MONTH, -1);
						Date lastMonth = cal.getTime();
						if(startTimeDate.before(lastMonth)) {
							NotificationUtil.showWarningNotification(this, "对不起，单次只能查询一个月范围内的数据，请修改后重试！");
							return;
						}
					}
	
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "对不起，查询失败，请重试！");
			}
		} else if (clearButton == event.getButton()) {
			//初始化查询条件的选择
			timeScope.setValue("今天");
//			outlineComboBox.select(outlineComboBox.getItemIds().toArray()[0]); //没有使用
			empNoComboBox.select(empNoComboBox.getItemIds().toArray()[0]);
			phoneNumberField.setValue("");
//			durationField.setValue("");
		}
	}

}
