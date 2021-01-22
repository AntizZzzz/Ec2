package com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVROptionType;
import com.jiangyifen.ec2.service.eaoservice.IVRActionService;
import com.jiangyifen.ec2.service.eaoservice.IVROptionService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
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
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @Description 描述：用于创建IVR 语音导航流程分支的 界面
 * 
 * @author  jrh
 * @date    2014年2月28日 下午3:55:55
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class AddIvrBranchView extends VerticalLayout implements Button.ClickListener,ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/******************************** 各种Action 类型都需要的组件 **************************/
	private Notification success_notification;					// 成功过提示信息
	private IvrManagement ivrManagement;						// 上级组件
	private OptionGroup branchType_op;							// 创建menu弹出窗口中的组件
	private TextField name_tf;									// menu名称
	private TextArea description_ta;							// menu描述信息
	private Button save_bt;									// 保存按钮
	private Button cancel_bt;									// 取消按钮
	private VerticalLayout changeAble_vlo; 					// 内部组件会发生编号的布局管理器
	
	
	/******************************** 分支按键需要的组件，如果是root分支就不需要改组件 **************************/
	private GridLayout branchKey_gl;							// 分支按键需要的组件
	private ComboBox branchKey_cb;								// 分支按键
	
	/******************************** toExten 类型需要的组件 **************************/
	private GridLayout extenType_gl;							// toExten类型的相关组件
	private ComboBox extens_cb;								// 可选分机
	
	
	/******************************** toQueue 类型需要的组件 **************************/
	private GridLayout queueType_gl;							// toQueue 类型的相关组件
	private ComboBox queues_cb;								// 可选队列
	
	
	/******************************** toMobile 类型需要的组件 **************************/
	private GridLayout mobileType_gl;							// toMobile 类型的相关组件
	private TextField mobile_tf;								// 手机号码
	private ComboBox outlines_cb;								// 可选外线
	
	
	/******************************** toPlayback 类型需要的组件 **************************/
	private GridLayout playbackType_gl;						// toPlayback 类型的相关组件
	private ComboBox playSound_cb;								// 语音文件
	
	
	/******************************** toRead 类型需要的组件 **************************/
	private GridLayout readType_gl;								// toRead 类型的相关组件
	private ComboBox readSound_cb;								// 语音文件
	private ComboBox errorCount_cb;								// 允许按键错误次数
	
	
	/******************************** toReadForAgi 类型需要的组件 **************************/
	private GridLayout readForAgiType_gl;						// toReadForAgi 类型的相关组件
	private ComboBox readAgiSound_cb;							// 语音文件
	private ComboBox errorAgiCount_cb;							// 允许按键错误次数
	private ComboBox agiNames_cb;								// 可选的agi 的名称
	
	/******************************** toVoicemail 类型需要的组件 **************************/
	private GridLayout voicemail_gl;
	private ComboBox voicemailQueues_cb;						// 可选队列 - 对队列里的成员进行随机式的抽选，然后做语音留言操作
	
	private Domain domain;										// 当前用户所属域
	private IVRMenu ivrMenu;									// 要添加IVR分支的 IVRMenu 
	private IVRAction parentAction; 							// 当前选中的分支的上层分支
	private IVRAction currentAction; 							// 当前选中的分支
	private IVRAction rootAction; 								// IVR 树的根
	private Integer currentLayer; 								// 当前IVR 树中被选中的分支所在层级
	
	private IVRAction newAction; 								// 新建的 IVRAction
	private IVROption newOption; 								// 新建的 IVROption
	
	private BeanItemContainer<String> useableKeysContainer;		// 目前可选的 按键 容器
	private BeanItemContainer<String> extenContainer;			// 目前可选的 分机 容器 
	private BeanItemContainer<String> outlineContainer;			// 目前可选的 外线 容器
	private BeanItemContainer<String> queueContainer;			// 目前可选的 队列 容器
    private	BeanItemContainer<SoundFile> soundFileContainer; 	// 语音beanItemContainer
    private	BeanItemContainer<String> agiContainer; 			// agi名称的集合

	private IVRActionService ivrActionService;					// 语音action service
	private IVROptionService ivrOptionService;					// 语音option service
	private SoundFileService soundFileService;					// 语音文件 service
	private SipConfigService sipConfigService;					// 分机、外线  service
	private QueueService queueService;							// 队列 service
	
	public AddIvrBranchView(IvrManagement ivrManagement) {
		this.setSpacing(true);
		this.setWidth("500px");
		this.ivrManagement=ivrManagement;

		domain = SpringContextHolder.getDomain();
		useableKeysContainer = new BeanItemContainer<String>(String.class);
		extenContainer = new BeanItemContainer<String>(String.class);
		outlineContainer = new BeanItemContainer<String>(String.class);
		queueContainer = new BeanItemContainer<String>(String.class);
		soundFileContainer = new BeanItemContainer<SoundFile>(SoundFile.class);
		agiContainer = new BeanItemContainer<String>(String.class);

		ivrActionService = SpringContextHolder.getBean("ivrActionService");
		ivrOptionService = SpringContextHolder.getBean("ivrOptionService");
		soundFileService = SpringContextHolder.getBean("soundFileService");
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		queueService = SpringContextHolder.getBean("queueService");
		
		for(SipConfig exten : sipConfigService.getAllExtsByDomain(domain)) {
			extenContainer.addBean(exten.getName());
		}
		
		for(SipConfig outline : sipConfigService.getAllOutlinesByDomain(domain)) {
			outlineContainer.addBean(outline.getName());
		}
		
		for(Queue queue : queueService.getAllByDomain(domain, true)) {
			queueContainer.addBean(queue.getName());
		}
		
		ArrayList<String> agiNameLs = new ArrayList<String>(IVRAction.AGI_NAMES_MAP.keySet());
		Collections.sort(agiNameLs,  new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		agiContainer.addAll(agiNameLs);
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);

		/*************************** 共用组件 第一部分  ************************/
		this.createCommonGL1(this);

		/*************************** 根据编辑的Action 类型，内部组件可变的容器  ********************/
		changeAble_vlo = new VerticalLayout();
		changeAble_vlo.setSpacing(true);
		this.addComponent(changeAble_vlo);

		/******************************** toExten 类型需要的组件 **************************/
		this.createBranchKeyGL(changeAble_vlo);

		/******************************** toExten 类型需要的组件 **************************/
		this.createExtenTypeGL(changeAble_vlo);
		
		/******************************** toQueue 类型需要的组件 **************************/
		this.createQueueTypeGL(changeAble_vlo);
		
		/******************************** toMobile 类型需要的组件 **************************/
		this.createMobileTypeGL(changeAble_vlo);

		/******************************** toPlayback 类型需要的组件 **************************/
		this.createPlaybackTypeGL(changeAble_vlo);
		
		/******************************** toRead 类型需要的组件 **************************/
		this.createReadTypeGl(changeAble_vlo);

		/******************************** toReadForAgi 类型需要的组件 **************************/
		this.createReadForAgiTypeGl(changeAble_vlo);

		/******************************** toVoicemail 类型需要的组件 **************************/
		this.createVoicemailTypeGl(changeAble_vlo);
		
		/*************************** 共用组件 第二部分  ************************/
		this.createCommonGL2();

		/*************************** 创建操作组件  ************************/
		this.createOperateUi(this);
		
	}

	/**
	 * 共用组件 第一部分
	 */
	private void createCommonGL1(VerticalLayout content) {
		Label caption_lb = new Label("<b><font color='red'>新建流程分支：</font></b>", Label.CONTENT_XHTML);
		caption_lb.setWidth("-1px");
		content.addComponent(caption_lb);
		content.setComponentAlignment(caption_lb, Alignment.TOP_LEFT);
		
		GridLayout common_gl1 = new GridLayout(2, 2);
		common_gl1.setSpacing(true);
		content.addComponent(common_gl1);
		
		// ivrMenu 类型
		Label type_lb = new Label("分支类型：");
		type_lb.setWidth("-1px");
		common_gl1.addComponent(type_lb, 0, 0);
	
		// 创建optionGroup
		branchType_op = new OptionGroup();
		branchType_op.addItem(IVRActionType.toExten);		// 添加Action类型
		branchType_op.addItem(IVRActionType.toQueue);
		branchType_op.addItem(IVRActionType.toMobile);
		branchType_op.addItem(IVRActionType.toPlayback);
		branchType_op.addItem(IVRActionType.toRead);
		branchType_op.addItem(IVRActionType.toReadForAgi);
		branchType_op.addItem(IVRActionType.toVoicemail);
		branchType_op.addItem(IVROptionType.toRepeat);
		branchType_op.addItem(IVROptionType.toReturnPre);
		branchType_op.addItem(IVROptionType.toReturnRoot);
		branchType_op.addStyleName("threecol");
		branchType_op.addListener(this);
		branchType_op.setImmediate(true);
		branchType_op.setNullSelectionAllowed(false);
		branchType_op.addStyleName("myopacity");
		common_gl1.addComponent(branchType_op, 1, 0);
	
		// menu名称label
		Label name_lb = new Label("分支名称：");
		name_lb.setWidth("-1px");
		common_gl1.addComponent(name_lb, 0, 1);
	
		// menu名称textField
		name_tf = new TextField();
		name_tf.setNullRepresentation("");
		name_tf.setMaxLength(100);
		name_tf.setWidth("250px");
		common_gl1.addComponent(name_tf, 1, 1);
	}
	
	/**
	 * 分支按键需要的组件，如果是root分支就不需要改组件
	 */
	private void createBranchKeyGL(VerticalLayout content) {
		branchKey_gl = new GridLayout(2, 1);
		branchKey_gl.setSpacing(true);
		content.addComponent(branchKey_gl);
		
		Label branchKey_lb = new Label("分支按键：");
		branchKey_lb.setWidth("-1px");
		branchKey_gl.addComponent(branchKey_lb, 0, 0);
		
		branchKey_cb = new ComboBox();
		branchKey_cb.setRequired(true);
		branchKey_cb.setWidth("250px");
		branchKey_cb.setNullSelectionAllowed(false);
		branchKey_cb.setContainerDataSource(useableKeysContainer);
		branchKey_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		branchKey_gl.addComponent(branchKey_cb, 1, 0);
	}

	/**
	 * toExten 类型需要的组件
	 */
	private void createExtenTypeGL(VerticalLayout content) {
		extenType_gl = new GridLayout(2, 1);
		extenType_gl.setSpacing(true);
		content.addComponent(extenType_gl);

		// 欢迎词label
		Label exten_lb = new Label("可选分机：");
		exten_lb.setWidth("-1px");
		extenType_gl.addComponent(exten_lb, 0, 0);
		
		extens_cb = new ComboBox();
		extens_cb.setRequired(true);
		extens_cb.setWidth("250px");
		extens_cb.setContainerDataSource(extenContainer);
		extens_cb.setNullSelectionAllowed(false);
		extens_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		extenType_gl.addComponent(extens_cb, 1, 0);
	}

	/**
	 * toQueue 类型需要的组件
	 */
	private void createQueueTypeGL(VerticalLayout content) {
		queueType_gl = new GridLayout(2, 1);
		queueType_gl.setSpacing(true);
		content.addComponent(queueType_gl);
		
		// 欢迎词label
		Label queue_lb = new Label("可选队列：");
		queue_lb.setWidth("-1px");
		queueType_gl.addComponent(queue_lb, 0, 0);
		
		queues_cb = new ComboBox();
		queues_cb.setRequired(true);
		queues_cb.setWidth("250px");
		queues_cb.setContainerDataSource(queueContainer);
		queues_cb.setNullSelectionAllowed(false);
		queues_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		queueType_gl.addComponent(queues_cb, 1, 0);
	}

	/**
	 * toMobile 类型需要的组件
	 */
	private void createMobileTypeGL(VerticalLayout content) {
		mobileType_gl = new GridLayout(2, 2);
		mobileType_gl.setSpacing(true);
		content.addComponent(mobileType_gl);
		
		Label mobile_lb = new Label("电话号码：");
		mobile_lb.setWidth("-1px");
		mobileType_gl.addComponent(mobile_lb, 0, 0);

		mobile_tf = new TextField();
		mobile_tf.setRequired(true);
		mobile_tf.setNullRepresentation("");
		mobile_tf.setMaxLength(20);
		mobile_tf.addValidator(new RegexpValidator("\\d+", "电话号码只能由数字组成"));
		mobile_tf.setValidationVisible(false);
		mobile_tf.setWidth("250px");
		mobileType_gl.addComponent(mobile_tf, 1, 0);
		
		Label outline_lb = new Label("可选外线：");
		outline_lb.setWidth("-1px");
		mobileType_gl.addComponent(outline_lb, 0, 1);
		
		outlines_cb = new ComboBox();
		outlines_cb.setRequired(true);
		outlines_cb.setWidth("250px");
		outlines_cb.setContainerDataSource(outlineContainer);
		outlines_cb.setNullSelectionAllowed(false);
		outlines_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		mobileType_gl.addComponent(outlines_cb, 1, 1);
	}

	/**
	 * toPlayback 类型需要的组件
	 */
	private void createPlaybackTypeGL(VerticalLayout content) {
		playbackType_gl = new GridLayout(2, 1);
		playbackType_gl.setSpacing(true);
		content.addComponent(playbackType_gl);
		
		// 欢迎词label
		Label playSound_lb = new Label("播放语音：");
		playSound_lb.setWidth("-1px");
		playbackType_gl.addComponent(playSound_lb, 0, 0);
		
		// 欢迎词combobox
		playSound_cb = new ComboBox();
		playSound_cb.setRequired(true);
		playSound_cb.setWidth("250px");
		playSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		playSound_cb.setContainerDataSource(soundFileContainer);
		playbackType_gl.addComponent(playSound_cb, 1, 0);
	}

	/**
	 * toRead 类型需要的组件
	 */
	private void createReadTypeGl(VerticalLayout content) {
		readType_gl = new GridLayout(2, 2);
		readType_gl.setSpacing(true);
		content.addComponent(readType_gl);
		
		Label readSound_lb = new Label("播放语音：");
		readSound_lb.setWidth("-1px");
		readType_gl.addComponent(readSound_lb, 0, 0);
	
		readSound_cb = new ComboBox();
		readSound_cb.setRequired(true);
		readSound_cb.setWidth("250px");
		readSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		readSound_cb.setContainerDataSource(soundFileContainer);
		readType_gl.addComponent(readSound_cb, 1, 0);
		
		Label errorCount_lb = new Label("按错次数：");
		errorCount_lb.setWidth("-1px");
		readType_gl.addComponent(errorCount_lb, 0, 1);
		
		errorCount_cb = new ComboBox();
		errorCount_cb.setRequired(true);
		errorCount_cb.setWidth("250px");
		errorCount_cb.setNullSelectionAllowed(false);
		errorCount_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		for(int i = 0; i < 10; i++) {
			errorCount_cb.addItem(i);
		}
		errorCount_cb.setValue(0);
		readType_gl.addComponent(errorCount_cb, 1, 1);
	}
	
	/**
	 * toReadForAgi 类型需要的组件
	 */
	private void createReadForAgiTypeGl(VerticalLayout content) {
		readForAgiType_gl = new GridLayout(2, 3);
		readForAgiType_gl.setSpacing(true);
		content.addComponent(readForAgiType_gl);
		
		Label readAgiSound_lb = new Label("播放语音：");
		readAgiSound_lb.setWidth("-1px");
		readForAgiType_gl.addComponent(readAgiSound_lb, 0, 0);
		
		readAgiSound_cb = new ComboBox();
		readAgiSound_cb.setRequired(true);
		readAgiSound_cb.setWidth("250px");
		readAgiSound_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		readAgiSound_cb.setContainerDataSource(soundFileContainer);
		readForAgiType_gl.addComponent(readAgiSound_cb, 1, 0);
		
		Label errorAgiCount_lb = new Label("按错次数：");
		errorAgiCount_lb.setWidth("-1px");
		readForAgiType_gl.addComponent(errorAgiCount_lb, 0, 1);
		
		errorAgiCount_cb = new ComboBox();
		errorAgiCount_cb.setRequired(true);
		errorAgiCount_cb.setNullSelectionAllowed(false);
		errorAgiCount_cb.setWidth("250px");
		errorAgiCount_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		for(int i = 0; i < 10; i++) {
			errorAgiCount_cb.addItem(i);
		}
		errorAgiCount_cb.setValue(0);
		readForAgiType_gl.addComponent(errorAgiCount_cb, 1, 1);

		Label agiNames_lb = new Label("处理内容：");
		agiNames_lb.setWidth("-1px");
		readForAgiType_gl.addComponent(agiNames_lb, 0, 2);
		
		agiNames_cb = new ComboBox();
		agiNames_cb.setRequired(true);
		agiNames_cb.setWidth("250px");
		agiNames_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		agiNames_cb.setContainerDataSource(agiContainer);
		agiNames_cb.setNullSelectionAllowed(false);
		
		for(String agiName : IVRAction.AGI_NAMES_MAP.keySet()) {
			agiNames_cb.setItemCaption(agiName, IVRAction.AGI_NAMES_MAP.get(agiName));
		}
		readForAgiType_gl.addComponent(agiNames_cb, 1, 2);
	}
	
	/**
	 * toVoicemail 类型需要的组件
	 */
	private void createVoicemailTypeGl(VerticalLayout content) {
		voicemail_gl = new GridLayout(2, 1);
		voicemail_gl.setSpacing(true);
		content.addComponent(voicemail_gl);
		
		// 欢迎词label
		Label queue_lb = new Label("可选队列：");
		queue_lb.setWidth("-1px");
		voicemail_gl.addComponent(queue_lb, 0, 0);
		
		voicemailQueues_cb = new ComboBox();
		voicemailQueues_cb.setRequired(true);
		voicemailQueues_cb.setWidth("250px");
		voicemailQueues_cb.setContainerDataSource(queueContainer);
		voicemailQueues_cb.setNullSelectionAllowed(false);
		voicemailQueues_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		voicemail_gl.addComponent(voicemailQueues_cb, 1, 0);
	}

	/**
	 * 共用组件 第二部分
	 */
	private void createCommonGL2() {
		GridLayout common_gl2 = new GridLayout(2, 1);
		common_gl2.setSpacing(true);
		this.addComponent(common_gl2);
		
		// 描述信息label
		Label description_lb = new Label("分支描述：");
		description_lb.setWidth("-1px");
		common_gl2.addComponent(description_lb, 0, 0);

		// 描述信息
		description_ta = new TextArea();
		description_ta.setNullRepresentation("");
		description_ta.setWidth("250px");
		description_ta.setHeight("50px");
		description_ta.setMaxLength(200);
		common_gl2.addComponent(description_ta, 1, 0);
	}

	/**
	 * 创建操作组件
	 * @param content 窗口布局管理器
	 */
	private void createOperateUi(VerticalLayout content) {
		// 存放下一步，取消按键
		HorizontalLayout operateUi_hlo = new HorizontalLayout();
		operateUi_hlo.setSpacing(true);
		operateUi_hlo.setWidth("-1px");
		content.addComponent(operateUi_hlo);

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
				this.getApplication().getMainWindow().showNotification("对不起，修改语音导航流程的分支 失败！", Notification.TYPE_WARNING_MESSAGE);
				logger.error("jrh 管理员编辑 IVR 语音导航流程的分支 时出现异常--->"+e.getMessage(), e);
			}
		} else if(source == cancel_bt) {
			ivrManagement.updateIVRTreeRightView();
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == branchType_op) {
			changeAble_vlo.removeAllComponents();
			if(rootAction != null) {	// 如果流程的根分支已经存在，则再添加分支，一定是需要按键的
				changeAble_vlo.addComponent(branchKey_gl);
				useableKeysContainer.removeAllItems();
				useableKeysContainer.addAll(ivrOptionService.getUseableKeyByActionId(currentAction.getId(), domain.getId(), null));
			}
			
			// 根据当前选中的类型，修改显示组件
			Object type = branchType_op.getValue();
			if(IVRActionType.toExten.equals(type)) {
				changeAble_vlo.addComponent(extenType_gl);
				
			} else if(IVRActionType.toQueue.equals(type)) {
				changeAble_vlo.addComponent(queueType_gl);
				
			} else if(IVRActionType.toMobile.equals(type)) {
				changeAble_vlo.addComponent(mobileType_gl);
				
			} else if(IVRActionType.toPlayback.equals(type)) {
				changeAble_vlo.addComponent(playbackType_gl);
				
			} else if(IVRActionType.toRead.equals(type)) {
				changeAble_vlo.addComponent(readType_gl);
				errorCount_cb.setValue(0);
				
			} else if(IVRActionType.toReadForAgi.equals(type)) {
				changeAble_vlo.addComponent(readForAgiType_gl);
				errorAgiCount_cb.setValue(0);
				
			} else if(IVRActionType.toVoicemail.equals(type)) {
				changeAble_vlo.addComponent(voicemail_gl);
			}
		}
	}

	/**
	 * 执行保存操作
	 */
	private void excuteSave() {
		String name = StringUtils.trimToNull((String)name_tf.getValue());
		String description = StringUtils.trimToNull((String)description_ta.getValue());
		
		String branchKey = (String) branchKey_cb.getValue();
		if(rootAction != null) {	// 如果已经存在根分支，则必须选择按键
			if(branchKey == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("请选中 分支按键！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
		} else {	// 如果没有根分支，则当前创建的就是根分支
			newAction.setIsRootAction(true);
		}
		
		// 根据当前修改的action类型，重新给action 设值
		Object branchType = branchType_op.getValue();
		if(IVRActionType.toExten.equals(branchType)) {
			String extenName = (String) extens_cb.getValue();
			if(extenName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("分机 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			newAction.setExtenName(extenName);
			newAction.setActionType(IVRActionType.toExten);
			newOption.setIvrOptionType(IVROptionType.toExten);
		} else if(IVRActionType.toQueue.equals(branchType)) {
			String queueName = (String) queues_cb.getValue();
			if(queueName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("队列 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			newAction.setQueueName(queueName);
			newAction.setActionType(IVRActionType.toQueue);
			newOption.setIvrOptionType(IVROptionType.toQueue);
		} else if(IVRActionType.toMobile.equals(branchType)) {
			if(!mobile_tf.isValid()) {
				ivrManagement.getApplication().getMainWindow().showNotification("电话号码 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			String mobileNumber = StringUtils.trimToNull((String) mobile_tf.getValue());
			if(mobileNumber == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("电话号码 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			String outlineName = (String) outlines_cb.getValue();
			if(outlineName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("外线 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			newAction.setMobileNumber(mobileNumber);
			newAction.setOutlineName(outlineName);
			newAction.setActionType(IVRActionType.toMobile);
			newOption.setIvrOptionType(IVROptionType.toMobile);
		} else if(IVRActionType.toPlayback.equals(branchType)) {
			SoundFile playSound = (SoundFile) playSound_cb.getValue();	// 语音文件
			if(playSound == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("语音 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			newAction.setSoundFile(playSound);
			newAction.setActionType(IVRActionType.toPlayback);
			newOption.setIvrOptionType(IVROptionType.toPlayback);
		} else if(IVRActionType.toRead.equals(branchType)) {
			SoundFile readSound = (SoundFile) readSound_cb.getValue();	// 语音文件
			if(readSound == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("语音 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			Integer errorOpportunity = (Integer) errorCount_cb.getValue();
			if(errorOpportunity == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("按错次数 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			newAction.setSoundFile(readSound);
			newAction.setErrorOpportunity(errorOpportunity);
			newAction.setActionType(IVRActionType.toRead);
			newOption.setIvrOptionType(IVROptionType.toRead);
		} else if(IVRActionType.toReadForAgi.equals(branchType)) {
			SoundFile agiSound = (SoundFile) readAgiSound_cb.getValue();	// 语音文件
			if(agiSound == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("语音 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			Integer errorOpportunity = (Integer) errorAgiCount_cb.getValue();
			if(errorOpportunity == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("按错次数 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			String agiName = (String) agiNames_cb.getValue();
			if(agiName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("处理内容 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			// TODO 待定  agi
			newAction.setSoundFile(agiSound);
			newAction.setErrorOpportunity(errorOpportunity);
			newAction.setActionType(IVRActionType.toReadForAgi);
			newAction.setAgiName(agiName);
			newAction.setAgiDescription(agiNames_cb.getItemCaption(agiName));
			newOption.setIvrOptionType(IVROptionType.toReadForAgi);
		} else if(IVRActionType.toVoicemail.equals(branchType)) {
			String queueName = (String) voicemailQueues_cb.getValue();
			if(queueName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("队列 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			newAction.setQueueName(queueName);
			newAction.setActionType(IVRActionType.toVoicemail);
			newOption.setIvrOptionType(IVROptionType.toVoicemail);
		}
		
		// 设置action 的基本属性
		newAction.setIvrActionName(name);
		newAction.setDescription(description);
		newAction.setDomain(domain);
		newAction.setIvrMenu(ivrMenu);
		
		if(rootAction != null) {		// 判断根分支是否存在
			// 设置Option 的基本属性
			newOption.setIvrOptionName(name);
			newOption.setDescription(description);
			newOption.setDomain(domain);
			newOption.setIvrMenu(ivrMenu);
			newOption.setLayerNumber(++currentLayer);
			newOption.setPressNumber(branchKey);
			newOption.setCurrentIvrAction(currentAction);
			newOption.setNextIvrAction(newAction);
			
			if(IVROptionType.toRepeat.equals(branchType)) {	// 如果当前选中的是 【重听、返回上一级、返回主菜单】，则不需要创建新的 Action 
				newOption.setNextIvrAction(currentAction);
				newOption.setIvrOptionType(IVROptionType.toRepeat);
				ivrOptionService.save(newOption);
			} else if(IVROptionType.toReturnPre.equals(branchType)) {
				newOption.setNextIvrAction(parentAction);
				newOption.setIvrOptionType(IVROptionType.toReturnPre);
				ivrOptionService.save(newOption);
			} else if(IVROptionType.toReturnRoot.equals(branchType)) {
				newOption.setNextIvrAction(rootAction);
				newOption.setIvrOptionType(IVROptionType.toReturnRoot);
				ivrOptionService.save(newOption);
			} else {	// 如果当前类型非上面三种，则要创建 action 和 option 两个对象，这二者合起来相当于一个分支
				ivrActionService.createBranch(newAction, newOption);
			}
		} else {	// 如果当前IVR 没有一个根分支，则直接创建一个跟节点即可
			ivrActionService.save(newAction);	// 这其中，在创建根Action 时，内部会修改IVRMenu 的rootActionType
		}
		
		ivrManagement.updateIVRTree();					// 重新构建IVR 树
		success_notification.setCaption("修改语音导航流程的分支 成功！");
		ivrManagement.getApplication().getMainWindow().showNotification(success_notification);
	}
	
	/**
	 * 更新用于添加IVR 导航流程的分支的界面组件，和初始化数据
	 * 
	 * @param ivrMenu			当前操作的IVR 菜单
	 * @param parentAction		当前选中的分支的上层分支
	 * @param currentAction		当前选中的分支
	 * @param rootAction		IVR 树的根
	 * @param currentLayer		当前IVR 树中被选中的分支所在层级
	 */
	public void updateUiDataSouce(IVRMenu ivrMenu, IVRAction parentAction, 
			IVRAction currentAction, IVRAction rootAction, Integer currentLayer) {
		this.ivrMenu = ivrMenu;
		this.parentAction = parentAction;
		this.currentAction = currentAction;
		this.rootAction = rootAction;
		this.currentLayer = currentLayer;
		this.newAction = new IVRAction();
		this.newOption = new IVROption();

		// 更新语音文件对象
		List<SoundFile> soundFiles = soundFileService.getAll(domain);
		soundFileContainer.removeAllItems();
		soundFileContainer.addAll(soundFiles);
		
		clearValueUiStatus();	// 清空组件的信息

		// 编辑分支类型选项
		branchType_op.removeItem(IVROptionType.toRepeat);
		branchType_op.removeItem(IVROptionType.toReturnPre);
		branchType_op.removeItem(IVROptionType.toReturnRoot);
		if(rootAction != null) {
			branchType_op.addItem(IVROptionType.toRepeat);
			if(parentAction != null) {		// 如果上级分支存在，则添加分类：返回上一级
				branchType_op.addItem(IVROptionType.toReturnPre);
			}
			branchType_op.addItem(IVROptionType.toReturnRoot);
		} 
		branchType_op.setValue(null);
		branchType_op.setValue(IVRActionType.toRead);
	}
	
	/**
	 * 每次弹屏，都从新清空组件的信息
	 */
	private void clearValueUiStatus() {
		name_tf.setValue(null);
		description_ta.setValue(null);
		branchKey_cb.setValue(null);
		extens_cb.setValue(null);
		queues_cb.setValue(null);
		mobile_tf.setValue(null);
		outlines_cb.setValue(null);
		playSound_cb.setValue(null);
		readSound_cb.setValue(null);
		errorCount_cb.setValue(0);
		readAgiSound_cb.setValue(null);
		errorAgiCount_cb.setValue(0);
		agiNames_cb.setValue(null);
		voicemailQueues_cb.setValue(null);
	}
}
