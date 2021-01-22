package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.deptmanage.AddDept;
import com.jiangyifen.ec2.ui.mgr.deptmanage.EditDept;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 角色和部门的关系由角色来维护
 * <p>
 * 部门不能决定自己被哪些角色管理
 * </p>
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class DeptManagement extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private Button search;

	// 部门表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private DepartmentService deptService;
	private FlipOverTableComponent<Department> flip;

	// 项目表格按钮组件
	private Button add;
	private Button edit;
	private Button delete;

	/**
	 * 右键组件
	 */
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { ADD, EDIT, DELETE };
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private AddDept addDeptWindow;
	private EditDept editDeptWindow;

	/**
	 * 构造器
	 */
	public DeptManagement() {
		this.setWidth("100%");
		this.setMargin(true);
		
		deptService = SpringContextHolder.getBean("departmentService");

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
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
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");// 节省每次创建的资源
		// 表格组件
		table = new Table() { // 创建指定日期格式的表格组件
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					return sdf.format(v);
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.addActionHandler(this);
		table.setWidth("100%");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(table);

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

		add = new Button("添加");
		add.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(add);

		edit = new Button("编辑");
		edit.setEnabled(false); // 创建时为不可用
		edit.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(edit);
		tableButtons.addComponent(tableButtonsLeft);

		delete = new Button("删除");
		delete.setEnabled(false); // 创建时为不可用
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);
		tableButtons.addComponent(tableButtonsLeft);

		// 右侧按钮（翻页组件）
		// table 已经创建，不为null，sql已经有search按钮的click事件初始化
		flip = new FlipOverTableComponent<Department>(Department.class, deptService, table, sqlSelect, sqlCount, this);
		table.setPageLength(20); // 为了方便修改，将设置Table的操作放在这里
		flip.setPageLength(20, false);

		// 重要提示：执行此处必须将Role设置为Eager状态
		// 设置表格头部显示
		Object[] visibleColumns = new Object[] {"name", /*"roles", */"parent" };
		String[] columnHeaders = new String[] {"部门名称", /*"包含角色", */"上级部门" };
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置生成的备注信息
		this.addColumn(table);

		// Flip 组件
		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 由 buildButtonsAndFlipLayout调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("描述信息", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
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
	 * 由buttonClick调用执行简单搜索，更新翻页组件到第一页
	 */
	private void executeSearch() {
		StringBuffer sbJpql = new StringBuffer("select count(s) from Department s where 1 = 1 ");
		// 关键字过滤
		String keyWordStr = "";
		if (keyWord.getValue() != null && !"".equals(keyWord.getValue().toString().trim())) {
			keyWordStr = keyWord.getValue().toString();
			sbJpql.append(" and s.name like '%");
			sbJpql.append(keyWordStr);
			sbJpql.append("%' ");
		}

		// 生成SelectSql和CountSql语句
		sqlCount = sbJpql.toString();/*sqlGenerator.generateSelectSql();*/
		sqlSelect = sqlCount.replaceFirst("count\\(s\\)", "s") + " order by s.id desc ";/*sqlGenerator.generateCountSql();*/

		// 按Sql更新表格显示的信息
		this.updateTable(true);

		if (table != null) {
			table.setValue(null);
		}
	}

	/**
	 * 由buttonClick调用，显示添加部门的窗口
	 */
	private void showAddWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (addDeptWindow == null) {
			addDeptWindow = new AddDept(this);
			this.getWindow().addWindow(addDeptWindow);
		} else {
			this.getWindow().addWindow(addDeptWindow);
		}
	}

	/**
	 * 由buttonClick调用，显示编辑部门的窗口
	 */
	private void showEditWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (editDeptWindow == null) {
			editDeptWindow = new EditDept(this);
			this.getWindow().addWindow(editDeptWindow);
		} else {
			this.getWindow().addWindow(editDeptWindow);
		}
	}

	/**
	 * 用来供回调的方法
	 * 
	 * @param depts
	 */
	public void flipOverCallBack(List<Department> depts) {
		for (Department dept : depts) {
			dept.getRoles().size();
		}
	}

	/**
	 * 由弹出窗口使用，获取table选中的值
	 * 
	 * @return
	 */
	public Table getTable() {
		return table;
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
	 * 由弹出窗口回调确认删除项目,此时deptService不应该为null
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed == true) {
			// 取得Table的选中状态信息
			Department dept = (Department) table.getValue();
			deptService.deleteById(dept.getId());
			// 并使Table处于未被选中状态
			table.setValue(null);
			this.updateTable(false);
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
			edit.setEnabled(true);
			delete.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			edit.setEnabled(false);
			delete.setEnabled(false);
		}
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
	 * 单击按钮时触发的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == search) {
			executeSearch();
		} else if (event.getButton() == add) {
			showAddWindow();
		} else if (event.getButton() == edit) {
			showEditWindow();
		} else if (event.getButton() == delete) {
			// 部门删除
			Department dept = (Department) table.getValue();
			
			//如果有子部门则提示删除子部门
			List<Department> subDepts = deptService.getChildDepartments(dept, SpringContextHolder.getDomain());
			if(subDepts.size()>0){
				String message="请先删除子部门  {";
				int size=subDepts.size();
				for(int i=0;i<size;i++){
					if(i!=size-1){
						message+=(subDepts.get(i).getName()+"、");
					}else{
						message+=(subDepts.get(i).getName());
					}
				}
				message+="}";
				NotificationUtil.showWarningNotification(this, message);
				return;
			}
			Label label = new Label("您确定要删除部门<b>" + dept.getName() + "</b>?",
					Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
					"confirmDelete");
			event.getButton().getWindow().addWindow(confirmWindow);
		}
	}

}
