package com.jiangyifen.ec2.ui.csr.toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 话务员定时器管理界面
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class TimerView extends VerticalLayout implements Button.ClickListener, ValueChangeListener {

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"responseTime", "title", "content", 
			"customerPhoneNum", "dialCount", "customerId", "lastDialTime", "firstRespTime", "popCount", "type"};
	
	private final String[] COL_HEADERS = new String[] {"下次响应时间", "标题", "提醒内容", "预约客户电话", "回访客户次数", "预约客户编号", "最近回访时间", "首次响应时间", "累计提醒次数", "响应类型"};
	
	private final Action EDIT = new Action("修改提醒", ResourceDataCsr.edit_16_ico);
	private final Action ADD = new Action("添加提醒", ResourceDataCsr.add_16_ico);
	private final Action DELETE = new Action("删除提醒", ResourceDataCsr.delete_16_ico);
	
	// 定时器显示界面
	private Table timersTable;
	private Button add;
	private Button delete;
	private FlipOverTableComponent<Timers> tableFlipOver;	// 翻页组件
	private HorizontalLayout buttonsHLayout;	// 用于存放以上组件

	// 定时器创建界面
	private VerticalLayout createNewTimer;		// 创建新的定时器界面
	private Label titleError;					// 标题填写错误 提醒
	private TextField titleField;				// 定时提醒的标题
	private HorizontalLayout titleHLayout;
	
	private ComboBox typeSelector;				// 定时提醒的类型
	private HorizontalLayout typeHLayout;
	
	private Label timeError;					// 时间填写错误提醒
	private PopupDateField responseTimeField;	// 定时提醒时间
	private HorizontalLayout timeHLayout;
	
	private RichTextArea contentArea;			// 提醒事件的内容
	
	private boolean isAddEvent = false;			// 是否是添加新定时提醒 默认为不是 false
	private Button edit;						// 编辑
	private Button save;						// 保存
	private Button cancel;						// 取消
	private HorizontalLayout modifyHLayout;		// 存放 save 和 cancel 按钮
	private Panel panel;
	
	private User loginUser;						// 当前登录用户
	private Calendar calendar;					// 日历工具 用于去掉PopupDateField 中的秒和毫秒值
	private boolean isEncryptMobile = true;			// 电话号码是否需要加密
	private ArrayList<String> ownBusinessModels;	// 当前登陆用户目前使用的角色类型下，所拥有的权限

	private TimersService timersService;		// 定时器服务类
	private TelephoneService telephoneService;		// 电话服务类
	
	public TimerView() {
		this.setSpacing(true);
		this.setMargin(true);
		this.setSizeFull();
		
		calendar = Calendar.getInstance();
		loginUser = SpringContextHolder.getLoginUser();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		timersService = SpringContextHolder.getBean("timersService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		
		//创建浏览定时器表格
		createScanTimerTable();
		
		// 创建新的定时器界面
		createNewTimerVLayout();
	}

	private void createScanTimerTable() {
		timersTable = createFormatColumnTable();
		timersTable.setWidth("100%");
		timersTable.setHeight("-1px");
		timersTable.setImmediate(true);
		timersTable.setSelectable(true);
		timersTable.setStyleName("striped");
		timersTable.setColumnWidth("title", 130);
		timersTable.addListener(this);
		timersTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(timersTable);
		
		buttonsHLayout = new HorizontalLayout();
		buttonsHLayout.setSpacing(true);
		buttonsHLayout.setWidth("100%");
		this.addComponent(buttonsHLayout);
		
		add = new Button("添 加", this);
		buttonsHLayout.addComponent(add);
		
		delete = new Button("删 除", this);
		delete.setEnabled(false);
		buttonsHLayout.addComponent(delete);
		
		String countSql = "select count(t) from Timers as t where t.creator.id = " + loginUser.getId();
		String searchSql = countSql.replaceFirst("count\\(t\\)", "t") + " order by t.responseTime desc";
		tableFlipOver = new FlipOverTableComponent<Timers>(Timers.class, timersService, timersTable, searchSql, countSql, null);
		tableFlipOver.getEntityContainer().addNestedContainerProperty("content");
		tableFlipOver.setPageLength(8, false);
		buttonsHLayout.addComponent(tableFlipOver);
		buttonsHLayout.setExpandRatio(tableFlipOver, 1);
		buttonsHLayout.setComponentAlignment(tableFlipOver, Alignment.MIDDLE_RIGHT);
		
		timersTable.setPageLength(8);
		timersTable.setVisibleColumns(VISIBLE_PROPERTIES);
		timersTable.setColumnHeaders(COL_HEADERS);
		
		addActionToTable(timersTable);
	}
	
	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getType() == Date.class) {
					if(property.getValue() != null) {
                		return dateFormat.format((Date)property.getValue());
                	}
                	return "";
				} else if(colId.equals("content")) {
	            	String content = property.getValue().toString();
	            	content = content.replaceAll("<.+?>", "");	// 去掉HTML 语句
	            	if(content.length() > 25){
	            		content = content.substring(0, 25) + "...";
	            	}
	            	return content;
				} else if("customerPhoneNum".equals(colId)) {
					if(isEncryptMobile) {
						return telephoneService.encryptMobileNo((String) property.getValue());
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	private void addActionToTable(Table table) {
		table.addActionHandler(new Action.Handler() {
			public void handleAction(Action action, Object sender, Object target) {
				timersTable.select(target);
				if(action == ADD) {
					add.click();
				} else if(action == EDIT) {
					edit.click();
				} else if(action == DELETE){
					delete.click();
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {ADD, EDIT, DELETE};
				}
				return new Action[]{ADD};
			}
		});
	}

	private void createNewTimerVLayout() {
		panel = new Panel();
		panel.setSizeFull();
		panel.setVisible(false);
		this.addComponent(panel);
		this.setExpandRatio(panel, 1.0f);
		
		createNewTimer = new VerticalLayout();
		createNewTimer.setSpacing(true);
		createNewTimer.setMargin(true);
		panel.setContent(createNewTimer);
		
		titleHLayout = new HorizontalLayout();
		titleHLayout.setWidth("100%");
		titleHLayout.setSpacing(true);
		createNewTimer.addComponent(titleHLayout);
		
		Label titleLabel = new Label("<B>标题：</B>", Label.CONTENT_XHTML);
		titleLabel.setWidth("-1px");
		titleHLayout.addComponent(titleLabel);

		titleField = new TextField();
		titleField.setRequired(true);
		titleField.setWidth("100%");
		titleField.setNullRepresentation("");
		titleHLayout.addComponent(titleField);
		titleHLayout.setExpandRatio(titleField, 1.0f);
		
		titleError = new Label("<font color='red'>标题不能为空！</font>", Label.CONTENT_XHTML);
		titleError.setWidth("-1px");
		titleError.setVisible(false);
		titleHLayout.addComponent(titleError);
		
		typeHLayout = new HorizontalLayout();
		typeHLayout.setSpacing(true);
		createNewTimer.addComponent(typeHLayout);
		
		Label typeLabel = new Label("<B>事件响应周期：</B>", Label.CONTENT_XHTML);
		typeHLayout.addComponent(typeLabel);

		typeSelector = new ComboBox();
		typeSelector.addItem("一次");
		typeSelector.setValue("一次");
		typeSelector.setWidth("160px");
		typeSelector.setNullSelectionAllowed(false);
		typeHLayout.addComponent(typeSelector);
		
		timeHLayout = new HorizontalLayout();
		timeHLayout.setSpacing(true);
		createNewTimer.addComponent(timeHLayout);
		
		Label timeLabel = new Label("<B>定时提醒时间：</B>", Label.CONTENT_XHTML);
		timeHLayout.addComponent(timeLabel);

		responseTimeField = new PopupDateField();
		responseTimeField.setRequired(true);
		responseTimeField.setRequiredError(null);
		responseTimeField.setWidth("162px");
		responseTimeField.setDateFormat("yyyy-MM-dd HH:mm");
		responseTimeField.setResolution(PopupDateField.RESOLUTION_MIN);
		timeHLayout.addComponent(responseTimeField);
		
		timeError = new Label("<font color='red'>提醒时间不能为空！</font>", Label.CONTENT_XHTML);
		timeError.setVisible(false);
		timeHLayout.addComponent(timeError);
		
		createNewTimer.addComponent(new Label("<B>提醒内容：</B>", Label.CONTENT_XHTML));

		contentArea = new RichTextArea();
		contentArea.setWidth("100%");
		contentArea.setNullRepresentation(" ");
		createNewTimer.addComponent(contentArea);
		
		modifyHLayout = new HorizontalLayout();
		modifyHLayout.setSpacing(true);
		createNewTimer.addComponent(modifyHLayout);
		
		edit = new Button("编 辑", this);
		edit.setStyleName("default");
		modifyHLayout.addComponent(edit);
		save = new Button("保 存", this);
		save.setStyleName("default");
		modifyHLayout.addComponent(save);
		cancel = new Button("取 消", this);
		modifyHLayout.addComponent(cancel);
		setComponetsVisible(true);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == timersTable) {
			Timers timer = (Timers) timersTable.getValue();
			delete.setEnabled(timer != null);
			panel.setVisible(timer != null);
			if(timer != null) {
				setComponentsReadOnly(false);
				setComponentsValue(timer.getTitle(), timer.getType(), timer.getResponseTime(), timer.getContent());
			} else {
				setComponentsReadOnly(false);
				setComponentsValue(null, null, null, null);
			}
			setComponetsVisible(true);
			setComponentsReadOnly(true);
			titleError.setVisible(false);
			timeError.setVisible(false);
			isAddEvent = false;
		}
	}
	
	/**
	 * 设置定时提醒详细内容显示区组件的值
	 * @param title	定时提醒的标题
	 * @param type	 定时提醒的类型
	 * @param responseTime  定时提醒的响应时间
	 * @param content		定时提醒的内容
	 */
	public void setComponentsValue(String title, String type, Date responseTime, String content) {
		titleField.setValue(title);
		typeSelector.setValue(type);
		responseTimeField.setValue(responseTime);
		contentArea.setValue(content);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			setComponetsVisible(false);
			setComponentsReadOnly(false);
		} else if(source == add) {
			setComponentsReadOnly(false);
			setComponetsVisible(false);
			panel.setVisible(true);
			setComponentsValue(null, "一次", null, "");
			isAddEvent = true;
		} else if(source == delete) {
			Timers timers = (Timers) timersTable.getValue();
			timersService.deleteById(timers.getId());
			timersService.refreshSchedule(true, timers);
			tableFlipOver.refreshInCurrentPage();
			timersTable.setValue(null);
		} else if(source == save) {
			if(checkValueQualified()) {
				handleSaveEvent();
			}
		} else if(source == cancel) {
			if(isAddEvent) {	// 只有是新增提醒时，点击取消后才需要是新增界面隐藏
				panel.setVisible(false);
			}
			setComponetsVisible(true);
			setComponentsReadOnly(true);
			isAddEvent = false;
		}
	}

	/**
	 * 处理保存事件
	 */
	private void handleSaveEvent() {
		String contentValue = StringUtils.trimToEmpty((String) contentArea.getValue());
		
		Timers timers = null;
		if(isAddEvent) {
			timers = new Timers();
			timers.setCreateTime(new Date());
			timers.setFirstRespTime(new Date());
			timers.setCreator(loginUser);
			timers.setDomain(loginUser.getDomain());
		} else {
			timers = (Timers) timersTable.getValue();
		}
		
		Date responseTime = (Date) responseTimeField.getValue();
		calendar.setTime(responseTime);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		timers.setTitle(titleField.getValue().toString().trim());
		timers.setType((String) typeSelector.getValue());
		timers.setResponseTime(calendar.getTime());
		timers.setContent(contentValue);
		
		if(isAddEvent) {
			timersService.save(timers);
			tableFlipOver.refreshToFirstPage();
			panel.setVisible(false);
		} else {
			timersService.update(timers);
			tableFlipOver.refreshInCurrentPage();
		}

		timersService.refreshSchedule(false, timers);	// 创建 TimerTask
		setComponetsVisible(true);
		titleError.setVisible(false);
		timeError.setVisible(false);
		setComponentsReadOnly(true);
		isAddEvent = false;
	}

	/**
	 * 检查编辑或添加 定时器时，其字段值是否合法
	 * @return
	 */
	private boolean checkValueQualified() {
		boolean isQualified = true;
		
		String title = (String) titleField.getValue();	// 检查标题
		if(title == null || "".equals(title.trim())) {
			isQualified = false;
			titleError.setVisible(true);
		} else {
			titleError.setVisible(false);
		}

		Date time = (Date) responseTimeField.getValue();	// 检查响应时间
		if(time == null) {
			isQualified = false;
			timeError.setVisible(true);
		} else {
			timeError.setVisible(false);
		}
		
		String contentValue = StringUtils.trimToEmpty((String) contentArea.getValue());	// 检查响应内容
		contentValue = contentValue.replaceAll("<.+?>", "");
		if("".equals(contentValue)) {
			contentArea.getApplication().getMainWindow().showNotification("定时提醒的内容不能为空！", Notification.TYPE_WARNING_MESSAGE);
			isQualified = false;
		}
		return isQualified;
	}

	/**
	 * 设置编辑组件的可见属性
	 * @param visible
	 */
	private void setComponetsVisible(boolean visible) {
		edit.setVisible(visible);
		save.setVisible(!visible);
		cancel.setVisible(!visible);
	}
	
	/**
	 * 设置组件的只读属性
	 * @param readOnly
	 */
	private void setComponentsReadOnly(boolean readOnly) {
		titleField.setReadOnly(readOnly);
		typeSelector.setReadOnly(readOnly);
		responseTimeField.setReadOnly(readOnly);
		contentArea.setReadOnly(readOnly);
	}
	
	public void refreshTimerTable() {
		tableFlipOver.refreshToFirstPage();
	}

	public Table getTimersTable() {
		return timersTable;
	}

}
