package com.jiangyifen.ec2.ui.admin.tab.threadview;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.ThreadViewVo;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 线程的数量增减变化窗口
 * @author JHT
 */
public class ThreadCountVariationWindow extends Window{

	private static final long serialVersionUID = -771756584627502391L;
	private final Object[] VISIBLE_COLUMNS = new Object[]{"id", "name"};
	private final String[] COLUMN_HEADERS = new String[]{"线程ID", "线程名称"};

	// 主要组件
	private Table augmentTable;								// 存放和上次查询结果相比增加的数据
	private Table lessenTable;								// 存放和上次查询结果相比减少的数据

	// 其他组件
	private List<ThreadViewVo> threadViewVoAugmentList;		// 和上次查询结果相比增加的数据		
	private List<ThreadViewVo> threadViewVoLessenList;		// 和上次查询结果相比减少的数据
	private BeanItemContainer<ThreadViewVo> augmentThreadsContainer = new BeanItemContainer<ThreadViewVo>(ThreadViewVo.class);
	private BeanItemContainer<ThreadViewVo> lessenThreadsContainer = new BeanItemContainer<ThreadViewVo>(ThreadViewVo.class);
	
	// 构造方法
	public ThreadCountVariationWindow(List<ThreadViewVo> threadViewVoAugmentList, List<ThreadViewVo> threadViewVoLessenList){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setImmediate(true);
		this.setWidth("900px");
		this.setHeight("510px");
		this.setCaption("线程数量的增减变化");
		
		this.threadViewVoAugmentList = threadViewVoAugmentList;
		this.threadViewVoLessenList = threadViewVoLessenList;
		
		HorizontalLayout contextLayout = new HorizontalLayout();	// 添加Window内最大的布局
		contextLayout.setSizeFull();
		contextLayout.setSpacing(true);
		this.addComponent(contextLayout);
		
		createLeftAugmentLayout(contextLayout);
		createRighLessenLayout(contextLayout);
	}
	
	
	// 添加窗体左侧的布局
	private void createLeftAugmentLayout(HorizontalLayout contextLayout){
		VerticalLayout leftLayout = new VerticalLayout();
		leftLayout.setSpacing(true);
		leftLayout.setWidth("100%");
		leftLayout.setHeight("100%");
		leftLayout.addComponent(new Label("增加："+threadViewVoAugmentList.size()));
		
		// 对数组进行排序
		if(threadViewVoAugmentList.size() > 0){
			Collections.sort(threadViewVoAugmentList, new Comparator<ThreadViewVo>() {
				@Override
				public int compare(ThreadViewVo o1, ThreadViewVo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		augmentTable = new Table();
		augmentTable.setStyleName("striped");
		augmentTable.setWidth("100%");
		augmentTable.setHeight("100%");
		augmentTable.setSelectable(true);
		augmentTable.setMultiSelect(false);
		augmentTable.setImmediate(true);
		augmentTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		augmentTable.setPageLength(22);
		augmentThreadsContainer.removeAllItems();
		augmentThreadsContainer.addAll(threadViewVoAugmentList);
		augmentTable.setContainerDataSource(augmentThreadsContainer);
		augmentTable.setVisibleColumns(VISIBLE_COLUMNS);
		augmentTable.setColumnHeaders(COLUMN_HEADERS);

		leftLayout.addComponent(augmentTable);
		contextLayout.addComponent(leftLayout);
	}
	
	// 添加窗体右侧的布局
	private void createRighLessenLayout(HorizontalLayout contextLayout){
		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setSpacing(true);
		rightLayout.setWidth("100%");
		rightLayout.setHeight("100%");
		rightLayout.addComponent(new Label("减少："+threadViewVoLessenList.size()));
		
		// 对集合进行排序
		if(threadViewVoLessenList.size() > 0){
			Collections.sort(threadViewVoLessenList, new Comparator<ThreadViewVo>() {
				@Override
				public int compare(ThreadViewVo o1, ThreadViewVo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		lessenTable = new Table();
		lessenTable.setStyleName("striped");
		lessenTable.setWidth("100%");
		lessenTable.setHeight("100%");
		lessenTable.setSelectable(true);
		lessenTable.setMultiSelect(false);
		lessenTable.setImmediate(true);
		lessenTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		lessenTable.setPageLength(22);
		lessenThreadsContainer.removeAllItems();
		lessenThreadsContainer.addAll(threadViewVoLessenList);
		lessenTable.setContainerDataSource(lessenThreadsContainer);
		lessenTable.setVisibleColumns(VISIBLE_COLUMNS);
		lessenTable.setColumnHeaders(COLUMN_HEADERS);
		
		rightLayout.addComponent(lessenTable);
		contextLayout.addComponent(rightLayout);
	}

}
