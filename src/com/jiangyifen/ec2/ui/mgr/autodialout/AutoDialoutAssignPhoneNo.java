package com.jiangyifen.ec2.ui.mgr.autodialout;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.AutoDialout;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 为自动外呼管理手机成员
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class AutoDialoutAssignPhoneNo extends Window implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息
	
	private ListSelect phoneNos_ls;									// 电话成员显示区
	private TextField inputPhoneNo_tf;								// 添加电话输入区
	private Button edit_button;										// 编辑按钮
	private Button add_button;										// 添加按钮
	private Button delete_button;									// 删除按钮
	private Button save_button;										// 保存按钮
	private Button cancel_button;									// 取消按钮

	private AutoDialout autoDialout;								// 自动外呼Tab 页对象
	
	private BeanItemContainer<String> phoneNosContainer;			// 存放电话号码的容器

	//持有从Table取出来的AutoDialoutTask引用
	private AutoDialoutTask autoDialoutTask;						// 自动外呼Tab 页当前选中的自动外呼对象
	private HashSet<String> oldPhoneNosSet;							// 自动外呼任务原始的电话成员
	
	private AutoDialoutTaskService autoDialoutTaskService;			// 自动外呼任务服务类
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员管理服务类
	private User loginUser=SpringContextHolder.getLoginUser();
	
	public AutoDialoutAssignPhoneNo(AutoDialout autoDialout) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.autoDialout = autoDialout;
		
		autoDialoutTaskService = SpringContextHolder.getBean("autoDialoutTaskService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
		
		phoneNosContainer = new BeanItemContainer<String>(String.class);

		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		windowContent.addComponent(createPhoneNosLayout());
		
	}

	/**
	 * 弹出新的窗口,刷新Table里面数据的值
	 */
	@Override
	public void attach() {
		super.attach();
		autoDialoutTask = autoDialout.getCurrentSelect();
		if(autoDialoutTask.getPhoneNos() != null) {
			oldPhoneNosSet = new HashSet<String>(autoDialoutTask.getPhoneNos());
		} else {
			oldPhoneNosSet = new HashSet<String>();
		}
		
		// 更新组件中的值
		phoneNosContainer.removeAllItems();
		phoneNosContainer.addAll(oldPhoneNosSet);
		
		this.setCaption("添加/移除手机成员");
	}

	/**
	 * 创建管理电话号的界面
	 * 
	 * @return
	 */
	private VerticalLayout createPhoneNosLayout() {
		VerticalLayout phoneNos_l = new VerticalLayout();
		phoneNos_l.setSpacing(true);
		phoneNos_l.setMargin(true);

		// 管理员管理的字段的组件
		phoneNos_ls = new ListSelect("手机成员：");
		phoneNos_ls.setContainerDataSource(phoneNosContainer);
		phoneNos_ls.setNullSelectionAllowed(false);
		phoneNos_ls.setMultiSelect(true);
		phoneNos_ls.setWidth("250px");
		phoneNos_l.addComponent(phoneNos_ls);

		// 添加可以输入的组件
		inputPhoneNo_tf = new TextField();
		inputPhoneNo_tf.setInputPrompt("请输入正确的手机号码");
		inputPhoneNo_tf.setImmediate(true);
		inputPhoneNo_tf.setWidth("250px");
		inputPhoneNo_tf.addValidator(new RegexpValidator("\\d{7,11}", "号码不能为空，并且只能是数字，长度为7-11位"));
		inputPhoneNo_tf.setValidationVisible(false);
		phoneNos_l.addComponent(inputPhoneNo_tf);

		// 按钮组件
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		phoneNos_l.addComponent(buttonsLayout);

		edit_button = new Button("编辑", this);
		edit_button.setStyleName("default");
		buttonsLayout.addComponent(edit_button);
		
		add_button = new Button("添加", this);
		buttonsLayout.addComponent(add_button);

		delete_button = new Button("删除", this);
		buttonsLayout.addComponent(delete_button);
		
		save_button = new Button("保存", this);
		save_button.setStyleName("default");
		buttonsLayout.addComponent(save_button);
		
		cancel_button = new Button("取消", this);
		cancel_button.setStyleName("default");
		buttonsLayout.addComponent(cancel_button);

		// 设置按钮的可视属性
		setProperty2Buttons(false);
		
		return phoneNos_l;
	}
	
	/**
	 * 设置按钮的可视化属性
	 * @param visible
	 */
	private void setProperty2Buttons(boolean visible) {
		inputPhoneNo_tf.setVisible(visible);
		edit_button.setVisible(!visible);
		add_button.setVisible(visible);
		delete_button.setVisible(visible);
		save_button.setVisible(visible);
		cancel_button.setVisible(visible);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		try {
			Button source = event.getButton();
			if(source == edit_button) {
				setProperty2Buttons(true);
			} else if(source == add_button) {
				executeAdd();
			} else if(source == save_button) {
				executeSave();
			} else if(source == delete_button) {
				executeDelete();
			} else if(source == cancel_button) {
				executeCancel();
			}
		} catch (Exception e) {
			e.printStackTrace();
			edit_button.getApplication().getMainWindow().showNotification("处理失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("jrh 自动外呼 天机/移除手机成员出现异常 -->"+e.getMessage(), e);
		}
	}

	/**
	 * 执行添加操作
	 */
	private void executeAdd() {
		//检查输入是否合法
		String phoneNumber = StringUtils.trimToEmpty((String)inputPhoneNo_tf.getValue());
		if("".equals(phoneNumber) || !inputPhoneNo_tf.isValid()) {
			warning_notification.setCaption("号码不能为空，并且只能是数字，长度为7-12位");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		phoneNosContainer.addBean(phoneNumber);
		OperationLogUtil.simpleLog(loginUser, "自动外呼-添加移除手机成员："+autoDialoutTask.getAutoDialoutTaskName()+" Id:"+autoDialoutTask.getId()+" 添加 "+phoneNumber);
		inputPhoneNo_tf.setValue("");
	}

	/**
	 * 执行保存操作
	 */
	private boolean executeSave() {
		// 第一步：保存最新信息
		Set<String> currentPhoneNos = new HashSet<String>(phoneNosContainer.getItemIds());
		autoDialoutTask.setPhoneNos(currentPhoneNos);
		try {
			autoDialoutTask = autoDialoutTaskService.update(autoDialoutTask);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改自动外呼的手机成员时出现异常--->"+e.getMessage(), e);
			return false;
		}

		// ----------- 处理队列成员 --------- // 
		Queue queue = autoDialoutTask.getQueue();
		String queueName = queue.getName();
		Domain domain = autoDialoutTask.getDomain();
		String defaultOutline = ShareData.domainToDefaultOutline.get(domain.getId());
		
		// 第二步：往自动外呼任务使用的队列中添加新的手机成员
		for(String phoneNo : currentPhoneNos) {
			if(!oldPhoneNosSet.contains(phoneNo)) {
				queueMemberRelationService.addQueueMemberRelation(queueName, phoneNo+"@"+defaultOutline, 5);
			}
		}
		
		// 第三步：往自动外呼任务使用的队列中移除旧的手机成员
		for(String phoneNo : oldPhoneNosSet) {
			if(!currentPhoneNos.contains(phoneNo)) {
				queueMemberRelationService.removeQueueMemberRelation(queueName, phoneNo+"@"+defaultOutline);
			}
		}
		
		// 第四步：更新用于保存自动外呼任务的原始手机成员的集合
		oldPhoneNosSet.clear();
		oldPhoneNosSet.addAll(autoDialoutTask.getPhoneNos());
		
		// 第五步：修改组件状态
		setProperty2Buttons(false);

		// 第六步：显示成功提示信息
		success_notification.setCaption("修改自动外呼任务手机成员成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
		
		return true;
	}

	/**
	 * 执行删除手机成员操作
	 */
	@SuppressWarnings("unchecked")
	private void executeDelete() {
		Set<String> phoneNos = (Set<String>) phoneNos_ls.getValue();
		if(phoneNos.size() == 0) {
			warning_notification.setCaption("请先选中一个手机成员后再删");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		for(String phoneNo : phoneNos) {
			OperationLogUtil.simpleLog(loginUser, "自动外呼-添加移除手机成员："+autoDialoutTask.getAutoDialoutTaskName()+" Id:"+autoDialoutTask.getId()+" 移除"+phoneNo);
			phoneNosContainer.removeItem(phoneNo);
		}
	}

	/**
	 * 执行取消操作
	 */
	private void executeCancel() {
		phoneNosContainer.removeAllItems();
		if(autoDialoutTask.getPhoneNos()!=null){
			phoneNosContainer.addAll(autoDialoutTask.getPhoneNos());
		}
		setProperty2Buttons(false);
	}
	
}
