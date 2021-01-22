package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.lang.reflect.Method;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CompanyService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.csr.workarea.incoming.IncomingDialTabView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CustomerBaseInfoEditorForm extends Form implements ClickListener {
	// 日志工具
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object[] VISIBLE_PROPERTIES = new Object[] { "name", "birthday", "sex", "company", "telephones"};

	private GridLayout gridLayout;				// Form 表的的布局管理器
	private Button editButton;					// 客户资源信息 编辑 按钮
	private Button saveButton;					// 客户资源信息 保存 按钮
	private Button cancelButton;				// 客户资源信息 取消编辑 按钮
	private VerticalLayout operatorVLayout;		// 存放以上三个按钮
	private Label warningLabel;					// 错误信息提示标签

	private Domain domain;						// 当前登录用户所在域
	private User loginUser;						// 当前登陆用户
	private boolean isSaveSuccess;				// 标记是否保存成功
	private String oldCompanyName;				// 用户未修改信息之前公司的名称
	private CustomerResource customerResource; 	// 客户信息源
	
	private CompanyService companyService;		// 公司服务类
	private TelephoneService telephoneService;	// 电话号码服务类
	private CustomerResourceService customerResourceService;// 客户信息源服务类
	
	private VerticalLayout sourceTableVLayout; 			// 拥有该Form 表单中的数据源的Table组件的上层管理器 如 MyTaskTabView、MyServiceRecordTabView
	private IncomingDialTabView incomingDialTabView;	// 呼入窗口中的Tab页
	private Window outgoingPopupWindow;// 在cdr 点击号码呼出，或者通过直接输入号码呼出时的弹出窗口

	/**
	 * @param domain 			当前登陆者所属域
	 * @param hasEditAuthority	是否拥有编辑客户资源的权限
	 */
	public CustomerBaseInfoEditorForm(User loginUser, boolean hasEditAuthority) {
		// 初始化参数
		this.setInvalidCommitted(false); 		// 数据不合格就不提交
		this.setWriteThrough(false); 			// 不允许缓存
		this.setVisibleItemProperties(VISIBLE_PROPERTIES);
		
		this.loginUser = loginUser;
		this.domain = loginUser.getDomain();
		
		companyService = SpringContextHolder.getBean("companyService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		customerResourceService = SpringContextHolder.getBean("customerResourceService");

		// 创建 主布局管理器 gridLayout 
		gridLayout = new GridLayout(5, 5);
		gridLayout.setSpacing(true);
		gridLayout.addComponent(new Label("姓名："), 0, 0);
		gridLayout.addComponent(new Label("生日："), 2, 0);
		gridLayout.addComponent(new Label("性别："), 0, 1);
		gridLayout.addComponent(new Label("编号："), 2, 1);
		gridLayout.addComponent(new Label("公司："), 0, 2);
		gridLayout.addComponent(new Label("手机："), 0, 3);
		this.setLayout(gridLayout);

		// 根据当前用户权限拥有情况，创建各种按钮
		if(hasEditAuthority == true) {
			operatorVLayout = new VerticalLayout();
			operatorVLayout.setSpacing(true);
			
			editButton = new Button("编 辑", (ClickListener) this);
			saveButton = new Button("保 存", (ClickListener) this);
			cancelButton = new Button("取 消", (ClickListener) this);

			operatorVLayout.addComponent(editButton);
			operatorVLayout.addComponent(saveButton);
			operatorVLayout.addComponent(cancelButton);
			gridLayout.addComponent(operatorVLayout, 4, 0, 4, 3);
			gridLayout.setComponentAlignment(operatorVLayout, Alignment.MIDDLE_CENTER);
			
			warningLabel = new Label();
			warningLabel.addStyleName("invisible");
			warningLabel.addStyleName("warning");
			gridLayout.addComponent(warningLabel, 0, 4, 3, 4);
			gridLayout.setComponentAlignment(warningLabel, Alignment.MIDDLE_CENTER);
		}
	}

	@Override
	protected void attachField(Object propertyId, Field field) {
		field.setWidth("130px");
		if ("name".equals(propertyId)) {
			field.setCaption(null);
			gridLayout.addComponent(field, 1, 0);
		} else if ("birthday".equals(propertyId)) {
			field.setCaption(null);
			gridLayout.addComponent(field, 3, 0);
		} else if ("sex".equals(propertyId)) {
			field.setCaption(null);
			field.setWidth("100%");
			gridLayout.addComponent(field, 1, 1);
		} else if ("id".equals(propertyId)) {
			field.setCaption(null);
			field.setWidth("100%");
			gridLayout.addComponent(field, 3, 1);
		} else if ("company".equals(propertyId)) {
			field.setCaption(null);
			field.setWidth("100%");
			oldCompanyName = (String) field.getValue();	// 初始化原始公司名称
			if(oldCompanyName == null) {
				oldCompanyName = "";
			}
			gridLayout.addComponent(field, 1, 2, 3, 2);
		} else if ("telephones".equals(propertyId)) {
			field.setCaption(null);
			field.setWidth("100%");
			gridLayout.addComponent(field, 1, 3, 3, 3);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == editButton) {
			this.setReadOnly(false);
		} else if (source == saveButton) {
			isSaveSuccess = saveCustomerInfo();
			if(isSaveSuccess) {
				this.setReadOnly(true);
				warningLabel.addStyleName("invisible");
			}
		} else if (source == cancelButton) {
			// 如果当前客户资源没有存入数据库，则当用户点击取消保存的时候，则显示提示信息
			customerResource = ((BeanItem<CustomerResource>) this.getItemDataSource()).getBean();
			if(customerResource.getId() == null) {
				warningLabel.setValue("当前用户尚未存入数据库，如要保存，请先编辑！");
				warningLabel.removeStyleName("invisible");
			} else {
				warningLabel.addStyleName("invisible");
			}
			this.discard();
			this.setReadOnly(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean saveCustomerInfo() {
		if (!this.getField("birthday").isValid()) {
			warningLabel.setValue("日期格式填写有误，如2012-08-18！");
			warningLabel.removeStyleName("invisible");
			return false;
		}
		String currentCustomerName = (String) this.getField("name").getValue();
		Date currentCustomerBirthday = (Date) this.getField("birthday").getValue();
		String currentCustomerSex = (String) this.getField("sex").getValue();
		String currentCompanyName = (String) this.getField("company").getValue();
		if(currentCompanyName == null) {
			currentCompanyName = "";
		}

		customerResource = ((BeanItem<CustomerResource>) this.getItemDataSource()).getBean();
		customerResource.setName(currentCustomerName);
		customerResource.setBirthday(currentCustomerBirthday);
		customerResource.setSex(currentCustomerSex);

		// 处理公司更改问题
		if(!oldCompanyName.equals(currentCompanyName)) {
			Company company = companyService.getCompanyByName(currentCompanyName, domain.getId());
			if(company != null) {	// 如果该公司已经存在,则直接与该公司关联
				customerResource.setCompany(company);
			} else {				// 如果该公司 不 存在,则创建新并与之关联
				company = new Company();
				company.setName(currentCompanyName);
				company.setDomain(domain);
				company = companyService.update(company);
				customerResource.setCompany(company);
			}
		}
		
		// 如果在保存该用户在数据库中不存在，则需要先保存其对应的电话号码。然后再持久化本身
		if(customerResource.getId() == null) {
			customerResource.setOwner(loginUser);
			customerResource = customerResourceService.update(customerResource);
			
			for(Telephone telephone : customerResource.getTelephones()) {
				telephone.setCustomerResource(customerResource);
				telephone.setDomain(domain);
				telephone = telephoneService.update(telephone);
			}
		} else {
			customerResource = customerResourceService.update(customerResource);
		}
		
		// 如果是呼入事件的弹出窗口，且该用户在数据库中不存在，则重新回显呼入弹窗中各组件的数据源
		if(incomingDialTabView != null) {
			incomingDialTabView.echoInformations(customerResource);
		} else if(outgoingPopupWindow != null) {	// 在呼出窗口中修改了用户的基本信息，那么就需要回显信息
			try {
				Method method = outgoingPopupWindow.getClass().getMethod("echoInformations",CustomerResource.class);
				method.invoke(outgoingPopupWindow, customerResource);
			} catch (Exception e) {
				logger.error(e.getMessage()+" 修改客户基本信息后，使用反射更新当前弹出窗口中其它模块的客户信息时，出现异常!", e);
			}
		}
		
		// 如果是普通的呼出弹窗，则在修改客户资源信息后，需要回显其对应Tab 页中的信息，如回显MyTaskTabView 中的Task 表格显示信息
		if(sourceTableVLayout != null) {
			try {
				Method method = sourceTableVLayout.getClass().getMethod("echoTableInfoByReflect");
				method.invoke(sourceTableVLayout);
			} catch (Exception e) {
				logger.error(e.getMessage()+" 修改客户基本信息后，使用反射更新当前操作模块所属表格中客户信息时，出现异常!", e);
			}
		}
		
		return true;
	}
	
	/**
	 * 重写Form 的setReadOnly 方法，设置表格组件的只读属性
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		if (editButton != null) {
			editButton.setEnabled(readOnly);
			saveButton.setEnabled(!readOnly);
			cancelButton.setEnabled(!readOnly);
			this.getField("id").setReadOnly(true);
		}
	}
	
	@Override
	public void discard() {
		super.discard();
		if(!this.getField("birthday").isValid()){		// discard 对PopupDateField 不起作用
			this.getField("birthday").setValue(null);
		}
	}

	/**
	 * 设置拥有数据源CustomerResource 的表格的 上级组件（如MyTaskTabView、MyServiceRecordTabView等）
	 * @param sourceTableVLayout
	 */
	public void setSourceTableVLayout(VerticalLayout sourceTableVLayout) {
		this.sourceTableVLayout = sourceTableVLayout;
	}
	
	/**
	 * 呼入的情况
	 *	 用于当Form 的数据源在数据库中不存在时，当保存了Form 的数据源后，则将保存的CustomerResource 对象传到窗口中其他组件中去
	 * @param incomingDialTabView
	 */
	public void setIncomingDialTabView(IncomingDialTabView incomingDialTabView) {
		this.incomingDialTabView = incomingDialTabView;
	}

	/**
	 * 各个模块的呼出窗口
	 * @param outgoingPopupWindow
	 */
	public void setOutgoingPopupWindow(Window outgoingPopupWindow) {
		this.outgoingPopupWindow = outgoingPopupWindow;
	}

	/**
	 * 供其他组件直接调用保存客户信息
	 */
	public boolean checkAndSaveCustomerResourceInfo() {
		boolean isReadOnly = this.isReadOnly();
		if(!isReadOnly) {
			saveButton.click();
			return isSaveSuccess;
		} 
		return true;
	}
	
}
