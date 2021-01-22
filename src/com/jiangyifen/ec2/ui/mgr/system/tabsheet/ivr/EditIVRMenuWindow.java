package com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述：语音导航流程 编辑窗口 IVRMenu
 * 
 * @author  jrh
 * @date    2014年2月27日 下午7:24:06
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class EditIVRMenuWindow extends Window implements Button.ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Notification success_notification;					// 成功过提示信息

	private TextField menuName_tf;								// menu名称
	private ComboBox welSound_cb;								// 欢迎词
	private ComboBox cloSound_cb;								// 结束语
	private TextArea description_ta;							// menu描述信息
	
	private Button save_bt;										// 保存按钮
	private Button cancel_bt;									// 取消按钮

	private IvrManagement ivrManagement;

	private IVRMenu ivrMenu;									// 待编辑的语音菜单实体
	private Domain domain;										// 当前用户所属域
    private	BeanItemContainer<SoundFile> soundFileContainer; 	// 语音beanItemContainer

	private IvrMenuService ivrMenuService;
	private SoundFileService soundFileService;					// 语音文件 service
	
	public EditIVRMenuWindow(IvrManagement ivrManagement) {
		this.setCaption("添加语音导航流程(IVR)");
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.ivrManagement=ivrManagement;

		domain = SpringContextHolder.getDomain();
		soundFileContainer = new BeanItemContainer<SoundFile>(SoundFile.class);
		
		ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
		soundFileService = SpringContextHolder.getBean("soundFileService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		// 创建menu时弹出的窗口中最外层组件
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// 创建填写IVRMenu 信息的相关组件
		this.createIvrMenuMainUi(windowContent);

		// 创建操作组件
		this.createOperateUi(windowContent);
		
	}

	/**
	 * 创建填写IVRMenu 信息的相关组件
	 * @param windowContent 窗口布局管理器
	 */
	private void createIvrMenuMainUi(VerticalLayout windowContent) {
		GridLayout gridLayout = new GridLayout(2, 4);
		gridLayout.setSpacing(true);
		windowContent.addComponent(gridLayout);
	
		// menu名称label
		Label name_lb = new Label("IVR名称：");
		name_lb.setWidth("-1px");
		gridLayout.addComponent(name_lb, 0, 0);
	
		// menu名称textField
		menuName_tf = new TextField();
		menuName_tf.setRequired(true);
		menuName_tf.setNullRepresentation("");
		menuName_tf.setMaxLength(100);
		menuName_tf.setWidth("250px");
		gridLayout.addComponent(menuName_tf, 1, 0);
	
		// 欢迎词label
		Label welSound_lb = new Label("欢迎词：");
		welSound_lb.setWidth("-1px");
		gridLayout.addComponent(welSound_lb, 0, 1);
	
		// 欢迎词combobox
		welSound_cb = new ComboBox();
		welSound_cb.setWidth("250px");
		welSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		welSound_cb.setContainerDataSource(soundFileContainer);
		gridLayout.addComponent(welSound_cb, 1, 1);
		
		// 结束语label
		Label cloSound_lb = new Label("结束语：");
		cloSound_lb.setWidth("-1px");
//		gridLayout.addComponent(cloSound_lb, 0, 2);
	
		// 结束语 combobox
		cloSound_cb = new ComboBox();
		cloSound_cb.setWidth("250px");
		cloSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cloSound_cb.setContainerDataSource(soundFileContainer);
//		gridLayout.addComponent(cloSound_cb, 1, 2);

		// 描述信息label
		Label description_lb = new Label("IVR描述：");
		description_lb.setWidth("-1px");
		gridLayout.addComponent(description_lb, 0, 3);

		// 描述信息
		description_ta = new TextArea();
		description_ta.setNullRepresentation("");
		description_ta.setWidth("250px");
		description_ta.setHeight("50px");
		description_ta.setMaxLength(200);
		gridLayout.addComponent(description_ta, 1, 3);
	}

	/**
	 * 创建操作组件
	 * @param windowContent 窗口布局管理器
	 */
	private void createOperateUi(VerticalLayout windowContent) {
		// 存放下一步，取消按键
		HorizontalLayout operateUi_hlo = new HorizontalLayout();
		operateUi_hlo.setSpacing(true);
		operateUi_hlo.setWidth("-1px");
		windowContent.addComponent(operateUi_hlo);

		// 保存按键
		save_bt = new Button("保存", this);
		save_bt.setStyleName("default");
		save_bt.setImmediate(true);
		operateUi_hlo.addComponent(save_bt);
		operateUi_hlo.setComponentAlignment(save_bt, Alignment.BOTTOM_LEFT);

		// 取消menu
		cancel_bt = new Button("取消", this);
		cancel_bt.setImmediate(true);
		operateUi_hlo.addComponent(cancel_bt);
		operateUi_hlo.setComponentAlignment(cancel_bt, Alignment.BOTTOM_LEFT);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_bt) {
			try {
				excuteSave();
			} catch (Exception e) {
				e.printStackTrace();
				ivrManagement.getApplication().getMainWindow().showNotification("对不起，编辑语音导航流程IVR失败！", Notification.TYPE_WARNING_MESSAGE);
				logger.error("jrh 管理员编辑 IVRMenu 时出现异常--->"+e.getMessage(), e);
			}
		} else if(source == cancel_bt) {
			ivrManagement.getApplication().getMainWindow().removeWindow(this);	// 关闭弹出的窗口
		}
	}

	/**
	 * 执行保存操作
	 */
	private void excuteSave() {
		String menuName = StringUtils.trimToEmpty((String) menuName_tf.getValue());	// 名称
		if ("".equals(menuName)) {
			ivrManagement.getApplication().getMainWindow().showNotification("请输入IVR 的名称", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		SoundFile welSound = (SoundFile) welSound_cb.getValue();	// 欢迎词
		SoundFile cloSound = (SoundFile) cloSound_cb.getValue();	// 结束语
		String description = StringUtils.trimToEmpty((String) description_ta.getValue());	// 描述信息
		
		// ivrMenu实体
		ivrMenu.setIvrMenuName(menuName);
		ivrMenu.setWelcomeSoundFile(welSound);
		ivrMenu.setCloseSoundFile(cloSound);
		ivrMenu.setDescription(description);
		ivrMenuService.update(ivrMenu);
		
		// 更新内存信息
		try {
			ShareData.ivrMenusMap.put(ivrMenu.getId(), ivrMenu);
		} catch (Exception e) {
			logger.error("jrh 更新ShareData.ivrMenusMap 时出现异常！！内存可能没有同步成功!!!!");
		}
		
		success_notification.setCaption("成功编辑语音导航流程！");
		ivrManagement.getApplication().getMainWindow().showNotification(success_notification);
		
		ivrManagement.refreshTable(false);		// 刷新IVRMenu 的显示表格
		ivrManagement.getApplication().getMainWindow().removeWindow(this);	// 关闭弹出的窗口
	}

	/**
	 * 更新当前窗口的某些组件需要的数据源，如语音对象等
	 */
	public void updateUiDataSouce(IVRMenu ivrMenu) {
		this.ivrMenu = ivrMenu;
		// 更新语音文件对象
		List<SoundFile> soundFiles = soundFileService.getAll(domain);
		soundFileContainer.removeAllItems();
		soundFileContainer.addAll(soundFiles);

		// 回显IVRMenu 的名称、描述，以及欢迎词跟结束语
		menuName_tf.setValue(ivrMenu.getIvrMenuName());
		description_ta.setValue(ivrMenu.getDescription());
		
		SoundFile welSound = ivrMenu.getWelcomeSoundFile();	// 欢迎词
		SoundFile cloSound = ivrMenu.getCloseSoundFile();	// 结束语
		
		for(SoundFile soundFile : soundFiles) {
			Long sid = soundFile.getId();
			if(welSound != null && sid.equals(welSound.getId())) {
				welSound_cb.setValue(soundFile);
			}
			if(cloSound != null && sid.equals(cloSound.getId())) {
				cloSound_cb.setValue(soundFile);
			}
		}
	}
}
