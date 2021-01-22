package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.autodialout.ProjectResourceConsumer;
import com.jiangyifen.ec2.autodialout.ProjectResourceProducer;
import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.sounddialout.AddSoundDialoutTask;
import com.jiangyifen.ec2.ui.mgr.sounddialout.EditSoundDialoutTask;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
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
 * 自动外呼控制
 * <p>
 * 此页面应该只包含搜索组件、表格组件、表格下的按钮组件
 * </p>
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class SoundDialout extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private ComboBox autoDialoutTaskStatus;
	private Button search;

	// 自动外呼表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<AutoDialoutTask> flip;

	// 自动外呼表格按钮组件
	private Button add;// 新建语音群发
	private Button start;
	private Button pause;
	private Button stop;
	private Button edit;
	private Button delete;

	/**
	 * 右键组件
	 */
	private Action START = new Action("开始");
	private Action PAUSE = new Action("暂停");
	private Action STOP = new Action("停止");
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { START, PAUSE, STOP, ADD, EDIT, DELETE };
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private EditSoundDialoutTask editSoundDialoutTaskWindow;
	private AddSoundDialoutTask addSoundDialoutTaskWindow;
	/**
	 * 其他组件
	 */
	// 自动外呼资源数目信息
	private DepartmentService departmentService;
	private AutoDialoutTaskService autoDialoutTaskService;
	private QueueService queueService;
	private CommonService commonService;
	// 如果当前有选中的自动外呼则会存储当前选中的自动外呼，如果没有选中的自动外呼则会存储null
	private AutoDialoutTask autoDialoutTask;
	private Domain domain;
	private User loginUser;

	/**
	 * 构造器
	 */
	public SoundDialout() {
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
		departmentService = SpringContextHolder.getBean("departmentService");
		autoDialoutTaskService = SpringContextHolder.getBean("autoDialoutTaskService");
		commonService = SpringContextHolder.getBean("commonService");
		queueService=SpringContextHolder.getBean("queueService");
		domain=SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
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
		keyWord.setInputPrompt("自动外呼名称");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		// 自动外呼状态
		searchLayout.addComponent(new Label("&nbsp;&nbsp;状态",Label.CONTENT_XHTML));
		autoDialoutTaskStatus = new ComboBox();
		autoDialoutTaskStatus.setInputPrompt("全部");
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.NEW);
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.RUNNING);
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.PAUSE);
		autoDialoutTaskStatus.addItem(AutoDialoutTaskStatus.OVER);
		autoDialoutTaskStatus.setWidth("8em");
		searchLayout.addComponent(autoDialoutTaskStatus);

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

		//新建语音群发
		add = new Button("添加");
		add.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(add);

		// 开始
		start = new Button("开始");
		start.setEnabled(false);
		start.addListener((Button.ClickListener) this);
		start.setStyleName(StyleConfig.BUTTON_STYLE);
		tableButtonsLeft.addComponent(start);

		// 暂停
		pause = new Button("暂停");
		pause.setEnabled(false);
		pause.addListener((Button.ClickListener) this);
		pause.setStyleName(StyleConfig.BUTTON_STYLE);
		tableButtonsLeft.addComponent(pause);

		// 停止
		stop = new Button("停止");
		stop.setEnabled(false);
		stop.addListener((Button.ClickListener) this);
		stop.setStyleName(StyleConfig.BUTTON_STYLE);
		tableButtonsLeft.addComponent(stop);
		
		// 创建编辑按钮
		edit = new Button("编辑");
		edit.setEnabled(false);
		edit.setStyleName(StyleConfig.BUTTON_STYLE);
		edit.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(edit);

		// 创建删除按钮，因为取Id所以没有加是否为Null的判断
		delete = new Button("删除");
		delete.setEnabled(false);
		delete.setStyleName(StyleConfig.BUTTON_STYLE);
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);

		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<AutoDialoutTask>(
				AutoDialoutTask.class, autoDialoutTaskService, table,
				sqlSelect, sqlCount, null);
