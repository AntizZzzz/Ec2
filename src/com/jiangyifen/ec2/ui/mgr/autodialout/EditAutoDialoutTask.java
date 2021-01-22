package com.jiangyifen.ec2.ui.mgr.autodialout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.autodialout.ProjectResourceConsumer;
import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.AutoDialout;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.AutoDialConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *添加和编辑窗口，可以添加自动外呼和编辑自动外呼 
 * @author chb
 */
@SuppressWarnings("serial")
public class EditAutoDialoutTask extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
/**
 * 主要组件输出	
 */
	//Form输出
	private Form form;
	private TextField autoDialoutName;
	private ComboBox ratioComboBox;
	private TextField staticExpectedCallersField;
	private OptionGroup preAudioPlay;
	private ComboBox projectComboBox;
	private TextArea noteArea;
	private ComboBox percentageDepthComboBox;
	private OptionGroup isSystemAjust;
	private ComboBox selectSoundComboBox;
	private ComboBox wrapUpTimeComboBox;
	private ComboBox timeoutComboBox;			// jrh 被叫用户必须在指定的时间能接起,用于设置队列的timeout 信息
	//选择播放音乐组件
	private HorizontalLayout selectSoundLayout;
		
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

/**
 * 其他组件
 */
	//持有自动外呼引用或自己新建的自动外呼
		private AutoDialoutTask autoDialoutTask;
		//持有自动外呼控制对象的引用
		private AutoDialout autoDialout;
		private Domain domain;
		private User loginUser;
		private List<MarketingProject> projectList;
		private AutoDialoutTaskService autoDialoutTaskService;
		private MarketingProjectService marketingProjectService;
		private SoundFileService soundFileService;
		private DepartmentService departmentService;
		private ReloadAsteriskService reloadAsteriskService;
		private List<SoundFile> soundFiles; //回显使用
	 
	
	/**
	 * 如果自动外呼为null，则自己新建自动外呼，否则为编辑自动外呼
	 * <p>是编辑自动外呼还是添加自动外呼由设置的自动外呼决定</p>
	 * @param project
	 */
	public EditAutoDialoutTask(AutoDialout autoDialout) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.autoDialout=autoDialout;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		ratioComboBox = new ComboBox("自动外呼系数");
		for(Double ratio=1D;ratio<10;ratio+=0.1D){
			DecimalFormat df = new DecimalFormat("0.0"); 
			ratio=Double.parseDouble(df.format(ratio));
			ratioComboBox.addItem(ratio);
		}
		ratioComboBox.setWidth("7em");
		ratioComboBox.setRequired(true);

		//队列深度
		percentageDepthComboBox = new ComboBox("调整深度(%)");
		for(Integer percentage=1;percentage<100;percentage+=1){
			percentageDepthComboBox.addItem(percentage);
		}
		percentageDepthComboBox.setWidth("7em");
		percentageDepthComboBox.setRequired(true);
		
		
		//From输出
		form=new Form();
		form.setValidationVisible(false);
		form.setValidationVisibleOnCommit(false);
		form.addStyleName("chb");
		
		//设置Form 生成的Field
		form.setFormFieldFactory(new DefaultFieldFactory() {
			@Override
			public Field createField(Item item, Object propertyId,
					Component uiContext) {
				if(propertyId.equals("autoDialoutTaskName")){
					autoDialoutName = new TextField("自动外呼名称");
					autoDialoutName.setInputPrompt("名称");
					autoDialoutName.setWidth("7em");
					autoDialoutName.setRequired(true);
					autoDialoutName.setRequiredError("自动外呼名称不能为空");
					autoDialoutName.setNullRepresentation("");
					return autoDialoutName; 
				}else if("marketingProject".equals(propertyId)) {
					BeanItemContainer<MarketingProject> projectContainer=new BeanItemContainer<MarketingProject>(MarketingProject.class);
					// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
					List<Long> allGovernedDeptIds = new ArrayList<Long>();
					for(Role role : loginUser.getRoles()) {
						if(role.getType().equals(RoleType.manager)) {
							for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
								Long deptId = dept.getId();
								if(!allGovernedDeptIds.contains(deptId)) {
									allGovernedDeptIds.add(deptId);
								}
							}
						}
					}
					// jrh 获取前用户所属部门及其所有角色的管辖部门成员创建的项目
					projectList = marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
					
					projectComboBox=new ComboBox("项目");
					projectComboBox.setNullSelectionAllowed(false);
					projectContainer.addAll(projectList);
					projectComboBox.setContainerDataSource(projectContainer);
					projectComboBox.setItemCaptionPropertyId("projectName");
					projectComboBox.setRequired(true);
					projectComboBox.setRequiredError("项目不能为空");
					return projectComboBox;
				}else if("ratio".equals(propertyId)) {
					return ratioComboBox; 
				}else if("percentageDepth".equals(propertyId)) {
					return percentageDepthComboBox; 
				}else if("staticExpectedCallers".equals(propertyId)) {
					staticExpectedCallersField = new TextField("队列深度");
					staticExpectedCallersField.setInputPrompt("队列深度");
					staticExpectedCallersField.setWidth("7em");
					staticExpectedCallersField.addValidator(new Validator() {
						@Override
						public void validate(Object value) throws InvalidValueException {
							if(value==null||value.equals("")){
								throw new Validator.InvalidValueException("队列深度不能为空");
							}else{
								try {
									Integer.parseInt(value.toString());
								} catch (NumberFormatException e) {
									throw new Validator.InvalidValueException("队列深度必须是整数");
								}
							}
						}
						
						@Override
						public boolean isValid(Object value) {
							if(value==null||value.equals("")){
								return false;
							}else{
								try {
									Integer.parseInt(value.toString());
									return true;
								} catch (NumberFormatException e) {
									return false;
								}
							}
						}
					});
					staticExpectedCallersField.setNullRepresentation("");
					staticExpectedCallersField.setRequired(true);
					staticExpectedCallersField.setRequiredError("队列深度不能为空");
					return staticExpectedCallersField; 
				}else if("preAudioPlay".equals(propertyId)) {
					preAudioPlay=new OptionGroup("外呼音乐", Arrays.asList(AutoDialoutTask.PLAY_PRE_AUDIO,AutoDialoutTask.NOT_PLAY_PRE_AUDIO));
					preAudioPlay.setImmediate(true);
					preAudioPlay.addListener(new ValueChangeListener() {
						@Override
						public void valueChange(ValueChangeEvent event) {
							if(preAudioPlay.getValue().equals(AutoDialoutTask.PLAY_PRE_AUDIO)){
								showSelectSoundComponent(true);
							}else if(preAudioPlay.getValue().equals(AutoDialoutTask.NOT_PLAY_PRE_AUDIO)){
								showSelectSoundComponent(false);
							}
						}
					});
					preAudioPlay.setStyleName("twocolchb");
					preAudioPlay.setRequired(true);
					preAudioPlay.setRequiredError("外呼音乐不能为空");
					return preAudioPlay;
				}  else if("isSystemAjust".equals(propertyId)) {
					isSystemAjust=new OptionGroup("外呼调整", Arrays.asList(AutoDialoutTask.SYSTEM_AJUST,AutoDialoutTask.NOT_SYSTEM_AJUST));
					isSystemAjust.setImmediate(true);
					isSystemAjust.addListener(new ValueChangeListener() {
						@Override
						public void valueChange(ValueChangeEvent event) {
							if(isSystemAjust.getValue().equals(AutoDialoutTask.SYSTEM_AJUST)){
								if(percentageDepthComboBox!=null){
									percentageDepthComboBox.setRequired(true);
									percentageDepthComboBox.setValue(Integer.parseInt(AutoDialConfig.props.getProperty(AutoDialConfig.DEFAULT_PERCENTAGE_DEPTH)));
									percentageDepthComboBox.setEnabled(true);

								}
								if(staticExpectedCallersField!=null){
									staticExpectedCallersField.setRequired(false);
									staticExpectedCallersField.setValue(null);
									staticExpectedCallersField.setEnabled(false);
								}
							}else if(isSystemAjust.getValue().equals(AutoDialoutTask.NOT_SYSTEM_AJUST)){
								if(percentageDepthComboBox!=null){
									percentageDepthComboBox.setRequired(false);
									percentageDepthComboBox.setValue(null);
									percentageDepthComboBox.setEnabled(false);
									
								}
								if(staticExpectedCallersField!=null){
									staticExpectedCallersField.setRequired(true);
									staticExpectedCallersField.setValue(Integer.parseInt(AutoDialConfig.props.getProperty(AutoDialConfig.DEFAULT_STATIC_EXPECTED_CALLERS)));
									staticExpectedCallersField.setEnabled(true);
								}
							}
						}
					});
					isSystemAjust.setStyleName("twocolchb");
					isSystemAjust.setRequired(true);
					isSystemAjust.setRequiredError("外呼调整不能为空");
					return isSystemAjust;
				}   else if(propertyId.equals("note")){
					noteArea = new TextArea("自动外呼备注");
					noteArea.setColumns(15);
					noteArea.setRows(3);
					noteArea.setWordwrap(true);
					noteArea.setInputPrompt("请输入与自动外呼相关的备注信息！");
					noteArea.setNullRepresentation("");
					return noteArea;
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
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		autoDialoutTaskService=SpringContextHolder.getBean("autoDialoutTaskService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		soundFileService=SpringContextHolder.getBean("soundFileService");
		departmentService=SpringContextHolder.getBean("departmentService");
		reloadAsteriskService=SpringContextHolder.getBean("reloadAsteriskService");
	}
	
	/**
	 * 按钮输出区域
	 * @return
	 */
	private VerticalLayout buildButtonsLayout() {
		//垂直布局
		VerticalLayout verticalLayout=new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.setWidth("100%");

		//声音选择组件
		selectSoundLayout=new HorizontalLayout();
		verticalLayout.addComponent(selectSoundLayout);
		
		//按钮组件
		HorizontalLayout fullWidthLayout=new HorizontalLayout();
		fullWidthLayout.setWidth("100%");
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		// lc
		HorizontalLayout wrapLayout = new HorizontalLayout();
		wrapLayout.setWidth("100%");
		wrapLayout.setSpacing(true);
 
		Label wrapLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;冷却时长(秒)", Label.CONTENT_XHTML);
		wrapLayout.addComponent(wrapLabel);
		//冷却时长
	    wrapUpTimeComboBox = new ComboBox();
	    wrapUpTimeComboBox.setNullSelectionAllowed(false);
		for(int i = 0;i <= 60; i++){
			wrapUpTimeComboBox.addItem(i);
		}
		wrapUpTimeComboBox.setWidth("7em");
		wrapLayout.addComponent(wrapUpTimeComboBox);
		
		// jrh
		HorizontalLayout timeoutLayout = new HorizontalLayout();
		timeoutLayout.setWidth("100%");
		timeoutLayout.setSpacing(true);
		
		Label timeoutLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;振铃时长(秒)", Label.CONTENT_XHTML);
		timeoutLayout.addComponent(timeoutLabel);
		timeoutComboBox = new ComboBox();
		timeoutComboBox.setNullSelectionAllowed(false);
		for(int i = 1;i <= 60; i++){
			timeoutComboBox.addItem(i);
		}
		timeoutComboBox.setWidth("7em");
		timeoutLayout.addComponent(timeoutComboBox);
		
		// 保存按钮
		save = new Button("保存");
		save.addListener(this);
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);
		fullWidthLayout.addComponent(buttonsLayout);
		verticalLayout.addComponent(wrapLayout);
		verticalLayout.addComponent(timeoutLayout);
		verticalLayout.addComponent(fullWidthLayout);
		return verticalLayout;
	}
	
	/**
	 * 是否选择音乐文件组件
	 * @param isShow
	 */
	private void showSelectSoundComponent(boolean isShow) {
		selectSoundLayout.removeAllComponents();
		//如果显示则添加组件
		if(isShow){
			selectSoundComboBox=new ComboBox();
			selectSoundComboBox.setNullSelectionAllowed(false);
			BeanItemContainer<SoundFile> soundFileContainer=new BeanItemContainer<SoundFile>(SoundFile.class);
			soundFiles=soundFileService.getAll(domain);
			soundFileContainer.addAll(soundFiles);
			selectSoundComboBox.setContainerDataSource(soundFileContainer);
			selectSoundComboBox.setItemCaptionPropertyId("descName");
			selectSoundLayout.addComponent(new Label("选择声音文件："));
			selectSoundLayout.addComponent(selectSoundComboBox);
		}
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private Boolean executeSave() {
		SoundFile soundFile=null;
		if(preAudioPlay.getValue().equals(AutoDialoutTask.PLAY_PRE_AUDIO)){
			soundFile=(SoundFile)selectSoundComboBox.getValue();
			if(soundFile==null){
				NotificationUtil.showWarningNotification(this, "请选择声音文件!");
				return false;
			}
		}
		
		String autoDialoutNameStr="";
		if(autoDialoutName.getValue()!=null){
			autoDialoutNameStr=autoDialoutName.getValue().toString().trim();
		}
		
		OperationLogUtil.simpleLog(loginUser, "自动外呼-编辑："+autoDialoutTask.getAutoDialoutTaskName()+" Id:"+autoDialoutTask.getId());
		
		if(autoDialoutNameStr.equals("")){
			NotificationUtil.showWarningNotification(this, "自动外呼名称不能为空");
			return false;
		}
		autoDialoutTask.setSoundFile(soundFile);
		autoDialoutTask=autoDialoutTaskService.update(autoDialoutTask);
		//刷新autoDialout的自动外呼Table在当前页
		autoDialout.updateTable(false);
		//更新正在进行自动外呼线程的相关数据
		if(autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)){
			ProjectResourceConsumer consumerThread=(ProjectResourceConsumer)AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId());
			if(consumerThread==null){
				logger.warn("chb: 正在进行自动外呼的任务没有线程在运行！");
			}else{
				consumerThread.refreshEditChangeVariable();
			}
		}
		
		  QueueService queueService =SpringContextHolder.getBean("queueService");		
	      Queue queue = autoDialoutTask.getQueue();
	      queue.setTimeout(Long.valueOf(timeoutComboBox.getValue().toString().trim()));
	      queue.setWrapuptime(Long.valueOf(wrapUpTimeComboBox.getValue().toString().trim()));
	      queueService.update(queue);
	  	queueService.updateAsteriskQueueFile(domain);
	  	
	  	reloadAsteriskService.reloadQueue();
		
		return true;
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		autoDialoutTask=(AutoDialoutTask)autoDialout.getTable().getValue();
		//设置标题
		this.setCaption("编辑自动外呼"+autoDialoutTask.getAutoDialoutTaskName());
		//设置Form的数据源
		form.setVisibleItemProperties(new String[] { "autoDialoutTaskName","marketingProject","ratio","isSystemAjust","percentageDepth","staticExpectedCallers","preAudioPlay","note"});
		form.setValue(autoDialoutTask);
		
		//回显
		Long projectId=autoDialoutTask.getMarketingProject().getId();
		for(MarketingProject project:projectList){
			if(project.getId().equals(projectId)){
				projectComboBox.setValue(project);
				break;
			}
		}
		
		//对于队列深度的回显进行调整限制
		if(autoDialoutTask.getIsSystemAjust().equals(AutoDialoutTask.SYSTEM_AJUST)){
			if(staticExpectedCallersField!=null){
				staticExpectedCallersField.setRequired(false);
				staticExpectedCallersField.setValue(null);
				staticExpectedCallersField.setEnabled(false);
			}
		}else{
			if(staticExpectedCallersField!=null){
				staticExpectedCallersField.setRequired(true);
				staticExpectedCallersField.setValue(autoDialoutTask.getStaticExpectedCallers());
				staticExpectedCallersField.setEnabled(true);
			}
		}
		
		//回显播放的语音文件
		if(soundFiles!=null&&soundFiles.size()>0&&selectSoundComboBox!=null){
			if(autoDialoutTask.getSoundFile()!=null){
				for(SoundFile soundFile:soundFiles){
					if(soundFile.getId() != null && soundFile.getId().equals(autoDialoutTask.getSoundFile().getId())){
						selectSoundComboBox.setValue(soundFile);
					}
				}
			}
		}
		
		Queue queue=autoDialoutTask.getQueue();
		if(queue!=null){
			Long wrapupTime=queue.getWrapuptime();
			int intWrap=wrapupTime.intValue();
			wrapUpTimeComboBox.setValue(intWrap);
			int intTimeout = queue.getTimeout().intValue();
			timeoutComboBox.setValue(intTimeout);
		}
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			try {
				try {
					form.commit();
				} catch (Exception e) {
					this.showNotification(e.getMessage());
					return;
				}
				if(!executeSave()){
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "保存出现异常！");
				return;
			}
		}
		this.getParent().removeWindow(this);
	}
}
