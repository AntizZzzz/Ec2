package com.jiangyifen.ec2.ui.mgr.outlinemanage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.MobileAreacode;
import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.OutlineManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.utils.CustomComboBox;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
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
 * 编辑外线组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class EditOutline extends Window implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 精简编辑时 form 表格中需要显示的属性列及顺序
	private final Object[] TYPICAL_VISIBLE_PROPERTIES = new Object[] {"sipType", "name", "username", "secret", "host", 
			"type", "context", "qualify", "canreinvite", "nat", "disallow", "allow", "call_limit","belong", "ispopupWin", "isDefaultOutline"};
	private final Object[] TYPICAL_VISIBLE_PROPERTIES_CAPTION = new Object[] { "外线类型", "外线名称", "注册名称", "密码","SIP服务器",
			 "注册类型","呼叫路由", "心跳确认", "二次拨号", "内网穿透", "不允许编码", "允许编码","并发限制","所属区号","是否弹屏","是否默认"};

	// 精简编辑时 form 表格中 分机 必须拥有值的字段
	private final String[] TYPICAL_REQUIRED_PROPERTIES = new String[] {"sipType", "name", "username", "secret", "host", 
			"type", "context", "qualify", "canreinvite", "nat", "disallow", "allow", "call_limit","belong", "ispopupWin", "isDefaultOutline"};

	// 高级编辑时 form 表格中需要显示的属性列及顺序
	private final Object[] ADVANCED_VISIBLE_PROPERTIES = new Object[] {"sipType", "name", "username", "secret", "host", "type", "context", 	// 必填项 
			"qualify", "canreinvite", "nat", "disallow", "allow", "call_limit", "belong", "ispopupWin", "isDefaultOutline", "insecure", "fromuser", "fromdomain", 	// 必填项
			"port", "cancallforward", "pickupgroup", "callgroup", "regseconds", "lastms","defaultuser", "regserver", "regexten", "fullcontact", 
			"subscribecontext", "accountcode", "amaflags", "callerid", "defaultip", "dtmfmode", "language", "ipaddr", "mailbox", "md5secret", 
			"permit", "deny", "mask", "musiconhold", "restrictcid", "rtptimeout", "rtpholdtimeout", "setvar"};

	// 高级编辑时  form 表格中 外线 必须拥有值的字段
	private final String[] ADVANCED_REQUIRED_PROPERTIES = new String[] {"sipType", "name", "username", "secret", "host", "type", "context", 
			"qualify", "canreinvite", "nat", "disallow", "allow", "call_limit","belong", "ispopupWin", "isDefaultOutline", "insecure", "fromuser", "fromdomain"};
	
	private final Object[] ADVANCED_REQUIRED_PROPERTIES_CAPTION = new Object[] { "外线类型", "外线名称", "注册名称", "密码","SIP服务器","注册类型","呼叫路由",
			 "心跳确认", "二次拨号", "内网穿透", "不允许编码", "允许编码","并发限制","归属地","是否弹屏","是否默认","认证模式","主叫用户","主叫域"};
	
	/**
	 * 主要组件输出	
	 */
	private OptionGroup settingType;		// 外线配置类型

	//Form输出
	private Form form;

	// 外线类型 （网关外线、SIP外线）
	private ComboBox sipType;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

	/**
	 * 其他参数
	 */
	private Domain domain;					// 外线所属的域
	private String oldSipname;				// 外线的初始名称
	private SipConfig sipConfig;
	private OutlineManagement outlineManagement;
	private SipConfigService sipConfigService;						// sip 服务类
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员关系管理服务类
	private Phone2PhoneSettingService phone2PhoneSettingService;	// 外转外配置服务类
	private UserQueueService userQueueService;						// 动态队列成员服务类
	private QueueService queueService;								// 队列服务类
	private AutoDialoutTaskService autoDialoutTaskService;			// 队列成员管理服务类
	
	public EditOutline(OutlineManagement outlineManagement) {
		this.center();
		this.setModal(true);
		this.setWidth("360px");
		this.setHeight("565px");
		this.setResizable(false);
		this.setCaption("编辑外线");
		this.outlineManagement = outlineManagement;

		domain = SpringContextHolder.getDomain();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
		phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		queueService = SpringContextHolder.getBean("queueService");
		autoDialoutTaskService = SpringContextHolder.getBean("autoDialoutTaskService");

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
		sipConfig = (SipConfig) outlineManagement.getTable().getValue();
		oldSipname = sipConfig.getName();
		settingType.setValue("typical");
		
		// 简单编辑分机的各字段的值
		typicalEditOutline();
	}

	/**
	 * 创建Form 表单组件
	 * @param windowContent
	 */
	private void createFormComponent(VerticalLayout windowContent) {
		form=new Form();
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
		form.setInvalidCommitted(false);
		form.setWriteThrough(false);
		form.setImmediate(true);
		form.addStyleName("chb");
		form.setFormFieldFactory(new MyFieldFactory());
		form.setFooter(creatFormFooterComponents());
		windowContent.addComponent(form);
	}

	/**
	 *  简单编辑外线的各字段的值
	 */
	private void typicalEditOutline() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<SipConfig>(sipConfig), Arrays.asList(TYPICAL_VISIBLE_PROPERTIES));
		for (int i = 0; i < TYPICAL_VISIBLE_PROPERTIES.length; i++) {
			form.getField(TYPICAL_VISIBLE_PROPERTIES[i]).setCaption(TYPICAL_VISIBLE_PROPERTIES_CAPTION[i].toString());
		}
		
		// 回显各组件的值
		form.getField("type").setValue(sipConfig.getType());
		form.getField("qualify").setValue(sipConfig.getQualify());
		form.getField("canreinvite").setValue(sipConfig.getCanreinvite());
		form.getField("nat").setValue(sipConfig.getNat());
		form.getField("isDefaultOutline").setValue(sipConfig.getIsDefaultOutline());
		
		// 如果当前外线是默认外线，则不让其修改
		if(sipConfig.getIsDefaultOutline() == true) {
			form.getField("isDefaultOutline").setReadOnly(true);
		}
		
		// 将必须填写内容的组件设为必填项
		for(String required : TYPICAL_REQUIRED_PROPERTIES) {
			form.getField(required).setRequired(true);
			form.getField(required).setRequiredError(required + " 不能为空！");
		}
	}

	/**
	 * 高级配置外线的各字段的值
	 */
	private void advancedEditOutline() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<SipConfig>(sipConfig), Arrays.asList(ADVANCED_VISIBLE_PROPERTIES));
		for (int i = 0; i < ADVANCED_VISIBLE_PROPERTIES.length; i++) {
			form.getField(ADVANCED_VISIBLE_PROPERTIES[i]).setCaption(ADVANCED_VISIBLE_PROPERTIES[i].toString());
		}
		
		// 回显各组件的值
		form.getField("type").setValue(sipConfig.getType());
		form.getField("qualify").setValue(sipConfig.getQualify());
		form.getField("canreinvite").setValue(sipConfig.getCanreinvite());
		form.getField("isDefaultOutline").setValue(sipConfig.getIsDefaultOutline());
		form.getField("nat").setValue(sipConfig.getNat());
		form.getField("cancallforward").setValue(sipConfig.getCancallforward());
		
		// 如果当前外线是默认外线，则不让其修改
		if(sipConfig.getIsDefaultOutline() == true) {
			form.getField("isDefaultOutline").setReadOnly(true);
		}
		
		// 将必须填写内容的组件设为必填项
		for(int i=0;i<ADVANCED_REQUIRED_PROPERTIES.length;i++) {
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setRequired(true);
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setCaption(ADVANCED_REQUIRED_PROPERTIES_CAPTION[i].toString());
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setRequiredError(ADVANCED_REQUIRED_PROPERTIES[i]+ " 不能为空！");
		}
	}
	
	/**
	 * 按钮输出区域
	 * @return
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
		Property source = event.getProperty();
		if(source == sipType) {
			TextField hostField = (TextField) form.getField("host");
			if(hostField != null) {
				if(SipConfigType.gateway_outline.equals(sipType.getValue())) {
					hostField.setValue("dynamic");
				} else {
					hostField.setValue("");
				}
			}
		} else {
			this.center();
			String typeValue = (String) settingType.getValue();
			if("advanced".equals(typeValue)) {
				this.setHeight("700px");
				this.setWidth("380px");
				// 创建高级配置所需的组件
				advancedEditOutline();
			}  else {
				this.setWidth("360px");
				this.setHeight("565px");
				// 创建简单配置所需的组件
				typicalEditOutline();
			}
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			// 由于在数据库中，外线的name 和 username 是严格一致的，所有可以这样进行获取
			String outlineName = (String) form.getField("name").getValue();
			logger.info("outlineName:"+outlineName);
			try {
				form.commit();
                if(!"".equals(outlineName) && !outlineName.equals(oldSipname) && sipConfigService.existBySipname(outlineName)) {
                	this.getApplication().getMainWindow().showNotification("外线号 " +outlineName+ " 已经存在，请修改后重试！");
                	return;
                }
            } catch (Exception e) {
            	return;
            }
			boolean saveSuccess = executeSave(outlineName);
			if(saveSuccess == true) {
				// 如果编辑队列成功，则修改内存中的数据 (ShareData 中的各Map)
				updateShareData(outlineName);
				
				// 更新队列在asterisk 中的配置文件  sip_regirster_domainname.conf
				sipConfigService.updateAsteriskRegisterSipConfigFile(domain);
				
				// 更新队列在asterisk 中的配置文件  sip_outline_domainname.conf
				sipConfigService.updateAsteriskOutlineSipConfigFile(domain);
				
				// 修改配置文件成功， 则重新加载asterisk 的配置文件
				reloadAsteriskService.reloadSip();
				
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}

	/**
	 * 由buttonClick 调用 执行保存操作
	 * @return
	 */
	private boolean executeSave(String outlineName) {
		// 原来的默认外线
		SipConfig originalDefaultOutline = null;
		// 当前外线是否要置为默认外线，默认为不是
		boolean isDefaultOutline = false;
		String settingTypeValue = (String) settingType.getValue();

		// 更新数据库
		if("typical".equals(settingTypeValue)) {
			String host = (String) form.getField("host").getValue();
			sipConfig.setFromdomain(host);
			String outlineUsername = (String) form.getField("username").getValue();
			sipConfig.setFromuser(outlineUsername);
		}
		
		try {
			// 如果当前被编辑外线原来不是默认外线，则需要进行下面的处理
			if(form.getField("isDefaultOutline").isReadOnly() == false) {
				isDefaultOutline = (Boolean) form.getField("isDefaultOutline").getValue();
				if(isDefaultOutline == true){
					// 查询 数据库，看该域是否已经存在默认外线，如果不存在则将当前要增加的外线只为默认；如果已经存在，并且当前外线要置为默认，那么需要先将之前的取消默认权限
					originalDefaultOutline = sipConfigService.getDefaultOutlineByDomain(domain);
					if(originalDefaultOutline != null) {	// 正常情况下这里肯定成立
						originalDefaultOutline.setIsDefaultOutline(false);
						sipConfigService.update(originalDefaultOutline);
						// 默认外线对象发生变化, 需要更新所有的队列中的手机成员
						updatePhoneInQueueMember(outlineName, originalDefaultOutline.getUsername());
						// 修改自动外呼任务下的队列中的手机成员
						updatePhoneInAutoQueueMember(outlineName, originalDefaultOutline.getUsername());
					}
				}
			} else if(!outlineName.equals(oldSipname)) {
				// 默认外线名字发生变化, 需要更新所有的队列中的手机成员
				updatePhoneInQueueMember(outlineName, oldSipname);
				// 修改自动外呼任务下的队列中的手机成员
				updatePhoneInAutoQueueMember(outlineName, oldSipname);
				// jrh 2014-02-21 如果外线名称发生编号，则需要检查IVRAction，如果有Action 在使用 当前外线，则需要一并修改
				sipConfigService.checkIvrActionAndUpdate(false, outlineName, domain.getId());
			}
		} catch (Exception e1) {
			logger.error(e1.getMessage()+ "修改原有默认外线时出现异常", e1);
			this.getApplication().getMainWindow().showNotification("保存外线失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		try {
			sipConfig = sipConfigService.update(sipConfig);
			
			//刷新outlineManagement的Table
			outlineManagement.updateTable(false);
			outlineManagement.getTable().setValue(null);
		} catch (Exception e) {
			// 如果要将当前外线置为默认外线，但是失败了，而且之前已经将该域的默认外线去掉了，则此时需要从新将原来的默认外线置回
			if(isDefaultOutline == true && originalDefaultOutline != null) {
				originalDefaultOutline.setIsDefaultOutline(true);
				sipConfigService.update(originalDefaultOutline);
			}
			
			logger.error(e.getMessage()+ "编辑外线时，更新出现异常", e);
			this.getApplication().getMainWindow().showNotification("保存外线失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		
		// 如果添加外线成功，并且当前外线被置为了默认外线，则更新内存中的数据
		if(sipConfig.getIsDefaultOutline() == true) {
			ShareData.domainToDefaultOutline.put(domain.getId(), outlineName);
		}
		
		return true;
	}
	
	/**
	 * 	当默认外线发生变化时，需要更新所有的队列中的手机成员
	 * @param currentOutlineName	当前的默认外线名称
	 * @param originalOutlineName	原来的默认外线名称
	 */
	private void updatePhoneInQueueMember(String currentOutlineName, String originalOutlineName) {
		// 第一步，判断全局配置外转外
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domain.getId());
		// 如果全局配置不存在，则直接返回
		if(globalSetting == null) {
			return;
		}
		
		// 获取所有非自动使用的队列
		List<Queue> allCommonQueues = queueService.getAllByDomain(domain, true);
		List<String> allCommonQueueNames = new ArrayList<String>();
		for(Queue commonQueue : allCommonQueues) {
			allCommonQueueNames.add(commonQueue.getName());
		}
		
		// 判断全局外转外是否正在运行
		boolean isGlobalRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);
		if(isGlobalRunning) {
			// 如果是外转外是呼入某些固定的手机号（便捷呼叫）
			if(globalSetting.getIsSpecifiedPhones()) {
				for(String phoneNum : globalSetting.getSpecifiedPhones()) {
					for(String queueName : allCommonQueueNames) {
						queueMemberRelationService.removeQueueMemberRelation(queueName, phoneNum+"@"+originalOutlineName);
						queueMemberRelationService.addQueueMemberRelation(queueName, phoneNum+"@"+currentOutlineName, 5);
					} 
				}
				// 如果全局配置是“便捷呼叫”，并且处于进行中状态，则不再处理话务员的自定义配置项
				return;
			} 
			
			// 如果全局配置是“智能呼叫”
			for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
				// 话务员必须有电话号,如果电话号不存在，则直接返回
				String phoneNum = specifiedCsr.getPhoneNumber();
				if(phoneNum == null || "".equals(phoneNum)) {
					continue;
				}
				updateSpecifyCsrQueueMember(specifiedCsr, currentOutlineName, originalOutlineName, phoneNum, allCommonQueueNames);
			}
		} 

		// 第二步：判断话务员自定义的外转外配置(能执行到这，说明如果是“便捷呼叫”，则当前全局配置一定没有运行外转外)
		// 对应自定义的设置而言，添加手机号到队列，需要满足 1、全局配置的外转外呼叫方式不是打到指定的手机号；2、全局配置项中之指定的话务员中不包含当前话务员
		List<Phone2PhoneSetting> customSettings = phone2PhoneSettingService.getAllCsrCustomSettings(domain.getId());
		for(Phone2PhoneSetting customSetting : customSettings) {
			// 话务员必须有电话号,如果电话号不存在，则直接返回
			User csr = customSetting.getCreator();
			String phoneNum = csr.getPhoneNumber();
			if(phoneNum == null || "".equals(phoneNum)) {
				continue;
			}
			
			// 是否需要更新指定的话务员的手机队列成员信息
			boolean needUpdateSpecifyCsr = true;
			if(!globalSetting.getIsSpecifiedPhones()) {
				for(User specifiedCsr : globalSetting.getSpecifiedCsrs()) {
					// 如果话务员包含在全局配置中，则以全局配置为主[智能呼叫：管理员对那些选中的话务员是完全控制外转外的]
					if(specifiedCsr.getId() != null && specifiedCsr.getId().equals(csr.getId())) {
						needUpdateSpecifyCsr = false;
						break;
					}
				}
			}
			
			// 3、话务员持有自定义外转外的授权；4、自定义的外转外存在并是开启状态；5、启动时刻 <= 当前时刻, 并且终止时刻 >= 当前时刻
			if(needUpdateSpecifyCsr && globalSetting.getIsLicensed2Csr()) {
				boolean isCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
				if(isCustomRunning) {
					updateSpecifyCsrQueueMember(csr, currentOutlineName, originalOutlineName, phoneNum, allCommonQueueNames);
				}
			}
		}
	}

	/**
	 * 修改自动外呼任务下的队列中的手机成员
	 * @param currentOutlineName	当前的默认外线名称
	 * @param originalOutlineName	原来的默认外线名称
	 */
	private void updatePhoneInAutoQueueMember(String currentOutlineName, String originalOutlineName) {
		List<AutoDialoutTask> autoDialoutTasks = autoDialoutTaskService.getAllByDialoutType(domain, "自动外呼");
		for(AutoDialoutTask autoDialoutTask : autoDialoutTasks) {
			Queue queue = autoDialoutTask.getQueue();
			String queueName = queue.getName();
			Set<String> phoneNos = autoDialoutTask.getPhoneNos();
			if(phoneNos == null) {
				continue;
			}
			for(String phoneNo : phoneNos) {
				queueMemberRelationService.removeQueueMemberRelation(queueName, phoneNo+"@"+originalOutlineName);
				queueMemberRelationService.addQueueMemberRelation(queueName, phoneNo+"@"+currentOutlineName, 5);
			}
		}
	}

	/**
	 * 根据话务员对象，如果话务员有电话，并且不在线，则更新队列中的手机成员
	 * @param csr					话务员对象
	 * @param outlineName			当前默认外线
	 * @param originalOutlineName	以前的默认外线
	 * @param phoneNum				话务员的电话号码
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void updateSpecifyCsrQueueMember(User csr, String outlineName, String originalOutlineName, String phoneNum, List<String> allCommonQueueNames) {
		// 如果用户已经登陆，则不做处理， 因为用户已登录的情况，呼入队列时找分机
		if(ShareData.userToExten.keySet().contains(csr.getId())) {
			return;
		}
		
		// 只有话务员不在线，才需要将话务员的手机号移入队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员,所以只对非自动外呼队列做处理
				queueMemberRelationService.removeQueueMemberRelation(userQueue.getQueueName(), phoneNum+"@"+originalOutlineName);
				queueMemberRelationService.addQueueMemberRelation(userQueue.getQueueName(), phoneNum+"@"+outlineName, 5);
			}
		}
	}
	
	/**
	 * 修改内存中的数据 (ShareData 中的各Map)
	 * @param outlineName 外线修改后的username 
	 */
	private void updateShareData(String outlineName) {
		// 在保存成功后，将该域的外线重新加载进 内存 ShareData 中去, 只有外线名发生变化才需要更改内存数据
		if(!outlineName.equals(oldSipname)) {
			// 1、修改一个域拥有的外线
			List<String> outlines = ShareData.domainToOutlines.get(domain.getId());
			if(outlines == null) {
				outlines = new ArrayList<String>();
			}
			outlines.remove(oldSipname);
			outlines.add(outlineName);
			ShareData.domainToOutlines.put(domain.getId(), outlines);

			// 2、修改分机对应的动态外线
			for(String exten : ShareData.extenToDynamicOutline.keySet()) {
				String dynOutline = ShareData.extenToDynamicOutline.get(exten);
				if(dynOutline.equals(oldSipname)) {
					ShareData.extenToDynamicOutline.put(exten, outlineName);
				}
			}
			
			// 3、修改分机对应的静态外线
			for(String exten : ShareData.extenToStaticOutline.keySet()) {
				String staticOutline = ShareData.extenToStaticOutline.get(exten);
				if(staticOutline.equals(oldSipname)) {
					ShareData.extenToStaticOutline.put(exten, outlineName);
				}
			}
			
			// 4、修改项目与外线的对应关系
			for(Long projectId : ShareData.projectToOutline.keySet()) {
				String staticOutline = ShareData.projectToOutline.get(projectId);
				if(staticOutline.equals(oldSipname)) {
					ShareData.projectToOutline.put(projectId, outlineName);
				}
			}
			
			// 5、修改外线到项目的对应关系
			Long projectId = ShareData.outlineToProject.get(oldSipname);
			if(projectId != null) {
				ShareData.outlineToProject.remove(oldSipname);
				ShareData.outlineToProject.put(outlineName, projectId);
			}
		}
	}
	
	private class MyFieldFactory extends DefaultFieldFactory{
		private String ipRegex = "([1-9]||[1-9]\\d||1\\d{2}||2[0-4]\\d||25[0-5])(\\.(\\d||[1-9]\\d||1\\d{2}||2[0-4]\\d|25[0-5])){3}";
		private String mailRegex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		
		@Override
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("sipType".equals(propertyId)) {
				sipType = new ComboBox();
				sipType.setImmediate(true);
				sipType.addItem(SipConfigType.sip_outline);
				sipType.addItem(SipConfigType.gateway_outline);
				sipType.setWidth("200px");
				sipType.addListener((ValueChangeListener) EditOutline.this);
				sipType.setNullSelectionAllowed(false);
				return sipType;
			} if("type".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.addItem("friend");
				comboBox.addItem("user");
				comboBox.addItem("peer");
				comboBox.setItemCaption("friend", "任意IP");
				comboBox.setWidth("200px");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}else if("qualify".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setItemCaption("no", "否");
				comboBox.setItemCaption("yes", "是");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}else if("canreinvite".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setItemCaption("no", "否");
				comboBox.setItemCaption("yes", "是");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}else if("belong".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
				comboBox.setWidth("200px");
				for(MobileAreacode areacode:ShareData.areacodeList){
					comboBox.addItem(areacode.getAreaCode());
					comboBox.setItemCaption(areacode.getAreaCode(),areacode.getMobileArea()+"("+areacode.getAreaCode()+")");
				}
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("ispopupWin".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem(true);
				comboBox.addItem(false);
				comboBox.setItemCaption(true, "呼叫-弹屏");
				comboBox.setItemCaption(false, "呼叫-不弹屏");
				comboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("isDefaultOutline".equals(propertyId)) {
				TreeMap<Boolean, String> treeMap = new TreeMap<Boolean, String>();
				treeMap.put(true, "是");
				treeMap.put(false, "否");
				
				CustomComboBox customComboBox = new CustomComboBox();
				customComboBox.setWidth("200px");
				customComboBox.setDataSource(treeMap);
				customComboBox.setNullSelectionAllowed(false);
				
				return customComboBox;
			}else if("nat".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setItemCaption("no", "否");
				comboBox.setItemCaption("yes", "是");
//				asterisk 11 支持force_rport,comedia 这种写法，而不支持 yes 的写法
//				comboBox.addItem("force_rport,comedia");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if ("cancallforward".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}
			
			Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
			field.setWidth("200px");

			if("call_limit".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{1,4}", "call_limit 只能为不大于10000的数值!"));
			} else if("name".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,80}", "name 只能由长度不大于80位的数字组成"));
			} else if("username".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,80}", "username 只能由长度不大于80位的数字组成"));
			} else if("secret".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,80}", "secret 只能由长度不大于80位的数字组成"));
			} else if("context".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "context 只能由长度不大于80位的字符组成"));
			} else if("fromuser".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,80}", "fromuser 只能由长度不大于80位的字符组成"));
			} else if("disallow".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "disallow 只能由长度不大于100位的字符组成"));
			} else if("allow".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "allow 只能由长度不大于100位的字符组成"));
			} else if("host".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex+"||(dynamic)", "host 只能为正确的ip格式或dynamic值"));
			} else if("fromdomain".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex+"||(dynamic)", "fromdomain 只能为正确的ip格式或dynamic值"));
			} else if("lastms".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\-?\\d{1,4}", "lastms 只能为不大于10000的数值"));
			} else if("amaflags".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,7}", "amaflags 只能由长度不大于7位的字符组成或为空"));
			} else if("regseconds".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "regseconds 只能由长度不大于18位的数字组成"));
			} else if("defaultuser".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,80}", "defaultuser 只能由长度不大于80位的数字组成"));
			} else if("mask".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "mask 必须为正确的ip格式或为空"));
			} else if("accountcode".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,20}", "accountcode 只能由长度不大于20位的字符组成或为空"));
			} else if("callgroup".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,10}", "callgroup 只能由长度不大于10位的字符组成或为空"));
			} else if("callerid".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "callerid 只能由长度不大于80位的字符组成或为空"));
			} else if("defaultip".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "defaultip 必须为正确的ip格式或为空"));
			} else if("dtmfmode".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,7}", "dtmfmode 只能由长度不大于7位的字符组成或为空"));
			} else if("fullcontact".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "fullcontact 只能由长度不大于80位的字符组成或为空"));
			} else if("ipaddr".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "ipaddr 必须为正确的ip格式或为空"));
			} else if("port".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,5}", "port 只能由长度不大于5位的数字组成"));
			} else if("regserver".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "regserver 只能由长度不大于100位的字符组成或为空"));
			} else if("insecure".equals(propertyId)) {
				field.addValidator(new RegexpValidator("(\\w*,?\\w*)*", "insecure 只能由长度不大于80位的字符组成或为空(可用逗号隔开)"));
			} else if("mailbox".equals(propertyId)) {
				field.addValidator(new RegexpValidator(mailRegex, "mailbox 邮箱格式不正确或为空"));
			} else if("language".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,2}", "language 只能由长度不大于2位的字符组成或为空"));
			} else if("md5secret".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "md5secret 只能由长度不大于80位的字符组成或为空"));
			} else if("permit".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,95}", "permit 只能由长度不大于95位的字符组成或为空"));
			} else if("deny".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,95}", "deny 只能由长度不大于95位的字符组成或为空"));
			} else if("musiconhold".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "musiconhold 只能由长度不大于100位的字符组成或为空"));
			} else if("pickupgroup".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "pickupgroup 只能由长度不大于100位的字符组成或为空"));
			} else if("regexten".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "regexten 只能由长度不大于80位的字符组成或为空"));
			} else if("restrictcid".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,1}", "restrictcid 只能为小于10的数值或为空"));
			} else if("rtptimeout".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,3}", "rtptimeout 只能为不大于999的数值或为空"));
			} else if("rtpholdtimeout".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,3}", "rtpholdtimeout 只能为不大于999的数值或为空"));
			} else if("setvar".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "setvar 只能由长度不大于100位的字符组成或为空"));
			}
			if (field instanceof TextField) {
				((TextField) field).setNullRepresentation("");
				((TextField) field).setNullSettingAllowed(true);
				((TextField) field).setImmediate(true);
			}
			
			return field;
		}
	}
	
}