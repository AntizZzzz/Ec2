package com.jiangyifen.ec2.utils;

public class PaginationLong {
	
	/** 
     * 每页显示的记录数 
     */  
    private Long pageRecords = 15L;  
  
    /** 
     * 总记录数 
     */  
    private Long totalRecord;
  
    /** 
     * 当前页的第一条数据编号
     */  
    private Long startIndex;

    /** 
     * 总页数
     */  
    private Long totalPage;
  
    /** 
     * 当前页号
     */  
    private Long currentPage;
    
    public PaginationLong(Long totalRecord) { 
    	currentPage = 1L;
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

	public Long getPageRecords() {
		return pageRecords;
	}

	public void setPageRecords(Long pageRecords) {
		if (pageRecords >= 5 && pageRecords <= 50) {  
            this.pageRecords = pageRecords;  
            init();  
        }  
	}

	public Long getTotalRecord() {
		return totalRecord;
	}

	public Long getStartIndex() {
		startIndex = (currentPage-1) * pageRecords;
		return startIndex;
	}

	public Long getTotalPage() {
		return totalPage;
	}
	
	/**
	 * 设置总记录数的值
	 * @param totalRecord
	 */
	public void setTotalRecord(Long totalRecord) {
		this.totalRecord = totalRecord;
		init();
	}

	public Long getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Long currentPage) {
		if (currentPage > totalPage) {
			this.currentPage = totalPage;
			return;
		}
		if (currentPage <= 0) {
			this.currentPage = 1L;
			return;
		}
		this.currentPage = currentPage;
	}
	
}
