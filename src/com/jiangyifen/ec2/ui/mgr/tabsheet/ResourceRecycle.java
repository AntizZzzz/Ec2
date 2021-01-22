package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.mgr.resourcerecycle.ResourceToBatch;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.VerticalLayout;
/**
 *自动外呼的资源回收页面
 * @author chb
 */
@SuppressWarnings("serial")
public class ResourceRecycle extends VerticalLayout{
	public static String NOTANSWERED="未呼通";
	public static String NOTPICKUP="坐席未接";
/**
 * 其他组件
 */
	private AutoDialoutTaskService autoDialoutTaskService;
	private MarketingProjectService marketingProjectService;
	private CommonService commonService;
	private Domain domain;

	public ResourceRecycle() {
		ResourceRecycle.this.initService();
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		ResourceRecycle.this.addComponent(windowContent);
		
		windowContent.addComponent(new Label("<b>坐席未接起:</b>表示电话号码有效,已经呼通客户,但是坐席没有及时接起的电话",Label.CONTENT_XHTML));
		windowContent.addComponent(new Label("<b>未呼通:</b>表示没有呼通客户,可能是无效号码,也可能是客户关机,客户正忙等",Label.CONTENT_XHTML));
		
		HorizontalLayout constraintLayout=new HorizontalLayout();
		constraintLayout.setSpacing(true);
		constraintLayout.addComponent(buildFirstPanel());
		constraintLayout.addComponent(buildSecondPanel());
		constraintLayout.addComponent(buildThirdPanel());
		windowContent.addComponent(constraintLayout);

	}
	
