package com.jiangyifen.ec2.ui.admin.tab;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

/**
 * 
 * 系统信息查看主页面中显示的TabSheet选项
 * 
 * @author JHT
 * 
 * @date 2014-10-16 上午9:29:18
 * 
 */
public class OperatorTabSheet extends TabSheet implements TabSheet.SelectedTabChangeListener {
	private static final long serialVersionUID = -4068508843470222479L;

	private TableInfoLookManagement tableInfoVoManagement; 						// 数据库表信息查看页面
	private MemoryMethodDataVisiableManagement argumentsMonitoringManagement; 	// 内存数据信息页面
	private ThreadViewManagement threadViewManagement;							// 线程显示页面
	private MemoryMethodCountManagement memoryMethodCountManagement;			// 内存数据大小信息
	
	private Tab selectTab; // 当前选择的Tab

	// 构造方法
	public OperatorTabSheet() {
		this.addListener(this);
		this.setImmediate(true);

		// 添加Tab
		showTaleInfoVoManagement();
		showMemoryMethodDataVisiable();
		showThreadViewManagement();
		showMemoryMethodCountManagement();
		//showAdvancedSettingManagement();
	}

	/**
	 * 添加Tab 表格大小统计
	 */
	public void showTaleInfoVoManagement() {
		if (tableInfoVoManagement == null) {
			tableInfoVoManagement = new TableInfoLookManagement();
		}
		tableInfoVoManagement.setSizeFull();
		selectTab = this.addTab(tableInfoVoManagement, "数据表信息");
		// selectTab.setClosable(true); 显示tab关闭选项
		this.setSelectedTab(selectTab);
	}

	/**
	 * 添加Tab 系统信息监控页面
	 */
	public void showMemoryMethodDataVisiable() {
		if (argumentsMonitoringManagement == null) {
			argumentsMonitoringManagement = new MemoryMethodDataVisiableManagement();
		}
		argumentsMonitoringManagement.setSizeFull();
		selectTab = this.addTab(argumentsMonitoringManagement, "内存数据信息");
	}
	
	/**
	 * 添加Tab 线程显示页面
	 */
	public void showThreadViewManagement(){
		if(threadViewManagement == null){
			threadViewManagement = new ThreadViewManagement();
		}
		threadViewManagement.setSizeFull();
		selectTab = this.addTab(threadViewManagement, "线程状态信息");
	}
	
	/**
	 * 添加Tab 内存数据大小信息显示页面
	 */
	public void showMemoryMethodCountManagement(){
//		if(memoryMethodCountManagement == null){
			memoryMethodCountManagement = new MemoryMethodCountManagement();
//		}
		memoryMethodCountManagement.setSizeFull();
		selectTab = this.addTab(memoryMethodCountManagement, "内存数据大小信息");
	}

	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		Component selected = this.getSelectedTab();
		if (selected != null) {
			if (selected == tableInfoVoManagement) { // 表格大小统计Tab页
				// 执行一些操作
			} else if (selected == argumentsMonitoringManagement) {

			} else if(selected == threadViewManagement){

			}
		}
	}

}
