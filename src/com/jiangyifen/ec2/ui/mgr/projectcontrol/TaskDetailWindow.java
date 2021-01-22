package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.CustomerQuestionnaireFinishStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 任务详情查看窗口
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class TaskDetailWindow extends Window implements Button.ClickListener,ValueChangeListener,Window.CloseListener{
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

		
	private static String SELECT_CURRENT_PAGE="全选当前页";
	private static String DESELECT_CURRENT_PAGE="取消选中当前页";
	private static String DESELECT_ALL="取消选中全部";
	private boolean isEncryptMobile = true; // 默认使用加密的电话号码和手机号
/**
 * 主要组件
 */
	//表格
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private CommonService commonService;
	private FlipOverTableComponent<MarketingProjectTask> flip;
	private AssignByCsr2 assignByCsr2Window;
	private TaskDetailSimpleFilter simpleFilter;
	
	//左侧按钮组件
	private ComboBox selectBox;
	private Button enable;
	private Button disable;
	private Button delete;
	private Button assign;
	private Label selectCount;
	private String[] selectValue = new String[] {SELECT_CURRENT_PAGE,DESELECT_CURRENT_PAGE,DESELECT_ALL};
	
/**
 * 其他组件
 */
	private Map<Long,MarketingProjectTask> selectedTasks=new HashMap<Long,MarketingProjectTask>();
	//持有资源导入组件的引用
	private ProjectControl projectControl;
	private MarketingProject currentProject;
	private TelephoneService telephoneService;			// 电话号码服务类
	
	/**
	 * 批次的缓存
	 */
	private Map<Long,String> batchIdToName=new HashMap<Long, String>(); //批次不会太多，所已没有清理动作
	
	
/**
 * 构造器
 * @param resourceImport 持有父组件的引用
 */
	public TaskDetailWindow(ProjectControl projectControl) {
		this.center();
		this.setModal(true);
		this.addListener((Window.CloseListener)this);
		this.setResizable(false);
		this.projectControl=projectControl;
		telephoneService=SpringContextHolder.getBean("telephoneService");
		
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);

		
		currentProject=(MarketingProject)projectControl.getTable().getValue();
		commonService=SpringContextHolder.getBean("commonService");
		
		//创建一个windowContent的组件
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		windowContent.setSizeUndefined();
		windowContent.setSpacing(true);
		windowContent.setMargin(false, true, true, true);
		this.setContent(windowContent);
		
		// 搜索组件
		simpleFilter = new TaskDetailSimpleFilter(this);
		windowContent.addComponent(simpleFilter);
		// 初始化Sql语句
		simpleFilter.getSearchButton().click();
		
		// 表格和按钮
		windowContent.addComponent(buildTabelAndButtonsLayout());
		
		if(selectedTasks.size()<1){
			assign.setEnabled(false);
			delete.setEnabled(false);
			enable.setEnabled(false);
			disable.setEnabled(false);
		}
		
		// 设置Button样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	
	/**
	 *  创建表格和按钮输出（Table）
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSizeUndefined();
		tabelAndButtonsLayout.setSpacing(true);
		//创建表格
		table = createFormatColumnTable();
		table.setStyleName("striped");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setSizeUndefined();
		tabelAndButtonsLayout.addComponent(table);
		
		//创建按钮
		tabelAndButtonsLayout.addComponent(buildButtonsLayout());
		
		return tabelAndButtonsLayout;
	}

	
	/**
	 * jrh
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@SuppressWarnings("unchecked")
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(v);
				} else if("isFinished".equals(colId)) {
					Boolean isFinished =(Boolean)property.getValue();
					if(isFinished==null) return "未完成"; //对于是否完成的处理
					return isFinished?"已完成":"未完成";
				} else if("isAnswered".equals(colId)) {
					Boolean isAnswered =(Boolean)property.getValue();
					if(isAnswered==null) return "未接通"; //对于是否接通的处理
					return isAnswered?"已接通":"未接通";
				} else if("isUseable".equals(colId)) {
					Boolean isUseable =(Boolean)property.getValue();
					if(isUseable==null) return "未启用";
					return isUseable?"已启用":"未启用";
				} else if("user".equals(colId)) {
					User user =(User)property.getValue();
					if(user==null) return "";
					String username=user.getUsername();
					String empNo=user.getEmpNo();
					if(StringUtils.isEmpty(username)){
						return empNo+"-"+username;
					}else{
						return empNo;
					}
				} else if("customerResource.accountManager".equals(colId)) {
					User user =(User)property.getValue();
					if(user==null) return "";
					String username=user.getUsername();
					String empNo=user.getEmpNo();
					if(StringUtils.isEmpty(username)){
						return empNo+"-"+username;
					}else{
						return empNo;
					}
				}else if("customerQuestionnaireFinishStatus".equals(colId)) {
					CustomerQuestionnaireFinishStatus customerQuestionnaireFinishStatus =(CustomerQuestionnaireFinishStatus)property.getValue();
					if(customerQuestionnaireFinishStatus==null) return "";
					return customerQuestionnaireFinishStatus.getName();
				}else if("customerResource.telephones".equals(colId)) {
					Set<Telephone> telephones =(Set<Telephone>)property.getValue();
					List<String> telephoneList=new ArrayList<String>();
					for(Telephone telephone:telephones){
						if(isEncryptMobile){
							telephoneList.add(telephoneService.encryptMobileNo(telephone.getNumber()));
						}else{
							telephoneList.add(telephone.getNumber());
						}
					}
					return StringUtils.join(telephoneList, ",");
				} else if("batchId".equals(colId)) {
					Long batchId=(Long)property.getValue();
					if(batchId==null) return "";
					String batchName=batchIdToName.get(batchId);
					if(batchName==null){
						//Search DB
						CustomerResourceBatch batch=(CustomerResourceBatch)commonService.get(CustomerResourceBatch.class, batchId);
						//Store key value
						batchId=batch.getId();
						batchName=batch.getBatchName();
						batchIdToName.put(batchId, batchName);
					}
					return batchName;
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 由buildTabelAndButtonsLayout调用，创建Table下的按钮输出
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private HorizontalLayout buildButtonsLayout() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setWidth("100%");
		tableButtons.setSpacing(true);

		//添加选中全部页、取消选中全部页、指派按钮、选中数量Label
		//添加左侧按钮组件
		HorizontalLayout leftButtons = new HorizontalLayout();
		leftButtons.setSpacing(true);
		tableButtons.addComponent(leftButtons);
		tableButtons.setComponentAlignment(leftButtons, Alignment.MIDDLE_LEFT);
		
		selectBox = new ComboBox();
		selectBox.setWidth("100");
		selectBox.setInputPrompt("---请选择---");
		selectBox.addListener(this);
		for (int i = 0; i < selectValue.length; i++) {
			selectBox.addItem(selectValue[i]);
		}
		leftButtons.addComponent(selectBox);

		enable=new Button("启用勾选项");
		enable.addListener((Button.ClickListener)this);
		leftButtons.addComponent(enable);
		
		disable=new Button("禁用勾选项");
		disable.addListener((Button.ClickListener)this);
		leftButtons.addComponent(disable);
		
		delete=new Button("删除勾选项");
		delete.addListener((Button.ClickListener)this);
		leftButtons.addComponent(delete);

		assign=new Button("指派勾选项");
		assign.addListener((Button.ClickListener)this);
		leftButtons.addComponent(assign);

		selectCount=new Label("选中数量-0");
		leftButtons.addComponent(selectCount);
		
		// 右侧按钮（翻页组件）
		flip =new FlipOverTableComponent<MarketingProjectTask>(MarketingProjectTask.class,
				commonService, table, sqlSelect, sqlCount,null);
		table.setPageLength(15);
		flip.setPageLength(15, true);
		
		flip.getEntityContainer().addNestedContainerProperty("customerResource.id");
		flip.getEntityContainer().addNestedContainerProperty("customerResource.name");
		flip.getEntityContainer().addNestedContainerProperty("customerResource.telephones");
		flip.getEntityContainer().addNestedContainerProperty("customerResource.accountManager");

		//设置表格头部显示 
		Object[] visibleColumns=new Object[] { "id", "isFinished","isAnswered","orderTime","orderNote","distributeTime","lastUpdateDate","user",
				"isUseable","lastStatus", "customerQuestionnaireFinishStatus","customerResource.id","customerResource.name","customerResource.telephones", 
				"customerResource.accountManager","batchId"};
		String[] columnHeaders=new String[] { "id", "完成情况", "是否接通","预约时间","预约备注","分配日期","最近更新日期","任务坐席",
				"是否启用","任务状态","问卷完成情况","客户编号","客户姓名","客户电话", "客户经理","资源批次"};
		
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		addColumn1(table);
		addColumn(table);
		
		//添加翻页组件
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
		table.addGeneratedColumn("选中状态", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				final MarketingProjectTask task=(MarketingProjectTask)itemId;
				
				final CheckBox check = new CheckBox();
				check.setImmediate(true);
				if(selectedTasks.keySet().contains(task.getId())) {
					check.setValue(true);
				}
				
				check.addListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						Boolean value = (Boolean) check.getValue();
						if(value == false) {
							selectedTasks.remove(task.getId());
						} else {
							selectedTasks.put(task.getId(), task);
						}
						updateCountInfo();
					}
				});
				
	        	return check;
			}
		});
	}
	
	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn1(final Table table) {
		table.addGeneratedColumn("操作", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				final MarketingProjectTask task=(MarketingProjectTask)itemId;
	
				MenuBar menuBar=new MenuBar();
				menuBar.addStyleName("nobackground");
				MenuItem menuItem = menuBar.addItem("操作", null);
				menuItem.addItem("禁用", new MenuBar.Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						task.setIsUseable(false);
						commonService.update(task);
						TaskDetailWindow.this.updateTable(false);
						NotificationUtil.showWarningNotification(TaskDetailWindow.this, "成功禁用任务！");
					}
				});
				
				menuItem.addItem("启用", new MenuBar.Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						task.setIsUseable(true);
						commonService.update(task);
						TaskDetailWindow.this.updateTable(false);
						NotificationUtil.showWarningNotification(TaskDetailWindow.this, "成功启用任务！");
					}
				});
				
				menuItem.addItem("删除", new MenuBar.Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						//删除关联
						Long batchId=task.getBatchId();
						Long resourceId=task.getCustomerResource().getId();
						if(batchId!=null){
							String sql="delete from ec2_customer_resource_ec2_customer_resource_batch where customerresources_id="+resourceId+" and customerresourcebatches_id="+batchId;
							commonService.excuteNativeSql(sql, ExecuteType.UPDATE);
						}
						
						selectedTasks.remove(task.getId());
						updateCountInfo();
						
						//删除任务
						commonService.delete(MarketingProjectTask.class,task.getId());
						TaskDetailWindow.this.updateTable(false);
						NotificationUtil.showWarningNotification(TaskDetailWindow.this, "成功删除任务！");
					}

				});
				menuItem.addItem("指派", new MenuBar.Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						Set<Long> taskSet=new HashSet<Long>();
						taskSet.add(task.getId());
						showAssignByCsrWindow(taskSet);
					}
				});
				
				return menuBar;
			}
		});
	}

	/**
	 * 由buttonClick调用 显示按照CSR指派任务的弹出窗口
	 */
	private void showAssignByCsrWindow(Set<Long> taskSet) {
		if (assignByCsr2Window == null) {
			try {
				assignByCsr2Window = new AssignByCsr2(projectControl,TaskDetailWindow.this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		
		assignByCsr2Window.setSelectedTasksId(taskSet);
		
		this.getApplication().getMainWindow().removeWindow(assignByCsr2Window);
		this.getApplication().getMainWindow().addWindow(assignByCsr2Window);
	}
	
	/**
	 * 由buttonClick调用执行简单搜索，更新翻页组件到第一页
	 */
	private void executeSearch() {
		this.initNewData();
		
		this.updateTable(true);
		if(table!=null){
			table.setValue(null);
		}
	}
	
	/**
	 * 由executeSearch调用，重新初始化重要数据，此处为Batch
	 */
	private void initNewData() {
		//设置Window的Caption
		this.setCaption("项目 "+currentProject.getProjectName()+" -- 任务详情");
	}

	/**
	 * attach 方法，每次弹出新窗口，都应该显示相应批次的信息
	 */
	@Override
	public void attach() {
		super.attach();
		this.executeSearch();
	}
	
	/**
	 * 由executeSearch调用更新表格内容
	 * @param isToFirst 是否更新到第一页，default 是 false
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
	 * 取得Table
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 由构造器调用， 设置界面按钮显示
	 */
	public void setButtonsStyle(String style) {
		style="small";
		assign.setStyleName(style);
		delete.setStyleName(style);
		enable.setStyleName(style);
		disable.setStyleName(style);
	}
	
	//选中当前页
	public void selectCurrent() {
		BeanItemContainer<MarketingProjectTask> beanItemContainer = flip.getEntityContainer();
		Collection<MarketingProjectTask> tasks = beanItemContainer.getItemIds();
		for(MarketingProjectTask task:tasks){
			selectedTasks.put(task.getId(),task);
		}
		this.updateTable(false);
		updateCountInfo();
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
	 * 取得当前的项目
	 * @return
	 */
	public MarketingProject getCurrentProject() {
		return currentProject;
	}
	
	/**
	 * 更新选中数量的计数器计数信息
	 */
	private void updateCountInfo() {
		// 修改删除按钮
		if(selectedTasks.size() > 0) {
			delete.setEnabled(true);
			assign.setEnabled(true);
			enable.setEnabled(true);
			disable.setEnabled(true);
		} else {
			delete.setEnabled(false);
			assign.setEnabled(false);
			enable.setEnabled(false);
			disable.setEnabled(false);
		}
		
		selectCount.setValue("选中数量"+selectedTasks.size());
	}

	//取消选中当前页
	public void deSelectCurrent() {
		BeanItemContainer<MarketingProjectTask> beanItemContainer = flip.getEntityContainer();
		Collection<MarketingProjectTask> tasks = beanItemContainer.getItemIds();
		for(MarketingProjectTask task:tasks){
			selectedTasks.remove(task.getId());
		}
		this.updateTable(false);
		updateCountInfo();
	}
	
	//取消选中当前页
	public void deSelectAll() {
		selectedTasks.clear();
		this.updateTable(false);
		updateCountInfo();
	}
	
	/**
	 * 取得翻页组件
	 * @return
	 */
	public FlipOverTableComponent<MarketingProjectTask> getFlip() {
		return flip;
	}
	
	/**
	 * 是否确认删除
	 * @param isConfirm
	 */
	public void confirmDelete(Boolean isConfirm){
		if(isConfirm){
			for(MarketingProjectTask task:selectedTasks.values()){
				//在批次中删除
				Long resourceId=task.getCustomerResource().getId();
				Long batchId=task.getBatchId();
				if(resourceId!=null&&batchId!=null){//如果资源和批次Id都不为空，执行原生Sql的删除操作
					String sql="delete from ec2_customer_resource_ec2_customer_resource_batch where customerresources_id="+resourceId+" and customerresourcebatches_id="+batchId;
					commonService.excuteNativeSql(sql, ExecuteType.UPDATE);
				}
				
				// 在任务中删除 -- 任务中多个批次共享一条资源有bug
				String nativeSql="delete from ec2_marketing_project_task where id="+task.getId();
				commonService.excuteNativeSql(nativeSql, ExecuteType.UPDATE);
			}
			int todeleteCount=selectedTasks.size();
			//清空选中的组
			selectedTasks.clear();
			
			this.updateTable(false);
			this.updateCountInfo();
			NotificationUtil.showWarningNotification(this, "成功删除"+todeleteCount+"个勾选项！");
		}
	}
	

	/**
	 * 监听窗口关闭事件
	 * <p>窗口关闭时更新任务条数</p>
	 */
	@Override
	public void windowClose(CloseEvent e) {
		projectControl.updateProjectResourceInfo();
	}
	
	
	@Override
	public void valueChange(ValueChangeEvent event) {
			if (selectBox.getValue() != null&& selectBox.getValue().equals(SELECT_CURRENT_PAGE)) {
				selectCurrent();
			} else if (selectBox.getValue() != null&& selectBox.getValue().equals(DESELECT_CURRENT_PAGE)) {
				deSelectCurrent();
			} else if (selectBox.getValue() != null&& selectBox.getValue().equals(DESELECT_ALL)) {
				deSelectAll();
			}
			//让ComboBox复原
			selectBox.setValue(null);
	}
	

	/**
	 * 监听搜索、添加、编辑、删除 的事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==assign){
			Set<Long> selectedTaskSet = selectedTasks.keySet();
			Set<Long> toAssignTaskSet = new HashSet<Long>();
			for(Long taskId:selectedTaskSet){
				/*MarketingProjectTask task = selectedTasks.get(taskId);*/
//				//已分配的资源不再分配  chb 20131031 姜吉祥
//				if(task==null||task.getUser()!=null){
//					continue;
//				}
				toAssignTaskSet.add(taskId);
			}
			showAssignByCsrWindow(toAssignTaskSet);
		}else if(event.getButton()==enable){
			for(MarketingProjectTask task:selectedTasks.values()){
				task.setIsUseable(true);
				commonService.update(task);
			}
			TaskDetailWindow.this.updateTable(false);
			NotificationUtil.showWarningNotification(this, "成功启用"+selectedTasks.size()+"个勾选项！");
		}else if(event.getButton()==disable){
			for(MarketingProjectTask task:selectedTasks.values()){
				task.setIsUseable(false);
				commonService.update(task);
			}
			TaskDetailWindow.this.updateTable(false);
			NotificationUtil.showWarningNotification(this, "成功禁用"+selectedTasks.size()+"个勾选项！");
		}else if(event.getButton()==delete){
			// 在confirmDelete方法中删除与批次的关联、与用户的关联
			Label label = new Label("您确定要删除选中的任务?",
					Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
					"confirmDelete");
			this.getApplication().getMainWindow().removeWindow(confirmWindow);
			this.getApplication().getMainWindow().addWindow(confirmWindow);
		}
	}
	
}
