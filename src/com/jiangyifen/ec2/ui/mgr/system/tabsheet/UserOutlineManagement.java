package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserOutline;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserOutlineService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
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

/**
 * 外线与用户对应关系管理  组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class UserOutlineManagement extends VerticalLayout implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 主要组件输出
	 */
	// 顶端外线选择组件
	private ComboBox outlineSelector;
	private SipConfig nearestSelectedOutline;					// 在本次之前最近被选中的外线
	private List<UserOutline> oldUserOutlinesToSip;				// 某一具体外线所拥有的 “用户-外线”对应关系对象
	private BeanItemContainer<SipConfig> outlineContainer;
	
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
	private final Object[] VISIBLE_PROPERTIES = new String[] {"empNo", "username", "realName", "department.name" };
	private final String[] COL_HEADERS = new String[] {"工号", "用户名", "真实姓名", "部门" };
	private Table leftTable;
	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private BeanItemContainer<User> leftTableContainer;

	// 下面外线中所有成员的优先级全局设置，添加和取消按钮
	private Button save;
	private Button cancel;

	/**
	 * 其他组件
	 */
	private Domain domain;
	private UserService userService;
	private SipConfigService sipConfigService;
	private UserOutlineService userOutlineService;

	public UserOutlineManagement() {
		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		userService = SpringContextHolder.getBean("userService");
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		userOutlineService = SpringContextHolder.getBean("userOutlineService");
		
		outlineContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
		List<SipConfig> allOutlines = sipConfigService.getAllOutlinesByDomain(domain);
		outlineContainer.addAll(allOutlines);
		
		// 创建界面顶部的 外线 选择组件
		createOutlineSelectComponents();

		// 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮) 
		HorizontalLayout centerHLayout = createCenterHLayout();
		this.addComponent(centerHLayout);
		
		// 创建界面底部组件(保存、取消按钮)
		createBottomComponents();

		// 添加拖拽支持
		makeTableDragAble(new SourceIs(rightTable), leftTable, true);
		makeTableDragAble(new SourceIs(leftTable), rightTable, false);
		
		// 设置初始值
		if(allOutlines.size() > 0) {
			outlineSelector.select(allOutlines.get(0));
		}
	}

	/**
	 * 创建界面顶部的 外线 选择组件
	 */
	private void createOutlineSelectComponents() {
		HorizontalLayout maintopLayout = new HorizontalLayout();
		maintopLayout.setSpacing(true);
		this.addComponent(maintopLayout);
		this.setComponentAlignment(maintopLayout, Alignment.MIDDLE_CENTER);

		Label caption = new Label("<B>外线名称：</B>", Label.CONTENT_XHTML);
		caption.setWidth("-1px");
		maintopLayout.addComponent(caption);

		outlineSelector = new ComboBox();
		outlineSelector.setNullSelectionAllowed(false);
		outlineSelector.setImmediate(true);
		outlineSelector.setContainerDataSource(outlineContainer);
		outlineSelector.addListener(this);
		maintopLayout.addComponent(outlineSelector);
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
		leftKeyword.setDescription("可按工号、用户名及部门名称进行搜索！");
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
		leftTable.setPageLength(20);
		leftTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftVLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
		leftTable.setContainerDataSource(leftTableContainer);
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
		rightKeyword.setDescription("可按工号、用户名及部门名称进行搜索！");
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
		rightTable.setPageLength(20);
		rightTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightVLayout.addComponent(rightTable);

		rightTableContainer = new BeanItemContainer<User>(User.class);
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(VISIBLE_PROPERTIES);
		rightTable.setColumnHeaders(COL_HEADERS);
		
		return rightVLayout;
	}

	/**
	 * 创建界面底部组件(保存、取消按钮)
	 */
	private void createBottomComponents() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("-1px");
		this.addComponent(buttonsLayout);
		this.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_LEFT);

		// 添加
		save = new Button("保 存", this);
		save.setStyleName("default");
		buttonsLayout.addComponent(save);

		// 取消
		cancel = new Button("取 消", this);
		buttonsLayout.addComponent(cancel);
		
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
				 new Like("empNo", "%" + leftKeywordStr + "%", false), 
	             new Like("username", "%" + leftKeywordStr + "%", false), 
	             new Like("department.name", "%" + leftKeywordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		leftTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
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
				 new Like("empNo", "%" + rightKeywordStr + "%", false), 
	             new Like("username", "%" + rightKeywordStr + "%", false), 
	             new Like("department.name", "%" + rightKeywordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		rightTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});

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
			boolean success = excuteSave();
			if(success == true) {
				// 修改内存中当前用户所属域的用户外线对应关系
				for(Long userId : ShareData.userToExten.keySet()) {
					UserOutline uo = userOutlineService.getByUserId(userId, domain.getId());
					String exten = ShareData.userToExten.get(userId);
					if(uo != null) {
						String outline = uo.getSip().getName();
						ShareData.extenToStaticOutline.put(exten, outline);
					} else {
						ShareData.extenToStaticOutline.remove(exten);
					}
				}
				
				// 将保存与取消按钮置为不可用状态
				setButtonsEnable(false);
			}
		} else if (source == cancel) {
			// 恢复表格中的数据源
			if(nearestSelectedOutline != null) {
				updateTableSource(nearestSelectedOutline);
			} else {
				updateTableSource(null);
			}
			// 更新表格的标题
			initializeTablesCaption();
			// 将保存与取消按钮置为不可用状态
			setButtonsEnable(false);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == outlineSelector) {
			SipConfig outline = (SipConfig) source.getValue();
			nearestSelectedOutline = outline;
			
			if(outline != null) {
				updateTableSource(outline);
			} else {
				updateTableSource(null);
			}
			leftKeyword.setValue("");
			rightKeyword.setValue("");
			initializeTablesCaption();
		} else if(source == leftKeyword) {
			leftSearch.click();
		} else if(source == rightKeyword) {
			rightSearch.click();
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

	private boolean excuteSave() {
		try {
			//更新外线用有的CSR
			Set<User> selectedCsrs = new HashSet<User>(rightTableContainer.getItemIds());
			SipConfig currentOutline = (SipConfig) outlineSelector.getValue();
			
			// 循环检验，添加新的 UserOutline
			for(User user : selectedCsrs) {
				// 默认是该用户与当前外线的对应关系在数据库中不存在
				UserOutline userOutline = null;
				
				// 检验是否已经存在用户 user 与当前外线的对应关系
				for(UserOutline uo : oldUserOutlinesToSip) {
					Long userId = uo.getUser().getId();
					if(userId.equals(user.getId())) {
						userOutline = uo;
						break;
					}
				}
				
				// 如果用户 user 与当前外线的对应关系不存在，则添加到数据库
				if(userOutline == null) {
					UserOutline newUserOutline = new UserOutline();
					newUserOutline.setDomain(domain);
					newUserOutline.setUser(user);
					newUserOutline.setSip(currentOutline);
					userOutlineService.save(newUserOutline);
				} 
			}
			
			// 循环检验，删除移走的UserQueue
			for(UserOutline userOutline : oldUserOutlinesToSip) {
				// 默认是该用户已经从外线中被删除了
				boolean hasRemoved = true;
				User oldUser = userOutline.getUser();
				
				// 检验是否已经移除了 用户 oldUser ，如果没移除，则将 hasRemoved 置为 false
				for(User user : selectedCsrs) {
					if(user.getId().equals(oldUser.getId())) {
						hasRemoved = false;
						break;
					}
				}
				
				// 如果已经移除了该条 userQueue 则执行删除操作
				if(hasRemoved == true) {
					userOutlineService.deleteById(userOutline.getId());
				}
			}
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage()+ "外线外线的静态用户成员时，保存出现异常", e);
			return false;
		}
	}

	/**
	 * 重新获取数据源，并刷新两个表格的显示内容
	 * @param queueName 
	 */
	private void updateTableSource(SipConfig outline) {
		if(outline == null) {
			leftTableContainer.removeAllItems();
			rightTableContainer.removeAllItems();
		} else {
			// 修改左侧表格中的数据源 ， 由于一个用户只能对应一条外线，所以左侧表格中的用户选项，必须是没有与任何一条外线关联的用户
			List<User> users = userService.getCsrsByDomain(domain);
			List<UserOutline> allUserOutlines = userOutlineService.getAllByDomain(domain);
			for(UserOutline userOutline : allUserOutlines) {
				User user = userOutline.getUser();
				for(int i = 0; i < users.size(); i++) {
					if(user.getId().equals(users.get(i).getId())) {
						users.remove(i);
						break;
					}
				}
			}
			leftTableContainer.removeAllItems();
			leftTableContainer.addAll(users);
			
			// 修改右侧表格中的数据源
			oldUserOutlinesToSip = userOutlineService.getAllByOutline(outline, domain);
			List<User> oldUserMembers = new ArrayList<User>();
			for(UserOutline userOutline : oldUserOutlinesToSip) {
				oldUserMembers.add(userOutline.getUser());
			}
			rightTableContainer.removeAllItems();
			rightTableContainer.addAll(oldUserMembers);
		}
	}
	
	/**
	 * 初始化左右两个表格的标题
	 */
	public void initializeTablesCaption() {
		String leftCaption = "可选成员 ( " + leftTable.getContainerDataSource().size() + " )";
		String rightCaption = "拥有成员 ( " + rightTable.getContainerDataSource().size() + " )";
		
		leftTable.setCaption(leftCaption);
		rightTable.setCaption(rightCaption);
	}

	/**
	 * 刷新显示外线的表格
	 */
	public void updateTable() {
		List<SipConfig> allOutlines = sipConfigService.getAllOutlinesByDomain(domain);
		outlineContainer.removeAllItems();
		outlineContainer.addAll(allOutlines);
		
		// 回显信息
		if(nearestSelectedOutline != null) {	// 如果之前选中的外线不为空
			boolean isExisted = false;
			for(SipConfig outline : allOutlines) {
				if(outline.getId() != null && outline.getId().equals(nearestSelectedOutline.getId())) {
					outlineSelector.select(outline);
					isExisted = true;
					break;
				}
			}
			if(isExisted == false && allOutlines.size() > 0) {
				outlineSelector.select(allOutlines.get(0));
			} else if(isExisted == false && allOutlines.size() == 0) {
				outlineSelector.select(null);
			}
		} else if(allOutlines.size() > 0) {		// 如果之前没有选择任何外线，而该域中的外线不为空，则默认选择第一条外线
			outlineSelector.select(allOutlines.get(0));
		} else {	// 如果之前没有选择任何外线，并且该域没有外线，则将外线选择框置为空
			outlineSelector.select(null);
		}
	}
	
}
