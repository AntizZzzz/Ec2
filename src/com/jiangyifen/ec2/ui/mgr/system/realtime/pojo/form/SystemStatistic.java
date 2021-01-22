package com.jiangyifen.ec2.ui.mgr.system.realtime.pojo.form;

public class SystemStatistic {

	private int findCode;
	private String findMsg;
	
	private long cupUse;

	private long memoryRamTotal;
	private long memoryRamUse;
	private long memoryRamFree;
	
	private long memorySwapTotal;
	private long memorySwapUse;
	private long memorySwapFree;
	
	private long diskRootTotal;
	private long diskRootUse;
	
	private long networkUp;
	private long networkDown;
	
	private String runTime;
	
	public int getFindCode() {
		return findCode;
	}
	public void setFindCode(int findCode) {
		this.findCode = findCode;
	}
	public String getFindMsg() {
		return findMsg;
	}
	public void setFindMsg(String findMsg) {
		this.findMsg = findMsg;
	}
	public long getCupUse() {
		return cupUse;
	}
	public void setCupUse(long cupUse) {
		this.cupUse = cupUse;
	}
	public long getMemoryRamTotal() {
		return memoryRamTotal;
	}
	public void setMemoryRamTotal(long memoryRamTotal) {
		this.memoryRamTotal = memoryRamTotal;
	}
	public long getMemoryRamUse() {
		return memoryRamUse;
	}
	public void setMemoryRamUse(long memoryRamUse) {
		this.memoryRamUse = memoryRamUse;
	}
	public long getMemoryRamFree() {
		return memoryRamFree;
	}
	public void setMemoryRamFree(long memoryRamFree) {
		this.memoryRamFree = memoryRamFree;
	}
	public long getMemorySwapTotal() {
		return memorySwapTotal;
	}
	public void setMemorySwapTotal(long memorySwapTotal) {
		this.memorySwapTotal = memorySwapTotal;
	}
	public long getMemorySwapUse() {
		return memorySwapUse;
	}
	public void setMemorySwapUse(long memorySwapUse) {
		this.memorySwapUse = memorySwapUse;
	}
	public long getMemorySwapFree() {
		return memorySwapFree;
	}
	public void setMemorySwapFree(long memorySwapFree) {
		this.memorySwapFree = memorySwapFree;
	}
	public long getDiskRootTotal() {
		return diskRootTotal;
	}
	public void setDiskRootTotal(long diskRootTotal) {
		this.diskRootTotal = diskRootTotal;
	}
	public long getDiskRootUse() {
		return diskRootUse;
	}
	public void setDiskRootUse(long diskRootUse) {
		this.diskRootUse = diskRootUse;
	}
	public long getNetworkUp() {
		return networkUp;
	}
	public void setNetworkUp(long networkUp) {
		this.networkUp = networkUp;
	}
	public long getNetworkDown() {
		return networkDown;
	}
	public void setNetworkDown(long networkDown) {
		this.networkDown = networkDown;
	}
	public String getRunTime() {
		return runTime;
	}
	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}
	
	 
	 
}
