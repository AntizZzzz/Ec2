package com.jiangyifen.ec2.ui.mgr.rolemanage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.service.eaoservice.BusinessModelService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.RoleService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.RoleManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 添加，可以添加角色
 * 
 * @author chb
 */
@SuppressWarnings("serial")
public class EditRole extends Window implements Button.ClickListener, ValueChangeListener {

	private static final Object[] ROLE_COL_ORDER = new Object[] {"name","description", "type", "departments"};
	private static final String[] ROLE_COL_NAME = new String[] {"角色名称","描述信息", "角色类型", "管辖部门"};

	/**
	 * 主要组件输出
	 */
	// Form输出
	private Form form;

	//角色类型
	private ComboBox roleTypes;

	// 保存按钮和取消按钮
	private Button save;
	private Button cancel;
	
	//右侧的角色管理组件输出区
	private RoleAccess roleAccess;
	
	/**
	 * 其他组件
	 */
	// 持有Role控制对象的引用
	private RoleManagement roleManagement;
	private Role role;
	private RoleService roleService; //更新Role时使用
	private DepartmentService departmentService; //获取角色对应的部门使用
	private BusinessModelService businessModelService; 
	private List<Department> allDepts; //用于回显部门

	/**
	 * 添加Role
	 * @param roleManagement
	 */
	public EditRole(RoleManagement roleManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.roleManagement = roleManagement;

		// Service初始化
		roleService = SpringContextHolder.getBean("roleService");
		departmentService = SpringContextHolder.getBean("departmentService");
		businessModelService = SpringContextHolder.getBean("businessModelService");
		
		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//上方（除了按钮组件）组件输出
		HorizontalLayout northLayout=new HorizontalLayout();
		northLayout.setSizeFull();
		windowContent.addComponent(northLayout);
		windowContent.setExpandRatio(northLayout, 1.0f);
		
		//按钮显示
		HorizontalLayout southLayout = buildButtonsLayout();
		windowContent.addComponent(southLayout);
		windowContent.setComponentAlignment(southLayout, Alignment.BOTTOM_RIGHT);
		
		// 创建Form时提前取出所有的部门模块
		allDepts= departmentService.getAll(SpringContextHolder.getDomain());
		
		//From输出
		form=new Form();
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
		form.addStyleName("chb");
		form.setWriteThrough(false);
		form.setFormFieldFactory(new MyFieldFactory());
		VerticalLayout formLayout=new VerticalLayout();
		formLayout.addComponent(form);
		northLayout.addComponent(formLayout);

		//添加右侧的权限控制输出区域
		roleAccess=new RoleAccess();
		northLayout.addComponent(roleAccess);
	}
	
