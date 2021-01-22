package com.jiangyifen.ec2.ui.mgr.resourceimport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceImport;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 高级搜索窗口
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class AdvanceSearchBatch extends Window implements Button.ClickListener{
	private VerticalLayout windowContent;
/**
 * 主要组件输出区	
 */
	//Id 组件
	private TextField idFrom;
	private TextField idTo;
	
	//时间 组件
	private PopupDateField timeFrom;
	private PopupDateField timeTo;
	
	//名称和备注关键字组件
	private TextField name;
	private TextField desc;
	
	//按钮组件
	private Button search;
	private Button cancel;
/**
 * 其他组件
 */
	//翻页组件引用
	/*private Domain domain;*/
	private User loginUser;
	private DepartmentService departmentService;
	private ResourceImport resourceImport;
	
	public AdvanceSearchBatch(ResourceImport resourceImport) {
		this.setCaption("高级搜索");
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.resourceImport=resourceImport;
		
		/*domain = SpringContextHolder.getDomain();*/
		loginUser = SpringContextHolder.getLoginUser();
		departmentService = SpringContextHolder.getBean("departmentService");
		
		//添加Window内最大的Layout
		windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//添加一个控制Id范围的水平Layout
		windowContent.addComponent(buildIdLayout());
		//添加一个控制时间范围的水平Layout
		windowContent.addComponent(buildTimeLayout());
		//添加按名称的水平Layout
		windowContent.addComponent(buildNameLayout());
		//添加描述查询的水平Layout
		windowContent.addComponent(buildDescLayout());
		//添加搜索和取消按钮水平Layout
		windowContent.addComponent(buildButtonsLayout());
	}
	/**
	 * 按钮布局
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		search=new Button("搜索");
		search.addListener(this);
		buttonsLayout.addComponent(search);
		
		cancel=new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);
		
		return buttonsLayout;
	}
	/**
	 * 备注信息关键字布局
	 * @return
	 */
	private HorizontalLayout buildDescLayout() {
		HorizontalLayout descLayout=new HorizontalLayout(); 
		//描述
		descLayout.addComponent(new Label("描述&nbsp&nbsp",Label.CONTENT_XHTML));
		desc=new TextField();
		desc.setWidth("5em");
		descLayout.addComponent(desc);
		return descLayout;
	}
	/**
	 * 名称布局
	 * @return
	 */
	private HorizontalLayout buildNameLayout() {
		HorizontalLayout nameLayout=new HorizontalLayout(); 
		//名称
		nameLayout.addComponent(new Label("名称&nbsp&nbsp",Label.CONTENT_XHTML));
		name=new TextField();
		name.setWidth("5em");
		nameLayout.addComponent(name);
		return nameLayout;
	}
	
	/**
	 * 时间窗口布局
	 * @return
	 */
	private HorizontalLayout buildTimeLayout() {
		HorizontalLayout timeLayout=new HorizontalLayout(); 
		timeLayout.setSpacing(true);
		timeLayout.addComponent(new Label("时间"));
		//从?时间 开始
		timeFrom=new PopupDateField();
		timeFrom.setResolution(PopupDateField.RESOLUTION_DAY);
		timeFrom.setDateFormat("yyyy-MM-dd");
		timeFrom.setWidth("8em");
		timeLayout.addComponent(timeFrom);
		
		//到
		timeLayout.addComponent(new Label("~"));

		//到?时间 结束
		timeTo=new PopupDateField();
		timeTo.setResolution(PopupDateField.RESOLUTION_DAY);
		timeTo.setDateFormat("yyyy-MM-dd");
		timeTo.setWidth("8em");
		timeLayout.addComponent(timeTo);
		return timeLayout;
	}

	/**
	 * 按Id搜索输出
	 * @return
	 */
	private HorizontalLayout buildIdLayout() {
		HorizontalLayout idLayout=new HorizontalLayout(); 
		idLayout.setSpacing(true);
		idLayout.addComponent(new Label("ID号"));
		//从Id? 开始
		idFrom=new TextField();
		idFrom.setWidth("5em");
		idLayout.addComponent(idFrom);
		
		//到
		idLayout.addComponent(new Label("~"));

		//到Id? 结束
		idTo=new TextField();
		idTo.setWidth("5em");
		idLayout.addComponent(idTo);
		return idLayout;
	}
	
	/**
	 * 由 buttonClick 调用， 单击按钮时执行的搜索方法
	 */
	private void executeSearch() {
		SqlGenerator sqlGenerator = new SqlGenerator("CustomerResourceBatch");
		
		//id范围
		String idFromStr="";
		if(idFrom.getValue()!=null) idFromStr=idFrom.getValue().toString();
		String idToStr="";
		if(idTo.getValue()!=null) idToStr=idTo.getValue().toString();

		SqlGenerator.Between idBetween=new SqlGenerator.Between("id", idFromStr, idToStr, false);
		sqlGenerator.addAndCondition(idBetween);
		
		//时间范围
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		String timeFromStr="";
		if(timeFrom.getValue()!=null) timeFromStr=sdf.format(timeFrom.getValue()).toString();
		String timeToStr="";
		if(timeTo.getValue()!=null) timeToStr=sdf.format(timeTo.getValue()).toString();
		
		SqlGenerator.Between timeBetween=new SqlGenerator.Between("createDate", timeFromStr, timeToStr, true);
		sqlGenerator.addAndCondition(timeBetween);
		
		//名称
		String nameStr="";
		if(name.getValue()!=null) nameStr=name.getValue().toString();
		
		SqlGenerator.Like nameLike=new SqlGenerator.Like("batchName", nameStr);
		sqlGenerator.addAndCondition(nameLike);
		
		//描述
		String descStr="";
		if(desc.getValue()!=null) descStr=desc.getValue().toString();
		
		SqlGenerator.Like descLike=new SqlGenerator.Like("note", descStr);
		sqlGenerator.addAndCondition(descLike);
		
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
		
		// jrh 获取当前用户所属部门及其子部门创建的批次
		for(Long deptId : allGovernedDeptIds) {
			SqlGenerator.Equal orEqual = new SqlGenerator.Equal("user.department.id", deptId.toString(), false);
			sqlGenerator.addOrCondition(orEqual);
		}
		
		//排序
		sqlGenerator.setOrderBy("id", SqlGenerator.DESC);
		
		String sqlSelect=sqlGenerator.generateSelectSql();
		String sqlCount=sqlGenerator.generateCountSql();
		
		resourceImport.setSqlSelect(sqlSelect);
		resourceImport.setSqlCount(sqlCount);
		resourceImport.updateTable(true);
		resourceImport.getTable().setValue(null);//使Table处于未选中状态
	}
	/**
	 * 搜索 和 取消按钮的 单击事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==search){
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "高级搜索出错！");
				return;
			}
		}
		//销毁窗口
		this.getParent().removeWindow(this);
	}
	
}
