package com.jiangyifen.ec2.ui.mgr.kbinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KbInfoType;
import com.jiangyifen.ec2.service.eaoservice.KbInfoTypeService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * 添加 知识窗口
 * @author lxy
 *
 */
public class EditKbInfoTypeWinodw extends Window implements ClickListener{
	
	//静态-非业务
	private static final long serialVersionUID = -2964718416132235880L;	 
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	//静态-业务

	//持有UI对象
	private KbInfoTypeManagement kbInfoTypeManagement;
	
	//本控件持有对象
	
	//页面布局组件
  	
	//页面组件
 	private Label lb_msg;
 	private TextField tf_name;
	private Button bt_edit_kit;
	private Button bt_cancel;
	
	//业务必须对象
	private Domain domain;
	//业务本页对象
	private KbInfoType kbInfoType;
	//业务注入对象
 	private KbInfoTypeService kbInfoTypeService;
	
 	public EditKbInfoTypeWinodw(KbInfoTypeManagement kbInfoTypeManagement){
 		this.kbInfoTypeManagement = kbInfoTypeManagement;
		initThis();
		initSpringContext();
		initCompanent();
	}
 	
 	private void initThis() {
 		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("编辑知识类型");
		this.setWidth("400px");
		this.setHeight("140px");
 	}

 	private void initSpringContext() {
 		domain = SpringContextHolder.getDomain();
 		kbInfoTypeService = SpringContextHolder.getBean("kbInfoTypeService");
	}

	private void initCompanent() {
 		 buildContextLayout();
 	}

	private void buildContextLayout(){
		GridLayout gl_form = new GridLayout(2,3);
 		gl_form.setSpacing(true);
		gl_form.addComponent(new Label("标题<font color='red'>*</font>",Label.CONTENT_XHTML));
		tf_name = new TextField();
		tf_name.setMaxLength(200);
		tf_name.setWidth("300px");
		gl_form.addComponent(tf_name);
		this.addComponent(gl_form);
		 
		
		HorizontalLayout hl_bottom = new HorizontalLayout();
		hl_bottom.setSpacing(true);
		bt_edit_kit = new Button("保存",this);
		hl_bottom.addComponent(bt_edit_kit);
		
		bt_cancel = new Button("关闭",this);
		hl_bottom.addComponent(bt_cancel);
		
		lb_msg = new Label("",Label.CONTENT_XHTML);
		hl_bottom.addComponent(lb_msg);
		this.addComponent(hl_bottom);
	}
	
	 
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_edit_kit) {
			excuteEditType();
		}else if(source == bt_cancel){
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}
	
	/**编辑类型**/
	private void excuteEditType(){
		try {
			if(editTypeValidateForm()){
				KbInfoType kit =  kbInfoTypeService.getKbInfoTypeById(kbInfoType.getId());
				kit.setName(this.tf_name.getValue().toString());
				kbInfoTypeService.updateKbInfoType(kit);
 				this.tf_name.setValue("");
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("编辑名称成功"));
				kbInfoTypeManagement.updateTreeDataAndParentUi();
				this.getApplication().getMainWindow().removeWindow(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("编辑知识类型异常_excuteAddType_LLXXYY", e);
			this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("编辑名称异常"));
			kbInfoTypeManagement.updateTreeDataAndParentUi();
			this.getApplication().getMainWindow().removeWindow(this);
		}
		
	}
	
	/**编辑类型校验**/
	private boolean editTypeValidateForm(){
		if(!WorkUIUtils.stringIsEmpty(this.tf_name.getValue())){	//校验添加的类型名称
			if(null != kbInfoType){
				KbInfoType pkit = kbInfoType.getParenetType();
				if(null != pkit){	//父类不为空说明	不在根目录下
					int count = kbInfoTypeService.getKbInfoTypeCountByNameAndParent(this.tf_name.getValue().toString(),pkit.getId(), domain.getId());
					if(count > 0){
						showLMsg(WorkUIUtils.fontColorHtmlString("该类型已经存在，请更换别的名称"));
						return false;
					}else{
						return true;
					}
				}else{				//父类为空说明为	根目录下
					int count = kbInfoTypeService.getKbInfoTypeCountByName(this.tf_name.getValue().toString(), domain.getId());
					if(count > 0){
						showLMsg(WorkUIUtils.fontColorHtmlString("该类型已经存在，请更换别的名称"));
						return false;
					}else{
						return true;
					}
				}
			}else{
				this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("未选中父类型，请在左边选中父类型"));
				return false;
			}		
		}else{
			showLMsg(WorkUIUtils.fontColorHtmlString("需要编辑的知识类型名称不能为空,请填写"));
			return false;
		}
	}
	
	public void refreshComponentInfo(KbInfoType kbInfoType){
		this.kbInfoType = kbInfoType;
		if(null != kbInfoType){
			this.tf_name.setValue(kbInfoType.getName());
			this.lb_msg.setValue("");
		}else{
			this.lb_msg.setValue("没有知识类型不能保存");
		}
	}
 
	private void showLMsg(String info){
		this.lb_msg.setValue(info);
	}

}
