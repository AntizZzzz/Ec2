package com.jiangyifen.ec2.sms;

import java.io.IOException;

public class Demo_Client {

//	首页>序列号详细信息
//	序列号:SDK-JHK-010-00001
//	特服号:207431
//	余额:0
//	密码:204063
//	状态:未用
//	签名:Label
//	注册用户:Label
//	联系人:Label
//	电话:Label
//	手机:Label
//	备注:
//	地址:Label
//http://sdk2.entinfo.cn/z_send.aspx?sn=SDK-JHK-010-00001&pwd=204063&mobile=15021157421&content=hichen
	
	public static void main(String[] args) throws IOException {

		String sn = "SDK-JHK-010-00001";
		String pwd = "204063";
		Client client = new Client(sn, pwd);

		String result_mt = client.mt("15021157421,15692122889", "Hi,What a u doing?", "", "", "");
		System.out.print(result_mt);
		
		/*
		 * 方法名称：mt 功 能：发送短信 参 数：mobile,content,ext,stime,rrid(手机号，内容，扩展码，定时时间，唯一标识)
		 * 返 回 值：唯一标识，如果不填写rrid将返回系统生成的
		 */
//		public String mt(String mobile, String content, String ext, String stime,
//				String rrid) {
		
		// 如为第一次使用，请先注册，注册方法如下，为方便在后来的合作成功客服人员给您提供服务请如实填写下列信息
		// 只需注册一次即可
		// 返回值说明：注册成功返回0 注册成功
		/*
		 * String result_register = client.register("省份", "城市", "服务", "公司名称",
		 * "联系人", "88888888", "13910423404", "111@126.com", "88888888", "北京",
		 * "123456"); if(result_register.startsWith("-")) {
		 * System.out.print(result_register); }else {
		 * System.out.print("恭喜您，注册成功！"); }
		 * 
		 * 
		 * //充值，参数依次为充值卡号和密码 String result_charge = client.chargeFee("", ""); if
		 * (result_charge.startsWith("-")) { System.out.print(result_charge);
		 * }else { System.out.print("充值成功,最新余额为：" + client.getBalance() + "条"); }
		 * 
		 * //查询余额 String result_balance = client.getBalance();
		 * System.out.print(result_balance);
		 * 
		 * //短信发送 String result_mt = client.mt("", "程序接口测试", "", "", "");
		 * System.out.print(result_mt);
		 * 
		 * //查询msgid //此方法用来查询最后成功发送短信的100次msgid String result_msgid =
		 * client.msgid(); System.out.print(result_msgid);
		 * 
		 * //接收短信 String result_mo = client.mo(); if (result_mo.startsWith("-")) {
		 * //接收失败的情况，输出失败信息 System.out.print(result_mo); }else if ("1" ==
		 * result_mo) { System.out.print("无可接收信息"); }else { //多条信息的情况，以回车换行分割
		 * String[] result = result_mo.split("\r\n"); for(int i=0;i<result.length;i++) {
		 * //内容做了url编码，在此解码，编码方式gb2312
		 * System.out.print(URLDecoder.decode(result_mo, "gb2312") + "\r\n"); } }
		 * String content1= URLEncoder.encode("您好1","gb2312"); String content2=
		 * URLEncoder.encode("您好2","gb2312"); String content3=
		 * URLEncoder.encode("您好3","gb2312"); String mobile1="15201692834";
		 * String mobile2="15201692834";//手机号不同 String mobile3="15201692834";
		 * String content4=content1+content2+content3; String
		 * mobile4=mobile1+mobile2+mobile3; String
		 * result=client.gxmt(mobile4,content4,"","","");
		 * System.out.print(result);
		 */

//		List list = client.RECSMSEx("1");
//		if (list != null && list.size() > 0) {
//			for (int i = 0; i <= list.size() - 1; i++) {
//				String str = "";
//				str = list.get(i).toString();
//				String aa[] = str.split(",");
//				System.out.print("特服号为: " + aa[1]);
//				System.out.print("手机号为: " + aa[2]);
//				System.out.print("短信内容为: " + URLDecoder.decode(aa[3], "GB2312"));// 对内容进行解码
//				System.out.print("回复时间为: " + aa[4]);
//				System.out.println("");
//			}
//		} else {
//			System.out.println("没有回复数据");
//		}
	}
}
