package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.VoicemailEao;
import com.jiangyifen.ec2.entity.Voicemail;
import com.jiangyifen.ec2.service.eaoservice.VoicemailService;

/**
 * 
 * 语音留言 Service 接口实现操作类
 *
 * @author jinht
 *
 * @date 2015-6-15 下午7:55:12 
 *
 */
public class VoicemailServiceImpl implements VoicemailService {

	private VoicemailEao voicemailEao;
	
	@Override
	public Voicemail get(Object primaryKey) {
		return voicemailEao.get(Voicemail.class, primaryKey);
	}

	@Override
	public void save(Voicemail voicemail) {
		voicemailEao.save(voicemail);
	}

	@Override
	public Voicemail update(Voicemail voicemail) {
		
		return (Voicemail) voicemailEao.update(voicemail);
	}

	@Override
	public void delete(Voicemail voicemail) {
		voicemailEao.delete(voicemail);
	}

	@Override
	public void deleteById(Object primaryKey) {
		voicemailEao.delete(Voicemail.class, primaryKey);
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<Voicemail> loadPageEntitiesByNativeSql(int start, int length, String nativeSql) {
		List<Voicemail> voicemailList=new ArrayList<Voicemail>();
		
		List list=voicemailEao.loadPageEntitiesByNativeSql(start, length, nativeSql);
		for(int i=0;i<list.size();i++){
			Object[] objs=(Object[])list.get(i);
			Long id=(Long)objs[0];
			Long userid=(Long)objs[1];
			String username=(String)objs[2];
			String cusnum=(String)objs[3];
			String fromOutline=(String)objs[4];
			Date origtimeDatetime=(Date)objs[5];
			Integer duration=(Integer)objs[6];
			String partfilename=(String)objs[7];

			Voicemail voicemail=new Voicemail();
			voicemail.setId(id);
			voicemail.setUserid(userid);
			voicemail.setUsername(username);
			voicemail.setCusnum(cusnum);
			voicemail.setFromOutline(fromOutline);
			voicemail.setOrigtimeDatetime(origtimeDatetime);
			voicemail.setDuration(duration);
			voicemail.setPartFilename(partfilename);
			
			voicemailList.add(voicemail);
		}
		return voicemailList;
	}

	@Override
	public boolean isAlreadyExists(Voicemail voicemail) {
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append("select v from Voicemail as v where v.userid = ");
		sbQuery.append(voicemail.getUserid());
		sbQuery.append(" and v.partFilename = '");
		sbQuery.append(voicemail.getPartFilename());
		sbQuery.append("' and v.domainid = ");
		sbQuery.append(voicemail.getDomainid());
		sbQuery.append(" and v.duration = ");
		sbQuery.append(voicemail.getDuration());
		
		List<Voicemail> voicemailList = voicemailEao.findByJpql(sbQuery.toString());
		if(voicemailList == null || voicemailList.size() == 0) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public int getEntityCountByNativeSql(String nativeSql) {
		return voicemailEao.getEntityCountByNativeSql(nativeSql);
	}

	@Override
	public List<Voicemail> loadPageEntities(int start, int length, String sql) {
		return null;
	}

	@Override
	public int getEntityCount(String sql) {
		return 0;
	}

	
	public VoicemailEao getVoicemailEao() {
		return voicemailEao;
	}

	public void setVoicemailEao(VoicemailEao voicemailEao) {
		this.voicemailEao = voicemailEao;
	}

}
