package com.jiangyifen.ec2.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.service.common.FlipSupportService;
import com.jiangyifen.ec2.utils.Pagination;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.BaseTheme;

/**
 * <p>
 * 	最重要更改：去掉下拉条转到指定页面，如果有100W页，加载时间很长时间-LXY 2014-12-24
 * </p>
 * <p>
 * 当要设置每页记录数,应设置 flipOverTableComponent.setPageLength(10);
 * 和table.setPageLength(10); 
 * 两个地方,这两个组件彼此独立
 * </p>
 * 
 * <p>
 * 刷新时 refreshToFirstPage()，刷新到首页
 * </p>
 * 
 * <p>
 * 刷新时 refreshInCurrentPage()，刷新在当前页
 * </p>
 * 
 * <p>通过指定一个Object实例，支持在每次刷新Table内容时回调特定实例的flipOverCallBack()方法</p>
 * <p>注:回调方法中暂时不能出现翻页组件自身,否则会Null Pointer,不过问题可以很容易解决</p>
 * 
 * <p>添加Container属性的操作由Table完成，不过操作必须在在setVisibleColumns前面，</p>
 * <p>这点与Table自身的先设Container，在设setVisibleColumns一致</p>
 * 
 * <p>设置行或者单元格显示样式table.setCellStyleGenerator由Table实例设置即可</p>
 * @param <T>
 */
