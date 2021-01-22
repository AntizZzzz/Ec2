package com.jiangyifen.ec2.ui.admin.tab.threadview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.ui.admin.tab.ThreadViewManagement;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.ThreadViewVo;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 操作隐藏显示线程的窗口
 * @author JHT
 */
public class ThreadVisibleCountWindow extends Window implements Button.ClickListener{

	private static final long serialVersionUID = 7719277649262732732L;
	private final Object[] VISIBLE_COLUMNS = new Object[]{"id", "name"};
	private final String[] COLUMN_HEADERS = new String[]{"线程ID", "线程名称"};
	
	// 主要组件
	private TextField tfKeyword;						// 线程名称搜索框
	private Button btnSearch;							// 搜索按钮
	private Button btnShowResult;						// 显示查询结果
	private Button btnShowAll;							// 显示全部按钮
	private Table table;								// 显示数据的table
	private Label lblSum;								// 显示总数的Label
	private Label lblResult;							// 显示查询到的Result
	
	// 其他组件
	private ThreadViewManagement threadViewManagement;
	private BeanItemContainer<ThreadViewVo> threadsContainer = new BeanItemContainer<ThreadViewVo>(ThreadViewVo.class);
	private List<ThreadViewVo> threadViewVoList = new ArrayList<ThreadViewVo>();
	
	// 构造方法
	public ThreadVisibleCountWindow(ThreadViewManagement threadViewManagement){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setImmediate(true);
		this.setWidth("900px");
		this.setHeight("500px");
		this.setCaption("操作隐藏显示线程");
		
		this.threadViewManagement = threadViewManagement;
		
		VerticalLayout contextLayout = new VerticalLayout();		// 添加Window内最大的布局
		contextLayout.setSizeFull();
		contextLayout.setSpacing(true);
		this.addComponent(contextLayout);
		
		createSearchLayout(contextLayout);
		createTableAndButtonsLayout(contextLayout);
	}
	
	// 添加搜索组件
	private void createSearchLayout(VerticalLayout contextLayout){
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setWidth("100%");
		
		HorizontalLayout searchLeftLayout = new HorizontalLayout();
		searchLeftLayout.setSpacing(true);
		Label lblKeyword = new Label("线程名称：");
		lblKeyword.setWidth("-1px");
		searchLeftLayout.addComponent(lblKeyword);
		tfKeyword = new TextField();
		tfKeyword.setImmediate(true);
		tfKeyword.setInputPrompt("线程名称");
		tfKeyword.setDescription("可按'线程名称'进行搜索");
		searchLeftLayout.addComponent(tfKeyword);
		btnSearch = new Button("搜索", this);
		searchLeftLayout.addComponent(btnSearch);
		searchLayout.addComponent(searchLeftLayout);
		
		HorizontalLayout searchRightLayout = new HorizontalLayout();
		searchRightLayout.setSpacing(true);
		btnShowResult = new Button("显示查询结果", this);
		btnShowResult.setDescription("只把当前查询结果数据显示");
		btnShowAll = new Button("显示所有", this);
		btnShowAll.setDescription("所有隐藏的线程全部显示");
		searchRightLayout.addComponent(btnShowResult);
		searchRightLayout.addComponent(btnShowAll);
		searchLayout.addComponent(searchRightLayout);
		searchLayout.setComponentAlignment(searchRightLayout, Alignment.TOP_RIGHT);
		
		contextLayout.addComponent(searchLayout);
	}
	
	// 添加窗体显示的table和按钮
	private void createTableAndButtonsLayout(VerticalLayout contextLayout){
		lblResult = new Label("查询结果：<font style='font-weight:bold;'>0</font> 个", Label.CONTENT_XHTML);
		lblResult.setWidth("-1px");
		lblSum = new Label("总线程数：<font style='font-weight:bold;'>0</font> 个", Label.CONTENT_XHTML);
		lblSum.setWidth("-1px");
		table = new Table();
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setHeight("100%");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setPageLength(20);
		refreshTable();
		contextLayout.addComponent(table);
		
		HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth("100%");
		HorizontalLayout footerLeftLayout = new HorizontalLayout();
		footerLeftLayout.setSpacing(true);
		footerLeftLayout.addComponent(lblResult);
		footerLayout.addComponent(footerLeftLayout);
		HorizontalLayout footerRightLayout = new HorizontalLayout();
		footerRightLayout.setSpacing(true);
		footerRightLayout.addComponent(lblSum);
		footerLayout.addComponent(footerRightLayout);
		footerLayout.setComponentAlignment(footerRightLayout, Alignment.BOTTOM_RIGHT);
		contextLayout.addComponent(footerLayout);
	}

	// 刷新表格的数据显示
	private void refreshTable() {
		threadViewVoList.clear();
		List<ThreadViewVo> threadViewVos = new ArrayList<ThreadViewVo>(threadViewManagement.getVisibleThreadMap().values());
		for(ThreadViewVo tVo:threadViewVos){
			if(tVo!=null&&StringUtils.contains(tVo.getName(), tfKeyword.getValue().toString())){
				threadViewVoList.add(tVo);
			}
		}
		// 对集合进行排序
		if(threadViewVoList.size() > 0){
			Collections.sort(threadViewVoList, new Comparator<ThreadViewVo>() {
				@Override
				public int compare(ThreadViewVo o1, ThreadViewVo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		threadsContainer.removeAllItems();
		table.removeGeneratedColumn("操作");
		threadsContainer.addAll(threadViewVoList);
		table.setContainerDataSource(threadsContainer);
		table.setVisibleColumns(VISIBLE_COLUMNS);
		table.setColumnHeaders(COLUMN_HEADERS);
		table.addGeneratedColumn("操作", new VisibleThreadViewColumn());
		
		lblSum.setValue("总线程数：<font style='font-weight:bold;'>"+threadViewManagement.getVisibleThreadMap().size()+"</font> 个");
		lblResult.setValue("查询结果：<font style='font-weight:bold;'>"+threadViewVoList.size()+"</font> 个");
	}

	/**
	 * 显示按钮 -- 表格数据显示列
	 * @author JHT
	 */
	private class VisibleThreadViewColumn implements ColumnGenerator{
		private static final long serialVersionUID = -7366446284775805873L;
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final ThreadViewVo threadViewVo = (ThreadViewVo) itemId;
			Button btnVisibleThread = new Button("显示");
			btnVisibleThread.addStyleName(BaseTheme.BUTTON_LINK);
			btnVisibleThread.setHeight("16px");
			btnVisibleThread.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = -6080788117967112254L;
				@Override
				public void buttonClick(ClickEvent event) {
					if(threadViewVo!=null){
						threadViewManagement.getVisibleThreadMap().remove(threadViewVo.getId());
						threadViewManagement.acquireThreads();
						refreshTable();
					}
				}
			});
			return btnVisibleThread;
		}
	}
	
	@Override
	public void attach() {
		super.attach();
		refreshTable();
	}

	// 按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btnSearch == source){
			refreshTable();
		} else if(btnShowAll == source){
			threadViewManagement.getVisibleThreadMap().clear();
			threadViewManagement.acquireThreads();
			refreshTable();
		} else if(btnShowResult == source){
			for(ThreadViewVo tVo : threadViewVoList){
				if(tVo != null){
					threadViewManagement.getVisibleThreadMap().remove(tVo.getId());
				}
			}
			threadViewManagement.acquireThreads();
			refreshTable();
		}
	}
	
	
}
