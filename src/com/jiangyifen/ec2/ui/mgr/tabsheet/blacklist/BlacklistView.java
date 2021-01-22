package com.jiangyifen.ec2.ui.mgr.tabsheet.blacklist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.BlackListItemService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.utils.ConfirmedWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 黑名单管理界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class BlacklistView extends VerticalLayout implements ClickListener, ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "phoneNumber", "type", "createTime", "reason"};
	
	private final String[] COL_HEADERS = new String[] {"黑名单编号", "号码", "类型", "加入时间", "加入原因"};

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final Action ADD = new Action("添 加");				// 右键单击事件
	private final Action EDIT = new Action("编 辑");
	private final Action DELETE = new Action("删 除");

	private Notification success_notification;					// 成功过提示信息
	private Notification warning_notification;					// 错误警告提示信息

	private TextField phoneNo_tf;								// “号码”输入框
	private ComboBox type_cb;									// “类型”选择框

	private Button search_button; 								// 搜索按钮
	private Button clear_button; 								// 清空按钮

	private Table blacklist_table;								// 黑名单显示区域
	
	private Button viewAndEditOutline2Blacklist_bt;				// 查看并编辑外线与黑名单之间的关系
	private Button add_bt; 										// 查看按钮
	private Button btnMutiAdd;									// 批量添加
	private Button edit_bt;										// 编辑并重发
	private Button delete_bt; 									// 删除按钮
	private ConfirmedWindow confirmWindow;						// 确认执行的弹屏窗口
	
	private FlipOverTableComponent<BlackListItem> blacklistItemFlip; 	// 翻页组件
	
	private BlacklistItemWindow blacklistItemWindow;			// 黑名单添加或编辑窗口
	private Outline2BlacklistWindow outline2Blacklist;			// 按外线设定黑名单窗口
	
	private Domain domain;										// 当前登录用户所属域
	private Integer[] screenResolution;							// 屏幕分辨率
	
	private BlackListItemService blackListItemService;			// 黑名单管理服务类
	
	
	public BlacklistView() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
		
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		blackListItemService = SpringContextHolder.getBean("blackListItemService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		// 创建过滤器组件
		createFilterComponents();
		
		// 创建黑名单显示表格
		createBlacklistTable();
		
		// 创建黑名单表格的翻页组件
		createTableFlipComponent();
		
	}

	/**
	 * 
	 */
	private void createFilterComponents() {
		HorizontalLayout filter_hl = new HorizontalLayout();
		filter_hl.setSpacing(true);
		this.addComponent(filter_hl);
		
		// 号码输入框
		Label phoneNoLabel = new Label("号码：");
		phoneNoLabel.setWidth("-1px");
		filter_hl.addComponent(phoneNoLabel);
		filter_hl.setComponentAlignment(phoneNoLabel, Alignment.MIDDLE_LEFT);
		
		phoneNo_tf = new TextField();
		phoneNo_tf.setWidth("120px");
		phoneNo_tf.setMaxLength(12);
		phoneNo_tf.addValidator(new RegexpValidator("\\d{1,12}", "号码只能由1-12位的数字组成"));
		phoneNo_tf.setValidationVisible(false);
		filter_hl.addComponent(phoneNo_tf);
		filter_hl.setComponentAlignment(phoneNo_tf, Alignment.MIDDLE_LEFT);
		
		// 号码输入框
		Label typeLabel = new Label("类型：");
		typeLabel.setWidth("-1px");
		filter_hl.addComponent(typeLabel);
		filter_hl.setComponentAlignment(typeLabel, Alignment.MIDDLE_LEFT);

		type_cb = new ComboBox();
		type_cb.addItem("all");
		type_cb.addItem(BlackListItem.TYPE_INCOMING);
		type_cb.addItem(BlackListItem.TYPE_OUTGOING);
		type_cb.setItemCaption("all", "全部");
		type_cb.setItemCaption(BlackListItem.TYPE_INCOMING, "呼入");
		type_cb.setItemCaption(BlackListItem.TYPE_OUTGOING, "呼出");
		type_cb.setNullSelectionAllowed(false);
		type_cb.setWidth("120px");
		type_cb.setImmediate(true);
		type_cb.setValue("all");
		filter_hl.addComponent(type_cb);
		filter_hl.setComponentAlignment(type_cb, Alignment.MIDDLE_LEFT);

		clear_button = new Button("清 空", this);
		filter_hl.addComponent(clear_button);
		
		search_button = new Button("查 询", this);
		search_button.setStyleName("default");
		filter_hl.addComponent(search_button);
	}

	/**
	 * 创建黑名单显示表格
	 */
	private void createBlacklistTable() {
		blacklist_table = createFormatColumnTable();
		blacklist_table.setWidth("100%");
		blacklist_table.setHeight("-1px");
		blacklist_table.setImmediate(true);
		blacklist_table.addListener(this);
		blacklist_table.setSelectable(true);
		blacklist_table.setStyleName("striped");
		blacklist_table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		blacklist_table.addGeneratedColumn("reason", new ReasonColumnGenerator());
		blacklist_table.setColumnExpandRatio("reason", 1.0f);
		blacklist_table.setColumnWidth("phoneNumber", 100);
		this.addComponent(blacklist_table);

		// 为表格添加右键单击事件
		addActionToTable(blacklist_table);
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
				} else if(property.getType() == Date.class) {
					return dateFormat.format((Date)property.getValue());
				} else if("type".equals(colId)) {
					return BlackListItem.TYPE_INCOMING.equals(property.getValue()) ? "呼入" : "呼出";
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 * 创建翻页组件
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

		viewAndEditOutline2Blacklist_bt = new Button("按外线设定黑名单", this);
		viewAndEditOutline2Blacklist_bt.setStyleName("default");
		operator_hl.addComponent(viewAndEditOutline2Blacklist_bt);
		
		add_bt = new Button("添加", this);
		operator_hl.addComponent(add_bt);
		
		btnMutiAdd = new Button("批量添加", this);
		operator_hl.addComponent(btnMutiAdd);
		
		edit_bt = new Button("编辑", this);
		edit_bt.setEnabled(false);
		operator_hl.addComponent(edit_bt);
		
		delete_bt = new Button("删除", this);
		delete_bt.setEnabled(false);
		operator_hl.addComponent(delete_bt);
		
		String countSql = "select count(bi) from BlackListItem as bi where bi.domain.id = " +domain.getId();
		String searchSql = countSql.replaceFirst("count\\(bi\\)", "bi") + " order by bi.createTime desc";
		
		blacklistItemFlip = new FlipOverTableComponent<BlackListItem>(BlackListItem.class, 
				blackListItemService, blacklist_table, searchSql , countSql, null);
		
		blacklistItemFlip.setSearchSql(searchSql);
		blacklistItemFlip.setCountSql(countSql);
		blacklistItemFlip.setPageLength(16, false);
		
		blacklist_table.setVisibleColumns(VISIBLE_PROPERTIES);
		blacklist_table.setColumnHeaders(COL_HEADERS);
		blacklist_table.setPageLength(16);
		
		bottom_hl.addComponent(blacklistItemFlip);
		bottom_hl.setComponentAlignment(blacklistItemFlip, Alignment.TOP_RIGHT);
	}

	/**
	 * 自动创建表格中黑名单内容显示组件
	 */
	private class ReasonColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			BlackListItem blacklist = (BlackListItem) itemId;
			if(columnId.equals("reason")) {
				String content = blacklist.getReason();
				if(content == null) {
					return null;
				} else if(!"".equals(content.trim())) {
					Label reasonLabel = new Label();
					reasonLabel.setWidth("-1px");
					String trimedContent = content.trim();
					reasonLabel.setDescription(trimedContent);
					reasonLabel.setValue(trimedContent);
					int len = (screenResolution[0] / 25) +1;
					if(trimedContent.length() > len) {
						reasonLabel.setValue(trimedContent.substring(0, len) + "...");
					} 
					return reasonLabel;
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
				if(action == ADD) {
					add_bt.click();
				} else if(action == EDIT) {
					edit_bt.click();
				} else if(action == DELETE) {
					delete_bt.click();
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {ADD, EDIT, DELETE};
				} 
				return new Action[] {ADD};
			}
		});
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == blacklist_table) {
			BlackListItem blacklistItem = (BlackListItem) blacklist_table.getValue();
			edit_bt.setEnabled(blacklistItem != null);
			delete_bt.setEnabled(blacklistItem != null);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_button) {
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 查询黑名单出现异常-->"+e.getMessage(), e);
			}
		} else if(source == viewAndEditOutline2Blacklist_bt) {
			showOutline2BlacklistWindow();
		} else if(source == add_bt) {
			showBlacklistItemWindow(true, false);
		} else if(source == btnMutiAdd){	// TODO 批量添加黑名单
			showBlacklistItemWindow(true, true);
		} else if(source == edit_bt) {
			showBlacklistItemWindow(false, false);
		} else if(source == delete_bt) {
			try {
				BlackListItem blacklistItem = (BlackListItem) blacklist_table.getValue();
				String phoneNo = blacklistItem.getPhoneNumber();		// 要删除的黑名单对应的电话号码
				String warningMsg = "<b><font color='red'>您确定要删除当前黑名单号码：<font color='blue'>["+phoneNo+"]</font></font></b>";
				confirmWindow = new ConfirmedWindow(warningMsg, "400px", this, "executeDelete");
				this.getApplication().getMainWindow().removeWindow(confirmWindow);
				this.getApplication().getMainWindow().addWindow(confirmWindow);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else if(source == clear_button) {
			phoneNo_tf.setValue("");
			type_cb.setValue("all");
		}
	}
	
	/**
	 * 显示"按外线设定黑名单"窗口
	 */
	private void showOutline2BlacklistWindow() {
		if(outline2Blacklist == null) {
			outline2Blacklist = new Outline2BlacklistWindow();
		}
		this.getApplication().getMainWindow().removeWindow(outline2Blacklist);
		outline2Blacklist.updateTable();
		this.getApplication().getMainWindow().addWindow(outline2Blacklist);
	}
	
	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		String phoneNo = StringUtils.trimToEmpty((String) phoneNo_tf.getValue());
		if(!phoneNo_tf.isValid()) {
			warning_notification.setCaption("号码不能为空，并且只能由1-12位的数字组成");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		StringBuffer dynamicCountSql = new StringBuffer();
		dynamicCountSql.append("select count(bi) from BlackListItem as bi where bi.domain.id = ");
		dynamicCountSql.append(domain.getId().toString());
		
		if(!"".equals(phoneNo)) {
			dynamicCountSql.append(" and bi.phoneNumber like '%");
			dynamicCountSql.append(phoneNo);
			dynamicCountSql.append("%'");
		}
	
		String type = (String) type_cb.getValue();
		if(!"all".equals(type)) {
			dynamicCountSql.append(" and bi.type = '");
			dynamicCountSql.append(type);
			dynamicCountSql.append("'");
		}
		
		String countSql = dynamicCountSql.toString();
		String searchSql = countSql.replaceFirst("count\\(bi\\)", "bi") + " order by bi.createTime desc";
		
		blacklistItemFlip.setSearchSql(searchSql);
		blacklistItemFlip.setCountSql(countSql);
		blacklistItemFlip.refreshToFirstPage();
	}

	/**
	 * 添加新的黑名单或编辑黑名单
	 * @param isAddNewBlacklist	是否为添加操作
	 */
	private void showBlacklistItemWindow(boolean isAddNewBlacklist, boolean isMutilAdd) {
		if(blacklistItemWindow == null) {
			blacklistItemWindow = new BlacklistItemWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(blacklistItemWindow);
		BlackListItem blacklistItem = (BlackListItem) blacklist_table.getValue();
		blacklistItemWindow.setCaption("编辑黑名单");
		if(isAddNewBlacklist) {
			blacklistItem = new BlackListItem();
			blacklistItem.setDomain(domain);
			blacklistItem.setType("alldirection");
			blacklistItemWindow.setCaption("添加黑名单");
		}
		if(isMutilAdd){
			blacklistItemWindow.setCaption("批量添加黑名单");
		}
		blacklistItemWindow.updateDataSource(blacklistItem, isAddNewBlacklist, isMutilAdd);
		this.getApplication().getMainWindow().addWindow(blacklistItemWindow);
	}

	/**
	 * 执行删除
	 */
	public void executeDelete() {
		try {
			BlackListItem blacklistItem = (BlackListItem) blacklist_table.getValue();
			String phoneNo = blacklistItem.getPhoneNumber();		// 要删除的黑名单对应的电话号码
			blackListItemService.deleteById(blacklistItem.getId());
			
			// 更新内存中的黑名单信息
			if( (blacklistItem.getType()).equals(BlackListItem.TYPE_INCOMING) ) {
				ShareData.domainToIncomingBlacklist.get(domain.getId()).remove(phoneNo);	// 从域范围内移除

				for(Long outlineId1 : ShareData.outlineToIncomingBlacklist.keySet()) {		// 从外线范围内移除
					List<String> incomingBlacklist = ShareData.outlineToIncomingBlacklist.get(outlineId1);
					if(incomingBlacklist != null && incomingBlacklist.contains(phoneNo)) {
						incomingBlacklist.remove(phoneNo);
					}
				}
			} else {
				ShareData.domainToOutgoingBlacklist.get(domain.getId()).remove(phoneNo);	// 从域范围内移除
				
				for(Long outlineId1 : ShareData.outlineToOutgoingBlacklist.keySet()) {		// 从外线范围内移除
					List<String> incomingBlacklist = ShareData.outlineToOutgoingBlacklist.get(outlineId1);
					if(incomingBlacklist != null && incomingBlacklist.contains(phoneNo)) {
						incomingBlacklist.remove(phoneNo);
					}
				}
			}
			
			blacklistItemFlip.refreshInCurrentPage();
		} catch (Exception e) {
			logger.error("删除黑名单失败！---》"+e.getMessage(), e);
			warning_notification.setCaption("删除失败，稍后请重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		blacklist_table.setValue(null);
		success_notification.setCaption("删除黑名单成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
	}

	/**
	 * 刷新黑名单显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			blacklistItemFlip.refreshToFirstPage();
		} else {
			blacklistItemFlip.refreshInCurrentPage();
		}
	}
	
}
