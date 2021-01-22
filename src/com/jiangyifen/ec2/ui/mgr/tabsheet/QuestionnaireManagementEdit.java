package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireEditService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.CustomerQuestionDownload;
import com.jiangyifen.ec2.ui.mgr.questionnaire.EditCustomerQuestionnaire;
import com.jiangyifen.ec2.ui.mgr.questionnaire.flip.QuestionFlipOverTable;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @author lxy
 * 
 * <br>模块内容：后台问卷调查管理编辑
 *
 */
public class QuestionnaireManagementEdit extends VerticalLayout implements Property.ValueChangeListener, Action.Handler, ClickListener  {
	
	private static final long serialVersionUID = 5429401624938789634L;
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
		
	private static final int PAGE_LENGTH = 10;
	// 右键组件
 	private final Action EditCustomer = new Action("编辑问卷答案", ResourceDataCsr.address_16_ico);
 	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// 检索
	
	private Button bt_search;					//所有按钮
	private Button bt_reset;
	private Label lb_msg;
	private Button bt_output_res;				//导出数据
	
	private String searchListJpql;				//检索列表Jpql
	private String searchCountJpql;				//检索总数Jpql
	private Button bt_edit_question;			// 编辑问卷答案
	
	
	private QuestionFlipOverTable	qflip;		//本页面分页组件
	private Table table;						//表格组件
	
	private VerticalLayout tablePageLayout;		//表格分页组件
	private HorizontalLayout musicLayout;	
	private HorizontalLayout buttonsLayout;		//底部按钮布局布局
	private HorizontalLayout playerLayout;		//底部录音布局
	
	private BeanItemContainer<Questionnaire> questionnaireContainer; 	// 问卷调查选择框数据源
	private ComboBox cb_questionnaire;
	private ComboBox cb_finish;
	private BeanItemContainer<User> userContainer; 	// 问卷调查选择框数据源
	private ComboBox cb_user;	
	private BeanItemContainer<MarketingProject> marketingProjectContainer; 	// 项目选择框数据源
	private ComboBox cb_marketingProject;
	private PopupDateField startStartTime;
	private PopupDateField startEndTime;
	private PopupDateField endStartTime;
	private PopupDateField endEndTime;
	
	private TextField tf_pnumber;
	private Label player;
	
	// 弹出窗口 只创建一次
	private EditCustomerQuestionnaire customerQuestionnaire;
	private CustomerQuestionDownload customerQuestionDownload;
 	// 业务组件
	private Long questionnaireId;
	private Long customerQuestionnaireId;
	
	private List<Long> allGovernedDeptIds;	// 当前用户所有管辖部门的id号
	private List<Department> allGovernedDept;//当前用户所有管辖部门

	private User loginUser;
	private Domain domain;									//EC域
	private boolean isEncryptMobile = true; 				// 默认使用加密的电话号码和手机号
  	private QuestionnaireService questionnaireService;	
  	private CustomerQuestionnaireEditService customerQuestionnaireEditService;
  	private QuestionService questionService;
  	private UserService userService;
  	private MarketingProjectService marketingProjectService;
  	private DepartmentService departmentService;
  	private TelephoneService telephoneService;
  	
  //录音基础路径
  	private String baseUrl=BaseUrlUtils.getBaseUrl();
  	
  	
 	/** 该类构造函数 */
	public QuestionnaireManagementEdit() {
		this.setSpacing(true);						// 设置边界等
		this.setMargin(false,true,true,true);
		this.setWidth("100%");	
		
		initBusinessAndSpring();			//	初始化业务与Spring组件
		initRoleBusines();
		createTableTopComponents();			// 	创建队列表格上方的组件（包括搜索组件及多项删除组件）
		createTablePageLayout();
		createTableButtonsComponents();
		createPlayerLayout();
	}
	
