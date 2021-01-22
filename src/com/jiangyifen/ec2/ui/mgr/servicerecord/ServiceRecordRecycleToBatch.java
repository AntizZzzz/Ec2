package com.jiangyifen.ec2.ui.mgr.servicerecord;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrServiceRecord;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 回收资源组件
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class ServiceRecordRecycleToBatch extends Window implements Button.ClickListener {
	
	// 导入新批次组件
	private TextField batchName;
	private TextArea note;
	
	/**
	 * 主要组件输出
	 */
	private Label messageLabel;
	
	private String sqlSelect;
	private String sqlCount;
	
	// 进度条组件
	private VerticalLayout progressOuterLayout;;
	private HorizontalLayout progressLayout;;
	private ProgressIndicator pi;
	
	/* ===========下方的回收和取消按钮============ */
	// 回收和取消按钮
	private Button recycle;
	private Button cancel;

	/**
	 * 其他组件
	 */
	// 持有调用它的组件引用ProjectControl,以刷新父组件
	private MgrServiceRecord mgrServiceRecord;
	/*private MarketingProject project;
	private MarketingProjectTaskService marketingProjectTaskService;*/
	private CommonService commonService;
	private User user;
	private Domain domain;

	/**
	 * 构造器
	 */
	public ServiceRecordRecycleToBatch(MgrServiceRecord mgrServiceRecord) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.mgrServiceRecord=mgrServiceRecord;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		// 批次名称
		windowContent.addComponent(buildBatchNameLayout());
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		
		//显示消息的label
		messageLabel=new Label("",Label.CONTENT_XHTML);
		windowContent.addComponent(messageLabel);
		
		//创建进度条组件
		progressOuterLayout=mgrServiceRecord.getProgressLayout();
		progressLayout=new HorizontalLayout();
		progressOuterLayout.addComponent(progressLayout);
		
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
		
		//回收和取消按钮组件
		HorizontalLayout buttonLayout=new HorizontalLayout();
		buttonLayout.setSpacing(true);
		
		recycle=new Button("回收");
		recycle.setDescription("回收指定批次中的所有未拨打的资源");
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
		/*marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");*/
		commonService=SpringContextHolder.getBean("commonService");
		user = SpringContextHolder.getLoginUser();
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
	 * 回收方法，检查选择了哪个CheckBox，然后将对应的批次进行回收
	 */
	private void executeRecycle() {
		final String batchNameStr=StringUtils.trimToEmpty((String)batchName.getValue());
		final String noteStr=StringUtils.trimToEmpty((String)note.getValue());
		if(batchNameStr.isEmpty()){
			NotificationUtil.showWarningNotification(this, "批次的名称不能为空！");
			return;
		}
		
		OperationLogUtil.simpleLog(user, "回收到批次："+batchNameStr);
		
		//===============分页取出resource，然后和批次关联====================//
		new Thread(){
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				//初始化进度条
				pi.setEnabled(true);
				pi.setValue(0f);
				progressLayout.addComponent(new Label("回收进度:"));
				progressLayout.addComponent(pi);
				
				/*Long startTime=System.currentTimeMillis();*/
				//创建一个空批次
				CustomerResourceBatch batch=new CustomerResourceBatch();
				batch.setBatchName(batchNameStr);
				batch.setNote(noteStr);
				batch.setUser(user);
				batch.setDomain(domain);
				batch.setBatchStatus(BatchStatus.USEABLE);
				batch.setCreateDate(new Date());
				batch.setCount(0L);
				batch=(CustomerResourceBatch)commonService.update(batch);
				
				Long totalLong=(Long)commonService.excuteSql(sqlCount, ExecuteType.SINGLE_RESULT);
				
				//记录总数和已经完成的任务条数，从而计算完成的百分比
				float taskNum=0;
				float totalNum=totalLong.floatValue();
				
				// ===========以步长3000取 customerresources_id ==================//
				Long recordId = Long.MAX_VALUE;
				int step = 3000;
				// 每次加载step步长条数据
				List<CustomerServiceRecord> records = null;
				for (;;) {
					String partSql=" and e.id<"+recordId+" order";
					sqlSelect=sqlSelect.replace("order", partSql);
					records = (List<CustomerServiceRecord>) commonService.getEntityManager().createQuery(sqlSelect).setFirstResult(0).setMaxResults(step).getResultList();
					
					//在SQL中不适用distinct对取最小值没有影响
					if (records.size() > 0) {
						// 取得最小的Id值
						CustomerServiceRecord serviceRecord=(CustomerServiceRecord)records.get(records.size() - 1);
						recordId = serviceRecord.getId();
					} else {
						break;	// 如果资源加载完，则跳出for循环
					}

					//将资源进行关联
					//将资源存入Excel中
					for(CustomerServiceRecord record:records){
						taskNum++;
						
						//为资源添加关联的批次
						CustomerResource customerResource=record.getCustomerResource();
						Set<CustomerResourceBatch> batches = customerResource.getCustomerResourceBatches();
						batches.add(batch);
						customerResource.setCustomerResourceBatches(batches);
						commonService.update(customerResource);
						
						float percentage = taskNum/totalNum;  //显示比例
						pi.setValue(percentage);
					}
				}
				// =============================================================//
				/*Long endTime=System.currentTimeMillis();*/
				
				//导入完成后进度条的处理进度条
				pi.setEnabled(false);
				progressLayout.removeAllComponents();
				NotificationUtil.showWarningNotification(ServiceRecordRecycleToBatch.this.getApplication(), "回收"+totalLong+"条客服记录资源成功！");
			}
		}.start();
		//===========================================================//
		
		//更新消息
		this.getParent().removeWindow(this);
	}
	
	/**
	 * 每次显示组件时更新组件信息
	 */
	@Override
	public void attach() {
		super.attach();
		this.setCaption("回收资源");
		batchName.setValue("");
		note.setValue("");
		sqlSelect=mgrServiceRecord.getSqlSelect();
		sqlCount=mgrServiceRecord.getSqlCount();
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==recycle){
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