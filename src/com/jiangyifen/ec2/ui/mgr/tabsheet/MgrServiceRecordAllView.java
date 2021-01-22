package com.jiangyifen.ec2.ui.mgr.tabsheet;

import com.jiangyifen.ec2.ui.mgr.tabsheet.mgrhistoryservicerecord.MgrHistoryServiceRecordTabView;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @Description 描述：管理员查看的客服记录界面
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
 * @date    2013年11月15日 上午11:11:11
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class MgrServiceRecordAllView extends VerticalLayout {

	public final static String LAST_SR_CAPTION = "切至最终客服记录";
	public final static String DETAIL_SR_CAPTION = "切至记录详单";
	
	public final static String LAST_VIEW_TYPE = "view_last_service_recored";
	public final static String DETAIL_VIEW_TYPE = "view_detail_service_record";
	
	private MgrServiceRecord detailServiceRecord;							// 记录视角界面
	private MgrHistoryServiceRecordTabView lastServiceRecordTabView;		// 切至最终客服记录
	
	private Component selectedView;				// 当前选中的视角对用的组件 [MgrServiceRecord 或者  MgrHistoryServiceRecordTabView]
	
	public MgrServiceRecordAllView() {
		detailServiceRecord = new MgrServiceRecord(this);
		selectedView = detailServiceRecord;
		this.addComponent(selectedView);
	}

	/**
	 * 刷新界面
	 * 
	 * @param isToFirst
	 */
	public void updateTable(boolean isToFirst) {
		if(selectedView == detailServiceRecord) {
			detailServiceRecord.updateTable(isToFirst);
		} else if(selectedView == lastServiceRecordTabView) {
			lastServiceRecordTabView.refreshTable(isToFirst);
		}
	}
	
	/**
	 * @Description 描述：切换视角
	 *
	 * @author  JRH
	 * @date    2014年5月28日 下午2:22:14
	 * @param viewType void
	 */
	public void switchView(String viewType) {
		Component targetViewUi = null;
		if(DETAIL_VIEW_TYPE.equals(viewType)) {
			if(detailServiceRecord == null) {
				detailServiceRecord = new MgrServiceRecord(this);
			} 
			detailServiceRecord.updateTable(false);
			targetViewUi = detailServiceRecord;
		} else if(LAST_VIEW_TYPE.equals(viewType)) {
			if(lastServiceRecordTabView == null) {
				lastServiceRecordTabView = new MgrHistoryServiceRecordTabView(this);
			}
			lastServiceRecordTabView.refreshTable(false);
			targetViewUi = lastServiceRecordTabView;
		}

		if(targetViewUi != null) {
			this.replaceComponent(selectedView, targetViewUi);
			selectedView = targetViewUi;
		}
	}

}
