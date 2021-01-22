package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ReloadAsteriskService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.queuemanage.AddQueue;
import com.jiangyifen.ec2.ui.mgr.queuemanage.ConfMultiQueueWindow;
import com.jiangyifen.ec2.ui.mgr.queuemanage.EditQueue;
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
 * 队列管理  组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class QueueManagement extends VerticalLayout implements Property.ValueChangeListener, Action.Handler, ClickListener {

	// 表格中队列的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"name", "description", "musiconhold", "strategy", "timeout", "wrapuptime", "maxlen", 
			"autopause", "dynamicmember", "isDefaultQueue", "isrecordSoundNotice", "readDigitsType", "joinempty", "setinterfacevar"};
	private final String[] VISIBLE_HEADERS = new String[] {"队列名称", "队列描述","等待音乐","振铃策略","坐席超时","冷却时长", "最大深度", 
			"未接置忙", "动态成员", "是否默认", "是否录音提醒", "播报类型", "允许入空队列","变量配置"};
	
	//搜索组件
	private TextField keyword;
	private Button search;
	
	//队列表格组件
	private Table table;
	private String searchSql;
	private String countSql;
	private FlipOverTableComponent<Queue> flip;
	
	//队列表格按钮组件
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
	private AddQueue addQueueWindow;
	private EditQueue editQueueWindow;
	private ConfMultiQueueWindow confMultiQueueWindow;

	/**
	 * 其他变量
	 */
	private Domain domain;
	private LinkedHashMap<Long, Queue> needDeleteQueues;			// 用于存储需要删除的队列（表格中被勾选的对象）
	
	private QueueService queueService;								// 队列服务类
	private UserService userService;								// 用户服务类
	private UserQueueService userQueueService;						// 动态队列成员服务类
	private StaticQueueMemberService staticQueueMemberService;		// 静态队列成员服务类
	private MarketingProjectService marketingProjectService;		// 项目服务类
	private ReloadAsteriskService reloadAsteriskService;			// 重新加载asterisk 的配置
	
	/**
	 * 构造器	
	 */
	public QueueManagement() {
		this.setMargin(true);
		this.setSpacing(true);
		this.setWidth("100%");

		needDeleteQueues = new LinkedHashMap<Long, Queue>();
		domain = SpringContextHolder.getDomain();
		
		queueService = SpringContextHolder.getBean("queueService");
		userService = SpringContextHolder.getBean("userService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		reloadAsteriskService = SpringContextHolder.getBean("reloadAsteriskService");
		
		// 创建队列表格上方的组件（包括搜索组件及多项删除组件）
		createTableTopComponents();

		// 创建队列表格显示区组件
		createQueueTable();
		
		// 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
		createTableFooterComponents();
	}

	/**
	 *  创建搜索需要的响应组件
	 */
	private void createTableTopComponents() {
		HorizontalLayout tableTopHLayout = new HorizontalLayout();
		tableTopHLayout.setWidth("100%");
		this.addComponent(tableTopHLayout);
		
		// 创建搜索队列的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		tableTopHLayout.addComponent(searchHLayout);
		
		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		
		keyword = new TextField();
		keyword.setImmediate(true);
		keyword.setInputPrompt("请输入搜索关键字");
		keyword.setDescription("可按'队列名称' 或 '描述内容'进行搜索");
		keyword.setStyleName("search");
		keyword.addListener(this);
		searchHLayout.addComponent(keyword);
		
		search = new Button("搜索", this);
		search.setImmediate(true);
		searchHLayout.addComponent(search);
	}
	
	/**
	 *  创建队列表格显示区组件
	 */
	private void createQueueTable() {
		table = createFormatColumnTable();
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
	 *  创建格式化 了 显示内容的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
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

				//判断true or false  
				if(v!=null&&v instanceof Boolean) {
					boolean value = (Boolean) property.getValue();
					if(value == true) {
						return "是";
					}else if(value == false){
						return "否";
					}
				}
				
				if(colId==null){
					return null;
				}else if(colId.equals("strategy")) {
					String strategy = (String) property.getValue();
					if("ringall".equals(strategy)) {
						return "群振";
					} else if("roundrobin".equals(strategy)) {
						return "轮询分配";
					} else if("rrmemory".equals(strategy)) {
						return "记忆轮询分配";
					} else if("leastrecent".equals(strategy)) {
						return "按最后一次接通分配";
					} else if("fewestcalls".equals(strategy)) {
						return "按呼叫次数分配";
					} else if("random".equals(strategy)) {
						return "随机分配";
					}
				} else if(colId.equals("isDefaultQueue")) {
					boolean value = (Boolean) property.getValue();
					if(value == true) {
						return "是";
					} 
					return "否";
				} else if(colId.equals("isrecordSoundNotice")) {
					boolean value = (Boolean) property.getValue();
					return value ? "已开启-录音提醒" : "已关闭-录音提醒";
				} else if(colId.equals("readDigitsType")) {
					String value = (String) property.getValue();
					if(Queue.READ_DIGITS_TYPE_READ_NONE.equals(value)) {
						return "无";
					} else if(Queue.READ_DIGITS_TYPE_READ_EMPNO.equals(value)) {
						return "报工号";
					} else if(Queue.READ_DIGITS_TYPE_READ_EXTEN.equals(value)) {
						return "报分机号";
					}
					return "无";
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
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
		
		// 初始化查询语句
		initializeSql();
		
		flip = new FlipOverTableComponent<Queue>(Queue.class, queueService, table, searchSql, countSql, null);
		flip.setPageLength(20, false);
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(VISIBLE_HEADERS);
		table.addGeneratedColumn("delete", new DeleteColumnGenerator());
		table.setColumnHeader("delete", "删除");
		footerHLayout.addComponent(flip);
		footerHLayout.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据不同的需求，初始化查询语句
	 */
	private void initializeSql() {
		String keywordValue = StringUtils.trimToEmpty((String) keyword.getValue());
		String keywordSql = "";
		if(!"".equals(keywordValue)) {
			keywordSql = " and (q.description like '%" + keywordValue + "%' or q.name like '%" + keywordValue + "%')";
		}
		
		countSql = "select count(q) from Queue as q where q.domain.id = " + domain.getId() +" and q.isModifyable = true "+keywordSql;
		searchSql = countSql.replaceFirst("count\\(q\\)", "q") + " order by q.name asc";
	}

	/**
	 * 用于自动生成“删除”列，用于标记 是否将对应行的队列删除
	 */
	private class DeleteColumnGenerator implements Table.ColumnGenerator {
		
		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			final Queue queue = (Queue) itemId;
			final CheckBox check = new CheckBox();
			check.setImmediate(true);
			if(needDeleteQueues.containsKey(queue.getId())) {
				check.setValue(true);
			}
			
			check.addListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					Boolean value = (Boolean) check.getValue();
					if(value == false) {
						needDeleteQueues.remove(queue.getId());
					} else {
						needDeleteQueues.put(queue.getId(), queue);
					}
					
					// 修改删除按钮
					updateButtonStatus();
				}
			});
			
        	return check;
		}
	}

	/**
	 * 修改删除按钮
	 */
	private void updateButtonStatus() {
		if(needDeleteQueues.size() > 0) {
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
	 * 当用户点击的添加队列按钮时，buttonClick调用显示 添加 队列窗口
	 */
	private void showAddWindow() {
		if(addQueueWindow==null){
			addQueueWindow=new AddQueue(this);
		}
		this.getWindow().addWindow(addQueueWindow);
	}
	
	/**
	 * 当用户点击的编辑队列按钮时，buttonClick调用显示 编辑 队列窗口
	 */
	private void showEditWindow() {
		if(editQueueWindow==null){
			editQueueWindow=new EditQueue(this);
		}
		this.getWindow().addWindow(editQueueWindow);
	}

	/**
	 * 当用户点击的批量编辑队列按钮时，buttonClick调用显示 编辑 队列窗口
	 */
	private void showConfMultiQueueWindow() {
		if(confMultiQueueWindow==null){
			confMultiQueueWindow=new ConfMultiQueueWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(confMultiQueueWindow);
		this.getApplication().getMainWindow().addWindow(confMultiQueueWindow);
	}
	
	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			Queue target = (Queue) table.getValue();
			edit.setEnabled(target != null);
			if(target == null && needDeleteQueues.size() == 0) {
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
				for(Object queue : table.getItemIds()) {
					Long id = ((Queue) queue).getId();
					needDeleteQueues.put(id, (Queue)queue);
				}
			} else if("InvertCurrentPage".equals(value)) {
				// 保存之前在当前页选中的Id 号
				Set<Long> originalIds = new HashSet<Long>();
				for(Object queue : table.getItemIds()) {
					Long id = ((Queue) queue).getId();
					if(needDeleteQueues.containsKey(id)) {	// 如果之前选中了，则加入set集合
						originalIds.add(id);
					}
					needDeleteQueues.put(id, (Queue)queue);
				}
				// 再将在当前页原来选中的去除
				for(Long qId : originalIds) {
					needDeleteQueues.remove(qId);
				}
			} else if("SelectAllPages".equals(value)) {
				String searchSql = flip.getSearchSql();
				System.out.println(searchSql);
				List<Queue> sips = queueService.getAllByJpql(searchSql);
				for(Queue queue : sips) {
					needDeleteQueues.put(queue.getId(), queue);
				}
			} else if("InvertAllPages".equals(value)) {
				// 保存之前所有选中的Id 号
				Set<Long> originalIds = new HashSet<Long>(needDeleteQueues.keySet());
				// 先将所有的客户放入集合
				String searchSql = flip.getSearchSql();
				System.out.println(searchSql);
				List<Queue> sips = queueService.getAllByJpql(searchSql);
				for(Queue queue : sips) {
					needDeleteQueues.put(queue.getId(), queue);
				}
				// 再将原来选中的去除
				for(Long qId : originalIds) {
					needDeleteQueues.remove(qId);
				}
			} else if("RemoveCurrentPage".equals(value)) {
				for(Object queue : table.getItemIds()) {
					Long id = ((Queue) queue).getId();
					needDeleteQueues.remove(id);
				}
			} else if("RemoveAllPages".equals(value)) {
				needDeleteQueues.clear();
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
			showConfMultiQueueWindow();
		} else if(source == delete) {
			executeDelete();
		} 
	}

	/**
	 * 执行删除
	 */
	private void executeDelete() {
		int size = needDeleteQueues.size();
		if(size > 0) {
			ArrayList<Long> queueIds = new ArrayList<Long>(needDeleteQueues.keySet());
			for(int i = 0; i < size; i++) {
				Long queueId = queueIds.get(i);
				Queue queue = needDeleteQueues.get(queueId);
				if(confirmQueueDeleteAble(queue) == true) {
					queueService.deleteById(queue.getId());
				}
				needDeleteQueues.remove(queueId);
			}
		} else {
			Queue queue = (Queue) table.getValue();
			if(queue != null && confirmQueueDeleteAble(queue) == true) {
				queueService.deleteById(queue.getId());
			}
		}
		
		// 删除队列成功后，更新该域所对应的asterisk配置文件 queues_domainname.conf
		queueService.updateAsteriskQueueFile(domain);
		// 重新加载asterisk 的配置文件
		reloadAsteriskService.reloadQueue();
		
		table.setValue(null);
		delete.setCaption("删 除");
		delete.setEnabled(false);
		
		updateTable(false);
	}

	/**
	 * 检查该队列拥有的用户CSR 是否存在已经登陆了的，如果存在，则不允许删除
	 * 如果queue 可以被删除，
	 * 		那么在此也要讲与其相关的UserQueue 删除
	 * 		同时，还要讲与其相关的静态成员StaticQueueMember 一起删除
	 * @param queue
	 * @return
	 */
	private boolean confirmQueueDeleteAble(Queue queue) {
		
		// 1、检查当前队列是否是默认外线，如果是，则不让删
		if(queue.getIsDefaultQueue() == true) {
			this.getApplication().getMainWindow().showNotification("队列 " +queue.getName()+ " 是当前域的默认队列，请修改默认队列后再试！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		// 2、检查动态成员中是否有人已经登陆
		List<UserQueue> uqs = userQueueService.getAllByQueueName(queue.getName(), domain);
		for(UserQueue uq : uqs) {
			for(Long userId : ShareData.extenToUser.values()) {
				User user = userService.get(userId);
				if(user.getUsername().equals(uq.getUsername())) {
					this.getApplication().getMainWindow().showNotification("已有队列成员在线，暂不可删除！", Notification.TYPE_WARNING_MESSAGE);
					return false;
				}
			}
		}
		
		// 3、检查静态成员中是否有队列正在被使用
		List<StaticQueueMember> sqms = staticQueueMemberService.getAllByQueueName(domain, queue.getName());
		for(StaticQueueMember sqm : sqms) {
			if(ShareData.extenToUser.containsKey(sqm.getSipname())) {
				this.getApplication().getMainWindow().showNotification("已有队列成员在线，暂不可删除！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}
		}
		
		// 4、检查该队列是否已经被项目使用，如果有，则不让删
		List<MarketingProject> projects = marketingProjectService.getAllByQueue(queue.getId(), domain.getId());
		if(projects.size() > 0) {
			this.getApplication().getMainWindow().showNotification("项目"+projects.get(0).getProjectName()+"正在使用，暂不可删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		// 5、jrh 2014-02-21 在指定域下，根据队列的名称，检查是否与之关联的IVRAction 对象，如果有，则表示不能删除	
		boolean isDeleteAble = queueService.checkDeleteAbleByIvrAction(queue.getName(), domain.getId()); 
		if(!isDeleteAble) {
			this.getApplication().getMainWindow().showNotification("队列 " +queue.getName()+ " 正在被IVR(呼入语音导航)使用，暂时不能删除！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		// 能执行到这，表示queue 可以被删除，那么在此也要讲与其相关的UserQueue 删除
		for(UserQueue uq : uqs) {
			userQueueService.deleteById(uq.getId());
		}
		
		// 能执行到这，表示queue 可以被删除，那么在此也要将与其相关的静态队列成员删除
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
			if(needDeleteQueues.size() > 0) {
				confMulti.click();
			} else {
				this.getApplication().getMainWindow().showNotification("请您先勾选要修改的队列！", Notification.TYPE_WARNING_MESSAGE);
			}
		}
	}
	
	/**
	 * 刷新显示队列的表格
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

	public LinkedHashMap<Long, Queue> getNeedDeleteQueues() {
		return needDeleteQueues;
	}

	public void clearNeedDeleteQueues() {
		needDeleteQueues.clear();
		updateButtonStatus();
	}
}
