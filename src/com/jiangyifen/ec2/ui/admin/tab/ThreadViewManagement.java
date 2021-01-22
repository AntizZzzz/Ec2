package com.jiangyifen.ec2.ui.admin.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.ui.admin.tab.threadview.ThreadCountVariationWindow;
import com.jiangyifen.ec2.ui.admin.tab.threadview.ThreadVisibleCountWindow;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.ThreadViewVo;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * 线程查看的TabSheet页面
 * 
 * @author JHT
 * 
 * @date 2014-10-16 上午9:29:18
 *  
 */
public class ThreadViewManagement extends VerticalLayout implements Button.ClickListener{

	private static final long serialVersionUID = 5054317732329474670L;
	private final Object[] VISIBLE_COLUMNS = new Object[]{"id", "name", "state"};
	private final String[] COLUMN_HEADERS = new String[]{"线程ID", "线程名称", "运行状态"};
			
	// 搜索组件
	private TextField tfKeyword;															// 线程名称关键字
	private Button btnSearch;																// 搜索按钮
	private Button btnRefresh;																// 刷新按钮
	
	// 表格组件
	private Table table;
	
	// 显示总信息的Label组件
	private Label lblSum;																	// 总数据条数
	private Label lblResult;																// 查询后的显示条数
	private Button btnVisible;																// 显示隐藏数量按钮
	private Button btnVariation;															// 查询结果的变化按钮
	private ThreadCountVariationWindow threadCountVariationWindow;
	private ThreadVisibleCountWindow threadVisibleCountWindow;
	
	// 其他组件
	private List<ThreadViewVo> threadViewVoAugmentList;										// 用来存储和上次查询总线程数的比较
	private List<ThreadViewVo> threadViewVoLessenList;	
	private Map<Long, ThreadViewVo> threadViewMap = new HashMap<Long, ThreadViewVo>();		// 用来存放线程信息
	private Map<Long, ThreadViewVo> oldThreadViewMap = new HashMap<Long, ThreadViewVo>();	// 获取上一次查询的结果
	private Map<Long, ThreadViewVo> visibleThreadMap = new HashMap<Long, ThreadViewVo>();	// 是否在表格中显示此线程
	private BeanItemContainer<ThreadViewVo> threadsContainer = new BeanItemContainer<ThreadViewVo>(ThreadViewVo.class);
	
	// 构造方法
 	public ThreadViewManagement(){
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		threadViewVoAugmentList = new ArrayList<ThreadViewVo>();
		threadViewVoLessenList = new ArrayList<ThreadViewVo>();
		
		// 添加一个最大的布局
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		constrantLayout.setWidth("100%");
		constrantLayout.setHeight("100%");
		this.addComponent(constrantLayout);
		
		createSearchLayout(constrantLayout);
		createTableAndLablesLayout(constrantLayout);
	}
	
	// 创建搜索组件输出
	private void createSearchLayout(VerticalLayout constrantLayout){
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
		btnRefresh = new Button("刷新", this);
		searchRightLayout.addComponent(btnRefresh);
		searchLayout.addComponent(searchRightLayout);
		searchLayout.setComponentAlignment(searchRightLayout, Alignment.TOP_RIGHT);
		
		constrantLayout.addComponent(searchLayout);
	}
	
	// 表格和Label组件输出
	private void createTableAndLablesLayout(VerticalLayout constrantLayout){
		lblResult = new Label("", Label.CONTENT_XHTML);
		lblResult.setWidth("-1px");
		lblSum = new Label("", Label.CONTENT_XHTML);
		lblSum.setWidth("-1px");
		btnVisible = new Button("", this);
		btnVisible.addStyleName(BaseTheme.BUTTON_LINK);
		btnVariation = new Button("", this);
		btnVariation.addStyleName(BaseTheme.BUTTON_LINK);
		table = new Table();
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		acquireThreads();
		table.setPageLength(27);
		constrantLayout.addComponent(table);
		
		// 表格底部显示搜索后的信息统计
		HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth("100%");
		HorizontalLayout footerLeftLayout = new HorizontalLayout();
		footerLeftLayout.setSpacing(true);
		footerLeftLayout.addComponent(lblResult);
		footerLeftLayout.addComponent(btnVisible);
		footerLayout.addComponent(footerLeftLayout);
		HorizontalLayout footerRightLayout = new HorizontalLayout();
		footerRightLayout.setSpacing(true);
		footerRightLayout.addComponent(lblSum);
		footerRightLayout.addComponent(btnVariation);
		footerLayout.addComponent(footerRightLayout);
		footerLayout.setComponentAlignment(footerRightLayout, Alignment.BOTTOM_RIGHT);
		
		constrantLayout.addComponent(footerLayout);
	}
	
	// 设置Table的数据源
	private void setBeanItemContainer(List<ThreadViewVo> threadViewVos){
		threadsContainer.removeAllItems();
		threadsContainer.addAll(threadViewVos);
		table.setContainerDataSource(threadsContainer);
		table.removeGeneratedColumn("操作");		// 需要先移除掉这一个显示列，不然在刷新的时候会出现重复添加显示列的错误
		table.setValue(null);
		table.setVisibleColumns(VISIBLE_COLUMNS);
		table.setColumnHeaders(COLUMN_HEADERS);
		table.addGeneratedColumn("操作", new VisibleThreadViewColumn());
	}

