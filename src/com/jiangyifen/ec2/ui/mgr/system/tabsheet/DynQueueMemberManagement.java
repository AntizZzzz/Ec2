package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;


/**
 * 动态队列成员管理 组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class DynQueueMemberManagement extends VerticalLayout implements
		ClickListener, ValueChangeListener {
	
	private final Object[] VISIBLE_PROPERTIES = new String[] {"empNo", "username", "realName", "department.name" };
	private final String[] COL_HEADERS = new String[] {"工号", "用户名", "真实姓名", "部门" };
	
	/**
	 * 主要组件输出
	 */
	// 顶端队列选择组件
	private ComboBox queueSelector;
	private Queue nearestSelectedQueue;						// 在本次之前最近被选中的队列
	private List<UserQueue> oldUserQueuesToQueueName;		// 某一具体队列所拥有的 “用户-队列”对应关系对象
	private BeanItemContainer<Queue> queueContainer;
	
	// 关键字和搜索按钮
	// 左
	private TextField leftKeyword;
	private Button leftSearch;
	// 右
	private TextField rightKeyword;
	private Button rightSearch;

	// 中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;

	// 表格
	private Table leftTable;
	private Table rightTable;
	private BeanItemContainer<User> leftTableContainer;
	private BeanItemContainer<User> rightTableContainer;

	// 下面队列中所有成员的优先级全局设置，添加和取消按钮
	private Button save;
	private Button cancel;
	private TextField priorityField;			// 全局设置优先级
	private Notification priorityError;			// 全局优先级设置格式出错时提醒

	// 如果设置了全局设置值，则弹出提醒窗口，让其确认后再更新
	private Window globalPriorityConfirmWindow;
	private Label noticeInPriorityWindow;
	private Button submit;
	private Button abolish;
	
	/**
	 * 其他组件
	 */
	private Domain domain;											// 当前登陆用户所属域
	private TreeMap<String, Integer> usernameToPriority;			// 用于保存 用户 与其在队列中优先级的 对应关系 
	private UserService userService;								// 用户服务类
	private QueueService queueService;								// 队列服务类
	private UserQueueService userQueueService;						// 动态队列成员服务类
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员关系管理服务类
	private Phone2PhoneSettingService phone2PhoneSettingService;	// 外转外配置服务类

	public DynQueueMemberManagement() {
		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		userService = SpringContextHolder.getBean("userService");
		queueService = SpringContextHolder.getBean("queueService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
		phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
		
		queueContainer = new BeanItemContainer<Queue>(Queue.class);
		List<Queue> allQueues = queueService.getAllByDomain(domain, true);
		for(Queue queue : allQueues) {
			if(queue.getDynamicmember() && queue.getIsModifyable()) {
				queueContainer.addBean(queue);
			}
		}
		
		priorityError = new Notification("全局优先级 <B>格式错误</B>，只能为<B> >0 </B>的整数", Notification.TYPE_ERROR_MESSAGE);
		priorityError.setDelayMsec(1000);
		priorityError.setHtmlContentAllowed(true);
		priorityError.setPosition(Notification.POSITION_CENTERED);
		
		usernameToPriority = new TreeMap<String, Integer>();
		
		// 创建界面顶部的 队列 选择组件
		createQueueSelectComponents();

		// 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮) 
		HorizontalLayout centerHLayout = createCenterHLayout();
		this.addComponent(centerHLayout);
		
		// 创建界面底部组件(保存、取消按钮)
		createBottomComponents();

		// 添加拖拽支持
		makeTableDragAble(new SourceIs(rightTable), leftTable, true);
		makeTableDragAble(new SourceIs(leftTable), rightTable, false);
		
		// 创建全局优先级设置确认 窗口及其相应组件
		createPriorityConfirmWindow();
		
		// 设置初始值
		for(Queue queue : allQueues) {
			if(queue.getDynamicmember()) {
				queueSelector.select(queue);
				break;
			}
		}
	}

	/**
	 * 创建界面顶部的 队列 选择组件
	 */
	private void createQueueSelectComponents() {
		HorizontalLayout maintopLayout = new HorizontalLayout();
		maintopLayout.setSpacing(true);
		this.addComponent(maintopLayout);
		this.setComponentAlignment(maintopLayout, Alignment.MIDDLE_CENTER);

		Label caption = new Label("<B>队列选择：</B>", Label.CONTENT_XHTML);
		caption.setWidth("-1px");
		maintopLayout.addComponent(caption);

		queueSelector = new ComboBox();
		queueSelector.setNullSelectionAllowed(false);
		queueSelector.setImmediate(true);
		queueSelector.setContainerDataSource(queueContainer);
		queueSelector.setItemCaptionPropertyId("descriptionAndName");
		queueSelector.addListener(this);
		maintopLayout.addComponent(queueSelector);
	}
	
	/** 
	 * 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮)
	 * @return HorizontalLayout
	 */
	private HorizontalLayout createCenterHLayout() {
		HorizontalLayout centerHLayout = new HorizontalLayout();
		centerHLayout.setSpacing(true);
		centerHLayout.setWidth("100%");
		
		// 创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
		VerticalLayout leftComponents = createLeftComponents();
		centerHLayout.addComponent(leftComponents);
		centerHLayout.setExpandRatio(leftComponents, 0.4f);
		
		// 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
		VerticalLayout middleComponents = createMiddleComponents();
		centerHLayout.addComponent(middleComponents);
		centerHLayout.setComponentAlignment(middleComponents, Alignment.MIDDLE_CENTER);
		centerHLayout.setExpandRatio(middleComponents, 0.2f);

		// 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件)
		VerticalLayout rightComponents = createRightComponents();
		centerHLayout.addComponent(rightComponents);
		centerHLayout.setExpandRatio(rightComponents, 0.4f);
		
		return centerHLayout;
	}
	
	/**
	 *  创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
	 * @return
	 */
	 
	private VerticalLayout createLeftComponents() {
		VerticalLayout leftVLayout = new VerticalLayout();
		leftVLayout.setSpacing(true);
		leftVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		leftVLayout.addComponent(searchHLayout);

		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		searchHLayout.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);

		leftKeyword = new TextField();
		leftKeyword.setImmediate(true);
		leftKeyword.setInputPrompt("请输入搜索关键字");
		leftKeyword.setDescription("可按工号、用户名及部门名称搜索！");
		leftKeyword.setStyleName("search");
		leftKeyword.addListener(this);
		searchHLayout.addComponent(leftKeyword);
		searchHLayout.setComponentAlignment(leftKeyword, Alignment.MIDDLE_CENTER);

		leftSearch = new Button("搜索", this);
		leftSearch.setImmediate(true);
		searchHLayout.addComponent(leftSearch);
		searchHLayout.setComponentAlignment(leftSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		// 表格
		leftTable = new Table("可选成员");
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftVLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setPageLength(20);
		leftTable.setVisibleColumns(VISIBLE_PROPERTIES);
		leftTable.setColumnHeaders(COL_HEADERS);
		
		return leftVLayout;
	}

	/**
	 * 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
	 * return 
	 */
	private VerticalLayout createMiddleComponents() {
		VerticalLayout operatorVLayout = new VerticalLayout();
		operatorVLayout.setSpacing(true);
		operatorVLayout.setSizeFull();

		// 占位组件
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));

		// 按钮组件
		addAll = new Button(">>>", this);
		operatorVLayout.addComponent(addAll);
		operatorVLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);

		add = new Button(">>", this);
		operatorVLayout.addComponent(add);
		operatorVLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove = new Button("<<", this);
		operatorVLayout.addComponent(remove);
		operatorVLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);

		removeAll = new Button("<<<", this);
		operatorVLayout.addComponent(removeAll);
		operatorVLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);
		
		// 占位组件
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		
		return operatorVLayout;
	}

	/**
	 * 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件)
	 * return 
	 */
	private VerticalLayout createRightComponents() {
		VerticalLayout rightVLayout = new VerticalLayout();
		rightVLayout.setSpacing(true);
		rightVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		rightVLayout.addComponent(searchHLayout);

		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		searchHLayout.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);

		rightKeyword = new TextField();
		rightKeyword.setImmediate(true);
		rightKeyword.setInputPrompt("请输入搜索关键字");
		rightKeyword.setDescription("可按工号、用户名及部门名称搜索！");
		rightKeyword.setStyleName("search");
		rightKeyword.addListener(this);
		searchHLayout.addComponent(rightKeyword);
		searchHLayout.setComponentAlignment(rightKeyword, Alignment.MIDDLE_CENTER);

		rightSearch = new Button("搜索", this);
		rightSearch.setImmediate(true);
		searchHLayout.addComponent(rightSearch);
		searchHLayout.setComponentAlignment(rightSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		// 表格
		rightTable = new Table("拥有成员");
		rightTable.setStyleName("striped");
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTable.setPageLength(16);
		rightTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightVLayout.addComponent(rightTable);

		rightTableContainer = new BeanItemContainer<User>(User.class);
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(VISIBLE_PROPERTIES);
		rightTable.setColumnHeaders(COL_HEADERS);
		rightTable.addGeneratedColumn("priority", new PriorityColumnGenerate());
		rightTable.setColumnHeader("priority", "优先级");
		
		return rightVLayout;
	}

	/**
	 * 创建界面底部组件(保存、取消按钮)
	 */
	private void createBottomComponents() {
		HorizontalLayout bottomHLayout = new HorizontalLayout();
		bottomHLayout.setSpacing(true);
		bottomHLayout.setWidth("100%");
		this.addComponent(bottomHLayout);
		
		// 表格底部左侧组件
		HorizontalLayout bottomLeft = new HorizontalLayout();
		bottomLeft.setSpacing(true);
		bottomLeft.setWidth("-1px");
		bottomHLayout.addComponent(bottomLeft);
		bottomHLayout.setComponentAlignment(bottomLeft, Alignment.MIDDLE_LEFT);
		
		// 保存
		save = new Button("保 存", this);
		save.setStyleName("default");
		bottomLeft.addComponent(save);
		bottomLeft.setComponentAlignment(save, Alignment.MIDDLE_LEFT);
		
		// 取消
		cancel = new Button("取 消", this);
		bottomLeft.addComponent(cancel);
		bottomLeft.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);

		// 表格底部右侧组件
		HorizontalLayout bottomRight = new HorizontalLayout();
		bottomRight.setSpacing(true);
		bottomRight.setWidth("-1px");
		bottomHLayout.addComponent(bottomRight);
		bottomHLayout.setComponentAlignment(bottomRight, Alignment.MIDDLE_RIGHT);
		
		Label priorityCaption = new Label("<B>全局设置队列成员优先级：</B>", Label.CONTENT_XHTML);
		priorityCaption.setWidth("-1px");
		bottomRight.addComponent(priorityCaption);
		bottomRight.setComponentAlignment(priorityCaption, Alignment.MIDDLE_RIGHT);
		
		priorityField = new TextField();
		priorityField.addListener(this);
		priorityField.setWidth("100px");
		priorityField.setImmediate(true);
		priorityField.setWriteThrough(false);
		bottomRight.addComponent(priorityField);
		bottomRight.setComponentAlignment(priorityField, Alignment.MIDDLE_RIGHT);
		
		// 将保存和取消按钮置为不可用状态
		setButtonsEnable(false);
	}
	
	/**
	 * 实现表格的拖曳功能
	 * @param acceptCriterion
	 * @param table
	 * @param isDragout 是否为从 UserQueues 中减少成员
	 */
	private void makeTableDragAble(final ClientSideCriterion acceptCriterion, final Table table, final boolean isDragout) {
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {
			@Override
			public void drop(DragAndDropEvent dropEvent) {
				// 验证传递过来的是否是可传递的对象
				DataBoundTransferable transferable = (DataBoundTransferable) dropEvent.getTransferable();
				
				// 不是BeanItemContainer则不响应，直接返回
				if (!(transferable.getSourceContainer() instanceof BeanItemContainer)) {
					return;
				}

				// 获取源sourceItemId
				User sourceItem = (User) transferable.getItemId();

				// 选中要Drop到的targetItemId
				table.getContainerDataSource().addItem(sourceItem);
				transferable.getSourceContainer().removeItem(sourceItem);

				// 按工号的升序排列
				leftTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
				rightTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
				
				// 如果是从右侧表格移除对象，则需要将对应的用户 -- 优先级对应关系一起删除
				if(isDragout == true) {
					usernameToPriority.remove(sourceItem.getUsername());
				}
				
				// 每一次移除表格中的对象后，就重新设置左右两个表格的标题
				initializeTablesCaption();
				
				// 表格中的数据发生变化后，将保存和取消按钮置为可用
				setButtonsEnable(true);
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return new com.vaadin.event.dd.acceptcriteria.And(acceptCriterion, AcceptItem.ALL);
			}
		});
	}

	/**
	 * 创建全局优先级设置确认 窗口及其相应组件
	 */
	private void createPriorityConfirmWindow() {
		globalPriorityConfirmWindow = new Window("确认操作");
		globalPriorityConfirmWindow.center();
		globalPriorityConfirmWindow.setWidth("300px");
		globalPriorityConfirmWindow.setModal(true);
		globalPriorityConfirmWindow.setResizable(false);
		
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeFull();
		windowContent.setMargin(true);
		windowContent.setSpacing(true);
		globalPriorityConfirmWindow.setContent(windowContent);
		
		noticeInPriorityWindow = new Label("", Label.CONTENT_XHTML);
		windowContent.addComponent(noticeInPriorityWindow);
		
		HorizontalLayout buttonsHLayout = new HorizontalLayout();
		buttonsHLayout.setSpacing(true);
		windowContent.addComponent(buttonsHLayout);
		
		submit = new Button("确 定", this);
		abolish = new Button("取 消", this);
		abolish.setStyleName("default");
		buttonsHLayout.addComponent(submit);
		buttonsHLayout.addComponent(abolish);
	}
	
	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * @param tableFrom 从哪个表取数据
	 * @param tableTo	添加到哪个表
	 * @param isAll 是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if(tableFrom == null||tableTo == null) return;
		
		//如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if(!isAll && ((Collection<User>) tableFrom.getValue()).size() == 0) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的CSR!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<User> csrs = null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			csrs = new ArrayList<User>((Collection<User>)tableFrom.getItemIds());
		} else {
			csrs = (Collection<User>) tableFrom.getValue();
		}
		
		//通过循环来改变TableFrom和TableTo的Item	
		for (User user : csrs) {
			tableFrom.getContainerDataSource().removeItem(user);
			tableTo.getContainerDataSource().addItem(user);
			
			// 如果源表格式 右侧表，则要重新处理用户与优先级的对应关系
			if(tableFrom == rightTable) {
				usernameToPriority.remove(user.getUsername());
			}
		}
		
		// 将保存与取消按钮置为 可用状态
		setButtonsEnable(true);

		// 初始化表格标题
		initializeTablesCaption();
	}
	
	/**
	 * 由buttonClick调用，执行生成左侧Table的过滤器,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		if(leftTableContainer==null) return;
		
		// 删除之前的所有过滤器
		leftTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String leftKeywordStr = ((String) leftKeyword.getValue()).trim();
		
		Or compareAll = new Or(
				 new Like("empNo", "%" + leftKeywordStr + "%", false), 
	             new Like("username", "%" + leftKeywordStr + "%", false), 
	             new Like("department.name", "%" + leftKeywordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		leftTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
		
		// 收索完成后初始化表格的标题
		String leftCaption = "可选成员 ( " + leftTableContainer.size() + " )";
		leftTable.setCaption(leftCaption);
	}
	
	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if(rightTableContainer==null) return;
		
		// 删除之前的所有过滤器
		rightTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = ((String) rightKeyword.getValue()).trim();

		Or compareAll = new Or(
				 new Like("empNo", "%" + rightKeywordStr + "%", false), 
	             new Like("username", "%" + rightKeywordStr + "%", false), 
	             new Like("department.name", "%" + rightKeywordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		rightTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
	}
	
	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == leftSearch) {
			executeLeftSearch();
		} else if (source == add) {
			addToOpposite(leftTable, rightTable, false);
			leftTable.setValue(null);
		} else if (source == addAll) {
			addToOpposite(leftTable, rightTable, true);
		} else if (source == remove) {
			addToOpposite(rightTable, leftTable, false);
		} else if (source == removeAll) {
			addToOpposite(rightTable, leftTable, true);
		} else if (source == rightSearch) {
			executeRightSearch();
		} else if (source == save) {
			String globalPriorityStr = ((String) priorityField.getValue()).trim();
			if(!"".equals(globalPriorityStr) && !globalPriorityStr.matches("[1-9]\\d*")) {
				this.getApplication().getMainWindow().showNotification(priorityError);
				return;
			}

			// 如果右侧搜索区域不为空，则需要先将右侧区域置空，再保存
			String rightKeywordStr = ((String) rightKeyword.getValue()).trim();
			if(!"".equals(rightKeywordStr)) {
				rightKeyword.setValue("");
			}
			
			executeConfirm(globalPriorityStr);
		} else if (source == cancel) {
			// 恢复表格中的数据源
			if(nearestSelectedQueue != null) {
				updateTableSource(nearestSelectedQueue.getName());
			} else {
				updateTableSource("");
			}
			// 更新表格的标题
			initializeTablesCaption();
			// 将保存与取消按钮置为不可用状态
			setButtonsEnable(false);
			priorityField.setValue("");		// 将全局优先级置空
		} else if(source == submit) {
			String globalPriority = ((String) priorityField.getValue()).trim();
			Integer globalPriorityInteger = Integer.valueOf(globalPriority);
			excuteSave(globalPriorityInteger);
			// 更新表格中的内容
			updateTable();
			this.getApplication().getMainWindow().removeWindow(globalPriorityConfirmWindow);
		} else if(source == abolish) {
			this.getApplication().getMainWindow().removeWindow(globalPriorityConfirmWindow);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == queueSelector) {
			usernameToPriority.clear();
			Queue queue = (Queue) source.getValue();
			nearestSelectedQueue = queue;
			
			if(queue != null) {
				// 获取该队列的所有 用户队列对应关系
				List<UserQueue> userQueues = userQueueService.getAllByQueueName(queue.getName(), domain);
				if(userQueues != null) {
					for(UserQueue userQueue : userQueues) {
						usernameToPriority.put(userQueue.getUsername(), userQueue.getPriority());
					}
				}
				updateTableSource(queue.getName());
			} else {
				updateTableSource("");
			}
			leftKeyword.setValue("");
			rightKeyword.setValue("");
			initializeTablesCaption();
		} else if(source == leftKeyword) {
			leftSearch.click();
		} else if(source == rightKeyword) {
			rightSearch.click();
		} else if(source == priorityField) {
			setButtonsEnable(true);
		}
	}

	/**
	 * 将保存 和 取消 按钮置为可用 或者不可用
	 * @param enabled
	 */
	public void setButtonsEnable(boolean enabled) {
		save.setEnabled(enabled);
		cancel.setEnabled(enabled);
	}

	/**
	 * 执行保存操作
	 */
	private void executeConfirm(String globalPriority) {
		if(!"".equals(globalPriority)) {
			noticeInPriorityWindow.setValue("<font color='red'>您是否确定将<B>所有</B>成员的优先级都置为: <B>" +globalPriority+ "</B>？？！");
			this.getApplication().getMainWindow().addWindow(globalPriorityConfirmWindow);
		} else {
			excuteSave(null);
			updateTable();			// 更新表格中的内容
		}
	}

	/**
	 * 执行保存操作
	 * @param globalPriorityInteger 全局优先级设置
	 */
	private void excuteSave(Integer globalPriorityInteger) {
		//更新队列拥有的CSR
		Set<User> selectedCsrs = new HashSet<User>(rightTableContainer.getItemIds());
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domain.getId());
		String defaultOutline = ShareData.domainToDefaultOutline.get(domain.getId());
		
		// 循环检验，添加新的 UserQueue
		for(User user : selectedCsrs) {
			// 默认是该用户队列在数据库中不存在
			UserQueue userQueue = null;
			Phone2PhoneSetting customSetting = phone2PhoneSettingService.getByUser(user.getId());
			String exten = ShareData.userToExten.get(user.getId());
			Integer currentPriority = usernameToPriority.get(user.getUsername());
			
			// 检验是否已经存在用户 user ，如果存在则将oldPriority 置为 其老的优先级
			for(UserQueue uq : oldUserQueuesToQueueName) {
				if(user.getUsername().equals(uq.getUsername())) {
					userQueue = uq;
					break;
				}
			}
			
			// 不存在，则添加到数据库
			if(userQueue == null) {
				UserQueue newUserQueue = new UserQueue();
				newUserQueue.setDomain(domain);
				newUserQueue.setUsername(user.getUsername());
				newUserQueue.setQueueName(nearestSelectedQueue.getName());
				Integer priority = (globalPriorityInteger == null) ? currentPriority : globalPriorityInteger; 
				newUserQueue.setPriority(priority);
				newUserQueue = userQueueService.update(newUserQueue);
				// 如果添加动态队列成员成功，并且用户在线， 则需要向Asterisk 发送信息
				if(newUserQueue.getId() != null) {
					// 更新队列成员信息
					updateQueueMembers(true, priority, exten, defaultOutline, user, globalSetting, customSetting);
				} 
			} else {	// 当前操作的UserQueue 已经存在
				// 如果设置了全局优先级，则将全局的优先级与已经存在的UserQueue 的老优先级比较，如果不相等，则更新数据
				if(globalPriorityInteger != null && userQueue.getPriority() != globalPriorityInteger) {
					userQueue.setPriority(globalPriorityInteger);
					userQueueService.update(userQueue);
					// 更新队列成员信息
					updateQueueMembers(true, globalPriorityInteger, exten, defaultOutline, user, globalSetting, customSetting);
				// 如果没设置全局优先级，则将UserQueue 的老优先级与当前优先级进行比较，如果不相等，则更新数据
				} else if(globalPriorityInteger == null && userQueue.getPriority() != currentPriority) {
					userQueue.setPriority(currentPriority);
					userQueueService.update(userQueue);
					// 更新队列成员信息
					updateQueueMembers(true, currentPriority, exten, defaultOutline, user, globalSetting, customSetting);
				}
			}
		}
		
		// 循环检验，删除移走的UserQueue
		for(UserQueue userQueue : oldUserQueuesToQueueName) {
			// 默认是该用户不存在与队列中
			boolean hasRemoved = true;
			String username = userQueue.getUsername();
			
			// 检验是否已经移除了用户名 为username 的 用户 user ，如果没移除，则将 hasRemoved 置为 false
			for(User user : selectedCsrs) {
				if(username.equals(user.getUsername())) {
					hasRemoved = false;
					break;
				}
			}
			
			// 如果已经移除了该条 userQueue 则执行删除操作
			if(hasRemoved == true) {
				// 执行删除操作
				userQueueService.deleteById(userQueue.getId());
				
				// 如果删除了某条动态成员，并且该用户在线， 则需要向Asterisk 发送 Action
				List<User> users = userService.getUsersByUsername(userQueue.getUsername());
				User user = null;
				if(users.size() > 0) {
					user = users.get(0);
					Phone2PhoneSetting customSetting = phone2PhoneSettingService.getByUser(user.getId());
					String exten = ShareData.userToExten.get(user.getId());
					// 更新队列成员信息
					updateQueueMembers(false, 5, exten, defaultOutline, user, globalSetting, customSetting);
				}
			}
		}
		
		priorityField.setValue("");		// 将全局优先级置空
		
		// 将保存与取消按钮置为不可用状态
		setButtonsEnable(false);
	}

	/**
	 * 点击保存时，根据队列成员的变化，及外转外配置的启动情况，更新队列成员，
	 * @param isAddMember		是否是要添加队列成员
	 * @param priority			队列成员在队列中的优先级
	 * @param exten				要添加的分机成员
	 * @param defaultOutline	当前的默认外线
	 * @param csr				当前被移动的话务员对象
	 * @param globalSetting		全局外转外配置对象
	 * @param customSetting		话务员自定义的外转外对象
	 */
	private void updateQueueMembers(boolean isAddMember, Integer priority, String exten, String defaultOutline, 
			User csr, Phone2PhoneSetting globalSetting, Phone2PhoneSetting customSetting) {
		if(exten != null) {	// 话务员在线
			queueMemberRelationService.removeQueueMemberRelation(nearestSelectedQueue.getName(), exten);
			if(isAddMember) {
				queueMemberRelationService.addQueueMemberRelation(nearestSelectedQueue.getName(), exten, priority);
			} 
		} else if(defaultOutline != null && globalSetting != null) {	// 当话务员不在线时, 并且默认外线存在
			// 话务员必须有电话号，如果没有电话，就不需要添加手机成员
			String phoneNum = csr.getPhoneNumber();
			if(phoneNum == null || "".equals(phoneNum)) { 
				return;
			}
			
			// 第一步，判断全局配置外转外
			// 对于全局配置而言，添加手机号到队列需要满足 1、全局配置是开启状态，2、启动时刻 <= 当前时刻, 并且终止时刻 >= 当前时刻
			boolean isGlobalRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);
			if(isGlobalRunning) {
				// 如果当前正在进行的方式是“便捷呼叫”，则直接退出
				if(globalSetting.getIsSpecifiedPhones()) {
					return;
				} 
				// 如果全局配置是“智能呼叫”，并且处于进行中状态,而且这些话务员中包含当前被选中的CSR，此时不需要考虑话务员自定义的配置了
				for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
					if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(csr.getId())) {
						if(isAddMember){
							queueMemberRelationService.addQueueMemberRelation(nearestSelectedQueue.getName(), 
									csr.getPhoneNumber()+"@"+defaultOutline, priority);
						} else { 
							queueMemberRelationService.removeQueueMemberRelation(nearestSelectedQueue.getName(), 
									csr.getPhoneNumber()+"@"+defaultOutline);
						}
						return;	
					}
				}
			}

			// 第二步：判断话务员自定义的外转外配置(能执行到这，说明如果是“便捷呼叫”，则当前全局配置一定没有运行外转外)
			// 如果自定义外转外配置不存在，则直接返回
			if(customSetting == null) {
				return;
			}
			// 对应自定义的设置而言，添加手机号到队列，需要满足 1、全局配置的外转外呼叫方式不是打到指定的手机号，2、全局配置项中之指定的话务员中不包含当前话务员
			if(!globalSetting.getIsSpecifiedPhones()) {
				for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
					// 如果话务员包含在全局配置中，则以全局配置为主[智能呼叫：管理员对那些选中的话务员是完全控制外转外的]
					if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(csr.getId())) {
						return;
					}
				}
				
			}
			// 3、话务员持有自定义外转外的授权，4、自定义的外转外存在并是开启状态，2、启动时刻 <= 当前时刻, 并且终止时刻 >= 当前时刻
			if(globalSetting.getIsLicensed2Csr()) {
				boolean isCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
				if(isCustomRunning) {
					if(isAddMember){
						queueMemberRelationService.addQueueMemberRelation(nearestSelectedQueue.getName(), 
								csr.getPhoneNumber()+"@"+defaultOutline, priority);
					} else {	
						queueMemberRelationService.removeQueueMemberRelation(nearestSelectedQueue.getName(), 
								csr.getPhoneNumber()+"@"+defaultOutline);
					}
				}
			}
		}
	}

	/**
	 * 重新获取数据源，并刷新两个表格的显示内容
	 * @param queueName 
	 */
	private void updateTableSource(String queueName) {
		List<User> users = userService.getCsrsByDomain(domain);
		oldUserQueuesToQueueName = userQueueService.getAllByQueueName(queueName, domain);
		
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		
		for(User user : users) {
			if(oldUserQueuesToQueueName.size() == 0) {
				leftTableContainer.addAll(users);
				return;
			}
			
			// 用于标示当前user 对象是否应该加入到右侧表格当做，如果是，则将其值置为 true ,默认为不是
			boolean needAddToRightTable = false;
			for(UserQueue userQueue : oldUserQueuesToQueueName) {
				if(user.getUsername().equals(userQueue.getUsername())) {
					rightTableContainer.addBean(user);
					needAddToRightTable = true;
					break;
				}
			}
			
			// 如果当前user 不是加入到右侧表格中的，那么就将其加入左侧表格
			if(needAddToRightTable == false) {
				leftTableContainer.addBean(user);
			}
		}
	}
	
	/**
	 * 初始化左右两个表格的标题
	 */
	public void initializeTablesCaption() {
		// 如果右侧搜索区域不为空，则需要先将右侧区域置空,再初始化标题
		String rightKeywordStr = ((String) rightKeyword.getValue()).trim();
		if(!"".equals(rightKeywordStr)) {
			rightKeyword.setValue("");
		}
		
		String leftCaption = "可选成员 ( " + leftTable.getContainerDataSource().size() + " )";
		String rightCaption = "拥有成员 ( " + rightTable.getContainerDataSource().size() + " )";
		
		leftTable.setCaption(leftCaption);
		rightTable.setCaption(rightCaption);
	}

	/**
	 * 刷新显示队列的表格
	 */
	public void updateTable() {
		List<Queue> allQueues = queueService.getAllByDomain(domain, true);
		List<Queue> dynQueues = new ArrayList<Queue>();
		for(Queue queue : allQueues) {	// 将所有的动态队列加入容器
			if(queue.getDynamicmember()) {
				dynQueues.add(queue);
			}
		}
		queueContainer.removeAllItems();
		queueContainer.addAll(dynQueues);
		
		// 回显信息
		if(nearestSelectedQueue != null) {	// 如果之前选中的动态队列不为空
			boolean isExisted = false;
			for(Queue queue : dynQueues) {
				if(queue.getId() != null && queue.getId().equals(nearestSelectedQueue.getId())) {
					queueSelector.select(queue);
					isExisted = true;
					break;
				}
			}
			if(isExisted == false && dynQueues.size() > 0) {
				queueSelector.select(dynQueues.get(0));
			} else if(isExisted == false && dynQueues.size() == 0) {
				queueSelector.select(null);
			}
		} else if(dynQueues.size() > 0) {		// 如果之前没有选择任何动态队列，而该域中的动态队列不为空，则默认选择第一条动态队列
			queueSelector.select(dynQueues.get(0));
		} else {	// 如果之前没有选择任何动态队列，并且该域没有动态队列，则将动态队列选择框置为空
			queueSelector.select(null);
		}
	}
	
	/**
	 * 自动生成 优先级设置列
	 */
	private class PriorityColumnGenerate implements Table.ColumnGenerator {

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final String username = (String) source.getItem(itemId).getItemProperty("username").getValue();
			final Integer priorityValue = usernameToPriority.get(username);
			final TextField priorityField = new TextField();
			priorityField.setWidth("60px");
			priorityField.setImmediate(true);
			priorityField.setWriteThrough(false);
			priorityField.addValidator(new RegexpValidator("[1-9]\\d*", "优先级只能是 >0 的整数！"));
			
			if(priorityValue == null) {
				priorityField.setValue("5");
				usernameToPriority.put(username, 5);
			} else {	// 当切换队列时会用到这种情况
				priorityField.setValue(priorityValue.toString());
			}
			
			// 当UserQueue 对应的优先级发生变化并且合法时，则将新的值存入到 Map 集合中
			priorityField.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					String valueStr = (String) priorityField.getValue();
					if(priorityField.isValid()) {
						Integer value = Integer.valueOf(valueStr);
						usernameToPriority.put(username, value);
						
						// 表格中的数据发生变化后，将保存和取消按钮置为可用
						setButtonsEnable(true);
					} else {
						Notification formatError = new Notification("优先级 <B>"+valueStr +"</B> 格式错误, 所以被置为原值", Notification.TYPE_ERROR_MESSAGE);
						formatError.setDelayMsec(1000);
						priorityField.getApplication().getMainWindow().showNotification(formatError);
						priorityField.setValue(usernameToPriority.get(username).toString());
					}
				}
			});
			return priorityField;
		}
	}
	
}
