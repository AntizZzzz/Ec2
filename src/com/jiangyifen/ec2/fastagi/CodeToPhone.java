package com.jiangyifen.ec2.fastagi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 只能天相使用，没有域概念
 * 输入客户Id获取电话号码
 * @author chb

nownownownownownownownownownownow
exten => _88860610214,1,GosubIf($["${recFileName}"=""]?monitor,m,1)
exten => _88860610214,n,Agi(agi://${AGISERVERADDR}/whiteList.agi)
exten => _88860610214,n,GotoIf($["${ISWHITELIST}"!="IS"]?whitelisthangup)
exten => _88860610214,n,Read(CUS_CODE,input_cid,,,3,15)
exten => _88860610214,n,Set(CUS_CODE=${CUS_CODE})
exten => _88860610214,n,Agi(agi://${AGISERVERADDR}/codeToPhone.agi) ; get customer phone number by code
exten => _88860610214,n,GotoIf($["${CUS_PHONE}"=""]?hangup)
exten => _88860610214,n,Dial(SIP/${CUS_PHONE}@${EXTEN},60,t)
exten => _88860610214,n,Hangup()
exten => _88860610214,n(whitelisthangup),Hangup()
exten => _88860610214,n(hangup),Playback(cid_error)
exten => _88860610214,n,Hangup()

tmptmptmptmptmptmptmptmptmptmptmp
exten => _88860610214,1,GosubIf($["${recFileName}"=""]?monitor,m,1)
exten => _88860610214,n,Agi(agi://${AGISERVERADDR}/whiteList.agi)
exten => _88860610214,n,GotoIf($["${ISWHITELIST}"!="IS"]?customer)
exten => _88860610214,n,Read(CUS_CODE,input_cid,,,3,15)
exten => _88860610214,n,Set(CUS_CODE=${CUS_CODE})
exten => _88860610214,n,Agi(agi://${AGISERVERADDR}/codeToPhone.agi) ; get customer phone number by code
exten => _88860610214,n,GotoIf($["${CUS_PHONE}"=""]?hangup)
exten => _88860610214,n,Dial(SIP/${CUS_PHONE}@${EXTEN},60,t)
exten => _88860610214,n,Hangup()

exten => _88860610214,n(customer),NoOp(customer-->${CALLERID(num)})
exten => _88860610214,n,Agi(agi://${AGISERVERADDR}/lTEFinder.agi)
exten => _88860610214,n,GotoIf($["${LTEXTEN}"!=""]?dialLTE)
exten => _88860610214,n,Playback(cid_error)
exten => _88860610214,n,Hangup()
exten => _88860610214,n(dialLTE),Dial(SIP/${LTEXTEN},60,t)
exten => _88860610214,n,Hangup()

exten => _88860610214,n(hangup),Playback(cid_error)
exten => _88860610214,n,Hangup()

 */
public class CodeToPhone extends BaseAgiScript{  //AD
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void service(AgiRequest request, AgiChannel channel)
			throws AgiException {
		String code=channel.getVariable("CUS_CODE");
		/*String outlinenum=channel.getVariable("EXTEN");*/
		//cuscode都是数字
		if(StringUtils.isNumeric(code)){
			//根据用户输入的编号获取资源电话
			//目前只是给天相使用，没有进行域判断 TODO
			CommonService commonService=SpringContextHolder.getBean("commonService");
			String sql="select c from CustomerResource c where c.id="+code;
			CustomerResource customerResource=(CustomerResource)commonService.excuteSql(sql, ExecuteType.SINGLE_RESULT);
			if(customerResource==null){
				return;
			}
			
			//获取电话号码
			Set<Telephone> telephoneSet = customerResource.getTelephones();
			String cusPhone="";
			if(telephoneSet!=null&&telephoneSet.size()>0){
				List<Telephone> telephoneList=new ArrayList<Telephone>(telephoneSet);
				cusPhone=telephoneList.get(0).getNumber();
			}else{
				return;
			}
			
			if(StringUtils.isNumeric(cusPhone)){
				//do sth
				cusPhone=MobileLocUtil.prefixProcessZero(cusPhone, "021");
				channel.setVariable("CUS_PHONE",cusPhone );
			}else{
				logger.warn("chb: Could not find CUS_PHONE by CUS_CODE "+code);
			}
		}else{
			logger.warn("chb: CUS_CODE should be number");
		}
	}
	
}
