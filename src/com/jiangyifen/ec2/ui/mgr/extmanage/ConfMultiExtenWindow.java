package com.jiangyifen.ec2.ui.mgr.extmanage;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.ExtManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @Description 描述：批量修改分机配置项
 * 
 * @author  jrh
 * @date    2014年1月21日 上午10:32:13
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class ConfMultiExtenWindow extends Window implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 要修改的列的标题名称
	private final String[] CAPTION_NAMES = new String[] {"name", "secret", "context", "type", "host", "qualify", 
			"canreinvite", "disallow", "allow", "pickupgroup", "callgroup", "nat", "port", "cancallforward"};
	
	private final Object[] CAPTION_NAMES_CAPTION = new Object[] {"分机号码", "密码", "呼出路由", "注册类型", "主机", "心跳确认",
			"二次拨号", "不允许编码", "允许编码", "代接组", "呼叫组", "内网穿透","端口","呼叫流转"};
	
	// 上述列对应的组件的默认值
	private final String[] DEFAULT_VALUES = new String[] {"", "456321", "outgoing", "friend", 
			"dynamic", "yes", "yes", "all", "g729", "1", "1", "yes", "5060", "yes"};

	// 上述列对应的组件默认的可用状态
	private Boolean[] COL2_ENABLES = new Boolean[]{true, true, false, false, false, false, 
			false,  false, true, false, false, false, false,  false};
	
	private GridLayout main_glo;
	private TextField name_tf;
	private TextField secret_tf;
	private TextField context_tf;
	private ComboBox type_cb;
	private TextField host_tf;
	private ComboBox qualify_cb;
	private ComboBox canreinvite_cb;
	private TextField disallow_tf;
	private TextField allow_tf;
	private TextField pickupgroup_tf;
	private TextField callgroup_tf;
	private ComboBox nat_cb;
	private TextField port_tf;
	private ComboBox cancallforward_cb;
	
	private Label name_lb;
	private Button secret_bt;
	private Button context_bt;
	private Button type_bt;
	private Button host_bt;
	private Button qualify_bt; 
	private Button canreinvite_bt; 
	private Button disallow_bt; 
	private Button allow_bt; 
	private Button pickupgroup_bt;
	private Button callgroup_bt; 
	private Button nat_bt; 
	private Button port_bt; 
	private Button cancallforward_bt;
	
	//保存按钮和取消按钮
	private Button save;
	private Button cancel;

	/**
	 * 其他参数
	 */
	private Domain domain;
	private ExtManagement extManagement;
	private SipConfigService sipConfigService;
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	public ConfMultiExtenWindow(ExtManagement extManagement) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setWidth("490px");
		this.setHeight("500px");
		this.setCaption("批量配置分机");
		this.extManagement = extManagement;

		domain = SpringContextHolder.getDomain();		
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");

		//添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		main_glo = new GridLayout(3, 15);
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
		
		LinkedHashMap<Long,SipConfig> needEditSips = extManagement.getNeedDeleteSips();
		String name_value = StringUtils.join(needEditSips.values(), ",");
		name_tf.setReadOnly(false);
		name_tf.setValue(name_value);
		name_tf.setDescription("<b>"+name_value+"</b>");
		name_tf.setReadOnly(true);
		name_lb.setValue(needEditSips.size()+"个");
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
		String ipRegex = "([1-9]||[1-9]\\d||1\\d{2}||2[0-4]\\d||25[0-5])(\\.(\\d||[1-9]\\d||1\\d{2}||2[0-4]\\d|25[0-5])){3}";
		
		int row = -1;
		name_tf = createTextField(++row, "(\\d+,?\\d+)+", null);
		secret_tf = createTextField(++row, "\\d{1,80}", 80);
		secret_tf.setValue(DEFAULT_VALUES[row]);
		context_tf = createTextField(++row, "\\w{1,80}", 80);
		type_cb = createComboBox(++row, new String[] {"friend", "user", "peer"});
		host_tf = createTextField(++row, ipRegex+"||(dynamic)", 15);
		qualify_cb = createComboBox(++row, new String[] {"yes", "no"});
		canreinvite_cb = createComboBox(++row, new String[] {"yes", "no"});
		disallow_tf = createTextField(++row, "\\w{1,100}", 100);
		allow_tf = createTextField(++row, "\\w{1,100}", 100);
		allow_tf.setValue(DEFAULT_VALUES[row]);
		pickupgroup_tf = createTextField(++row, "\\w{1,100}", 100);
		callgroup_tf = createTextField(++row, "\\w{1,10}", 10);
		nat_cb = createComboBox(++row, new String[] {"yes", "no"});
		port_tf = createTextField(++row, "\\d{1,5}", 5);
		cancallforward_cb = createComboBox(++row, new String[] {"yes", "no"});
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
	private ComboBox createComboBox(int row, String[] items) {
		ComboBox cb = new ComboBox();
		for(String item : items) {
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
	 * 创建第三列组件：按钮
	 */
	private void createCol3Componets() {
		int row = -1;
		name_lb = new Label("", Label.CONTENT_XHTML);
		name_lb.setWidth("-1px");
		main_glo.addComponent(name_lb, 2, ++row);
		secret_bt = createOptButton(++row);
		context_bt = createOptButton(++row);
		type_bt = createOptButton(++row);
		host_bt = createOptButton(++row);
		qualify_bt = createOptButton(++row);
		canreinvite_bt = createOptButton(++row);
		disallow_bt = createOptButton(++row);
		allow_bt = createOptButton(++row);
		pickupgroup_bt = createOptButton(++row);
		callgroup_bt = createOptButton(++row);
		nat_bt = createOptButton(++row);
		port_bt = createOptButton(++row);
		cancallforward_bt = createOptButton(++row);
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
			// 保存编辑后的分机
			boolean saveSuccess = executeSave();
			if(saveSuccess) {
				// 更新队列在asterisk 中的配置文件  sip_base_domainname.conf
				sipConfigService.updateAsteriskExtenSipConfigFile(domain);
				// 重新加载asterisk 的配置文件
				reloadAsteriskService.reloadSip();
				
				this.getParent().removeWindow(this);
			}
		} else if(source == cancel) {
			this.getParent().removeWindow(this);
		} else if(source == secret_bt) {
			updateTextFieldStatus(secret_bt, secret_tf);
		} else if(source == context_bt) {
			updateTextFieldStatus(context_bt, context_tf);
		} else if(source == type_bt) {
			updateComboBoxStatus(type_bt, type_cb);
		} else if(source == host_bt) {
			updateTextFieldStatus(host_bt, host_tf);
		} else if(source == qualify_bt) {
			updateComboBoxStatus(qualify_bt, qualify_cb);
		} else if(source == canreinvite_bt) {
			updateComboBoxStatus(canreinvite_bt, canreinvite_cb);
		} else if(source == disallow_bt) {
			updateTextFieldStatus(disallow_bt, disallow_tf);
		} else if(source == allow_bt) {
			updateTextFieldStatus(allow_bt, allow_tf);
		} else if(source == pickupgroup_bt) {
			updateTextFieldStatus(pickupgroup_bt, pickupgroup_tf);
		} else if(source == callgroup_bt) {
			updateTextFieldStatus(callgroup_bt, callgroup_tf);
		} else if(source == nat_bt) {
			updateComboBoxStatus(nat_bt, nat_cb);
		} else if(source == port_bt) {
			updateTextFieldStatus(port_bt, port_tf);
		} else if(source == cancallforward_bt) {
			updateComboBoxStatus(cancallforward_bt, cancallforward_cb);
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
			dst_comp.setValue(DEFAULT_VALUES[row]);
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
			if(!secret_tf.isEnabled() && !context_tf.isEnabled() && !type_cb.isEnabled() 
					&& !host_tf.isEnabled() && !qualify_cb.isEnabled() && !canreinvite_cb.isEnabled() 
					&& !disallow_tf.isEnabled() && !allow_tf.isEnabled() && !pickupgroup_tf.isEnabled() 
					&& !callgroup_tf.isEnabled() && !nat_cb.isEnabled() && !port_tf.isEnabled() 
					&& !cancallforward_cb.isEnabled()) {
				this.getApplication().getMainWindow().showNotification("对不起，您没有修改任何配置信息，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
			
			String secret = StringUtils.trimToEmpty((String) secret_tf.getValue());
			String context = StringUtils.trimToEmpty((String) context_tf.getValue());
			String type = StringUtils.trimToEmpty((String) type_cb.getValue());
			String host = StringUtils.trimToEmpty((String) host_tf.getValue());
			String qualify = StringUtils.trimToEmpty((String) qualify_cb.getValue());
			String canreinvite = StringUtils.trimToEmpty((String) canreinvite_cb.getValue());
			String disallow = StringUtils.trimToEmpty((String) disallow_tf.getValue());
			String allow = StringUtils.trimToEmpty((String) allow_tf.getValue());
			String pickupgroup = StringUtils.trimToEmpty((String) pickupgroup_tf.getValue());
			String callgroup = StringUtils.trimToEmpty((String) callgroup_tf.getValue());
			String nat = StringUtils.trimToEmpty((String) nat_cb.getValue());
			String port = StringUtils.trimToEmpty((String) port_tf.getValue());
			String cancallforward = StringUtils.trimToEmpty((String) cancallforward_cb.getValue());

			// 验证输入的信息是否合法
			if(secret_tf.isEnabled() && (!secret_tf.isValid() || "".equals(secret))) {
				this.getApplication().getMainWindow().showNotification("secret 只能由长度不大于80位的字符组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(context_tf.isEnabled() && (!context_tf.isValid() || "".equals(context))) {
				this.getApplication().getMainWindow().showNotification("context 只能由长度不大于80位的字符组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(type_cb.isEnabled() && (!type_cb.isValid() || "".equals(type))) {
				this.getApplication().getMainWindow().showNotification("type 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(host_tf.isEnabled() && (!host_tf.isValid() || "".equals(host))) {
				this.getApplication().getMainWindow().showNotification("host 只能为dynamic，或正确的ip格式", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(qualify_cb.isEnabled() && (!qualify_cb.isValid() || "".equals(qualify))) {
				this.getApplication().getMainWindow().showNotification("qualify 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(canreinvite_cb.isEnabled() && (!canreinvite_cb.isValid() || "".equals(canreinvite))) {
				this.getApplication().getMainWindow().showNotification("canreinvite 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(disallow_tf.isEnabled() && (!disallow_tf.isValid() || "".equals(disallow))) {
				this.getApplication().getMainWindow().showNotification("disallow 只能由长度不大于100位的字符组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(allow_tf.isEnabled() && (!allow_tf.isValid() || "".equals(allow))) {
				this.getApplication().getMainWindow().showNotification("allow 只能由长度不大于100位的字符组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(pickupgroup_tf.isEnabled() && (!pickupgroup_tf.isValid() || "".equals(pickupgroup))) {
				this.getApplication().getMainWindow().showNotification("pickupgroup 只能由长度不大于100位的字符组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(callgroup_tf.isEnabled() && (!callgroup_tf.isValid() || "".equals(callgroup))) {
				this.getApplication().getMainWindow().showNotification("callgroup 只能由长度不大于10位的字符组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(nat_cb.isEnabled() && (!nat_cb.isValid() || "".equals(nat))) {
				this.getApplication().getMainWindow().showNotification("nat 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(port_tf.isEnabled() && (!port_tf.isValid() || "".equals(port))) {
				this.getApplication().getMainWindow().showNotification("port 只能由长度不大于5位的数字组成，且不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			} else if(cancallforward_cb.isEnabled() && (!cancallforward_cb.isValid() || "".equals(cancallforward))) {
				this.getApplication().getMainWindow().showNotification("cancallforward 不能为空", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			// 更新数据
			LinkedHashMap<Long,SipConfig> needEditSips = extManagement.getNeedDeleteSips();
			for(SipConfig sip : needEditSips.values()) {
				if(!"".equals(secret)) {
					sip.setSecret(secret);
				}
				if(!"".equals(context)) {
					sip.setContext(context);
				}
				if(!"".equals(type)) {
					sip.setType(type);
				}
				if(!"".equals(host)) {
					sip.setHost(host);
				}
				if(!"".equals(qualify)) {
					sip.setQualify(qualify);
				}
				if(!"".equals(canreinvite)) {
					sip.setCanreinvite(canreinvite);
				}
				if(!"".equals(disallow)) {
					sip.setDisallow(disallow);
				}
				if(!"".equals(allow)) {
					sip.setAllow(allow);
				}
				if(!"".equals(pickupgroup)) {
					sip.setPickupgroup(pickupgroup);
				}
				if(!"".equals(callgroup)) {
					sip.setCallgroup(callgroup);
				}
				if(!"".equals(nat)) {
					sip.setNat(nat);
				}
				if(!"".equals(port)) {
					sip.setPort(port);
				}
				if(!"".equals(cancallforward)) {
					sip.setCancallforward(cancallforward);
				}
				sipConfigService.update(sip);
			}
			
			//刷新sipManagement的Table
			extManagement.clearNeedDeleteSips(); 
			extManagement.updateTable(false); 
			extManagement.getTable().setValue(null); 
			this.getApplication().getMainWindow().showNotification("保存分机成功！");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			this.getApplication().getMainWindow().showNotification("保存分机失败，可能是信息填写有误！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		
		return true;
	}
	
}