	// 获取线程信息，放入到Map数组中
	public void acquireThreads() {
		ThreadViewVo threadViewVo = null;
		Thread ta [] = new Thread[Thread.activeCount()];	// 获取总线程数量
		Thread.enumerate(ta);
		
		oldThreadViewMap.clear();	// 清空上一次的存放的值
		oldThreadViewMap.putAll(threadViewMap);
		threadViewMap.clear();	// 每次都先清空 上一次查询结果Map里的数据
		
		List<ThreadViewVo> threadViewVos = new ArrayList<ThreadViewVo>();	// 用来存储根据条件查询后的结果显示
		for(Thread thread : ta){
			// 如果这个线程不为空，并且这个线程的名字中包含搜索条件的信息，则继续执行下面的方法
			if(thread!=null){
				threadViewVo = new ThreadViewVo();
				threadViewVo.setId(thread.getId());
				threadViewVo.setName(thread.getName());
				threadViewVo.setState(thread.getState().toString().equals("RUNNABLE")?"运行":(thread.getState().toString().equals("WAITING")?"等待":"休眠"));
				threadViewVo.setIsDaemon(thread.isDaemon()==true?"是":"否");
				
				if(StringUtils.contains(thread.getName(), tfKeyword.getValue().toString())&&!visibleThreadMap.containsKey(thread.getId())){	// 这一层的判断就是过滤掉设置为不显示的线程名称
					threadViewVos.add(threadViewVo);	// 把根据条件查询到的数据存入到要显示在页面的List集合中
				}
				threadViewMap.put(thread.getId(), threadViewVo);	// 把所有的线程数据都存入到Map集合中
			}
		}
		lblResult.setValue("查询结果：<font style='font-weight:bold;'>"+threadViewVos.size()+"</font> 个");
		lblSum.setValue("总线程数：<font style='font-weight:bold;'>"+ta.length+"</font> 个");
		
		// 对List集合进行按照Name排序
		if(threadViewVos.size() > 0){
			Collections.sort(threadViewVos, new Comparator<ThreadViewVo>() {
				@Override
				public int compare(ThreadViewVo o1, ThreadViewVo o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		
		// 进行比较上次查询的线程结果和这次查询的线程结果是增加或减少的线程数量
		threadViewVoAugmentList.clear();
		threadViewVoLessenList.clear();
		
		if(oldThreadViewMap.size() >0){
			boolean contain = false;
			for(Object o : oldThreadViewMap.keySet()){	// 获取这次的查询结果和上次的查询结果的比较减少了的数据
				contain = threadViewMap.containsKey(o);	
				if(contain){
					contain = oldThreadViewMap.get(o).getName().equals(threadViewMap.get(o).getName());
				}
				if(!contain){
					threadViewVoLessenList.add(oldThreadViewMap.get(o));	
				}
			}
			
			contain = false;
			for(Object o : threadViewMap.keySet()){	// 获取这次的查询结果和上次的查询结果比较增加了的数据
				contain = oldThreadViewMap.containsKey(o);
				if(contain){
					contain = threadViewMap.get(o).getName().equals(oldThreadViewMap.get(o).getName());
				}
				if(!contain){
					threadViewVoAugmentList.add(threadViewMap.get(o));
				}
			}
		}
		btnVisible.setCaption("隐藏数量："+visibleThreadMap.size());
		btnVariation.setCaption("增减变化："+(threadViewVoLessenList.size()+threadViewVoAugmentList.size()));
		
		setBeanItemContainer(threadViewVos);
	}
	
	/**
	 * 隐藏按钮 -- 表格数据显示列
	 * @author JHT
	 */
	private class VisibleThreadViewColumn implements ColumnGenerator{
		private static final long serialVersionUID = 6529304822307851648L;
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final ThreadViewVo threadViewVo = (ThreadViewVo) itemId;
			Button btnVisibleThread = new Button("隐藏");
			btnVisibleThread.addStyleName(BaseTheme.BUTTON_LINK);
			btnVisibleThread.setHeight("16px");
			btnVisibleThread.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = -6080788117967112254L;
				@Override
				public void buttonClick(ClickEvent event) {
					if(threadViewVo!=null){
						visibleThreadMap.put(threadViewVo.getId(), threadViewVo);
						acquireThreads();
					}
				}
			});
			return btnVisibleThread;
		}
	}
	
	// 鼠标点击按钮事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btnSearch == source){
			acquireThreads();
		} else if(btnRefresh == source){
			acquireThreads();
		} else if(btnVariation == source){
			if((threadViewVoAugmentList.size()+threadViewVoLessenList.size()) > 0){
				threadCountVariationWindow = new ThreadCountVariationWindow(threadViewVoAugmentList, threadViewVoLessenList);
				this.getApplication().getMainWindow().addWindow(threadCountVariationWindow);
			} else{
				this.getApplication().getMainWindow().showNotification("本次查询结果和上次显示结果比较无任何变化！", Notification.TYPE_HUMANIZED_MESSAGE);
			}
		} else if(btnVisible == source){
			if(visibleThreadMap.size() > 0){
				if(threadVisibleCountWindow == null)
					threadVisibleCountWindow = new ThreadVisibleCountWindow(this);
				this.getApplication().getMainWindow().addWindow(threadVisibleCountWindow);
			} else{
				this.getApplication().getMainWindow().showNotification("没有隐藏任何线程信息！", Notification.TYPE_HUMANIZED_MESSAGE);
			}
		}
	}

	// 是否在表格中显示线程数组的GET方法，方便在另一个页面用作显示时的调用
	public Map<Long, ThreadViewVo> getVisibleThreadMap() {
		return visibleThreadMap;
	}

}
