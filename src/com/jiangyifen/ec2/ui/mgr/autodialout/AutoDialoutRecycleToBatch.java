package com.jiangyifen.ec2.ui.mgr.autodialout;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.AutoDialout;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 自动外呼的资源回收
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class AutoDialoutRecycleToBatch extends Window implements Button.ClickListener{
//	private Logger logger = LoggerFactory.getLogger(this.getClass());
/**
 * 主要组件输出
 */
	// 导入新批次组件
	private TextField batchName;
	private TextArea note;
		
		
	//关键字和搜索按钮
	private CheckBox missedCheckBox;
	private CheckBox notConnnectedCheckBox;
	
	private Label missedLabel;
	private Label notConnnectedLabel;
	
	//下面的添加和取消按钮
	private Button recycle;
	private Button cancel;
/**
 * 其他组件
 */
	private Domain domain;
	//持有从Table取出来的AutoDialoutTask引用
	private AutoDialoutTask autoDialoutTask;
	private CommonService commonService;
	//持有调用它的组件引用AutoDialout,以刷新父组件
	private AutoDialout autoDialout;
	
	private User loginUser=SpringContextHolder.getLoginUser();
	
	public AutoDialoutRecycleToBatch(AutoDialout autoDialout) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setWidth("30em");
		this.autoDialout=autoDialout;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeFull();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		// 批次名称
		windowContent.addComponent(buildBatchNameLayout());
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		
		windowContent.addComponent(new Label("<b>漏接电话:接通中的漏接电话</b></br><b>未接通电话:客户没接起的电话</b>",Label.CONTENT_XHTML));
		windowContent.addComponent(buildUpperLayout());
		windowContent.addComponent(buildLowerLayout());
		
		/*=========下面的按钮输出===========*/
		HorizontalLayout tempButtonsLayout=buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout, Alignment.BOTTOM_RIGHT);
		
