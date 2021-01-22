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
 * @Description 描述：用于编辑IVR 语音导航流程分支的 界面
 * 
 * @author  jrh
 * @date    2014年2月28日 下午3:59:43
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class EditIvrBranchView extends VerticalLayout implements Button.ClickListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/******************************** 各种Action 类型都需要的组件 **************************/
	private Notification success_notification;					// 成功过提示信息
	private IvrManagement ivrManagement;						// 上级组件
	private OptionGroup branchType_op;							// 创建menu弹出窗口中的组件
	private TextField name_tf;									// menu名称
	private TextArea description_ta;							// menu描述信息
	private Button edit_bt;									// 编辑按键
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
	private GridLayout readType_gl;							// toRead 类型的相关组件
	private ComboBox readSound_cb;								// 语音文件
	private ComboBox errorCount_cb;							// 允许按键错误次数
	
	
	/******************************** toReadForAgi 类型需要的组件 **************************/
	private GridLayout readForAgiType_gl;						// toReadForAgi 类型的相关组件
	private ComboBox readAgiSound_cb;							// 语音文件
	private ComboBox errorAgiCount_cb;							// 允许按键错误次数
	private ComboBox agiNames_cb;								// 可选的agi
	
	/******************************** toVoicemail 类型需要的组件 **************************/
	private GridLayout voicemail_gl;
	private ComboBox voicemailQueues_cb;						// 可选队列 - 对队列里的成员进行随机式的抽选，然后做语音留言操作
	
	private Domain domain;										// 当前用户所属域
	private IVRAction selectedIvrAction; 						// 待编辑的 IVRAction
	private IVROption selectedIvrOption; 						// 待编辑的 IVROption
	
	private BeanItemContainer<String> useableKeysContainer;	// 目前可选的 按键 容器
	private BeanItemContainer<String> extenContainer;			// 目前可选的 分机 容器 
	private BeanItemContainer<String> outlineContainer;		// 目前可选的 外线 容器
	private BeanItemContainer<String> queueContainer;			// 目前可选的 队列 容器
    private	BeanItemContainer<SoundFile> soundFileContainer;// 语音beanItemContainer
    private	BeanItemContainer<String> agiContainer; 		// agi名称的集合

	private IVRActionService ivrActionService;					// 语音action service
	private IVROptionService ivrOptionService;					// 语音option service
	private SoundFileService soundFileService;					// 语音文件 service
	private SipConfigService sipConfigService;					// 分机、外线  service
	private QueueService queueService;							// 队列 service
	
	public EditIvrBranchView(IvrManagement ivrManagement) {
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
		Label caption_lb = new Label("<b><font color='blue'>编辑当前选中的流程分支：</font></b>", Label.CONTENT_XHTML);
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
		branchType_op.setNullSelectionAllowed(false);
		branchType_op.setReadOnly(true);
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

		Label agis_lb = new Label("处理内容：");
		agis_lb.setWidth("-1px");
		readForAgiType_gl.addComponent(agis_lb, 0, 2);
		
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
		edit_bt = new Button("编辑", this);
		edit_bt.setStyleName("default");
		edit_bt.setImmediate(true);
		operateUi_hlo.addComponent(edit_bt);
		operateUi_hlo.setComponentAlignment(edit_bt, Alignment.BOTTOM_LEFT);

		// 保存按键
		save_bt = new Button("保存", this);
		save_bt.setStyleName("default");
		save_bt.setImmediate(true);
		save_bt.setVisible(false);
		operateUi_hlo.addComponent(save_bt);
		operateUi_hlo.setComponentAlignment(save_bt, Alignment.BOTTOM_LEFT);

		// 取消menu
		cancel_bt = new Button("取消", this);
		cancel_bt.setImmediate(true);
		cancel_bt.setVisible(false);
		operateUi_hlo.addComponent(cancel_bt);
		operateUi_hlo.setComponentAlignment(cancel_bt, Alignment.BOTTOM_LEFT);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit_bt) {
			updateValueUiStatus(true);
		} else if(source == save_bt) {
			try {
				excuteSave();
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("对不起，修改语音导航流程的分支 失败！", Notification.TYPE_WARNING_MESSAGE);
				logger.error("jrh 管理员编辑 IVR 语音导航流程的分支 时出现异常--->"+e.getMessage(), e);
			}
		} else if(source == cancel_bt) {
			updateValueUiStatus(false);
		}
	}

	/**
	 * 改变操作按键的可见性、已经操作组件的可用或可见性
	 * @param visible  是否可见、是否可用
	 */
	private void updateValueUiStatus(boolean visible) {
		edit_bt.setVisible(!visible);
		save_bt.setVisible(visible);
		cancel_bt.setVisible(visible);
		name_tf.setReadOnly(!visible);
		description_ta.setReadOnly(!visible);
		branchKey_cb.setReadOnly(!visible);
		extens_cb.setReadOnly(!visible);
		queues_cb.setReadOnly(!visible);
		mobile_tf.setReadOnly(!visible);
		outlines_cb.setReadOnly(!visible);
		playSound_cb.setReadOnly(!visible);
		readSound_cb.setReadOnly(!visible);
		errorCount_cb.setReadOnly(!visible);
		readAgiSound_cb.setReadOnly(!visible);
		errorAgiCount_cb.setReadOnly(!visible);
		agiNames_cb.setReadOnly(!visible);
		voicemailQueues_cb.setReadOnly(!visible);
	}

	/**
	 * 执行保存操作
	 */
	private void excuteSave() {
		String name = StringUtils.trimToNull((String)name_tf.getValue());
		String description = StringUtils.trimToNull((String)description_ta.getValue());
		
		selectedIvrAction.setIvrActionName(name);
		selectedIvrAction.setDescription(description);
		
		if(selectedIvrOption != null) {
			selectedIvrOption.setIvrOptionName(name);
			selectedIvrOption.setDescription(description);
			
			String branchKey = (String) branchKey_cb.getValue();
			if(branchKey == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("请选中 分支按键！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			selectedIvrOption.setPressNumber(branchKey);
			IVROptionType optionType = selectedIvrOption.getIvrOptionType();
			if(IVROptionType.toRepeat.equals(optionType)) {
				ivrOptionService.update(selectedIvrOption);
				return;
			} else if(IVROptionType.toReturnPre.equals(optionType)) {
				ivrOptionService.update(selectedIvrOption);
			} else if(IVROptionType.toReturnRoot.equals(optionType)) {
				ivrOptionService.update(selectedIvrOption);
				return;
			}
		}
		
		// 根据当前修改的action类型，重新给action 设值
		IVRActionType ivrActionType = selectedIvrAction.getActionType();
		if(IVRActionType.toExten.equals(ivrActionType)) {
			String extenName = (String) extens_cb.getValue();
			if(extenName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("分机 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			selectedIvrAction.setExtenName(extenName);
		} else if(IVRActionType.toQueue.equals(ivrActionType)) {
			String queueName = (String) queues_cb.getValue();
			if(queueName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("队列 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			selectedIvrAction.setQueueName(queueName);
		} else if(IVRActionType.toMobile.equals(ivrActionType)) {
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

			selectedIvrAction.setMobileNumber(mobileNumber);
			selectedIvrAction.setOutlineName(outlineName);
		} else if(IVRActionType.toPlayback.equals(ivrActionType)) {
			SoundFile playSound = (SoundFile) playSound_cb.getValue();	// 语音文件
			if(playSound == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("语音 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			selectedIvrAction.setSoundFile(playSound);
		} else if(IVRActionType.toRead.equals(ivrActionType)) {
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

			selectedIvrAction.setSoundFile(readSound);
			selectedIvrAction.setErrorOpportunity(errorOpportunity);
		} else if(IVRActionType.toReadForAgi.equals(ivrActionType)) {
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
//			TODO
			selectedIvrAction.setSoundFile(agiSound);
			selectedIvrAction.setErrorOpportunity(errorOpportunity);
			selectedIvrAction.setActionType(IVRActionType.toReadForAgi);
			selectedIvrAction.setAgiName(agiName);
			selectedIvrAction.setAgiDescription(agiNames_cb.getItemCaption(agiName));
		}  else if(IVRActionType.toVoicemail.equals(ivrActionType)) {
			String queueName = (String) voicemailQueues_cb.getValue();
			if(queueName == null) {
				ivrManagement.getApplication().getMainWindow().showNotification("队列 不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			selectedIvrAction.setQueueName(queueName);
		}
		
		if(selectedIvrOption != null) {
			ivrActionService.updateIvrBranch(selectedIvrAction, selectedIvrOption);		// 将当前分支对应的 action 和 option 放在一个 事物下进行保存
		} else {	// 这表明修改的是主分支
			ivrActionService.update(selectedIvrAction);
		}
		updateValueUiStatus(false);						// 改变界面显示组件
		ivrManagement.updateIVRTree();					// 重新构建IVR 树
		success_notification.setCaption("修改语音导航流程的分支 成功！");
		ivrManagement.getApplication().getMainWindow().showNotification(success_notification);
	}

	/**
	 * 更新当前窗口的某些组件需要的数据源，如语音对象等
	 * 更新界面组件的状态，如可见性，默认值等
	 * 
	 * @param currentIvrBranch	当前选中的IVR 树的分支【该值可能是IVROption、也可能是IVRAction(当选中的是根分支时)】
	 * @param operateType		当前选中分支要执行的操作是什么:编辑、浏览【edit、scan】
	 */	
	public void updateUiDataSouce(Object currentIvrBranch, String operateType) {
		if(currentIvrBranch instanceof IVRAction) {	
			this.selectedIvrOption = null;
			this.selectedIvrAction = (IVRAction) currentIvrBranch;
			
		} else if(currentIvrBranch instanceof IVROption) {
			this.selectedIvrOption = (IVROption) currentIvrBranch;
			this.selectedIvrAction = selectedIvrOption.getNextIvrAction();
		}
		
		// 更新语音文件对象
		List<SoundFile> soundFiles = soundFileService.getAll(domain);
		soundFileContainer.removeAllItems();
		soundFileContainer.addAll(soundFiles);
		updateValueUiStatus(true);
		
		// 回显组件值
		changeAble_vlo.removeAllComponents();
		branchType_op.setReadOnly(false);
		this.rebuildChangedUi(selectedIvrOption, selectedIvrAction, soundFiles);
		branchType_op.setReadOnly(true);
		
		// 根据操作类型修改界面组件的状态
		if("edit".equals(operateType)) {
			updateValueUiStatus(true);
		} else if("scan".equals(operateType)) {
			updateValueUiStatus(false);
		}
	}

	/**
	 * 根据 当前选中分支对应的IVROption 或 IVRAction 更新界面组件
	 * 
	 * @param option		当前选中分支对应的IVROption
	 * @param action		当前选中分支对应的IVRAction
	 * @param soundFiles	当前所有的语音文件
	 */
	private void rebuildChangedUi(IVROption option, IVRAction action, List<SoundFile> soundFiles) {
		// 如果当前选中的是分支是 IVR 树的非根分支，则需要判断特殊类型 【重听、返回上一级、返回主菜单】
		if(option != null) {
			// 如果 IVROption 选项不为空，标识非根分支，需要设置按键
			IVRAction preAction = option.getCurrentIvrAction();
			changeAble_vlo.addComponent(branchKey_gl);
			useableKeysContainer.removeAllItems();
			useableKeysContainer.addAll(ivrOptionService.getUseableKeyByActionId(preAction.getId(), domain.getId(), option.getPressNumber()));
			branchKey_cb.setValue(option.getPressNumber());
			
			IVROptionType optionType = option.getIvrOptionType();
			if(IVROptionType.toRepeat.equals(optionType)) {
				branchType_op.setValue(IVROptionType.toRepeat);
				return;
			} else if(IVROptionType.toReturnPre.equals(optionType)) {
				branchType_op.setValue(IVROptionType.toReturnPre);
				return;
			} else if(IVROptionType.toReturnRoot.equals(optionType)) {
				branchType_op.setValue(IVROptionType.toReturnPre);
				return;
			}
		}
		
		/******************** 则根据IVR 树中的当前选中分支对应的 Action的类型设置组件 ***************/
		
		IVRActionType ivrActionType = selectedIvrAction.getActionType();
		branchType_op.setValue(ivrActionType);
		name_tf.setValue(selectedIvrAction.getIvrActionName());
		description_ta.setValue(selectedIvrAction.getDescription());
		SoundFile soundFile = selectedIvrAction.getSoundFile();
		
		if(IVRActionType.toExten.equals(ivrActionType)) {
			changeAble_vlo.addComponent(extenType_gl);
			extens_cb.setValue(selectedIvrAction.getExtenName());
			
		} else if(IVRActionType.toQueue.equals(ivrActionType)) {
			changeAble_vlo.addComponent(queueType_gl);
			queues_cb.setValue(selectedIvrAction.getQueueName());
			
		} else if(IVRActionType.toMobile.equals(ivrActionType)) {
			changeAble_vlo.addComponent(mobileType_gl);
			mobile_tf.setValue(selectedIvrAction.getMobileNumber());
			outlines_cb.setValue(selectedIvrAction.getOutlineName());
			
		} else if(IVRActionType.toPlayback.equals(ivrActionType)) {
			changeAble_vlo.addComponent(playbackType_gl);
			if(soundFile == null) {
				playSound_cb.setValue(null);
			} else {
				for(SoundFile sound : soundFiles) {
					Long sid = sound.getId();
					if(sid.equals(soundFile.getId())) {
						playSound_cb.setValue(sound); 
						break;
					}
				}
			}
			
		} else if(IVRActionType.toRead.equals(ivrActionType)) {
			changeAble_vlo.addComponent(readType_gl);
			if(soundFile == null) {
				readSound_cb.setValue(null);
			} else {
				for(SoundFile sound : soundFiles) {
					Long sid = sound.getId();
					if(sid.equals(soundFile.getId())) {
						readSound_cb.setValue(sound); 
						break;
					}
				}
			}
			errorCount_cb.setValue(selectedIvrAction.getErrorOpportunity());
			
		} else if(IVRActionType.toReadForAgi.equals(ivrActionType)) {
			changeAble_vlo.addComponent(readForAgiType_gl);
			if(soundFile == null) {
				readAgiSound_cb.setValue(null);
			} else {
				for(SoundFile sound : soundFiles) {
					Long sid = sound.getId();
					if(sid.equals(soundFile.getId())) {
						readAgiSound_cb.setValue(sound); 
						break;
					}
				}
			}
			errorAgiCount_cb.setValue(selectedIvrAction.getErrorOpportunity());
			agiNames_cb.setValue(selectedIvrAction.getAgiName());
			
		} else if(IVRActionType.toVoicemail.equals(ivrActionType)) {
			changeAble_vlo.addComponent(voicemail_gl);
			voicemailQueues_cb.setValue(selectedIvrAction.getQueueName());
			
		}
	}
	
}
