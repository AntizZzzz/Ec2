package com.jiangyifen.ec2.ui.csr.toolbar;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.NoticeItemService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
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
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.VerticalLayout;

/**
 * 话务员的通知信息管理界面
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class NoticeView extends VerticalLayout implements ClickListener, ValueChangeListener {
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"notice.sender", "notice.title", "notice.content", "notice.sendDate"};
	private final String[] COL_HEADERS = new String[] {"通知人", "通知主题", "通知内容", "通知日期"};
	private final Action DELETE = new Action("删除", ResourceDataCsr.delete_16_ico);
	
	private Label titleLabel;				// 主题标签
	private Label senderLabel;				// 发送者标签
	private Label receiverLabel;			// 接收者标签
	private Label timeLabel;				// 接收时间标签
	private Label contentLabel;				// 内容显示区
	private Panel contentPanel; 			// 内容显示面板
	private Table noticeTable;				// 通知显示表格
	private Button delete;					// 删除按钮
	private HorizontalLayout footerHLayout;	// 存放delete按钮的水平布局管理器
	
	private User loginUser;					// 当前登录用户
	private Button myNotice;				// 我的通知
	private NoticeItem currentNoticeItem;	// 被选中的通知项
	private NoticeItemService noticeItemService;
	private FlipOverTableComponent<NoticeItem> tableFlipOver;	// 为Table添加的翻页组件
	
	public NoticeView() {
		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);
		loginUser = SpringContextHolder.getLoginUser();
		noticeItemService = (NoticeItemService) SpringContextHolder.getBean("noticeItemService");
		
		noticeTable = createFormatColumnTable();
		noticeTable.setColumnReorderingAllowed(true);
		noticeTable.setWidth("100%");
		noticeTable.setHeight("-1px");
		noticeTable.setImmediate(true);
		noticeTable.setSelectable(true);
		noticeTable.setStyleName("striped");
		noticeTable.setColumnWidth("notice.title", 200);
		noticeTable.setColumnExpandRatio("notice.content", 1f);
		noticeTable.setColumnAlignment("type", Table.ALIGN_CENTER);
		noticeTable.addListener((ValueChangeListener) this);
		noticeTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(noticeTable);
		
		footerHLayout = new HorizontalLayout();
		footerHLayout.setWidth("100%");
		this.addComponent(footerHLayout);

		delete = new Button("删 除", (ClickListener)this);
		delete.setEnabled(false);
		footerHLayout.addComponent(delete);
		
		createTableFlipOver();
		setCellStyleToTable(noticeTable);
		addActionToNoticeTable();
		createContentPanel();
	}

	private void createTableFlipOver() {
		String countSql = "select count(ni) from NoticeItem as ni where ni.user.id = " + loginUser.getId();
		String searchSql = countSql.replaceFirst("count\\(ni\\)", "ni") + " order by ni.notice.sendDate desc";
		tableFlipOver = new FlipOverTableComponent<NoticeItem>(NoticeItem.class, 
				noticeItemService, noticeTable, searchSql, countSql, null);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			tableFlipOver.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		noticeTable.setVisibleColumns(VISIBLE_PROPERTIES);
		noticeTable.setColumnHeaders(COL_HEADERS);
		noticeTable.setPageLength(8);
		tableFlipOver.setPageLength(8, false);
		footerHLayout.addComponent(tableFlipOver);
		footerHLayout.setComponentAlignment(tableFlipOver, Alignment.MIDDLE_RIGHT);
	}

	/**
	 *  创建格式化 了 日期列和通知内容列 的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if(colId.equals("notice.sendDate")) {
	            		return dateFormat.format((Date)property.getValue());
				} else if(colId.equals("notice.content")) {
	            	String content = property.getValue().toString();
	            	content = content.replaceAll("<.+?>", "");	// 去掉HTML 语句
	            	if(content.length() > 25){
	            		content = content.substring(0, 25) + "...";
	            	}
	            	return content;
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 * 为指定的表格设置CSS 样式
	 * @param table
	 */
	private void setCellStyleToTable(final Table table) {
		table.setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(Object itemId, Object propertyId) {
				if(propertyId != null) {
					Item item = table.getItem(itemId);
					Boolean hasReaded = (Boolean) item.getItemProperty("hasReaded").getValue();
					if(!hasReaded) 
						return "boldcontent";
				}
				return null;
			}
		});
	}
	
	/**
	 * 为NoticeTable 添加右键单击事件
	 */
	private void addActionToNoticeTable() {
		noticeTable.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				noticeTable.select(target);
				if(action == DELETE) {
					delete.click();
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {DELETE};
				}
				return null;
			}
		});
	}
	
	// 创建Panel 组件，存放详细内容
	private void createContentPanel() {
		VerticalLayout headerVLayout = new VerticalLayout();
		headerVLayout.setWidth("100%");
		headerVLayout.setStyleName("noticebackground");
		
		titleLabel = new Label();
		titleLabel.setContentMode(Label.CONTENT_XHTML);
		headerVLayout.addComponent(titleLabel);
		
		senderLabel = new Label();
		senderLabel.setContentMode(Label.CONTENT_XHTML);
		headerVLayout.addComponent(senderLabel);
		
		receiverLabel = new Label();
		receiverLabel.setContentMode(Label.CONTENT_XHTML);
		headerVLayout.addComponent(receiverLabel);
		
		timeLabel = new Label();
		timeLabel.setContentMode(Label.CONTENT_XHTML);
		headerVLayout.addComponent(timeLabel);
		
		VerticalLayout contentVLayout = new VerticalLayout();
		contentVLayout.setSpacing(true);
		contentVLayout.setMargin(true, false, false, false);
		
		contentLabel = new Label();
		contentLabel.setContentMode(Label.CONTENT_XHTML);
		contentVLayout.addComponent(contentLabel);
		
		contentPanel = new Panel("通知内容显示区：");
		contentPanel.setSizeFull();
		contentPanel.addComponent(headerVLayout);
		contentPanel.addComponent(contentVLayout);
		this.addComponent(contentPanel);
		this.setExpandRatio(contentPanel, 1);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {	// delete 按钮的监听器
		Button source = event.getButton();
		if(source == delete) {
			NoticeItem noticeItem = (NoticeItem) noticeTable.getValue();
			noticeItemService.deleteById(noticeItem.getId());
			clearNoticePanelContext();	// 清空显示区的内容
			tableFlipOver.refreshInCurrentPage();
			delete.setEnabled(false);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {	// Table 的监听器
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss (E)");
		currentNoticeItem = (NoticeItem) noticeTable.getValue();
		if(currentNoticeItem != null) {
			Notice notice = currentNoticeItem.getNotice();
			titleLabel.setValue("标&nbsp&nbsp 题：<B>" + notice.getTitle() + "</B>");
			
			String senderName = (notice.getSender().getRealName() == null) ? "" : " - "+notice.getSender().getRealName();
			senderLabel.setValue("发件人：" +"<font color='red'>"+ notice.getSender().getUsername()+ senderName +"</font>");
			
			// 编辑收件人显示信息 可以考虑放到service 上去
			StringBuffer receiverStrs = new StringBuffer();
			List<User> receivers = new ArrayList<User>(notice.getReceivers());
			Collections.sort(receivers, new Comparator<User>() {
				@Override
				public int compare(User o1, User o2) {
					int emp1 = Integer.parseInt(o1.getEmpNo());
					int emp2 = Integer.parseInt(o2.getEmpNo());
					return emp1 - emp2;
				}
			});
			
			for(int i = 1; i <= receivers.size(); i++) {
				User receiver = receivers.get(i-1);
				String name = receiver.getRealName();
				if(name == null) {
					receiverStrs.append(receiver.getEmpNo() +",");
				} else {
					receiverStrs.append(receiver.getEmpNo() +"-"+name+",");
				} 
				if(i%8 == 0 && i != receivers.size()) {
					receiverStrs.append("</br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
				}
			}
			String receiverStr = receiverStrs.toString().substring(0, receiverStrs.toString().length()-1);
			receiverLabel.setValue("接收人：<font color='red'>"+receiverStr+"</font>");
			
			timeLabel.setValue("时&nbsp&nbsp 间："+dateFormat.format(notice.getSendDate()));
			contentLabel.setValue(notice.getContent());
			if(currentNoticeItem.isHasReaded() == false) {
				currentNoticeItem.setHasReaded(true);
				noticeItemService.update(currentNoticeItem);
				tableFlipOver.refreshInCurrentPage();
				for(int i = 0; i < noticeTable.getItemIds().size(); i++) {
					NoticeItem item = (NoticeItem) noticeTable.getItemIds().toArray()[i];
					if(item.getId() != null && item.getId().equals(currentNoticeItem.getId())) {
						noticeTable.select(item);
					}
				}
				refreshNoticeButtonCaption();
			}
		} else {
			clearNoticePanelContext();
		}
		delete.setEnabled(currentNoticeItem != null);
	}
	
	/**
	 * 刷新我的通知按钮的显示内容
	 */
	public void refreshNoticeButtonCaption() {
		int unreadNoticesCount = noticeItemService.getEntityCount("select count(ni) from NoticeItem as ni " +
				"where ni.user.id = " + loginUser.getId() + " and ni.hasReaded = false");
		if(unreadNoticesCount > 0) {
			myNotice.setCaption("我的通知 "+"<font color='black' size='4'><B>( "+unreadNoticesCount+" )</B></font>");
		} else {
			myNotice.setCaption("我的通知 ");
		}
	}

	/**
	 * 清空显示区的内容
	 */
	private void clearNoticePanelContext() {
		titleLabel.setValue(null);
		senderLabel.setValue(null);
		receiverLabel.setValue(null);
		timeLabel.setValue(null);
		contentLabel.setValue(null);
	}
	
	public void selectItem() {
		noticeTable.select(noticeTable.firstItemId());
	}
	
	public void refreshNoticeTable() {
		tableFlipOver.refreshInCurrentPage();
	}

	public void setMyNotice(Button myNotice) {
		this.myNotice = myNotice;
	}

	public Table getNoticeTable() {
		return noticeTable;
	}
	
}
