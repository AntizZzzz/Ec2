package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.enumtype.CommodityStatus;
import com.jiangyifen.ec2.service.eaoservice.CommodityService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.commoditymanange.AddCommodity;
import com.jiangyifen.ec2.ui.mgr.commoditymanange.EditCommodity;
import com.jiangyifen.ec2.ui.mgr.commoditymanange.EditConnectedProjectWindow;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 商品管理
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class CommodityManagement extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private ComboBox commodityStatus;
	private ComboBox connectedProjects_cb;	// jrh 关联项目
	private Button search;

	// 商品表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<Commodity> flip;

	// 商品表格按钮组件
	private Button add;// 添加商品
	private Button edit;
	private Button delete;
	private Button editProjectToCommodity_bt;	// 编辑关联项目

	/**
	 * 右键组件
	 */
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] {ADD, EDIT, DELETE };
	
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private AddCommodity addWindow;
	private EditCommodity editWindow;
	private EditConnectedProjectWindow editConnectedProjectWindow;
	
	private BeanItemContainer<MarketingProject> projectContainer;
	
	/**
	 * 其他组件
	 */
//	private CommonService commonService;
	private CommodityService commodityService;
	private MarketingProjectService marketingProjectService;
	// 如果当前有选中的商品则会存储当前选中的商品，如果没有选中的商品则会存储null
	private Commodity currentSelectCommodity;
	private Domain domain;
//	
//	private User loginUser;

	/**
	 * 构造器
	 */
	public CommodityManagement() {
		this.initService();
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索
		constrantLayout.addComponent(buildSearchLayout());
		// 初始化Sql语句
		search.click();
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());
	}

	/**
	 * 将Service进行初始化
	 */
	private void initService() {
//		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		commodityService = SpringContextHolder.getBean("commodityService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");

		projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		projectContainer.addAll(marketingProjectService.getAll(domain));
	}

	/**
	 * 创建搜索组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字:"));// 关键字
		keyWord = new TextField();// 输入区域
		keyWord.setWidth("6em");
		keyWord.setStyleName("search");
		keyWord.setInputPrompt("商品名称");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		// 商品状态
		commodityStatus = new ComboBox();
		commodityStatus.setInputPrompt("商品状态");
		commodityStatus.addItem(CommodityStatus.ONSALE);
		commodityStatus.addItem(CommodityStatus.UNDERCARRIAGE);
		commodityStatus.setWidth("8em");
		searchLayout.addComponent(commodityStatus);
		
		// jrh 关联的项目
		connectedProjects_cb = new ComboBox();
		connectedProjects_cb.setInputPrompt("关联项目");
		connectedProjects_cb.setItemCaptionPropertyId("projectName");
		connectedProjects_cb.setContainerDataSource(projectContainer); 
		connectedProjects_cb.setWidth("180");
		searchLayout.addComponent(connectedProjects_cb);

		// 搜索按钮
		search = new Button("搜索");
		search.setStyleName("small");
		search.addListener((Button.ClickListener) this);
		searchLayout.addComponent(search);

		return searchLayout;
	}

	/**
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		// 创建表格
		table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
							.format(v);
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.addActionHandler(this);
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(table);
		// 创建按钮
		tabelAndButtonsLayout.addComponent(buildTableButtons());
		return tabelAndButtonsLayout;
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建按钮的输出，为Table设置数据源
	 * 
	 * @return
	 */
	private HorizontalLayout buildTableButtons() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setWidth("100%");
		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);
		tableButtons.addComponent(tableButtonsLeft);

		//对左侧按钮的布局，两排的约束组件
		VerticalLayout leftButtonsVerticalLayout=new VerticalLayout();
		leftButtonsVerticalLayout.setSpacing(true);
		tableButtonsLeft.addComponent(leftButtonsVerticalLayout);
		
		//第一排
		HorizontalLayout firstLineLayout=new HorizontalLayout();
		firstLineLayout.setSpacing(true);
		leftButtonsVerticalLayout.addComponent(firstLineLayout);
		
		//新建
		add= new Button("添加");
		add.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(add);

		// 创建编辑按钮
		edit = new Button("编辑");
		edit.setEnabled(false);
		edit.setStyleName(StyleConfig.BUTTON_STYLE);
		edit.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(edit);

		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
		delete = new Button("删除");
		delete.setEnabled(false);
		delete.setStyleName(StyleConfig.BUTTON_STYLE);
		delete.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(delete);
		
		// 创建编辑按钮
		editProjectToCommodity_bt = new Button("编辑关联项目");
		editProjectToCommodity_bt.setEnabled(false);
		editProjectToCommodity_bt.setStyleName(StyleConfig.BUTTON_STYLE);
		editProjectToCommodity_bt.addListener((Button.ClickListener) this);
		firstLineLayout.addComponent(editProjectToCommodity_bt);

		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<Commodity>(
				Commodity.class, commodityService, table,
				sqlSelect, sqlCount, null);
		table.setPageLength(10);
		flip.setPageLength(10, false);
		
