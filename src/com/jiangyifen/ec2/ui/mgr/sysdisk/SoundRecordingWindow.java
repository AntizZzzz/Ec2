package com.jiangyifen.ec2.ui.mgr.sysdisk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.FirstManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class SoundRecordingWindow  extends Window implements Button.ClickListener,Property.ValueChangeListener{

	private static final long serialVersionUID = -1786635446241303230L;
	private final Logger logger = LoggerFactory.getLogger(SoundRecordingWindow.class);
	
	private final String[] COL_HEADERS_DEFAULT = new String[] { "存储目录","文件件名","占用空间"};
	private final Object[] COL_PROPERTIES_DEFAULT = new Object[] {"absolutePath","name","fileTotalSize"};
	
	private Button btnReload;					// 查询按钮
	private Button btnDelete;					// 查询按钮
	private Label lbInfo;
	
	private Table table;						// 表格组件
	private BeanItemContainer<SoundRecording> soundRecordingContainer;
	
	private ChooseDeleteWindow chooseDeleteWindow;//删除文件夹窗体
	private FirstManagement firstManagement;	//首页
	
	private User loginUser;
	private Domain domain;
	
	public SoundRecordingWindow(FirstManagement firstManagement){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setWidth("600px");
		this.setCaption("管理录音文件");

		this.firstManagement = firstManagement;
		
		initBusinessAndSpring();			// 初始化业务与Spring组件
 		createOneLayout();
 		createTableLayout();
	}
	
	//业务属性
	private void initBusinessAndSpring(){// 初始化业务与Spring组件
		loginUser  = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
	}
	
	//第一行组件
	private void createOneLayout() {
		HorizontalLayout honeLayout = new HorizontalLayout();
		this.addComponent(honeLayout);
		
		btnReload = new Button("刷新",this);
		honeLayout.addComponent(btnReload);
		
		btnDelete = new Button("删除文件夹",this);
		btnDelete.setEnabled(false);
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.SYSTEM_MANAGEMENT_FIRSTMANAGEMENT_REMOVEFILE)){
			if(ShareData.domainList.size() == 1){//单域情况下才可以使用该功能,多域不可用
				honeLayout.addComponent(btnDelete);
			}
		}
		
		lbInfo = new Label("",Label.CONTENT_XHTML);
		honeLayout.addComponent(lbInfo);
		
	}
	
	// 创建分机表格显示区组件
	private void createTableLayout() {//表格组件
		table = createFormatColumnTable();
		table.setWidth("100%");	
		table.setHeight("100%");
 		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
 		table.addStyleName("striped");
 		table.addListener(this);
		this.addComponent(table);
	}
	
	//创建table信息
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = 698394523336680735L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}


	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnReload){
			reloadGrid();
		}else if(source == btnDelete){
			try {
				if(table.getValue() != null ){
					if (chooseDeleteWindow == null) {
						chooseDeleteWindow = new ChooseDeleteWindow("<font color='red' > 删除文件需要一段时间，请稍等一段时间后，在点击【刷新】。删除文件夹下的录音,不能恢复,确定删除吗？</font>","SoundRecording",this);
					}
					chooseDeleteWindow.setSoundRecording((SoundRecording)table.getValue());
					this.getApplication().getMainWindow().removeWindow(chooseDeleteWindow);
					this.getApplication().getMainWindow().addWindow(chooseDeleteWindow);
				}else{//没有选择记录
					lbInfo.setValue("请先选择记录!");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			reloadGrid();
		}
	}

	//重新加载文件加信息
	public void reloadGrid() {
		try {
			DiskUseInfoUtil diskUseInfoUtil =  new DiskUseInfoUtil();
	 		soundRecordingContainer = new BeanItemContainer<SoundRecording>(SoundRecording.class);
	 		soundRecordingContainer.removeAllItems();
	 		soundRecordingContainer.addAll(diskUseInfoUtil.getSoundRecordings());
			table.setContainerDataSource(soundRecordingContainer);
			table.setVisibleColumns(COL_PROPERTIES_DEFAULT);
			table.setColumnHeaders(COL_HEADERS_DEFAULT);
			firstManagement.reloadDiskInfo();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		if (table.getValue() != null) {
			btnDelete.setEnabled(true);
		} else {
			btnDelete.setEnabled(false);
		}
		this.lbInfo.setValue("");
	}

	/**
	 * 确定删除文件夹 回调函数
	 * @param soundRecording
	 * @param deleteTf
	 */
	public void deleteOkBack(SoundRecording soundRecording,boolean deleteTf) {
		try {
			if(deleteTf){
				if(soundRecording != null ){
					DiskUseInfoUtil diskUseInfoUtil =  new DiskUseInfoUtil();
					diskUseInfoUtil.removeSoundRecording(soundRecording,domain,loginUser);
					reloadGrid();	
					lbInfo.setValue("删除文件夹成功");
				}else{//没有选择记录
					reloadGrid();
					lbInfo.setValue("请先选择记录");
				}
			}
			chooseDeleteWindow.closeWindow();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
