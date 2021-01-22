package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.ResourceManageService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.csr.workarea.myresource.AddCustomerResourceWindow;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * @author chao
 * 
 */
@SuppressWarnings("serial")
public class ResourceManage_old_lxy extends VerticalLayout implements
		ValueChangeListener, ClickListener {
	
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Notification warning_notification; // 错误警告提示信息

	private ResourceManageService manageService = SpringContextHolder
			.getBean("resourceManageService");
	private UserService userService = SpringContextHolder
			.getBean("userService");

	private User user = SpringContextHolder.getLoginUser();

	private Domain domain = SpringContextHolder.getDomain();

	private Integer[] screenResolution;

	private CustomerResourceBatchService resourceBatchService = SpringContextHolder
			.getBean("customerResourceBatchService");
	private DepartmentService departmentService = SpringContextHolder
			.getBean("departmentService");

	private GridLayout gridLayout;

	private VerticalLayout verticalLayout;

	private HorizontalLayout horizontalLayout;

	private HorizontalLayout buttonLayout;

	private Label nameLabel;
	private TextField nameField;
	private Label genderLabel;
	private ComboBox genderBox;
	private Label callCount;
	private TextField callCountField;
	private Label customerIdLabel; // jrh 客户编号组件标题标签
	private TextField customerId_tf; // jrh 客户编号输入文本框
	private Label importDateLabel;
	private PopupDateField importDateField;
	private Label telphoneLabel;
	private TextField telphoneField;
	private Label companyLabel;
	private ComboBox companyBox;
	private Label provinceLabel;
	private ComboBox provinceBox;
	private Label cityLabel;
	private ComboBox cityBox;
	private Label ageLabel;
	private TextField fromField;
	private Label toLabel;
	private TextField toField;
	private Label batchLabel;
	private ComboBox batchBox;
	private Label customerLevelLabel;
	private ComboBox customerLevelBox;
	private Label projectLabel;
	private ComboBox projectBox;
	private Label customerRecordStateLabel;
	private ComboBox customerRecordStateBox;
	private Button selectButton;
	private Button reselectButton;
	private ComboBox selectBox;
	private Button deleteButton;
	private Button createBathButton;
	private Button addCustomerResource;
	private AddCustomerResourceWindow addCustomerResourceWindow;// 资源添加界面

	private ComboBox serviceRecordEmpnoComboBox;
	private ComboBox taskEmpnoComboBox;
	
	
	// 进度提醒

	private ProgressIndicator indicator;

	private Table table;

	private Window warnWindow;
	private VerticalLayout warnVerticalLayout;
	private Label warnLabel;
	private HorizontalLayout warnHorizontalLayout;
	private Button saveButton;
	private Button cancelButton;

	private Window bathWindow;
	private VerticalLayout bathVerticalLayout;
	private HorizontalLayout layout1;
	private HorizontalLayout layout2;
	private HorizontalLayout layout3;
	private Label bathName;
	private TextField bathField;
	private TextArea textArea;
	private Label remark;
	private Button saveBath;
	private Button cancelBath;

	private boolean isEncryptMobile = true; // 默认使用加密的电话号码和手机号
	
	private FlipOverTableComponent<CustomerResource> flipOverTableComponent;

	private String[] selectValue = new String[] { "全选当前页", "全选所有页" };

	private String[] columnHeaders = new String[] { "客户编号", "姓名", "电话", "性别", "生日",
			"导入者", "拨打次数", "最后拨打时间", /*"过期日期",*/ "导入时间", "客户经理", "资源等级", "默认地址",
			"公司", "批次" };
	private Object[] visibleColumns = new Object[] { "id", "name", "telephones", "sex",
			"birthday", "owner", "count", "lastDialDate", /*"expireDate",*/
			"importDate", "accountManager", "customerLevel", "defaultAddress",
			"company", "customerResourceBatches" };

	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息",
			ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息",
			ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息",
			ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录",
			ResourceDataCsr.customer_history_record_16_ico);

	private TabSheet customerInfoTabSheet; // 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow; // 客户所有相关信息查看窗口

	private ComboBox empNoComboBox;		// 工号选择框
	
	private String jpql;
	private String searchSql;
	private Long domainId;
	private List<String> gender;
	private List<MarketingProject> project;
	private List<Company> company=new ArrayList<Company>();
	private List<CustomerResourceBatch> bath;
	private List<CustomerLevel> customerLevel;
	private List<Province> province;
	private List<City> cities;
	private List<CustomerServiceRecordStatus> recordStatus;
	

	{
		gender = new ArrayList<String>();
		gender.add("男");
		gender.add("女");

		domainId = domain.getId();

		jpql = "select p from MarketingProject as p where p.domain.id = " + "'"
				+ domainId + "'";

		project = initList(MarketingProject.class, jpql);

//		jpql = "select c from Company as c where c.domain.id = " + "'"
//				+ domainId + "'";
//
//		company = initList(Company.class, jpql);

		jpql = "select b from CustomerResourceBatch as b where b.domain.id = "
				+ "'" + domainId + "'";

		bath = initList(CustomerResourceBatch.class, jpql);

		jpql = "select c from CustomerLevel as c where c.domain.id = " + "'"
				+ domainId + "'";

		customerLevel = initList(CustomerLevel.class, jpql);

		jpql = "select p from Province as p";

		province = initList(Province.class, jpql);

	}

	public ResourceManage_old_lxy() {
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);

		screenResolution = SpringContextHolder.getScreenResolution();

		createCustomerInfoWindow();

		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);

		warning_notification = new Notification("",
				Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);

		gridLayout = new GridLayout(5, 4);
		gridLayout.setSpacing(true);

		this.addComponent(gridLayout);

		verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		verticalLayout.setSpacing(true);
		this.addComponent(verticalLayout);
		this.setExpandRatio(verticalLayout, 1);

		table = createFormatColumnTable();
		table.setSizeFull();
		table.setImmediate(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.setStyleName("striped");
		table.setSelectable(true);
		table.setMultiSelect(true);
		// jrh
		table.addGeneratedColumn("telephones", new TelephonesColumnGenerator());
		verticalLayout.addComponent(table);
		verticalLayout.setExpandRatio(table, 1);

		addActionToTable(table);

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		verticalLayout.addComponent(horizontalLayout);

		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		horizontalLayout.addComponent(buttonLayout);
		horizontalLayout.setComponentAlignment(buttonLayout,
				Alignment.BOTTOM_LEFT);

		selectBox = new ComboBox();
		selectBox.setWidth("100");
		selectBox.setInputPrompt("---请选择---");
		selectBox.addListener(this);
		for (int i = 0; i < selectValue.length; i++) {
			selectBox.addItem(selectValue[i]);
		}
		buttonLayout.addComponent(selectBox);

		deleteButton = new Button("删除");
		deleteButton.addListener((ClickListener) this);
		buttonLayout.addComponent(deleteButton);
		buttonLayout.setComponentAlignment(deleteButton, Alignment.BOTTOM_LEFT);

		createBathButton = new Button("创建批次");
		createBathButton.addListener((ClickListener) this);
		buttonLayout.addComponent(createBathButton);
		buttonLayout.setComponentAlignment(createBathButton,
				Alignment.BOTTOM_LEFT);

		addCustomerResource = new Button("添加资源");
		addCustomerResource.addListener((ClickListener) this);
		buttonLayout.addComponent(addCustomerResource);
		buttonLayout.setComponentAlignment(addCustomerResource,
				Alignment.BOTTOM_LEFT);

		addFlipOverTableComponent(horizontalLayout);

		batchLabel = new Label("批次名称：");
		batchBox = new ComboBox();
		batchBox.setWidth("150px");
		batchBox.addListener(this);
		batchBox.setNullSelectionAllowed(true);
		gridLayout.addComponent(
				getHorizontalLayout(batchLabel, batchBox, bath,
						CustomerResourceBatch.class), 0, 0);

		projectLabel = new Label("项目名称：");
		projectBox = new ComboBox();
		projectBox.setWidth("150px");
		projectBox.addListener(this);
		gridLayout.addComponent(
				getHorizontalLayout(projectLabel, projectBox, project,
						MarketingProject.class), 1, 0);

		customerRecordStateLabel = new Label("客服记录状态：");
		customerRecordStateBox = new ComboBox();
		customerRecordStateBox.setWidth("150px");
		customerRecordStateBox.setEnabled(false);
		gridLayout.addComponent(
				getHorizontalLayout(customerRecordStateLabel,
						customerRecordStateBox, null, null), 2, 0);

		customerLevelLabel = new Label("客&nbsp;&nbsp;户&nbsp;&nbsp;等&nbsp;&nbsp;级：",Label.CONTENT_XHTML);
		customerLevelBox = new ComboBox();
		customerLevelBox.setWidth("153px");
		gridLayout.addComponent(
				getHorizontalLayout(customerLevelLabel, customerLevelBox,
						customerLevel, CustomerLevel.class), 3, 0);

		importDateLabel = new Label("导入日期：");
		importDateField = new PopupDateField();
		// importDateField.setValue(new Date());
		importDateField.setWidth("150px");
		gridLayout.addComponent(
				getHorizontalLayout(importDateLabel, importDateField, null,
						null), 0, 1);

		//chenhb 20141022 不能按公司搜索，暂时处理，此处公司为空
		companyLabel = new Label("公司名称：");
		companyBox = new ComboBox();
		companyBox.setEnabled(false);
		companyBox.addListener(this);
		companyBox.setWidth("150px");
		gridLayout.addComponent(
				getHorizontalLayout(companyLabel, companyBox, company,
						Company.class), 1, 1);

		provinceLabel = new Label("省&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;份：",Label.CONTENT_XHTML);
		provinceBox = new ComboBox();
		provinceBox.setWidth("150px");
		provinceBox.addListener(this);
		gridLayout.addComponent(
				getHorizontalLayout(provinceLabel, provinceBox, province,
						Province.class), 2, 1);

		cityLabel = new Label("市&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;区：",Label.CONTENT_XHTML);
		cityBox = new ComboBox();
		cityBox.setWidth("155px");
		cityBox.setEnabled(false);
		cityBox.setDescription("请先选择您所要查询的省份！");
		gridLayout.addComponent(
				getHorizontalLayout(cityLabel, cityBox, null, null), 3, 1);

		nameLabel = new Label("姓&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;名：",Label.CONTENT_XHTML);
		nameField = new TextField();
		nameField.setWidth("150px");
		gridLayout.addComponent(
				getHorizontalLayout(nameLabel, nameField, null, null), 0, 2);

		genderLabel = new Label("性&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;别：",Label.CONTENT_XHTML);
		genderBox = new ComboBox();
		genderBox.setWidth("150px");
		for (String str : gender) {
			genderBox.addItem(str);
		}
		gridLayout.addComponent(
				getHorizontalLayout(genderLabel, genderBox, null, null), 1, 2);

		ageLabel = new Label("年&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;龄&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;段：",Label.CONTENT_XHTML);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		fromField = new TextField();
		fromField.setWidth("72px");
		toLabel = new Label("-");
		toField = new TextField();
		toField.setWidth("72px");
		horizontalLayout.addComponent(fromField);
		horizontalLayout.addComponent(toLabel);
		horizontalLayout.addComponent(toField);
		gridLayout.addComponent(
				getHorizontalLayout(ageLabel, horizontalLayout, null, null), 2,
				2);
		

		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : SpringContextHolder.getLoginUser().getRoles()) {
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
		
		//chb add 
		//客服记录坐席
		HorizontalLayout serviceRecordLayout=new HorizontalLayout();
		serviceRecordLayout.addComponent(new Label("客服记录坐席："));
		serviceRecordEmpnoComboBox = buildEmpNoComboBox(allGovernedDeptIds);
		serviceRecordLayout.addComponent(serviceRecordEmpnoComboBox);
		gridLayout.addComponent(serviceRecordLayout, 3,2);
		
		//任务坐席
		HorizontalLayout taskEmpnoLayout=new HorizontalLayout();
		taskEmpnoLayout.addComponent(new Label("持有任务坐席："));
		taskEmpnoComboBox = buildEmpNoComboBox(allGovernedDeptIds);
		taskEmpnoLayout.addComponent(taskEmpnoComboBox);
		gridLayout.addComponent(taskEmpnoLayout, 3,3);
		
		telphoneLabel = new Label("电话号码：");
		telphoneField = new TextField();
		telphoneField.setWidth("150px");
		gridLayout.addComponent(
				getHorizontalLayout(telphoneLabel, telphoneField, null, null),
				0, 3);

		callCount = new Label("呼叫次数：");
		callCountField = new TextField();
		callCountField.setWidth("150px");
		gridLayout.addComponent(
				getHorizontalLayout(callCount, callCountField, null, null), 1,
				3);

		customerIdLabel = new Label("客&nbsp;&nbsp;户&nbsp;&nbsp;编&nbsp;&nbsp;&nbsp;号：",Label.CONTENT_XHTML);
		customerId_tf = new TextField();
		customerId_tf.setWidth("150px");
		customerId_tf
				.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		gridLayout
				.addComponent(
						getHorizontalLayout(customerIdLabel, customerId_tf,
								null, null), 2, 3);

		selectButton = new Button("查询");
		selectButton.addListener((ClickListener) this);
		gridLayout.addComponent(selectButton, 4, 0);

		reselectButton = new Button("重选");
		reselectButton.addListener((ClickListener) this);
		gridLayout.addComponent(reselectButton, 4, 1);

	}

	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("telephones")) {
				CustomerResource resource = (CustomerResource) itemId;
				Set<Telephone> telephones = resource.getTelephones();
				return new TelephoneNumberInTableLabelStyle(telephones, isEncryptMobile);
			} 
			return null;
		}
	}

	public void addFlipOverTableComponent(HorizontalLayout horizontalLayout) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		// String current_date = dateFormat.format(date);
		Calendar calendar = dateFormat.getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, +1);
		// String nextDate = dateFormat.format(calendar.getTime());

		// 创建初始化sql
		String searchSql = "select customerResource from CustomerResource as customerResource where customerResource.importDate = '2113-08-14'";

		String countSql = "select count(customerResource) from CustomerResource as customerResource where customerResource.importDate = '2113-08-14'";

		flipOverTableComponent = new FlipOverTableComponent<CustomerResource>(
				CustomerResource.class, manageService, table, searchSql,
				countSql, null);
		horizontalLayout.addComponent(flipOverTableComponent);
		horizontalLayout.setComponentAlignment(flipOverTableComponent,
				Alignment.BOTTOM_RIGHT);

		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		setTablePageLength();
	}

	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if (screenResolution[1] == 768) {
			table.setPageLength(16);
			flipOverTableComponent.setPageLength(16, true);
		} else if (screenResolution[1] == 900) {
			table.setPageLength(23);
			flipOverTableComponent.setPageLength(23, true);
		} else if (screenResolution[1] == 1050) {
			table.setPageLength(30);
			flipOverTableComponent.setPageLength(30, true);
		} else if (screenResolution[1] == 1080) {
			table.setPageLength(34);
			flipOverTableComponent.setPageLength(34, true);
		}
	}

	/**
	 * 为指定任务表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				table.setValue(null);
				table.select(target);

				CustomerResource customerResource = null;

				table.getApplication().getMainWindow()
						.removeWindow(customerAllInfoWindow);

				table.getApplication().getMainWindow()
						.addWindow(customerAllInfoWindow);

				Set<CustomerResource> resources = (Set<CustomerResource>) table
						.getValue();

				Iterator<CustomerResource> iterator = resources.iterator();
				while (iterator.hasNext()) {
					customerResource = iterator.next();

					break;
				}
				customerAllInfoWindow.initCustomerResource(customerResource);

				if (action == BASEINFO) {
					customerAllInfoWindow
							.echoCustomerBaseInfo(customerResource);
					customerInfoTabSheet.setSelectedTab(0);
				} else if (action == DESCRIPTIONINFO) {
					customerAllInfoWindow
							.echoCustomerDescription(customerResource);
					customerInfoTabSheet.setSelectedTab(1);
				} else if (action == ADDRESSINFO) {
					customerAllInfoWindow.echoCustomerAddress(customerResource);
					customerInfoTabSheet.setSelectedTab(2);
				} else if (action == HISTORYRECORD) {
					customerAllInfoWindow.echoHistoryRecord(customerResource);
					customerInfoTabSheet.setSelectedTab(3);
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				if (target != null) {
					return new Action[] { BASEINFO, DESCRIPTIONINFO,
							ADDRESSINFO, HISTORYRECORD };
				}
				return null;
			}
		});
	}

	/**
	 * 创建客户信息查看窗口及相关组件
	 */
	private void createCustomerInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.manager);
		customerAllInfoWindow.setResizable(false);
		customerAllInfoWindow.setEchoModifyByReflect(this);

		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}

	/**
	 * 供 CustomerBaseInfoEditorForm 调用 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		flipOverTableComponent.refreshInCurrentPage();
	}

	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			@SuppressWarnings("unchecked")
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (property.getValue() == null) {
					return "";
				} else if ((property.getType() == Date.class)) {

					return dateFormat.format((Date) property.getValue());
				} else if ("customerResourceBatches".equals(colId)) {
					Set<CustomerResourceBatch> batches = (Set<CustomerResourceBatch>) property
							.getValue();
					StringBuffer sb = new StringBuffer();
					for (CustomerResourceBatch batch : batches) {
						sb.append(batch.getBatchName());
						sb.append(",");
					}
					if (!sb.toString().equals("")) {

						String batchNames = sb.toString().substring(0,
								sb.toString().length() - 1);
						return batchNames;
					}

				}

				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	public <T> List<T> initList(Class<T> entityClass, String jpql) {

		return manageService.getEntity(entityClass, jpql);

	}

	public <T> HorizontalLayout getHorizontalLayout(Component component1,
			Component component2, List<T> list, Class<T> type) {

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);

		if (component2 instanceof ComboBox) {

			((ComboBox) component2).setNullSelectionAllowed(true);

			if (list != null) {

				bindValue(component2, list, type);
			}

		} else if (component2 instanceof PopupDateField) {
			((DateField) component2).setDateFormat("yyyy-MM-dd");
			((DateField) component2)
					.setResolution(PopupDateField.RESOLUTION_DAY);
		}

		horizontalLayout.addComponent(component1);
		horizontalLayout.addComponent(component2);

		return horizontalLayout;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		String value = null;
		String jpql;
		Long id = null;
		if (selectBox.getValue() != null
				&& selectBox.getValue().equals("全选当前页")) {

			selectCurrent();

		} else if (selectBox.getValue() != null
				&& selectBox.getValue().equals("全选所有页")) {

			selectAll();
		}

		if (event.getProperty() == provinceBox) {

			if (provinceBox.getValue() == null) {
				cityBox.removeAllItems();
				cityBox.setEnabled(false);
				cityBox.setDescription("请先选择您所要查询的省份！");
				return;
			}

			value = provinceBox.getValue().toString();

			cityBox.setEnabled(true);
			cityBox.setDescription(null);
			cityBox.removeAllItems();
			jpql = "select c from City as c,Province as p where p.id = c.province.id and p.name = "
					+ "'" + value + "'";
			cities = initList(City.class, jpql);
			bindComponent(cityBox, City.class, jpql);

		} else if (event.getProperty() == batchBox) {

			if (batchBox.getValue() == null) {

				return;
			}
			value = batchBox.getValue().toString();

			if (projectBox.getValue() == null) {

				Iterator<CustomerResourceBatch> iterator = bath.iterator();
				while (iterator.hasNext()) {
					CustomerResourceBatch batch = iterator.next();
					if (batch.getBatchName().equals(value)) {
						id = batch.getId();
					}
				}
				projectBox.removeAllItems();

				jpql = "select p from MarketingProject as p,p.batches as b "
						+ "where b.id = " + id + " and p.domain.id = " + "'"
						+ domainId + "'";
				bindComponent(projectBox, MarketingProject.class, jpql);
			}
		} else if (event.getProperty() == projectBox) {

			if (projectBox.getValue() == null) {

				return;
			}
			value = projectBox.getValue().toString();

			if (batchBox.getValue() == null) {

				Iterator<MarketingProject> iterator = project.iterator();
				while (iterator.hasNext()) {
					MarketingProject project = iterator.next();
					if (project.getProjectName().equals(value)) {
						id = project.getId();
					}
				}

				batchBox.removeAllItems();
				jpql = "select b from MarketingProject as p,p.batches as b "
						+ "where p.id = " + id + " and p.domain.id = " + "'"
						+ domainId + "'";
				bindComponent(batchBox, CustomerResourceBatch.class, jpql);
			}

			customerRecordStateBox.setEnabled(true);
			customerRecordStateBox.removeAllItems();
			jpql = "select s from CustomerServiceRecord as r,r.marketingProject as p,r.serviceRecordStatus as s "
					+ "where p.id = "
					+ id
					+ " and r.domain.id = "
					+ "'"
					+ domainId + "'";
			recordStatus = initList(CustomerServiceRecordStatus.class, jpql);
			bindComponent(customerRecordStateBox,
					CustomerServiceRecordStatus.class, jpql);

		}

	}

	public <T> void bindComponent(Component component, Class<T> entityClass,
			String jpql) {

		List<T> list = manageService.getEntity(entityClass, jpql);

		bindValue(component, list, entityClass);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void bindValue(Component component, List<T> list, Class type) {

		BeanItemContainer<T> container = new BeanItemContainer<T>(type);

		Iterator<T> iterator = list.iterator();

		if (component instanceof ComboBox) {

			while (iterator.hasNext()) {
				T entity = iterator.next();
				container.addBean(entity);
			}

			((ComboBox) component).setContainerDataSource(container);
		} else if (component instanceof Table) {

			while (iterator.hasNext()) {
				T entity = iterator.next();
				container.addBean(entity);
			}

			((Table) component).setContainerDataSource(container);
			((Table) component).setVisibleColumns(visibleColumns);
			((Table) component).setColumnHeaders(columnHeaders);
			((Table) component).setStyleName("striped");
			((Table) component).setSelectable(true);
			((Table) component).setMultiSelect(true);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public void buttonClick(ClickEvent event) {

		Button button = event.getButton();
		if (button == reselectButton) {

			reselect();// 重选

		} else if (button == selectButton) {

			select();// 查询

		} else if (button == createBathButton) {
			if(table.getContainerDataSource().size() == 0) {  	// jrh 添加  
				table.getApplication().getMainWindow().showNotification("当前没有查询到的资源，不能创建批次！", Notification.TYPE_WARNING_MESSAGE);
			} else {
				createBath();// 创建批次
			}

		} else if (button == addCustomerResource) {

			addCustomerResource();// 添加资源

		} else if (button == deleteButton) {

			final Set<CustomerResource> resources = (Set<CustomerResource>) table
					.getValue();
			if (resources.size() == 0) {
				event.getComponent().getApplication().getMainWindow()
						.showNotification("请先选择资源");

				return;
			}

			warnWindow = new Window();
			warnWindow.center();
			warnWindow.setModal(true);
			warnWindow.setWidth("400");
			warnWindow.setHeight("250");

			this.getApplication().getMainWindow().addWindow(warnWindow);

			warnVerticalLayout = new VerticalLayout();
			warnVerticalLayout.setSizeFull();
			warnVerticalLayout.setMargin(true);
			warnVerticalLayout.setSpacing(true);

			warnWindow.setContent(warnVerticalLayout);

			warnLabel = new Label(
					"<h1>删除资源时，会删除对应的,<span style=\"color:red\">资源描述信息、客服记录、项目任务</span> 确定删除?</h1>",
					Label.CONTENT_XHTML);

			warnVerticalLayout.addComponent(warnLabel);

			indicator = new ProgressIndicator();
			indicator.setVisible(false);
			warnVerticalLayout.addComponent(indicator);

			warnHorizontalLayout = new HorizontalLayout();
			warnHorizontalLayout.setWidth("100%");
			warnHorizontalLayout.setMargin(true);
			warnHorizontalLayout.setSpacing(true);
			warnVerticalLayout.addComponent(warnHorizontalLayout);

			cancelButton = new Button("取消");
			cancelButton.addListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {

					event.getComponent().getApplication().getMainWindow()
							.removeWindow(warnWindow);
					warnWindow.removeAllComponents();
					return;

				}
			});
			warnHorizontalLayout.addComponent(cancelButton);
			warnHorizontalLayout.setComponentAlignment(cancelButton,
					Alignment.BOTTOM_LEFT);
			saveButton = new Button("确定");
			saveButton.addListener(new ClickListener() {

				@Override
				public void buttonClick(final ClickEvent event) {

					indicator.setVisible(true);

					new Thread(new Runnable() {

						@Override
						public void run() {
OperationLogUtil.simpleLog(user, "资源管理-删除资源："+resources.size()+"条");
							delete();// 删除

							event.getComponent().getApplication()
									.getMainWindow().removeWindow(warnWindow);
							warnWindow.removeAllComponents();
						}
					}).start();

				}
			});
			warnHorizontalLayout.addComponent(saveButton);
			warnHorizontalLayout.setComponentAlignment(saveButton,
					Alignment.BOTTOM_RIGHT);

		}

	}

	public void select() {
		
		
		if(!customerId_tf.isValid()) {
			customerId_tf.getApplication().getMainWindow().showNotification("客户编号只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		Long id = null;
		String value = null;
		boolean isQuery = false;
		StringBuffer selectJpql = new StringBuffer(
				"select customerResource from CustomerResource as customerResource");
		StringBuffer whereJpql = new StringBuffer(" where ");

		// 批次名称
		if (batchBox.getValue() != null) {
			isQuery = true;
			value = batchBox.getValue().toString();
			Iterator<CustomerResourceBatch> iterator = bath.iterator();
			while (iterator.hasNext()) {
				CustomerResourceBatch batch = iterator.next();
				if (batch.getBatchName().equals(value)) {
					id = batch.getId();
					break;
				}
			}

			whereJpql
					.append("customerResource.id in (select customerResource.id from CustomerResource as customerResource,customerResource.customerResourceBatches as customerResourceBatches where customerResourceBatches.id = "
							+ id + ") and ");

		}

		//通过客服记录查询
		if (serviceRecordEmpnoComboBox.getValue() != null) {
			User user=(User)serviceRecordEmpnoComboBox.getValue();
			if(user!=null && user.getId()!=null){
				whereJpql.append("customerResource.id in (select c.customerResource.id  from CustomerServiceRecord as c where c.creator.username = '"
					+ user.getUsername() + "') and ");
			}
		}
		
		//通过task查询
		if (taskEmpnoComboBox.getValue() != null) {
			User user=(User)taskEmpnoComboBox.getValue();
			if(user!=null && user.getId()!=null){
				whereJpql
				.append("customerResource.id in (select m.customerResource.id  from MarketingProjectTask as m where m.user.username = '"
						+ user.getUsername() + "') and ");
			}
			
		}

		// 客户等级
		if (customerLevelBox.getValue() != null) {
			isQuery = true;
			value = customerLevelBox.getValue().toString();
			Iterator<CustomerLevel> iterator = customerLevel.iterator();
			while (iterator.hasNext()) {
				CustomerLevel level = iterator.next();
				if (level.getLevelName().equals(value)) {
					id = level.getId();
					break;
				}
			}

			whereJpql.append("customerResource.customerLevel.id = " + id
					+ " and ");

		}

		// 公司
		if (companyBox.getValue() != null) {
			isQuery = true;
			value = companyBox.getValue().toString();
			Iterator<Company> iterator = company.iterator();
			while (iterator.hasNext()) {
				Company company = iterator.next();
				if (company.getName().equals(value)) {
					id = company.getId();
					break;
				}
			}

			whereJpql.append("customerResource.company.id = " + id + " and ");
		}

		// 省份
		if (provinceBox.getValue() != null) {
			isQuery = true;
			value = provinceBox.getValue().toString();
			Iterator<Province> iterator = province.iterator();
			while (iterator.hasNext()) {
				Province province = iterator.next();
				if (province.getName().equals(value)) {
					id = province.getId();
					break;
				}
			}

			whereJpql.append("customerResource.defaultAddress.province.id = "
					+ id + " and ");
		}

		// 市区
		if (cityBox.getValue() != null) {
			isQuery = true;
			value = cityBox.getValue().toString();
			Iterator<City> iterator = cities.iterator();
			while (iterator.hasNext()) {
				City city = iterator.next();
				if (city.getName().equals(value)) {
					id = city.getId();
					break;
				}
			}

			whereJpql.append("customerResource.defaultAddress.city.id = " + id
					+ " and ");

		}

		// 姓名
		if (!nameField.getValue().equals("")) {
			isQuery = true;
			value = nameField.getValue().toString().trim();
			whereJpql.append("customerResource.name like " + "'%" + value
					+ "%'" + " and ");
		}

		// jrh 客户编号
		String customerId = StringUtils.trimToEmpty((String) customerId_tf
				.getValue());
		if (!"".equals(customerId)) {
			isQuery = true;
			whereJpql.append("customerResource.id = " + customerId
					+ " and ");
		}

		// 性别
		if (genderBox.getValue() != null) {
			isQuery = true;
			value = genderBox.getValue().toString().trim();
			whereJpql.append("customerResource.sex = " + "'" + value + "'"
					+ " and ");
		}
		// 呼叫次数
		if (!callCountField.getValue().equals("")) {
			isQuery = true;
			value = callCountField.getValue().toString().trim();
			whereJpql.append("customerResource.count = " + "'" + value + "'"
					+ " and ");
		}

		// 导入时间
		if (importDateField.getValue() != null) {
			isQuery = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			value = dateFormat.format(importDateField.getValue());
			Calendar calendar = dateFormat.getCalendar();
			calendar.add(Calendar.DAY_OF_MONTH, +1);
			String nextDate = dateFormat.format(calendar.getTime());
			whereJpql.append("customerResource.importDate >= " + "'" + value
					+ "'" + " and customerResource.importDate < " + "'"
					+ nextDate + "'" + " and ");
		}
		// 最小年龄
		if (fromField.getValue() != null && !fromField.getValue().equals("")) {
			isQuery = true;
			value = fromField.getValue().toString().trim();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.YEAR, -Integer.parseInt(value));

			whereJpql.append("customerResource.birthday <= " + "'"
					+ dateFormat.format(calendar.getTime()) + "' and ");
		}
		// 最大年龄
		if (toField.getValue() != null && !toField.getValue().equals("")) {
			isQuery = true;
			value = toField.getValue().toString().trim();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.YEAR, -Integer.parseInt(value));

			whereJpql.append("customerResource.birthday >= " + "'"
					+ dateFormat.format(calendar.getTime()) + "' and ");

		}

		// 项目名称
		if (projectBox.getValue() != null) {
			isQuery = true;
			value = projectBox.getValue().toString();
			Iterator<MarketingProject> iterator = project.iterator();
			while (iterator.hasNext()) {
				MarketingProject project = iterator.next();
				if (project.getProjectName().equals(value)) {
					id = project.getId();
					break;
				}
			}

			selectJpql
					.append(",customerResource.customerResourceBatches as customerResourceBatches");
			whereJpql
					.append("customerResourceBatches.id in (select batches.id from MarketingProject as marketingProject,marketingProject.batches as batches where marketingProject.id = "
							+ id + ") and ");

		}

		// 客服记录状态
		if (customerRecordStateBox.getValue() != null) {
			isQuery = true;
			value = customerRecordStateBox.getValue().toString();
			Iterator<CustomerServiceRecordStatus> iterator = recordStatus
					.iterator();
			while (iterator.hasNext()) {
				CustomerServiceRecordStatus recordStatus = iterator.next();
				if (recordStatus.getStatusName().equals(value)) {
					id = recordStatus.getId();
					break;
				}
			}

			whereJpql
					.append("customerResource.id in (select customerServiceRecord.customerResource.id from CustomerServiceRecord as customerServiceRecord where customerServiceRecord.serviceRecordStatus.id = "
							+ id + ") and ");

		}

		// 电话号码
		String telephoneNum = StringUtils.trimToEmpty((String) telphoneField.getValue());
		if (!"".equals(telephoneNum)) {
			isQuery = true;
			value = telphoneField.getValue().toString();

			whereJpql
					.append("customerResource.id in (select telephone.customerResource.id from Telephone as telephone where telephone.number like '%"
							+ value + "%') and ");

		}

		whereJpql.append("customerResource.domain.id = " + domainId + " and ");

		if (isQuery) {
			executeSelect(selectJpql, whereJpql);
		}

	}


	/**
	 * 创建客服工号的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildEmpNoComboBox(List<Long> allGovernedDeptIds) {
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User user=new User();
		user.setEmpNo("全部");
		userContainer.addBean(user);
		userContainer.addAll(userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId()));
		
		// 创建ComboBox
		empNoComboBox = new ComboBox();
		empNoComboBox.setContainerDataSource(userContainer);
		empNoComboBox.setItemCaptionPropertyId("empNo");
		empNoComboBox.setValue(user);
		empNoComboBox.setWidth("158px");
		empNoComboBox.setNullSelectionAllowed(false);
		return empNoComboBox;
	}
	
	public void selectCurrent() {

		BeanItemContainer<CustomerResource> beanItemContainer = flipOverTableComponent
				.getEntityContainer();

		Collection<CustomerResource> customerResources = beanItemContainer
				.getItemIds();

		table.setValue(customerResources);

	}

	public void selectAll() {

		if (searchSql != null) {

			List<CustomerResource> resources = manageService.getEntity(
					CustomerResource.class, searchSql);

			table.setValue(resources);

		}

	}

	public void createBath() {

		if (bathWindow == null) {
			bathWindow = new Window("创建批次");
			bathWindow.center();
			bathWindow.setWidth("300");
			bathWindow.setHeight("250");
		}

		this.getApplication().getMainWindow().addWindow(bathWindow);

		if (bathVerticalLayout == null) {
			bathVerticalLayout = new VerticalLayout();
			bathVerticalLayout.setSizeFull();
			bathVerticalLayout.setSpacing(true);
		}

		bathWindow.addComponent(bathVerticalLayout);

		if (layout1 == null) {
			layout1 = new HorizontalLayout();
			layout1.setSpacing(true);
		}

		bathVerticalLayout.addComponent(layout1);

		if (bathName == null) {
			bathName = new Label("批次名称");
		}
		if (bathField == null) {
			bathField = new TextField();
		}

		layout1.addComponent(bathName);
		layout1.addComponent(bathField);

		if (layout2 == null) {
			layout2 = new HorizontalLayout();
			layout2.setSpacing(true);
		}

		bathVerticalLayout.addComponent(layout2);

		if (remark == null) {
			remark = new Label("备注信息");
		}
		if (textArea == null) {
			textArea = new TextArea();
			textArea.setInputPrompt("请输入与批次相关的备注信息！");
		}

		layout2.addComponent(remark);
		layout2.addComponent(textArea);

		if (layout3 == null) {
			layout3 = new HorizontalLayout();
			layout3.setSpacing(true);
		}
		bathVerticalLayout.addComponent(layout3);

		if (saveBath == null) {
			saveBath = new Button("保存");
			saveBath.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {

					try {
						event.getComponent().getApplication().getMainWindow()
								.removeWindow(bathWindow);
						bathWindow.removeAllComponents();

						String batchName = StringUtils.trimToEmpty((String) bathField.getValue());
						String note = StringUtils.trimToEmpty((String) textArea.getValue());

						if("".equals(batchName)) {  	// jrh 添加 
							table.getApplication().getMainWindow().showNotification("批次名称不能为空！", Notification.TYPE_WARNING_MESSAGE);
							return;
						}
						// 批次
						CustomerResourceBatch customerResourceBatch = new CustomerResourceBatch();
						customerResourceBatch.setBatchName(batchName);
						customerResourceBatch.setCreateDate(new Date());
						customerResourceBatch.setDomain(domain);
						customerResourceBatch.setUser(user);
						customerResourceBatch.setNote(note);

						// 持久化批次
						CustomerResourceBatch batch = resourceBatchService.update(customerResourceBatch);
						
OperationLogUtil.simpleLog(user, "资源管理-创建批次："+batch.getBatchName());

						// 获被持久化后批次的id
						Long batchId = batch.getId();

						// 资源
						List<CustomerResource> customerResources = manageService.getEntity(CustomerResource.class, searchSql);

						// 向维护批资与资源关联关系的中间表插入数据
						for (CustomerResource customerResource : customerResources) {
							manageService
									.executeUpdate("insert into ec2_customer_resource_ec2_customer_resource_batch(customerresources_id,customerresourcebatches_id) values ("
											+ customerResource.getId()
											+ ","
											+ batchId + ")");
						}
						
						table.getApplication().getMainWindow().showNotification("批次创建成功！");
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("lc 在资源管理模块，创建批次出现异常：-->"+e.getMessage(), e);
						table.getApplication().getMainWindow().showNotification("批次创建失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
					}
					
				}
			});
		}
		layout3.addComponent(saveBath);
		layout3.setComponentAlignment(saveBath, Alignment.BOTTOM_LEFT);

		if (cancelBath == null) {
			cancelBath = new Button("取消");
			cancelBath.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					event.getComponent().getApplication().getMainWindow()
							.removeWindow(bathWindow);
					bathWindow.removeAllComponents();

				}
			});
		}
		layout3.addComponent(cancelBath);
		layout3.setComponentAlignment(cancelBath, Alignment.BOTTOM_RIGHT);

	}

	public void addCustomerResource() {

		showAddCustomerResourceForm();
	}

	/**
	 * 当用户点击的添加分机按钮时，buttonClick调用显示 添加 资源窗口
	 */
	private void showAddCustomerResourceForm() {
		if (addCustomerResourceWindow == null) {
			addCustomerResourceWindow = new AddCustomerResourceWindow(this);
		}
		this.getApplication().getMainWindow()
				.addWindow(addCustomerResourceWindow);
	}
	
	

	public void executeSelect(StringBuffer selectJpql, StringBuffer whereJpql) {

		String str = selectJpql.toString() + whereJpql.toString();

		searchSql = str.substring(0, (str.length() - 4));
		logger.info("资源管理查询jpql语句：    " + searchSql);


		logger.info("");
		logger.info(searchSql);
		logger.info("");
		
		String countSql = searchSql.replaceFirst("customerResource",
				"count\\(customerResource\\)");

		flipOverTableComponent.setSearchSql(searchSql);
		flipOverTableComponent.setCountSql(countSql);
		flipOverTableComponent.refreshInCurrentPage();

	}

	public void reselect() {

		nameField.setValue("");

		callCountField.setValue("");

		importDateField.setValue(null);

		telphoneField.setValue("");

		companyBox.removeAllItems();
		bindValue(companyBox, company, Company.class);

		provinceBox.removeAllItems();
		bindValue(provinceBox, province, Province.class);

		fromField.setValue("");

		toField.setValue("");

		batchBox.removeAllItems();
		bindValue(batchBox, bath, CustomerResourceBatch.class);

		customerLevelBox.removeAllItems();
		bindValue(customerLevelBox, customerLevel, CustomerLevel.class);

		projectBox.removeAllItems();
		bindValue(projectBox, project, MarketingProject.class);

		customerRecordStateBox.setEnabled(false);

		customerId_tf.setValue("");

		cityBox.removeAllItems();
		cityBox.setEnabled(false);

	}

	@SuppressWarnings("unchecked")
	public void delete() {

		List<Long> idList = new ArrayList<Long>();

		Set<CustomerResource> resources = (Set<CustomerResource>) table
				.getValue();

		for (CustomerResource customerResource : resources) {
			idList.add(customerResource.getId());
		}

		deleteById(idList);

	}

	public void deleteById(List<Long> idList) {

		long start = System.currentTimeMillis();
		long time = 0;

		long count = 0;

		for (Long id : idList) {

			manageService.deleteBatch(id);

			count++;

			indicator.setValue((float) count / idList.size());

		}

		time = System.currentTimeMillis() - start;
		this.getApplication()
				.getMainWindow()
				.showNotification(
						"成功删除" + idList.size() + "条资源      用时："
								+ ((float) time / 1000) + "秒");
		logger.info("成功删除: " + idList.size() + "条资源    用时："
				+ ((float) time / 1000) + "秒");

		// 刷新table
		flipOverTableComponent.refreshInCurrentPage();

	}

	/**
	 * 通过反射的方式刷新界面信息
	 * 
	 * @param refreshToFirstPage
	 */
	public void refreshTableInfoByReflect(boolean refreshToFirstPage) {
		if (refreshToFirstPage) {
			flipOverTableComponent.refreshToFirstPage();
		} else {
			flipOverTableComponent.refreshInCurrentPage();
		}
		table.setValue(null);
	}

}
