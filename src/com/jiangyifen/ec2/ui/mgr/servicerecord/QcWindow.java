package com.jiangyifen.ec2.ui.mgr.servicerecord;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.QcMgr;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrServiceRecord;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 创建导入新资源（Upload）
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class QcWindow extends Window implements Button.ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 错误警告提示信息
	private Notification warning_notification;
	
	/**
	 * 主要组件区域
	 */
	private OptionGroup optionGroup;
	
	// 导入新批次组件
	private TextArea note;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

	private User loginUser;								// 当前登陆用户
	private CustomerServiceRecord customerServiceRecord;
	
	private MgrServiceRecord mgrServiceRecordView;		// 调用该质检窗口的垂直布局管理器界面

	private CustomerServiceRecordService customerServiceRecordService;	// 客服记录服务类
	
	/**
	 * 构造器
	 * @param resourceImport
	 *            资源导入视图的引用
	 */
	public QcWindow(MgrServiceRecord serviceRecord) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("质检窗口");
		this.mgrServiceRecordView = serviceRecord;
		
		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);

		//TODO 添加动态生成box
		windowContent.addComponent(buildQcResultLayout());
		
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		
		//底下按钮输出
		windowContent.addComponent(buildButtonsLayout());

	}
	
	/**
	 * 初始化此类中用到的Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();

		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
	}


	/**
	 * 质检结果输出
	 * 
	 * @return
	 */
	private FormLayout buildQcResultLayout() {
		FormLayout layout = new FormLayout();
//		layout.addComponent(new Label("质检结果:"));
		
		optionGroup=new OptionGroup("质检结果", Arrays.asList(QcMgr.values()));
		optionGroup.setRequired(true);
		optionGroup.setStyleName("twocolchb");
		optionGroup.setRequiredError("请选择质检结果");
		layout.addComponent(optionGroup);
		
		return layout;
	}
	
	/**
	 * 描述信息的输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildNoteLayout() {
		HorizontalLayout noteLayout = new HorizontalLayout();
		noteLayout.addComponent(new Label("质检备注:"));
		note = new TextArea();
		note.setColumns(15);
		note.setRows(3);
		note.setWordwrap(true);
		note.setNullRepresentation("");
		note.setInputPrompt("请输入备注信息！");
		noteLayout.addComponent(note);
		return noteLayout;
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
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);
		
		return buttonsLayout;
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private Boolean executeSave() {
		QcMgr qcMgr = (QcMgr)optionGroup.getValue();
		if(qcMgr == null) {
			warning_notification.setCaption("质检结果不能为空，请重试! ");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return false;
		}
		try {
			customerServiceRecord.setQcMgr(qcMgr);
			customerServiceRecord.setQcReason(StringUtils.trimToEmpty((String)note.getValue()));
			customerServiceRecord.setMgrQcCreator(loginUser);
			customerServiceRecord = customerServiceRecordService.update(customerServiceRecord);
			mgrServiceRecordView.updateTable(false);
		} catch (Exception e) {
			e.printStackTrace();
			warning_notification.setCaption("保存质检信息失败，请重试! ");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			logger.error("jrh 管理员历史客服记录保存质检原因出现异常--》"+e.getMessage(), e);
			return false;
		}
		this.getApplication().getMainWindow().showNotification("管理员保存质检成功！");
		this.getParent().removeWindow(this);
		return true;
	}
	
	/**
	 * 更新质检窗口里面的信息
	 * @param obj
	 */
	public void updateInfo(CustomerServiceRecord obj) {
		this.customerServiceRecord=obj;
		optionGroup.setValue(obj.getQcMgr());
		note.setValue(obj.getQcReason());
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

	
}
