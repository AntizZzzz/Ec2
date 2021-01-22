package com.jiangyifen.ec2.ui.mgr.knowledgebase;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Knowledge;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.KnowledgeStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.KnowledgeManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class AddKnowledgeWindow extends Window implements ClickListener {

	// 标签和输入框组件
	private Label label;
	private TextField titleField;
	private TextArea contentArea;

	// 按钮组件
	private Button save;
	private Button cancel;
	//持有上层组件引用
	private KnowledgeManagement knowledgeManagement;

	private CommonService commonService;
	private Domain domain;

	public AddKnowledgeWindow(KnowledgeManagement knowledgeManagement) {
		this.commonService = SpringContextHolder.getBean("commonService");
		this.domain = SpringContextHolder.getDomain();
		this.knowledgeManagement = knowledgeManagement;
		this.setCaption("添加知识");
		this.center();
		this.setModal(true);
		this.setResizable(false);

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);

		HorizontalLayout layoutTitle = new HorizontalLayout();
		Label titelLabel = new Label("知识标题:");
		titelLabel.setWidth("80px");
		layoutTitle.addComponent(titelLabel);
		
		titleField= new TextField();
		titleField.setRequired(true);
		titleField.setNullRepresentation("");
		titleField.setWidth("80em");
		titleField.setInputPrompt("知识标题");
		layoutTitle.addComponent(titleField);
		windowContent.addComponent(layoutTitle);
		

		HorizontalLayout layout = new HorizontalLayout();
		label = new Label("知识内容:");
		label.setWidth("80px");
		layout.addComponent(label);

		contentArea = new TextArea();
		contentArea.setColumns(80);
		contentArea.setRows(30);
		
		contentArea.setRequired(true);
		contentArea.setNullRepresentation("");
		contentArea.setInputPrompt("请输入知识内容");
		layout.addComponent(contentArea);

		windowContent.addComponent(layout);

		windowContent.addComponent(buildButtonsLayout());

	}

	// 创建按钮组件
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 保存按钮
		save = new Button("保存");
		save.setStyleName("default");
		save.addListener((ClickListener) this);
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener((ClickListener) this);
		buttonsLayout.addComponent(cancel);
		return buttonsLayout;
	}

	

	@Override
	public void attach() {
		super.attach();
		titleField.setValue(null);
		contentArea.setValue(null);
	}

	//执行添加操作
	@SuppressWarnings("unchecked")
	private void executeSave() {
		//验证
		String content = (String) contentArea.getValue();
		String title=(String)titleField.getValue();
		if(StringUtils.isEmpty(content)||StringUtils.isEmpty(title)){
			this.showNotification("知识标题和内容不能为空！");
			return;
		}

		String sql="select k from Knowledge k where k.domain.id="+domain.getId()+" and k.title like '%"+title.trim()+"%'";
		ArrayList<Knowledge> knowledgeList=(ArrayList<Knowledge>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
		if(knowledgeList.size()>0){
			this.showNotification("此知识标题已经存在，不允许重复添加！");
			return;
		}
		
		// 存储到数据库
		Knowledge knowledge = new Knowledge();
		User loginUser = SpringContextHolder.getLoginUser();
		knowledge.setKnowledgeStatus(KnowledgeStatus.UNPUBLISHED);
		knowledge.setUser(loginUser);
		knowledge.setTitle(title);
		knowledge.setContent(content);
		knowledge.setDomain(domain);
		knowledge=(Knowledge)commonService.update(knowledge);
		
//		//==============================生成并写入文件=========================//
//		LucenceService lucenceService=SpringContextHolder.getBean("lucenceService");
//		ArrayList<Knowledge> knowledgeList = new ArrayList<Knowledge>();
//		knowledgeList.add(knowledge);
//		lucenceService.generateFile(domain.getId(),knowledgeList , true);
	}
	
	//按钮点击事件监听器
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == save) {
			try {
				executeSave();
				this.getParent().removeWindow(this);
				knowledgeManagement.updateTable(true);
				knowledgeManagement.getTable().setValue(null);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("添加失败");
			}

		} else if (event.getButton() == cancel) {
			this.getParent().removeWindow(this);
		}

	}

}
