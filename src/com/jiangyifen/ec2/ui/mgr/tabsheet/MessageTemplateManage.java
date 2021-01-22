package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.List;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.enumtype.MessageTemplateType;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.ui.mgr.messagetemplatemanage.AddMessageWindow;
import com.jiangyifen.ec2.ui.mgr.messagetemplatemanage.EditMessageWindow;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class MessageTemplateManage extends VerticalLayout implements
		ClickListener, ValueChangeListener {

	private static final Object[] MESSAGE_COL_ORDER = { "user","title", "content",
			"type" };
	private static final String[] MESSAGE_COL_HEADER = { "创建者","模板标题", "短信内容", "短信类型" };

	// 搜索组件
	private ComboBox typeBox; // 按类型查找
	private Button search;

	// 表格组件
	private Table table;

	// 按钮组件
	private Button add;
	private Button delete;
	private Button edit;

	// 当前选中的短信
	private MessageTemplate selectMessage;
	// 弹出窗口
	private AddMessageWindow addWindow;
	private EditMessageWindow editWindow;

	private BeanItemContainer<MessageTemplate> container;

	private MessageTemplateService messageTemplateService;

	public MessageTemplateManage() {
		messageTemplateService = SpringContextHolder.getBean("messageTemplateService");
		this.setSizeFull();
		this.setMargin(true);

		// 设置最大的layout
		VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setSpacing(true);
		this.addComponent(contentLayout);
		// 添加搜索组件
		contentLayout.addComponent(buildSearchLayout());
		// 添加表格和按钮组件
		contentLayout.addComponent(buildTableAndButtonsLayout());
		//初始化container
		this.getSearch().click();

	}

	// 创建搜索组件
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
//		searchLayout.setWidth("100%");

//		HorizontalLayout domainLayout = new HorizontalLayout();

		HorizontalLayout typeLayout = new HorizontalLayout();
		Label type = new Label("按类型查找:");
		typeBox = new ComboBox();
		typeBox.addItem(MessageTemplateType.csr);
		typeBox.addItem(MessageTemplateType.system);
//		typeBox.setValue(MessageType.system);// 默认系统类型
		typeLayout.addComponent(type);
		typeLayout.addComponent(typeBox);
		searchLayout.addComponent(typeLayout);

		search = new Button("查询");
		search.addListener((ClickListener) this);
		searchLayout.addComponent(search);
		return searchLayout;
	}

	// 创建表格和按钮组件
	private VerticalLayout buildTableAndButtonsLayout() {
		VerticalLayout tableAndButtonsLayout = new VerticalLayout();
		tableAndButtonsLayout.setSpacing(true);

		table = new Table();
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addListener((Property.ValueChangeListener) this);

		table.addGeneratedColumn("content", new Table.ColumnGenerator() {

			@Override
			public Object generateCell(Table source, Object itemId,
					Object columnId) {
				MessageTemplate message = (MessageTemplate) itemId;
				String value = "";
				if (message.getContent().length() < 10) {
					value = message.getContent();
				} else {
					value = message.getContent().substring(0, 9) + "......";
				}
				Label content = new Label();
				content.setValue(value);
				content.setDescription(message.getContent());
				return content;
			}
		});

		tableAndButtonsLayout.addComponent(table);	
		
		// 创建按钮
		tableAndButtonsLayout.addComponent(buildButtons());

		return tableAndButtonsLayout;
	}

	// 创建按钮组件
	private HorizontalLayout buildButtons() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);

		add = new Button("新建");
		add.addListener((ClickListener) this);
		buttonsLayout.addComponent(add);

		edit = new Button("编辑");
		edit.addListener((ClickListener) this);
		edit.setEnabled(false);
		buttonsLayout.addComponent(edit);

		delete = new Button("删除");
		delete.addListener((ClickListener) this);
		delete.setEnabled(false);
		buttonsLayout.addComponent(delete);

		return buttonsLayout;
	}

	// 弹出添加窗口
	private void showAddWindow() {
		if (addWindow == null) {
			addWindow = new AddMessageWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(addWindow);
		this.getApplication().getMainWindow().addWindow(addWindow);

	}

	// 弹出编辑窗口
	private void showEditWindow() {
		if (editWindow == null) {
			editWindow = new EditMessageWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(editWindow);
		this.getApplication().getMainWindow().addWindow(editWindow);

	}

	public void updateTable() {
		List<MessageTemplate> messages = messageTemplateService.getAllByDomain(SpringContextHolder.getDomain());
		container.removeAllItems();
		container.addAll(messages);
		// 刷新表格行在内存中的信息
		table.refreshRowCache();
	}

	// 执行删除
	private void executeDelete() {
		Label label = new Label("您确定要删除吗?", Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmDelete");
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);

	}

	// 执行搜索
	private void executeSearch() {
		MessageTemplateType type = (MessageTemplateType) typeBox.getValue();
		container = new BeanItemContainer<MessageTemplate>(MessageTemplate.class);
		if (typeBox.getValue() != null) {
			List<MessageTemplate> messages = messageTemplateService.getMessagesByType(type);
			if (messages != null) {
				for (MessageTemplate m : messages) {
					container.addItem(m);
				}
			}
		} else {
			List<MessageTemplate> messages = messageTemplateService.getAllByDomain(SpringContextHolder.getDomain());
			container.addAll(messages);
		}
		table.setContainerDataSource(container);
		table.setVisibleColumns(MESSAGE_COL_ORDER);
		table.setColumnHeaders(MESSAGE_COL_HEADER);
	}

	// 确认删除窗口
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed) {
			messageTemplateService.delete(selectMessage);
			table.setValue(null);
			this.updateTable();
			this.getApplication().getMainWindow().showNotification("删除成功！");
		}
	}

	// 表格valueChange事件
	@Override
	public void valueChange(ValueChangeEvent event) {
		this.selectMessage = (MessageTemplate) table.getValue();

		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			edit.setEnabled(true);
			delete.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			edit.setEnabled(false);
			delete.setEnabled(false);
		}

	}

	// 按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == add) {
			showAddWindow();
		} else if (event.getButton() == edit) {
			showEditWindow();
		} else if (event.getButton() == delete) {
			executeDelete();
		} else if (event.getButton() == search) {
			executeSearch();
		}

	}

	// *****************setter和getter*******************//
	public MessageTemplate getSelectMessage() {
		return selectMessage;
	}

	public void setSelectMessage(MessageTemplate selectMessage) {
		this.selectMessage = selectMessage;
	}

	public Table getTable() {
		return table;
	}

	public Button getSearch() {
		return search;
	}

	public void setSearch(Button search) {
		this.search = search;
	}
	

}
