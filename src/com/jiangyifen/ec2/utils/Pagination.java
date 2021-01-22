package com.jiangyifen.ec2.utils;

public class Pagination {
	
	/** 
     * 每页显示的记录数 
     */  
    private int pageRecords = 15;  
  
    /** 
     * 总记录数 
     */  
    private int totalRecord;
  
    /** 
     * 当前页的第一条数据编号
     */  
    private int startIndex;

    /** 
     * 总页数
     */  
    private int totalPage;
  
    /** 
     * 当前页号
     */  
    private int currentPage;
    
    public Pagination(int totalRecord) { 
    	currentPage = 1;
		this.totalRecord = totalRecord;
		init();
    }

    /**
     * 设置总页数的值
     */
	private void init() {
		totalPage = (totalRecord + pageRecords - 1) / pageRecords;
		if(totalPage == 0) totalPage++;
	}

	public int getPageRecords() {
		return pageRecords;
	}

	public void setPageRecords(int pageRecords) {
		if (pageRecords > 0 && pageRecords <= 50) {  
            this.pageRecords = pageRecords;  
            init();  
        }  
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public int getStartIndex() {
		startIndex = (currentPage-1) * pageRecords;
		return startIndex;
	}

	public int getTotalPage() {
		return totalPage;
	}
	
	/**
	 * 设置总记录数的值
	 * @param totalRecord
	 */
	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
		init();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		if (currentPage > totalPage) {
			this.currentPage = totalPage;
			return;
		}
		if (currentPage <= 0) {
			this.currentPage = 1;
			return;
		}
		this.currentPage = currentPage;
	}
	
}
