package com.jiangyifen.ec2.ui.mgr.resourceimport;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceImport;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 编辑批次窗口
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class EditBatch extends Window implements Button.ClickListener{
/**
 * 主要组件	
 */
	//导入新批次组件
	private TextField batchName;
	private TextArea noteArea;

	//批次状态选择框
	private ComboBox statusComboBox;
	
	//编辑页面时的保存按钮
	private Button save;
	private Button cancel;
/**
 * 其他组件	
 */
	private ResourceImport resourceImport;
	//批次对象
	private CustomerResourceBatch batch;
	private CustomerResourceBatchService batchService;
	private CommonService commonService;
	
/**
 * 	编辑批次窗口
 */
	public EditBatch(ResourceImport resourceImport) {
		commonService=SpringContextHolder.getBean("commonService");
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.resourceImport=resourceImport;
		this.setSizeUndefined();
		this.setCaption("编辑批次");
		
		VerticalLayout windowContent=new VerticalLayout();
		this.setContent(windowContent);
		//添加Window内最大的Layout
		windowContent.setSizeUndefined();//.setSizeFull();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		
		// 批次名称
		windowContent.addComponent(buildBatchNameLayout());
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		// 批次状态编辑信息
		windowContent.addComponent(buildStatusLayout());
		//添加编辑和保存按钮
		HorizontalLayout editButtonsLayout=buildEditButtons();
		windowContent.addComponent(editButtonsLayout);
		windowContent.setComponentAlignment(editButtonsLayout, Alignment.BOTTOM_RIGHT);
	}
	
	/**
	 * 导入批次的名称
	 * @return
	 */
	private HorizontalLayout buildBatchNameLayout() {
		HorizontalLayout importBatchNameLayout = new HorizontalLayout();
		this.addComponent(importBatchNameLayout);

		importBatchNameLayout.addComponent(new Label("批次名称:"));
		batchName = new TextField();
		batchName.setInputPrompt("默认为日期");
		batchName.setWidth("7em");
		importBatchNameLayout.addComponent(batchName);
		return importBatchNameLayout;
	}
	
	/**
	 * 描述信息的输出
	 * @return
	 */
	private HorizontalLayout buildNoteLayout() {
		HorizontalLayout noteLayout = new HorizontalLayout();
		noteLayout.setSizeUndefined();
		noteLayout.addComponent(new Label("备注信息:"));
		noteArea = new TextArea();
		noteArea.setColumns(15);
		noteArea.setRows(3);
		noteArea.setWordwrap(true);
		noteArea.setInputPrompt("请输入与批次相关的备注信息！");
		noteLayout.addComponent(noteArea);
		return noteLayout;
	}

	/**
	 * 描述信息的输出
	 * @return
	 */
	private HorizontalLayout buildStatusLayout() {
		HorizontalLayout statusLayout = new HorizontalLayout();
		statusLayout.setSizeUndefined();
		statusLayout.addComponent(new Label("是否可用:"));
		statusComboBox=new ComboBox();
		for(BatchStatus batchStatus:BatchStatus.values()){
			statusComboBox.addItem(batchStatus);
		}
		statusComboBox.setNullSelectionAllowed(false);
		statusLayout.addComponent(statusComboBox);
		return statusLayout;
	}
	/**
	 * 添加编辑按钮
	 * @return
	 */
	private HorizontalLayout buildEditButtons(){
		HorizontalLayout editButtonsLayout=new HorizontalLayout();
		editButtonsLayout.setSpacing(true);
		
		save=new Button("保存");
		save.setStyleName(StyleConfig.BUTTON_STYLE);
		save.addListener(this);
		editButtonsLayout.addComponent(save);

		cancel=new Button("取消");
		cancel.setStyleName(StyleConfig.BUTTON_STYLE);
		cancel.addListener(this);
		editButtonsLayout.addComponent(cancel);
		return editButtonsLayout;
	}
	/**
	 * 每次Attach主动查找对应的批次信息，并显示
	 */
	@Override
	public void attach() {
		super.attach();
		batch= (CustomerResourceBatch) resourceImport.getTable().getValue();
		batchName.setValue(batch.getBatchName());
		if(batch.getNote()==null){
			noteArea.setValue("");
		}else{
			noteArea.setValue(batch.getNote());
		}
		statusComboBox.setValue(batch.getBatchStatus());
	}
	
	/**
	 * 监听保存和取消按钮的单击事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			
			//设置批次名称
			String batchNameStr="";
			if(batchName.getValue()!=null){
				batchNameStr=batchName.getValue().toString().trim();
			}
			if(batchNameStr.equals("")){
				NotificationUtil.showWarningNotification(this, "名字不能为空!");
				return;
			}
			batch.setBatchName(batchNameStr);
			BatchStatus batchStatus = (BatchStatus)statusComboBox.getValue();
			batch.setBatchStatus(batchStatus);
			taskStatusProcess(batch);
			//设置备注信息
			if(noteArea.getValue()!=null){
				batch.setNote(noteArea.getValue().toString());
			}
			if(batchService==null){
				batchService=SpringContextHolder.getBean("customerResourceBatchService");
			}
			batchService.update(batch);
			//必须放在更新Table之前，使更新Table时不在调用显示ConfirmWindow的方法
			resourceImport.updateTable(false);
		}
		this.getApplication().getMainWindow().removeWindow(this);
	}

	/**
	 * 对于任务状态进行处理
	 * @param batch2
	 * @param batchStatus
	 */
	private void taskStatusProcess(CustomerResourceBatch batch) {
		Boolean isUseable=false;
		if(batch.getBatchStatus()==BatchStatus.USEABLE){
			isUseable=true;
		}else if(batch.getBatchStatus()==BatchStatus.UNUSEABLE){
			isUseable=false;
		}
		//对于Task的中batchId为此批次的所有task进行状态更新操作
		String nativeSql="update ec2_marketing_project_task set isuseable="+isUseable+" where batchid="+batch.getId();
		commonService.excuteNativeSql(nativeSql, ExecuteType.UPDATE);
	}
	
}
