package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.autodialout.ProjectResourceConsumer;
import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.ui.utils.ConfirmedWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *添加和编辑窗口，可以添加项目和编辑项目 
 * @author chb
 */
@SuppressWarnings("serial")
public class EditProject extends Window implements Button.ClickListener, ValueChangeListener {
/**
 * 主要组件输出	
 */
	//Form输出
	private Form form; //注：此Form仅仅起到布局管理器的作用
	private TextField projectName;
	// jrh
	private ComboBox projectType_cb;
	private OptionGroup commissionerRouting;
	private TextArea noteArea;
	private ComboBox selectOutLine;
	private ComboBox selectQueue;
	// jrh 选择问卷
	private ComboBox csrMaxUnfinishedTask_cb;
	private ComboBox csrMaxPickTask_cb;
	// jrh 选择问卷
	private TwinColSelect questionnaires_tcs;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;
	
	private ConfirmedWindow confirmWindow;	// jrh

/**
 * 其他组件
 */
	private MarketingProjectService marketingProjectService;
	private SipConfigService sipService;
	private QueueService queueService;
	private QuestionnaireService questionnaireService;
	//持有项目引用或自己新建的项目
	private MarketingProject project;
	//持有项目控制对象的引用
	private ProjectControl projectControl;
	//域
	private Domain domain;
	//持有Sip的引用
	private List<Long> allUseableOutlineIds; 	// jrh 可用的外线Id号
	private List<SipConfig> allOutlines;
	private List<SipConfig> allUseableOutlines;
	private SipConfig originalSip;
	private List<Questionnaire> allQuestionnaires; // jrh 用于回显问卷
	
	private User loginUser=SpringContextHolder.getLoginUser();
	
	/**
	 * 所有可用的队列（非自动外呼队列、没有被其他项目使用的队列）
	 * jrh
	 */
	private List<Queue> allUseableSimpleQueues;
	
