package com.jiangyifen.ec2.ui.mgr.sounddialout;

import java.util.ArrayList;
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
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.SoundDialout;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *添加和编辑窗口，可以添加语音群发和编辑语音群发 
 * @author chb
 */
@SuppressWarnings("serial")
public class AddSoundDialoutTask extends Window implements Button.ClickListener {
/**
 * 主要组件输出	
 */
	//Form输出
	private Form form;
	private TextField autoDialoutName;
	private TextField concurrentLimitField;
	private ComboBox projectComboBox;
	private ComboBox selectSoundComboBox;
	private TextArea noteArea;
	
	//选择播放音乐组件
	private HorizontalLayout selectSoundLayout;
		
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

/**
 * 其他组件
 */
	//持有语音群发引用或自己新建的语音群发
	private AutoDialoutTask autoDialoutTask;
	//持有语音群发控制对象的引用
	private SoundDialout soundDialout;
	private List<MarketingProject> projectList;
	private Domain domain;
	private User loginUser;
	private AutoDialoutTaskService autoDialoutTaskService;
	private QueueService queueService;
	private SoundFileService soundFileService;
	private MarketingProjectService marketingProjectService;
	private DepartmentService departmentService;
	
	/**
	 * 如果语音群发为null，则自己新建语音群发，否则为编辑语音群发
	 * <p>是编辑语音群发还是添加语音群发由设置的语音群发决定</p>
	 * @param project
	 */
	public AddSoundDialoutTask(SoundDialout soundDialout) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.soundDialout=soundDialout;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
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
					autoDialoutName = new TextField("语音群发名称");
					autoDialoutName.setInputPrompt("名称");
					autoDialoutName.setWidth("7em");
					autoDialoutName.setRequired(true);
					autoDialoutName.setRequiredError("语音群发名称不能为空");
					autoDialoutName.setNullRepresentation("");
					return autoDialoutName; 
				}else if("marketingProject".equals(propertyId)) {
					BeanItemContainer<MarketingProject> projectContainer=new BeanItemContainer<MarketingProject>(MarketingProject.class);
					// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
					List<Long> allGovernedDeptIds = new ArrayList<Long>();
					for(Role role : loginUser.getRoles()) {
						if(role.getType().equals(RoleType.manager)) {
							List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
							if(departments.isEmpty()) {
								allGovernedDeptIds.add(0L);
							} else {
								for (Department dept : departments) {
									Long deptId = dept.getId();
									if (!allGovernedDeptIds.contains(deptId)) {
										allGovernedDeptIds.add(deptId);
									}
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
				}else if("concurrentLimit".equals(propertyId)) {
					concurrentLimitField = new TextField("并发上限");
					concurrentLimitField.setInputPrompt("并发上限");
					concurrentLimitField.setWidth("7em");
					concurrentLimitField.addValidator(new Validator() {
						@Override
						public void validate(Object value) throws InvalidValueException {
							if(value==null||value.equals("")){
								throw new Validator.InvalidValueException("并发上限不能为空");
							}else{
								try {
									Integer.parseInt(value.toString());
								} catch (NumberFormatException e) {
									throw new Validator.InvalidValueException("并发上限必须是整数");
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
					concurrentLimitField.setNullRepresentation("");
					concurrentLimitField.setRequired(true);
					concurrentLimitField.setRequiredError("并发上限不能为空");
					return concurrentLimitField; 
				}  else if(propertyId.equals("note")){
					noteArea = new TextArea("语音群发备注");
					noteArea.setColumns(15);
					noteArea.setRows(3);
					noteArea.setWordwrap(true);
					noteArea.setInputPrompt("请输入与语音群发相关的备注信息！");
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
		domain=SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
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
		
		// 保存按钮
		save = new Button("保存");
		save.addListener(this);
		buttonsLayout.addComponent(save);

		//取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);
		fullWidthLayout.addComponent(buttonsLayout);
		verticalLayout.addComponent(fullWidthLayout);
		return verticalLayout;
	}
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private Boolean executeSave() {
		SoundFile soundFile=(SoundFile)selectSoundComboBox.getValue();
		if(soundFile==null){
			NotificationUtil.showWarningNotification(this, "请选择声音文件!");
			return false;
		}
		
		//如果为null说明是新建的语音群发，对语音群发设置时间，否则不设置时间
		autoDialoutTask.setCreateDate(new Date());
		autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.NEW);
		autoDialoutTask.setPreAudioPlay(AutoDialoutTask.PLAY_PRE_AUDIO);
		autoDialoutTask.setSoundFile(soundFile);
		autoDialoutTask.setDialoutType(AutoDialoutTask.SOUND_DIALOUT);
		autoDialoutTask.setDomain(domain);
		autoDialoutTask.setCreator(loginUser);
		
		Queue queue=new Queue();
		queue.setDomain(domain);
		queue.setTimeout(1L);
		queue.setWrapuptime(1L);
		queue.setAnnounce_frequency(60L);
		queue.setAnnounce_round_seconds(15L);
		queue.setAutopause("no");
		queue.setJoinempty("yes");
		queue.setMonitor_format("wav");
		queue.setDynamicmember(true);
		queue.setStrategy("rrmemory");
		queue.setSetinterfacevar(true);
		queue.setIsModifyable(false);
		//创建假对象，使EL可以存储
		MusicOnHold musicOnHold=new MusicOnHold();
		musicOnHold.setId(1L);
		queue.setMusiconhold(musicOnHold);
		queue.setDescription(autoDialoutTask.getAutoDialoutTaskName());
		queue=queueService.update(queue);;
		autoDialoutTask.setQueue(queue);
		//将Queue刷新到Asterisk
		queueService.updateAsteriskQueueFile(domain); 
		autoDialoutTaskService.update(autoDialoutTask);
		return true;
	}

	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo(){
		this.setCaption("新建语音群发");
		autoDialoutTask=new AutoDialoutTask();
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<AutoDialoutTask>(autoDialoutTask));
		form.setVisibleItemProperties(new String[] { "autoDialoutTaskName","note","marketingProject","concurrentLimit"});
		if(projectList.size()>0){
			projectComboBox.setValue(projectList.get(0));
		}
	}
	
	/**
	 * 刷新声音文件
	 */
	private void refreshSoundFile() {
		selectSoundLayout.removeAllComponents();
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
	
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		autoDialoutTask=null;
		updateFormInfo();
		refreshSoundFile();
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==save){
			Boolean isSaveSucess=false;
			try {
				try {
					form.commit();
				} catch (Exception e) {
					this.showNotification(e.getMessage());
					return;
				}
				isSaveSucess=executeSave();
				soundDialout.updateTable(false);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "新建语音外呼出现错误！");
				soundDialout.updateTable(false);
				return;
			}
			if(isSaveSucess){
				//刷新ProjectControl的语音群发Table在当前页
				this.getParent().removeWindow(this);
			}
		}else{
			this.getParent().removeWindow(this);
		}
	}
}
