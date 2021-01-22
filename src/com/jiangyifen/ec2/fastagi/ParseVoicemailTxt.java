package com.jiangyifen.ec2.fastagi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.Voicemail;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.eaoservice.VoicemailService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * <p>解析文件夹下的txt文件到数据库</p>
 * 
 * <p>对于voicemail中生成的文件进行解析</p>
 *
 * @version $Id: ParseVoicemailTxt.java 2014-6-19 下午4:54:20 chenhb $
 *
 */
public class ParseVoicemailTxt extends BaseAgiScript {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private VoicemailService voicemailService = SpringContextHolder.getBean("voicemailService");
	
	@Override
	public void service(AgiRequest request, AgiChannel channel) throws AgiException {
		//如果path 为空处理
		String path = request.getParameter("path");
		String mode = request.getParameter("mode");
		if(StringUtils.isEmpty(path)){
			logger.warn("chenhb: path should not null");
			return;
		}else{
			logger.info("chenhb: path is "+path);
			logger.info("chenhb: mode is "+mode);
		}
		
		//遍历所有txt结尾的文件
		File fileDir=new File(path);
		File[] fileArray = fileDir.listFiles();
		for(File file:fileArray){
			String fileName = file.getName();
			if(fileName.endsWith(".txt")){
				parseTxtFileToDb(file,mode);
			}
		} 
	}

	/**
	 * 解析txt文件到数据库表
	 * @param file
	 * @param mode
	 */
	private void parseTxtFileToDb(File file, String mode) {
		
		//逐行读取文件内容
		Map<String,String> contentMap = new HashMap<String, String>();
        BufferedReader br=null;
        try { 
            br = new BufferedReader(new FileReader(file));
            String line = ""; 
            while ((line = br.readLine()) != null) {
            	//包含=号，并不以
            	if(line.contains("=")&&(!line.endsWith("="))){
            		String[] results = line.split("=");
            		if(results.length==2){
            			contentMap.put(results[0], results[1]);
            		}
            	}
            }
        }catch (Exception e) {
        	
        }finally{
            if(br!=null){
                try {
                    br.close();
                    br=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //将map中的内容解析到数据库
        String useridStr=contentMap.get("origmailbox");
        Long userid=Long.parseLong(useridStr);  //to use
        String cusnum=contentMap.get("callerid"); //to use
        String fromOutlineChannel=contentMap.get("callerchan"); //to use
        String fromOutline=""; //to use
        if(!StringUtils.isEmpty(fromOutlineChannel)){
        	try {
				fromOutline=fromOutlineChannel.substring(fromOutlineChannel.indexOf("/") + 1, fromOutlineChannel.indexOf("-"));
			} catch (Exception e) {
				e.printStackTrace();
				//ignore
			}
        }
        String origtimeTimestamp=contentMap.get("origtime");
        Date origtimeDatetime=new Date(Long.parseLong(origtimeTimestamp+"000")); //to use
        String durationStr=contentMap.get("duration"); 
        Integer duration=Integer.parseInt(durationStr); //to use
        User user = userService.get(userid);
        String partFilename="callvoicemail"+user.getDomain().getId()+"/"+useridStr+"/INBOX/"+file.getName().replaceFirst("txt", "wav");
        
        //持久化到数据库
        Voicemail voicemail=new Voicemail();
        voicemail.setUserid(userid);
        voicemail.setDomainid(user.getDomain().getId());
        voicemail.setPartFilename(partFilename);
        voicemail.setCusnum(cusnum);
        voicemail.setFromOutlineChannel(fromOutlineChannel);
        voicemail.setFromOutline(fromOutline);
        voicemail.setOrigtimeDatetime(origtimeDatetime);
        voicemail.setDuration(duration);
        voicemail.setPartFilename(partFilename);
        
        if(voicemailService.isAlreadyExists(voicemail)) {
        	//根据mode对文件做相应的处理
    		if(StringUtils.isEmpty(mode)){
    			//delete file
    			file.delete();
    		}else if("remove".equals(mode)){
    			//delete file
    			file.delete();
    		}else if("done".equals(mode)){
    			//change file name ,end with .done
    			File dst = new File(file.getAbsolutePath()+".done");
    			file.renameTo(dst);
    		}else{
    			//delete file
    			file.delete();
    		}
        } else {
        	voicemail = voicemailService.update(voicemail);
        }
        
	}
	
	public static void main(String[] args) {
//		String[] abc = "context=voicemail".split("=");
//		System.err.println(abc[0]+"------"+abc[1]);
//		System.out.println(abc.length);
		
//		File file=new File("F:/eyeBeam1.5.rar");
//		System.err.println(file.getAbsolutePath());
		Date date=new Date(Long.parseLong("1438842265000"));
		System.err.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
	}
	
}


//;
//; Message Information file
//;
//[message]
//origmailbox=2
//context=voicemail
//macrocontext=
//exten=voicemail
//rdnis=unknown
//priority=4
//callerchan=SIP/88860847043-00000016
//callerid=02161851888
//origdate=Thu Jun 19 08:04:40 AM UTC 2014
//origtime=1403165080
//category=
//flag=
//duration=11
