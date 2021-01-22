package com.jiangyifen.ec2.ui.mgr.tabsheet.mgrhistoryservicerecord;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.QcMgr;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 客服记录质检窗口
 * 
 * @author jrh
 * 2013-7-18
 */
@SuppressWarnings("serial")
public class MgrQcWindow extends Window implements ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 错误警告提示信息
	private Notification warning_notification;
	
	private OptionGroup mgrQc_og;
	private TextArea reason_ta;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

	private User loginUser;												// 当前登陆用户
	private CustomerServiceRecord customerServiceRecord;				// 当前质检的客服记录对象
	
	private VerticalLayout sourceTableView; 							// 调用该质检窗口的垂直布局管理器界面

	private CustomerServiceRecordService customerServiceRecordService;	// 客服记录服务类

	/**
	 * 构造器
	 * @param sourceTableView  调用该质检窗口的垂直布局管理器界面
	 * 如：MgrHistoryServiceRecordTabView、MgrDetailSingleCustomerHistoryRecordView
	 */
	public MgrQcWindow(VerticalLayout sourceTableView) {
		this.center();
		this.setModal(true);
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("管理员质检");
		this.sourceTableView = sourceTableView;
		
		loginUser = SpringContextHolder.getLoginUser();
		
		customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");

		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);

		// 添加动态生成box
		windowContent.addComponent(buildQcResultLayout());
		
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		
		//底下按钮输出
		windowContent.addComponent(buildButtonsLayout());

	}
	
	/**
	 * 质检结果输出
	 * @return
	 */
	private FormLayout buildQcResultLayout() {
		mgrQc_og = new OptionGroup("质检结果", Arrays.asList(QcMgr.values()));
		mgrQc_og.setRequired(true);
		mgrQc_og.setStyleName("twocolchb");
		mgrQc_og.setWidth("300px");
		mgrQc_og.setRequiredError("请选择质检结果");
		FormLayout layout = new FormLayout();
		layout.addComponent(mgrQc_og);
		
		return layout;
	}
	
	/**
	 * 描述信息的输出
	 * @return
	 */
	private HorizontalLayout buildNoteLayout() {
		HorizontalLayout reasonLayout = new HorizontalLayout();
		reasonLayout.addComponent(new Label("质检原因:"));
		reason_ta = new TextArea();
		reason_ta.setWidth("300px");
		reason_ta.setWordwrap(true);
		reason_ta.setNullRepresentation("");
		reason_ta.setInputPrompt("请输入合格或不合格的原因！");
		reasonLayout.addComponent(reason_ta);
		return reasonLayout;
	}

	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存", this);
		save.addStyleName("default");
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);
		
		return buttonsLayout;
	}
	
	/**
	 * 点击按钮
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			executeSave();
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private Boolean executeSave() {
		QcMgr qcMgr = (QcMgr)mgrQc_og.getValue();
		if(qcMgr == null) {
			warning_notification.setCaption("质检结果不能为空，请重试! ");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return false;
		}
		try {
			customerServiceRecord.setQcMgr(qcMgr);
			customerServiceRecord.setQcReason(StringUtils.trimToEmpty((String)reason_ta.getValue()));
			customerServiceRecord.setMgrQcCreator(loginUser);
			customerServiceRecord = customerServiceRecordService.update(customerServiceRecord);
		} catch (Exception e) {
			e.printStackTrace();
			warning_notification.setCaption("保存质检信息失败，请重试! ");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			logger.error("jrh 管理员历史客服记录保存质检原因出现异常--》"+e.getMessage(), e);
			return false;
		}
		try {
			Method method = sourceTableView.getClass().getMethod("refreshTable", boolean.class);
			method.invoke(sourceTableView, false);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 管理员历史客服记录保存质检原因后刷新表格出现异常--》"+e.getMessage(), e);
		} 
		this.getApplication().getMainWindow().showNotification("管理员保存质检成功！");
		this.getParent().removeWindow(this);
		return true;
	}
	
	/**
	 * 更新质检窗口里面的信息
	 * @param customerServiceRecord
	 */
	public void updateInfo(CustomerServiceRecord customerServiceRecord) {
		this.customerServiceRecord = customerServiceRecord;
		mgrQc_og.setValue(customerServiceRecord.getQcMgr());
		reason_ta.setValue(customerServiceRecord.getQcReason());
	}

}
