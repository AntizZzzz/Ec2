package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OutlinePool;
import com.jiangyifen.ec2.entity.OutlinePoolOutlineLink;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolOutlineLinkService;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.outlinepool.AddOutlinePool;
import com.jiangyifen.ec2.ui.mgr.outlinepool.EditOutlinePool;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 外线池管理 组件
 * 
 * @author xiejianwei
 *
 */
@SuppressWarnings("serial")
public class UserOutlinePoolManagement extends VerticalLayout
		implements ClickListener, ValueChangeListener, Action.Handler {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 主要组件输出
	 */
	// 部门表格组件
	private Table poolTable;
	private OutlinePool outlinePool; // 在本次之前最近被选中的外线
	private BeanItemContainer<SipConfig> outlineContainer;

	// 项目表格按钮组件
	private Button addPool;
	private Button edit;
	private Button delete;
	private FlipOverTableComponent<OutlinePool> flip;

	// 中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;

	// 表格
	private final Object[] VISIBLE_PROPERTIES = new String[] { "name", "username", "host" };
	private final String[] COL_HEADERS = new String[] { "外线名称", "注册名称", "sip服务器" };
	private Table leftTable;
	// 关键字和搜索按钮
	// 左
	private TextField leftKeyword;
	private Button leftSearch;

	private Table rightTable;
	// 右
	private TextField rightKeyword;
	private Button rightSearch;
	private BeanItemContainer<SipConfig> rightTableContainer;
	private BeanItemContainer<SipConfig> leftTableContainer;

	// 下面外线中所有成员的优先级全局设置，添加和取消按钮
	private Button save;
	private Button cancel;

	// 弹出窗口 只创建一次
	private AddOutlinePool addOutlinePool;
	private EditOutlinePool editOutlinePool;

	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private Button search;

	private String sqlSelect;
	private String sqlCount;

	/**
	 * 其他组件
	 */

	private SipConfigService sipConfigService;
	private OutlinePoolService outlinePoolService;
	private OutlinePoolOutlineLinkService outlinePoolOutlineLinkService;
	private CommonService commonService;
	private Domain domain;

	public UserOutlinePoolManagement() {
		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		outlinePoolService = SpringContextHolder.getBean("outlinePoolService");
		outlinePoolOutlineLinkService = SpringContextHolder.getBean("outlinePoolOutlineLinkService");
		commonService = SpringContextHolder.getBean("commonService");

		outlineContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
		List<SipConfig> allOutlines = sipConfigService.getAllOutlinesByDomain(domain);
		outlineContainer.addAll(allOutlines);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setSpacing(true);
		// 搜索
		constrantLayout.addComponent(buildSearchLayout());

		constrantLayout.addComponent(buildTabelAndButtonsLayout());

		// 初始化Sql语句
		search.click();

		this.addComponent(constrantLayout);

		// 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮)
		HorizontalLayout centerHLayout = createCenterHLayout();
		this.addComponent(centerHLayout);

		// 创建界面底部组件(保存、取消按钮)
		createBottomComponents();
	}

	/**
	 * 创建搜索输出（Search）
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字:")); // 添加关键字Label
		keyWord = new TextField();
		keyWord.setWidth("6em");
		keyWord.setInputPrompt("关键字");
		constrantLayout.addComponent(keyWord); // 添加关键字Field
		searchLayout.addComponent(constrantLayout);

		// 搜索按钮
		search = new Button("搜索");
		search.addListener((Button.ClickListener) this);
		searchLayout.addComponent(search);

		return searchLayout;
	}

	/**
	 * 由buttonClick调用执行简单搜索，更新翻页组件到第一页
	 */
	private void executeSearch() {
		// 读取搜索关键字
		String keyWordStr = StringUtils.trimToEmpty((String) keyWord.getValue());
		// 拼装Sql语句
		sqlSelect = "select count(o) from OutlinePool o where o.name like " + "'%" + keyWordStr + "%' and o.domain.id="
				+ domain.getId() + " order by o.id desc";

		sqlCount = "select count(o) from OutlinePool o where o.domain.id=" + domain.getId() + " and  o.name like "
				+ "'%" + keyWordStr + "%'";

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlCount.replaceFirst("count\\(o\\)", "o");

		// 按Sql更新表格显示的信息
		this.updateTable(true);

		if (poolTable != null) {
			poolTable.setValue(null);
		}
	}

	/**
	 * 由executeSearch调用更新表格内容,上层组件TabSheet调用
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	/**
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");// 节省每次创建的资源
		// 表格组件
		poolTable = new Table() { // 创建指定日期格式的表格组件
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					return sdf.format(v);
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		poolTable.setStyleName("striped");
		poolTable.addActionHandler(this);
		poolTable.setWidth("100%");
		poolTable.setHeight("30%");
		poolTable.setSelectable(true);
		poolTable.setMultiSelect(false);
		poolTable.setImmediate(true);
		poolTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		poolTable.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(poolTable);

		// 表格下面的按钮组件，包括Flip
		tabelAndButtonsLayout.addComponent(buildButtonsAndFlipLayout());
		return tabelAndButtonsLayout;
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建Table下的按钮输出，包括Flip
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsAndFlipLayout() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setSpacing(true);
		tableButtons.setWidth("100%");

		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);

		addPool = new Button("添加池");
		addPool.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(addPool);

		edit = new Button("编辑池");
		edit.setEnabled(false); // 创建时为不可用
		edit.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(edit);
		tableButtons.addComponent(tableButtonsLeft);

		delete = new Button("删除池");
		delete.setEnabled(false); // 创建时为不可用
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);
		tableButtons.addComponent(tableButtonsLeft);

		// 右侧按钮（翻页组件）
		// table 已经创建，不为null，sql已经有search按钮的click事件初始化
		flip = new FlipOverTableComponent<OutlinePool>(OutlinePool.class, outlinePoolService, poolTable, sqlSelect,
				sqlCount, null);
		poolTable.setPageLength(10); // 为了方便修改，将设置Table的操作放在这里
		flip.setPageLength(10, false);

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "poolNum", "name", "createTime", "description" };
		String[] columnHeaders = new String[] { "号码池编号", "外线池名称", "创建时间", "描述" };
		poolTable.setVisibleColumns(visibleColumns);
		poolTable.setColumnHeaders(columnHeaders);

		// Flip 组件
		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 创建中间的组件布局管理器(存放左右表格及其搜索组件，还有移动成员的操作按钮)
	 * 
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
	 * 创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
	 * 
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

		leftKeyword = new TextField();
		leftKeyword.setImmediate(true);
		leftKeyword.setInputPrompt("请输入搜索关键字");
		leftKeyword.setDescription("可按外线号码进行搜索！");
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
		leftTable = new Table("可选外线");
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTable.setPageLength(20);
		leftTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftVLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setVisibleColumns(VISIBLE_PROPERTIES);
		leftTable.setColumnHeaders(COL_HEADERS);

		return leftVLayout;
	}

	/**
	 * 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮) return
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
	 * 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件) return
	 */
	private VerticalLayout createRightComponents() {
		VerticalLayout rightVLayout = new VerticalLayout();
		rightVLayout.setSpacing(true);
		rightVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		rightVLayout.addComponent(searchHLayout);

		rightKeyword = new TextField();
		rightKeyword.setImmediate(true);
		rightKeyword.setInputPrompt("请输入搜索关键字");
		rightKeyword.setDescription("可按外线号码进行搜索！");
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
		rightTable = new Table("已选外线");
		rightTable.setStyleName("striped");
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTable.setPageLength(20);
		rightTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightVLayout.addComponent(rightTable);

		rightTableContainer = new BeanItemContainer<SipConfig>(SipConfig.class);
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
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * 
	 * @param tableFrom
	 *            从哪个表取数据
	 * @param tableTo
	 *            添加到哪个表
	 * @param isAll
	 *            是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if (tableFrom == null || tableTo == null)
			return;

		// 如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if (!isAll && ((Collection<SipConfig>) tableFrom.getValue()).size() == 0) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的外线!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}

		// 从tableFrom中取出所有选中的Csr
		Collection<SipConfig> outlines = null;
		if (isAll) {
			// 出现 java.util.ConcurrentModificationException异常，所以包装
			outlines = new ArrayList<SipConfig>((Collection<SipConfig>) tableFrom.getItemIds());
		} else {
			outlines = (Collection<SipConfig>) tableFrom.getValue();
		}

		// 通过循环来改变TableFrom和TableTo的Item
		for (SipConfig outlinePool : outlines) {
			tableFrom.getContainerDataSource().removeItem(outlinePool);
			tableTo.getContainerDataSource().addItem(outlinePool);
		}

		// 将保存与取消按钮置为 可用状态
		setButtonsEnable(true);
	}

	/**
	 * 当用户点击的添加队列按钮时，buttonClick调用显示 添加 队列窗口
	 */
	private void showAddPoolWindow() {
		if (addOutlinePool == null) {
			addOutlinePool = new AddOutlinePool(this);
		}
		this.getWindow().addWindow(addOutlinePool);
	}

	/**
	 * 由buttonClick调用，显示编辑部门的窗口
	 */
	private void showEditWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (editOutlinePool == null) {
			editOutlinePool = new EditOutlinePool(this);
			this.getWindow().addWindow(editOutlinePool);
		} else {
			this.getWindow().addWindow(editOutlinePool);
		}
	}

	private void showDeletePoolWindow(ClickEvent event) {
		OutlinePool outlinePool = (OutlinePool) poolTable.getValue();
		List<OutlinePoolOutlineLink> poolOutlineLinks = outlinePoolOutlineLinkService
				.getAllByPoolId(outlinePool.getId());
		String msg = "";
		if (poolOutlineLinks != null && poolOutlineLinks.size() > 0) {
			msg = "该号码池配置了号码,您确定删除<b>" + outlinePool.getName() + "</b>?";
		} else
			msg = "您确定删除<b>" + outlinePool.getName() + "</b>?";
		Label label = new Label(msg, Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this, "confirmDelete");
		event.getButton().getWindow().addWindow(confirmWindow);
	}

	/**
	 * 由buttonClick调用，执行生成左侧Table的过滤器,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		if (leftTableContainer == null)
			return;

		// 删除之前的所有过滤器
		leftTableContainer.removeAllContainerFilters();

		// 根据输入的搜索条件创建 过滤器
		String leftKeywordStr = (String) leftKeyword.getValue();

		Or compareAll = new Or(new Like("name", "%" + leftKeywordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		leftTableContainer.sort(new Object[] { "id" }, new boolean[] { true });
		// 收索完成后初始化表格的标题
		initializeTablesCaption();
	}

	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if (rightTableContainer == null)
			return;

		// 删除之前的所有过滤器
		rightTableContainer.removeAllContainerFilters();

		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = (String) rightKeyword.getValue();

		Or compareAll = new Or(new Like("name", "%" + rightKeywordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		rightTableContainer.sort(new Object[] { "id" }, new boolean[] { true });

		// 收索完成后初始化表格的标题
		initializeTablesCaption();
	}

	/**
	 * 由弹出窗口回调确认删除项目,此时deptService不应该为null
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed == true) {
			// 取得Table的选中状态信息
			OutlinePool outlinePool = (OutlinePool) poolTable.getValue();
			outlinePoolService.deleteById(outlinePool.getId());
			// 并使Table处于未被选中状态
			poolTable.setValue(null);
			this.updateTable(false);
		}
	}

	/**
	 * 监听搜索、高级搜索，按钮的单击事件 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (event.getButton() == search) {
			executeSearch();
		} else if (source == addPool) {
			showAddPoolWindow();
		} else if (source == edit) {
			showEditWindow();
		} else if (source == delete) {
			showDeletePoolWindow(event);
		} else if (source == leftSearch) {
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
			excuteSave();
		} else if (source == cancel) {
			// 恢复表格中的数据源
			if (outlinePool != null) {
				updateTableSource(outlinePool);
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
		if (source == poolTable) {
			if (poolTable.getValue() != null) {
				outlinePool = (OutlinePool) poolTable.getValue();
				// 应该是可以通过设置父组件来使之全部为true;
				edit.setEnabled(true);
				delete.setEnabled(true);
				updateTableSource(outlinePool);
			} else {
				// 应该是可以通过设置父组件来使之全部为false;
				edit.setEnabled(false);
				delete.setEnabled(false);
				updateTableSource(null);
			}
		} else if (source == leftKeyword) {
			leftSearch.click();
		} else if (source == rightKeyword) {
			rightSearch.click();
		}

	}

	/**
	 * 将保存 和 取消 按钮置为可用 或者不可用
	 * 
	 * @param enabled
	 */
	public void setButtonsEnable(boolean enabled) {
		save.setEnabled(enabled);
		cancel.setEnabled(enabled);
	}

	private void excuteSave() {
		try {
			Set<SipConfig> selectedCsrs = new HashSet<SipConfig>(rightTableContainer.getItemIds());
			if(selectedCsrs == null || selectedCsrs.size() <= 0) {
				this.getApplication().getMainWindow().showNotification("已选外线不能为空！", Notification.TYPE_WARNING_MESSAGE);
			}
			OutlinePool outlinePool = (OutlinePool) poolTable.getValue();
			outlinePoolOutlineLinkService.delete(outlinePool.getId());
			if (selectedCsrs != null && selectedCsrs.size() > 0) {
				for (SipConfig sip : selectedCsrs) {
					OutlinePoolOutlineLink link = new OutlinePoolOutlineLink(outlinePool.getId(), sip.getId());
					outlinePoolOutlineLinkService.save(link);
				}
			}
			ShareData.outlinePoolToOutline.put(outlinePool.getPoolNum(), selectedCsrs);
			// 将保存与取消按钮置为不可用状态
			setButtonsEnable(false);
		} catch (Exception e) {
			logger.error(e.getMessage() + "外线外线池时，保存出现异常", e);
		}
	}

	/**
	 * 重新获取数据源，并刷新两个表格的显示内容
	 * 
	 * @param queueName
	 */
	@SuppressWarnings("unchecked")
	private void updateTableSource(OutlinePool outlinePool) {
		if (outlinePool == null) {
			leftTableContainer.removeAllItems();
			rightTableContainer.removeAllItems();
		} else {

			String sipTypeSql = "(" + getSipTypeSql(SipConfigType.sip_outline) + ","
					+ getSipTypeSql(SipConfigType.gateway_outline) + ")";
			String outlineSearchSql = "Select s from SipConfig as s where s.domain.id = " + domain.getId()
					+ " and s.sipType in " + sipTypeSql;

			List<SipConfig> sipConfigs = (List<SipConfig>) commonService.excuteSql(outlineSearchSql,
					ExecuteType.RESULT_LIST);

			// 通过poolId获取对应的外线id
			List<OutlinePoolOutlineLink> outlineLinks = outlinePoolOutlineLinkService
					.getAllByPoolId(outlinePool.getId());
			if (sipConfigs != null && sipConfigs.size() > 0) {
				List<SipConfig> leftSipConfigs = new ArrayList<SipConfig>();
				List<SipConfig> rightSipConfigs = new ArrayList<SipConfig>();
				if (outlineLinks != null && outlineLinks.size() > 0) {
					for (SipConfig sipConfig : sipConfigs) {
						for (OutlinePoolOutlineLink outlineLink : outlineLinks) {
							if (outlineLink.getOutlineId().equals(sipConfig.getId())) {
								rightSipConfigs.add(sipConfig);
							}
						}
					}
					for (SipConfig sipConfig : sipConfigs) {
						if (!rightSipConfigs.contains(sipConfig)) {
							leftSipConfigs.add(sipConfig);
						}
					}
					leftTableContainer.removeAllItems();
					leftTableContainer.addAll(leftSipConfigs);
					rightTableContainer.removeAllItems();
					rightTableContainer.addAll(rightSipConfigs);
				} else {
					leftTableContainer.removeAllItems();
					leftTableContainer.addAll(sipConfigs);
					rightTableContainer.removeAllItems();
				}
			}
		}
	}

	/**
	 * jrh 根据SipConfig对象，得到用于查询sip 的类型创建收索语句语句
	 * 
	 * @param sipConfigType
	 *            sip类型
	 * @return String
	 */
	private String getSipTypeSql(SipConfigType sipConfigType) {
		String statuStr = sipConfigType.getClass().getName() + ".";
		if (sipConfigType.getIndex() == 0) {
			statuStr += "exten";
		} else if (sipConfigType.getIndex() == 1) {
			statuStr += "sip_outline";
		} else if (sipConfigType.getIndex() == 2) {
			statuStr += "gateway_outline";
		}
		return statuStr;
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
		if (outlinePool != null) { // 如果之前选中的外线不为空
			updateTableSource(outlinePool);
		}
	}

	public Table getPoolTable() {
		return poolTable;
	}

	public void setPoolTable(Table poolTable) {
		this.poolTable = poolTable;
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		// TODO Auto-generated method stub

	}

}
