package com.jiangyifen.ec2.ui.mgr.commoditymanange;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.ProjectToCommodityLink;
import com.jiangyifen.ec2.service.eaoservice.CommodityService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.CommodityManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述：在商品管理界面，编辑商品关联的项目
 * 
 * @author  jrh
 * @date    2013年12月5日 下午9:14:21
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class EditConnectedProjectWindow extends Window implements Button.ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Notification success_notification;						// 成功过提示信息
	
	private Panel main_pn;
	private TwinColSelect project_tcs;

	private Button save_button;										// 保存按钮
	private Button cancel_button;									// 取消按钮
	
	private CommodityManagement commodityManagement;

	private Domain domain;
	private Commodity commodity;
	private ArrayList<Long> oldSelectedProjectIds;					// 修改之前商品关联的项目对应的Id集合
	private BeanItemContainer<MarketingProject> projectContainer;

	private CommodityService commodityService;
	private MarketingProjectService marketingProjectService;
	
	public EditConnectedProjectWindow(CommodityManagement commodityManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.commodityManagement=commodityManagement;

		domain = SpringContextHolder.getDomain();
		
		commodityService = SpringContextHolder.getBean("commodityService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");

		projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		main_pn = new Panel("编辑关联项目");
		main_pn.setSizeUndefined();
		windowContent.addComponent(main_pn);
		
		createProjectSelectorComponent(main_pn);
		creatOperateComponents(main_pn);
		
	}

	/**
	 * 创建项目选择器
	 * @param main_pn
	 */
	private void createProjectSelectorComponent(Panel main_pn) {
		project_tcs = new TwinColSelect();
		project_tcs.setNullSelectionAllowed(true);
		project_tcs.setImmediate(true);
		project_tcs.setWidth("400px");
		project_tcs.setHeight("200px");
		project_tcs.setMultiSelect(true);
		project_tcs.setContainerDataSource(projectContainer);
		project_tcs.setItemCaptionPropertyId("projectName");
		project_tcs.setLeftColumnCaption("可选项目：");
		project_tcs.setRightColumnCaption("已选项目：");
		main_pn.addComponent(project_tcs);
	}

	/**
	 *  创建操作按钮
	 */
	private void creatOperateComponents(Panel main_pn) {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
		operator_l.setWidth("100%");
		main_pn.addComponent(operator_l);

		save_button = new Button("保 存", this);
		save_button.setStyleName("default");
		operator_l.addComponent(save_button);
		
		cancel_button = new Button("取 消", this);
		operator_l.addComponent(cancel_button);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_button) {
			try {
				executeSave();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 在商品管理界面，编辑商品关联的项目时出现异常 ！---->"+e.getMessage(), e);
				this.getApplication().getMainWindow().showNotification("编辑失败，请核查信息后重试！", Notification.TYPE_WARNING_MESSAGE);
			}
		} else if(source == cancel_button) {
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 执行保存
	 */
	@SuppressWarnings("unchecked")
	private void executeSave() {
		Set<MarketingProject> selectedProjects = (Set<MarketingProject>) project_tcs.getValue();	// 当前选中的项目
		ArrayList<Long> selectedProjectIds = new ArrayList<Long>();					// 用于存储当前选中的所有项目对应的编号
		ArrayList<Long> needAddProjectIds = new ArrayList<Long>();					// 用于存储当前 新增加的项目编号
		
		for(MarketingProject project : selectedProjects) {
			Long projectId = project.getId();
			selectedProjectIds.add(projectId);
			if(!oldSelectedProjectIds.contains(projectId)) {						// 如果之前没有选中该项目，则加入新增行列
				needAddProjectIds.add(projectId);
			}
		}
		
		ArrayList<Long> needRemoveProjectIds = new ArrayList<Long>();				// 用于存储当前 删除的项目编号
		for(Long oid : oldSelectedProjectIds) {
			if(!selectedProjectIds.contains(oid)) {									// 如果当前没有选中该项目，则加入异常行列
				needRemoveProjectIds.add(oid);
			}
		}

		if(needAddProjectIds.size() > 0 || needRemoveProjectIds.size() > 0) {		// 判断商品选择的项目是否发生变化
			// 更新商品与项目对应关系的中间表
			updateProjectToCommodityLink(commodity.getId(), needAddProjectIds, needRemoveProjectIds);
		}
		
		success_notification.setCaption("编辑成功");
		commodityManagement.updateTable(false);
		this.getApplication().getMainWindow().showNotification(success_notification);
		this.getApplication().getMainWindow().removeWindow(this);
	}

	/**
	 * 更新商品与项目对应关系的中间表
	 * 
	 * @param commodityId				当前待操作的商品对应的编号
	 * @param needAddProjectIds			需要将商品与之添加关联的项目编号
	 * @param needRemoveProjectIds		需要将商品与项目关联移除的项目编号
	 */
	private void updateProjectToCommodityLink(Long commodityId, ArrayList<Long> needAddProjectIds, ArrayList<Long> needRemoveProjectIds) {
		for(Long addProjectId : needAddProjectIds) {
			ProjectToCommodityLink projectToCommodityLink = new ProjectToCommodityLink();
			projectToCommodityLink.setCommodityId(commodityId);
			projectToCommodityLink.setProjectId(addProjectId);
			commodityService.saveProjectToCommodityLink(projectToCommodityLink);
		}
		for(Long deleteProjectId : needRemoveProjectIds) {
			commodityService.deleteProjectToCommodityLinkByIds(commodityId, deleteProjectId);
		}
	}
	
	/**
	 * 跟新编辑界面的数据源
	 * @param commodity	商品
	 */
	public void echoWindowInfo(Commodity commodity) {
		this.commodity = commodity;
		this.oldSelectedProjectIds = new ArrayList<Long>();

		// 更新重新查询项目
		List<MarketingProject> allProjects = marketingProjectService.getAll(domain);
		projectContainer.removeAllItems();
		projectContainer.addAll(allProjects);
		
		project_tcs.setValue(null);						// 清空原来选中的值，一定的加上,不然会导致 project_tcs.getValue() 获取的Set集合会越来越大
		List<Long> needSelectedProjectIds = commodityService.getAllProjectIdsByBlacklistItemId(commodity.getId());
		for(Long projectId : needSelectedProjectIds) {
			for(MarketingProject project : allProjects) {
				if(projectId.equals(project.getId())) {
					project_tcs.select(project);
					oldSelectedProjectIds.add(project.getId());
				}
			}
		}
	}
	
}
