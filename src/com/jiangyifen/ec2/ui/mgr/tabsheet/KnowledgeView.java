package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.mgr.LucenceService;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 知识库查看页面
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class KnowledgeView extends VerticalLayout implements
		Button.ClickListener {
	// 搜索的组件
	private TextField keyWord;
	private Button search;
	
	//简略信息
	private Panel simplePanel;
	
	//详细内容
	private Panel detailInfoPanel;
	private Label detailLabel;

	public KnowledgeView() {
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSizeFull();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索
		constrantLayout.addComponent(buildSearchLayout());

		//主界面输出
		HorizontalLayout mainLayout=new HorizontalLayout();
		mainLayout.setSizeFull();
		constrantLayout.addComponent(mainLayout);
		constrantLayout.setExpandRatio(mainLayout, 1.0f);
		
		// 搜索简略信息
		simplePanel=buildSimpleInfoLayout();
		simplePanel.setWidth("25em");
		simplePanel.setHeight("100%");
		mainLayout.addComponent(simplePanel);

//		constrantLayout.addComponent(new Label("详细信息"));

		// 搜索详细
		detailInfoPanel=buildDetailInfoLayout();
		detailInfoPanel.setSizeFull();
		mainLayout.addComponent(detailInfoPanel);
		mainLayout.setExpandRatio(detailInfoPanel, 1.0f);
	}

	/**
	 * 搜索简略信息
	 * 
	 * @return
	 */
	private Panel buildSimpleInfoLayout() {
		Panel simplePanel=new Panel();
		return simplePanel;
	}

	/**
	 * 搜索详细
	 * 
	 * @return
	 */
	private Panel buildDetailInfoLayout() {
		Panel detailInfoPanel=new Panel();
		detailLabel=new Label("",Label.CONTENT_XHTML);
		detailInfoPanel.addComponent(detailLabel);
		return detailInfoPanel;
	}

	/**
	 * 创建主窗口的内容输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
//		mainLayout.setMargin(true);
//		mainLayout.setSpacing(true);

		// 关键字Label
		mainLayout.addComponent(new Label("请输入关键字："));

		// 关键字组件
		keyWord = new TextField();
		keyWord.setNullRepresentation("");
		mainLayout.addComponent(keyWord);

		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);

		// 搜索按钮
		search = new Button("搜索");
		search.addListener(this);
		buttonConstraintLayout.addComponent(search);
		return mainLayout;
	}
	
	/**
	 * 显示知识库是更新显示
	 */
	public void update() {
		 simplePanel.removeAllComponents();
		 detailInfoPanel.removeAllComponents();
		 keyWord.setValue(null);
	}

	/**
	 * 执行搜索操作
	 * 
	 * @throws Exception
	 */
	private void executeSearch() throws Exception {
		// 取得关键词
		String keyWordStr=StringUtils.trimToEmpty((String)keyWord.getValue());
		if(keyWordStr.equals("")){
			NotificationUtil.showWarningNotification(this,"关键词不能为空！");
			return;
		}
		
		//执行搜索操作
		LucenceService lucenceService=SpringContextHolder.getBean("lucenceService");
		Domain domain=SpringContextHolder.getDomain();
		String knowledgebase=(String)Config.props.get("knowledgebase");
		knowledgebase=knowledgebase+domain.getId()+"/";
		String indexDir=knowledgebase+"index/";
		List<String> resultMsg =new ArrayList<String>();
		try {
			resultMsg = lucenceService.search(indexDir, keyWordStr);
		} catch (Exception e) {
			NotificationUtil.showWarningNotification(this,"搜索出现错误！");
			e.printStackTrace();
			return;
		}

		if(resultMsg.size()<1){
			NotificationUtil.showWarningNotification(this,"请确认先向知识库中添加知识！");
		}
		
		//更新结果的显示
		simplePanel.removeAllComponents();
		for(int i=0;i<resultMsg.size()-1;i++){
			String orginalResult=resultMsg.get(i);
			if(StringUtils.trimToEmpty(orginalResult).equals(""))
				continue;
			String changedResult=orginalResult.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "").replaceAll("[(/>)<]", "");
			
			//显示的按钮
			String buttonCaption=changedResult;
			if(changedResult.length()>15){
				buttonCaption=changedResult.substring(0,15);
				buttonCaption+="...";
			}
			Button msgButton=new Button(buttonCaption);
			msgButton.setData(orginalResult);
			msgButton.addListener(this);
			msgButton.setStyleName(BaseTheme.BUTTON_LINK);
			simplePanel.addComponent(msgButton);
			
			//显示的label
			String labelCaption=changedResult;
			if(changedResult.length()>50){
				labelCaption=changedResult.substring(0,50);
				labelCaption+="...";
			}
			simplePanel.addComponent(new Label(labelCaption));
			
		}
		
		simplePanel.addComponent(new Label(resultMsg.get(resultMsg.size()-1)));
		
	}

	// 监听按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == search) {
			try {
				executeSearch();
			} catch (Exception e) {
				this.getApplication().getMainWindow()
						.showNotification("搜索出现错误！");
				e.printStackTrace();
			}
		}else{
			String content=(String)event.getButton().getData();
			detailLabel.setValue(content);
		}
	}

}
