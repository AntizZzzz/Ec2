package com.jiangyifen.ec2.ui.csr.workarea.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Message;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageType;
import com.jiangyifen.ec2.service.eaoservice.MessagesManageService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.DialComponentToTable;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * 话务员发送的历史短信 查看界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CsrHistoryMessageView extends VerticalLayout implements ClickListener, ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"phoneNumber", "content", "time", "messageType"};
	
	private final String[] COL_HEADERS = new String[] {"收信人", "内容", "发送时间", "短信类型"};

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final Action SCAN = new Action("查 看");				// 右键单击事件
	private final Action DELETE = new Action("删 除");
	private final Action EDIT_SEND_AGAIN = new Action("编辑并重发");

	private Notification success_notification;					// 成功过提示信息
	private Notification warning_notification;					// 错误警告提示信息
	
	private ComboBox timeScope;									// “时间范围”选择框
	private PopupDateField startTime;							// “开始时间”选择框
	private PopupDateField finishTime;							// “截止时间”选择框

	private ValueChangeListener timeScopeListener;				// 时间范围的监听器
	private ValueChangeListener startTimeListener;				// 开始时间的监听器
	private ValueChangeListener finishTimeListener;				// 截止时间的监听器
	
	private Button search_button; 								// 搜索按钮
	private Button clear_button; 								// 清空按钮

	private Table message_table;								// 短信显示区域
	private Button scan_button; 								// 查看按钮
	private Button delete_button; 								// 删除按钮
	private Button editAndSend_button;							// 编辑并重发
	
	private FlipOverTableComponent<Message> messageFlip; 		// 翻页组件
	
	private MessageDetailWindow messageDetailWindow;			// 短信详细内容查看窗口
	
	private User loginUser;										// 当前登陆用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限
	
	private MessagesManageService messagesManageService;		// 短信管理服务类
	
	public CsrHistoryMessageView() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		messagesManageService = SpringContextHolder.getBean("messagesManageService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		// 创建过滤器组件
		createFilterComponents();
		
		// 创建历史短息显示表格
		createMessageTable();
		
		// 创建历史短息表格的翻页组件
		createTableFlipComponent();
		
		// 根据分辨率设置表格行数
		setTablePageLength();

	}
	
	/**
	 * 创建过滤器组件
	 */
	private void createFilterComponents() {
		HorizontalLayout filter_hl = new HorizontalLayout();
		filter_hl.setSpacing(true);
		this.addComponent(filter_hl);
		
		// 时间范围选中框
		Label timeScopeLabel = new Label("时间范围：");
		timeScopeLabel.setWidth("-1px");
		filter_hl.addComponent(timeScopeLabel);
		
		timeScope = new ComboBox();
		timeScope.addItem("今天");
		timeScope.addItem("昨天");
		timeScope.addItem("本周");
		timeScope.addItem("上周");
		timeScope.addItem("本月");
		timeScope.addItem("上月");
		timeScope.addItem("精确时间");
		timeScope.setValue("今天");
		timeScope.setWidth("115px");
		timeScope.setImmediate(true);
		timeScope.setNullSelectionAllowed(false);
		filter_hl.addComponent(timeScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)timeScope.getValue();
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
		timeScope.addListener(timeScopeListener);
		
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		// 开始时间选中框
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		filter_hl.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("精确时间");
				timeScope.addListener(timeScopeListener);
			}
		};
		
		startTime = new PopupDateField();
		startTime.setImmediate(true);
		startTime.setWidth("156px");
		startTime.setValue(dates[0]);
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setParseErrorMessage("时间格式不合法");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTime.addListener(startTimeListener);
		filter_hl.addComponent(startTime);
	
		// 截止时间选中框
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		filter_hl.addComponent(finishTimeLabel);
	
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(finishTimeListener);
				timeScope.setValue("精确时间");
				timeScope.addListener(timeScopeListener);
			}
		};
		
		finishTime = new PopupDateField();
		finishTime.setImmediate(true);
		finishTime.setWidth("156px");
		finishTime.setValue(dates[1]);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setParseErrorMessage("时间格式不合法");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTime.addListener(finishTimeListener);
		filter_hl.addComponent(finishTime);
		
		clear_button = new Button("清 空", this);
		filter_hl.addComponent(clear_button);
		
		search_button = new Button("查 询", this);
		search_button.setStyleName("default");
		filter_hl.addComponent(search_button);
	}

	/**
	 *  创建历史短息显示表格
	 */
	private void createMessageTable() {
		message_table = createFormatColumnTable();
		message_table.setWidth("100%");
		message_table.setHeight("-1px");
		message_table.setImmediate(true);
		message_table.addListener(this);
		message_table.setSelectable(true);
		message_table.setStyleName("striped");
		message_table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		message_table.addGeneratedColumn("phoneNumber", new DialColumnGenerator());
		message_table.setColumnWidth("phoneNumber", 195);
		message_table.addGeneratedColumn("content", new ContentColumnGenerator());
		message_table.setColumnExpandRatio("content", 1.0f);
		this.addComponent(message_table);

		// 为表格添加右键单击事件
		addActionToTable(message_table);
	}
	
	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date)property.getValue());
				} else if("messageType".equals(colId)) {
					MessageType mt = (MessageType) property.getValue();
					return mt.getName();
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 *  创建历史短息表格的翻页组件
	 */
	private void createTableFlipComponent() {
		HorizontalLayout bottom_hl = new HorizontalLayout();
		bottom_hl.setWidth("100%");
		bottom_hl.setSpacing(true);
		this.addComponent(bottom_hl);
		
		// 操作按钮组件
		HorizontalLayout operator_hl = new HorizontalLayout();
		operator_hl.setSpacing(true);
		bottom_hl.addComponent(operator_hl);
		
		scan_button = new Button("查看", this);
		scan_button.setEnabled(false);
		operator_hl.addComponent(scan_button);
		
		delete_button = new Button("删除", this);
		delete_button.setEnabled(false);
		operator_hl.addComponent(delete_button);

		editAndSend_button = new Button("编辑并重发", this);
		editAndSend_button.setEnabled(false);
		operator_hl.addComponent(editAndSend_button);
		
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		
		String countSql = "select count(m) from Message as m where m.user.id = " +loginUser.getId()
					+" and m.time >= '" +dateStrs[0]+ "' and m.time <= '" +dateStrs[1]+"'";
		String searchSql = countSql.replaceFirst("count\\(m\\)", "m") + " order by m.time desc";
		
		messageFlip = new FlipOverTableComponent<Message>(Message.class, 
				messagesManageService, message_table, searchSql , countSql, null);
		
		messageFlip.setSearchSql(searchSql);
		messageFlip.setCountSql(countSql);
		
		message_table.setVisibleColumns(VISIBLE_PROPERTIES);
		message_table.setColumnHeaders(COL_HEADERS);
		
		bottom_hl.addComponent(messageFlip);
		bottom_hl.setComponentAlignment(messageFlip, Alignment.TOP_RIGHT);
	}

	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			message_table.setPageLength(35);
			messageFlip.setPageLength(35, false);
		} else if(screenResolution[1] >= 1050) {
			message_table.setPageLength(33);
			messageFlip.setPageLength(33, false);
		} else if(screenResolution[1] >= 900) {
			message_table.setPageLength(26);
			messageFlip.setPageLength(26, false);
		} else if(screenResolution[1] >= 768) {
			message_table.setPageLength(19);
			messageFlip.setPageLength(19, false);
		} else {
			message_table.setPageLength(14);
			messageFlip.setPageLength(14, false);
		}
	}

	/**
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class DialColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("phoneNumber")) {
				Message message = (Message) itemId;
				HashSet<Telephone> telephones = new HashSet<Telephone>();
				Telephone tel = new Telephone();
				tel.setNumber(message.getPhoneNumber());
				telephones.add(tel);
				return new DialComponentToTable(source, itemId, telephones, loginUser, ownBusinessModels, CsrHistoryMessageView.this);
			} 
			return null;
		}
	}
	
	/**
	 * 自动创建表格中短信内容显示组件
	 */
	private class ContentColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Message message = (Message) itemId;
			if(columnId.equals("content")) {
				String content = message.getContent();
				if(content == null) {
					return null;
				} else if(!"".equals(content.trim())) {
					Label contentLabel = new Label();
					contentLabel.setWidth("-1px");
					String trimedContent = content.trim();
					contentLabel.setDescription(trimedContent);
					contentLabel.setValue(trimedContent);
					int len = (screenResolution[0] / 25) +1;
					if(trimedContent.length() > len) {
						contentLabel.setValue(trimedContent.substring(0, len) + "...");
					} 
					return contentLabel;
				}
			} 
			return null;
		}
	}
	
	/**
	 * 为指定模板表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				table.select(target);
				if(action == SCAN) {
					scan_button.click();
				} else if(action == DELETE) {
					delete_button.click();
				} else if(action == EDIT_SEND_AGAIN) {
					editAndSend_button.click();
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {SCAN, DELETE, EDIT_SEND_AGAIN};
				}
				return null;
			}
		});
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == message_table) {
			Message message = (Message) message_table.getValue();
			scan_button.setEnabled(message != null);
			delete_button.setEnabled(message != null);
			editAndSend_button.setEnabled(message != null);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_button) {
			executeSearch();
		} else if(source == scan_button) {
			showMessageDetailWindow(false);
		} else if(source == delete_button) {
			executeDelete();
		} else if(source == editAndSend_button) {
			showMessageDetailWindow(true);
		} else if(source == clear_button) {
			timeScope.setValue("今天");
		}
	}

	/**
	 * 显示短信的详细内容 或者编辑后重发
	 * @param isSendAgain	是否重发
	 */
	private void showMessageDetailWindow(boolean isSendAgain) {
		if(messageDetailWindow == null) {
			messageDetailWindow = new MessageDetailWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(messageDetailWindow);
		Message message = (Message) message_table.getValue();
		messageDetailWindow.updateFormDataSource(message, isSendAgain);
		this.getApplication().getMainWindow().addWindow(messageDetailWindow);
	}

	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		StringBuffer dynamicCountSql = new StringBuffer();
		dynamicCountSql.append("select count(m) from Message as m where m.user.id = ");
		dynamicCountSql.append(loginUser.getId().toString());
		if(startTime.getValue() != null) {
			dynamicCountSql.append(" and m.time >= '");
			dynamicCountSql.append(dateFormat.format(startTime.getValue()));
			dynamicCountSql.append("'");
		}
	
		if(finishTime.getValue() != null) {
			dynamicCountSql.append(" and m.time <= '");
			dynamicCountSql.append(dateFormat.format(finishTime.getValue()));
			dynamicCountSql.append("'");
		}
		
		String countSql = dynamicCountSql.toString();
		String searchSql = countSql.replaceFirst("count\\(m\\)", "m") + " order by m.time desc";
		
		messageFlip.setSearchSql(searchSql);
		messageFlip.setCountSql(countSql);
		messageFlip.refreshToFirstPage();
	}

	/**
	 * 执行删除
	 */
	private void executeDelete() {
		try {
			Message message = (Message) message_table.getValue();
			messagesManageService.deleteMessage(message);
			messageFlip.refreshInCurrentPage();
		} catch (Exception e) {
			logger.error("话务员删除历史短信失败！---》"+e.getMessage(), e);
			warning_notification.setCaption("删除失败，稍后请重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		success_notification.setCaption("删除历史短息成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
	}
	
	/**
	 * 刷新历史短息显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			messageFlip.refreshToFirstPage();
		} else {
			messageFlip.refreshInCurrentPage();
		}
	}

	/**
	 * 获取历史信息显示表格
	 * @return
	 */
	public Table getMessageTable() {
		return message_table;
	}
	
}
