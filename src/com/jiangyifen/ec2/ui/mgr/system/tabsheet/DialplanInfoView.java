package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * 软电话 拨号方案详细信息显示界面
 * 
 * @author jrh
 */
@SuppressWarnings("serial")
public class DialplanInfoView extends VerticalLayout {
	
	/**
	 * 构造函数
	 */
	public DialplanInfoView() {
		this.setMargin(true);
		this.setSizeFull();
		this.setSpacing(true);
		
		// 界面顶端布局管理器
		HorizontalLayout upperLayout = new HorizontalLayout();
		upperLayout.setWidth("100%");
		upperLayout.setSpacing(true);
		this.addComponent(upperLayout);
		
		// 创建 拨号方案显示组件
		this.showDialplanInfo(upperLayout);

		// 界面顶端布局管理器
		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setWidth("100%");
		bottomLayout.setSpacing(true);
		this.addComponent(bottomLayout);
		this.setComponentAlignment(bottomLayout, Alignment.BOTTOM_LEFT);
		
//		// 序列号显示信息
//		this.showSequenceCodeInfo(bottomLayout);
	}

	/**
	 * 创建 拨号方案显示组件
	 * @param upperLayout
	 */
	private void showDialplanInfo(HorizontalLayout upperLayout) {
		VerticalLayout leftLayout = new VerticalLayout();
		leftLayout.setWidth("100%");
		leftLayout.setSpacing(true);
		upperLayout.addComponent(leftLayout);
		
		Label description = new Label("<B>拨打以下号码，进入相应功能：</B>", Label.CONTENT_XHTML);
		description.setWidth("-1px");
		leftLayout.addComponent(description);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp000 ：</B>用户登陆(输入用户名及密码)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp001 ：</B>检查该分机是否已经有用户登陆<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp002 ：</B>修改密码<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp003 ：</B>退出系统<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp004 ：</B>获取最近的联系人电话号<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp005 ：</B>分机置忙<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp006 ：</B>取消分机置忙<br/><br/>");
		
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp55 ：</B>分机可根据提示信息创建客服记录<br/><br/>");
		
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp分机号 ：</B>拨打指定分机(如：800001)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp手机或固话 ：</B>拨打手机或固话，拨打外地手机加拨'0',拨打外地固话加拨区号<br/><br/>");

		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'555'+分机号 ：</B>监听：监听指定的分机的当前通话(如555800001)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'556'+分机号 ：</B>密语：管理员可以跟指定的分机说话，但客户听不到管理员的声音(如556800001)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'998'+分机号 ：</B>打入某个会议室，进行三方会议(如998800001)<br/><br/>");
		
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'#'+分机号+'#' ：</B>转接到指定分机，以'#'号开头，并以'#'号结束(如#800001#)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'#'+手机或固话+'#' ：</B>转接到手机或固话，以'#'号开头，并以'#'号结束(如#13888997698#)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'#'+队列名+'#' ：</B>转接到队列，以'#'号开头，并以'#'号结束(如#900001#)<br/><br/>");
		sb.append("<B>&nbsp&nbsp&nbsp&nbsp'#'+888+'#' ：</B>邀请客户对话务员的服务进行评价(如#888#)<br/><br/>");
		
		Label dialplan = new Label(sb.toString(), Label.CONTENT_XHTML);
		dialplan.setWidth("-1px");
		leftLayout.addComponent(dialplan);
	}
	
//	/**
//	 *  序列号显示信息
//	 * @param bottomLayout
//	 */
//	private void showSequenceCodeInfo(HorizontalLayout bottomLayout) {
//		Label sequence = new Label("<B>系统序列号 ：</B>"+GlobalData.SEQUENCE_CODE, Label.CONTENT_XHTML);
//		sequence.setWidth("100%");
//		bottomLayout.addComponent(sequence);
//	}

}