	/**
	 * 初始化所有的Service
	 */
	private void initService() {
		autoDialoutTaskService=SpringContextHolder.getBean("autoDialoutTaskService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		commonService=SpringContextHolder.getBean("commonService");
		domain=SpringContextHolder.getDomain();
	}
	
	
	/**
	 * 创建第一块面板，按自动外呼回收
	 * @return
	 */
	private Panel buildFirstPanel() {
		final Panel panel=new Panel("按自动外呼回收");
		//为所有的自动外呼添加数据源
		final ComboBox comboBox=new ComboBox("自动外呼任务");
		comboBox.setNullSelectionAllowed(true);
		BeanItemContainer<AutoDialoutTask> container=new BeanItemContainer<AutoDialoutTask>(AutoDialoutTask.class);
		List<AutoDialoutTask> allAutodialTaskList = autoDialoutTaskService.getAllByDialoutType(domain, "自动外呼");
		container.addAll(allAutodialTaskList);
		comboBox.setContainerDataSource(container);
		comboBox.setItemCaptionPropertyId("autoDialoutTaskName");
		panel.addComponent(comboBox);

		//////////////////时间组件////////////////////////
		final PopupDateField timeFrom = new PopupDateField("从时间");
		timeFrom.setResolution(PopupDateField.RESOLUTION_MIN);
		timeFrom.setDateFormat("yyyy-MM-dd hh:mm:ss");
		panel.addComponent(timeFrom);
		
		//到?时间 结束
		final PopupDateField timeTo = new PopupDateField("到时间");
		timeTo.setResolution(PopupDateField.RESOLUTION_MIN);
		timeTo.setDateFormat("yyyy-MM-dd hh:mm:ss");
		panel.addComponent(timeTo);
		//////////////////////////////////////////
		
		//选择坐席未接还是未呼通
		final OptionGroup optionGroup=new OptionGroup("呼叫结果", Arrays.asList(NOTANSWERED,NOTPICKUP));
		optionGroup.setStyleName("twocolchb1");
		panel.addComponent(optionGroup);
		
		//数量信息
		final Label countLabel=new Label("数量:");
		panel.addComponent(countLabel);
		
		//操作按钮
		HorizontalLayout buttonConstraintLayout=new HorizontalLayout();
		buttonConstraintLayout.setSpacing(true);
		panel.addComponent(buttonConstraintLayout);

		Button view=new Button("查看数量");
		view.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				AutoDialoutTask autoDialoutTask=(AutoDialoutTask)comboBox.getValue();
				String callType=(String)optionGroup.getValue();
				String sql="";
				if(autoDialoutTask==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择自动外呼任务");
					return;
				}else if(callType==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择呼叫结果");
					return;
				}else{ //根据用户选中的自动外呼和选中的呼叫结果来查询可回收资源的数量
					String sqlPart=parseDateToSqlPart(timeFrom.getValue(),timeTo.getValue());
					if(callType.equals(NOTANSWERED)){
						sql="select count(*) from ec2_marketing_project_task where autodialid="+autoDialoutTask.getId()+" and autodialisanswered=false"+sqlPart;
					}else if(callType.equals(NOTPICKUP)){
						sql="select count(*) from ec2_marketing_project_task where autodialid="+autoDialoutTask.getId()+" and autodialisanswered=true and autodialiscsrpickup=false"+sqlPart;
					}
				}
				Long count = (Long)commonService.excuteNativeSql(sql, ExecuteType.SINGLE_RESULT);
				countLabel.setValue("数量:"+count);
			}

		});
		buttonConstraintLayout.addComponent(view);
		
		Button recycle=new Button("回收数据");
		recycle.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				String sqlPart=parseDateToSqlPart(timeFrom.getValue(),timeTo.getValue());
				AutoDialoutTask autoDialoutTask=(AutoDialoutTask)comboBox.getValue();
				String callType=(String)optionGroup.getValue();
				if(autoDialoutTask==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择自动外呼任务");
					return;
				}else if(callType==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择呼叫结果");
					return;
				}
				showResourceToBatch(autoDialoutTask,callType,timeFrom.getValue(),timeTo.getValue(),sqlPart);
			}

			private void showResourceToBatch(AutoDialoutTask autoDialoutTask,
					String callType,Object dateFrom, Object dateTo,String sqlPart) {
				ResourceToBatch resourceToBatch=new ResourceToBatch(autoDialoutTask,callType,dateFrom,dateTo,sqlPart);
				panel.getApplication().getMainWindow().addWindow(resourceToBatch);
			}
		});
		buttonConstraintLayout.addComponent(recycle);
		return panel;
	}
	/**
	 * 创建第二块面板，按项目回收
	 * @return
	 */
	private Panel buildSecondPanel() {
		final Panel panel=new Panel("按项目回收自动外呼资源");
		//为所有的自动外呼添加数据源
		final ComboBox comboBox=new ComboBox("项目选择");
		comboBox.setNullSelectionAllowed(true);
		BeanItemContainer<MarketingProject> container=new BeanItemContainer<MarketingProject>(MarketingProject.class);
		List<MarketingProject> projectList = marketingProjectService.getAll(domain);
		container.addAll(projectList);
		comboBox.setContainerDataSource(container);
		comboBox.setItemCaptionPropertyId("projectName");
		panel.addComponent(comboBox);

		//////////////////时间组件////////////////////////
		final PopupDateField timeFrom = new PopupDateField("从时间");
		timeFrom.setResolution(PopupDateField.RESOLUTION_MIN);
		timeFrom.setDateFormat("yyyy-MM-dd hh:mm:ss");
		panel.addComponent(timeFrom);
		
		//到?时间 结束
		final PopupDateField timeTo = new PopupDateField("到时间");
		timeTo.setResolution(PopupDateField.RESOLUTION_MIN);
		timeTo.setDateFormat("yyyy-MM-dd hh:mm:ss");
		panel.addComponent(timeTo);
		//////////////////////////////////////////

		//选择坐席未接还是未呼通
		final OptionGroup optionGroup=new OptionGroup("呼叫结果", Arrays.asList(NOTANSWERED,NOTPICKUP));
		optionGroup.setStyleName("twocolchb1");
		panel.addComponent(optionGroup);
		
		//数量信息
		final Label countLabel=new Label("数量:");
		panel.addComponent(countLabel);
		
		//操作按钮
		HorizontalLayout buttonConstraintLayout=new HorizontalLayout();
		buttonConstraintLayout.setSpacing(true);
		panel.addComponent(buttonConstraintLayout);

		Button view=new Button("查看数量");
		view.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				MarketingProject project=(MarketingProject)comboBox.getValue();
				String callType=(String)optionGroup.getValue();
				String sql="";
				if(project==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择项目");
					return;
				}else if(callType==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择呼叫结果");
					return;
				}else{ //根据用户选中的自动外呼和选中的呼叫结果来查询可回收资源的数量
					String sqlPart=parseDateToSqlPart(timeFrom.getValue(),timeTo.getValue());
					if(callType.equals(NOTANSWERED)){
						sql="select count(*) from ec2_marketing_project_task where autodialisanswered=false and marketingproject_id="+project.getId()+" and autodialid is not null"+sqlPart;
					}else if(callType.equals(NOTPICKUP)){
						sql="select count(*) from ec2_marketing_project_task where autodialisanswered=false and marketingproject_id="+project.getId()+" and autodialisanswered=true and autodialiscsrpickup=false and autodialid is not null"+sqlPart;
					}
				}
				Long count = (Long)commonService.excuteNativeSql(sql, ExecuteType.SINGLE_RESULT);
				countLabel.setValue("数量:"+count);
			}
		});
		buttonConstraintLayout.addComponent(view);
		
		Button recycle=new Button("回收数据");
		recycle.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				MarketingProject project=(MarketingProject)comboBox.getValue();
				String callType=(String)optionGroup.getValue();
				if(project==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择项目");
					return;
				}else if(callType==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择呼叫结果");
					return;
				}
				String sqlPart=parseDateToSqlPart(timeFrom.getValue(),timeTo.getValue());
				showResourceToBatch(project,callType,timeFrom.getValue(),timeTo.getValue(),sqlPart);
			}

			private void showResourceToBatch(MarketingProject project,
					String callType, Object dateFrom, Object dateTo,String sqlPart) {
				ResourceToBatch resourceToBatch=new ResourceToBatch(project,callType,dateFrom,dateTo,sqlPart);
				panel.getApplication().getMainWindow().addWindow(resourceToBatch);
				
			}
		});
		buttonConstraintLayout.addComponent(recycle);
		return panel;
	}
	/**
	 * 创建第三块面板，回收全部
	 * @return
	 */
	private Panel buildThirdPanel() {
		final Panel panel=new Panel("回收全部自动外呼资源");

		//////////////////时间组件////////////////////////
		final PopupDateField timeFrom = new PopupDateField("从时间");
		timeFrom.setResolution(PopupDateField.RESOLUTION_MIN);
		timeFrom.setDateFormat("yyyy-MM-dd hh:mm:ss");
		panel.addComponent(timeFrom);
		
		//到?时间 结束
		final PopupDateField timeTo = new PopupDateField("到时间");
		timeTo.setResolution(PopupDateField.RESOLUTION_MIN);
		timeTo.setDateFormat("yyyy-MM-dd hh:mm:ss");
		panel.addComponent(timeTo);
		//////////////////////////////////////////
		
		//选择坐席未接还是未呼通
		final OptionGroup optionGroup=new OptionGroup("呼叫结果", Arrays.asList(NOTANSWERED,NOTPICKUP));
		optionGroup.setStyleName("twocolchb1");
		panel.addComponent(optionGroup);
		
		//数量信息
		final Label countLabel=new Label("数量:");
		panel.addComponent(countLabel);
		
		//操作按钮
		HorizontalLayout buttonConstraintLayout=new HorizontalLayout();
		buttonConstraintLayout.setSpacing(true);
		panel.addComponent(buttonConstraintLayout);

		Button view=new Button("查看数量");
		view.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				String callType=(String)optionGroup.getValue();
				String sql="";
				if(callType==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择呼叫结果");
					return;
				}else{ //根据用户选中的自动外呼和选中的呼叫结果来查询可回收资源的数量
					String sqlPart=parseDateToSqlPart(timeFrom.getValue(),timeTo.getValue());
					if(callType.equals(NOTANSWERED)){
						sql="select count(*) from ec2_marketing_project_task where autodialid is not null and autodialisanswered=false"+sqlPart;
					}else if(callType.equals(NOTPICKUP)){
						sql="select count(*) from ec2_marketing_project_task where autodialid is not null and autodialisanswered=true and autodialiscsrpickup=false"+sqlPart;
					}
				}
				Long count = (Long)commonService.excuteNativeSql(sql, ExecuteType.SINGLE_RESULT);
				countLabel.setValue("数量:"+count);
			}
		});
		buttonConstraintLayout.addComponent(view);
		
		Button recycle=new Button("回收数据");
		recycle.addListener(new Button.ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				String sqlPart=parseDateToSqlPart(timeFrom.getValue(),timeTo.getValue());
				String callType=(String)optionGroup.getValue();
				if(callType==null){
					NotificationUtil.showWarningNotification(ResourceRecycle.this,"请先选择呼叫结果");
					return;
				}
				showResourceToBatch(callType,timeFrom.getValue(),timeTo.getValue(),sqlPart);
			}

			private void showResourceToBatch(String callType, Object dateFrom, Object dateTo,String sqlPart) {
				ResourceToBatch resourceToBatch=new ResourceToBatch(callType,dateFrom,dateTo,sqlPart);
				panel.getApplication().getMainWindow().addWindow(resourceToBatch);
				
			}
		});
		buttonConstraintLayout.addComponent(recycle);
		return panel;
	}
	
	/**
	 * 将起始日期解析为字符串
	 * @param value
	 * @param value2
	 */
	private String parseDateToSqlPart(Object fromDate1, Object toDate1) {
		Date fromDate = (Date)fromDate1;
		Date toDate = (Date)toDate1;
		String sqlPart="";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		if(fromDate==null&&toDate==null){
			sqlPart=" ";
		}else if(fromDate==null&&toDate!=null){
			sqlPart=" and autodialtime<'"+sdf.format(toDate)+"'";
		}else if(fromDate!=null&&toDate==null){
			sqlPart=" and autodialtime>'"+sdf.format(fromDate)+"'";
		}else{
			sqlPart=" and autodialtime>'"+sdf.format(fromDate)+"' and autodialtime<'"+sdf.format(toDate)+"'";
		}
		return sqlPart;
	}
	
}