@SuppressWarnings("serial")
public class FlipOverTableComponentNoGo<T> extends HorizontalLayout implements
		Button.ClickListener {
	
	/* 显示的翻页按钮，状态显示组件 */
	private Button firstPage;
	private Button previousPage;
	private Button nextPage;
	private Button lastPage;
	 
	// 显示当前页号和总页数的标签
	private Label showNo;

	/* 设置分页信息类 */
	private Pagination pagination;
	private String searchSql;
	private String countSql;

	/* Table 和 Table的 Container */
	private FlipSupportService<T> flipSupportService;
	private BeanItemContainer<T> entityContainer;
	
	/* 特殊情况,需要回调 */
	private Object object;
	/*表格组件*/
	private Table table;
	
	/**
	 * 翻页组件构造器
	 * 
	 * @param type
	 *            bean的类型
	 * @param flipSupportService
	 *            bean的Service
	 * @param table
	 *            表格引用
	 * @param searchSql
	 *            查询Sql语句
	 * @param countSql
	 *            计数Sql语句
	 * @param object 传入要调用反射回调flipOverCallBack()方法的类实例
	 */
	public FlipOverTableComponentNoGo(Class<T> type, FlipSupportService<T> flipSupportService, Table table,
			String searchSql, String countSql,Object object) {
		// 对参数进行验证
		if (type == null || table == null) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.info("翻页组件参数不正确，组件未能正常初始化！");
			return;
		}
		this.object=object;
		this.table=table;
		// 翻页组件的显示
		this.setSpacing(true);
 		buildButtonsLayout();
		this.setButtonStyle(BaseTheme.BUTTON_LINK);

		// 与Table显示有关的组件
		this.flipSupportService = flipSupportService;
		this.entityContainer = new BeanItemContainer<T>(type);
		table.setContainerDataSource(entityContainer);

		// Sql语句
		this.searchSql = searchSql;
		this.countSql = countSql;
		//构建Pagination类
		pagination = new Pagination(0);
	}

	/**
	 * 刷新页面到首页
	 */
	public void refreshToFirstPage() {
//		long t1 = System.currentTimeMillis();
  		loadPage(1,true);
//		long t2 = System.currentTimeMillis();
//		System.out.println("refreshToFirstPage 耗时========="+(t2-t1)+"-------"+this);
	}

	/**
	 * 当前页刷新页面
	 */
	public void refreshInCurrentPage() {
//		long t1 = System.currentTimeMillis();
		loadPage(pagination.getCurrentPage(),true);
//		long t2 = System.currentTimeMillis();
//		System.out.println("refreshInCurrentPage 耗时========="+(t2-t1)+"-------"+this);
	}

	/**
	 * 加载哪个页面，仅供内部调用
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadPage(int pageNum,boolean isReloadTotal) {
		rebuildPagination(isReloadTotal);
		// 将PageNum设置在符合条件的合适页数，并更新Pagination当前页
		pageNum = pageNum < 1 ? 1 : pageNum;
		pageNum = pageNum > pagination.getTotalPage() ? pagination
				.getTotalPage() : pageNum;
		pagination.setCurrentPage(pageNum);
		
		// 重新设置数据源List<T> entitys
		List<T> entitys = flipSupportService.loadPageEntities(
					pagination.getStartIndex(), pagination.getPageRecords(),searchSql);

		//支持在每次刷新Table内容时回调特定实例的flipOverCallBack(List entitys)方法	
		//要支持角色的部门延迟加载,必须在前面
		if(object!=null){
			Class objClass=null;
			try{
				objClass=object.getClass();
				Method objMethod=objClass.getMethod("flipOverCallBack",List.class);
				objMethod.invoke(object,entitys);
			} catch (Exception e) {
				Logger logger=LoggerFactory.getLogger(this.getClass());
				logger.warn("Class "+objClass.getName()+" 调用 flipOverCallBack() 方法失败!");
			}
		}
		
		entityContainer.removeAllItems();
		entityContainer.addAll(entitys);
		//chb 新增添选中原来的行功能
		selectPreviousRow(entitys);
		
		// 更新按钮状态
		updateButtonsStatu();
		
		// jrh 刷新表格行在内存中的信息
//		synchronized (table) {
			table.refreshRowCache();
//		}
	}
	
	//仅仅限于选中一行状况,且实例中有getId方法的情况
	@SuppressWarnings("unchecked")
	private void selectPreviousRow(List<T> entitys) {
		T obj=null;
		//取出Table选中一个实体的值
		if(table.isMultiSelect()){
			Collection<T> selectedEntitys = (Collection<T>) table.getValue();
			if(selectedEntitys.size()!=1) return;
			table.setValue(null);
			obj=new ArrayList<T>(selectedEntitys).get(0);
		}else{
			obj=(T)table.getValue();
			if(obj==null) return;
		}
		
		Long id=reflectEntityId(obj);
		
		//由 selectPreviousRow 调用,让Table选中新的Entity
		for(T entity:entitys){
			Long entityId=reflectEntityId(entity);
			if(id==entityId){
				table.select(entity);
			}
		}
	}

	//取出实体的Id值
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Long reflectEntityId(T obj) {
		Long id=null;
		//通过反射获取以前选中实体的Id值
		Class objClass=null;
		try{
			objClass=obj.getClass();
			Method objMethod=objClass.getMethod("getId");
			id=(Long)objMethod.invoke(obj);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	// 更新按钮及记录状态
	private void updateButtonsStatu() {
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
		
		showNo.setValue(pagination.getCurrentPage() + "/"
				+ pagination.getTotalPage() + "页" + "--总数："
				+ pagination.getTotalRecord());
	}

	/**
	 * 重新计算并构建Pagination类
	 */
	private void rebuildPagination(boolean isReloadTotal) {
		if(isReloadTotal){
			pagination.setTotalRecord(flipSupportService.getEntityCount(countSql));
		}
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
		 

		showNo = new Label();

		// 添加全部组件
		this.addComponent(firstPage);
		this.addComponent(previousPage);
		this.addComponent(nextPage);
		this.addComponent(lastPage);
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
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == firstPage) {
			loadPage(1,false);
		} else if (event.getButton() == previousPage) {
			loadPage(pagination.getCurrentPage() - 1,false);
		} else if (event.getButton() == nextPage) {
			loadPage(pagination.getCurrentPage() + 1,false);
		} else if (event.getButton() == lastPage) {
			// 可能有小Bug，解决方式是刷新Pagination组件，然后取最大页
			loadPage(pagination.getTotalPage(),false);
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
	 * 【注意：如果你在Tabsheet 的Tab 页切换时调用 里翻页组件的 refreshToFirstPage 或 refreshInCurrentPage 方法，
	 * 		则在设置Page Length 时，就不要再刷新界面，第二个参数就该传false, 否则传 true】
	 * @param pageLength		加载条数
	 * @param isNeedRefresh		是否需要重新加载  
	 */
	public void setPageLength(int pageLength, boolean isNeedRefresh) {
		pagination.setPageRecords(pageLength);
		if(isNeedRefresh) {
			refreshToFirstPage();
		}
	}
	
	/**
	 * 取得Bean容器
	 * @return
	 */
	public BeanItemContainer<T> getEntityContainer() {
		return entityContainer;
	}
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
