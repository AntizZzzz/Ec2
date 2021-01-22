package com.jiangyifen.ec2.ui.mgr.sounddialout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.asteriskjava.manager.action.QueueAddAction;
import org.asteriskjava.manager.action.QueueRemoveAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.SoundDialout;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 为语音群发添加Csr
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class SoundDialoutAssignCsr extends Window implements Button.ClickListener{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
/**
 * 主要组件输出
 */
	//关键字和搜索按钮
	//左
	private TextField leftKeyWord;
	private Button leftSearch;
	//右
	private TextField rightKeyWord;
	private Button rightSearch;
	
	//中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;
	
	//表格
	private Object[] columns = new String[] { "id", "username","empNo","department.name"};
	private String[] headers = new String[] { "ID", "用户名","工号","部门"};
	private Table leftTable;
	private MarketingProjectService marketingProjectService;

	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private BeanItemContainer<User> leftTableContainer;
	
	//下面的添加和取消按钮
	private Button assign;
	private Button cancel;
/**
 * 其他组件
 */
	private Domain domain;
	private AutoDialoutTaskService autoDialoutTaskService;
	private UserQueueService userQueueService;
	//持有从Table取出来的AutoDialoutTask引用
	private AutoDialoutTask autoDialoutTask;
	//持有调用它的组件引用AutoDialout,以刷新父组件
	private SoundDialout soundDialout;
	//右侧组件刚开始的CSR数据，用于计算本次分配的数目
	private int originalSize;
	//记录组件弹出时的初始Csr集合
	private List<User> originalCsrs;
	
	public SoundDialoutAssignCsr(SoundDialout soundDialout) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.soundDialout=soundDialout;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		/*=====中间的两个表格和按钮=========*/
		HorizontalLayout tempTablesLayout=buildTablesLayout();
		windowContent.addComponent(tempTablesLayout);
		windowContent.setExpandRatio(tempTablesLayout, 1.0f);
		
		/*=========下面的按钮输出===========*/
		HorizontalLayout tempButtonsLayout=buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout, Alignment.BOTTOM_RIGHT);
		
		//设置按钮样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
		//添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);
	}
	
	/**
	 * 初始化Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		autoDialoutTaskService=SpringContextHolder.getBean("autoDialoutTaskService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		userQueueService=SpringContextHolder.getBean("userQueueService");
	}
	
	
	/**
	 * 创建主要输出部分，中间的两个Table组件和Table按钮组件
	 * @return
	 */
	private HorizontalLayout buildTablesLayout() {
		HorizontalLayout tablesLayout=new HorizontalLayout();
		tablesLayout.setSpacing(true);
		//要想剔除左侧Table包含的右侧Table的Item，必须先创建右侧组件，再创建左侧组件
		VerticalLayout tempRight = buildRight();
		VerticalLayout tempLeft = buildLeft();

		//左侧输出
		tablesLayout.addComponent(tempLeft);
		tablesLayout.setExpandRatio(tempLeft, 8.0f);
		
		//中间输出
		VerticalLayout tempCenter = buildCenter();
		tablesLayout.addComponent(tempCenter);
		tablesLayout.setExpandRatio(tempCenter, 1.5f);
		
		//右侧输出
		tablesLayout.addComponent(tempRight);
		tablesLayout.setExpandRatio(tempRight, 8.0f);
		return tablesLayout;
	}

	/**
	 * 添加Csr左侧输出
	 * @return
	 */
	private VerticalLayout buildLeft() {
		//左侧
		VerticalLayout leftLayout=new VerticalLayout();
		leftLayout.setSpacing(true);
		
		//搜索组件
		leftLayout.addComponent(buildLeftSearch());
		
		//Table的状态Label
		leftLayout.addComponent(new Label("全部CSR"));
		
		//表格组件 和翻页组件
		leftLayout.addComponent(buildLeftTable());
		
		return leftLayout;
	}

	/**
	 * 添加Csr右侧输出
	 * @return
	 */
	private VerticalLayout buildRight() {
		VerticalLayout rightLayout=new VerticalLayout();
		rightLayout.setSpacing(true);
		
		//搜索组件
		rightLayout.addComponent(buildRightSearch());
		
		//Table的状态Label
		rightLayout.addComponent(new Label("已选CSR"));
		
		//表格组件
		rightLayout.addComponent(buildRightTable());
		
		return rightLayout;
	}

	/**
	 * 添加Csr中间输出
	 * @return
	 */
	private VerticalLayout buildCenter() {
		VerticalLayout centerLayout=new VerticalLayout();
		centerLayout.setSpacing(true);
		//占位组件
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		
		//按钮组件
		addAll=new Button(">>>");
		addAll.addListener(this);
		centerLayout.addComponent(addAll);
		centerLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);
		
		add=new Button(">>");
		add.addListener(this);
		centerLayout.addComponent(add);
		centerLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove=new Button("<<");
		remove.addListener(this);
		centerLayout.addComponent(remove);
		centerLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);
		
		removeAll=new Button("<<<");
		removeAll.addListener(this);
		centerLayout.addComponent(removeAll);
		centerLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);
		
		return centerLayout;
	}
	/**
	 * 创建此窗口最下边按钮的输出
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("23%");
		//添加
		assign=new Button("确定");
		assign.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(assign);
		buttonsLayout.setComponentAlignment(assign, Alignment.MIDDLE_CENTER);
		//取消
		cancel=new Button("取消");
		cancel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(cancel);
		buttonsLayout.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		return buttonsLayout;
	}
	
	/**
	 * 由 buildLeft 调用，左侧的搜索组件
	 * @return
	 */
	private HorizontalLayout buildLeftSearch() {
		HorizontalLayout leftSearchLayout=new HorizontalLayout();
		leftSearchLayout.setSpacing(true);
		
		HorizontalLayout constraintLayout=new HorizontalLayout();
		constraintLayout.addComponent(new Label("关键字"));
		leftKeyWord=new TextField();//关键字
		leftKeyWord.setStyleName("search");
		leftKeyWord.setWidth("5em");
		leftKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(leftKeyWord);
		leftSearchLayout.addComponent(constraintLayout);
		
		// 搜索按钮
		leftSearch = new Button("搜索");
		leftSearch.addListener(this);
		leftSearchLayout.addComponent(leftSearch);
		return leftSearchLayout;
	}
	/**
	 * 由 buildLeft 调用，左侧表格和翻页组件,此时sqlSelect和sqlCount应该已经被初始化
	 * @return
	 */
	private VerticalLayout buildLeftTable() {
		VerticalLayout tableLayout=new VerticalLayout();
		tableLayout.setWidth("100%");
		//表格
		leftTable=new Table();
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		tableLayout.addComponent(leftTable);
		
		leftTableContainer=new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setPageLength(10);
		leftTable.setVisibleColumns(columns);
		leftTable.setColumnHeaders(headers);
		
		return tableLayout;
	}

	/**
	 * 由 buildRight 调用，右侧的搜索组件
	 * @return
	 */
	private HorizontalLayout buildRightSearch() {
		HorizontalLayout rightSearchLayout=new HorizontalLayout();
		rightSearchLayout.setSpacing(true);
		
		HorizontalLayout constraintLayout=new HorizontalLayout();
		constraintLayout.addComponent(new Label("关键字"));
		rightKeyWord=new TextField();//关键字
		rightKeyWord.setWidth("5em");
		rightKeyWord.setStyleName("search");
		rightKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(rightKeyWord);
		rightSearchLayout.addComponent(constraintLayout);
		
		// 搜索按钮
		rightSearch = new Button("搜索");
		rightSearch.addListener(this);
		rightSearchLayout.addComponent(rightSearch);
		return rightSearchLayout;
	}
	/**
	 * 由buildRight调用，创建右侧表格组件
	 * @return
	 */
	private VerticalLayout buildRightTable() {
		VerticalLayout tableLayout=new VerticalLayout();
		tableLayout.setWidth("100%");
		//表格
		rightTable=new Table();
		rightTable.setStyleName("striped");
		rightTable.setPageLength(10);
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTableContainer= new BeanItemContainer<User>(User.class);
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}
	
	/**
	 * 由buildRightTable和attach调用，更新右侧表格的回显信息
	 */
	private void updateRightTable() {
		List<User> csrs=autoDialoutTaskService.getCsrsByAutoDialoutTask(autoDialoutTask, domain);
		rightTableContainer.removeAllItems();
		rightTableContainer.addAll(csrs);
		originalSize = csrs.size();
		originalCsrs=csrs;
	}
	
	/**
	 * 更新左侧表格
	 */
	private void updateLeftTable() {
		//为左侧组件设置Container
		List<User> entitys =marketingProjectService.getCsrsByProject(autoDialoutTask.getMarketingProject(), domain);
		leftTableContainer.removeAllItems();
		leftTableContainer.addAll(entitys);
		//移除在右侧组件中出现的Item
		Collection<User> rightUsers=(Collection<User>)rightTableContainer.getItemIds();
		//将被移除的User
		List<User> toRemove=new ArrayList<User>();
		//查找出将要移除的用户
		for(User leftUser:entitys){
			for(User rightUser:rightUsers){
				if(leftUser.getId() != null && leftUser.getId().equals(rightUser.getId())){
					toRemove.add(leftUser);
				}
			}
		}
		//从左侧组件中移除出现在右侧组件中的用户
		for(int i=0;i<toRemove.size();i++){
			leftTable.removeItem(toRemove.get(i));
		}
	}
	
	/**
	 * 由 构造器 调用，设置按钮的样式
	 * @param style
	 */
	private void setButtonsStyle(String style) {
		style="small";
		leftSearch.setStyleName(style);
		rightSearch.setStyleName(style);
		add.setStyleName(style);
		addAll.setStyleName(style);
		remove.setStyleName(style);
		removeAll.setStyleName(style);
	}
	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * @param tableFrom 从哪个表取数据
	 * @param tableTo	添加到哪个表
	 * @param isAll 是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom,Table tableTo,Boolean isAll) {
		if(tableFrom==null||tableTo==null) return;
		
		//如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if(!isAll) if (tableFrom.getValue() == null) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的CSR!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<User> csrs=null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			csrs=new ArrayList<User>((Collection<User>)tableFrom.getItemIds());
		}else{
			csrs=(Collection<User>) tableFrom.getValue();
		}
		//通过循环来改变TableFrom和TableTo的Item	
		for (User csr : csrs) {
			tableFrom.getContainerDataSource().removeItem(csr);
			tableTo.getContainerDataSource().addItem(csr);
		}
	}

	
	/**
	 * 由buttonClick调用，执行生成左侧Table的搜索的Sql,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		if(leftTableContainer==null) return;
		// 搜索符合条件的批次信息
		leftTableContainer.removeAllContainerFilters();
		// 关键字
		String leftKeyWordStr ="";
		if (leftKeyWordStr.equals("")) {
			leftKeyWordStr = leftKeyWord.getValue().toString();
		}
		Or compareAll = new Or(
	             new Like("username", "%" + leftKeyWordStr + "%", false), 
	             new Like("empNo", "%" + leftKeyWordStr + "%", false), 
	             new Like("department.name", "%" + leftKeyWordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
	}
	
	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if(rightTableContainer==null) return;
		// 搜索符合条件的批次信息
		rightTableContainer.removeAllContainerFilters();
		// 关键字
		String rightKeyWordStr ="";
		if (rightKeyWordStr.equals("")) {
			rightKeyWordStr = rightKeyWord.getValue().toString();
		}
		Or compareAll = new Or(
	             new Like("username", "%" + rightKeyWordStr + "%", false), 
	             new Like("empNo", "%" + rightKeyWordStr + "%", false), 
	             new Like("department.name", "%" + rightKeyWordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
	}
	/**
	 * 执行给语音群发添加CSR的任务，此时以前分配资源的CSR仍然在Task中
	 */
	private void executeAssign() {
		try {
			//更新给语音群发分配的CSR
			Set<User> selectedCsrs=new HashSet<User>((Collection<User>)rightTableContainer.getItemIds());
			//估计一个语音群发的CSR数量不会太多，上千上万，所以让EL自己去管理，不手工控制
			autoDialoutTask.setUsers(selectedCsrs);
			//更新数据库和Asterisk中的CSR和动态队列的关系 
			csrAndQueueRefresh(originalCsrs,selectedCsrs);
			autoDialoutTaskService.update(autoDialoutTask);
			//成功分配的CSR记录数
			int successAssignNum=selectedCsrs.size()-originalSize;//目前的计数减去初始时计数
			
			String csrStr="添加了"+successAssignNum;
			if(successAssignNum<0){
				csrStr="移除了"+(-successAssignNum);
			}
			Notification msgNotif=new Notification("成功给语音群发"+autoDialoutTask.getAutoDialoutTaskName()+"指派"+csrStr+"个CSR!",Notification.TYPE_HUMANIZED_MESSAGE);
			//更新ProjectControl的状态信息，并且显示Notif提示
			Window mainWindow=this.getApplication().getMainWindow();
			this.getParent().removeWindow(this);
			mainWindow.showNotification(msgNotif);
		} catch (Exception e) {
			e.printStackTrace();
			NotificationUtil.showWarningNotification(this, "语音群发指派CSR出现异常！");
		}
	}
	
	/**
	 * 刷新Queue和CSR
	 */
	private void csrAndQueueRefresh(List<User> originalCsrs,
			Set<User> nowCsrs) {
		String queueName=autoDialoutTask.getQueue().getName();
		//========================找出添加的 和 移除的CSR==================================//
		//找出已经被移除的CSR
		List<User> removedCsrs=new ArrayList<User>();
		for(User originalCsr:originalCsrs){
			Boolean isInNow=false;
			for(User nowCsr:nowCsrs){
				//如果新旧csr的Id值相等，则标记isInNow为true
				if(originalCsr.getId() != null && originalCsr.getId().equals(nowCsr.getId())){
					isInNow=true;
				}
			}
			//如果没出现在现在的Csr中则已经被移除
			if(!isInNow){
				removedCsrs.add(originalCsr);
			}
		}
		
		//找出新添加的CSR
		List<User> addedCsrs=new ArrayList<User>();
		for(User nowCsr:nowCsrs){
			Boolean isInOriginal=false;
			for(User originalCsr:originalCsrs){
				//如果新旧csr的Id值相等，则标记isInNow为true
				if(originalCsr.getId() != null && originalCsr.getId().equals(nowCsr.getId())){
					isInOriginal=true;
				}
			}
			//如果没出现在原来的Csr中则说明是新添加的CSR
			if(!isInOriginal){
				addedCsrs.add(nowCsr);
			}
		}
		logger.info("语音群发移除："+removedCsrs+"    \n语音群发新加："+addedCsrs);

		//========================ec2_user_queue表中的对应关系进行更新==================================//
		for(User user:addedCsrs){
			String userName=user.getUsername();
			UserQueue userQueue=new UserQueue();
			userQueue.setUsername(userName);
			userQueue.setQueueName(queueName);
			userQueue.setPriority(1);
			userQueue.setDomain(domain);
			userQueueService.update(userQueue);
		}

		for(User user:removedCsrs){
			String userName=user.getUsername();
			userQueueService.removeRelation(queueName,userName,domain.getId());
		}
		
		//========================操纵Asterisk将新添加的（处于登陆状态的用户）添加进动态队列，将移除的用户从语音群发的队列中移除==================================//
		for(User user:addedCsrs){
			String exten=ShareData.userToExten.get(user.getId());
			if(exten!=null){ //用户已经登陆
				maintainQueueMemberRelation(queueName,exten,"add");
			}
		}
		
		for(User user:removedCsrs){
			String exten=ShareData.userToExten.get(user.getId());
			if(exten!=null){ //用户已经登陆
				maintainQueueMemberRelation(queueName,exten,"remove");
			}
		}
	}
	
	/**
	 * 当添加和移除语音群发成员时，用此方法来维护Asterisk中用户和队列的相应信息
	 */
	private void maintainQueueMemberRelation(String queueName, String exten,String opera) {
		if(opera.equals("add")){
			QueueAddAction queueAddAction = new QueueAddAction(queueName, "SIP/" + exten);
			AmiManagerThread.sendAction(queueAddAction);
		}else if(opera.equals("remove")){
			QueueRemoveAction queueRemoveAction = new QueueRemoveAction(queueName, "SIP/" + exten);
			AmiManagerThread.sendAction(queueRemoveAction);
		}
	}

	/**
	 * 弹出新的窗口,刷新Table里面数据的值
	 */
	@Override
	public void attach() {
		super.attach();
		//要想剔除左侧Table包含的右侧Table的Item，必须先更新右侧组件，再更新左侧组件
		autoDialoutTask=soundDialout.getCurrentSelect();
		this.setCaption("语音群发 "+autoDialoutTask.getAutoDialoutTaskName()+" 添加/移除CSR");
		//更新右侧组件的回显信息
		this.updateRightTable();
		this.updateLeftTable();
	}
	
	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 
	 * 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==leftSearch){
			executeLeftSearch();
		}else if(event.getButton()==add){
			addToOpposite(leftTable, rightTable,false);
		}else if(event.getButton()==addAll){
			addToOpposite(leftTable, rightTable,true);
		}else if(event.getButton()==remove){
			addToOpposite(rightTable,leftTable,false);
		}else if(event.getButton()==removeAll){
			addToOpposite(rightTable,leftTable,true);
		}else if(event.getButton()==rightSearch){
			executeRightSearch();
		}else if(event.getButton()==assign){
			executeAssign();
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}
}
