package com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.IVRActionService;
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
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述：语音导航流程 创建窗口 IVRMenu
 * 
 * @author  jrh
 * @date    2014年2月27日 上午11:34:46
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class AddIVRMenuWindow extends Window implements Button.ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Notification success_notification;					// 成功过提示信息

	private OptionGroup menuType_op;							// 创建menu弹出窗口中的组件
	private TextField menuName_tf;								// menu名称
	private ComboBox welSound_cb;								// 欢迎词
	private ComboBox cloSound_cb;								// 结束语
	private TextArea description_ta;							// menu描述信息
	
	private Button save_bt;										// 保存按钮
	private Button cancel_bt;									// 取消按钮

	private IvrManagement ivrManagement;
	
	private Domain domain;										// 当前用户所属域
	private User loginUser;										// 当前登录用户
    private	BeanItemContainer<SoundFile> soundFileContainer; 	//语音beanItemContainer

	private IvrMenuService ivrMenuService;
	private SoundFileService soundFileService;					// 语音文件 service
	private IVRActionService ivrActionService;					// 语音action service
	
	public AddIVRMenuWindow(IvrManagement ivrManagement) {
		this.setCaption("添加语音导航流程(IVR)");
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.ivrManagement=ivrManagement;

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		soundFileContainer = new BeanItemContainer<SoundFile>(SoundFile.class);
		
		ivrMenuService = SpringContextHolder.getBean("ivrMenuService");
		soundFileService = SpringContextHolder.getBean("soundFileService");
		ivrActionService = SpringContextHolder.getBean("ivrActionService");
		
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
		GridLayout gridLayout = new GridLayout(2, 5);
		gridLayout.setSpacing(true);
		windowContent.addComponent(gridLayout);
		
		// ivrMenu 类型
		Label type_lb = new Label("IVR类型：");
		type_lb.setWidth("-1px");
		gridLayout.addComponent(type_lb, 0, 0);
	
		// 创建optionGroup
		menuType_op = new OptionGroup();
		menuType_op.addItem(IVRActionType.toExten);		// 添加Action类型
		menuType_op.addItem(IVRActionType.toQueue);
		menuType_op.addItem(IVRActionType.toMobile);
		menuType_op.addItem(IVRActionType.toPlayback);
		menuType_op.addItem(IVRActionType.toRead);
//		TODO 暂时还没有这个功能，以后可以考虑加进来
		menuType_op.addItem(IVRActionType.toReadForAgi);
		menuType_op.addItem("EmptyMenu");					// 自定义类型
		menuType_op.setItemCaption("EmptyMenu", "创建空语音导航");
		menuType_op.addStyleName("threecol");
		menuType_op.setNullSelectionAllowed(false);
		menuType_op.setValue(IVRActionType.toRead);
		gridLayout.addComponent(menuType_op, 1, 0);
	
		// menu名称label
		Label name_lb = new Label("IVR名称：");
		name_lb.setWidth("-1px");
		gridLayout.addComponent(name_lb, 0, 1);
	
		// menu名称textField
		menuName_tf = new TextField();
		menuName_tf.setRequired(true);
		menuName_tf.setNullRepresentation("");
		menuName_tf.setMaxLength(100);
		menuName_tf.setWidth("300px");
		gridLayout.addComponent(menuName_tf, 1, 1);
		
		// 欢迎词label
		Label welSound_lb = new Label("欢迎词：");
		welSound_lb.setWidth("-1px");
		gridLayout.addComponent(welSound_lb, 0, 2);
	
		// 欢迎词combobox
		welSound_cb = new ComboBox();
		welSound_cb.setWidth("300px");
		welSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		welSound_cb.setContainerDataSource(soundFileContainer);
		gridLayout.addComponent(welSound_cb, 1, 2);
		
		// 结束语label
		Label cloSound_lb = new Label("结束语：");
		cloSound_lb.setWidth("-1px");
//		gridLayout.addComponent(cloSound_lb, 0, 3);
	
		// 结束语 combobox
		cloSound_cb = new ComboBox();
		cloSound_cb.setWidth("300px");
		cloSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cloSound_cb.setContainerDataSource(soundFileContainer);
//		gridLayout.addComponent(cloSound_cb, 1, 3);

		// 描述信息label
		Label description_lb = new Label("IVR描述：");
		description_lb.setWidth("-1px");
		gridLayout.addComponent(description_lb, 0, 4);

		// 描述信息
		description_ta = new TextArea();
		description_ta.setNullRepresentation("");
		description_ta.setWidth("300px");
		description_ta.setHeight("50px");
		description_ta.setMaxLength(200);
		gridLayout.addComponent(description_ta, 1, 4);
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
				ivrManagement.getApplication().getMainWindow().showNotification("对不起，创建语音导航流程IVR失败！", Notification.TYPE_WARNING_MESSAGE);
				logger.error("jrh 管理员创建 IVRMenu 时出现异常--->"+e.getMessage(), e);
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
		IVRMenu ivrMenu = new IVRMenu();
		ivrMenu.setDomain(domain);
		ivrMenu.setIvrMenuType(IVRMenuType.customize);
		ivrMenu.setCreateDate(new Date());
		ivrMenu.setCreator(loginUser);
		ivrMenu.setIvrMenuName(menuName);
		ivrMenu.setWelcomeSoundFile(welSound);
		ivrMenu.setCloseSoundFile(cloSound);
		ivrMenu.setDescription(description);

		if (menuType_op.getValue() instanceof IVRActionType) {
			IVRActionType ivrActionType = (IVRActionType) menuType_op.getValue();	// ivrAction类型
			ivrMenu.setRootActionType(ivrActionType);								// 设置根action 的类型
			if(!ivrActionType.equals(IVRActionType.toReadForAgi)) {
				ivrMenuService.createNewIvrByIvrTemplate(ivrMenu, ivrActionType);		// 按模板创建IVRMenu
			} else {
				ivrMenuService.save(ivrMenu);
				
				String defaultAgiName = "dialSpecifiedExtenByIvr.agi";
				String defaultAgiFunc = IVRAction.AGI_NAMES_MAP.get("dialSpecifiedExtenByIvr.agi");
				IVRAction ivrAction = new IVRAction();
				ivrAction.setIsRootAction(true);
				ivrAction.setSoundFile(null);
				ivrAction.setErrorOpportunity(0);
				ivrAction.setActionType(IVRActionType.toReadForAgi);
				ivrAction.setAgiName(defaultAgiName);
				ivrAction.setAgiDescription(defaultAgiFunc);
				ivrAction.setIvrActionName(defaultAgiFunc);
				ivrAction.setDescription(defaultAgiFunc);
				ivrAction.setIvrMenu(ivrMenu);
				ivrAction.setDomain(domain);
				ivrActionService.save(ivrAction);	// 这其中，在创建根Action 时，内部会修改IVRMenu 的rootActionType
			}
		} else {	// 创建一个没有根分支的IVRMenu
			ivrMenuService.save(ivrMenu);
		}
		
		success_notification.setCaption("成功创建语音导航流程！");
		ivrManagement.getApplication().getMainWindow().showNotification(success_notification);

		ivrManagement.refreshTable(true);		// 刷新IVRMenu 的显示表格
		
		// 更新内存信息
		ShareData.ivrMenusMap.put(ivrMenu.getId(), ivrMenu);
		
		ivrManagement.getApplication().getMainWindow().removeWindow(this);	// 关闭弹出的窗口
	}

	/**
	 * 更新当前窗口的某些组件需要的数据源，如语音对象等
	 */
	public void updateUiDataSouce() {
		// 更新语音文件对象
		List<SoundFile> soundFiles = soundFileService.getAll(domain);
		soundFileContainer.removeAllItems();
		soundFileContainer.addAll(soundFiles);
		
		// 清空组件上次选择的内容
		menuType_op.setValue(IVRActionType.toRead);
		menuName_tf.setValue(null);
		welSound_cb.setValue(null);
		cloSound_cb.setValue(null);
		description_ta.setValue("");
	}
}
