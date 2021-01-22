package com.jiangyifen.ec2.ui.csr.workarea.sms;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 话务员的短信模板界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CsrMessageTemplateView extends VerticalLayout implements ClickListener, ValueChangeListener {
	
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"title", "user", "content"};
	
	private final String[] COL_HEADERS = new String[] {"标题", "创建者", "内容"};
	
	private final Action ADD = new Action("添 加");					// 右键单击事件
	private final Action EDIT = new Action("编 辑");
	private final Action DELETE = new Action("删 除");

	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息
	
	private TextField titleSearch_tf;								// 标题收索框
	private Button search_button;									// 搜索按钮
	private Table messageTemplate_table;							// 短信模板显示表格
	
	private Button add_button;										// 添加按钮
	private Button edit_button;										// 编辑按钮
	private Button delete_button;									// 删除按钮
	
	private FlipOverTableComponent<MessageTemplate> messageTemplateFlip; 	// 翻页组件
	
	private MessageTemplateEditorWindow templateEditorWindow;		// 模板信息添加、编辑窗口
	
	private User loginUser;											// 当前登陆用户
	private Domain domain;											// 当前登陆用户所属域
	private Integer[] screenResolution;								// 屏幕分辨率
	private MessageTemplateService messageTemplateService;			// 模板短信服务类
	
	public CsrMessageTemplateView() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		messageTemplateService = SpringContextHolder.getBean("messageTemplateService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		// 创建并添加搜索组件
		createSearchComponents();
		
		// 创建并添加模板显示组件
		createTemplateViewComponents();

		// 创建客服记录表格的翻页组件
		createTableFlipComponent();

		// 根据分辨率设置表格行数
		setTablePageLength();
	}
	
	/**
	 * 创建并添加搜索组件
	 */
	private void createSearchComponents() {
		HorizontalLayout search_hl = new HorizontalLayout();
		search_hl.setSpacing(true);
		this.addComponent(search_hl);
		
		Label caption = new Label("<B>按标题搜索：</B>", Label.CONTENT_XHTML);
		caption.setWidth("-1px");
		search_hl.addComponent(caption);
		search_hl.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
		
		titleSearch_tf = new TextField();
		titleSearch_tf.setStyleName("search");
		titleSearch_tf.setInputPrompt("请输入标题内容");
		titleSearch_tf.setNullRepresentation("");
		titleSearch_tf.setWidth("200px");
		search_hl.addComponent(titleSearch_tf);
		search_hl.setComponentAlignment(titleSearch_tf, Alignment.MIDDLE_LEFT);
		
		search_button = new Button("查 询", this);
		search_button.setImmediate(true);
		search_hl.addComponent(search_button);
	}

	/**
	 *  创建并添加模板显示组件
	 */
	private void createTemplateViewComponents() {
		messageTemplate_table = createFormatColumnTable();
		messageTemplate_table.setWidth("100%");
		messageTemplate_table.setImmediate(true);
		messageTemplate_table.addListener(this);
		messageTemplate_table.setSelectable(true);
		messageTemplate_table.setStyleName("striped");
		messageTemplate_table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		messageTemplate_table.setColumnWidth("title", -1);
		messageTemplate_table.setColumnExpandRatio("content", 1.0f);
		messageTemplate_table.addGeneratedColumn("content", new ContentColumnGenerator());
		this.addComponent(messageTemplate_table);

		// 为表格添加右键单击事件
		addActionToTable(messageTemplate_table);
	}

	/**
	 *  创建格式化 了 营销任务完成状态列和营销任务接通状态列 的 Table对象
	 */
	private Table createFormatColumnTable() {
		Table table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if("user".equals(colId)) {
					User creator = (User) property.getValue();
					if(creator.getRealName() == null) {
						return creator.getUsername();
					}
					return creator.getRealName()+""+creator.getUsername();
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		return table;
	}
	
	// 创建翻页组件
	private void createTableFlipComponent() {
		HorizontalLayout bottom_hl = new HorizontalLayout();
		bottom_hl.setWidth("100%");
		bottom_hl.setSpacing(true);
		this.addComponent(bottom_hl);
		
		// 操作按钮组件
		bottom_hl.addComponent(createOperatorButtons());

		// 坐席能看到自己创建的模板和其所属域下所有管理员创建的模板
		String countSql = "select count(mt) from MessageTemplate as mt where mt.user.id = "+loginUser.getId()+" or (mt.domain.id="+loginUser.getDomain().getId()+" and mt.type=com.jiangyifen.ec2.entity.enumtype.MessageTemplateType.system)";
		String searchSql = countSql.replaceFirst("count\\(mt\\)", "mt")+" order by mt.id desc";
		
		messageTemplateFlip = new FlipOverTableComponent<MessageTemplate>(MessageTemplate.class, 
				messageTemplateService, messageTemplate_table, searchSql , countSql, null);
		
		messageTemplateFlip.setSearchSql(searchSql);
		messageTemplateFlip.setCountSql(countSql);
		
		messageTemplate_table.setVisibleColumns(VISIBLE_PROPERTIES);
		messageTemplate_table.setColumnHeaders(COL_HEADERS);
		
		bottom_hl.addComponent(messageTemplateFlip);
		bottom_hl.setComponentAlignment(messageTemplateFlip, Alignment.TOP_RIGHT);
	}

	/**
	 *  创建操作按钮
	 */
	private HorizontalLayout createOperatorButtons() {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
	
		add_button = new Button("添 加", this);
		operator_l.addComponent(add_button);
		
		edit_button = new Button("编 辑", this);
		edit_button.setEnabled(false);
		operator_l.addComponent(edit_button);
		
		delete_button = new Button("删 除", this);
		delete_button.setEnabled(false);
		operator_l.addComponent(delete_button);
		
		return operator_l;
	}

	/**
	 * 自动创建表格中短信内容显示组件
	 */
	private class ContentColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			MessageTemplate template = (MessageTemplate) itemId;
			if(columnId.equals("content")) {
				String content = template.getContent();
				if(content == null) {
					return null;
				} else if(!"".equals(content.trim())) {
					Label contentLabel = new Label();
					contentLabel.setWidth("-1px");
					String trimedContent = content.trim();
					contentLabel.setDescription(trimedContent);
					contentLabel.setValue(trimedContent);
					int len = (screenResolution[0] / 21) +1;
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
				if(action == ADD) {
					add_button.click();
				} else if(action == EDIT) {
					edit_button.click();
				} else if(action == DELETE) {
					delete_button.click();
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
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			messageTemplate_table.setPageLength(35);
			messageTemplateFlip.setPageLength(35, false);
		} else if(screenResolution[1] >= 1050) {
			messageTemplate_table.setPageLength(33);
			messageTemplateFlip.setPageLength(33, false);
		} else if(screenResolution[1] >= 900) {
			messageTemplate_table.setPageLength(26);
			messageTemplateFlip.setPageLength(26, false);
		} else if(screenResolution[1] >= 768) {
			messageTemplate_table.setPageLength(19);
			messageTemplateFlip.setPageLength(19, false);
		} else {
			messageTemplate_table.setPageLength(14);
			messageTemplateFlip.setPageLength(14, false);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == messageTemplate_table) {
			MessageTemplate template = (MessageTemplate) messageTemplate_table.getValue();
			edit_button.setEnabled(template != null);
			delete_button.setEnabled(template != null);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_button) {
			executeSearch();
		} else if(source == add_button) {
			showTemplateEditorWindow(true);
		} else if(source == edit_button) {
			MessageTemplate messageTemplate = (MessageTemplate) messageTemplate_table.getValue();
			if(!messageTemplate.getUser().getId().equals(loginUser.getId()) && MessageTemplateType.system.equals(messageTemplate.getType())) {
				this.getApplication().getMainWindow().showNotification("对不起，该模板是管理员所建，您无权修改！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			showTemplateEditorWindow(false);
		} else if(source == delete_button) {
			executeDelete();
		} 
	}
	
	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		String title = StringUtils.trimToEmpty((String) titleSearch_tf.getValue());
		StringBuffer dynamicSql = new StringBuffer();
		dynamicSql.append("select count(mt) from MessageTemplate as mt where (mt.user.id = ");
		dynamicSql.append(loginUser.getId().toString());
		if(!"".equals(title)) {
			dynamicSql.append(" and mt.title like '%");
			dynamicSql.append(title);
			dynamicSql.append("%'");
			dynamicSql.append(") or (mt.domain.id="+domain.getId()+" and mt.type=com.jiangyifen.ec2.entity.enumtype.MessageTemplateType.system and mt.title like '%"+title+"%')");
		} else {
			dynamicSql.append(") or (mt.domain.id="+domain.getId()+" and mt.type=com.jiangyifen.ec2.entity.enumtype.MessageTemplateType.system)");
		}
		
		String countSql = dynamicSql.toString();
		String searchSql = countSql.replaceFirst("count\\(mt\\)", "mt")+" order by mt.id desc";	
		
		messageTemplateFlip.setSearchSql(searchSql);
		messageTemplateFlip.setCountSql(countSql);
		messageTemplateFlip.refreshToFirstPage();
	}
	
	/**
	 * 执行删除操作
	 */
	private void executeDelete() {
		MessageTemplate messageTemplate = (MessageTemplate) messageTemplate_table.getValue();
		if(!messageTemplate.getUser().getId().equals(loginUser.getId()) && MessageTemplateType.system.equals(messageTemplate.getType())) {
			this.getApplication().getMainWindow().showNotification("对不起，该模板是管理员所建，您无权删除！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		try {
			messageTemplateService.delete(messageTemplate);
		} catch (Exception e) {
			logger.error("话务员删除信息模板出现异常---> "+e.getMessage(), e);
			warning_notification.setCaption("删除失败，稍后请重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		// 刷新界面
		messageTemplateFlip.refreshInCurrentPage();
		success_notification.setCaption("删除短息模板成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
	}

	/**
	 * 显示短信样板编辑器，如果是添加样板，则传递一个新建的对象过去，如果是编辑短信，则传递表格中的选中值
	 * @param isAdd
	 */
	private void showTemplateEditorWindow(boolean isAdd) {
		if(templateEditorWindow == null) {
			templateEditorWindow = new MessageTemplateEditorWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(templateEditorWindow);
		MessageTemplate messageTemplate = (MessageTemplate) messageTemplate_table.getValue();
		templateEditorWindow.setCaption("编辑信息模板");
		if(isAdd) {
			messageTemplate = new MessageTemplate();
			messageTemplate.setType(MessageTemplateType.csr);
			messageTemplate.setUser(loginUser);
			messageTemplate.setDomain(domain);
			templateEditorWindow.setCaption("添加信息模板");
		}
		templateEditorWindow.updateFormDataSource(messageTemplate, isAdd);
		this.getApplication().getMainWindow().addWindow(templateEditorWindow);
	}

	/**
	 * 刷新我的客服记录显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			messageTemplateFlip.refreshToFirstPage();
		} else {
			messageTemplateFlip.refreshInCurrentPage();
		}
	}
	
}
