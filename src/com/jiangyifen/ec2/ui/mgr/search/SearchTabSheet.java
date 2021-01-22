package com.jiangyifen.ec2.ui.mgr.search;

import com.jiangyifen.ec2.ui.mgr.tabsheet.GlobalSearch;
import com.vaadin.ui.TabSheet;
/**
 * 全局搜索的TabSheet页
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class SearchTabSheet extends TabSheet {
	
	/**
	 * 全局搜索的引用
	 */
	private GlobalSearch globalSearch;

	/**
	 * 构造器
	 * @param globalSearch
	 */
	public SearchTabSheet(GlobalSearch globalSearch) {
		this.globalSearch=globalSearch;
		this.addTab(new ResourceSearch(this), "按资源搜索",null);
		this.addTab(new ProjectSearch(this), "按项目搜索",null);
		this.addTab(new BatchSearch(this), "按批次搜索",null);
		this.addTab(new CustomerOwnerSearch(this), "按客服客户搜索",null);
		this.addTab(new TaskSearch(this), "按任务搜索",null);
		this.addTab(new RecordSearch(this), "按记录搜索",null);
	}
	
	/**
	 * 执行搜索
	 * @param 
	 */
	public void executeSearch(String selectSql,String countSql){
		globalSearch.executeSearch(selectSql, countSql);
	}
}