	/**
	 * 按钮输出区域
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("23%");
		buttonsLayout.setSpacing(true);
		
		HorizontalLayout constraintLayout = new HorizontalLayout();
		constraintLayout.setSpacing(true);
		buttonsLayout.addComponent(constraintLayout);
		// 保存按钮
		save = new Button("保存");
		save.addListener((Button.ClickListener)this);
		constraintLayout .addComponent(save);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener((Button.ClickListener)this);
		constraintLayout .addComponent(cancel);
		
		return buttonsLayout;
	}
	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo() {
		this.setCaption("编辑角色");
		role=(Role)roleManagement.getTable().getValue();
		// 设置Form的数据源
		form.setItemDataSource(new BeanItem<Role>(role),
				Arrays.asList(ROLE_COL_ORDER));
		for (int i = 0; i < ROLE_COL_ORDER.length; i++) {
			form.getField(ROLE_COL_ORDER[i]).setCaption(ROLE_COL_NAME[i]);
		}

		// 回显角色类型
		form.getField("type").setValue(role.getType());
		
		// TODO 判断是不是在编辑CSR角色，如果是坐席角色，则不需要处理部门信息 ,如果是manager则需要更新部门信息 
		if(form.getField("departments").isReadOnly() == false) {
			//设置部门的回显信息
			Set<Department> selectedDepts = role.getDepartments();
			Set<Department> deptSelectValue = new HashSet<Department>();
			for(Department dept:selectedDepts){
				for(int i=0;i<allDepts.size();i++){
					if(dept.getId().equals(allDepts.get(i).getId())){
						deptSelectValue.add(allDepts.get(i));
					}
				}
			}
			form.getField("departments").setValue(deptSelectValue);
		}
		
		//设置角色权限的回显信息
//		Set<BusinessModel> selectedModels = role.getBusinessModels();
		List<BusinessModel> selectedModels = businessModelService.getModelsByRoleId(role.getId());	// JRH-20141212 使用原生SQL从DB中取出权限，这样如果管理员将自己的某个权限去掉了，研发人员就可以到后台往ec2_role_businessmodel_link加条记录就可以了
		Map<Long, BusinessModel> selectedIds = new HashMap<Long, BusinessModel>();
		for(BusinessModel businessModel : selectedModels){
			selectedIds.put(businessModel.getId(), businessModel);
		}
		roleAccess.echoSelectedModelIds(selectedIds);
		
		// 编辑是不可以该角色类型的
		form.getField("type").setReadOnly(true);
	}
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private boolean executeSave() {
		// 已经在按钮单击时commit过了
		role.setDomain(SpringContextHolder.getDomain());
		//为角色设置权限
		Set<BusinessModel> businessModels=new HashSet<BusinessModel>();
		for(CheckBox checkBox:roleAccess.getCheckBoxMap().values()){ // 通过RoleAccess取得所有的CheckBox
			if((Boolean)checkBox.getValue()){ 
				businessModels.add((BusinessModel)checkBox.getData());
			}
		}
		role.setBusinessModels(businessModels);
		try {
			roleService.update(role);
			// 刷新roleManagement的Table在当前页
			roleManagement.updateTable(false);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存用户失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		return true;
	}
	/**
	 * 内部类
	 * @author chb
	 *
	 */
	private class MyFieldFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if ("name".equals(propertyId)) {
				TextField nameField = new TextField();
				nameField.setNullRepresentation("");
				nameField.setRequired(true);
				nameField.setWidth("170px");
				nameField.setRequiredError("角色名称不能为空");
				nameField.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,64}$", "角色名称 只能由长度不大于64位的字符组成(可以为汉字)"));
				return nameField;
			} else if ("type".equals(propertyId)) {
				roleTypes = new ComboBox();
				roleTypes.addListener(EditRole.this);
				roleTypes.addItem(RoleType.manager);
				roleTypes.addItem(RoleType.csr);
				roleTypes.setNullSelectionAllowed(false);
				roleTypes.setTextInputAllowed(false);
				roleTypes.setImmediate(true);
				roleTypes.setWidth("170px");
				return roleTypes;
			} else if ("departments".equals(propertyId)) {
				TwinColSelect deptSelect = new TwinColSelect();
				deptSelect.setItemCaptionPropertyId("name");
				deptSelect.setLeftColumnCaption("所有部门");
				deptSelect.setRightColumnCaption("所选部门");
				deptSelect.setMultiSelect(true);
				deptSelect.setImmediate(true);
				deptSelect.setRows(6);
				// 设置RoleSelect的数据源
				BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(
						Department.class);
				deptContainer.addAll(allDepts);
				deptSelect.setContainerDataSource(deptContainer);

				RoleType roleType=(RoleType)roleTypes.getValue();
				if(roleType.equals(RoleType.csr)) {
					deptSelect.setReadOnly(true);
				} else if(roleType.equals(RoleType.manager)) {
					deptSelect.setReadOnly(false);
				}
				
				return deptSelect;
			} else if("description".equals(propertyId)){
				TextField noteField=new TextField();
				noteField.setNullRepresentation("");
				noteField.setWidth("170px");
				noteField.setInputPrompt("请输入描述信息！");
				noteField.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,64}$", "角色描述 只能由长度不大于64位的字符组成(可以为汉字)"));
				return noteField;
			}else{
				return null;
			}
		}
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		updateFormInfo();
	}

	/**
	 * 点击保存、取消按钮的事件
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			try {
				form.commit();
			} catch (Exception e) {
				this.getApplication().getMainWindow().showNotification(e.getMessage());
				return;
			}
			boolean saveSuccess = executeSave();
			if(saveSuccess) {
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}
	/**
	 * 选中的角色类型改变时，更改权限空着的显示
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		RoleType roleType=(RoleType)roleTypes.getValue();
		if(form.getField("departments") != null) {
			if(roleType.equals(RoleType.csr)) {
				form.getField("departments").setReadOnly(true);
			} else if(roleType.equals(RoleType.manager)) {
				form.getField("departments").setReadOnly(false);
			}
		}
		// 更新权限设置勾选框组件
		roleAccess.updateModels(roleType);
	}

}
