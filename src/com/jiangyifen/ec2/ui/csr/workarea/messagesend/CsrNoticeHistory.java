package com.jiangyifen.ec2.ui.csr.workarea.messagesend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.NoticeItem;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.NoticeItemService;
import com.jiangyifen.ec2.service.eaoservice.NoticeService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 内部消息发送历史
 * @author lxy
 *
 */
public class CsrNoticeHistory  extends VerticalLayout implements Button.ClickListener,Property.ValueChangeListener, Action.Handler  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5310345673663968652L;
	
	//历史消息表格组件
    private Table table;
    private String sqlSelect;
    private String sqlCount;
    private NoticeService noticeService;
	private NoticeItemService noticeItemService;
	private DepartmentService departmentService;
	//private String allGovernedDeptIdsStr;
    private FlipOverTableComponent<Notice> flip;
    
    //表格搜索区域和按钮组件
    private TextField keyWord;
    private Button search;
    private Button writeMessage;
    private Button preView;
    private Button edit;
    private Button delete;
    
	/**
	 * 右键组件
	 */
	private Action ADD = new Action("添加");
	private Action PREVIEW = new Action("预览");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { ADD, PREVIEW, EDIT, DELETE };
	
    /**
     * 弹出窗口
     */
	private User loginUser;
    private CsrPreView preViewWindow;
    private CsrEditNotice editNoticeWindow;
    
    /**
	 *  其它组件   
	 */
    //TODO 后添加private MgrTabSheet mgrTabSheet;
    
    /**
	 * 构造器
	 */
    //TODO 后添加public CsrNoticeHistory(MgrTabSheet mgrTabSheet) {
    public CsrNoticeHistory() {
		this.setSizeFull();
		//TODO 后添加this.mgrTabSheet=mgrTabSheet;
		loginUser = SpringContextHolder.getLoginUser();
		
		noticeItemService = SpringContextHolder.getBean("noticeItemService");
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
		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		//创建历史消息输出
		VerticalLayout historyNoticeLayout = buildHistoryNoticeLayout();
		constrantLayout.addComponent(historyNoticeLayout);
		constrantLayout.setExpandRatio(historyNoticeLayout, 3.0f);
	}
	
	/**
	 * 显示历史发送的消息输出表格
	 * @return
	 */
	private VerticalLayout buildHistoryNoticeLayout() {
		VerticalLayout historyNoticeLayout=new VerticalLayout();
		historyNoticeLayout.setSpacing(true);
		historyNoticeLayout.setSizeFull();
		
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
		historyNoticeLayout.addComponent(searchLayout);
		//创建表格
		table = createFormatColumnTable();
        table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addListener(this);
		historyNoticeLayout.addComponent(table);
		
		//初始化Sql
		search.click();
		
		//翻页组件和操作按钮输出
		noticeService=SpringContextHolder.getBean("noticeService");
		flip=new FlipOverTableComponent<Notice>(Notice.class, noticeService, table, sqlSelect, sqlCount, null);
		flip.getEntityContainer().addNestedContainerProperty("sender.username");
		table.setPageLength(20);
		
		//设置表格头部显示
		Object[] visibleColumns=new Object[] { "id", "title", "content", "sendDate", "sender.username", "receivers"}; 
		String[] columnHeaders=new String[] { "ID", "标题", "内容", "发送时间", "发件人", "收件人"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		table.addGeneratedColumn("receivers", new ReceiversColumnGenerator());	// jrh 2013-11-01

		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);

		writeMessage = new Button("新建消息");
		writeMessage.addListener((Button.ClickListener) this);
// jrh 注释 2013-11-12 为满足中道需求
//		tableButtonsLeft.addComponent(writeMessage);
		
		preView = new Button("预览");
		preView.setEnabled(false); //创建时为不可用
		preView.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(preView);

		edit = new Button("编辑");
		edit.setEnabled(false); //创建时为不可用
		edit.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(edit);

		delete = new Button("删除");
		delete.setEnabled(false); //创建时为不可用
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);
		
		//下方的按钮输出
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setWidth("100%");
		historyNoticeLayout.addComponent(buttonsLayout);
		
		//左侧按钮组件的添加
		buttonsLayout.addComponent(tableButtonsLeft);
		buttonsLayout.setComponentAlignment(tableButtonsLeft, Alignment.BOTTOM_LEFT);
		
		//右侧Flip组件
		buttonsLayout.addComponent(flip);
		buttonsLayout.setComponentAlignment(flip, Alignment.BOTTOM_RIGHT);

		return historyNoticeLayout;
	}
	

	/**
	 *  创建格式化 了的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = -7684817746524558630L;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if(colId.equals("sendDate")) {
	            		return dateFormat.format((Date)property.getValue());
				} else if(colId.equals("title")) {
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
	
	/**
	 * jrh 用于自动生成管理员发送的通知后，接收者的显示样式，如果接收者尚未读取，则工号显示红色加粗
	 */
	private class ReceiversColumnGenerator implements Table.ColumnGenerator {
		private static final long serialVersionUID = 4612836715276670909L;

		public Object generateCell(Table source, Object itemId, Object columnId) {
			Notice notice = (Notice) itemId;
			if (columnId.equals("receivers")) {
				String receivers_caption = compileCaption(notice);
				Label receivers_lb = new Label(receivers_caption, Label.CONTENT_XHTML);
				receivers_lb.setWidth("-1px");
				return receivers_lb;
			} 
			return "";
		}
	}

	/**
	 * jrh
	 *  获取接收人信息，处理标签信息，如果通知已经被坐席读取，或者被坐席删除，都认为是坐席已经读取了
	 *  如果没读，则改变显示信息样式
	 * @param notice 通知条
	 * @return
	 */
	private String compileCaption(Notice notice) {
		ArrayList<User> receivers = new ArrayList<User>(notice.getReceivers());
		Collections.sort(receivers, new Comparator<User>() {
			@Override
			public int compare(User u1, User u2) {
				try {
					int emp1 = Integer.parseInt(u1.getEmpNo());
					int emp2 = Integer.parseInt(u2.getEmpNo());
					return emp1 - emp2;
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return 0;
				}
			}
			
		});
		
		StringBuffer strBf = new StringBuffer();
		for(User receiver : receivers) {
			NoticeItem item = noticeItemService.getByUserAndNotice(receiver, notice);
			if(item != null && !item.isHasReaded()) {
				strBf.append("<font color='red'><b>");
				strBf.append(receiver.getEmpNo());
				strBf.append("</b></font>");
			} else {
				strBf.append(receiver.getEmpNo());
			}
			strBf.append(",");
		}
		if(strBf.length() > 0) {
			strBf.deleteCharAt(strBf.length() - 1);
		}
		return strBf.toString();
	}
	
	/**
	 * 预览消息显示
	 */
	private void showPreviewWindow() {
		if(preViewWindow==null){
			preViewWindow=new CsrPreView(this);
		}
		this.getWindow().addWindow(preViewWindow);
	}

	/**
	 * 显示编辑窗口
	 */
	private void showEditWindow() {
		if(editNoticeWindow==null){
			editNoticeWindow=new CsrEditNotice(this);
		}
		this.getWindow().addWindow(editNoticeWindow);
	}
	

	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("Notice");
		// 关键字过滤,标题
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like titleName = new SqlGenerator.Like("title", keyWordStr);
		sqlGenerator.addOrCondition(titleName);
		
		//内容过滤
		SqlGenerator.Like contentValue = new SqlGenerator.Like("content", keyWordStr);
		sqlGenerator.addOrCondition(contentValue);
		
		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		sqlCount = sqlGenerator.generateCountSql();

		//只能查看自己发的消息
		String deptSql = " and e.sender.id = " + loginUser.getId() + " ";
		sqlSelect += deptSql + " order by e.id desc";
		sqlCount += deptSql;		
		//更新Table，并使Table处于未选中
		this.updateTable(true);
		if(table!=null){
			table.setValue(null);
		}
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
			noticeService.deleteById(((Notice)table.getValue()).getId());
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
			return new Action[] {ADD};
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (ADD == action) {
			writeMessage.click();
		} else if (PREVIEW == action) {
			preView.click();
		} else if (EDIT == action) {
			edit.click();
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
		}else if(event.getButton().equals(writeMessage)){
			//TODO 后添加mgrTabSheet.showNoticeSend();
		}else if(event.getButton().equals(preView)){
			showPreviewWindow();
		}else if(event.getButton().equals(edit)){
			showEditWindow();
		}else if(event.getButton().equals(delete)){
			//由于能选中Delete则table.getValue()一定不为null
			Notice notice=(Notice)table.getValue();
			Label label=new Label("您确定要删除消息<b>"+notice.getTitle()+"</b>?",Label.CONTENT_XHTML);
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
			edit.setEnabled(false);
			delete.setEnabled(false);
		}else{                
			preView.setEnabled(true);
			edit.setEnabled(true);
			delete.setEnabled(true);
		}
	}

}