	/**
	 * 如果项目为null，则自己新建项目，否则为编辑项目
	 * <p>是编辑项目还是添加项目由设置的项目决定</p>
	 * @param project
	 */
	public EditProject(ProjectControl projectControl) {
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
		
		//From输出
		form=new Form();
		
		//设置Form 生成的Field
		form.setFormFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field createField(Item item, Object propertyId,
					Component uiContext) {
				if(propertyId.equals("projectName")){
					projectName = new TextField("项目名称");
					projectName.setInputPrompt("默认为日期");
					projectName.setWidth("150px");
					projectName.setNullRepresentation("");
					return projectName; 
				}else if("commissionerRouting".equals(propertyId)) {
					commissionerRouting=new OptionGroup("专员路由", Arrays.asList(MarketingProject.COMMISSIONER_ROUTING_ON,MarketingProject.COMMISSIONER_ROUTING_OFF));
					commissionerRouting.setImmediate(true);
					commissionerRouting.setStyleName("twocolchb");
					commissionerRouting.setRequired(true);
					commissionerRouting.setRequiredError("请选择是否启用专员路由");
					return commissionerRouting;
				}else if(propertyId.equals("note")){
					noteArea = new TextArea("项目备注");
					noteArea.setColumns(15);
					noteArea.setRows(3);
					noteArea.setWordwrap(true);
					noteArea.setInputPrompt("请输入与项目相关的备注信息！");
					noteArea.setNullRepresentation("");
					return noteArea;
				}else if(propertyId.equals("sip")){
					selectOutLine = new ComboBox("外线选择");
					BeanItemContainer<SipConfig> sipContainer=new BeanItemContainer<SipConfig>(SipConfig.class);
					sipContainer.removeAllItems();
					sipContainer.addAll(allOutlines);
					selectOutLine.setContainerDataSource(sipContainer);
					selectOutLine.setInputPrompt("外线选择");
					selectOutLine.setImmediate(true);
					selectOutLine.setWidth("150px");
					
					/**************************jrh 修改下拉框显示的 下拉项的名称 *******************************************/
					for(SipConfig outline : allOutlines) {
						if(allUseableOutlineIds.contains(outline.getId())) {
							selectOutLine.setItemCaption(outline, outline.getName());
						} else {
							selectOutLine.setItemCaption(outline, outline.getName()+"-已占用");
						}
					}
					/*********************************************************************/
					
					return selectOutLine;
				}else if(propertyId.equals("queue")){
					selectQueue= new ComboBox("队列选择");
					BeanItemContainer<Queue> queueContainer=new BeanItemContainer<Queue>(Queue.class);
					queueContainer.addAll(allUseableSimpleQueues);
					selectQueue.setContainerDataSource(queueContainer);
					selectQueue.setItemCaptionPropertyId("descriptionAndName");
					selectQueue.setInputPrompt("队列选择");
					selectQueue.setImmediate(true);
					selectQueue.setWidth("150px");
					return selectQueue;
				} else if("csrMaxUnfinishedTaskCount".equals(propertyId)) {	// jrh 坐席最大未完成任务数
					csrMaxUnfinishedTask_cb = new ComboBox("坐席最大未完成任务数");
					csrMaxUnfinishedTask_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
					csrMaxUnfinishedTask_cb.setValue(MarketingProjectType.MARKETING);
					csrMaxUnfinishedTask_cb.setNullSelectionAllowed(false);
					csrMaxUnfinishedTask_cb.setWidth("150px");
					for(int i = 1; i < 501; i++) {
						csrMaxUnfinishedTask_cb.addItem(i);
					}
					csrMaxUnfinishedTask_cb.setValue(10);
					return csrMaxUnfinishedTask_cb;
				} else if("csrOnceMaxPickTaskCount".equals(propertyId)) {	// jrh 坐席单次获取任务数
					csrMaxPickTask_cb = new ComboBox("坐席单次获取任务数");
					csrMaxPickTask_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
					csrMaxPickTask_cb.setValue(MarketingProjectType.MARKETING);
					csrMaxPickTask_cb.setNullSelectionAllowed(false);
					csrMaxPickTask_cb.setWidth("150px");
					for(int i = 0; i < 101; i++) {
						csrMaxPickTask_cb.addItem(i);
					}
					csrMaxPickTask_cb.setValue(10);
					return csrMaxPickTask_cb;
				} else if("marketingProjectType".equals(propertyId)) {	// jrh 项目类型选择
					BeanItemContainer<MarketingProjectType> types = new BeanItemContainer<MarketingProjectType>(MarketingProjectType.class);
					types.addBean(MarketingProjectType.MARKETING);
					types.addBean(MarketingProjectType.QUESTIONNAIRE);
//					types.addBean(MarketingProjectType.CALL_BACK);	// TODO 以后加上 回访
					
					projectType_cb = new ComboBox("项目类型");
					projectType_cb.setContainerDataSource(types);
					projectType_cb.setItemCaptionPropertyId("name");
					projectType_cb.setValue(MarketingProjectType.MARKETING);
					projectType_cb.setNullSelectionAllowed(false);
					projectType_cb.addListener(EditProject.this);
					projectType_cb.setImmediate(true);
					projectType_cb.setWidth("150px");
					return projectType_cb;
				}else if(propertyId.equals("questionnaires")) {	// jrh 问卷选择
					questionnaires_tcs = new TwinColSelect("问卷选择");
					questionnaires_tcs.setItemCaptionPropertyId("mainTitle");
					questionnaires_tcs.setLeftColumnCaption("可选问卷");
					questionnaires_tcs.setRightColumnCaption("已选问卷");
					questionnaires_tcs.setMultiSelect(true); 
					questionnaires_tcs.setImmediate(true); 
					questionnaires_tcs.setRows(5);
					questionnaires_tcs.setWidth("350px");

					BeanItemContainer<Questionnaire> questionnaireContainer = new BeanItemContainer<Questionnaire>(Questionnaire.class);
					allQuestionnaires = questionnaireService.getAllByDomainId(domain.getId(), "E");
					questionnaireContainer.addAll(allQuestionnaires);
					questionnaires_tcs.setContainerDataSource(questionnaireContainer);
					
					// 设置组件的可视化属性
					MarketingProjectType projectType = (MarketingProjectType) projectType_cb.getValue();
					if(projectType.getIndex() == MarketingProjectType.QUESTIONNAIRE.getIndex()) {
						questionnaires_tcs.setVisible(true);
					} else {
						questionnaires_tcs.setVisible(false);
					}
					return questionnaires_tcs;
				}
				return null;
			}
		});
		windowContent.addComponent(form);
		
		//底下按钮输出
		form.setFooter(buildButtonsLayout());
	}
	
	/**
	 * 初始化所有的Service
	 */
	private void initService() {
		sipService=SpringContextHolder.getBean("sipConfigService");
		queueService=SpringContextHolder.getBean("queueService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		questionnaireService = SpringContextHolder.getBean("questionnaireService");
		domain=SpringContextHolder.getDomain();
	}
	
	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout fullWidthLayout=new HorizontalLayout();
		fullWidthLayout.setWidth("100%");
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存", this);
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);
		fullWidthLayout.addComponent(buttonsLayout);
		
		return fullWidthLayout;
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	public Boolean executeSave() {
		Integer csrMaxUnfinishedTaskCount = (Integer) csrMaxUnfinishedTask_cb.getValue();
		Integer csrOnceMaxPickTaskCount = (Integer) csrMaxPickTask_cb.getValue();
		String projectNameStr = StringUtils.trimToEmpty((String) projectName.getValue());
		String noteStr = StringUtils.trimToEmpty((String) noteArea.getValue());
		SipConfig selectSip = (SipConfig)selectOutLine.getValue();
		
		//**************************************************************************数据库操作************************************************************//
		
		/************************************jrh 外线处理****************************************************/
		//对以前选中的外线处理
		// 需更新当前选中外线的情况：1、如果原来选择了外线，现在没指定外线，则需要更新，2、如果原来指定了外线，现在也指定了，但是现在指定的外线跟原来的不一样了
		if((originalSip != null && selectSip == null) || (originalSip!=null && selectSip!= null && !selectSip.getId().equals(originalSip.getId())) ) {
			//先从以前的外线的外键中解除绑定关系
			originalSip.setMarketingProject(null);
			originalSip=sipService.update(originalSip);
		}

		//对当前选中的外线处理
		// 需更新当前选中外线的情况： 1、如果原来没有指定外线，现在指定了；2、如果原来指定了外线，现在也指定了，但是现在指定的外线跟原来的不一样了
		if((originalSip == null && selectSip != null) || (originalSip!=null && selectSip!= null && !selectSip.getId().equals(originalSip.getId()))) {
			// 将原有的项目对应的外线置为空,这里必须执行，不然会导致界面缓存残留
			MarketingProject otherProject = selectSip.getMarketingProject();
			if(otherProject != null) {
				otherProject.setSip(null);
				ShareData.projectToOutline.remove(otherProject.getId());
				ShareData.outlineToProject.remove(selectSip.getName());
				marketingProjectService.update(otherProject);
			}
			
			//在外线的外键中存储项目和外线的对应关系
			selectSip.setMarketingProject(project);
			selectSip=sipService.update(selectSip);
		}
		/**********************************************************************************************/

		//***************************队列处理*******************************//
		//对当前选中的外线处理
		Queue queue = (Queue)selectQueue.getValue();

		//***************************项目处理*******************************//
		//存储项目
		project.setProjectName(projectNameStr);
		project.setNote(noteStr);
		project.setSip(selectSip);
		project.setQueue(queue); //这里Queue可以为空
		project.setCsrMaxUnfinishedTaskCount(csrMaxUnfinishedTaskCount);
		project.setCsrOnceMaxPickTaskCount(csrOnceMaxPickTaskCount);
		project=marketingProjectService.update(project);
		OperationLogUtil.simpleLog(loginUser, "项目控制-编辑项目："+project.getProjectName());
		
		//***************************ShareData中projectToQueue、projectToOutline、outlineToProject处理*****************************************************//
		if(project.getMarketingProjectStatus().equals(MarketingProjectStatus.RUNNING)){
			//队列不为空，更改后添加对应关系，如果队列为空，则移除对应关系
			if(queue!=null){
				ShareData.projectToQueue.put(project.getId(), queue.getName());
			}else{
				ShareData.projectToQueue.remove(project.getId());
			}

			//先移除外线到项目的旧对应关系
			if(originalSip!=null){
				ShareData.outlineToProject.remove(originalSip.getName());
			}
			
			//外线不为空，更改后添加对应关系，如果外线为空，则移除对应关系
			if(selectSip!=null){
				ShareData.projectToOutline.put(project.getId(), selectSip.getName());
				ShareData.outlineToProject.put( selectSip.getName(),project.getId());
			}else{
				ShareData.projectToOutline.remove(project.getId());
			}

			//*****分机动态外线对应关系处理（如果项目没有运行，分机和项目不应该有对应关系）****//
			//更改分机和外线的对应关系
			for(String exten:ShareData.extenToProject.keySet()){
				Long marketingProjectId =ShareData.extenToProject.get(exten);
				if(project.getId() != null && project.getId().equals(marketingProjectId)){
					if( project.getSip()==null){
						ShareData.extenToDynamicOutline.remove(exten);
					}else{
						ShareData.extenToDynamicOutline.put(exten, project.getSip().getName());
					}
				}
			}
		}

		//****************************************************************自动外呼线程处理*********************************************************//
		//更改自动外呼和外线的对应关系
		//遍历所有线程
		for(String autoDialThreadName:AutoDialHolder.nameToThread.keySet()){
			Thread autoDialThread=AutoDialHolder.nameToThread.get(autoDialThreadName);
			//如果是消费线程
			if(autoDialThread instanceof ProjectResourceConsumer){
				ProjectResourceConsumer thread=(ProjectResourceConsumer)autoDialThread;
				//如果线程对应的项目是目前正在编辑的项目
				if(thread.getMarketingProject().getId().equals(project.getId())){
					thread.refreshEditChangeVariable();//更新线程内的所有变量
				}
			}
		}
		
		//刷新ProjectControl的项目Table在当前页
		projectControl.updateTable(false);

		this.getParent().removeWindow(this);	// 关闭窗口
		return true;	
	}

	/**
	 * jrh
	 * 	当修改的外线已经被其他项目占用时，需要用户进行确认后，方可保存
	 * @param selectSip
	 */
	private void showConfirmWindow(SipConfig selectSip) {
		MarketingProject otherProject = selectSip.getMarketingProject();
		String proName = (otherProject != null) ? otherProject.getProjectName() : "";
		String warningMsg = "<b><font color='red'>1、外线 <font color='blue'>["+selectSip.getName()+"]</font> "
				+ "已经有项目 <font color='blue'>["+proName+"]</font> 在使用，您确定要使用这条外线吗？？<br/>"
				+ "2、如确定保存，则原来所使用这条外线的项目将取消指定外线 <br/>&nbsp; &nbsp; &nbsp;(原项目呼出将使用默认外线)。</b></font>";
		if(confirmWindow == null) {
			confirmWindow = new ConfirmedWindow(warningMsg, "500px", this, "executeSave");
		} else {
			confirmWindow.setWarningMsg(warningMsg);
		}
		
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		allUseableOutlineIds = new ArrayList<Long>();
		allOutlines = sipService.getAllOutlinesByDomain(domain);
		allUseableOutlines = sipService.getAllUseableOutlinesByDomain(domain);
		for(SipConfig sip : allUseableOutlines) {
			allUseableOutlineIds.add(sip.getId());
		}
			
		allUseableSimpleQueues = queueService.getAllUseableSimpleQueueByDomain(domain);
		project=(MarketingProject)projectControl.getTable().getValue();
		originalSip = project.getSip();
		allUseableSimpleQueues.add(project.getQueue());
		
		//设置标题
		this.setCaption("编辑项目"+project.getProjectName());
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<MarketingProject>(project));
//		TODO
//		form.setVisibleItemProperties(new String[] { "projectName","commissionerRouting","note","sip","queue"});
		form.setVisibleItemProperties(new String[] { "projectName", "commissionerRouting", "note","sip","queue", "csrMaxUnfinishedTaskCount", "csrOnceMaxPickTaskCount", "marketingProjectType", "questionnaires"});
		
		//回显外线,只显示可用的外线
		if(originalSip != null){
			for(SipConfig sip : allOutlines) {
				if(sip.getId().equals(originalSip.getId())) {
					selectOutLine.setValue(sip);
				}
			}
		}

		//回显队列
		if(project.getQueue()!=null){
			for(Queue queue:allUseableSimpleQueues){
				if(queue.getId().equals(project.getQueue().getId())){
					selectQueue.setValue(queue);
					break;
				}
			}
		}
		
		//jrh 回显问卷
		if(project.getQuestionnaires() != null){
			Set<Questionnaire> selectedQuestionnaires = new HashSet<Questionnaire>();
			for(Questionnaire questionnaire : project.getQuestionnaires()){
				for(Questionnaire q : allQuestionnaires) {
					if(questionnaire.getId().equals(q.getId())){
						selectedQuestionnaires.add(q);
						continue;
					}
				}
			}
			questionnaires_tcs.setValue(selectedQuestionnaires);
		}

		// jrh
		csrMaxUnfinishedTask_cb.setValue(project.getCsrMaxUnfinishedTaskCount());
		csrMaxPickTask_cb.setValue(project.getCsrOnceMaxPickTaskCount());
		
		// 将项目类型置为只读状态
		projectType_cb.setReadOnly(true);
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			try {
				if(confirmValues()) {
					executeSave();
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "保存出现异常！");
			}
		} else if(source == cancel) {
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * jrh 确认修改的信息
	 * @return
	 */
	private boolean confirmValues() {
		Integer csrMaxUnfinishedTaskCount = (Integer) csrMaxUnfinishedTask_cb.getValue();
		Integer csrOnceMaxPickTaskCount = (Integer) csrMaxPickTask_cb.getValue();
		if(csrOnceMaxPickTaskCount > csrMaxUnfinishedTaskCount) {
			NotificationUtil.showWarningNotification(this, "[坐席单次获取任务数] 不能大于 [坐席最大未完成任务数]");
			return false;
		}
		
		String projectNameStr= StringUtils.trimToEmpty((String) projectName.getValue());
		if(projectNameStr.equals("")){
			NotificationUtil.showWarningNotification(this, "项目名称不能为空");
			return false;
		}
		
		SipConfig selectSip = (SipConfig)selectOutLine.getValue();
		// 需判断外线是否已被占用的情况： 1、如果原来没有指定外线，现在指定了；2、如果原来指定了外线，现在也指定了，但是现在指定的外线跟原来的不一样了
		if((originalSip == null && selectSip != null) || (originalSip!=null && selectSip!= null && !selectSip.getId().equals(originalSip.getId())) ) {
			if(!allUseableOutlineIds.contains(selectSip.getId())) {
				showConfirmWindow(selectSip);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * jrh 当项目类型发生变化时，修改问卷选择组件的可视化属性
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == projectType_cb) {
			MarketingProjectType projectType = (MarketingProjectType) projectType_cb.getValue();
			if(questionnaires_tcs != null) {
				if(projectType.getIndex() == MarketingProjectType.QUESTIONNAIRE.getIndex()) {
					questionnaires_tcs.setVisible(true);
				} else {
					questionnaires_tcs.setVisible(false);
				}
			}
		}
	}
}