//		//设置按钮样式
//		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	
	/**
	 * 初始化Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		commonService=SpringContextHolder.getBean("commonService");
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
	 * 创建第一排
	 * @return
	 */
	private HorizontalLayout buildUpperLayout() {
		HorizontalLayout layout=new HorizontalLayout();
		missedCheckBox=new CheckBox("漏接电话");
		layout.addComponent(missedCheckBox);
		missedLabel=new Label("");
		layout.addComponent(missedLabel);
		return layout;
	}

	/**
	 * 创建第二排
	 * @return
	 */
	private HorizontalLayout buildLowerLayout() {
		HorizontalLayout layout=new HorizontalLayout();
		notConnnectedCheckBox=new CheckBox("未接通电话");
		layout.addComponent(notConnnectedCheckBox);
		notConnnectedLabel=new Label("");
		layout.addComponent(notConnnectedLabel);
		return layout;
	}

	/**
	 * 创建此窗口最下边按钮的输出
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("40%");
		//添加
		recycle=new Button("回收");
		recycle.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(recycle);
		buttonsLayout.setComponentAlignment(recycle, Alignment.MIDDLE_CENTER);
		//取消
		cancel=new Button("取消");
		cancel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(cancel);
		buttonsLayout.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		return buttonsLayout;
	}

	/**
	 * 更新自动外呼显示的信息
	 */
	public void updateInfo() {

		//呼叫总数
		String callCountSql = "select count(*) from ec2_marketing_project_task where domain_id="+ domain.getId()
				+ " and autodialid="+autoDialoutTask.getId()+" and isfinished=true"; //加isfinished和域都 不起作用
		//客户应答总数
		String answeredCountSql = "select count(*) from ec2_marketing_project_task where domain_id="+ domain.getId()
				+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true";
		//坐席漏接总数
		String missedCountSql = "select count(*) from ec2_marketing_project_task where domain_id="+ domain.getId()
				+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and autodialiscsrpickup=false";

		// 查询数据并设置数据源
		Long callCountNum = 0L;
		Long answeredCountNum = 0L;
		Long missedCountNum = 0L;

		callCountNum=(Long) commonService.excuteNativeSql(callCountSql, ExecuteType.SINGLE_RESULT);
		answeredCountNum=(Long) commonService.excuteNativeSql(answeredCountSql,ExecuteType.SINGLE_RESULT);
		missedCountNum=(Long) commonService.excuteNativeSql(missedCountSql,ExecuteType.SINGLE_RESULT);
		
		//设置未接通与漏接电话数量
		missedLabel.setValue(missedCountNum);
		if(missedCountNum<1){
			missedCheckBox.setValue(false);
			missedCheckBox.setEnabled(false);
		}else{
			missedCheckBox.setValue(false);
			missedCheckBox.setEnabled(true);
		}
		
		notConnnectedLabel.setValue(callCountNum-answeredCountNum);
		if(callCountNum-answeredCountNum<1){
			notConnnectedCheckBox.setValue(false);
			notConnnectedCheckBox.setEnabled(false);
		}else{
			notConnnectedCheckBox.setValue(false);
			notConnnectedCheckBox.setEnabled(true);
		}
	}

	/**
	 * 执行回收操作
	 */
	private void executeRecycle() {
		String batchNameStr=StringUtils.trimToEmpty((String)batchName.getValue());
		String noteStr=StringUtils.trimToEmpty((String)note.getValue());
		if(batchNameStr.isEmpty()){
			NotificationUtil.showWarningNotification(this, "批次的名称不能为空！");
			return;
		}
		
		//提示
		if((Boolean)missedCheckBox.getValue()==false&&(Boolean)notConnnectedCheckBox.getValue()==false){
			NotificationUtil.showWarningNotification(this, "请选择回收漏接还是回收未接通！");
			return;
		}
		
		//创建一个空批次
		CustomerResourceBatch batch=new CustomerResourceBatch();
		batch.setBatchName(batchNameStr);
		batch.setNote(noteStr);
		batch.setUser(SpringContextHolder.getLoginUser());
		batch.setDomain(domain);
		batch.setBatchStatus(BatchStatus.USEABLE);
		batch.setCreateDate(new Date());
		batch.setCount(0L);
		batch=(CustomerResourceBatch)commonService.update(batch);
		
		OperationLogUtil.simpleLog(loginUser, "自动外呼-回收到批次："+autoDialoutTask.getAutoDialoutTaskName()+" Id:"+autoDialoutTask.getId()+" 批次："+batchNameStr);
		
		//回收漏接
		if((Boolean)missedCheckBox.getValue()==true){
			String missedSqlDelete = "delete from ec2_marketing_project_task where domain_id="+ domain.getId()
					+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and autodialiscsrpickup=false";
			String missedSql = "insert into ec2_customer_resource_ec2_customer_resource_batch(customerresources_id,customerresourcebatches_id) (select distinct customerresource_id,"+batch.getId()+" from ec2_marketing_project_task where domain_id="+ domain.getId()
					+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and autodialiscsrpickup=false)";
			commonService.excuteNativeSql(missedSql, ExecuteType.UPDATE);
			commonService.excuteNativeSql(missedSqlDelete, ExecuteType.UPDATE);
		}
		
		//回收未接通
		if((Boolean)notConnnectedCheckBox.getValue()==true){
			String notConnnectedDelete = "delete from ec2_marketing_project_task where domain_id="+ domain.getId()
					+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=false";
			String notConnnected = "insert into ec2_customer_resource_ec2_customer_resource_batch(customerresources_id,customerresourcebatches_id) (select distinct customerresource_id,"+batch.getId()+" from ec2_marketing_project_task where domain_id="+ domain.getId()
					+ " and autodialid="+autoDialoutTask.getId()+" and autodialisanswered=false)";
			commonService.excuteNativeSql(notConnnected, ExecuteType.UPDATE);
			commonService.excuteNativeSql(notConnnectedDelete, ExecuteType.UPDATE);
		}
		
		this.getParent().removeWindow(this);
		autoDialout.updateInfo();
	}
	
	/**
	 * 弹出新的窗口,刷新Table里面数据的值
	 */
	@Override
	public void attach() {
		super.attach();
		autoDialoutTask=autoDialout.getCurrentSelect();
		this.setCaption("自动外呼 "+autoDialoutTask.getAutoDialoutTaskName()+" 回收资源");
		updateInfo();
	}
	
	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 
	 * 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==recycle){
			executeRecycle(); 
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}

}
