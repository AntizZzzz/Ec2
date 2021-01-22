package com.jiangyifen.ec2.ui.mgr.tabsheet.email;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.email.entity.MailHistory;
import com.jiangyifen.ec2.email.service.MailHistoryService;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class EmailHistoryView  extends VerticalLayout implements Button.ClickListener,Property.ValueChangeListener, Action.Handler  {
	  //历史邮件表格组件
    private Table table;
    private String sqlSelect;
    private String sqlCount;
	private MailHistoryService mailHistoryService;
	private DepartmentService departmentService;
	/*private String allGovernedDeptIdsStr;*/
    private FlipOverTableComponent<MailHistory> flip;
    
    //表格搜索区域和按钮组件
    private TextField keyWord;
    private Button search;
    private Button preView;
    private Button delete;
    
	/**
	 * 右键组件
	 */
	private Action PREVIEW = new Action("查看");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { PREVIEW, DELETE };
	
    /**
     * 弹出窗口
     */
	private User loginUser;
//    private PreView preViewWindow;
//    private EditNotice editNoticeWindow;
    
    /**
	 * 构造器
	 */
	public EmailHistoryView() {
		this.setSizeFull();
		loginUser = SpringContextHolder.getLoginUser();
		
		mailHistoryService = SpringContextHolder.getBean("mailHistoryService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for (Role role : loginUser.getRoles()) {
			if (role.getType().equals(RoleType.manager)) {
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
		
		/*if(allGovernedDeptIds.size() > 0) {
			allGovernedDeptIdsStr = StringUtils.join(allGovernedDeptIds, ",");
		} else {
			allGovernedDeptIdsStr = "";
		}*/
		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		//创建历史消息输出
		VerticalLayout historyLayout = buildHistoryLayout();
		constrantLayout.addComponent(historyLayout);
		constrantLayout.setExpandRatio(historyLayout, 3.0f);
	}
	
	/**
	 * 显示历史发送的消息输出表格
	 * @return
	 */
	private VerticalLayout buildHistoryLayout() {
		VerticalLayout historyLayout=new VerticalLayout();
		historyLayout.setSpacing(true);
		historyLayout.setSizeFull();
		
		//搜索区域的创建
		HorizontalLayout searchLayout=new HorizontalLayout();
		searchLayout.addComponent(new Label("关键字:"));
		keyWord=new TextField();
		keyWord.setWidth("6em");
		keyWord.setInputPrompt("关键字");
		searchLayout.addComponent(keyWord);
		search=new Button("搜索");
		search.setStyleName("small");
		search.addListener((Button.ClickListener)this);
		searchLayout.addComponent(search);
		historyLayout.addComponent(searchLayout);
		//创建表格
		table = createFormatColumnTable();
        table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addListener(this);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		historyLayout.addComponent(table);
		
		//初始化Sql
		search.click();
		
		//翻页组件和操作按钮输出
		flip=new FlipOverTableComponent<MailHistory>(MailHistory.class, mailHistoryService, table, sqlSelect, sqlCount, null);
		flip.getEntityContainer().addNestedContainerProperty("user.username");
		flip.setPageLength(20, false);
		table.setPageLength(20);
		
		//设置表格头部显示
		Object[] visibleColumns=new Object[] {"toAddresses","fromAddress","subject", "sendTimeDate", "user.username", "content"}; 
		String[] columnHeaders=new String[] {"收件人","发送人","发送主题", "发送时间", "发件人", "发送内容"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
//		table.addGeneratedColumn("receivers", new ReceiversColumnGenerator());	// jrh 2013-11-01

		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);

		preView = new Button("查看");
		preView.setEnabled(false); //创建时为不可用
		preView.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(preView);

		delete = new Button("删除");
		delete.setEnabled(false); //创建时为不可用
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);
		
		//下方的按钮输出
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setWidth("100%");
		historyLayout.addComponent(buttonsLayout);
		
		//左侧按钮组件的添加
		buttonsLayout.addComponent(tableButtonsLeft);
		buttonsLayout.setComponentAlignment(tableButtonsLeft, Alignment.BOTTOM_LEFT);
		
		//右侧Flip组件
		buttonsLayout.addComponent(flip);
		buttonsLayout.setComponentAlignment(flip, Alignment.BOTTOM_RIGHT);

		return historyLayout;
	}
	

	/**
	 *  创建格式化 了的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if(colId.equals("sendTimeDate")) {
	            		return dateFormat.format((Date)property.getValue());
				} else if(colId.equals("subject")) {
	            	String title = property.getValue().toString();
	            	if(title.length() > 25){
	            		title = title.substring(0, 25) + "...";
	            	}
	            	return title;
				} else if(colId.equals("content")) {
					String content = property.getValue().toString();
					content = content.replaceAll("<.+?>", "");	// 去掉HTML 语句
					if(content.length() > 25){
						content = content.substring(0, 25) + "...";
					}
					return content;
//				} else if(colId.equals("receivers")) {
//					StringBuffer sb = new StringBuffer();
//            		Set<User> receivers = (Set<User>) property.getValue();
//					return StringUtils.join(receivers, ",");
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	@Override
	public void attach() {
		super.attach();
		updateTable(true);
	}
//	/**
//	 * jrh 用于自动生成管理员发送的通知后，接收者的显示样式，如果接收者尚未读取，则工号显示红色加粗
//	 */
//	private class ReceiversColumnGenerator implements Table.ColumnGenerator {
//		public Object generateCell(Table source, Object itemId, Object columnId) {
//			Notice notice = (Notice) itemId;
//			if (columnId.equals("receivers")) {
//				String receivers_caption = compileCaption(notice);
//				Label receivers_lb = new Label(receivers_caption, Label.CONTENT_XHTML);
//				receivers_lb.setWidth("-1px");
//				return receivers_lb;
//			} 
//			return "";
//		}
//	}

//	/**
//	 * jrh
//	 *  获取接收人信息，处理标签信息，如果通知已经被坐席读取，或者被坐席删除，都认为是坐席已经读取了
//	 *  如果没读，则改变显示信息样式
//	 * @param notice 通知条
//	 * @return
//	 */
//	private String compileCaption(Notice notice) {
//		ArrayList<User> receivers = new ArrayList<User>(notice.getReceivers());
//		Collections.sort(receivers, new Comparator<User>() {
//			@Override
//			public int compare(User u1, User u2) {
//				try {
//					int emp1 = Integer.parseInt(u1.getEmpNo());
//					int emp2 = Integer.parseInt(u2.getEmpNo());
//					return emp1 - emp2;
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//					return 0;
//				}
//			}
//			
//		});
//		
//		StringBuffer strBf = new StringBuffer();
//		for(User receiver : receivers) {
//			NoticeItem item = noticeItemService.getByUserAndNotice(receiver, notice);
//			if(item != null && !item.isHasReaded()) {
//				strBf.append("<font color='red'><b>");
//				strBf.append(receiver.getEmpNo());
//				strBf.append("</b></font>");
//			} else {
//				strBf.append(receiver.getEmpNo());
//			}
//			strBf.append(",");
//		}
//		if(strBf.length() > 0) {
//			strBf.deleteCharAt(strBf.length() - 1);
//		}
//		return strBf.toString();
//	}
	
	/**
	 * 预览消息显示
	 */
	private void showPreviewWindow() {
		MailHistory mailHistory=(MailHistory)table.getValue();
		if(mailHistory!=null){
			this.getWindow().addWindow(new MailHistoryView(mailHistory));
		}else{
			//ignore
		}
	}

	/**
	 * 显示编辑窗口
	 */
	@SuppressWarnings("unused")
	private void showEditWindow() {
//		if(editNoticeWindow==null){
//			editNoticeWindow=new EditNotice(this);
//		}
//		this.getWindow().addWindow(editNoticeWindow);
	}
	

	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		
		// 关键字过滤,标题
		String keyWordStr = keyWord.getValue().toString();
		
		AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
		sqlSelect = "select s from MailHistory as s where s.subject like '%"+keyWordStr+"%' and s.content like '%"+keyWordStr+"%' and s.domain.id = " + loginUser.getDomain().getId() + " and s.user.department.id in ("+achieveBasicUtil.getDeptIds()+") order by s.id desc";
		sqlCount = "select count(s.id) from MailHistory as s where s.subject like '%"+keyWordStr+"%' and s.content like '%"+keyWordStr+"%' and s.domain.id = " + loginUser.getDomain().getId() + " and s.loginUser.department.id in ("+achieveBasicUtil.getDeptIds()+")";
		//更新Table，并使Table处于未选中
		this.updateTable(true);
		if(table!=null){
			table.setValue(null);
		}
		
		/*SqlGenerator sqlGenerator = new SqlGenerator("MailHistory");
		// 关键字过滤,标题
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like subjectName = new SqlGenerator.Like("subject", keyWordStr);
		sqlGenerator.addOrCondition(subjectName);
		// sqlSelect = "select s from MailHistory as s where s.subject like '%"+keyWordStr+"%' and s.content like '%"+keyWordStr+"%' and s.domain.id = " + loginUser.getDomain().getId() + " order by s.id desc";
		
		//内容过滤
		SqlGenerator.Like contentValue = new SqlGenerator.Like("content", keyWordStr);
		sqlGenerator.addOrCondition(contentValue);
		
		// jrh 只能查看自己发送的消息
		SqlGenerator.Equal equal = new SqlGenerator.Equal("user.id", loginUser.getId().toString(), false);
		sqlGenerator.addAndCondition(equal);
		sqlGenerator.setOrderBy("id",SqlGenerator.DESC); //排序
		
		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		sqlCount = sqlGenerator.generateCountSql();

//		// jrh 让管理员能看到自己管辖部门下任何人发的消息
//		String deptSql = " and e.sender.department.id in (" + allGovernedDeptIdsStr+ ")";
//		sqlSelect += deptSql + " order by e.id desc";
//		sqlCount += deptSql;
		
		//更新Table，并使Table处于未选中
		this.updateTable(true);
		if(table!=null){
			table.setValue(null);
		}*/
	}

	/**
	 * 由executeSearch调用更新表格内容
	 * @param isToFirst 是否更新到第一页，default 是 false
	 * @param isToFirst
	 */
	public void updateTable(Boolean isToFirst) {
		if(flip!=null){
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if(isToFirst){
				flip.refreshToFirstPage();
			}else{
				flip.refreshInCurrentPage();
			}
		}
	}

	/**
	 * 确认删除
	 * @param isDelete
	 */
	public void confirmDelete(Boolean isConfirmed){
		if(isConfirmed){
			MailHistory mailHistory=(MailHistory)(table.getValue());
			mailHistoryService.deleteById(mailHistory.getId());
			this.updateTable(false);
			table.setValue(null);
		}
	}
	/**
	 * 获取表格
	 * @return
	 */
	public Table getTable() {
		return table;
	}
	
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[] {};
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (PREVIEW == action) {
			preView.click();
		} else if (DELETE == action) {
			delete.click();
		}
	}

	/**
	 * 添加Csr按钮单击事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton().equals(search)){
			executeSearch();
		}else if(event.getButton().equals(preView)){
			showPreviewWindow();
		}else if(event.getButton().equals(delete)){
			//由于能选中Delete则table.getValue()一定不为null
			MailHistory mailHistory=(MailHistory)table.getValue();
			Label label=new Label("您确定要删除邮件<b>"+mailHistory.getSubject()+"</b>?",Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow=new ConfirmWindow(label,this,"confirmDelete");
			event.getButton().getWindow().addWindow(confirmWindow);
		}
	}
	
	/**
	 * 表格条目选择改变时的事件
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		if(table.getValue()==null){
			preView.setEnabled(false);
			delete.setEnabled(false);
		}else{                
			preView.setEnabled(true);
			delete.setEnabled(true);
		}
	}
	
	public void update(){
		updateTable(true);
	}

}


@SuppressWarnings("serial")
class MailHistoryView extends Window implements Button.ClickListener{
/**
 * 主要组件输出	
 */
	private Label receiverLabel;
	private Label contentLabel;
	private Button close;
	/**
	 *其它组件 
	 */
	private MailHistory mailHistory;
	
	public MailHistoryView(MailHistory mailHistory) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.mailHistory=mailHistory;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		//创建内容部分
		windowContent.addComponent(buildContentArea());
		
		close = new Button("关闭", this);
		close.setImmediate(true);
		windowContent.addComponent(close);
	}
	
	/**
	 * 创建内容输出区域
	 * @return
	 */
	private Panel buildContentArea() {
		Panel panel=new Panel();
		panel.setWidth("650px");
		panel.setHeight("330px");
		panel.setScrollable(true);
		receiverLabel = new Label("收件人：", Label.CONTENT_XHTML);
		receiverLabel.setWidth("-1px");
		panel.addComponent(receiverLabel);
		panel.addComponent(new Label("<b>邮件内容:</b>",Label.CONTENT_XHTML));
		contentLabel=new Label("",Label.CONTENT_XHTML);
		panel.addComponent(contentLabel);
		return panel;
	}
	/**
	 * 每当Attach时触发加载Table选中条目数据
	 */
	@Override
	public void attach() {
		super.attach();
		String receiverStr = mailHistory.getToAddresses();
		receiverLabel.setValue("<B>接收人：</B><font color='red'>"+receiverStr+"</font>");
		this.setCaption(mailHistory.getSubject());
		contentLabel.setValue(mailHistory.getContent());
	}
	/**
	 * 点击关闭按钮时关闭窗口
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==close){
			this.getParent().removeWindow(this);
		}
	}
}