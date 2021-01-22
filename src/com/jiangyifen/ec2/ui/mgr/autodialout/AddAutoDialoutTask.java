package com.jiangyifen.ec2.ui.mgr.autodialout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MusicOnHold;
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
public class AddAutoDialoutTask extends Window implements Button.ClickListener {
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
	private ComboBox selectSoundComboBox;
	private ComboBox percentageDepthComboBox;
	private ComboBox wrapUpTimeComboBox;
	private ComboBox timeoutComboBox;			// jrh 被叫用户必须在指定的时间能接起,用于设置队列的timeout 信息
	private OptionGroup isSystemAjust;
	private TextArea noteArea;
	
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
	private List<MarketingProject> projectList;
	private Domain domain;
	private User loginUser;
	private AutoDialoutTaskService autoDialoutTaskService;
	private QueueService queueService;
	private MarketingProjectService marketingProjectService;
	private SoundFileService soundFileService;
	private DepartmentService departmentService;
	
	/**
	 * 如果自动外呼为null，则自己新建自动外呼，否则为编辑自动外呼
	 * <p>是编辑自动外呼还是添加自动外呼由设置的自动外呼决定</p>
	 * @param project
	 */
	public AddAutoDialoutTask(AutoDialout autoDialout) {
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
		ratioComboBox.setNullSelectionAllowed(false);
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
					staticExpectedCallersField.setEnabled(false);
					staticExpectedCallersField.addValidator(new Validator() {
						@Override
						public void validate(Object value) throws InvalidValueException {
							if(value==null||value.equals("")){
								throw new Validator.InvalidValueException("固定排队数不能为空");
							}else{
								try {
									Integer.parseInt(value.toString());
								} catch (NumberFormatException e) {
									throw new Validator.InvalidValueException("固定排队数必须是整数");
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
					staticExpectedCallersField.setRequiredError("固定排队数不能为空");
					return staticExpectedCallersField; 
				}else if("preAudioPlay".equals(propertyId)) {
					preAudioPlay=new OptionGroup("语音播放", Arrays.asList(AutoDialoutTask.PLAY_PRE_AUDIO,AutoDialoutTask.NOT_PLAY_PRE_AUDIO));
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
				} else if("isSystemAjust".equals(propertyId)) {
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
				}  else if(propertyId.equals("note")){
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
		VerticalLayout footerLayout=buildButtonsLayout();
		form.setFooter(footerLayout);
	}
	/**
	 * 初始化所有的Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		queueService=SpringContextHolder.getBean("queueService");
		autoDialoutTaskService=SpringContextHolder.getBean("autoDialoutTaskService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		soundFileService=SpringContextHolder.getBean("soundFileService");
		departmentService=SpringContextHolder.getBean("departmentService");
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
		
//		lc
		HorizontalLayout wrapLayout = new HorizontalLayout();
		wrapLayout.setWidth("100%");
		wrapLayout.setSpacing(true);
		
		Label wrapLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;冷却时长(秒)", Label.CONTENT_XHTML);
		wrapLabel.setWidth("-1px");
		wrapLayout.addComponent(wrapLabel);
		//冷却时长
	    wrapUpTimeComboBox = new ComboBox();
	    wrapUpTimeComboBox.setNullSelectionAllowed(false);
		for(int i = 0;i <= 60; i++){
			wrapUpTimeComboBox.addItem(i);
		}
		wrapUpTimeComboBox.setValue(1);
		wrapUpTimeComboBox.setWidth("7em");
		wrapLayout.addComponent(wrapUpTimeComboBox);

//		jrh
		HorizontalLayout timeoutLayout = new HorizontalLayout();
		timeoutLayout.setWidth("100%");
		timeoutLayout.setSpacing(true);
		
		Label timeoutLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;振铃时长(秒)", Label.CONTENT_XHTML);
		timeoutLabel.setWidth("-1px");
		timeoutLayout.addComponent(timeoutLabel);
		
		timeoutComboBox = new ComboBox();
		timeoutComboBox.setNullSelectionAllowed(false);
		for(int i = 1; i <= 60; i++){
			timeoutComboBox.addItem(i);
		}
		timeoutComboBox.setValue(8);
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
		
		//如果为null说明是新建的自动外呼，对自动外呼设置时间，否则不设置时间
		autoDialoutTask.setCreateDate(new Date());
		autoDialoutTask.setDialoutType(AutoDialoutTask.AUTO_DIALOUT);
		autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.NEW);
		autoDialoutTask.setSoundFile(soundFile);
		autoDialoutTask.setDomain(domain);
		autoDialoutTask.setCreator(loginUser);
		OperationLogUtil.simpleLog(loginUser, "自动外呼-添加："+autoDialoutTask.getAutoDialoutTaskName());
		
		Queue queue=new Queue();
		queue.setDomain(domain);
		queue.setAnnounce_frequency(60L);
		queue.setAnnounce_round_seconds(15L);
		queue.setAutopause("no");
		queue.setMaxlen(500L);
//		queue.setTimeout(8L);
		queue.setTimeout(Long.valueOf(timeoutComboBox.getValue().toString().trim()));
		queue.setWrapuptime(Long.valueOf(wrapUpTimeComboBox.getValue().toString().trim()));
		queue.setJoinempty("yes");
		queue.setDynamicmember(true);
		queue.setStrategy("rrmemory");
		queue.setSetinterfacevar(true);
		queue.setIsModifyable(false);
		//创建假对象，使EL可以存储
		MusicOnHold musicOnHold=new MusicOnHold();
		musicOnHold.setId(1L);
		queue.setMusiconhold(musicOnHold);
		queue.setDescription(autoDialoutTask.getAutoDialoutTaskName());
		queue=queueService.update(queue);
		//将Queue刷新到Asterisk
		queueService.updateAsteriskQueueFile(domain);
		//
		ReloadAsteriskService reloadAsteriskService=SpringContextHolder.getBean("reloadAsteriskService");
		reloadAsteriskService.reloadQueue();
		autoDialoutTask.setQueue(queue);
		try {
			autoDialoutTaskService.update(autoDialoutTask);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
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
			List<SoundFile> soundFiles=soundFileService.getAll(domain);
			soundFileContainer.addAll(soundFiles);
			selectSoundComboBox.setContainerDataSource(soundFileContainer);
			selectSoundComboBox.setItemCaptionPropertyId("descName");
			selectSoundLayout.addComponent(new Label("选择声音文件："));
			selectSoundLayout.addComponent(selectSoundComboBox);
		}
	}

	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo(){
		this.setCaption("新建自动外呼");
		autoDialoutTask=new AutoDialoutTask();
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<AutoDialoutTask>(autoDialoutTask));
		form.setVisibleItemProperties(new String[] { "autoDialoutTaskName","marketingProject","ratio","isSystemAjust","percentageDepth","staticExpectedCallers","preAudioPlay","note"});
		if(projectList.size()>0){
			projectComboBox.setValue(projectList.get(0));
		}
		//默认选中不播放音乐
		preAudioPlay.setValue(AutoDialoutTask.NOT_PLAY_PRE_AUDIO);
	}
	
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		autoDialoutTask=null;
		updateFormInfo();
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			Boolean isSaveSuccess=false;
			try {
				try {
					form.commit();
				} catch (Exception e) {
					this.showNotification(e.getMessage());
					return;
				}
				isSaveSuccess=executeSave();
				autoDialout.updateTable(false);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "新建自动外呼出现错误！");
				autoDialout.updateTable(false);
				return;
			}
			if(isSaveSuccess){
				//刷新ProjectControl的自动外呼Table在当前页
				this.getParent().removeWindow(this);
			}
		}else{
			this.getParent().removeWindow(this);
		}
	}
}
