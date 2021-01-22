package com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVROptionType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.IVRActionService;
import com.jiangyifen.ec2.service.eaoservice.IVROptionService;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.utils.ConfirmedWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @Description 描述：语音导航菜单管理界面
 * 
 * @author  jrh
 * @date    2014年2月24日 下午5:59:14
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class IvrManagement extends VerticalLayout implements Action.Handler, ClickListener, ValueChangeListener {
	
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 语音菜单
	private final Object[] MENU_VISIBLE_PROPERTIES = new Object[] { "id", "ivrMenuName", "welcomeSoundFile", "outlines", "description", "creator", "createDate"};
	
	private final String[] MENU_COL_HEADERS = new String[] { "编号", "名称", "欢迎词", "当前关联外线", "描述信息", "创建人", "创建时间" };

	/******************************IVR Menu 相关组件 *****************************/
	
	private Table ivrMenu_tb; 											// 显示呼叫记录的表格
	private FlipOverTableComponent<IVRMenu> ivrMenuFlip; 				// IvrMenu 翻页组件
	private AddIVRMenuWindow addIVRMenuWindow;		// 添加IVRMenu 的窗口
	private EditIVRMenuWindow editIVRMenuWindow;	// 编辑IVRMenu 的窗口
	
	private Button addMenu_bt;						// menu 创建按钮
	private Button editMenu_bt;						// menu 编辑按钮
	private Button deleteMenu_bt;					// menu 删除按钮
	private Button addRootAction_bt;				// menu 添加根

	/******************************IVR 菜单 树Tree 相关组件 *****************************/
	
	private Action ADD_IVR_BRANCH = new Action("添加 流程分支");
	private Action EDIT_IVR_BRANCH = new Action("编辑 流程分支");
	private Action DELETE_IVR_BRANCH = new Action("删除 流程分支");

	private Tree ivrTree;							// 创建一个显示IVR流程的完整树
	private HorizontalLayout pnContent_hlo;

	private EditIvrBranchView editIvrBranchView;	// 编辑IVR流程分支 的界面
	private AddIvrBranchView addIvrBranchView;		// 添加IVR流程分支 的界面
	

	/****************************** 全局共用 组件或对象 **********************************/
	
	private ConfirmedWindow confirmWindow;			// 确认执行的弹屏窗口

	private IVRMenu ivrMenu;						// 语音菜单实体
	private IVRAction rootAction;					// ivrAction实体
	
	private Domain domain;							// 当前用户所属域

	private String searchMenuSql;					// IVRMenu 表格翻页组件用到的查询语句
	private String countMenuSql;					// IVRMenu 表格翻页组件用到的统计语句
	
	private IvrMenuService ivrMenuService;			// IVRMenu  service
	private IVRActionService ivrActionService;		// 语音action service
	private IVROptionService ivrOptionService;		// 语音option service
	
	public IvrManagement() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		domain = SpringContextHolder.getDomain();
		
		ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
		ivrActionService = SpringContextHolder.getBean("ivrActionService");
		ivrOptionService = SpringContextHolder.getBean("ivrOptionService");

		// 存放IVRMenu 的显示表格 和 翻页组件
		VerticalLayout menuMain_vlo = new VerticalLayout();
		menuMain_vlo.setSpacing(true);
		menuMain_vlo.setMargin(false);
		this.addComponent(menuMain_vlo);
		
		// 创建IVRMenu 的显示表格
		this.createIVRMenuTable(menuMain_vlo);

		// 创建存放IVRMenu 表格下方的按钮以及翻页组件
		this.createMenuTableFooterUI(menuMain_vlo);
		
		// 创建组界面下部的相关组件【IVR 显示树、Action 的编辑界面等】
		this.createMainBottomUI();

	}

	/**
	 * 设置表格的默认选项，当表格中存在数据时，默认选择第一行
	 * 
	 * @param table
	 */
	private void selectDefaultValues(Table table) {
		Object tableValue = null;
		for (Object obj : table.getContainerDataSource().getItemIds()) { // 默认选中ivrMenu表格第一行
			tableValue = obj;
			break;
		}
		table.setValue(tableValue);
	}

	/**
	 * 创建呼叫记录的查看表格
	 * 
	 * @param callRecordVLayout
	 */
	private void createIVRMenuTable(VerticalLayout callRecordVLayout) {
		ivrMenu_tb = createFormatColumnTable();
		ivrMenu_tb.setStyleName("striped");
		ivrMenu_tb.setSelectable(true);
		ivrMenu_tb.setNullSelectionAllowed(false);
		ivrMenu_tb.setImmediate(true);
		ivrMenu_tb.setSizeFull();
		ivrMenu_tb.addListener((ValueChangeListener)this);
		ivrMenu_tb.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		callRecordVLayout.addComponent(ivrMenu_tb);
	}

	/**
	 * 创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if (property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date) property.getValue());
				} else if ("welcomeSoundFile".equals(colId)) {
					SoundFile soundFile = (SoundFile) property.getValue();
					return soundFile.getDescName()+" - "+soundFile.getStoreName();
				} else if ("closeSoundFile".equals(colId)) {
					SoundFile soundFile = (SoundFile) property.getValue();
					return soundFile.getDescName()+" - "+soundFile.getStoreName();
				} else if ("creator".equals(colId)) {
					User creator = (User) property.getValue();
					return creator.getMigrateCsr();
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 创建存放IVRMenu 表格下方的按钮以及翻页组件
	 * 
	 * @param menuMain_vlo  父级组件
	 */
	private void createMenuTableFooterUI(VerticalLayout menuMain_vlo) {
		// 存放IVRMenu 表格下方的按键以及翻页组件
		HorizontalLayout menuTableFooter_hlo = new HorizontalLayout();
		menuTableFooter_hlo.setWidth("100%");
		menuTableFooter_hlo.setSpacing(true);
		menuMain_vlo.addComponent(menuTableFooter_hlo);
		
		// IVRMenu 表格下方左侧组件
		HorizontalLayout menuTableFooterLeft_hlo = new HorizontalLayout();
		menuTableFooterLeft_hlo.setSpacing(true);
		menuTableFooter_hlo.addComponent(menuTableFooterLeft_hlo);
		menuTableFooter_hlo.setComponentAlignment(menuTableFooterLeft_hlo, Alignment.TOP_LEFT);
		
		addMenu_bt = new Button("添加", this);
		addMenu_bt.setImmediate(true);
		menuTableFooterLeft_hlo.addComponent(addMenu_bt);

		// menu 编辑
		editMenu_bt = new Button("编辑", this);
		editMenu_bt.setImmediate(true);
		editMenu_bt.setEnabled(false);
		menuTableFooterLeft_hlo.addComponent(editMenu_bt);

		// menu删除
		deleteMenu_bt = new Button("删除", this);
		deleteMenu_bt.setImmediate(true);
		deleteMenu_bt.setEnabled(false);
		menuTableFooterLeft_hlo.addComponent(deleteMenu_bt);
		
		// menu 添加根 action 
		addRootAction_bt = new Button("添加根分支", this);
		addRootAction_bt.setImmediate(true);
		addRootAction_bt.setEnabled(false);
		menuTableFooterLeft_hlo.addComponent(addRootAction_bt);
		
		// IVRMenu 表格下方左侧组件
		HorizontalLayout menuTableFooterRight_hlo = new HorizontalLayout();
		menuTableFooterRight_hlo.setSpacing(true);
		menuTableFooter_hlo.addComponent(menuTableFooterRight_hlo);
		menuTableFooter_hlo.setComponentAlignment(menuTableFooterRight_hlo, Alignment.TOP_RIGHT);
		
		/********************************  添加 IVRMenu 翻页组件  ****************************/
		// 创建初始搜索语句
		searchMenuSql = "select m from IVRMenu as m where m.domain.id = "+ domain.getId() + " and m.ivrMenuType = com.jiangyifen.ec2.entity.enumtype.IVRMenuType.customize order by m.id desc";
		countMenuSql = "select count(m) from IVRMenu as m where m.domain.id = "+ domain.getId()+" and m.ivrMenuType = com.jiangyifen.ec2.entity.enumtype.IVRMenuType.customize";

		// 添加 IvrMenu 翻页组件
		ivrMenuFlip = new FlipOverTableComponent<IVRMenu>(IVRMenu.class,
				ivrMenuService, ivrMenu_tb, searchMenuSql, countMenuSql, null);
		menuTableFooterRight_hlo.addComponent(ivrMenuFlip);
		ivrMenu_tb.addGeneratedColumn("outlines", new OutlinesColumnGenerator());
		ivrMenu_tb.setVisibleColumns(MENU_VISIBLE_PROPERTIES);
		ivrMenu_tb.setColumnHeaders(MENU_COL_HEADERS);

		// 设置表格的显示行数
		ivrMenu_tb.setPageLength(8);
		ivrMenuFlip.setPageLength(8, false);
	}

	/**
	 * 创建组界面下部的相关组件【IVR 显示树、Action 的编辑界面等】
	 */
	private void createMainBottomUI() {
		Panel mainBottom_pn = new Panel();
		mainBottom_pn.setScrollable(true);
		this.addComponent(mainBottom_pn);
		
		// panel 的整体布局管理器
		pnContent_hlo = new HorizontalLayout();
		pnContent_hlo.setSpacing(true);
		/*pnContent_hlo.setHeight("350px"); // 设置 IVR 显示列侧的固定高度, 由于下面添加了自动适应大小的设置, 所以这里的设置值不起作用*/
		pnContent_hlo.setWidth("100%");
		pnContent_hlo.setMargin(false, false, true, false);
		mainBottom_pn.setContent(pnContent_hlo);
		// jinht 设置 Panel 面板根据内容自适应大小
		mainBottom_pn.getContent().setSizeUndefined();
	
		this.createIvrTreeUi(pnContent_hlo);	// 创建IVR流程树
	}

	/**
	 * 创建IVR流程树
	 * @param pnContent_hlo 上层容器
	 */
	private void createIvrTreeUi(HorizontalLayout pnContent_hlo) {
		// 存放tree的布局管理器
		VerticalLayout treeMain_vlo = new VerticalLayout();
		treeMain_vlo.setWidth("600px");
		treeMain_vlo.setSpacing(true);
		pnContent_hlo.addComponent(treeMain_vlo);
		pnContent_hlo.setComponentAlignment(treeMain_vlo, Alignment.TOP_LEFT);
		
		// IVR 树的标题
		Label treeCaption_lb = new Label("<b><font color='blue'>IVR树显示区：</font></b>", Label.CONTENT_XHTML);
		treeCaption_lb.setWidth("-1px");
		treeMain_vlo.addComponent(treeCaption_lb);
		treeMain_vlo.setComponentAlignment(treeCaption_lb, Alignment.TOP_LEFT);
		
		// 创建tree
		ivrTree = new Tree("IVR tree");
		ivrTree.setSizeUndefined();
		ivrTree.addListener(this);
		ivrTree.setImmediate(true);
		ivrTree.setNullSelectionAllowed(false);
		ivrTree.addActionHandler(this);
		
		treeMain_vlo.addComponent(ivrTree);
		treeMain_vlo.setComponentAlignment(ivrTree, Alignment.TOP_LEFT);
		treeMain_vlo.setExpandRatio(ivrTree, 1.0f);
	}
	
	/**
	 * @Description 描述：自动生成当前关联的外线信息
	 * 
	 * @author  jrh
	 * @date    2014年3月7日 下午3:18:40
	 * @version v1.0.0
	 */
	private class OutlinesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			IVRMenu menu = (IVRMenu) itemId;
			if(columnId.equals("outlines")) {
				return getRelatedOutlineNames(menu);
			} 
			return null;
		}
	}

	/**
	 * 根据IVRMenu 获取到其关联的外线名称
	 * @author jrh
	 * @param menu		当前被操作的IVR 菜单
	 * @return String 	返回相关联的外线名称
	 */
	private String getRelatedOutlineNames(IVRMenu menu) {
		List<SipConfig> outlines = ivrMenuService.getRelatedOutlineByIVRMenuId(menu.getId());
		StringBuffer outlineNameBf = new StringBuffer();
		for(SipConfig outline : outlines) {
			outlineNameBf.append(outline.getName());
			outlineNameBf.append(", ");
		}
		String outlineNames = outlineNameBf.toString();
		while(outlineNames.endsWith(", ")) {
			outlineNames = outlineNames.substring(0, outlineNames.length() - 2);
		}
		
		return outlineNames;
	}
	
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[]{ADD_IVR_BRANCH};
		}
		
		// 获取当前分支对应的Action
		IVRAction action = null;
		if(target instanceof IVRAction) {
			action = (IVRAction) target;
		} else if(target instanceof IVROption) {
			IVROption option = (IVROption) target;
			action = option.getNextIvrAction();
			IVROptionType optionType = option.getIvrOptionType();
			if(IVROptionType.toRepeat.equals(optionType) 
					|| IVROptionType.toReturnPre.equals(optionType)
					|| IVROptionType.toReturnRoot.equals(optionType)) {
				action = null;
			}
		}
		
		if(action != null && IVRActionType.toRead.equals(action.getActionType())) {	// 如果当前分支对应的Action 的类型是read ，则拥有添加功能
			return new Action[]{ADD_IVR_BRANCH, EDIT_IVR_BRANCH, DELETE_IVR_BRANCH};
		}
		
		return new Action[]{EDIT_IVR_BRANCH, DELETE_IVR_BRANCH};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		ivrTree.select(target);

		if(action == ADD_IVR_BRANCH) {
			IVRAction parentAction = null;
			IVRAction currentAction = null;
			Integer currentLayer = null;
			if(target instanceof IVRAction) {
				currentAction = (IVRAction) target;
				parentAction = null;
				currentLayer = 0;
			} else if(target instanceof IVROption) {
				IVROption option = (IVROption) target;
				parentAction = option.getCurrentIvrAction();
				currentAction = option.getNextIvrAction();
				currentLayer = option.getLayerNumber();
			}
			showAddIvrBranchView(parentAction, currentAction, currentLayer);
		} else if(action == DELETE_IVR_BRANCH) {
			String itemCaption = ivrTree.getItemCaption(target);
			try {
				String warningMsg = "<b><font color='red'>您确定要删除当前IVR的分支 ： <font color='blue'>["+itemCaption+"]</font> 吗？？<br/>"
						+ "注意： 删除当前 语音导航IVR分支时，同时也会将其下面的所有子节点全部删除！！！</font></b>";
				showConfirmWindow(warningMsg, "400px", "executeDeleteBranch");
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("对不起，删除语音导航IVR分支 失败，请重试！！");
				logger.error("管理员删除 IVRAction (删除IVR分支)时出现异常--->"+e.getMessage(), e);
			}
		} else if(action == EDIT_IVR_BRANCH) {
			showEditIvrBranchView("edit");
		}
		
	}

	/**
	 * 弹屏处理添加IVRMenu
	 */
	private void showAddIvrMenuWindow() {
		if(addIVRMenuWindow == null) {
			addIVRMenuWindow = new AddIVRMenuWindow(this);
		}

		addIVRMenuWindow.updateUiDataSouce();
		this.getApplication().getMainWindow().removeWindow(addIVRMenuWindow);
		this.getApplication().getMainWindow().addWindow(addIVRMenuWindow);
	}
	
	/**
	 * 弹屏处理编辑IVRMenu
	 */
	private void showEditIvrMenuWindow() {
		if(editIVRMenuWindow == null) {
			editIVRMenuWindow = new EditIVRMenuWindow(this);
		}
		
		editIVRMenuWindow.updateUiDataSouce(ivrMenu);
		this.getApplication().getMainWindow().removeWindow(editIVRMenuWindow);
		this.getApplication().getMainWindow().addWindow(editIVRMenuWindow);
	}

	/**
	 * 显示用于添加 IVR 语音导航流程的分支的界面
	 * @param operateType
	 */
	private void showAddIvrBranchView(IVRAction parentAction, IVRAction currentAction, Integer currentLayer) {
		if(editIvrBranchView != null) {
			pnContent_hlo.removeComponent(editIvrBranchView);
		}
		if(addIvrBranchView == null) {
			addIvrBranchView = new AddIvrBranchView(this);
		}
		
		pnContent_hlo.addComponent(addIvrBranchView);
		addIvrBranchView.updateUiDataSouce(ivrMenu, parentAction, currentAction, rootAction, currentLayer);
		addIvrBranchView.setVisible(true);
	}

	/**
	 * 显示用于编辑 IVR 语音导航流程的分支的界面
	 * @param operateType
	 */
	private void showEditIvrBranchView(String operateType) {
		if(addIvrBranchView != null) {
			pnContent_hlo.removeComponent(addIvrBranchView);
		}
		if(editIvrBranchView == null) {
			editIvrBranchView = new EditIvrBranchView(this);
		}

		pnContent_hlo.addComponent(editIvrBranchView);
		editIvrBranchView.updateUiDataSouce(ivrTree.getValue(), operateType);
		editIvrBranchView.setVisible(true);
	}

	/**
	 * jrh
	 * 	当修改的外线已经被其他项目占用时，需要用户进行确认后，方可保存
	 * @param selectSip
	 */
	private void showConfirmWindow(String warningMsg, String msgPxLen, String executeMethod) {
		confirmWindow = new ConfirmedWindow(warningMsg, msgPxLen, this, executeMethod);
		
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}

	/**
	 * 更新跟IVRMenu相关的组件状态，如菜单表格下方的按键的空用性
	 * 
	 * @param isEnable  是否可用
	 */
	private void updateMenuUiStatus(boolean enabled) {
		addMenu_bt.setEnabled(true);
		deleteMenu_bt.setEnabled(enabled);
		editMenu_bt.setEnabled(enabled);
	}

	/**
	 * jrh 当IVRMenu 表格中被选中的条目改变是，修改IVR 树的显示情况
	 * 
	 * @param ivrMenu
	 *            当前选中的IVRMenu
	 */
	private void makeTreeByIvrMenu(IVRMenu ivrMenu) {
		ivrTree.removeAllItems();		// 将树清空
		
		if(ivrMenu == null) {
			ivrTree.setCaption("IVR: ");
			this.rootAction = null;
		}  else {
			ivrTree.setCaption("IVR: "+ivrMenu.getIvrMenuName());
			ivrTree.setItemDescriptionGenerator(new TreeItemStyleGenerator());

			ArrayList<Long> handledActionIds = new ArrayList<Long>();
			IVRAction rootAction = ivrActionService.getRootIVRActionByIVRMenu(ivrMenu.getId()); // 取得根action

			if (rootAction != null) {
				String rootCaption = "根分支："+rootAction.getIvrActionName();
				cascadeCreateActionAndOption(null, rootAction, rootCaption, handledActionIds); // 根据当前action，及上一层次的action 添加 Tree 的分支
				addRootAction_bt.setEnabled(false);
			} else {
				addRootAction_bt.setEnabled(true);
			}
			
			this.rootAction = rootAction;
			ivrTree.select(rootAction); // tree 默认选择RootAction, 如果没有则选择 null
		}
	}
	
	/**
	 * jrh 根据当前action，及上一层次的action 添加 Tree 的分支
	 * 
	 * @param currentItem
	 * 				当前Tree 层次的action 分支
	 * @param parentItem
	 *            上一次Tree层次的action 分支
	 * @param itemCaption
	 *            当前要加的Tree分支的名称
	 * @param handledActionIds
	 *            已经加入到Tree 中的 action的编号集合
	 */
	private void cascadeCreateActionAndOption(Object currentItem, Object parentItem, String itemCaption,
			ArrayList<Long> handledActionIds) {
		IVRAction handleAction = null;						// 当前分支对应的Action 
		
		if(currentItem == null && parentItem instanceof IVRAction) {	// 表示当前处理的是IVR树的根,则应该根据parentItem 来处理
			handleAction = (IVRAction) parentItem;
			currentItem = handleAction;
		}  else if(currentItem instanceof IVROption) {
			IVROption handleOption = (IVROption) currentItem;
			handleAction = handleOption.getNextIvrAction();
		} else {
			this.getApplication().getMainWindow().showNotification("级联创建IVR 树出现异常！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		ivrTree.addItem(currentItem);
		ivrTree.setItemCaption(currentItem, itemCaption);
		ivrTree.setChildrenAllowed(currentItem, true);
		ivrTree.setParent(currentItem, parentItem);
		ivrTree.expandItem(currentItem);
		handledActionIds.add(handleAction.getId()); // 加入到 新建的action 集合中

		parentItem = currentItem;			// 为了让代码可读行增强，下面是根据增加子节点，而子节点的上级应该是上面刚加的节点
		
		// 如果当前的 tempRootAction 有按键配置，则根据按键配置创建 action、option
		List<IVROption> options = ivrOptionService.getAllByActionId(handleAction.getId(), domain.getId());
		options = ivrMenuService.sortOptionsByPressKey(options);	 // 将传入的IVROption的集合，按option的按键进行排序，排序的顺序按数字由小到大，如0、1、2、3、4、5、6、7、8、9、*
		
		for (IVROption option : options) { // 遍历，获取按键 option 对应的 下一级  action，进行创建新的action
			IVRAction nextAction = option.getNextIvrAction();
			Long nextActId = nextAction.getId();

			if (!handledActionIds.contains(nextActId)) {
				String subCaption = assembleTreeBranchName(nextAction, option);
				if (!IVRActionType.toRead.equals(nextAction.getActionType())) {
					ivrTree.addItem(option);
					ivrTree.setItemCaption(option, subCaption);
					ivrTree.setChildrenAllowed(option, false);
					ivrTree.setParent(option, parentItem);
					handledActionIds.add(nextActId); // 加入到 新建的action 集合中
				} else {
					cascadeCreateActionAndOption(option, parentItem, subCaption, handledActionIds);
				}
			} else {	// 如果当前按键对应的下一步Action 已经被处理过了，则只有三种可能 【重听、返回上一级、返回主菜单】
				String subCaption = "按键："+option.getPressNumber();
				IVROptionType optionType = option.getIvrOptionType();
				if(IVROptionType.toRepeat.equals(optionType)) {
					subCaption += " - 执行：重听";
				} else if(IVROptionType.toReturnPre.equals(optionType)) {
					subCaption += " - 执行：返回上一级";
				} else if(IVROptionType.toReturnRoot.equals(optionType)) {
					subCaption += " - 执行：返回主菜单";
				}
				ivrTree.addItem(option);
				ivrTree.setChildrenAllowed(option, false);
				ivrTree.setParent(option, parentItem);
				ivrTree.setItemCaption(option, subCaption);
			}
		}
	}
	
	/**
	 * 
	 * @Description 描述：自动生成 IVR树的枝干的描述信息
	 * 
	 * @author jrh
	 * @date 2014年2月25日 下午6:21:30
	 * @version v1.0.0
	 */
	private class TreeItemStyleGenerator implements ItemDescriptionGenerator {

		@Override
		public String generateDescription(Component source, Object itemId,
				Object propertyId) {
			if (itemId instanceof IVRAction) {
				IVRAction action = (IVRAction) itemId;
				String name = StringUtils.trimToEmpty(action.getIvrActionName());
				String description = StringUtils.trimToEmpty(action.getDescription());
				return name+"---"+description;
			} else if(itemId instanceof IVROption) {
				IVROption option = (IVROption) itemId;
				IVRAction nextAction = option.getNextIvrAction();
				if(nextAction != null) {
					String name = StringUtils.trimToEmpty(nextAction.getIvrActionName());
					String description = StringUtils.trimToEmpty(nextAction.getDescription());
					return name+"---"+description;
				} else {
					String name = StringUtils.trimToEmpty(option.getIvrOptionName());
					String description = StringUtils.trimToEmpty(option.getDescription());
					return name+"---"+description;
				}
			}

			return "";
		}
		
	}

	/**
	 * jrh 重新组装 IVR树的各分支的名称
	 * 
	 * @param action
	 *            分支上对应的Action 对象
	 * @param option
	 *            该分支对应的按键信息
	 * @return String 返回分支名次
	 */
	private String assembleTreeBranchName(IVRAction action, IVROption option) {
		StringBuffer nameBf = new StringBuffer();
		nameBf.append("按键：");
		nameBf.append(option.getPressNumber());
		nameBf.append(" - 执行：");

		nameBf.append(action.getActionType().getName());
		nameBf.append(" [");
		if(IVRActionType.toExten.equals(action.getActionType())) {
			nameBf.append(action.getExtenName());
		} else if(IVRActionType.toQueue.equals(action.getActionType())) {
			nameBf.append(action.getQueueName());
		} else if(IVRActionType.toMobile.equals(action.getActionType())) {
			nameBf.append(action.getMobileNumber());
		} else if(IVRActionType.toPlayback.equals(action.getActionType())) {
			SoundFile sound = action.getSoundFile();
			if(sound != null) {
				nameBf.append(sound.getDescName()+"-"+sound.getStoreName());
			} else {
				nameBf.append("没选语音");
			}
		} else if(IVRActionType.toRead.equals(action.getActionType())) {
			SoundFile sound = action.getSoundFile();
			if(sound != null) {
				nameBf.append(sound.getDescName()+"-"+sound.getStoreName());
			} else {
				nameBf.append("没选语音");
			}
		} else if(IVRActionType.toReadForAgi.equals(action.getActionType())) {
			SoundFile sound = action.getSoundFile();
			if(sound != null) {
				nameBf.append(sound.getDescName()+"-"+sound.getStoreName());
			} else {
				nameBf.append("没选语音");
			}
			nameBf.append("处理--");
			nameBf.append(action.getAgiDescription());
		} else if(IVRActionType.toVoicemail.equals(action.getActionType())) {
			nameBf.append(action.getQueueName());
		}
		nameBf.append("]");
		return nameBf.toString();
	}

	/**
	 * jrh 执行删除IVRMenu 
	 * @param ivrMenu
	 */
	public void executeDeleteMenu() {
		try {
			IVRMenu menu = (IVRMenu) ivrMenu_tb.getValue();	// 执行删除
			Long ivrMenuId = menu.getId();
			ivrMenuService.deleteById(ivrMenuId);
			ivrTree.setValue(null);
			
			// 删除成功之后，更新内存中的信息
			ShareData.ivrMenusMap.remove(ivrMenuId);	// 从 ivrMenusMap 移除 当前删除的对象
			
			for(Long outlineId : ShareData.outlineIdToIvrLinkMap.keySet()) {	// 从 outlineIdToIvrLinkMap 中将外线对应的Link 删除
				ArrayList<OutlineToIvrLink> otilList = ShareData.outlineIdToIvrLinkMap.get(outlineId);
				synchronized (otilList) {
					OutlineToIvrLink removeLink = null;
					for(OutlineToIvrLink link : otilList) {
						if(link.getIvrMenuId().equals(ivrMenuId)) {
							removeLink = link;
							break;
						}
					}
					if(removeLink != null) {
						otilList.remove(removeLink);
					}
				}
			}
			
			this.refreshTable(true);	// 刷新界面到第一页
			this.getApplication().getMainWindow().showNotification("删除语音导航IVR 成功！！");
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("对不起，删除语音导航流程 失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("jrh 管理员执行上传 IVRMenu 出现异常！---->"+e.getMessage(), e);
		}
	}

	/**
	 * jrh 执行删除IVRMenu 
	 * @param ivrMenu
	 */
	public void executeDeleteBranch() {
		try {
			Object branch = ivrTree.getValue();
			if(branch instanceof IVRAction) {
				IVRAction action = (IVRAction) ivrTree.getValue();	// 执行删除
				ivrActionService.delete(action);
			} else if(branch instanceof IVROption) {
				IVROption option = (IVROption) ivrTree.getValue();
				ivrOptionService.delete(option);		// 执行删除
			}

			makeTreeByIvrMenu(ivrMenu);
			this.getApplication().getMainWindow().showNotification("删除语音导航IVR的分支 成功！！");
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("对不起，删除语音导航IVR的分支 失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("jrh 管理员执行上传 IVRAction 跟Option时 出现异常！---->"+e.getMessage(), e);
		}
	}
	
	/**
	 * 当对分支做了增、删、改后，需要重新构建IVR 树
	 */
	public void updateIVRTree() {
		IVRMenu menu = (IVRMenu) ivrMenu_tb.getValue();
		makeTreeByIvrMenu(menu);
	}
	
	/**
	 * 当管理员取消添加新的流程分支时，需要将addIvrBranchView 移除，并根据情况，显示 editIvrBranchView
	 */
	public void updateIVRTreeRightView() {
		Object obj = ivrTree.getValue();
		if(addIvrBranchView != null) {
			pnContent_hlo.removeComponent(addIvrBranchView);
		}
		if(editIvrBranchView != null) {
			pnContent_hlo.addComponent(editIvrBranchView);
			editIvrBranchView.setVisible(obj != null);
		}
	}

	/**
	 * 是否要刷新到首页
	 * 
	 * @param refreshToFirstPage
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if (refreshToFirstPage == true) {
			ivrMenuFlip.refreshToFirstPage();
			selectDefaultValues(ivrMenu_tb);
		} else {
			ivrMenuFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == ivrMenu_tb) {
			ivrMenu = (IVRMenu) source.getValue();
			
			updateMenuUiStatus(ivrMenu != null);	// 更新跟IVRMenu相关的组件状态，如菜单表格下方的按键的空用性

			makeTreeByIvrMenu(ivrMenu);				// 建IVR 树的各分支
		} else if (source == ivrTree) {
			Object selectedItem = ivrTree.getValue();
			
			if(selectedItem != null) {
				showEditIvrBranchView("scan");
			} else if(selectedItem == null) {
				editIvrBranchView.setVisible(false);
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == addMenu_bt) {
			showAddIvrMenuWindow();
		} else if(source == editMenu_bt) {
			showEditIvrMenuWindow();
		} else if(source == deleteMenu_bt) {
			try {
				String outlineNames = getRelatedOutlineNames(ivrMenu);		// 根据IVRMenu 获取到其关联的外线名称
				String warningMsg = "<b><font color='red'>您确定要删除当前IVR ： <font color='blue'>["+ivrMenu.getIvrMenuName()+"]</font> 吗？？<br/></font></b>";
				if(outlineNames.length() > 0) {
					warningMsg = "<b><font color='red'>删除当前IVR ： <font color='blue'>["+ivrMenu.getIvrMenuName()+"]</font> ，"
							+ "该IVR 当前关联了以下外线:<font color='blue'>["+outlineNames+"]</font><br/> 如果删除可能会导致经上述外线呼入时找不到IVR，您确定要删除吗？？<br/></font></b>";
				}
				showConfirmWindow(warningMsg, "450px", "executeDeleteMenu");
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("对不起，删除语音导航IVR 失败，请重试！！");
				logger.error("管理员删除 IVRMenu 语音导航导航菜单时出现异常--->"+e.getMessage(), e);
			}
		} else if(source == addRootAction_bt) {
			showAddIvrBranchView(null, null, null);
		}
	}

	public Tree getIvrTree() {
		return ivrTree;
	}

	public void setIvrTree(Tree ivrTree) {
		this.ivrTree = ivrTree;
	}

	public IVRMenu getIvrMenu() {
		return ivrMenu;
	}

	public void setIvrMenu(IVRMenu ivrMenu) {
		this.ivrMenu = ivrMenu;
	}
	
	
}
