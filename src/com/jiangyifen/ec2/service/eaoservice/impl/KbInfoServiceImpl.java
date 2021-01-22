package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.eao.KbInfoEao;
import com.jiangyifen.ec2.eao.KbInfoTypeEao;
import com.jiangyifen.ec2.entity.KbInfo;
import com.jiangyifen.ec2.entity.KbInfoType;

import com.jiangyifen.ec2.service.eaoservice.KbInfoService;
/**
* Service实现类：知识库管理
* 
* @author lxy
* 
*/
public class KbInfoServiceImpl implements KbInfoService {

	
	/** 需要注入的Eao */
	private KbInfoEao kbInfoEao; 
	private KbInfoTypeEao kbInfoTypeEao;
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<KbInfo> loadPageEntities(int start, int length, String sql) {
		return kbInfoEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return kbInfoEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得知识库
	@Override
	public KbInfo getKbInfoById(Long id){
		return kbInfoEao.get(KbInfo.class, id);
	}
	
	// 保存知识库
	@Override
	public void saveKbInfo(KbInfo kbInfo){
		kbInfo.setCreateDate(new Date());
		kbInfo.setLastUpdateDate(new Date());
		kbInfo.setLooks(0l);
		kbInfo.setUses(0l);
		kbInfo.setStatus(1);
		kbInfoEao.save(kbInfo);
	}
	
	// 更新知识库
	@Override
	public KbInfo updateKbInfo(KbInfo kbInfo){
		return (KbInfo)kbInfoEao.update(kbInfo);
	}
	
	// 删除知识库
	@Override
	public void deleteKbInfo(KbInfo kbInfo){
		kbInfoEao.delete(kbInfo);
	}
	
	@Override
	public int getKbInfoCountByTypeId(long typeid) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(typeid);
		ids = this.getTypeIds(typeid,ids);
		if(ids.size() < 1){
			ids.add(typeid);
		}
		Long count = (Long)kbInfoEao.getEntityManager().createQuery("select count(s) from KbInfo as s where s.kbInfoType.id in :ids ").setParameter("ids", ids).getSingleResult();
		return count.intValue();
	}
	
	@Override
	public void deleteKbInfoTypeByTypeId(long typeid) {
		List<Long> ids = new ArrayList<Long>();
		ids.add(typeid);
		ids = getTypeIds(typeid,ids);
		if(ids.size() < 1){
			ids.add(typeid);
		}
		kbInfoEao.getEntityManager().createQuery("delete from KbInfoType where id in :ids ").setParameter("ids", ids).executeUpdate();
	}
	
	@Override
	public List<Long> getTypeIds(Long pid,List<Long> ids){
		String jpql = "select s from KbInfoType as s where s.parenetType.id  = " +pid;
		List<KbInfoType> kitls = kbInfoTypeEao.loadKbInfoTypeList(jpql);
		if(kitls.size() > 0){
			for (KbInfoType kbInfoType : kitls) {
				ids.add(kbInfoType.getId());
				getTypeIds(kbInfoType.getId(),ids);
			}
		}
		return ids;
	}
	
	@Override
	public List<KbInfo> getKbInfoCountByTitle(String title, KbInfoType infoType,
			Long domainid) {
		String jpql = "";
		if(null== infoType){
			jpql = "select s from KbInfo as s where s.title='"+title+"' and s.kbInfoType is null and s.domain.id = "+domainid;
		}else{
			jpql = "select s from KbInfo as s where s.title='"+title+"' and s.kbInfoType.id  = " +infoType.getId() +" and s.domain.id = "+domainid;
		}
	 
		List<KbInfo> list = kbInfoEao.loadKbInfoList(jpql);
		return list;
	}
	
	@Override
	public void deleteKbInfoById(Long id) {
		 KbInfo info = getKbInfoById(id);
		 kbInfoEao.delete(info);
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from KbInfo as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from KbInfo as s where s. = "++" order by s.ordernumber asc ";
		List<KbInfo> list = kbInfoEao.loadKbInfoList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public KbInfoEao getKbInfoEao() {
		return kbInfoEao;
	}
	
	//Eao注入
	public void setKbInfoEao(KbInfoEao kbInfoEao) {
		this.kbInfoEao = kbInfoEao;
	}
	//Eao注入
	public KbInfoTypeEao getKbInfoTypeEao() {
		return kbInfoTypeEao;
	}
	//Eao注入
	public void setKbInfoTypeEao(KbInfoTypeEao kbInfoTypeEao) {
		this.kbInfoTypeEao = kbInfoTypeEao;
	}

	

	

	

	
}