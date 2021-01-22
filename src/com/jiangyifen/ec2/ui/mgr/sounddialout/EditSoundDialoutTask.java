package com.jiangyifen.ec2.ui.mgr.sounddialout;

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
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
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
public class EditSoundDialoutTask extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
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
	private List<SoundFile> soundFiles;
	private Domain domain;
	private User loginUser;
	private AutoDialoutTaskService autoDialoutTaskService;
	private SoundFileService soundFileService;
	private MarketingProjectService marketingProjectService;
	private DepartmentService departmentService;
	 
	
	/**
	 * 如果语音群发为null，则自己新建语音群发，否则为编辑语音群发
	 * <p>是编辑语音群发还是添加语音群发由设置的语音群发决定</p>
	 * @param project
	 */
	public EditSoundDialoutTask(SoundDialout soundDialout) {
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
		form.setFooter(buildButtonsLayout());
	}
	
	/**
	 * 初始化所有的Service
	 */
	private void initService() {
		domain=SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
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
	private void executeSave() {
		SoundFile soundFile=(SoundFile)selectSoundComboBox.getValue();
		if(soundFile==null){
			NotificationUtil.showWarningNotification(this, "请选择声音文件!");
			return;
		}
		autoDialoutTask.setSoundFile(soundFile);

		autoDialoutTaskService.update(autoDialoutTask);
		//刷新autoDialout的语音群发Table在当前页
		soundDialout.updateTable(false);
		//更新正在进行语音群发线程的相关数据
		if(autoDialoutTask.getAutoDialoutTaskStatus().equals(AutoDialoutTaskStatus.RUNNING)){
			ProjectResourceConsumer consumerThread=(ProjectResourceConsumer)AutoDialHolder.nameToThread.get(AutoDialHolder.AUTODIAL_CONSUMER_PRE+autoDialoutTask.getId());
			if(consumerThread==null){
				logger.warn("chb: 正在进行语音群发的任务没有线程在运行！");
			}else{
				consumerThread.refreshEditChangeVariable();
			}
		}
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		autoDialoutTask=(AutoDialoutTask)soundDialout.getTable().getValue();
		//设置标题
		this.setCaption("编辑语音群发"+autoDialoutTask.getAutoDialoutTaskName());
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<AutoDialoutTask>(autoDialoutTask),Arrays.asList( "autoDialoutTaskName","dialoutType","note","marketingProject","ratio","concurrentLimit","preAudioPlay"));
		
		refreshSoundFile();
		
		//回显
		Long projectId=autoDialoutTask.getMarketingProject().getId();
		for(MarketingProject project:projectList){
			if(project.getId().equals(projectId)){
				projectComboBox.setValue(project);
				break;
			}
		}
		
		//回显播放的语音文件
		if(soundFiles!=null&&soundFiles.size()>0&&selectSoundComboBox!=null){
			for(SoundFile soundFile:soundFiles){
				if(soundFile.getId() != null && soundFile.getId().equals(autoDialoutTask.getSoundFile().getId())){
					selectSoundComboBox.setValue(soundFile);
				}
			}
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
		soundFiles=soundFileService.getAll(domain);
		soundFileContainer.addAll(soundFiles);
		selectSoundComboBox.setContainerDataSource(soundFileContainer);
		selectSoundComboBox.setItemCaptionPropertyId("descName");
		selectSoundLayout.addComponent(new Label("选择声音文件："));
		selectSoundLayout.addComponent(selectSoundComboBox);
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
				executeSave();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "保存出现异常！");
				return;
			}
		}
		this.getParent().removeWindow(this);
	}
}
