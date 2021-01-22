package com.jiangyifen.ec2.ui.csr.workarea.myresource;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.AddressService;
import com.jiangyifen.ec2.service.eaoservice.CompanyService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 添加资源窗口
 * @author jrh
 */
@SuppressWarnings("serial")
public class AddCustomerResourceWindow extends Window implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object[] VISIBLE_PROPERTIES = new Object[] { "name", "birthday", "sex", "company", "telephones", "defaultAddress"};
	
	private final String[] COL_HEADERS = new String[] { "姓名", "生日", "性别", "公司", "电话", "地址"};
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	//Form输出
	private Form form;

	//保存按钮和取消按钮
	private Button save;
	private Button cancel;
	
	private Notification notification;				// 提示信息

	private Domain domain;
	private User loginUser;
	private CustomerResource customerResource;
//	private MyResourcesTabView myResourceTabView;

	private VerticalLayout sourceTableVLayout; 			// 拥有该Form 表单中的数据源的Table组件的上层管理器 如 MyResourcesTabView、ResourceManage

	private CompanyService companyService;			// 公司服务类
	private TelephoneService telephoneService;		// 电话号码服务类
	private CustomerResourceService customerResourceService; // 客户信息源服务类
	private AddressService addressService;			// 地址服务类
	
	public AddCustomerResourceWindow(VerticalLayout sourceTableVLayout) {
		this.center();
		this.setModal(true);
		this.setImmediate(true);
		this.setCaption("添加资源");
		this.sourceTableVLayout = sourceTableVLayout;

		notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		companyService = SpringContextHolder.getBean("companyService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		addressService = SpringContextHolder.getBean("addressService");

		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// 创建Form 表单
		createFormComponent(windowContent);
	}
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		customerResource = new CustomerResource();
		
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<CustomerResource>(customerResource), Arrays.asList(VISIBLE_PROPERTIES));
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			form.getField(VISIBLE_PROPERTIES[i]).setCaption(COL_HEADERS[i].toString()+"：");
		}
	}

	/**
	 * 创建表格组件
	 * @param windowContent
	 */
	private void createFormComponent(VerticalLayout windowContent) {
		form=new Form();
		form.setValidationVisible(false);
		form.setValidationVisibleOnCommit(true);
		form.setInvalidCommitted(false);
		form.setWriteThrough(false);
		form.setImmediate(true);
		form.addStyleName("chb");
		form.setFormFieldFactory(new CustomerFeildFactory());
		form.setFooter(creatFormFooterComponents());
		windowContent.addComponent(form);
	}

	/**
	 * Form 表单下方组建
	 * @return VerticalLayout 存放组件的布局管理器
	 */
	private HorizontalLayout creatFormFooterComponents() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("100%");

		// 保存按钮
		save = new Button("保存", this);
		save.setStyleName("default");
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);
		
		return buttonsLayout;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			boolean success = excuteSave();
			OperationLogUtil.simpleLog(loginUser, "添加资源："+customerResource.getName());
			if(success == true) {
				this.getApplication().getMainWindow().showNotification("添加资源成功！");
				this.getParent().removeWindow(this);
				if(sourceTableVLayout != null) {
					try {
						Method method = sourceTableVLayout.getClass().getMethod("refreshTableInfoByReflect", boolean.class);
						method.invoke(sourceTableVLayout, true);
					} catch (Exception e) {
						logger.error(e.getMessage()+" 修改客户基本信息后，使用反射更新当前操作模块所属表格中客户信息时，出现异常!", e);
					}
				}
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * 执行保存操作
	 * @return
	 */
	private boolean excuteSave() {
		try {
			if(!form.getField("telephones").isValid()) {
				notification.setCaption("电话号码格式填写错误！");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}
			form.commit();
		} catch (Exception e) {
			logger.info("CSR 添加资源时，资源各字段格式填写错误！");
			return false;
		} 
		int originalSize = customerResource.getTelephones().size();
		if(originalSize == 0) {
			notification.setCaption("电话号不能为空！");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}
		
		// 处理资源的电话号码
		List<Telephone> useablePhones = new ArrayList<Telephone>();
		CustomerResource existedResource = null;
		for(Telephone telephone : customerResource.getTelephones()) {
			Telephone existedTel = telephoneService.getByNumber(telephone.getNumber(), domain.getId());
			// 如果号码存在，则应检查使用号码的人是不是有价值的客户，如果不是则将其覆盖
			if(existedTel != null) {
				existedResource = existedTel.getCustomerResource();
				Date expiredDate = existedResource.getExpireDate();
				if(existedResource.getAccountManager() != null || expiredDate.after(new Date()) ) {	// 检查电话的拥有者是否处于有效期
					notification.setCaption("电话号"+telephone.getNumber()+" 已被有效客户使用，请修改号码后重试！");
					this.getApplication().getMainWindow().showNotification(notification);
					return false;
				} else {
					useablePhones.add(telephone);
				}
			} else {
				useablePhones.add(telephone);
			}
		}
		
		// 如果没有有效号码，则直接返回
		if(useablePhones.size() == 0) {
			notification.setCaption("没有可用的号码，请修改号码后重试！");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}
		
		// 处理资源的所属公司
		Company company = null;
		if(customerResource.getCompany() != null) {
			company = companyService.getCompanyByName(customerResource.getCompany().getName(), domain.getId());
			if(company != null) {	// 如果该公司已经存在,则直接与该公司关联
				customerResource.setCompany(company);
			}
		}
		
		// 如果是过期资源，则更新信息
		if(existedResource != null) {
			existedResource.getTelephones().addAll(useablePhones);
			existedResource.setName(customerResource.getName());
			Date birthDay = customerResource.getBirthday();
			if(birthDay != null) {
				existedResource.setBirthdayStr(dateFormat.format(birthDay));
			} 
			if(customerResource.getSex() != null) {
				existedResource.setSex(customerResource.getSex());
			} 
			if(company != null) {
				existedResource.setCompany(company);
			}
			existedResource.setOwner(loginUser);
			customerResource = customerResourceService.update(existedResource);
		} else {
			// 保存资源对象
			customerResource.setImportDate(new Date());
			Calendar cal = Calendar.getInstance();
			cal.set(1970, 0, 1, 0, 0, 0);
			customerResource.setExpireDate(cal.getTime());
			customerResource.setOwner(loginUser);
			customerResource.setDomain(domain);
			customerResource = customerResourceService.update(customerResource);
		}
		
		Address address = customerResource.getDefaultAddress(); 
		if(address != null) {
			address = addressService.getAddress(address.getId());
			address.setName(customerResource.getName());
			address.setCustomerResource(customerResource);
			addressService.updateAddress(address);
		}
		
		// 都是新号码，则添加号码
		for(Telephone telephone : useablePhones) {
			telephone = telephoneService.getByNumber(telephone.getNumber(), domain.getId());
			telephone.setCustomerResource(customerResource);
			telephoneService.update(telephone);
		}
		
		return true;
	}
	
	private class CustomerFeildFactory extends DefaultFieldFactory {
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("name".equals(propertyId)) {
				TextField nameField = new TextField();
				nameField.setNullRepresentation("");
				nameField.setWidth("200px");
				nameField.setRequired(true);
				nameField.setRequiredError("姓名不能为空！");
				return nameField; 
			} else if("birthday".equals(propertyId)) {
				PopupDateField birthday = new PopupDateField();
				birthday.setResolution(PopupDateField.RESOLUTION_DAY);
				birthday.setInputPrompt("如：2012-08-18");
				birthday.setDateFormat("yyyy-MM-dd");
				birthday.setValidationVisible(false);
				birthday.setImmediate(true);
				birthday.setWidth("200px");
				return birthday;
			} else if("sex".equals(propertyId)) {
				OptionGroup genderSelector = new OptionGroup();
				genderSelector.addStyleName("twocolchb");
				genderSelector.addStyleName("myopacity");
				genderSelector.addItem("男");
				genderSelector.addItem("女");
				genderSelector.setWidth("200px");
				return genderSelector;
			} else if("company".equals(propertyId)) {
				AddCompanyField companyField = new AddCompanyField();
				companyField.setWidth("200px");
				return companyField;
			} else if("telephones".equals(propertyId)) {
				AddTelephonesField telephonesField = new AddTelephonesField("200px", domain);
				return telephonesField;
			} else if("defaultAddress".equals(propertyId)) {
				AddAddressField addressField = new AddAddressField();
				addressField.setWidth("200px");
				return addressField;
			}
			return null;
		}
	}
	
}
