package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 回收资源组件
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class RecycleByBatch extends Window implements Button.ClickListener {
	/**
	 * 主要组件输出
	 */
	private Label messageLabel;
	//所有的CheckBox组件
	private Panel checkBoxPanel;
	private ArrayList<CheckBox> checkBoxList;
	
	/* ===========下方的回收和取消按钮============ */
	// 回收和取消按钮
	private Button selectAll;
	private Button deselectAll;
	private Button reverseSelect;
	private Button recycle;
	private Button cancel;

	/**
	 * 其他组件
	 */
	// 持有调用它的组件引用ProjectControl,以刷新父组件
	private ProjectControl projectControl;
	private MarketingProject project;
	private MarketingProjectTaskService marketingProjectTaskService;
	private CommonService commonService;
	private Domain domain;

	private User loginUser=SpringContextHolder.getLoginUser();
	/**
	 * 构造器
	 */
	public RecycleByBatch(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.projectControl=projectControl;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//显示消息的label
		messageLabel=new Label("",Label.CONTENT_XHTML);
		windowContent.addComponent(messageLabel);
		
		//显示CheckBox的布局组件
		checkBoxPanel=new Panel();
		checkBoxPanel.setWidth("30em");
		windowContent.addComponent(checkBoxPanel);
		
		//回收和取消按钮组件
		HorizontalLayout buttonLayout=new HorizontalLayout();
		buttonLayout.setSpacing(true);
		
		selectAll=new Button("全选");
		selectAll.addListener(this);
		buttonLayout.addComponent(selectAll);
		
		deselectAll=new Button("全不选");
		deselectAll.addListener(this);
		buttonLayout.addComponent(deselectAll);

		reverseSelect=new Button("反选");
		reverseSelect.addListener(this);
		buttonLayout.addComponent(reverseSelect);
		
		recycle=new Button("回收");
		recycle.addListener(this);
		buttonLayout.addComponent(recycle);
		
		cancel=new Button("取消");
		cancel.addListener(this);
		buttonLayout.addComponent(cancel);
		windowContent.addComponent(buttonLayout);
		windowContent.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
	}
	
	/**
	 * 初始化Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
		commonService=SpringContextHolder.getBean("commonService");
	}

	/**
	 * 回收方法，检查选择了哪个CheckBox，然后将对应的批次进行回收
	 */
	private void executeRecycle() {
		//取出选中要回收的批次集合
		List<CustomerResourceBatch> toRecycleBatches=new ArrayList<CustomerResourceBatch>();
		for(CheckBox batchCheckBox:checkBoxList){
			if(batchCheckBox.booleanValue())
				toRecycleBatches.add((CustomerResourceBatch)batchCheckBox.getData());
		}
		if(toRecycleBatches.isEmpty()){
			//如果用户没有选择批次，显示提示信息，不关闭窗口
			NotificationUtil.showWarningNotification(this, "请选择至少一个批次！！！");
			 return;
		}
		
		for(CustomerResourceBatch customerResourceBatch:toRecycleBatches){
			OperationLogUtil.simpleLog(loginUser, "项目控制-回收任务："+customerResourceBatch.getBatchName());
		}
		
		//移除Window后就取不到Application了，所以先取出mainWindow的引用
		Window mainWindow=this.getApplication().getMainWindow();
		//执行回收操作，返回回收的数值
		Long recycleNum=marketingProjectTaskService.recycleByBatches(project, toRecycleBatches, domain);
		//更新消息
		this.getParent().removeWindow(this);
		projectControl.updateProjectResourceInfo();
		//显示提示
		Notification msgNotif=new Notification("项目"+project.getProjectName()+"成功回收了"+recycleNum+"条资源!",Notification.TYPE_HUMANIZED_MESSAGE);
		mainWindow.showNotification(msgNotif);
	}
	
	/**
	 * 每次显示组件时更新组件信息
	 */
	@Override
	public void attach() {
		super.attach();
		this.setCaption("按批次回收资源");
		//项目不应该为null
		project=(MarketingProject)projectControl.getTable().getValue();
		checkBoxPanel.removeAllComponents();//初始化panel
		checkBoxList=new ArrayList<CheckBox>();//初始化checkBoxList
		Set<CustomerResourceBatch> batchesSet = project.getBatches();
		List<CustomerResourceBatch> batches = new ArrayList<CustomerResourceBatch>(batchesSet);
		
		//约束组件
		GridLayout constraintLayout=new GridLayout(2,batches.size());
		for(int i=0;i<batches.size();i++){
			CustomerResourceBatch batch=batches.get(i);
			//CheckBox组件
			CheckBox batchCheckBox=new CheckBox(batch.getBatchName());
			batchCheckBox.setData(batch);
			//信息显示组件
			//已完成:"已分配:"总数:
			String nativeSql="select isfinished,user_id is not null as ishaveuid,count(*) from ec2_marketing_project_task where domain_id="+
					domain.getId()+" and customerresource_id in(" +
					"select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") " +
					"and marketingproject_id="+project.getId()+" group by isfinished,ishaveuid";
			
			//查询数据并设置数据源
			Long notDistributedNum=0L;
			Long finishedNum = 0L;
			Long notFinishedNum = 0L;
			
			@SuppressWarnings("unchecked")
			List<Object[]> nums = (List<Object[]>)commonService.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST);
			if(nums.size()>3) throw new RuntimeException("用户Id为null，应该标记为未完成状态！");
			for(int j=0;j<nums.size();j++){
				Object[] objects=nums.get(j);
				Boolean isfinished=(Boolean)objects[0];
				Boolean ishaveuid=(Boolean)objects[1];
				Long count=(Long)objects[2];
				if(isfinished==false&&ishaveuid==false){ //没有用户名，没有完成状态  -- 未分配
					notDistributedNum=count;
				}else if(isfinished==true&&ishaveuid==true){ //有用户名，  已完成状态 -- 已完成
					finishedNum=count;
				}else if(isfinished==false&&ishaveuid==true){ //有用户名，  未完成状态 -- 未完成
					notFinishedNum=count;
				}
			}
			//显示的信息
			Label infoLabel = new Label("    已完成:" + finishedNum +"    未完成:" + notFinishedNum+ "    已分配:"
					+ (finishedNum+notFinishedNum) + "    未分配:" + notDistributedNum +"    总数:" + (finishedNum+notFinishedNum+notDistributedNum));
			batchCheckBox.setEnabled(false);
			//含有已经分配并且并未完成
			if(notFinishedNum>0) batchCheckBox.setEnabled(true);
			constraintLayout.addComponent(batchCheckBox,0,i);
			constraintLayout.addComponent(infoLabel,1,i);
			//yongList记录所有的CheckBox
			checkBoxList.add(batchCheckBox);
			//添加Layout到Panel
			checkBoxPanel.addComponent(constraintLayout);
		}
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==selectAll){
			for(CheckBox checkBox:checkBoxList){
				checkBox.setValue(true);
			}
		}else if(event.getButton()==deselectAll){
			for(CheckBox checkBox:checkBoxList){
				checkBox.setValue(false);
			}
		}else if(event.getButton()==reverseSelect){
			for(CheckBox checkBox:checkBoxList){
				checkBox.setValue(!(Boolean)checkBox.getValue());
			}
		}else if(event.getButton()==recycle){
			try {
				executeRecycle();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "回收出现异常！");
			}
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}
}