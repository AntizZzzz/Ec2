package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.util.ArrayList;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.ui.csr.workarea.incoming.IncomingDialTabView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 	 客户详细信息面板中显示该客户的具体信息
 */
@SuppressWarnings("serial")
public class CustomerBaseInfoView extends VerticalLayout {
	// 用于判断该用户所属角色是否拥有编辑客户信息的权限 
	private static final String TASK_MANAGEMENT_EDIT_CUSTOMER = "task_management&edit_customer";
	
	private boolean hasEditAuthority = false; 				// 判断当前用户是否拥有编辑客户信息的权限
	private ArrayList<String> ownBusinessModels;			// 当前登陆用户目前使用的角色类型所对应的所有权限
	
	private Panel customerInfoPanel;						// 用于存放Form 的面板
	private CustomerBaseInfoEditorForm customerBaseInfoEditorForm;	// 客户详细信息显示表
	
	private Set<Telephone> telephones;						// 客户的所有电话号码，用于作为自定义手机组件的数据源
	private BeanItem<CustomerResource> emptyCustomerItem;	// From 的数据源
	private BeanItem<CustomerResource> newCustomerItem;		// From 的数据源
	private CustomerResourceService customerResourceService;// 客户资源服务器

	private User loginUser;									// 当前登陆用户
	private RoleType roleType;								// 当前登陆用户登陆时使用的角色类型
	private CustomerResource customerResource;
	
	public CustomerBaseInfoView(User loginUser, RoleType roleType) {
		this.loginUser = loginUser;
		this.roleType = roleType;
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		customerResource = new CustomerResource();
		emptyCustomerItem = new BeanItem<CustomerResource>(customerResource);

		// 获取当前登陆用户所有权限
		ownBusinessModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			} else if(role.getType().equals(RoleType.manager)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			}
		}
		
		// 判断当前登陆用户是否可以编辑客户信息
		if(ownBusinessModels.contains(TASK_MANAGEMENT_EDIT_CUSTOMER) || !RoleType.csr.equals(roleType)) {
			hasEditAuthority = true;
		}

		customerInfoPanel = new Panel("客户基础信息");
		customerInfoPanel.setIcon(ResourceDataCsr.customer_info_16_ico);
		customerInfoPanel.addStyleName("light");
		customerInfoPanel.setHeight("192px");
		this.addComponent(customerInfoPanel);
		
		customerBaseInfoEditorForm = new CustomerBaseInfoEditorForm(loginUser, hasEditAuthority);
		customerBaseInfoEditorForm.setFormFieldFactory(new CustomerFeildFactory());
		customerBaseInfoEditorForm.setItemDataSource(emptyCustomerItem);
		customerBaseInfoEditorForm.setReadOnly(true);
		customerInfoPanel.addComponent(customerBaseInfoEditorForm);
	}

	// 根据customerResourceTable 中的内容回显信息
	public void echoCustomerBaseInfo(CustomerResource customerResource) {
		if(customerResource != null && customerResourceService != null) {
			this.customerResource = customerResource;
			if(customerResource.getId() == null) {
				newCustomerItem = new BeanItem<CustomerResource>(customerResource);
			} else {
				customerResource = customerResourceService.get(customerResource.getId());
				newCustomerItem = new BeanItem<CustomerResource>(customerResource);
			}
			telephones = customerResource.getTelephones();
			customerBaseInfoEditorForm.setItemDataSource(newCustomerItem);
			customerBaseInfoEditorForm.setReadOnly(customerResource.getId() != null);
		} else {	// TODO 在目前的情况下，下面的情况根本不会发生
			telephones = null;
			this.customerResource = new CustomerResource();
			customerBaseInfoEditorForm.setItemDataSource(emptyCustomerItem);
			customerBaseInfoEditorForm.setReadOnly(true);
		}
	}
	
	private class CustomerFeildFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("name".equals(propertyId)) {
				TextField nameField = new TextField();
				nameField.setNullRepresentation("");
				return nameField; 
			} else if("birthday".equals(propertyId)) {
				PopupDateField birthday = new PopupDateField();
				birthday.setResolution(PopupDateField.RESOLUTION_DAY);
				birthday.setInputPrompt("如:2012-08-18");
				birthday.setDateFormat("yyyy-MM-dd");
				birthday.setValidationVisible(false);
				birthday.setImmediate(true);
				return birthday;
			} else if("sex".equals(propertyId)) {
				OptionGroup genderSelector = new OptionGroup();
				genderSelector.addStyleName("twocolchb");
				genderSelector.addStyleName("myopacity");
				genderSelector.addItem("男");
				genderSelector.addItem("女");
				return genderSelector;
			} else if("id".equals(propertyId)) {
				TextField idField = new TextField();
				idField.setNullRepresentation("");
				return idField; 
			} else if("company".equals(propertyId)) {
				TextField companyField = new TextField();
				companyField.setNullRepresentation("");
				return companyField;
			} else if("telephones".equals(propertyId)) {
				if(!RoleType.csr.equals(roleType)) {
					return new PhoneNumberComponentToForm(loginUser, roleType, customerResource, ownBusinessModels);
				} else {
					DialComponentToForm dialComponentToForm = new DialComponentToForm(telephones, 
							hasEditAuthority, loginUser, roleType, ownBusinessModels, CustomerBaseInfoView.this);
					dialComponentToForm.setCustomerResource(customerResource);
					return dialComponentToForm;
				}
			}
			return null;
		}
	}

	/**
	 * 获取客户基本信息编辑表单 Form
	 * @return
	 */
	public CustomerBaseInfoEditorForm getCustomerBaseInfoEditorForm() {
		return customerBaseInfoEditorForm;
	}

	/**
	 * 设置客户基本信息显示面板的高度
	 * @param height	高度
	 */
	public void setCustomerInfoPanelHeight(String height) {
		customerInfoPanel.setHeight(height);
	}

	/**
	 * 供呼出弹窗使用等组件使用
	 * 		设置拥有数据源CustomerResource 的表格的 上级组件（如MyTaskTabView、MyServiceRecordTabView,ProprietaryCustomersTabView等）
	 * @param sourceTableVLayout
	 */
	public void setEchoModifyByReflect(VerticalLayout sourceTableVLayout) {
		customerBaseInfoEditorForm.setSourceTableVLayout(sourceTableVLayout);
	}
	
	/**
	 * 供呼入弹窗等组件使用
	 * 		用于当Form 的数据源在数据库中不存在时，当保存了Form 的数据源后，则将保存的CustomerResource 对象传到窗口中其他组件中去
	 * @param incomingDialTabView
	 */
	public void setIncomingDialTabView(IncomingDialTabView incomingDialTabView) {
		customerBaseInfoEditorForm.setIncomingDialTabView(incomingDialTabView);
	}
	
	/**
	 * 各个模块的呼出窗口
	 * @param outgoingPopupWindow
	 */
	public void setOutgoingPopupWindow(Window outgoingPopupWindow) {
		customerBaseInfoEditorForm.setOutgoingPopupWindow(outgoingPopupWindow);
	}
	
}
