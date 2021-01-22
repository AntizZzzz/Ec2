package com.jiangyifen.ec2.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.service.common.FlipOverNativeSqlService;
import com.jiangyifen.ec2.utils.Pagination;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @Description 描述：使用原生SQL 语句的翻页组件，并且查询后，返回的内容是一个List<Object> 集合
 * 	
 * 		主要用于报表的统计，用它来完成报表的翻页统计
 * 		但刷新界面可以通过Object 反射回调，也客户在翻页组件中之间完成
 * 
 * @author  jrh
 * @date    2013-11-6 下午3:40:27
 * @version
 */
@SuppressWarnings("serial")
public class FlipOverTableUseNativeSql extends HorizontalLayout implements Button.ClickListener {
	
	/* 显示的翻页按钮，状态显示组件 */
	private Button firstPage;
	private Button previousPage;
	private Button nextPage;
	private Button lastPage;
	private Button gotoPage;
	// 输入页号
	private ComboBox pageSelector;
	private BeanItemContainer<Integer> pageContainer;
	// 显示当前页号和总页数的标签
	private Label showNo;

	/* 设置分页信息类 */
	private Pagination pagination;
	private String searchSql;
	private String countSql;

	/* 特殊情况,需要回调 */
	//private Object object;
	/*表格组件*/
	private Table table;

	/* Table 和 Table的 Container */
	private FlipOverNativeSqlService flipOverNativeSqlService;

	/* 特殊情况,需要回调 */
	private Object object;
	
	/*是否通过反射来刷新界面信息，如表格的内容 */
	private boolean isRefreshByReflect;
	
	/**
	 * 翻页组件构造器
	 * @param flipOverNativeSqlService
	 * 					bean的Service
	 * @param table
	 *           		 表格引用
	 * @param pageLength
	 * 					每页显示的行数
	 * @param searchSql
	 *           		 查询Sql语句
	 * @param countSql
	 *           		 计数Sql语句
	 * @param object 	
	 * 					传入要调用反射回调flipOverCallBack()方法的类实例
	 * @param isRefreshByReflect
	 * 					是否通过反射来刷新界面信息，如表格的内容
	 * 	【选择原则，如果通过原生Sql 获取的List<Object[]>就是你想要在界面展示的结果，那就传 false, 如果你获取的List还需要后期处理，则传true】
	 * 
	 */
	public FlipOverTableUseNativeSql(FlipOverNativeSqlService flipOverNativeSqlService, Table table, int pageLength,
			String searchSql, String countSql,Object object, boolean isRefreshByReflect) {
		// Sql语句
		this.flipOverNativeSqlService = flipOverNativeSqlService;
		this.table = table;
		this.searchSql = searchSql;
		this.countSql = countSql;
		this.object = object;
		this.isRefreshByReflect = isRefreshByReflect;
		
		// 翻页组件的显示
		this.setSpacing(true);
		pageContainer = new BeanItemContainer<Integer>(Integer.class);
		buildButtonsLayout();
		this.setButtonStyle(BaseTheme.BUTTON_LINK);

		//构建Pagination类
		pagination = new Pagination(0);
		pagination.setPageRecords(pageLength);
	}

	/**
	 * 刷新页面到首页
	 */
	public void refreshToFirstPage() {
  		loadPage(1);
	}

	/**
	 * 当前页刷新页面
	 */
	public void refreshInCurrentPage() {
		loadPage(pagination.getCurrentPage());
	}

