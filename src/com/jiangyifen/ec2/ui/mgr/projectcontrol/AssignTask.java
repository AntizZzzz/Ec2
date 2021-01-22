package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.mgr.DistributeToTaskService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AssignTask extends Window implements Button.ClickListener{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件
	 */
	private Panel checkBoxPanel;
	private TextField assignNumber;
	private Button assign;
	private Button cancel;

	private ArrayList<CheckBox> checkBoxList;
	/**
	 * 其他组件
	 */
	private ProjectControl projectControl;
	private MarketingProject marketingProject;
	private DistributeToTaskService distributeToTaskService;
	private CommonService commonService;
	private MarketingProjectService marketingProjectService;
	private Domain domain;
	
	/**
	 * 项目中所有坐席数，在线的项目坐席数
	 */
	private Label projectCsr;
	private Label onlineCsr;
	private CheckBox isOnlyOnlineCsr;

	private List<User> projectUsers;
	private List<User> onlineUsers;
	
	private User loginUser=SpringContextHolder.getLoginUser();
	
	public AssignTask(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setResizable(false);
		this.setCaption("派发任务");
		this.setModal(true);
		this.projectControl = projectControl;

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		/**
		 * 输入每个CSR分配的资源数目
		 */
		Label label = new Label(
				"注:给当前项目的所有未完成任务数小于资源上限的CSR分配任务("
						+ "如果资源充足,为CSR分配未完成任务到上限,否则,为未完成任务数最少的几个CSR分配资源,使几个CSR的未完成任务到一个平均值)");
		label.setWidth("25em");
		windowContent.addComponent(label);

		checkBoxPanel = new Panel();
		checkBoxPanel.setWidth("30em");
		windowContent.addComponent(checkBoxPanel);
		
		//在线坐席数输出
		HorizontalLayout csrCountLayout=new HorizontalLayout();
		Label projectCsrCaption=new Label("项目坐席数");
		projectCsr=new Label("");
		Label onlineCsrCaption=new Label("在线坐席数");
		onlineCsr=new Label("");
		isOnlyOnlineCsr=new CheckBox("只分给在线坐席");
		csrCountLayout.addComponent(projectCsrCaption);
		csrCountLayout.addComponent(projectCsr);
		csrCountLayout.addComponent(new Label("&nbsp;&nbsp;&nbsp;",Label.CONTENT_XHTML));
		csrCountLayout.addComponent(onlineCsrCaption);
		csrCountLayout.addComponent(onlineCsr);
		csrCountLayout.addComponent(new Label("&nbsp;&nbsp;&nbsp;",Label.CONTENT_XHTML));
		csrCountLayout.addComponent(isOnlyOnlineCsr);
		windowContent.addComponent(csrCountLayout);
		
		
		HorizontalLayout textFieldConstraint = new HorizontalLayout();
		textFieldConstraint.setSpacing(true);
		textFieldConstraint.addComponent(new Label("个人拥有任务上限数:"));

		assignNumber = new TextField();
		assignNumber.setWidth("120px");
		assignNumber.setInputPrompt("上限值不要过大！");
		assignNumber.setDescription("<B>实际分配数量 = 任务上限值 - 已拥有任务数(未分配任务充足的情况下)</B>");
		assignNumber.addValidator(new RegexpValidator("[1-9]\\d*", "指派数量只能是大于0的数字，请重新输入！"));
//		TODO jrh 
//		assignNumber.setDescription("最多不能超过"
//				+ ConfigProperty.ASSIGN_RESOURCE_NUMBER + "条！");
		textFieldConstraint.addComponent(assignNumber);
		windowContent.addComponent(textFieldConstraint);

		/**
		 * 创建按钮
		 */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		windowContent.addComponent(buttonsLayout);
		windowContent.setComponentAlignment(buttonsLayout,
				Alignment.BOTTOM_RIGHT);

		assign = new Button("指派");
		assign.addListener(this);
		buttonsLayout.addComponent(assign);

		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		domain = SpringContextHolder.getDomain();
		distributeToTaskService = SpringContextHolder
				.getBean("distributeToTaskService");
		commonService=SpringContextHolder.getBean("commonService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
	}

	/**
	 * 由buttonClick调用将资源分配给CSR，使资源变为现实的任务
	 */
	private void executeAssign() {
		
		// 由于assignNumber.getValue()可能为null，所以没用toString()方法

		// jrh 判断输入的指派数量是否合法
		if(!"".equals(assignNumber.getValue()) && assignNumber.isValid()) {
			//对用户输入的数据和用户选择的批次进行处理
			int num = Integer.parseInt((assignNumber.getValue() + "").toString());
			// 大小范围不对，抛出异常
			if (num <= 0) {
				throw new NumberFormatException();
			}

			Window mainWindow = this.getApplication().getMainWindow();
			// 将用户选中的批次添加到要分配的集合当中
			List<CustomerResourceBatch> toDistributeBatch = new ArrayList<CustomerResourceBatch>();
			for (CheckBox checkBox : checkBoxList) {
				if ((Boolean) checkBox.getValue()) {
					toDistributeBatch.add((CustomerResourceBatch) checkBox
							.getData());
				}
			}
			if (toDistributeBatch.isEmpty()) {
				// 如果用户没有选择批次，显示提示信息，不关闭窗口
				NotificationUtil.showWarningNotification(this, "请选择至少一个批次！！！");
				return;
			}

			this.getParent().removeWindow(this);
			
			Set<User> toDistributedUsers=null;
			if((Boolean)isOnlyOnlineCsr.getValue()){ //如果是true
				toDistributedUsers=new HashSet<User>(onlineUsers);
			}else{ //如果是false
				toDistributedUsers=new HashSet<User>(projectUsers);
			}
			
			List<Long> ids=new ArrayList<Long>();
			for(CustomerResourceBatch customerResourceBatch:toDistributeBatch){
				ids.add(customerResourceBatch.getId());
			}
			OperationLogUtil.simpleLog(loginUser, "项目控制-指派任务：分配资源 Ids:"+ids);
			
			//执行分配操作，返回分配数量
			Long distributeNum = distributeToTaskService.distribute(
					marketingProject, toDistributeBatch,
					toDistributedUsers, num+0L,
					domain);

			mainWindow.showNotification("成功分配" + distributeNum + "个资源给CSR");
			projectControl.updateTable(false);
			projectControl.updateProjectResourceInfo();
		} else {
			assignNumber.setValue("");
			Notification notif = new Notification("上限值只能是大于0的数字，请重新输入！");
			this.showNotification(notif);
		}
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		// 项目不应该为null
		marketingProject = projectControl.getCurrentSelect();
		checkBoxPanel.removeAllComponents();// 初始化panel
		checkBoxList = new ArrayList<CheckBox>();// 初始化checkBoxList
		assignNumber.setValue("");
		Set<CustomerResourceBatch> batchesSet = marketingProject.getBatches();
		List<CustomerResourceBatch> batches = new ArrayList<CustomerResourceBatch>(
				batchesSet);
		// 根据项目取出的批次动态地生成ComboBox外面的Layout
		GridLayout constraintLayout = new GridLayout(2, batches.size());
		for (int i = 0; i < batches.size(); i++) {
			CustomerResourceBatch batch = batches.get(i);
			// CheckBox组件
			CheckBox batchCheckBox = new CheckBox(batch.getBatchName());
			batchCheckBox.setData(batch);
			// 已完成:"已分配:"总数:
			String nativeSql="select isfinished,user_id is not null as ishaveuid,count(*) from ec2_marketing_project_task where domain_id="+
					domain.getId()+" and customerresource_id in(" +
					"select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="+batch.getId()+") " +
					"and marketingproject_id="+marketingProject.getId()+" group by isfinished,ishaveuid";
			
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
			
			if (notDistributedNum>0) {
				batchCheckBox.setEnabled(true);
			} else {
				batchCheckBox.setEnabled(false);
			}
			constraintLayout.addComponent(batchCheckBox, 0, i);
			constraintLayout.addComponent(infoLabel, 1, i);
			//批次List记录所有的CheckBox
			checkBoxList.add(batchCheckBox);
			// 添加Layout到Panel
			checkBoxPanel.addComponent(constraintLayout);
		}
		
		//计算项目的坐席数
		projectUsers=marketingProjectService.getCsrsByProject(marketingProject, domain);
		projectCsr.setValue(projectUsers.size());
		
		//项目在线的坐席数
		onlineUsers=new ArrayList<User>();
		for(User user:projectUsers){
			if(ShareData.userToExten.keySet().contains(user.getId())){
				onlineUsers.add(user);
			}
		}
		onlineCsr.setValue(onlineUsers.size());
		
		isOnlyOnlineCsr.setValue(true);
	}

	/**
	 * 点击保存和取消按钮的事件监听器
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == assign) {
			// 在执行分配的方法时显示提示信息，进行Try Catch操作
			Long startTime=System.currentTimeMillis();
			executeAssign();
			Long endTime=System.currentTimeMillis();
			logger.info("按批次指派耗时:"+(endTime-startTime)/1000+"秒");
		} else {
			this.getParent().removeWindow(this);
		}
	}
}
