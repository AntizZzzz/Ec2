package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.VerticalLayout;
/**
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class DutyTableManagement extends VerticalLayout implements Property.ValueChangeListener, Action.Handler{
/**
 * 主要组件
 */
	//搜索组件
//	private TextField keyWord;
//	private ComboBox projectStatus;
//	private Button search;
//	private Button advanceSearch;
	
	//项目表格组件
//	private Table table;
//	private String sqlSelect;
//	private String sqlCount;
//	private MarketingProjectService marketingProjectService;
//	private FlipOverTableComponent<MarketingProject> flip;
	
	//项目表格按钮组件
//	private Button start;
//	private Button stop;
//	private Button edit;
//	private Button delete;
//	private Button assignCsr;
//	private Button assignResource;
//	private Button assignTask;
//	private Button recycleByCsr;
//	private Button recycleAll;
//	private Button addProject;//添加项目
//	
	//项目状态信息组件
//	private Label finishendNumLabel;
//	private Label notFinishendNumLabel;
//	private Label notDistributeNumLabel;
//	private Label csrNumLabel;
//	
	/**
	 * 右键组件
	 */
	private Action START= new Action("开始");
	private Action STOP= new Action("停止");
	private Action ADD= new Action("添加");
	private Action EDIT= new Action("编辑");
	private Action DELETE= new Action("删除");
	private Action[] ACTIONS = new Action[] {START,STOP,ADD,EDIT,DELETE};
/**
 * 弹出窗口
 */
	//弹出窗口 只创建一次
//	private AdvanceSearchProject advanceSearchWindow;
//	private EditProject editProjectWindow;
//	private AssignCsr assignCsrWindow;
//	private AssignResource assignResourceWindow;
//	private AssignTask assignTaskWindow;
//	private RecycleByCsr recycleByCsrWindow;
//	private AddProject addProjectWindow;
/**
 * 其他组件
 */
//	//项目资源数目信息
//	private MarketingProjectTaskService marketingProjectTaskService;
//	//记录所有已经分配给CSR的未完成资源数目
//	private int distributedNum;
//	private int notFinished;
	
	
/**
 * 构造器	
 */
	public DutyTableManagement() {
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

//		// 搜索
//		constrantLayout.addComponent(buildSearchLayout());
//		//初始化Sql语句
//		search.click();
//		// 表格和按钮
//		constrantLayout.addComponent(buildTabelAndButtonsLayout());
//		
//		//另起一排按钮
//		constrantLayout.addComponent(buildOtherButtons());
//		
//		//显示项目的状态信息
//		constrantLayout.addComponent(buildProjectResourceInfoLayout());
	}
	
//	/**
//	 * 由buttonClick调用显示高级搜索窗口，由于用户点击的高级搜索
//	 */
//	private void showAdvanceSearchWindow() {
//		if(advanceSearchWindow==null){
//			advanceSearchWindow=new AdvanceSearchProject(this);
//		}
//		this.getWindow().addWindow(advanceSearchWindow);
//	}
//	
	
	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
//		//改变按钮
//		if(table.getValue()!=null){
//			//应该是可以通过设置父组件来使之全部为true;
//			start.setEnabled(true);
//			stop.setEnabled(true);
//			assignCsr.setEnabled(true);
//			assignResource.setEnabled(true);
//			delete.setEnabled(true);
//			edit.setEnabled(true);
//			recycleAll.setEnabled(true);
//			recycleByCsr.setEnabled(true);
//			assignTask.setEnabled(true);
//		}else{
//			//应该是可以通过设置父组件来使之全部为false;
//			start.setEnabled(false);
//			stop.setEnabled(false);
//			assignCsr.setEnabled(false);
//			assignResource.setEnabled(false);
//			delete.setEnabled(false);
//			edit.setEnabled(false);
//			recycleAll.setEnabled(false);
//			recycleByCsr.setEnabled(false);
//			assignTask.setEnabled(false);
//		}
//		//改变状态信息（项目数目状态）
//		updateProjectResourceInfo();
	}
	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
//		table.setValue(null);
//		table.select(target);
//		if(START==action){
//			start.click();
//		}else if(STOP==action){
//			stop.click();
//		}else if(ADD==action){
//			addProject.click();
//		}else if(EDIT==action){
//			edit.click();
//		}else if(DELETE==action){
//			delete.click();
//		}
	}

}