//		flip.getEntityContainer().addNestedContainerProperty("soundFile.description");
		table.setPageLength(10);
		flip.setPageLength(10, false);

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "autoDialoutTaskName",
				"autoDialoutTaskStatus", "dialoutType","concurrentLimit","createDate" };
		String[] columnHeaders = new String[] { "ID", "语音群发名", "状态","类型","并发上限","创建时间" };
		
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// 设置表格的样式
		this.setStyleGeneratorForTable(table);
		// 生成备注列
		this.addColumn(table);

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
		table.addGeneratedColumn("备注", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				Object note = table.getContainerDataSource()
						.getContainerProperty(itemId, "note");
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
	 * 由 buildTableButtons 调用，设置生成表格行的样式
	 * 
	 * @param table
	 */
	private void setStyleGeneratorForTable(final Table table) {
		// style generator
		table.setCellStyleGenerator(new CellStyleGenerator() {
			public String getStyle(Object itemId, Object propertyId) {
				//暂时保持颜色和项目的配色方案一致
				String[] styles = { StyleConfig.PROJECT_CONTROL_NEW_COLOR,
						StyleConfig.PROJECT_CONTROL_RUNNING_COLOR,
						StyleConfig.PROJECT_CONTROL_STOP_COLOR,StyleConfig.PROJECT_CONTROL_STOP_COLOR };
				if (propertyId == null) {
					AutoDialoutTaskStatus status = (AutoDialoutTaskStatus) table
							.getContainerProperty(itemId,
									"autoDialoutTaskStatus").getValue();
					return styles[status.getIndex()];
				} else {
					return null;
				}
			}
		});
	}

	/**
	 * 由buttonClick 调用，执行搜索功能
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("AutoDialoutTask");
		// 关键字过滤,自动外呼名
		String keyWordStr = keyWord.getValue().toString();
		SqlGenerator.Like autoDialoutTaskName = new SqlGenerator.Like("autoDialoutTaskName",
				keyWordStr);
		sqlGenerator.addAndCondition(autoDialoutTaskName);

		// 自动外呼状态
		AutoDialoutTaskStatus autoDialoutTaskStatu = (AutoDialoutTaskStatus) autoDialoutTaskStatus
				.getValue();
		if (autoDialoutTaskStatu != null) {
			String statuStr = autoDialoutTaskStatu.getClass().getName() + ".";
			if (autoDialoutTaskStatu.getIndex() == 0) {
				statuStr += "NEW";
			} else if (autoDialoutTaskStatu.getIndex() == 1) {
				statuStr += "RUNNING";
			} else if (autoDialoutTaskStatu.getIndex() == 2) {
				statuStr += "PAUSE";
			} else if (autoDialoutTaskStatu.getIndex() == 3) {
				statuStr += "OVER";
			}
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"autoDialoutTaskStatus", statuStr, false);
			sqlGenerator.addAndCondition(statu);
		}

		SqlGenerator.Equal type = new SqlGenerator.Equal(
				"dialoutType", AutoDialoutTask.SOUND_DIALOUT, true);
		sqlGenerator.addAndCondition(type);

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
		
		// jrh 获取当前用户管辖部门创建的语音群发
		for (Long deptId : allGovernedDeptIds) {
			SqlGenerator.Equal orEqual = new SqlGenerator.Equal(
					"creator.department.id", deptId.toString(), false);
			sqlGenerator.addOrCondition(orEqual);
		}

		// 排序
		sqlGenerator.setOrderBy("id", SqlGenerator.DESC);

		// 生成SelectSql和CountSql语句
		sqlSelect = sqlGenerator.generateSelectSql();
		sqlCount = sqlGenerator.generateCountSql();
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
		if(autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)){
			NotificationUtil.showWarningNotification(this,"语音外呼正在运行，不允许删除");
			return;
		}	
		Label label = new Label("您确定要删除语音外呼<b>"
			+ autoDialoutTask.getAutoDialoutTaskName() + "</b>?",
			Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
			"confirmDelete");
			this.getWindow().addWindow(confirmWindow);
	}

	/**
	 * 停止线程
	 */
	private void stopThreads() {
		//停止消费线程
		ProjectResourceConsumer consumerThread=(ProjectResourceConsumer)AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId());
		//应该一定不为null，但为了安全，进行判断
		if(consumerThread!=null){
			//由于销毁线程的方法有缺点和风险，故用此方式使线程自动停止
			consumerThread.setIsLoop(false);

			//如果只有当前的消费线程依赖生产线程，则停止生产线程
			ProjectResourceProducer producerThread=(ProjectResourceProducer)AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_PRODUCER_PRE+autoDialoutTask.getMarketingProject().getId());
			if(producerThread!=null){
				producerThread.removeDependConsumer(consumerThread.getName());
			}
		}
		
	}

	/**
	 * 启动线程
	 */
	private void startThreads() {
		Long projectId=autoDialoutTask.getMarketingProject().getId();
//		//==============队列计数处理========================//
//		String queueName=autoDialoutTask.getQueue().getName();
//		//此队列正在呼叫计数清0
//		AutoDialHolder.cleanCallingCount(queueName);
		
		
		//==============获取资源线程处理========================//
		//如果ThreadHolder中没有启动此项目中的资源获取线程，则启动，如果已经启动则不再启动
		//以“前缀+项目Id”作为生产线程的名字
		ProjectResourceProducer producerThread=(ProjectResourceProducer)AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_PRODUCER_PRE+projectId);
		if(producerThread==null){
			producerThread=new ProjectResourceProducer(autoDialoutTask,projectId);
		}

		//==============消费资源线程处理========================//
		//启动消费线程
		Thread consumerThread=AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId());
		//应该一定为null，但为了安全，进行判断
		if(consumerThread==null){
			//参数为生产线程名字和消费线程名字
			consumerThread=new ProjectResourceConsumer(autoDialoutTask,AutoDialHolder.AUTODIAL_PRODUCER_PRE+projectId);
		}else{
System.err.println("原线程"+AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId()+"还未停止,原线程被替换！");
			//参数为生产线程名字和消费线程名字
			consumerThread=new ProjectResourceConsumer(autoDialoutTask,AutoDialHolder.AUTODIAL_PRODUCER_PRE+projectId);
			AutoDialHolder.nameToThread.put(AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId(),consumerThread);
		}
		producerThread.addDependConsumer(consumerThread);
	}

	/**
	 * 显示添加语音群呼窗口
	 */
	private void showAddSoundDialoutTaskWindow() {
		if (addSoundDialoutTaskWindow == null) {
			addSoundDialoutTaskWindow = new AddSoundDialoutTask(this);
		}
		this.getWindow().addWindow(addSoundDialoutTaskWindow);
	}
	
	/**
	 * 由buttonClick调用，显示编辑自动外呼窗口
	 */
	private void showEditSoundDialoutTaskWindow() {
		if (editSoundDialoutTaskWindow == null) {
			editSoundDialoutTaskWindow = new EditSoundDialoutTask(this);
		}
		this.getWindow().addWindow(editSoundDialoutTaskWindow);
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
	 * 由弹出窗口回调确认删除自动外呼
	 */
	public void confirmDelete(Boolean isConfirmed) {
			if (isConfirmed == true) {
			try {
				autoDialoutTaskService.deleteById(autoDialoutTask.getId());
				//同时应该删除队列，并刷新Asterisk
				queueService.deleteById(autoDialoutTask.getQueue().getId());
				//将Queue刷新到Asterisk
				queueService.updateAsteriskQueueFile(domain); 
				// 更新Table数据，并使Table处于未被选中状态
				this.updateTable(false);
				table.setValue(null);
			} catch (Exception e) {
				NotificationUtil.showWarningNotification(this,"删除异常！");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 返回FlipOver的一个引用
	 * 
	 * @return
	 */
	public FlipOverTableComponent<AutoDialoutTask> getFlip() {
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
	 * 取得当前选中的自动外呼
	 * 
	 * @return
	 */
	public AutoDialoutTask getCurrentSelect() {
		return autoDialoutTask;
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
		if (START == action) {
			start.click();
		} else if (STOP == action) {
			stop.click();
		} else if (PAUSE == action) {
			pause.click();
		} else if (ADD == action) {
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
	 * 搜索、高级搜索、新建自动外呼、开始、停止、添加CSR/添加资源 、指派任务
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
				NotificationUtil.showWarningNotification(this,"搜索出现错误");
			}
		}else if (event.getButton() == start) {
			//判断号码是否呼完，如果呼完提示用户 
			String sql="select count(mpt) from MarketingProjectTask mpt where mpt.user=null and mpt.marketingProject.id="+autoDialoutTask.getMarketingProject().getId();
			Long count=(Long)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
			if(count<1){
				NotificationUtil.showWarningNotification(this,"资源不足，请在项目["+autoDialoutTask.getMarketingProject().getProjectName()+"]中添加资源");
				return;
			}
			
			try {
				//如果是没有在进行中的项目则调用startThreads操作
				if(!autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)){
					autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.RUNNING);
					autoDialoutTask = autoDialoutTaskService.update(autoDialoutTask);
					startThreads();
					this.updateTable(false);
				}else{
					NotificationUtil.showWarningNotification(this,"语音外呼正在运行！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"语音外呼开始异常！");
			}
		} else if (event.getButton() == stop||event.getButton() == pause) {
			// 自动外呼结束
			try {
				if(autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)){
					autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.OVER);
					autoDialoutTask = autoDialoutTaskService.update(autoDialoutTask);
					stopThreads();
					this.updateTable(false);
				}else{
					NotificationUtil.showWarningNotification(this,"语音外呼还没有运行！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"语音外呼结束异常！");
			}
		}  else if (event.getButton() == delete) {
			// 自动外呼删除
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"删除出现异常！");
			}
		} else if (event.getButton() == edit) {
			showEditSoundDialoutTaskWindow();
		} else if (event.getButton() == add) {
			showAddSoundDialoutTaskWindow();
		}
	}
	
	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		autoDialoutTask=(AutoDialoutTask)table.getValue();
		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			start.setEnabled(true);
			stop.setEnabled(true);
			pause.setEnabled(true);
			delete.setEnabled(true);
			edit.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			start.setEnabled(false);
			stop.setEnabled(false);
			pause.setEnabled(false);
			delete.setEnabled(false);
			edit.setEnabled(false);
		}
	}
}
