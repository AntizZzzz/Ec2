package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
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
 * 静态队列成员管理  组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class StaticQueueMemberManagement extends VerticalLayout implements
		ClickListener, ValueChangeListener {

	/**
	 * 主要组件输出
	 */
	// 顶端队列选择组件
	private ComboBox queueSelector;
	private Queue nearestSelectedQueue;							// 在本次之前最近被选中的队列
	private List<StaticQueueMember> oldQueueMemberToQueueName;		// 某一具体队列所拥有的 “分机-队列”对应关系对象
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
	private final Object[] VISIBLE_PROPERTIES = new String[] {"name", "context", "type", "call_limit"};
	private final String[] COL_HEADERS = new String[] {"分机号", "上下文", "类型", "可并发数"};
	private Table leftTable;
	private Table rightTable;
	private BeanItemContainer<SipConfig> rightTableContainer;
	private BeanItemContainer<SipConfig> leftTableContainer;

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
	private Domain domain;
	private TreeMap<String, Integer> sipnameToPriority;				// 用于保存 分机 与其在队列中优先级的 对应关系 
	private QueueService queueService;								// 队列服务类
	private SipConfigService sipConfigService;						// 分机服务类
	private StaticQueueMemberService staticQueueMemberService;		// 队列静态成员服务类
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员关系管理服务类

	public StaticQueueMemberManagement() {
		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		queueService = SpringContextHolder.getBean("queueService");
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
		
		queueContainer = new BeanItemContainer<Queue>(Queue.class);
		List<Queue> allQueues = queueService.getAllByDomain(domain, true);
		for(Queue queue : allQueues) {
			if(!queue.getDynamicmember()) {
				queueContainer.addBean(queue);
			}
		}
		
		priorityError = new Notification("全局优先级 <B>格式错误</B>，只能为<B> >0 </B>的整数", Notification.TYPE_ERROR_MESSAGE);
		priorityError.setDelayMsec(1000);
		priorityError.setHtmlContentAllowed(true);
		priorityError.setPosition(Notification.POSITION_CENTERED);
		
		sipnameToPriority = new TreeMap<String, Integer>();
		
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
			if(!queue.getDynamicmember()) {
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
		leftKeyword.setInputPrompt("可按分机号和类型搜索");
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
		leftTable = new Table();
		leftTable.setCaption("可选分机");
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftVLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
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
		rightKeyword.setInputPrompt("可按分机号和类型搜索");
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
		rightTable = new Table();
		rightTable.setStyleName("striped");
		rightTable.setCaption("拥有分机");
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTable.setPageLength(16);
		rightTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightVLayout.addComponent(rightTable);

		rightTableContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
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
		
		// 添加
		save = new Button("保 存", this);
		save.setStyleName("default");
		bottomLeft.addComponent(save);
		
		// 取消
		cancel = new Button("取 消", this);
		bottomLeft.addComponent(cancel);

		// 表格底部右侧组件
		HorizontalLayout bottomRight = new HorizontalLayout();
		bottomRight.setSpacing(true);
		bottomRight.setWidth("-1px");
		bottomHLayout.addComponent(bottomRight);
		bottomHLayout.setComponentAlignment(bottomRight, Alignment.MIDDLE_RIGHT);
		
		Label priorityCaption = new Label("<B>全局设置队列成员优先级：</B>", Label.CONTENT_XHTML);
		priorityCaption.setWidth("-1px");
		bottomRight.addComponent(priorityCaption);
		
		priorityField = new TextField();
		priorityField.addListener(this);
		priorityField.setWidth("100px");
		priorityField.setImmediate(true);
		priorityField.setWriteThrough(false);
		bottomRight.addComponent(priorityField);
		
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
			public void drop(DragAndDropEvent dropEvent) {
				// 验证传递过来的是否是可传递的对象
				DataBoundTransferable transferable = (DataBoundTransferable) dropEvent.getTransferable();
				
				// 不是BeanItemContainer则不响应，直接返回
				if (!(transferable.getSourceContainer() instanceof BeanItemContainer)) {
					return;
				}

				// 获取源sourceItemId
				SipConfig sourceItem = (SipConfig) transferable.getItemId();
				
				// 选中要Drop到的targetItemId
				table.getContainerDataSource().addItem(sourceItem);
				transferable.getSourceContainer().removeItem(sourceItem);

				// 按分机号的升序排列
				leftTableContainer.sort(new Object[] {"name"}, new boolean[] {true});
				rightTableContainer.sort(new Object[] {"name"}, new boolean[] {true});
				
				// 如果是从右侧表格移除对象，则需要将对应的用户 -- 优先级对应关系一起删除
				if(isDragout == true) {
					sipnameToPriority.remove(sourceItem.getName());
				}
				
				// 每一次移除表格中的对象后，就重新设置左右两个表格的标题
				initializeTablesCaption();
				
				// 表格中的数据发生变化后，将保存和取消按钮置为可用
				setButtonsEnable(true);
			}

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
		if(!isAll && ((Collection<SipConfig>) tableFrom.getValue()).size() == 0) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的分机!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<SipConfig> sips = null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			sips = new ArrayList<SipConfig>((Collection<SipConfig>)tableFrom.getItemIds());
		} else {
			sips = (Collection<SipConfig>) tableFrom.getValue();
		}
		
		// 如果是从右侧表格中将成员移除，则需要判断要移除的分机中是否存在正被使用的分机，如果是，则不让移除，表格不发生任何变化
		if(tableFrom == rightTable) {
			for (SipConfig sip : sips) {
				for(String sipname : ShareData.extenToUser.keySet()) {
					if(sipname.equals(sip.getName())) {
						this.getApplication().getMainWindow().showNotification("分机 " +sip.getName()
								+" 正被使用，暂不能移除！", Notification.TYPE_WARNING_MESSAGE);
						return;
					}
				}
			}
		}
		
		//通过循环来改变TableFrom和TableTo的Item	
		for (SipConfig sip : sips) {
			tableFrom.getContainerDataSource().removeItem(sip);
			tableTo.getContainerDataSource().addItem(sip);
			
			// 如果源表格式 右侧表，则要重新处理用户与优先级的对应关系
			if(tableFrom == rightTable) {
				sipnameToPriority.remove(sip.getName());
			}
		}
		
		// 将保存与取消按钮置为 可用状态
		setButtonsEnable(true);
	}

	/**
	 * 由buttonClick调用，执行生成左侧Table的过滤器,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		if(leftTableContainer==null) return;
		
		// 删除之前的所有过滤器
		leftTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String leftKeywordStr = (String) leftKeyword.getValue();
		
		Or compareAll = new Or(
	             new Like("name", "%" + leftKeywordStr + "%", false), 
	             new Like("type", "%" + leftKeywordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 按分机号的升序排列
		leftTableContainer.sort(new Object[] {"name"}, new boolean[] {true});
		
		// 收索完成后初始化表格的标题
		initializeTablesCaption();
	}
	
	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if(rightTableContainer==null) return;
		
		// 删除之前的所有过滤器
		rightTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = (String) rightKeyword.getValue();

		Or compareAll = new Or(
	             new Like("name", "%" + rightKeywordStr + "%", false), 
	             new Like("type", "%" + rightKeywordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 按分机号的升序排列
		rightTableContainer.sort(new Object[] {"name"}, new boolean[] {true});

		// 收索完成后初始化表格的标题
		initializeTablesCaption();
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
			
			executeConfirm(globalPriorityStr);
		} else if (source == cancel) {
			//  恢复表格中的数据源
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
			this.getApplication().getMainWindow().removeWindow(globalPriorityConfirmWindow);
			String globalPriority = ((String) priorityField.getValue()).trim();
			Integer globalPriorityInteger = Integer.valueOf(globalPriority);
			excuteSave(globalPriorityInteger);
			// 更新表格中的内容
			updateTable();
		} else if(source == abolish) {
			this.getApplication().getMainWindow().removeWindow(globalPriorityConfirmWindow);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == queueSelector) {
			sipnameToPriority.clear();
			Queue queue = (Queue) source.getValue();
			nearestSelectedQueue = queue;
			if(queue != null) {
				// 获取该队列的所有 用户队列对应关系
				List<StaticQueueMember> queueMembers = staticQueueMemberService.getAllByQueueName(domain, queue.getName());
				if(queueMembers != null) {
					for(StaticQueueMember qm : queueMembers) {
						sipnameToPriority.put(qm.getSipname(), qm.getPriority());
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

	private void excuteSave(Integer globalPriorityInteger) {
		//更新队列拥有的CSR
		Set<SipConfig> selectedSips = new HashSet<SipConfig>((Collection<SipConfig>)rightTableContainer.getItemIds());
		
		// 循环检验，添加新的 queueMember
		for(SipConfig sip : selectedSips) {
			// 默认是该用户队列在数据库中不存在
			StaticQueueMember queueMember = null;
			Long userId = ShareData.extenToUser.get(sip.getName());
			Integer currentPriority = sipnameToPriority.get(sip.getName());
			
			// 检验是否已经存在用户 user ，如果存在则将oldPriority 置为 其老的优先级
			for(StaticQueueMember qm : oldQueueMemberToQueueName) {
				if(sip.getName().equals(qm.getSipname())) {
					queueMember = qm;
					break;
				}
			}
			
			// 不存在，则添加到数据库
			if(queueMember == null) {
				StaticQueueMember newQueueMember = new StaticQueueMember();
				newQueueMember.setDomain(domain);
				newQueueMember.setSipname(sip.getName());
				newQueueMember.setQueueName(nearestSelectedQueue.getName());
				Integer priority = (globalPriorityInteger == null) ? currentPriority : globalPriorityInteger;
				newQueueMember.setPriority(priority);
				newQueueMember = staticQueueMemberService.update(newQueueMember);
				
				// 如果添加静态队列成员成功，并且用户在线， 则需要向Asterisk 发送信息
				if(newQueueMember.getId() != null && userId != null) {
					queueMemberRelationService.addQueueMemberRelation(nearestSelectedQueue.getName(), sip.getName(), priority);
				}
			} else {	// 当前操作的queueMember 已经存在
				// 如果设置了全局优先级，则将全局的优先级与已经存在的UserQueue 的老优先级比较，如果不相等，则更新数据
				if(globalPriorityInteger != null && queueMember.getPriority() != globalPriorityInteger) {
					queueMember.setPriority(globalPriorityInteger);
					staticQueueMemberService.update(queueMember);
					
					// 更新了分机的优先级后，如果用户在线，则需要向Asterisk 发送信息
					if(userId != null) {
						queueMemberRelationService.removeQueueMemberRelation(nearestSelectedQueue.getName(), queueMember.getSipname());
						queueMemberRelationService.addQueueMemberRelation(nearestSelectedQueue.getName(), queueMember.getSipname(), globalPriorityInteger);
					}
				// 如果没设置全局优先级，则将queueMember 的老优先级与当前优先级进行比较，如果不相等，则更新数据
				} else if(globalPriorityInteger == null && queueMember.getPriority() != currentPriority) {
					queueMember.setPriority(currentPriority);
					staticQueueMemberService.update(queueMember);
					
					// 更新了分机的优先级后，如果用户在线，则需要向Asterisk 发送信息
					if(userId != null) {
						queueMemberRelationService.removeQueueMemberRelation(nearestSelectedQueue.getName(), queueMember.getSipname());
						queueMemberRelationService.addQueueMemberRelation(nearestSelectedQueue.getName(), queueMember.getSipname(), currentPriority);
					}
				}
			}
		}
		
		// 循环检验，删除移走的UserQueue
		for(StaticQueueMember qm : oldQueueMemberToQueueName) {
			// 默认是该用户不存在与队列中
			boolean hasRemoved = true;
			String sipname = qm.getSipname();
			
			// 检验是否已经移除了用户名 为username 的 用户 user ，如果没移除，则将 hasRemoved 置为 false
			for(SipConfig sip : selectedSips) {
				if(sipname.equals(sip.getName())) {
					hasRemoved = false;
					break;
				}
			}
			
			// 如果已经移除了该条 userQueue 则执行删除操作
			if(hasRemoved == true) {
				// 执行删除操作
				staticQueueMemberService.deleteById(qm.getId());
				// 如果删除了某条静态队列成员则需要向Asterisk 发送 Action
				queueMemberRelationService.removeQueueMemberRelation(nearestSelectedQueue.getName(), sipname);
			}
		}
		
		priorityField.setValue("");		// 将全局优先级置空
		
		// 将保存与取消按钮置为不可用状态
		setButtonsEnable(false);
	}

	/**
	 * 重新获取数据源，并刷新两个表格的显示内容
	 * @param queueName 
	 */
	private void updateTableSource(String queueName) {
		List<SipConfig> sips = sipConfigService.getAllExtsByDomain(domain);
		oldQueueMemberToQueueName = staticQueueMemberService.getAllByQueueName(domain, queueName);
		
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		
		for(SipConfig sip : sips) {
			if(oldQueueMemberToQueueName.size() == 0) {
				leftTableContainer.addAll(sips);
				return;
			}
			
			// 用于标示当前sip 对象是否应该加入到右侧表格当做，如果是，则将其值置为 true ,默认为不是
			boolean needAddToRightTable = false;
			for(StaticQueueMember qm : oldQueueMemberToQueueName) {
				if(qm.getSipname().equals(sip.getName())) {
					rightTableContainer.addBean(sip);
					needAddToRightTable = true;
					break;
				}
			}
			
			// 如果当前sip 不是加入到右侧表格中的，那么就将其加入左侧表格
			if(needAddToRightTable == false) {
				leftTableContainer.addBean(sip);
			}
		}
	}
	
	/**
	 * 初始化左右两个表格的标题
	 */
	public void initializeTablesCaption() {
		String leftCaption = "可选分机 ( " + leftTable.getContainerDataSource().size() + " )";
		String rightCaption = "拥有分机 ( " + rightTable.getContainerDataSource().size() + " )";
		
		leftTable.setCaption(leftCaption);
		rightTable.setCaption(rightCaption);
	}

	/**
	 * 刷新显示队列的表格
	 */
	public void updateTable() {
		List<Queue> allQueues = queueService.getAllByDomain(domain, true);
		List<Queue> staticQueues = new ArrayList<Queue>();
		for(Queue queue : allQueues) {	// 将所有的静态队列加入容器
			if(!queue.getDynamicmember()) {
				staticQueues.add(queue);
			}
		}
		queueContainer.removeAllItems();
		queueContainer.addAll(staticQueues);
		
		// 回显信息
		if(nearestSelectedQueue != null) {	// 如果之前选中的静态队列不为空
			boolean isExisted = false;
			for(Queue queue : staticQueues) {
				if(queue.getId() != null && queue.getId().equals(nearestSelectedQueue.getId())) {
					queueSelector.select(queue);
					isExisted = true;
					break;
				}
			}
			if(isExisted == false && staticQueues.size() > 0) {
				queueSelector.select(staticQueues.get(0));
			} else if(isExisted == false && staticQueues.size() == 0) {
				queueSelector.select(null);
			}
		} else if(staticQueues.size() > 0) {		// 如果之前没有选择任何静态队列，而该域中的静态队列不为空，则默认选择第一条静态队列
			queueSelector.select(staticQueues.get(0));
		} else {	// 如果之前没有选择任何静态队列，并且该域没有静态队列，则将静态队列选择框置为空
			queueSelector.select(null);
		}
	}
	
	/**
	 * 自动生成 优先级设置列
	 */
	private class PriorityColumnGenerate implements Table.ColumnGenerator {

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final String sipname = (String) source.getItem(itemId).getItemProperty("name").getValue();
			final Integer priorityValue = sipnameToPriority.get(sipname);
			final TextField priorityField = new TextField();
			priorityField.setWidth("60px");
			priorityField.setImmediate(true);
			priorityField.setWriteThrough(false);
			priorityField.addValidator(new RegexpValidator("[1-9]\\d*", "优先级只能是 >0 的整数！"));
			
			if(priorityValue == null) {
				priorityField.setValue("5");
				sipnameToPriority.put(sipname, 5);
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
						sipnameToPriority.put(sipname, value);
						
						// 表格中的数据发生变化后，将保存和取消按钮置为可用
						setButtonsEnable(true);
					} else {
						Notification formatError = new Notification("优先级 <B>"+valueStr +"</B> 格式错误, 所以被置为原值", Notification.TYPE_ERROR_MESSAGE);
						formatError.setDelayMsec(1000);
						priorityField.getApplication().getMainWindow().showNotification(formatError);
						priorityField.setValue(sipnameToPriority.get(sipname).toString());
					}
				}
			});
			
			return priorityField;
		}
		
	}
}