//		container=(BeanItemContainer<Commodity>)table.getContainerDataSource();
		
		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "commodityName","commodityStatus", "commodityPrice", "stockQty"};
		String[] columnHeaders = new String[] { "ID", "商品名", "商品状态", "商品价格", "库存量"};
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置表格的样式
		this.setStyleGeneratorForTable(table);
		// 生成备注列
		this.addColumn(table);
		
		// jrh 
		table.addGeneratedColumn("connectedProjects", new ConnectedProjectsGenerator());
		table.setColumnHeader("connectedProjects", "关联项目");
		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("商品描述", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				Object note = table.getContainerDataSource()
						.getContainerProperty(itemId, "description");
				String longNote = "";
				if (note != null && note.toString() != null) {
					longNote = note.toString();
				}
				String shortNote = longNote;
				if (shortNote.length() < 5) {
					shortNote += "...";
				} else {
					shortNote = shortNote.substring(0, 5) + "...";
				}
				Label label = new Label(shortNote);
				label.setDescription(longNote);
				return label;
			}
		});
	}

	/**
	 * jrh 用于自动生成商品关联的所有项目显示列
	 */
	private class ConnectedProjectsGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("connectedProjects")) {
				Commodity commodity = (Commodity) itemId;
				List<Long> projectIds = commodityService.getAllProjectIdsByBlacklistItemId(commodity.getId());
				ArrayList<String> projectNameList = new ArrayList<String>();
				for(Long projectId : projectIds) {
					MarketingProject project = marketingProjectService.get(projectId);
					if(project != null) {
						projectNameList.add(project.getProjectName());
					}
				}
				return StringUtils.join(projectNameList, ", ");
			} 
			return null;
		}
	}
	
	/**
	 * 由 buildTableButtons 调用，设置生成表格行的样式
	 * 
	 * @param table
	 */
	private void setStyleGeneratorForTable(final Table table) {
		// style generator
		table.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				return null;
			}
		});
	}

	/**
	 * 由buttonClick 调用，执行搜索功能
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("Commodity");
		// 关键字过滤,商品名
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like commodityName = new SqlGenerator.Like("commodityName",
				keyWordStr);
		sqlGenerator.addAndCondition(commodityName);

		// 商品状态
		CommodityStatus commodityStatu = (CommodityStatus) commodityStatus
				.getValue();
		if (commodityStatu != null) {
			String statuStr = commodityStatu.getClass().getName() + ".";
			if (commodityStatu.getIndex() == 0) {
				statuStr += "ONSALE";
			} else if (commodityStatu.getIndex() == 1) {
				statuStr += "UNDERCARRIAGE";
			}
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"commodityStatus", statuStr, false);
			sqlGenerator.addAndCondition(statu);
		}

		SqlGenerator.Equal domainEqual = new SqlGenerator.Equal(
				"domain.id", domain.getId().toString(), false);
		sqlGenerator.addAndCondition(domainEqual);

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		
		sqlCount = sqlGenerator.generateCountSql();

		// --------------------- jrh 按项目查询 -------------------------//
		MarketingProject project = (MarketingProject) connectedProjects_cb.getValue();
		if(project != null) {
			ArrayList<Long> commodityIds = new ArrayList<Long>();
			commodityIds.add(0L);
				
			for(Commodity commodity : commodityService.getAllByProjectId(project.getId())) {
				commodityIds.add(commodity.getId());
			}
			String commodityIdStr = StringUtils.join(commodityIds, ",");
			
			sqlSelect += " and e.id in ("+commodityIdStr+") order by e.id desc";
			sqlCount += " and e.id in ("+commodityIdStr+")";
		} else {
			sqlSelect += " order by e.id desc";
		}
		// --------------------- jrh 按项目查询 结束 -------------------------//
		
		// 更新Table，并使Table处于未选中
		this.updateTable(true);
		if (table != null) {
			table.setValue(null);
		}
	}

	/**
	 * 执行删除操作
	 */
	private void executeDelete() {
		// 在confirmDelete方法中删除与批次的关联、与用户的关联
		Label label = new Label("您确定要删除商品<b>"
				+ currentSelectCommodity.getCommodityName()+ "</b>?",
				Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmDelete");
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}
	
	/**
	 * 确定删除
	 * @param isConfirmed
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if(isConfirmed){
			commodityService.deleteById(currentSelectCommodity.getId());
			//更新Table，并使Table处于未选中
			this.updateTable(true);
			if (table != null) {
				table.setValue(null);
			}
		}
	}

	/**
	 * 添加窗口
	 */
	private void showAddWindow() {
		if (addWindow == null) {
			addWindow = new AddCommodity(this);
		}
		this.getApplication().getMainWindow().removeWindow(addWindow);
		this.getApplication().getMainWindow().addWindow(addWindow);
	}

	/**
	 * 由buttonClick调用，显示编辑商品窗口
	 */
	private void showEditWindow() {
		if (editWindow == null) {
			editWindow = new EditCommodity(this);
		}
		this.getApplication().getMainWindow().removeWindow(editWindow);
		this.getApplication().getMainWindow().addWindow(editWindow);
	}

	/**
	 * 编辑商品与项目的关联窗口
	 */
	private void showEditConnectedProjectWindow() {
		if (editConnectedProjectWindow == null) {
			editConnectedProjectWindow = new EditConnectedProjectWindow(this);
		}
		editConnectedProjectWindow.echoWindowInfo(currentSelectCommodity);
		this.getApplication().getMainWindow().removeWindow(editConnectedProjectWindow);
		this.getApplication().getMainWindow().addWindow(editConnectedProjectWindow);
	}

	/**
	 * 由executeSearch调用更新表格内容
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 * @param isToFirst
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
	 * 返回FlipOver的一个引用
	 * 
	 * @return
	 */
	public FlipOverTableComponent<Commodity> getFlip() {
		return flip;
	}

	/**
	 * 返回Table的一个引用
	 * 
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlSelect
	 */
	public void setSqlSelect(String sqlSelect) {
		this.sqlSelect = sqlSelect;
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlCount
	 */
	public void setSqlCount(String sqlCount) {
		this.sqlCount = sqlCount;
	}

	/**
	 * 取得当前选中的商品
	 * 
	 * @return
	 */
	public Commodity getCurrentSelect() {
		return currentSelectCommodity;
	}

	/**
	 * Action.Handler 实现方法
	 */
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
			add.click();
		} else if (EDIT == action) {
			edit.click();
		} else if (DELETE == action) {
			delete.click();
		}
	}

	/**
	 * 按钮单击监听器
	 * <p>
	 * 搜索、高级搜索、新建商品、开始、停止、添加CSR/添加资源 、指派任务
	 * </p>
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == search) {
			// 普通搜索
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("搜索出现错误");
			}
		} else if (event.getButton() == delete) {
			// 商品删除
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("删除出现异常！");
			}
		} else if (event.getButton() == edit) {
			showEditWindow();
		} else if (event.getButton() == add) {
			showAddWindow();
		} else if (event.getButton() == editProjectToCommodity_bt) {
			showEditConnectedProjectWindow();
		}
	}

	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			delete.setEnabled(true);
			edit.setEnabled(true);
			editProjectToCommodity_bt.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			delete.setEnabled(false);
			edit.setEnabled(false);
			editProjectToCommodity_bt.setEnabled(false);
		}
		// 维护表格中当前选中的商品
		currentSelectCommodity = (Commodity) table.getValue();
	}
}
