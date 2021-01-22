package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.util.ArrayList;

import java.util.List;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.report.tabsheet.ProjectFinishedStatus;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;

import com.jiangyifen.ec2.utils.SpringContextHolder;

import com.vaadin.data.util.BeanItemContainer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;

import com.vaadin.ui.VerticalLayout;

/**
 * REPORTPAGE项目完成情况
 * @author lxy
 *
 */
public class TopUiProjectFinishedStatus extends VerticalLayout {

	private static final long serialVersionUID = 8004377440726182946L;

	private ComboBox projectComboBox;

	private Tab tab;

	private ProjectFinishedStatus projectFinishedStatus;
	private ReportService reportService = SpringContextHolder
			.getBean("reportService");
	private String sql;
	private List<MarketingProject> marketingProjects;

	private List<Object[]> countList = new ArrayList<Object[]>();

	private List<String> namesList = new ArrayList<String>();

	// 登陆的user
	private User loginUser = (User) SpringContextHolder.getHttpSession()
			.getAttribute("loginUser");
	// 登陆用户的domainId
	private String domainId = loginUser.getDomain().getId().toString();

	/**
	 * 构造器
	 */
	public TopUiProjectFinishedStatus(
			ProjectFinishedStatus projectFinishedStatus) {
		this.projectFinishedStatus = projectFinishedStatus;
		this.setMargin(true);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		this.addComponent(horizontalLayout);

		// 创建项目选择框
		horizontalLayout.addComponent(new Label("项目:"));
		projectComboBox = buildProjectComboBox();
		horizontalLayout.addComponent(projectComboBox);

		// 刷新按钮
		Button refresh = new Button("刷新");
		refresh.setStyleName("small");
		refresh.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 1736942477860695599L;
			@Override
			public void buttonClick(ClickEvent event) {
				refresh();
			}
		});
		horizontalLayout.addComponent(refresh);
	}

	/**
	 * 创建选择项目的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildProjectComboBox() {
		// 从数据库中获取“项目”信息，并绑定到Container中
		BeanItemContainer<MarketingProject> projectContainer = new BeanItemContainer<MarketingProject>(
				MarketingProject.class);

		MarketingProject project = new MarketingProject();
		project.setProjectName("全部");

		projectContainer.addBean(project);
		MarketingProjectService projectService = SpringContextHolder
				.getBean("marketingProjectService");

		marketingProjects = projectService.getAll(SpringContextHolder
				.getDomain());

		projectContainer.addAll(marketingProjects);

		// 创建ComboBox
		projectComboBox = new ComboBox();
		projectComboBox.setContainerDataSource(projectContainer);
		projectComboBox.setImmediate(true);
		projectComboBox.setWidth("120px");
		projectComboBox.setValue(project);// 设置组合框的默认选中值
		projectComboBox.setNullSelectionAllowed(false);
		return projectComboBox;
	}

	public void refresh() {

		TabSheet tabSheet = projectFinishedStatus.getTabSheet();

		MarketingProject project = (MarketingProject) projectComboBox
				.getValue();
		int finishedCount;
		int unfinishedCount;
		int undistributeCount;

		if (project.getProjectName().equals("全部")) {

			// 已完成
			sql = "select count(*) from ec2_marketing_project_task where  isfinished = 'TRUE' and user_id is not null and domain_id = "
					+ "'" + domainId + "'";

			List<Object> list = reportService.getOneRecord(sql);

			finishedCount = Integer.parseInt(list.get(0).toString());

			// 未完成
			sql = "select count(*) from ec2_marketing_project_task where isfinished = 'FALSE' and user_id is not null and domain_id = "
					+ "'" + domainId + "'";

			list = reportService.getOneRecord(sql);

			unfinishedCount = Integer.parseInt(list.get(0).toString());

			// 未分配
			sql = "select count(*) from ec2_marketing_project_task where user_id is null and domain_id = "
					+ "'" + domainId + "'";

			list = reportService.getOneRecord(sql);

			undistributeCount = Integer.parseInt(list.get(0).toString());

			Object[] objects = new Object[] { finishedCount, unfinishedCount,
					undistributeCount };

			countList.clear();
			countList.add(objects);

			namesList.add("已完成");
			namesList.add("未完成");
			namesList.add("未分配");

			tab = tabSheet.addTab(new ReportTableUtil(countList, "项目完成情况(全部)",
					namesList, "", ""), "全部", null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);

		} else {

			Long projectId = project.getId();

			// 已完成
			sql = "select count(*) from ec2_marketing_project_task where  isfinished = 'TRUE' and user_id is not null  and marketingproject_id = "
					+ projectId + " and domain_id = " + "'" + domainId + "'";

			List<Object> list = reportService.getOneRecord(sql);

			finishedCount = Integer.parseInt(list.get(0).toString());

			// 未完成
			sql = "select count(*) from ec2_marketing_project_task where isfinished = 'FALSE' and user_id is not null and marketingproject_id = "
					+ projectId + " and domain_id = " + "'" + domainId + "'";

			list = reportService.getOneRecord(sql);

			unfinishedCount = Integer.parseInt(list.get(0).toString());

			// 未分配
			sql = "select count(*) from ec2_marketing_project_task where user_id is null and marketingproject_id = "
					+ projectId + " and domain_id = " + "'" + domainId + "'";

			list = reportService.getOneRecord(sql);

			undistributeCount = Integer.parseInt(list.get(0).toString());

			Object[] objects = new Object[] { finishedCount, unfinishedCount,
					undistributeCount };

			countList.clear();
			countList.add(objects);

			namesList.add("已完成");
			namesList.add("未完成");
			namesList.add("未分配");

			tab = tabSheet.addTab(new ReportTableUtil(countList, "项目完成情况("
					+ project.getProjectName() + ")", namesList, "", ""), "全部",
					null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);

		}

	}

}
