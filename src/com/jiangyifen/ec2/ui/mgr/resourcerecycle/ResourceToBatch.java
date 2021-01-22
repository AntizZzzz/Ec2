package com.jiangyifen.ec2.ui.mgr.resourcerecycle;

import java.util.Date;

import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceRecycle;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class ResourceToBatch  extends Window implements Button.ClickListener{
	/**
	 * 主要组件区域
	 */
	// 导入新批次组件
	private TextField batchName;
	private TextArea note;
	private Button save;
	private Button cancel;
	
	/**
	 * 附加组件
	 */
	private String sql;
	
	public ResourceToBatch() {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("自动外呼资源回收");
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		windowContent.addComponent(buildBatchNameLayout());
		windowContent.addComponent(buildNoteLayout());
		windowContent.addComponent(buildButtonsLayout());
	}
	
	//将资源回收到批次中
	public ResourceToBatch(AutoDialoutTask autoDialoutTask, String callType,
			Object dateFrom, Object dateTo,String sqlPart) {
		this();
		if(callType.equals(ResourceRecycle.NOTANSWERED)){
			sql="select customerresource_id from ec2_marketing_project_task where autodialid="+autoDialoutTask.getId()+" and autodialisanswered=false"+sqlPart;
		}else if(callType.equals(ResourceRecycle.NOTPICKUP)){
			sql="select customerresource_id from ec2_marketing_project_task where autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and autodialiscsrpickup=false"+sqlPart;
		}
	}

	public ResourceToBatch(MarketingProject project, String callType,
			Object dateFrom, Object dateTo,String sqlPart) {
		this();
		if(callType.equals(ResourceRecycle.NOTANSWERED)){
			sql="select customerresource_id from ec2_marketing_project_task where autodialisanswered=false and marketingproject_id="+project.getId()+" and autodialid is not null"+sqlPart;
		}else if(callType.equals(ResourceRecycle.NOTPICKUP)){
			sql="select customerresource_id from ec2_marketing_project_task where autodialisanswered=false and marketingproject_id="+project.getId()+" and autodialisanswered=true and autodialiscsrpickup=false and autodialid is not null"+sqlPart;
		}
	}

	public ResourceToBatch(String callType, Object dateFrom, Object dateTo,String sqlPart) {
		this();
		if(callType.equals(ResourceRecycle.NOTANSWERED)){
			sql="select customerresource_id from ec2_marketing_project_task where autodialid is not null and autodialisanswered=false"+sqlPart;
		}else if(callType.equals(ResourceRecycle.NOTPICKUP)){
			sql="select customerresource_id from ec2_marketing_project_task where autodialid is not null and autodialisanswered=true and autodialiscsrpickup=false"+sqlPart;
		}
	}
	
	/**
	 * 导入批次的名称
	 * 
	 * @return
	 */
	private HorizontalLayout buildBatchNameLayout() {
		HorizontalLayout importBatchNameLayout = new HorizontalLayout();
		this.addComponent(importBatchNameLayout);

		importBatchNameLayout.addComponent(new Label("批次名称:"));
		batchName = new TextField();
		batchName.setImmediate(true);
		batchName.setWriteThrough(true);
		batchName.setInputPrompt("批次名称");
		batchName.setWidth("7em");
		importBatchNameLayout.addComponent(batchName);
		return importBatchNameLayout;
	}

	/**
	 * 描述信息的输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildNoteLayout() {
		HorizontalLayout noteLayout = new HorizontalLayout();
		noteLayout.addComponent(new Label("备注信息:"));
		note = new TextArea();
		note.setColumns(15);
		note.setRows(3);
		note.setWordwrap(true);
		note.setInputPrompt("请输入与批次相关的备注信息！");
		noteLayout.addComponent(note);
		return noteLayout;
	}

	/**
	 * 上传文件组件输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout horizontalLayout= new HorizontalLayout();
		horizontalLayout.setSpacing(true);

		// 保存按钮
		save= new Button("保存");
		save.addListener(this);
		horizontalLayout.addComponent(save);

		// 取消按钮
		cancel= new Button("取消");
		cancel.addListener(this);
		horizontalLayout.addComponent(cancel);
		return horizontalLayout;
	}
	
	/**
	 * 执行保存操作
	 */
	private void executeSave() {
		String batchNameInner = (String)batchName.getValue();
		if(batchNameInner==null||batchNameInner.trim().equals("")){
			NotificationUtil.showWarningNotification(this, "批次名称不能为空");
			return;
		}
		String noteStr = (String)note.getValue();
		if(noteStr==null){
			noteStr="";
		}
		//存储批次
		CustomerResourceBatch batch=new CustomerResourceBatch();
		batch.setBatchName(batchNameInner.trim());
		batch.setCreateDate(new Date());
		batch.setNote(noteStr);
		batch.setDomain(SpringContextHolder.getDomain());
		batch.setUser(SpringContextHolder.getLoginUser());
		CustomerResourceBatchService batchService=SpringContextHolder.getBean("customerResourceBatchService");
		batch=batchService.update(batch);
		
		
		Long newBatchId=batch.getId();
		sql=sql.replaceAll("select", "select distinct "+newBatchId+",");
		String relationBuildSql="insert into ec2_customer_resource_ec2_customer_resource_batch(customerresourcebatches_id,customerresources_id) "+sql;
		CommonService commonService=SpringContextHolder.getBean("commonService");
		commonService.excuteNativeSql(relationBuildSql, ExecuteType.UPDATE);
		this.getApplication().getMainWindow().showNotification("成功回收资源到批次！");
		this.getParent().removeWindow(this);
	}
	
	/**
	 * 按钮点击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			executeSave();
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}
}