	private void initRoleBusines(){
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
				allGovernedDeptIds = new ArrayList<Long>();
				allGovernedDept = new ArrayList<Department>();
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
									allGovernedDept.add(dept);
								}
							}
						}
					}
				}
	}
	
	
	/** 初始化业务与Spring组件 */
	private void initBusinessAndSpring(){
		domain = SpringContextHolder.getDomain();
		loginUser  = SpringContextHolder.getLoginUser();
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);

		questionnaireService = SpringContextHolder.getBean("questionnaireService");
 		questionService = SpringContextHolder.getBean("questionService");
 		customerQuestionnaireEditService = SpringContextHolder.getBean("customerQuestionnaireEditService");
 		userService = SpringContextHolder.getBean("userService");
 		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
 		departmentService = SpringContextHolder.getBean("departmentService");
 		telephoneService = SpringContextHolder.getBean("telephoneService");
	}
	
	/** 创建队列表格上方的组件（包括搜索组件及多项删除组件） */
	private void createTableTopComponents() {
		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout1 = new HorizontalLayout();
		searchHLayout1.setSpacing(true);
		searchHLayout1.setMargin(true, false, false, false);
		this.addComponent(searchHLayout1);
		
		Label lb_1_1 = new Label("问卷标题<font color='red'><b>*</b></font>",Label.CONTENT_XHTML);
		lb_1_1.setWidth("-1px");
		searchHLayout1.addComponent(lb_1_1);
		
		List<Questionnaire> qels = questionnaireService.getAllByDomainId(domain.getId(), "E");
		questionnaireContainer = new BeanItemContainer<Questionnaire>(Questionnaire.class);
		questionnaireContainer.addAll(qels);
		 
		cb_questionnaire = new ComboBox();
 		cb_questionnaire.setImmediate(true);
		cb_questionnaire.setInputPrompt("请先选择问卷，点击检索,在导出数据!");
		cb_questionnaire.setWidth("320px");
		cb_questionnaire.setNullSelectionAllowed(false);
		cb_questionnaire.setItemCaptionPropertyId("mainTitle");		
		cb_questionnaire.setContainerDataSource(questionnaireContainer);
		searchHLayout1.addComponent(cb_questionnaire);
		
		Label lb_1_2 = new Label("完成状态",Label.CONTENT_XHTML);
		lb_1_2.setWidth("-1px");
		searchHLayout1.addComponent(lb_1_2);
		
		cb_finish = new ComboBox();
		cb_finish.setImmediate(true);
		cb_finish.setWidth("100px");
		cb_finish.addItem("start");
		cb_finish.addItem("end");
		cb_finish.setItemCaption("start", "开始");
		cb_finish.setItemCaption("end", "完成");
		searchHLayout1.addComponent(cb_finish);
		
		bt_search = new Button("搜索", this);
		bt_search.setImmediate(true);
		searchHLayout1.addComponent(bt_search);
		
		bt_reset = new Button("重置", this);
		bt_reset.setImmediate(true);
		searchHLayout1.addComponent(bt_reset);
		
		bt_output_res = new Button("导出数据", this);
		bt_output_res.setImmediate(true);
		bt_output_res.setWidth("96px");
		searchHLayout1.addComponent(bt_output_res);
		
		lb_msg = new Label(WorkUIUtils.fontColorHtmlString("[请先选择问卷，点击检索,再导出数据!]"),Label.CONTENT_XHTML);
		searchHLayout1.addComponent(lb_msg);
		
		
		HorizontalLayout searchHLayout2 = new HorizontalLayout();
		searchHLayout2.setSpacing(true);
		this.addComponent(searchHLayout2);
		
		Label lb_2_1 = new Label("开始时间&nbsp",Label.CONTENT_XHTML);
		lb_2_1.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_1);
		
		startStartTime = new PopupDateField();
		startStartTime.setWidth("153px");
		startStartTime.setImmediate(true);
		startStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startStartTime.setParseErrorMessage("时间格式不合法");
		startStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(startStartTime);
		Label lb_2_2 = new Label("-");
		lb_2_2.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_2);
		startEndTime = new PopupDateField();
		startEndTime.setWidth("153px");
		startEndTime.setImmediate(true);
		startEndTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startEndTime.setParseErrorMessage("时间格式不合法");
		startEndTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(startEndTime);
		
		Label lb_2_3 = new Label("结束时间");
		lb_2_3.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_3);
		
		endStartTime = new PopupDateField();
		endStartTime.setWidth("153px");
		endStartTime.setImmediate(true);
		endStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		endStartTime.setParseErrorMessage("时间格式不合法");
		endStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(endStartTime);
		Label lb_2_4 = new Label("-");
		lb_2_4.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_4);
		endEndTime = new PopupDateField();
		endEndTime.setWidth("153px");
		endEndTime.setImmediate(true);
		endEndTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		endEndTime.setParseErrorMessage("时间格式不合法");
		endEndTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(endEndTime);
		
		
		HorizontalLayout searchHLayout3 = new HorizontalLayout();
		searchHLayout3.setSpacing(true);
		this.addComponent(searchHLayout3);
		
		
		Label lb_3_1 = new Label("项目名称&nbsp",Label.CONTENT_XHTML);
		lb_3_1.setWidth("-1px");
		searchHLayout3.addComponent(lb_3_1);
		
		List<MarketingProject> mgpListBic = marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
		marketingProjectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		marketingProjectContainer.addAll(mgpListBic);
		cb_marketingProject = new ComboBox();
		cb_marketingProject.addListener((ValueChangeListener)this);
		cb_marketingProject.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS); 	//检索过滤模式
		cb_marketingProject.setImmediate(true);
		cb_marketingProject.setWidth("320px");
		cb_marketingProject.setNullSelectionAllowed(true);
		cb_marketingProject.setItemCaptionPropertyId("projectName");		
		cb_marketingProject.setContainerDataSource(marketingProjectContainer);
		searchHLayout3.addComponent(cb_marketingProject);
		
		Label lb_3_2 = new Label("话务员工号");
		lb_3_2.setWidth("-1px");
		searchHLayout3.addComponent(lb_3_2);
		
		userContainer = new BeanItemContainer<User>(User.class);
		List<User> userListBic = userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId());
 		User user = new User();
 		user.setEmpNo("全部");
		userContainer.addBean(user);
		userContainer.addAll(userListBic);
		cb_user = new ComboBox();
		cb_user.setWidth("150px");
		cb_user.setNullSelectionAllowed(false);
		cb_user.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS); 	//检索过滤模式
		cb_user.setContainerDataSource(userContainer);
		for(User csr : userContainer.getItemIds()) {
			String tmp = csr.getEmpNo();
			if(csr.getRealName() != null) {
				tmp = tmp +" - "+ csr.getRealName();
			}
			cb_user.setItemCaption(csr, tmp);
		}
		searchHLayout3.addComponent(cb_user);
		
		Label lb_3_3 = new Label("客户号码");
		lb_3_3.setWidth("-1px");
		searchHLayout3.addComponent(lb_3_3);
		
		tf_pnumber = new TextField();
		tf_pnumber.setWidth("110px");
		searchHLayout3.addComponent(tf_pnumber);
		
	}
	private void createTablePageLayout(){
		tablePageLayout = new VerticalLayout();
		tablePageLayout.setSizeFull();
		this.addComponent(tablePageLayout);
	}

	/** 创建分机表格显示区组件*/
	private void createQuestionnairTable() {
		table = new Table() {
			private static final long serialVersionUID = 4347052160778120067L;
			@Override
			protected String formatPropertyValue(Object rowId, Object columnId, Property property) {
				if(columnId.equals("客户号码")) {
					String cphone = "";
					if (null!= property && null != property.getValue()) {
						cphone = property.getValue().toString();
						if(isEncryptMobile) {
							cphone = telephoneService.encryptMobileNo(cphone);
						}
					}
					return cphone;
				}
				return super.formatPropertyValue(rowId, columnId, property);
			}
		};
		table.setWidth("100%");	
 		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.addActionHandler(this);
		table.setPageLength(PAGE_LENGTH);
		table.setColumnCollapsingAllowed(true);
		// 设置table列名
				//for (String str : names) {"","客户编号","","","问卷开始时间","问卷结束时间","问卷是否完成","话务员工号","话务员用户名","话务员姓名"
				//cqne.id,eu.id ,eu.username,eu.realname,eu.empno,ecr.id,ecr.name,tel.number,qne.maintitle,cqne.starttime,cqne.endtime,cqne.finish
		table.addContainerProperty("问卷编号", Long.class, null);
		table.addContainerProperty("话务员编号", Long.class, null);
		table.addContainerProperty("话务员账户", String.class, null);
		table.addContainerProperty("话务员真实姓名", String.class, null);
		table.addContainerProperty("话务员工号", String.class, null);
		table.addContainerProperty("客户编号", Long.class, null);
		table.addContainerProperty("客户姓名", String.class, null);
		table.addContainerProperty("客户号码", String.class, null);
		table.addContainerProperty("问卷名称", String.class, null);
		table.addContainerProperty("开始时间", String.class, null);
		table.addContainerProperty("结束时间", String.class, null);
		table.addContainerProperty("完成状态", String.class, null);
		
		
		List<Question> lqQuestions = questionService.getQuestionListByQuestionnaireId(questionnaireId);
		for (int i = 0; i < lqQuestions.size(); i++) {
				String t = lqQuestions.get(i).getTitle();
				table.addContainerProperty(t+"--题"+(i+1), String.class, null);
		}
		initializeSql();
		qflip = new QuestionFlipOverTable(customerQuestionnaireEditService, table, searchListJpql, searchCountJpql, null,questionnaireId,PAGE_LENGTH);
		tablePageLayout.addComponent(table);
		tablePageLayout.addComponent(qflip);
		tablePageLayout.setComponentAlignment(qflip, Alignment.MIDDLE_RIGHT);
		
	}

	/** 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件） */
	private void createTableButtonsComponents() {
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		this.addComponent(buttonsLayout);
		bt_edit_question = new Button("编辑问卷答案", this);
		bt_edit_question.setImmediate(true);
		bt_edit_question.setEnabled(false);
		buttonsLayout.addComponent(bt_edit_question);
		
		musicLayout = new HorizontalLayout();
		musicLayout.setSpacing(true);
 		buttonsLayout.addComponent(musicLayout);
 	}
	
	/** 根据不同的需求，初始化查询语句 */
	private void initializeSql() {
		StringBuffer nativeSqlList = new StringBuffer();
		StringBuffer nativeSqlCount = new StringBuffer();
		StringBuffer nativeSql = new StringBuffer();
		
		nativeSqlList.append("select");//
		nativeSqlList.append(" cqne.id,eu.id ,eu.username,eu.realname,eu.empno,ecr.id,ecr.name,tel.number,qne.maintitle,cqne.starttime,cqne.endtime,cqne.finish");
		nativeSqlCount.append(" select ");
		nativeSqlCount.append(" count(*) ");
		nativeSql.append(" from ec2_customer_questionnaire as cqne ");	//客户问卷
		nativeSql.append("	left join ec2_customer_resource as ecr ");	//客户资源
		nativeSql.append("	on  cqne.customerid = ecr.id");				
		nativeSql.append("	left join ec2_questionnaire as qne");		//问卷
		nativeSql.append("	on cqne.questionnaire_id = qne.id");		//
		/*nativeSql.append("	left join ec2_marketing_project_questionnaire_link as mpql");
		nativeSql.append("	on  cqne.id = mpql.questionnaire_id");*/
		nativeSql.append("	left join ec2_user as eu");					//csr用户
		nativeSql.append("	on  cqne.userid = eu.id");
		nativeSql.append("	left join ec2_telephone as tel");
		nativeSql.append("	on  ecr.id = tel.customerresource_id");
		
		nativeSql.append("	where 1 = 1");
		nativeSql.append("	and  qne.domain_id = " + domain.getId());
		nativeSql.append("	and  qne.id = " + questionnaireId);
		
		User u_q = (User)cb_user.getValue();
		if(null != u_q){//客服工号
			if(!"全部".equals(u_q.getEmpNo())){
				nativeSql.append("	and  eu.empno = '" + u_q.getEmpNo()+"'");}
		}
		MarketingProject marketingProject =  (MarketingProject)cb_marketingProject.getValue();
		if(null != marketingProject){//项目名称
			nativeSql.append("	and  ecr.id in( " );
			nativeSql.append("	select customerresource_id from ec2_marketing_project_task  where marketingproject_id =  "+marketingProject.getId() );
			nativeSql.append("	) " );	
		}
		if(null != startStartTime.getValue()) {//开始开始时间
			nativeSql.append(" and cqne.starttime >= '" + sdf.format(startStartTime.getValue()) +"'");
		} 
		if(null !=startEndTime.getValue()) {//开始结束时间
			nativeSql.append(" and cqne.starttime <= '" + sdf.format(startEndTime.getValue()) +"'");
		} 
		if(null != endStartTime.getValue()) {//结束开始时间
			nativeSql.append(" and cqne.endtime >= '" + sdf.format(endStartTime.getValue()) +"'");
		}
		if(null != endEndTime.getValue()) {//结束开始时间
			nativeSql.append(" and cqne.endtime <= '" + sdf.format(endEndTime.getValue()) +"'");
		}
		if(null !=cb_finish.getValue()){//完成
			nativeSql.append(" and cqne.finish = '" + cb_finish.getValue() +"'");
		}
		if(null !=tf_pnumber.getValue() && null != tf_pnumber.getValue().toString()){//客户号码
			if(tf_pnumber.getValue().toString().trim().length() > 0){
				nativeSql.append(" and tel.number = '" + tf_pnumber.getValue().toString().trim() +"'");
			}
		}
		
		//nativeSql.append("	and cqne.finish = 'end'");
		nativeSqlList.append(nativeSql);
		nativeSqlList.append(" order by cqne.id desc ");
		nativeSqlCount.append(nativeSql);
		
		searchListJpql  = nativeSqlList.toString();
		searchCountJpql = nativeSqlCount.toString();
	}
	
	/**
	 * 刷新显示分机的表格
	 * @param isToFirst 刷新后是否跳转至第一页   true 第一页 ；false 当前页 */
	public void updateTable(Boolean isToFirst) {
		if (qflip != null) {
			qflip.setSearchSql(searchListJpql);
			qflip.setCountSql(searchCountJpql);
			if (isToFirst) {
				qflip.refreshToFirstPage();
			} else {
				qflip.refreshInCurrentPage();
			}
		}
	}
	/** 本页面表格组件*/
	public Table getTable() {
		return table;
	}
	 

	/**
	 * 创建播放录音组件
	 * 
	 * @param tableFooterLayout
	 */
	private void createPlayerLayout() {
		playerLayout = new HorizontalLayout();
		playerLayout.setSpacing(true);
		this.addComponent(playerLayout);

		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 "
				+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		playerLayout.addComponent(player);
		playerLayout.setComponentAlignment(player, Alignment.MIDDLE_RIGHT);
	}
	//---------------------需要实现的接口方法------开始-----------------------------------
	/** 表格选择改变的监听器，设置按钮样式，状态信息*/
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			Item ob = table.getItem(table.getValue());
			if(null != ob){
				Property questionnaire_id = ob.getItemProperty("问卷编号");
 				if((null!=questionnaire_id)&&(null!=questionnaire_id.toString().trim())&&(questionnaire_id.toString().trim().length()>0)){
 					customerQuestionnaireId = Long.valueOf(questionnaire_id.toString().trim());
					bt_edit_question.setEnabled(true);
					//添加录音
					updateMusicUI(customerQuestionnaireId);
				}
			}else{
				bt_edit_question.setEnabled(false);
			}
		}
	}

	private void updateMusicUI(Long customerQuestionnaireId){
		musicLayout.removeAllComponents();
		List<RecordFile>  rflist = customerQuestionnaireEditService.getRecordFileList(customerQuestionnaireId,domain);
		if(rflist.size()>0){
			for (RecordFile rf : rflist) {
				String originalDownloadPath = rf.findOriginalDownloadPath();
				
				if(null != originalDownloadPath){
					final String shortName = originalDownloadPath.substring(originalDownloadPath.lastIndexOf("/")+1);
					
					final String httpPath =baseUrl+originalDownloadPath;		//http路径	老的录音文件名存储方式
					String hdPath = Config.props.getProperty(Config.REC_DIR_PATH)+originalDownloadPath; 		//硬盘路径	 检查文件是否存在，存在则返回url ,不存在，则返回null
					
					File musicFile = new File(hdPath);
					if(musicFile.exists()) {	//录音文件实际存在硬盘
						Button listen = new Button("试听:"+shortName);
						listen.setStyleName(BaseTheme.BUTTON_LINK);
						listen.setIcon(ResourceDataCsr.listen_type_ico);
						listen.addListener(new ClickListener() {
							private static final long serialVersionUID = -3872481972116614164L;
							@Override
							public void buttonClick(ClickEvent event) {
								String url = httpPath;
								String path = "<EMBED src='"
										+ url
										+ "' hidden=false autostart='true' width=360 height=63 "
										+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
								player.setValue(path);
							}
						});
						musicLayout.addComponent(listen);
					}else{
						logger.warn("录音文件不存在:"+shortName);
					}
				}
			}
		}else{
			musicLayout.addComponent(new Label("抱歉，未找到对应的录音!"+customerQuestionnaireId));
		}
	}
	 
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_search) {
			Questionnaire questionnaire = (Questionnaire)cb_questionnaire.getValue();
			if(null != questionnaire){
				this.lb_msg.setValue("");
				Long qid = questionnaire.getId();
				if(questionnaireId == qid){
					//不更新表格布局 制作数据加载
					initializeSql();
					updateTable(true);
				}else{
					//更新表格布局 去除表格 重新加载
					questionnaireId = qid;
					tablePageLayout.removeAllComponents();
					createQuestionnairTable();		
				}
			}else{
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("[请先选择问卷,必选项]"));
			}
		}else if(source == bt_edit_question){
			if(null != customerQuestionnaireId && customerQuestionnaireId > 0){
				if(null == customerQuestionnaire){
					customerQuestionnaire = new EditCustomerQuestionnaire(this);
				}
				this.getApplication().getMainWindow().removeWindow(customerQuestionnaire);
				this.getApplication().getMainWindow().addWindow(customerQuestionnaire);
				customerQuestionnaire.refreshComponentInfo(customerQuestionnaireId, 0, null);
				this.lb_msg.setValue("");
			}else{
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("[请选择客户问卷]"));//
			}
		}else if(source == bt_output_res){
			if (null != questionnaireId && questionnaireId > 0) {
				initializeSql();
				showCustomerQuestionDownload();
			}else{
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("[请选择客户问卷]"));//
			}
			
		}else if(source == bt_reset){
			this.cb_questionnaire.setValue(null);
			this.cb_finish.setValue(null);
			this.startStartTime.setValue(null);
			this.startEndTime.setValue(null);
			this.endStartTime.setValue(null);
			this.endEndTime.setValue(null);
			this.cb_marketingProject.setValue(null);
			this.cb_user.setValue(null);
			this.tf_pnumber.setValue("");
		}
	}
	
	private void showCustomerQuestionDownload() {
		if(null == customerQuestionDownload){
			customerQuestionDownload = new CustomerQuestionDownload();
		}
		this.getApplication().getMainWindow().removeWindow(customerQuestionDownload);
		this.getApplication().getMainWindow().addWindow(customerQuestionDownload);
		customerQuestionDownload.refreshComponentInfo(this, questionnaireId, searchCountJpql,searchListJpql);
		
	}
	
	/**
	 * 关闭问卷编辑导出，再打开，刷新问卷标题，项目名称等数据
	 * 
	 */
	public void refreshComponentInfo(){
		List<Questionnaire> qels = questionnaireService.getAllByDomainId(domain.getId(), "E");
		questionnaireContainer = new BeanItemContainer<Questionnaire>(Questionnaire.class);
		questionnaireContainer.removeAllItems();
		questionnaireContainer.addAll(qels);
		cb_questionnaire.setContainerDataSource(questionnaireContainer);
		 
		List<MarketingProject> mgpListBic = marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
 		marketingProjectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		marketingProjectContainer.removeAllItems();
		marketingProjectContainer.addAll(mgpListBic);
		cb_marketingProject.setContainerDataSource(marketingProjectContainer);
	}
	
	/** Action.Handler 实现方法 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		return new Action[]{ EditCustomer};
	}
	
	/**	页面右击时间 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (EditCustomer == action) {
			bt_edit_question.click();
		}  
	}
	
	
	
	//---------------------需要实现的接口方法------结束-----------------------------------
	 
}
