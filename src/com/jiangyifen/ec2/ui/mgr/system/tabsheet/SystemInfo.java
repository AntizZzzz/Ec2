package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
/**
 * 显示系统信息
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class SystemInfo extends VerticalLayout implements Button.ClickListener{
	private static Logger logger=LoggerFactory.getLogger(SystemInfo.class);
	
	private static String PAGE_TITLE="pageTitle";
	private static String SYSTEM_TITLE="systemTitle";
	private static String STATUS="status";
	
	//页面标题
	private TextField pageTitleField;
	private Label pageTitleLabel;
	//系统标题
	private TextField systemTitleField;
	private Label systemTitleLabel;
	//状态
	private TextField statusField;
	private Label statusLabel;
	
	//网格输出
	private GridLayout gridLayout;
	//按钮输出
	private HorizontalLayout buttonsLayout;
	
	private Button editButton;
	private Button saveButton;
	private Button cancelButton;
	
	//属性
	private static Properties props = new Properties();
	
	static{
		props = new Properties();
		loadTitleFile("title.properties");	
	}
	
	public static void loadTitleFile(String titleFilename){
		try {
			//初始化配置文件信息
			InputStream inputStream = SystemInfo.class.getClassLoader().getResourceAsStream(titleFilename);
			props.load(inputStream);
			inputStream.close();
			
			//页面标题
			String pageTitle = (String)props.get(PAGE_TITLE);
			if(StringUtils.isEmpty(pageTitle)){ //默认值
				props.put(PAGE_TITLE, "EC2呼叫中心");
			}

			//系统标题
			String systemTitle = (String)props.get(SYSTEM_TITLE);
			if(StringUtils.isEmpty(systemTitle)){ //默认值
				props.put(SYSTEM_TITLE, "Efficient Call");
			}

			//状态栏
			String status= (String)props.get(STATUS);
			if(StringUtils.isEmpty(status)){ //默认值
				props.put(STATUS, "EC2 呼叫中心");
			}
		} catch (Exception e) {
			e.printStackTrace();

			//页面标题
			String pageTitle = (String)props.get(PAGE_TITLE);
			if(StringUtils.isEmpty(pageTitle)){ //默认值
				props.put(PAGE_TITLE, "EC2呼叫中心");
			}

			//系统标题
			String systemTitle = (String)props.get(SYSTEM_TITLE);
			if(StringUtils.isEmpty(systemTitle)){ //默认值
				props.put(SYSTEM_TITLE, "Efficient Call");
			}

			//状态栏
			String status= (String)props.get(STATUS);
			if(StringUtils.isEmpty(status)){ //默认值
				props.put(STATUS, "EC2 呼叫中心");
			}
		}
	}
	
	/**
	 * 构造器
	 */
	public SystemInfo() {
//		loadTitleFile("title.properties");
		this.setSizeFull();
		this.setMargin(true);
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setWidth("100%");
		layout.addComponent(buildSystemInfoEditor());
//		layout.addComponent(buildExampleLayout()); 建议去掉，看着丑
		
		
		this.addComponent(layout);
	}

	//右侧的系统样例图片
	@SuppressWarnings("unused")
	private VerticalLayout buildExampleLayout() {
		VerticalLayout verticalLayout=new VerticalLayout();
		//左侧图片
		Embedded image = new Embedded("",new ThemeResource("../../img/pageinfo_example.png"));
		image.setWidth("120em");
		image.setHeight("55em");
		verticalLayout.addComponent(image);
		return verticalLayout;
	}

	//左侧的系统信息编辑
	private Component buildSystemInfoEditor() {
		Panel panel=new Panel("系统信息设置");
		
		//约束组件
		VerticalLayout constraintLayout=new VerticalLayout();
		constraintLayout.setWidth("100%");
		panel.addComponent(constraintLayout);
		
		gridLayout = new GridLayout(2,1);
//		gridLayout.setWidth("100%");
		gridLayout.setSpacing(true);
		constraintLayout.addComponent(gridLayout);
		
		int column=0;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("<b>页面标题</b>", Label.CONTENT_XHTML), 0, column);
		pageTitleField = new TextField();
		pageTitleField.setWidth("35em");
		pageTitleField.setRequired(true);
		pageTitleField.setNullRepresentation("");
		pageTitleField.setValue(getPageTitle());
		//
		pageTitleLabel=new Label("<font color='#608525'>"+getPageTitle()+"</font>", Label.CONTENT_XHTML);
		pageTitleLabel.setWidth("35em");
		gridLayout.addComponent(pageTitleLabel, 1, column);
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("<b>系统标题</b>", Label.CONTENT_XHTML), 0, column);
		systemTitleField= new TextField();
		systemTitleField.setWidth("35em");
		systemTitleField.setRequired(true);
//		systemTitleField.setInputPrompt("eg：system title");
		systemTitleField.setValue(getSystemTitle());
		systemTitleLabel=new Label("<font color='#608525'>"+getSystemTitle()+"</font>", Label.CONTENT_XHTML);
		systemTitleLabel.setWidth("35em");
		gridLayout.addComponent(systemTitleLabel, 1, column);

		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("<b>系统状态信息</b>", Label.CONTENT_XHTML), 0, column);
		statusField= new TextField();
		statusField.setWidth("35em");
		statusField.setRequired(true);