	/**
	 * 加载哪个页面，仅供内部调用
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadPage(int pageNum) {
		rebuildPagination();
		// 将PageNum设置在符合条件的合适页数，并更新Pagination当前页
		pageNum = pageNum < 1 ? 1 : pageNum;
		pageNum = pageNum > pagination.getTotalPage() ? pagination
				.getTotalPage() : pageNum;
		pagination.setCurrentPage(pageNum);
		
 		List<Object[]> list  = flipOverNativeSqlService.loadPageInfosByNativeSql(pagination.getStartIndex(), pagination.getPageRecords(),searchSql);
 		table.removeAllItems();
 		
 		//支持在每次刷新Table内容时回调特定实例的flipOverCallBack(List entitys)方法	
		// 这里用来加载，刷新界面的信息，如表格中的内容等
		if(object != null){
			Class objClass = null;
			try{
				objClass = object.getClass();
				Method objMethod = objClass.getMethod("flipOverCallBack",List.class);
				objMethod.invoke(object,list);
			} catch (Exception e) {
				e.printStackTrace();
				Logger logger=LoggerFactory.getLogger(this.getClass());
				logger.warn("Class "+objClass.getName()+" 调用 flipOverCallBack() 方法失败!");
			}
		}

		if(!isRefreshByReflect) {	// 如果不需要直接刷新界面，则不处理
			for (Object[] objects : list) {
				table.addItem(objects, objects);
			} 
		}
		
		// 刷新表格行在内存中的信息
		table.refreshRowCache();
		
		// 更新按钮状态
		updateButtonsStatus();
	}

	// 更新按钮及记录状态
	private void updateButtonsStatus() {
		firstPage.setEnabled(true);
		previousPage.setEnabled(true);
		nextPage.setEnabled(true);
		lastPage.setEnabled(true);

		if (pagination.getCurrentPage() == 1) {
			firstPage.setEnabled(false);
			previousPage.setEnabled(false);
		}

		if (pagination.getCurrentPage() == pagination.getTotalPage()) {
			nextPage.setEnabled(false);
			lastPage.setEnabled(false);
		}
		
		pageContainer.removeAllItems();
//		JRH 20141224 修改成 pageContainer.addAll
//		for(int i = 1; i <= pagination.getTotalPage(); i++) {
//			pageContainer.addItem(i);	// JRH 这种方式性能太低，如果执行20万次，大概需要8分钟左右，如果100万，则直接卡死了
//		}
		List<Integer> pageLs = new ArrayList<Integer>();
		for(int i = 1; i <= pagination.getTotalPage(); i++) {
			pageLs.add(i);
		}
		pageContainer.addAll(pageLs); 
		
		showNo.setValue(pagination.getCurrentPage() + "/"
				+ pagination.getTotalPage() + "页" + "--总数："
				+ pagination.getTotalRecord());
	}

	/**
	 * 重新计算并构建Pagination类
	 */
	private void rebuildPagination() {
		pagination.setTotalRecord(flipOverNativeSqlService.getInfoCountByNativeSql(countSql));
	}

	/**
	 * 创建显示组件的输出
	 */
	private void buildButtonsLayout() {
		// 按钮组件
		firstPage = new Button("首页");
		firstPage.addListener(this);
		previousPage = new Button("上一页");
		previousPage.addListener(this);
		nextPage = new Button("下一页");
		nextPage.addListener(this);
		lastPage = new Button("尾页");
		lastPage.addListener(this);
		gotoPage = new Button("跳转至");
		gotoPage.addListener(this);

		pageSelector = new ComboBox();
		pageSelector.setWidth("50px");
		pageSelector.setImmediate(true);
		pageSelector.setInputPrompt("页码");
		pageSelector.setContainerDataSource(pageContainer);
		pageSelector.setNullSelectionAllowed(false);
		pageSelector.addListener(new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				gotoSpecifiedPage();
			}
		});
		showNo = new Label();

		// 添加全部组件
		this.addComponent(firstPage);
		this.addComponent(previousPage);
		this.addComponent(nextPage);
		this.addComponent(lastPage);
		this.addComponent(gotoPage);
		this.addComponent(pageSelector);
		this.addComponent(showNo);
	}

	/**
	 * 设置按钮格式
	 * 
	 * @param style
	 */
	private void setButtonStyle(String style) {
		firstPage.setStyleName(style);
		previousPage.setStyleName(style);
		nextPage.setStyleName(style);
		lastPage.setStyleName(style);
		gotoPage.setStyleName(style);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == firstPage) {
			refreshToFirstPage();
		} else if (event.getButton() == previousPage) {
			loadPage(pagination.getCurrentPage() - 1);
		} else if (event.getButton() == nextPage) {
			loadPage(pagination.getCurrentPage() + 1);
		} else if (event.getButton() == lastPage) {
			// 可能有小Bug，解决方式是刷新Pagination组件，然后取最大页
			loadPage(pagination.getTotalPage());
		} else if (event.getButton() == gotoPage) {
			gotoSpecifiedPage();
		}
	}

	/**
	 * 去指定页，内部调用
	 */
	private void gotoSpecifiedPage() {
		if(pageSelector.getValue() != null) {
			String selectedPage = pageSelector.getValue().toString();
			loadPage(Integer.parseInt(selectedPage));
			pageSelector.setValue(null);
		}
	}
	
	/**
	 * 获取搜索语句
	 * @return
	 */
	public String getSearchSql() {
		return searchSql;
	}

	/**
	 * 设置查询语句
	 * @param searchSql
	 */
	public void setSearchSql(String searchSql) {
		this.searchSql = searchSql;
	}

	/**
	 * 获取统计数量的查询语句
	 * @return
	 */
	public String getCountSql() {
		return countSql;
	}

	/**
	 * 设置计数语句
	 * @param countSql
	 */
	public void setCountSql(String countSql) {
		this.countSql = countSql;
	}

	/**
	 * 设置翻页组件的每页记录条数，默认为15条
	 */
	public void setPageLength(int pageLength) {
		pagination.setPageRecords(pageLength);
		refreshToFirstPage();
	}
	/**
	 * 取得Bean容器
	 * @return
	 */
	 
	/**
	 * 取得管理的Table
	 * @return
	 */
	public Table getTable() {
		return table;
	}
	
	/**
	 * jrh
	 *  获取中记录数
	 * @return
	 */
	public int getTotalRecord() {
		return pagination.getTotalRecord();
	}
	
}
