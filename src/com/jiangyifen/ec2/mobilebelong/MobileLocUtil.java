package com.jiangyifen.ec2.mobilebelong;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.MobileLoc;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.MobileLocUpdateIfaceType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 如果手机号码合法(11位或12位以0开头),则根据数据库中信息判断归属地,本地去0，外地加0，否则不处理
 * @author chb
 *
 */
public class MobileLocUtil {
	
	/**
	 * 根据更新接口更新号码数据库信息, TODO 暂时没有实现
	 * @return
	 */
	public static Boolean updateDb(MobileLocUpdateIfaceType updateIface){
		return false;
	}
	

	/**
	 * chb 20130730  弹屏显示相应信息 添加方法
	 * 取得电话号码的AreaCode
	 * @param phoneNumber
	 * @return mobileLoc 返回号码信息，如果为空则没有查找到相应信息  
	 */
	public static MobileLoc getMobileAreaCode(String phoneNumber){
		//截取7位 1330000
		if(phoneNumber==null) return null;
		
		if(phoneNumber.length()!=11&&phoneNumber.length()!=12)
			return null;

		if(phoneNumber.length()==12&&phoneNumber.startsWith("0"))
			phoneNumber=phoneNumber.substring(1);
		
		//查找指定电话号
		String abbresiveNumber=StringUtils.left(phoneNumber, 7);
		CommonService commonService=SpringContextHolder.getBean("commonService");
		String sql="select m from MobileLoc m where m.mobileNumber='"+abbresiveNumber+"'";
		MobileLoc mobileLoc=(MobileLoc)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
		return mobileLoc;
	}
	
	
	/**
	 * 电话号码前缀处理,根据本地地址决定电话号码是是否加区号
	 * @param phoneNumber 电话号码
	 * @param localArea 本地地址 可以是区号，也可以是本地地名
	 * @return
	 */
	public static String prefixProcessPostCode(String phoneNumber,String localArea){
		//截取7位 1330000
		if(phoneNumber==null) return null;
		
		if(phoneNumber.length()!=11&&phoneNumber.length()!=12)
			return phoneNumber;

		String originateNumber = phoneNumber;	// jrh 用于保存坐席拨打的原始号码
		if(phoneNumber.length()==12&&phoneNumber.startsWith("0"))
			phoneNumber=phoneNumber.substring(1);
		
		//查找指定电话号
		String abbresiveNumber=StringUtils.left(phoneNumber, 7);
		CommonService commonService=SpringContextHolder.getBean("commonService");
		String sql="select m from MobileLoc m where m.mobileNumber='"+abbresiveNumber+"'";
		MobileLoc mobileLoc=(MobileLoc)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
		if(mobileLoc==null){//不知是本地还是外地
			return originateNumber;
		}
		
		return mobileLoc.getAreaCode()+phoneNumber;
//		//可以根据本地地址判断是否加0
//		if(StringUtils.isAlphanumeric(localArea)){ //说明是地区区号
//			if(mobileLoc.getAreaCode().equals(localArea)){ //如果是本地
//				return phoneNumber;
//			}else{ //如果是外地
//				return "0"+phoneNumber;
//			}
//		}else{ //说明是地区名称
//			if(mobileLoc.getMobileArea().startsWith(localArea)){ //如果是本地
//				return phoneNumber;
//			}else{ //如果是外地
//				return "0"+phoneNumber;
//			}
//		}
	}
	
	/**
	 * 电话号码前缀处理,根据本地地址决定电话号码是加0还是去0处理
	 * @param phoneNumber 电话号码
	 * @param localArea 本地地址 可以是区号，也可以是本地地名
	 * @return
	 */
	public static String prefixProcessZero(String phoneNumber,String localArea){
		//截取7位 1330000
		if(phoneNumber==null) return null;
		
		if(phoneNumber.length()!=11&&phoneNumber.length()!=12)
			return phoneNumber;

		String originateNumber = phoneNumber;	// jrh 用于保存坐席拨打的原始号码
		
		if(phoneNumber.length()==12&&phoneNumber.startsWith("0"))
			phoneNumber=phoneNumber.substring(1);
		
		//查找指定电话号
		String abbresiveNumber=StringUtils.left(phoneNumber, 7);
		CommonService commonService=SpringContextHolder.getBean("commonService");
		String sql="select m from MobileLoc m where m.mobileNumber='"+abbresiveNumber+"'";
		MobileLoc mobileLoc=(MobileLoc)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
		if(mobileLoc==null){//jrh 不知是本地还是外地,则返回坐席实际拨打的号码
			return originateNumber;
		}
		//可以根据本地地址判断是否加0
		if(StringUtils.isAlphanumeric(localArea)){ //说明是地区区号
			if(mobileLoc.getAreaCode().equals(localArea)){ //如果是本地
				return phoneNumber;
			}else{ //如果是外地
				return "0"+phoneNumber;
			}
		}else{ //说明是地区名称
			if(mobileLoc.getMobileArea().startsWith(localArea)){ //如果是本地
				return phoneNumber;
			}else{ //如果是外地
				return "0"+phoneNumber;
			}
		}
	}
	
	/**
	 * 根据电话号码查询号码归属地
	 * @param phoneNumber
	 * @return
	 */
	public static MobileLoc searchMobileLoc(String phoneNumber){
		if(phoneNumber.length()==12&&phoneNumber.startsWith("0"))
			phoneNumber=phoneNumber.substring(1);
		
		//截取7位 1330000
		String abbresiveNumber=StringUtils.left(phoneNumber, 7);
		CommonService commonService=SpringContextHolder.getBean("commonService");
		String sql="select m from MobileLoc m where m.mobileNumber='"+abbresiveNumber+"'";
		MobileLoc mobileLoc=(MobileLoc)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
		return mobileLoc;
	}
}
