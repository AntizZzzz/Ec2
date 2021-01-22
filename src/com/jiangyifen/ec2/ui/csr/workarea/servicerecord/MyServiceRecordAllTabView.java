package com.jiangyifen.ec2.ui.csr.workarea.servicerecord;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.csr.workarea.historyservicerecord.MyHistoryServiceRecordTabView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @Description 描述：坐席查看的客服记录界面
 * 
 * 		该界面将原来的“客服记录详情”界面和以单个客户为视角的“历史客服记录”界面合并到一起了
 * 
 * 		客服记录查看的两种是视角：客户视角界面、记录视角界面
 * 
 * 客户视角界面: 
 * 		即以客户为中心，无论坐席给他打了多少次电话，做了多少次客服记录，哪怕10000条记录，最终在界面上只显示最近一次跟他联系的记录，但可以在该界面查询其他历史记录
 * 
 * 记录视角界面：
 * 		即以客服记录本身为中心，也就是说如果坐席跟他联系了5次，并且创建了5条客服记录，这样在界面上将直接显示着5条客服记录的详情
 * 
 * @author  jrh
 * @date    2013年11月15日 下午1:58:52
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class MyServiceRecordAllTabView extends VerticalLayout {

	public final static String LAST_SR_CAPTION = "切至最终客服记录";
	public final static String DETAIL_SR_CAPTION = "切至记录详单";
	
	public final static String LAST_VIEW_TYPE = "view_last_service_recored";
	public final static String DETAIL_VIEW_TYPE = "view_detail_service_record";
	
	private User loginUser;						// 当前登录的用户 
	
	private Component selectedView;				// 当前选中的视角对用的组件 [MgrServiceRecord 或者  MgrHistoryServiceRecordTabView]

	private MyServiceRecordTabView detailServiceRecord;						// 记录视角界面
	private MyHistoryServiceRecordTabView lastServiceRecordTabView;		// 切至最终客服记录
	
	public MyServiceRecordAllTabView() {
		loginUser = SpringContextHolder.getLoginUser();
		
		detailServiceRecord = new MyServiceRecordTabView(this);
		selectedView = detailServiceRecord;
		this.addComponent(selectedView);
	}

	/**
	 * @Description 描述：切换视角
	 *
	 * @author  JRH
	 * @date    2014年5月28日 下午2:22:14
	 * @param viewType void
	 */
	public void switchView(String viewType) {
		if(DETAIL_VIEW_TYPE.equals(viewType)) {
			if(detailServiceRecord == null) {
				detailServiceRecord = new MyServiceRecordTabView(this);
			} 
			this.replaceComponent(selectedView, detailServiceRecord);
			
			ShareData.csrToCurrentTab.put(loginUser.getId(), detailServiceRecord);	// 修改当前坐席在内存中对应的主界面
			detailServiceRecord.refreshTable(false);
			selectedView = detailServiceRecord;
		} else if(LAST_VIEW_TYPE.equals(viewType)) {
			if(lastServiceRecordTabView == null) {
				lastServiceRecordTabView = new MyHistoryServiceRecordTabView(this);
			}
			this.replaceComponent(selectedView, lastServiceRecordTabView);

			ShareData.csrToCurrentTab.put(loginUser.getId(), lastServiceRecordTabView);	// 修改当前坐席在内存中对应的主界面
			lastServiceRecordTabView.refreshTable(false);
			selectedView = lastServiceRecordTabView;
		}

	}
	
	/**
	 * 刷新界面,由于坐席界面比较特殊，所以在刷新界面的同时还需要修改当前坐席在内存中对应的主界面
	 * 
	 * @param isToFirst  是否跳至首页
	 */
	public void refreshTable(boolean isToFirst) {
		if(selectedView == detailServiceRecord) {
			detailServiceRecord.refreshTable(isToFirst);
			ShareData.csrToCurrentTab.put(loginUser.getId(), detailServiceRecord);
		} else if(selectedView == lastServiceRecordTabView) {
			lastServiceRecordTabView.refreshTable(isToFirst);
			ShareData.csrToCurrentTab.put(loginUser.getId(), lastServiceRecordTabView);
		}
	}


}
