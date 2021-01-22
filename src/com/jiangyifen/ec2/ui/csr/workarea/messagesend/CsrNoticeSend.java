package com.jiangyifen.ec2.ui.csr.workarea.messagesend;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.csr.ManagerSendNoticeToCsrService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 内部消息发送主窗体
 * @author lxy
 *
 */
public class CsrNoticeSend  extends VerticalLayout implements Button.ClickListener{

	private static final long serialVersionUID = -8848794316921781522L;
	
	//主要组件
	//消息组件
	private TextField title;
	private TextField receiver;
    private RichTextArea content;
    private Button addReceiver;
    private Button removeAll;
    private Button viewHistory;
    private Button send;
    
    /**
     * 弹出窗口
     */
    private CsrSelectCsr selectCsrWindow;
    
	/**
	 *  其它组件   
	 */
    private List<User> allCsrs;
    private User loginUser;
    private Domain domain;
    private UserService userService;
	//private DepartmentService departmentService;
	//TODO  private MgrTabSheet mgrTabSheet;
    
	/**
	 * 构造器
	 * @return 
	 */
	//TODO public NoticeSend(MgrTabSheet mgrTabSheet) {
	public CsrNoticeSend() {
		this.setSizeFull();
		//TODO this.mgrTabSheet=mgrTabSheet;
		
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		userService=SpringContextHolder.getBean("userService");
		//departmentService=SpringContextHolder.getBean("departmentService");
		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setHeight("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		//创建新建通知组件
		VerticalLayout tempNoticeLayout = buildNewNoticeLayout();
		constrantLayout.addComponent(tempNoticeLayout);
		tempNoticeLayout.setHeight("100%");
		constrantLayout.setExpandRatio(tempNoticeLayout, 10.0f);
		CsrCsrTree treeComponent = new CsrCsrTree(this);
		treeComponent.setHeight("100%");
		constrantLayout.addComponent(treeComponent);
		constrantLayout.setExpandRatio(treeComponent, 2.0f);
//		TODO
		//让allCsrs中有数据
		this.updateCsrInfo();
	}
	
	/**
	 * 显示新建消息输出的页面
	 * @return
	 */
	private VerticalLayout buildNewNoticeLayout() {
		VerticalLayout newNoticeLayout=new VerticalLayout();
		newNoticeLayout.setSpacing(true);
		newNoticeLayout.setWidth("100%");
		//选中的CSR
		HorizontalLayout constraintCsrLayout=new HorizontalLayout();
		constraintCsrLayout.setWidth("100%");
		Label tempLabel = new Label("接收人:");
		tempLabel.setSizeUndefined();
		constraintCsrLayout.addComponent(tempLabel);
		receiver=new TextField();
		receiver.setWidth("100%");
		constraintCsrLayout.addComponent(receiver);
		constraintCsrLayout.setExpandRatio(receiver, 1.0f);
		addReceiver=new Button("添加接收人");
		addReceiver.setStyleName("small");
		addReceiver.addListener((Button.ClickListener)this);
		constraintCsrLayout.addComponent(addReceiver);
		removeAll=new Button("清除全部");
		removeAll.setStyleName("small");
		removeAll.addListener((Button.ClickListener)this);
		constraintCsrLayout.addComponent(removeAll);
		newNoticeLayout.addComponent(constraintCsrLayout);
		
		//标题栏
		HorizontalLayout constraintLayout=new HorizontalLayout();
		constraintLayout.setWidth("100%");
		Label tempLabel1 = new Label("标&nbsp&nbsp&nbsp题:",Label.CONTENT_XHTML);
		tempLabel1.setSizeUndefined();
		constraintLayout.addComponent(tempLabel1);
		title=new TextField();
		title.setWidth("100%");
		constraintLayout.addComponent(title);
		constraintLayout.setExpandRatio(title, 1.0f);
		newNoticeLayout.addComponent(constraintLayout);
		
		//消息内容
		content = new RichTextArea();
		content.setSizeFull();
		content.setValue("");
		newNoticeLayout.addComponent(content);
		newNoticeLayout.setExpandRatio(content, 1.0f);
		
		//按钮输出
		HorizontalLayout constraintButtonsLayout=new HorizontalLayout();
		constraintButtonsLayout.setSpacing(true);
		newNoticeLayout.addComponent(constraintButtonsLayout);
		newNoticeLayout.setComponentAlignment(constraintButtonsLayout, Alignment.BOTTOM_LEFT);
		
		//按钮
		send=new Button("发 送");
		send.setStyleName("default");
		send.addListener((Button.ClickListener)this);
		constraintButtonsLayout.addComponent(send);
		
		viewHistory=new Button("查看历史");
		viewHistory.addListener((Button.ClickListener)this);
		
		return newNoticeLayout;
	}
	
	
	/**
	 * 显示添加CSR的窗口
	 */
	private void showSelectCsrWindow() {
		if(selectCsrWindow==null){
			selectCsrWindow=new CsrSelectCsr(this);
		}
		this.getWindow().addWindow(selectCsrWindow);
	}
	
	/**
	 * 执行给项目添加CSR的任务
	 */
	private void executeSend() {
		//对接收人栏的处理
		String receiverStr="";
		if(receiver.getValue()!=null){
			receiverStr=receiver.getValue().toString().trim();
		}
		if(receiverStr.equals("")){
			this.getApplication().getMainWindow().showNotification("请先添加接收人", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		//验证Csr的工号信息
		String result=validateEmpNos();
		if(result!=null){
			this.getApplication().getMainWindow().showNotification("找不到工号为<i>"+result+"</i>的CSR！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		//标题
		String titleStr="";
		if(title.getValue()!=null){
			titleStr=title.getValue().toString();
		}
		if(titleStr.trim().equals("")){
			this.getApplication().getMainWindow().showNotification("消息标题不能为空", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		//内容
		String contentStr = StringUtils.trimToEmpty((String) content.getValue());
		if("".equals(contentStr)){
			this.getApplication().getMainWindow().showNotification("消息内容不能为空", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		Set<User> selectedCsrs = this.getSelectedCsrs(); //根据工号取出Csr
		//对Notice进行保存
		Notice notice=new Notice();
		notice.setTitle(titleStr);
		notice.setContent(contentStr);
		notice.setDomain(SpringContextHolder.getDomain());
		notice.setSendDate(new Date());
		notice.setReceivers(selectedCsrs);
		notice.setSender(SpringContextHolder.getLoginUser());
		ManagerSendNoticeToCsrService sendService=SpringContextHolder.getBean("managerSendNoticeToCsrService");
		sendService.save(notice, selectedCsrs);
		sendService.refreshCsrUi(selectedCsrs);

		//提示消息
		//TODO mgrTabSheet.showHistoryNotice(true);
		Notification msgNotif=new Notification("成功给"+selectedCsrs.size()+"个CSR发送了通知!",Notification.TYPE_HUMANIZED_MESSAGE);
		this.getWindow().showNotification(msgNotif);
		
		//将Csr全部置空,并清空收件人，标题，内容
		selectedCsrs=null;
		receiver.setValue("");
		title.setValue("");
		content.setValue("");
	}
	
	/**
	 * 由executeSend调用，验证Csr的工号信息
	 * @return 验证通过返回null，否则返回工号
	 */
	private String validateEmpNos() {
		//由于发送之前已经验证，接收人中不可能为空
		String usefulSelectCsrStr=(String)receiver.getValue();
		usefulSelectCsrStr=usefulSelectCsrStr.trim();
		//去查找选中的Csr
		String[] csrEmpNos = usefulSelectCsrStr.split(",");
		for(String str:csrEmpNos){
			User user=getUserByEmpNo(str);
			if(user==null){
				return str;
			}
		}
		return null; //验证通过
	}

	/**
	 * 有可能新添了Csr，更新Csr的信息
	 */
	public void updateCsrInfo() {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		/*for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}*/
		// jrh  获取当前用户所属部门及其子部门下的所有用户
		//TODO  控制用户可发送的规则
		allGovernedDeptIds.add(loginUser.getDepartment().getId());
		allCsrs = new ArrayList<User>();
		allCsrs.addAll(userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId()));
	}
	
	/**
	 * 取得所有的Csr
	 * @return
	 */
	public List<User> getAllCsrs() {
		return allCsrs;
	}
	
	/**
	 * 根据接收人的EmpNo去查找所有Csr，取得选中的Csr
	 * <p>如果为null，则工号格式不正确</p>
	 */
	public Set<User> getSelectedCsrs() { //用Set来排除重复
		Set<User> selectedCsrs=new HashSet<User>();
		String usefulSelectCsrStr="";
		if(receiver.getValue()!=null){
			usefulSelectCsrStr=(String)receiver.getValue();
			usefulSelectCsrStr=usefulSelectCsrStr.trim();
		}
		//如果输入栏中为空返回空集合
		if(usefulSelectCsrStr.equals("")){
			return selectedCsrs;
		}
		
		//去查找选中的Csr
		String[] csrEmpNos = usefulSelectCsrStr.split(",");
		for(String str:csrEmpNos){
			User user=getUserByEmpNo(str);
			if(user!=null){
				selectedCsrs.add(user);
			}
		}
		return selectedCsrs;
	}
	
	/**
	 * 由 getSelectedCsrs调用
	 * 根据empNo取得Csr
	 */
	private User getUserByEmpNo(String empNoStr) {
		empNoStr=empNoStr.trim();
		//根据工号查找Csr
		for(User csr:allCsrs){
			if(csr.getEmpNo().equals(empNoStr)){
				return csr;
			}
		}
		return null;
	}

	/**
	 * 设置选中的CSR，只是起到一个设置接收人菜单的值得作用
	 * @param selectedCsrs
	 */
	public void setSelectedCsrs(Set<User> selectedCsrs) {
		String usefulStr="";//只记录EmpNo的值
		for(User csr:selectedCsrs){
			if(usefulStr.equals("")){
				usefulStr+=csr.getEmpNo();
			}else{
				usefulStr+=(","+csr.getEmpNo());
			}
		}
		receiver.setValue(usefulStr);
	}
	/**
	 * 添加一个CSR
	 */
	public void addSelectedCsr(String empNo) {
		//获取已经选中的Csr的字符串
		String receiverStr="";
		if(receiver.getValue()!=null){
			receiverStr=receiver.getValue().toString().trim();
		}
		//添加Csr
		if(receiverStr.equals("")){
			receiverStr+=empNo;
		}else{
			receiverStr+=","+empNo;
		}
		receiver.setValue(receiverStr);
	}
	/**
	 * 添加一个部门的CSR
	 */
	public void addSelectedCsrs(List<User> selectedCsrs) {
		//获取已经选中的Csr的字符串
		String receiverStr="";
		if(receiver.getValue()!=null){
			receiverStr=receiver.getValue().toString().trim();
		}
		//判断是否已经存在，如果存在，不处理，否则添加新的EmpNo
		for(User user:selectedCsrs){
			if(receiverStr.indexOf(user.getEmpNo())==-1){
				if(receiverStr.equals("")){
					receiverStr+=user.getEmpNo();
				}else{
					receiverStr+=","+user.getEmpNo();
				}
			}
			//否则不做任何处理
		}
		receiver.setValue(receiverStr);
	}
	
	/**
	 * 添加Csr按钮单击事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton().equals(addReceiver)){
			showSelectCsrWindow();
		}else if(event.getButton().equals(removeAll)){
			receiver.setValue("");
		}else if(event.getButton().equals(send)){
			executeSend();
		}else if(event.getButton().equals(viewHistory)){
			//TODO mgrTabSheet.showHistoryNotice(false);
		}
	}
}
