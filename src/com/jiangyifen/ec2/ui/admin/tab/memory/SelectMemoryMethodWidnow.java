package com.jiangyifen.ec2.ui.admin.tab.memory;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.MemoryMethod;
import com.jiangyifen.ec2.service.eaoservice.MemoryMethodService;
import com.jiangyifen.ec2.ui.admin.tab.MemoryMethodDataVisiableManagement;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.MemoryParameter;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * 选择需要查询的方法名称
 *
 * @author jinht
 *
 * @date 2015-7-3 下午3:10:58 
 *
 */
public class SelectMemoryMethodWidnow extends Window implements Property.ValueChangeListener{
	private static final long serialVersionUID = -6255658493614322708L;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 主要组件
	private TextField keyWord;														// 输入的内容用来查询筛选表格中的数据
	private Table table;															// 用来显示数据的Table
	
	// 其他
	private MemoryMethodDataVisiableManagement memoryMethodDataVisiableManagement;
	private List<MemoryMethod> memoryMethodList;									// 内存方法的实体对象
	private MemoryParameter memoryParameter;										// 存储选择的信息类
	private MemoryMethodService memoryMethodService;								// 业务操作调用
	private Object[] VISIBLE_COLUMNS = new Object[]{"abbrName", "name", "description"};
	private String[] COLUMN_HEADERS = new String[]{"简写名称", "方法名称", "介绍信息"};
	
	// 构造方法
	public SelectMemoryMethodWidnow(MemoryMethodDataVisiableManagement memoryMethodDataVisiableManagement){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setImmediate(true);
		this.setWidth("970px");
		this.setHeight("620px");
		this.setCaption("选择需要查询的方法名称");
		
		this.memoryMethodDataVisiableManagement = memoryMethodDataVisiableManagement;
		
		memoryMethodService = SpringContextHolder.getBean("memoryMethodService");	// 初始化Service
		
		VerticalLayout contextLayout = new VerticalLayout();						// 添加窗体内最大的Layout
		contextLayout.setSizeFull();
		this.addComponent(contextLayout);
		
		contextLayout.addComponent(buildSearchLayout());
		contextLayout.addComponent(buildTableLayout());
		
	}
	
	// 创建搜索输出
	private HorizontalLayout buildSearchLayout(){
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setWidth("100%");
		
		searchLayout.setSpacing(true);
		keyWord = new TextField();
		keyWord.setImmediate(true);
		keyWord.setInputPrompt("请输入搜索关键字");
		keyWord.setDescription("可按'简写名称'或者'方法名称'或者'介绍信息'进行查询");
		keyWord.setStyleName("search");
		keyWord.addListener(this);
		keyWord.setWidth("300px");
		searchLayout.addComponent(keyWord);
		return searchLayout;
	}
	
	// 创建表格输出
	private VerticalLayout buildTableLayout(){
		VerticalLayout tableLayout = new VerticalLayout();
		//tableLayout.setMargin(true,false,false,false);
		tableLayout.setSpacing(true);
		tableLayout.setSizeFull();
		
		table = new Table();
		table.setSizeFull();
		table.setStyleName("striped");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setPageLength(28);
		tableItemClick(table);
		tableLayout.addComponent(table);
		executeSearch();
		return tableLayout;
	}
	
	// 执行查询方法
	private void executeSearch(){
		String keyWordString = "";
		if(keyWord.getValue() != null){
			keyWordString = keyWord.getValue().toString();
		}
		try{
			memoryMethodList = memoryMethodService.findByKeyWord(keyWordString);
			setBeanItemContainer(memoryMethodList);
		}catch (Exception e) {	
			logger.error("jinht -->> 数据类型转换异常：" + e.getMessage(), e);
		}
		keyWord.focus();
	}
	
	// 设置Table的数据源显示
	private void setBeanItemContainer(List<MemoryMethod> memoryMethods){
		BeanItemContainer<MemoryMethod> memBeanItemContainer = new BeanItemContainer<MemoryMethod>(MemoryMethod.class);
		memBeanItemContainer.removeAllItems();
		memBeanItemContainer.addAll(memoryMethods);
		table.setContainerDataSource(memBeanItemContainer);
		table.setVisibleColumns(VISIBLE_COLUMNS);
		table.setColumnHeaders(COLUMN_HEADERS);
		table.setColumnExpandRatio("abbrName", 0.1f);
		table.setColumnExpandRatio("name", 0.4f);
		table.setColumnExpandRatio("description", 0.5f);
	}
	
	// 添加表格的点击事件
	private void tableItemClick(final Table tb){
		tb.addListener(new ItemClickListener() {
			private static final long serialVersionUID = 5475772330003170239L;
			@Override
			public void itemClick(ItemClickEvent event) {
				if(event.isDoubleClick()){							// 双击事件
					try {
						memoryParameter = new MemoryParameter();
						memoryParameter.setMemoryMethod((MemoryMethod) event.getItemId());
						memoryMethodDataVisiableManagement.setMemoryMethodParameter(memoryParameter);
					} catch (Exception e) {
						logger.error("jinht -->> 获取item错误: " + e.getMessage(), e);
					}
					exit();
				}
			}
		});
	}
	
	
	// 关闭这个窗口
	public void exit(){
		this.close();
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		executeSearch();
	}
	
	
}
