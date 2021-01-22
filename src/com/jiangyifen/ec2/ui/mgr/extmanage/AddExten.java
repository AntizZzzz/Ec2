package com.jiangyifen.ec2.ui.mgr.extmanage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.ExtManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
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
 * 添加分机组件 
 * @author jrh
 */
@SuppressWarnings("serial")
public class AddExten extends Window implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// form 表格中 分机各字段的显示顺序
	private final Object[] TYPICAL_VISIBLE_PROPERTIES = new Object[] {"secret"};
	
	private final Object[] ADVANCED_VISIBLE_PROPERTIES = new Object[] {"secret", "context", "type", 
			"host", "qualify", "canreinvite", "disallow", "allow", "pickupgroup", "callgroup", "call_limit", // 必填项 
			"nat", "port", "cancallforward", "regseconds", "lastms", "username", "fromuser", "fromdomain", "insecure", "defaultuser", "regserver", 
			"regexten", "fullcontact", "subscribecontext", "accountcode", "amaflags", "callerid", "defaultip", "dtmfmode", "language", "ipaddr", 
			"mailbox", "md5secret", "permit", "deny", "mask", "musiconhold", "restrictcid", "rtptimeout", "rtpholdtimeout", "setvar"};

	
	// form 表格中 分机 必须拥有值的字段
	private final String[] ADVANCED_REQUIRED_PROPERTIES = new String[] {"secret", "context", "type", "host", 
			"qualify", "canreinvite", "disallow", "allow", "pickupgroup",  "callgroup", "call_limit"};
	
	private final Object[] ADVANCED_REQUIRED_PROPERTIES_CAPTION = new Object[] { "密码", "呼出路由", "注册类型", "主机",
			"心跳确认", "二次拨号", "不允许编码", "允许编码", "代接组", "呼叫组", "并发限制"};
	/**
	 * 主要组件输出	
	 */
	private TextField addExtenCount;		// 单次添加分机数量
	
	//Form输出
	private Form form;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;
	private OptionGroup settingType;		// 分机配置类型
	
	/**
	 * 其他参数
	 */
	private Domain domain;
	private SipConfig sipConfig;
	private ExtManagement extManagement;
	private SipConfigService sipConfigService;
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	public AddExten(ExtManagement extManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setHeight("230px");
		this.setWidth("350px");
		this.setCaption("添加分机");
		this.extManagement = extManagement;
		
		domain = SpringContextHolder.getDomain();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");

		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		// 创建表单上方的组件
		createAddCountComponents(windowContent);

		// 创建Form 表单
		createFormComponent(windowContent);
	}
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		sipConfig = new SipConfig();
		settingType.setValue("typical");
		addExtenCount.setValue("");
		
		// 创建简单配置的分机对象
		createTypicalSipConfig();
	}
	
	/**
	 * 创建表单扇面的组件（指定添加数量）
	 * @param windowContent
	 */
	private void createAddCountComponents(VerticalLayout windowContent) {
		// 分机添加数量
		HorizontalLayout countLayout = new HorizontalLayout();
		countLayout.setSpacing(true);
		countLayout.setWidth("100%");
		windowContent.addComponent(countLayout);

		Label typeLabel = new Label("添加数量：");
		typeLabel.setWidth("-1px");
		countLayout.addComponent(typeLabel);
		
		addExtenCount = new TextField();
		addExtenCount.setRequired(true);
		addExtenCount.setWidth("170px");
		addExtenCount.addValidator(new RegexpValidator("[1-9][0-9]?||[1-4][0-9][0-9]||500", "分机数量必须大于0，小于500"));
		addExtenCount.setValidationVisible(false);
		addExtenCount.setImmediate(true);
		addExtenCount.setNullRepresentation("");
		addExtenCount.setNullSettingAllowed(true);
		countLayout.addComponent(addExtenCount);
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
	private void createTypicalSipConfig() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<SipConfig>(sipConfig), Arrays.asList(TYPICAL_VISIBLE_PROPERTIES));
	}

	/**
	 * 创建拥有高级配置的分机对象
	 */
	private void createAdvancedSipConfig() {
		//设置Form的数据源
		form.setItemDataSource(new BeanItem<SipConfig>(sipConfig), Arrays.asList(ADVANCED_VISIBLE_PROPERTIES));
		for(int i = 0; i < ADVANCED_VISIBLE_PROPERTIES.length; i++) {
			form.getField(ADVANCED_VISIBLE_PROPERTIES[i]).setCaption(ADVANCED_VISIBLE_PROPERTIES[i].toString() +"：");
		}
		
		// 设置默认值
		form.getField("context").setValue("outgoing");
		form.getField("type").setValue("friend");
		form.getField("qualify").setValue("yes");
		form.getField("canreinvite").setValue("no");
		form.getField("nat").setValue("yes");
//		asterisk 11 支持force_rport,comedia 这种写法，而不支持 yes 的写法
//		form.getField("nat").setValue("force_rport,comedia");
		form.getField("disallow").setValue("all");
//		form.getField("allow").setValue("alaw");
		form.getField("allow").setValue("g729");
		form.getField("host").setValue("dynamic");
		form.getField("call_limit").setValue("1");
		form.getField("port").setValue("5060"); 
		form.getField("pickupgroup").setValue("1");
		form.getField("callgroup").setValue("1");
		form.getField("cancallforward").setValue("yes");
		
		form.getField("call_limit").setReadOnly(false);
		
		// 将必须填写内容的组件设为必填项
		for(int i=0;i<ADVANCED_REQUIRED_PROPERTIES.length;i++) {
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setRequired(true);
			form.getField(ADVANCED_REQUIRED_PROPERTIES[i]).setCaption(ADVANCED_REQUIRED_PROPERTIES_CAPTION[i].toString());
		}
	}

	/**
	 * Form 表单下方组建
	 * @return VerticalLayout 存放组件的布局管理器
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
			this.setWidth("400px");
			// 创建高级配置所需的组件
			createAdvancedSipConfig();
		}  else {
			this.setHeight("230px");
			this.setWidth("350px");
			// 创建简单配置所需的组件
			createTypicalSipConfig();
		}
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			int count = 0;
			try {
				form.commit();
				
				// 检验分机添加数量是否符合要求
				if(addExtenCount.isValid()) {
					count = Integer.parseInt((String) addExtenCount.getValue());
				} else {
					save.getApplication().getMainWindow().showNotification("分机数量必须大于0，小于500！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}

				// 检查密码输入是否符合要求
				if(((ExtenSecretCustomField) form.getField("secret")).isValid() == false) {
					save.getApplication().getMainWindow().showNotification("密码不能为空！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
			} catch (Exception e) {
				return;
			}
			
			// 保存新添加的分机
			boolean saveSuccess = executeSave(count);
			if(saveSuccess) {
				// 添加成功后，需要往内存的domainToExts 中添加一个分机
				List<SipConfig> extenList = sipConfigService.getAllExtsByDomain(domain);
				List<String> extens = new ArrayList<String>();
				for(SipConfig exten : extenList) {
					extens.add(exten.getName());
					ShareData.extenToDomain.put(exten.getName(), domain.getId());
				}
				ShareData.domainToExts.put(domain.getId(), extens);
				
				// 更新队列在asterisk 中的配置文件  sip_base_domainname.conf
				sipConfigService.updateAsteriskExtenSipConfigFile(domain);
				// 重新加载asterisk 的配置文件
				reloadAsteriskService.reloadSip();
				
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			form.discard();
			this.getParent().removeWindow(this);
		}
	}
	
	/**
	 * 由buttonClick 调用 执行保存方法
	 */
	private boolean executeSave(int count) {
		String typeValue = (String) settingType.getValue();
		String secret = (String) ((ExtenSecretCustomField) form.getField("secret")).getValue();
		
		// 设值
		for(int i = 0; i < count; i++) {
			sipConfig.setIsDefaultOutline(false);
			sipConfig.setSipType(SipConfigType.exten);
			sipConfig.setDomain(domain);
			if("typical".equals(typeValue)) {
				sipConfig.setHost("dynamic");
				sipConfig.setContext("outgoing");
				sipConfig.setPickupgroup("1");
				sipConfig.setCallgroup("1");
				sipConfig.setNat("yes");
				sipConfig.setAllow("g729");
//				asterisk 11 支持force_rport,comedia 这种写法，而不支持 yes 的写法
//				sipConfig.setNat("force_rport,comedia");
			}
			sipConfig.setCall_limit(1);
			
			// 如果返回的密码为空字符串，表示由系统随机生成密码
			if("".equals(secret)) {
				Random random = new Random();
				String result = "";
				for(int j = 0; j < 6;j++) {
					result+=random.nextInt(10);
				}
				sipConfig.setSecret(result);
			} else {
				sipConfig.setSecret(secret);
			}
			
			try {
				sipConfigService.update(sipConfig);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.getApplication().getMainWindow().showNotification("成功添加 "+i+" 个分机，另有"+(count - i)+" 个分机添加失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
				addExtenCount.setValue((count-i)+"");
				return false;
			} 
		}
		
		//刷新sipManagement的Table
		extManagement.updateTable(true);
		extManagement.getTable().setValue(null);
		
		return true;
	}
	
	private class MyFieldFactory extends DefaultFieldFactory{
		private String ipRegex = "([1-9]||[1-9]\\d||1\\d{2}||2[0-4]\\d||25[0-5])(\\.(\\d||[1-9]\\d||1\\d{2}||2[0-4]\\d|25[0-5])){3}";
		private String mailRegex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if("type".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.addItem("friend");
				comboBox.addItem("user");
				comboBox.addItem("peer");
				comboBox.setWidth("200px");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}else if("qualify".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}else if("canreinvite".equals(propertyId)){
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			}else if("nat".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
//				TODO  asterisk 11 支持force_rport,comedia 这种写法，而不支持 yes 的写法
//				comboBox.addItem("force_rport,comedia");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("cancallforward".equals(propertyId)) {
				ComboBox comboBox = new ComboBox();
				comboBox.setImmediate(true);
				comboBox.setWidth("200px");
				comboBox.addItem("yes");
				comboBox.addItem("no");
				comboBox.setNullSelectionAllowed(false);
				return comboBox;
			} else if("secret".equals(propertyId)) {
				ExtenSecretCustomField extenSecretCustomField = new ExtenSecretCustomField();
				extenSecretCustomField.setWidth("200px");
				return extenSecretCustomField;
			}
			
			Field field = DefaultFieldFactory.get().createField(item, propertyId, uiContext);
			field.setWidth("200px");
			
			if("call_limit".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{1,4}", "call_limit 只能为不大于10000的数值!"));
			} else if("host".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex+"||(dynamic)", "host 只能为dynamic或ip格式"));
			} else if("fromdomain".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "fromdomain 必须为正确的ip格式或为空"));
			} else if("mask".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "mask 必须为正确的ip格式或为空"));
			} else if("accountcode".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,20}", "accountcode 只能由长度不大于20位的字符组成或为空"));
			} else if("amaflags".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,7}", "amaflags 只能由长度不大于7位的字符组成或为空"));
			} else if("callgroup".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,10}", "callgroup 只能由长度不大于10位的字符组成或为空"));
			} else if("callerid".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "callerid 只能由长度不大于80位的字符组成或为空"));
			} else if("context".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "context 只能由长度不大于80位的字符组成或为空"));
			} else if("defaultip".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "defaultip 必须为正确的ip格式或为空"));
			} else if("dtmfmode".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,7}", "dtmfmode 只能由长度不大于7位的字符组成或为空"));
			} else if("fromuser".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "fromuser 只能由长度不大于80位的字符组成或为空"));
			} else if("disallow".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "disallow 只能由长度不大于100位的字符组成"));
			} else if("allow".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "allow 只能由长度不大于100位的字符组成"));
			} else if("fullcontact".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "fullcontact 只能由长度不大于80位的字符组成或为空"));
			} else if("ipaddr".equals(propertyId)) {
				field.addValidator(new RegexpValidator(ipRegex, "ipaddr 必须为正确的ip格式或为空"));
			} else if("port".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,5}", "port 只能由长度不大于5位的数字组成"));
			} else if("regserver".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,100}", "regserver 只能由长度不大于100位的字符组成或为空"));
			} else if("regseconds".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\d{0,18}", "regseconds 只能由长度不大于18位的数字组成或为空"));
			} else if("lastms".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\-?\\d{1,4}", "lastms 只能为不大于10000的数值"));
			} else if("username".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "username 只能由长度不大于80位的字符组成或为空"));
			} else if("defaultuser".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "defaultuser 只能由长度不大于80位的字符组成或为空"));
			} else if("insecure".equals(propertyId)) {
				field.addValidator(new RegexpValidator("(\\w*,?\\w*)*", "insecure 只能由长度不大于80位的字符组成或为空"));
			} else if("secret".equals(propertyId)) {
				field.addValidator(new RegexpValidator("\\w{0,80}", "secret 只能由长度不大于80位的字符组成或为空"));
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
			}
			
			return field;
		}
	}

}
