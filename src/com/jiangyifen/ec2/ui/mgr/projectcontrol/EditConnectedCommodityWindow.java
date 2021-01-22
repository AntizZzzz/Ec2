package com.jiangyifen.ec2.ui.mgr.projectcontrol;

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
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
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
 * @Description 描述：在项目管理界面，编辑项目关联的商品
 * 
 * @author  jrh
 * @date    2013年12月5日 下午9:14:21
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class EditConnectedCommodityWindow extends Window implements Button.ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Notification success_notification;						// 成功过提示信息
	
	private Panel main_pn;
	private TwinColSelect commodity_tcs;

	private Button save_button;										// 保存按钮
	private Button cancel_button;									// 取消按钮

	private ProjectControl projectControl;

	private Domain domain;
	private MarketingProject project;
	private ArrayList<Long> oldSelectedCommodityIds;					// 修改之前商品关联的项目对应的Id集合
	private BeanItemContainer<Commodity> commodityContainer;

	private CommodityService commodityService;
	
	public EditConnectedCommodityWindow(ProjectControl projectControl) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.projectControl = projectControl;

		domain = SpringContextHolder.getDomain();
		
		commodityService = SpringContextHolder.getBean("commodityService");

		commodityContainer = new BeanItemContainer<Commodity>(Commodity.class);
		
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
		
		main_pn = new Panel("编辑关联商品");
		main_pn.setSizeUndefined();
		windowContent.addComponent(main_pn);
		
		createProjectSelectorComponent(main_pn);
		creatOperateComponents(main_pn);
		
	}

	/**
	 * 创建商品选择器
	 * @param main_pn
	 */
	private void createProjectSelectorComponent(Panel main_pn) {
		commodity_tcs = new TwinColSelect();
		commodity_tcs.setNullSelectionAllowed(true);
		commodity_tcs.setImmediate(true);
		commodity_tcs.setWidth("400px");
		commodity_tcs.setHeight("200px");
		commodity_tcs.setMultiSelect(true);
		commodity_tcs.setContainerDataSource(commodityContainer);
		commodity_tcs.setItemCaptionPropertyId("commodityName");
		commodity_tcs.setLeftColumnCaption("可选商品：");
		commodity_tcs.setRightColumnCaption("已选商品：");
		main_pn.addComponent(commodity_tcs);
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
				logger.error("jrh 在项目管理界面，编辑项目关联的商品时出现异常 ！---->"+e.getMessage(), e);
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
		Set<Commodity> selectedCommoditys = (Set<Commodity>) commodity_tcs.getValue();	// 当前选中的项目
		ArrayList<Long> selectedCommodityIds = new ArrayList<Long>();					// 用于存储当前选中的所有项目对应的编号
		ArrayList<Long> needAddCommodityIds = new ArrayList<Long>();					// 用于存储当前 新增加的项目编号
		
		for(Commodity commodity : selectedCommoditys) {
			Long commodityId = commodity.getId();
			selectedCommodityIds.add(commodityId);
			if(!oldSelectedCommodityIds.contains(commodityId)) {						// 如果之前没有选中该项目，则加入新增行列
				needAddCommodityIds.add(commodityId);
			}
		}
		
		ArrayList<Long> needRemoveCommodityIds = new ArrayList<Long>();				// 用于存储当前 删除的项目编号
		for(Long oid : oldSelectedCommodityIds) {
			if(!selectedCommodityIds.contains(oid)) {									// 如果当前没有选中该项目，则加入异常行列
				needRemoveCommodityIds.add(oid);
			}
		}

		if(needAddCommodityIds.size() > 0 || needRemoveCommodityIds.size() > 0) {		// 判断商品选择的项目是否发生变化
			// 更新商品与项目对应关系的中间表
			updateProjectToCommodityLink(project.getId(), needAddCommodityIds, needRemoveCommodityIds);
		}
		
		success_notification.setCaption("编辑成功");
		projectControl.updateTable(false);
		this.getApplication().getMainWindow().showNotification(success_notification);
		this.getApplication().getMainWindow().removeWindow(this);
	}

	/**
	 * 更新商品与项目对应关系的中间表
	 * 
	 * @param projectId						当前待操作的项目对应的编号
	 * @param needAddCommodityIds			需要将项目与之添加关联的商品编号
	 * @param needRemoveCommodityIds		需要将项目与项目关联移除的商品编号
	 */
	private void updateProjectToCommodityLink(Long projectId, ArrayList<Long> needAddCommodityIds, ArrayList<Long> needRemoveCommodityIds) {
		for(Long addCommodityId : needAddCommodityIds) {
			ProjectToCommodityLink projectToCommodityLink = new ProjectToCommodityLink();
			projectToCommodityLink.setCommodityId(addCommodityId);
			projectToCommodityLink.setProjectId(projectId);
			commodityService.saveProjectToCommodityLink(projectToCommodityLink);
		}
		for(Long deleteCommodityId : needRemoveCommodityIds) {
			commodityService.deleteProjectToCommodityLinkByIds(deleteCommodityId, projectId);
		}
	}
	
	/**
	 * 跟新编辑界面的数据源
	 * @param project	项目
	 */
	public void echoWindowInfo(MarketingProject project) {
		this.project = project;
		this.oldSelectedCommodityIds = new ArrayList<Long>();

		// 更新重新查询项目
		List<Commodity> allCommoditys = commodityService.getAllByDomainId(domain.getId());
		commodityContainer.removeAllItems();
		commodityContainer.addAll(allCommoditys);
		
		commodity_tcs.setValue(null);						// 清空原来选中的值，一定的加上,不然会导致 project_tcs.getValue() 获取的Set集合会越来越大
		List<Commodity> needSelectedCommoditys = commodityService.getAllByProjectId(project.getId());
		for(Commodity oldCommodity : needSelectedCommoditys) {		// 遍历原来选中的商品
			Long oldCid = oldCommodity.getId();
			for(Commodity commodity : allCommoditys) {
				Long newCid = commodity.getId();
				if(oldCid.equals(newCid)) {
					commodity_tcs.select(commodity);
					oldSelectedCommodityIds.add(oldCid);
				}
			}
		}
	}
	
}
