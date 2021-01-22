package com.jiangyifen.ec2.ui.mgr.deptmanage;

import java.util.Arrays;
import java.util.List;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.DeptManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *编辑部门
 * @author chb
 */
@SuppressWarnings("serial")
public class EditDept extends Window implements Button.ClickListener {
	private static final Object[] DEPT_COL_ORDER = new Object[] {"name","parent","description"};
	private static final String[] DEPT_COL_NAME = new String[] {"部门名称","上级部门","描述信息"};
	
/**
 * 主要组件输出	
 */
	//Form输出
	private Form form;
	private ComboBox depts;//部门列表
	private BeanItemContainer<Department> deptContainer;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

/**
 * 其他组件
 */
	//持有Department控制对象的引用
	private DeptManagement deptManagement;
	private Department dept;
	private DepartmentService deptService;
	private List<Department> departments;
	/**
	 * 添加Department
	 * @param project
	 */
	public EditDept(DeptManagement deptManagement) {
		this.center();
		this.setModal(true);
		this.deptManagement=deptManagement;
		
		//Service初始化
		deptService=SpringContextHolder.getBean("departmentService");
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//From输出
		form=new Form();
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
		form.addStyleName("chb");
		//创建Form时提前取出所有的角色，以用于回显做准备
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(buildButtonsLayout());
		windowContent.addComponent(form);
	}
	
	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
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

		return buttonsLayout;
	}
	
	/**
	 * 由attach 调用每次显示窗口时更新显示窗口里form信息
	 */
	private void updateFormInfo(){
		//则自己新建部门
		this.setCaption("编辑部门");
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<Department>(dept),Arrays.asList(DEPT_COL_ORDER));
		for (int i = 0; i < DEPT_COL_ORDER.length; i++) {
			form.getField(DEPT_COL_ORDER[i]).setCaption(DEPT_COL_NAME[i]);
		}
		
		//排除子部门 ，设置ComboBox的数据源
		departments=deptService.excludeSubDepartments(dept,SpringContextHolder.getDomain());
		deptContainer.removeAllItems();
		deptContainer.addAll(departments);
		
		//设置特殊组件的回显信息，设置部门的选中情况
		Department parent=dept.getParent(); 
		Department toSelectDept=null;
		for(Department deptTemp:departments){ //迭代所有部门，判断Id是否相等
			if(parent!=null&&parent.getId().equals(deptTemp.getId())){
				toSelectDept=deptTemp;
			}
		}
		
		//设置上级部门ComboBox的选中值
		form.getField("parent").setValue(toSelectDept);
	}

	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private boolean executeSave() {
		try {
			//已经在按钮单击时commit过了
			//Department不必去维护它和角色或用户的关系
			dept.setDomain(SpringContextHolder.getDomain());
			deptService.update(dept);
			//刷新deptManagement的Table到首页
			deptManagement.updateTable(false);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存部门失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		return true;
	}
	
	private class MyFieldFactory extends DefaultFieldFactory{
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("name".equals(propertyId)){
				TextField empNoField=new TextField();
				empNoField.setNullRepresentation("");
				empNoField.setRequired(true);
				empNoField.setRequiredError("部门名字不能为空");
				empNoField.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,64}$", "部门名称 只能由长度不大于64位的字符组成(可以为汉字)"));
				return empNoField;
			}else if ("parent".equals(propertyId)) {
				depts = new ComboBox();
				deptContainer=new BeanItemContainer<Department>(Department.class);
				depts.setContainerDataSource(deptContainer);
				depts.setItemCaptionPropertyId("name");
				depts.setNullSelectionAllowed(false);
				depts.setInputPrompt("选择上级部门");
				depts.setTextInputAllowed(false);
				depts.setImmediate(true);
				depts.setWidth("7em");
				return depts;
			}else if("description".equals(propertyId)){
				TextArea noteArea = new TextArea();
				noteArea.setColumns(20);
				noteArea.setRows(5);
				noteArea.setWordwrap(true);
				noteArea.setNullRepresentation("");
				noteArea.setInputPrompt("请输入与项目相关的描述信息！");
				noteArea.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,64}$", "部门描述 只能由长度不大于64位的字符组成(可以为汉字)"));
				return noteArea;
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
		dept=(Department)deptManagement.getTable().getValue();
		updateFormInfo();
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
				form.commit();
			} catch (Exception e) {
				this.showNotification(e.getMessage());
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
}
