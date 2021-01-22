package com.jiangyifen.ec2.ui.mgr.queuemanage;

import java.util.Arrays;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.MusicOnHoldService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.QueueManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.utils.CustomComboBox;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 编辑队列 组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class EditQueue extends Window implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 精简编辑时 form 表格中需要显示的属性列及顺序
	private final Object[] TYPICAL_VISIBLE_PROPERTIES = new Object[] {"name", "description", "musiconhold", "strategy", "timeout", "wrapuptime",
			"maxlen", "autopause", "dynamicmember", "isDefaultQueue", "isrecordSoundNotice", "readDigitsType"};

	private final Object[] TYPICAL_VISIBLE_PROPERTIES_CAPTION  = new Object[] {"队列名称", "队列描述","等待音乐","振铃策略","坐席超时","冷却时长", 
			"最大深度", "未接置忙", "动态成员", "是否默认", "是否录音提醒", "播报类型"};
	
	// 精简编辑时 form 表格中 分机 必须拥有值的字段
	private final String[] TYPICAL_REQUIRED_PROPERTIES = new String[] {"name", "description", "musiconhold", "strategy", "timeout", "wrapuptime", "maxlen", "autopause", "dynamicmember", "isDefaultQueue", "isrecordSoundNotice", "readDigitsType"};

	// 高级编辑时 form 表格中需要显示的属性列及顺序
	private final Object[] ADVANCED_VISIBLE_PROPERTIES = new Object[] {"name", "description", "musiconhold", "strategy", "timeout", "wrapuptime", "maxlen", "autopause", "dynamicmember", 
			"announce_frequency", "announce_round_seconds", "joinempty", "setinterfacevar", "isDefaultQueue", "isrecordSoundNotice", "readDigitsType", "monitor_join", "monitor_format", "announce", "announce_holdtime", "context", "queue_youarenext", 
			"queue_thereare", "queue_callswaiting", "queue_holdtime", "queue_minutes", "queue_seconds", "queue_lessthan", "queue_thankyou", "queue_reporthold", "queue_periodic_announce", 
			"retry", "servicelevel", "leavewhenempty", "eventmemberstatus", "eventwhencalled", "reportholdtime", "memberdelay", "weight", "timeoutrestart", "periodic_announce_frequency"};

	// 高级编辑时  form 表格中 分机 必须拥有值的字段
	private final String[] ADVANCED_REQUIRED_PROPERTIES = new String[] {"name", "description", "musiconhold", "strategy", "timeout", "wrapuptime", 
			"maxlen", "autopause", "dynamicmember", "announce_frequency", "announce_round_seconds", "joinempty", "setinterfacevar", "isDefaultQueue", "isrecordSoundNotice", "readDigitsType"};
	
	private final Object[] ADVANCED_REQUIRED_PROPERTIES_CAPTION  = new Object[] {"队列名称", "队列描述","等待音乐","振铃策略","坐席超时","冷却时长", 
			"最大深度", "未接置忙", "动态成员", "提醒频度","提醒时长","允许入空队列","变量配置","是否默认", "是否录音提醒", "播报类型"};

	/**
	 * 主要组件输出	
	 */
	//Form输出
	private Form form;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;
	private OptionGroup settingType;		// 队列配置类型
	
	/**
	 * 其他参数
	 */
	private Queue queue;
	private Domain domain;					// 队列所在域
	private QueueManagement queueManagement;
	private QueueService queueService;
	private MusicOnHoldService musicOnHoldService;
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	public EditQueue(QueueManagement queueManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setWidth("400px");
		this.setHeight("450px");
		this.setCaption("编辑队列");
		this.queueManagement = queueManagement;
		
		domain = SpringContextHolder.getDomain();
		queueService = SpringContextHolder.getBean("queueService");
		musicOnHoldService = SpringContextHolder.getBean("musicOnHoldService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");

		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// 创建Form 表单
		createFormComponent(windowContent);
		
	}

	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		queue = (Queue) queueManagement.getTable().getValue();
		settingType.setValue("typical");
		
		// 创建简单配置的分机对象
		simpleEditQueue();
	}
	
	/**
	 * 创建表格组件
	 * @param windowContent
	 */
	private void createFormComponent(VerticalLayout windowContent) {
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
	 *  创建简单配置的分机对象
	 */
	@SuppressWarnings("unchecked")
	private void simpleEditQueue() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<Queue>(queue), Arrays.asList(TYPICAL_VISIBLE_PROPERTIES));
		for (int i = 0; i < TYPICAL_VISIBLE_PROPERTIES.length; i++) {
			form.getField(TYPICAL_VISIBLE_PROPERTIES[i]).setCaption(TYPICAL_VISIBLE_PROPERTIES_CAPTION[i].toString() +"：");
		}

		// 设置默认值
		form.getField("strategy").setValue(queue.getStrategy());
		form.getField("dynamicmember").setValue(queue.getDynamicmember());
		form.getField("isDefaultQueue").setValue(queue.getIsDefaultQueue());
		form.getField("isrecordSoundNotice").setValue(queue.getIsrecordSoundNotice());
		form.getField("readDigitsType").setValue(queue.getReadDigitsType());
		
		// 设置musicOnHold 的默认值
		MusicOnHold moh1 = queue.getMusiconhold();
		BeanItemContainer<MusicOnHold> mohs = (BeanItemContainer<MusicOnHold>) ((ComboBox) form.getField("musiconhold")).getContainerDataSource();
		for(MusicOnHold moh : mohs.getItemIds()) {
			if(moh.getId().equals(moh1.getId())) {
				form.getField("musiconhold").setValue(moh);
			}
		}		
		
		// 如果当前队列时默认队列，则不让其修改默认项
		if(queue.getIsDefaultQueue() == true) {
			form.getField("isDefaultQueue").setReadOnly(true);
		}
		
		// 将必须填写内容的组件设为必填项
		for(String required : TYPICAL_REQUIRED_PROPERTIES) {
			form.getField(required).setRequired(true);
			form.getField(required).setRequiredError(required + " 不能为空！");
		}
		
		// 队列名不允许修改
		form.getField("name").setReadOnly(true);
	}
	
	/**
	 * 创建拥有高级配置的分机对象
	 */
	@SuppressWarnings("unchecked")
	private void advancedEditQueue() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<Queue>(queue), Arrays.asList(ADVANCED_VISIBLE_PROPERTIES));
		for (int i = 0; i < ADVANCED_VISIBLE_PROPERTIES.length; i++) {
			form.getField(ADVANCED_VISIBLE_PROPERTIES[i]).setCaption(ADVANCED_VISIBLE_PROPERTIES[i].toString() +"：");
		}

		// 设置默认值
		form.getField("strategy").setValue(queue.getStrategy());
		form.getField("dynamicmember").setValue(queue.getDynamicmember());
		form.getField("setinterfacevar").setValue(queue.getSetinterfacevar());
		form.getField("monitor_join").setValue(queue.getMonitor_join());
		form.getField("eventmemberstatus").setValue(queue.getEventmemberstatus());
		form.getField("eventwhencalled").setValue(queue.getEventwhencalled());
		form.getField("reportholdtime").setValue(queue.getReportholdtime());
		form.getField("timeoutrestart").setValue(queue.getTimeoutrestart());
		form.getField("isDefaultQueue").setValue(queue.getIsDefaultQueue());
		form.getField("isrecordSoundNotice").setValue(queue.getIsrecordSoundNotice());
		form.getField("readDigitsType").setValue(queue.getReadDigitsType());
		
		// 设置musicOnHold 的默认值
		MusicOnHold moh1 = queue.getMusiconhold();
		BeanItemContainer<MusicOnHold> mohs = (BeanItemContainer<MusicOnHold>) ((ComboBox) form.getField("musiconhold")).getContainerDataSource();
		for(MusicOnHold moh : mohs.getItemIds()) {
			if(moh.getId().equals(moh1.getId())) {
				form.getField("musiconhold").setValue(moh);
			}
		}
		
		// 如果当前队列时默认队列，则不让其修改默认项
		if(queue.getIsDefaultQueue() == true) {
			form.getField("isDefaultQueue").setReadOnly(true);
		}
		
		// 将必须填写内容的组件设为必填项
		for(int i=0;i< ADVANCED_REQUIRED_PROPERTIES.length;i++) {
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setRequired(true);
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setCaption(ADVANCED_REQUIRED_PROPERTIES_CAPTION[i].toString());
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setRequiredError(ADVANCED_REQUIRED_PROPERTIES[i] + " 不能为空！");
		}
		
		// 队列名不允许修改
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
		
		// 配置方式选项
		Label settingTypeLabel = new Label("配置方式：");
		settingTypeLabel.setWidth("-1px");
		buttonsLayout.addComponent(settingTypeLabel);
		buttonsLayout.setComponentAlignment(settingTypeLabel, Alignment.MIDDLE_CENTER);
		
		// 高级配置复选框
		settingType = new OptionGroup();
		settingType.addItem("typical");
		settingType.addItem("advanced");
		settingType.select("typical");
		settingType.setItemCaption("typical", "经典");
		settingType.setItemCaption("advanced", "高级");
		settingType.setImmediate(true);
		settingType.setStyleName("twocol100");
		settingType.addListener((ValueChangeListener) this);
		buttonsLayout.addComponent(settingType);
		buttonsLayout.setComponentAlignment(settingType, Alignment.MIDDLE_CENTER);

		return buttonsLayout;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		this.center();
		String typeValue = (String) settingType.getValue();
		if("advanced".equals(typeValue)) {
			this.setHeight("700px");
			this.setWidth("470px");
			// 创建高级配置所需的组件
			advancedEditQueue();
		}  else {
			this.setWidth("400px");
			this.setHeight("450px");
			// 创建简单配置所需的组件
			simpleEditQueue();
		}
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
			// 编辑后执行保存
			boolean saveSuccess = executeSave();
			if(saveSuccess) {
				// 添加队列成功后，更新该域所对应的asterisk配置文件 queues_domainname.conf
				queueService.updateAsteriskQueueFile(domain);
				// 重新加载asterisk 的配置文件
				reloadAsteriskService.reloadQueue();
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
		// 原来的默认队列
		Queue originalDefaultQueue = null;
		// 当前队列是否要置为默认队列，默认为不是
		boolean isDefaultQueue = false;
		
		try {
			// 如果当前被编辑外线原来不是默认外线，则需要进行下面的处理
			if(form.getField("isDefaultQueue").isReadOnly() == false) {
				isDefaultQueue = (Boolean) form.getField("isDefaultQueue").getValue();
				if(isDefaultQueue == true) {
					// 查询 数据库，看该域是否已经存在默认队列，如果不存在则将当前要增加的队列置为默认；如果已经存在，并且当前队列要置为默认，那么需要先将之前的取消默认权限
					originalDefaultQueue = queueService.getDefaultQueueByDomain(domain.getId());
					if(originalDefaultQueue != null && isDefaultQueue == true){
						originalDefaultQueue.setIsDefaultQueue(false);
						queueService.update(originalDefaultQueue);
					}
				}
			}
		} catch (Exception e1) {
			logger.error(e1.getMessage()+ "修改原有默认队列时出现异常", e1);
			this.getApplication().getMainWindow().showNotification("保存队列失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		try {
			// 更新数据库
			queue = queueService.update(queue);
			
			//刷新queueManagement的Table
			queueManagement.updateTable(false);
			queueManagement.getTable().setValue(null);
		} catch (Exception e) {
			// 如果要将当前队列置为默认队列，但是失败了，而且之前已经将该域的默认队列去掉了，则此时需要从新将原来的默认队列置回
			if(isDefaultQueue == true && originalDefaultQueue != null){
				originalDefaultQueue.setIsDefaultQueue(true);
				queueService.update(originalDefaultQueue);
			}
			
			logger.error(e.getMessage()+ "修改队列时，更新出现异常", e);
			this.getApplication().getMainWindow().showNotification("保存队列失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		
		// 如果修改队列成功，并且当前队列被置为了默认队列，则更新内存中的数据
		if(queue.getIsDefaultQueue() == true) {
			ShareData.domainToDefaultQueue.put(domain.getId(), queue.getName());
		}
		
		return true;
	}
	
	/**
	 * 自定义form 表单中域的构造器
	 * @author jrh
	 */
	private class MyFieldFactory extends DefaultFieldFactory{
		private int domainIdLen = domain.getId().toString().length();
		
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("musiconhold".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("210px");
				comboBox.setNullSelectionAllowed(false);
				
				BeanItemContainer<MusicOnHold> mohContainer = new BeanItemContainer<MusicOnHold>(MusicOnHold.class);
				mohContainer.addAll(musicOnHoldService.getAllByDomain(domain));
				comboBox.setContainerDataSource(mohContainer);
				comboBox.setValue(queue.getMusiconhold());
				
				return comboBox;
			} else if("strategy".equals(propertyId)){
				TreeMap<String, String> treeMap = new TreeMap<String, String>();
				treeMap.put("ringall", "群振");
				treeMap.put("roundrobin", "轮询分配");
				treeMap.put("rrmemory", "记忆轮询分配");
				treeMap.put("leastrecent", "按最后一次接通分配");
				treeMap.put("fewestcalls", "按呼叫次数分配");
				treeMap.put("random", "随机分配");
				
				CustomComboBox customComboBox = new CustomComboBox();
				customComboBox.setWidth("210px");
				customComboBox.setNullSelectionAllowed(false);
				customComboBox.setDataSource(treeMap);
				
				return customComboBox;
			} else if("autopause".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("210px");
				comboBox.addItem("no");
				comboBox.addItem("yes"); 
				comboBox.setItemCaption("no", "否");
				comboBox.setItemCaption("yes", "是");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("joinempty".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("210px");
				comboBox.addItem("no");
				comboBox.addItem("yes");
				comboBox.setItemCaption("no", "否");
				comboBox.setItemCaption("yes", "是");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("isDefaultQueue".equals(propertyId)) {
				return createBooleanTypeField(false);
			} else if("dynamicmember".equals(propertyId)){
				return createBooleanTypeField(false);
			} else if("setinterfacevar".equals(propertyId)) {
				return createBooleanTypeField(false);
			} else if("monitor_join".equals(propertyId)) {
				return createBooleanTypeField(true);
			} else if("eventmemberstatus".equals(propertyId)) {
				return createBooleanTypeField(true);
			} else if("eventwhencalled".equals(propertyId)) {
				return createBooleanTypeField(true);
			} else if("reportholdtime".equals(propertyId)) {
				return createBooleanTypeField(true);
			} else if("timeoutrestart".equals(propertyId)) {
				return createBooleanTypeField(true);
			} else if("isrecordSoundNotice".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("210px");
				comboBox.addItem(false);
				comboBox.addItem(true);
				comboBox.setItemCaption(true, "开启-录音提醒");
				comboBox.setItemCaption(false, "关闭-录音提醒");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("readDigitsType".equals(propertyId)) {
				OptionGroup rdt_og = new OptionGroup();
				rdt_og.setNullSelectionAllowed(false);
				rdt_og.addItem(Queue.READ_DIGITS_TYPE_READ_NONE);
				rdt_og.addItem(Queue.READ_DIGITS_TYPE_READ_EMPNO);
				rdt_og.addItem(Queue.READ_DIGITS_TYPE_READ_EXTEN);
				rdt_og.setItemCaption(Queue.READ_DIGITS_TYPE_READ_NONE, "无");
				rdt_og.setItemCaption(Queue.READ_DIGITS_TYPE_READ_EMPNO, "报工号");
				rdt_og.setItemCaption(Queue.READ_DIGITS_TYPE_READ_EXTEN, "报分机号");
				rdt_og.setWidth("210px");
				rdt_og.addStyleName("threecol210");
				rdt_og.setImmediate(true);
				rdt_og.setValue(Queue.READ_DIGITS_TYPE_READ_NONE);
				return rdt_og;
			}
			
			Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
			field.setWidth("210px");
			
			if("name".equals(propertyId)) {
				int maxlen = 80 - domainIdLen;
				field.addValidator(new RegexpValidator("\\w{0," +maxlen+ "}", "username 只能由长度不大于" +maxlen+ "位的字符组成"));
			} else if("description".equals(propertyId)) {
				field.addValidator(new RegexpValidator("^[2E80-\\u9fa5 \\-]{0,128}$", "description 只能由长度不大于128位的字符组成"));
			} else if("timeout".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "timeout 只能由长度不大于18位的数字组成或为空"));
			} else if("wrapuptime".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "wrapuptime 只能由长度不大于18位的数字组成或为空"));
			} else if("maxlen".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "maxlen 只能由长度不大于18位的数字组成或为空"));
			} else if("monitor_format".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "monitor_format 只能由长度不大于128位的字符组成或为空"));
			} else if("announce".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "announce 只能由长度不大于128位的字符组成或为空"));
			} else if("announce_frequency".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "announce_frequency 只能由长度不大于18位的数字组成或为空"));
			} else if("announce_round_seconds".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "announce_round_seconds 只能由长度不大于18位的数字组成或为空"));
			} else if("announce_holdtime".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "announce_holdtime 只能由长度不大于128位的字符组成或为空"));
			} else if("context".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "context 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_youarenext".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_youarenext 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_thereare".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_thereare 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_callswaiting".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_callswaiting 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_holdtime".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_holdtime 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_minutes".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_minutes 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_seconds".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_seconds 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_lessthan".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_lessthan 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_thankyou".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_thankyou 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_reporthold".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "queue_reporthold 只能由长度不大于128位的字符组成或为空"));
			} else if("queue_periodic_announce".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "queue_periodic_announce 只能由长度不大于18位的数字组成或为空"));
			} else if("retry".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "retry 只能由长度不大于18位的数字组成或为空"));
			} else if("servicelevel".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "servicelevel 只能由长度不大于18位的数字组成或为空"));
			} else if("leavewhenempty".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,128}", "leavewhenempty 只能由长度不大于128位的字符组成或为空"));
			} else if("memberdelay".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "memberdelay 只能由长度不大于18位的数字组成或为空"));
			} else if("weight".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "weight 只能由长度不大于18位的数字组成或为空"));
			} else if("periodic_announce_frequency".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "periodic_announce_frequency 只能由长度不大于18位的数字组成或为空"));
			}
			
			
			if (field instanceof TextField) {
				((TextField) field).setNullRepresentation("");
				((TextField) field).setNullSettingAllowed(true);
			}
				
			return field;
		}
		
		/**
		 * 创建自定义组件，以此来实现  原始数据  跟  界面显示信息  之间的转换
		 * @param nullSelectionAllowed 是否可空
		 * @return CustomComboBox 自定义的 ComboBox 组件
		 */
		private CustomComboBox createBooleanTypeField(boolean nullSelectionAllowed) {
			TreeMap<Boolean, String> treeMap = new TreeMap<Boolean, String>();
			treeMap.put(true, "是");
			treeMap.put(false, "否");
			
			CustomComboBox customComboBox = new CustomComboBox();
			customComboBox.setWidth("210px");
			customComboBox.setDataSource(treeMap);
			customComboBox.setNullSelectionAllowed(nullSelectionAllowed);
			
			return customComboBox;
		}
	}

}
