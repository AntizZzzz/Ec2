package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.sysdisk.DiskUseInfoUtil;
import com.jiangyifen.ec2.ui.mgr.sysdisk.SoundRecordingWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * 系统首页
 * @author lxy
 * 
 */
@SuppressWarnings("serial")
public class FirstManagement extends VerticalLayout implements Button.ClickListener{

	private VerticalLayout mainLayout;

	private Panel tableNubmerPanel;
	private Label lbCustomerNum;
	private Label lbBatchNum;
	private Label lbCustomerRecordNum;
	private Button btnRealodTableNumber;
	
	private Panel diskPanel;
	private Label lbDiskChart;
	private Label lbDiskTxt;
	private Button btnReloadDisk;
	private Button btnSoundRecordingWinodow;
	private SoundRecordingWindow soundRecordingWindow;
	
	//private User loginUser;
	private Domain domain;
	
	private CommonService commonService ;
	
	public FirstManagement() {
		this.setWidth("100%");
		this.setMargin(true);

		mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);
		this.addComponent(mainLayout);
		
		initBusinessAndSpring();
		
		buildDiskLayout();					//磁盘使用信息
		
		buildBusinessTableNumberLayout();	//数据量
		reloadTableNumber();				//异步加载数据量
		
	}
	
	//业务属性
	private void initBusinessAndSpring(){// 初始化业务与Spring组件
		//loginUser  = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		commonService = SpringContextHolder.getBean("commonService");
	}
	
	private void reloadTableNumber() {
		(new Thread(new Runnable() {
			@Override
			public void run() {
				String customerNumJpql = "select count(S) from CustomerResource as S where S.domain.id = "+domain.getId() +" ";
				int customerNum = commonService.getEntityCount(customerNumJpql);
				lbCustomerNum.setValue(customerNum);
				
				String batchNumJpql = "select count(S) from CustomerResourceBatch as S where S.domain.id = "+domain.getId() +" ";
				int batchNum = commonService.getEntityCount(batchNumJpql);
				lbBatchNum.setValue(batchNum);
				
				String customerRecordNumJpql = "select count(S) from CustomerServiceRecord as S where S.domain.id = "+domain.getId() +" ";
				int customerRecordNum = commonService.getEntityCount(customerRecordNumJpql);
				lbCustomerRecordNum.setValue(customerRecordNum);
				
			}
		})).start();
		
	}

	private void buildBusinessTableNumberLayout(){
		tableNubmerPanel = new Panel();
		tableNubmerPanel.setSizeFull();
		tableNubmerPanel.setCaption("拥有数据情况");
		
		mainLayout.addComponent(tableNubmerPanel);
		GridLayout gridPanel = new GridLayout(5, 4);
		gridPanel.setSizeFull();
		tableNubmerPanel.addComponent(gridPanel);
		
		
		gridPanel.addComponent(new Label("拥有客户资源："),0,0);
		lbCustomerNum = new Label("--",Label.CONTENT_XHTML);
		gridPanel.addComponent(lbCustomerNum,1,0);
		
		gridPanel.addComponent(new Label("拥有批次数："),2,0);
		lbBatchNum = new Label("--",Label.CONTENT_XHTML);
		gridPanel.addComponent(lbBatchNum,3,0);
		
		
		btnRealodTableNumber = new Button("刷新",this);
		gridPanel.addComponent(btnRealodTableNumber,4,0);
		
		gridPanel.addComponent(new Label("客服记录数量："),0,1);
		lbCustomerRecordNum = new Label("--",Label.CONTENT_XHTML);
		gridPanel.addComponent(lbCustomerRecordNum,1,1);
		
	}
	
	/**
	 * 硬盘信息
	 */
	private void buildDiskLayout() {

		diskPanel = new Panel();
		diskPanel.setSizeFull();
		 
		diskPanel.setCaption("磁盘使用状态");
		
		DiskUseInfoUtil diskUseInfo = new DiskUseInfoUtil();
		diskUseInfo.excuteSheel();
		
		lbDiskChart = new Label(findDDB(diskUseInfo),Label.CONTENT_XHTML);
		diskPanel.addComponent(lbDiskChart);
		
		//LXY 多租户也可以查看磁盘信息if(ShareData.domainList.size() == 1){//单域情况下才可以使用该功能,多域不可用
			mainLayout.addComponent(diskPanel);
		//}
		
		HorizontalLayout m = new HorizontalLayout();
		m.setMargin(true,false,false,false);
		m.setWidth("100%");
		m.setSpacing(true);

		lbDiskTxt = new Label(findDiskLabelValue(diskUseInfo),Label.CONTENT_XHTML);
		m.addComponent(lbDiskTxt);
		
		
		HorizontalLayout m2 = new HorizontalLayout();
		
		btnReloadDisk = new Button("刷新",this);
		m2.addComponent(btnReloadDisk);
		
		/*if(ShareData.domainList.size() == 1){//单域情况下才可以使用该功能,多域不可用
			btnSoundRecordingWinodow = new Button("管理录音",this);
			m2.addComponent(btnSoundRecordingWinodow);
		}*/
		
		m.addComponent(m2);
		m.setComponentAlignment(m2, Alignment.BOTTOM_RIGHT);
		
		diskPanel.addComponent(m);
	}

	private String findDiskLabelValue(DiskUseInfoUtil diskUseInfo) {
		return " 总量："+diskUseInfo.getTotalValToShow()+" 使用："+diskUseInfo.getUseValToShow()+" 使用率 :"+diskUseInfo.getPercent();
	}

	  
	/**
	 * 由buttonClick调用，显示编辑用户的窗口
	 */
	private void showEditWindow() {
		if (soundRecordingWindow == null) {
			soundRecordingWindow = new SoundRecordingWindow(this);
			soundRecordingWindow.reloadGrid();
		}
		this.getApplication().getMainWindow().removeWindow(soundRecordingWindow);
		this.getApplication().getMainWindow().addWindow(soundRecordingWindow);
	}

	 
	 
	
	/**
	 * 由executeSearch调用更新表格内容,上层组件TabSheet调用
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 */
	public void refreshTable(Boolean isToFirst) {
		 
	}

	 
	 
	/**
	 * 单击按钮时触发的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == btnReloadDisk ) {
			reloadDiskInfo();
		} else if(source == btnSoundRecordingWinodow){
			showEditWindow();
		} else if(source == btnRealodTableNumber){
			reloadTableNumber();
		}
	}

	
	private String findDDB(DiskUseInfoUtil diskUseInfo) {
		StringBuffer ddbBuffer = new StringBuffer();
		String bgcolor = "#F9C824";
		double val = diskUseInfo.getPercentPoint();
		int percent = -1;
		if(val >= 0){
			percent = (int) (val*100);
		}
		
		if(val <= 0.6){
			bgcolor = "#92FCA5";
		}else if(val > 0.6 && val <= 0.7){
			bgcolor = "#04A923";
		}else if(val > 0.7 && val <= 0.8){
			bgcolor = "#E6DE37";
		}else if(val > 0.8 && val <= 0.9){
			bgcolor = "#F49C2A";
		}else if(val > 0.9 && val <= 1){
			bgcolor = "#F84125";
		}
		
		ddbBuffer.append("<dl style=\"margin:0;padding:0;width:100%;\">");
			ddbBuffer.append("<dd style=\"position:relative;display:block;float:left;width:100%;height:30px; margin:0;border:1px solid #eee;\">");
				ddbBuffer.append("<div style=\"position:relative;background:"+bgcolor+";height:30px;line-height:30px;text-align:right;width:" + percent + "%;\"><strong>" + percent + "%</strong>");
				ddbBuffer.append("</div>");
			ddbBuffer.append("</dd>");
		ddbBuffer.append("</dl>");
		return ddbBuffer.toString();
	}

	public void reloadDiskInfo() {
		DiskUseInfoUtil diskUseInfo = new DiskUseInfoUtil();
		diskUseInfo.excuteSheel();
		lbDiskChart.setValue(findDDB(diskUseInfo));
		lbDiskTxt.setValue(findDiskLabelValue(diskUseInfo));
	}
}
