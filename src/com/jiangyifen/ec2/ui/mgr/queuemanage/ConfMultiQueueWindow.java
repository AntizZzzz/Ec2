package com.jiangyifen.ec2.ui.mgr.queuemanage;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.MusicOnHoldService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.QueueManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述：批量修改队列配置项
 * 
 * @author  jrh
 * @date    2014年1月21日 下午 06:32:13
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class ConfMultiQueueWindow extends Window implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 要修改的列的标题名称
	private final String[] CAPTION_NAMES = new String[] {"name", "musiconhold", "strategy", 
			"timeout", "wrapuptime", "maxlen", "autopause", "announce_frequency", "announce_round_seconds", 
			"joinempty", "setinterfacevar", "isrecordSoundNotice", "readDigitsType"};
	
	private final Object[] CAPTION_NAMES_CAPTION  = new Object[] {"队列名称","等待音乐","振铃策略",
			"坐席超时","冷却时长", "最大深度", "未接置忙", "提醒频度","提醒时长","允许入空队列","变量配置","是否录音提醒", "播报类型"};
	
	// 上述列对应的组件的默认值
	private final Object[] DEFAULT_VALUES = new Object[] {"", "", "rrmemory", 
			"30", "5", "10", "no", "60", "15", 
			"yes", "yes", false, Queue.READ_DIGITS_TYPE_READ_NONE};

	// 上述列对应的组件默认的可用状态
	private Boolean[] COL2_ENABLES = new Boolean[]{true, false, true, 
			false,  false, false, false, false, false,  false, false,  false, false};
	
	private GridLayout main_glo;
	private TextField name_tf;
	private ComboBox musiconhold_cb;
	private ComboBox strategy_cb;
	private TextField timeout_tf;
	private TextField wrapuptime_tf;
	private TextField maxlen_tf;
	private ComboBox autopause_cb;
	private TextField announce_frequency_tf;
	private TextField announce_round_seconds_tf;
	private ComboBox joinempty_cb;
	private ComboBox setinterfacevar_cb;
	private ComboBox isrecordSoundNotice_cb;
	private OptionGroup readDigitsType_og;
	
	private Label name_lb;
	private Button musiconhold_bt;
	private Button strategy_bt;
	private Button timeout_bt;
	private Button wrapuptime_bt;
	private Button maxlen_bt;
	private Button autopause_bt; 
	private Button announce_frequency_bt; 
	private Button announce_round_seconds_bt; 
	private Button joinempty_bt; 
	private Button setinterfacevar_bt;
	private Button isrecordSoundNotice_bt;
	private Button readDigitsType_bt;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

	/**
	 * 其他参数
	 */
	private Domain domain;
	private MusicOnHold defaultMoh;								// 默认等待音乐
	private BeanItemContainer<MusicOnHold> mohContainer;		// 等待音乐集合
	
	private QueueManagement queueManagement;
	private QueueService queueService;
	private MusicOnHoldService musicOnHoldService;
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	public ConfMultiQueueWindow(QueueManagement queueManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setWidth("560px");
		this.setHeight("440px");
		this.setCaption("批量配置队列");
		this.queueManagement = queueManagement;

		domain = SpringContextHolder.getDomain();		
		queueService = SpringContextHolder.getBean("queueService");
		musicOnHoldService = SpringContextHolder.getBean("musicOnHoldService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		
		mohContainer = new BeanItemContainer<MusicOnHold>(MusicOnHold.class);
		mohContainer.addAll(musicOnHoldService.getAllByDomain(domain));
		for(MusicOnHold moh : mohContainer.getItemIds()) {
			if(moh.getName().equals("default")) {
				defaultMoh = moh;
				break;
			}
		}
		
		//添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		main_glo = new GridLayout(3, 13);
		main_glo.setSpacing(true);
		main_glo.setMargin(false, true, false, true);
		this.addComponent(main_glo);

		// 创建第一列组件：标题
		createCol1Labels();

		// 创建第二列组件：文本框、下拉框等
		createCol2Componets();

		// 创建第三列组件：按钮
		createCol3Componets();

		// 创建操作组件
		creatOperatorComponents(windowContent);
	}
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		
		LinkedHashMap<Long, Queue> needDeleteQueues = queueManagement.getNeedDeleteQueues();
		String name_value = StringUtils.join(needDeleteQueues.values(), ",");
		name_tf.setReadOnly(false);
		name_tf.setValue(name_value);
		name_tf.setDescription("<b>"+name_value+"</b>");
		name_tf.setReadOnly(true);
		name_lb.setValue(needDeleteQueues.size()+"个");
	}
	
	/**
	 *  创建第一列组件：标题
	 */
	private void createCol1Labels() {
		for(int r = 0; r < CAPTION_NAMES.length; r++) {
			Label rowCaption_lb = new Label(CAPTION_NAMES_CAPTION[r].toString());
			rowCaption_lb.setWidth("-1px");
			main_glo.addComponent(rowCaption_lb, 0, r);
		}
	}

	/**
	 *  创建第二列组件：文本框、下拉框等
	 */
	private void createCol2Componets() {
		int row = -1;
		name_tf = createTextField(++row, "(\\d+,?\\d+)+", null);
		musiconhold_cb = createComboBox(++row, new String[] {});
		musiconhold_cb.setContainerDataSource(mohContainer);
		
		strategy_cb = createComboBox(++row, new String[] {"ringall","roundrobin","rrmemory","leastrecent","fewestcalls","random"});
		strategy_cb.setItemCaption("ringall", "群振");
		strategy_cb.setItemCaption("roundrobin", "轮询分配");
		strategy_cb.setItemCaption("rrmemory", "记忆轮询分配");
		strategy_cb.setItemCaption("leastrecent", "按最后一次接通分配");
		strategy_cb.setItemCaption("fewestcalls", "按呼叫次数分配");
		strategy_cb.setItemCaption("random", "随机分配");
		strategy_cb.setValue(DEFAULT_VALUES[row]);
		
		timeout_tf = createTextField(++row, "\\d{1,18}", 18);
		wrapuptime_tf = createTextField(++row, "\\d{1,18}", 18);
		maxlen_tf = createTextField(++row, "\\d{1,18}", 18);
		autopause_cb = createComboBox(++row, new String[] {"yes", "no"});
		announce_frequency_tf = createTextField(++row, "\\d{1,18}", 18);
		announce_round_seconds_tf = createTextField(++row, "\\d{1,18}", 18);
		joinempty_cb = createComboBox(++row, new String[] {"yes", "no"});
		setinterfacevar_cb = createComboBox(++row, new String[] {"yes", "no"});
		
		isrecordSoundNotice_cb = createComboBox(++row, new Boolean[] {false, true});
		isrecordSoundNotice_cb.setItemCaption(false, "关闭-录音提醒");
		isrecordSoundNotice_cb.setItemCaption(true, "开启-录音提醒");
		
		readDigitsType_og = createOptionGroup(++row, new String[]{Queue.READ_DIGITS_TYPE_READ_NONE, 
				Queue.READ_DIGITS_TYPE_READ_EMPNO, Queue.READ_DIGITS_TYPE_READ_EXTEN});
		readDigitsType_og.setItemCaption(Queue.READ_DIGITS_TYPE_READ_NONE, "无");
		readDigitsType_og.setItemCaption(Queue.READ_DIGITS_TYPE_READ_EMPNO, "报工号");
		readDigitsType_og.setItemCaption(Queue.READ_DIGITS_TYPE_READ_EXTEN, "报分机号");
		readDigitsType_og.addStyleName("threecol210");
		readDigitsType_og.setImmediate(true);
	}

	/**
	 * 为指定行的第二列创建文本框组件
	 * @param row		行号
	 * @param pattern	正则表达式
	 * @param maxLen	文本框能输入的字符长度
	 * @return TextField 返回文本框组件
	 */
	private TextField createTextField(int row, String pattern, Integer maxLen) {
		TextField tf = new TextField();
		tf.setWidth("305px");
		if(maxLen != null) {
			tf.setMaxLength(maxLen);
		}
		tf.addValidator(new RegexpValidator(pattern, null));
		tf.setValidationVisible(false);
		tf.setNullRepresentation("");
		tf.setNullSettingAllowed(true);
		tf.setImmediate(true);
		tf.setEnabled(COL2_ENABLES[row]);
		main_glo.addComponent(tf, 1, row);
		return tf;
	}
	
	/**
	 * 为指定行的第二列创建下拉框组件
	 * @param row		行号
	 * @param items		下拉框中的内容项
	 * @return
	 */
	private ComboBox createComboBox(int row, Object[] items) {
		ComboBox cb = new ComboBox();
		for(Object item : items) {
			cb.addItem(item);
		}
		cb.setImmediate(true);
		cb.setWidth("305px");
		cb.setNullSelectionAllowed(false);
		cb.setEnabled(COL2_ENABLES[row]);
		main_glo.addComponent(cb, 1, row);
		return cb;
	}

	/**
	 * 为指定行的第二列创建OptionGroup组件
	 * @param row		行号
	 * @param items		下拉框中的内容项
	 * @return
	 */
	private OptionGroup createOptionGroup(int row, String[] items) {
		OptionGroup og = new OptionGroup();
		for(String item : items) {
			og.addItem(item);
		}
		og.setImmediate(true);
		og.setWidth("305px");
		og.setNullSelectionAllowed(false);
		og.setEnabled(COL2_ENABLES[row]);
		main_glo.addComponent(og, 1, row);
		return og;
	}
	
	/** 
	 * 创建第三列组件：按钮
	 */
	private void createCol3Componets() {
		int row = -1;
		name_lb = new Label("", Label.CONTENT_XHTML);
		name_lb.setWidth("-1px");
		main_glo.addComponent(name_lb, 2, ++row);
		musiconhold_bt = createOptButton(++row);
		strategy_bt = createOptButton(++row);
		timeout_bt = createOptButton(++row);
		wrapuptime_bt = createOptButton(++row);
		maxlen_bt = createOptButton(++row);
		autopause_bt = createOptButton(++row);
		announce_frequency_bt = createOptButton(++row);
		announce_round_seconds_bt = createOptButton(++row);
		joinempty_bt = createOptButton(++row);
		setinterfacevar_bt = createOptButton(++row);
		isrecordSoundNotice_bt = createOptButton(++row);
		readDigitsType_bt = createOptButton(++row);
	}
	
	/**
	 * 自动为指定的行添加一个判断是否需要编辑的按钮
	 * @param row  所属行号
	 * @return Button 按钮
	 */
	private Button createOptButton(int row) {
		Resource icon = ResourceDataMgr.cancel_14_ico;
		if(main_glo.getComponent(1, row).isEnabled()) {
			icon = ResourceDataMgr.check_14_ico;
		}
		Button opt_bt = new Button(null, this);
		opt_bt.setIcon(icon);
		opt_bt.setStyleName("borderless");
		opt_bt.setImmediate(true);
		opt_bt.setWidth("-1px");
		opt_bt.setData(row);
		main_glo.addComponent(opt_bt, 2, row);
		return opt_bt;
	}

	/**
	 * 操作按钮
	 * @return
	 */
	private void creatOperatorComponents(VerticalLayout windowContent) {
		HorizontalLayout opt_hlo = new HorizontalLayout();
		opt_hlo.setSpacing(true);
		windowContent.addComponent(opt_hlo);

		// 保存按钮
		save = new Button("保存", this);
		save.setStyleName("default");
		opt_hlo.addComponent(save);

		// 取消按钮
		cancel = new Button("取消", this);
		opt_hlo.addComponent(cancel);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			// 保存编辑后的队列
			boolean saveSuccess = executeSave();
			if(saveSuccess) {
				// 添加队列成功后，更新该域所对应的asterisk配置文件 queues_domainname.conf
				queueService.updateAsteriskQueueFile(domain);
				// 重新加载asterisk 的配置文件
				reloadAsteriskService.reloadQueue();
				
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			this.getParent().removeWindow(this);
		} else if(source == musiconhold_bt) {
			updateComboBoxStatus(musiconhold_bt, musiconhold_cb);
		} else if(source == strategy_bt) {
			updateComboBoxStatus(strategy_bt, strategy_cb);
		} else if(source == timeout_bt) {
			updateTextFieldStatus(timeout_bt, timeout_tf);
		} else if(source == wrapuptime_bt) {
			updateTextFieldStatus(wrapuptime_bt, wrapuptime_tf);
		} else if(source == maxlen_bt) {
			updateTextFieldStatus(maxlen_bt, maxlen_tf);
		} else if(source == autopause_bt) {
			updateComboBoxStatus(autopause_bt, autopause_cb);
		} else if(source == announce_frequency_bt) {
			updateTextFieldStatus(announce_frequency_bt, announce_frequency_tf);
		} else if(source == announce_round_seconds_bt) {
			updateTextFieldStatus(announce_round_seconds_bt, announce_round_seconds_tf);
		} else if(source == joinempty_bt) {
			updateComboBoxStatus(joinempty_bt, joinempty_cb);
		} else if(source == setinterfacevar_bt) {
			updateComboBoxStatus(setinterfacevar_bt, setinterfacevar_cb);
		} else if(source == isrecordSoundNotice_bt) {
			updateComboBoxStatus(isrecordSoundNotice_bt, isrecordSoundNotice_cb);
		} else if(source == readDigitsType_bt) {
			updateOptionGroupStatus(readDigitsType_bt, readDigitsType_og);
		} 
	}
	
	/**
	 * 当点击的是操作组件时，更新操作组件的状态
	 * @param src_bt	被点击的按钮
	 * @param dst_comp	应该发生变化的目标组件
	 */
	private void updateTextFieldStatus(Button src_bt, TextField dst_comp) {
		int row = (Integer) src_bt.getData();
		if(src_bt.getIcon().equals(ResourceDataMgr.cancel_14_ico)) {
			src_bt.setIcon(ResourceDataMgr.check_14_ico);
			dst_comp.setEnabled(true);
			dst_comp.setValue(DEFAULT_VALUES[row]);
		} else {
			src_bt.setIcon(ResourceDataMgr.cancel_14_ico);
			dst_comp.setEnabled(false);
			dst_comp.setValue(null);
		}
	}
	
	/**
	 * 当点击的是操作组件时，更新操作组件的状态
	 * @param src_bt	被点击的按钮
	 * @param dst_comp	应该发生变化的目标组件
	 */
	private void updateComboBoxStatus(Button src_bt, ComboBox dst_comp) {
		if(src_bt.getIcon().equals(ResourceDataMgr.cancel_14_ico)) {
			src_bt.setIcon(ResourceDataMgr.check_14_ico);
			int row = (Integer) src_bt.getData();
			dst_comp.setEnabled(true);
			if(row != 1) {
				dst_comp.setValue(DEFAULT_VALUES[row]);
			} else {
				dst_comp.setValue(defaultMoh);
			}
		} else {
			src_bt.setIcon(ResourceDataMgr.cancel_14_ico);
			dst_comp.setEnabled(false);
			dst_comp.setValue(null);
		}
	}
	
	/**
	 * 当点击的是操作组件时，更新操作组件的状态
	 * @param src_bt	被点击的按钮
	 * @param dst_comp	应该发生变化的目标组件
	 */
	private void updateOptionGroupStatus(Button src_bt, OptionGroup dst_comp) {
		if(src_bt.getIcon().equals(ResourceDataMgr.cancel_14_ico)) {
			src_bt.setIcon(ResourceDataMgr.check_14_ico);
			int row = (Integer) src_bt.getData();
			dst_comp.setEnabled(true);
			if(row != 1) {
				dst_comp.setValue(DEFAULT_VALUES[row]);
			} else {
				dst_comp.setValue(defaultMoh);
			}
		} else {
			src_bt.setIcon(ResourceDataMgr.cancel_14_ico);
			dst_comp.setEnabled(false);
			dst_comp.setValue(null);
		}
	}

	/**
	 * 由buttonClick 调用 执行保存操作
	 */
	private boolean executeSave() {
		try {
			// 必须要有修改项
			if(!musiconhold_cb.isEnabled() && !strategy_cb.isEnabled() && !timeout_tf.isEnabled() 
					&& !wrapuptime_tf.isEnabled() && !maxlen_tf.isEnabled() && !autopause_cb.isEnabled() 
					&& !announce_frequency_tf.isEnabled() && !announce_round_seconds_tf.isEnabled() 
					&& !joinempty_cb.isEnabled() && !setinterfacevar_cb.isEnabled() && !isrecordSoundNotice_cb.isEnabled() 
					&& !readDigitsType_og.isEnabled()) {
				this.getApplication().getMainWindow().showNotification("对不起，您没有修改任何配置信息，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			
			MusicOnHold musiconhold = (MusicOnHold) musiconhold_cb.getValue();
			String strategy = StringUtils.trimToEmpty((String) strategy_cb.getValue());
			String timeout = StringUtils.trimToEmpty((String) timeout_tf.getValue());
			String wrapuptime = StringUtils.trimToEmpty((String) wrapuptime_tf.getValue());
			String maxlen = StringUtils.trimToEmpty((String) maxlen_tf.getValue());
			String autopause = StringUtils.trimToEmpty((String) autopause_cb.getValue());
			String announce_frequency = StringUtils.trimToEmpty((String) announce_frequency_tf.getValue());
			String announce_round_seconds = StringUtils.trimToEmpty((String) announce_round_seconds_tf.getValue());
			String joinempty = StringUtils.trimToEmpty((String) joinempty_cb.getValue());
			String setinterfacevar = StringUtils.trimToEmpty((String) setinterfacevar_cb.getValue());
			Boolean isrecordSoundNotice = (Boolean) isrecordSoundNotice_cb.getValue();
			String readDigitsType = StringUtils.trimToEmpty((String) readDigitsType_og.getValue());

			// 验证输入的信息是否合法
			if(musiconhold_cb.isEnabled() && musiconhold == null) {
				this.getApplication().getMainWindow().showNotification("musiconhold 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false; 
			} else if(strategy_cb.isEnabled() && "".equals(strategy)) {
				this.getApplication().getMainWindow().showNotification("strategy 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(timeout_tf.isEnabled() && (!timeout_tf.isValid() || "".equals(timeout))) {
				this.getApplication().getMainWindow().showNotification("timeout 只能由长度不大于18位的数字组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(wrapuptime_tf.isEnabled() && (!wrapuptime_tf.isValid() || "".equals(wrapuptime))) {
				this.getApplication().getMainWindow().showNotification("wrapuptime 只能由长度不大于18位的数字组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(maxlen_tf.isEnabled() && (!maxlen_tf.isValid() || "".equals(maxlen))) {
				this.getApplication().getMainWindow().showNotification("maxlen 只能由长度不大于18位的数字组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(autopause_cb.isEnabled() && "".equals(autopause)) {
				this.getApplication().getMainWindow().showNotification("autopause 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(announce_frequency_tf.isEnabled() && (!announce_frequency_tf.isValid() || "".equals(announce_frequency))) {
				this.getApplication().getMainWindow().showNotification("announce_frequency 只能由长度不大于18位的数字组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(announce_round_seconds_tf.isEnabled() && (!announce_round_seconds_tf.isValid() || "".equals(announce_round_seconds))) {
				this.getApplication().getMainWindow().showNotification("announce_round_seconds 只能由长度不大于18位的数字组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(joinempty_cb.isEnabled() && "".equals(joinempty)) {
				this.getApplication().getMainWindow().showNotification("joinempty 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(setinterfacevar_cb.isEnabled() && "".equals(setinterfacevar)) {
				this.getApplication().getMainWindow().showNotification("setinterfacevar 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			// 更新数据
			LinkedHashMap<Long, Queue> needDeleteQueues = queueManagement.getNeedDeleteQueues();
			for(Queue qu : needDeleteQueues.values()) {
				if(musiconhold != null) {
					qu.setMusiconhold(musiconhold);
				}
				if(!"".equals(strategy)) {
					qu.setStrategy(strategy);
				}
				if(!"".equals(timeout)) {
					qu.setTimeout(Long.parseLong(timeout));
				}
				if(!"".equals(wrapuptime)) {
					qu.setWrapuptime(Long.parseLong(wrapuptime));
				}
				if(!"".equals(maxlen)) {
					qu.setMaxlen(Long.parseLong(maxlen));
				}
				if(!"".equals(autopause)) {
					qu.setAutopause(autopause);
				}
				if(!"".equals(announce_frequency)) {
					qu.setAnnounce_frequency(Long.parseLong(announce_frequency));
				}
				if(!"".equals(announce_round_seconds)) {
					qu.setAnnounce_round_seconds(Long.parseLong(announce_round_seconds));
				}
				if(!"".equals(joinempty)) {
					qu.setJoinempty(joinempty);
				}
				if(!"".equals(setinterfacevar)) {
					boolean value = "yes".equals(setinterfacevar) ? true : false;
					qu.setSetinterfacevar(value);
				}
				if(!"".equals(readDigitsType)) {
					qu.setReadDigitsType(readDigitsType);
				}
				
				if(isrecordSoundNotice != null) {
					qu.setIsrecordSoundNotice(isrecordSoundNotice);
				}
				
				queueService.update(qu);
			}
			
			//刷新sipManagement的Table
			queueManagement.clearNeedDeleteQueues(); 
			queueManagement.updateTable(false); 
			queueManagement.getTable().setValue(null); 
			this.getApplication().getMainWindow().showNotification("保存队列成功！");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			this.getApplication().getMainWindow().showNotification("保存队列失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		
		return true;
	}
	
}