package com.jiangyifen.ec2.ui.mgr.mohmanage;

import java.util.Arrays;

import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.service.eaoservice.MusicOnHoldService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.MusicOnHoldManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 编辑呼入队列时的保持音乐组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class EditMusicOnHold extends Window implements ClickListener {
	
	// form 表格中 分机 必须拥有值的字段
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"name", "description"};

	/**
	 * 主要组件输出	
	 */
	//Form输出
	private Form form;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;
	
	/**
	 * 其他参数
	 */
	private MusicOnHold musicOnHold;
	private MusicOnHoldManagement voiceFolderManagement;
	private MusicOnHoldService musicOnHoldService;
	
	public EditMusicOnHold(MusicOnHoldManagement musicOnHoldManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("160px");
		this.setWidth("320px");
		this.setCaption("添加语音文件夹");
		this.voiceFolderManagement = musicOnHoldManagement;
		
		musicOnHoldService = SpringContextHolder.getBean("musicOnHoldService");

		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//From
		form=new Form();
		form.setValidationVisible(false);
		form.setValidationVisibleOnCommit(true);
		form.setInvalidCommitted(false);
		form.setWriteThrough(false);
		form.setImmediate(true);
		form.addStyleName("chb");
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(creatFormFooterComponents());
		windowContent.addComponent(form);
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		musicOnHold = (MusicOnHold) voiceFolderManagement.getleftTable().getValue();
		
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<MusicOnHold>(musicOnHold), Arrays.asList(VISIBLE_PROPERTIES));
		
		// 回显信息
		form.getField("name").setValue(musicOnHold.getName());
		form.getField("name").setReadOnly(true);
	}

	/**
	 * Form 表单下方组建
	 * @return HorizontalLayout 存放组件的布局管理器
	 */
	private HorizontalLayout creatFormFooterComponents() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("100%");

		// 保存按钮
		save = new Button("保存", this);
		save.setStyleName("default");
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);

		return buttonsLayout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			try {
				form.commit();
			} catch (Exception e) {
				return;
			}
			// 执行保存新增加的队列
			boolean saveSuccess = executeSave();
			if(saveSuccess) {
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 * @return
	 */
	private boolean executeSave() {
		try {
			// 保存至数据库
			musicOnHold = musicOnHoldService.update(musicOnHold);
			
			//刷新queueManagement的Table
			voiceFolderManagement.updateTable(true);
			voiceFolderManagement.getleftTable().setValue(null);
		} catch (Exception e) {
			e.printStackTrace();
			this.getApplication().getMainWindow().showNotification("保存语音文件夹失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		return true;
	}
	
	/**
	 * 自定义form 表单中域的构造器
	 * @author jrh
	 */
	private class MyFieldFactory extends DefaultFieldFactory{
		
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("name".equals(propertyId)) {
				TextField nameField = new TextField();
				nameField.setNullRepresentation("");
				nameField.setNullSettingAllowed(true);
				nameField.setWidth("200px");
				nameField.setRequired(true);
				nameField.setCaption("配置名称：");
				nameField.setRequiredError("配置名称不能为空！");
				return nameField;
			} else if("description".equals(propertyId)) {
				TextField field = new TextField();
				field.setNullRepresentation("");
				field.setNullSettingAllowed(true);
				field.setWidth("200px");
				field.setCaption("描述信息：");
				field.setRequired(true);
				field.setRequiredError("描述信息不能为空！");
				field.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,128}$", "description 只能由长度不大于128位的字符组成！"));
				return field;
			} else {
				return null;
			}
		}
	}
}
