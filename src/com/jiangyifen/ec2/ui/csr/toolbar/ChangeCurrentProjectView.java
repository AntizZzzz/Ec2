package com.jiangyifen.ec2.ui.csr.toolbar;

import java.util.List;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView.Content;

/**
 * 话务员的修改当前工作项目显示 组件
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class ChangeCurrentProjectView extends Panel implements Content {
	
	private OptionGroup projectOption;		// 项目选择项
	private String currentNameAndType;		// 当前项目名称和项目类型
	
	private BeanItemContainer<MarketingProject> projectContainer;
	
	public ChangeCurrentProjectView(List<MarketingProject> projectList) {
		this.setCaption("项目选择");
		this.setWidth("150px");
		currentNameAndType = "暂无";
		projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		projectContainer.addAll(projectList);
		
		projectOption = new OptionGroup();
		projectOption.setWidth("150px");
		projectOption.setImmediate(true);
		projectOption.setNullSelectionAllowed(true);
		projectOption.setContainerDataSource(projectContainer);
		projectOption.setItemCaptionPropertyId("projectNameAndType");
		this.addComponent(projectOption);

		// 如果当前该用户现在拥有项目，则默认选取第一个项目
		if(projectList.size() > 0) {
			MarketingProject project = (MarketingProject)projectContainer.getItemIds().toArray()[0];
			projectOption.setValue(project);
			
			//取得分机
			String exten=ShareData.userToExten.get(SpringContextHolder.getLoginUser().getId());
			//取得外线
			SipConfig outLineSip=project.getSip();
			//存储分机和外线的对应关系
			if(outLineSip!=null){
				ShareData.extenToDynamicOutline.put(exten, outLineSip.getName());
			}else{
				ShareData.extenToDynamicOutline.remove(exten);
			}
			//存储分机和项目的对应关系
			ShareData.extenToProject.put(exten, project.getId());
		}
		
		// 根据项目集合，刷新Optiongroup 的数据源
		refershOptionSource(projectList);
	}
	
	/**
	 * 根据项目集合，刷新Optiongroup 的数据源
	 * @param projectList
	 */
	public void refershOptionSource(List<MarketingProject> projectList) {
		// 第一步：保存之前选中项目对象
		MarketingProject oldProject = (MarketingProject) projectOption.getValue();
		
		// 第二步：更换Container中的数据源，并根据项目存在情况改变OptionGroup 的标题
		projectContainer.removeAllItems();
		projectContainer.addAll(projectList);
		if(projectList.size() > 0) {
			projectOption.setCaption("选项:");
		} else {
			projectOption.setCaption("暂无项目");
			currentNameAndType = "暂无";
		}
		
		// 第三步：回显项目对象
		boolean isExisted = false;
		for(MarketingProject project : projectContainer.getItemIds()) {
			if(oldProject != null && project.getId().equals(oldProject.getId())) {
				projectOption.setValue(project);
				currentNameAndType = project.getProjectNameAndType();
				isExisted = true;
				break;
			}
		}
		// 第四步 如果没有找到，则将选择框置为空
		if(isExisted == false) {
			projectOption.setValue(null);
		}
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "<font size='2' color='blue'><B>"+currentNameAndType+"</B></font>";
	}
	
	@Override
	public Component getPopupComponent() {
		return this;
	}

	public OptionGroup getProjectOption() {
		return projectOption;
	}

}
