package com.jiangyifen.ec2.ui.mgr.rolemanage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.service.eaoservice.BusinessModelService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TreeTable;

/**
 * 角色权限设置
 * @author jrh
 */
@SuppressWarnings("serial")
public class RoleAccess extends HorizontalLayout {
	
	private TreeTable modelsTree;
	private HashMap<Long, CheckBox> checkBoxMap;
	private HashSet<Long> parentIds;					// 所有父Model的Id值集合
	private BusinessModelService businessModelService;
	private List<BusinessModel> modelsList;				// 所有父Model的集合
	private Map<Long, BusinessModel> selectedModelIds;	// 选中的角色权限
	
	/**
	 * RoleAccess(角色权限的构造器)
	 * @param roleType
	 */
	public RoleAccess() {
		this.setWidth("420px");

		checkBoxMap = new HashMap<Long, CheckBox>();
		parentIds = new HashSet<Long>();
		selectedModelIds = new HashMap<Long, BusinessModel>();
		
		// TreeTable
		modelsTree = new TreeTable();
		modelsTree.setPageLength(9);
		modelsTree.setWidth("100%");
		modelsTree.setStyleName("striped");
		modelsTree.setWriteThrough(false);
		modelsTree.setRowHeaderMode(TreeTable.ROW_HEADER_MODE_INDEX);
		modelsTree.addContainerProperty("描述", String.class, "");
		modelsTree.addContainerProperty("权限", CheckBox.class, "");
		this.addComponent(modelsTree);
		
		// 取出所有父BusinessModel
		businessModelService = SpringContextHolder.getBean("businessModelService");
	}

	/**
	 *  由 buttonClick调用,选中或者是不选中父CheckBox或子CheckBox
	 * @param id
	 */
	private void selectParentAndChild(Long id) {
		if (parentIds.contains(id)) { // 说明是Model级组件
			CheckBox selfCB = checkBoxMap.get(id);
			Boolean parentValue = (Boolean) selfCB.getValue();
			BusinessModel self = (BusinessModel) selfCB.getData();
			Set<BusinessModel> childs = ((BusinessModel) selfCB.getData()).getChilds();

			// 更改选中框中的值，来改变选中模块的信息
			if(parentValue) {
				selectedModelIds.put(self.getId(), self);
			} else {
				selectedModelIds.remove(self.getId());
			}
			
			for (BusinessModel child : childs) {
				checkBoxMap.get(child.getId()).setValue(parentValue);
				if(parentValue) {
					selectedModelIds.put(child.getId(), child);	
				} else {
					selectedModelIds.remove(child.getId());
				}
			}
		} else { // 说明是Function级组件
			CheckBox child = checkBoxMap.get(id);
			Boolean childValue = (Boolean) child.getValue();
			// 取得Model级组件
			BusinessModel self = (BusinessModel) child.getData();
			BusinessModel parent = ((BusinessModel) (child.getData())).getParent();
			Long parentId = parent.getId();
			CheckBox parentCB = checkBoxMap.get(parentId);
			
			if (childValue) { // 子组件为true，则选中父组件
				parentCB.setValue(true);
				selectedModelIds.put(self.getId(), self);
				selectedModelIds.put(parent.getId(), parent);
			} else { // 子组件为false，则判断是否有其他子组件选中，有则选中父组件，没有则置父组件为False
				Set<BusinessModel> childs = ((BusinessModel) parentCB.getData()).getChilds();
				Boolean hasTrue = false;
				for (BusinessModel tempChild : childs) {
					hasTrue = (Boolean) checkBoxMap.get(tempChild.getId()).getValue();
					if (hasTrue) {
						break;
					}
				}
				// 子组件中没有选中的组件
				if (hasTrue == false) {
					parentCB.setValue(false);
					selectedModelIds.remove(parent.getId());
				}
				selectedModelIds.remove(self.getId());
			}
		}
	}
	
	/**
	 * jrh 按角色类型更新TreeTable的显示
	 * 
	 * @param roleType
	 */
	public void updateModels(RoleType roleType) {
		parentIds = new HashSet<Long>();
		modelsList = businessModelService.getModelsByRoleType(roleType);
		checkBoxMap = new HashMap<Long, CheckBox>();
		modelsTree.removeAllItems();
		
		for (int i = 0; i < modelsList.size(); i++) {
			// 父功能模块
			BusinessModel tempModel = modelsList.get(i);
			parentIds.add(tempModel.getId());
			
			CheckBox parentBox = createChockBox(tempModel);
			checkBoxMap.put(tempModel.getId(), parentBox);
			Object modelParent = modelsTree.addItem(new Object[] { tempModel.getDescription(), parentBox}, tempModel);
			modelsTree.setChildrenAllowed(modelParent, true);
			modelsTree.setCollapsed(modelParent, false);

			// 选function(),所有子功能模块
			List<BusinessModel> functionList = orderSet(tempModel);
			for (int j = 0; j < functionList.size(); j++) {
				BusinessModel tempModel2 = functionList.get(j);

				CheckBox childBox = createChockBox(tempModel2);
				checkBoxMap.put(tempModel2.getId(), childBox);
				Object modelChild = modelsTree.addItem(new Object[] { tempModel2.getDescription(), childBox},
						tempModel2);
				modelsTree.setChildrenAllowed(modelChild, false);
				modelsTree.setParent(modelChild, modelParent);
			}
		}
		
	}
	
	/**
	 * jrh 创建权限勾选框
	 * @param model
	 * @return
	 */
	private CheckBox createChockBox(final BusinessModel model) {
		CheckBox checkBox = new CheckBox();
		checkBox.setImmediate(true);
		checkBox.setData(model);
		checkBox.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				selectParentAndChild(model.getId());
			}
		});
		return checkBox;
	}

	/**
	 * 按照Id值为Set排序
	 * 使Set变为按照Id排序的有序List集合
	 * @param tempModel
	 * @return
	 */
	private List<BusinessModel> orderSet(BusinessModel tempModel) {
		Set<BusinessModel> modelSet = tempModel.getChilds();
		ArrayList<BusinessModel> modelList = new ArrayList<BusinessModel>(modelSet);
		Collections.sort(modelList, new Comparator<BusinessModel>() {
			@Override
			public int compare(BusinessModel model1, BusinessModel model2) {
				if(model1.getId()>model2.getId()){
					return 1;
				}else if(model1.getId()<model2.getId()){
					return -1;
				}
				return 0;
			}
		});
		return modelList;
	}
	
	/**
	 * 取得所有CheckBox的Map
	 * @return
	 */
	public HashMap<Long, CheckBox> getCheckBoxMap(){
		return checkBoxMap;
	}

	/**
	 * jrh 回显需要选择的权限
	 * @param selectedModelIds
	 */
	public void echoSelectedModelIds(Map<Long, BusinessModel> selectedModelIds) {
		this.selectedModelIds = new HashMap<Long, BusinessModel>(selectedModelIds);
		
		// 回显需要选择的权限
		for(Long modelId:selectedModelIds.keySet()){
			if(checkBoxMap.get(modelId)!=null){
				checkBoxMap.get(modelId).setValue(true);
			}
		}
	}
	
	

}
