package com.jiangyifen.ec2.ui.csr.workarea.questionnairetask;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.CustomerQuestionnaireFinishStatus;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 已完成问卷任务搜索组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class FinishedQuestionnaireTaskFilter extends VerticalLayout implements ClickListener, Content {
	
	private Notification warning_notification;	// 错误警告提示信息
	
	private Label unfinishedTaskLabel;	// 说明该组件是用于搜索已完成问卷任务的
	private GridLayout gridLayout;		// 放所有的查询组件
	
	private TextField customerName_tf;		// 客户姓名输入文本框
	private TextField customerPhone_tf;	// 客户电话输入文本框
	private TextField customerId_tf;	// 客户编号输入文本框
	
	private ComboBox taskFinishedStatusSelector;	// 问卷任务呼叫完成状态选择框
	private ComboBox bridgedSelector;	// 是否接通选择框
	
	private Button searchButton;		// 刷新结果按钮
	private Button clearButton;			// 清空输入内容
	
	private User loginUser; 			// 当前的登陆用户
	private Long currentProjectId = (long) 0;		// 当前工作项目的Id
	
	private Table csrTaskTable;			// 存放问卷任务查询结果的表格
	private FlipOverTableComponent<MarketingProjectTask> finishedTableFlip;		// finishedTaskTable的翻页组件
	
	public FinishedQuestionnaireTaskFilter() {
		this.setSpacing(true);
		this.setWidth("-1px");
		loginUser = SpringContextHolder.getLoginUser();

		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);
		
		unfinishedTaskLabel = new Label("<B>搜索条件 </B>", Label.CONTENT_XHTML);
		unfinishedTaskLabel.setWidth("-1px");
		this.addComponent(unfinishedTaskLabel);
		
		gridLayout = new GridLayout(7, 2);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false, true, false, true);
		this.addComponent(gridLayout);
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createButtonsLayout();
	}

	private void createFilterHLayout1() {
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 0, 0);
		
		customerName_tf = new TextField();
		customerName_tf.setWidth("120px");
		gridLayout.addComponent(customerName_tf, 1, 0);

		// 电话号码号输入区
		Label customerPhoneLabel = new Label("电话号码：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 2, 0);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("120px");
		gridLayout.addComponent(customerPhone_tf, 3, 0);
		
		// 客户编号输入区
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		gridLayout.addComponent(customerIdLabel, 4, 0);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("134px");
		gridLayout.addComponent(customerId_tf, 5, 0);
	}

	private void createFilterHLayout2() {
		// 接通条件选择框
		Label bridgedLabel = new Label("接通情况：");
		bridgedLabel.setWidth("-1px");
		gridLayout.addComponent(bridgedLabel, 0, 1);
		
		bridgedSelector = new ComboBox();
		bridgedSelector.addItem("all");
		bridgedSelector.addItem("bridged");
		bridgedSelector.addItem("unbridged");
		bridgedSelector.setItemCaption("all", "全部");
		bridgedSelector.setItemCaption("bridged", "已接通");
		bridgedSelector.setItemCaption("unbridged", "未接通");
		bridgedSelector.setValue("all");
		bridgedSelector.setWidth("120px");
		bridgedSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(bridgedSelector, 1, 1);

		// 问卷任务呼叫完成状态选择框
		Label taskFinishedStatusLabel = new Label("完成情况：");
		taskFinishedStatusLabel.setWidth("-1px");
		gridLayout.addComponent(taskFinishedStatusLabel, 2, 1);
		
		taskFinishedStatusSelector = new ComboBox();
		taskFinishedStatusSelector.addItem("all");
		taskFinishedStatusSelector.addItem("un_started");
		taskFinishedStatusSelector.addItem("part_finished");
		taskFinishedStatusSelector.addItem("finished");
		taskFinishedStatusSelector.setItemCaption("all", "全部");
		taskFinishedStatusSelector.setItemCaption("un_started", "尚未开始");
		taskFinishedStatusSelector.setItemCaption("part_finished", "部分完成");
		taskFinishedStatusSelector.setItemCaption("finished", "已完成");
		taskFinishedStatusSelector.setWidth("120px");
		taskFinishedStatusSelector.setValue("all");
		taskFinishedStatusSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(taskFinishedStatusSelector, 3, 1);
	}

	private void createButtonsLayout() {
		gridLayout.setColumnExpandRatio(4, 1.0f);

		searchButton = new Button("查 询", this);
		searchButton.addStyleName("default");
		gridLayout.addComponent(searchButton,6, 0);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);

		clearButton = new Button("清 空", this);
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}

	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton && currentProjectId != 0) {
			if(!customerId_tf.isValid()) {
				warning_notification.setCaption("客户编号只能由数字组成！");
				customerId_tf.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			
			String countSql = createCountSql();
			String searchSql = countSql.replaceFirst("count\\(mpt\\)", "mpt") + " order by mpt.lastUpdateDate desc, mpt.id asc";
			
			finishedTableFlip.setSearchSql(searchSql);
			finishedTableFlip.setCountSql(countSql);
			finishedTableFlip.refreshToFirstPage();
			
			// 在查询到结果后，默认选择 Table 的第一条记录
			BeanItemContainer<MarketingProjectTask> taskBeanItemContainer = finishedTableFlip.getEntityContainer();
			if(taskBeanItemContainer.size() > 0) {
				Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
				csrTaskTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
			} else {
				csrTaskTable.setValue(null);
			}
		} else {
			customerName_tf.setValue("");
			customerPhone_tf.setValue("");
			customerId_tf.setValue("");
			taskFinishedStatusSelector.setValue("all");
			bridgedSelector.select("all");
		}
	}

	private String  createCountSql() {
		String customerNameSql = "";
		String inputName = ((String)customerName_tf.getValue()).trim();
		if(!"".equals(inputName) && inputName != null) {
			customerNameSql = " and mpt.customerResource.name like '%" + inputName + "%' ";
		}
		
		String customerPhoneSql = "";
		String phoneNumber = StringUtils.trimToEmpty((String) customerPhone_tf.getValue());
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = "and mpt.customerResource in " 
					+ "( select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%' )";
		}

		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and mpt.customerResource.id = " + customerId;
		}
		
		String isAnsweredSql = "";	// 是否接通
		if( "bridged".equals(bridgedSelector.getValue()) ) {
			isAnsweredSql = " and mpt.isAnswered = true ";
		} else if( "unbridged".equals(bridgedSelector.getValue()) ) {
			isAnsweredSql = " and mpt.isAnswered = false ";
		}
		
		String taskFinishedStatuSql = "";
		String finishedStatus = (String) taskFinishedStatusSelector.getValue();
		if(!"all".equals(finishedStatus)) {
			String typeSql = CustomerQuestionnaireFinishStatus.class.getName()+"."+StringUtils.upperCase(finishedStatus);
			taskFinishedStatuSql = " and mpt.customerQuestionnaireFinishStatus = " + typeSql;
		}
		
		// 按任务类型 为 “问卷任务”查询
		String typeSql = MarketingProjectTaskType.class.getName()+".QUESTIONNAIRE";
		String sql = " and mpt.user.id = "+loginUser.getId() + " and mpt.marketingProject.id = " + currentProjectId + " and mpt.isFinished = true " 
				+ customerNameSql + customerPhoneSql +customerIdSql + taskFinishedStatuSql + isAnsweredSql;
		return "select count(mpt) from MarketingProjectTask as mpt where mpt.isUseable = true and mpt.marketingProjectTaskType = " +typeSql +sql;
	}

	/**
	 * 将收索组件中的值置为默认值, 并重启设置当前项目的Id 号
	 */
	public void refresh(Long currentWorkingProjectId) {
		this.currentProjectId = currentWorkingProjectId;
		clearButton.click();
	}
	
	public void setFinishedTableFlip(FlipOverTableComponent<MarketingProjectTask> finishedTableFlip) {
		this.finishedTableFlip = finishedTableFlip;
		csrTaskTable = finishedTableFlip.getTable();
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return this;
	}

}
