package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
import com.vaadin.ui.Alignment;
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
public class AddProject extends Window implements Button.ClickListener, ValueChangeListener {
/**
 * 主要组件输出	
 */
	//Form输出
	private Form form;
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

	private User loginUser=SpringContextHolder.getLoginUser();
	
/**
 * 其他组件
 */
	//持有项目引用或自己新建的项目
	private MarketingProject project;
	//持有项目控制对象的引用
	private ProjectControl projectControl;
	private SipConfigService sipService;
	private QueueService queueService;
	private MarketingProjectService marketingProjectService;
	private QuestionnaireService questionnaireService;
	private Domain domain;
	private List<Long> allUseableOutlineIds; 	// jrh 可用的外线Id号
	private List<SipConfig> allOutlines;
	private List<SipConfig> allUseableOutlines;
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
	public AddProject(ProjectControl projectControl) {
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
					projectName.setInputPrompt("请输入名称");
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
					csrMaxUnfinishedTask_cb.setValue(100);
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
				} else if("marketingProjectType".equals(propertyId)) {	// jrh 项目类型
					BeanItemContainer<MarketingProjectType> types = new BeanItemContainer<MarketingProjectType>(MarketingProjectType.class);
					types.addBean(MarketingProjectType.MARKETING);
					types.addBean(MarketingProjectType.QUESTIONNAIRE);
//					types.addBean(MarketingProjectType.CALL_BACK);	// TODO 以后加上 回访
					
					projectType_cb = new ComboBox("项目类型");
					projectType_cb.setContainerDataSource(types);
					projectType_cb.setItemCaptionPropertyId("name");
					projectType_cb.setValue(MarketingProjectType.MARKETING);
					projectType_cb.setNullSelectionAllowed(false);
					projectType_cb.setImmediate(true);
					projectType_cb.addListener(AddProject.this);
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
					questionnaires_tcs.setVisible(false);
					questionnaires_tcs.setWidth("350px");
					BeanItemContainer<Questionnaire> questionnaireContainer = new BeanItemContainer<Questionnaire>(Questionnaire.class);
					questionnaireContainer.addAll(questionnaireService.getAllByDomainId(domain.getId(), "E"));
					questionnaires_tcs.setContainerDataSource(questionnaireContainer);
					return questionnaires_tcs;
				}
				return null;
			}
		});
		
		windowContent.addComponent(form);
		
		//底下按钮输出
		HorizontalLayout footerLayout=buildButtonsLayout();
		form.setFooter(footerLayout);
	}
	/**
	 * 初始化所有的Service
	 */
	private void initService() {
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		sipService=SpringContextHolder.getBean("sipConfigService");
		queueService=SpringContextHolder.getBean("queueService");
		questionnaireService = SpringContextHolder.getBean("questionnaireService");
		domain=SpringContextHolder.getDomain();
	}

	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout fullWidthLayout=new HorizontalLayout();
//		fullWidthLayout.setWidth("100%");
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存", this);
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);
		fullWidthLayout.addComponent(buttonsLayout);
		
		fullWidthLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);
		
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
		SipConfig selectSip = (SipConfig) selectOutLine.getValue();

		//存储项目
		project.setProjectName(projectNameStr);
		project.setNote(noteStr);
		project.setCreater(SpringContextHolder.getLoginUser());
		project.setSip(selectSip);
		project.setCsrMaxUnfinishedTaskCount(csrMaxUnfinishedTaskCount);
		project.setCsrOnceMaxPickTaskCount(csrOnceMaxPickTaskCount);
		//Queue可以为null
		Queue queue=(Queue)selectQueue.getValue();
		project.setQueue(queue);
		project.setCreateDate(new Date());
		project.setMarketingProjectStatus(MarketingProjectStatus.NEW);
		project.setDomain(SpringContextHolder.getDomain());
		project=marketingProjectService.update(project);

		if(selectSip!=null){
			// 将原有的项目对应的外线置为空,这里必须执行，不然会导致界面缓存残留
			MarketingProject otherProject = selectSip.getMarketingProject();
			if(otherProject != null) {
				otherProject.setSip(null);
				ShareData.projectToOutline.remove(otherProject.getId());
				ShareData.outlineToProject.remove(selectSip.getName());
				marketingProjectService.update(otherProject);
			}
						
			selectSip.setMarketingProject(project);
			selectSip=sipService.update(selectSip);
		}
		
		//刷新ProjectControl的项目Table在当前页
		projectControl.updateTable(true);
		
		this.getParent().removeWindow(this);
		return true;
	}

	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo(){
		//如果项目为null，则自己新建项目，否则为编辑项目
		if(project==null){
			this.setCaption("新建项目");
			project=new MarketingProject();
		}
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<MarketingProject>(project));
//		TODO
//		form.setVisibleItemProperties(new String[] { "projectName","commissionerRouting","note","sip","queue"});
		form.setVisibleItemProperties(new String[] { "projectName", "commissionerRouting","note","sip","queue", "csrMaxUnfinishedTaskCount", "csrOnceMaxPickTaskCount", "marketingProjectType", "questionnaires"});
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
		project=null;
		updateFormInfo();
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			try {
				if(confirmValues()) {
					executeSave();
					OperationLogUtil.simpleLog(loginUser, "项目控制-添加项目："+project.getProjectName());
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "新建项目出现错误！");
				projectControl.updateTable(false);
				return;
			}
		}else{
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
		
		String projectNameStr = StringUtils.trimToEmpty((String) projectName.getValue());
		if(projectNameStr.equals("")){
			NotificationUtil.showWarningNotification(AddProject.this, "项目名称不能为空");
			return false;
		}
		
		//在外线中存储项目和外线的关系
		
		SipConfig selectSip = (SipConfig)selectOutLine.getValue();
		// 需判断外线是否已被占用的情况： 1、如果原来没有指定外线，现在指定了；2、如果原来指定了外线，现在也指定了，但是现在指定的外线跟原来的不一样了
		if(selectSip != null && !allUseableOutlineIds.contains(selectSip.getId())) {
			showConfirmWindow(selectSip);
			return false;
		}
		
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
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == projectType_cb) {
			MarketingProjectType projectType = (MarketingProjectType) projectType_cb.getValue();
			if(projectType.getIndex() == MarketingProjectType.QUESTIONNAIRE.getIndex()) {
				questionnaires_tcs.setVisible(true);
			} else {
				questionnaires_tcs.setVisible(false);
			}
		}
	}
}
