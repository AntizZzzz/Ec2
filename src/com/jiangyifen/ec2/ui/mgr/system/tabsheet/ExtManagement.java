package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.SipConfigType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.extmanage.AddExten;
import com.jiangyifen.ec2.ui.mgr.extmanage.ConfMultiExtenWindow;
import com.jiangyifen.ec2.ui.mgr.extmanage.EditExten;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 分机管理 组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class ExtManagement extends VerticalLayout implements Property.ValueChangeListener, Action.Handler, ClickListener {
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"name", "secret", "context", "type", "qualify", "canreinvite", "disallow", "allow", "host" };
	private final String[] VISIBLE_HEADERS = new String[] {"分机号码", "密码", "呼出路由", "注册类型", "心跳确认", "二次拨号", "不允许编码", "允许编码", "主机" };

	//搜索组件
	private TextField keyword;
	private Button search;
	
	//分机表格组件
	private Table table;
	private String searchSql;
	private String countSql;
	private SipConfigService sipConfigService;
	private ReloadAsteriskService reloadAsteriskService;
	private FlipOverTableComponent<SipConfig> flip;
	
	//分机表格按钮组件
	private ComboBox selectType_cb;							// 选择类型（全选当前页、反选当前页等）
	private Button add;
	private Button edit;
	private Button delete;
	private Button confMulti;
	
	/**
	 * 右键组件
	 */
	private Action ADD= new Action("添加");
	private Action EDIT= new Action("编辑");
	private Action DELETE= new Action("删除");
	private Action CONF_MULTI = new Action("批量配置勾选项");

	/**
	 * 弹出窗口
	 */
	//弹出窗口 只创建一次
	private AddExten addExtWindow;
	private EditExten editExtWindow;
	private ConfMultiExtenWindow confMultiExtWindow;

	/**
	 * 其他变量
	 */
	private Domain domain;
	private LinkedHashMap<Long,SipConfig> needDeleteSips;
	private StaticQueueMemberService staticQueueMemberService;
	
	/**
	 * 构造器	
	 */
	public ExtManagement() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("100%");
		
		needDeleteSips = new LinkedHashMap<Long, SipConfig>();
		domain = SpringContextHolder.getDomain();
		sipConfigService = SpringContextHolder.getBean("sipConfigService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		
		// 创建队列表格上方的组件（包括搜索组件及多项删除组件）
		createTableTopComponents();

		// 创建分机表格显示区组件
		createSipConfigTable();
		
		// 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
		createTableFooterComponents();
	}

	/**
	 *  创建搜索需要的响应组件
	 */
	private void createTableTopComponents() {
		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		this.addComponent(searchHLayout);
		
		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		
		keyword = new TextField();
		keyword.setImmediate(true);
		keyword.setInputPrompt("请输入搜索关键字");
		keyword.setDescription("可按'分机名称'进行搜索");
		keyword.setStyleName("search");
		keyword.addListener(this);
		searchHLayout.addComponent(keyword);
		
		search = new Button("搜索", this);
		search.setImmediate(true);
		searchHLayout.addComponent(search);
	}
	
	/**
	 *  创建分机表格显示区组件
	 */
	private void createSipConfigTable() {
		// 创建表格
		table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				Object v = property.getValue();
				
				//判断yes or no  
				if(v!=null&&v instanceof String) {
					String value=(String)v;
					if("yes".equals(value)){
						return "是";
					}else if("no".equals(value)){
						return "否";
					}else if("true".equals(value)){
						return "是";
					}else if("false".equals(value)){
						return "否";
					}
					//continue
				}
				
				if(v==null){
					return "";
				}else if (v instanceof Date) {
					// 缺点是每创建一行就创建一次SimpleDateFormat对象
					return new SimpleDateFormat("yyyy年MM月dd日 hh时mm分ss秒")
							.format(v);
				} else if("context".equals(colId)) {
					if("incoming".equals(v)){
						return "呼出";
					}else if("outgoing".equals(v)){
						return "呼入";
					}
				} else if("type".equals(colId)) {
					if("friend".equals(v)){
						return "任意IP";
					}else{
						//fail to default
					}
				} else if("disallow".equals(colId)) {
					if("all".equals(v)){
						return "所有";
					}else{
						//fail to default
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setSizeFull();
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.addActionHandler(this);
		table.setPageLength(20);
		this.addComponent(table);
	}
	
	/**
	 *  创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
	 */
	private void createTableFooterComponents() {
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		footerHLayout.setWidth("100%");
		this.addComponent(footerHLayout);

		HorizontalLayout leftFooter = new HorizontalLayout();
		leftFooter.setSpacing(true);
		footerHLayout.addComponent(leftFooter);

		Label selectType_lb = new Label("<b>全选/反选：</b>", Label.CONTENT_XHTML);
		selectType_lb.setWidth("-1px");
		leftFooter.addComponent(selectType_lb);
		leftFooter.setComponentAlignment(selectType_lb, Alignment.MIDDLE_LEFT);
		
		selectType_cb = new ComboBox();
		selectType_cb.setWidth("100px");
		selectType_cb.addListener(this);
		selectType_cb.setImmediate(true);
		selectType_cb.setNullSelectionAllowed(true);
		selectType_cb.addItem("SelectCurrentPage");
		selectType_cb.addItem("InvertCurrentPage");
		selectType_cb.addItem("RemoveCurrentPage");
		selectType_cb.addItem("SelectAllPages");
		selectType_cb.addItem("InvertAllPages");
		selectType_cb.addItem("RemoveAllPages");
		selectType_cb.setItemCaption("SelectCurrentPage", "全选当前页");
		selectType_cb.setItemCaption("InvertCurrentPage", "反选当前页");
		selectType_cb.setItemCaption("RemoveCurrentPage", "不选当前页");
		selectType_cb.setItemCaption("SelectAllPages", "全选所有页");
		selectType_cb.setItemCaption("InvertAllPages", "反选所有页");
		selectType_cb.setItemCaption("RemoveAllPages", "不选所有页");
		leftFooter.addComponent(selectType_cb);
		leftFooter.setComponentAlignment(selectType_cb, Alignment.MIDDLE_LEFT);
		
		add = new Button("添 加", this);
		add.setImmediate(true);
		leftFooter.addComponent(add);
		
		edit = new Button("编 辑", this);
		edit.setImmediate(true);
		edit.setEnabled(false);
		leftFooter.addComponent(edit);
		
		delete = new Button("删 除", this);
		delete.setImmediate(true);
		delete.setEnabled(false);
		leftFooter.addComponent(delete);
		
		confMulti = new Button("批量配置勾选项", this);
		confMulti.setImmediate(true);
		confMulti.setEnabled(false);
		leftFooter.addComponent(confMulti);
		
		// 初始化搜索语句
		initializeSql();
		
		flip = new FlipOverTableComponent<SipConfig>(SipConfig.class, sipConfigService, table, searchSql, countSql, null);
		flip.setPageLength(20, false);
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(VISIBLE_HEADERS);
		table.addGeneratedColumn("delete", new DeleteColumnGenerator());
		table.setColumnHeader("delete","勾选项");
		footerHLayout.addComponent(flip);
		footerHLayout.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据不同的需求，初始化查询语句
	 */
	private void initializeSql() {
		String keywordValue = StringUtils.trimToEmpty((String) keyword.getValue());
		String keyWordSql = "";
		if(!"".equals(keywordValue)) {
			keyWordSql = " and s.name like '%" + keywordValue + "%'";
		}
		String sipTypeSql = getSipTypeSql(SipConfigType.exten);
		
		countSql = "select count(s) from SipConfig as s where s.domain.id = " +domain.getId()+ " and s.sipType = " +sipTypeSql+ keyWordSql;
		searchSql = countSql.replaceFirst("count\\(s\\)", "s") + " order by s.name asc";
	}

	/**
	 * jrh 根据SipConfig对象，得到用于查询sip 的类型创建收索语句语句
	 * @param 	sipConfigType 	sip类型
	 * @return 	String
	 */
	private String getSipTypeSql(SipConfigType sipConfigType) {
		String statuStr = sipConfigType.getClass().getName() + ".";
		if (sipConfigType.getIndex() == 0) {
			statuStr += "exten";
		} else if (sipConfigType.getIndex() == 1) {
			statuStr += "sip_outline";
		} else if (sipConfigType.getIndex() == 2) {
			statuStr += "gateway_outline";
		}
		return statuStr;
	}
	
	/**
	 * 用于自动生成“删除”列，用于标记 是否将对应行的分机删除
	 */
	private class DeleteColumnGenerator implements Table.ColumnGenerator {
		
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			final SipConfig sip = (SipConfig) itemId;
			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			if(needDeleteSips.containsKey(sip.getId())) {
				check.setValue(true);
			}
			
			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if(value == false) {
						needDeleteSips.remove(sip.getId());
					} else {
						needDeleteSips.put(sip.getId(), sip);
					}
					
					// 修改删除按钮
					updateButtonStatus();
				}
			});
			
        	return check;
		}
	}

	/**
	 *  修改按钮状态，如删除按钮、批量编辑按钮等
	 */
	private void updateButtonStatus() {
		if(needDeleteSips.size() > 0) {
			delete.setCaption("删除勾选项");
			delete.setEnabled(true);
			confMulti.setEnabled(true);
		} else {
			delete.setCaption("删 除");
			confMulti.setEnabled(false);
			delete.setEnabled(table.getValue() != null);
		}
	}
	
	/**
	 * 当用户点击的添加分机按钮时，buttonClick调用显示 添加 分机窗口
	 */
	private void showAddWindow() {
		if(addExtWindow==null){
			addExtWindow=new AddExten(this);
		}
		this.getApplication().getMainWindow().addWindow(addExtWindow);
	}
	
	/**
	 * 当用户点击的编辑分机按钮时，buttonClick调用显示 编辑 分机窗口
	 */
	private void showEditWindow() {
		if(editExtWindow==null){
			editExtWindow=new EditExten(this);
		}
		this.getApplication().getMainWindow().addWindow(editExtWindow);
	}
	
	/**
	 * 当用户点击的批量编辑分机按钮时，buttonClick调用显示 编辑 分机窗口
	 */
	private void showConfMultiExtenWindow() {
		if(confMultiExtWindow==null){
			confMultiExtWindow=new ConfMultiExtenWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(confMultiExtWindow);
		this.getApplication().getMainWindow().addWindow(confMultiExtWindow);
	}
	
	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			SipConfig target = (SipConfig) table.getValue();
			edit.setEnabled(target != null);
			if(target == null && needDeleteSips.size() == 0) {
				delete.setEnabled(false);
			} else {
				delete.setEnabled(true);
			}
		} else if(source == keyword) {
			search.click();
		} else if(source == selectType_cb) {
			String value = (String) selectType_cb.getValue();
			if(value == null) {
				return;
			}
			
			if("SelectCurrentPage".equals(value)) {
				for(Object sip : table.getItemIds()) {
					Long id = ((SipConfig) sip).getId();
					needDeleteSips.put(id, (SipConfig)sip);
				}
			} else if("InvertCurrentPage".equals(value)) {
				// 保存之前在当前页选中的Id 号
				Set<Long> originalIds = new HashSet<Long>();
				for(Object sip : table.getItemIds()) {
					Long id = ((SipConfig) sip).getId();
					if(needDeleteSips.containsKey(id)) {	// 如果之前选中了，则加入set集合
						originalIds.add(id);
					}
					needDeleteSips.put(id, (SipConfig)sip);
				}
				// 再将在当前页原来选中的去除
				for(Long sId : originalIds) {
					needDeleteSips.remove(sId);
				}
			} else if("SelectAllPages".equals(value)) {
				String searchSql = flip.getSearchSql();
				List<SipConfig> sips = sipConfigService.getAllByJpql(searchSql);
				for(SipConfig sip : sips) {
					needDeleteSips.put(sip.getId(), sip);
				}
			} else if("InvertAllPages".equals(value)) {
				// 保存之前所有选中的Id 号
				Set<Long> originalIds = new HashSet<Long>(needDeleteSips.keySet());
				// 先将所有的客户放入集合
				String searchSql = flip.getSearchSql();
				List<SipConfig> sips = sipConfigService.getAllByJpql(searchSql);
				for(SipConfig sip : sips) {
					needDeleteSips.put(sip.getId(), sip);
				}
				// 再将原来选中的去除
				for(Long sId : originalIds) {
					needDeleteSips.remove(sId);
				}
			} else if("RemoveCurrentPage".equals(value)) {
				for(Object sip : table.getItemIds()) {
					Long id = ((SipConfig) sip).getId();
					needDeleteSips.remove(id);
				}
			} else if("RemoveAllPages".equals(value)) {
				needDeleteSips.clear();
			} 
			flip.refreshInCurrentPage();
			
			// 修改删除按钮
			updateButtonStatus();
			selectType_cb.setValue(null);
		}
	}
	
	/**
	 * 处理各种操作按钮的单击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search) {
			initializeSql();
			flip.setCountSql(countSql);
			flip.setSearchSql(searchSql);
			flip.refreshToFirstPage();
			table.setValue(null);
		} else if(source == add) {
			showAddWindow();
		} else if(source == edit) {
			showEditWindow();
		} else if(source == confMulti) {
			showConfMultiExtenWindow();
		} else if(source == delete) {
			executeDelete();
		} 
	}

	/**
	 * 执行删除
	 */
	private void executeDelete() {
		int size = needDeleteSips.size();
		if(size > 0) {
			ArrayList<Long> sipIds = new ArrayList<Long>(needDeleteSips.keySet());
			for(int i = 0; i < size; i++) {
				Long sipId = sipIds.get(i);
				SipConfig sip = needDeleteSips.get(sipId);
				if(confirmSipDeleteAble(sip) == true) {
					// 1、删除分机
					sipConfigService.deleteById(sip.getId());

					// 2、删除域与当前被删除分机的对应关系
					List<String> extens = ShareData.domainToExts.get(domain.getId());
					extens.remove(sip.getName());
				}
				// 不管能不能删，都应该将其从集合中移除
				needDeleteSips.remove(sipId);
			}
		} else {
			SipConfig sip = (SipConfig) table.getValue();
			if(sip != null && confirmSipDeleteAble(sip) == true) {
				// 1、删除分机
				sipConfigService.deleteById(sip.getId());

				// 2、删除域与当前被删除分机的对应关系
				List<String> extens = ShareData.domainToExts.get(domain.getId());
				extens.remove(sip.getName());
			}
		}

		// 更新分机在asterisk 中的配置文件  sip_base_domainname.conf
		sipConfigService.updateAsteriskExtenSipConfigFile(domain);
		// 修改配置文件成功， 则重新加载asterisk 的配置文件
		reloadAsteriskService.reloadSip();
		
		table.setValue(null);
		delete.setCaption("删 除");
		delete.setEnabled(false);
		
		updateTable(false);
	}

	/**
	 * 检验分机是否已经有用户在使用，如果是则返回false 表示该分机不可删除
	 * 	如果能执行到这，表示可以删除sip分机，因此也要删除sip对应的静态队列成员
	 * @param sip
	 * @return
	 */
	private boolean confirmSipDeleteAble(SipConfig sip) {
		// 检查sip分机是否可删
		if(ShareData.extenToUser.containsKey(sip.getName())) {
			this.getApplication().getMainWindow().showNotification("分机 " +sip.getName()+ " 正在被使用，暂不能删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		// jrh 2014-02-21	检查是否与之关联的IVRAction 对象，如果有，则表示不能删除
		boolean isDeleteAble = sipConfigService.checkDeleteAbleByIvrAction(true, sip.getName(), domain.getId()); 
		if(!isDeleteAble) {
			this.getApplication().getMainWindow().showNotification("分机 " +sip.getName()+ " 正在与IVR(呼入语音导航关联)，暂不能删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		// 如果能执行到这，表示可以删除sip分机，因此也要删除sip对应的静态队列成员
		List<StaticQueueMember> sqms = staticQueueMemberService.getAllBySipname(domain, sip.getName());
		for(StaticQueueMember sqm : sqms) {
			staticQueueMemberService.deleteById(sqm.getId());
		}
		
		return true;
	}
	
	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[]{ADD, CONF_MULTI};
		} 
		return new Action[]{ADD, EDIT, DELETE, CONF_MULTI};
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if(ADD==action){
			add.click();
		}else if(EDIT==action){
			edit.click();
		}else if(DELETE==action){
			delete.click();
		}else if(CONF_MULTI==action){
			if(needDeleteSips.size() > 0) {
				confMulti.click();
			} else {
				this.getApplication().getMainWindow().showNotification("请您先勾选要修改的分机！", Notification.TYPE_WARNING_MESSAGE);
			}
		}
	}
	
	/**
	 * 刷新显示分机的表格
	 * @param isToFirst 刷新后是否跳转至第一页
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(searchSql);
			flip.setCountSql(countSql);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	public Table getTable() {
		return table;
	}

	public LinkedHashMap<Long, SipConfig> getNeedDeleteSips() {
		return needDeleteSips;
	}

	public void clearNeedDeleteSips() {
		needDeleteSips.clear();
		updateButtonStatus();
	}
}