//		statusField.setInputPrompt("eg：system status");
		statusField.setValue(getStatusString());
		statusLabel=new Label("<font color='#608525'>"+getStatusString()+"</font>", Label.CONTENT_XHTML);
		statusLabel.setWidth("35em");
		gridLayout.addComponent(statusLabel, 1, column);

//		column++;
//		gridLayout.setRows(column+1);
//		Embedded logoEmbedded = new Embedded("",new ThemeResource("../../img/logo_24.png"));
//		gridLayout.addComponent(new Label("<b>系统Logo</b>", Label.CONTENT_XHTML), 0, column);
//		gridLayout.addComponent(logoEmbedded, 1, column);

		column++;
		gridLayout.setRows(column+1);
		buttonsLayout=buildButtonsLayout();
		gridLayout.addComponent(buttonsLayout, 1, column);
		
		return panel;
	}
	
	/**
	 * 创建按钮组件
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout horizontalLayout=new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		
		editButton=new Button("编辑信息");
		editButton.addListener(this);
		
		saveButton=new Button("保存");
		saveButton.addListener(this);
		
		cancelButton=new Button("取消");
		cancelButton.addListener(this);
		
		horizontalLayout.addComponent(editButton);
		return horizontalLayout;
	}

	/**
	 * 设置是否可以编辑
	 * @param b
	 */
	private void setComponentsEnabled(boolean isEnabled) {
		if(isEnabled){
			gridLayout.replaceComponent(pageTitleLabel, pageTitleField);
			gridLayout.replaceComponent(systemTitleLabel, systemTitleField);
			gridLayout.replaceComponent(statusLabel, statusField);
		}else{
			gridLayout.replaceComponent(pageTitleField,pageTitleLabel);
			gridLayout.replaceComponent(systemTitleField,systemTitleLabel);
			gridLayout.replaceComponent(statusField,statusLabel);
		}
	}
	
	/**
	 * 获取PageTitle
	 * @return
	 */
	public static String getPageTitle(){
		return props.getProperty(PAGE_TITLE);
	}
	
	/**
	 * 获取SystemTitle
	 * @return
	 */
	public static String getSystemTitle(){
		return props.getProperty(SYSTEM_TITLE);
	}
	
	/**
	 * 获取Status string
	 * @return
	 */
	public static String getStatusString(){
		return props.getProperty(STATUS);
	}
	
	/**
	 * 持久化标题等配置
	 */
	private Boolean persistInfoAndRefresh() {
		//验证参数有效性
		String pageTitle=(String)pageTitleField.getValue();
		if(StringUtils.isEmpty(pageTitle)){
			NotificationUtil.showWarningNotification(this, "页面标题不能为空");
			return false;
		}
		String systemTitle=(String)systemTitleField.getValue();
		if(StringUtils.isEmpty(systemTitle)){
			NotificationUtil.showWarningNotification(this, "系统标题不能为空");
			return false;
		}
		String statusStr=(String)statusField.getValue();
		if(StringUtils.isEmpty(statusStr)){
			NotificationUtil.showWarningNotification(this, "系统状态信息不能为空");
			return false;
		}
		
		
		try {
			URL resourceurl = SystemLicence.class.getResource("/title.properties");
logger.info("chb: SystemInfo upgrade "+resourceurl.getPath());
			OutputStream fos = new FileOutputStream(resourceurl.getPath());
			props.store(fos, "test");
			
			//store
			props.put(PAGE_TITLE, pageTitle);
			props.put(SYSTEM_TITLE, systemTitle);
			props.put(STATUS, statusStr);
			props.store(fos, "title");
			
		} catch (Exception e) {
			e.printStackTrace();
			NotificationUtil.showWarningNotification(this, "系统信息更新失败");
			return false;
		}
		
		//更新组件内容
		pageTitleField.setValue(getPageTitle());
		pageTitleLabel.setValue("<font color='#608525'>"+getPageTitle()+"</font>");
		
		systemTitleField.setValue(getSystemTitle());
		systemTitleLabel.setValue("<font color='#608525'>"+getSystemTitle()+"</font>");
		
		statusField.setValue(getStatusString());
		statusLabel.setValue("<font color='#608525'>"+getStatusString()+"</font>");
		NotificationUtil.showWarningNotification(this, "系统信息更新成功，重新登陆生效");
		return true;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==editButton){
			setComponentsEnabled(true);
			buttonsLayout.removeAllComponents();
			buttonsLayout.addComponent(saveButton);
			buttonsLayout.addComponent(cancelButton);
		}else if(event.getButton()==saveButton){
			Boolean isSuccess=persistInfoAndRefresh();
			if(isSuccess){
				setComponentsEnabled(false);
				buttonsLayout.removeAllComponents();
				buttonsLayout.addComponent(editButton);
			}
		}else if(event.getButton()==cancelButton){
			setComponentsEnabled(false);
			buttonsLayout.removeAllComponents();
			buttonsLayout.addComponent(editButton);
		}
	}


}
