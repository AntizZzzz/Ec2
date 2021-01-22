package com.jiangyifen.ec2.iface;
/**
 * Ec2系统的接口
 * @author chb
 *
 */
public interface Ec2Iface {
//	/**
//	 * 认证
//	 * @param userName
//	 * @param password
//	 * @return
//	 */
//	public String userAuthentication(String userName,String password);
	
	/**
	 * 绑定分机
	 * @param extenNo
	 * @param userName
	 * @param realName
	 * @return
	 */
	public Boolean bindToSip(String extenNo,String userName,String realName);
	
	/*
上下线：   1为下线       0为上线
更改上下线的状态
update dst_commodity set commoditystatus=1 where commodityno='207587';



update dst_commodity set commoditystatus=0 where commodityno='161154';
update dst_commodity set commoditystatus=1 where commodityno='207587';
update dst_commodity set commoditystatus=1 where commodityno='207334';
update dst_commodity set commoditystatus=1 where commodityno='273381';

//===================更新或者添加视频的code================//
update dst_video set mediacode='Umai:PROG/933954@BESTV.SMG.SMG' where id=(select d.id from dst_video d,dst_commodity c where c.video_id=d.id and c.commodityno='175522');


	 * */
	/**
	 * 发起呼叫
	 * @param exten
	 * @param phoneNumber
	 * @return
	 */
	public String originate(String exten,String phoneNumber); //通过分机打电话
	
	/**
	 * 挂断电话
	 * @param channel
	 * @return
	 */
	public Boolean hangUp(String channel); //根据channel挂断
	public Boolean hangUp(String userName,String exten); //挂断指定用户的所有分机
	
	/**
	 * 呼叫转接
	 * @param channel
	 * @param extension
	 * @return
	 */
	public Boolean redirectCall(String channel,String exten);
		
}
